package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.StaffTableDTO;

/**
 * 
 */
public class StaffTableDAO extends DBContext {

    public List<StaffTableDTO> getPhysicalTables() {
        List<StaffTableDTO> tables = new ArrayList<>();
        String sql = "SELECT t.tableID, t.tableName, t.capacity, t.areaType, "
                + "o.orderID, o.orderStatus, o.tableStatus, o.orderTime, "
                + "CASE WHEN o.tableStatus = 'cleaning' THEN 'cleaning' "
                + "WHEN o.tableStatus = 'serving' THEN 'serving' "
                + "WHEN o.tableStatus = 'reserved' THEN 'reserved' "
                + "ELSE 'available' END AS physicalStatus "
                + "FROM `Table` t "
                + "LEFT JOIN Order_Table ot ON ot.tableID = t.tableID "
                + " AND EXISTS (SELECT 1 FROM `Order` active_o "
                + " WHERE active_o.orderID = ot.orderID "
                + " AND active_o.orderStatus <> 'cancelled' "
                + " AND active_o.tableStatus IN ('reserved','serving','cleaning')) "
                + "LEFT JOIN `Order` o ON o.orderID = ot.orderID "
                + "WHERE t.isActive = 1 "
                + "ORDER BY t.areaType, t.capacity, t.tableName";
        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tables.add(mapTable(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public List<StaffTableDTO> getReservationsWaitingForTables() {
        List<StaffTableDTO> rows = new ArrayList<>();
        String sql = "SELECT o.orderID, o.orderStatus, o.tableStatus, o.orderTime, "
                + "d.capacity, d.areaType, d.quantity AS requiredQuantity, "
                + "COUNT(DISTINCT CASE WHEN t.capacity = d.capacity "
                + "AND t.areaType = d.areaType THEN t.tableID END) assignedQuantity, "
                + "GROUP_CONCAT(DISTINCT CASE WHEN t.capacity = d.capacity "
                + "AND t.areaType = d.areaType THEN t.tableName END "
                + "ORDER BY t.tableName SEPARATOR ', ') assignedTableNames "
                + "FROM `Order` o "
                + "JOIN order_reservation_detail d ON d.orderID = o.orderID "
                + "LEFT JOIN Order_Table ot ON ot.orderID = o.orderID "
                + "LEFT JOIN `Table` t ON t.tableID = ot.tableID "
                + "WHERE o.orderType = 1 "
                + "AND o.orderStatus IN ('reserved','serving') "
                + "AND o.tableStatus IN ('reserved','serving') "
                + "AND DATE(o.orderTime)=CURRENT_DATE "
                + "GROUP BY o.orderID,o.orderStatus,o.tableStatus,o.orderTime,"
                + "d.capacity,d.areaType,d.quantity "
                + "ORDER BY o.orderTime,o.orderID,d.areaType,d.capacity";
        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StaffTableDTO row = new StaffTableDTO();
                row.setOrderID(rs.getInt("orderID"));
                row.setOrderStatus(rs.getString("orderStatus"));
                row.setTableStatus(rs.getString("tableStatus"));
                row.setOrderTime(rs.getTimestamp("orderTime"));
                row.setCapacity(rs.getInt("capacity"));
                row.setAreaType(rs.getString("areaType"));
                row.setRequiredQuantity(rs.getInt("requiredQuantity"));
                row.setAssignedQuantity(rs.getInt("assignedQuantity"));
                row.setAssignedTableNames(rs.getString("assignedTableNames"));
                rows.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    /**
     * Gia tri int[]: [tong ban, dang su dung, da gan truoc, cho don].
     */
    public Map<String, int[]> getSummaryByTableType() {
        Map<String, int[]> summary = new LinkedHashMap<>();
        for (StaffTableDTO table : getPhysicalTables()) {
            String key = table.getAreaType() + " - "
                    + table.getCapacity() + " chỗ";
            int[] counts = summary.computeIfAbsent(key, ignored -> new int[4]);
            counts[0]++;
            if ("serving".equals(table.getPhysicalStatus())) {
                counts[1]++;
            } else if ("cleaning".equals(table.getPhysicalStatus())) {
                counts[3]++;
            }
        }

        //  So ban dat truoc tinh theo nhu cau cua don hom nay,
        // ke ca khi nhan vien chua gan tableID vat ly.
        String sql = "SELECT d.areaType,d.capacity,SUM(d.quantity) reservedCount "
                + "FROM `Order` o "
                + "JOIN order_reservation_detail d ON d.orderID=o.orderID "
                + "WHERE o.orderType=1 AND o.orderStatus='reserved' "
                + "AND o.tableStatus='reserved' "
                + "AND DATE(o.orderTime)=CURRENT_DATE "
                + "GROUP BY d.areaType,d.capacity";
        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String key = rs.getString("areaType") + " - "
                        + rs.getInt("capacity") + " chỗ";
                int[] counts = summary.computeIfAbsent(
                        key, ignored -> new int[4]);
                counts[2] = rs.getInt("reservedCount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }

    /**
     * Gan tung ban phu hop. Chi chuyen serving khi da gan du tat ca ban.
     *
     * @return null neu thanh cong; noi dung loi neu that bai.
     */
    public String assignTable(int orderID, int tableID, int employeeID) {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            if (!hasMatchingUnfilledRequirement(conn, orderID, tableID)) {
                conn.rollback();
                return "Bàn không phù hợp, đơn đã đủ bàn hoặc không còn đặt trước.";
            }
            if (isTableBusy(conn, tableID)) {
                conn.rollback();
                return "Bàn này đang được sử dụng hoặc đang chờ dọn.";
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Order_Table(orderID,tableID) VALUES(?,?)")) {
                ps.setInt(1, orderID);
                ps.setInt(2, tableID);
                ps.executeUpdate();
            }

            // [STAFF TABLE] Ghi nhan nhan vien da xep ban.
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE `Order` SET employeeID=?, isStaffConfirmed=1 "
                    + "WHERE orderID=?")) {
                ps.setInt(1, employeeID);
                ps.setInt(2, orderID);
                ps.executeUpdate();
            }

            // [STAFF TABLE] Don chi serving khi da gan du so luong yeu cau.
            if (hasAllRequiredTables(conn, orderID)) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE `Order` SET orderStatus='serving', "
                        + "tableStatus='serving' WHERE orderID=?")) {
                    ps.setInt(1, orderID);
                    ps.executeUpdate();
                }
            }
            conn.commit();
            return null;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            return "Không thể gán bàn do lỗi dữ liệu.";
        } finally {
            restoreAutoCommit(conn);
        }
    }

