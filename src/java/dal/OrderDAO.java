package dal;


import model.Order;
import model.OrderItem;
import model.MenuItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    private Connection getConnection() {
        return new DBContext().getConnection();
    }

    // =========================================================
    // 1. TẠO ORDER MỚI
    // =========================================================
    public int createOrder(Order order) {
        String sql = "INSERT INTO `Order` "
                   + "(customerID, tableID, invoiceID, orderType, tableStatus, "
                   + " totalAmount, checkoutRequestAt, isStaffConfirmed, "
                   + " createdAt, orderTime, depositAmount, orderStatus) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject    (1,  order.getCustomerID() == 0 ? null : order.getCustomerID());
            ps.setObject    (2,  order.getTableID()    == 0 ? null : order.getTableID());
            ps.setObject    (3,  order.getInvoiceID()  == 0 ? null : order.getInvoiceID());
            ps.setInt       (4,  order.getOrderType());
            ps.setString    (5,  order.getTableStatus() != null ? order.getTableStatus() : "available");
            ps.setLong(6,  order.getTotalAmount());
            ps.setTimestamp (7,  order.getCheckoutRequestAt());
            ps.setInt       (8,  order.getIsStaffConfirmed());
            ps.setTimestamp (9,  order.getCreatedAt() != null
                                ? order.getCreatedAt()
                                : new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp (10, order.getOrderTime());
            ps.setLong(11, order.getDepositAmount());
            ps.setString    (12, order.getOrderStatus() != null ? order.getOrderStatus() : "pending");

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[OrderDAO] createOrder lỗi: " + e.getMessage());
        }
        return -1;
    }

    // =========================================================
    // 2. LẤY ORDER ĐANG MỞ CỦA MỘT BÀN
    // =========================================================
    public Order getActiveOrderByTableId(int tableID) {
        String sql = "SELECT * FROM `Order` "
                   + "WHERE tableID = ? "
                   + "  AND tableStatus = 'occupied' "
                   + "  AND orderStatus NOT IN ('completed', 'checkout') "
                   + "ORDER BY createdAt DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tableID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapToOrder(rs);

        } catch (SQLException e) {
            System.err.println("[OrderDAO] getActiveOrderByTableId lỗi: " + e.getMessage());
        }
        return null;
    }

    // =========================================================
    // 3. THÊM MÓN VÀO GIỎ
    // =========================================================
    public int addOrderItem(int orderID, int itemID, int quantity, String note) {
        OrderItem existing = getOrderItemByOrderAndItem(orderID, itemID);
        if (existing != null) {
            int newQty = existing.getQuantity() + quantity;
            boolean updated = updateOrderItemQuantity(existing.getOrderItemID(), newQty);
            return updated ? existing.getOrderItemID() : -1;
        }

        String sql = "INSERT INTO OrderItem (orderID, itemID, quantity, note) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1, orderID);
            ps.setInt   (2, itemID);
            ps.setInt   (3, quantity);
            ps.setString(4, note);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[OrderDAO] addOrderItem lỗi: " + e.getMessage());
        }
        return -1;
    }

    // =========================================================
    // 4. CẬP NHẬT SỐ LƯỢNG
    // =========================================================
    public boolean updateOrderItemQuantity(int orderItemID, int newQuantity) {
        String sql = "UPDATE OrderItem SET quantity = ? WHERE orderItemID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
    public boolean removeOrderItem(int orderItemID) {
        String sql = "DELETE FROM OrderItem WHERE orderItemID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderItemID);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[OrderDAO] removeOrderItem lỗi: " + e.getMessage());
        }
        return false;
    }

    // =========================================================
    // 6. LẤY DANH SÁCH ORDER ITEM (chỉ dữ liệu OrderItem)
    // =========================================================
    public List<OrderItem> getOrderItemsByOrderId(int orderID) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT * FROM OrderItem WHERE orderID = ? ORDER BY orderItemID";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapToOrderItem(rs));

        } catch (SQLException e) {
            System.err.println("[OrderDAO] getOrderItemsByOrderId lỗi: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // 7. LẤY DANH SÁCH MENU ITEM TƯƠNG ỨNG VỚI ORDER
    // Thứ tự trả về KHỚP với getOrderItemsByOrderId
    // =========================================================
    public List<MenuItem> getMenuItemsByOrderId(int orderID) {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT mi.* "
                   + "FROM OrderItem oi "
                   + "JOIN MenuItem mi ON mi.itemID = oi.itemID "
                   + "WHERE oi.orderID = ? "
                   + "ORDER BY oi.orderItemID";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapToMenuItem(rs));

        } catch (SQLException e) {
            System.err.println("[OrderDAO] getMenuItemsByOrderId lỗi: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // HELPER: kiểm tra món đã có trong đơn chưa
    // =========================================================
    private OrderItem getOrderItemByOrderAndItem(int orderID, int itemID) {
        String sql = "SELECT * FROM OrderItem WHERE orderID = ? AND itemID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderID);
            ps.setInt(2, itemID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapToOrderItem(rs);

        } catch (SQLException e) {
            System.err.println("[OrderDAO] getOrderItemByOrderAndItem lỗi: " + e.getMessage());
        }
        return null;
    }

    // =========================================================
    // HELPER: map ResultSet -> Order
    // =========================================================
    private Order mapToOrder(ResultSet rs) throws SQLException {
        return new Order(
            rs.getInt       ("orderID"),
            rs.getInt       ("customerID"),
            rs.getInt       ("tableID"),
            rs.getInt       ("invoiceID"),
            rs.getString    ("tableStatus"),
            rs.getLong("totalAmount"),
            rs.getTimestamp ("checkoutRequestAt"),
            rs.getInt       ("isStaffConfirmed"),
            rs.getTimestamp ("createdAt"),
            rs.getInt       ("orderType"),
            rs.getTimestamp ("orderTime"),
            rs.getLong("depositAmount"),
            rs.getString    ("orderStatus")
        );
    }

    // =========================================================
    // HELPER: map ResultSet -> OrderItem
    // =========================================================
    private OrderItem mapToOrderItem(ResultSet rs) throws SQLException {
        return new OrderItem(
            rs.getInt   ("orderItemID"),
            rs.getInt   ("orderID"),
            rs.getInt   ("itemID"),
            rs.getInt   ("quantity"),
            rs.getString("note")
        );
    }

    // =========================================================
    // HELPER: map ResultSet -> MenuItem
    // =========================================================
    private MenuItem mapToMenuItem(ResultSet rs) throws SQLException {
        return new MenuItem(
            rs.getInt       ("itemID"),
            rs.getInt       ("categoryID"),
            rs.getString    ("itemName"),
            rs.getString    ("description"),
            rs.getInt("price"),
            rs.getInt("discountPercent"),
            rs.getInt("discountedPrice"),
            rs.getString    ("image"),
            rs.getInt       ("isAvailable"),
            rs.getString    ("allergyNotes"),
            null
        );
    }
}