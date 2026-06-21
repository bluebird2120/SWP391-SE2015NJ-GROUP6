package dal;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Order;
import model.OrderReservationDetail;

public class OrderDAOSon extends DBContext {

    /**
     * Tạo một đơn đặt bàn và các dòng chi tiết trong cùng transaction. Order
     * không còn lưu capacity/areaType; các giá trị đó cùng quantity được lưu
     * tại OrderReservationDetail.
     */
    public int createReservation(int customerID, Timestamp orderTime,
            List<OrderReservationDetail> details, BigDecimal depositAmount) {

        String orderSql
                = "INSERT INTO `Order` "
                + "(customerID, employeeID, invoiceID, orderType, tableStatus, "
                + " totalAmount, checkoutRequestAt, isStaffConfirmed, createdAt, "
                + " orderTime, depositAmount, orderStatus) "
                + "VALUES (?, NULL, NULL, 1, 'reserved', "
                + " 0, NULL, 0, NOW(), ?, ?, 'reserved')";

        String detailSql
                = "INSERT INTO order_reservation_detail "
                + "(orderID, capacity, areaType, quantity) "
                + "VALUES (?, ?, ?, ?)";

        if (details == null || details.isEmpty()) {
            return -1;
        }

        try {
            connection.setAutoCommit(false);
            int orderID;

            try (PreparedStatement ps = connection.prepareStatement(
                    orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, customerID);
                ps.setTimestamp(2, orderTime);
                ps.setInt(3, depositAmount != null ? depositAmount.intValue() : 0);

                if (ps.executeUpdate() == 0) {
                    connection.rollback();
                    return -1;
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        connection.rollback();
                        return -1;
                    }
                    orderID = keys.getInt(1);
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(detailSql)) {
                for (OrderReservationDetail detail : details) {
                    ps.setInt(1, orderID);
                    ps.setInt(2, detail.getCapacity());
                    ps.setString(3, detail.getAreaType());
                    ps.setInt(4, detail.getQuantity());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            connection.commit();
            return orderID;

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception rollbackError) {
                rollbackError.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return -1;
    }

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

    /**
     * Sau giờ hẹn 30 phút, nếu nhân viên chưa chuyển bàn từ reserved sang
     * serving thì đơn được hủy và bàn được tính là available trở lại.
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

    public List<OrderReservationDetail> getReservationDetails(int orderID) {
        List<OrderReservationDetail> details = new ArrayList<>();

        String sql
                = "SELECT detailID, orderID, capacity, areaType, quantity "
                + "FROM order_reservation_detail "
                + "WHERE orderID = ? "
                + "ORDER BY areaType, capacity";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    details.add(mapReservationDetail(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return details;
    }

    public Map<Integer, List<OrderReservationDetail>> getReservationDetailsByCustomer(
            int customerID) {
        Map<Integer, List<OrderReservationDetail>> detailsByOrder = new LinkedHashMap<>();

        String sql
                = "SELECT ord.detailID, ord.orderID, ord.capacity, "
                + "ord.areaType, ord.quantity "
                + "FROM order_reservation_detail ord "
                + "JOIN `Order` o ON o.orderID = ord.orderID "
                + "WHERE o.customerID = ? "
                + "  AND o.orderType = 1 "
                + "ORDER BY o.createdAt DESC, ord.areaType, ord.capacity";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderReservationDetail detail = mapReservationDetail(rs);
                    detailsByOrder
                            .computeIfAbsent(detail.getOrderID(), key -> new ArrayList<>())
                            .add(detail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return detailsByOrder;
    }

    private OrderReservationDetail mapReservationDetail(ResultSet rs) throws Exception {
        OrderReservationDetail detail = new OrderReservationDetail();
        detail.setDetailID(rs.getInt("detailID"));
        detail.setOrderID(rs.getInt("orderID"));
        detail.setCapacity(rs.getInt("capacity"));
        detail.setAreaType(rs.getString("areaType"));
        detail.setQuantity(rs.getInt("quantity"));
        return detail;
    }

    private Order mapRow(ResultSet rs) throws Exception {
        Order o = new Order();

        o.setOrderID(rs.getInt("orderID"));
        o.setCustomerID((Integer) rs.getObject("customerID"));
        o.setEmployeeID((Integer) rs.getObject("employeeID"));
        o.setInvoiceID((Integer) rs.getObject("invoiceID"));
        o.setOrderType(rs.getInt("orderType"));
        o.setTableStatus(rs.getString("tableStatus"));
        o.setTotalAmount(rs.getInt("totalAmount"));
        o.setCheckoutRequestAt(rs.getTimestamp("checkoutRequestAt"));
        o.setIsStaffConfirmed(rs.getInt("isStaffConfirmed"));
        o.setCreatedAt(rs.getTimestamp("createdAt"));
        o.setOrderTime(rs.getTimestamp("orderTime"));
        o.setDepositAmount(rs.getInt("depositAmount"));
        o.setOrderStatus(rs.getString("orderStatus"));

        return o;
    }
}
