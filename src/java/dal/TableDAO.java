package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Table;

/**
 * DAO CHO BẢNG VẬT LÝ.
 *
 * <p>Thứ tự nhóm hàm:
 * 1) dữ liệu filter/đặt bàn;
 * 2) lấy một bàn hoặc danh sách bàn;
 * 3) CRUD Owner;
 * 4) QR và kiểm tra bàn trống;
 * 5) phân trang;
 * 6) liên kết bàn với order.</p>
 */
public class TableDAO extends DBContext {

    /**
     * Lấy các mức sức chứa đang thực sự tồn tại để tạo bộ lọc động.
     * DISTINCT giúp mỗi mức sức chứa chỉ xuất hiện một lần trong combobox.
     */
    /** Lấy các sức chứa khác nhau từ DB để tạo filter động. */
    public List<Integer> getDistinctCapacities() {
        List<Integer> capacities = new ArrayList<>();
        String sql = "SELECT DISTINCT capacity "
                + "FROM `Table` "
                + "WHERE capacity BETWEEN 1 AND 50 "
                + "ORDER BY capacity ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                capacities.add(rs.getInt("capacity"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return capacities;
    }

    /** Lấy các khu vực bàn khác nhau đang có trong DB. */
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

    // =========================================================
    // 1. OWNER - LIST TABLE (FILTER + PAGINATION)
    // =========================================================

