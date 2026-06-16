package dal;

import model.MonthlyShiftPlan;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MonthlyShiftPlanDAO extends DBContext {

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
                if (ps.executeUpdate() == 0) return -1;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
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

    public boolean cancelIfPending(int planID) {
        String sql = "UPDATE MonthlyShiftPlan SET status = ? "
                   + "WHERE planID = ? AND status IN ('DRAFT','NOTIFIED')";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, MonthlyShiftPlan.CANCELLED);
            ps.setInt(2, planID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

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
                if (rs.next()) return mapBasic(rs);
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
