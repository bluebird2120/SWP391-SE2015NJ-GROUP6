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

public class EmployeeShiftDAO extends DBContext {

    public static final int LATE_THRESHOLD_MINUTES = 15;

    /**
     * Gán ca làm việc mới cho nhân viên (ở trạng thái 'scheduled').
     * Người gọi (Caller) PHẢI kiểm tra sự trùng lặp ca bằng hasOverlap trước khi thực hiện.
     * 
     * @param employeeID ID của nhân viên cần gán ca
     * @param templateID ID của mẫu ca làm việc
     * @param workDate Ngày làm việc
     * @return ID tự tăng (shiftID) của ca làm việc mới, hoặc -1 nếu có lỗi xảy ra
     */
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

    /**
     * Kiểm tra nhân viên đã có ca làm việc nào trong ngày được chọn chưa.
     * Quy tắc nghiệp vụ: Mỗi nhân viên chỉ được gán tối đa 1 ca làm việc trong 1 ngày.
     * 
     * @param employeeID ID nhân viên
     * @param workDate Ngày cần kiểm tra
     * @param newTemplateID Tham số giữ lại để không phá vỡ signature cũ, không dùng trong query
     * @return true nếu nhân viên đã có ca trong ngày, ngược lại false
     */
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

    /**
     * Thực hiện gán ca làm việc hàng loạt cho toàn bộ các ngày trong tháng (phục vụ chức năng gán ca tự động của Owner).
     * Sử dụng Database Transaction; nếu một ngày gặp lỗi sẽ rollback toàn bộ.
     * 
     * @param employeeID ID nhân viên
     * @param templateID ID của mẫu ca làm việc gán cho cả tháng
     * @param year Năm áp dụng
     * @param month Tháng áp dụng
     * @return Số lượng ngày (ca) đã được gán thành công, hoặc -1 nếu thất bại
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
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
            return -1;
        } finally {
            try {
                connection.setAutoCommit(originalAuto);
            } catch (SQLException ignore) {
            }
        }
    }

    /**
     * Gán ca hàng loạt cho các ngày chưa có lịch làm việc trong tháng.
     * Bỏ qua (skip) các ngày đã được xếp ca trước đó để tránh ghi đè dữ liệu.
     * 
     * @param employeeID ID nhân viên
     * @param templateID ID của mẫu ca làm việc
     * @param year Năm áp dụng
     * @param month Tháng áp dụng
     * @return Số ca mới được gán thành công, hoặc -1 nếu thất bại
     */
    public int assignMonthSkipExisting(int employeeID, int templateID, int year, int month) {
        java.time.LocalDate first = java.time.LocalDate.of(year, month, 1);
        int days = first.lengthOfMonth();

        java.util.Set<Integer> existingDays = new java.util.HashSet<>();
        String findSql = "SELECT DISTINCT DAY(workDate) AS d FROM EmployeeShifts "
                + "WHERE employeeID = ? AND YEAR(workDate) = ? AND MONTH(workDate) = ?";
        try (PreparedStatement ps = connection.prepareStatement(findSql)) {
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
            originalAuto = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
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
            connection.commit();
            return count;
        } catch (SQLException ex) {
            ex.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
            return -1;
        } finally {
            try {
                connection.setAutoCommit(originalAuto);
            } catch (SQLException ignore) {
            }
        }
    }

