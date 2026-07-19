package dal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.EmployeeShifts;

public class EmployeeShiftDAO extends DBContext {

    public static final int LATE_THRESHOLD_MINUTES = 15;

    private Connection currentConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = new DBContext().getConnection();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
        return connection;
    }


    public int assign(int employeeID, int templateID, Date workDate) {

        String sql = "INSERT INTO EmployeeShifts (templateID, employeeID, workDate, status) "
                + "VALUES (?, ?, ?, 'scheduled')";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, templateID);

            ps.setInt(2, employeeID);

            ps.setDate(3, workDate);

            if (ps.executeUpdate() == 0)
                return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (keys.next())
                    return keys.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }


    public boolean hasOverlap(int employeeID, Date workDate, int newTemplateID) {

        String sql = "SELECT 1 FROM EmployeeShifts "
                 + "WHERE employeeID = ? AND workDate = ? "
                 + "LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, employeeID);

            ps.setDate(2, workDate);
            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return true;
        }
    }


    public int assignMonth(int employeeID, int templateID, int year, int month) {
        Connection conn = currentConnection();
        if (conn == null) {
            System.err.println("[EmployeeShiftDAO] assignMonth lỗi: Không thể kết nối database.");
            return -1;
        }

        java.time.LocalDate first = java.time.LocalDate.of(year, month, 1);
        int days = first.lengthOfMonth();
        String sql = "INSERT INTO EmployeeShifts (templateID, employeeID, workDate, status) "
                + "VALUES (?, ?, ?, 'scheduled')";
        boolean originalAuto = true;
        try {
            originalAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int d = 1; d <= days; d++) {
                    Date workDate = Date.valueOf(first.withDayOfMonth(d));
                    ps.setInt(1, templateID);
                    ps.setInt(2, employeeID);
                    ps.setDate(3, workDate);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            return days;
        } catch (SQLException ex) {
            ex.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ignore) {
            }
            return -1;
        } finally {
            try {
                conn.setAutoCommit(originalAuto);
            } catch (SQLException ignore) {
            }
        }
    }


    public int assignMonthSkipExisting(int employeeID, int templateID, int year, int month) {
        Connection conn = currentConnection();
        if (conn == null) {
            System.err.println("[EmployeeShiftDAO] assignMonthSkipExisting lỗi: Không thể kết nối database.");
            return -1;
        }

        java.time.LocalDate first = java.time.LocalDate.of(year, month, 1);

        int days = first.lengthOfMonth();


        java.util.Set<Integer> existingDays = new java.util.HashSet<>();

        String findSql = "SELECT DISTINCT DAY(workDate) AS d FROM EmployeeShifts "
                + "WHERE employeeID = ? AND YEAR(workDate) = ? AND MONTH(workDate) = ?";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {

            ps.setInt(1, employeeID);

            ps.setInt(2, year);

            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    existingDays.add(rs.getInt("d"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }


        if (existingDays.size() >= days)
            return 0;


        String insertSql = "INSERT INTO EmployeeShifts (templateID, employeeID, workDate, status) "
                + "VALUES (?, ?, ?, 'scheduled')";

        boolean originalAuto = true;

        int count = 0;
        try {

            originalAuto = conn.getAutoCommit();

            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {

                for (int d = 1; d <= days; d++) {

                    if (existingDays.contains(d))
                        continue;

                    Date workDate = Date.valueOf(first.withDayOfMonth(d));

                    ps.setInt(1, templateID);

                    ps.setInt(2, employeeID);

                    ps.setDate(3, workDate);

                    ps.addBatch();

                    count++;
                }

                if (count > 0)
                    ps.executeBatch();
            }

            conn.commit();

            return count;
        } catch (SQLException ex) {
            ex.printStackTrace();
            try {

                conn.rollback();
            } catch (SQLException ignore) {
            }
            return -1;
        } finally {
            try {

                conn.setAutoCommit(originalAuto);
            } catch (SQLException ignore) {
            }
        }
    }


    public int replaceMonth(int employeeID, int templateID, int year, int month) {
        Connection conn = currentConnection();
        if (conn == null) {
            System.err.println("[EmployeeShiftDAO] replaceMonth lỗi: Không thể kết nối database.");
            return -1;
        }

        java.time.LocalDate first = java.time.LocalDate.of(year, month, 1);

        int days = first.lengthOfMonth();


        String deleteSql = "DELETE FROM EmployeeShifts "
                + "WHERE employeeID = ? AND YEAR(workDate) = ? AND MONTH(workDate) = ? "
                + "AND status = 'scheduled'";

        String insertSql = "INSERT INTO EmployeeShifts (templateID, employeeID, workDate, status) "
                + "VALUES (?, ?, ?, 'scheduled')";


        java.util.Set<Integer> attendedDays = new java.util.HashSet<>();

        String findAttended = "SELECT DISTINCT DAY(workDate) AS d FROM EmployeeShifts "
                + "WHERE employeeID = ? AND YEAR(workDate) = ? AND MONTH(workDate) = ? "
                + "AND status != 'scheduled'";


        boolean originalAuto = true;
        try {

            try (PreparedStatement ps = conn.prepareStatement(findAttended)) {

                ps.setInt(1, employeeID);

                ps.setInt(2, year);
                ps.setInt(3, month);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {

                        attendedDays.add(rs.getInt("d"));
                    }
                }
            }
            originalAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {

                ps.setInt(1, employeeID);

                ps.setInt(2, year);

                ps.setInt(3, month);

                ps.executeUpdate();
            }
            int count = 0;

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {

                for (int d = 1; d <= days; d++) {

                    if (attendedDays.contains(d))
                        continue;

                    Date workDate = Date.valueOf(first.withDayOfMonth(d));

                    ps.setInt(1, templateID);

                    ps.setInt(2, employeeID);

                    ps.setDate(3, workDate);

                    ps.addBatch();

                    count++;
                }

                if (count > 0)
                    ps.executeBatch();
            }


            conn.commit();

            return count;
        } catch (SQLException ex) {
            ex.printStackTrace();
            try {

                conn.rollback();
            } catch (SQLException ignore) {
            }
            return -1;
        } finally {
            try {

                conn.setAutoCommit(originalAuto);
            } catch (SQLException ignore) {
            }
        }
    }


    public boolean hasAnyShiftInMonth(int employeeID, int year, int month) {
        String sql = "SELECT 1 FROM EmployeeShifts "
                + "WHERE employeeID = ? "
                + "  AND YEAR(workDate) = ? AND MONTH(workDate) = ? "
                + "LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return true;
        }
    }


    public List<ShiftRow> listByEmployeeAndMonth(int employeeID, int year, int month) {

        List<ShiftRow> list = new ArrayList<>();

        String sql = "SELECT es.shiftID, es.employeeID, e.fullName, "
                + "       es.templateID, st.shiftName, st.startTime, st.endTime, "
                + "       es.workDate, es.checkInTime, es.checkOutTime, es.status "
                + "FROM EmployeeShifts es "
                + "JOIN Employee e        ON e.employeeID = es.employeeID "
                + "JOIN ShiftTemplates st ON st.templateID = es.templateID "
                + "WHERE es.employeeID = ? "
                + "  AND YEAR(es.workDate) = ? AND MONTH(es.workDate) = ? "
                + "ORDER BY es.workDate, st.startTime";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, employeeID);

            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    ShiftRow r = new ShiftRow();

                    r.setShiftID(rs.getInt("shiftID"));
                    r.setEmployeeID(rs.getInt("employeeID"));
                    r.setFullName(rs.getString("fullName"));

                    r.setTemplateID(rs.getInt("templateID"));
                    r.setShiftName(rs.getString("shiftName"));
                    r.setStartTime(rs.getTime("startTime"));
                    r.setEndTime(rs.getTime("endTime"));

                    r.setWorkDate(
                            rs.getString("workDate") != null ? java.sql.Date.valueOf(rs.getString("workDate")) : null);

                    r.setCheckInTime(rs.getTimestamp("checkInTime"));
                    r.setCheckOutTime(rs.getTimestamp("checkOutTime"));
                    r.setStatus(rs.getString("status"));
                    list.add(r);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }


    public boolean unassign(int shiftID) {

        String deleteRequestsSql = "DELETE FROM ShiftSwapRequests WHERE requesterShiftID = ?";

        String deleteShiftSql = "DELETE FROM EmployeeShifts WHERE shiftID = ? AND status = 'scheduled'";


        java.sql.Connection conn = connection;

        boolean originalAuto = true;
        try {

            if (conn == null) {
                System.err.println("[EmployeeShiftDAO] unassign lỗi: Không thể kết nối database.");
                return false;
            }

            originalAuto = conn.getAutoCommit();

            conn.setAutoCommit(false);


            try (PreparedStatement psReq = conn.prepareStatement(deleteRequestsSql)) {

                psReq.setInt(1, shiftID);

                psReq.executeUpdate();
            }


            int affectedRows = 0;

            try (PreparedStatement psShift = conn.prepareStatement(deleteShiftSql)) {

                psShift.setInt(1, shiftID);

                affectedRows = psShift.executeUpdate();
            }


            conn.commit();

            return affectedRows > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            try {

                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return false;
        } finally {
            try {

                if (conn != null) {
                    conn.setAutoCommit(originalAuto);
                }
            } catch (SQLException ignore) {
            }
        }
    }


    public boolean hasConflictingShift(int employeeID, Date workDate, int excludeShiftID) {

        String sql = "SELECT 1 FROM EmployeeShifts WHERE employeeID = ? AND workDate = ? AND shiftID != ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, employeeID);

            ps.setDate(2, workDate);

            ps.setInt(3, excludeShiftID);
            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return true;
        }
    }


    public List<ShiftRow> listByDate(Date workDate) {

        List<ShiftRow> list = new ArrayList<>();

        String sql = "SELECT es.shiftID, es.employeeID, e.fullName, "
                + "       es.templateID, st.shiftName, st.startTime, st.endTime, "
                + "       es.workDate, es.checkInTime, es.checkOutTime, es.status "
                + "FROM EmployeeShifts es "
                + "JOIN Employee e        ON e.employeeID = es.employeeID "
                + "JOIN ShiftTemplates st ON st.templateID = es.templateID "
                + "WHERE es.workDate = ? "
                + "ORDER BY st.startTime, e.fullName";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setDate(1, workDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    ShiftRow r = new ShiftRow();
                    r.setShiftID(rs.getInt("shiftID"));
                    r.setEmployeeID(rs.getInt("employeeID"));
                    r.setFullName(rs.getString("fullName"));
                    r.setTemplateID(rs.getInt("templateID"));
                    r.setShiftName(rs.getString("shiftName"));
                    r.setStartTime(rs.getTime("startTime"));
                    r.setEndTime(rs.getTime("endTime"));
                    r.setWorkDate(rs.getDate("workDate"));

                    r.setCheckInTime(rs.getTimestamp("checkInTime"));
                    r.setCheckOutTime(rs.getTimestamp("checkOutTime"));
                    r.setStatus(rs.getString("status"));
                    list.add(r);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }


    public EmployeeShifts findById(int shiftID) {

        String sql = "SELECT shiftID, templateID, employeeID, workDate, "
                + "checkInTime, checkOutTime, status FROM EmployeeShifts WHERE shiftID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, shiftID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    EmployeeShifts s = new EmployeeShifts();
                    s.setShiftID(rs.getInt("shiftID"));
                    s.setTemplateID(rs.getInt("templateID"));
                    s.setEmployeeID(rs.getInt("employeeID"));
                    s.setWorkDate(rs.getDate("workDate"));
                    s.setCheckInTime(rs.getTimestamp("checkInTime"));
                    s.setCheckOutTime(rs.getTimestamp("checkOutTime"));
                    s.setStatus(rs.getString("status"));
                    return s;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public boolean checkIn(int shiftID, Timestamp checkInTime) {

        String getSql = "SELECT st.startTime FROM EmployeeShifts es "
                + "JOIN ShiftTemplates st ON st.templateID = es.templateID "
                + "WHERE es.shiftID = ?";
        Time startTime = null;
        try (PreparedStatement ps = connection.prepareStatement(getSql)) {

            ps.setInt(1, shiftID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    startTime = rs.getTime("startTime");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        if (startTime == null)
            return false;


        String status = computeStatus(checkInTime, startTime);


        String sql = "UPDATE EmployeeShifts "
                + "SET checkInTime = ?, status = ? "
                + "WHERE shiftID = ? "
                + "  AND status = 'scheduled' "
                + "  AND checkInTime IS NULL "
                + "  AND workDate = CURRENT_DATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setTimestamp(1, checkInTime);

            ps.setString(2, status);

            ps.setInt(3, shiftID);

            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public boolean checkOut(int shiftID, Timestamp checkOutTime) {

        String sql = "UPDATE EmployeeShifts "
                + "SET checkOutTime = ? "
                + "WHERE shiftID = ? "
                + "  AND status IN ('present','late') "
                + "  AND checkInTime  IS NOT NULL "
                + "  AND checkOutTime IS NULL "
                + "  AND workDate = CURRENT_DATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setTimestamp(1, checkOutTime);

            ps.setInt(2, shiftID);

            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public boolean markAbsent(int shiftID) {

        String sql = "UPDATE EmployeeShifts "
                + "SET status = 'absent', checkInTime = NULL, checkOutTime = NULL "
                + "WHERE shiftID = ? AND status = 'scheduled' AND workDate = CURRENT_DATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, shiftID);

            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public boolean reset(int shiftID) {

        String sql = "UPDATE EmployeeShifts "
                + "SET status = 'scheduled', checkInTime = NULL, checkOutTime = NULL "
                + "WHERE shiftID = ? AND checkOutTime IS NULL AND workDate = CURRENT_DATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, shiftID);

            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public ShiftRow getShiftByID(int shiftID) {

        String sql = "SELECT es.shiftID, es.employeeID, e.fullName, es.templateID, st.shiftName, st.startTime, st.endTime, es.workDate, es.status "
                + "FROM EmployeeShifts es "
                + "JOIN Employee e ON es.employeeID = e.employeeID "
                + "JOIN ShiftTemplates st ON es.templateID = st.templateID "
                + "WHERE es.shiftID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, shiftID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    ShiftRow r = new ShiftRow();
                    r.setShiftID(rs.getInt("shiftID"));
                    r.setEmployeeID(rs.getInt("employeeID"));
                    r.setFullName(rs.getString("fullName"));
                    r.setTemplateID(rs.getInt("templateID"));
                    r.setShiftName(rs.getString("shiftName"));
                    r.setStartTime(rs.getTime("startTime"));
                    r.setEndTime(rs.getTime("endTime"));
                    r.setWorkDate(
                            rs.getString("workDate") != null ? java.sql.Date.valueOf(rs.getString("workDate")) : null);
                    r.setStatus(rs.getString("status"));
                    return r;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public Integer getActiveEmployeeForCurrentShift() {
        String sql = "SELECT es.employeeID, COUNT(o.orderID) AS active_orders "
                + "FROM EmployeeShifts es "
                + "JOIN ShiftTemplates st ON es.templateID = st.templateID "
                + "JOIN Employee e ON es.employeeID = e.employeeID "
                + "LEFT JOIN `Order` o ON o.employeeID = e.employeeID AND o.orderStatus NOT IN ('completed', 'cancelled') "
                + "WHERE es.workDate = CURDATE() "
                + "  AND e.roleID = 2 "
                + "  AND es.checkInTime IS NOT NULL "
                + "  AND es.checkOutTime IS NULL "
                + "  AND es.status IN ('present', 'late') "
                + "  AND ( "
                + "    (st.startTime <= st.endTime AND CURRENT_TIME() BETWEEN st.startTime AND st.endTime) "
                + "    OR "
                + "    (st.startTime > st.endTime AND (CURRENT_TIME() >= st.startTime OR CURRENT_TIME() <= st.endTime)) "
                + "  ) "
                + "GROUP BY es.employeeID "
                + "ORDER BY active_orders ASC "
                + "LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("employeeID");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public boolean isEmployeeOnShift(int employeeID) {
        String sql = "SELECT 1 FROM EmployeeShifts es "
                + "JOIN ShiftTemplates st ON es.templateID = st.templateID "
                + "WHERE es.employeeID = ? "
                + "  AND es.workDate = CURDATE() "
                + "  AND es.checkInTime IS NOT NULL "
                + "  AND es.checkOutTime IS NULL "
                + "  AND es.status IN ('present', 'late') "
                + "  AND ( "
                + "    (st.startTime <= st.endTime AND CURRENT_TIME() BETWEEN st.startTime AND st.endTime) "
                + "    OR "
                + "    (st.startTime > st.endTime AND (CURRENT_TIME() >= st.startTime OR CURRENT_TIME() <= st.endTime)) "
                + "  ) "
                + "LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    //Tính trạng thái chấm công
    static String computeStatus(Timestamp checkInTime, Time startTime) {

        long checkInMs = checkInTime.getTime();

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(checkInMs);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long midnight = cal.getTimeInMillis();

        String[] parts = startTime.toString().split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int s = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        //Tính thời điểm bắt đầu ca đầy đủ trong ngày checkIn
        long startAbs = midnight + ((h * 3600L) + (m * 60L) + s) * 1000L;
        //Tính mốc đi muộn
        long lateThreshold = startAbs + LATE_THRESHOLD_MINUTES * 60_000L;

        return checkInMs > lateThreshold ? "late" : "present";
    }
}
