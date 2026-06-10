package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import model.ShiftTemplates;

/**
 * DAO cho bảng ShiftTemplates.
 * - Block update giờ và delete khi template đã được EmployeeShifts tham chiếu.
 * - updateName an toàn (cosmetic), luôn cho phép.
 */
public class ShiftTemplateDAO extends DBContext {

    public List<ShiftTemplates> findAll() {
        List<ShiftTemplates> list = new ArrayList<>();
        String sql = "SELECT templateID, shiftName, startTime, endTime "
                + "FROM ShiftTemplates ORDER BY startTime";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public ShiftTemplates findById(int id) {
        String sql = "SELECT templateID, shiftName, startTime, endTime "
                + "FROM ShiftTemplates WHERE templateID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /** Caller đã validate endTime > startTime. */
    public int insert(ShiftTemplates t) {
        String sql = "INSERT INTO ShiftTemplates (shiftName, startTime, endTime) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getShiftName());
            ps.setTime(2, t.getStartTime());
            ps.setTime(3, t.getEndTime());
            if (ps.executeUpdate() == 0) return -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    /** Đổi tên: luôn cho phép, không ảnh hưởng lịch sử điểm danh. */
    public boolean updateName(int templateID, String newName) {
        String sql = "UPDATE ShiftTemplates SET shiftName = ? WHERE templateID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, templateID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Đổi giờ: caller phải gọi countShiftsUsing trước, chỉ update khi == 0. */
    public boolean updateTimes(int templateID, Time start, Time end) {
        String sql = "UPDATE ShiftTemplates SET startTime = ?, endTime = ? WHERE templateID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTime(1, start);
            ps.setTime(2, end);
            ps.setInt(3, templateID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Delete: caller phải gọi countShiftsUsing trước, chỉ delete khi == 0. */
    public boolean delete(int templateID) {
        String sql = "DELETE FROM ShiftTemplates WHERE templateID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, templateID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Đếm số EmployeeShifts đang tham chiếu — dùng để chặn update giờ / delete. */
    public int countShiftsUsing(int templateID) {
        String sql = "SELECT COUNT(*) FROM EmployeeShifts WHERE templateID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, templateID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private ShiftTemplates mapRow(ResultSet rs) throws SQLException {
        ShiftTemplates t = new ShiftTemplates();
        t.setTemplateID(rs.getInt("templateID"));
        t.setShiftName(rs.getString("shiftName"));
        t.setStartTime(rs.getTime("startTime"));
        t.setEndTime(rs.getTime("endTime"));
        return t;
    }
}
