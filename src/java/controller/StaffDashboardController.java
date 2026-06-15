package controller;

import dal.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.Employee;

/**
 * Controller xử lý trang Dashboard dùng chung cho Staff và Owner.
 * URL: /staff/dashboard (được AuthFilter bảo vệ cho cả hai role).
 */
@WebServlet(name = "StaffDashboardController", urlPatterns = {"/staff/dashboard"})
public class StaffDashboardController extends HttpServlet {

      private static final String VIEW = "/views/staff/dashboard.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee emp = session == null ? null : (Employee) session.getAttribute("employee");
        if (emp == null) {
            resp.sendRedirect(req.getContextPath() + "/login?type=employee");
            return;
        }
        int unread = new NotificationDAO().countUnread(emp.getEmployeeID(), "staff");
        session.setAttribute("unreadCount", unread);

        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}
