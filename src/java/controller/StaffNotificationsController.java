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

@WebServlet(name = "StaffNotificationsController", urlPatterns = {"/staff/notifications"})
public class StaffNotificationsController extends HttpServlet {

    private static final int LIST_LIMIT = 50;
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Employee emp = session == null ? null : (Employee) session.getAttribute("employee");
        if (emp == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=required");
            return;
        }

        List<Notifications> list = notificationDAO.listByRecipient(emp.getEmployeeID(), "staff", LIST_LIMIT);
        int unread = notificationDAO.countUnread(emp.getEmployeeID(), "staff");

        //Nuôi header
        session.setAttribute("unreadCount", unread);
        request.setAttribute("notifications", list);
        //Nuôi trang notification
        request.setAttribute("unreadCount", unread);
        request.getRequestDispatcher("/views/notifications.jsp").forward(request, response);
    }

    // doPost: xử lý TẤT CẢ action JSP gửi lên (markRead)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Employee emp = session == null ? null : (Employee) session.getAttribute("employee");
        if (emp == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=required");
            return;
        }

        String action = request.getParameter("action");

        if ("markRead".equals(action)) {
            int notifID = parseInt(request.getParameter("notificationID"), 0);
            if (notifID > 0) {
                notificationDAO.markRead(notifID, emp.getEmployeeID(), "staff");
            }
        }

        // Cập nhật lại unreadCount vào session
        session.setAttribute("unreadCount", notificationDAO.countUnread(emp.getEmployeeID(), "staff"));
        response.sendRedirect(request.getContextPath() + "/staff/notifications");
    }

    private static int parseInt(String str, int def) {
        if (str == null || str.isBlank()) {
            return def;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
