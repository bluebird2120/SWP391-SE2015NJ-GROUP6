package controller;

import dal.ReviewDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import model.PublicReview;

/**
 * NGHIỆP VỤ: Hiển thị đánh giá công khai cho khách truy cập.
 *
 * Controller hiển thị trang đánh giá công khai tại URL /reviews.
 *
 * Controller này lấy danh sách review công khai, tổng số review công khai và
 * điểm trung bình để truyền sang JSP hiển thị cho khách hàng.
 */
@WebServlet(name = "PublicReviewController", urlPatterns = {"/reviews"})
public class PublicReviewController extends HttpServlet {

    /**
     * Trang JSP dùng để hiển thị danh sách review công khai.
     */
    private static final String VIEW = "/views/customer/public-reviews.jsp";

    /**
     * Số review mặc định khi URL không truyền tham số limit.
     */
    private static final int DEFAULT_LIMIT = 30;

    /**
     * Giới hạn tối đa số review được lấy để tránh tải quá nhiều dữ liệu.
     */
    private static final int MAX_LIMIT = 100;

    private final ReviewDAO reviewDAO = new ReviewDAO();

    /**
     * Xử lý request xem trang review công khai.
     *
     * @param request request từ trình duyệt, có thể chứa tham số limit.
     * @param response response dùng để forward sang JSP.
     * @throws ServletException nếu forward JSP gặp lỗi.
     * @throws IOException nếu xử lý response gặp lỗi.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int limit = parseLimit(request.getParameter("limit"));
        // Chỉ lấy review công khai: không bị ẩn, có comment, không phải tài khoản owner tự đăng.
        List<PublicReview> reviews = reviewDAO.getPublicReviews(limit);
        // Hai chỉ số tổng quan dùng cho phần thống kê/điểm sao ở trang public.
        int totalPublicReviews = reviewDAO.countPublicReviews();
        double averageRating = reviewDAO.getPublicAverageRating();

        request.setAttribute("reviews", reviews);
        request.setAttribute("limit", limit);
        request.setAttribute("totalPublicReviews", totalPublicReviews);
        request.setAttribute("totalPublicReviewsText", formatTotalReviews(totalPublicReviews));
        request.setAttribute("averageRatingText", formatAverageRating(averageRating));
        request.setAttribute("fullAverageStars", fullAverageStars(averageRating));
        request.setAttribute("hasHalfAverageStar", hasHalfAverageStar(averageRating));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    /**
     * Chuyển tham số limit từ URL thành số review hợp lệ cần lấy.
     *
     * @param rawLimit giá trị limit thô lấy từ request.
     * @return limit hợp lệ trong khoảng cho phép.
     */
    private int parseLimit(String rawLimit) {
        if (rawLimit == null || rawLimit.isBlank()) {
            return DEFAULT_LIMIT;
        }
        try {
            int parsed = Integer.parseInt(rawLimit.trim());
            if (parsed < 1) {
                return DEFAULT_LIMIT;
            }
            return Math.min(parsed, MAX_LIMIT);
        } catch (NumberFormatException ex) {
            return DEFAULT_LIMIT;
        }
    }

    /**
     * Format điểm trung bình theo kiểu Việt Nam, ví dụ 3.5 thành 3,5.
     *
     * @param averageRating điểm trung bình dạng số.
     * @return điểm trung bình dạng chuỗi để hiển thị.
     */
    private String formatAverageRating(double averageRating) {
        return String.format(Locale.US, "%.1f", averageRating).replace('.', ',');
    }

    /**
     * Format tổng số review theo kiểu Việt Nam, ví dụ 28168 thành 28.168.
     *
     * @param totalReviews tổng số review.
     * @return tổng số review dạng chuỗi để hiển thị.
     */
    private String formatTotalReviews(int totalReviews) {
        return NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(totalReviews);
    }

    /**
     * Tính số sao đầy của điểm trung bình, không làm tròn lên.
     *
     * @param averageRating điểm trung bình.
     * @return số sao đầy từ 0 đến 5.
     */
    private int fullAverageStars(double averageRating) {
        return Math.max(0, Math.min(5, (int) Math.floor(averageRating)));
    }

    /**
     * Kiểm tra điểm trung bình có cần hiển thị nửa sao hay không.
     *
     * @param averageRating điểm trung bình.
     * @return true nếu cần hiển thị nửa sao.
     */
    private boolean hasHalfAverageStar(double averageRating) {
        return averageRating - Math.floor(averageRating) >= 0.5 && averageRating < 5;
    }
}
