package controller;

import dal.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

import model.Employee;
import model.Notifications;

@WebServlet(name = "OwnerNotificationsController", urlPatterns = {"/owner/notifications"})
public class OwnerNotificationsController extends HttpServlet {

    private static final String VIEW = "/views/notifications.jsp";
    private static final int LIST_LIMIT = 50;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee emp = session == null ? null : (Employee) session.getAttribute("employee");
        if (emp == null || emp.getRoleID() != 1) {
            resp.sendRedirect(req.getContextPath() + "/login?type=employee");
            return;
        }

        NotificationDAO dao = new NotificationDAO();
        List<Notifications> list = dao.listByRecipient(emp.getEmployeeID(), "staff", LIST_LIMIT);
        
        int unread = dao.countUnread(emp.getEmployeeID(), "staff");
        session.setAttribute("unreadCount", unread);

        req.setAttribute("notifications", list);
        req.setAttribute("unreadCount", unread);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee emp = session == null ? null : (Employee) session.getAttribute("employee");
        if (emp == null || emp.getRoleID() != 1) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        if ("markRead".equals(action)) {
            int notifID = parseInt(req.getParameter("notificationID"), 0);
            if (notifID > 0) {
                new NotificationDAO().markRead(notifID, emp.getEmployeeID(), "staff");
            }
        }
        resp.sendRedirect(req.getContextPath() + "/owner/notifications");
    }

    private static int parseInt(String s, int def) {
        if (s == null || s.isBlank()) return def;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
