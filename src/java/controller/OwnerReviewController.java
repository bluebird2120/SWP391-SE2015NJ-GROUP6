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
import java.util.List;
import java.util.UUID;
import model.Employee;
import model.Reviews;
import util.UserRole;

/**
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isOwner(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        int page = Math.max(1, toInt(request.getParameter("page"), 1));
        int total = reviewDAO.countAllReviews();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        if (page > totalPages) {
            page = totalPages;
        }
        int offset = (page - 1) * PAGE_SIZE;

        List<Reviews> reviews = reviewDAO.getAllReviewsForOwner(offset, PAGE_SIZE);
        request.setAttribute("reviews", reviews);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalReviews", total);
        request.setAttribute("csrfToken", getOrCreateCsrfToken(request));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

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
        switch (action == null ? "" : action) {
            case "hide":
                reviewDAO.setHidden(reviewID, true);
                redirectSuccess(response, request.getContextPath(), page, "hidden");
                return;
            case "unhide":
                reviewDAO.setHidden(reviewID, false);
                redirectSuccess(response, request.getContextPath(), page, "unhidden");
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
                redirectSuccess(response, request.getContextPath(), page, "replied");
                return;
            }
            case "delete-reply":
                reviewDAO.setOwnerReply(reviewID, null);
                redirectSuccess(response, request.getContextPath(), page, "reply_deleted");
                return;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thao tác không hợp lệ.");
        }
    }

    private boolean isOwner(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object emp = session.getAttribute("employee");
        return emp instanceof Employee
                && ((Employee) emp).getRoleID() == UserRole.RESTAURANT_OWNER.getRoleID();
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

    private int toInt(String value, int def) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return def;
        }
    }

    private void redirectSuccess(HttpServletResponse response, String ctx, String page, String success)
            throws IOException {
        StringBuilder sb = new StringBuilder(ctx).append("/owner/reviews?success=").append(success);
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
