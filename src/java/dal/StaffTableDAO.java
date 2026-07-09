package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import dal.NotificationDAO;
import model.Notifications;
import model.StaffTableDTO;

public class StaffTableDAO extends DBContext {

    public List<StaffTableDTO> getPhysicalTables() {
        List<StaffTableDTO> tables = new ArrayList<>();
        // [TABLE STATUS STANDARD] Dung 'serving' cho ban dang phuc vu.
        String sql = "SELECT t.tableID, t.tableName, t.capacity, t.areaType, "
                + "o.orderID, o.orderStatus, o.tableStatus, o.orderTime, "
                + "CASE "
                + "WHEN o.tableStatus = 'pending' THEN 'pending' "
                + "WHEN o.tableStatus = 'cleaning' THEN 'cleaning' "
                + "WHEN o.tableStatus = 'serving' THEN 'serving' "
                + "WHEN o.tableStatus = 'reserved' THEN 'reserved' "
                + "ELSE 'available' END AS physicalStatus "
                + "FROM `Table` t "
                + "LEFT JOIN Order_Table ot ON ot.tableID = t.tableID "
                + " AND EXISTS (SELECT 1 FROM `Order` active_o "
                + " WHERE active_o.orderID = ot.orderID "
                + " AND active_o.orderStatus <> 'cancelled' "
                + " AND active_o.tableStatus IN ('reserved','serving','cleaning','pending')) "
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

    /**
     * [PHAN QUYEN PHUC VU] Chi lay ban/don duoc giao cho nhan vien dang
     * dang nhap. Le tan van dung getPhysicalTables() de xem toan bo nha hang.
     */
    public List<StaffTableDTO> getTablesForEmployee(int employeeID) {
        List<StaffTableDTO> tables = new ArrayList<>();
        String sql = "SELECT t.tableID, t.tableName, t.capacity, t.areaType, "
                + "o.orderID, o.orderStatus, o.tableStatus, o.orderTime, "
                + "CASE "
                + "WHEN o.tableStatus='cleaning' THEN 'cleaning' "
                + "WHEN o.tableStatus='serving' THEN 'serving' "
                + "WHEN o.tableStatus='reserved' THEN 'reserved' "
                + "WHEN o.tableStatus='pending' THEN 'pending' "
                + "ELSE 'available' END physicalStatus "
                + "FROM `Order` o "
                + "JOIN Order_Table ot ON ot.orderID=o.orderID "
                + "JOIN `Table` t ON t.tableID=ot.tableID "
                + "WHERE o.employeeID=? AND o.orderStatus<>'cancelled' "
                + "AND o.tableStatus IN ('reserved','serving','cleaning','pending') "
                + "ORDER BY o.orderTime,t.tableName";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tables.add(mapTable(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public List<StaffTableDTO> getReservationsWaitingForTables() {
        List<StaffTableDTO> rows = new ArrayList<>();
        String sql = "SELECT o.orderID, o.orderStatus, o.tableStatus, o.orderTime, "
                + "e.fullName AS servingEmployeeName, "
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
                // [PHAN QUYEN LE TAN] Hien thi nhan vien phuc vu da duoc he thong gan cho don.
                + "LEFT JOIN Employee e ON e.employeeID = o.employeeID "
                + "WHERE o.orderType = 1 "
                + "AND o.orderStatus IN ('reserved','serving') "
                + "AND o.tableStatus IN ('reserved','serving') "
                + "AND DATE(o.orderTime)=CURRENT_DATE "
                + "GROUP BY o.orderID,o.orderStatus,o.tableStatus,o.orderTime,"
                + "e.fullName,"
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
                row.setServingEmployeeName(rs.getString("servingEmployeeName"));
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
            
            // 🌟 ĐÃ SỬA: Đếm cả bàn pending và serving vào mục "Đang dùng"
            if ("serving".equals(table.getPhysicalStatus()) || "pending".equals(table.getPhysicalStatus())) {
                counts[1]++;
            } else if ("cleaning".equals(table.getPhysicalStatus())) {
                counts[3]++;
            }
        }

        // So ban dat truoc tinh theo nhu cau cua don hom nay,
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

    public String assignTable(int orderID, int tableID) {
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

            // [PHAN QUYEN LE TAN] Le tan chi gan ban. Nhan vien phuc vu
            // duoc he thong chon tu dong theo ca va so don dang phuc vu.
            // Neu don da co phuc vu (vi du don nhieu ban), giu nguyen nguoi do.
            Integer servingEmployeeID = findAssignedServingEmployee(conn, orderID);
            if (servingEmployeeID == null) {
                servingEmployeeID = findLeastLoadedServingEmployee(conn);
            }
            // [DU PHONG GAN PHUC VU] Neu hien tai ngoai gio ca,
            // chi chon nhan vien role 2 dang hoat dong VA co lich lam viec hom nay.
            if (servingEmployeeID == null) {
                servingEmployeeID = findLeastLoadedActiveServingEmployee(conn);
            }
            if (servingEmployeeID == null) {
                conn.rollback();
                return "Khong co nhan vien phuc vu co lich lam viec hom nay de nhan don.";
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Order_Table(orderID,tableID) VALUES(?,?)")) {
                ps.setInt(1, orderID);
                ps.setInt(2, tableID);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE `Order` SET employeeID=?, isStaffConfirmed=1 "
                    + "WHERE orderID=?")) {
                ps.setInt(1, servingEmployeeID);
                ps.setInt(2, orderID);
                ps.executeUpdate();
            }

            boolean allDone = hasAllRequiredTables(conn, orderID);
            if (allDone) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE `Order` SET orderStatus='serving', "
                        + "tableStatus='serving' WHERE orderID=?")) {
                    ps.setInt(1, orderID);
                    ps.executeUpdate();
                }
            }
            conn.commit();

            //Notification
            // ── [THÔNG BÁO NHÂN VIÊN PHỤC VỤ] Sau commit, gửi thông báo
            //    cho nhân viên vừa được gán phục vụ đơn này.
            //    Chỉ gửi khi tất cả bàn đã đủ (allDone) để tránh spam
            //    nhiều thông báo khi đơn cần nhiều bàn.
            if (allDone) {
                try {
                    // Lấy tableName vừa gán để hiển thị trong thông báo
                    String tableNameSql
                            = "SELECT tableName FROM `Table` WHERE tableID=?";
                    String tableName = "?";
                    try (PreparedStatement ps
                            = conn.prepareStatement(tableNameSql)) {
                        ps.setInt(1, tableID);
                        try (ResultSet rs = ps.executeQuery()) {
                            //Nếu ko thấy tên bàn thì để "Bạn được phân công phục vụ bàn ? (Đơn #99)"
                            if (rs.next()) tableName = rs.getString("tableName");
                        }
                    }
                    Notifications n = new Notifications();
                    n.setRecipientID(servingEmployeeID);
                    n.setRecipientType("staff");
                    n.setType("table_assigned");
                    n.setMessage("Bạn được phân công phục vụ bàn "
                            + tableName + " (Đơn #" + orderID + ").");
                    n.setIsRead(0);
                    new NotificationDAO().insert(n);
                } catch (Exception ignored) {
                    // Thông báo thất bại không rollback transaction chính
                }
            }
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
     * [PHAN QUYEN PHUC VU] Nhan vien chi duoc xac nhan don cua chinh minh.
     */
    public boolean markCleaningCompleted(int orderID, int employeeID) {
        String sql = "UPDATE `Order` SET tableStatus='available' "
                + "WHERE orderID=? AND employeeID=? "
                + "AND orderStatus='completed' AND tableStatus='cleaning'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ps.setInt(2, employeeID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * [TU DONG GAN PHUC VU] Chi chon role 2 dang hoat dong, dang trong ca,
     * uu tien nguoi co it don chua hoan tat nhat.
     */
    private Integer findLeastLoadedServingEmployee(Connection conn)
            throws SQLException {
        String sql = "SELECT es.employeeID,COUNT(o.orderID) active_orders "
                + "FROM EmployeeShifts es "
                + "JOIN ShiftTemplates st ON st.templateID=es.templateID "
                + "JOIN Employee e ON e.employeeID=es.employeeID "
                + "LEFT JOIN `Order` o ON o.employeeID=e.employeeID "
                + "AND o.orderStatus NOT IN ('completed','cancelled') "
                + "WHERE es.workDate=CURDATE() AND e.roleID=2 AND e.isActive=1 "
                + "AND es.checkOutTime IS NULL "
                + "AND es.status IN ('scheduled','present','late') "
                + "AND ((st.startTime<=st.endTime "
                + "AND CURRENT_TIME() BETWEEN st.startTime AND st.endTime) "
                + "OR (st.startTime>st.endTime "
                + "AND (CURRENT_TIME()>=st.startTime OR CURRENT_TIME()<=st.endTime))) "
                + "GROUP BY es.employeeID "
                + "ORDER BY active_orders ASC,es.employeeID ASC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("employeeID") : null;
        }
    }

    /**
     * [TU DONG GAN PHUC VU] Don nhieu ban van chi thuoc mot nhan vien.
     */
    private Integer findAssignedServingEmployee(Connection conn, int orderID)
            throws SQLException {
        String sql = "SELECT o.employeeID FROM `Order` o "
                + "JOIN Employee e ON e.employeeID=o.employeeID "
                + "WHERE o.orderID=? AND e.roleID=2 "
                // [TU DONG GAN PHUC VU] Don da co nhan vien thi cung phai la nguoi co lich hom nay.
                + "AND EXISTS (SELECT 1 FROM EmployeeShifts es "
                + "WHERE es.employeeID=e.employeeID "
                + "AND es.workDate=CURDATE() "
                + "AND es.status IN ('scheduled','present','late') "
                + "AND es.checkOutTime IS NULL) "
                + "LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("employeeID") : null;
            }
        }
    }

    /**
     * [DU PHONG GAN PHUC VU] Dung khi ngoai khung gio ca nhung van can chon
     * nhan vien co lich lam viec trong ngay. Khong chon nguoi khong co lich.
     */
    private Integer findLeastLoadedActiveServingEmployee(Connection conn)
            throws SQLException {
        String sql = "SELECT e.employeeID,COUNT(o.orderID) active_orders "
                + "FROM Employee e "
                + "LEFT JOIN `Order` o ON o.employeeID=e.employeeID "
                + "AND o.orderStatus NOT IN ('completed','cancelled') "
                + "WHERE e.roleID=2 AND e.isActive=1 "
                + "AND EXISTS (SELECT 1 FROM EmployeeShifts es "
                + "WHERE es.employeeID=e.employeeID "
                + "AND es.workDate=CURDATE() "
                + "AND es.status IN ('scheduled','present','late') "
                + "AND es.checkOutTime IS NULL) "
                + "GROUP BY e.employeeID "
                + "ORDER BY active_orders ASC,e.employeeID ASC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("employeeID") : null;
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
        // [TABLE STATUS STANDARD] Ban reserved/serving/cleaning/pending deu duoc xem la ban.
        String sql = "SELECT 1 FROM Order_Table ot "
                + "JOIN `Order` o ON o.orderID=ot.orderID "
                + "WHERE ot.tableID=? AND o.orderStatus<>'cancelled' "
                + "AND o.tableStatus IN ('reserved','serving','cleaning','pending') "
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
        }
    }

    private void restoreAutoCommit(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }
}