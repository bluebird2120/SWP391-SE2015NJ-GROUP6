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
        // ĐÃ SỬA: Thêm capacity và areaType vào câu lệnh SQL (tổng cộng 14 cột)
        String sql = "INSERT INTO `Order` "
                + "(customerID, employeeID, invoiceID, orderType, tableStatus, "
                + " totalAmount, capacity, areaType, checkoutRequestAt, isStaffConfirmed, "
                + " createdAt, orderTime, depositAmount, orderStatus) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Xử lý các trường có thể NULL (dùng Integer object)
            if (order.getCustomerID() != null) {
                ps.setInt(1, order.getCustomerID());
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            if (order.getEmployeeID() != null) {
                ps.setInt(2, order.getEmployeeID());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            if (order.getInvoiceID() != null) {
                ps.setInt(3, order.getInvoiceID());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setInt(4, order.getOrderType());
            ps.setString(5, order.getTableStatus() != null ? order.getTableStatus() : "available");

            // ĐÃ SỬA: Đổi từ setLong sang setInt
            ps.setInt(6, order.getTotalAmount());

            // ĐÃ SỬA: Thêm 2 tham số mới cho capacity và areaType
            if (order.getCapacity() != null) {
                ps.setInt(7, order.getCapacity());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setString(8, order.getAreaType());

            ps.setTimestamp(9, order.getCheckoutRequestAt());
            ps.setInt(10, order.getIsStaffConfirmed());
            ps.setTimestamp(11, order.getCreatedAt() != null ? order.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(12, order.getOrderTime());

            // ĐÃ SỬA: Đổi từ setLong sang setInt
            ps.setInt(13, order.getDepositAmount());
            ps.setString(14, order.getOrderStatus() != null ? order.getOrderStatus() : "ordering");

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("[OrderDAO] createOrder lỗi: " + e.getMessage());
        }
        return -1;
    }

    // =========================================================
    // 1.5 BỔ SUNG: LIÊN KẾT ORDER VÀ TABLE (BẢNG TRUNG GIAN)
    // =========================================================
    public boolean linkOrderAndTable(int orderID, int tableID) {
        String sql = "INSERT INTO Order_Table (orderID, tableID) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ps.setInt(2, tableID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[OrderDAO] linkOrderAndTable lỗi: " + e.getMessage());
        }
        return false;
    }

    // =========================================================
    // 2. LẤY ORDER ĐANG MỞ CỦA MỘT BÀN (ĐÃ UPDATE DÙNG JOIN)
    // =========================================================
    public Order getActiveOrderByTableId(int tableID) {
        // Dùng JOIN để quét qua bảng Order_Table
        String sql = "SELECT o.* FROM `Order` o "
                + "JOIN Order_Table ot ON o.orderID = ot.orderID "
                + "WHERE ot.tableID = ? "
                + "  AND o.tableStatus = 'occupied' "
                + "  AND o.orderStatus NOT IN ('completed', 'cancelled') "
                + "ORDER BY o.createdAt DESC LIMIT 1";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tableID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapToOrder(rs);
            }

        } catch (SQLException e) {
            System.err.println("[OrderDAO] getActiveOrderByTableId lỗi: " + e.getMessage());
        }
        return null;
    }

    // =========================================================
    // 2.5 LẤY THÔNG TIN ORDER THEO MÃ ORDER (orderID)
    // =========================================================
    public Order getOrderById(int orderID) {
        String sql = "SELECT * FROM `Order` WHERE orderID = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapToOrder(rs);
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] getOrderById lỗi: " + e.getMessage());
        }
        return null;
    }

    // =========================================================
    // 3. THÊM MÓN VÀO GIỎ (ĐÃ UPDATE THÊM tableID & price)
    // =========================================================
    public int addOrderItem(int orderID, int itemID, Integer tableID, int quantity, int price, String note) {
        // Check xem món này có cùng cấu hình (cùng order, item, table, note) chưa để cộng dồn
        OrderItem existing = getOrderItemByOrderAndItemAndTable(orderID, itemID, tableID);
        if (existing != null) {
            int newQty = existing.getQuantity() + quantity;
            boolean updated = updateOrderItemQuantity(existing.getOrderItemID(), newQty);
            return updated ? existing.getOrderItemID() : -1;
        }

        // Bổ sung insert tableID và price chốt cứng
        String sql = "INSERT INTO OrderItem (orderID, itemID, tableID, quantity, price, note) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, orderID);
            ps.setInt(2, itemID);

            if (tableID != null) {
                ps.setInt(3, tableID);
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setInt(4, quantity);
            ps.setInt(5, price);
            ps.setString(6, note);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

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

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderItemID);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[OrderDAO] removeOrderItem lỗi: " + e.getMessage());
        }
        return false;
    }

    // =========================================================
    // 6. LẤY DANH SÁCH ORDER ITEM
    // =========================================================
    public List<OrderItem> getOrderItemsByOrderId(int orderID) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT * FROM OrderItem WHERE orderID = ? ORDER BY orderItemID";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapToOrderItem(rs));
            }

        } catch (SQLException e) {
            System.err.println("[OrderDAO] getOrderItemsByOrderId lỗi: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // 7. LẤY DANH SÁCH MENU ITEM TƯƠNG ỨNG VỚI ORDER
    // =========================================================
    public List<MenuItem> getMenuItemsByOrderId(int orderID) {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT mi.* FROM OrderItem oi "
                + "JOIN MenuItem mi ON mi.itemID = oi.itemID "
                + "WHERE oi.orderID = ? "
                + "ORDER BY oi.orderItemID";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapToMenuItem(rs));
            }

        } catch (SQLException e) {
            System.err.println("[OrderDAO] getMenuItemsByOrderId lỗi: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // 8. LẤY DANH SÁCH OrderItem THEO DANH SÁCH orderItemID
    // =========================================================
    public List<OrderItem> getOrderItemsByIds(List<Integer> orderItemIDs) {
        List<OrderItem> list = new ArrayList<>();
        if (orderItemIDs == null || orderItemIDs.isEmpty()) {
            return list;
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(orderItemIDs.size(), "?"));
        String sql = "SELECT * FROM OrderItem WHERE orderItemID IN (" + placeholders + ") ORDER BY orderItemID";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < orderItemIDs.size(); i++) {
                ps.setInt(i + 1, orderItemIDs.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapToOrderItem(rs));
            }

        } catch (SQLException e) {
            System.err.println("[OrderDAO] getOrderItemsByIds lỗi: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // 9. LẤY DANH SÁCH MenuItem THEO DANH SÁCH orderItemID
    // =========================================================
    public List<MenuItem> getMenuItemsByOrderItemIds(List<Integer> orderItemIDs) {
        List<MenuItem> list = new ArrayList<>();
        if (orderItemIDs == null || orderItemIDs.isEmpty()) {
            return list;
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(orderItemIDs.size(), "?"));
        String sql = "SELECT mi.* FROM OrderItem oi "
                + "JOIN MenuItem mi ON mi.itemID = oi.itemID "
                + "WHERE oi.orderItemID IN (" + placeholders + ") "
                + "ORDER BY oi.orderItemID";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < orderItemIDs.size(); i++) {
                ps.setInt(i + 1, orderItemIDs.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapToMenuItem(rs));
            }

        } catch (SQLException e) {
            System.err.println("[OrderDAO] getMenuItemsByOrderItemIds lỗi: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // HELPER: kiểm tra món đã có trong đơn (Xét cả Bàn)
    // =========================================================
    private OrderItem getOrderItemByOrderAndItemAndTable(int orderID, int itemID, Integer tableID) {
        String sql = "SELECT * FROM OrderItem WHERE orderID = ? AND itemID = ? AND (tableID = ? OR (tableID IS NULL AND ? IS NULL))";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderID);
            ps.setInt(2, itemID);

            if (tableID != null) {
                ps.setInt(3, tableID);
                ps.setInt(4, tableID);
            } else {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapToOrderItem(rs);
            }

        } catch (SQLException e) {
            System.err.println("[OrderDAO] getOrderItemByOrderAndItemAndTable lỗi: " + e.getMessage());
        }
        return null;
    }

    public boolean addTableToExistingOrder(int orderID, int tableID) {
        String sql = "INSERT INTO Order_Table (orderID, tableID) VALUES (?, ?)";

        // ĐÃ SỬA: Thêm new DBContext() để khởi tạo đối tượng trước khi gọi hàm
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderID);
            ps.setInt(2, tableID);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            // Bắt lỗi Duplicate Key nếu khách cố tình gộp 1 bàn 2 lần
            if (e.getErrorCode() == 1062) {
                System.out.println("Bàn này đã nằm trong đơn hàng rồi!");
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            // ĐÃ SỬA: Bắt thêm lỗi chung (như ClassNotFoundException) từ DBContext
            System.out.println("Lỗi kết nối CSDL: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================
    // HELPER: map ResultSet -> Order (ĐÃ CẬP NHẬT THEO MODEL MỚI)
    // =========================================================
    private Order mapToOrder(ResultSet rs) throws SQLException {
        return new Order(
                rs.getInt("orderID"),
                (Integer) rs.getObject("customerID"), // Lấy Integer object để nhận Null
                (Integer) rs.getObject("employeeID"),
                (Integer) rs.getObject("invoiceID"),
                rs.getInt("orderType"),
                rs.getString("tableStatus"),
                rs.getInt("totalAmount"), // ĐÃ SỬA: Dùng getInt thay cho getLong
                rs.getTimestamp("checkoutRequestAt"),
                rs.getInt("isStaffConfirmed"),
                rs.getTimestamp("createdAt"),
                rs.getTimestamp("orderTime"),
                rs.getInt("depositAmount"), // ĐÃ SỬA: Dùng getInt thay cho getLong
                rs.getString("orderStatus"),
                (Integer) rs.getObject("capacity"), // ĐÃ SỬA: Hứng thêm cột capacity
                rs.getString("areaType") // ĐÃ SỬA: Hứng thêm cột areaType
        );
    }

    // =========================================================
    // HELPER: map ResultSet -> OrderItem (ĐÃ THÊM tableID, price)
    // =========================================================
    private OrderItem mapToOrderItem(ResultSet rs) throws SQLException {
        return new OrderItem(
                rs.getInt("orderItemID"),
                rs.getInt("orderID"),
                rs.getInt("itemID"),
                (Integer) rs.getObject("tableID"),
                rs.getInt("quantity"),
                rs.getInt("price"),
                rs.getString("note")
        );
    }

    // =========================================================
    // HELPER: map ResultSet -> MenuItem (ĐÃ THÊM LẠI categoryID)
    // =========================================================
    private MenuItem mapToMenuItem(ResultSet rs) throws SQLException {
        MenuItem mi = new MenuItem();
        mi.setItemID(rs.getInt("itemID"));

        // ĐÃ SỬA: Thêm lại hàm lấy categoryID từ Database
        mi.setCategoryID(rs.getInt("categoryID"));

        mi.setItemName(rs.getString("itemName"));
        mi.setDescription(rs.getString("description"));
        mi.setPrice(rs.getInt("price"));
        mi.setDiscountPercent(rs.getInt("discountPercent"));
        mi.setDiscountedPrice(rs.getInt("discountedPrice"));
        mi.setImage(rs.getString("image"));
        mi.setIsAvailable(rs.getInt("isAvailable"));
        mi.setAllergyNotes(rs.getString("allergyNotes"));
        return mi;
    }
}
