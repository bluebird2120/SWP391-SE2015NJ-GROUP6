/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.sql.Timestamp;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author admin
 */
public class EmailDAO extends DBContext {

    public void createOtp(String email, String otpCode, String purpose, int exprireMinutes)
            throws SQLException {
        invalidateOldOtps(email, purpose);

        String sql = "INSERT INTO EmailOTP (email, otpCode, purpose, expiresAt) "
                + "VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE))";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, otpCode);
            ps.setString(3, purpose);
            ps.setInt(4, exprireMinutes);
            ps.executeUpdate();
        }
    }

    private void invalidateOldOtps(String email, String purpose) throws SQLException {
        String sql = "UPDATE EmailOTP SET isUsed = 1 "
                + "WHERE email = ? AND purpose = ? AND isUsed = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, purpose);
            ps.executeUpdate();
        }
    }

    //Trả về 1 trong 5 trạng thái
    public enum VerifyResult {
        VALID, NOT_FOUND, EXPIRED, WRONG_CODE, TOO_MANY_ATTEMPTS
    }

    private static final int MAX_ATTEMPTS = 5;

    public VerifyResult verifyOtp(String email, String otpCode, String purpose) throws SQLException {
        String sql = "SELECT otpID, otpCode, expiresAt, attemptCount FROM EmailOTP "
                + "WHERE email = ? AND purpose = ? AND isUsed = 0 "
                + "ORDER BY createdAt DESC LIMIT 1";

        int otpID;
        String storedCode;
        Timestamp expiresAt;
        int attemptCount;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, purpose);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return VerifyResult.NOT_FOUND;
                }

                otpID = rs.getInt("otpID");
                storedCode = rs.getString("otpCode");
                expiresAt = rs.getTimestamp("expiresAt");
                attemptCount = rs.getInt("attemptCount");
            }
        }

        if (attemptCount >= MAX_ATTEMPTS) {
            return VerifyResult.TOO_MANY_ATTEMPTS;
        }
        if (expiresAt.before(new Timestamp(System.currentTimeMillis()))) {
            return VerifyResult.EXPIRED;
        }
        if (!storedCode.equals(otpCode)) {
            incrementAttempt(otpID);
            return VerifyResult.WRONG_CODE;
        }

        markUsed(otpID);
        return VerifyResult.VALID;
    }

    //Trả về số lần nhập sai otp
    private void incrementAttempt(int otpID) throws SQLException {
        String sql = "UPDATE EmailOTP SET attemptCount = attemptCount + 1 WHERE otpID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, otpID);
            ps.executeUpdate();
        }
    }

    //Đánh dấu otp đã được sử dụng rồi
    private void markUsed(int otpID) throws SQLException {
        String sql = "UPDATE EmailOTP SET isUsed = 1 WHERE otpID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, otpID);
            ps.executeUpdate();
        }
    }
}
