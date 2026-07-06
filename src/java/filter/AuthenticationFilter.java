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
    // [PHAN QUYEN LE TAN] Role 3 chi quan ly va gan ban.
    private static final int RECEPTIONIST_ROLE_ID = 3;

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

        //Employee (/staff/*, /owner/*): Phân quyền theo Role
        if (uri.startsWith(ctx + "/staff/")
                || uri.startsWith(ctx + "/owner/")
                || uri.startsWith(ctx + "/reception/")) {
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
            
            // /owner/* chỉ dành cho Owner (roleID = 1)
            if (uri.startsWith(ctx + "/owner/") && employee.getRoleID() != OWNER_ROLE_ID) {
                response.sendRedirect(ctx + "/unauthorized");
                return;
            }
            
            // /staff/* chỉ dành cho Staff (roleID = 2) hoặc Owner truy cập chức năng staff
            // [PHAN QUYEN LE TAN] Le tan duoc dung dashboard, lich va thong bao.
            boolean receptionistSharedPage = employee.getRoleID() == RECEPTIONIST_ROLE_ID
                    && (uri.equals(ctx + "/staff/dashboard")
                    || uri.startsWith(ctx + "/staff/my-schedule")
                    || uri.startsWith(ctx + "/staff/notifications")
                    || uri.startsWith(ctx + "/staff/change-password"));

            if (uri.startsWith(ctx + "/staff/")
                    && employee.getRoleID() != STAFF_ROLE_ID 
                    && employee.getRoleID() != OWNER_ROLE_ID
                    && !receptionistSharedPage) {
                response.sendRedirect(ctx + "/unauthorized");
                return;
            }

            // [PHAN QUYEN LE TAN] Chi Le tan va Owner duoc vao khu tiep nhan.
            if (uri.startsWith(ctx + "/reception/")
                    && employee.getRoleID() != RECEPTIONIST_ROLE_ID
                    && employee.getRoleID() != OWNER_ROLE_ID) {
                response.sendRedirect(ctx + "/unauthorized");
                return;
            }

            //Bắt buộc đổi mật khẩu lần đầu (áp dụng cho riêng staff)
            if (uri.startsWith(ctx + "/staff/")
                    && employee.getRoleID() != OWNER_ROLE_ID
                    && employee.getMustChangePassword() == 1
                    && !uri.contains("/staff/change-password")) {
                response.sendRedirect(ctx + "/staff/change-password?first=true");
                return;
            }

            if (employee.getRoleID() != OWNER_ROLE_ID) {
                java.sql.Timestamp lastChanged = employee.getLastPasswordChangedAt();
                if (lastChanged != null) {
                    long daysSince = (System.currentTimeMillis() - lastChanged.getTime())
                            / (1000L * 60 * 60 * 24);
                    if (daysSince >= 90 && !uri.contains("staff/change-password")) {
                        response.sendRedirect(ctx + "/staff/change-password?expired=true");
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
