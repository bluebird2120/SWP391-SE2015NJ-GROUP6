package controller;

import dal.EmployeeDAO;
import dal.NotificationDAO;
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
import java.util.List;
import java.util.UUID;
import model.Customer;
import model.Notifications;
import model.Reviews;

@WebServlet(name = "CustomerReviewController", urlPatterns = {"/customer/reviews"})
public class CustomerReviewController extends HttpServlet {

    /*
     * NGHIỆP VỤ: Customer quản lý đánh giá của chính mình.
     *
     * Customer chỉ được đánh giá đơn hàng thuộc về họ và đã completed.
     * Mỗi order chỉ được tạo một review. Sau khi tạo, customer có thể sửa/xóa
     * review của chính họ, nhưng không thể thao tác review của customer khác.
     *
     * Luồng chính:
     * - GET /customer/reviews: xem danh sách review của customer đang đăng nhập.
     * - GET ?action=create&orderID=...: mở form đánh giá đơn đã hoàn tất.
     * - GET ?action=edit&reviewID=...: mở form sửa review của chính customer.
     * - POST create/update/delete: xử lý dữ liệu form, kiểm tra CSRF rồi gọi ReviewDAO.
     */
    private static final int MAX_COMMENT_LENGTH = 1000;
    private static final String VIEW = "/views/customer/reviews.jsp";
    private static final String CSRF_TOKEN_SESSION_KEY = "reviewCsrfToken";

    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Customer phải đăng nhập mới được xem/tạo/sửa/xóa review cá nhân.
        Customer customer = getCustomer(request);
        if (customer == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=required");
            return;
        }

        String action = request.getParameter("action");
        if ("create".equals(action)) {
            showCreateForm(request, response, customer);
            return;
        }

        if ("edit".equals(action)) {
            showEditForm(request, response, customer);
            return;
        }

