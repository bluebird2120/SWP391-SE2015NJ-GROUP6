package controller;

import dal.OrderDAO;
import dal.TableDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Order;
import model.Table;

@WebServlet(name = "ScanQRController", urlPatterns = { "/scan" })
public class ScanQRController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String token = request.getParameter("token");

        // Logic xử lý quét QR
        if (token != null && !token.isEmpty()) {
            TableDAO tableDAO = new TableDAO();
            Table currentTable = tableDAO.getTableByToken(token);

            if (currentTable != null && currentTable.getIsActive() == 1) {

                OrderDAO orderDAO = new OrderDAO();
                Order activeOrder = orderDAO.getActiveOrderByTableId(currentTable.getTableID());

                String role = (String) session.getAttribute("roleInTable");
                Integer sessionOrderID = (Integer) session.getAttribute("orderID");

                // =========================================================
                // 1. TÍNH NĂNG CHỦ BÀN QUÉT MÃ QR ĐỂ GỘP BÀN
                // =========================================================
                if ("HOST".equals(role) && sessionOrderID != null) {
                    // 👉 KHÁCH ĐANG GỘP BÀN -> TUYỆT ĐỐI KHÔNG GHI ĐÈ BÀN GỐC TRONG SESSION
                    if (activeOrder == null) {
                        boolean isAdded = orderDAO.addTableToExistingOrder(sessionOrderID, currentTable.getTableID());
                        if (isAdded) {
                            session.setAttribute("successMsg", "Đã gộp thêm bàn thành công vào hóa đơn của bạn!");
                            response.sendRedirect(request.getContextPath() + "/menu");
                            return;
                        }
                    } else if (activeOrder.getOrderID() != sessionOrderID) {
                        session.setAttribute("errorMsg", "Bàn này đang có khách ngồi, không thể gộp!");
                        response.sendRedirect(request.getContextPath() + "/menu");
                        return;
                    }
                } else {
                    // 👉 KHÁCH LẠ LẦN ĐẦU QUÉT MÃ -> MỚI ĐƯỢC LƯU SESSION
                    session.setAttribute("tableID", currentTable.getTableID());
                    session.setAttribute("currentTableID", currentTable.getTableID());
                    session.setAttribute("areaType", currentTable.getAreaType());
                }
                // =========================================================

                if (activeOrder != null) {

                    // // 👉 TRƯỜNG HỢP 1: BÀN ĐÃ CÓ ORDER
                    //
                    // // --- BẮT ĐẦU: BARIE CHẶN CHỜ NHÂN VIÊN DUYỆT BÀN ---
                    // if (activeOrder.getIsStaffConfirmed() == 0) {
                    // session.setAttribute("pendingOrderID", activeOrder.getOrderID());
                    // request.getRequestDispatcher("/views/user/waiting_staff.jsp").forward(request,
                    // response);
                    // return; // Khóa luồng, không cho load Menu!
                    // }
                    // // --- KẾT THÚC BARIE ---

                    if (role != null && sessionOrderID != null && sessionOrderID == activeOrder.getOrderID()) {
                        // Host hoặc Guest đã duyệt -> Cho vào Menu gọi món bình thường
                        response.sendRedirect(request.getContextPath() + "/menu");
                        return;
                    } else {
                        // // === BẮT ĐẦU VÁ LỖ HỔNG ĐƠN ĐẶT TRƯỚC (RESERVATION) ===
                        // // Giả sử orderType == 2 là mã của Đơn đặt trước trong Database của bạn
                        // if ("reserved".equals(activeOrder.getTableStatus())) {
                        // session.setAttribute("pendingOrderID", activeOrder.getOrderID());
                        // // Chuyển sang trang yêu cầu nhập Số điện thoại để lấy lại quyền Host
                        // request.getRequestDispatcher("/views/user/claim_host.jsp").forward(request,
                        // response);
                        // return;
                        // }
                        // // === KẾT THÚC PHẦN VÁ ===

                        // Người lạ -> Xin phép Host (Dành cho các bàn khách walk-in bình thường)
                        session.setAttribute("pendingOrderID", activeOrder.getOrderID());
                        request.getRequestDispatcher("/views/user/join_table.jsp").forward(request, response);
                        return;
                    }
                } else {
                    // 👉 TRƯỜNG HỢP 2: BÀN TRỐNG (Người đầu tiên quét)
                    Order newOrder = new Order();
                    newOrder.setTableStatus("occupied");
                    newOrder.setOrderType(1);

                    // SỬA SỐ 0 THÀNH SỐ 1: FIX CỨNG ĐÃ ĐƯỢC DUYỆT
                    newOrder.setIsStaffConfirmed(1);

                    newOrder.setOrderStatus("ordering");
                    newOrder.setTotalAmount(0);
                    newOrder.setDepositAmount(0);


                    dal.EmployeeShiftDAO esDAO = new dal.EmployeeShiftDAO();
                    Integer assignedStaffId = esDAO.getActiveEmployeeForCurrentShift();
                    if (assignedStaffId != null) {
                        newOrder.setEmployeeID(assignedStaffId);
                    }

                    int newOrderID = orderDAO.createOrder(newOrder);
                    if (newOrderID > 0) {
                        orderDAO.linkOrderAndTable(newOrderID, currentTable.getTableID());
                        session.setAttribute("orderID", newOrderID);
                        session.setAttribute("roleInTable", "HOST");


                        if (newOrder.getEmployeeID() != null) {
                            try {
                                dal.NotificationDAO notifDAO = new dal.NotificationDAO();
                                model.Notifications n = new model.Notifications();
                                n.setRecipientID(newOrder.getEmployeeID());
                                n.setRecipientType("staff");
                                n.setType("new_order");
                                n.setMessage("Bạn được phân công phục vụ Đơn hàng #" + newOrderID + " (Bàn "
                                        + currentTable.getTableID() + ") mới tạo.");
                                n.setIsRead(0);
                                notifDAO.insert(n);
                            } catch (Exception e) {
                                System.err.println("[ScanQRController] Gửi thông báo thất bại: " + e.getMessage());
                            }
                        }

                        // TẠM THỜI COMMENT ĐOẠN ĐẨY RA MÀN HÌNH CHỜ
                        /*
                         * session.setAttribute("pendingOrderID", newOrderID);
                         * request.getRequestDispatcher("/views/user/waiting_staff.jsp").forward(
                         * request, response);
                         * return;
                         */

                        // Xử lý xong thì đẩy sang Menu
                        response.sendRedirect(request.getContextPath() + "/menu");
                        return;
                    }
                }
            } else {
                // Bàn không hoạt động hoặc không tồn tại
                response.sendRedirect(request.getContextPath() + "/error.jsp");
                return;
            }
        } else {
            // Quét mã không có Token, đẩy về trang chủ hoặc menu
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}