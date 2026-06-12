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
                HttpSession s = request.getSession(true);
                s.setAttribute("redirectAfterLogin", uri);
                response.sendRedirect(ctx + "/login?msg=required");
                return;
            }
            chain.doFilter(req, res);
            return;
        }

        //Staff
        if (uri.startsWith(ctx + "/staff/")) {
            if (employee == null) {
                response.sendRedirect(ctx + "/login?msg=required");
                return;
            }
            if (employee.getRoleID() == 1) { // Owner không được vào staff routes
                response.sendRedirect(ctx + "/owner/dashboard");
                return;
            }
            // Bắt buộc đổi mật khẩu lần đầu
            if (employee.getMustChangePassword() == 1
                    && !uri.contains("/staff/change-password")) {
                response.sendRedirect(ctx + "/staff/change-password?first=true");
                return;
            }
            chain.doFilter(req, res);
            return;
        }

        //Staff
        if (uri.startsWith(ctx + "/owner/")) {
            if (employee == null) {
                response.sendRedirect(ctx + "/login?msg=required");
                return;
            }
            if (employee.getRoleID() != OWNER_ROLE_ID) {
                // Staff thường → không có quyền
                response.sendRedirect(ctx + "/unauthorized");
                return;
            }
            chain.doFilter(req, res);
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
