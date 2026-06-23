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
import jakarta.servlet.http.HttpSession;
import model.Customer;
import model.Employee;
import java.sql.SQLException;

@WebServlet(name = "ChangePasswordController", urlPatterns = {"/change-password"})
public class ChangePasswordController extends HttpServlet {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/change-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String oldPassword = trim(request.getParameter("oldPassword"));
        String newPassword = trim(request.getParameter("newPassword"));
        String confirmNewPassword = trim(request.getParameter("confirmNewPassword"));
        boolean hasError = false;

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Customer customer = (Customer) session.getAttribute("customer");
        Employee employee = (Employee) session.getAttribute("employee");

        try {

            //Old Password
            String oldPasswordError = validateOldPassword(oldPassword);
            boolean oldPasswordCorrect = false;
            
            if (oldPasswordError == null) {
                if (customer != null
                        && !customerDAO.checkCurrentPassword(customer.getCustomerID(), oldPassword)) {
                    oldPasswordError = "Mật khẩu hiện tại không đúng.";
                }
                if (employee != null
                        && !employeeDAO.checkCurrentPassword(employee.getEmployeeID(), oldPassword)) {
                    oldPasswordError = "Mật khẩu hiện tại không đúng.";
                }
                if (oldPasswordError == null) {
                    oldPasswordCorrect = true;  //chỉ true khi DB xác nhận đúng
                }
            }
            if (oldPasswordError != null) {
                request.setAttribute("oldPasswordError", oldPasswordError);
                hasError = true;
            }

            //New Password
            String newPasswordError = validateNewPassword(newPassword);
            if (newPasswordError == null && oldPasswordCorrect) {
                if (util.PasswordUtil.hash(newPassword).equals(util.PasswordUtil.hash(oldPassword))) {
                    
                }
                newPasswordError = "Mật khẩu mới phải khác mật khẩu hiện tại.";
            }
            if (newPasswordError != null) {
                request.setAttribute("newPasswordError", newPasswordError);
                hasError = true;
            }
            //Confirm New Password
            if (newPasswordError == null && !newPassword.equals(confirmNewPassword)) {
                request.setAttribute("confirmNewPasswordError", "Mật khẩu không khớp!");
                hasError = true;
            }

            if (hasError) {
                request.getRequestDispatcher("/views/change-password.jsp").forward(request, response);
                return;
            }
            String hashedPassword = util.PasswordUtil.hash(newPassword);

            if (customer != null) {
                customerDAO.updatePassword(customer.getCustomerID(), hashedPassword);
            }
            if (employee != null) {
                employeeDAO.updatePassword(employee.getEmployeeID(), hashedPassword);
            }

            session.invalidate();
            HttpSession newSession = request.getSession();
            newSession.setAttribute("successMessage", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.");
            response.sendRedirect(request.getContextPath() + "/login");
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("generalError", "Lỗi hệ thống. Vui lòng thử lại.");
            request.getRequestDispatcher("/views/change-password.jsp").forward(request, response);
            return;
        }
    }

    private String validateOldPassword(String password) {
        if (password == null || password.isBlank()) {
            return "Vui lòng nhập mật khẩu hiện tại.";
        }
        return null;
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
