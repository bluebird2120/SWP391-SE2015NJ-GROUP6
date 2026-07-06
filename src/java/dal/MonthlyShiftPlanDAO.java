package dal;

import model.MonthlyShiftPlan;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MonthlyShiftPlanDAO extends DBContext {

    /**
     * Lưu mới hoặc cập nhật một kế hoạch phân ca tháng (Monthly Shift Plan).
     * - Nếu kế hoạch tháng chưa tồn tại: Tạo mới kế hoạch ở trạng thái 'DRAFT'.
     * - Nếu đã có kế hoạch và trạng thái là 'APPLIED' (đã áp dụng gán ca): Không
     * cho phép cập nhật nữa (trả về -2).
     * - Nếu kế hoạch có sẵn trùng template và chưa bị hủy: Trả về planID hiện tại.
     * - Nếu đổi mẫu ca khác: Cập nhật lại templateID và trả trạng thái về 'DRAFT'.
     * 
     * @param employeeID ID của nhân viên gán ca
     * @param templateID ID của mẫu ca mẫu gán cho nhân viên
     * @param year       Năm áp dụng
     * @param month      Tháng áp dụng (1-12)
     * @param createdBy  ID của Owner tạo/cập nhật kế hoạch này
     * @return ID của kế hoạch (planID) nếu thành công; -1 nếu lỗi database; -2 nếu
     *         kế hoạch đã ở trạng thái APPLIED không thể sửa
     */
    public int saveOrUpdate(int employeeID, int templateID, int year, int month, int createdBy) {
        MonthlyShiftPlan existing = findByEmployee(employeeID, year, month);
        if (existing == null) {
            String sql = "INSERT INTO MonthlyShiftPlan "
                    + "(employeeID, templateID, effectiveYear, effectiveMonth, status, createdBy) "
                    + "VALUES (?, ?, ?, ?, 'DRAFT', ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, employeeID);
                ps.setInt(2, templateID);
                ps.setInt(3, year);
                ps.setInt(4, month);
                ps.setInt(5, createdBy);
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
        if (MonthlyShiftPlan.APPLIED.equals(existing.getStatus())) {
            return -2;
        }
        if (existing.getTemplateID() == templateID
                && !MonthlyShiftPlan.CANCELLED.equals(existing.getStatus())) {
            return existing.getPlanID();
        }
        String sql = "UPDATE MonthlyShiftPlan SET templateID = ?, status = 'DRAFT' WHERE planID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, templateID);
            ps.setInt(2, existing.getPlanID());
            ps.executeUpdate();
            return existing.getPlanID();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    /**
     * Lấy danh sách các kế hoạch phân ca tháng đang ở trạng thái nháp hoặc đã gửi
     * thông báo ('DRAFT', 'NOTIFIED').
     * Dùng để chạy tác vụ nền tự động áp dụng kế hoạch thành các ca làm việc thực
     * tế hàng ngày.
     * 
     * @return Danh sách các kế hoạch phân ca chưa áp dụng xong
     */
    public List<MonthlyShiftPlan> listPending() {
        String sql = "SELECT p.planID, p.employeeID, p.templateID, p.effectiveYear, p.effectiveMonth, "
                + "       p.status, p.createdBy, p.createdAt, p.updatedAt, "
                + "       e.fullName AS employeeName, "
                + "       t.shiftName, t.startTime, t.endTime "
                + "FROM MonthlyShiftPlan p "
                + "JOIN Employee e ON e.employeeID = p.employeeID "
                + "JOIN ShiftTemplates t ON t.templateID = p.templateID "
                + "WHERE p.status IN ('DRAFT','NOTIFIED') "
                + "ORDER BY p.effectiveYear, p.effectiveMonth, p.employeeID";
        return runListQuery(sql, null, null);
    }

    /**
     * Lấy danh sách kế hoạch phân ca theo tháng và năm cụ thể để hiển thị trên giao
     * diện xếp lịch của Owner.
     * 
     * @param year  Năm cần xem
     * @param month Tháng cần xem (1-12)
     * @return Danh sách các kế hoạch phân ca tháng khớp với thời gian lọc
     */
    public List<MonthlyShiftPlan> listByMonth(int year, int month) {
        String sql = "SELECT p.planID, p.employeeID, p.templateID, p.effectiveYear, p.effectiveMonth, "
                + "       p.status, p.createdBy, p.createdAt, p.updatedAt, "
                + "       e.fullName AS employeeName, "
                + "       t.shiftName, t.startTime, t.endTime "
                + "FROM MonthlyShiftPlan p "
                + "JOIN Employee e ON e.employeeID = p.employeeID "
                + "JOIN ShiftTemplates t ON t.templateID = p.templateID "
                + "WHERE p.effectiveYear = ? AND p.effectiveMonth = ? "
                + "ORDER BY e.fullName";
        return runListQuery(sql, year, month);
    }

    /**
     * Cập nhật trạng thái của kế hoạch phân ca tháng (DRAFT -> NOTIFIED -> APPLIED
     * hoặc CANCELLED).
     * 
     * @param planID    ID của kế hoạch
     * @param newStatus Trạng thái mới cần chuyển đổi
     * @return true nếu cập nhật thành công, ngược lại false
     */
    public boolean updateStatus(int planID, String newStatus) {
        String sql = "UPDATE MonthlyShiftPlan SET status = ? WHERE planID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, planID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public MonthlyShiftPlan findById(int planID) {
        String sql = "SELECT planID, employeeID, templateID, effectiveYear, effectiveMonth, "
                + "       status, createdBy, createdAt, updatedAt "
                + "FROM MonthlyShiftPlan "
                + "WHERE planID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, planID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapBasic(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean cancelPlan(int planID) {
        MonthlyShiftPlan plan = findById(planID);
        if (plan == null)
            return false;

        boolean originalAuto = true;
        try {
            if (connection == null) {
                System.err.println("[MonthlyShiftPlanDAO] cancelPlan lỗi: Không thể kết nối database.");
                return false;
            }
            originalAuto = connection.getAutoCommit();
            connection.setAutoCommit(false);

            List<Integer> shiftIDs = new ArrayList<>();
            String selectSql = "SELECT shiftID FROM EmployeeShifts "
                    + "WHERE employeeID = ? AND templateID = ? "
                    + "  AND YEAR(workDate) = ? AND MONTH(workDate) = ? "
                    + "  AND status = 'scheduled'";
            try (PreparedStatement psSel = connection.prepareStatement(selectSql)) {
                psSel.setInt(1, plan.getEmployeeID());
                psSel.setInt(2, plan.getTemplateID());
                psSel.setInt(3, plan.getEffectiveYear());
                psSel.setInt(4, plan.getEffectiveMonth());
                try (ResultSet rs = psSel.executeQuery()) {
                    while (rs.next()) {
                        shiftIDs.add(rs.getInt("shiftID"));
                    }
                }
            }

            if (!shiftIDs.isEmpty()) {
                String deleteReqSql = "DELETE FROM ShiftSwapRequests WHERE requesterShiftID = ?";
                try (PreparedStatement psReq = connection.prepareStatement(deleteReqSql)) {
                    for (int id : shiftIDs) {
                        psReq.setInt(1, id);
                        psReq.addBatch();
                    }
                    psReq.executeBatch();
                }

                String deleteShiftSql = "DELETE FROM EmployeeShifts WHERE shiftID = ? AND status = 'scheduled'";
                try (PreparedStatement psShift = connection.prepareStatement(deleteShiftSql)) {
                    for (int id : shiftIDs) {
                        psShift.setInt(1, id);
                        psShift.addBatch();
                    }
                    psShift.executeBatch();
                }
            }

            String updatePlanSql = "UPDATE MonthlyShiftPlan SET status = ? WHERE planID = ?";
            try (PreparedStatement psPlan = connection.prepareStatement(updatePlanSql)) {
                psPlan.setString(1, MonthlyShiftPlan.CANCELLED);
                psPlan.setInt(2, planID);
                psPlan.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ignore) {
            }
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(originalAuto);
                }
            } catch (SQLException ignore) {
            }
        }
    }

    /**
     * Tìm kế hoạch phân ca của một nhân viên trong một tháng và năm nhất định.
     * 
     * @param employeeID ID nhân viên
     * @param year       Năm cần tìm
     * @param month      Tháng cần tìm
     * @return Đối tượng MonthlyShiftPlan nếu tồn tại, ngược lại trả về null
     */
    public MonthlyShiftPlan findByEmployee(int employeeID, int year, int month) {
        String sql = "SELECT planID, employeeID, templateID, effectiveYear, effectiveMonth, "
                + "       status, createdBy, createdAt, updatedAt "
                + "FROM MonthlyShiftPlan "
                + "WHERE employeeID = ? AND effectiveYear = ? AND effectiveMonth = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapBasic(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private List<MonthlyShiftPlan> runListQuery(String sql, Integer y, Integer m) {
        List<MonthlyShiftPlan> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (y != null && m != null) {
                ps.setInt(1, y);
                ps.setInt(2, m);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MonthlyShiftPlan p = mapBasic(rs);
                    p.setEmployeeName(rs.getString("employeeName"));
                    p.setTemplateName(rs.getString("shiftName"));
                    p.setStartTime(rs.getTime("startTime"));
                    p.setEndTime(rs.getTime("endTime"));
                    out.add(p);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    private MonthlyShiftPlan mapBasic(ResultSet rs) throws SQLException {
        MonthlyShiftPlan p = new MonthlyShiftPlan();
        p.setPlanID(rs.getInt("planID"));
        p.setEmployeeID(rs.getInt("employeeID"));
        p.setTemplateID(rs.getInt("templateID"));
        p.setEffectiveYear(rs.getInt("effectiveYear"));
        p.setEffectiveMonth(rs.getInt("effectiveMonth"));
        p.setStatus(rs.getString("status"));
        p.setCreatedBy(rs.getInt("createdBy"));
        p.setCreatedAt(rs.getTimestamp("createdAt"));
        p.setUpdatedAt(rs.getTimestamp("updatedAt"));
        return p;
    }
}
