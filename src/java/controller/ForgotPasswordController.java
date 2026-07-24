/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dal.CustomerDAO;
import dal.EmployeeDAO;
import jakarta.mail.MessagingException;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Customer;
import model.Employee;
import util.EmailService;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

/**
 *
 * @author admin
 */
@WebServlet(name = "ForgotPasswordController", urlPatterns = {"/forgot-password"})
public class ForgotPasswordController extends HttpServlet {

    private static final int RESET_TOKEN_EXPIRE_MINUTES = 5;

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = trim(request.getParameter("email"));

        String emailError = validateEmail(email);
        if (emailError != null) {
            request.setAttribute("emailError", emailError);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
            return;
        }

        try {
            //Giữ lại email đã nhập cho toàn bộ phía dưới
            request.setAttribute("email", email);

            // Tìm Employee trước 
            Employee employee = employeeDAO.findByEmail(email);
            Customer customer = (employee == null) ? customerDAO.findByEmail(email) : null;
            if (customer == null && employee == null) {
                request.setAttribute("emailError", "Email này chưa được đăng kí hoặc không khả dụng");
                request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
                return;
            }

            if ((customer != null && customer.getIsActive() == 0) || (employee != null && employee.getIsActive() == 0)) {
                request.setAttribute("generalError", "Tài khoản đã bị vô hiệu hóa.");
                request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
                return;
            }

            //Nếu là tài khoản gg thì không thể reset password
            if (customer != null && "google".equalsIgnoreCase(customer.getLoginProvider())) {
                request.setAttribute("emailError", "Tài khoản này đã đăng kí bằng Google. Vui lòng đăng nhập bằng Google.");
                request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
                return;
            }

            String token = UUID.randomUUID().toString();
            Timestamp expiry = new Timestamp(System.currentTimeMillis() + RESET_TOKEN_EXPIRE_MINUTES * 60_000L);

            // ── LƯU TOKEN TRƯỚC ── để link trong email vừa gửi có thể dùng được ngay
            boolean saved;
            if (customer != null) {
                saved = customerDAO.saveResetToken(customer.getCustomerID(), token, expiry);
            } else {
                saved = employeeDAO.saveResetToken(employee.getEmployeeID(), token, expiry);
            }

            if (!saved) {
                request.setAttribute("generalError", "Lỗi hệ thống. Vui lòng thử lại sau.");
                request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
                return;
            }

            // Tự động dựng URL đầy đủ dẫn tới trang đặt lại mật khẩu kèm theo mã Token ngẫu nhiên.
            // Dựng đường dẫn động (Dynamic URL) theo môi trường:
            // - request.getScheme(): "http" hoặc "https"
            // - request.getServerName(): tên miền hoặc "localhost"
            // - request.getServerPort(): tự thêm cổng (như :8080) nếu không phải cổng chuẩn 80/443
            // - request.getContextPath(): tên ứng dụng web (Context Root)
            String resetLink = request.getScheme() + "://" + request.getServerName()
                    + (request.getServerPort() == 80 || request.getServerPort() == 443
                    ? "" : ":" + request.getServerPort())
                    + request.getContextPath() + "/reset-password?token=" + token;

            try {
                EmailService.sendResetLinkEmail(email, resetLink);
            } catch (MessagingException e) {
                e.printStackTrace();
                // Gửi email thất bại -> xóa token vừa lưu để tránh token "mồ côi" không ai dùng được
                if (customer != null) {
                    customerDAO.clearResetToken(customer.getCustomerID());
                } else {
                    employeeDAO.clearResetToken(employee.getEmployeeID());
                }
                request.setAttribute("generalError",
                        "Không gửi được email. Vui lòng kiểm tra lại địa chỉ email hoặc thử lại sau.");
                request.setAttribute("email", email);
                request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
                return;
            }

            request.setAttribute("successMessage",
                    "Liên kết đặt lại mật khẩu đã được gửi đến email " + email
                    + ". Vui lòng kiểm tra hộp thư (liên kết có hiệu lực trong " + RESET_TOKEN_EXPIRE_MINUTES + " phút).");
            request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("generalError", "Lỗi hệ thống. Vui lòng thử lại sau.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
        }
    }

    private String validateEmail(String email) {
        if (email == null || email.isBlank()) {
            return "Vui lòng nhập email của bạn.";
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,10}$")) {
            return "Email không hợp lệ.";
        }
        return null;
    }

    private String trim(String str) {
        return str == null ? "" : str.trim();
    }
}
