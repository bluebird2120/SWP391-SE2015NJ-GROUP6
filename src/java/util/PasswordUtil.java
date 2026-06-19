package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash password bằng SHA-256 với salt cố định.
 * Dùng cho project học thuật. Production nên dùng BCrypt/Argon2.
 */
public final class PasswordUtil {

    private static final String SALT = "restaurant_swp_2026";

    private PasswordUtil() {
    }

    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            //xử lý byte chứ không xử lý string và hash(md.digest) byte bằng SHA-256
            byte[] bytes = md.digest((rawPassword + SALT).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** So sánh raw password với hash đã lưu. */
    public static boolean verify(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return hash(rawPassword).equalsIgnoreCase(hashedPassword);
    }
    public static void main(String[] args) {
        String pass = "123456";
        System.out.println(hash(pass));
    }
}
