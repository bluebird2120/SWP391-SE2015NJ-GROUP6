package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import model.Customer;
import model.Employee;
import model.RoutePermission;

@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {
    "/customer/*",
    "/staff/*",
    "/owner/*"
})
public class AuthenticationFilter implements Filter {

    private static final int OWNER_ROLE_ID = 1;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        //ép kiểu từ ServletRequest thành HttpServletRequest
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        //ko tạo mới session
        HttpSession session = request.getSession(false);

        String ctx = request.getContextPath();
        String uri = request.getRequestURI();

        //session trả về kiểu Object nên phải ép kiểu
        Customer customer = (session != null) ? (Customer) session.getAttribute("customer") : null;
        Employee employee = (session != null) ? (Employee) session.getAttribute("employee") : null;

        //Customer
        if (uri.startsWith(ctx + "/customer/")) {
            //kiểm tra đăng nhập chưa
            if (customer == null) {
                //tạo mới session nếu session đó chưa có
                HttpSession newSession = request.getSession(true);
                newSession.setAttribute("redirectAfterLogin", uri);
                response.sendRedirect(ctx + "/login?msg=required");
                return;
            }
            chain.doFilter(req, res);
            return;
        }

        //Employee (/staff/*, /owner/*): Phân quyền động
        if (uri.startsWith(ctx + "/staff/") || uri.startsWith(ctx + "/owner/")) {
            if (employee == null) {
                // Đã login customer nhưng cố vào staff/owner
                if (customer != null) {
                    response.sendRedirect(ctx + "/unauthorized");
                    return;
                }

                //Customer chưa login
                response.sendRedirect(ctx + "/login?msg=required");
                return;
            }

            // Tìm permission cần thiết cho route này (từ dữ liệu DB, có cache)
            String requiredPermission = findRequiredPermission(ctx, uri);

            if (requiredPermission == null) {
                response.sendRedirect(ctx + "/unauthorized");
                return;
            }

            //Set != List ko cho trùng lặp phần tử
            Set<String> employeePermissions = PermissionCache.getPermissionsForRole(employee.getRoleID());

            if (!employeePermissions.contains(requiredPermission)) {
                //Owner cố vào Staff -> đưa về dashboard Owner
                if (employee.getRoleID() == OWNER_ROLE_ID && uri.startsWith(ctx + "/staff/")) {
                    response.sendRedirect(ctx + "/owner/dashboard");
                    return;
                }
                response.sendRedirect(ctx + "/unauthorized");
                return;
            }

            //Bắt buộc đổi mật khẩu lần đầu (áp dụng cho riêng staff)
            if (uri.startsWith(ctx + "/staff/")
                    && employee.getMustChangePassword() == 1
                    && !uri.contains("/staff/change-password")) {
                response.sendRedirect(ctx + "/staff/change-password?first=true");
                return;
            }
            
            java.sql.Timestamp lastChanged = employee.getLastPasswordChangedAt();
            if (lastChanged != null) {
                long daysSince = (System.currentTimeMillis() - lastChanged.getTime())
                            / (1000L * 60 * 60 * 24);
                if (daysSince >= 90 && !uri.contains("staff/change-password")) {
                    response.sendRedirect(ctx + "/staff/change-password?expired=true");
                    return;
                }
            }
            
            chain.doFilter(req, res);
            return;
        }
        chain.doFilter(req, res);
    }

    private String findRequiredPermission(String ctx, String uri) {
        List<RoutePermission> rules = PermissionCache.getRouteRules();
        for (RoutePermission rule : rules) {
            if (uri.startsWith(ctx + rule.getRoutePrefix())) {
                //owner.access / staff.access
                return rule.getPermissionKey();
            }
        }
        return null;
    }
}
