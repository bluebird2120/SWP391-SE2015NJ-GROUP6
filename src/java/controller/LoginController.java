package controller;

import dal.CustomerDAO;
import dal.EmployeeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import model.Customer;
import model.Employee;

@WebServlet(name = "LoginController", urlPatterns = {"/login"})
public class LoginController extends HttpServlet {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Nếu đã login rồi thì không vào trang login nữa
        if (session != null) {
            if (session.getAttribute("employee") != null) {
                redirectEmployee(request, response, (Employee) session.getAttribute("employee"));
                return;
            }
            if (session.getAttribute("customer") != null) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
        }

        request.getRequestDispatcher("/views/login.jsp").forward(request, response);
    }

    // ── POST: xử lý đăng nhập ──────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String phone = request.getParameter("identifier");
        String password = request.getParameter("password");
        String loginType = request.getParameter("loginType"); // "employee" hoặc "customer"

        // ── 1. Validate đầu vào ──────────────────────────────────────────
        String phoneError = validatePhone(phone);
        String passwordError = validatePassword(password);

        if (phoneError != null || passwordError != null) {
            request.setAttribute("phoneError", phoneError);
            request.setAttribute("passwordError", passwordError);
            request.setAttribute("identifier", phone);
            request.setAttribute("loginType", loginType);

            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        // ── 2. Xác thực DB theo loại tài khoản ──────────────────────────
        try {
            if ("employee".equals(loginType)) {
                handleEmployeeLogin(request, response, phone, password);
            } else {
                handleCustomerLogin(request, response, phone, password);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("loginError", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
            request.setAttribute("identifier", phone);
            request.setAttribute("loginType", loginType);

            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
        }
    }

    // ── Xử lý login Employee (Staff / Owner) ─────────────────────────────
    private void handleEmployeeLogin(HttpServletRequest request, HttpServletResponse response,
            String phone, String password)
            throws ServletException, IOException, SQLException {

        Employee employee = employeeDAO.findByPhoneAndPassword(phone, password);

        if (employee == null) {
            request.setAttribute("loginError", "Số điện thoại hoặc mật khẩu không đúng.");
            request.setAttribute("identifier", phone);
            request.setAttribute("loginType", "employee");
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        if (employee.getIsActive() == 0) {
            request.setAttribute("loginError", "Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ quản lý.");
            request.setAttribute("identifier", phone);
            request.setAttribute("loginType", "employee");
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        // Tạo session — key "employee" cho đồng nhất với header.jsp và AuthenticationFilter
        HttpSession session = request.getSession(true);
        session.setAttribute("employee", employee);
        session.setMaxInactiveInterval(30 * 60); // 30 phút

        // Bắt buộc đổi mật khẩu lần đầu
        if (employee.getMustChangePassword() == 1) {
            response.sendRedirect(request.getContextPath() + "/staff/change-password?first=true");
            return;
        }

        redirectEmployee(request, response, employee);
    }

    // ── Xử lý login Customer ─────────────────────────────────────────────
    private void handleCustomerLogin(HttpServletRequest request, HttpServletResponse response,
            String phone, String password)
            throws ServletException, IOException, SQLException {

        Customer customer = customerDAO.findByPhoneAndPassword(phone, password);

        if (customer == null) {
            request.setAttribute("loginError", "Số điện thoại hoặc mật khẩu không đúng.");
            request.setAttribute("identifier", phone);
            request.setAttribute("loginType", "customer");
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("customer", customer);
        session.setMaxInactiveInterval(30 * 60);

        String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
        if (redirectUrl != null) {
            session.removeAttribute("redirectAfterLogin");
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect(request.getContextPath() + "/");
        }
    }

    // ── Redirect Employee theo roleName ──────────────────────────────────
    private void redirectEmployee(HttpServletRequest request, HttpServletResponse response,
            Employee employee) throws IOException {
        int roleID = employee.getRoleID();
        // ID 1 for Owner
        if (roleID == 1) {
            response.sendRedirect(request.getContextPath() + "/staff/dashboard"); // Owner
        } else {
            response.sendRedirect(request.getContextPath() + "/staff/dashboard"); // Staff
        }
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "Vui lòng nhập số điện thoại.";
        }
        if (!phone.matches("\\d{10}")) {
            return "Số điện thoại phải có đúng 10 chữ số.";
        }
        return null;
    }

    private String validatePassword(String password) {
        if (password == null || password.isBlank()) {
            return "Vui lòng nhập mật khẩu.";
        }
        if (password.length() < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự.";
        }
        return null;
    }
}
