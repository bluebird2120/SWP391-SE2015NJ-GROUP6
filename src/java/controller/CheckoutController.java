/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
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

/**
 *
 * @author taduc
 */
@WebServlet(name = "CheckoutController", urlPatterns = {"/checkout"})
public class CheckoutController extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    private final OrderDAO orderDAO = new OrderDAO();
    private final InvoicesDAO invoicesDAO = new InvoicesDAO();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet CheckoutController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet CheckoutController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
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

        request.getRequestDispatcher("/views/checkout.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        System.out.println("DEBUG orderID = " + session.getAttribute("orderID"));
        System.out.println("DEBUG tableID = " + session.getAttribute("tableID"));
        System.out.println("DEBUG selectedItems = "
                + java.util.Arrays.toString(request.getParameterValues("selectedItems")));

        // --- Lấy orderID từ session ---
        Integer orderID = (Integer) session.getAttribute("orderID");
        if (orderID == null) {
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }

        // --- Lấy danh sách orderItemID được tích từ cart ---
        String[] selectedArr = request.getParameterValues("selectedItems");
        if (selectedArr == null || selectedArr.length == 0) {
            response.sendRedirect(request.getContextPath() + "/order?action=cart");
            return;
        }

        List<Integer> selectedIDs = new ArrayList<>();
        for (String id : selectedArr) {
            selectedIDs.add(Integer.parseInt(id));
        }

        // --- Lấy OrderItem và MenuItem tương ứng ---
        List<OrderItem> orderItems = orderDAO.getOrderItemsByIds(selectedIDs);
        List<MenuItem> menuItems = orderDAO.getMenuItemsByOrderItemIds(selectedIDs);

//        // --- Lấy Order để lấy depositAmount ---
//        Order order = orderDAO.getActiveOrderByTableId(
//                (Integer) session.getAttribute("tableID")
//        );
        Integer tableID = (Integer) session.getAttribute("tableID");
        Order order = null;
        if (tableID != null) {
            order = orderDAO.getActiveOrderByTableId(tableID);
        }

        long depositDeducted = (order != null) ? order.getDepositAmount() : 0;

        // --- Tính subTotal ---
        long subTotal = 0;
        for (int i = 0; i < orderItems.size(); i++) {
            MenuItem mi = menuItems.get(i);
            int qty = orderItems.get(i).getQuantity();
            int unitPrice = (mi.getDiscountedPrice() > 0)
                    ? mi.getDiscountedPrice() : mi.getPrice();
            subTotal += (long) unitPrice * qty;
        }

        // --- Tính thuế VAT 10% ---
        long taxAmount = subTotal * 10 / 100;

        // --- Tính finalAmount ---
        long finalAmount = subTotal + taxAmount - depositDeducted;

        // --- Tạo Invoice ---
        Invoices invoice = new Invoices();
        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        invoice.setSubTotal(subTotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setDepositDeducted(depositDeducted);
        invoice.setFinalAmount(finalAmount);
        invoice.setIssuedDate(new Date(System.currentTimeMillis()));
        invoice.setStatus("unpaid");

        int invoiceID = invoicesDAO.createInvoice(invoice);
        if (invoiceID == -1) {
            response.sendRedirect(request.getContextPath() + "/order?action=cart&error=invoice_failed");
            return;
        }

        // --- Gắn invoiceID vào Order ---
        invoicesDAO.linkInvoiceToOrder(invoiceID, orderID);

        // --- Lưu invoiceID vào session ---
        session.setAttribute("invoiceID", invoiceID);

        // --- Truyền dữ liệu sang checkout.jsp ---
        invoice.setInvoiceID(invoiceID);
        request.setAttribute("invoice", invoice);
        request.setAttribute("orderItems", orderItems);
        request.setAttribute("menuItems", menuItems);
        request.setAttribute("orderID", orderID);

        request.getRequestDispatcher("/views/user/checkout.jsp").forward(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
