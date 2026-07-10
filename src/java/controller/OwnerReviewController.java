package controller;

import dal.ReviewDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import model.Employee;
import model.Reviews;
import util.UserRole;

/**
 * NGHIỆP VỤ: Owner quản lý review và phản hồi đánh giá.
 *
 * Trang quản trị review cho Owner: ẩn/hiện review, phản hồi (reply) công khai.
 * AuthFilter đã chặn /owner/* cho non-owner. Controller giữ thêm 1 lớp defensive.
 */
@WebServlet(name = "OwnerReviewController", urlPatterns = {"/owner/reviews"})
public class OwnerReviewController extends HttpServlet {

    private static final int PAGE_SIZE = 20;
    private static final int MAX_REPLY_LENGTH = 1000;
    private static final String VIEW = "/views/owner/reviews.jsp";
    private static final String CSRF_TOKEN_SESSION_KEY = "ownerReviewCsrfToken";

    private final ReviewDAO reviewDAO = new ReviewDAO();

    /**
     * Hiển thị trang quản lý đánh giá cho owner.
     *
     * Method này đọc tham số rating để lọc danh sách review theo số sao, đồng thời
     * chuẩn bị dữ liệu báo cáo số lượng review từ 1 sao đến 5 sao.
     *
     * @param request request từ trình duyệt, có thể chứa page và rating.
     * @param response response dùng để forward sang JSP.
     * @throws ServletException nếu forward JSP gặp lỗi.
     * @throws IOException nếu xử lý response gặp lỗi.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isOwner(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // rating = 0 nghĩa là không lọc; 1..5 nghĩa là chỉ lấy review đúng số sao.
        int selectedRating = parseRatingFilter(request.getParameter("rating"));
        int page = Math.max(1, toInt(request.getParameter("page"), 1));
        int total = reviewDAO.countAllReviews(selectedRating);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        if (page > totalPages) {
            page = totalPages;
        }
        int offset = (page - 1) * PAGE_SIZE;
        int[] ratingCounts = reviewDAO.countReviewsByRating();
        int maxRatingCount = 0;
        // maxRatingCount dùng để JSP tính tỉ lệ thanh thống kê theo từng mức sao.
        for (int rating = 1; rating <= 5; rating++) {
            maxRatingCount = Math.max(maxRatingCount, ratingCounts[rating]);
        }

        // Lấy danh sách review cho owner, kèm tên customer, trạng thái ẩn/hiện và phản hồi.
        List<Reviews> reviews = reviewDAO.getAllReviewsForOwner(offset, PAGE_SIZE, selectedRating);
        request.setAttribute("reviews", reviews);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalReviews", total);
        request.setAttribute("selectedRating", selectedRating);
        request.setAttribute("ratingCounts", ratingCounts);
        request.setAttribute("ratingLevels", Arrays.asList(5, 4, 3, 2, 1));
        request.setAttribute("maxRatingCount", maxRatingCount);
        request.setAttribute("csrfToken", getOrCreateCsrfToken(request));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    /**
     * Xử lý các thao tác owner thực hiện trên review.
     *
     * Khi owner đang lọc theo một mức sao, method giữ lại rating hiện tại để sau
     * khi ẩn/hiện/phản hồi vẫn quay về đúng danh sách đang xem.
     *
     * @param request request chứa action, reviewID, page, rating và csrfToken.
     * @param response response dùng để redirect sau khi xử lý.
     * @throws ServletException nếu servlet gặp lỗi xử lý.
     * @throws IOException nếu redirect hoặc response gặp lỗi.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!isOwner(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!isValidCsrfToken(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "CSRF token không hợp lệ.");
            return;
        }

        String action = request.getParameter("action");
        int reviewID = toInt(request.getParameter("reviewID"), -1);
        if (reviewID <= 0) {
            redirectError(request, response, "Đánh giá không hợp lệ.");
            return;
        }
        if (reviewDAO.getReviewByIdForOwner(reviewID) == null) {
            redirectError(request, response, "Không tìm thấy đánh giá.");
            return;
        }

        String page = request.getParameter("page");
        String rating = request.getParameter("rating");
        // Mỗi action tương ứng với một trạng thái trong review state diagram:
        // hide/unhide đổi Visible <-> Hidden, reply tạo Replied, delete-reply bỏ Replied.
        switch (action == null ? "" : action) {
            case "hide":
                reviewDAO.setHidden(reviewID, true);
                redirectSuccess(response, request.getContextPath(), page, rating, "hidden");
                return;
            case "unhide":
                reviewDAO.setHidden(reviewID, false);
                redirectSuccess(response, request.getContextPath(), page, rating, "unhidden");
                return;
            case "reply": {
                String reply = request.getParameter("reply");
                String trimmed = reply == null ? "" : reply.trim();
                if (trimmed.isEmpty()) {
                    redirectError(request, response, "Nội dung phản hồi không được để trống.");
                    return;
                }
                if (trimmed.length() > MAX_REPLY_LENGTH) {
                    redirectError(request, response, "Phản hồi không được vượt quá 1000 ký tự.");
                    return;
                }
                reviewDAO.setOwnerReply(reviewID, trimmed);
                
                // Gửi thông báo cho khách hàng
                model.Reviews r = reviewDAO.getReviewByIdForOwner(reviewID);
                if (r != null) {
                    dal.NotificationDAO notifDAO = new dal.NotificationDAO();
                    model.Notifications notif = new model.Notifications();
                    notif.setRecipientID(r.getCustomerID());
                    notif.setRecipientType("customer");
                    notif.setType("feedback_response");
                    String truncatedReply = trimmed.length() > 50 ? trimmed.substring(0, 47) + "..." : trimmed;
                    notif.setMessage("Nhà hàng đã phản hồi đánh giá của bạn: \"" + truncatedReply + "\"");
                    notif.setIsRead(0);
                    notifDAO.insert(notif);
                }
                
                redirectSuccess(response, request.getContextPath(), page, rating, "replied");
                return;
            }
            case "delete-reply":
                reviewDAO.setOwnerReply(reviewID, null);
                redirectSuccess(response, request.getContextPath(), page, rating, "reply_deleted");
                return;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thao tác không hợp lệ.");
        }
    }

    private boolean isOwner(HttpServletRequest request) {
        // AuthFilter đã chặn /owner/*, nhưng vẫn kiểm tra lại để chống request đi vòng.
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object emp = session.getAttribute("employee");
        return emp instanceof Employee
                && ((Employee) emp).getRoleID() == UserRole.RESTAURANT_OWNER.getRoleID();
    }

    private String getOrCreateCsrfToken(HttpServletRequest request) {
        // Token lưu trong session và nhúng xuống form để chống submit giả mạo.
        HttpSession session = request.getSession(true);
        Object token = session.getAttribute(CSRF_TOKEN_SESSION_KEY);
        if (token instanceof String && !((String) token).isBlank()) {
            return (String) token;
        }
        String newToken = UUID.randomUUID().toString();
        session.setAttribute(CSRF_TOKEN_SESSION_KEY, newToken);
        return newToken;
    }

    private boolean isValidCsrfToken(HttpServletRequest request) {
        // POST thay đổi dữ liệu review nên phải có csrfToken khớp với session.
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object token = session.getAttribute(CSRF_TOKEN_SESSION_KEY);
        String submitted = request.getParameter("csrfToken");
        return token instanceof String && submitted != null && submitted.equals(token);
    }

    private int toInt(String value, int def) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * Chuẩn hóa tham số rating trên URL.
     *
     * Chỉ giá trị từ 1 đến 5 mới được dùng để lọc; các giá trị khác được hiểu là
     * không lọc và trả về 0.
     *
     * @param value giá trị rating lấy từ request.
     * @return rating hợp lệ từ 1 đến 5, hoặc 0 nếu không lọc.
     */
    private int parseRatingFilter(String value) {
        int rating = toInt(value, 0);
        return rating >= 1 && rating <= 5 ? rating : 0;
    }

    /**
     * Redirect sau khi owner thao tác thành công.
     *
     * URL redirect giữ lại page và rating hiện tại để owner không bị mất bộ lọc
     * đang chọn sau khi ẩn/hiện review hoặc gửi phản hồi.
     *
     * @param response response dùng để gửi redirect.
     * @param ctx context path của ứng dụng.
     * @param page trang hiện tại.
     * @param rating bộ lọc sao hiện tại.
     * @param success mã thông báo thành công.
     * @throws IOException nếu redirect thất bại.
     */
    private void redirectSuccess(HttpServletResponse response, String ctx, String page, String rating, String success)
            throws IOException {
        StringBuilder sb = new StringBuilder(ctx).append("/owner/reviews?success=").append(success);
        int parsedRating = parseRatingFilter(rating);
        if (parsedRating > 0) {
            sb.append("&rating=").append(parsedRating);
        }
        if (page != null && !page.isEmpty()) {
            sb.append("&page=").append(page);
        }
        response.sendRedirect(sb.toString());
    }

    private void redirectError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        response.sendRedirect(request.getContextPath() + "/owner/reviews?error=" + encoded);
    }
}
