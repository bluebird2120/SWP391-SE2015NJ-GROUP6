package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Employee;
import util.UserRole;

/**
 * DAO cho bảng Employee. Hỗ trợ login + CRUD staff.
 * roleID: 1 = RESTAURANT_OWNER, 2 = RESTAURANT_STAFF.
 */
public class EmployeeDAO extends DBContext {

    /** Tìm employee theo email (dùng cho login). */
    public Employee findByEmail(String email) {
        String sql = "SELECT employeeID, roleID, password, fullName, dob, phoneNumber, email, "
                + "salary, isActive, address, image, createdAt, lastPasswordChangedAt, mustChangePassword "
                + "FROM Employee WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Employee findById(int id) {
        String sql = "SELECT employeeID, roleID, password, fullName, dob, phoneNumber, email, "
                + "salary, isActive, address, image, createdAt, lastPasswordChangedAt, mustChangePassword "
                + "FROM Employee WHERE employeeID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * List staff (roleID = 2) có search theo tên/email/phone.
     * Owner gọi để hiển thị bảng quản lý.
     */
    public List<Employee> listStaff(String keyword) {
        List<Employee> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT employeeID, roleID, password, fullName, dob, phoneNumber, email, "
                + "salary, isActive, address, image, createdAt, lastPasswordChangedAt, mustChangePassword "
                + "FROM Employee WHERE roleID = ?");
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword) {
            sql.append(" AND (fullName LIKE ? OR email LIKE ? OR phoneNumber LIKE ?)");
        }
        sql.append(" ORDER BY employeeID DESC");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setInt(1, UserRole.RESTAURANT_STAFF.getRoleID());
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(2, like);
                ps.setString(3, like);
                ps.setString(4, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /** Insert staff mới. Trả về employeeID hoặc -1 nếu fail. */
    public int insert(Employee e) {
        String sql = "INSERT INTO Employee "
                + "(roleID, password, fullName, dob, phoneNumber, email, salary, isActive, address, image, mustChangePassword) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getRoleID());
            ps.setString(2, e.getPassword());
            ps.setString(3, e.getFullName());
            if (e.getDob() != null) {
                ps.setDate(4, e.getDob());
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            ps.setString(5, e.getPhoneNumber());
            ps.setString(6, e.getEmail());
            if (e.getSalary() != null) {
                ps.setBigDecimal(7, e.getSalary());
            } else {
                ps.setNull(7, java.sql.Types.DECIMAL);
            }
            ps.setInt(8, e.getIsActive());
            ps.setString(9, e.getAddress());
            ps.setString(10, e.getImage());
            ps.setInt(11, e.getMustChangePassword());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                return -1;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    /** Update staff (không update password ở đây). */
    public boolean update(Employee e) {
        String sql = "UPDATE Employee SET fullName = ?, dob = ?, phoneNumber = ?, email = ?, "
                + "salary = ?, isActive = ?, address = ?, image = ? WHERE employeeID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getFullName());
            if (e.getDob() != null) {
                ps.setDate(2, e.getDob());
            } else {
                ps.setNull(2, java.sql.Types.DATE);
            }
            ps.setString(3, e.getPhoneNumber());
            ps.setString(4, e.getEmail());
            if (e.getSalary() != null) {
                ps.setBigDecimal(5, e.getSalary());
            } else {
                ps.setNull(5, java.sql.Types.DECIMAL);
            }
            ps.setInt(6, e.getIsActive());
            ps.setString(7, e.getAddress());
            ps.setString(8, e.getImage());
            ps.setInt(9, e.getEmployeeID());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Soft delete: set isActive = 0. Tránh xoá cứng vì có FK từ EmployeeShifts/Feedback... */
    public boolean softDelete(int employeeID) {
        String sql = "UPDATE Employee SET isActive = 0 WHERE employeeID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Reactivate: set isActive = 1. */
    public boolean reactivate(int employeeID) {
        String sql = "UPDATE Employee SET isActive = 1 WHERE employeeID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Đếm tổng số staff thoả filter.
     * keyword: search theo fullName/phoneNumber/email (có thể null/blank).
     * status: 1 = active, 0 = inactive, null = tất cả.
     */
    public int countStaff(String keyword, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Employee WHERE roleID = ?");
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword) {
            sql.append(" AND (fullName LIKE ? OR phoneNumber LIKE ? OR email LIKE ?)");
        }
        if (status != null) {
            sql.append(" AND isActive = ?");
        }
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, UserRole.RESTAURANT_STAFF.getRoleID());
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            if (status != null) {
                ps.setInt(idx++, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy danh sách staff có phân trang + filter.
     * page bắt đầu từ 1.
     */
    public List<Employee> listStaffPaged(String keyword, Integer status, int page, int pageSize) {
        List<Employee> list = new ArrayList<>();
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 5;

        StringBuilder sql = new StringBuilder(
                "SELECT employeeID, roleID, password, fullName, dob, phoneNumber, email, "
                + "salary, isActive, address, image, createdAt, lastPasswordChangedAt, mustChangePassword "
                + "FROM Employee WHERE roleID = ?");
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword) {
            sql.append(" AND (fullName LIKE ? OR phoneNumber LIKE ? OR email LIKE ?)");
        }
        if (status != null) {
            sql.append(" AND isActive = ?");
        }
        sql.append(" ORDER BY employeeID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, UserRole.RESTAURANT_STAFF.getRoleID());
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            if (status != null) {
                ps.setInt(idx++, status);
            }
            ps.setInt(idx++, (page - 1) * pageSize);
            ps.setInt(idx++, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public boolean updatePassword(int employeeID, String newHashedPassword) {
        String sql = "UPDATE Employee SET password = ?, lastPasswordChangedAt = ?, mustChangePassword = 0 "
                + "WHERE employeeID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newHashedPassword);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, employeeID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean isEmailExists(String email, int excludeID) {
        String sql = "SELECT 1 FROM Employee WHERE email = ? AND employeeID <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean isPhoneExists(String phone, int excludeID) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM Employee WHERE phoneNumber = ? AND employeeID <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setInt(2, excludeID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmployeeID(rs.getInt("employeeID"));
        e.setRoleID(rs.getInt("roleID"));
        e.setPassword(rs.getString("password"));
        e.setFullName(rs.getString("fullName"));
        e.setDob(rs.getDate("dob"));
        e.setPhoneNumber(rs.getString("phoneNumber"));
        e.setEmail(rs.getString("email"));
        e.setSalary(rs.getBigDecimal("salary"));
        e.setIsActive(rs.getInt("isActive"));
        e.setAddress(rs.getString("address"));
        e.setImage(rs.getString("image"));
        e.setCreatedAt(rs.getTimestamp("createdAt"));
        e.setLastPasswordChangedAt(rs.getTimestamp("lastPasswordChangedAt"));
        e.setMustChangePassword(rs.getInt("mustChangePassword"));
        return e;
    }
}
