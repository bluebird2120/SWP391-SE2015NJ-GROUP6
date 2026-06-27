package controller;

import dal.NotificationDAO;
import model.Customer;
import model.Notifications;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "CustomerNotificationsController", urlPatterns = {"/customer/notifications"})
public class CustomerNotificationsController extends HttpServlet {

    private static final String VIEW = "/views/notifications.jsp";
    private static final int LIST_LIMIT = 50;
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Customer customer = (session != null) ? (Customer) session.getAttribute("customer") : null;
        if (customer == null) {
            resp.sendRedirect(req.getContextPath() + "/login?msg=required");
            return;
        }

        String action = req.getParameter("action");
        if ("readAndRedirect".equals(action)) {
            int notifID = parseInt(req.getParameter("notificationID"), 0);
            if (notifID > 0) {
                // Đánh dấu đã đọc
                notificationDAO.markRead(notifID, customer.getCustomerID(), "customer");
                
                // Lấy thông tin loại thông báo để chuyển hướng cho phù hợp
                List<Notifications> list = notificationDAO.listByRecipient(customer.getCustomerID(), "customer", LIST_LIMIT);
                Notifications found = null;
                for (Notifications n : list) {
                    if (n.getNotificationID() == notifID) {
                        found = n;
                        break;
                    }
                }
                
                if (found != null) {
                    if ("reservation_confirmed".equals(found.getType())) {
                        resp.sendRedirect(req.getContextPath() + "/reservation?action=history");
                        return;
                    } else if ("feedback_response".equals(found.getType())) {
                        resp.sendRedirect(req.getContextPath() + "/customer/reviews");
                        return;
                    }
                }
            }
            resp.sendRedirect(req.getContextPath() + "/customer/notifications");
            return;
        }

        List<Notifications> list = notificationDAO.listByRecipient(customer.getCustomerID(), "customer", LIST_LIMIT);
        int unread = notificationDAO.countUnread(customer.getCustomerID(), "customer");
        
        session.setAttribute("unreadCount", unread);

        req.setAttribute("notifications", list);
        req.setAttribute("unreadCount", unread);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Customer customer = (session != null) ? (Customer) session.getAttribute("customer") : null;
        if (customer == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        if ("markRead".equals(action)) {
            int notifID = parseInt(req.getParameter("notificationID"), 0);
            if (notifID > 0) {
                notificationDAO.markRead(notifID, customer.getCustomerID(), "customer");
            }
        } else if ("markAllRead".equals(action)) {
            notificationDAO.markAllRead(customer.getCustomerID(), "customer");
        }
        
        // Cập nhật lại số lượng chưa đọc vào session
        int unread = notificationDAO.countUnread(customer.getCustomerID(), "customer");
        session.setAttribute("unreadCount", unread);

        resp.sendRedirect(req.getContextPath() + "/customer/notifications");
    }

    private static int parseInt(String s, int def) {
        if (s == null || s.isBlank()) return def;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
