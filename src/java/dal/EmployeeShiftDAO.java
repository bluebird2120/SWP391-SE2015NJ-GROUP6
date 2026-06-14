package dal;

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

/**
 * DAO cho EmployeeShifts.
 *
 * Status flow: scheduled -> present | late | absent
 *   Đã ra ca = checkOutTime IS NOT NULL (status không bị ghi đè 'completed').
 *
 * Mọi UPDATE attendance đều có:
 *   - WHERE workDate = CURRENT_DATE  (today-only rule)
 *   - WHERE status / checkInTime / checkOutTime phù hợp (optimistic lock)
 */
public class EmployeeShiftDAO extends DBContext {

    public static final int LATE_THRESHOLD_MINUTES = 15;

    /* ===================== ROSTER ===================== */

    /**
     * Insert ca mới với status='scheduled'. Caller PHẢI gọi hasOverlap trước.
     * Trả shiftID hoặc -1.
     */
    public int assign(int employeeID, int templateID, Date workDate) {
        String sql = "INSERT INTO EmployeeShifts (templateID, employeeID, workDate, status) "
                + "VALUES (?, ?, ?, 'scheduled')";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, templateID);
            ps.setInt(2, employeeID);
            ps.setDate(3, workDate);
            if (ps.executeUpdate() == 0) return -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    /**
     * Kiểm tra staff đã có ca khác chồng giờ trong cùng workDate.
     * Trả true = có overlap, không cho assign nữa.
     */
    public boolean hasOverlap(int employeeID, Date workDate, int newTemplateID) {
        String sql = "SELECT 1 "
                + "FROM EmployeeShifts es "
                + "JOIN ShiftTemplates st ON es.templateID = st.templateID "
                + "JOIN ShiftTemplates nt ON nt.templateID = ? "
                + "WHERE es.employeeID = ? AND es.workDate = ? "
                + "  AND st.startTime < nt.endTime "
                + "  AND st.endTime   > nt.startTime "
                + "LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, newTemplateID);
            ps.setInt(2, employeeID);
            ps.setDate(3, workDate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return true;
        }
    }

    /**
     * Batch insert ca cho mọi ngày trong tháng (calendar).
     * Phải gọi hasAnyShiftInMonth trước để chặn duplicate.
     * Dùng transaction; rollback nếu lỗi 1 row → trả -1.
     */
    public int assignMonth(int employeeID, int templateID, int year, int month) {
        java.time.LocalDate first = java.time.LocalDate.of(year, month, 1);
        int days = first.lengthOfMonth();
        String sql = "INSERT INTO EmployeeShifts (templateID, employeeID, workDate, status) "
                + "VALUES (?, ?, ?, 'scheduled')";
        boolean originalAuto = true;
        try {
            originalAuto = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (int d = 1; d <= days; d++) {
                    Date workDate = Date.valueOf(first.withDayOfMonth(d));
                    ps.setInt(1, templateID);
                    ps.setInt(2, employeeID);
                    ps.setDate(3, workDate);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            connection.commit();
            return days;
        } catch (SQLException ex) {
            ex.printStackTrace();
            try { connection.rollback(); } catch (SQLException ignore) {}
            return -1;
        } finally {
            try { connection.setAutoCommit(originalAuto); } catch (SQLException ignore) {}
        }
    }

    /** Có bất kỳ ca nào của nhân viên trong tháng (year/month)? */
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
            return true; // fail-safe: chặn batch nếu query lỗi
        }
    }

    /** Liệt kê ca theo tháng (cho UI calendar tháng). */
    public List<ShiftRow> listByMonth(int year, int month) {
        List<ShiftRow> list = new ArrayList<>();
        String sql = "SELECT es.shiftID, es.employeeID, e.fullName, "
                + "       es.templateID, st.shiftName, st.startTime, st.endTime, "
                + "       es.workDate, es.checkInTime, es.checkOutTime, es.status "
                + "FROM EmployeeShifts es "
                + "JOIN Employee e        ON e.employeeID = es.employeeID "
                + "JOIN ShiftTemplates st ON st.templateID = es.templateID "
                + "WHERE YEAR(es.workDate) = ? AND MONTH(es.workDate) = ? "
                + "ORDER BY es.workDate, st.startTime, e.fullName";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
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

    /** Liệt kê ca của 1 nhân viên trong tháng — dùng cho Staff My Schedule. */
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

    /** Chỉ cho unassign khi status='scheduled' (chưa xử lý). */
    public boolean unassign(int shiftID) {
        String sql = "DELETE FROM EmployeeShifts WHERE shiftID = ? AND status = 'scheduled'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shiftID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Liệt kê mọi shift trong ngày, JOIN Employee + ShiftTemplates.
     * Không filter status — Attendance view cần thấy hết.
     */
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

    /** Find by id (raw EmployeeShifts, không join). */
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

    /* ===================== ATTENDANCE ===================== */

    /**
     * Check-in. Auto compute status:
     *   - late nếu checkInTime > startTime + 15p
     *   - present ngược lại
     *
     * Update có optimistic lock + today-only:
     *   status='scheduled' AND checkInTime IS NULL AND workDate = CURRENT_DATE
     *
     * Trả true = thành công (1 row affected).
     */
    public boolean checkIn(int shiftID, Timestamp checkInTime) {
        // 1. Lấy startTime của template để compare
        String getSql = "SELECT st.startTime FROM EmployeeShifts es "
                + "JOIN ShiftTemplates st ON st.templateID = es.templateID "
                + "WHERE es.shiftID = ?";
        Time startTime = null;
        try (PreparedStatement ps = connection.prepareStatement(getSql)) {
            ps.setInt(1, shiftID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) startTime = rs.getTime("startTime");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        if (startTime == null) return false;

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

    /**
     * Check-out: chỉ set checkOutTime, status giữ nguyên (present/late).
     * Optimistic lock: status IN (present,late) AND checkInTime IS NOT NULL AND checkOutTime IS NULL AND today.
     */
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

    /** Mark absent: chỉ khi đang scheduled + today. */
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

    /** Reset về scheduled: chỉ khi chưa check-out + today. */
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

    /* ===================== HELPERS ===================== */

    /**
     * So sánh checkInTime với startTime + 15 phút (cùng ngày).
     * v1 không hỗ trợ overnight nên workDate luôn = ngày hiện tại của Timestamp.
     */
    static String computeStatus(Timestamp checkInTime, Time startTime) {
        long checkInMs = checkInTime.getTime();
        // Construct startTime ts cho cùng ngày với checkInTime
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(checkInMs);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long midnight = cal.getTimeInMillis();
        long startMs = startTime.getTime();              // ms từ epoch ngày 1970-01-01
        // startTime java.sql.Time chỉ giữ giờ:phút:giây nhưng giá trị raw có offset timezone.
        // Cách an toàn: parse string.
        String[] parts = startTime.toString().split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int s = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        long startAbs = midnight + ((h * 3600L) + (m * 60L) + s) * 1000L;
        long lateThreshold = startAbs + LATE_THRESHOLD_MINUTES * 60_000L;
        return checkInMs > lateThreshold ? "late" : "present";
    }
}
