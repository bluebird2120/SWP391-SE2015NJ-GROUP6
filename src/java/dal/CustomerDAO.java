package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Customer;

public class CustomerDAO extends DBContext {

    public Customer findByPhoneAndPassword(String phoneNumber, String rawPassword)
            throws SQLException {

        String sql = "SELECT customerID, userName, password, phoneNumber, email, createdAt, loginProvider "
                + "FROM Customer "
                + "WHERE phoneNumber = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, phoneNumber);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                String storedPassword = rs.getString("password") == null ? "" : rs.getString("password");

                if (!storedPassword.equals(rawPassword)) {
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

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerID(rs.getInt("customerID"));
        c.setUserName(rs.getString("userName"));
        c.setPhoneNumber(rs.getString("phoneNumber"));
        c.setEmail(rs.getString("email"));
        c.setCreatedAt(rs.getTimestamp("createdAt"));
        c.setLoginProvider(rs.getString("loginProvider"));
        return c;
    }

    public boolean register(String userName, String phoneNumber,
            String email, String password) throws SQLException {
        String sql = "INSERT INTO Customer (userName, phoneNumber, email, password, loginProvider) "
                + "VALUES (?, ?, ?, ?, 'local')";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userName);
            ps.setString(2, phoneNumber);
            ps.setString(3, email);
            ps.setString(4, password);
            //trả về true / false
            return ps.executeUpdate() > 0;
        }

    }
}
