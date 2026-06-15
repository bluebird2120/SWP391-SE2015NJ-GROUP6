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
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("areaType"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tính số bàn còn trống theo từng capacity trong khu vực.
     *
     * Công thức: Còn trống = Tổng bàn (capacity=X, khu=Y) - Bàn bận có
     * tableStatus IN ('reserved','serving','cleaning')
     *
     * Bàn bận gồm 2 loại: 1. Đơn đặt online (tableID IS NULL): đếm theo
     * capacity + areaType trong Order 2. Khách walk-in (tableID IS NOT NULL):
     * JOIN Table lấy capacity + areaType thực tế
     *
     * Trước khi tính, lazy-expire các đơn reserved quá 30 phút.
     */
    public List<Table> findAvailableTableGroups(String areaType, Timestamp orderTime) {

        autoExpireReservations();

        List<Table> resultList = new ArrayList<>();

        String sqlTotal
                = "SELECT capacity, COUNT(*) AS total "
                + "FROM `Table` "
                + "WHERE isActive = 1 AND areaType = ? "
                + "GROUP BY capacity";

        /*
      Đơn online tableID IS NULL:
      Chỉ trừ reserved nếu thời gian khách đang chọn bị trùng trong khoảng giữ bàn 30 phút.
      Ví dụ đã có đơn 16/06 19:00:
      - chọn 16/06 19:15 => bị trừ
      - chọn 16/06 19:30 => không bị trừ nữa
      - chọn 17/06 19:00 => không bị trừ
         */
        String sqlBusyOnline
                = "SELECT capacity, COUNT(*) AS busy "
                + "FROM `Order` "
                + "WHERE tableID IS NULL "
                + "  AND areaType = ? "
                + "  AND orderStatus = 'reserved' "
                + "  AND tableStatus = 'reserved' "
                + "  AND orderTime < DATE_ADD(?, INTERVAL 30 MINUTE) "
                + "  AND DATE_ADD(orderTime, INTERVAL 30 MINUTE) > ? "
                + "GROUP BY capacity";

        /*
      Bàn khách đang ăn hoặc đang dọn.
      Loại này có tableID thật rồi, nên cứ serving/cleaning là bận.
         */
        String sqlBusyCurrentTable
                = "SELECT t.capacity, COUNT(DISTINCT t.tableID) AS busy "
                + "FROM `Order` o "
                + "JOIN `Table` t ON o.tableID = t.tableID "
                + "WHERE t.isActive = 1 "
                + "  AND t.areaType = ? "
                + "  AND o.tableStatus IN ('serving', 'cleaning') "
                + "GROUP BY t.capacity";

        Map<Integer, Integer> totalMap = new HashMap<>();
        Map<Integer, Integer> busyOnlineMap = new HashMap<>();
        Map<Integer, Integer> busyCurrentMap = new HashMap<>();

        try {
            try (PreparedStatement ps = connection.prepareStatement(sqlTotal)) {
                ps.setString(1, areaType);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        totalMap.put(rs.getInt("capacity"), rs.getInt("total"));
                    }
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(sqlBusyOnline)) {
                ps.setString(1, areaType);
                ps.setTimestamp(2, orderTime);
                ps.setTimestamp(3, orderTime);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        busyOnlineMap.put(rs.getInt("capacity"), rs.getInt("busy"));
                    }
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(sqlBusyCurrentTable)) {
                ps.setString(1, areaType);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        busyCurrentMap.put(rs.getInt("capacity"), rs.getInt("busy"));
                    }
                }
            }

            for (Map.Entry<Integer, Integer> entry : totalMap.entrySet()) {
                int cap = entry.getKey();
                int total = entry.getValue();

                int busyOnline = busyOnlineMap.getOrDefault(cap, 0);
                int busyCurrent = busyCurrentMap.getOrDefault(cap, 0);

                int availableCount = Math.max(0, total - busyOnline - busyCurrent);

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

    /**
     * Lazy expire: hủy các đơn reserved quá 30 phút mà khách chưa đến.
     */
    private void autoExpireReservations() {
        String sql
                = "UPDATE `Order` "
                + "SET orderStatus = 'cancelled', tableStatus = 'available' "
                + "WHERE orderType = 1 "
                + "  AND orderStatus = 'reserved' "
                + "  AND tableStatus = 'reserved' "
                + "  AND orderTime < DATE_SUB(NOW(), INTERVAL 30 MINUTE)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int expired = ps.executeUpdate();
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
        String sql = "SELECT * FROM `Table` WHERE isActive = 1 ORDER BY areaType, capacity";
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
        t.setCurrentStaffID(rs.getInt("currentStaffID"));
        t.setTableName(rs.getString("tableName"));
        t.setCapacity(rs.getInt("capacity"));
        t.setQRCodeToken(rs.getString("QRCodeToken"));
        t.setAreaType(rs.getString("areaType"));
        t.setIsActive(rs.getInt("isActive"));
        return t;
    }
}
