package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.HourStat;

public class PeakHoursDAO extends DBContext {

    /**
     * Lấy số đơn theo từng giờ dựa trên thời gian khách đến (orderTime)
     * filterType: today | week | month | year | custom
     */
    public List<HourStat> getOrderCountByHour(String filterType,
            String startDate, String endDate) throws SQLException {

        String sql = "SELECT HOUR(orderTime) AS h, COUNT(*) AS cnt "
                   + "FROM `Order` "
                   + "WHERE orderStatus NOT IN ('cancelled') "
                   + "AND orderTime IS NOT NULL " // Đảm bảo đơn có giờ hẹn/giờ ăn rõ ràng
                   + buildWhereClause(filterType)
                   + "GROUP BY HOUR(orderTime) "
                   + "ORDER BY h ASC";

        List<HourStat> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            
            if ("custom".equals(filterType)
                    && startDate != null && !startDate.isBlank()
                    && endDate   != null && !endDate.isBlank()) {
                ps.setString(1, startDate);
                ps.setString(2, endDate);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HourStat(rs.getInt("h"), rs.getInt("cnt")));
                }
            }
        }
        return list;
    }

    /**
     * Tổng số đơn trong khoảng thời gian.
     */
    public int getTotalOrders(String filterType,
            String startDate, String endDate) throws SQLException {

        String sql = "SELECT COUNT(*) FROM `Order` "
                   + "WHERE orderStatus NOT IN ('cancelled') "
                   + "AND orderTime IS NOT NULL "
                   + buildWhereClause(filterType);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            
            if ("custom".equals(filterType)
                    && startDate != null && !startDate.isBlank()
                    && endDate   != null && !endDate.isBlank()) {
                ps.setString(1, startDate);
                ps.setString(2, endDate);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Trả về phần WHERE động dựa trên cột orderTime
     */
    private String buildWhereClause(String filterType) {
        if (filterType == null) filterType = "today";
        switch (filterType) {
            case "today":
                // Đã sửa: DATE(createdAt) -> DATE(orderTime)
                return "AND DATE(orderTime) = CURDATE() ";
            case "week":
                // Đã sửa: YEARWEEK(createdAt, 1) -> YEARWEEK(orderTime, 1)
                return "AND YEARWEEK(orderTime, 1) = YEARWEEK(CURDATE(), 1) ";
            case "month":
                // Đã sửa: YEAR/MONTH(createdAt) -> YEAR/MONTH(orderTime)
                return "AND YEAR(orderTime) = YEAR(CURDATE()) "
                     + "AND MONTH(orderTime) = MONTH(CURDATE()) ";
            case "year":
                // Đã sửa: YEAR(createdAt) -> YEAR(orderTime)
                return "AND YEAR(orderTime) = YEAR(CURDATE()) ";
            case "custom":
                // Đã sửa: DATE(createdAt) -> DATE(orderTime)
                return "AND DATE(orderTime) BETWEEN ? AND ? ";
            default:
                return "AND DATE(orderTime) = CURDATE() ";
        }
    }
}