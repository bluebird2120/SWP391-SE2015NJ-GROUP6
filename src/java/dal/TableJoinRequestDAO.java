package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.TableJoinRequest;

public class TableJoinRequestDAO extends DBContext {

    /**
     * 1. THÊM YÊU CẦU MỚI (Khi Guest quét QR và nhập tên xin vào bàn)
     */
    public boolean createJoinRequest(TableJoinRequest req) {
        String sql = "INSERT INTO TableJoinRequest (orderID, guestSessionID, guestName, status) VALUES (?, ?, ?, 'pending')";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, req.getOrderID());
            ps.setString(2, req.getGuestSessionID());
            ps.setString(3, req.getGuestName());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TableJoinRequestDAO] createJoinRequest lỗi: " + e.getMessage());
        }
        return false;
    }

    /**
     * 2. LẤY DANH SÁCH ĐANG CHỜ (Cho màn hình của HOST tải về để hiển thị Popup duyệt)
     */
    public List<TableJoinRequest> getPendingRequestsByOrderId(int orderID) {
        List<List> list = new ArrayList<>(); // Sử dụng cấu trúc mảng động giống dự án của bạn
        List<TableJoinRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM TableJoinRequest WHERE orderID = ? AND status = 'pending' ORDER BY createdAt ASC";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TableJoinRequest req = new TableJoinRequest(
                        rs.getInt("requestID"),
                        rs.getInt("orderID"),
                        rs.getString("guestSessionID"),
                        rs.getString("guestName"),
                        rs.getString("status"),
                        rs.getTimestamp("createdAt")
                    );
                    requests.add(req);
                }
            }
        } catch (SQLException e) {
            System.err.println("[TableJoinRequestDAO] getPendingRequestsByOrderId lỗi: " + e.getMessage());
        }
        return requests;
    }

    /**
     * 3. CẬP NHẬT TRẠNG THÁI (Khi HOST bấm nút "Cho phép" hoặc "Từ chối")
     */
    public boolean updateRequestStatus(int requestID, String newStatus) {
        String sql = "UPDATE TableJoinRequest SET status = ? WHERE requestID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newStatus); // 'approved' hoặc 'rejected'
            ps.setInt(2, requestID);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TableJoinRequestDAO] updateRequestStatus lỗi: " + e.getMessage());
        }
        return false;
    }

    /**
     * 4. KIỂM TRA TRẠNG THÁI HIỆN TẠI (Cho màn hình GUEST chạy ngầm kiểm tra xem mình được duyệt chưa)
     * Trả về: 'pending', 'approved', 'rejected' hoặc null nếu không tìm thấy đơn
     */
    public String checkRequestStatus(String guestSessionID, int orderID) {
        String sql = "SELECT status FROM TableJoinRequest WHERE guestSessionID = ? AND orderID = ? ORDER BY createdAt DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, guestSessionID);
            ps.setInt(2, orderID);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        } catch (SQLException e) {
            System.err.println("[TableJoinRequestDAO] checkRequestStatus lỗi: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 5. YÊU CẦU CẤP LẠI QUYỀN CHỦ BÀN (Khách hàng báo mất Session, chờ Nhân viên duyệt)
     */
    public boolean createReclaimRequest(int orderID, String guestSessionID) {
        // Đánh dấu status là 'pending_reclaim' để phân biệt với xin vào bàn thông thường
        String sql = "INSERT INTO TableJoinRequest (orderID, guestSessionID, guestName, status) VALUES (?, ?, 'YÊU CẦU LẤY LẠI QUYỀN CHỦ BÀN', 'pending_reclaim')";
        try (java.sql.PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ps.setString(2, guestSessionID);
            
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLException e) {
            System.err.println("[TableJoinRequestDAO] createReclaimRequest lỗi: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 5. UPDATE RECLAIM STATUS BY ORDER ID (Chống trùng lặp 2 máy cùng xin Host)
     */
    public boolean updateReclaimStatusByOrder(int orderID, String newStatus) {
        int targetRequestID = -1;
        
        // BƯỚC 1: Tìm ra yêu cầu xin khôi phục MỚI NHẤT của bàn này
        String getSql = "SELECT requestID FROM TableJoinRequest WHERE orderID = ? AND status = 'pending_reclaim' ORDER BY createdAt DESC LIMIT 1";
        try (java.sql.PreparedStatement psGet = connection.prepareStatement(getSql)) {
            psGet.setInt(1, orderID);
            try (java.sql.ResultSet rs = psGet.executeQuery()) {
                if (rs.next()) {
                    targetRequestID = rs.getInt("requestID");
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[TableJoinRequestDAO] Lỗi lấy RequestID: " + e.getMessage());
        }

        // Nếu tìm thấy ít nhất 1 người đang xin quyền
        if (targetRequestID != -1) {
            try {
                connection.setAutoCommit(false); // Bật Transaction để đảm bảo an toàn tuyệt đối
                
                // BƯỚC 2: Chỉ cấp quyền (Approve) cho đúng 1 máy đó
                String approveSql = "UPDATE TableJoinRequest SET status = ? WHERE requestID = ?";
                try (java.sql.PreparedStatement psApprove = connection.prepareStatement(approveSql)) {
                    psApprove.setString(1, newStatus); // Sẽ là 'approved_reclaim'
                    psApprove.setInt(2, targetRequestID);
                    psApprove.executeUpdate();
                }

                // BƯỚC 3: Đánh trượt (Reject) TẤT CẢ các máy khác đang cắn theo xin quyền của bàn này
                String rejectSql = "UPDATE TableJoinRequest SET status = 'rejected' WHERE orderID = ? AND status = 'pending_reclaim'";
                try (java.sql.PreparedStatement psReject = connection.prepareStatement(rejectSql)) {
                    psReject.setInt(1, orderID);
                    psReject.executeUpdate();
                }
                
                connection.commit(); // Lưu thay đổi
                connection.setAutoCommit(true);
                return true;
                
            } catch (java.sql.SQLException e) {
                try { connection.rollback(); } catch (java.sql.SQLException ex) {}
                System.err.println("[TableJoinRequestDAO] Lỗi cập nhật quyền Host: " + e.getMessage());
            }
        }
        return false; // Trả về false nếu không có ai xin quyền
    }
    
    /**
     * Lấy danh sách các Đơn hàng (orderID) đang có yêu cầu khôi phục quyền Chủ bàn
     */
    public java.util.List<Integer> getOrdersWithPendingReclaim() {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT orderID FROM TableJoinRequest WHERE status = 'pending_reclaim'";
        
        try (java.sql.PreparedStatement ps = connection.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getInt("orderID"));
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[TableJoinRequestDAO] getOrdersWithPendingReclaim lỗi: " + e.getMessage());
        }
        return list;
    }
}