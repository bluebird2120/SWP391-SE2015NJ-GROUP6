package controller;

import dal.InvoicesDAO;
import dal.OrderDAO;
import model.Invoices;
import model.MenuItem;
import model.Order;
import model.OrderItem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet(name = "CheckoutController", urlPatterns = {"/checkout"})
public class CheckoutController extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final InvoicesDAO invoicesDAO = new InvoicesDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Mọi yêu cầu GET (như truy cập qua URL hoặc thẻ <a>) đều chuyển vào hàm xử lý hiển thị
        processCheckoutDisplay(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        // NHÁNH 1: KHÁCH HÀNG NHẤN NÚT "XÁC NHẬN & HOÀN TẤT ĐƠN" CHỌN CỔNG THANH TOÁN
        if ("confirm".equals(action)) {
            processPaymentConfirm(request, response);
        } 
        // NHÁNH 2: TỪ TRANG CART ĐI SANG (Form POST)
        else {
            processCheckoutDisplay(request, response);
        }
    }

    // =================================================================
    // HÀM XỬ LÝ GIAO DIỆN THANH TOÁN (TẠO HÓA ĐƠN & TÍNH TIỀN)
    // =================================================================
    private void processCheckoutDisplay(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Integer orderID = (Integer) session.getAttribute("orderID");

        if (orderID == null) {
            response.sendRedirect(request.getContextPath() + "/order?action=cart");
            return;
        }

        // 🌟 ĐÃ SỬA: Lấy TOÀN BỘ món ăn của đơn hàng trực tiếp từ DB (Không phụ thuộc checkbox)
        List<OrderItem> orderItems = orderDAO.getOrderItemsByOrderId(orderID);
        List<MenuItem> menuItems = orderDAO.getMenuItemsByOrderId(orderID);

        if (orderItems == null || orderItems.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/order?action=cart&error=empty_cart");
            return;
        }

        Order order = orderDAO.getOrderById(orderID);
        long depositDeducted = (order != null) ? order.getDepositAmount() : 0;

        long subTotal = 0;
        for (int i = 0; i < orderItems.size(); i++) {
            MenuItem mi = menuItems.get(i);
            int qty = orderItems.get(i).getQuantity();
            int unitPrice = (mi.getDiscountedPrice() > 0) ? mi.getDiscountedPrice() : mi.getPrice();
            subTotal += (long) unitPrice * qty;
        }

        long taxAmount = subTotal * 10 / 100;
        long finalAmount = subTotal + taxAmount - depositDeducted;
        if (finalAmount < 0) finalAmount = 0;

        // Tạo mới hoặc lấy Hóa đơn cũ (tránh spam rác Database khi khách F5 nhiều lần)
        Integer invoiceID = (Integer) session.getAttribute("invoiceID");
        Invoices invoice = null;

        if (invoiceID != null) {
            invoice = invoicesDAO.getInvoiceById(invoiceID);
        }

        if (invoice == null) {
            invoice = new Invoices();
            invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
            invoice.setSubTotal(subTotal);
            invoice.setTaxAmount(taxAmount);
            invoice.setDepositDeducted(depositDeducted);
            invoice.setFinalAmount(finalAmount);
            invoice.setIssuedDate(new Date(System.currentTimeMillis()));
            invoice.setStatus("unpaid");

            int newInvoiceID = invoicesDAO.createInvoice(invoice);
            if (newInvoiceID != -1) {
                invoicesDAO.linkInvoiceToOrder(newInvoiceID, orderID);
                session.setAttribute("invoiceID", newInvoiceID);
                invoice.setInvoiceID(newInvoiceID);
            }
        }

        // Đẩy dữ liệu sang trang JSP
        request.setAttribute("invoice", invoice);
        request.setAttribute("orderItems", orderItems);
        request.setAttribute("menuItems", menuItems);
        request.setAttribute("orderID", orderID);
        
        request.getRequestDispatcher("/views/user/checkout.jsp").forward(request, response);
    }

    // =================================================================
    // HÀM XỬ LÝ CỔNG THANH TOÁN (VNPAY, TIỀN MẶT...)
    // =================================================================
    private void processPaymentConfirm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        String paymentGateway = request.getParameter("paymentGateway");
        String orderIDParam = request.getParameter("orderID");
        Integer invoiceID = (Integer) session.getAttribute("invoiceID");

        if (orderIDParam != null && invoiceID != null) {
            int orderID = Integer.parseInt(orderIDParam);

            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                // ... (Giữ nguyên đoạn code xử lý DB cọc và hóa đơn ăn của bạn ở đây) ...

                // Dọn dẹp session để khách có thể quét mã gọi món lượt mới
                session.removeAttribute("orderID");
                session.removeAttribute("invoiceID");
                session.removeAttribute("tableID");
                session.removeAttribute("reservationOrderID");
                session.removeAttribute("reservationFlow");
                session.removeAttribute("depositAmount");
                session.removeAttribute("reservationHoldExpiresAt");

                if (invoiceID != null) {
                    response.sendRedirect(request.getContextPath() + "/payment-info?invoiceID=" + invoiceID);
                    return;
                } else {
                    response.sendRedirect(request.getContextPath() + "/home?status=order_success");
                    return;
                }
            } else if ("vnpay".equals(paymentGateway)) {
                // Thanh toán VNPay: Chuyển hướng sang servlet xử lý cổng thanh toán
                response.sendRedirect(request.getContextPath() + "/payment?orderID=" + orderID);
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/home");
    }

    @Override
    public String getServletInfo() {
        return "Checkout Controller with Integrated Payments";
    }
}