    /**
     * Xóa toàn bộ các ca làm việc ở trạng thái chưa điểm danh ('scheduled') trong tháng và gán lại theo mẫu ca mới.
     * Bảo toàn các ca đã được điểm danh hoặc có trạng thái khác (present, late, absent).
     * 
     * @param employeeID ID nhân viên
     * @param templateID ID mẫu ca làm việc mới
     * @param year Năm áp dụng
     * @param month Tháng áp dụng
     * @return Số lượng ca mới được gán thành công, hoặc -1 nếu thất bại
     */
    public int replaceMonth(int employeeID, int templateID, int year, int month) {
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
            try (PreparedStatement ps = connection.prepareStatement(findAttended)) {
                ps.setInt(1, employeeID);
                ps.setInt(2, year);
                ps.setInt(3, month);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        attendedDays.add(rs.getInt("d"));
                    }
                }
            }

            originalAuto = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(deleteSql)) {
                ps.setInt(1, employeeID);
                ps.setInt(2, year);
                ps.setInt(3, month);
                ps.executeUpdate();
            }

            int count = 0;
            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
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

            connection.commit();
            return count;
        } catch (SQLException ex) {
            ex.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
            return -1;
        } finally {
            try {
                connection.setAutoCommit(originalAuto);
            } catch (SQLException ignore) {
            }
        }
    }

    /**
     * Kiểm tra xem nhân viên đã có bất kỳ ca làm việc nào được xếp lịch trong tháng và năm được chọn chưa.
     * Dùng để ngăn chặn phân ca tháng trùng lặp.
     * 
     * @param employeeID ID nhân viên
     * @param year Năm cần kiểm tra
     * @param month Tháng cần kiểm tra
     * @return true nếu nhân viên đã được gán ít nhất một ca làm việc trong tháng, ngược lại false
     */
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

    /**
     * Liệt kê lịch làm việc cá nhân của một nhân viên trong tháng được chọn (hiển thị trên trang Lịch của tôi).
     * 
     * @param employeeID ID của nhân viên
     * @param year Năm cần xem
     * @param month Tháng cần xem
     * @return Danh sách ca làm việc của nhân viên đó trong tháng
     */
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

    /**
     * Hủy gán ca làm việc của nhân viên (thu hồi ca đã xếp).
     * Chỉ cho phép hủy gán khi ca làm việc vẫn ở trạng thái 'scheduled' (chưa được điểm danh).
     * Tự động xóa mọi yêu cầu đổi ca/xin nghỉ (ShiftSwapRequests) liên quan đến ca làm việc bị xóa này.
     * 
     * @param shiftID ID của ca làm việc cần hủy gán
     * @return true nếu hủy gán ca làm việc thành công, ngược lại false
     */
    public boolean unassign(int shiftID) {
        String deleteRequestsSql = "DELETE FROM ShiftSwapRequests WHERE requesterShiftID = ? OR targetShiftID = ?";
        String deleteShiftSql = "DELETE FROM EmployeeShifts WHERE shiftID = ? AND status = 'scheduled'";

        java.sql.Connection conn = connection;
        boolean originalAuto = true;
        try {
            originalAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement psReq = conn.prepareStatement(deleteRequestsSql)) {
                psReq.setInt(1, shiftID);
                psReq.setInt(2, shiftID);
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
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(originalAuto);
            } catch (SQLException ignore) {
            }
        }
    }

    /**
     * Kiểm tra xem nhân viên đã có ca làm việc khác trong ngày làm việc chỉ định hay chưa (loại trừ chính ca đang xử lý).
     * Dùng khi phê duyệt đổi ca để tránh trường hợp một nhân viên có hai ca làm việc trùng lặp trong cùng một ngày.
     * 
     * @param employeeID ID của nhân viên cần kiểm tra
     * @param workDate Ngày làm việc
     * @param excludeShiftID ID của ca làm việc hiện tại cần loại trừ khỏi kiểm tra
     * @return true nếu có ca làm việc khác xung đột trong cùng ngày, ngược lại false
     */
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

    /**
     * Liệt kê tất cả các ca làm việc của toàn bộ nhân viên trong một ngày cụ thể.
     * Dùng cho giao diện chấm công ngày (Attendance).
     * 
     * @param workDate Ngày cần xem danh sách ca làm việc
     * @return Danh sách đối tượng ShiftRow chi tiết của ngày đó
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

    /**
     * Tìm kiếm thông tin cơ bản của ca làm việc theo ID ca làm việc (không kết hợp thông tin JOIN).
     * 
     * @param shiftID ID của ca làm việc
     * @return Đối tượng EmployeeShifts cơ bản, hoặc null nếu không tìm thấy
     */
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

    /**
     * Thực hiện Check-in ca làm việc của nhân viên.
     * Tự động tính toán trạng thái chuyên cần (status):
     * - Trạng thái là 'late' nếu thời gian check-in trễ hơn thời gian bắt đầu ca (startTime) cộng thêm 15 phút (ngưỡng đi trễ).
     * - Trạng thái là 'present' nếu đi làm đúng giờ (dưới ngưỡng 15 phút).
     * Áp dụng quy tắc khóa lạc quan và quy tắc chỉ điểm danh trong ngày hôm nay:
     * - Ca làm việc phải ở trạng thái 'scheduled', chưa check-in và ngày làm việc phải là ngày hôm nay.
     * 
     * @param shiftID ID của ca làm việc cần check-in
     * @param checkInTime Thời điểm check-in thực tế
     * @return true nếu check-in thành công (1 dòng bị ảnh hưởng), ngược lại false
     */
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

    /**
     * Thực hiện Check-out ca làm việc.
     * Chỉ thiết lập thời gian checkOutTime, trạng thái (present/late) được giữ nguyên.
     * Áp dụng quy tắc khóa lạc quan và chỉ cho phép check-out trong ngày hôm nay:
     * - Trạng thái hiện tại phải là 'present' hoặc 'late', đã check-in và chưa check-out.
     * 
     * @param shiftID ID của ca làm việc cần check-out
     * @param checkOutTime Thời điểm check-out thực tế
     * @return true nếu check-out thành công, ngược lại false
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

    /**
     * Đánh dấu nhân viên vắng mặt (absent) trong ca làm việc.
     * Chỉ cho phép thực hiện với các ca chưa điểm danh ('scheduled') và ngày làm việc là ngày hôm nay.
     * 
     * @param shiftID ID ca làm việc
     * @return true nếu cập nhật thành công, ngược lại false
     */
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

    /**
     * Khôi phục ca làm việc về trạng thái ban đầu ('scheduled').
     * Chỉ cho phép thực hiện với ca làm việc chưa hoàn tất check-out và diễn ra trong ngày hôm nay.
     * 
     * @param shiftID ID ca làm việc
     * @return true nếu khôi phục thành công, ngược lại false
     */
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

    /**
     * Lấy danh sách các ca làm việc của các đồng nghiệp khác có đủ điều kiện để nhân viên chọn đổi ca.
     * Điều kiện: Ca của người khác (excludeEmployeeID), diễn ra trong tương lai hoặc hôm nay, và chưa điểm danh ('scheduled').
     * 
     * @param excludeEmployeeID ID của nhân viên đang yêu cầu đổi ca (để loại trừ ca của chính họ)
     * @return Danh sách các ca làm việc khả dụng để yêu cầu đổi ca
     */
    public List<ShiftRow> listEligibleSwaps(int excludeEmployeeID) {
        List<ShiftRow> list = new ArrayList<>();
        String sql = "SELECT es.shiftID, es.employeeID, e.fullName, es.templateID, st.shiftName, st.startTime, st.endTime, es.workDate, es.status "
                + "FROM EmployeeShifts es "
                + "JOIN Employee e ON es.employeeID = e.employeeID "
                + "JOIN ShiftTemplates st ON es.templateID = st.templateID "
                + "WHERE es.employeeID != ? AND es.workDate >= CURRENT_DATE AND es.status = 'scheduled' "
                + "ORDER BY es.workDate ASC, st.startTime ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, excludeEmployeeID);
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
                    r.setStatus(rs.getString("status"));
                    list.add(r);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy chi tiết ca làm việc cụ thể kèm thông tin nhân viên và mẫu ca theo ID ca.
     * 
     * @param shiftID ID ca làm việc
     * @return Đối tượng ShiftRow chi tiết, hoặc null nếu không tồn tại
     */
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
        long startAbs = midnight + ((h * 3600L) + (m * 60L) + s) * 1000L;
        long lateThreshold = startAbs + LATE_THRESHOLD_MINUTES * 60_000L;
        return checkInMs > lateThreshold ? "late" : "present";
    }
}
