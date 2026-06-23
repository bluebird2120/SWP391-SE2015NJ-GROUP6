package filter;

import dal.PermissionDAO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.RoutePermission;

/**
 * Cache trong RAM cho dữ liệu phân quyền (route rules + quyền theo role).
 *
 * Vì AuthenticationFilter chạy ở MỌI request vào /customer, /staff, /owner,
 * nếu query DB mỗi request sẽ rất tốn -> cache lại, tự refresh sau mỗi
 * TTL_MS để vẫn phản ánh được thay đổi dữ liệu (vd Admin vừa sửa quyền
 * trong DB) mà không cần restart server.
 */
final class PermissionCache {

    private static final long TTL_MS = 30_000; // 30 giây

    private static final PermissionDAO dao = new PermissionDAO();
    //Cache
    private static volatile List<RoutePermission> routeRules = Collections.emptyList();
    //lưu thời điểm cuối cùng cache được cập nhật
    private static volatile long lastRouteRefresh = 0L;

    //ConcurrentHashMap giúp chạy đa luồng và chạy cực nhanh không bắt các luồng phải xếp hàng chờ nhau
    private static final Map<Integer, Set<String>> rolePermissionCache = new ConcurrentHashMap<>();
    private static volatile long lastRoleRefresh = 0L;

    private PermissionCache() {
    }

    /** Danh sách luật route -> permission cần thiết (đã sort dài -> ngắn). */
    static synchronized List<RoutePermission> getRouteRules() {
        long now = System.currentTimeMillis();
        if (now - lastRouteRefresh > TTL_MS) {
            routeRules = dao.getAllRouteRules();
            lastRouteRefresh = now;
        }
        return routeRules;
    }

    /** Set các permissionKey mà 1 roleID đang có. */
    static Set<String> getPermissionsForRole(int roleID) {
        long now = System.currentTimeMillis();
        if (now - lastRoleRefresh > TTL_MS) {
            //Ko clear sẽ bốc dữ liệu cũ trong map
            rolePermissionCache.clear();
            lastRoleRefresh = now;
        }
        //Nếu Map trống thì gọi getPermissionKeysByRole rồi truyền roleID vào gọi DB
        return rolePermissionCache.computeIfAbsent(roleID, id -> dao.getPermissionKeysByRole(id));
    }
}