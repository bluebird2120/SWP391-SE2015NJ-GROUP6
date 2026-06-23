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

public class EmployeeDAO extends DBContext {

    /**
     * Tìm kiếm nhân viên bằng số điện thoại và kiểm chứng mật khẩu.
     * Dùng cho chức năng Đăng nhập hệ thống của nhân viên.
     * 
     * @param phoneNumber Số điện thoại đăng nhập
     * @param rawPassword Mật khẩu chưa mã hóa do người dùng nhập
     * @return Đối tượng Employee tương ứng nếu thông tin chính xác và trùng khớp,
     *         ngược lại trả về null
     * @throws SQLException Nếu phát sinh lỗi trong quá trình truy vấn cơ sở dữ liệu
     */
    public Employee findByPhoneAndPassword(String phoneNumber, String rawPassword)
            throws SQLException {

        String sql = "SELECT e.employeeID, e.roleID, "
                + "       e.fullName, e.phoneNumber, e.email, "
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

                Employee emp = new Employee();
                emp.setEmployeeID(rs.getInt("employeeID"));
                emp.setRoleID(rs.getInt("roleID"));

                emp.setFullName(rs.getString("fullName"));
                emp.setPhoneNumber(rs.getString("phoneNumber"));
                emp.setEmail(rs.getString("email"));
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

    /**
     * Tìm kiếm thông tin nhân viên theo Mã ID định danh.
     * 
     * @param id Mã ID của nhân viên
     * @return Đối tượng Employee chứa đầy đủ thông tin cá nhân, ngược lại trả về
     *         null
     */
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
    /**
     * --
     * Thêm mới tài khoản nhân viên vào cơ sở dữ liệu.
     * Dùng khi Owner tạo mới hồ sơ nhân viên phục vụ/bếp.
     * 
     * @param e Đối tượng Employee chứa thông tin nhân viên cần thêm mới
     * @return Mã ID tự sinh (employeeID) của nhân viên vừa được tạo mới, hoặc -1
     *         nếu thất bại
     */
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

    /**
     * -----
     * Cập nhật thông tin cá nhân của nhân viên (Họ tên, ngày sinh, SĐT, Email, địa
     * chỉ, ảnh đại diện).
     * Không cập nhật mật khẩu tại đây.
     * 
     * @param e Đối tượng Employee chứa dữ liệu cần cập nhật kèm theo ID nhân viên
     * @return true nếu cập nhật thành công ít nhất 1 dòng, ngược lại false
     */
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

    /**
     * ----
     * Khóa tài khoản nhân viên (xóa mềm - Soft Delete).
     * Thiết lập cột isActive = 0 để vô hiệu hóa tài khoản mà không làm mất tính
     * toàn vẹn khóa ngoại (Foreign Key).
     * 
     * @param employeeID Mã ID của nhân viên cần khóa
     * @return true nếu khóa tài khoản thành công, ngược lại false
     */
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

    /**
     * ----
     * Mở khóa/kích hoạt lại tài khoản nhân viên đã bị khóa.
     * Thiết lập cột isActive = 1.
     * 
     * @param employeeID Mã ID của nhân viên cần kích hoạt lại
     * @return true nếu kích hoạt thành công, ngược lại false
     */
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
     * --
     * Đếm tổng số lượng nhân viên thỏa mãn các điều kiện tìm kiếm và lọc trạng
     * thái.
     * Dùng để phục vụ thuật toán phân trang trên danh sách quản lý của Owner.
     * 
     * @param keyword Từ khóa tìm kiếm theo tên, số điện thoại, hoặc email (có thể
     *                null hoặc rỗng)
     * @param status  Trạng thái lọc (1 = đang hoạt động, 0 = bị khóa, null = tất
     *                cả)
     * @return Tổng số lượng nhân viên thỏa mãn điều kiện lọc
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
     * ------
     * Lấy danh sách nhân viên phục vụ/nhà bếp có phân trang kết hợp tìm kiếm và lọc
     * trạng thái.
     * 
     * @param keyword  Từ khóa tìm kiếm theo họ tên, SĐT, hoặc email (có thể null
     *                 hoặc rỗng)
     * @param status   Trạng thái lọc (1 = đang hoạt động, 0 = bị khóa, null = tất
     *                 cả)
     * @param page     Số trang hiện tại cần lấy dữ liệu (bắt đầu từ 1)
     * @param pageSize Số lượng bản ghi tối đa trên một trang
     * @return Danh sách các đối tượng Employee trong trang được yêu cầu
     */
    public List<Employee> listStaffPaged(String keyword, Integer status, int page, int pageSize) {
        List<Employee> list = new ArrayList<>();
        if (page < 1)
            page = 1;
        if (pageSize < 1)
            pageSize = 5;

        StringBuilder sql = new StringBuilder(
                "SELECT employeeID, roleID, password, fullName, dob, phoneNumber, email, "
                        + "isActive, address, image, createdAt, lastPasswordChangedAt, mustChangePassword "
                        + "FROM Employee WHERE roleID = ?");
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword) {
            sql.append(" AND (fullName LIKE ? OR phoneNumber LIKE ? OR email LIKE ?)");
        }
        if (status != null) {
            sql.append(" AND isActive = ?");
        }
        sql.append(" ORDER BY employeeID DESC LIMIT ? OFFSET ?");

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

    /**
     * Kiểm tra xem một địa chỉ Email đã tồn tại trong hệ thống hay chưa (ngoại trừ
     * một nhân viên cụ thể).
     * Dùng để tránh trùng lặp Email khi tạo mới hoặc cập nhật hồ sơ nhân viên khác.
     * 
     * @param email     Địa chỉ email cần kiểm tra
     * @param excludeID Mã ID nhân viên cần loại trừ khi kiểm tra (ví dụ: ID của
     *                  chính nhân viên đang sửa)
     * @return true nếu email đã được sử dụng bởi người khác, ngược lại false
     */
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

    /**
     * Kiểm tra xem Số điện thoại đã tồn tại trong hệ thống hay chưa (ngoại trừ một
     * nhân viên cụ thể).
     * Dùng để kiểm tra trùng lặp số điện thoại khi thêm mới hoặc cập nhật thông tin
     * nhân viên.
     * 
     * @param phone     Số điện thoại cần kiểm tra
     * @param excludeID Mã ID nhân viên cần loại trừ
     * @return true nếu số điện thoại đã tồn tại ở tài khoản khác, ngược lại false
     */
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

    /**
     * Lấy danh sách mã ID của tất cả các Owner đang hoạt động.
     * Dùng để gửi thông báo (Notifications) cho tất cả các chủ cửa hàng khi nhân
     * viên gửi đơn xin nghỉ.
     * 
     * @return Danh sách các ID chủ cửa hàng (Role = Owner)
     */
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
     * Lấy danh sách tất cả các nhân viên phục vụ/nhà bếp (Role = Staff) đang hoạt
     * động.
     * Dùng để hiển thị danh sách cho Owner phân ca làm việc thủ công.
     * 
     * @return Danh sách đối tượng Employee chỉ chứa ID và Họ tên
     */
    public List<Employee> listActiveStaff() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT employeeID, fullName FROM Employee "
                + "WHERE roleID = ? AND isActive = 1 ORDER BY fullName";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, UserRole.RESTAURANT_STAFF.getRoleID());
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

    /**
     * Chuyển đổi một dòng dữ liệu từ ResultSet sang đối tượng Employee.
     * 
     * @param rs Đối tượng ResultSet thu được từ câu lệnh truy vấn CSDL
     * @return Đối tượng Employee chứa dữ liệu của dòng hiện tại
     * @throws SQLException Nếu phát sinh lỗi khi lấy dữ liệu từ ResultSet
     */
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
