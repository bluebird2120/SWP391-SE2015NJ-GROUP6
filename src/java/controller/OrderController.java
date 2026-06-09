/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dal.OrderDAO;
import model.Order;
import model.OrderItem;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.MenuItem;

/**
 *
 * @author taduc
 */
@WebServlet(name = "OrderController", urlPatterns = {"/order"})
public class OrderController extends HttpServlet {

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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet OrderController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet OrderController at " + request.getContextPath() + "</h1>");
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
    // =========================================================
    // GET /order?action=cart  →  hiển thị giỏ hàng
    // =========================================================
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
// 
//        String action = request.getParameter("action");
// 
//        if ("cart".equals(action)) {
//            HttpSession session = request.getSession();
//            Integer orderID = (Integer) session.getAttribute("orderID");
// 
//            if (orderID == null) {
//                request.setAttribute("orderItems", null);
//                request.setAttribute("menuItems",  null);
//            } else {
//                // Lấy 2 list song song — thứ tự index tương ứng nhau
//                List<OrderItem> orderItems = orderDAO.getOrderItemsByOrderId(orderID);
//                List<MenuItem>  menuItems  = orderDAO.getMenuItemsByOrderId(orderID);
// 
//                request.setAttribute("orderItems", orderItems);
//                request.setAttribute("menuItems",  menuItems);
//                request.setAttribute("orderID",    orderID);
//            }
// 
//            request.getRequestDispatcher("/views/cart.jsp").forward(request, response);
//        }
//    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        // Nếu không truyền action, mặc định xem như là "cart" để tránh trang trắng
        if (action == null) {
            action = "cart";
        }

        if ("cart".equals(action)) {
            HttpSession session = request.getSession();

            // Tạm thời fix cứng bằng 2 để test
            Integer orderID = 2;
            session.setAttribute("orderID", orderID); // thêm dòng này

            if (orderID == null) {
                request.setAttribute("orderItems", null);
                request.setAttribute("menuItems", null);
            } else {
                // Lấy 2 list song song — thứ tự index tương ứng nhau
                List<OrderItem> orderItems = orderDAO.getOrderItemsByOrderId(orderID);
                List<MenuItem> menuItems = orderDAO.getMenuItemsByOrderId(orderID);

                request.setAttribute("orderItems", orderItems);
                request.setAttribute("menuItems", menuItems);
                request.setAttribute("orderID", orderID);
            }

            request.getRequestDispatcher("/views/user/cart.jsp").forward(request, response);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */

    // =========================================================
    // POST /order?action=add
    // Thêm món vào giỏ hàng
    // =========================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();

        // --- THÊM MÓN ---
        if ("add".equals(action)) {
            int itemID = Integer.parseInt(request.getParameter("itemID"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String note = request.getParameter("note");

            Integer orderID = (Integer) session.getAttribute("orderID");

            if (orderID == null) {
                Integer tableID = (Integer) session.getAttribute("tableID");
                Integer customerID = (Integer) session.getAttribute("customerID");

                Order newOrder = new Order();
                newOrder.setTableID(tableID != null ? tableID : 0);
                newOrder.setCustomerID(customerID != null ? customerID : 0);
                newOrder.setOrderType(tableID != null ? 1 : 2);
                newOrder.setTableStatus(tableID != null ? "occupied" : "available");
                newOrder.setOrderStatus("pending");
                newOrder.setIsStaffConfirmed(0);

                int newOrderID = orderDAO.createOrder(newOrder);
                if (newOrderID == -1) {
                    response.sendRedirect(request.getContextPath() + "/menu?error=create_order_failed");
                    return;
                }

                session.setAttribute("orderID", newOrderID);
                orderID = newOrderID;
            }

            int result = orderDAO.addOrderItem(orderID, itemID, quantity, note);
            if (result == -1) {
                response.sendRedirect(request.getContextPath() + "/menu?error=add_item_failed");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/menu?success=added");

            // --- CẬP NHẬT SỐ LƯỢNG ---
        } else if ("update".equals(action)) {
            try {
                int orderItemID = Integer.parseInt(request.getParameter("orderItemID"));
                int quantity = Integer.parseInt(request.getParameter("quantity"));

                // Kiểm tra số lượng hợp lệ (1 - 99)
                if (quantity < 1 || quantity > 99) {
                    response.sendRedirect(request.getContextPath() + "/order?action=cart&error=invalid_quantity");
                    return;
                }

                orderDAO.updateOrderItemQuantity(orderItemID, quantity);

            } catch (NumberFormatException e) {
                // Nhập chữ hoặc số không hợp lệ → bỏ qua, về lại cart
                response.sendRedirect(request.getContextPath() + "/order?action=cart&error=invalid_quantity");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/order?action=cart");

            // --- XÓA MÓN ---
        } else if ("remove".equals(action)) {
            int orderItemID = Integer.parseInt(request.getParameter("orderItemID"));

            orderDAO.removeOrderItem(orderItemID);

            response.sendRedirect(request.getContextPath() + "/order?action=cart");
        }
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
