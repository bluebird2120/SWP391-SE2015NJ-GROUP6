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

/**
 * Xử lý thông báo cho Staff (roleID=2) VÀ Lễ tân (roleID=3). Cả 2 role đều dùng
 * recipientType = "staff" trong DB.
 *
 * Mapping type → URL redirect:
 *
 * STAFF (nhân viên phục vụ) nhận: table_assigned → /staff/tables (bàn được
 * giao — có thể do lễ tân gán đơn online, hoặc do lễ tân mở bàn cho khách
 * vãng lai) payment_success → /staff/tables (khách thanh toán xong, cần dọn
 * bàn) checkout_requested → /staff/tables (khách yêu cầu thanh toán)
 * shift_plan → /staff/my-schedule (lịch ca tháng mới) shift_request →
 * /staff/my-schedule (yêu cầu đổi ca từ đồng nghiệp) shift_request_approved →
 * /staff/my-schedule (đổi ca được duyệt) shift_request_rejected →
 * /staff/my-schedule (đổi ca bị từ chối) shift_request_colleague_pending →
 * /staff/my-schedule (đồng nghiệp chờ xác nhận)
 * shift_request_colleague_rejected→ /staff/my-schedule (đồng nghiệp từ chối)
 *
 * LỄ TÂN nhận: new_order → /reception/tables (khách vãng lai quét QR, cần ra
 * mở bàn — lễ tân mở bàn xong hệ thống MỚI gán nhân viên ít việc nhất, nhân
 * viên đó nhận table_assigned ở trên) reservation_needs_table →
 * /reception/tables (đơn online cần gán bàn)
 */
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

        List<Notifications> list = notificationDAO.listByRecipient(
                emp.getEmployeeID(), "staff", LIST_LIMIT);
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

        } else if ("readAndRedirect".equals(action)) {
            int notifID = parseInt(request.getParameter("notificationID"), 0);
            if (notifID > 0) {
                notificationDAO.markRead(notifID, emp.getEmployeeID(), "staff");

                // Lấy notification để biết type → redirect đúng trang
                List<Notifications> list = notificationDAO.listByRecipient(
                        emp.getEmployeeID(), "staff", LIST_LIMIT);

                for (Notifications noti : list) {
                    if (noti.getNotificationID() == notifID) {
                        String url = resolveRedirectUrl(noti.getType(),
                                request.getContextPath());
                        updateUnread(session, emp);
                        response.sendRedirect(url);
                        return;
                    }
                }
            }
        }

        updateUnread(session, emp);
        response.sendRedirect(request.getContextPath() + "/staff/notifications");
    }

    /**
     * Map notification type → URL trang đích. Thêm type mới vào đây khi cần.
     */
    private String resolveRedirectUrl(String type, String ctx) {
        if (type == null) {
            return ctx + "/staff/notifications";
        }

        switch (type) {
            // ── BÀN & ĐƠN HÀNG (Staff phục vụ) ──────────────────────────
            // Bàn được giao cho nhân viên
            case "table_assigned":
            // Khách thanh toán thành công → cần dọn bàn
            case "payment_success":
            // Khách yêu cầu thanh toán → cần chốt hóa đơn
            case "checkout_requested":
                return ctx + "/staff/tables";

            // ── LỄ TÂN: khách vãng lai quét QR, cần ra mở bàn ──────────
            // (new_order chỉ gửi cho lễ tân — họ mở bàn xong hệ thống mới
            //  gán nhân viên ít việc nhất, lúc đó nhân viên mới nhận table_assigned)
            case "new_order":
            // đơn online cần gán bàn ─────────────────────────
            case "reservation_needs_table":
                return ctx + "/reception/tables";

            // ── CA LÀM VIỆC (tất cả loại shift notification) ────────────
            // Lịch ca tháng mới được phát hành
            case "shift_plan":
            // Có yêu cầu đổi ca từ đồng nghiệp
            case "shift_request":
            // Yêu cầu đổi ca của mình được duyệt
            case "shift_request_approved":
            // Yêu cầu đổi ca của mình bị từ chối
            case "shift_request_rejected":
            // Đồng nghiệp đang chờ mình xác nhận đổi ca
            case "shift_request_colleague_pending":
            // Đồng nghiệp từ chối đổi ca với mình
            case "shift_request_colleague_rejected":
                return ctx + "/staff/my-schedule";

            // ── MẶC ĐỊNH: ở lại trang thông báo ─────────────────────────
            default:
                return ctx + "/staff/notifications";
        }
    }

    private void updateUnread(HttpSession session, Employee emp) {
        int unread = notificationDAO.countUnread(emp.getEmployeeID(), "staff");
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
}