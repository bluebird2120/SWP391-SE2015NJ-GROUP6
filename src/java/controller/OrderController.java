package controller;

import dal.OrderDAO;
import dal.TableDAO;
import model.Order;
import model.OrderItem;
import model.MenuItem;

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
                Order currentOrder = orderDAO.getOrderById(orderID); // Hãy đảm bảo bạn có hàm này trong OrderDAO
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
            int itemID = Integer.parseInt(request.getParameter("itemID"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            
            // 🌟 ĐÃ SỬA LỖI 500: Chống lỗi NullPointerException cho note
            String note = request.getParameter("note");
            if (note == null) {
                note = "";
            }
            
            int price = request.getParameter("price") != null ? Integer.parseInt(request.getParameter("price")) : 0;

            Integer tableID = request.getParameter("tableID") != null && !request.getParameter("tableID").isEmpty() ? 
                              Integer.parseInt(request.getParameter("tableID")) : (Integer) session.getAttribute("tableID");

            // NẾU CHƯA CÓ ĐƠN HÀNG -> TẠO ĐƠN HÀNG MỚI (Vẫn phải tạo DB Order trước để lấy cái khung)
            if (currentOrderID == null) {
                Integer customerID = (Integer) session.getAttribute("customerID");
                Order newOrder = new Order();
                newOrder.setCustomerID(customerID);
                newOrder.setOrderType(tableID != null ? 1 : 2); 
                // [TABLE STATUS FLOW] serving chi dung cho orderStatus; ban co khach/host thi la occupied.
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

            // Thêm vào Session thay vì DB
            boolean found = false;
            for (OrderItem item : sessionCart) {
                // 🌟 ĐÃ SỬA LỖI 500: An toàn khi so sánh chuỗi note
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
                // Dùng id âm tạm thời để phân biệt trong session
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
            response.sendRedirect(request.getContextPath() + "/menu?success=added");
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
        // GỬI BẾP CÁC MÓN ĐƯỢC CHỌN (SESSION -> DATABASE)
        // =========================================================
        } else if ("sendToKitchen".equals(action)) {
            String[] selectedItems = request.getParameterValues("selectedItems");
            
            if (currentOrderID != null && !sessionCart.isEmpty() && selectedItems != null) {
                // Gom các ID món được chọn từ giao diện (là các số âm)
                List<Integer> idsToSend = new java.util.ArrayList<>();
                for (String s : selectedItems) {
                    idsToSend.add(Integer.parseInt(s));
                }
                
                // Duyệt giỏ hàng Session, cái nào có tích thì gửi đi và xóa khỏi giỏ
                Iterator<OrderItem> iterator = sessionCart.iterator();
                while (iterator.hasNext()) {
                    OrderItem oi = iterator.next();
                    if (idsToSend.contains(oi.getOrderItemID())) {
                        orderDAO.addOrderItem(currentOrderID, oi.getItemID(), oi.getTableID(), oi.getQuantity(), oi.getPrice(), oi.getNote());
                        iterator.remove(); // Xóa món này khỏi giỏ tạm sau khi đã đẩy xuống bếp
                    }
                }
                
                // Cập nhật lại giỏ Session mới (chứa các món chưa gửi)
                session.setAttribute("sessionCart", sessionCart);
                session.setAttribute("successMsg", "Đã gửi các món được chọn xuống Bếp thành công!");
            }
            response.sendRedirect(request.getContextPath() + "/order?action=cart");
            return;
            
            
        // =========================================================
        // THANH TOÁN TỔNG
        // =========================================================
        } else if ("checkoutTotal".equals(action)) {
            // [YEU CAU THANH TOAN] Khach chi gui yeu cau tinh tien.
            // Hoa don cuoi cung se do nhan vien phuc vu kiem tra va tao.
            if (currentOrderID != null && orderDAO.requestCheckout(currentOrderID)) {
                session.setAttribute("successMsg", "Đã gửi yêu cầu tính tiền. Vui lòng chờ nhân viên kiểm tra và chốt hóa đơn.");
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
    
    // Hàm phụ trợ: Lấy thông tin chi tiết của 1 món ăn dựa vào mã itemID
    private MenuItem getMenuItemById(int itemID) {
        String sql = "SELECT * FROM MenuItem WHERE itemID = ?";
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
        return new MenuItem(); // Trả về object rỗng để không bị lỗi màn hình
    }
}
