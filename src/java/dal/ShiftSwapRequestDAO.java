package dal;

import model.ShiftSwapRequests;
import model.ShiftSwapRequestDetail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftSwapRequestDAO extends DBContext {

    /**
     * Thêm mới một yêu cầu đổi ca hoặc xin nghỉ vào cơ sở dữ liệu.
     *
     * @param req Đối tượng ShiftSwapRequests chứa thông tin ca yêu cầu, ca muốn
     * đổi, lý do và loại yêu cầu ('swap' hoặc 'leave')
     * @return true nếu thêm mới thành công, ngược lại false
     */
    public boolean insert(ShiftSwapRequests req) {
        String sql = "INSERT INTO ShiftSwapRequests (requesterShiftID, targetShiftID, status, reason, requestType) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, req.getRequesterShiftID());
            if (req.getTargetShiftID() != null) {
                ps.setInt(2, req.getTargetShiftID());
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

    /**
     * Lấy toàn bộ danh sách các yêu cầu đang chờ Owner duyệt (thường là đơn xin
     * nghỉ 'leave' ở trạng thái 'pending'). Truy vấn kết hợp thông tin chi tiết
     * của người gửi yêu cầu (requester) và người liên quan (target).
     *
     * @return Danh sách chi tiết các yêu cầu đang chờ phê duyệt
     */
    public List<ShiftSwapRequestDetail> listPendingRequests() {
        List<ShiftSwapRequestDetail> list = new ArrayList<>();
        String sql = "SELECT "
                + "    ssr.swapID, ssr.requesterShiftID, ssr.targetShiftID, ssr.status, ssr.reason, ssr.createdAt, ssr.requestType, "
                + "    req_es.employeeID AS reqEmpID, req_e.fullName AS reqEmpName, req_st.shiftName AS reqShiftName, req_es.workDate AS reqWorkDate, req_st.startTime AS reqStartTime, req_st.endTime AS reqEndTime, "
                + "    tar_es.employeeID AS tarEmpID, tar_e.fullName AS tarEmpName, tar_st.shiftName AS tarShiftName, tar_es.workDate AS tarWorkDate, tar_st.startTime AS tarStartTime, tar_st.endTime AS tarEndTime "
                + "FROM ShiftSwapRequests ssr "
                + "JOIN EmployeeShifts req_es ON ssr.requesterShiftID = req_es.shiftID "
                + "JOIN Employee req_e ON req_es.employeeID = req_e.employeeID "
                + "JOIN ShiftTemplates req_st ON req_es.templateID = req_st.templateID "
                + "LEFT JOIN EmployeeShifts tar_es ON ssr.targetShiftID = tar_es.shiftID "
                + "LEFT JOIN Employee tar_e ON tar_es.employeeID = tar_e.employeeID "
                + "LEFT JOIN ShiftTemplates tar_st ON tar_es.templateID = tar_st.templateID "
                + "WHERE ssr.status = 'pending' AND ssr.requestType = 'leave' "
                + "ORDER BY ssr.createdAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ShiftSwapRequestDetail d = new ShiftSwapRequestDetail();
                d.setSwapID(rs.getInt("swapID"));
                d.setRequesterShiftID(rs.getInt("requesterShiftID"));

                int tarShiftID = rs.getInt("targetShiftID");
                d.setTargetShiftID(rs.wasNull() ? null : tarShiftID);

                d.setStatus(rs.getString("status"));
                d.setReason(rs.getString("reason"));
                d.setCreatedAt(rs.getTimestamp("createdAt"));
                d.setRequestType(rs.getString("requestType"));

                d.setReqEmployeeID(rs.getInt("reqEmpID"));
                d.setReqEmployeeName(rs.getString("reqEmpName"));
                d.setReqShiftName(rs.getString("reqShiftName"));
                d.setReqWorkDate(rs.getString("reqWorkDate") != null ? java.sql.Date.valueOf(rs.getString("reqWorkDate")) : null);
                d.setReqStartTime(rs.getTime("reqStartTime"));
                d.setReqEndTime(rs.getTime("reqEndTime"));

                int tarEmpID = rs.getInt("tarEmpID");
                if (!rs.wasNull()) {
                    d.setTargetEmployeeID(tarEmpID);
                    d.setTargetEmployeeName(rs.getString("tarEmpName"));
                    d.setTargetShiftName(rs.getString("tarShiftName"));
                    d.setTargetWorkDate(rs.getString("tarWorkDate") != null ? java.sql.Date.valueOf(rs.getString("tarWorkDate")) : null);
                    d.setTargetStartTime(rs.getTime("tarStartTime"));
                    d.setTargetEndTime(rs.getTime("tarEndTime"));
                }
                list.add(d);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy thông tin chi tiết của một yêu cầu đổi ca/xin nghỉ cụ thể theo ID.
     *
     * @param swapID ID của yêu cầu đổi ca/xin nghỉ
     * @return Đối tượng ShiftSwapRequestDetail chứa đầy đủ thông tin cá nhân và
     * thời gian ca làm việc liên quan
     */
    public ShiftSwapRequestDetail getDetailById(int swapID) {
        String sql = "SELECT "
                + "    ssr.swapID, ssr.requesterShiftID, ssr.targetShiftID, ssr.status, ssr.reason, ssr.createdAt, ssr.requestType, "
                + "    req_es.employeeID AS reqEmpID, req_e.fullName AS reqEmpName, req_st.shiftName AS reqShiftName, req_es.workDate AS reqWorkDate, req_st.startTime AS reqStartTime, req_st.endTime AS reqEndTime, "
                + "    tar_es.employeeID AS tarEmpID, tar_e.fullName AS tarEmpName, tar_st.shiftName AS tarShiftName, tar_es.workDate AS tarWorkDate, tar_st.startTime AS tarStartTime, tar_st.endTime AS tarEndTime "
                + "FROM ShiftSwapRequests ssr "
                + "JOIN EmployeeShifts req_es ON ssr.requesterShiftID = req_es.shiftID "
                + "JOIN Employee req_e ON req_es.employeeID = req_e.employeeID "
                + "JOIN ShiftTemplates req_st ON req_es.templateID = req_st.templateID "
                + "LEFT JOIN EmployeeShifts tar_es ON ssr.targetShiftID = tar_es.shiftID "
                + "LEFT JOIN Employee tar_e ON tar_es.employeeID = tar_e.employeeID "
                + "LEFT JOIN ShiftTemplates tar_st ON tar_es.templateID = tar_st.templateID "
                + "WHERE ssr.swapID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, swapID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ShiftSwapRequestDetail d = new ShiftSwapRequestDetail();
                    d.setSwapID(rs.getInt("swapID"));
                    d.setRequesterShiftID(rs.getInt("requesterShiftID"));

                    int tarShiftID = rs.getInt("targetShiftID");
                    d.setTargetShiftID(rs.wasNull() ? null : tarShiftID);

                    d.setStatus(rs.getString("status"));
                    d.setReason(rs.getString("reason"));
                    d.setCreatedAt(rs.getTimestamp("createdAt"));
                    d.setRequestType(rs.getString("requestType"));

                    d.setReqEmployeeID(rs.getInt("reqEmpID"));
                    d.setReqEmployeeName(rs.getString("reqEmpName"));
                    d.setReqShiftName(rs.getString("reqShiftName"));
                    d.setReqWorkDate(rs.getString("reqWorkDate") != null ? java.sql.Date.valueOf(rs.getString("reqWorkDate")) : null);
                    d.setReqStartTime(rs.getTime("reqStartTime"));
                    d.setReqEndTime(rs.getTime("reqEndTime"));

                    int tarEmpID = rs.getInt("tarEmpID");
                    if (!rs.wasNull()) {
                        d.setTargetEmployeeID(tarEmpID);
                        d.setTargetEmployeeName(rs.getString("tarEmpName"));
                        d.setTargetShiftName(rs.getString("tarShiftName"));
                        d.setTargetWorkDate(rs.getString("tarWorkDate") != null ? java.sql.Date.valueOf(rs.getString("tarWorkDate")) : null);
                        d.setTargetStartTime(rs.getTime("tarStartTime"));
                        d.setTargetEndTime(rs.getTime("tarEndTime"));
                    }
                    return d;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật trạng thái yêu cầu xin nghỉ hoặc đổi ca.
     *
     * Flow hiện tại: - Owner dùng method này để duyệt/từ chối đơn xin nghỉ
     * (requestType = 'leave'). - Staff dùng method này khi đồng nghiệp đồng ý
     * đổi ca (requestType = 'swap', approvedByID = null).
     *
     * Khi approved: - leave: cập nhật ca của nhân viên thành 'absent'. - swap:
     * hoán đổi employeeID giữa hai ca làm việc.
     */
    public boolean updateStatus(int swapID, String status, Integer approvedByID) {
        Connection conn = connection;
        try {
            conn.setAutoCommit(false);

            String updateRequestSql = "UPDATE ShiftSwapRequests SET status = ?, approvedByID = ? WHERE swapID = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateRequestSql)) {
                ps.setString(1, status);
                if (approvedByID != null) {
                    ps.setInt(2, approvedByID);
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setInt(3, swapID);
                ps.executeUpdate();
            }

            if ("approved".equals(status)) {
                ShiftSwapRequestDetail detail = getDetailById(swapID);
                if (detail != null) {
                    if ("leave".equals(detail.getRequestType())) {
                        String updateShiftSql = "UPDATE EmployeeShifts SET status = 'absent' WHERE shiftID = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateShiftSql)) {
                            ps.setInt(1, detail.getRequesterShiftID());
                            ps.executeUpdate();
                        }
                    } else if ("swap".equals(detail.getRequestType()) && detail.getTargetShiftID() != null) {
                        int reqEmpID = detail.getReqEmployeeID();
                        int tarEmpID = detail.getTargetEmployeeID();

                        String updateReqShiftSql = "UPDATE EmployeeShifts SET employeeID = ? WHERE shiftID = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateReqShiftSql)) {
                            ps.setInt(1, tarEmpID);
                            ps.setInt(2, detail.getRequesterShiftID());
                            ps.executeUpdate();
                        }

                        String updateTarShiftSql = "UPDATE EmployeeShifts SET employeeID = ? WHERE shiftID = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateTarShiftSql)) {
                            ps.setInt(1, reqEmpID);
                            ps.setInt(2, detail.getTargetShiftID());
                            ps.executeUpdate();
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

    /**
     * Lấy bản đồ (Map) các yêu cầu đổi ca/xin nghỉ đang chờ xử lý của một nhân
     * viên cụ thể. Khóa (Key) là shiftID, Giá trị (Value) là đối tượng
     * ShiftSwapRequests. Giúp Controller kiểm tra nhanh xem một ca làm việc cụ
     * thể có đang nằm trong quá trình đổi ca hoặc xin nghỉ hay không.
     *
     * @param employeeID ID nhân viên cần kiểm tra
     * @return Bản đồ tương quan giữa ID ca làm việc và yêu cầu đang chờ xử lý
     */
    public Map<Integer, ShiftSwapRequests> getPendingRequestsMap(int employeeID) {
        Map<Integer, ShiftSwapRequests> map = new HashMap<>();
        String sql = "SELECT swapID, requesterShiftID, targetShiftID, status, reason, createdAt, requestType "
                + "FROM ShiftSwapRequests WHERE (status = 'pending' OR status = 'pending_colleague') AND "
                + "(requesterShiftID IN (SELECT shiftID FROM EmployeeShifts WHERE employeeID = ?) "
                + " OR targetShiftID IN (SELECT shiftID FROM EmployeeShifts WHERE employeeID = ?))";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            ps.setInt(2, employeeID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ShiftSwapRequests r = new ShiftSwapRequests();
                    r.setSwapID(rs.getInt("swapID"));
                    r.setRequesterShiftID(rs.getInt("requesterShiftID"));

                    int tar = rs.getInt("targetShiftID");
                    r.setTargetShiftID(rs.wasNull() ? null : tar);

                    r.setStatus(rs.getString("status"));
                    r.setReason(rs.getString("reason"));
                    r.setCreatedAt(rs.getTimestamp("createdAt"));
                    r.setRequestType(rs.getString("requestType"));

                    map.put(r.getRequesterShiftID(), r);
                    if (r.getTargetShiftID() != null) {
                        map.put(r.getTargetShiftID(), r);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return map;
    }

    /**
     * Lấy danh sách các yêu cầu đổi ca do đồng nghiệp khác gửi tới cho một nhân
     * viên cụ thể (đang chờ đồng nghiệp phản hồi).
     *
     * @param targetEmployeeID ID của nhân viên nhận được lời mời đổi ca
     * @return Danh sách chi tiết các yêu cầu đổi ca đang chờ phản hồi từ đồng
     * nghiệp
     */
    public List<ShiftSwapRequestDetail> listPendingColleagueRequests(int targetEmployeeID) {
        List<ShiftSwapRequestDetail> list = new ArrayList<>();
        String sql = "SELECT "
                + "    ssr.swapID, ssr.requesterShiftID, ssr.targetShiftID, ssr.status, ssr.reason, ssr.createdAt, ssr.requestType, "
                + "    req_es.employeeID AS reqEmpID, req_e.fullName AS reqEmpName, req_st.shiftName AS reqShiftName, req_es.workDate AS reqWorkDate, req_st.startTime AS reqStartTime, req_es.workDate AS reqWorkDate_2, req_st.endTime AS reqEndTime, "
                + "    tar_es.employeeID AS tarEmpID, tar_e.fullName AS tarEmpName, tar_st.shiftName AS tarShiftName, tar_es.workDate AS tarWorkDate, tar_st.startTime AS tarStartTime, tar_es.workDate AS tarWorkDate_2, tar_st.endTime AS tarEndTime "
                + "FROM ShiftSwapRequests ssr "
                + "JOIN EmployeeShifts req_es ON ssr.requesterShiftID = req_es.shiftID "
                + "JOIN Employee req_e ON req_es.employeeID = req_e.employeeID "
                + "JOIN ShiftTemplates req_st ON req_es.templateID = req_st.templateID "
                + "JOIN EmployeeShifts tar_es ON ssr.targetShiftID = tar_es.shiftID "
                + "JOIN Employee tar_e ON tar_es.employeeID = tar_e.employeeID "
                + "JOIN ShiftTemplates tar_st ON tar_es.templateID = tar_st.templateID "
                + "WHERE ssr.status = 'pending_colleague' AND tar_es.employeeID = ? "
                + "ORDER BY ssr.createdAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, targetEmployeeID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ShiftSwapRequestDetail d = new ShiftSwapRequestDetail();
                    d.setSwapID(rs.getInt("swapID"));
                    d.setRequesterShiftID(rs.getInt("requesterShiftID"));
                    d.setTargetShiftID(rs.getInt("targetShiftID"));
                    d.setStatus(rs.getString("status"));
                    d.setReason(rs.getString("reason"));
                    d.setCreatedAt(rs.getTimestamp("createdAt"));
                    d.setRequestType(rs.getString("requestType"));

                    d.setReqEmployeeID(rs.getInt("reqEmpID"));
                    d.setReqEmployeeName(rs.getString("reqEmpName"));
                    d.setReqShiftName(rs.getString("reqShiftName"));
                    d.setReqWorkDate(rs.getString("reqWorkDate") != null ? java.sql.Date.valueOf(rs.getString("reqWorkDate")) : null);
                    d.setReqStartTime(rs.getTime("reqStartTime"));
                    d.setReqEndTime(rs.getTime("reqEndTime"));

                    d.setTargetEmployeeID(rs.getInt("tarEmpID"));
                    d.setTargetEmployeeName(rs.getString("tarEmpName"));
                    d.setTargetShiftName(rs.getString("tarShiftName"));
                    d.setTargetWorkDate(rs.getString("tarWorkDate") != null ? java.sql.Date.valueOf(rs.getString("tarWorkDate")) : null);
                    d.setTargetStartTime(rs.getTime("tarStartTime"));
                    d.setTargetEndTime(rs.getTime("tarEndTime"));

                    list.add(d);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Cập nhật trạng thái phản hồi của đồng nghiệp đối với yêu cầu đổi ca
     * ('approved' hoặc 'rejected').
     *
     * @param swapID ID của yêu cầu đổi ca
     * @param status Trạng thái mới do đồng nghiệp chọn
     * @return true nếu cập nhật thành công trạng thái, ngược lại false
     */
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
}
