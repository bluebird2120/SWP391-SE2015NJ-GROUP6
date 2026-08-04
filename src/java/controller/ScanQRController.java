package controller;

import dal.EmployeeShiftDAO;
import dal.NotificationDAO;
import dal.OrderDAO;
import dal.TableDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import model.Notifications;
import model.Order;
import model.Table;

/**
 * Điểm bắt đầu của luồng khách quét mã QR tại bàn.
 *
 * <p>Thứ tự xử lý trong doGet: kiểm tra QR -> tìm bàn và active order ->
 * xử lý HOST đang gộp bàn -> xử lý bàn đã có order -> xử lý bàn trống.
 * Toàn bộ quyết định HOST/JOINER được đặt trong doGet để dễ đọc luồng.</p>
 */
@WebServlet(name = "ScanQRController", urlPatterns = {"/scan"})
public class ScanQRController extends HttpServlet {

    // ==================== GET: TOÀN BỘ LUỒNG QUÉT QR ====================

    /**
     * Nhận token từ URL /scan?token=..., xác định bàn và quyền của người quét.
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        //  1. KIỂM TRA TOKEN QR 
        String token = request.getParameter("token");
        if (token == null || token.trim().isEmpty()) {
            // Không có token thì đây không phải một lượt quét QR hợp lệ.
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        TableDAO tableDAO = new TableDAO();
        Table currentTable = tableDAO.getTableByToken(token.trim());
        if (currentTable == null || currentTable.getIsActive() != 1) {
            // Token sai, bàn không tồn tại hoặc bàn đang tạm ngưng.
            response.sendRedirect(
                    request.getContextPath() + "/home?error=invalid_qr");
            return;
        }

        //  2. LẤY BÀN, ACTIVE ORDER VÀ PHIÊN CỦA NGƯỜI QUÉT 
        int tableID = currentTable.getTableID();
        OrderDAO orderDAO = new OrderDAO();
        Order activeOrder = orderDAO.getActiveOrderByTableId(tableID);

        String roleInTable = (String) session.getAttribute("roleInTable");
        Integer sessionOrderID = (Integer) session.getAttribute("orderID");

        //  3. HOST HIỆN TẠI QUÉT QR ĐỂ GỘP THÊM BÀN 
        if ("HOST".equals(roleInTable) && sessionOrderID != null) {
            if (activeOrder == null) {
                // Bàn vừa quét đang trống: liên kết bàn này vào order của HOST.
                boolean added = orderDAO.addTableToExistingOrder(
                        sessionOrderID, tableID);
                if (added) {
                    session.setAttribute("successMsg",
                            "Đã gộp thêm bàn thành công vào hóa đơn của bạn!");
                    response.sendRedirect(request.getContextPath() + "/menu");
                    return;
                }
            } else if (activeOrder.getOrderID() != sessionOrderID) {
                // Bàn thuộc một order khác nên không thể gộp.
                session.setAttribute("errorMsg",
                        "Bàn này đang có khách ngồi, không thể gộp!");
                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }
        }

        //  4. BÀN ĐÃ CÓ ACTIVE ORDER 
        if (activeOrder != null) {
            String tableStatus = activeOrder.getTableStatus();

            //  4.1. NGƯỜI QUÉT ĐÃ Ở TRONG ĐÚNG ORDER NÀY 
            if (roleInTable != null
                    && sessionOrderID != null
                    && sessionOrderID == activeOrder.getOrderID()) {

                if ("pending".equals(tableStatus)) {
                    // Bàn chưa được lễ tân mở: tiếp tục ở màn hình chờ.
                    session.setAttribute(
                            "pendingOrderID", activeOrder.getOrderID());
                    request.getRequestDispatcher(
                            "/views/user/waiting_staff.jsp")
                            .forward(request, response);
                    return;
                }

                // Bàn đã được duyệt: session cũ vẫn hợp lệ, đi vào menu.
                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }

            //  4.2. HOST MẤT SESSION NHƯNG CÒN COOKIE 
            boolean isHostReturning = false;
            Cookie[] cookies = request.getCookies();
            if (cookies != null && activeOrder.getHostToken() != null) {
                String expectedCookieName = "HOST_OF_TABLE_" + tableID;
                for (Cookie cookie : cookies) {
                    if (expectedCookieName.equals(cookie.getName())
                            && activeOrder.getHostToken()
                                    .equals(cookie.getValue())) {
                        isHostReturning = true;
                        break;
                    }
                }
            }

            if (isHostReturning) {
                // Khôi phục đầy đủ order, quyền HOST và vị trí bàn vào session.
                session.setAttribute("orderID", activeOrder.getOrderID());
                session.setAttribute("roleInTable", "HOST");
                session.setAttribute("tableID", tableID);
                session.setAttribute("currentTableID", tableID);
                session.setAttribute("areaType", currentTable.getAreaType());

                if ("pending".equals(tableStatus)) {
                    session.setAttribute(
                            "pendingOrderID", activeOrder.getOrderID());
                    request.getRequestDispatcher(
                            "/views/user/waiting_staff.jsp")
                            .forward(request, response);
                    return;
                }

                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }

            //  4.3. BÀN ĐẶT TRƯỚC NHƯNG CHƯA CHECK-IN 
            if ("reserved".equals(tableStatus)) {
                session.setAttribute("errorMsg",
                        "Bàn này đã được đặt trước! Vui lòng gặp nhân viên "
                        + "để check-in và nhận bàn.");
                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }

            //  4.4. KHÁCH ĐẶT TRƯỚC QUÉT LẦN ĐẦU SAU CHECK-IN 
            if ("arrived".equals(tableStatus)) {
                // Người đầu tiên quét sau check-in trở thành HOST của order.
                orderDAO.updateTableStatus(
                        activeOrder.getOrderID(), "occupied");

                String newHostToken = UUID.randomUUID().toString();
                orderDAO.updateHostToken(
                        activeOrder.getOrderID(), newHostToken);

                // Lưu cookie để có thể khôi phục HOST nếu session bị mất.
                Cookie hostCookie = new Cookie(
                        "HOST_OF_TABLE_" + tableID, newHostToken);
                hostCookie.setMaxAge(24 * 60 * 60);
                hostCookie.setPath("/");
                hostCookie.setHttpOnly(true);
                hostCookie.setSecure(request.isSecure());
                response.addCookie(hostCookie);

                // Cấp order, quyền HOST và vị trí bàn vào session.
                session.setAttribute("orderID", activeOrder.getOrderID());
                session.setAttribute("roleInTable", "HOST");
                session.setAttribute("tableID", tableID);
                session.setAttribute("currentTableID", tableID);
                session.setAttribute("areaType", currentTable.getAreaType());

                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }

            //  4.5. BÀN ĐÃ THANH TOÁN VÀ ĐANG CHỜ DỌN 
            if ("cleaning".equals(tableStatus)) {
                session.setAttribute("errorMsg",
                        "Bàn này đang chờ dọn dẹp. Vui lòng chọn bàn khác "
                        + "hoặc liên hệ nhân viên.");
                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }

            //  4.6. NGƯỜI LẠ QUÉT BÀN ĐANG CÓ KHÁCH 
            if ("occupied".equals(tableStatus)
                    || "pending".equals(tableStatus)) {
                // Chưa cấp JOINER ngay; lưu tạm để trang join gửi yêu cầu.
                session.setAttribute(
                        "pendingOrderID", activeOrder.getOrderID());
                session.setAttribute("pendingTableID", tableID);
                session.setAttribute(
                        "pendingAreaType", currentTable.getAreaType());

                request.getRequestDispatcher("/views/user/join_table.jsp")
                        .forward(request, response);
                return;
            }

            // Trạng thái active order không thuộc luồng QR được hỗ trợ.
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }

        //  5. BÀN TRỐNG: TẠO ORDER CHỜ MỞ BÀN 

        // Chỉ lúc bàn thật sự trống mới lưu vị trí bàn vào session.
        session.setAttribute("tableID", tableID);
        session.setAttribute("currentTableID", tableID);
        session.setAttribute("areaType", currentTable.getAreaType());

        Order newOrder = new Order();
        newOrder.setTableStatus("pending");
        newOrder.setOrderType(1);
        newOrder.setIsStaffConfirmed(0);
        newOrder.setOrderStatus("ordering");
        newOrder.setTotalAmount(0);
        newOrder.setDepositAmount(0);

        Timestamp now = new Timestamp(System.currentTimeMillis());
        newOrder.setOrderTime(now);
        newOrder.setCreatedAt(now);

        // Token này nhận diện HOST ngay cả khi session của trình duyệt bị mất.
        String hostToken = UUID.randomUUID().toString();
        newOrder.setHostToken(hostToken);

        // createOrder đồng thời thực hiện logic tự động gán nhân viên của DAO.
        int newOrderID = orderDAO.createOrder(newOrder);
        if (newOrderID <= 0) {
            session.setAttribute("errorMsg",
                    "Không thể tạo yêu cầu mở bàn. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }

        orderDAO.linkOrderAndTable(newOrderID, tableID);

        // Cookie HOST tồn tại 24 giờ và không cho JavaScript đọc token.
        Cookie hostCookie = new Cookie(
                "HOST_OF_TABLE_" + tableID, hostToken);
        hostCookie.setMaxAge(24 * 60 * 60);
        hostCookie.setPath("/");
        hostCookie.setHttpOnly(true);
        hostCookie.setSecure(request.isSecure());
        response.addCookie(hostCookie);

        // Cấp phiên HOST tạm thời trong lúc chờ lễ tân mở bàn.
        session.setAttribute("orderID", newOrderID);
        session.setAttribute("roleInTable", "HOST");
        session.setAttribute("pendingOrderID", newOrderID);

        //  5.1. THÔNG BÁO CHO LỄ TÂN ĐANG TRỰC 
        try (NotificationDAO notificationDAO = new NotificationDAO();
                EmployeeShiftDAO shiftDAO = new EmployeeShiftDAO()) {

            List<Integer> receptionistIDs
                    = shiftDAO.getOnDutyReceptionistIDs();

            if (receptionistIDs.isEmpty()) {
                System.err.println("[ScanQRController] Không có lễ tân "
                        + "đang trực để nhận thông báo đơn #" + newOrderID);
            } else {
                String message = "Khách vãng lai vừa quét QR bàn #"
                        + tableID + " (Đơn #" + newOrderID
                        + "). Vui lòng mở bàn cho khách.";

                for (int receptionistID : receptionistIDs) {
                    Notifications notification = new Notifications();
                    notification.setRecipientID(receptionistID);
                    notification.setRecipientType("staff");
                    notification.setType("table_open_request");
                    notification.setMessage(message);
                    notification.setIsRead(0);
                    notificationDAO.insert(notification);
                }
            }
        } catch (Exception e) {
            // Gửi notification lỗi không được làm hỏng order vừa tạo.
            System.err.println("[ScanQRController] Gửi thông báo lễ tân "
                    + "thất bại: " + e.getMessage());
        }

        // Giữ URL /scan?token=... và hiển thị màn hình chờ lễ tân.
        request.getRequestDispatcher("/views/user/waiting_staff.jsp")
                .forward(request, response);
    }

    // ==================== POST: DÙNG CHUNG LUỒNG QUÉT QR ====================

    /** Controller không có nghiệp vụ POST riêng nên dùng lại doGet. */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
