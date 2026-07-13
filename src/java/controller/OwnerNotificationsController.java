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

    private static final int LIST_LIMIT = 50;
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Employee emp = session == null ? null : (Employee) session.getAttribute("employee");
        if (emp == null || emp.getRoleID() != 1) {
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Employee emp = session == null ? null : (Employee) session.getAttribute("employee");
        if (emp == null || emp.getRoleID() != 1) {
            response.sendRedirect(request.getContextPath() + "/login?msg=required");
            return;
        }

        String action = request.getParameter("action");

        if ("markRead".equals(action)) {
            int notifID = parseInt(request.getParameter("notificationID"), 0);
            if (notifID > 0) {
                notificationDAO.markRead(notifID, emp.getEmployeeID(), "staff");
            }

        } else if ("readAndRedirect".equals(action)) {
            int notifID = parseInt(request.getParameter("notificationID"), 0);
            if (notifID > 0) {
                notificationDAO.markRead(notifID, emp.getEmployeeID(), "staff");

                // Lấy notification để biết type → redirect đúng trang
                List<Notifications> list = notificationDAO.listByRecipient(
                        emp.getEmployeeID(), "staff", LIST_LIMIT);

                for (Notifications noti : list) {
                    if (noti.getNotificationID() == notifID) {
                        String url = resolveRedirectUrl(noti.getType(), request.getContextPath());
                        session.setAttribute("unreadCount", notificationDAO.countUnread(emp.getEmployeeID(), "staff"));
                        response.sendRedirect(url);
                        return;
                    }
                }
            }
        }

        // Cập nhật lại unreadCount vào session
        session.setAttribute("unreadCount", notificationDAO.countUnread(emp.getEmployeeID(), "staff"));
        response.sendRedirect(request.getContextPath() + "/owner/notifications");
    }

    /**
     * Map notification type → URL trang đích dành riêng cho Owner. Owner
     * (roleID=1) chỉ nhận 2 loại thông báo trong hệ thống hiện tại: new_review
     * → /owner/reviews (khách vừa gửi đánh giá mới) shift_request →
     * /owner/shift-roster (nhân viên xin nghỉ, chờ owner duyệt) Thêm type mới
     * vào đây khi cần.
     */
    private String resolveRedirectUrl(String type, String ctx) {
        if (type == null) {
            return ctx + "/owner/notifications";
        }

        switch (type) {
            case "new_review":
                return ctx + "/owner/reviews";
            case "shift_request":
                return ctx + "/owner/shift-roster";
            default:
                return ctx + "/owner/notifications";
        }
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
