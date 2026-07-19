package controller;

import dal.InvoicesDAO;
import dal.OrderDAO;
import model.Invoices;
import model.MenuItem;
import model.Order;
import model.OrderItem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "PaymentInfoController", urlPatterns = {"/payment-info"})
public class PaymentInfoController extends HttpServlet {

    private final InvoicesDAO invoicesDAO = new InvoicesDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        
        // 1. Đọc invoiceID từ URL parameter (?invoiceID=...)
        String invoiceIDParam = request.getParameter("invoiceID");
        Integer invoiceID = null;

        if (invoiceIDParam != null && !invoiceIDParam.isEmpty()) {
            invoiceID = Integer.parseInt(invoiceIDParam);
        } else {
            invoiceID = (Integer) session.getAttribute("invoiceID");
        }

        // 2. Chốt chặn an toàn: Nếu cả URL và Session đều không có ID -> Đẩy về giỏ hàng
        if (invoiceID == null) {
            response.sendRedirect(request.getContextPath() + "/order?action=cart");
            return;
        }

        // 3. Tải thông tin Hóa đơn từ DB
        Invoices invoice = invoicesDAO.getInvoiceById(invoiceID);
        if (invoice == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        // 4. Tìm kiếm Đơn hàng tương ứng bằng cách dò ngược invoiceID dưới DB
        Order order = getOrderByInvoiceIdFromDB(invoiceID);

        // 5. Tải danh sách món ăn đã dùng dựa trên đơn hàng tìm được
        List<OrderItem> orderItems = new ArrayList<>();
        List<MenuItem> menuItems = new ArrayList<>();
        
        if (order != null) {
            orderItems = orderDAO.getOrderItemsByOrderId(order.getOrderID());
            menuItems = orderDAO.getMenuItemsByOrderId(order.getOrderID());
        }

        // 6. Truyền toàn bộ dữ liệu sang trang giao dịch
        request.setAttribute("invoice", invoice);
        request.setAttribute("order", order);
        request.setAttribute("orderItems", orderItems);
        request.setAttribute("menuItems", menuItems);
        
        // Phân biệt cọc và thanh toán khi ăn xong 
        request.setAttribute("isDepositPayment",
                invoice.getInvoiceNumber() != null
                && invoice.getInvoiceNumber().startsWith("DEP-"));

        request.getRequestDispatcher("/views/user/payment_info.jsp").forward(request, response);
    }

    // Hàm phụ trợ kết nối tìm kiếm Order dựa vào cột invoiceID trong CSDL
    private Order getOrderByInvoiceIdFromDB(int invoiceID) {
        String sql = "SELECT * FROM `Order` WHERE invoiceID = ? LIMIT 1";
        try (Connection conn = new dal.DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, invoiceID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Order(
                        rs.getInt("orderID"),
                        (Integer) rs.getObject("customerID"),
                        (Integer) rs.getObject("employeeID"),
                        rs.getInt("invoiceID"), // Đã sửa lỗi ép kiểu (getObject -> getInt cho đúng constructor)
                        rs.getInt("orderType"),
                        rs.getString("tableStatus"),
                        rs.getInt("totalAmount"),
                        rs.getTimestamp("checkoutRequestAt"),
                        rs.getInt("isStaffConfirmed"),
                        rs.getTimestamp("createdAt"),
                        rs.getTimestamp("orderTime"),
                        rs.getInt("depositAmount"),
                        rs.getString("orderStatus"),
                        rs.getString("hostToken") // 🌟 ĐÃ SỬA: Thêm tham số thứ 14 vào đây
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi getOrderByInvoiceId: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}