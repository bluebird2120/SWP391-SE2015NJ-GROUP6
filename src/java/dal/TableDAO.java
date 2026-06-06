package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
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
     * Tìm tất cả bàn còn trống theo khu vực + khung giờ (loại trừ bàn có đơn trong vòng 2 tiếng).
     */
    public List<Table> findAvailableTables(String areaType, Timestamp orderTime) {
        List<Table> list = new ArrayList<>();
        long TWO_HOURS = 2L * 60 * 60 * 1000;
        Timestamp windowStart = new Timestamp(orderTime.getTime() - TWO_HOURS);
        Timestamp windowEnd   = new Timestamp(orderTime.getTime() + TWO_HOURS);

        String sql = "SELECT t.tableID, t.employeeID, t.currentStaffID, "
                   + "       t.tableName, t.capacity, t.QRCodeToken, "
                   + "       t.areaType, t.isActive "
                   + "FROM `Table` t "
                   + "WHERE t.isActive = 1 "
                   + "  AND t.areaType = ? "
                   + "  AND t.tableID NOT IN ( "
                   + "      SELECT o.tableID FROM `Order` o "
                   + "      WHERE o.tableID    IS NOT NULL "
                   + "        AND o.orderType   = 1 "
                   + "        AND o.orderStatus NOT IN ('cancelled') "
                   + "        AND o.orderTime BETWEEN ? AND ? "
                   + "  ) "
                   + "ORDER BY t.capacity ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, areaType);
            ps.setTimestamp(2, windowStart);
            ps.setTimestamp(3, windowEnd);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * GỢI Ý NÂNG CAO 1: Tìm các bàn trống ở CÙNG KHU VỰC nhưng có SỨC CHỨA LỚN HƠN.
     */
    public List<Table> findAlternativeTablesHigherCapacity(String areaType, Timestamp orderTime, int failedCapacity) {
        List<Table> list = new ArrayList<>();
        long TWO_HOURS = 2L * 60 * 60 * 1000;
        Timestamp windowStart = new Timestamp(orderTime.getTime() - TWO_HOURS);
        Timestamp windowEnd   = new Timestamp(orderTime.getTime() + TWO_HOURS);

        String sql = "SELECT * FROM `Table` t "
                   + "WHERE t.isActive = 1 "
                   + "  AND t.areaType = ? "
                   + "  AND t.capacity > ? "
                   + "  AND t.tableID NOT IN ( "
                   + "      SELECT o.tableID FROM `Order` o "
                   + "      WHERE o.tableID IS NOT NULL AND o.orderType = 1 "
                   + "        AND o.orderStatus NOT IN ('cancelled') "
                   + "        AND o.orderTime BETWEEN ? AND ? "
                   + "  ) "
                   + "ORDER BY t.capacity ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, areaType);
            ps.setInt(2, failedCapacity);
            ps.setTimestamp(3, windowStart);
            ps.setTimestamp(4, windowEnd);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * GỢI Ý NÂNG CAO 2: Tìm các bàn trống ở KHU VỰC KHÁC.
     */
    public List<Table> findAlternativeTablesOtherArea(String failedAreaType, Timestamp orderTime) {
        List<Table> list = new ArrayList<>();
        long TWO_HOURS = 2L * 60 * 60 * 1000;
        Timestamp windowStart = new Timestamp(orderTime.getTime() - TWO_HOURS);
        Timestamp windowEnd   = new Timestamp(orderTime.getTime() + TWO_HOURS);

        String sql = "SELECT * FROM `Table` t "
                   + "WHERE t.isActive = 1 "
                   + "  AND t.areaType != ? "
                   + "  AND t.tableID NOT IN ( "
                   + "      SELECT o.tableID FROM `Order` o "
                   + "      WHERE o.tableID IS NOT NULL AND o.orderType = 1 "
                   + "        AND o.orderStatus NOT IN ('cancelled') "
                   + "        AND o.orderTime BETWEEN ? AND ? "
                   + "  ) "
                   + "ORDER BY t.areaType, t.capacity ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, failedAreaType);
            ps.setTimestamp(2, windowStart);
            ps.setTimestamp(3, windowEnd);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Table getTableByID(int tableID) {
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