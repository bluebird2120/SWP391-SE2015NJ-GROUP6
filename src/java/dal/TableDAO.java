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
     * Lấy danh sách khu vực có sẵn để hiển thị cho khách chọn.
     */
    public List<String> getAllAreaTypes() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT areaType FROM `Table` WHERE isActive = 1 ORDER BY areaType";
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

    /**
     * THUẬT TOÁN SỨC CHỨA: Tính toán số lượng bàn còn trống theo từng quy mô nhóm bàn (Capacity)
     * Công thức: Số bàn trống = Tổng số bàn chủ đã add - Số bàn đang bị chiếm dụng
     */
    public List<Table> findAvailableTableGroups(String areaType, Timestamp orderTime) {
        List<Table> resultList = new ArrayList<>();
        long TWO_HOURS = 2L * 60 * 60 * 1000;
        Timestamp windowStart = new Timestamp(orderTime.getTime() - TWO_HOURS);
        Timestamp windowEnd   = new Timestamp(orderTime.getTime() + TWO_HOURS);

        // 1. Lấy tổng số bàn chủ nhà hàng đã add cho từng loại sức chứa
        String sqlTotal = "SELECT capacity, COUNT(*) as total FROM `Table` "
                        + "WHERE isActive = 1 AND areaType = ? GROUP BY capacity";
        
        // 2. Lấy số bàn đang bận (Đặt trước trùng lịch OR Khách vãng lai đang ăn / đang dọn)
        String sqlBusy = "SELECT t.capacity, COUNT(DISTINCT o.tableID) as busy "
                       + "FROM `Order` o JOIN `Table` t ON o.tableID = t.tableID "
                       + "WHERE t.isActive = 1 AND t.areaType = ? AND o.orderStatus NOT IN ('cancelled') "
                       + "  AND ( "
                       + "       (o.orderType = 1 AND o.orderTime BETWEEN ? AND ?) "
                       + "       OR (o.tableStatus IN ('serving', 'cleaning')) "
                       + "  ) "
                       + "GROUP BY t.capacity";

        Map<Integer, Integer> totalMap = new HashMap<>();
        Map<Integer, Integer> busyMap = new HashMap<>();

        try {
            // Đếm số lượng bàn gốc trong hệ thống
            try (PreparedStatement ps = connection.prepareStatement(sqlTotal)) {
                ps.setString(1, areaType);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        totalMap.put(rs.getInt("capacity"), rs.getInt("total"));
                    }
                }
            }
            
            // Đếm số lượng bàn đang bận thực tế tại khung giờ đó
            try (PreparedStatement ps = connection.prepareStatement(sqlBusy)) {
                ps.setString(1, areaType);
                ps.setTimestamp(2, windowStart);
                ps.setTimestamp(3, windowEnd);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        busyMap.put(rs.getInt("capacity"), rs.getInt("busy"));
                    }
                }
            }

            // 3. Thực hiện phép toán trừ tính lượng bàn trống khả dụng còn lại
            for (Map.Entry<Integer, Integer> entry : totalMap.entrySet()) {
                int capacity = entry.getKey();
                int total = entry.getValue();
                int busy = busyMap.getOrDefault(capacity, 0);
                int availableCount = total - busy;

                // Tạo đối tượng DTO đại diện cho nhóm bàn để truyền tải dữ liệu sang JSP hiển thị
                Table groupDto = new Table();
                groupDto.setCapacity(capacity);
                groupDto.setAreaType(areaType);
                groupDto.setTableName("Bàn " + capacity + " chỗ");
                groupDto.setTableID(capacity); // Dùng sức chứa làm ID định danh cho form chọn
                groupDto.setIsActive(availableCount); // Mượn tạm trường isActive để lưu trữ số bàn trống thực tế

                resultList.add(groupDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public Table getTableByID(int tableID) {
        Table t = new Table();
        t.setTableID(tableID);
        t.setCapacity(tableID);
        t.setTableName("Bàn loại " + tableID + " chỗ");
        return t;
    }

    public List<Table> getAllActiveTables() {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM `Table` WHERE isActive = 1 ORDER BY areaType, capacity";
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

    public List<Table> findAlternativeTablesHigherCapacity(String areaType, Timestamp orderTime, int failedCapacity) {
        return new ArrayList<>();
    }
    public List<Table> findAlternativeTablesOtherArea(String failedAreaType, Timestamp orderTime) {
        return new ArrayList<>();
    }
}