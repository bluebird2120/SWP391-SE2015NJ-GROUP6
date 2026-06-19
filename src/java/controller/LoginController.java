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

        //register -> login
        if ("true".equals(request.getParameter("registered"))) {

            request.setAttribute(
                    "successMessage",
                    "Đăng ký thành công. Vui lòng đăng nhập."
            );

            if (session != null) {
                request.setAttribute(
                        "identifier",
                        session.getAttribute("registeredPhone")
                );

                request.setAttribute(
                        "prefillPassword",
                        session.getAttribute("registeredPassword")
                );

                session.removeAttribute("registeredPhone");
                session.removeAttribute("registeredPassword");
            }
        }

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

    //POST: xử lý đăng nhập
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String phone = trim(request.getParameter("identifier"));
        String password = request.getParameter("password");

        //Validate
        String phoneError = validatePhone(phone);
        String passwordError = validatePassword(password);

        if (phoneError != null || passwordError != null) {
            request.setAttribute("phoneError", phoneError);
            request.setAttribute("passwordError", passwordError);
            request.setAttribute("identifier", phone);
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        //Tìm Employee trước, không có thì tìm Customer
        try {
            Employee employee = employeeDAO.findByPhoneAndPassword(phone, password);
            if (employee != null) {
                handleEmployeeLogin(request, response, employee);
                return;
            }

            Customer customer = customerDAO.findByPhoneAndPassword(phone, password);
            if (customer != null) {
                handleCustomerLogin(request, response, customer);
                return;
            }

            //nếu cả 2 null -> sai thông tin
            request.setAttribute("loginError", "Số điện thoại hoặc mật khẩu không đúng!");
            request.setAttribute("identifier", phone);
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("loginError", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau");
            request.setAttribute("identifier", phone);
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
        }
    }

    private void handleEmployeeLogin(HttpServletRequest request, HttpServletResponse response,
            Employee employee) throws IOException {

        if (employee.getIsActive() == 0) {
            try {
                request.setAttribute("loginError", "Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ quản lý.");
                request.setAttribute("identifier", employee.getPhoneNumber());
                request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            } catch (ServletException e) {
                e.printStackTrace();
            }
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("employee", employee);
        session.setMaxInactiveInterval(30 * 60);

        if (employee.getMustChangePassword() == 1) {
            response.sendRedirect(request.getContextPath() + "/staff/change-password?first=true");
            return;
        }

        redirectEmployee(request, response, employee);
    }

    // ── Xử lý login Customer ─────────────────────────────────────────────
    private void handleCustomerLogin(HttpServletRequest request, HttpServletResponse response,
            Customer customer)
            throws ServletException, IOException, SQLException {

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
            response.sendRedirect(request.getContextPath() + "/owner/dashboard"); // Owner
        } else {
            response.sendRedirect(request.getContextPath() + "/staff/dashboard"); // Staff
        }
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "Vui lòng nhập số điện thoại.";
        }
        return null;
    }

    private String validatePassword(String password) {
        if (password == null || password.isBlank()) {
            return "Vui lòng nhập mật khẩu.";
        }
        return null;
    }

    private String trim(String str) {
        return str == null ? "" : str.trim();
    }
}
