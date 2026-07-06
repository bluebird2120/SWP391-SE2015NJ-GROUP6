package dal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBContext {

    protected Connection connection;
    protected ResultSet resultSet;
    protected PreparedStatement statement;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";
    private static final String URL = "jdbc:mysql://localhost:3306/Restaurant_Reservation_and_Table_Service_System"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&useUnicode=true"
            + "&characterEncoding=UTF-8"
            + "&serverTimezone=Asia/Ho_Chi_Minh";

    public DBContext() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = managedConnection();

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
            connection = unavailableConnection(ex);
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

    public boolean isConnectionAvailable() {
        try (Connection ignored = openConnection()) {
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    private Connection managedConnection() {
        InvocationHandler handler = new InvocationHandler() {
            private Connection transactionConnection;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                switch (name) {
                    case "prepareStatement":
                    case "createStatement":
                    case "prepareCall":
                        return createAutoClosingStatement(method, args);
                    case "getAutoCommit":
                        return transactionConnection == null ? true : transactionConnection.getAutoCommit();
                    case "setAutoCommit":
                        boolean autoCommit = (Boolean) args[0];
                        if (!autoCommit) {
                            if (transactionConnection == null || transactionConnection.isClosed()) {
                                transactionConnection = openConnection();
                            }
                            transactionConnection.setAutoCommit(false);
                        } else if (transactionConnection != null) {
                            transactionConnection.setAutoCommit(true);
                            transactionConnection.close();
                            transactionConnection = null;
                        }
                        return null;
                    case "commit":
                        if (transactionConnection != null) {
                            transactionConnection.commit();
                            transactionConnection.close();
                            transactionConnection = null;
                        }
                        return null;
                    case "rollback":
                        if (transactionConnection != null) {
                            transactionConnection.rollback();
                            transactionConnection.close();
                            transactionConnection = null;
                        }
                        return null;
                    case "close":
                        if (transactionConnection != null) {
                            transactionConnection.close();
                            transactionConnection = null;
                        }
                        return null;
                    case "isClosed":
                        return transactionConnection != null && transactionConnection.isClosed();
                    case "isValid":
                        return isConnectionAvailable();
                    case "toString":
                        return "Managed auto-closing database connection";
                    case "unwrap":
                        return null;
                    case "isWrapperFor":
                        return false;
                    default:
                        return invokeOnConnection(method, args);
                }
            }

            private Object createAutoClosingStatement(Method method, Object[] args) throws Throwable {
                if (transactionConnection != null) {
                    return DBContext.invoke(method, transactionConnection, args);
                }
                Connection statementConnection = openConnection();
                try {
                    Object stmt = DBContext.invoke(method, statementConnection, args);
                    return autoClosingStatement(stmt, statementConnection, keepsGeneratedKeys(args));
                } catch (Throwable ex) {
                    statementConnection.close();
                    throw ex;
                }
            }

            private Object invokeOnConnection(Method method, Object[] args) throws Throwable {
                try (Connection conn = openConnection()) {
                    return DBContext.invoke(method, conn, args);
                }
            }
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler);
    }

    private boolean keepsGeneratedKeys(Object[] args) {
        if (args == null) {
            return false;
        }
        for (Object arg : args) {
            if (arg instanceof Integer && ((Integer) arg) == Statement.RETURN_GENERATED_KEYS) {
                return true;
            }
            if (arg instanceof int[] || arg instanceof String[]) {
                return true;
            }
        }
        return false;
    }

    private Object autoClosingStatement(Object stmt, Connection ownerConnection, boolean keepsGeneratedKeys) {
        Class<?> iface;
        if (stmt instanceof CallableStatement) {
            iface = CallableStatement.class;
        } else if (stmt instanceof PreparedStatement) {
            iface = PreparedStatement.class;
        } else {
            iface = Statement.class;
        }
        InvocationHandler handler = (Object proxy, Method method, Object[] args) -> {
            String name = method.getName();
            switch (name) {
                case "executeQuery": {
                    ResultSet rs = (ResultSet) invoke(method, stmt, args);
                    return autoClosingResultSet(rs, stmt, ownerConnection);
                }
                case "getGeneratedKeys": {
                    ResultSet rs = (ResultSet) invoke(method, stmt, args);
                    return autoClosingResultSet(rs, stmt, ownerConnection);
                }
                case "executeUpdate":
                case "executeBatch": {
                    Object result = invoke(method, stmt, args);
                    if (!keepsGeneratedKeys) {
                        closeStatementAndConnection(stmt, ownerConnection);
                    }
                    return result;
                }
                case "execute": {
                    Object result = invoke(method, stmt, args);
                    if (Boolean.FALSE.equals(result) && !keepsGeneratedKeys) {
                        closeStatementAndConnection(stmt, ownerConnection);
                    }
                    return result;
                }
                case "close":
                    closeStatementAndConnection(stmt, ownerConnection);
                    return null;
                default:
                    return invoke(method, stmt, args);
            }
        };
        return Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface}, handler);
    }

    private Object autoClosingResultSet(ResultSet rs, Object stmt, Connection ownerConnection) {
        InvocationHandler handler = (Object proxy, Method method, Object[] args) -> {
            String name = method.getName();
            if ("next".equals(name)) {
                boolean hasNext = (Boolean) invoke(method, rs, args);
                if (!hasNext) {
                    closeResultSetStatementAndConnection(rs, stmt, ownerConnection);
                }
                return hasNext;
            }
            if ("close".equals(name)) {
                closeResultSetStatementAndConnection(rs, stmt, ownerConnection);
                return null;
            }
            return invoke(method, rs, args);
        };
        return Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                handler);
    }

    private void closeResultSetStatementAndConnection(ResultSet rs, Object stmt, Connection ownerConnection)
            throws SQLException {
        try {
            if (!rs.isClosed()) {
                rs.close();
            }
        } finally {
            closeStatementAndConnection(stmt, ownerConnection);
        }
    }

    private void closeStatementAndConnection(Object stmt, Connection ownerConnection) throws SQLException {
        try {
            if (stmt instanceof Statement && !((Statement) stmt).isClosed()) {
                ((Statement) stmt).close();
            }
        } finally {
            if (!ownerConnection.isClosed()) {
                ownerConnection.close();
            }
        }
    }

    private static Object invoke(Method method, Object target, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    private Connection unavailableConnection(Exception cause) {
        InvocationHandler handler = (Object proxy, Method method, Object[] args) -> {
            String name = method.getName();
            switch (name) {
                case "close":
                    return null;
                case "isClosed":
                    return true;
                case "isValid":
                    return false;
                case "getAutoCommit":
                    return true;
                case "setAutoCommit":
                case "commit":
                case "rollback":
                    throw new SQLException("Không thể kết nối database.", cause);
                case "toString":
                    return "Unavailable database connection";
                case "unwrap":
                    return null;
                case "isWrapperFor":
                    return false;
                default:
                    throw new SQLException("Không thể kết nối database.", cause);
            }
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler);
    }

    public static void main(String[] args) {
        DBContext db = new DBContext();
        if (db.isConnectionAvailable()) {
            System.out.println("✅ Thành công! Kết nối MySQL hoạt động: " + db.connection);
        } else {
            System.out.println("❌ Thất bại! Không thể kết nối MySQL.");
        }
    }
}
