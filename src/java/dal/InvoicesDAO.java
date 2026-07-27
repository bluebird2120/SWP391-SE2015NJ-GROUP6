package dal;

import model.Invoices;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO CHO HÓA ĐƠN VÀ HOÀN TẤT THANH TOÁN.
 *
 * <p>Nhóm hàm: CRUD invoice -> cập nhật thanh toán transaction
 * -> danh sách/filter Owner -> thống kê doanh thu.</p>
 */
public class InvoicesDAO {

    private Connection getConnection() {
        return new DBContext().getConnection();
    }

    // ==================== 1. OWNER - INVOICE LIST ====================

    /** Đếm hóa đơn theo filter để tính phân trang màn Owner. */
    public int getTotalFilteredInvoices(String startDate, String endDate,
            String status) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM Invoices WHERE 1=1 ");
        List<Object> parameters = new ArrayList<>();
        appendInvoiceFilters(sql, parameters, startDate, endDate, status);

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParameters(ps, parameters);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("[InvoicesDAO] getTotalFilteredInvoices lỗi: "
                    + e.getMessage());
            return 0;
        }
    }

    /** Lấy một trang hóa đơn theo ngày, trạng thái, offset và limit. */
    public List<Invoices> getFilteredInvoices(String startDate,
            String endDate, String status, int offset, int limit) {
        List<Invoices> invoices = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM Invoices WHERE 1=1 ");
        List<Object> parameters = new ArrayList<>();
        appendInvoiceFilters(sql, parameters, startDate, endDate, status);
        sql.append(" ORDER BY issuedDate DESC LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add(offset);

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParameters(ps, parameters);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    invoices.add(mapToInvoice(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[InvoicesDAO] getFilteredInvoices lỗi: "
                    + e.getMessage());
        }
        return invoices;
    }

    // ==================== 2. OWNER - INVOICE DETAIL ====================

    /** Lấy một invoice theo khóa chính cho màn chi tiết/in. */
    public Invoices getInvoiceById(int invoiceID) {
        String sql = "SELECT * FROM Invoices WHERE invoiceID = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapToInvoice(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("[InvoicesDAO] getInvoiceById lỗi: "
                    + e.getMessage());
            return null;
        }
    }

    // ==================== 3. CHECKOUT - CREATE / LINK INVOICE ====================

    // =========================================================
    // 1. TẠO INVOICE MỚI
    // Trả về invoiceID vừa tạo, hoặc -1 nếu thất bại
    // =========================================================
    /** Tạo invoice và trả về invoiceID được database sinh. */
    public int createInvoice(Invoices invoice) {
        String sql = "INSERT INTO Invoices "
                + "(invoiceNumber, paymentMethod, subTotal, taxAmount, "
                + " depositDeducted, finalAmount, issuedDate, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, invoice.getInvoiceNumber());
            ps.setString(2, invoice.getPaymentMethod());
            ps.setLong(3, invoice.getSubTotal());
            ps.setLong(4, invoice.getTaxAmount());
            ps.setLong(5, invoice.getDepositDeducted());
            ps.setLong(6, invoice.getFinalAmount());
            ps.setDate(7, invoice.getIssuedDate() != null
                    ? invoice.getIssuedDate()
                    : new Date(System.currentTimeMillis()));
            ps.setString(8, invoice.getStatus() != null
                    ? invoice.getStatus() : "unpaid");

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("[InvoicesDAO] createInvoice lỗi: " + e.getMessage());
        }
        return -1;
    }

    // =========================================================
    // 3. CẬP NHẬT invoiceID VÀO ORDER sau khi tạo Invoice
    // =========================================================
    /** Gắn invoice vào order sau khi nhân viên chốt hóa đơn. */
    public boolean linkInvoiceToOrder(int invoiceID, int orderID) {
        String sql = "UPDATE `Order` SET invoiceID = ? WHERE orderID = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, invoiceID);
            ps.setInt(2, orderID);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[InvoicesDAO] linkInvoiceToOrder lỗi: " + e.getMessage());
        }
        return false;
    }

    // =========================================================
    // HELPER: map ResultSet -> Invoices
    // =========================================================
    /** Mapper dùng chung: ResultSet -> Invoices. */
    private Invoices mapToInvoice(ResultSet rs) throws SQLException {
        Timestamp issuedTimestamp = rs.getTimestamp("issuedDate");
        Date issuedDate = issuedTimestamp != null
                ? new Date(issuedTimestamp.getTime()) : null;
        return new Invoices(
                rs.getInt("invoiceID"),
                rs.getString("invoiceNumber"),
                rs.getString("paymentMethod"),
                rs.getLong("subTotal"),
                rs.getLong("taxAmount"),
                rs.getLong("depositDeducted"),
                rs.getLong("finalAmount"),
                issuedDate,
                rs.getString("status")
        );
    }

    /** Dùng chung điều kiện ngày và status cho count/list của Owner. */
    private void appendInvoiceFilters(StringBuilder sql,
            List<Object> parameters, String startDate, String endDate,
            String status) {
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND DATE(issuedDate) >= ? ");
            parameters.add(startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND DATE(issuedDate) <= ? ");
            parameters.add(endDate);
        }
        if (status != null && !status.trim().isEmpty()
                && !"all".equals(status)) {
            sql.append(" AND status = ? ");
            parameters.add(status);
        }
    }

    /** Bind danh sách tham số theo đúng thứ tự đã thêm vào câu SQL. */
    private void bindParameters(PreparedStatement ps, List<Object> parameters)
            throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            ps.setObject(i + 1, parameters.get(i));
        }
    }

    // ==================== 4. PAYMENT - UPDATE STATUS ====================
    /** Cập nhật trạng thái và phương thức thanh toán của invoice. */
    public boolean updateInvoiceStatus(int invoiceID, String status, String paymentMethod) {
        String sql = "UPDATE Invoices SET status = ?, paymentMethod = ? WHERE invoiceID = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, paymentMethod);
            ps.setInt(3, invoiceID);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[InvoicesDAO] updateInvoiceStatus lỗi: " + e.getMessage());
        }
        return false;
    }

    // =========================================================
    // 5. CẬP NHẬT THANH TOÁN THÀNH CÔNG VÀ CHUYỂN BÀN SANG CHỜ DỌN
    // Tích hợp: Tự động thêm dòng tiền vào bảng Payments (Giao dịch an toàn ACID)
    // =========================================================
    /**
     * Transaction hoàn tất thanh toán: ghi Payments, đổi invoice=paid,
     * order=completed và tableStatus=cleaning; lỗi ở bước nào rollback bước đó.
     */
    public boolean updatePaymentSuccessAndCleaningTable(int invoiceID, int orderID, String paymentMethod, long amount, String transactionCode) {
        // 1. Thêm lịch sử giao dịch vào bảng Payments
        String sqlPayment = "INSERT INTO Payments (invoiceID, transactionCode, paymentGateway, amount, status, paidAt) VALUES (?, ?, ?, ?, 'success', NOW())";

        // 2. Hóa đơn thành 'paid' và ghi nhận phương thức thanh toán
        String sqlInvoice = "UPDATE Invoices SET status = 'paid', paymentMethod = ? "
                + "WHERE invoiceID = ? AND status <> 'paid'";

        // 3. Đơn hàng thành 'completed' và Bàn thành 'cleaning' (Chờ dọn dẹp)
        String sqlOrder = "UPDATE `Order` SET orderStatus = 'completed', "
                + "tableStatus = 'cleaning' WHERE orderID = ? AND invoiceID = ? "
                + "AND orderStatus NOT IN ('completed','cancelled')";

        // 4. Lấy employeeID phụ trách đơn này để gửi thông báo
        String sqlGetEmployee = "SELECT employeeID FROM `Order` WHERE orderID = ?";

        // 5. Gửi thông báo cho nhân viên phụ trách
        String sqlNotify = "INSERT INTO Notifications (recipientID, recipientType, type, message, isRead) VALUES (?, 'staff', 'payment_success', ?, 0)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Bật chế độ giao dịch an toàn (Transaction)

            try (PreparedStatement ps0 = conn.prepareStatement(sqlPayment); 
                 PreparedStatement ps1 = conn.prepareStatement(sqlInvoice); 
                 PreparedStatement ps2 = conn.prepareStatement(sqlOrder); 
                 PreparedStatement psGetEmp = conn.prepareStatement(sqlGetEmployee); 
                 PreparedStatement psNotify = conn.prepareStatement(sqlNotify)) {

                // Insert Payment
                ps0.setInt(1, invoiceID);
                ps0.setString(2, transactionCode);
                ps0.setString(3, paymentMethod);
                ps0.setLong(4, amount);
                ps0.executeUpdate();

                // Update Invoice
                ps1.setString(1, paymentMethod);
                ps1.setInt(2, invoiceID);
                // [SECURITY FIX - PAYMENT] Callback/submit lặp không được paid lần hai.
                if (ps1.executeUpdate() != 1) {
                    throw new SQLException("Invoice đã paid hoặc không tồn tại");
                }

                // Update Order
                ps2.setInt(1, orderID);
                ps2.setInt(2, invoiceID);
                // [SECURITY FIX - PAYMENT] Order bắt buộc liên kết đúng invoice.
                if (ps2.executeUpdate() != 1) {
                    throw new SQLException("Order không khớp invoice");
                }

                // Gửi thông báo cho nhân viên phụ trách bàn (nếu có)
                psGetEmp.setInt(1, orderID);
                try (ResultSet rs = psGetEmp.executeQuery()) {
                    if (rs.next()) {
                        Object empObj = rs.getObject("employeeID");
                        if (empObj != null) {
                            int empID = (int) empObj;
                            String msg = "Khách hàng đã thanh toán thành công đơn #" + orderID
                                    + " qua " + paymentMethod.toUpperCase()
                                    + ". Vui lòng dọn dẹp bàn.";
                            psNotify.setInt(1, empID);
                            psNotify.setString(2, msg);
                            psNotify.executeUpdate();
                        }
                    }
                }

                conn.commit(); // Lưu toàn bộ thay đổi cùng lúc
                return true;

            } catch (SQLException e) {
                conn.rollback(); // Hoàn tác toàn bộ nếu 1 trong các câu lệnh bị lỗi
                System.err.println("[InvoicesDAO] updatePaymentSuccessAndCleaningTable lỗi Transaction: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("[InvoicesDAO] updatePaymentSuccessAndCleaningTable lỗi Connection: " + e.getMessage());
        }
        return false;
    }

    // =========================================================
    // LẤY TOÀN BỘ HÓA ĐƠN CHO ADMIN (Sắp xếp mới nhất lên đầu)
    // =========================================================
    public List<Invoices> getAllInvoices() {
        List<Invoices> list = new ArrayList<>();
        String sql = "SELECT * FROM Invoices ORDER BY issuedDate DESC";

        try (java.sql.Connection conn = new dal.DBContext().getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sql); java.sql.ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Invoices inv = new Invoices();
                inv.setInvoiceID(rs.getInt("invoiceID"));
                inv.setInvoiceNumber(rs.getString("invoiceNumber"));
                inv.setSubTotal(rs.getLong("subTotal"));
                inv.setTaxAmount(rs.getLong("taxAmount"));
                inv.setDepositDeducted(rs.getLong("depositDeducted"));
                inv.setFinalAmount(rs.getLong("finalAmount"));
                inv.setIssuedDate(rs.getDate("issuedDate"));
                inv.setStatus(rs.getString("status"));
                list.add(inv);
            }
        } catch (Exception e) {
            System.err.println("[InvoicesDAO] getAllInvoices lỗi: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // THỐNG KÊ: TỔNG DOANH THU HÓA ĐƠN ĐÃ THANH TOÁN THEO KHOẢNG NGÀY
    // =========================================================
    public long getPaidRevenueByDateRange(String startDate, String endDate) {
        String sql = "SELECT COALESCE(SUM(finalAmount), 0) FROM Invoices "
                + "WHERE status = 'paid' AND DATE(issuedDate) BETWEEN ? AND ?";

        try (Connection conn = getConnection()) {
            if (conn == null) {
                return 0;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, startDate);
                ps.setString(2, endDate);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[InvoicesDAO] getPaidRevenueByDateRange lỗi: " + e.getMessage());
        }
        return 0;
    }

    // =========================================================
    // THỐNG KÊ: SỐ HÓA ĐƠN ĐÃ THANH TOÁN THEO KHOẢNG NGÀY
    // =========================================================
    public int countPaidInvoicesByDateRange(String startDate, String endDate) {
        String sql = "SELECT COUNT(*) FROM Invoices "
                + "WHERE status = 'paid' AND DATE(issuedDate) BETWEEN ? AND ?";

        try (Connection conn = getConnection()) {
            if (conn == null) {
                return 0;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, startDate);
                ps.setString(2, endDate);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[InvoicesDAO] countPaidInvoicesByDateRange lỗi: " + e.getMessage());
        }
        return 0;
    }
}
