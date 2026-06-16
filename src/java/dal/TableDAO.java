package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Table;

public class TableDAO extends DBContext {

    public List<String> getAllAreaTypes() {
        List<String> list = new ArrayList<>();

        String sql = "SELECT DISTINCT areaType "
                   + "FROM `Table` "
                   + "WHERE isActive = 1 "
                   + "ORDER BY areaType";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("areaType"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Table> findAvailableTableGroups(String areaType, Timestamp orderTime) {

        List<Table> resultList = new ArrayList<>();

        String sqlTotal
                = "SELECT capacity, COUNT(*) AS total "
                + "FROM `Table` "
                + "WHERE isActive = 1 "
                + "  AND areaType = ? "
                + "GROUP BY capacity";

        /*
         * Đơn online đã reserved nhưng chưa gán bàn thật.
         * Chỉ trừ nếu thời gian khách chọn bị trùng trong khoảng giữ bàn 30 phút.
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
         * Bàn thật đang có khách ăn hoặc đang dọn.
         * Chỉ dùng query này nếu khách chọn ngày hôm nay.
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
            /*
             * 1. Lấy tổng số bàn theo capacity.
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
             * 2. Lấy số đơn online reserved trùng giờ.
             */
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

            /*
             * 3. Chỉ nếu chọn hôm nay thì mới trừ bàn đang serving / cleaning.
             * Nếu chọn ngày tương lai thì không trừ serving / cleaning hiện tại.
             */
            LocalDate selectedDate = orderTime.toLocalDateTime().toLocalDate();
            LocalDate today = LocalDate.now();

            if (selectedDate.equals(today)) {
                try (PreparedStatement ps = connection.prepareStatement(sqlBusyCurrentTable)) {
                    ps.setString(1, areaType);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            busyCurrentMap.put(rs.getInt("capacity"), rs.getInt("busy"));
                        }
                    }
                }
            }

            /*
             * 4. Tính số bàn còn trống.
             */
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

                /*
                 * Tạm dùng isActive để chứa số lượng bàn còn trống.
                 * Vì giao diện của bạn đang lấy getIsActive() để hiển thị số bàn.
                 */
                dto.setIsActive(availableCount);

                resultList.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        resultList.sort((a, b) -> Integer.compare(a.getCapacity(), b.getCapacity()));
        return resultList;
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

        String sql = "SELECT * "
                   + "FROM `Table` "
                   + "WHERE isActive = 1 "
                   + "ORDER BY areaType, capacity";

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