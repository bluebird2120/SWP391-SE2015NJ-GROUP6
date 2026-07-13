package dal;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.BusinessSchedule;

public class BusinessScheduleDAO extends DBContext {

    public static final String[] WEEK_DAYS = {
        "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY",
        "FRIDAY", "SATURDAY", "SUNDAY"
    };

    public Map<String, BusinessSchedule> getWeeklyScheduleMap() {
        Map<String, BusinessSchedule> map = new LinkedHashMap<>();
        for (String day : WEEK_DAYS) {
            BusinessSchedule defaultRow = new BusinessSchedule();
            defaultRow.setDayOfWeek(day);
            defaultRow.setOpenTime(Time.valueOf("08:00:00"));
            defaultRow.setCloseTime(Time.valueOf("22:00:00"));
            defaultRow.setIsClosed(0);
            map.put(day, defaultRow);
        }

        String sql = "SELECT scheduleID, dayOfWeek, specificDate, openTime, closeTime, "
                + "isClosed, reason, updatedAt "
                + "FROM BusinessSchedule "
                + "WHERE specificDate IS NULL AND dayOfWeek IS NOT NULL "
                + "ORDER BY FIELD(dayOfWeek,'MONDAY','TUESDAY','WEDNESDAY',"
                + "'THURSDAY','FRIDAY','SATURDAY','SUNDAY')";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BusinessSchedule row = mapRow(rs);
                map.put(row.getDayOfWeek(), row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public List<BusinessSchedule> getSpecialDates() {
        List<BusinessSchedule> list = new ArrayList<>();
        String sql = "SELECT scheduleID, dayOfWeek, specificDate, openTime, closeTime, "
                + "isClosed, reason, updatedAt "
                + "FROM BusinessSchedule "
                + "WHERE specificDate IS NOT NULL "
                + "ORDER BY specificDate DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveWeeklySchedule(String dayOfWeek, Time openTime,
            Time closeTime, int isClosed, String reason) {
        BusinessSchedule existing = findByDayOfWeek(dayOfWeek);
        if (existing == null) {
            String insert = "INSERT INTO BusinessSchedule "
                    + "(dayOfWeek, specificDate, openTime, closeTime, isClosed, reason, updatedAt) "
                    + "VALUES (?, NULL, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement ps = connection.prepareStatement(insert)) {
                ps.setString(1, dayOfWeek);
                ps.setTime(2, openTime);
                ps.setTime(3, closeTime);
                ps.setInt(4, isClosed);
                ps.setString(5, reason);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        String update = "UPDATE BusinessSchedule "
                + "SET openTime=?, closeTime=?, isClosed=?, reason=?, updatedAt=CURRENT_TIMESTAMP "
                + "WHERE scheduleID=?";
        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setTime(1, openTime);
            ps.setTime(2, closeTime);
            ps.setInt(3, isClosed);
            ps.setString(4, reason);
            ps.setInt(5, existing.getScheduleID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveSpecialDate(Date specificDate, Time openTime,
            Time closeTime, int isClosed, String reason) {
        BusinessSchedule existing = findBySpecificDate(specificDate);
        if (existing == null) {
            String insert = "INSERT INTO BusinessSchedule "
                    + "(dayOfWeek, specificDate, openTime, closeTime, isClosed, reason, updatedAt) "
                    + "VALUES (NULL, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement ps = connection.prepareStatement(insert)) {
                ps.setDate(1, specificDate);
                ps.setTime(2, openTime);
                ps.setTime(3, closeTime);
                ps.setInt(4, isClosed);
                ps.setString(5, reason);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        String update = "UPDATE BusinessSchedule "
                + "SET openTime=?, closeTime=?, isClosed=?, reason=?, updatedAt=CURRENT_TIMESTAMP "
                + "WHERE scheduleID=?";
        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setTime(1, openTime);
            ps.setTime(2, closeTime);
            ps.setInt(3, isClosed);
            ps.setString(4, reason);
            ps.setInt(5, existing.getScheduleID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSpecialDate(int scheduleID) {
        String sql = "DELETE FROM BusinessSchedule "
                + "WHERE scheduleID=? AND specificDate IS NOT NULL";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, scheduleID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * [OPERATING HOURS] Kiem tra thoi gian khach dat ban co nam trong gio
     * hoat dong cua nha hang khong. Uu tien lich ngay dac biet, neu khong co
     * thi dung lich theo thu trong tuan.
     */
    public String validateReservationTime(Timestamp orderTime) {
        if (orderTime == null) {
            return "Vui lòng chọn ngày và giờ đến.";
        }

        LocalDateTime dateTime = orderTime.toLocalDateTime();
        BusinessSchedule schedule = findScheduleForDate(Date.valueOf(dateTime.toLocalDate()));
        if (schedule == null) {
            return "Nhà hàng chưa thiết lập giờ hoạt động cho ngày này.";
        }

        if (schedule.getIsClosed() == 1) {
            String reason = schedule.getReason();
            return reason == null || reason.isBlank()
                    ? "Nhà hàng không hoạt động vào ngày này."
                    : "Nhà hàng không hoạt động vào ngày này: " + reason;
        }

        if (schedule.getOpenTime() == null || schedule.getCloseTime() == null) {
            return "Nhà hàng chưa thiết lập giờ hoạt động cho ngày này.";
        }

        LocalTime selected = dateTime.toLocalTime();
        LocalTime open = schedule.getOpenTime().toLocalTime();
        LocalTime close = schedule.getCloseTime().toLocalTime();
        // [OPERATING HOURS] Khach phai dat ban truoc gio dong cua toi thieu 1 gio 30 phut.
        LocalTime latestReservationTime = close.minusMinutes(60);
        if (selected.isBefore(open) || selected.isAfter(close)) {
            return "Thời gian đặt bàn phải nằm trong giờ hoạt động: "
                    + open + " - " + close + ".";
        }

        if (selected.isAfter(latestReservationTime)) {
            return "Hôm nay nhà hàng hoạt động từ "+open+"-"+close+
                   " Nhà hàng chỉ nhận đặt bàn trước giờ đóng cửa tối thiểu 1 giờ.";
                    
        }
        return null;
    }

    private BusinessSchedule findScheduleForDate(Date date) {
        BusinessSchedule special = findBySpecificDate(date);
        if (special != null) {
            return special;
        }

        LocalDate localDate = date.toLocalDate();
        DayOfWeek day = localDate.getDayOfWeek();
        return findByDayOfWeek(day.name());
    }

    private BusinessSchedule findByDayOfWeek(String dayOfWeek) {
        String sql = "SELECT scheduleID, dayOfWeek, specificDate, openTime, closeTime, "
                + "isClosed, reason, updatedAt "
                + "FROM BusinessSchedule "
                + "WHERE dayOfWeek=? AND specificDate IS NULL "
                + "LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, dayOfWeek);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private BusinessSchedule findBySpecificDate(Date specificDate) {
        String sql = "SELECT scheduleID, dayOfWeek, specificDate, openTime, closeTime, "
                + "isClosed, reason, updatedAt "
                + "FROM BusinessSchedule "
                + "WHERE specificDate=? "
                + "LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, specificDate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private BusinessSchedule mapRow(ResultSet rs) throws SQLException {
        return new BusinessSchedule(
                rs.getInt("scheduleID"),
                rs.getString("dayOfWeek"),
                rs.getDate("specificDate"),
                rs.getTime("openTime"),
                rs.getTime("closeTime"),
                rs.getInt("isClosed"),
                rs.getString("reason"),
                rs.getTimestamp("updatedAt")
        );
    }
}
