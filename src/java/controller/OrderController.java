package controller;

import dal.OrderDAO;
import dal.TableDAO;
import dal.NotificationDAO;
import model.Order;
import model.OrderItem;
import model.MenuItem;
import model.Notifications;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Timestamp;
import model.Table;

@WebServlet(name = "OrderController", urlPatterns = {"/order"})
public class OrderController extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        util.CsrfUtil.ensureToken(request.getSession());
        String action = request.getParameter("action");
        if (action == null) { action = "cart"; }

        if ("cart".equals(action)) {
            HttpSession session = request.getSession();
            Integer orderID = (Integer) session.getAttribute("orderID");

            if (orderID == null) {
                request.setAttribute("orderItems", null);
                request.setAttribute("menuItems", null);
                request.setAttribute("assignedTables", null);
                request.setAttribute("errorMsg", "Giỏ hàng của bạn đang trống!");
            } else {
                // 1. KÉO CÁC MÓN ĐÃ GỬI BẾP (DATABASE)
                List<OrderItem> dbOrderItems = orderDAO.getOrderItemsByOrderId(orderID);
                List<MenuItem> dbMenuItems = orderDAO.getMenuItemsByOrderId(orderID);
                
                // Lấy thông tin đơn hàng gốc để biết tiền cọc
                Order currentOrder = orderDAO.getOrderById(orderID); 
                request.setAttribute("currentOrder", currentOrder);
                
                // 2. KÉO CÁC MÓN MỚI CHỌN (SESSION CART)
                List<OrderItem> sessionCart = (List<OrderItem>) session.getAttribute("sessionCart");
                List<MenuItem> sessionMenuItems = new ArrayList<>();
                if(sessionCart != null && !sessionCart.isEmpty()){
                    for(OrderItem oi : sessionCart) {
                        sessionMenuItems.add(getMenuItemById(oi.getItemID()));
                    }
                }

                TableDAO tableDAO = new TableDAO();
                List<Table> assignedTables = tableDAO.getTablesByOrderId(orderID);
                
                request.setAttribute("assignedTables", assignedTables);
                request.setAttribute("dbOrderItems", dbOrderItems); // Món ở bếp
                request.setAttribute("dbMenuItems", dbMenuItems);
                request.setAttribute("sessionCart", sessionCart);   // Giỏ hàng tạm
                request.setAttribute("sessionMenuItems", sessionMenuItems);
                request.setAttribute("orderID", orderID);
                
                // Tính tổng tiền các món đã gửi bếp để show ở nút "Thanh toán tổng"
                long totalOrderedAmount = 0;
                if(dbOrderItems != null && dbMenuItems != null && dbOrderItems.size() == dbMenuItems.size()){
                    for(int i=0; i<dbOrderItems.size(); i++){
                         MenuItem mi = dbMenuItems.get(i);
                         int price = mi.getDiscountedPrice() > 0 ? mi.getDiscountedPrice() : mi.getPrice();
                         totalOrderedAmount += price * dbOrderItems.get(i).getQuantity();
                    }
                }
                request.setAttribute("totalOrderedAmount", totalOrderedAmount);
            }

            request.getRequestDispatcher("/views/user/cart.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        // [CSRF FIX] Chặn website khác lợi dụng cookie session để gọi món,
        // gửi bếp hoặc yêu cầu thanh toán thay người dùng.
        if (!util.CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "CSRF token không hợp lệ.");
            return;
        }
        String action = request.getParameter("action");
        HttpSession session = request.getSession();

        Integer currentOrderID = (Integer) session.getAttribute("orderID");

        // --- GỘP BÀN TỪ QUÉT QR ---
        if ("addTable".equals(action)) {
            String newTableToken = request.getParameter("tableToken");
            if (currentOrderID != null && newTableToken != null) {
                TableDAO tableDAO = new TableDAO();
                Table newTable = tableDAO.getTableByToken(newTableToken);
                if (newTable != null && tableDAO.isTableAvailable(newTable.getTableID())) {
                    if (orderDAO.addTableToExistingOrder(currentOrderID, newTable.getTableID())) {
                        session.setAttribute("successMsg", "Đã gộp thành công Bàn " + newTable.getTableName() + " vào đơn hàng của bạn!");
                    }
                } else {
                    session.setAttribute("errorMsg", "Bàn đang có khách ngồi hoặc mã QR không hợp lệ!");
                }
            }
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }

        // =========================================================
        // THAO TÁC GIỎ HÀNG BỘ NHỚ TẠM (SESSION)
        // =========================================================
        List<OrderItem> sessionCart = (List<OrderItem>) session.getAttribute("sessionCart");
        if (sessionCart == null) { sessionCart = new ArrayList<>(); }

        // --- THÊM MÓN VÀO GIỎ TẠM ---
        if ("add".equals(action)) {
            int itemID;
            int quantity;
            try {
                itemID = Integer.parseInt(request.getParameter("itemID"));
                quantity = Integer.parseInt(request.getParameter("quantity"));
            } catch (NumberFormatException e) {
                session.setAttribute("errorMsg", "Thông tin món ăn không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }

            // [ORDER VALIDATION] Backend phải chặn số âm/request sửa tay.
            if (quantity < 1 || quantity > 99) {
                session.setAttribute("errorMsg", "Số lượng phải từ 1 đến 99.");
                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }

            MenuItem selectedMenuItem = getMenuItemById(itemID);
            if (selectedMenuItem == null || selectedMenuItem.getItemID() <= 0) {
                session.setAttribute("errorMsg", "Món ăn không tồn tại.");
                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }

            // [SECURITY FIX - QR FLOW] Không cho POST bỏ qua màn hình chờ mở bàn.
            if (session.getAttribute("currentTableID") != null
                    && !canModifyOrder(currentOrderID)) {
                session.setAttribute("errorMsg", "Bàn chưa được nhân viên xác nhận mở.");
                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }
            
            // Chống lỗi NullPointerException cho note
            String note = request.getParameter("note");
            if (note == null) {
                note = "";
            }
            note = note.trim();
            if (note.length() > 1000) {
                session.setAttribute("errorMsg", "Ghi chú không được vượt quá 1000 ký tự.");
                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }
            
            // [ORDER VALIDATION] Giá lấy từ DB, không tin hidden input của client.
            int price = selectedMenuItem.getDiscountedPrice() > 0
                    ? selectedMenuItem.getDiscountedPrice()
                    : selectedMenuItem.getPrice();

            Integer tableID;
            try {
                tableID = request.getParameter("tableID") != null
                        && !request.getParameter("tableID").isEmpty()
                        ? Integer.parseInt(request.getParameter("tableID"))
                        : (Integer) session.getAttribute("tableID");
            } catch (NumberFormatException e) {
                session.setAttribute("errorMsg", "Mã bàn không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }

            // [ORDER VALIDATION] Order tại bàn phải do luồng quét QR tạo;
            // client không được tự gửi tableID để liên kết một bàn bất kỳ.
            if (currentOrderID == null && tableID != null) {
                session.setAttribute("errorMsg", "Vui lòng quét lại mã QR của bàn.");
                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }

            // NẾU CHƯA CÓ ĐƠN HÀNG -> TẠO ĐƠN HÀNG MỚI
            if (currentOrderID == null) {
                Integer customerID = (Integer) session.getAttribute("customerID");
                Order newOrder = new Order();
                newOrder.setCustomerID(customerID);
                newOrder.setOrderType(tableID != null ? 1 : 2); 
                newOrder.setTableStatus(tableID != null ? "occupied" : "available");
                newOrder.setOrderStatus("ordering");
                newOrder.setIsStaffConfirmed(0);
                newOrder.setTotalAmount(0);
                newOrder.setDepositAmount(0);
                newOrder.setOrderTime(new Timestamp(System.currentTimeMillis()));

                int newOrderID = orderDAO.createOrder(newOrder);
                if (newOrderID == -1) {
                    response.sendRedirect(request.getContextPath() + "/menu?error=create_order_failed");
                    return;
                }
                if (tableID != null && tableID > 0) orderDAO.linkOrderAndTable(newOrderID, tableID);
                session.setAttribute("orderID", newOrderID);
                currentOrderID = newOrderID;
            }

            // [ORDER VALIDATION] Không cho gắn món vào bàn của order khác.
            if (tableID != null && !new TableDAO().isTableAssignedToOrder(
                    currentOrderID, tableID)) {
                session.setAttribute("errorMsg", "Bàn không thuộc đơn hàng hiện tại.");
                response.sendRedirect(request.getContextPath() + "/menu");
                return;
            }

            // Thêm vào Session thay vì DB
            boolean found = false;
            for (OrderItem item : sessionCart) {
                String itemNote = (item.getNote() == null) ? "" : item.getNote();
                
                // Nếu cùng món, cùng bàn, cùng ghi chú -> Gộp số lượng
                if (item.getItemID() == itemID && 
                   ((item.getTableID() == null && tableID == null) || (item.getTableID() != null && item.getTableID().equals(tableID))) &&
                   itemNote.equals(note)) {
                    item.setQuantity(item.getQuantity() + quantity);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                OrderItem newItem = new OrderItem();
                newItem.setOrderItemID(-(sessionCart.size() + 1)); 
                newItem.setOrderID(currentOrderID);
                newItem.setItemID(itemID);
                newItem.setTableID(tableID);
                newItem.setQuantity(quantity);
                newItem.setPrice(price);
                newItem.setNote(note);
                sessionCart.add(newItem);
            }
            
            session.setAttribute("sessionCart", sessionCart);
            // [GIU TRANG MENU] Quay lai dung trang/filter da gui mon.
            // Chi chap nhan URL /menu noi bo de tranh open redirect.
            String returnUrl = request.getParameter("returnUrl");
            String menuUrl = request.getContextPath() + "/menu";
            boolean validReturnUrl = returnUrl != null
                    && (returnUrl.equals(menuUrl)
                    || returnUrl.startsWith(menuUrl + "?"))
                    && !returnUrl.contains("\r")
                    && !returnUrl.contains("\n");
            if (!validReturnUrl) {
                returnUrl = menuUrl;
            }
            session.setAttribute("successMsg", "Đã thêm món vào giỏ hàng.");
            response.sendRedirect(returnUrl);
            return;

        // --- CẬP NHẬT SỐ LƯỢNG (SESSION) ---
        } else if ("update".equals(action)) {
            int orderItemID = Integer.parseInt(request.getParameter("orderItemID"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            
            if (quantity >= 1 && quantity <= 99) {
                for (OrderItem item : sessionCart) {
                    if (item.getOrderItemID() == orderItemID) {
                        item.setQuantity(quantity);
                        break;
                    }
                }
                session.setAttribute("sessionCart", sessionCart);
            }
            response.sendRedirect(request.getContextPath() + "/order?action=cart");
            return;

        // --- XÓA MÓN (SESSION) ---
        } else if ("remove".equals(action)) {
            int orderItemID = Integer.parseInt(request.getParameter("orderItemID"));
            Iterator<OrderItem> iterator = sessionCart.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getOrderItemID() == orderItemID) {
                    iterator.remove();
                    break;
                }
            }
            session.setAttribute("sessionCart", sessionCart);
            response.sendRedirect(request.getContextPath() + "/order?action=cart");
            return;

        // =========================================================
        // GỬI BẾP CÁC MÓN ĐƯỢC CHỌN (TRỪ KHO + GHI DATABASE)
        // =========================================================
        } else if ("sendToKitchen".equals(action)) {
            String[] selectedItems = request.getParameterValues("selectedItems");

            // [SECURITY FIX - QR FLOW] Kiểm tra lại trước khi ghi DB.
            if (!canModifyOrder(currentOrderID)) {
                session.setAttribute("errorMsg", "Đơn hàng chưa được phép gửi bếp.");
                response.sendRedirect(request.getContextPath() + "/order?action=cart");
                return;
            }
            
            if (currentOrderID != null && !sessionCart.isEmpty() && selectedItems != null) {
                List<Integer> idsToSend = new java.util.ArrayList<>();
                for (String s : selectedItems) {
                    idsToSend.add(Integer.parseInt(s));
                }
                
                // Khởi tạo DAO để thao tác với bảng tồn kho
                List<String> outOfStockMessages = new ArrayList<>();
                boolean hasSuccess = false;
                
                Iterator<OrderItem> iterator = sessionCart.iterator();
                while (iterator.hasNext()) {
                    OrderItem oi = iterator.next();
                    
                    // Chỉ xử lý những món có ID nằm trong danh sách khách đã tích chọn
                    if (idsToSend.contains(oi.getOrderItemID())) {
                        
                        // 1. Cố gắng trừ kho an toàn (Sử dụng hàm đã viết trước đó)
                        // [TRANSACTION FIX] Trừ kho và ghi OrderItem cùng transaction.
                        boolean isStockDeducted = orderDAO.sendItemToKitchen(
                                currentOrderID, oi.getItemID(), oi.getTableID(),
                                oi.getQuantity(), oi.getPrice(), oi.getNote());
                        
                        if (isStockDeducted) {
                            // 2A. Nếu trừ kho thành công -> Ghi vào đơn hàng và xóa khỏi giỏ tạm
                            iterator.remove(); 
                            hasSuccess = true;
                        } else {
                            // 2B. Nếu kho không đủ -> Giữ lại món trong giỏ và lấy tên món báo lỗi
                            MenuItem mi = getMenuItemById(oi.getItemID());
                            String itemName = (mi != null) ? mi.getItemName() : "Một món ăn";
                            outOfStockMessages.add("Rất tiếc, " + itemName + " đã hết hoặc không đủ số lượng phục vụ!");
                        }
                    }
                }
                
                // Cập nhật lại giỏ hàng (chỉ còn những món chưa gửi đi hoặc bị lỗi hết kho)
                session.setAttribute("sessionCart", sessionCart);
                
                // Phản hồi về giao diện dựa trên kết quả trừ kho
                if (!outOfStockMessages.isEmpty()) {
                    // Nếu có món hết hàng, gộp các câu thông báo lại
                    String errorMsg = String.join("<br>", outOfStockMessages);
                    session.setAttribute("errorMsg", errorMsg);
                } 
                
                if (hasSuccess) {
                    session.setAttribute("successMsg", "Đã gửi các món ăn hợp lệ xuống Bếp!");
                }
            }
            response.sendRedirect(request.getContextPath() + "/order?action=cart");
            return;
            
        // =========================================================
        // THANH TOÁN TỔNG
        // =========================================================
        } else if ("checkPaymentStatus".equals(action)) {
            // [CHO THANH TOAN] Khach chi kiem tra ket qua do staff xu ly,
            // khong tu cap nhat trang thai thanh toan.
            if (currentOrderID == null) {
                clearTableSession(session);
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }

            Order currentOrder = orderDAO.getOrderById(currentOrderID);
            boolean paid = currentOrder != null
                    && "completed".equalsIgnoreCase(
                            currentOrder.getOrderStatus());

            if (paid) {
                clearTableSession(session);
                response.sendRedirect(request.getContextPath()
                        + "/home?payment=success");
            } else {
                // Chua thanh toan: dong popup de HOST co the xem lai gio
                // va gui lai yeu cau neu can.
                session.removeAttribute("checkoutWaiting");
                session.setAttribute("errorMsg",
                        "Đơn hàng chưa được thanh toán. "
                        + "Vui lòng chờ nhân viên hoặc gửi lại yêu cầu nếu cần.");
                response.sendRedirect(request.getContextPath()
                        + "/order?action=cart");
            }
            return;

        } else if ("checkoutTotal".equals(action)) {
            // [YEU CAU THANH TOAN] Khach chi gui yeu cau tinh tien.
            // Hoa don cuoi cung se do nhan vien phuc vu kiem tra va tao.
            if (currentOrderID != null && orderDAO.requestCheckout(currentOrderID)) {
                session.setAttribute("successMsg", "Đã gửi yêu cầu tính tiền. Vui lòng chờ nhân viên kiểm tra và chốt hóa đơn.");
                // [CHO THANH TOAN] Bat popup cho HOST sau khi gui yeu cau.
                session.setAttribute("checkoutWaiting", true);

                // Thông báo ngay cho nhân viên đang phụ trách bàn/đơn này để họ qua chốt hóa đơn.
                try {
                    Order order = orderDAO.getOrderById(currentOrderID);
                    if (order != null && order.getEmployeeID() != null) {
                        List<Table> tables = new TableDAO().getTablesByOrderId(currentOrderID);
                        StringBuilder tableNames = new StringBuilder();
                        for (int i = 0; i < tables.size(); i++) {
                            if (i > 0) tableNames.append(", ");
                            tableNames.append(tables.get(i).getTableName());
                        }
                        String tableLabel = tableNames.length() > 0 ? tableNames.toString() : ("#" + currentOrderID);

                        try (NotificationDAO notifDAO = new NotificationDAO()) {
                            Notifications n = new Notifications();
                            n.setRecipientID(order.getEmployeeID());
                            n.setRecipientType("staff");
                            n.setType("checkout_requested");
                            n.setMessage("Bàn " + tableLabel + " (Đơn #" + currentOrderID
                                    + ") vừa yêu cầu thanh toán. Vui lòng đến kiểm tra và chốt hóa đơn.");
                            n.setIsRead(0);
                            notifDAO.insert(n);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[OrderController] Gửi thông báo yêu cầu thanh toán thất bại: " + e.getMessage());
                }
            } else {
                session.setAttribute("errorMsg", "Không thể gửi yêu cầu tính tiền. Vui lòng kiểm tra lại món đã gọi.");
            }
            response.sendRedirect(request.getContextPath() + "/order?action=cart");
            return;
        }
    }

    @Override
    public String getServletInfo() {
        return "Order Controller Handles Session Cart and DB sync";
    }

    /**
     * [CHO THANH TOAN] Xoa trang thai tham gia ban tren dung thiet bi khach
     * sau khi staff da thanh toan. Khong xoa tai khoan dang nhap customer.
     */
    private void clearTableSession(HttpSession session) {
        session.removeAttribute("orderID");
        session.removeAttribute("tableID");
        session.removeAttribute("currentTableID");
        session.removeAttribute("areaType");
        session.removeAttribute("roleInTable");
        session.removeAttribute("pendingOrderID");
        session.removeAttribute("pendingTableID");
        session.removeAttribute("pendingAreaType");
        session.removeAttribute("sessionCart");
        session.removeAttribute("checkoutWaiting");
    }
    
    // Hàm phụ trợ: Lấy thông tin chi tiết của 1 món ăn dựa vào mã itemID
    private MenuItem getMenuItemById(int itemID) {
        // [ORDER VALIDATION] MenuItem dùng cột isAvailable (không phải status).
        // DailyInventory chỉ lưu tồn kho; món vẫn phải đang ở trạng thái mở bán.
        String sql = "SELECT * FROM MenuItem "
                + "WHERE itemID = ? AND isAvailable = 1";
        try (java.sql.Connection conn = new dal.DBContext().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemID);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MenuItem mi = new MenuItem();
                    mi.setItemID(rs.getInt("itemID"));
                    mi.setItemName(rs.getString("itemName"));
                    mi.setDescription(rs.getString("description"));
                    mi.setPrice(rs.getInt("price"));
                    mi.setDiscountPercent(rs.getInt("discountPercent"));
                    mi.setDiscountedPrice(rs.getInt("discountedPrice"));
                    mi.setImage(rs.getString("image"));
                    return mi;
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy thông tin món ăn: " + e.getMessage());
        }
        return null;
    }

    /**
     * [SECURITY FIX - QR FLOW] Chỉ bàn đã được nhân viên xác nhận mới gọi món.
     */
    private boolean canModifyOrder(Integer orderID) {
        if (orderID == null) {
            return false;
        }
        Order order = orderDAO.getOrderById(orderID);
        if (order == null) {
            return false;
        }
        // Đơn mang về không đi qua luồng mở bàn QR.
        if (order.getOrderType() == 2) {
            return !"completed".equals(order.getOrderStatus())
                    && !"cancelled".equals(order.getOrderStatus());
        }
        return order.getIsStaffConfirmed() == 1
                && ("occupied".equals(order.getTableStatus())
                || "arrived".equals(order.getTableStatus()));
    }
}
