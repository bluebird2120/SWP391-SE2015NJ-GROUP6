package dal;

import model.Invoices;
import java.sql.*;

public class InvoicesDAO {

    private Connection getConnection() {
        return new DBContext().getConnection();
    }

    // =========================================================
    // 1. TẠO INVOICE MỚI
    // Trả về invoiceID vừa tạo, hoặc -1 nếu thất bại
    // =========================================================
    public int createInvoice(Invoices invoice) {
        String sql = "INSERT INTO Invoices "
                   + "(invoiceNumber, paymentMethod, subTotal, taxAmount, "
                   + " depositDeducted, finalAmount, issuedDate, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, invoice.getInvoiceNumber());
            ps.setString(2, invoice.getPaymentMethod());
            ps.setLong  (3, invoice.getSubTotal());
            ps.setLong  (4, invoice.getTaxAmount());
            ps.setLong  (5, invoice.getDepositDeducted());
            ps.setLong  (6, invoice.getFinalAmount());
            ps.setDate  (7, invoice.getIssuedDate() != null
                           ? invoice.getIssuedDate()
                           : new Date(System.currentTimeMillis()));
            ps.setString(8, invoice.getStatus() != null
                           ? invoice.getStatus() : "unpaid");

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[InvoicesDAO] createInvoice lỗi: " + e.getMessage());
        }
        return -1;
    }

    // =========================================================
    // 2. LẤY INVOICE THEO invoiceID
    // =========================================================
    public Invoices getInvoiceById(int invoiceID) {
        String sql = "SELECT * FROM Invoices WHERE invoiceID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, invoiceID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapToInvoice(rs);

        } catch (SQLException e) {
            System.err.println("[InvoicesDAO] getInvoiceById lỗi: " + e.getMessage());
        }
        return null;
    }

    // =========================================================
    // 3. CẬP NHẬT invoiceID VÀO ORDER sau khi tạo Invoice
    // =========================================================
    public boolean linkInvoiceToOrder(int invoiceID, int orderID) {
        String sql = "UPDATE `Order` SET invoiceID = ? WHERE orderID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
    private Invoices mapToInvoice(ResultSet rs) throws SQLException {
        return new Invoices(
            rs.getInt   ("invoiceID"),
            rs.getString("invoiceNumber"),
            rs.getString("paymentMethod"),
            rs.getLong  ("subTotal"),
            rs.getLong  ("taxAmount"),
            rs.getLong  ("depositDeducted"),
            rs.getLong  ("finalAmount"),
            rs.getDate  ("issuedDate"),
            rs.getString("status")
        );
    }
    
    // =========================================================
    // 4. CẬP NHẬT TRẠNG THÁI VÀ PHƯƠNG THỨC THANH TOÁN
    // =========================================================
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
}