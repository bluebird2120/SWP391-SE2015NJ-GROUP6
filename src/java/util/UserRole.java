package util;

/**
 * Enum định danh role trong hệ thống.
 * - CUSTOMER: khách hàng (bảng Customer, không có roleID)
 * - RESTAURANT_OWNER: chủ nhà hàng (Employee.roleID = 1)
 * - RESTAURANT_STAFF: nhân viên (Employee.roleID = 2)
 */
public enum UserRole {
    CUSTOMER(0),
    RESTAURANT_OWNER(1),
    RESTAURANT_STAFF(2);

    private final int roleID;

    UserRole(int roleID) {
        this.roleID = roleID;
    }

    public int getRoleID() {
        return roleID;
    }

    /** Map roleID lấy từ DB sang enum. Trả về null nếu không khớp. */
    public static UserRole fromRoleID(int roleID) {
        for (UserRole r : values()) {
            if (r.roleID == roleID) {
                return r;
            }
        }
        return null;
    }

    /** Map tên role (lưu trong bảng Role) sang enum. Trả về null nếu không khớp. */
    public static UserRole fromName(String name) {
        if (name == null) {
            return null;
        }
        String cleanName = name.trim().toUpperCase();
        if (cleanName.equals("OWNER") || cleanName.equals("RESTAURANT_OWNER")) {
            return RESTAURANT_OWNER;
        }
        if (cleanName.equals("STAFF") || cleanName.equals("RESTAURANT_STAFF")) {
            return RESTAURANT_STAFF;
        }
        if (cleanName.equals("CUSTOMER")) {
            return CUSTOMER;
        }
        return null;
    }
}
