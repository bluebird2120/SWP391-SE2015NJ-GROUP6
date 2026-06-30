package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.PublicReview;
import model.Reviews;
import util.UserRole;

/**
 * DAO xử lý toàn bộ thao tác dữ liệu liên quan đến bảng Reviews.
 *
 * Class này phục vụ 3 nhóm chức năng chính:
 * - Hiển thị review công khai cho khách xem.
 * - Quản lý review cá nhân của customer.
 * - Cho owner ẩn/hiện review và phản hồi công khai.
 */
public class ReviewDAO extends DBContext {

    /**
     * Danh sách cột mặc định của bảng Reviews được tái sử dụng trong nhiều câu SELECT.
     *
     * Mục đích là tránh lặp lại cùng một danh sách cột ở nhiều method, giúp các truy vấn
     * lấy review luôn đồng bộ cấu trúc dữ liệu.
     */
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
     * @return danh sách review công khai đã được lọc và sắp xếp mới nhất trước.
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

    /**
     * Lấy toàn bộ review của một customer cụ thể.
     *
     * Method này dùng cho trang "Đánh giá của tôi", chỉ trả về review thuộc đúng
     * customer đang đăng nhập.
     *
     * @param customerID ID của customer cần lấy review.
     * @return danh sách review của customer, sắp xếp theo thời gian tạo mới nhất trước.
     */
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

    /**
     * Lấy một review theo reviewID và customerID.
     *
     * Việc lọc thêm customerID giúp đảm bảo customer chỉ có thể xem/sửa/xóa review
     * thuộc về chính họ.
     *
     * @param reviewID ID của review cần tìm.
     * @param customerID ID của customer sở hữu review.
     * @return đối tượng Reviews nếu tìm thấy, ngược lại trả về null.
     */
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

    /**
     * Lấy review của một đơn hàng theo orderID và customerID.
     *
     * Method này thường được dùng để kiểm tra một đơn hàng đã được customer đánh giá
     * hay chưa.
     *
     * @param orderID ID của đơn hàng.
     * @param customerID ID của customer sở hữu đơn hàng.
     * @return review tương ứng nếu tồn tại, ngược lại trả về null.
     */
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

    /**
     * Kiểm tra customer đã đánh giá một đơn hàng hay chưa.
     *
     * @param orderID ID của đơn hàng cần kiểm tra.
     * @param customerID ID của customer cần kiểm tra.
     * @return true nếu đã có review cho đơn hàng này, false nếu chưa có.
     */
    public boolean hasReviewedOrder(int orderID, int customerID) {
        return getReviewByOrderAndCustomer(orderID, customerID) != null;
    }

    /**
     * Kiểm tra customer có đủ điều kiện đánh giá một đơn hàng hay không.
     *
     * Điều kiện hiện tại:
     * - Đơn hàng thuộc về customer đó.
     * - Trạng thái đơn hàng là completed.
     *
     * @param orderID ID của đơn hàng cần đánh giá.
     * @param customerID ID của customer đang gửi đánh giá.
     * @return true nếu customer được phép đánh giá đơn hàng, false nếu không hợp lệ.
     */
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

    /**
     * Tạo review mới cho một đơn hàng đã hoàn tất.
     *
     * Câu INSERT dùng SELECT ... WHERE EXISTS để đảm bảo ở tầng database rằng:
     * - Đơn hàng tồn tại, thuộc customer đang gửi.
     * - Đơn hàng đã completed.
     * - Customer chưa từng review đơn hàng này.
     *
     * @param customerID ID của customer tạo review.
     * @param orderID ID của đơn hàng được review.
     * @param rating số sao đánh giá.
     * @param comment nội dung bình luận.
     * @return true nếu thêm review thành công, false nếu không đủ điều kiện hoặc có lỗi.
     */
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

    /**
     * Cập nhật rating và comment của một review.
     *
     * Method lọc theo cả reviewID và customerID để tránh customer sửa review của người khác.
     *
     * @param reviewID ID của review cần cập nhật.
     * @param customerID ID của customer sở hữu review.
     * @param rating số sao mới.
     * @param comment nội dung bình luận mới.
     * @return true nếu cập nhật thành công, false nếu không tìm thấy review hoặc có lỗi.
     */
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

    /**
     * Xóa review của một customer.
     *
     * Method lọc theo cả reviewID và customerID để đảm bảo customer chỉ xóa được
     * review của chính họ.
     *
     * @param reviewID ID của review cần xóa.
     * @param customerID ID của customer sở hữu review.
     * @return true nếu xóa thành công, false nếu không tìm thấy review hoặc có lỗi.
     */
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

    /**
     * Cập nhật trạng thái ẩn/hiện của review.
     *
     * Đây là soft hide: review không bị xóa khỏi database, chỉ đổi cờ isHidden để
     * quyết định có hiển thị công khai hay không.
     *
     * @param reviewID ID của review cần cập nhật.
     * @param hidden true nếu muốn ẩn review, false nếu muốn hiện lại.
     * @return true nếu cập nhật thành công, false nếu không tìm thấy review hoặc có lỗi.
     */
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
     * Lưu hoặc xóa phản hồi của owner cho một review.
     *
     * Quy tắc xử lý:
     * - reply null hoặc rỗng: xóa phản hồi, đưa ownerReply và ownerReplyAt về NULL.
     * - reply có nội dung: lưu phản hồi sau khi trim và cập nhật ownerReplyAt = NOW().
     *
     * @param reviewID ID của review cần phản hồi.
     * @param reply nội dung phản hồi của owner, hoặc null/rỗng nếu muốn xóa phản hồi.
     * @return true nếu cập nhật thành công, false nếu không tìm thấy review hoặc có lỗi.
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

    /**
     * Lấy một review theo reviewID dành cho owner.
     *
     * Khác với getReviewByIdAndCustomer, method này không lọc theo customerID vì owner
     * có quyền quản lý toàn bộ review trong hệ thống.
     *
     * @param reviewID ID của review cần lấy.
     * @return review nếu tồn tại, ngược lại trả về null.
     */
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
     *
     * customerUserName được set qua transient field của Reviews.
     *
     * @param offset vị trí bắt đầu lấy dữ liệu, dùng cho phân trang.
     * @param limit số lượng review tối đa cần lấy.
     * @return danh sách review cho owner, kèm tên customer và sắp xếp mới nhất trước.
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

    /**
     * Đếm tổng số review trong hệ thống.
     *
     * Method này dùng để tính tổng số trang ở màn hình quản lý review của owner.
     *
     * @return tổng số review, hoặc 0 nếu có lỗi truy vấn.
     */
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

    /**
     * Chuyển một dòng ResultSet thành đối tượng Reviews.
     *
     * Method này gom logic mapping dữ liệu từ database sang model để các method SELECT
     * không phải lặp lại cùng một đoạn code set field.
     *
     * @param rs ResultSet đang trỏ tới dòng dữ liệu cần map.
     * @return đối tượng Reviews chứa dữ liệu của dòng hiện tại.
     * @throws SQLException nếu đọc dữ liệu từ ResultSet xảy ra lỗi.
     */
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
