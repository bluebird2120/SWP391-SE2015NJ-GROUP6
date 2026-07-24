package controller;

import dal.EmployeeShiftDAO;
import dal.NotificationDAO;
import dal.OrderDAO;
import dal.TableDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import model.Order;
import model.Table;

@WebServlet(name = "ScanQRController", urlPatterns = { "/scan" })
public class ScanQRController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        // [CSRF FIX] Chuẩn bị token cho các AJAX/form sau khi quét QR.
        util.CsrfUtil.ensureToken(session);
        String token = request.getParameter("token");

        // Logic xử lý quét QR
        if (token != null && !token.isEmpty()) {
            TableDAO tableDAO = new TableDAO();
            Table currentTable = tableDAO.getTableByToken(token);

            if (currentTable != null && currentTable.getIsActive() == 1) {

                OrderDAO orderDAO = new OrderDAO();
                // Lấy đơn hàng hoạt động của bàn (đã bao gồm đơn đặt trước/arrived nhờ DAO cập nhật)
                Order activeOrder = orderDAO.getActiveOrderByTableId(currentTable.getTableID());

                String role = (String) session.getAttribute("roleInTable");
                Integer sessionOrderID = (Integer) session.getAttribute("orderID");
                int tableID = currentTable.getTableID(); // Lấy sẵn tableID để dùng cho tiện

                // =========================================================
                // 1. TÍNH NĂNG CHỦ BÀN QUÉT MÃ QR ĐỂ GỘP BÀN
                // =========================================================
                if ("HOST".equals(role) && sessionOrderID != null) {
                    // 👉 KHÁCH ĐANG GỘP BÀN -> TUYỆT ĐỐI KHÔNG GHI ĐÈ BÀN GỐC TRONG SESSION
                    if (activeOrder == null) {
                        boolean isAdded = orderDAO.addTableToExistingOrder(sessionOrderID, tableID);
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
                } 
                
                // 🌟 LỖ HỔNG ĐÃ VÁ: 
                // Phần gán Session tableID (Khách lạ lần đầu) đã được chuyển xuống mục BÀN TRỐNG TINH.
                // Tuyệt đối không gán ở đây để tránh tạo Order ảo!
                // =========================================================

                if (activeOrder != null) {

                    // 👉 TRƯỜNG HỢP 1: BÀN ĐÃ CÓ ĐƠN HÀNG (PENDING / RESERVED / ARRIVED / SERVING / OCCUPIED)

                    // 1. Kiểm tra xem người quét có phải là Host/Guest đã ở trong bàn này không (Check bằng Session)
                    if (role != null && sessionOrderID != null && sessionOrderID == activeOrder.getOrderID()) {
                        
                        // --- BARIE CHẶN CHỜ NHÂN VIÊN DUYỆT BÀN ---
                        if ("pending".equals(activeOrder.getTableStatus())) {
                            session.setAttribute("pendingOrderID", activeOrder.getOrderID());
                            request.getRequestDispatcher("/views/user/waiting_staff.jsp").forward(request, response);
                            return;
                        }

                        // Nếu đã được duyệt, vào thẳng menu
                        response.sendRedirect(request.getContextPath() + "/menu");
                        return;
                    } 
                    
                    // 🌟 1.5 KIỂM TRA COOKIE XEM CÓ PHẢI CHỦ BÀN BỊ MẤT SESSION QUAY LẠI KHÔNG?
                    boolean isHostReturning = false;
                    Cookie[] cookies = request.getCookies();
                    if (cookies != null && activeOrder.getHostToken() != null) {
                        for (Cookie c : cookies) {
                            if (c.getName().equals("HOST_OF_TABLE_" + tableID) 
                                    && c.getValue().equals(activeOrder.getHostToken())) {
                                isHostReturning = true;
                                break;
                            }
                        }
                    }

                    if (isHostReturning) {
                        // KHOAN HÔ! CHỦ BÀN ĐÃ TRỞ LẠI -> Khôi phục quyền HOST
                        session.setAttribute("orderID", activeOrder.getOrderID());
                        session.setAttribute("roleInTable", "HOST");

                                                // Khach dat truoc da duoc le tan gan ban, nen lan quet QR dau tien
                        // phai luu ban vao session de /menu di dung trang user/menu.jsp.
                        session.setAttribute("tableID", tableID);
                        session.setAttribute("currentTableID", tableID);
                        session.setAttribute("areaType", currentTable.getAreaType());
       
                        // 🌟 THÊM 3 DÒNG NÀY ĐỂ MENU BIẾT BẠN ĐANG NGỒI Ở ĐÂU
                        session.setAttribute("tableID", tableID);
                        session.setAttribute("currentTableID", tableID);
                        session.setAttribute("areaType", currentTable.getAreaType());
                        // ----------------------------------------------------
                        
                        if ("pending".equals(activeOrder.getTableStatus())) {
                            session.setAttribute("pendingOrderID", activeOrder.getOrderID());
                            request.getRequestDispatcher("/views/user/waiting_staff.jsp").forward(request, response);
                            return;
                        }
                        response.sendRedirect(request.getContextPath() + "/menu");
                        return;
                    }
                    
                    // 2. NGƯỜI LẠ HOẶC NGƯỜI QUÉT LẦN ĐẦU (Khách đến sau)
                    
                    // Bàn đang chờ khách đặt trước tới nhận (Reserved) - Chưa check-in
                    if ("reserved".equals(activeOrder.getTableStatus())) {
                        session.setAttribute("errorMsg", "Bàn này đã được đặt trước! Vui lòng gặp nhân viên để check-in và nhận bàn.");
                        response.sendRedirect(request.getContextPath() + "/menu");
                        return;
                    }
                    
                    // === 🌟 VÁ LỖ HỔNG ĐƠN ĐẶT TRƯỚC (BỔ SUNG HOST TOKEN) ===
                    // Bàn Đặt Trước vừa được nhân viên Check-in (Trạng thái: arrived)
                    if ("arrived".equals(activeOrder.getTableStatus())) {
                        
                        // Đóng cửa lại thành 'occupied' để người thứ 2 quét mã phải xin gộp bàn
                        orderDAO.updateTableStatus(activeOrder.getOrderID(), "occupied");
                        
                        // 1. Sinh mã Định danh Host cho người quét đầu tiên của bàn đặt trước
                        String newHostToken = UUID.randomUUID().toString();
                        
                        // 2. Lưu token này xuống CSDL để sau này nhận diện
                        orderDAO.updateHostToken(activeOrder.getOrderID(), newHostToken);

                                                
                        // Khach dat truoc da duoc le tan gan ban, nen lan quet QR dau tien
                        // phai luu ban vao session de /menu di dung trang user/menu.jsp.
                        session.setAttribute("tableID", tableID);
                        session.setAttribute("currentTableID", tableID);
                        session.setAttribute("areaType", currentTable.getAreaType());
                        
                        // 3. Gửi Cookie xuống điện thoại khách (Sống 24 tiếng)
                        Cookie hostCookie = new Cookie("HOST_OF_TABLE_" + tableID, newHostToken);
                        hostCookie.setMaxAge(24 * 60 * 60); 
                        hostCookie.setPath("/"); 
                        // [COOKIE SECURITY] JavaScript không cần đọc host token;
                        // Secure chỉ bật khi ứng dụng đang chạy HTTPS.
                        hostCookie.setHttpOnly(true);
                        hostCookie.setSecure(request.isSecure());
                        response.addCookie(hostCookie);

                        // Cấp quyền HOST vào Session
                        session.setAttribute("orderID", activeOrder.getOrderID());
                        session.setAttribute("roleInTable", "HOST");
                        
                        // 🌟 THÊM 3 DÒNG NÀY CHO KHÁCH ĐẶT TRƯỚC
                        session.setAttribute("tableID", tableID);
                        session.setAttribute("currentTableID", tableID);
                        session.setAttribute("areaType", currentTable.getAreaType());
                        // ---------------------------------------------

                        // Đưa khách vào Menu để tiến hành gọi thêm món hoặc thanh toán
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
                    // 🌟 ĐÃ GỘP THÊM 'pending' VÀO ĐÂY ĐỂ CHẶN NGƯỜI LẠ LỌT VÀO MENU.
                    // 🌟 ĐÃ GỘP THÊM 'pending' VÀO ĐÂY ĐỂ CHẶN NGƯỜI LẠ LỌT VÀO MENU.
                    if ("occupied".equals(activeOrder.getTableStatus()) || "pending".equals(activeOrder.getTableStatus())) {
                        session.setAttribute("pendingOrderID", activeOrder.getOrderID());
                        
                        // --- THÊM 2 DÒNG NÀY ĐỂ GIỮ TẠM VỊ TRÍ BÀN ---
                        session.setAttribute("pendingTableID", tableID);
                        session.setAttribute("pendingAreaType", currentTable.getAreaType());
                        // ---------------------------------------------
                        
                        request.getRequestDispatcher("/views/user/join_table.jsp").forward(request, response);
                        return;
                    }

                } else {
                    // 👉 TRƯỜNG HỢP 2: BÀN TRỐNG TINH (Khách vãng lai đến quán mở bàn mới)
                    
                    // 👉 KHÁCH LẠ LẦN ĐẦU QUÉT MÃ -> MỚI ĐƯỢC LƯU SESSION VỊ TRÍ
                    session.setAttribute("tableID", tableID);
                    session.setAttribute("currentTableID", tableID);
                    session.setAttribute("areaType", currentTable.getAreaType());

                    Order newOrder = new Order();
                    
                    // Set trạng thái là 'pending' để chờ nhân viên ra mở bàn
                    newOrder.setTableStatus("pending"); 
                    newOrder.setOrderType(1);
                    
                    // Bắt buộc phải được staff xác nhận
                    newOrder.setIsStaffConfirmed(0);

                    newOrder.setOrderStatus("ordering");
                    newOrder.setTotalAmount(0);
                    newOrder.setDepositAmount(0);

                    // Gán thời gian tạo đơn
                    newOrder.setOrderTime(new java.sql.Timestamp(System.currentTimeMillis()));
                    newOrder.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                    
                    // 🌟 TẠO MÃ ĐỊNH DANH HOST_TOKEN BẢO MẬT BẰNG UUID CHO KHÁCH VÃNG LAI
                    String hostToken = UUID.randomUUID().toString();
                    newOrder.setHostToken(hostToken);
                    
                    // (Lưu ý: Logic tự động gán nhân viên đã được tích hợp sẵn vào OrderDAO.createOrder)
                    int newOrderID = orderDAO.createOrder(newOrder);
                    if (newOrderID > 0) {
                        orderDAO.linkOrderAndTable(newOrderID, tableID);
                        
                        // 🌟 GỬI COOKIE NHẬN DIỆN CHỦ BÀN XUỐNG ĐIỆN THOẠI KHÁCH (Sống 24 tiếng)
                        Cookie hostCookie = new Cookie("HOST_OF_TABLE_" + tableID, hostToken);
                        hostCookie.setMaxAge(24 * 60 * 60); 
                        hostCookie.setPath("/"); 
                        // [COOKIE SECURITY] Giảm nguy cơ lộ quyền HOST qua XSS.
                        hostCookie.setHttpOnly(true);
                        hostCookie.setSecure(request.isSecure());
                        response.addCookie(hostCookie);
                        
                        // Cấp phiên làm việc tạm (Session)
                        session.setAttribute("orderID", newOrderID);
                        session.setAttribute("roleInTable", "HOST");

                        // Gửi thông báo cho TẤT CẢ Lễ tân đang trực để mở bàn
                        // (Staff sẽ nhận thông báo riêng sau khi Lễ tân bấm mở bàn)
                        try (NotificationDAO notifDAO = new NotificationDAO();
                                EmployeeShiftDAO shiftDAO = new EmployeeShiftDAO()) {
                            java.util.List<Integer> receptionistIDs = shiftDAO.getOnDutyReceptionistIDs();

                            if (receptionistIDs.isEmpty()) {
                                // Không có Lễ tân đang trực → log để biết, không crash
                                System.err.println("[ScanQRController] Không có Lễ tân đang trực để nhận thông báo đơn #" + newOrderID);
                            } else {
                                String msg = "Khách vãng lai vừa quét QR bàn #" + tableID
                                        + " (Đơn #" + newOrderID + "). Vui lòng mở bàn cho khách.";
                                for (int receptionistID : receptionistIDs) {
                                    model.Notifications n = new model.Notifications();
                                    n.setRecipientID(receptionistID);
                                    n.setRecipientType("staff");
                                    n.setType("table_open_request");
                                    n.setMessage(msg);
                                    n.setIsRead(0);
                                    notifDAO.insert(n);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("[ScanQRController] Gửi thông báo Lễ tân thất bại: " + e.getMessage());
                        }

                        // MỞ KHÓA MÀN HÌNH CHỜ NHÂN VIÊN XÁC NHẬN BÀN
                        session.setAttribute("pendingOrderID", newOrderID);
                        request.getRequestDispatcher("/views/user/waiting_staff.jsp").forward(request, response);
                        return;
                    }
                }
            } else {
                // Bàn không hoạt động hoặc không tồn tại
                // [ERROR ROUTING FIX] Dự án không có /error.jsp, chuyển về route tồn tại.
                response.sendRedirect(request.getContextPath() + "/home?error=invalid_qr");
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
