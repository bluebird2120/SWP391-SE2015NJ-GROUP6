package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import model.Employee;
import util.UserRole;

public class EmployeeDAO extends DBContext {

    public Employee findByPhoneAndPassword(String phoneNumber, String rawPassword)
            throws SQLException {

        String sql = "SELECT e.employeeID, e.roleID, "
                + "       e.fullName, e.dob, e.phoneNumber, e.email, "
                + "       e.isActive, e.address, e.image, "
                + "       e.createdAt, e.lastPasswordChangedAt, "
                + "       e.mustChangePassword, e.password "
                + "FROM Employee e "
                + "WHERE e.phoneNumber = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, phoneNumber);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                String storedPassword = rs.getString("password");
                if (!util.PasswordUtil.verify(rawPassword, storedPassword)) {
                    return null;
                }
                return mapRow(rs);
            }
        }
    }

    public Employee findByEmail(String email) {


        String sql = "SELECT employeeID, roleID, password, fullName, dob, phoneNumber, email, "
                + "isActive, address, image, createdAt, lastPasswordChangedAt, mustChangePassword "
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
                + "isActive, address, image, createdAt, lastPasswordChangedAt, mustChangePassword "
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


    public List<Employee> listStaff(String keyword) {
        List<Employee> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT employeeID, roleID, password, fullName, dob, phoneNumber, email, "
                + "isActive, address, image, createdAt, lastPasswordChangedAt, mustChangePassword "
                + "FROM Employee WHERE roleID IN (?,?)");
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword) {
            sql.append(" AND (fullName LIKE ? OR email LIKE ? OR phoneNumber LIKE ?)");
        }
        sql.append(" ORDER BY employeeID DESC");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setInt(1, UserRole.RESTAURANT_STAFF.getRoleID());

            ps.setInt(2, UserRole.RECEPTIONIST.getRoleID());
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(3, like);
                ps.setString(4, like);
                ps.setString(5, like);
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


    public int insert(Employee e) {


        String sql = "INSERT INTO Employee "
                + "(roleID, password, fullName, dob, phoneNumber, email, isActive, address, image, mustChangePassword) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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


            ps.setInt(7, e.getIsActive());
            ps.setString(8, e.getAddress());

            ps.setString(9, e.getImage());

            ps.setInt(10, e.getMustChangePassword());

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


    public boolean update(Employee e) {


        String sql = "UPDATE Employee SET fullName = ?, dob = ?, phoneNumber = ?, email = ?, "
                + "isActive = ?, address = ?, image = ? WHERE employeeID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, e.getFullName());
            if (e.getDob() != null) {
                ps.setDate(2, e.getDob());
            } else {
                ps.setNull(2, java.sql.Types.DATE);
            }
            ps.setString(3, e.getPhoneNumber());
            ps.setString(4, e.getEmail());

            ps.setInt(5, e.getIsActive());
            ps.setString(6, e.getAddress());
            ps.setString(7, e.getImage());

            ps.setInt(8, e.getEmployeeID());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }


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


    public int countStaff(String keyword, Integer status, Integer roleID) {

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM Employee WHERE roleID IN (?,?)");
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword) {

            sql.append(" AND (fullName LIKE ? OR phoneNumber LIKE ? OR email LIKE ?)");
        }
        if (status != null) {

            sql.append(" AND isActive = ?");
        }

        if (roleID != null) {
            sql.append(" AND roleID = ?");
        }
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;

            ps.setInt(idx++, UserRole.RESTAURANT_STAFF.getRoleID());


            ps.setInt(idx++, UserRole.RECEPTIONIST.getRoleID());
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";

                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            if (status != null) {
                ps.setInt(idx++, status);
            }
            if (roleID != null) {
                ps.setInt(idx++, roleID);
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


    public List<Employee> listStaffPaged(
            String keyword, Integer status, Integer roleID,
            int page, int pageSize) {

        List<Employee> list = new ArrayList<>();
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 5;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT employeeID, roleID, password, fullName, dob, phoneNumber, email, "
                + "isActive, address, image, createdAt, lastPasswordChangedAt, mustChangePassword "
                + "FROM Employee WHERE roleID IN (?,?)");
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword) {

            sql.append(" AND (fullName LIKE ? OR phoneNumber LIKE ? OR email LIKE ?)");
        }
        if (status != null) {
            sql.append(" AND isActive = ?");
        }

        if (roleID != null) {
            sql.append(" AND roleID = ?");
        }
        sql.append(" ORDER BY employeeID DESC LIMIT ? OFFSET ?");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;

            ps.setInt(idx++, UserRole.RESTAURANT_STAFF.getRoleID());


            ps.setInt(idx++, UserRole.RECEPTIONIST.getRoleID());
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";

                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            if (status != null) {
                ps.setInt(idx++, status);
            }
            if (roleID != null) {
                ps.setInt(idx++, roleID);
            }

            ps.setInt(idx++, pageSize);

            ps.setInt(idx++, (page - 1) * pageSize);

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
        String sql = "SELECT 1 FROM Employee WHERE phoneNumber = ? AND employeeID <> ? "
                + "UNION "
                + "SELECT 1 FROM Customer WHERE phoneNumber = ? "
                + "LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, phone);

            ps.setInt(2, excludeID);
            ps.setString(3, phone);
            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean checkCurrentPassword(int employeeID, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return false;
        }
        String sql = "SELECT password FROM Employee WHERE employeeID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password") == null ? "" : rs.getString("password");
                    return util.PasswordUtil.verify(rawPassword, storedPassword);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }


    public List<Integer> getActiveOwnerIDs() {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT employeeID FROM Employee WHERE roleID = ? AND isActive = 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, UserRole.RESTAURANT_OWNER.getRoleID());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("employeeID"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy danh sách ID của các Lễ tân (RECEPTIONIST) active để gửi notification.
     */
    public List<Integer> getActiveReceptionistIDs() {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT employeeID FROM Employee WHERE roleID = ? AND isActive = 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, UserRole.RECEPTIONIST.getRoleID());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("employeeID"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Employee> listActiveStaff() {

        List<Employee> list = new ArrayList<>();
        String sql = "SELECT employeeID, fullName FROM Employee "
                + "WHERE roleID IN (?,?) AND isActive = 1 ORDER BY fullName";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, UserRole.RESTAURANT_STAFF.getRoleID());

            ps.setInt(2, UserRole.RECEPTIONIST.getRoleID());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    Employee e = new Employee();
                    e.setEmployeeID(rs.getInt("employeeID"));
                    e.setFullName(rs.getString("fullName"));
                    list.add(e);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Employee> listAvailableStaffByDate(Date workDate, int excludeEmployeeID) {

        List<Employee> list = new ArrayList<>();

        String sql = "SELECT e.employeeID, e.fullName "
                + "FROM Employee e "
                + "WHERE e.roleID = ? AND e.isActive = 1 AND e.employeeID <> ? "
                + "AND NOT EXISTS ("
                + "    SELECT 1 FROM EmployeeShifts es "
                + "    WHERE es.employeeID = e.employeeID AND es.workDate = ?"
                + ") "
                + "ORDER BY e.fullName";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, UserRole.RESTAURANT_STAFF.getRoleID());

            ps.setInt(2, excludeEmployeeID);

            ps.setDate(3, workDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    Employee e = new Employee();
                    e.setEmployeeID(rs.getInt("employeeID"));
                    e.setFullName(rs.getString("fullName"));
                    list.add(e);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public boolean isActiveStaff(int employeeID) {

        String sql = "SELECT 1 FROM Employee WHERE employeeID = ? AND roleID = ? AND isActive = 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, employeeID);

            ps.setInt(2, UserRole.RESTAURANT_STAFF.getRoleID());
            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public int getLeastBusyAndLongestIdleStaffID() {
        String sql = "SELECT e.employeeID, "
                + "COUNT(CASE WHEN o.orderStatus NOT IN ('completed', 'cancelled') THEN 1 END) AS activeOrders, "
                + "COUNT(CASE WHEN o.orderStatus = 'completed' AND DATE(o.createdAt) = CURDATE() THEN 1 END) AS completedOrdersToday "
                + "FROM Employee e "
                + "LEFT JOIN `Order` o ON e.employeeID = o.employeeID "
                + "WHERE e.roleID = ? AND e.isActive = 1 "
                + "GROUP BY e.employeeID, e.fullName "
                + "ORDER BY activeOrders ASC, completedOrdersToday ASC "
                + "LIMIT 1";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, UserRole.RESTAURANT_STAFF.getRoleID());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("employeeID");
                }
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] getLeastBusyAndLongestIdleStaffID lỗi: " + e.getMessage());
        }
        return -1;
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


        e.setIsActive(rs.getInt("isActive"));

        e.setAddress(rs.getString("address"));

        e.setImage(rs.getString("image"));

        e.setCreatedAt(rs.getTimestamp("createdAt"));

        e.setLastPasswordChangedAt(rs.getTimestamp("lastPasswordChangedAt"));

        e.setMustChangePassword(rs.getInt("mustChangePassword"));

        return e;
    }
}