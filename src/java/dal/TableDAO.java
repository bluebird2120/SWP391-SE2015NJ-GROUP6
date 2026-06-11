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

    /**
     * Lấy danh sách khu vực có sẵn.
     */
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
     *             - Bàn bận có tableStatus IN ('reserved', 'serving', 'cleaning')
     *
     * Bàn bận gồm 2 loại:
     *   1. Đơn đặt trước online (tableID IS NULL):
     *      đếm theo Order.capacity + Order.areaType
     *      tableStatus IN ('reserved','serving','cleaning')
     *      Loại trừ đơn reserved quá 30 phút mà orderTime đã qua (hết hạn giữ bàn).
     *
     *   2. Khách walk-in (tableID IS NOT NULL):
     *      đếm theo tableStatus IN ('reserved','serving','cleaning')
     *      JOIN Table để lấy capacity + areaType thực tế.
     *
     * Trước khi tính, tự động hủy các đơn reserved hết hạn (lazy expire).
     */
    public List<Table> findAvailableTableGroups(String areaType, Timestamp orderTime) {

        // Lazy expire: hủy đơn reserved quá 30 phút trước khi tính toán
        autoExpireReservations();

        List<Table> resultList = new ArrayList<>();

        // ── Query 1: Tổng số bàn theo capacity trong khu vực ──
        String sqlTotal =
            "SELECT capacity, COUNT(*) AS total " +
            "FROM `Table` " +
            "WHERE isActive = 1 AND areaType = ? " +
            "GROUP BY capacity";

        // ── Query 2: Bàn bận — đơn online (tableID IS NULL) ──
        // Đếm các đơn có tableStatus bận, trừ đơn reserved đã hết 30 phút giữ bàn.
        // Không giới hạn cửa sổ thời gian vì reserved là trạng thái thực — bàn
        // chỉ trả về available khi hủy hoặc khách đến check-in.
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

        // ── Query 3: Bàn bận — walk-in (tableID IS NOT NULL) ──
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

        // Sắp xếp theo capacity tăng dần
        resultList.sort((a, b) -> Integer.compare(a.getCapacity(), b.getCapacity()));

        return resultList;
    }

    /**
     * Lazy expire: hủy các đơn reserved đã quá 30 phút mà khách chưa đến.
     * Gọi tự động trước mỗi lần tính bàn trống.
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
            if (expired > 0) {
                System.out.println("[TableDAO] Auto-expired " + expired + " reservation(s).");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy thông tin bàn theo tableID — dùng sau khi nhân viên gán bàn thực.
     */
    
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
        t.setCurrentStaffID(rs.getInt("currentStaffID"));
        t.setTableName(rs.getString("tableName"));
        t.setCapacity(rs.getInt("capacity"));
        t.setQRCodeToken(rs.getString("QRCodeToken"));
        t.setAreaType(rs.getString("areaType"));
        t.setIsActive(rs.getInt("isActive"));
        return t;
    }
}