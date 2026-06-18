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

    /*
     * Tạo đơn đặt bàn online.
     *
     * DB mới KHÔNG còn tableID trong bảng Order.
     * Vì vậy đơn online lúc khách đặt chỉ lưu:
     * - customerID
     * - orderType = 1
     * - orderStatus = reserved
     * - tableStatus = reserved
     * - capacity
     * - areaType
     * - orderTime
     *
     * Chưa insert vào Order_Table vì lúc này chưa gán bàn thật.
     */
    public int createReservation(int customerID, int capacity,
            String areaType, Timestamp orderTime,
            BigDecimal depositAmount) {

        String sql
                = "INSERT INTO `Order` "
                + "(customerID, employeeID, invoiceID, orderType, tableStatus, "
                + " totalAmount, capacity, areaType, checkoutRequestAt, "
                + " isStaffConfirmed, createdAt, orderTime, depositAmount, orderStatus) "
                + "VALUES (?, NULL, NULL, 1, 'reserved', "
                + " 0, ?, ?, NULL, "
                + " 0, NOW(), ?, ?, 'reserved')";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, customerID);
            ps.setInt(2, capacity);
            ps.setString(3, areaType);
            ps.setTimestamp(4, orderTime);
            ps.setInt(5, depositAmount != null ? depositAmount.intValue() : 0);

            int affected = ps.executeUpdate();

            if (affected == 0) {
                return -1;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /*
     * Khách tự hủy đơn đặt bàn.
     */
    public boolean cancelReservation(int orderID, int customerID) {
        String sql
                = "UPDATE `Order` "
                + "SET orderStatus = 'cancelled', tableStatus = 'available' "
                + "WHERE orderID = ? "
                + "  AND customerID = ? "
                + "  AND orderType = 1 "
                + "  AND orderStatus = 'reserved'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ps.setInt(2, customerID);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /*
     * Tự hủy đơn đặt online nếu:
     *
     * - Là đơn đặt bàn online: orderType = 1
     * - Vẫn đang reserved
     * - Đã quá giờ khách đến 30 phút
     * - Nhân viên chưa chuyển tableStatus sang serving
     *
     * Ví dụ:
     * Khách đặt 19:00
     * Đến 19:31 mà đơn vẫn là reserved
     * => tự động chuyển cancelled / available
     */
    public int autoExpireReservations() {
        String sql
                = "UPDATE `Order` "
                + "SET orderStatus = 'cancelled', tableStatus = 'available' "
                + "WHERE orderType = 1 "
                + "  AND orderStatus = 'reserved' "
                + "  AND tableStatus = 'reserved' "
                + "  AND orderTime < DATE_SUB(NOW(), INTERVAL 30 MINUTE)";

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
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Order> getReservationsByCustomer(int customerID) {
        List<Order> list = new ArrayList<>();

        String sql
                = "SELECT * FROM `Order` "
                + "WHERE customerID = ? "
                + "  AND orderType = 1 "
                + "ORDER BY createdAt DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
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
        o.setInvoiceID(rs.getInt("invoiceID"));
        o.setOrderType(rs.getInt("orderType"));
        o.setTableStatus(rs.getString("tableStatus"));
        o.setTotalAmount(rs.getInt("totalAmount"));
        o.setCheckoutRequestAt(rs.getTimestamp("checkoutRequestAt"));
        o.setIsStaffConfirmed(rs.getInt("isStaffConfirmed"));
        o.setCreatedAt(rs.getTimestamp("createdAt"));
        o.setOrderTime(rs.getTimestamp("orderTime"));
        o.setDepositAmount(rs.getInt("depositAmount"));
        o.setOrderStatus(rs.getString("orderStatus"));
        o.setCapacity(rs.getInt("capacity"));
        o.setAreaType(rs.getString("areaType"));

        return o;
    }
}
