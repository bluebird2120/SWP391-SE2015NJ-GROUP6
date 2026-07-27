/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.PasswordAuthentication;
import java.security.SecureRandom;
import java.util.Properties;

/**
 *
 * @author Admin
 */
public class EmailService {

    private static final String SENDER_EMAIL = "truongdz24112005@gmail.com";
    private static final String SENDER_APP_PASSWORD = "zdkxkyqajywsiete";
    private static final String SENDER_DISPLAY_NAME = "Vị An Restaurant";

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateOtp() {
        int number = RANDOM.nextInt(1_000_000);
        //%d in in số nguyên, độ dài 6, thiếu thì thêm 0 bên trái
        return String.format("%06d", number);
    }

    public static void sendOtpEmail(String toEmail, String otpCode, int expireMinutes)
            throws MessagingException {

        Properties props = new Properties();
        //Cấu hình
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        //Session chứa toàn bộ thông tin cấu hình và xác thực để làm việc với SMTP Server
        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_DISPLAY_NAME, "UTF-8"));
            //chuyển cho ai và chỉ định địa chỉ người nhận
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));

            message.setSubject("Mã xác thực đăng ký tài khoản", "UTF-8");

            String htmlContent = "<div style='font-family:Arial,sans-serif;max-width:480px;margin:auto'>"
                    + "<h2>Xác thực email của bạn</h2>"
                    + "<p>Mã OTP của bạn là:</p>"
                    + "<p style='font-size:28px;font-weight:bold;letter-spacing:4px;color:#d97706'>"
                    + otpCode + "</p>"
                    + "<p>Mã có hiệu lực trong <b>" + expireMinutes + " phút</b>.</p>"
                    + "<p style='color:#888;font-size:13px'>Nếu bạn không thực hiện yêu cầu này, "
                    + "vui lòng bỏ qua email.</p>"
                    + "</div>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new MessagingException("Lỗi encode email người gửi", ex);
        }
    }

//    public static String generateRandomPassword(int length) {
//        String chars = "abcdefghijklmnopqrstuvwxyz";
//        String charsUpper = chars.toUpperCase();
//        String digit = "0123456789";
//        String all = digit + chars + charsUpper;
//        StringBuilder sb = new StringBuilder(length);
//
//        for (int i = 0; i < length; i++) {
//            sb.append(all.charAt(RANDOM.nextInt(all.length())));
//        }
//        return sb.toString();
//    }

    public static void sendNewPasswordEmail(String toEmail, String newPassword)
            throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_DISPLAY_NAME, "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Mật khẩu mới của bạn", "UTF-8");

            String htmlContent = "<div style='font-family:Arial,sans-serif;max-width:480px;margin:auto'>"
                    + "<h2>Yêu cầu lấy lại mật khẩu</h2>"
                    + "<p>Mật khẩu mới của bạn là:</p>"
                    + "<p style='font-size:24px;font-weight:bold;letter-spacing:2px;color:#d97706'>"
                    + newPassword + "</p>"
                    + "<p>Vui lòng đăng nhập bằng mật khẩu này. Nếu bạn không thực hiện yêu cầu này, "
                    + "vui lòng liên hệ với chúng tôi ngay.</p>"
                    + "</div>";

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new MessagingException("Lỗi encode email người gửi", ex);
        }
    }

    public static void sendResetLinkEmail(String toEmail, String resetLink)
            throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_DISPLAY_NAME, "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Đặt lại mật khẩu của bạn", "UTF-8");

            String htmlContent = "<div style='font-family:Arial,sans-serif;max-width:480px;margin:auto'>"
                    + "<h2>Yêu cầu đặt lại mật khẩu</h2>"
                    + "<p>Nhấn vào nút bên dưới để đặt mật khẩu mới. Liên kết này chỉ có hiệu lực "
                    + "trong <b>15 phút</b> và chỉ dùng được 1 lần.</p>"
                    + "<p style='text-align:center;margin:24px 0'>"
                    + "<a href='" + resetLink + "' "
                    + "style='background:#76493b;color:#fff;padding:12px 24px;border-radius:8px;"
                    + "text-decoration:none;font-weight:bold;display:inline-block'>Đặt lại mật khẩu</a>"
                    + "</p>"
                    + "<p style='color:#888;font-size:13px'>Nếu bạn không thực hiện yêu cầu này, "
                    + "vui lòng bỏ qua email này, mật khẩu của bạn sẽ không thay đổi.</p>"
                    + "</div>";

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new MessagingException("Lỗi encode email người gửi", ex);
        }
    }
}