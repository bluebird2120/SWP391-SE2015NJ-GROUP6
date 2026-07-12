package dal;

import model.Order;
import model.OrderItem;
import model.MenuItem;
import model.OrderReservationDetail;
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
                + "(customerID, employeeID, invoiceID, orderType, tableStatus, "
                + " totalAmount, checkoutRequestAt, isStaffConfirmed, "
                + " createdAt, orderTime, depositAmount, orderStatus) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
            ps.setInt(6, order.getTotalAmount());
            ps.setTimestamp(7, order.getCheckoutRequestAt());
            ps.setInt(8, order.getIsStaffConfirmed());
            ps.setTimestamp(9, order.getCreatedAt() != null ? order.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(10, order.getOrderTime());
            ps.setInt(11, order.getDepositAmount());
            ps.setString(12, order.getOrderStatus() != null ? order.getOrderStatus() : "ordering");

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("[OrderDAO] createOrder lỗi: " + e.getMessage());
        }

        return -1;
    }

    // =========================================================
    // 1.5 THÊM CHI TIẾT ĐẶT BÀN
    // =========================================================
    public boolean addOrderReservationDetail(OrderReservationDetail detail) {
        String sql = "INSERT INTO OrderReservationDetail (orderID, capacity, areaType, quantity) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detail.getOrderID());
            ps.setInt(2, detail.getCapacity());
            ps.setString(3, detail.getAreaType());
            ps.setInt(4, detail.getQuantity());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[OrderDAO] addOrderReservationDetail lỗi: " + e.getMessage());
        }
        return false;
    }

    public List<OrderReservationDetail> getReservationDetailsByOrderId(int orderID) {
        List<OrderReservationDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM OrderReservationDetail WHERE orderID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new OrderReservationDetail(
                        rs.getInt("detailID"),
                        rs.getInt("orderID"),
                        rs.getInt("capacity"),
                        rs.getString("areaType"),
                        rs.getInt("quantity")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] getReservationDetailsByOrderId lỗi: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // 1.6 LIÊN KẾT ORDER VÀ TABLE
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
    // 2. LẤY ORDER ĐANG MỞ CỦA MỘT BÀN (Đã fix lỗi nhận diện bàn)
    // =========================================================
    public Order getActiveOrderByTableId(int tableID) {
        //  SỬA CÂU LỆNH SQL ĐỂ BẮT ĐƯỢC ĐƠN ĐẶT TRƯỚC
        String sql = "SELECT o.* FROM `Order` o "
                + "JOIN Order_Table ot ON o.orderID = ot.orderID "
                + "WHERE ot.tableID = ? "
                + "  AND o.orderStatus NOT IN ('completed', 'cancelled') "
                // [TABLE STATUS FLOW] serving la orderStatus, khong dung lam tableStatus.
                + "  AND o.tableStatus IN ('pending', 'reserved', 'arrived', 'occupied', 'cleaning') "
                + "  AND DATE(o.orderTime) = CURRENT_DATE "
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

    // BỔ SUNG: Cập nhật trạng thái bàn cho Order (Dùng cho ScanQRController)
    public boolean updateTableStatus(int orderID, String newStatus) {
        String sql = "UPDATE `Order` SET tableStatus = ? WHERE orderID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[OrderDAO] updateTableStatus lỗi: " + e.getMessage());
        }
        return false;
    }

    // =========================================================
    // 2.5 LẤY THÔNG TIN ORDER THEO MÃ ORDER
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
    // 3. THÊM MÓN VÀO GIỎ
    // =========================================================
    public int addOrderItem(int orderID, int itemID, Integer tableID, int quantity, int price, String note) {
        OrderItem existing = getOrderItemByOrderAndItemAndTable(orderID, itemID, tableID);
        if (existing != null) {
            int newQty = existing.getQuantity() + quantity;
            boolean updated = updateOrderItemQuantity(existing.getOrderItemID(), newQty);
            return updated ? existing.getOrderItemID() : -1;
        }

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
    // 7. LẤY DANH SÁCH MENU ITEM
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
    // 8. LẤY DANH SÁCH OrderItem THEO IDs
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
    // 9. LẤY DANH SÁCH MenuItem THEO IDs
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
    // HELPER: kiểm tra món đã có trong đơn
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
    
    // =========================================================
    // 10. CHỨC NĂNG THÊM MÓN/CỘNG DỒN CHO GIỎ HÀNG
    // =========================================================
    public boolean checkItemExistInOrder(int orderID, int itemID) {
        String sql = "SELECT * FROM OrderItem WHERE orderID = ? AND itemID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ps.setInt(2, itemID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] checkItemExistInOrder lỗi: " + e.getMessage());
        }
        return false;
    }

    public void addOrUpdateOrderItem(int orderID, int itemID, int quantity, double price, String notes) {
        if (checkItemExistInOrder(orderID, itemID)) {
            String sql = "UPDATE OrderItem SET quantity = quantity + ?, note = CONCAT(IFNULL(note,''), CASE WHEN ? != '' THEN CONCAT(' | ', ?) ELSE '' END) WHERE orderID = ? AND itemID = ?";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, quantity);
                ps.setString(2, notes != null ? notes : "");
                ps.setString(3, notes != null ? notes : "");
                ps.setInt(4, orderID);
                ps.setInt(5, itemID);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[OrderDAO] addOrUpdateOrderItem (Update) lỗi: " + e.getMessage());
            }
        } else {
            String sql = "INSERT INTO OrderItem (orderID, itemID, quantity, price, note) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orderID);
                ps.setInt(2, itemID);
                ps.setDouble(3, price);
                ps.setString(4, notes);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[OrderDAO] addOrUpdateOrderItem (Insert) lỗi: " + e.getMessage());
            }
        }
    }
    
    // =========================================================
    // 11. NHÂN VIÊN XÁC NHẬN MỞ BÀN
    // =========================================================
    public boolean confirmTableOrder(int orderID) {
        String sql = "UPDATE `Order` SET isStaffConfirmed = 1 WHERE orderID = ?";
        try (Connection conn = getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLException e) {
            System.err.println("[OrderDAO] confirmTableOrder lỗi: " + e.getMessage());
        }
        return false;
    }
    
    // =========================================================
    // LẤY THÔNG TIN ORDER DỰA TRÊN MÃ HÓA ĐƠN (invoiceID)
    // =========================================================
    public Order getOrderByInvoiceId(int invoiceID) {
        String sql = "SELECT * FROM `Order` WHERE invoiceID = ?";
        try (java.sql.Connection conn = getConnection(); 
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, invoiceID);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToOrder(rs); // Sử dụng lại hàm mapToOrder có sẵn của bạn
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[OrderDAO] getOrderByInvoiceId lỗi: " + e.getMessage());
        }
        return null;
    }

    // =========================================================
    // HELPER: map ResultSet -> Order
    // =========================================================
    private Order mapToOrder(ResultSet rs) throws SQLException {
        return new Order(
                rs.getInt("orderID"),
                (Integer) rs.getObject("customerID"),
                (Integer) rs.getObject("employeeID"),
                (Integer) rs.getObject("invoiceID"),
                rs.getInt("orderType"),
                rs.getString("tableStatus"),
                rs.getInt("totalAmount"),
                rs.getTimestamp("checkoutRequestAt"),
                rs.getInt("isStaffConfirmed"),
                rs.getTimestamp("createdAt"),
                rs.getTimestamp("orderTime"),
                rs.getInt("depositAmount"),
                rs.getString("orderStatus")
        );
    }

    // =========================================================
    // HELPER: map ResultSet -> OrderItem
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
    // HELPER: map ResultSet -> MenuItem
    // =========================================================
    private MenuItem mapToMenuItem(ResultSet rs) throws SQLException {
        MenuItem mi = new MenuItem();
        mi.setItemID(rs.getInt("itemID"));
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

    public boolean addTableToExistingOrder(int orderID, int tableID) {
        String sql = "INSERT INTO Order_Table (orderID, tableID) VALUES (?, ?)";

        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderID);
            ps.setInt(2, tableID);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("Bàn này đã nằm trong đơn hàng rồi!");
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("Lỗi kết nối CSDL: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
