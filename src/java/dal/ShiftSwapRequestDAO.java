package dal;

import model.ShiftSwapRequests;
import model.ShiftSwapRequestDetail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftSwapRequestDAO extends DBContext {


    public boolean insert(ShiftSwapRequests req) {

        String sql = "INSERT INTO ShiftSwapRequests (requesterShiftID, approvedByID, status, reason, requestType) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, req.getRequesterShiftID());
            if (req.getApprovedByID() != null) {

                ps.setInt(2, req.getApprovedByID());
            } else {

                ps.setNull(2, Types.INTEGER);
            }

            ps.setString(3, req.getStatus());

            ps.setString(4, req.getReason());

            ps.setString(5, req.getRequestType());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public List<ShiftSwapRequestDetail> listPendingRequests() {
        List<ShiftSwapRequestDetail> list = new ArrayList<>();
        String sql = "SELECT "
                + "    ssr.swapID, ssr.requesterShiftID, ssr.approvedByID, ssr.status, ssr.reason, ssr.createdAt, ssr.requestType, "
                + "    req_es.employeeID AS reqEmpID, req_e.fullName AS reqEmpName, req_st.shiftName AS reqShiftName, req_es.workDate AS reqWorkDate, req_st.startTime AS reqStartTime, req_st.endTime AS reqEndTime, "
                + "    cover_e.fullName AS coverEmpName "
                + "FROM ShiftSwapRequests ssr "
                + "JOIN EmployeeShifts req_es ON ssr.requesterShiftID = req_es.shiftID "
                + "JOIN Employee req_e ON req_es.employeeID = req_e.employeeID "
                + "JOIN ShiftTemplates req_st ON req_es.templateID = req_st.templateID "
                + "LEFT JOIN Employee cover_e ON ssr.approvedByID = cover_e.employeeID "
                + "WHERE ssr.status = 'pending' AND ssr.requestType = 'leave' "
                + "ORDER BY ssr.createdAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapDetail(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }


    public ShiftSwapRequestDetail getDetailById(int swapID) {

        String sql = "SELECT "
                + "    ssr.swapID, ssr.requesterShiftID, ssr.approvedByID, ssr.status, ssr.reason, ssr.createdAt, ssr.requestType, "
                + "    req_es.employeeID AS reqEmpID, req_e.fullName AS reqEmpName, req_st.shiftName AS reqShiftName, req_es.workDate AS reqWorkDate, req_st.startTime AS reqStartTime, req_st.endTime AS reqEndTime, "
                + "    cover_e.fullName AS coverEmpName "
                + "FROM ShiftSwapRequests ssr "
                + "JOIN EmployeeShifts req_es ON ssr.requesterShiftID = req_es.shiftID "
                + "JOIN Employee req_e ON req_es.employeeID = req_e.employeeID "
                + "JOIN ShiftTemplates req_st ON req_es.templateID = req_st.templateID "
                + "LEFT JOIN Employee cover_e ON ssr.approvedByID = cover_e.employeeID "
                + "WHERE ssr.swapID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, swapID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDetail(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public boolean updateStatus(int swapID, String status, Integer approvedByID) {

        Connection conn = connection;
        try {

            conn.setAutoCommit(false);


            ShiftSwapRequestDetail detail = "approved".equals(status) ? getDetailById(swapID) : null;

            Integer storedApprovedByID = approvedByID;

            if (detail != null && "cover".equals(detail.getRequestType()) && storedApprovedByID == null) {


                storedApprovedByID = detail.getTargetEmployeeID();
            }

            String expectedStatus = "pending";
            if (detail != null && "cover".equals(detail.getRequestType())) {
                expectedStatus = "pending_colleague";
            }

            String updateRequestSql = "UPDATE ShiftSwapRequests SET status = ?, approvedByID = ? "
                    + "WHERE swapID = ? AND status = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateRequestSql)) {


                ps.setString(1, status);

                if (storedApprovedByID != null) {

                    ps.setInt(2, storedApprovedByID);
                } else {

                    ps.setNull(2, Types.INTEGER);
                }


                ps.setInt(3, swapID);

                ps.setString(4, expectedStatus);

                int affected = ps.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Yêu cầu đã được xử lý bởi người khác.");
                }
            }


            if ("approved".equals(status)) {

                if (detail != null) {

                    if ("leave".equals(detail.getRequestType())) {


                        String updateShiftSql = "UPDATE EmployeeShifts SET status = 'leave_approved' WHERE shiftID = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateShiftSql)) {

                            ps.setInt(1, detail.getRequesterShiftID());

                            ps.executeUpdate();
                        }

                    } else if ("cover".equals(detail.getRequestType()) && detail.getTargetEmployeeID() != null) {


                        String updateShiftSql = "UPDATE EmployeeShifts SET employeeID = ? WHERE shiftID = ? AND status = 'scheduled'";
                        try (PreparedStatement ps = conn.prepareStatement(updateShiftSql)) {

                            ps.setInt(1, detail.getTargetEmployeeID());

                            ps.setInt(2, detail.getRequesterShiftID());

                            if (ps.executeUpdate() == 0) {
                                throw new SQLException("Shift is no longer available for cover.");
                            }
                        }
                    }
                }
            }


            conn.commit();

            conn.setAutoCommit(true);

            return true;
        } catch (SQLException ex) {
            try {


                conn.rollback();

                conn.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            ex.printStackTrace();
            return false;
        }
    }


    public Map<Integer, ShiftSwapRequests> getPendingRequestsMap(int employeeID) {

        Map<Integer, ShiftSwapRequests> map = new HashMap<>();

        String sql = "SELECT swapID, requesterShiftID, approvedByID, status, reason, createdAt, requestType "
                + "FROM ShiftSwapRequests WHERE (status = 'pending' OR status = 'pending_colleague') AND "
                + "(requesterShiftID IN (SELECT shiftID FROM EmployeeShifts WHERE employeeID = ?) "
                + " OR approvedByID = ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, employeeID);

            ps.setInt(2, employeeID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    ShiftSwapRequests r = mapRequest(rs);


                    map.put(r.getRequesterShiftID(), r);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return map;
    }


    public List<ShiftSwapRequestDetail> listPendingColleagueRequests(int targetEmployeeID) {

        List<ShiftSwapRequestDetail> list = new ArrayList<>();

        String sql = "SELECT "
                + "    ssr.swapID, ssr.requesterShiftID, ssr.approvedByID, ssr.status, ssr.reason, ssr.createdAt, ssr.requestType, "
                + "    req_es.employeeID AS reqEmpID, req_e.fullName AS reqEmpName, req_st.shiftName AS reqShiftName, req_es.workDate AS reqWorkDate, req_st.startTime AS reqStartTime, req_st.endTime AS reqEndTime, "
                + "    cover_e.fullName AS coverEmpName "
                + "FROM ShiftSwapRequests ssr "
                + "JOIN EmployeeShifts req_es ON ssr.requesterShiftID = req_es.shiftID "
                + "JOIN Employee req_e ON req_es.employeeID = req_e.employeeID "
                + "JOIN Employee target_e ON target_e.employeeID = ssr.approvedByID "
                + "JOIN ShiftTemplates req_st ON req_es.templateID = req_st.templateID "
                + "LEFT JOIN Employee cover_e ON ssr.approvedByID = cover_e.employeeID "
                + "WHERE ssr.status = 'pending_colleague' "
                + "AND ssr.requestType = 'cover' AND ssr.approvedByID = ? "
                + "AND req_e.roleID = target_e.roleID "
                + "ORDER BY ssr.createdAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, targetEmployeeID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    list.add(mapDetail(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }


    public boolean updateColleagueStatus(int swapID, String status) {

        String sql = "UPDATE ShiftSwapRequests SET status = ? WHERE swapID = ? AND status = 'pending_colleague'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, status);

            ps.setInt(2, swapID);

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private ShiftSwapRequestDetail mapDetail(ResultSet rs) throws SQLException {

        ShiftSwapRequestDetail d = new ShiftSwapRequestDetail();

        d.setSwapID(rs.getInt("swapID"));
        d.setRequesterShiftID(rs.getInt("requesterShiftID"));
        d.setStatus(rs.getString("status"));
        d.setReason(rs.getString("reason"));
        d.setCreatedAt(rs.getTimestamp("createdAt"));
        d.setRequestType(rs.getString("requestType"));

        d.setReqEmployeeID(rs.getInt("reqEmpID"));
        d.setReqEmployeeName(rs.getString("reqEmpName"));
        d.setReqShiftName(rs.getString("reqShiftName"));
        d.setReqWorkDate(rs.getDate("reqWorkDate"));
        d.setReqStartTime(rs.getTime("reqStartTime"));
        d.setReqEndTime(rs.getTime("reqEndTime"));

        int coverEmpID = rs.getInt("approvedByID");
        if (!rs.wasNull()) {
            d.setTargetEmployeeID(coverEmpID);
            d.setTargetEmployeeName(rs.getString("coverEmpName"));
        }

        return d;
    }

    private ShiftSwapRequests mapRequest(ResultSet rs) throws SQLException {

        ShiftSwapRequests r = new ShiftSwapRequests();

        r.setSwapID(rs.getInt("swapID"));
        r.setRequesterShiftID(rs.getInt("requesterShiftID"));
        r.setStatus(rs.getString("status"));
        r.setReason(rs.getString("reason"));
        r.setCreatedAt(rs.getTimestamp("createdAt"));
        r.setRequestType(rs.getString("requestType"));

        int approved = rs.getInt("approvedByID");
        r.setApprovedByID(rs.wasNull() ? null : approved);

        return r;
    }
}
