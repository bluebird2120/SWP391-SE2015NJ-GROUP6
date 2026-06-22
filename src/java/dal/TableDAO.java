package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Table;

public class TableDAO extends DBContext {

    public List<String> getAllAreaTypes() {

        List<String> list = new ArrayList<>();

        String sql
                = "SELECT DISTINCT areaType "
                + "FROM `Table` "
                + "WHERE isActive = 1 "
                + "ORDER BY areaType";

        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("areaType"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /*
     * Tính số bàn còn trống theo từng capacity trong khu vực.
     *
     * Logic:
     * Bàn trống = Tổng số bàn - bàn bận trong cùng ngày
     *
     * Bàn bận gồm:
     * - reserved
     * - serving
     * - cleaning
     *
     * Không giới hạn thời gian ăn:
     * Nếu bàn đã reserved / serving / cleaning trong ngày đó
     * thì khóa đến hết ngày đó. hoặc có nhân viên xác nhận đã dọn xẹp xong chuyển trạng thái bàn 
     *
     * Khách vẫn có thể đặt ngày tương lai khác,

     */
    public List<Table> findAvailableTableGroups(String areaType, Timestamp orderTime) {

        autoExpireReservations();

        List<Table> resultList = new ArrayList<>();

        System.out.println("[TableDAO] areaType = " + areaType);
        System.out.println("[TableDAO] orderTime = " + orderTime);

        /*
         * Tổng số bàn active theo capacity trong khu vực.
         */
        String sqlTotal
                = "SELECT capacity, COUNT(*) AS total "
                + "FROM `Table` "
                + "WHERE isActive = 1 "
                + "  AND areaType = ? "
                + "GROUP BY capacity";

        String sqlBusyOnline
                = "SELECT ord.capacity, SUM(ord.quantity) AS busy "
                + "FROM `Order` o "
                + "JOIN order_reservation_detail ord ON ord.orderID = o.orderID "
                + "WHERE o.orderType = 1 "
                + "  AND ord.areaType = ? "
                + "  AND DATE(o.orderTime) = DATE(?) "
                + "  AND o.tableStatus IN ('reserved', 'serving', 'cleaning') "
                + "  AND (o.orderStatus IS NULL OR o.orderStatus <> 'cancelled') "
                + "  AND NOT EXISTS ( "
                + "      SELECT 1 "
                + "      FROM Order_Table ot "
                + "      WHERE ot.orderID = o.orderID "
                + "  ) "
                + "GROUP BY ord.capacity";

        /*
         * Đơn đã được gán bàn thật.
        
         * - Order không lưu capacity / areaType
         
         */
        String sqlBusyAssigned
                = "SELECT t.capacity, COUNT(DISTINCT t.tableID) AS busy "
                + "FROM `Order` o "
                + "JOIN Order_Table ot ON o.orderID = ot.orderID "
                + "JOIN `Table` t ON ot.tableID = t.tableID "
                + "WHERE t.isActive = 1 "
                + "  AND t.areaType = ? "
                + "  AND DATE(o.orderTime) = DATE(?) "
                + "  AND o.tableStatus IN ('reserved', 'serving', 'cleaning') "
                + "  AND (o.orderStatus IS NULL OR o.orderStatus <> 'cancelled') "
                + "GROUP BY t.capacity";

        Map<Integer, Integer> totalMap = new HashMap<>();
        Map<Integer, Integer> busyOnlineMap = new HashMap<>();
        Map<Integer, Integer> busyAssignedMap = new HashMap<>();

        try {
            /*
             * Query 1: lấy tổng số bàn.
             */
            try (PreparedStatement ps = connection.prepareStatement(sqlTotal)) {
                ps.setString(1, areaType);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        totalMap.put(rs.getInt("capacity"), rs.getInt("total"));
                    }
                }
            }

            /*
             * Query 2: lấy số bàn bận do đơn online chưa gán bàn.
             */
            try (PreparedStatement ps = connection.prepareStatement(sqlBusyOnline)) {
                ps.setString(1, areaType);
                ps.setTimestamp(2, orderTime);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        busyOnlineMap.put(rs.getInt("capacity"), rs.getInt("busy"));
                    }
                }
            }

