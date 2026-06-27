package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.RoutePermission;

/**
 * DAO phục vụ cơ chế phân quyền động (Dynamic RBAC).
 */
public class PermissionDAO extends DBContext {

    /**
     * Lấy toàn bộ permissionKey mà 1 role đang được cấp (qua RolePermission).
     */
    public Set<String> getPermissionKeysByRole(int roleID) {
        Set<String> result = new HashSet<>();
        String sql = "SELECT p.permissionKey "
                + "FROM Permission p "
                + "JOIN RolePermission rp ON p.permissionID = rp.permissionID "
                + "WHERE rp.roleID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, roleID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("permissionKey"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Lấy tất cả luật route → permission, sắp xếp dài → ngắn để prefix cụ thể
     * hơn được kiểm tra trước.
     */
    public List<RoutePermission> getAllRouteRules() {
        List<RoutePermission> list = new ArrayList<>();
        String sql = "SELECT routePrefix, permissionKey FROM RoutePermission "
                + "ORDER BY LENGTH(routePrefix) DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new RoutePermission(
                        rs.getString("routePrefix"),
                        rs.getString("permissionKey")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy các quyền ĐƯỢC CẤP THÊM cho từng nhân viên cụ thể (từ bảng
     * EmployeePermission — không phải quyền của role).
     */
    public Set<String> getExtraPermissionsByEmployee(int employeeID) {
        Set<String> result = new HashSet<>();
        String sql = "SELECT permissionKey FROM EmployeePermission WHERE employeeID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("permissionKey"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Thay toàn bộ quyền của 1 nhân viên (dùng khi lưu form phân quyền). Xóa
     * hết rồi insert lại những quyền được tick.
     */
    public boolean setEmployeePermissions(int employeeID, Set<String> permissionKeys, int grantedBy) {
        String deleteSql = "DELETE FROM EmployeePermission WHERE employeeID = ?";
        String insertSql = "INSERT INTO EmployeePermission (employeeID, permissionKey, grantedBy) VALUES (?, ?, ?)";
        try {
            //bắt đầu Transaction (tắt tự commit)
            connection.setAutoCommit(false);

            try (PreparedStatement del = connection.prepareStatement(deleteSql)) {
                del.setInt(1, employeeID);
                //đánh dấu DELETE
                del.executeUpdate();
            }

            if (permissionKeys != null && !permissionKeys.isEmpty()) {
                try (PreparedStatement ins = connection.prepareStatement(insertSql)) {
                    for (String key : permissionKeys) {
                        ins.setInt(1, employeeID);
                        ins.setString(2, key);
                        ins.setInt(3, grantedBy);
                        //không thực thi SQL vội, đưa vào hàng đợi
                        ins.addBatch();
                    }
                    //đánh dấu INSERT
                    ins.executeBatch();
                }
            }

            //xác nhận và lưu thay đổi xuống DB
            connection.commit();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            try {
                //lỗi hủy toàn bộ thay đổi
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                //bật lại auto commit cho các hàm khác dùng
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Lấy danh sách các permission có thể cấp cho Staff (những permission không
     * phải owner.access và staff.access).
     */
    public List<String[]> getGrantablePermissions() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT permissionKey, description FROM Permission "
                + "WHERE permissionKey NOT IN ('owner.access', 'staff.access') "
                + "ORDER BY permissionKey";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{rs.getString("permissionKey"), rs.getString("description")});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
}
