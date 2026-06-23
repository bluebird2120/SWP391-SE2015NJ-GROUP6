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

    public List<Table> findAvailableTableGroups(String areaType, Timestamp orderTime) {
        autoExpireReservations();

        List<Table> resultList = new ArrayList<>();
        String sqlTotal = "SELECT capacity, COUNT(*) AS total FROM `Table` WHERE isActive = 1 AND areaType = ? GROUP BY capacity";

        String sqlBusyOnline
                = "SELECT ord.capacity, SUM(ord.quantity) AS busy FROM `Order` o "
                + "JOIN order_reservation_detail ord ON ord.orderID = o.orderID "
                + "WHERE o.orderType = 1 AND ord.areaType = ? AND DATE(o.orderTime) = DATE(?) "
                + "  AND o.tableStatus IN ('reserved', 'serving', 'cleaning') "
                + "  AND (o.orderStatus IS NULL OR o.orderStatus NOT IN ('cancelled', 'completed')) "
                + "  AND NOT EXISTS ( SELECT 1 FROM Order_Table ot WHERE ot.orderID = o.orderID ) "
                + "GROUP BY ord.capacity";

        String sqlBusyAssigned
                = "SELECT t.capacity, COUNT(DISTINCT t.tableID) AS busy FROM `Order` o "
                + "JOIN Order_Table ot ON o.orderID = ot.orderID "
                + "JOIN `Table` t ON ot.tableID = t.tableID "
                + "WHERE t.isActive = 1 AND t.areaType = ? AND DATE(o.orderTime) = DATE(?) "
                + "  AND o.tableStatus IN ('reserved', 'serving', 'cleaning') "
                + "  AND (o.orderStatus IS NULL OR o.orderStatus NOT IN ('cancelled', 'completed')) "
                + "GROUP BY t.capacity";

        Map<Integer, Integer> totalMap = new HashMap<>();
        Map<Integer, Integer> busyOnlineMap = new HashMap<>();
        Map<Integer, Integer> busyAssignedMap = new HashMap<>();

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
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        busyOnlineMap.put(rs.getInt("capacity"), rs.getInt("busy"));
                    }
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(sqlBusyAssigned)) {
                ps.setString(1, areaType);
                ps.setTimestamp(2, orderTime);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        busyAssignedMap.put(rs.getInt("capacity"), rs.getInt("busy"));
                    }
                }
            }

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

    private void autoExpireReservations() {
        String sql
                = "UPDATE `Order` SET orderStatus = 'cancelled', tableStatus = 'available' "
                + "WHERE orderType = 1 AND orderStatus = 'reserved' AND tableStatus = 'reserved' "
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

    // =========================================================
    // HÀM MAP DỮ LIỆU DÙNG CHUNG CHO TOÀN BỘ DAO
    // =========================================================
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

    public boolean addTable(Table t) {
        String sql = "INSERT INTO `Table` (employeeID, tableName, capacity, QRCodeToken, areaType, isActive) VALUES (?, ?, ?, ?, ?, ?)";
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

    public boolean updateTable(Table t) {
        String sql = "UPDATE `Table` SET tableName = ?, capacity = ?, areaType = ?, isActive = ? WHERE tableID = ?";
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
    
    // 1. Đếm tổng số bàn theo bộ lọc (Dùng trực tiếp connection kế thừa, KHÔNG TỰ ĐÓNG)
    public int countSearchTables(String searchName, Integer searchCapacity, String searchArea, Integer searchStatus) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM `Table` WHERE 1=1 ");
        
        if (searchName != null && !searchName.isEmpty()) {
            sql.append(" AND tableName LIKE ? ");
        }
        if (searchCapacity != null) {
            sql.append(" AND capacity = ? ");
        }
        if (searchArea != null && !searchArea.isEmpty()) {
            sql.append(" AND areaType = ? ");
        }
        if (searchStatus != null) {
            sql.append(" AND isActive = ? ");
        }
        
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
             
            int paramIndex = 1;
            if (searchName != null && !searchName.isEmpty()) {
                ps.setString(paramIndex++, "%" + searchName + "%");
            }
            if (searchCapacity != null) {
                ps.setInt(paramIndex++, searchCapacity);
            }
            if (searchArea != null && !searchArea.isEmpty()) {
                ps.setString(paramIndex++, searchArea);
            }
            if (searchStatus != null) {
                ps.setInt(paramIndex++, searchStatus);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("[TableDAO] countSearchTables lỗi: " + e.getMessage());
        }
        return 0;
    }

    // 2. Lấy danh sách bàn theo bộ lọc VÀ phân trang (Dùng trực tiếp connection kế thừa, KHÔNG TỰ ĐÓNG)
    public List<Table> searchTablesPaging(String searchName, Integer searchCapacity, String searchArea, Integer searchStatus, int offset, int pageSize) {
        List<Table> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM `Table` WHERE 1=1 ");
        
        if (searchName != null && !searchName.isEmpty()) {
            sql.append(" AND tableName LIKE ? ");
        }
        if (searchCapacity != null) {
            sql.append(" AND capacity = ? ");
        }
        if (searchArea != null && !searchArea.isEmpty()) {
            sql.append(" AND areaType = ? ");
        }
        if (searchStatus != null) {
            sql.append(" AND isActive = ? ");
        }
        
        sql.append(" ORDER BY tableID DESC LIMIT ? OFFSET ?");
        
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
             
            int paramIndex = 1;
            if (searchName != null && !searchName.isEmpty()) {
                ps.setString(paramIndex++, "%" + searchName + "%");
            }
            if (searchCapacity != null) {
                ps.setInt(paramIndex++, searchCapacity);
            }
            if (searchArea != null && !searchArea.isEmpty()) {
                ps.setString(paramIndex++, searchArea);
            }
            if (searchStatus != null) {
                ps.setInt(paramIndex++, searchStatus);
            }
            
            ps.setInt(paramIndex++, pageSize);
            ps.setInt(paramIndex++, offset);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs)); // Gọi hàm mapRow an toàn có sẵn của bạn
                }
            }
        } catch (Exception e) {
            System.err.println("[TableDAO] searchTablesPaging lỗi: " + e.getMessage());
        }
        return list;
    }
}