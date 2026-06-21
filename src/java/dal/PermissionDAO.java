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
 * Toàn bộ luật phân quyền được lưu trong DB (Permission, RolePermission,
 * RoutePermission) -> muốn đổi quyền chỉ cần sửa dữ liệu, không cần build lại code.
 */
public class PermissionDAO extends DBContext {

    /**
     * Lấy toàn bộ permissionKey mà 1 role đang được cấp.
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
     * Lấy toàn bộ luật route -> permission cần thiết, sắp xếp theo độ dài
     * routePrefix giảm dần để khi so khớp sẽ ưu tiên prefix cụ thể/dài hơn
     * trước (vd '/staff/menu/' được kiểm tra trước '/staff/').
     */
    public List<RoutePermission> getAllRouteRules() {
        List<RoutePermission> list = new ArrayList<>();
        String sql = "SELECT routePrefix, permissionKey FROM RoutePermission "
                + "ORDER BY LENGTH(routePrefix) DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
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
}