    /** Lấy toàn bộ bàn cho màn quản lý không phân trang. */
    public List<Table> getAllTablesForManagement() {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM `Table` ORDER BY tableID DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Đếm số bàn thỏa bộ lọc để controller tính tổng số trang. */
    public int countSearchTables(String searchName, Integer searchCapacity,
            String searchArea, Integer searchStatus) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM `Table` WHERE 1=1 ");

        appendManagementFilters(sql, searchName, searchCapacity,
                searchArea, searchStatus);

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            bindManagementFilters(ps, searchName, searchCapacity,
                    searchArea, searchStatus);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            System.err.println("[TableDAO] countSearchTables lỗi: "
                    + e.getMessage());
            return 0;
        }
    }

    /** Lấy một trang danh sách bàn theo filter và offset. */
    public List<Table> searchTablesPaging(String searchName,
            Integer searchCapacity, String searchArea, Integer searchStatus,
            int offset, int pageSize) {
        List<Table> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM `Table` WHERE 1=1 ");

        appendManagementFilters(sql, searchName, searchCapacity,
                searchArea, searchStatus);
        sql.append(" ORDER BY tableID DESC LIMIT ? OFFSET ?");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int parameterIndex = bindManagementFilters(ps, searchName,
                    searchCapacity, searchArea, searchStatus);
            ps.setInt(parameterIndex++, pageSize);
            ps.setInt(parameterIndex, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("[TableDAO] searchTablesPaging lỗi: "
                    + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // 2. OWNER - ADD TABLE
    // =========================================================

    /** Thêm bàn và sinh QRCodeToken UUID duy nhất. */
    public boolean addTable(Table table) {
        String sql = "INSERT INTO `Table` "
                + "(employeeID, tableName, capacity, QRCodeToken, "
                + "areaType, isActive) VALUES (?, ?, ?, ?, ?, ?)";
        String qrToken = java.util.UUID.randomUUID().toString();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (table.getEmployeeID() > 0) {
                ps.setInt(1, table.getEmployeeID());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, table.getTableName());
            ps.setInt(3, table.getCapacity());
            ps.setString(4, qrToken);
            ps.setString(5, table.getAreaType() != null
                    ? table.getAreaType() : "public");
            ps.setInt(6, table.getIsActive());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // 3. OWNER - EDIT TABLE
    // =========================================================

    /** Sửa thông tin bàn nhưng không cập nhật QRCodeToken. */
    public boolean updateTable(Table table) {
        String sql = "UPDATE `Table` SET tableName = ?, capacity = ?, "
                + "areaType = ?, isActive = ? WHERE tableID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, table.getTableName());
            ps.setInt(2, table.getCapacity());
            ps.setString(3, table.getAreaType());
            ps.setInt(4, table.getIsActive());
            ps.setInt(5, table.getTableID());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // 4. OWNER - TABLE DETAIL + QR
    // =========================================================

    /** Lấy đúng một bàn cho màn Edit hoặc Detail. */
    public Table getTableByTableID(int tableID) {
        String sql = "SELECT * FROM `Table` WHERE tableID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tableID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // =========================================================
    // 5. RESERVATION / TABLE AVAILABILITY
    // =========================================================

    /*
     * Tính số bàn còn trống theo từng capacity trong khu vực.
 
   
     */
    public List<Table> findAvailableTableGroups(String areaType, Timestamp orderTime) {

       
        new ReservationDAO().autoExpireReservations();

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
                + "  AND o.tableStatus IN ('pending', 'reserved', 'arrived', 'occupied', 'cleaning') "
                + "  AND (o.orderStatus IS NULL OR o.orderStatus <> 'cancelled' OR o.tableStatus = 'cleaning') "
                + "  AND NOT EXISTS ( "
                + "      SELECT 1 "
                + "      FROM Order_Table ot "
                + "      WHERE ot.orderID = o.orderID "
                + "  ) "
                + "GROUP BY ord.capacity";

        /*
         * Đơn đã được gán bàn thật.
         */
        String sqlBusyAssigned
                = "SELECT t.capacity, COUNT(DISTINCT t.tableID) AS busy "
                + "FROM `Order` o "
                + "JOIN Order_Table ot ON o.orderID = ot.orderID "
                + "JOIN `Table` t ON ot.tableID = t.tableID "
                + "WHERE t.isActive = 1 "
                + "  AND t.areaType = ? "
                + "  AND DATE(o.orderTime) = DATE(?) "               
                + "  AND o.tableStatus IN ('pending', 'reserved', 'arrived', 'occupied', 'cleaning') "
                + "  AND (o.orderStatus IS NULL OR o.orderStatus <> 'cancelled' OR o.tableStatus = 'cleaning') "
                + "GROUP BY t.capacity";

        Map<Integer, Integer> totalMap = new HashMap<>();
        Map<Integer, Integer> busyOnlineMap = new HashMap<>();
        Map<Integer, Integer> busyAssignedMap = new HashMap<>();

        try {
            /*
             * Query 1: lấy tổng số bàn thuộc loại bàn 
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

    // =========================================================
    // HÀM MAP DỮ LIỆU DÙNG CHUNG CHO TOÀN BỘ DAO
    // =========================================================
    /** Chuyển một dòng ResultSet thành model Table, dùng chung cho các query. */
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

    /** Dùng chung cho hai query List để tránh lặp điều kiện filter. */
    private void appendManagementFilters(StringBuilder sql,
            String name, Integer capacity, String area, Integer status) {
        if (name != null && !name.isEmpty()) {
            sql.append(" AND tableName LIKE ? ");
        }
        if (capacity != null) {
            sql.append(" AND capacity = ? ");
        }
        if (area != null && !area.isEmpty()) {
            sql.append(" AND areaType = ? ");
        }
        if (status != null) {
            sql.append(" AND isActive = ? ");
        }
    }

    /**
     * Bind filter theo đúng thứ tự appendManagementFilters().
     * Trả index tiếp theo để searchTablesPaging bind LIMIT/OFFSET.
     */
    private int bindManagementFilters(PreparedStatement ps,
            String name, Integer capacity, String area, Integer status)
            throws SQLException {
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
        return index;
    }

    // =========================================================
    // CÁC HÀM CHỨC NĂNG QUẢN LÝ BÀN (TABLE MANAGEMENT)
    // =========================================================
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
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("[TableDAO] searchTables lỗi: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // CÁC HÀM HỖ TRỢ TÍNH NĂNG QUÉT QR GỘP BÀN
    // =========================================================
    /** Tìm bàn từ token nằm trong URL của mã QR. */
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

    /** Kiểm tra bàn chưa thuộc order hoạt động nào trước khi mở/gộp bàn. */
    public boolean isTableAvailable(int tableID) {
        String sql = "SELECT COUNT(*) FROM Order_Table ot "
                + "JOIN `Order` o ON ot.orderID = o.orderID "
                + "WHERE ot.tableID = ? AND o.orderStatus NOT IN ('completed', 'cancelled')";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tableID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count == 0;
                }
            }
        } catch (Exception e) {
            System.err.println("[TableDAO] isTableAvailable lỗi: " + e.getMessage());
        }
        return false;
    }
    
    // =========================================================
    // PHÂN TRANG & TÌM KIẾM
    // =========================================================
    public int countTables() {
        String sql = "SELECT COUNT(*) FROM `Table`";
        try (java.sql.Connection conn = getConnection(); 
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("[TableDAO] countTables lỗi: " + e.getMessage());
        }
        return 0;
    }

    public List<Table> getTablesPaging(int offSet, int pageSize) {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM `Table` ORDER BY tableID ASC LIMIT ? OFFSET ?";
        try (java.sql.Connection conn = getConnection(); 
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, pageSize);
            ps.setInt(2, offSet);
            
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs)); // Dùng chung hàm mapRow(rs)
                }
            }
        } catch (Exception e) {
            System.err.println("[TableDAO] getTablesPaging lỗi: " + e.getMessage());
        }
        return list;
    }
    
    // =========================================================
    // PHÂN TRANG & TÌM KIẾM ĐÃ SỬA LỖI ĐÓNG KẾT NỐI (CONNECTION)
    // =========================================================
    
    // =========================================================
    // LẤY DANH SÁCH TẤT CẢ CÁC BÀN CỦA MỘT ĐƠN HÀNG (DÙNG CHO GIỎ HÀNG)
    // =========================================================
    /** Lấy tất cả bàn đang được gắn với một order (hỗ trợ gộp bàn). */
    public List<Table> getTablesByOrderId(int orderID) {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT t.* FROM `Table` t "
                   + "JOIN Order_Table ot ON t.tableID = ot.tableID "
                   + "WHERE ot.orderID = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs)); // Tận dụng lại hàm mapRow cực chuẩn của bạn
                }
            }
        } catch (Exception e) {
            System.err.println("[TableDAO] getTablesByOrderId lỗi: " + e.getMessage());
        }
        return list;
    }

    /** [ORDER VALIDATION] Xác nhận tableID thuộc đúng order hiện tại. */
    /** Xác minh tableID thực sự thuộc orderID trước khi thêm món. */
    public boolean isTableAssignedToOrder(int orderID, int tableID) {
        String sql = "SELECT 1 FROM Order_Table "
                + "WHERE orderID = ? AND tableID = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ps.setInt(2, tableID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.err.println("[TableDAO] isTableAssignedToOrder lỗi: "
                    + e.getMessage());
            return false;
        }
    }
}
