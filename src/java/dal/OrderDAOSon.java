package dal;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Order;

public class OrderDAOSon extends DBContext {

    /**
     * Tạo đơn đặt bàn trước (orderType = 1).
     * tableID = NULL — nhân viên gán bàn thực khi khách đến.
     * Lưu areaType + capacity để tính bàn trống chính xác.
     * orderStatus = 'reserved', tableStatus = 'reserved' ngay khi xác nhận.
     *
     * @return orderID nếu thành công, -1 nếu lỗi
     */
    public int createReservation(int customerID, int capacity,
                                 String areaType, Timestamp orderTime,
                                 BigDecimal depositAmount) {
        String sql =
            "INSERT INTO `Order` " +
            "(customerID, tableID, orderType, orderStatus, tableStatus, " +
            " areaType, capacity, orderTime, depositAmount, createdAt) " +
            "VALUES (?, NULL, 1, 'reserved', 'reserved', ?, ?, ?, ?, NOW())";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerID);
            ps.setString(2, areaType);
            ps.setInt(3, capacity);
            ps.setTimestamp(4, orderTime);
            ps.setBigDecimal(5, depositAmount != null ? depositAmount : BigDecimal.ZERO);

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Hủy đơn đặt bàn — chỉ hủy được khi đang reserved.
     * Đồng thời trả tableStatus về 'available'.
     */
    public boolean cancelReservation(int orderID, int customerID) {
        String sql =
            "UPDATE `Order` SET orderStatus = 'cancelled', tableStatus = 'available' " +
            "WHERE orderID = ? AND customerID = ? " +
            "AND orderStatus = 'reserved'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ps.setInt(2, customerID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Auto-cancel các đơn reserved quá 30 phút mà khách chưa đến check-in.
     * Gọi từ background job hoặc lazy-check khi query bàn trống.
     * Chỉ hủy đơn có orderTime đã qua (khách trễ hẹn).
     */
    public int autoExpireReservations() {
        String sql =
            "UPDATE `Order` " +
            "SET orderStatus = 'cancelled', tableStatus = 'available' " +
            "WHERE orderType = 1 " +
            "AND orderStatus = 'reserved' " +
            "AND orderTime < DATE_SUB(NOW(), INTERVAL 30 MINUTE)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Order getOrderByID(int orderID) {
        String sql = "SELECT * FROM `Order` WHERE orderID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Order> getReservationsByCustomer(int customerID) {
        List<Order> list = new ArrayList<>();
        String sql =
            "SELECT * FROM `Order` " +
            "WHERE customerID = ? AND orderType = 1 " +
            "ORDER BY createdAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private Order mapRow(ResultSet rs) throws Exception {
        Order o = new Order();
        o.setOrderID(rs.getInt("orderID"));
        o.setCustomerID(rs.getInt("customerID"));
        o.setTableID(rs.getInt("tableID"));
        o.setInvoiceID(rs.getInt("invoiceID"));
        o.setOrderType(rs.getInt("orderType"));
        o.setTableStatus(rs.getString("tableStatus"));
        o.setTotalAmount(rs.getLong("totalAmount"));
        o.setCheckoutRequestAt(rs.getTimestamp("checkoutRequestAt"));
        o.setIsStaffConfirmed(rs.getInt("isStaffConfirmed"));
        o.setCreatedAt(rs.getTimestamp("createdAt"));
        o.setOrderTime(rs.getTimestamp("orderTime"));
        o.setDepositAmount(rs.getLong("depositAmount"));
        o.setOrderStatus(rs.getString("orderStatus"));
       
        o.setCapacity(rs.getInt("capacity"));// thêm thuôc tinh moi 
        o.setAreaType(rs.getString("areaType"));
        return o;
    }
}