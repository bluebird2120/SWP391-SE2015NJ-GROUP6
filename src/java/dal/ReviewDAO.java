package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.PublicReview;
import model.Reviews;
import util.UserRole;

public class ReviewDAO extends DBContext {

    /** Cột Reviews dùng cho mọi SELECT (giữ DRY). */
    private static final String REVIEW_COLUMNS =
            "reviewID, customerID, orderID, rating, comment, createdAt, "
          + "isHidden, ownerReply, ownerReplyAt";

    /**
     * Lấy danh sách review công khai để hiển thị trên trang /reviews.
     * Lọc bỏ review của customer có email hoặc phoneNumber trùng với Employee
     * thuộc role Owner (roleID = 1) — tức là ẩn comment do owner tự đăng.
     * Bỏ qua review đã bị owner ẩn (isHidden = 1).
     * Chỉ lấy review có comment khác rỗng để giao diện không bị trống.
     *
     * @param limit số lượng review tối đa muốn lấy (>0).
     */
    public List<PublicReview> getPublicReviews(int limit) {
        List<PublicReview> reviews = new ArrayList<>();
        String sql = "SELECT r.reviewID, c.userName, r.rating, r.comment, r.createdAt, "
                + "       r.ownerReply, r.ownerReplyAt "
                + "FROM Reviews r "
                + "JOIN Customer c ON c.customerID = r.customerID "
                + "WHERE r.isHidden = 0 "
                + "  AND r.comment IS NOT NULL AND TRIM(r.comment) <> '' "
                + "  AND NOT EXISTS ( "
                + "        SELECT 1 FROM Employee e "
                + "        WHERE e.roleID = ? "
                + "          AND ( (c.email IS NOT NULL AND e.email = c.email) "
                + "                OR (c.phoneNumber IS NOT NULL AND e.phoneNumber = c.phoneNumber) ) "
                + "  ) "
                + "ORDER BY r.createdAt DESC "
                + "LIMIT ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, UserRole.RESTAURANT_OWNER.getRoleID());
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PublicReview pr = new PublicReview(
                            rs.getInt("reviewID"),
                            rs.getString("userName"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            rs.getTimestamp("createdAt")
                    );
                    pr.setOwnerReply(rs.getString("ownerReply"));
                    pr.setOwnerReplyAt(rs.getTimestamp("ownerReplyAt"));
                    reviews.add(pr);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return reviews;
    }

    public List<Reviews> getReviewsByCustomer(int customerID) {
        List<Reviews> reviews = new ArrayList<>();
        String sql = "SELECT " + REVIEW_COLUMNS
                + " FROM Reviews WHERE customerID = ? ORDER BY createdAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return reviews;
    }

    public Reviews getReviewByIdAndCustomer(int reviewID, int customerID) {
        String sql = "SELECT " + REVIEW_COLUMNS
                + " FROM Reviews WHERE reviewID = ? AND customerID = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reviewID);
            ps.setInt(2, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Reviews getReviewByOrderAndCustomer(int orderID, int customerID) {
        String sql = "SELECT " + REVIEW_COLUMNS
                + " FROM Reviews WHERE orderID = ? AND customerID = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ps.setInt(2, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean hasReviewedOrder(int orderID, int customerID) {
        return getReviewByOrderAndCustomer(orderID, customerID) != null;
    }

    public boolean canCustomerReviewOrder(int orderID, int customerID) {
        String sql = "SELECT 1 FROM `Order` "
                + "WHERE orderID = ? AND customerID = ? AND orderStatus = 'completed'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ps.setInt(2, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean createReview(int customerID, Integer orderID, int rating, String comment) {
        if (orderID == null) {
            return false;
        }

        String sql = "INSERT INTO Reviews (customerID, orderID, rating, comment, createdAt) "
                + "SELECT ?, ?, ?, ?, NOW() "
                + "WHERE EXISTS (SELECT 1 FROM `Order` WHERE orderID = ? AND customerID = ? AND orderStatus = 'completed') "
                + "AND NOT EXISTS (SELECT 1 FROM Reviews WHERE orderID = ? AND customerID = ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            ps.setInt(2, orderID);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            ps.setInt(5, orderID);
            ps.setInt(6, customerID);
            ps.setInt(7, orderID);
            ps.setInt(8, customerID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean updateReview(int reviewID, int customerID, int rating, String comment) {
        String sql = "UPDATE Reviews SET rating = ?, comment = ? "
                + "WHERE reviewID = ? AND customerID = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, rating);
            ps.setString(2, comment);
            ps.setInt(3, reviewID);
            ps.setInt(4, customerID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean deleteReview(int reviewID, int customerID) {
        String sql = "DELETE FROM Reviews WHERE reviewID = ? AND customerID = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reviewID);
            ps.setInt(2, customerID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // =========================================================
    // Owner moderation
    // =========================================================

    /** Owner ẩn / hiện lại review (soft hide). */
    public boolean setHidden(int reviewID, boolean hidden) {
        String sql = "UPDATE Reviews SET isHidden = ? WHERE reviewID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, hidden ? 1 : 0);
            ps.setInt(2, reviewID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Owner đặt / xóa reply.
     * - reply rỗng / null  → xóa reply (cả 2 cột về NULL).
     * - reply có nội dung → set reply + ownerReplyAt = NOW().
     */
    public boolean setOwnerReply(int reviewID, String reply) {
        boolean clear = reply == null || reply.trim().isEmpty();
        String sql = clear
                ? "UPDATE Reviews SET ownerReply = NULL, ownerReplyAt = NULL WHERE reviewID = ?"
                : "UPDATE Reviews SET ownerReply = ?, ownerReplyAt = NOW() WHERE reviewID = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (clear) {
                ps.setInt(1, reviewID);
            } else {
                ps.setString(1, reply.trim());
                ps.setInt(2, reviewID);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /** Trả 1 review không lọc theo customer — dành cho owner moderation. */
    public Reviews getReviewByIdForOwner(int reviewID) {
        String sql = "SELECT " + REVIEW_COLUMNS + " FROM Reviews WHERE reviewID = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reviewID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Liệt kê review cho trang quản trị, kèm userName của khách.
     * customerUserName được set qua transient field của Reviews.
     */
    public List<Reviews> getAllReviewsForOwner(int offset, int limit) {
        List<Reviews> reviews = new ArrayList<>();
        String sql = "SELECT r.reviewID, r.customerID, r.orderID, r.rating, r.comment, "
                + "       r.createdAt, r.isHidden, r.ownerReply, r.ownerReplyAt, "
                + "       c.userName AS customerUserName "
                + "FROM Reviews r "
                + "JOIN Customer c ON c.customerID = r.customerID "
                + "ORDER BY r.createdAt DESC "
                + "LIMIT ? OFFSET ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            ps.setInt(2, Math.max(0, offset));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reviews review = mapRow(rs);
                    review.setCustomerUserName(rs.getString("customerUserName"));
                    reviews.add(review);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return reviews;
    }

    public int countAllReviews() {
        String sql = "SELECT COUNT(*) FROM Reviews";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private Reviews mapRow(ResultSet rs) throws SQLException {
        Reviews review = new Reviews();
        review.setReviewID(rs.getInt("reviewID"));
        review.setCustomerID(rs.getInt("customerID"));
        int orderID = rs.getInt("orderID");
        review.setOrderID(rs.wasNull() ? null : orderID);
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setCreatedAt(rs.getTimestamp("createdAt"));
        review.setIsHidden(rs.getInt("isHidden"));
        review.setOwnerReply(rs.getString("ownerReply"));
        review.setOwnerReplyAt(rs.getTimestamp("ownerReplyAt"));
        return review;
    }
}
