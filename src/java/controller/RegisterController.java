/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dal.CustomerDAO;
import dal.EmailDAO;
import dal.EmployeeDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;
/**
 *
 * @author admin
 */
@WebServlet(name = "RegisterController", urlPatterns = {"/register"})
public class RegisterController extends HttpServlet {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("customer") != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        
        //fill thông tin khi bấm đăng kí lại tại trang verify-otp
        if (session != null) {
            Object pendingUserName = session.getAttribute("pendingUserName");
            Object pendingPhoneNumber = session.getAttribute("pendingPhoneNumber");
            Object pendingEmail = session.getAttribute("pendingEmail");

            if (pendingUserName != null) {
                request.setAttribute("userName", pendingUserName);
            }
            if (pendingPhoneNumber != null) {
                request.setAttribute("phoneNumber", pendingPhoneNumber);
            }
            if (pendingEmail != null) {
                request.setAttribute("email", pendingEmail);
            }
        }
        
        request.getRequestDispatcher("/views/register.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userName = trim(request.getParameter("userName"));
        String phoneNumber = trim(request.getParameter("phoneNumber"));
        String email = trim(request.getParameter("email"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        //Validate
        boolean hasError = false;

        //UserName
        String userNameError = validateUserName(userName);
        if (userNameError == null && customerDAO.isUserNameExists(userName, 0)) {
            userNameError = "Tên đăng nhập đã được sử dụng.";
        }
        if (userNameError != null) {
            request.setAttribute("userNameError", userNameError);
            hasError = true;
        }

        //PhoneNumber
        String phoneNumberError = validatePhone(phoneNumber);
        if (phoneNumberError == null 
                && (customerDAO.isPhoneExists(phoneNumber, 0)
                || employeeDAO.isPhoneExists(phoneNumber, 0))) {
            phoneNumberError = "Số điện thoại này đã được đăng ký hoặc không khả dụng.";
        }
        if (phoneNumberError != null) {
            request.setAttribute("phoneNumberError", phoneNumberError);
            hasError = true;
        }

        //Email
        String emailError = validateEmail(email);
        if (emailError == null 
                && (customerDAO.isEmailExists(email, 0)
                || employeeDAO.isEmailExists(email, 0))) {
            emailError = "Email này đã được đăng ký hoặc không khả dụng.";
        }
        if (emailError != null) {
            request.setAttribute("emailError", emailError);
            hasError = true;
        }

        //Password
        String passwordError = validatePassword(password);
        if (passwordError != null) {
            request.setAttribute("passwordError", passwordError);
            hasError = true;
        }
        //Confirm Password
        if (passwordError == null && !password.equals(confirmPassword)) {
            request.setAttribute("confirmError", "Mật khẩu không khớp!");
            hasError = true;
        }

        //Nếu có lỗi
        if (hasError) {
            request.setAttribute("userName", userName);
            request.setAttribute("phoneNumber", phoneNumber);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/views/register.jsp").forward(request, response);
            return;
        }
        
        //Lưu tạm thông tin đăng ký vào session (CHƯA insert vào Customer)
        //Password được hash tại đây
        String hashedPassword = util.PasswordUtil.hash(password);
        
        HttpSession session = request.getSession();
        session.setAttribute("pendingUserName", userName);
        session.setAttribute("pendingPhoneNumber", phoneNumber);
        session.setAttribute("pendingEmail", email);
        session.setAttribute("pendingPasswordHash", hashedPassword);
        
        //password để gửi qua VerifyOtpController và để fill vô form login
        session.setAttribute("pendingPassword", password);
        
        String otpCode = util.EmailService.generateOtp();
        int expireMinutes = 5;
        
        try {
            EmailDAO otpDAO = new EmailDAO();
            otpDAO.createOtp(email, otpCode, "register", expireMinutes);
            util.EmailService.sendOtpEmail(email, otpCode, expireMinutes);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("registerError", "Lỗi hệ thống. Vui lòng thử lại.");
            request.setAttribute("userName", userName);
            request.setAttribute("phoneNumber", phoneNumber);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/views/register.jsp").forward(request, response);
            return;
        } catch (jakarta.mail.MessagingException e) {
            e.printStackTrace();
            request.setAttribute("registerError", "Không gửi được email xác thực. Vui lòng kiểm tra lại email hoặc thử lại sau.");
            request.setAttribute("userName", userName);
            request.setAttribute("phoneNumber", phoneNumber);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/views/register.jsp").forward(request, response);
            return;
        }
        
        response.sendRedirect(request.getContextPath() + "/verify-otp");
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

    private String validateUserName(String userName) {
        if (userName == null || userName.isBlank()) {
            return "Vui lòng nhập tên của bạn.";
        }
        if (userName.length() < 2 || userName.length() > 50) {
            return "Tên trong khoảng từ 2-50 kí tự.";
        }
        return null;
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "Vui lòng nhập số điện thoại";
        }
        if (!phone.matches("^[0-9]{10,11}$")) {
            return "Số điện thoại phải có đúng 10-11 chữ số.";
        }
        return null;
    }

    private String validatePassword(String password) {
        if (password == null || password.isBlank()) {
            return "Vui lòng nhập mật khẩu.";
        }
        if (password.length() < 6 || password.length() > 50) {
            return "Mật khẩu phải có ít nhất 6 đến 50 ký tự.";
        }
        return null;
    }

    private String trim(String str) {
        return str == null ? "" : str.trim();
    }

}
