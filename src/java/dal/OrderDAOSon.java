package dal;

// java.math.BigDecimal;
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

    public static final int HOLD_MINUTES = 5;
    // Tiền cọc cố định khi khách chỉ đặt bàn.
    public static final int DEFAULT_DEPOSIT_AMOUNT = 100000;
    // Khách đặt kèm món cọc thêm 30% tổng tiền món.
    public static final int PREORDER_DEPOSIT_PERCENT = 30;

    /**
     * Tạo một đơn đặt bàn và các dòng chi tiết trong cùng transaction. 
       
     * tại OrderReservationDetail.
     */
    public int createReservation(int customerID, Timestamp orderTime,
            List<OrderReservationDetail> details, Integer depositAmount) {

        String orderSql
                = "INSERT INTO `Order` "
                + "(customerID, employeeID, invoiceID, orderType, tableStatus, "
                + " totalAmount, checkoutRequestAt, isStaffConfirmed, createdAt, "
                + " orderTime, depositAmount, orderStatus) "
                + "VALUES (?, NULL, NULL, 1, 'reserved', "
                + " 0, DATE_ADD(NOW(), INTERVAL " + HOLD_MINUTES
                + " MINUTE), 0, NOW(), ?, ?, 'pending')";

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
                + "  AND orderStatus IN ('reserved', 'pending')";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ps.setInt(2, customerID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public int createDepositInvoice(int orderID, int depositAmount) {
        String invoiceSql
                = "INSERT INTO Invoices "
                + "(invoiceNumber, paymentMethod, subTotal, taxAmount, "
                + " depositDeducted, finalAmount, issuedDate, status) "
                + "VALUES (?, 'vnpay', ?, 0, 0, ?, CURDATE(), 'unpaid')";
        //  Lưu cùng lúc invoiceID và số tiền cọc thực tế
        // để hóa đơn cuối có thể trừ đúng khoản khách đã thanh toán trước.
        String linkSql
                = "UPDATE `Order` SET invoiceID = ?, depositAmount = ? "
                + "WHERE orderID = ? AND orderStatus = 'pending'";

        try {
            connection.setAutoCommit(false);
            int invoiceID;

            try (PreparedStatement ps = connection.prepareStatement(
                    invoiceSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "DEP-" + orderID + "-"
                        + System.currentTimeMillis());
                ps.setInt(2, depositAmount);
                ps.setInt(3, depositAmount);
                if (ps.executeUpdate() == 0) {
                    connection.rollback();
                    return -1;
                }
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        connection.rollback();
                        return -1;
                    }
                    invoiceID = keys.getInt(1);
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(linkSql)) {
                ps.setInt(1, invoiceID);
                ps.setInt(2, depositAmount);
                ps.setInt(3, orderID);
                if (ps.executeUpdate() == 0) {
                    connection.rollback();
                    return -1;
                }
            }

            connection.commit();
            return invoiceID;
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

    public boolean updatePendingDepositInvoice(int orderID, int invoiceID,
            int depositAmount) {
        // Nếu khách đổi món trước khi thanh toán cọc,
        // cập nhật lại hóa đơn DEP- chưa paid để số tiền cọc luôn đúng.
        String sql
                = "UPDATE Invoices i "
                + "JOIN `Order` o ON o.invoiceID = i.invoiceID "
                + "SET i.subTotal = ?, i.finalAmount = ?, "
                + "    o.depositAmount = ? "
                + "WHERE o.orderID = ? "
                + "  AND i.invoiceID = ? "
                + "  AND o.orderStatus = 'pending' "
                + "  AND i.status = 'unpaid' "
                + "  AND i.invoiceNumber LIKE 'DEP-%'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, depositAmount);
            ps.setInt(2, depositAmount);
            ps.setInt(3, depositAmount);
            ps.setInt(4, orderID);
            ps.setInt(5, invoiceID);
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
        int changed = synchronizeDepositStatus();
        String sql
                = "UPDATE `Order` "
                + "SET orderStatus = 'cancelled', tableStatus = 'available' "
                + "WHERE orderType = 1 "
                + "  AND orderStatus = 'reserved' "
                + "  AND tableStatus = 'reserved' "
                + "  AND orderTime < DATE_SUB(NOW(), INTERVAL 30 MINUTE)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            return changed + ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Đồng bộ đơn giữ bàn với hóa đơn do module thanh toán quản lý.
     * - paid: xác nhận giữ bàn.
     * - failed/cancelled/expired: hủy giữ bàn.
     * - chưa thanh toán và hết 5 phút: hủy giữ bàn.
     */
    public int synchronizeDepositStatus() {
        String confirmSql
                = "UPDATE `Order` o "
                + "JOIN Invoices i ON i.invoiceID = o.invoiceID "
                + "SET o.orderStatus = 'reserved', "
                + "    o.tableStatus = 'reserved', "
                + "    o.checkoutRequestAt = NULL "
                + "WHERE o.orderType = 1 "
                + "  AND o.orderStatus = 'pending' "
                + "  AND i.status = 'paid'";

        String cancelSql
                = "UPDATE `Order` o "
                + "LEFT JOIN Invoices i ON i.invoiceID = o.invoiceID "
                + "SET o.orderStatus = 'cancelled', "
                + "    o.tableStatus = 'available' "
                + "WHERE o.orderType = 1 "
                + "  AND o.orderStatus = 'pending' "
                + "  AND ("
                + "      LOWER(COALESCE(i.status, '')) "
                + "          IN ('failed', 'cancelled', 'expired') "
                + "      OR (o.checkoutRequestAt IS NOT NULL "
                + "          AND o.checkoutRequestAt <= NOW() "
                + "          AND COALESCE(i.status, 'unpaid') <> 'paid')"
                + "  )";

        int changed = 0;
        try (PreparedStatement confirmPs = connection.prepareStatement(confirmSql);
                PreparedStatement cancelPs = connection.prepareStatement(cancelSql)) {
            changed += confirmPs.executeUpdate();
            changed += cancelPs.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return changed;
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
