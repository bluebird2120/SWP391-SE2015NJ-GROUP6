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
import java.io.PrintWriter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "CheckoutController", urlPatterns = {"/checkout"})
public class CheckoutController extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final InvoicesDAO invoicesDAO = new InvoicesDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Integer invoiceID = (Integer) session.getAttribute("invoiceID");
        Integer orderID = (Integer) session.getAttribute("orderID");

        if (invoiceID == null || orderID == null) {
            response.sendRedirect(request.getContextPath() + "/order?action=cart");
            return;
        }

        Invoices invoice = invoicesDAO.getInvoiceById(invoiceID);
        request.setAttribute("invoice", invoice);
        request.setAttribute("orderID", orderID);

        request.getRequestDispatcher("/views/user/checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        System.out.println("DEBUG action = " + action);
        System.out.println("DEBUG orderID = " + session.getAttribute("orderID"));
        System.out.println("DEBUG tableID = " + session.getAttribute("tableID"));

        // =================================================================
        // NHÁNH 1: XỬ LÝ KHI KHÁCH HÀNG NHẤN NÚT "XÁC NHẬN & HOÀN TẤT ĐƠN" TRÊN CHECKOUT.JSP
        // =================================================================
        if ("confirm".equals(action)) {
            String paymentGateway = request.getParameter("paymentGateway");
            String orderIDParam = request.getParameter("orderID");
            Integer invoiceID = (Integer) session.getAttribute("invoiceID");

            if (orderIDParam != null && invoiceID != null) {
                int orderID = Integer.parseInt(orderIDParam);

                if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                    // ... (Giữ nguyên đoạn code xử lý DB cọc và hóa đơn ăn của bạn) ...

                    // Dọn dẹp session để khách có thể quét mã gọi món lượt mới
                    session.removeAttribute("orderID");
                    session.removeAttribute("invoiceID");
                    session.removeAttribute("tableID");
                    session.removeAttribute("reservationOrderID");
                    session.removeAttribute("reservationFlow");
                    session.removeAttribute("depositAmount");
                    session.removeAttribute("reservationHoldExpiresAt");

                    // 🌟 SỬA ĐOẠN NÀY: Thay vì out.println thô sơ, hãy redirect về trang hóa đơn chuẩn
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
            return;
        }

        // =================================================================
        // NHÁNH 2: LOGIC GỐC - XỬ LÝ KHI ĐI TỪ TRANG CART.JSP SANG CHECKOUT.JSP
        // =================================================================
        String[] selectedArr = request.getParameterValues("selectedItems");
        Integer orderID = (Integer) session.getAttribute("orderID");

        if (orderID == null || selectedArr == null || selectedArr.length == 0) {
            response.sendRedirect(request.getContextPath() + "/order?action=cart");
            return;
        }

        List<Integer> selectedIDs = new ArrayList<>();
        for (String id : selectedArr) {
            selectedIDs.add(Integer.parseInt(id));
        }

        List<OrderItem> orderItems = orderDAO.getOrderItemsByIds(selectedIDs);
        List<MenuItem> menuItems = orderDAO.getMenuItemsByOrderItemIds(selectedIDs);

        Integer tableID = (Integer) session.getAttribute("tableID");
        Order order = null;
        if (tableID != null) {
            order = orderDAO.getActiveOrderByTableId(tableID);
        }

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

        Invoices invoice = new Invoices();
        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        invoice.setSubTotal(subTotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setDepositDeducted(depositDeducted);
        invoice.setFinalAmount(finalAmount);
        invoice.setIssuedDate(new Date(System.currentTimeMillis()));
        invoice.setStatus("unpaid");

        int newInvoiceID = invoicesDAO.createInvoice(invoice);
        if (newInvoiceID == -1) {
            response.sendRedirect(request.getContextPath() + "/order?action=cart&error=invoice_failed");
            return;
        }

        invoicesDAO.linkInvoiceToOrder(newInvoiceID, orderID);
        session.setAttribute("invoiceID", newInvoiceID);

        invoice.setInvoiceID(newInvoiceID);
        request.setAttribute("invoice", invoice);
        request.setAttribute("orderItems", orderItems);
        request.setAttribute("menuItems", menuItems);
        request.setAttribute("orderID", orderID);

        request.getRequestDispatcher("/views/user/checkout.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Checkout Controller with Integrated Payments";
    }
}
