/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dal.CustomerDAO;
import dal.EmailDAO;
import dal.EmailDAO.VerifyResult;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import util.EmailService;

/**
 *
 * @author admin
 */
@WebServlet(name = "VerifyOtpController", urlPatterns = {"/verify-otp"})
public class VerifyOtpController extends HttpServlet {

    private static final int OTP_EXPIRE_MINUTES = 5;

    private final EmailDAO otpDAO = new EmailDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String pendingEmail = (session == null) ? null : (String) session.getAttribute("pendingEmail");

        //Không có thông tin đăng kí đang chờ -> đẩy vào register
        if (pendingEmail == null) {
            response.sendRedirect(request.getContextPath() + "/register");
            return;
        }

        request.setAttribute("pendingEmail", pendingEmail);
        request.getRequestDispatcher("/views/verify-otp.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String pendingEmail = (session == null) ? null : (String) session.getAttribute("pendingEmail");

        if (pendingEmail == null) {
            response.sendRedirect(request.getContextPath() + "/register");
            return;
        }

        String action = request.getParameter("action");

        if ("resend".equals(action)) {
            handleResend(request, response, pendingEmail);
            return;
        }

        handleVerify(request, response, session, pendingEmail);
    }

    private void handleVerify(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String pendingEmail) throws ServletException, IOException {

        String otpCode = trim(request.getParameter("otpCode"));

        if (otpCode.isBlank()) {
            request.setAttribute("otpError", "Vui lòng nhập mã OTP.");
            request.setAttribute("pendingEmail", pendingEmail);
            request.getRequestDispatcher("/views/verify-otp.jsp").forward(request, response);
            return;
        }

        try {
            VerifyResult result = otpDAO.verifyOtp(pendingEmail, otpCode, "register");

            switch (result) {
                case VALID:
                    completeRegistration(request, response, session, pendingEmail);
                    return;
                case WRONG_CODE:
                    request.setAttribute("otpError", "Mã OTP không đúng. Vui lòng thử lại.");
                    break;
                case EXPIRED:
                    request.setAttribute("otpError", "Mã OTP đã hết hạn. Vui lòng bấm \"Gửi lại mã\".");
                    break;
                case TOO_MANY_ATTEMPTS:
                    request.setAttribute("otpError", "Bạn đã nhập sai quá nhiều lần. Vui lòng bấm \"Gửi lại mã\" để nhận mã mới.");
                    break;
                case NOT_FOUND:
                default:
                    request.setAttribute("otpError", "Không tìm thấy mã OTP hợp lệ. Vui lòng bấm \"Gửi lại mã\".");
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("otpError", "Lỗi hệ thống. Vui lòng thử lại.");
        }

        request.setAttribute("pendingEmail", pendingEmail);
        request.getRequestDispatcher("/views/verify-otp.jsp").forward(request, response);
    }

    private void handleResend(HttpServletRequest request, HttpServletResponse response, 
            String pendingEmail) throws ServletException, IOException {
 
        String otpCode = EmailService.generateOtp();
 
        try {
            otpDAO.createOtp(pendingEmail, otpCode, "register", OTP_EXPIRE_MINUTES);
            EmailService.sendOtpEmail(pendingEmail, otpCode, OTP_EXPIRE_MINUTES);
            request.setAttribute("otpInfo", "Đã gửi lại mã OTP mới tới email của bạn.");
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("otpError", "Lỗi hệ thống. Vui lòng thử lại.");
        } catch (MessagingException e) {
            e.printStackTrace();
            request.setAttribute("otpError", "Không gửi được email. Vui lòng thử lại sau.");
        }
 
        request.setAttribute("pendingEmail", pendingEmail);
        request.getRequestDispatcher("/views/verify-otp.jsp").forward(request, response);
    }

    private void completeRegistration(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String pendingEmail) throws ServletException, IOException {

        String userName = (String) session.getAttribute("pendingUserName");
        String phoneNumber = (String) session.getAttribute("pendingPhoneNumber");
        String hashedPassword = (String) session.getAttribute("pendingPasswordHash");
        String rawPassword = (String) session.getAttribute("pendingPassword");
        
        try {
            //Kiểm tra lần nữa tránh trong lúc chờ nhập otp có người đăng kí trùng
            if (customerDAO.isUserNameExists(userName, 0)
                    || customerDAO.isPhoneExists(phoneNumber, 0)
                    || customerDAO.isEmailExists(pendingEmail, 0)) {
                request.setAttribute("otpError", "Thông tin đăng ký không còn khả dụng (đã được sử dụng). Vui lòng đăng ký lại.");
                clearPendingSession(session);
                request.getRequestDispatcher("/views/register.jsp").forward(request, response);
                return;
            }

            boolean created = customerDAO.registerVerified(userName, phoneNumber, pendingEmail, hashedPassword);
            if (!created) {
                request.setAttribute("otpError", "Đăng ký thất bại. Vui lòng thử lại.");
                request.setAttribute("pendingEmail", pendingEmail);
                request.getRequestDispatcher("/views/verify-otp.jsp").forward(request, response);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("otpError", "Lỗi hệ thống. Vui lòng thử lại.");
            request.setAttribute("pendingEmail", pendingEmail);
            request.getRequestDispatcher("/views/verify-otp.jsp").forward(request, response);
            return;
        }

        clearPendingSession(session);
        session.setAttribute("registeredPhone", phoneNumber);
        session.setAttribute("registeredPassword", rawPassword);

        response.sendRedirect(request.getContextPath() + "/login?registered=true");
    }

    private void clearPendingSession(HttpSession session) {
        session.removeAttribute("pendingUserName");
        session.removeAttribute("pendingPhoneNumber");
        session.removeAttribute("pendingEmail");
        session.removeAttribute("pendingPasswordHash");
        session.removeAttribute("pendingPassword");
    }

    private String trim(String str) {
        return str == null ? "" : str.trim();
    }

}
