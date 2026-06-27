package controller;

import dal.CustomerDAO;
import dal.EmployeeDAO;
import dal.PermissionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import model.Customer;
import model.Employee;

@WebServlet(name = "LoginController", urlPatterns = {"/login"})
public class LoginController extends HttpServlet {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            //Đã đăng nhập thì đẩy về trang phù hợp
            if (session.getAttribute("employee") != null) {
                redirectEmployee(request, response, (Employee) session.getAttribute("employee"));
                return;
            }
            if (session.getAttribute("customer") != null) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

            String successMessage = (String) session.getAttribute("successMessage");
            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
                session.removeAttribute("successMessage");
            }
        }

        //register -> login
        if ("true".equals(request.getParameter("registered"))) {
            request.setAttribute("successMessage",
                    "Đăng ký thành công. Vui lòng đăng nhập.");
            if (session != null) {
                request.setAttribute("identifier", session.getAttribute("registeredPhone"));
                request.setAttribute("prefillPassword", session.getAttribute("registeredPassword"));

                // dùng 1 lần rồi xóa
                session.removeAttribute("registeredPhone");
                session.removeAttribute("registeredPassword");
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

            //Xóa successMessage khi login thất bại
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute("successMessage");
            }

            request.getRequestDispatcher("/views/login.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("loginError", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau");
            request.setAttribute("identifier", phone);

            //Xóa successMessage khi login thất bại
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute("successMessage");
            }

            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
        }
    }

    private void handleEmployeeLogin(HttpServletRequest request, HttpServletResponse response,
            Employee employee)
            throws ServletException, IOException, SQLException {

        if (employee.getIsActive() == 0) {
            request.setAttribute("loginError", "Tài khoản đã bị vô hiệu hóa.");
            request.setAttribute("identifier", employee.getPhoneNumber());
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("employee", employee);
        session.setMaxInactiveInterval(30 * 60);

        // Load extra permissions được cấp riêng cho nhân viên này
       Set<String> extraPerms = permissionDAO.getExtraPermissionsByEmployee(employee.getEmployeeID());
        session.setAttribute("extraPerms", extraPerms);
        
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

        if (customer.getIsActive() == 0) {
            request.setAttribute("loginError", "Tài khoản đã bị vô hiệu hóa.");
            request.setAttribute("identifier", customer.getPhoneNumber());
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
            response.sendRedirect(request.getContextPath() + "/owner/dashboard"); // Owner
        } else {
            response.sendRedirect(request.getContextPath() + "/staff/dashboard"); // Staff
        }
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "Vui lòng nhập số điện thoại.";
        }
        if (!phone.matches("\\d{10,11}")) {
            return "Số điện thoại phải có đúng 10-11 chữ số.";
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

    private String trim(String str) {
        return str == null ? "" : str.trim();
    }
}
