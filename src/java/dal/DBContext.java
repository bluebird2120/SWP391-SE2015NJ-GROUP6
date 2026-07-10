package dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBContext {

    protected Connection connection;
    protected ResultSet resultSet;
    protected PreparedStatement statement;

    public DBContext() {
        try {
            String username = "root";
            String password = "123456";

            String url = "jdbc:mysql://localhost:3306/Restaurant_Reservation_and_Table_Service_System"
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&useUnicode=true"
                    + "&characterEncoding=UTF-8"
                    + "&serverTimezone=Asia/Ho_Chi_Minh";

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);

        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeResources() {
        try {
            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
            }
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public static void main(String[] args) {
        DBContext db = new DBContext();
        if (db.connection != null) {
            System.out.println("✅ Thành công! Kết nối MySQL hoạt động: " + db.connection);
        } else {
            System.out.println("❌ Thất bại! Biến connection vẫn bị null.");
        }
    }
}
