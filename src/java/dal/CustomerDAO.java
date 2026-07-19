package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Customer;

public class CustomerDAO extends DBContext {

    public Customer findByPhoneAndPassword(String phoneNumber, String rawPassword)
            throws SQLException {

        String sql = "SELECT customerID, userName, password, phoneNumber, email, createdAt, loginProvider, isActive, "
                + "dob, address, image "
                + "FROM Customer "
                + "WHERE phoneNumber = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, phoneNumber);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                String storedPassword = rs.getString("password") == null ? "" : rs.getString("password");

                if (!util.PasswordUtil.verify(rawPassword, storedPassword)) {
                    return null;
                }

                return mapRow(rs);
            }
        }
    }

    public boolean isPhoneExists(String phone, int excludeID) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM Customer WHERE phoneNumber = ? AND customerID <> ?";
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

    public boolean isEmailExists(String email, int excludeID) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM Customer WHERE email = ? AND customerID <> ?";
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

    public boolean isUserNameExists(String userName, int excludeID) {
        if (userName == null || userName.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM Customer WHERE userName = ? AND customerID <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userName);
            ps.setInt(2, excludeID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean checkCurrentPassword(int customerID, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return false;
        }
        String sql = "SELECT password FROM Customer WHERE customerID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerID);
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

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerID(rs.getInt("customerID"));
        c.setUserName(rs.getString("userName"));
        c.setPhoneNumber(rs.getString("phoneNumber"));
        c.setEmail(rs.getString("email"));
        c.setCreatedAt(rs.getTimestamp("createdAt"));
        c.setLoginProvider(rs.getString("loginProvider"));
        c.setIsActive(rs.getInt("isActive"));
        c.setDob(rs.getDate("dob"));
        c.setAddress(rs.getString("address"));
        c.setImage(rs.getString("image"));
        return c;
    }

    public boolean registerVerified(String userName, String phoneNumber,
            String email, String hashedPassword) throws SQLException {
        String sql = "INSERT INTO Customer (userName, phoneNumber, email, password, loginProvider, isActive) "
                + "VALUES (?, ?, ?, ?, 'local', 1)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userName);
            ps.setString(2, phoneNumber);
            ps.setString(3, email);
            ps.setString(4, hashedPassword);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Tìm Customer theo customerID
     */
    public Customer findByID(int id) throws SQLException {
        String sql = "SELECT customerID, userName, password, phoneNumber, email, createdAt, loginProvider, isActive, "
                + "dob, address, image "
                + "FROM Customer WHERE customerID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm Customer theo email
     */
    public Customer findByEmail(String email) throws SQLException {
        String sql = "SELECT customerID, userName, password, phoneNumber, email, createdAt, loginProvider, isActive, "
                + "dob, address, image "
                + "FROM Customer WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tạo userName không trùng
     */
    private String generateUniqueUserName(String base) throws SQLException {
        String candidate = base;
        int attempt = 0;
        while (isUserNameExists(candidate, 0)) {
            attempt++;
            candidate = base + attempt;
        }
        return candidate;
    }

    public Customer findOrCreateByGoogle(String email, String fullName)
            throws SQLException {

        Customer existing = findByEmail(email);

        if (existing != null) {

            //Nếu account là local thì KHÔNG cho login Google
            if ("local".equalsIgnoreCase(existing.getLoginProvider())) {
                return null; // hoặc throw Exception
            }

            // Nếu đã là google account → cho login
            return existing;
        }

        // Chưa có → tạo mới Google account 
        //Thay thế bất kì kí tự nào ko phải a-zA-Z0-9_ thành _
        String baseUserName = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
        String userName = generateUniqueUserName(baseUserName);

        String sql = "INSERT INTO Customer (userName, email, password, loginProvider, isActive) "
                + "VALUES (?, ?, NULL, 'google', 1)";

        try (PreparedStatement ps = connection.prepareStatement(sql,
                java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, userName);
            ps.setString(2, email);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return findByID(keys.getInt(1));
                }
            }
        }

        return null;
    }

    public List<Customer> getCustomerList(String search, String loginProvider, int page, int pageSize)
            throws SQLException {

        List<Customer> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT customerID, userName, password, phoneNumber, email, isActive, createdAt, loginProvider ");
        sql.append("FROM Customer ");
        sql.append("WHERE 1 = 1 ");

        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (userName LIKE ? OR phoneNumber LIKE ? OR email LIKE ?) ");
            String keyword = "%" + search.trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        if (loginProvider != null
                && !loginProvider.trim().isEmpty()
                && !"all".equalsIgnoreCase(loginProvider)) {
            sql.append("AND loginProvider = ? ");
            params.add(loginProvider.trim());
        }

        sql.append("ORDER BY createdAt DESC ");
        sql.append("LIMIT ? OFFSET ? ");

        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    public int countCustomerList(String search, String loginProvider)
            throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ");
        sql.append("FROM Customer ");
        sql.append("WHERE 1 = 1 ");

        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (userName LIKE ? OR phoneNumber LIKE ? OR email LIKE ?) ");
            String keyword = "%" + search.trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        if (loginProvider != null
                && !loginProvider.trim().isEmpty()
                && !"all".equalsIgnoreCase(loginProvider)) {
            sql.append("AND loginProvider = ? ");
            params.add(loginProvider.trim());
        }

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    /**
     * Khóa hoặc mở khóa tài khoản khách hàng.
     */
    public boolean updateActiveStatus(int customerID, int isActive)
            throws SQLException {
        String sql = "UPDATE Customer SET isActive = ? WHERE customerID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, isActive);
            ps.setInt(2, customerID);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int customerID, String newHashedPassword)
            throws SQLException {
        String sql = "UPDATE Customer SET password = ? WHERE customerID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newHashedPassword);
            ps.setInt(2, customerID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updateProfile(Customer customer) {
        String sql = "UPDATE Customer SET userName = ?, dob = ?, address = ?, image = ? "
                + "WHERE customerID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customer.getUserName());
            if (customer.getDob() != null) {
                ps.setDate(2, customer.getDob());
            } else {
                ps.setNull(2, java.sql.Types.DATE);
            }
            ps.setString(3, customer.getAddress());
            ps.setString(4, customer.getImage());
            ps.setInt(5, customer.getCustomerID());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
