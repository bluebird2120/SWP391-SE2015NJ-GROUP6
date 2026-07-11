package controller;

import dal.InvoicesDAO;
import dal.OrderDAO;
import model.Employee;
import model.Invoices;
import model.Order;
import model.MenuItem;
import model.OrderItem;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminInvoiceDetailController", urlPatterns = {"/admin/invoice-detail"})
public class AdminInvoiceDetailController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        // 1. KIỂM TRA PHÂN QUYỀN ADMIN
        HttpSession session = request.getSession();
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null || employee.getRoleID() != 1) {
            response.sendRedirect(request.getContextPath() + "/login?type=employee");
            return;
        }

        // 2. LẤY ID HÓA ĐƠN TỪ URL
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/invoices");
            return;
        }

        try {
            int invoiceID = Integer.parseInt(idParam);
            
            // 3. KÉO DỮ LIỆU TỪ DB
            InvoicesDAO invoicesDAO = new InvoicesDAO();
            OrderDAO orderDAO = new OrderDAO();
            
            Invoices invoice = invoicesDAO.getInvoiceById(invoiceID);
            if (invoice == null) {
                response.sendRedirect(request.getContextPath() + "/admin/invoices");
                return;
            }
            
            Order order = orderDAO.getOrderByInvoiceId(invoiceID);
            List<OrderItem> orderItems = null;
            List<MenuItem> menuItems = null;
            
            if (order != null) {
                // Tận dụng lại các hàm đã có sẵn trong OrderDAO của bạn
                orderItems = orderDAO.getOrderItemsByOrderId(order.getOrderID());
                menuItems = orderDAO.getMenuItemsByOrderId(order.getOrderID());
            }

            // 4. ĐẨY DỮ LIỆU SANG JSP
            request.setAttribute("invoice", invoice);
            request.setAttribute("order", order);
            request.setAttribute("orderItems", orderItems);
            request.setAttribute("menuItems", menuItems);
            
            request.getRequestDispatcher("/views/admin/invoice-detail.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/invoices");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}