/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dal.CustomerDAO;
import dal.EmployeeDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Customer;
import model.Employee;
import util.PasswordUtil;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Xử lý bước cuối của luồng quên mật khẩu: user bấm vào link trong email
 * (?token=...), nhập mật khẩu mới, hệ thống validate token rồi cập nhật DB.
 *
 * @author admin
 */
@WebServlet(name = "ResetPasswordController", urlPatterns = {"/reset-password"})
public class ResetPasswordController extends HttpServlet {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = trim(request.getParameter("token"));

        if (token.isEmpty()) {
            request.setAttribute("tokenInvalid", true);
            request.setAttribute("generalError", "Liên kết không hợp lệ.");
            request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
            return;
        }

        try {
            // Tìm Employee trước, không thấy thì tìm Customer
            Employee employee = employeeDAO.findByResetToken(token);
            Customer customer = (employee == null) ? customerDAO.findByResetToken(token) : null;

            if (employee == null && customer == null) {
                request.setAttribute("tokenInvalid", true);
                request.setAttribute("generalError", "Liên kết không hợp lệ hoặc đã được sử dụng.");
                request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
                return;
            }

            Timestamp expiry = (employee != null) ? employee.getResetTokenExpiry() : customer.getResetTokenExpiry();
            if (expiry == null || expiry.before(new Timestamp(System.currentTimeMillis()))) {
                request.setAttribute("tokenInvalid", true);
                request.setAttribute("generalError", "Liên kết đã hết hạn. Vui lòng yêu cầu link mới.");
                request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
                return;
            }

            // Token hợp lệ, giữ lại để doPost dùng
            request.setAttribute("token", token);
            request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("tokenInvalid", true);
            request.setAttribute("generalError", "Lỗi hệ thống. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = trim(request.getParameter("token"));
        String newPassword = trim(request.getParameter("newPassword"));
        String confirmNewPassword = trim(request.getParameter("confirmNewPassword"));

        if (token.isEmpty()) {
            request.setAttribute("tokenInvalid", true);
            request.setAttribute("generalError", "Liên kết không hợp lệ.");
            request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
            return;
        }

        try {
            Employee employee = employeeDAO.findByResetToken(token);
            Customer customer = (employee == null) ? customerDAO.findByResetToken(token) : null;

            if (employee == null && customer == null) {
                request.setAttribute("tokenInvalid", true);
                request.setAttribute("generalError", "Liên kết không hợp lệ hoặc đã được sử dụng.");
                request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
                return;
            }

            Timestamp expiry = (employee != null) ? employee.getResetTokenExpiry() : customer.getResetTokenExpiry();
            if (expiry == null || expiry.before(new Timestamp(System.currentTimeMillis()))) {
                request.setAttribute("tokenInvalid", true);
                request.setAttribute("generalError", "Liên kết đã hết hạn. Vui lòng yêu cầu link mới.");
                request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
                return;
            }

            request.setAttribute("token", token);

            String newPasswordError = validateNewPassword(newPassword);
            if (newPasswordError == null && !newPassword.equals(confirmNewPassword)) {
                request.setAttribute("confirmNewPasswordError", "Mật khẩu không khớp!");
            }
            if (newPasswordError != null) {
                request.setAttribute("newPasswordError", newPasswordError);
            }
            if (newPasswordError != null || !newPassword.equals(confirmNewPassword)) {
                request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
                return;
            }

            String hashedPassword = PasswordUtil.hash(newPassword);

            boolean updated;
            if (customer != null) {
                updated = customerDAO.updatePassword(customer.getCustomerID(), hashedPassword);
                if (updated) {
                    customerDAO.clearResetToken(customer.getCustomerID());
                }
            } else {
                updated = employeeDAO.updatePassword(employee.getEmployeeID(), hashedPassword);
                if (updated) {
                    employeeDAO.clearResetToken(employee.getEmployeeID());
                }
            }

            if (!updated) {
                request.setAttribute("generalError", "Lỗi hệ thống. Vui lòng thử lại sau.");
                request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
                return;
            }

            request.getSession().setAttribute("successMessage",
                    "Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới.");
            response.sendRedirect(request.getContextPath() + "/login");

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("tokenInvalid", true);
            request.setAttribute("generalError", "Lỗi hệ thống. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/views/reset-password.jsp").forward(request, response);
        }
    }

    private String validateNewPassword(String password) {
        if (password == null || password.isBlank()) {
            return "Vui lòng nhập mật khẩu mới.";
        }
        if (password.length() < 6 || password.length() > 50) {
            return "Mật khẩu phải từ 6 đến 50 ký tự.";
        }
        return null;
    }

    private String trim(String str) {
        return str == null ? "" : str.trim();
    }
}