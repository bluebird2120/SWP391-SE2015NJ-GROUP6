package controller;

import dal.OrderDAO;
import dal.TableDAO;
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
import java.sql.Timestamp;
import model.Table;

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

            // LẤY ORDER_ID THẬT TỪ SESSION (Đã sinh ra lúc quét QR)
            Integer orderID = (Integer) session.getAttribute("orderID");

            if (orderID == null) {
                // Nếu không có Order (khách chưa chọn món nào mà bấm vào giỏ)
                request.setAttribute("orderItems", null);
                request.setAttribute("menuItems", null);
                request.setAttribute("assignedTables", null); // Thêm dòng này
                request.setAttribute("errorMsg", "Giỏ hàng của bạn đang trống!");
            } else {
                // Lấy 2 list song song từ Database dựa trên orderID thật
                List<OrderItem> orderItems = orderDAO.getOrderItemsByOrderId(orderID);
                List<MenuItem> menuItems = orderDAO.getMenuItemsByOrderId(orderID);
                
                // === BẮT ĐẦU THÊM MỚI ===
                TableDAO tableDAO = new TableDAO();
                List<Table> assignedTables = tableDAO.getTablesByOrderId(orderID);
                request.setAttribute("assignedTables", assignedTables);
                // === KẾT THÚC THÊM MỚI ===

                request.setAttribute("orderItems", orderItems);
                request.setAttribute("menuItems", menuItems);
                request.setAttribute("orderID", orderID);
            }

            request.getRequestDispatcher("/views/user/cart.jsp").forward(request, response);
        }
    }

    // =========================================================
    // POST /order
    // Xử lý các logic: Thêm món, Cập nhật số lượng, Xóa món, Gộp bàn
    // =========================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        HttpSession session = request.getSession();

        // Lấy orderID hiện tại của khách từ Session
        Integer currentOrderID = (Integer) session.getAttribute("orderID");

        // --- GỘP BÀN TỪ QUÉT QR ---
        if ("addTable".equals(action)) {
            String newTableToken = request.getParameter("tableToken");

            if (currentOrderID != null && newTableToken != null) {
                TableDAO tableDAO = new TableDAO();

                Table newTable = tableDAO.getTableByToken(newTableToken);

                if (newTable != null) {
                    boolean isTableFree = tableDAO.isTableAvailable(newTable.getTableID());

                    if (isTableFree) {
                        boolean success = orderDAO.addTableToExistingOrder(currentOrderID, newTable.getTableID());

                        if (success) {
                            session.setAttribute("successMsg", "Đã gộp thành công Bàn " + newTable.getTableName() + " vào đơn hàng của bạn!");
                        }
                    } else {
                        session.setAttribute("errorMsg", "Bàn này đang có khách ngồi, không thể gộp!");
                    }
                } else {
                    session.setAttribute("errorMsg", "Mã QR bàn không hợp lệ!");
                }
            }
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }

        // --- THÊM MÓN VÀO GIỎ ---
        if ("add".equals(action)) {
            int itemID = Integer.parseInt(request.getParameter("itemID"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String note = request.getParameter("note");

            // Lấy giá trị tiền của món ăn
            int price = 0;
            String priceParam = request.getParameter("price");
            if (priceParam != null && !priceParam.isEmpty()) {
                price = Integer.parseInt(priceParam);
            }

            Integer orderID = (Integer) session.getAttribute("orderID");
            
            // --- SỬA LOGIC LẤY BÀN ĐỂ CHỌN ĐƯỢC BÀN KHI GỘP ---
            Integer tableID = null;
            String selectedTableStr = request.getParameter("tableID"); // Lấy từ Dropdown chọn bàn trên giao diện
            
            if (selectedTableStr != null && !selectedTableStr.isEmpty()) {
                tableID = Integer.parseInt(selectedTableStr); // Lấy bàn khách vừa chọn
            } else {
                tableID = (Integer) session.getAttribute("tableID"); // Khách chưa chọn hoặc đơn lẻ thì lấy bàn gốc
            }
            // -------------------------------------------------

            // NẾU CHƯA CÓ ĐƠN HÀNG -> TẠO ĐƠN HÀNG MỚI

            // NẾU CHƯA CÓ ĐƠN HÀNG -> TẠO ĐƠN HÀNG MỚI
            if (orderID == null) {
                Integer customerID = (Integer) session.getAttribute("customerID");

                Order newOrder = new Order();
                newOrder.setCustomerID(customerID);
                newOrder.setOrderType(tableID != null ? 1 : 2); // 1: Dùng tại bàn, 2: Mang về
                newOrder.setTableStatus(tableID != null ? "occupied" : "available");
                newOrder.setOrderStatus("ordering");
                newOrder.setIsStaffConfirmed(0);
                newOrder.setTotalAmount(0);
                newOrder.setDepositAmount(0);
                newOrder.setOrderTime(new Timestamp(System.currentTimeMillis()));

                // ĐÃ XÓA SẠCH ĐOẠN SET CAPACITY VÀ AREATYPE THEO CHUẨN MODEL MỚI

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

            // Gọi hàm thêm món vào chi tiết đơn
            int result = orderDAO.addOrderItem(orderID, itemID, tableID, quantity, price, note);
            if (result == -1) {
                response.sendRedirect(request.getContextPath() + "/menu?error=add_item_failed");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/menu?success=added");

        // --- CẬP NHẬT SỐ LƯỢNG MÓN ---
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
                response.sendRedirect(request.getContextPath() + "/order?action=cart&error=invalid_quantity");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/order?action=cart");

        // --- XÓA MÓN KHỎI GIỎ ---
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
