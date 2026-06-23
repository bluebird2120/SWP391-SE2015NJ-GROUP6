package dal;

import model.Notifications;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO extends DBContext {

    /**
     * Thêm mới một bản ghi thông báo vào cơ sở dữ liệu.
     * 
     * @param n Đối tượng Notifications chứa thông tin thông báo cần tạo
     * @return ID tự tăng của thông báo vừa tạo, hoặc -1 nếu có lỗi xảy ra
     */
    public int insert(Notifications n) {
        String sql = "INSERT INTO Notifications (recipientID, recipientType, type, message, isRead) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getRecipientID());
            ps.setString(2, n.getRecipientType());
            ps.setString(3, n.getType());
            ps.setString(4, n.getMessage());
            ps.setInt(5, n.getIsRead());
            int aff = ps.executeUpdate();
            if (aff == 0) return -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    /**
     * Lấy danh sách các thông báo gần nhất của người nhận theo số lượng giới hạn.
     * 
     * @param recipientID ID của người nhận thông báo
     * @param recipientType Loại người nhận (ví dụ: 'staff' hoặc 'customer')
     * @param limit Số lượng thông báo tối đa muốn lấy
     * @return Danh sách các đối tượng Notifications, sắp xếp theo thời gian mới nhất trước
     */
    public List<Notifications> listByRecipient(int recipientID, String recipientType, int limit) {
        List<Notifications> out = new ArrayList<>();
        String sql = "SELECT notificationID, recipientID, recipientType, type, message, isRead, createdAt "
                   + "FROM Notifications WHERE recipientID = ? AND recipientType = ? "
                   + "ORDER BY createdAt DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, recipientID);
            ps.setString(2, recipientType);
            ps.setInt(3, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notifications n = new Notifications();
                    n.setNotificationID(rs.getInt("notificationID"));
                    n.setRecipientID(rs.getInt("recipientID"));
                    n.setRecipientType(rs.getString("recipientType"));
                    n.setType(rs.getString("type"));
                    n.setMessage(rs.getString("message"));
                    n.setIsRead(rs.getInt("isRead"));
                    n.setCreatedAt(rs.getTimestamp("createdAt"));
                    out.add(n);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    /**
     * Đánh dấu một thông báo cụ thể là đã đọc.
     * Đảm bảo tính bảo mật bằng cách kiểm tra ID và loại người nhận tương ứng.
     * 
     * @param notificationID ID của thông báo cần đánh dấu
     * @param recipientID ID của người nhận
     * @param recipientType Loại người nhận
     * @return true nếu cập nhật thành công trạng thái, ngược lại false
     */
    public boolean markRead(int notificationID, int recipientID, String recipientType) {
        String sql = "UPDATE Notifications SET isRead = 1 "
                   + "WHERE notificationID = ? AND recipientID = ? AND recipientType = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, notificationID);
            ps.setInt(2, recipientID);
            ps.setString(3, recipientType);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Đếm số lượng thông báo chưa đọc của người nhận.
     * Dùng để hiển thị badge số lượng thông báo mới ở Header giao diện.
     * 
     * @param recipientID ID của người nhận
     * @param recipientType Loại người nhận ('staff' hoặc 'customer')
     * @return Số lượng thông báo chưa đọc
     */
    public int countUnread(int recipientID, String recipientType) {
        String sql = "SELECT COUNT(*) FROM Notifications "
                   + "WHERE recipientID = ? AND recipientType = ? AND isRead = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, recipientID);
            ps.setString(2, recipientType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}
