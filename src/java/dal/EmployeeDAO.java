package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Employee;

public class EmployeeDAO extends DBContext {

    /**
     * Tìm nhân viên theo số điện thoại + mật khẩu. JOIN bảng Role để lấy
     * roleName, map thẳng vào Employee.roleName.
     *
     * @return Employee (có roleName) nếu đúng, null nếu sai phone/password
     */
    public Employee findByPhoneAndPassword(String phoneNumber, String rawPassword)
            throws SQLException {

        String sql = "SELECT e.employeeID, e.roleID, "
                + "       e.fullName, e.phoneNumber, e.email, "
                + "       e.salary, e.isActive, e.address, e.image, "
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
                if (!storedPassword.equals(rawPassword)) {
                    return null;
                }

                Employee emp = new Employee();
                emp.setEmployeeID(rs.getInt("employeeID"));
                emp.setRoleID(rs.getInt("roleID"));

                emp.setFullName(rs.getString("fullName"));
                emp.setPhoneNumber(rs.getString("phoneNumber"));
                emp.setEmail(rs.getString("email"));
                emp.setSalary(rs.getBigDecimal("salary"));
                emp.setIsActive(rs.getInt("isActive"));
                emp.setAddress(rs.getString("address"));
                emp.setImage(rs.getString("image"));
                emp.setCreatedAt(rs.getTimestamp("createdAt"));
                emp.setLastPasswordChangedAt(rs.getTimestamp("lastPasswordChangedAt"));
                emp.setMustChangePassword(rs.getInt("mustChangePassword"));

                return emp;
            }
        }
    }
}
