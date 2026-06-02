package dal;

import model.Order;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.OrderItem;

public class OrderDAO extends BaseDAO {

    /**
     * Tạo Order mới, trả về orderID vừa tạo hoặc -1 nếu thất bại.
     */
    public int createOrder(Order order) {
        String sql = "INSERT INTO [Order] "
                   + "(customerID, tableID, invoiceID, orderType, tableStatus, "
                   + " totalAmount, checkoutRequestAt, isStaffConfirmed, "
                   + " createdAt, orderTime, depositAmount, orderStatus) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setObject   (1,  order.getCustomerID() == 0 ? null : order.getCustomerID());  // null nếu walk-in
            ps.setObject   (2,  order.getTableID()    == 0 ? null : order.getTableID());     // null nếu take-away
            ps.setObject   (3,  order.getInvoiceID()  == 0 ? null : order.getInvoiceID());   // null khi mới tạo
            ps.setInt      (4,  order.getOrderType());        // 1=dine-in, 2=take-away, 3=pre-order
            ps.setString   (5,  order.getTableStatus() != null
                               ? order.getTableStatus() : "available");
            ps.setBigDecimal(6, order.getTotalAmount());      // null khi chưa tính tiền
            ps.setTimestamp(7,  order.getCheckoutRequestAt()); // null khi mới tạo
            ps.setInt      (8,  order.getIsStaffConfirmed()); // mặc định 0
            ps.setTimestamp(9,  order.getCreatedAt() != null
                               ? order.getCreatedAt()
                               : new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(10, order.getOrderTime());        // null nếu chưa xác định
            ps.setBigDecimal(11, order.getDepositAmount());   // null hoặc 0 nếu không đặt cọc
            ps.setString   (12, order.getOrderStatus() != null
                               ? order.getOrderStatus() : "pending");

            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);  // trả về orderID vừa tạo
            }

        } catch (SQLException e) {
            System.err.println("[OrderDAO] createOrder lỗi: " + e.getMessage());
        }
        return -1;
    }
    
     // =========================================================
    // 2. KIỂM TRA ĐƠN ĐANG MỞ CỦA BÀN
    // =========================================================
    /**
     * Lấy Order đang active (occupied) của 1 bàn.
     * Dùng trước khi addOrderItem để biết orderID cần thêm món vào.
     * Trả về Order nếu có, null nếu bàn chưa có đơn nào đang mở.
     */
    public Order getActiveOrderByTableId(int tableID) {
        String sql = "SELECT TOP 1 * FROM [Order] "
                   + "WHERE tableID = ? "
                   + "  AND tableStatus = 'occupied' "
                   + "  AND orderStatus NOT IN ('completed', 'checkout') "
                   + "ORDER BY createdAt DESC";
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, tableID);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToOrder(rs);
            }

        } catch (SQLException e) {
            System.err.println("[OrderDAO] getActiveOrderByTableId lỗi: " + e.getMessage());
        }
        return null;
    }

    // =========================================================
    // 3. THÊM MÓN VÀO GIỎ (OrderItem)
    // =========================================================
    /**
     * Thêm 1 món vào OrderItem.
     * Nếu món đã tồn tại trong đơn → gọi updateOrderItemQuantity thay vì insert mới.
     * Trả về orderItemID vừa tạo, hoặc -1 nếu thất bại.
     */
    public int addOrderItem(int orderID, int itemID, int quantity, String note) {
        // Kiểm tra món đã có trong đơn chưa
        OrderItem existing = getOrderItemByOrderAndItem(orderID, itemID);
        if (existing != null) {
            // Đã có → cộng thêm số lượng
            int newQty = existing.getQuantity() + quantity;
            boolean updated = updateOrderItemQuantity(existing.getOrderItemID(), newQty);
            return updated ? existing.getOrderItemID() : -1;
        }

        // Chưa có → insert mới
        String sql = "INSERT INTO OrderItem (orderID, itemID, quantity, note) "
                   + "VALUES (?, ?, ?, ?)";
        try {
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt   (1, orderID);
            ps.setInt   (2, itemID);
            ps.setInt   (3, quantity);
            ps.setString(4, note);

            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("[OrderDAO] addOrderItem lỗi: " + e.getMessage());
        }
        return -1;
    }

    // =========================================================
    // 4. CẬP NHẬT SỐ LƯỢNG MÓN TRONG GIỎ
    // =========================================================
    /**
     * Cập nhật số lượng của 1 OrderItem.
     * Trả về true nếu thành công.
     */
    public boolean updateOrderItemQuantity(int orderItemID, int newQuantity) {
        String sql = "UPDATE OrderItem SET quantity = ? WHERE orderItemID = ?";
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, newQuantity);
            ps.setInt(2, orderItemID);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[OrderDAO] updateOrderItemQuantity lỗi: " + e.getMessage());
        }
        return false;
    }

    // =========================================================
    // 5. XÓA MÓN KHỎI GIỎ
    // =========================================================
    /**
     * Xóa 1 OrderItem khỏi giỏ hàng.
     * Trả về true nếu thành công.
     */
    public boolean removeOrderItem(int orderItemID) {
        String sql = "DELETE FROM OrderItem WHERE orderItemID = ?";
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, orderItemID);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[OrderDAO] removeOrderItem lỗi: " + e.getMessage());
        }
        return false;
    }

    // =========================================================
    // 6. XEM GIỎ HÀNG (danh sách món trong đơn)
    // =========================================================
    /**
     * Lấy toàn bộ món trong giỏ của 1 orderID.
     * JOIN với MenuItem để lấy tên, giá, hình ảnh luôn.
     */
    public List<OrderItem> getOrderItemsByOrderId(int orderID) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.orderItemID, oi.orderID, oi.itemID, oi.quantity, oi.note, "
                   + "       mi.itemName, mi.price, mi.discountedPrice, mi.image "
                   + "FROM OrderItem oi "
                   + "JOIN MenuItem  mi ON mi.itemID = oi.itemID "
                   + "WHERE oi.orderID = ? "
                   + "ORDER BY oi.orderItemID";
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, orderID);
            rs = ps.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setOrderItemID(rs.getInt("orderItemID"));
                item.setOrderID    (rs.getInt("orderID"));
                item.setItemID     (rs.getInt("itemID"));
                item.setQuantity   (rs.getInt("quantity"));
                item.setNote       (rs.getString("note"));

                list.add(item);
            }

        } catch (SQLException e) {
            System.err.println("[OrderDAO] getOrderItemsByOrderId lỗi: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // HELPER: kiểm tra món đã có trong đơn chưa
    // =========================================================
    private OrderItem getOrderItemByOrderAndItem(int orderID, int itemID) {
        String sql = "SELECT * FROM OrderItem WHERE orderID = ? AND itemID = ?";
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, orderID);
            ps.setInt(2, itemID);
            rs = ps.executeQuery();

            if (rs.next()) {
                OrderItem item = new OrderItem();
                item.setOrderItemID(rs.getInt("orderItemID"));
                item.setOrderID    (rs.getInt("orderID"));
                item.setItemID     (rs.getInt("itemID"));
                item.setQuantity   (rs.getInt("quantity"));
                item.setNote       (rs.getString("note"));
                return item;
            }

        } catch (SQLException e) {
            System.err.println("[OrderDAO] getOrderItemByOrderAndItem lỗi: " + e.getMessage());
        }
        return null;
    }

    // =========================================================
    // HELPER: map ResultSet -> Order object
    // =========================================================
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderID          (rs.getInt("orderID"));
        order.setCustomerID       (rs.getInt("customerID"));
        order.setTableID          (rs.getInt("tableID"));
        order.setInvoiceID        (rs.getInt("invoiceID"));
        order.setOrderType        (rs.getInt("orderType"));
        order.setTableStatus      (rs.getString("tableStatus"));
        order.setTotalAmount      (rs.getBigDecimal("totalAmount"));
        order.setCheckoutRequestAt(rs.getTimestamp("checkoutRequestAt"));
        order.setIsStaffConfirmed (rs.getInt("isStaffConfirmed"));
        order.setCreatedAt        (rs.getTimestamp("createdAt"));
        order.setOrderTime        (rs.getTimestamp("orderTime"));
        order.setDepositAmount    (rs.getBigDecimal("depositAmount"));
        order.setOrderStatus      (rs.getString("orderStatus"));
        return order;
    }
}