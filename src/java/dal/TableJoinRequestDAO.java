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
}