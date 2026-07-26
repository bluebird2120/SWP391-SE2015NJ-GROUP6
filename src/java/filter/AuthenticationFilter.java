package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.Customer;
import model.Employee;

@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {
    "/customer/*",
    "/staff/*",
    "/owner/*",
    "/reception/*"
})
public class AuthenticationFilter implements Filter {

    private static final int OWNER_ROLE_ID = 1;
    private static final int STAFF_ROLE_ID = 2;
    private static final int RECEPTIONIST_ROLE_ID = 3;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);

        String ctx = request.getContextPath();
        String uri = request.getRequestURI();

        Customer customer = (session != null) ? (Customer) session.getAttribute("customer") : null;
        Employee employee = (session != null) ? (Employee) session.getAttribute("employee") : null;

        //Customer
        if (uri.startsWith(ctx + "/customer/")) {

            if (customer == null) {

                HttpSession newSession = request.getSession(true);
                newSession.setAttribute("redirectAfterLogin", uri);
                response.sendRedirect(ctx + "/login?msg=required");
                return;
            }
            chain.doFilter(req, res);
            return;
        }

        //Employee (/staff/*, /owner/*, /reception/*)
        if (uri.startsWith(ctx + "/staff/")
                || uri.startsWith(ctx + "/owner/")
                || uri.startsWith(ctx + "/reception/")) {

            if (employee == null) {

                if (customer != null) {
                    response.sendRedirect(ctx + "/unauthorized");
                    return;
                }
                response.sendRedirect(ctx + "/login?msg=required");
                return;
            }

            // /owner/*
            if (uri.startsWith(ctx + "/owner/") && employee.getRoleID() != OWNER_ROLE_ID) {
                boolean allowed = (employee.getRoleID() == STAFF_ROLE_ID
                        || employee.getRoleID() == RECEPTIONIST_ROLE_ID)
                        && uri.startsWith(ctx + "/owner/business-hours");
                if (!allowed) {
                    response.sendRedirect(ctx + "/unauthorized");
                    return;
                }
            }

            // /staff/*
            if (uri.startsWith(ctx + "/staff/") && employee.getRoleID() != STAFF_ROLE_ID) {
                boolean allowed = false;
                if (employee.getRoleID() == RECEPTIONIST_ROLE_ID) {
                    allowed = uri.startsWith(ctx + "/staff/dashboard")
                            || uri.startsWith(ctx + "/staff/my-schedule")
                            || uri.startsWith(ctx + "/staff/notifications");
                }

                if (!allowed) {
                    response.sendRedirect(ctx + "/unauthorized");
                    return;
                }
            }

            // /reception/* 
            if (uri.startsWith(ctx + "/reception/")
                    && employee.getRoleID() != RECEPTIONIST_ROLE_ID) {
                response.sendRedirect(ctx + "/unauthorized");
                return;
            }

            // ── CHECK ĐỔI MẬT KHẨU ONLY STAFF & RECEPTION ──
            if (employee.getRoleID() == STAFF_ROLE_ID
                    || employee.getRoleID() == RECEPTIONIST_ROLE_ID) {

                if (employee.getMustChangePassword() == 1) {
                    response.sendRedirect(ctx + "/change-password?first=true");
                    return;
                }

                // Check quá hạn 90 ngày
                java.sql.Timestamp lastChanged = employee.getLastPasswordChangedAt();
                if (lastChanged != null) {
                    long daysSince = (System.currentTimeMillis() - lastChanged.getTime())
                            / (1000L * 60 * 60 * 24);
                    if (daysSince >= 90) {
                        response.sendRedirect(ctx + "/change-password?expired=true");
                        return;
                    }
                }
            }
            chain.doFilter(req, res);
            return;
        }
        chain.doFilter(req, res);
    }
}
