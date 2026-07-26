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

    private static final int LIST_LIMIT = 50;

    // doGet: CHỈ hiển thị danh sách thông báo, không xử lý logic
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Customer customer = (session != null) ? (Customer) session.getAttribute("customer") : null;

        if (customer == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=required");
            return;
        }

        // ── 1. Lấy tham số Filter ──
        String keyword = trim(request.getParameter("keyword"));
        String readStatus = request.getParameter("readStatus");

        // ── 2. Validate Backend ──
        if (keyword != null && keyword.length() > 100) {
            keyword = keyword.substring(0, 100);
        }
        if (readStatus == null || (!readStatus.equals("unread") && !readStatus.equals("read"))) {
            readStatus = "all";
        }

        try (NotificationDAO notificationDAO = new NotificationDAO()) {
            // ── 3. Gọi DAO đã có Filter ──
            List<Notifications> list = notificationDAO.listByRecipientFiltered(
                    customer.getCustomerID(), "customer", LIST_LIMIT, keyword, readStatus);
            int unread = notificationDAO.countUnread(customer.getCustomerID(), "customer");

            //Nuôi header
            session.setAttribute("unreadCount", unread);
            request.setAttribute("notifications", list);
            //Nuôi trang notification
            request.setAttribute("unreadCount", unread);
            request.setAttribute("keyword", keyword);
            request.setAttribute("readStatus", readStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
        request.getRequestDispatcher("/views/notifications.jsp").forward(request, response);
    }

    // doPost: xử lý TẤT CẢ action JSP gửi lên (markRead, markAllRead, readAndRedirect)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Customer customer = (session != null) ? (Customer) session.getAttribute("customer") : null;
        if (customer == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=required");
            return;
        }

        String action = request.getParameter("action");

        try (NotificationDAO notificationDAO = new NotificationDAO()) {
            if ("markRead".equals(action)) {
                int notifID = parseInt(request.getParameter("notificationID"), 0);
                if (notifID > 0) {
                    notificationDAO.markRead(notifID, customer.getCustomerID(), "customer");
                }

            } else if ("markAllRead".equals(action)) {
                notificationDAO.markAllRead(customer.getCustomerID(), "customer");
            } else if ("readAndRedirect".equals(action)) {
                // Đánh dấu đã đọc rồi chuyển đến trang liên quan
                int notifID = parseInt(request.getParameter("notificationID"), 0);
                if (notifID > 0) {
                    notificationDAO.markRead(notifID, customer.getCustomerID(), "customer");

                    // Lấy thông báo để biết type mà redirect đúng trang
                    List<Notifications> list = notificationDAO.listByRecipient(customer.getCustomerID(), "customer", LIST_LIMIT);
                    for (Notifications noti : list) {
                        if (noti.getNotificationID() == notifID) {
                            if ("reservation_confirmed".equals(noti.getType())) {
                                //Cập nhật số lượng tin nhắn chưa đọc trong session 
                                updateUnread(session, customer, notificationDAO);
                                response.sendRedirect(request.getContextPath() + "/reservation?action=history");
                                return;
                            } else if ("feedback_response".equals(noti.getType())) {
                                //Cập nhật số lượng tin nhắn chưa đọc trong session 
                                updateUnread(session, customer, notificationDAO);
                                response.sendRedirect(request.getContextPath() + "/customer/reviews");
                                return;
                            }
                            break;
                        }
                    }
                }
            }

            updateUnread(session, customer, notificationDAO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect(request.getContextPath() + "/customer/notifications");
    }

    // Cập nhật unreadCount vào session sau mỗi action
    private void updateUnread(HttpSession session, Customer customer, NotificationDAO notificationDAO) {
        int unread = notificationDAO.countUnread(customer.getCustomerID(), "customer");
        session.setAttribute("unreadCount", unread);
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

    private String trim(String str) {
        return str == null ? "" : str.trim();
    }
}
