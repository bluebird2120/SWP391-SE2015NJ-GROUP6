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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Employee emp = session == null ? null : (Employee) session.getAttribute("employee");

        if (emp == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=required");
            return;
        }

        // ── 1. Lấy tham số Filter ──
        String keyword = trim(request.getParameter("keyword"));
        String readStatus = request.getParameter("readStatus");

        // ── 2. Validate Backend ──
        if (keyword != null && keyword.length() > 100) {
            keyword = keyword.substring(0, 100); // Giới hạn tối đa 100 ký tự
        }
        if (readStatus == null || (!readStatus.equals("unread") && !readStatus.equals("read"))) {
            readStatus = "all"; // Mặc định hiển thị tất cả nếu truyền sai
        }

        try (NotificationDAO notificationDAO = new NotificationDAO()) {
            // ── 3. Gọi DAO đã có Filter ──
            List<Notifications> list = notificationDAO.listByRecipientFiltered(
                    emp.getEmployeeID(), "staff", LIST_LIMIT, keyword, readStatus);
            int unread = notificationDAO.countUnread(emp.getEmployeeID(), "staff");

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

        try (NotificationDAO notificationDAO = new NotificationDAO()) {
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
                            updateUnread(session, emp, notificationDAO);
                            response.sendRedirect(url);
                            return;
                        }
                    }
                }
            }

            updateUnread(session, emp, notificationDAO);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            // khách vãng lai quét QR cần mở bàn
            case "table_open_request":
            // đơn online cần gán bàn ─────────────────────────
            case "reservation_needs_table":
            // đơn online đã đặt trước (hôm nay) bị khách hủy ---
            case "reservation_cancelled":
                return ctx + "/reception/tables";

            // ── CA LÀM VIỆC (tất cả loại shift notification) ────────────
            // Owner vừa gán ca mới (theo ngày hoặc theo tháng)
            case "shift_assigned":
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
//            /**
//             * [RESTAURANT CLOSED]
//             * Thông báo nhà hàng đóng cửa sẽ mở lịch làm việc cá nhân.
//             */
//            case "restaurant_closed":
//            /**
//             * [RESTAURANT REOPENED]
//             * Thông báo nhà hàng mở lại cũng mở lịch làm việc cá nhân.
//             */
//            case "restaurant_reopened":
//                return ctx + "/staff/my-schedule";

            // ── MẶC ĐỊNH: ở lại trang thông báo ─────────────────────────
            default:
                return ctx + "/staff/notifications";
        }
    }

    private void updateUnread(HttpSession session, Employee emp, NotificationDAO notificationDAO) {
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

    private String trim(String str) {
        return str == null ? "" : str.trim();
    }
}