        showList(request, response, customer, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        // POST là thao tác thay đổi dữ liệu nên luôn kiểm tra đăng nhập và CSRF.
        Customer customer = getCustomer(request);
        if (customer == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=required");
            return;
        }

        if (!isValidCsrfToken(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "CSRF token không hợp lệ.");
            return;
        }

        String action = request.getParameter("action");
        if ("create".equals(action)) {
            createReview(request, response, customer);
            return;
        }

        if ("update".equals(action)) {
            updateReview(request, response, customer);
            return;
        }

        if ("delete".equals(action)) {
            deleteReview(request, response, customer);
            return;
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thao tác không hợp lệ.");
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response, Customer customer)
            throws ServletException, IOException {
        // orderID lấy từ URL; chỉ đơn completed của customer này mới được đánh giá.
        int orderID = toInt(request.getParameter("orderID"), -1);
        if (orderID <= 0) {
            redirectError(request, response, "Đơn đặt bàn không hợp lệ.");
            return;
        }

        if (!reviewDAO.canCustomerReviewOrder(orderID, customer.getCustomerID())) {
            redirectError(request, response, "Chỉ có thể đánh giá đơn đã hoàn tất của bạn.");
            return;
        }

        if (reviewDAO.hasReviewedOrder(orderID, customer.getCustomerID())) {
            redirectError(request, response, "Bạn đã đánh giá đơn này.");
            return;
        }

        request.setAttribute("formMode", "create");
        request.setAttribute("orderID", orderID);
        showList(request, response, customer, null);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response, Customer customer)
            throws ServletException, IOException {
        // Lọc theo cả reviewID và customerID để customer không sửa review của người khác.
        int reviewID = toInt(request.getParameter("reviewID"), -1);
        Reviews review = reviewDAO.getReviewByIdAndCustomer(reviewID, customer.getCustomerID());
        if (review == null) {
            redirectError(request, response, "Không tìm thấy đánh giá của bạn.");
            return;
        }

        request.setAttribute("formMode", "edit");
        request.setAttribute("editingReview", review);
        showList(request, response, customer, null);
    }

    private void createReview(HttpServletRequest request, HttpServletResponse response, Customer customer)
            throws ServletException, IOException {
        int orderID = toInt(request.getParameter("orderID"), -1);
        // Validate đủ điều kiện: rating hợp lệ, order completed, chưa review order này.
        String error = validateReviewInput(request, orderID, customer.getCustomerID(), true);
        if (error != null) {
            request.setAttribute("formMode", "create");
            request.setAttribute("orderID", orderID);
            request.setAttribute("ratingValue", request.getParameter("rating"));
            request.setAttribute("commentValue", request.getParameter("comment"));
            showList(request, response, customer, error);
            return;
        }

        int rating = toInt(request.getParameter("rating"), -1);
        String comment = normalizeComment(request.getParameter("comment"));
        boolean created = reviewDAO.createReview(customer.getCustomerID(), orderID, rating, comment);
        if (!created) {
            request.setAttribute("formMode", "create");
            request.setAttribute("orderID", orderID);
            request.setAttribute("ratingValue", rating);
            request.setAttribute("commentValue", comment);
            showList(request, response, customer, "Không thể lưu đánh giá. Vui lòng thử lại.");
            return;
        }

        // Tạo review xong sẽ gửi notification cho owner biết có đánh giá mới.
        notifyOwnersAboutNewReview(customer, orderID, rating, comment);
        response.sendRedirect(request.getContextPath() + "/customer/reviews?success=created");
    }

    private void updateReview(HttpServletRequest request, HttpServletResponse response, Customer customer)
            throws ServletException, IOException {
        // Update chỉ kiểm tra rating/comment, không kiểm tra duplicate vì review đã tồn tại.
        int reviewID = toInt(request.getParameter("reviewID"), -1);
        Reviews existing = reviewDAO.getReviewByIdAndCustomer(reviewID, customer.getCustomerID());
        if (existing == null) {
            redirectError(request, response, "Không tìm thấy đánh giá của bạn.");
            return;
        }

        String error = validateRatingAndComment(request);
        if (error != null) {
            existing.setRating(toInt(request.getParameter("rating"), existing.getRating()));
            existing.setComment(normalizeComment(request.getParameter("comment")));
            request.setAttribute("formMode", "edit");
            request.setAttribute("editingReview", existing);
            showList(request, response, customer, error);
            return;
        }

        int rating = toInt(request.getParameter("rating"), -1);
        String comment = normalizeComment(request.getParameter("comment"));
        boolean updated = reviewDAO.updateReview(reviewID, customer.getCustomerID(), rating, comment);
        if (!updated) {
            request.setAttribute("formMode", "edit");
            request.setAttribute("editingReview", existing);
            showList(request, response, customer, "Không thể cập nhật đánh giá. Vui lòng thử lại.");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/customer/reviews?success=updated");
    }

    private void deleteReview(HttpServletRequest request, HttpServletResponse response, Customer customer)
            throws IOException {
        // DAO xóa kèm customerID để đảm bảo chỉ xóa review thuộc customer đang đăng nhập.
        int reviewID = toInt(request.getParameter("reviewID"), -1);
        boolean deleted = reviewDAO.deleteReview(reviewID, customer.getCustomerID());
        String result = deleted ? "deleted" : "delete_failed";
        response.sendRedirect(request.getContextPath() + "/customer/reviews?success=" + result);
    }

    private void showList(HttpServletRequest request, HttpServletResponse response, Customer customer, String error)
            throws ServletException, IOException {
        // Màn hình luôn tải lại danh sách review mới nhất của customer hiện tại.
        List<Reviews> reviews = reviewDAO.getReviewsByCustomer(customer.getCustomerID());
        request.setAttribute("reviews", reviews);
        request.setAttribute("error", error);
        request.setAttribute("csrfToken", getOrCreateCsrfToken(request));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private String validateReviewInput(HttpServletRequest request, int orderID, int customerID, boolean checkDuplicate) {
        String error = validateRatingAndComment(request);
        if (error != null) {
            return error;
        }
        if (orderID <= 0) {
            return "Đơn đặt bàn không hợp lệ.";
        }
        if (!reviewDAO.canCustomerReviewOrder(orderID, customerID)) {
            return "Chỉ có thể đánh giá đơn đã hoàn tất của bạn.";
        }
        if (checkDuplicate && reviewDAO.hasReviewedOrder(orderID, customerID)) {
            return "Bạn đã đánh giá đơn này.";
        }
        return null;
    }

    private String validateRatingAndComment(HttpServletRequest request) {
        int rating = toInt(request.getParameter("rating"), -1);
        if (rating < 1 || rating > 5) {
            return "Vui lòng chọn số sao từ 1 đến 5.";
        }

        String comment = normalizeComment(request.getParameter("comment"));
        if (comment.length() > MAX_COMMENT_LENGTH) {
            return "Nội dung bình luận không được vượt quá 1000 ký tự.";
        }
        return null;
    }

    private Customer getCustomer(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Customer) session.getAttribute("customer");
    }

    private void notifyOwnersAboutNewReview(Customer customer, int orderID, int rating, String comment) {
        try {
            // Gửi thông báo cho toàn bộ owner đang active để owner vào phản hồi review.
            List<Integer> ownerIDs = employeeDAO.getActiveOwnerIDs();
            if (ownerIDs.isEmpty()) {
                return;
            }

            String customerName = customer.getUserName() == null || customer.getUserName().isBlank()
                    ? "Khách hàng #" + customer.getCustomerID()
                    : customer.getUserName();
            String shortComment = comment == null || comment.isBlank()
                    ? "Không có bình luận"
                    : truncate(comment, 80);
            String message = customerName + " vừa gửi đánh giá " + rating
                    + " sao cho đơn #" + orderID + ": \"" + shortComment + "\"";

            for (int ownerID : ownerIDs) {
                Notifications notif = new Notifications();
                notif.setRecipientID(ownerID);
                notif.setRecipientType("staff");
                notif.setType("new_review");
                notif.setMessage(message);
                notif.setIsRead(0);
                notificationDAO.insert(notif);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private String getOrCreateCsrfToken(HttpServletRequest request) {
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
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Object token = session.getAttribute(CSRF_TOKEN_SESSION_KEY);
        String submitted = request.getParameter("csrfToken");
        return token instanceof String && submitted != null && submitted.equals(token);
    }

    private String normalizeComment(String comment) {
        return comment == null ? "" : comment.trim();
    }

    private int toInt(String value, int def) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return def;
        }
    }

    private void redirectError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        response.sendRedirect(request.getContextPath() + "/customer/reviews?error=" + encoded);
    }
}
