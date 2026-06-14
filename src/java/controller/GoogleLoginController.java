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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@WebServlet(name = "GoogleLoginController", urlPatterns = {"/login/google", "/login/google/callback"})
public class GoogleLoginController extends HttpServlet {

    // ========================================================
    // ĐỔI 3 GIÁ TRỊ NÀY THEO THÔNG TIN GOOGLE CONSOLE CỦA BẠN
    // ========================================================
    private static final String CLIENT_ID = "1096276853074-s0bkcjnl6fdica04ie5mot0cuiifbllf.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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"; // ← điền secret thật vào đây
    private static final String REDIRECT_URI = "http://localhost:8080/Restaurant-Reservation-And-Table-Service-System/login/google/callback";
    // ========================================================

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String SCOPE = "openid email profile";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/login/google".equals(path)) {
            // Bước 1: Redirect đến Google để xin phép
            redirectToGoogle(request, response);
        } else if ("/login/google/callback".equals(path)) {
            // Bước 2: Google gọi về đây sau khi user đồng ý
            handleCallback(request, response);
        }
    }

    // ── Bước 1: Tạo URL và redirect sang Google ──────────────────────────
    private void redirectToGoogle(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Tạo state ngẫu nhiên để chống CSRF(Cross-Site Request Forgery)
        String state = java.util.UUID.randomUUID().toString();
        HttpSession session = request.getSession(true);
        session.setAttribute("oauth_state", state);
        //Bước 1: Server redirect user sang: AUTH_URL
        String googleUrl = AUTH_URL
                + "?client_id=" + encode(CLIENT_ID)
                + "&redirect_uri=" + encode(REDIRECT_URI)
                + "&response_type=code"
                + "&scope=" + encode(SCOPE)
                + "&state=" + encode(state)
                + "&access_type=offline";

        response.sendRedirect(googleUrl);
    }

    // ── Bước 2: Nhận code từ Google, đổi lấy token, lấy thông tin user ──
    private void handleCallback(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //lấy từ url
        String error = request.getParameter("error");
        if (error != null) {
            // User từ chối cấp quyền
            response.sendRedirect(request.getContextPath() + "/login?error=google_denied");
            return;
        }

        // Kiểm tra state để chống CSRF
        String returnedState = request.getParameter("state");
        //Lấy state đã lưu trong session ở redirectToGoogle
        HttpSession session = request.getSession(false);

        String savedState = session != null ? (String) session.getAttribute("oauth_state") : null;
        if (savedState == null || !savedState.equals(returnedState)) {
            response.sendRedirect(request.getContextPath() + "/login?error=state_mismatch");
            return;
        }
        //xóa state để tránh dùng lại state cũ
        session.removeAttribute("oauth_state");

        String code = request.getParameter("code");
        if (code == null || code.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/login?error=no_code");
            return;
        }

        try {
            // Đổi code lấy access_token
            String accessToken = exchangeCodeForToken(code);
            if (accessToken == null) {
                request.setAttribute("loginError", "Không thể xác thực với Google. Vui lòng thử lại.");
                request.getRequestDispatcher("/views/login.jsp").forward(request, response);
                return;
            }

            // Dùng access_token lấy thông tin user
            JSONObject userInfo = getUserInfo(USER_INFO_URL, accessToken);
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

            // Tìm hoặc tạo Customer trong DB
            CustomerDAO customerDAO = new CustomerDAO();
            Customer customer = customerDAO.findOrCreateByGoogle(email, name);

            if (customer == null) {
                request.setAttribute("loginError",
                        "Email này đã được đăng ký bằng tài khoản số điện thoại. Vui lòng đăng nhập bằng số điện thoại & mật khẩu.");
                request.getRequestDispatcher("/views/login.jsp").forward(request, response);
                return;
            }

            // Tạo session
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("customer", customer);
            newSession.setMaxInactiveInterval(30 * 60);

            // Redirect về trang trước đó hoặc trang chủ
            String redirectUrl = (String) newSession.getAttribute("redirectAfterLogin");
            if (redirectUrl != null) {
                newSession.removeAttribute("redirectAfterLogin");
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

    // ── Đổi authorization code lấy access_token ──────────────────────────
    private String exchangeCodeForToken(String code) {
        try {
            String body = "code=" + encode(code)
                    + "&client_id=" + encode(CLIENT_ID)
                    + "&client_secret=" + encode(CLIENT_SECRET)
                    + "&redirect_uri=" + encode(REDIRECT_URI)
                    + "&grant_type=authorization_code";

            //Đổi body lấy data dạng JSON dạng String lưu vào responseBody
            String responseBody = postRequest(TOKEN_URL, body);
            if (responseBody == null) {
                return null;
            }

            //Chuyển chuỗi JSON Google trả về thành object Java
            JSONObject json = (JSONObject) new JSONParser().parse(responseBody);
            return (String) json.get("access_token");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Lấy thông tin user từ Google ──────────────────────────────────────
    private JSONObject getUserInfo(String urlStr, String accessToken) {
        try {
            URL url = new URL(urlStr);
            //Tạo một đối tượng kết nối HTTP tới URL
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //Thông báo cho Google biết định dạng dữ liệu mà server sắp gửi trong body
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) {
                return null;
            }

            String responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return (JSONObject) new JSONParser().parse(responseBody);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── HTTP POST helper ──────────────────────────────────────────────────
    private String postRequest(String urlStr, String body) {
        try {
            URL url = new URL(urlStr);
            //Tạo một đối tượng kết nối HTTP tới URL
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            //Khai báo để được gửi dữ liệu đi
            conn.setDoOutput(true);
            //Thông báo cho Google biết định dạng dữ liệu mà server sắp gửi trong body
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            //                          Luồng để ghi dữ liệu đi
            try (OutputStream os = conn.getOutputStream()) {
                //Ghi dữ liệu vào luồng đó và chuyển body(String) thành mảng byte theo chuẩn UTF-8
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            //Luồng trả dữ liệu về
            InputStream is;
            if (conn.getResponseCode() < 400) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }

            // Đọc toàn bộ dữ liệu bytes từ InputStream và chuyển thành String theo chuẩn UTF-8
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Biến đổi dữ liệu thành dạng an toàn để truyền qua URL
    private String encode(String value) throws java.io.UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}
