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

    /**
     * Thêm mới một yêu cầu làm thay hoặc xin nghỉ vào cơ sở dữ liệu.
     *
     * @param req Đối tượng ShiftSwapRequests chứa thông tin ca yêu cầu, người
     * làm thay, lý do và loại yêu cầu ('cover' hoặc 'leave')
     * @return true nếu thêm mới thành công, ngược lại false
     */
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

    /**
     * Lấy toàn bộ danh sách các yêu cầu đang chờ Owner duyệt (thường là đơn xin
     * nghỉ 'leave' ở trạng thái 'pending'). Truy vấn kết hợp thông tin chi tiết
     * của người gửi yêu cầu.
     *
     * @return Danh sách chi tiết các yêu cầu đang chờ phê duyệt
     */
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
                d.setReqWorkDate(rs.getString("reqWorkDate") != null ? java.sql.Date.valueOf(rs.getString("reqWorkDate")) : null);
                d.setReqStartTime(rs.getTime("reqStartTime"));
                d.setReqEndTime(rs.getTime("reqEndTime"));

                int coverEmpID = rs.getInt("approvedByID");
                if (!rs.wasNull()) {
                    d.setTargetEmployeeID(coverEmpID);
                    d.setTargetEmployeeName(rs.getString("coverEmpName"));
                }
                list.add(d);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy thông tin chi tiết của một yêu cầu làm thay/xin nghỉ cụ thể theo ID.
     *
     * @param swapID ID của yêu cầu làm thay/xin nghỉ
     * @return Đối tượng ShiftSwapRequestDetail chứa đầy đủ thông tin cá nhân và
     * thời gian ca làm việc liên quan
     */
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
                    d.setReqWorkDate(rs.getString("reqWorkDate") != null ? java.sql.Date.valueOf(rs.getString("reqWorkDate")) : null);
                    d.setReqStartTime(rs.getTime("reqStartTime"));
                    d.setReqEndTime(rs.getTime("reqEndTime"));

                    int coverEmpID = rs.getInt("approvedByID");
                    if (!rs.wasNull()) {
                        d.setTargetEmployeeID(coverEmpID);
                        d.setTargetEmployeeName(rs.getString("coverEmpName"));
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
     * Cập nhật trạng thái yêu cầu xin nghỉ hoặc làm thay.
     *
     * Flow hiện tại: - Owner dùng method này để duyệt/từ chối đơn xin nghỉ
     * (requestType = 'leave'). - Staff dùng method này khi đồng nghiệp đồng ý
     * làm thay lịch (requestType = 'cover').
     *
     * Khi approved: - leave: cập nhật ca của nhân viên thành 'absent'. - cover:
     * chuyển employeeID của ca sang nhân viên làm thay.
     */
    public boolean updateStatus(int swapID, String status, Integer approvedByID) {
        Connection conn = connection;
        try {
            conn.setAutoCommit(false);

            ShiftSwapRequestDetail detail = "approved".equals(status) ? getDetailById(swapID) : null;
            Integer storedApprovedByID = approvedByID;
            if (detail != null && "cover".equals(detail.getRequestType()) && storedApprovedByID == null) {
                storedApprovedByID = detail.getTargetEmployeeID();
            }

            String updateRequestSql = "UPDATE ShiftSwapRequests SET status = ?, approvedByID = ? WHERE swapID = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateRequestSql)) {
                ps.setString(1, status);
                if (storedApprovedByID != null) {
                    ps.setInt(2, storedApprovedByID);
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setInt(3, swapID);
                ps.executeUpdate();
            }

            if ("approved".equals(status)) {
                if (detail != null) {
                    if ("leave".equals(detail.getRequestType())) {
                        String updateShiftSql = "UPDATE EmployeeShifts SET status = 'absent' WHERE shiftID = ?";
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

    /**
     * Lấy bản đồ (Map) các yêu cầu làm thay/xin nghỉ đang chờ xử lý của một nhân
     * viên cụ thể. Khóa (Key) là shiftID, Giá trị (Value) là đối tượng
     * ShiftSwapRequests. Giúp Controller kiểm tra nhanh xem một ca làm việc cụ
     * thể có đang nằm trong quá trình làm thay hoặc xin nghỉ hay không.
     *
     * @param employeeID ID nhân viên cần kiểm tra
     * @return Bản đồ tương quan giữa ID ca làm việc và yêu cầu đang chờ xử lý
     */
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
                    ShiftSwapRequests r = new ShiftSwapRequests();
                    r.setSwapID(rs.getInt("swapID"));
                    r.setRequesterShiftID(rs.getInt("requesterShiftID"));

                    r.setStatus(rs.getString("status"));
                    r.setReason(rs.getString("reason"));
                    r.setCreatedAt(rs.getTimestamp("createdAt"));
                    r.setRequestType(rs.getString("requestType"));
                    int approved = rs.getInt("approvedByID");
                    r.setApprovedByID(rs.wasNull() ? null : approved);

                    map.put(r.getRequesterShiftID(), r);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return map;
    }

    /**
     * Lấy danh sách các yêu cầu làm thay do đồng nghiệp khác gửi tới cho một nhân
     * viên cụ thể (đang chờ đồng nghiệp phản hồi).
     *
     * @param targetEmployeeID ID của nhân viên nhận được lời mời làm thay
     * @return Danh sách chi tiết các yêu cầu làm thay đang chờ phản hồi từ đồng
     * nghiệp
     */
    public List<ShiftSwapRequestDetail> listPendingColleagueRequests(int targetEmployeeID) {
        List<ShiftSwapRequestDetail> list = new ArrayList<>();
        String sql = "SELECT "
                + "    ssr.swapID, ssr.requesterShiftID, ssr.approvedByID, ssr.status, ssr.reason, ssr.createdAt, ssr.requestType, "
                + "    req_es.employeeID AS reqEmpID, req_e.fullName AS reqEmpName, req_st.shiftName AS reqShiftName, req_es.workDate AS reqWorkDate, req_st.startTime AS reqStartTime, req_es.workDate AS reqWorkDate_2, req_st.endTime AS reqEndTime, "
                + "    cover_e.fullName AS coverEmpName "
                + "FROM ShiftSwapRequests ssr "
                + "JOIN EmployeeShifts req_es ON ssr.requesterShiftID = req_es.shiftID "
                + "JOIN Employee req_e ON req_es.employeeID = req_e.employeeID "
                + "JOIN ShiftTemplates req_st ON req_es.templateID = req_st.templateID "
                + "LEFT JOIN Employee cover_e ON ssr.approvedByID = cover_e.employeeID "
                + "WHERE ssr.status = 'pending_colleague' "
                + "AND ssr.requestType = 'cover' AND ssr.approvedByID = ? "
                + "ORDER BY ssr.createdAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, targetEmployeeID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    d.setReqWorkDate(rs.getString("reqWorkDate") != null ? java.sql.Date.valueOf(rs.getString("reqWorkDate")) : null);
                    d.setReqStartTime(rs.getTime("reqStartTime"));
                    d.setReqEndTime(rs.getTime("reqEndTime"));

                    int coverEmpID = rs.getInt("approvedByID");
                    if (!rs.wasNull()) {
                        d.setTargetEmployeeID(coverEmpID);
                        d.setTargetEmployeeName(rs.getString("coverEmpName"));
                    }

                    list.add(d);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Cập nhật trạng thái phản hồi của đồng nghiệp đối với yêu cầu làm thay
     * ('approved' hoặc 'rejected').
     *
     * @param swapID ID của yêu cầu làm thay
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
