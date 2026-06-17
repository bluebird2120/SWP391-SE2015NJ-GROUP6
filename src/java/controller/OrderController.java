package controller;

import dal.OrderDAO;
import model.Order;
import model.OrderItem;
import model.MenuItem;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "OrderController", urlPatterns = {"/order"})
public class OrderController extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
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

    // =========================================================
    // GET /order?action=cart  →  hiển thị giỏ hàng
    // =========================================================
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

            // Tạm thời fix cứng bằng 2 để test (Nhớ xóa hoặc comment lại khi đưa vào thực tế)
            Integer orderID = 2;
            session.setAttribute("orderID", orderID);
            session.setAttribute("tableID", 2);

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

    // =========================================================
    // POST /order?action=add
    // Thêm món vào giỏ hàng
    // =========================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        HttpSession session = request.getSession();

        // --- THÊM MÓN ---
        if ("add".equals(action)) {
            int itemID = Integer.parseInt(request.getParameter("itemID"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String note = request.getParameter("note");

            // Lấy giá trị tiền của món ăn (Phải truyền từ form JSP lên)
            int price = 0;
            String priceParam = request.getParameter("price");
            if (priceParam != null && !priceParam.isEmpty()) {
                price = Integer.parseInt(priceParam);
            }

            Integer orderID = (Integer) session.getAttribute("orderID");
            Integer tableID = (Integer) session.getAttribute("tableID");

            if (orderID == null) {
                Integer customerID = (Integer) session.getAttribute("customerID");

                Order newOrder = new Order();
                newOrder.setCustomerID(customerID);
                newOrder.setOrderType(tableID != null ? 1 : 2); // 1: Dùng tại bàn, 2: Mang về
                newOrder.setTableStatus(tableID != null ? "occupied" : "available");
                newOrder.setOrderStatus("ordering");
                newOrder.setIsStaffConfirmed(0);

                // THÊM LẠI: Lấy sức chứa và khu vực từ session để đưa vào hóa đơn
                String areaType = (String) session.getAttribute("areaType");
                Integer capacity = (Integer) session.getAttribute("capacity");
                newOrder.setAreaType(areaType != null ? areaType : "Chưa xác định");
                newOrder.setCapacity(capacity != null ? capacity : 0);

                int newOrderID = orderDAO.createOrder(newOrder);
                if (newOrderID == -1) {
                    response.sendRedirect(request.getContextPath() + "/menu?error=create_order_failed");
                    return;
                }

                // Lưu liên kết Bàn và Đơn hàng vào bảng Order_Table
                if (tableID != null && tableID > 0) {
                    orderDAO.linkOrderAndTable(newOrderID, tableID);
                }

                session.setAttribute("orderID", newOrderID);
                orderID = newOrderID;
            }

            // Truyền thêm tableID và price vào hàm addOrderItem
            int result = orderDAO.addOrderItem(orderID, itemID, tableID, quantity, price, note);
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

    @Override
    public String getServletInfo() {
        return "Order Controller Handles Add, Update, Remove logic";
    }
}