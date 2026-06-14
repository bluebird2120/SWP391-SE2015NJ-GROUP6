package dal;

import model.Notifications;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng Notifications.
 * Dùng chung kết nối từ DBContext.
 */
public class NotificationDAO extends DBContext {

    /**
     * Insert 1 notification, trả notificationID hoặc -1 nếu lỗi.
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
     * Lấy N notification gần nhất của 1 recipient (employee/customer).
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
     * Đánh dấu đã đọc — chỉ owner thật của notification mới đổi được.
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
     * Đếm notification chưa đọc cho badge UI.
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
