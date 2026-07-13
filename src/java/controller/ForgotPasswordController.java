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
import util.PasswordUtil;
import java.sql.SQLException;

/**
 *
 * @author admin
 */
@WebServlet(name = "ForgotPasswordController", urlPatterns = {"/forgot-password"})
public class ForgotPasswordController extends HttpServlet {

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
            
            //Tìm Customer trước
            Customer customer = customerDAO.findByEmail(email);
            Employee employee = (customer == null) ? employeeDAO.findByEmail(email) : null;
            if (customer == null && employee == null) {
                request.setAttribute("emailError", "Email này chưa được đăng kí hoặc không khả dụng");
                request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
                return;
            }

            if ((customer != null && customer.getIsActive() == 0) || (employee != null && employee.getIsActive() == 0)) {
                request.setAttribute("generalError","Tài khoản đã bị vô hiệu hóa.");
                request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
                return;
            }

            //Nếu là tài khoản gg thì không thể reset password
            if (customer != null && "google".equalsIgnoreCase(customer.getLoginProvider())) {
                request.setAttribute("emailError", "Tài khoản này đã đăng kí bằng Google. Vui lòng đăng nhập bằng Google.");
                request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
                return;
            }

            String newPassword = EmailService.generateRandomPassword(6);
            String hashedPassword = PasswordUtil.hash(newPassword);

            // ── GỬI EMAIL TRƯỚC ── nếu fail thì không đổi mật khẩu trong DB
            // tránh tình trạng mật khẩu bị đổi nhưng user không nhận được email mới
            try {
                EmailService.sendNewPasswordEmail(email, newPassword);
            } catch (MessagingException e) {
                e.printStackTrace();
                request.setAttribute("generalError",
                        "Không gửi được email. Vui lòng kiểm tra lại địa chỉ email hoặc thử lại sau.");
                request.setAttribute("email", email);
                request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
                return;
            }

            // ── CHỈ ĐỔI MẬT KHẨU TRONG DB SAU KHI EMAIL ĐÃ GỬI THÀNH CÔNG ──
            boolean updated;
            if (customer != null) {
                updated = customerDAO.updatePassword(customer.getCustomerID(), hashedPassword);
            } else {
                updated = employeeDAO.updatePassword(employee.getEmployeeID(), hashedPassword);
            }

            if (!updated) {
                request.setAttribute("generalError", "Lỗi hệ thống. Vui lòng thử lại sau.");
                request.getRequestDispatcher("/views/forgot-password.jsp").forward(request, response);
                return;
            }

            request.setAttribute("successMessage",
                    "Mật khẩu mới đã được gửi đến email " + email + ". Vui lòng kiểm tra hộp thư để đăng nhập.");
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