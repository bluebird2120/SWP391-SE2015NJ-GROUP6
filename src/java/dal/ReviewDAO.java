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


    public int countPublicReviews() {
        String sql = "SELECT COUNT(*) "
                + "FROM Reviews r "
                + "JOIN Customer c ON c.customerID = r.customerID "
                + "WHERE r.isHidden = 0 "
                + "  AND r.comment IS NOT NULL AND TRIM(r.comment) <> '' "
                + "  AND NOT EXISTS ( "
                + "        SELECT 1 FROM Employee e "
                + "        WHERE e.roleID = ? "
                + "          AND ( (c.email IS NOT NULL AND e.email = c.email) "
                + "                OR (c.phoneNumber IS NOT NULL AND e.phoneNumber = c.phoneNumber) ) "
                + "  ) ";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, UserRole.RESTAURANT_OWNER.getRoleID());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }


    public double getPublicAverageRating() {
        String sql = "SELECT COALESCE(AVG(r.rating), 0) "
                + "FROM Reviews r "
                + "JOIN Customer c ON c.customerID = r.customerID "
                + "WHERE r.isHidden = 0 "
                + "  AND r.comment IS NOT NULL AND TRIM(r.comment) <> '' "
                + "  AND NOT EXISTS ( "
                + "        SELECT 1 FROM Employee e "
                + "        WHERE e.roleID = ? "
                + "          AND ( (c.email IS NOT NULL AND e.email = c.email) "
                + "                OR (c.phoneNumber IS NOT NULL AND e.phoneNumber = c.phoneNumber) ) "
                + "  ) ";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, UserRole.RESTAURANT_OWNER.getRoleID());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }


    public List<Reviews> getReviewsByCustomer(int customerID) {
        List<Reviews> reviews = new ArrayList<>();
        String sql = "SELECT reviewID, customerID, orderID, rating, comment, createdAt, "
                + "isHidden, ownerReply, ownerReplyAt "
                + "FROM Reviews WHERE customerID = ? ORDER BY createdAt DESC";

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
        String sql = "SELECT reviewID, customerID, orderID, rating, comment, createdAt, "
                + "isHidden, ownerReply, ownerReplyAt "
                + "FROM Reviews WHERE reviewID = ? AND customerID = ?";

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
        String sql = "SELECT reviewID, customerID, orderID, rating, comment, createdAt, "
                + "isHidden, ownerReply, ownerReplyAt "
                + "FROM Reviews WHERE orderID = ? AND customerID = ?";

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


    public Reviews getReviewByIdForOwner(int reviewID) {

        String sql = "SELECT reviewID, customerID, orderID, rating, comment, createdAt, "
                + "isHidden, ownerReply, ownerReplyAt "
                + "FROM Reviews WHERE reviewID = ?";

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


    public List<Reviews> getAllReviewsForOwner(int offset, int limit) {
        return getAllReviewsForOwner(offset, limit, 0);
    }


    public List<Reviews> getAllReviewsForOwner(int offset, int limit, int ratingFilter) {

        List<Reviews> reviews = new ArrayList<>();

        String sql = "SELECT r.reviewID, r.customerID, r.orderID, r.rating, r.comment, "
                + "       r.createdAt, r.isHidden, r.ownerReply, r.ownerReplyAt, "
                + "       c.userName AS customerUserName "
                + "FROM Reviews r "
                + "JOIN Customer c ON c.customerID = r.customerID "
                + (ratingFilter >= 1 && ratingFilter <= 5 ? "WHERE r.rating = ? " : "")
                + "ORDER BY r.createdAt DESC "
                + "LIMIT ? OFFSET ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            int paramIndex = 1;
            if (ratingFilter >= 1 && ratingFilter <= 5) {

                ps.setInt(paramIndex++, ratingFilter);
            }

            ps.setInt(paramIndex++, Math.max(1, limit));

            ps.setInt(paramIndex, Math.max(0, offset));
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
        return countAllReviews(0);
    }


    public int countAllReviews(int ratingFilter) {

        String sql = "SELECT COUNT(*) FROM Reviews"
                + (ratingFilter >= 1 && ratingFilter <= 5 ? " WHERE rating = ?" : "");
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (ratingFilter >= 1 && ratingFilter <= 5) {

                ps.setInt(1, ratingFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }


    public int[] countReviewsByRating() {

        int[] counts = new int[6];

        String sql = "SELECT rating, COUNT(*) AS total FROM Reviews "
                + "WHERE rating BETWEEN 1 AND 5 "
                + "GROUP BY rating";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {

                int rating = rs.getInt("rating");
                if (rating >= 1 && rating <= 5) {

                    counts[rating] = rs.getInt("total");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return counts;
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
