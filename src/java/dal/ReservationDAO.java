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
import model.Notifications;
import model.Order;
import model.OrderReservationDetail;

public class ReservationDAO extends DBContext {

    public static final int HOLD_MINUTES = 2;
    // Tiền cọc cố định khi khách chỉ đặt bàn.
    public static final int DEFAULT_DEPOSIT_AMOUNT = 100000;

    /**
     * Tạo một đơn đặt bàn và các dòng chi tiết trong cùng transaction. * tại
     * OrderReservationDetail.
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
        // [UNPAID RESERVATION CLEANUP]
        // Chưa cọc: xóa dữ liệu giữ chỗ tạm. Đã cọc: giữ lịch sử và chỉ
        // chuyển cancelled để phục vụ đối soát/hoàn tiền khi cần.
        String stateSql
                = "SELECT o.invoiceID, o.employeeID, "
                + "CASE WHEN LOWER(COALESCE(i.status,''))='paid' "
                + " OR EXISTS (SELECT 1 FROM Payments p "
                + "            WHERE p.invoiceID=o.invoiceID "
                + "              AND p.status='success') "
                + "THEN 1 ELSE 0 END AS isPaid "
                + "FROM `Order` o "
                + "LEFT JOIN Invoices i ON i.invoiceID=o.invoiceID "
                + "WHERE o.orderID=? AND o.customerID=? "
                + "AND o.orderType=1 "
                + "AND o.orderStatus IN ('reserved','pending') "
                + "FOR UPDATE";
        String cancelPaidSql
                = "UPDATE `Order` "
                + "SET orderStatus='cancelled', tableStatus='available' "
                + "WHERE orderID=?";

        try {
            connection.setAutoCommit(false);
            Integer invoiceID;
            Integer assignedEmployeeID;
            boolean paid;
            try (PreparedStatement ps = connection.prepareStatement(stateSql)) {
                ps.setInt(1, orderID);
                ps.setInt(2, customerID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return false;
                    }
                    invoiceID = (Integer) rs.getObject("invoiceID");
                    assignedEmployeeID = (Integer) rs.getObject("employeeID");
                    paid = rs.getInt("isPaid") == 1;
                }
            }

            // [NGHIỆP VỤ] Bàn chỉ được gán khi khách thật sự đến quán (lễ tân gán
            // lúc đó), không gán trước ngay khi cọc. Một khi đã gán bàn + gán nhân
            // viên phụ trách rồi thì coi như khách đã có mặt/đang xử lý tại quán —
            // không cho hủy online nữa (muốn hủy phải báo trực tiếp lễ tân/nhân viên).
            if (assignedEmployeeID != null) {
                connection.rollback();
                return false;
            }

            boolean changed;
            if (paid) {
                try (PreparedStatement ps
                        = connection.prepareStatement(cancelPaidSql)) {
                    ps.setInt(1, orderID);
                    changed = ps.executeUpdate() > 0;
                }
            } else {
                changed = deleteUnpaidReservationData(orderID, invoiceID);
            }

            if (changed) {
                connection.commit();
            } else {
                connection.rollback();
            }

            return changed;
        } catch (Exception e) {
            rollbackQuietly();
            e.printStackTrace();
            return false;
        } finally {
            restoreAutoCommit();
        }
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
     * Đồng bộ đơn giữ bàn với hóa đơn do module thanh toán quản lý. - paid: xác
     * nhận giữ bàn. - paid: xác nhận giữ bàn. - chưa paid và hết thời gian giữ:
     * xóa dữ liệu giữ chỗ tạm. - cancelled chưa paid do luồng cũ/TableDAO tạo:
     * cũng được dọn.
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

        String cleanupCandidatesSql
                = "SELECT o.orderID, o.invoiceID "
                + "FROM `Order` o "
                + "LEFT JOIN Invoices i ON i.invoiceID = o.invoiceID "
                + "WHERE o.orderType = 1 "
                + "  AND o.orderStatus IN ('pending','cancelled') "
                + "  AND LOWER(COALESCE(i.status,'unpaid')) <> 'paid' "
                + "  AND NOT EXISTS (SELECT 1 FROM Payments p "
                + "                  WHERE p.invoiceID=o.invoiceID "
                + "                    AND p.status='success') "
                + "  AND ("
                + "       o.orderStatus='cancelled' "
                + "       OR LOWER(COALESCE(i.status,'')) "
                + "          IN ('failed','cancelled','expired') "
                + "       OR (o.checkoutRequestAt IS NOT NULL "
                + "           AND o.checkoutRequestAt <= NOW())"
                + "  ) FOR UPDATE";

        int changed = 0;
        //Notification
        try {
            connection.setAutoCommit(false);

            // ── [BƯỚC 1] Lấy trước danh sách orderID SẮP được confirm trong lần này.
            //    Phải query TRƯỚC khi UPDATE để chỉ lấy đúng đơn mới, không lặp đơn cũ.
            //    LƯU Ý: KHÔNG lọc theo ngày ở đây nữa — khách đặt cho hôm nay hay
            //    ngày mai/ngày kia đều phải được xác nhận "đặt bàn thành công".
            //    Việc lọc theo ngày (chỉ hôm nay) CHỈ áp dụng cho thông báo của
            //    LỄ TÂN ở BƯỚC 3 bên dưới (vì lễ tân chỉ cần xử lý đúng ngày,
            //    đơn tương lai đã có DailyReservationNotifyTask lo vào đúng ngày lúc 06:00).
            String pendingSql
                    = "SELECT o.orderID, o.customerID, DATE(o.orderTime) = CURDATE() AS isToday "
                    + "FROM `Order` o "
                    + "JOIN Invoices i ON i.invoiceID = o.invoiceID "
                    + "WHERE o.orderType = 1 "
                    + "  AND o.orderStatus = 'pending' " // chưa confirm → sẽ được UPDATE
                    + "  AND i.status = 'paid'";
            List<Integer> newlyConfirmedIDs = new ArrayList<>();
            List<Integer> newlyConfirmedTodayIDs = new ArrayList<>();
            // Map orderID → customerID để thông báo cho đúng customer
            java.util.Map<Integer, Integer> orderCustomerMap = new java.util.LinkedHashMap<>();
            try (PreparedStatement ps0
                    = connection.prepareStatement(pendingSql); ResultSet rs0 = ps0.executeQuery()) {
                while (rs0.next()) {
                    int oID = rs0.getInt("orderID");
                    int cID = rs0.getInt("customerID");
                    boolean isToday = rs0.getBoolean("isToday");
                    newlyConfirmedIDs.add(oID);
                    orderCustomerMap.put(oID, cID);
                    if (isToday) {
                        newlyConfirmedTodayIDs.add(oID);
                    }
                }
            }

            // ── [BƯỚC 2] Thực hiện UPDATE xác nhận cọc ──────────────────────
            int confirmed = 0;
            try (PreparedStatement confirmPs
                    = connection.prepareStatement(confirmSql)) {
                confirmed = confirmPs.executeUpdate();
                changed += confirmed;
            }

            // ── [BƯỚC 3] Thông báo cho lễ tân — chỉ với đơn MỚI vừa confirm.
            //    Dùng danh sách lấy từ BƯỚC 1 (trước UPDATE) nên không bao giờ
            //    lặp lại các đơn cũ đã reserved từ lần chạy trước.
            if (confirmed > 0 && !newlyConfirmedIDs.isEmpty()) {
                NotificationDAO notifDAO = new NotificationDAO();

                // ── [THÔNG BÁO CUSTOMER] Gửi xác nhận đặt bàn thành công cho khách.
                //    Áp dụng cho TẤT CẢ đơn mới confirm, không phân biệt hôm nay hay
                //    ngày tương lai — khách luôn cần biết đơn của mình đã được xác nhận.
                for (int oID : newlyConfirmedIDs) {
                    Integer customerID = orderCustomerMap.get(oID);
                    if (customerID != null) {
                        Notifications nc = new Notifications();
                        nc.setRecipientID(customerID);
                        nc.setRecipientType("customer");
                        nc.setType("reservation_confirmed");
                        nc.setMessage("Đặt bàn thành công! Đơn đã được xác nhận. Vui lòng đến đúng giờ đã đặt.");
                        nc.setIsRead(0);
                        notifDAO.insert(nc);
                    }
                }

                // ── [BƯỚC 3] Thông báo cho lễ tân — chỉ với đơn của HÔM NAY.
                //    Dùng danh sách lấy từ BƯỚC 1 (trước UPDATE) nên không bao giờ
                //    lặp lại các đơn cũ đã reserved từ lần chạy trước.
                if (!newlyConfirmedTodayIDs.isEmpty()) {
                    // [FIX: CHỈ BÁO CHO LỄ TÂN CÓ CA HÔM NAY] Trước đây lấy
                    // TẤT CẢ Employee roleID=3 isActive=1, kể cả người không
                    // có lịch làm hôm nay. Giờ join thêm EmployeeShifts để
                    // chỉ lấy đúng lễ tân có ca hôm nay
                    String receptionistSql
                            = "SELECT DISTINCT es.employeeID "
                            + "FROM EmployeeShifts es "
                            + "JOIN Employee e ON e.employeeID = es.employeeID "
                            + "WHERE es.workDate = CURDATE() "
                            + "AND e.roleID = 3 AND e.isActive = 1";
                    List<Integer> receptionistIDs = new ArrayList<>();
                    try (PreparedStatement rps
                            = connection.prepareStatement(receptionistSql); ResultSet rrs = rps.executeQuery()) {
                        while (rrs.next()) {
                            receptionistIDs.add(rrs.getInt("employeeID"));
                        }
                    }

                    for (int oID : newlyConfirmedTodayIDs) {
                        // Mỗi đơn mới → 1 thông báo riêng cho từng lễ tân
                        for (int recID : receptionistIDs) {
                            Notifications n = new Notifications();
                            n.setRecipientID(recID);
                            n.setRecipientType("staff");
                            n.setType("reservation_needs_table");
                            n.setMessage("Đơn đặt bàn online #" + oID
                                    + " vừa thanh toán cọc thành công, cần gán bàn hôm nay.");
                            n.setIsRead(0);
                            notifDAO.insert(n);
                        }
                    }
                }
            }

            List<Integer> orderIDs = new ArrayList<>();
            List<Integer> invoiceIDs = new ArrayList<>();
            try (PreparedStatement ps
                    = connection.prepareStatement(cleanupCandidatesSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orderIDs.add(rs.getInt("orderID"));
                    invoiceIDs.add((Integer) rs.getObject("invoiceID"));
                }
            }

            // [UNPAID RESERVATION CLEANUP] Chỉ các bản ghi đã được khóa và
            // xác nhận không có payment success mới được xóa.
            for (int i = 0; i < orderIDs.size(); i++) {
                if (deleteUnpaidReservationData(
                        orderIDs.get(i), invoiceIDs.get(i))) {
                    changed++;
                }
            }
            connection.commit();
        } catch (Exception e) {
            rollbackQuietly();
            e.printStackTrace();
        } finally {
            restoreAutoCommit();
        }
        return changed;
    }

    /**
     * [UNPAID RESERVATION CLEANUP] Xóa toàn bộ dữ liệu của một lượt giữ bàn
     * chưa thanh toán. Phương thức này phải được gọi bên trong transaction sau
     * khi Order đã khóa.
     */
    private boolean deleteUnpaidReservationData(
            int orderID, Integer invoiceID) throws Exception {
        String[] orderChildSql = {
            "DELETE FROM TableJoinRequest WHERE orderID=?",
            "DELETE FROM OrderItem WHERE orderID=?",
            "DELETE FROM Order_Table WHERE orderID=?",
            "DELETE FROM order_reservation_detail WHERE orderID=?"
        };
        for (String sql : orderChildSql) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, orderID);
                ps.executeUpdate();
            }
        }

        int deletedOrder;
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM `Order` WHERE orderID=?")) {
            ps.setInt(1, orderID);
            deletedOrder = ps.executeUpdate();
        }

        if (deletedOrder > 0 && invoiceID != null) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM Payments "
                    + "WHERE invoiceID=? AND status<>'success'")) {
                ps.setInt(1, invoiceID);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM Invoices WHERE invoiceID=? "
                    + "AND LOWER(COALESCE(status,''))<>'paid' "
                    + "AND NOT EXISTS (SELECT 1 FROM Payments p "
                    + "WHERE p.invoiceID=? AND p.status='success')")) {
                ps.setInt(1, invoiceID);
                ps.setInt(2, invoiceID);
                ps.executeUpdate();
            }
        }
        return deletedOrder > 0;
    }

    private void rollbackQuietly() {
        try {
            connection.rollback();
        } catch (Exception ignored) {
            // Không che mất lỗi gốc.
        }
    }

    private void restoreAutoCommit() {
        try {
            connection.setAutoCommit(true);
        } catch (Exception ignored) {
            // Connection do DBContext quản lý.
        }
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
