package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Customer;

public class CustomerDAO extends DBContext {

    /**
     * Login customer bằng phone + password
     */
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

                String storedPassword = rs.getString("password");

                if (!storedPassword.equals(rawPassword)) {
                    return null;
                }

                Customer c = new Customer();

                c.setCustomerID(rs.getInt("customerID"));
                c.setUserName(rs.getString("userName"));
                c.setPassword(rs.getString("password")); // optional (thường KHÔNG set)
                c.setPhoneNumber(rs.getString("phoneNumber"));
                c.setEmail(rs.getString("email"));
                c.setCreatedAt(rs.getTimestamp("createdAt"));
                c.setLoginProvider(rs.getString("loginProvider"));

                return c;
            }
        }
    }
}