    /**
     * completed/cleaning -> completed/available.
     * Trang thai o cap Order nen se giai phong moi ban cua don.
     */
    public boolean markCleaningCompleted(int orderID) {
        // [CLEANING FLOW]
        // Don van completed de tra cuu lich su; chi giai phong ban ve available.
        String sql = "UPDATE `Order` SET tableStatus='available' "
                + "WHERE orderID=? AND orderStatus='completed' "
                + "AND tableStatus='cleaning'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hasMatchingUnfilledRequirement(
            Connection conn, int orderID, int tableID) throws SQLException {
        String sql = "SELECT 1 FROM `Order` o "
                + "JOIN order_reservation_detail d ON d.orderID=o.orderID "
                + "JOIN `Table` selected ON selected.tableID=? "
                + "WHERE o.orderID=? AND o.orderType=1 "
                + "AND o.orderStatus='reserved' AND o.tableStatus='reserved' "
                + "AND DATE(o.orderTime)=CURRENT_DATE "
                + "AND selected.isActive=1 "
                + "AND selected.capacity=d.capacity "
                + "AND selected.areaType=d.areaType "
                + "AND (SELECT COUNT(DISTINCT assigned.tableID) "
                + " FROM Order_Table ot JOIN `Table` assigned "
                + " ON assigned.tableID=ot.tableID "
                + " WHERE ot.orderID=o.orderID "
                + " AND assigned.capacity=d.capacity "
                + " AND assigned.areaType=d.areaType) < d.quantity "
                + "LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableID);
            ps.setInt(2, orderID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isTableBusy(Connection conn, int tableID)
            throws SQLException {
        String sql = "SELECT 1 FROM Order_Table ot "
                + "JOIN `Order` o ON o.orderID=ot.orderID "
                + "WHERE ot.tableID=? AND o.orderStatus<>'cancelled' "
                + "AND o.tableStatus IN ('reserved','serving','cleaning') "
                + "LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean hasAllRequiredTables(Connection conn, int orderID)
            throws SQLException {
        String sql = "SELECT "
                + "(SELECT COALESCE(SUM(quantity),0) "
                + " FROM order_reservation_detail WHERE orderID=?) requiredCount,"
                + "(SELECT COUNT(DISTINCT tableID) "
                + " FROM Order_Table WHERE orderID=?) assignedCount";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ps.setInt(2, orderID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("requiredCount") > 0
                        && rs.getInt("assignedCount")
                        >= rs.getInt("requiredCount");
            }
        }
    }

    private StaffTableDTO mapTable(ResultSet rs) throws SQLException {
        StaffTableDTO row = new StaffTableDTO();
        row.setTableID(rs.getInt("tableID"));
        row.setTableName(rs.getString("tableName"));
        row.setCapacity(rs.getInt("capacity"));
        row.setAreaType(rs.getString("areaType"));
        row.setPhysicalStatus(rs.getString("physicalStatus"));
        row.setOrderID((Integer) rs.getObject("orderID"));
        row.setOrderStatus(rs.getString("orderStatus"));
        row.setTableStatus(rs.getString("tableStatus"));
        row.setOrderTime(rs.getTimestamp("orderTime"));
        return row;
    }

    private void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // Khong che mat loi goc.
        }
    }

    private void restoreAutoCommit(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignored) {
            // Connection duoc DBContext quan ly.
        }
    }
}
