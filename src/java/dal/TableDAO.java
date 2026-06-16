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
        String sql = "SELECT DISTINCT areaType FROM `Table` WHERE isActive = 1 ORDER BY areaType";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString("areaType"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tính số bàn còn trống theo từng capacity trong khu vực.
     *
     * Công thức:
     *   Còn trống = Tổng bàn (capacity=X, khu=Y)
     *             - Bàn bận có tableStatus IN ('reserved','serving','cleaning')
     *
     * Bàn bận gồm 2 loại:
     *   1. Đơn đặt online (tableID IS NULL): đếm theo capacity + areaType trong Order
     *   2. Khách walk-in (tableID IS NOT NULL): JOIN Table lấy capacity + areaType thực tế
     *
     * Trước khi tính, lazy-expire các đơn reserved quá 30 phút.
     */
    public List<Table> findAvailableTableGroups(String areaType, Timestamp orderTime) {

        autoExpireReservations();

        List<Table> resultList = new ArrayList<>();

        // Query 1: Tổng số bàn theo capacity trong khu vực
        String sqlTotal =
            "SELECT capacity, COUNT(*) AS total " +
            "FROM `Table` " +
            "WHERE isActive = 1 AND areaType = ? " +
            "GROUP BY capacity";

        // Query 2: Bàn bận — đơn online (tableID IS NULL)
        String sqlBusyOnline =
            "SELECT capacity, COUNT(*) AS busy " +
            "FROM `Order` " +
            "WHERE tableID IS NULL " +
            "  AND areaType = ? " +
            "  AND tableStatus IN ('reserved', 'serving', 'cleaning') " +
            "  AND NOT ( " +
            "        orderStatus = 'reserved' " +
            "        AND orderTime < DATE_SUB(NOW(), INTERVAL 30 MINUTE) " +
            "      ) " +
            "GROUP BY capacity";

        // Query 3: Bàn bận — walk-in (tableID IS NOT NULL)
        String sqlBusyWalkin =
            "SELECT t.capacity, COUNT(DISTINCT t.tableID) AS busy " +
            "FROM `Order` o " +
            "JOIN `Table` t ON o.tableID = t.tableID " +
            "WHERE t.isActive = 1 " +
            "  AND t.areaType = ? " +
            "  AND o.tableStatus IN ('reserved', 'serving', 'cleaning') " +
            "GROUP BY t.capacity";

        Map<Integer, Integer> totalMap      = new HashMap<>();
        Map<Integer, Integer> busyOnlineMap = new HashMap<>();
        Map<Integer, Integer> busyWalkinMap = new HashMap<>();

        try {
            // Query 1
            try (PreparedStatement ps = connection.prepareStatement(sqlTotal)) {
                ps.setString(1, areaType);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        totalMap.put(rs.getInt("capacity"), rs.getInt("total"));
                }
            }

            // Query 2
            try (PreparedStatement ps = connection.prepareStatement(sqlBusyOnline)) {
                ps.setString(1, areaType);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        busyOnlineMap.put(rs.getInt("capacity"), rs.getInt("busy"));
                }
            }

            // Query 3
            try (PreparedStatement ps = connection.prepareStatement(sqlBusyWalkin)) {
                ps.setString(1, areaType);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        busyWalkinMap.put(rs.getInt("capacity"), rs.getInt("busy"));
                }
            }

            // Tổng hợp: tính số bàn còn trống từng loại capacity
            for (Map.Entry<Integer, Integer> entry : totalMap.entrySet()) {
                int cap            = entry.getKey();
                int total          = entry.getValue();
                int busyOnline     = busyOnlineMap.getOrDefault(cap, 0);
                int busyWalkin     = busyWalkinMap.getOrDefault(cap, 0);
                int availableCount = Math.max(0, total - busyOnline - busyWalkin);

                Table dto = new Table();
                dto.setCapacity(cap);
                dto.setAreaType(areaType);
                dto.setTableName("Bàn " + cap + " chỗ");
                dto.setIsActive(availableCount); // isActive = số bàn còn trống

                resultList.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        resultList.sort((a, b) -> Integer.compare(a.getCapacity(), b.getCapacity()));
        return resultList;
    }

    /**
     * Lazy expire: hủy các đơn reserved quá 30 phút mà khách chưa đến.
     */
    private void autoExpireReservations() {
        String sql =
            "UPDATE `Order` " +
            "SET orderStatus = 'cancelled', tableStatus = 'available' " +
            "WHERE orderType = 1 " +
            "  AND orderStatus = 'reserved' " +
            "  AND orderTime < DATE_SUB(NOW(), INTERVAL 30 MINUTE)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int expired = ps.executeUpdate();
            if (expired > 0)
                System.out.println("[TableDAO] Auto-expired " + expired + " reservation(s).");
        } catch (Exception e) {
            System.err.println("[TableDAO] autoExpire error: " + e.getMessage());
        }
    }

    public Table getTableByTableID(int tableID) {
        String sql = "SELECT * FROM `Table` WHERE tableID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tableID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Table> getAllActiveTables() {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM `Table` WHERE isActive = 1 ORDER BY areaType, capacity";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private Table mapRow(ResultSet rs) throws Exception {
        Table t = new Table();
        t.setTableID(rs.getInt("tableID"));
        t.setEmployeeID(rs.getInt("employeeID"));
        
        // BỎ DÒNG NÀY ĐI VÌ DATABASE MỚI KHÔNG CÒN CỘT NÀY NỮA
        // t.setCurrentStaffID(rs.getInt("currentStaffID")); 
        
        t.setTableName(rs.getString("tableName"));
        t.setCapacity(rs.getInt("capacity"));
        t.setQRCodeToken(rs.getString("QRCodeToken"));
        t.setAreaType(rs.getString("areaType"));
        t.setIsActive(rs.getInt("isActive"));
        return t;
    }
    
    // =========================================================
    // CÁC HÀM THÊM MỚI CHO CHỨC NĂNG QUẢN LÝ BÀN (TABLE MANAGEMENT)
    // =========================================================

    /**
     * 1. Xem danh sách TẤT CẢ các bàn (Dành cho Owner & Employee)
     * Khác với getAllActiveTables() của bạn bạn, hàm này lấy cả bàn isActive = 0
     * để Quản lý có thể thấy và mở lại bàn nếu cần.
     */
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

    /**
     * 2. Thêm bàn mới (Chỉ dành cho Owner)
     */
    public boolean addTable(Table t) {
        String sql = "INSERT INTO `Table` (employeeID, tableName, capacity, QRCodeToken, areaType, isActive) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        // Tự động sinh mã QR Token ngẫu nhiên và duy nhất
        String uniqueQRToken = java.util.UUID.randomUUID().toString();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // employeeID: ID của Owner thực hiện thêm bàn
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
        String sql = "UPDATE `Table` SET tableName = ?, capacity = ?, areaType = ?, isActive = ? " +
                     "WHERE tableID = ?";
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
}