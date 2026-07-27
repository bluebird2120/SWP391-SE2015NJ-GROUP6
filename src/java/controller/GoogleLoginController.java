package controller;

import dal.CustomerDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Customer;

import java.io.IOException;
import org.json.simple.JSONObject;
import util.GoogleUtils;

@WebServlet(name = "GoogleLoginController", urlPatterns = {"/login/google", "/login/google/callback"})
public class GoogleLoginController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/login/google".equals(path)) {
            redirectToGoogle(request, response);
        } else if ("/login/google/callback".equals(path)) {
            handleCallback(request, response);
        }
    }

    // ── Tạo URL và redirect sang Google ──────────────────────────
    private void redirectToGoogle(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Tạo state ngẫu nhiên để chống CSRF(Cross-Site Request Forgery)
        String state = java.util.UUID.randomUUID().toString();
        HttpSession session = request.getSession(true);
        session.setAttribute("oauth_state", state);
        String googleUrl = GoogleUtils.buildGoogleAuthUrl(state);
        response.sendRedirect(googleUrl);
    }

    // ── Nhận code từ Google, đổi lấy token, lấy thông tin user ──
    private void handleCallback(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //lấy từ url
        String error = request.getParameter("error");
        if (error != null) {
            response.sendRedirect(request.getContextPath() + "/login?error=google_denied");
            return;
        }

        String returnedState = request.getParameter("state");
        HttpSession session = request.getSession(false);

        String savedState = session != null ? (String) session.getAttribute("oauth_state") : null;
        
        if (savedState == null || !savedState.equals(returnedState)) {
            response.sendRedirect(request.getContextPath() + "/login?error=state_mismatch");
            return;
        }
        session.removeAttribute("oauth_state");

        String code = request.getParameter("code");
        if (code == null || code.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/login?error=no_code");
            return;
        }

        try {
            String accessToken = GoogleUtils.exchangeCodeForToken(code);
            if (accessToken == null) {
                request.setAttribute("loginError", "Không thể xác thực với Google. Vui lòng thử lại.");
                request.getRequestDispatcher("/views/login.jsp").forward(request, response);
                return;
            }

            JSONObject userInfo = GoogleUtils.getUserInfo(accessToken);
            if (userInfo == null) {
                request.setAttribute("loginError", "Không thể lấy thông tin từ Google. Vui lòng thử lại.");
                request.getRequestDispatcher("/views/login.jsp").forward(request, response);
                return;
            }

            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");

            if (email == null || email.isBlank()) {
                request.setAttribute("loginError", "Không lấy được email từ Google.");
                request.getRequestDispatcher("/views/login.jsp").forward(request, response);
                return;
            }

            CustomerDAO customerDAO = new CustomerDAO();
            Customer customer = customerDAO.findOrCreateByGoogle(email, name);

            if (customer == null) {
                request.setAttribute("loginError",
                        "Email này đã được đăng ký bằng tài khoản số điện thoại. Vui lòng đăng nhập bằng số điện thoại & mật khẩu.");
                request.getRequestDispatcher("/views/login.jsp").forward(request, response);
                return;
            }

            if (customer.getIsActive() == 0) {
                request.setAttribute("loginError", "Tài khoản đã bị vô hiệu hóa");
                request.getRequestDispatcher("/views/login.jsp").forward(request, response);
                return;
            }

            String redirectUrl = null;
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                redirectUrl = (String) oldSession.getAttribute("redirectAfterLogin");
                // bảo mật: hủy session cũ trước khi tạo mới
                oldSession.invalidate();
            }

            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("customer", customer);
            newSession.setMaxInactiveInterval(30 * 60);

            if (redirectUrl != null) {
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect(request.getContextPath() + "/");
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("loginError", "Đã xảy ra lỗi. Vui lòng thử lại.");
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
        }
    }

}