            /*
             * Query 3: lấy số bàn bận do đơn đã gán bàn thật.
             */
            try (PreparedStatement ps = connection.prepareStatement(sqlBusyAssigned)) {
                ps.setString(1, areaType);
                ps.setTimestamp(2, orderTime);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        busyAssignedMap.put(rs.getInt("capacity"), rs.getInt("busy"));
                    }
                }
            }

            System.out.println("[TableDAO] totalMap = " + totalMap);
            System.out.println("[TableDAO] busyOnlineMap = " + busyOnlineMap);
            System.out.println("[TableDAO] busyAssignedMap = " + busyAssignedMap);

            /*
             * Tính bàn trống:
             * available = total - busyOnline - busyAssigned
             */
            for (Map.Entry<Integer, Integer> entry : totalMap.entrySet()) {
                int cap = entry.getKey();
                int total = entry.getValue();

                int busyOnline = busyOnlineMap.getOrDefault(cap, 0);
                int busyAssigned = busyAssignedMap.getOrDefault(cap, 0);

                int availableCount = Math.max(0, total - busyOnline - busyAssigned);

                Table dto = new Table();
                dto.setCapacity(cap);
                dto.setAreaType(areaType);
                dto.setTableName("Bàn " + cap + " chỗ");

                dto.setIsActive(availableCount);

                resultList.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        resultList.sort((a, b) -> Integer.compare(a.getCapacity(), b.getCapacity()));

        return resultList;
    }

    /*
     * Lazy expire:
     *
     * Nếu khách đặt bàn online,
     * quá giờ đến 30 phút mà nhân viên chưa chuyển sang serving,
     * thì đơn tự động bị hủy.
     *
     * Chỉ hủy orderType = 1.
     * Không động vào khách walk-in / đơn đang serving / cleaning.
     */
    private void autoExpireReservations() {
        String confirmPaidSql
                = "UPDATE `Order` o "
                + "JOIN Invoices i ON i.invoiceID = o.invoiceID "
                + "SET o.orderStatus = 'reserved', "
                + "    o.tableStatus = 'reserved', "
                + "    o.checkoutRequestAt = NULL "
                + "WHERE o.orderType = 1 "
                + "  AND o.orderStatus = 'pending' "
                + "  AND i.status = 'paid'";

        String releasePendingSql
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

        String lateArrivalSql
                = "UPDATE `Order` "
                + "SET orderStatus = 'cancelled', tableStatus = 'available' "
                + "WHERE orderType = 1 "
                + "  AND orderStatus = 'reserved' "
                + "  AND tableStatus = 'reserved' "
                + "  AND orderTime < DATE_SUB(NOW(), INTERVAL 30 MINUTE)";

        try (PreparedStatement confirmPs = connection.prepareStatement(confirmPaidSql);
                PreparedStatement pendingPs = connection.prepareStatement(releasePendingSql);
                PreparedStatement latePs = connection.prepareStatement(lateArrivalSql)) {
            confirmPs.executeUpdate();
            int expired = pendingPs.executeUpdate() + latePs.executeUpdate();

            if (expired > 0) {
                System.out.println("[TableDAO] Auto-expired " + expired + " reservation(s).");
            }

        } catch (Exception e) {
            System.err.println("[TableDAO] autoExpire error: " + e.getMessage());
        }
    }

    public Table getTableByTableID(int tableID) {
        String sql = "SELECT * FROM `Table` WHERE tableID = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tableID);

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

    public List<Table> getAllActiveTables() {
        List<Table> list = new ArrayList<>();

        String sql
                = "SELECT * "
                + "FROM `Table` "
                + "WHERE isActive = 1 "
                + "ORDER BY areaType, capacity";

        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private Table mapRow(ResultSet rs) throws Exception {
        Table t = new Table();

        t.setTableID(rs.getInt("tableID"));
        t.setEmployeeID(rs.getInt("employeeID"));
        t.setTableName(rs.getString("tableName"));
        t.setCapacity(rs.getInt("capacity"));
        t.setQRCodeToken(rs.getString("QRCodeToken"));
        t.setAreaType(rs.getString("areaType"));
        t.setIsActive(rs.getInt("isActive"));

        return t;
    }

    // =========================================================
    // CÁC HÀM CHỨC NĂNG QUẢN LÝ BÀN (TABLE MANAGEMENT)
    // =========================================================
    /**
     * 1. Xem danh sách TẤT CẢ các bàn (Dành cho Owner & Employee)
     */
    public List<Table> getAllTablesForManagement() {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM `Table` ORDER BY tableID DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 1.5 BỔ SUNG: Tìm kiếm và lọc danh sách bàn đa tiêu chí (SẠCH LỖI KHỚP
     * 100%)
     */
    public List<Table> searchTables(String name, Integer capacity, String area, Integer status) {
        List<Table> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM `Table` WHERE 1=1");

        if (name != null && !name.isEmpty()) {
            sql.append(" AND tableName LIKE ?");
        }
        if (capacity != null) {
            sql.append(" AND capacity = ?");
        }
        if (area != null && !area.isEmpty()) {
            sql.append(" AND areaType = ?");
        }
        if (status != null) {
            sql.append(" AND isActive = ?");
        }
        sql.append(" ORDER BY tableID DESC");

        // Sử dụng trực tiếp đối tượng connection từ DBContext kế thừa sang
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int index = 1;
            if (name != null && !name.isEmpty()) {
                ps.setString(index++, "%" + name + "%");
            }
            if (capacity != null) {
                ps.setInt(index++, capacity);
            }
            if (area != null && !area.isEmpty()) {
                ps.setString(index++, area);
            }
            if (status != null) {
                ps.setInt(index++, status);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Gọi hàm mapRow(rs) có sẵn của bạn để không bị trùng lặp code gán dữ liệu
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("[TableDAO] searchTables lỗi: " + e.getMessage());
        }
        return list;
    }

    /**
     * 2. Thêm bàn mới (Chỉ dành cho Owner)
     */
    public boolean addTable(Table t) {
        String sql = "INSERT INTO `Table` (employeeID, tableName, capacity, QRCodeToken, areaType, isActive) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        String uniqueQRToken = java.util.UUID.randomUUID().toString();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (t.getEmployeeID() > 0) {
                ps.setInt(1, t.getEmployeeID());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }

            ps.setString(2, t.getTableName());
            ps.setInt(3, t.getCapacity());
            ps.setString(4, uniqueQRToken);
            ps.setString(5, t.getAreaType() != null ? t.getAreaType() : "public");
            ps.setInt(6, t.getIsActive());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 3. Cập nhật thông tin bàn (Chỉ dành cho Owner)
     */
    public boolean updateTable(Table t) {
        String sql = "UPDATE `Table` SET tableName = ?, capacity = ?, areaType = ?, isActive = ? "
                + "WHERE tableID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, t.getTableName());
            ps.setInt(2, t.getCapacity());
            ps.setString(3, t.getAreaType());
            ps.setInt(4, t.getIsActive());
            ps.setInt(5, t.getTableID());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================
    // CÁC HÀM HỖ TRỢ TÍNH NĂNG QUÉT QR GỘP BÀN
    // =========================================================
    /**
     * Tìm thông tin bàn dựa trên mã QRCodeToken (Mã bảo mật)
     */
    public Table getTableByToken(String token) {
        String sql = "SELECT * FROM `Table` WHERE QRCodeToken = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            System.err.println("[TableDAO] getTableByToken lỗi: " + e.getMessage());
        }
        return null;
    }

    /**
     * Kiểm tra xem bàn có đang rảnh hay không. Bàn rảnh = Không có đơn hàng nào
     * đang 'reserved', 'preparing' hoặc 'serving' gắn với bàn này trong bảng
     * Order_Table.
     */
    public boolean isTableAvailable(int tableID) {
        String sql = "SELECT COUNT(*) FROM Order_Table ot "
                + "JOIN `Order` o ON ot.orderID = o.orderID "
                + "WHERE ot.tableID = ? AND o.orderStatus NOT IN ('completed', 'cancelled')";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tableID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count == 0; // Nếu = 0 tức là không bị kẹt đơn nào -> Bàn rảnh
                }
            }
        } catch (Exception e) {
            System.err.println("[TableDAO] isTableAvailable lỗi: " + e.getMessage());
        }
        return false;
    }
}
