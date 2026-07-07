package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import model.ShiftTemplates;

public class ShiftTemplateDAO extends DBContext {

    /**
     * Lấy toàn bộ danh sách các ca làm việc mẫu đang hoạt động trong hệ thống.
     * 
     * @return Danh sách các đối tượng ShiftTemplates, sắp xếp theo giờ bắt đầu tăng dần
     */
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

    /**
     * Tìm kiếm một mẫu ca cụ thể theo ID.
     * 
     * @param id ID của mẫu ca làm việc
     * @return Đối tượng ShiftTemplates nếu tìm thấy, ngược lại trả về null
     */
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

    public boolean isShiftNameExists(String shiftName, int excludeID) {
        if (shiftName == null || shiftName.isBlank()) {
            return false;
        }

        String sql = "SELECT 1 FROM ShiftTemplates "
                + "WHERE LOWER(TRIM(shiftName)) = LOWER(TRIM(?)) "
                + "AND templateID <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, shiftName.trim());
            ps.setInt(2, excludeID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Thêm mới một mẫu ca làm việc vào cơ sở dữ liệu.
     * 
     * @param t Đối tượng ShiftTemplates chứa tên ca, giờ bắt đầu và giờ kết thúc
     * @return ID tự sinh của mẫu ca làm việc vừa được chèn, hoặc -1 nếu thất bại
     */
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

    /**
     * Cập nhật tên của mẫu ca làm việc.
     * Việc đổi tên luôn được cho phép vì không làm ảnh hưởng đến dữ liệu chấm công cũ.
     * 
     * @param templateID ID của mẫu ca làm việc
     * @param newName Tên ca làm việc mới
     * @return true nếu cập nhật thành công, ngược lại false
     */
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

    /**
     * Cập nhật khung giờ làm việc của mẫu ca.
     * Người gọi (Caller) cần đếm số lượng ca đang dùng mẫu này bằng countShiftsUsing trước khi sửa.
     * Chỉ cho phép đổi giờ khi chưa có ca làm việc thực tế nào áp dụng mẫu ca này.
     * 
     * @param templateID ID của mẫu ca làm việc
     * @param start Giờ bắt đầu mới
     * @param end Giờ kết thúc mới
     * @return true nếu cập nhật giờ thành công, ngược lại false
     */
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

    /**
     * Xóa một mẫu ca làm việc khỏi cơ sở dữ liệu.
     * Chỉ cho phép xóa khi mẫu ca chưa từng được sử dụng để xếp lịch cho bất kỳ nhân viên nào.
     * 
     * @param templateID ID của mẫu ca cần xóa
     * @return true nếu xóa thành công, ngược lại false
     */
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

    /**
     * Đếm số lượng ca làm việc thực tế (trong bảng EmployeeShifts) đang liên kết với mẫu ca này.
     * Dùng để kiểm tra trước khi thực hiện hành động xóa hoặc đổi khung giờ.
     * 
     * @param templateID ID của mẫu ca làm việc
     * @return Số lượng ca làm việc thực tế đang áp dụng mẫu này
     */
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

    /**
     * Map một dòng kết quả ResultSet thành đối tượng mẫu ca ShiftTemplates.
     */
    private ShiftTemplates mapRow(ResultSet rs) throws SQLException {
        ShiftTemplates t = new ShiftTemplates();
        t.setTemplateID(rs.getInt("templateID"));
        t.setShiftName(rs.getString("shiftName"));
        t.setStartTime(rs.getTime("startTime"));
        t.setEndTime(rs.getTime("endTime"));
        return t;
    }
}
