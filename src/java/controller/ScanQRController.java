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
                //  Lấy đơn hàng hoạt động của bàn (đã bao gồm đơn đặt trước/arrived nhờ DAO cập nhật)
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
                    // 👉 KHÁCH LẠ LẦN ĐẦU QUÉT MÃ -> MỚI ĐƯỢC LƯU SESSION VỊ TRÍ
                    session.setAttribute("tableID", currentTable.getTableID());
                    session.setAttribute("currentTableID", currentTable.getTableID());
                    session.setAttribute("areaType", currentTable.getAreaType());
                }
                // =========================================================

                if (activeOrder != null) {

                    // 👉 TRƯỜNG HỢP 1: BÀN ĐÃ CÓ ĐƠN HÀNG (PENDING / RESERVED / ARRIVED / SERVING / OCCUPIED)

                    // 1. Kiểm tra xem người quét có phải là Host/Guest đã ở trong bàn này không
                    if (role != null && sessionOrderID != null && sessionOrderID == activeOrder.getOrderID()) {
                        
                        // --- BARIE CHẶN CHỜ NHÂN VIÊN DUYỆT BÀN ---
                        // Nếu nhân viên chưa xác nhận mở bàn (Status: pending)
                        if ("pending".equals(activeOrder.getTableStatus())) {
                            session.setAttribute("pendingOrderID", activeOrder.getOrderID());
                            request.getRequestDispatcher("/views/user/waiting_staff.jsp").forward(request, response);
                            return;
                        }

                        // Nếu đã được duyệt, vào thẳng menu
                        response.sendRedirect(request.getContextPath() + "/menu");
                        return;
                    } 
                    
                    // 2. NGƯỜI LẠ HOẶC NGƯỜI QUÉT LẦN ĐẦU
                    
                    // Bàn đang chờ khách đặt trước tới nhận (Reserved) - Chưa check-in
                    if ("reserved".equals(activeOrder.getTableStatus())) {
                        session.setAttribute("errorMsg", "Bàn này đã được đặt trước! Vui lòng gặp nhân viên để check-in và nhận bàn.");
                        response.sendRedirect(request.getContextPath() + "/menu");
                        return;
                    }
                    
                    // === 🌟 VÁ LỖ HỔNG DEADLOCK KHÁCH ĐẶT TRƯỚC ===
                    // Bàn Đặt Trước vừa được nhân viên Check-in (Trạng thái: arrived)
                    if ("arrived".equals(activeOrder.getTableStatus())) {
                        // Cấp ngay quyền HOST cho người quét đầu tiên của nhóm khách đặt trước
                        session.setAttribute("orderID", activeOrder.getOrderID());
                        session.setAttribute("roleInTable", "HOST");

                        // Đóng cửa lại thành 'occupied' để người thứ 2 quét mã phải xin gộp bàn
                        orderDAO.updateTableStatus(activeOrder.getOrderID(), "occupied");

                        // Đưa khách vào Menu để tiến hành gọi thêm món hoặc thanh toán
                        response.sendRedirect(request.getContextPath() + "/menu");
                        return;
                    }

                    // Bàn đang chờ nhân viên mở (Pending) nhưng là người lạ quét
                    if ("pending".equals(activeOrder.getTableStatus())) {
                        session.setAttribute("errorMsg", "Bàn này đang chờ nhân viên mở bàn. Vui lòng đợi trong giây lát.");
                        response.sendRedirect(request.getContextPath() + "/menu");
                        return;
                    }
                    
                    // [TABLE STATUS FLOW] Ban da thanh toan nhung chua don xong thi chua cho khach moi vao.
                    if ("cleaning".equals(activeOrder.getTableStatus())) {
                        session.setAttribute("errorMsg", "Bàn này đang chờ dọn dẹp. Vui lòng chọn bàn khác hoặc liên hệ nhân viên.");
                        response.sendRedirect(request.getContextPath() + "/menu");
                        return;
                    }

                    // Ban da co HOST/khach dang ngoi an thi phai xin gop ban.
                    // [TABLE STATUS FLOW] occupied = da co HOST/khach dang dung ban.
                    if ("occupied".equals(activeOrder.getTableStatus())) {
                        session.setAttribute("pendingOrderID", activeOrder.getOrderID());
                        request.getRequestDispatcher("/views/user/join_table.jsp").forward(request, response);
                        return;
                    }

                } else {
                    // 👉 TRƯỜNG HỢP 2: BÀN TRỐNG TINH (Khách vãng lai đến quán)
                    Order newOrder = new Order();
                    
                    // Set trạng thái là 'pending' để chờ nhân viên ra mở bàn
                    newOrder.setTableStatus("pending"); 
                    newOrder.setOrderType(1);
                    
                    // Bắt buộc phải được staff xác nhận
                    newOrder.setIsStaffConfirmed(0);

                    newOrder.setOrderStatus("ordering");
                    newOrder.setTotalAmount(0);
                    newOrder.setDepositAmount(0);

                    // 🌟 ĐÃ VÁ LỖI TẠI ĐÂY: Gán thời gian tạo đơn để SQL bắt được đơn hàng
                    newOrder.setOrderTime(new java.sql.Timestamp(System.currentTimeMillis()));
                    newOrder.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                    
                    // Gán nhân viên phụ trách theo ca làm việc hiện tại
                    dal.EmployeeShiftDAO esDAO = new dal.EmployeeShiftDAO();
                    Integer assignedStaffId = esDAO.getActiveEmployeeForCurrentShift();
                    if (assignedStaffId != null) {
                        newOrder.setEmployeeID(assignedStaffId);
                    }

                    int newOrderID = orderDAO.createOrder(newOrder);
                    if (newOrderID > 0) {
                        orderDAO.linkOrderAndTable(newOrderID, currentTable.getTableID());
                        
                        // Cấp phiên làm việc
                        session.setAttribute("orderID", newOrderID);
                        session.setAttribute("roleInTable", "HOST");

                        // Gửi thông báo cho nhân viên
                        if (newOrder.getEmployeeID() != null) {
                            try {
                                dal.NotificationDAO notifDAO = new dal.NotificationDAO();
                                model.Notifications n = new model.Notifications();
                                n.setRecipientID(newOrder.getEmployeeID());
                                n.setRecipientType("staff");
                                n.setType("new_order");
                                n.setMessage("Bàn " + currentTable.getTableID() + " (Đơn #" + newOrderID + ") đang chờ được mở.");
                                n.setIsRead(0);
                                notifDAO.insert(n);
                            } catch (Exception e) {
                                System.err.println("[ScanQRController] Gửi thông báo thất bại: " + e.getMessage());
                            }
                        }

                        // MỞ KHÓA MÀN HÌNH CHỜ (Waiting_staff.jsp)
                        session.setAttribute("pendingOrderID", newOrderID);
                        request.getRequestDispatcher("/views/user/waiting_staff.jsp").forward(request, response);
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
