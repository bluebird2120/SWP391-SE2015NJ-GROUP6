package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * Tiện ích CSRF dùng chung cho các thao tác thay đổi dữ liệu quan trọng.
 */
public final class CsrfUtil {

    public static final String SESSION_KEY = "csrfToken";

    private CsrfUtil() {
    }

    public static String ensureToken(HttpSession session) {
        String token = (String) session.getAttribute(SESSION_KEY);
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(SESSION_KEY, token);
        }
        return token;
    }

    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String expected = (String) session.getAttribute(SESSION_KEY);
        String actual = request.getParameter("csrfToken");
        if (expected == null || actual == null) {
            return false;
        }
        // [CSRF FIX] So sánh constant-time để tránh rò rỉ token qua timing.
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
