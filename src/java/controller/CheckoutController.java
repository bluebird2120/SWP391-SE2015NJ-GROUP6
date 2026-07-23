package controller;

import dal.InvoicesDAO;
import dal.OrderDAO;
import dal.StaffTableDAO;
import dal.TableDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import model.Employee;
import model.Invoices;
import model.MenuItem;
import model.Order;
import model.OrderItem;
import model.Table;

@WebServlet(name = "CheckoutController", urlPatterns = {"/checkout"})
public class CheckoutController extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final InvoicesDAO invoicesDAO = new InvoicesDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processCheckoutDisplay(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("confirm".equals(action)) {
            processPaymentConfirm(request, response);
            return;
        }
        processCheckoutDisplay(request, response);
    }

    private void processCheckoutDisplay(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer orderID = (Integer) session.getAttribute("orderID");
        Employee employee = (Employee) session.getAttribute("employee");

        if (orderID == null || employee == null) {
            response.sendRedirect(request.getContextPath() + "/order?action=cart");
            return;
        }

        // [NHAN VIEN THANH TOAN] Chi nhan vien duoc giao don nay moi vao duoc checkout.
        StaffTableDAO staffTableDAO = new StaffTableDAO();
        if (!staffTableDAO.isOrderAssignedToEmployee(orderID, employee.getEmployeeID())) {
            session.setAttribute("staffTableMessage", "Bạn không có quyền thanh toán đơn này.");
            response.sendRedirect(request.getContextPath() + "/staff/tables");
            return;
        }

        List<OrderItem> orderItems = orderDAO.getOrderItemsByOrderId(orderID);
        List<MenuItem> menuItems = orderDAO.getMenuItemsByOrderId(orderID);
        if (orderItems == null || orderItems.isEmpty()) {
            session.setAttribute("staffTableMessage", "Đơn chưa có món để thanh toán.");
            response.sendRedirect(request.getContextPath() + "/staff/tables");
            return;
        }

        Order order = orderDAO.getOrderById(orderID);
        // [FIX VI TRI PHUC VU] Session hien tai la cua nhan vien nen khong co
        // tableID cua khach. Lay ban truc tiep theo orderID de hien thi dung
        // ca don an tai cho mot ban va don gop nhieu ban.
        List<Table> assignedTables = new TableDAO().getTablesByOrderId(orderID);
        Invoices invoice = createOrGetMealInvoice(order, orderItems, menuItems);
        if (invoice == null) {
            session.setAttribute("staffTableMessage", "Không thể tạo hóa đơn thanh toán.");
            response.sendRedirect(request.getContextPath() + "/staff/tables");
            return;
        }

        session.setAttribute("invoiceID", invoice.getInvoiceID());
        request.setAttribute("invoice", invoice);
        request.setAttribute("assignedTables", assignedTables);
        request.setAttribute("orderItems", orderItems);
        request.setAttribute("menuItems", menuItems);
        request.setAttribute("orderID", orderID);
        request.getRequestDispatcher("/views/user/checkout.jsp").forward(request, response);
    }

    private Invoices createOrGetMealInvoice(Order order, List<OrderItem> orderItems,
            List<MenuItem> menuItems) {
        if (order == null) {
            return null;
        }

        Integer invoiceID = order.getInvoiceID();
        if (invoiceID != null) {
            Invoices current = invoicesDAO.getInvoiceById(invoiceID);
            if (current != null && current.getInvoiceNumber() != null
                    && !current.getInvoiceNumber().startsWith("DEP-")
                    && !"paid".equalsIgnoreCase(current.getStatus())) {
                return current;
            }
        }

        long subTotal = 0;
        int size = Math.min(orderItems.size(), menuItems.size());
        for (int i = 0; i < size; i++) {
            MenuItem mi = menuItems.get(i);
            int unitPrice = mi.getDiscountedPrice() > 0
                    ? mi.getDiscountedPrice() : mi.getPrice();
            subTotal += (long) unitPrice * orderItems.get(i).getQuantity();
        }

        long taxAmount = subTotal * 10 / 100;
        long depositDeducted = order.getDepositAmount();
        long finalAmount = subTotal + taxAmount - depositDeducted;
        if (finalAmount < 0) {
            finalAmount = 0;
        }

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
            return null;
        }
        invoicesDAO.linkInvoiceToOrder(newInvoiceID, order.getOrderID());
        invoice.setInvoiceID(newInvoiceID);
        return invoice;
    }

    private void processPaymentConfirm(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String paymentGateway = request.getParameter("paymentGateway");
        String orderIDParam = request.getParameter("orderID");
        Integer invoiceID = (Integer) session.getAttribute("invoiceID");

        if (orderIDParam == null || invoiceID == null) {
            response.sendRedirect(request.getContextPath() + "/staff/tables");
            return;
        }

        int orderID = Integer.parseInt(orderIDParam);
        Invoices invoice = invoicesDAO.getInvoiceById(invoiceID);
        if (invoice == null) {
            response.sendRedirect(request.getContextPath() + "/staff/tables");
            return;
        }

        if ("vnpay".equals(paymentGateway)) {
            response.sendRedirect(request.getContextPath() + "/payment?orderID=" + orderID);
            return;
        }

        // [THANH TOAN TAI QUAY] Nhan vien xac nhan thanh toan tai quay.
        String transactionCode = "CASH-" + System.currentTimeMillis();
        boolean ok = invoicesDAO.updatePaymentSuccessAndCleaningTable(
                invoiceID, orderID, "cash", invoice.getFinalAmount(), transactionCode);

        if (ok) {
            clearDiningSession(session);
            response.sendRedirect(request.getContextPath()
                    + "/payment-info?invoiceID=" + invoiceID);
        } else {
            response.sendRedirect(request.getContextPath()
                    + "/checkout?error=payment_failed");
        }
    }

    private void clearDiningSession(HttpSession session) {
        session.removeAttribute("orderID");
        session.removeAttribute("invoiceID");
        session.removeAttribute("tableID");
        session.removeAttribute("reservationOrderID");
        session.removeAttribute("reservationFlow");
        session.removeAttribute("depositAmount");
        session.removeAttribute("reservationHoldExpiresAt");
    }

    @Override
    public String getServletInfo() {
        return "Checkout Controller - staff checkout";
    }
}
