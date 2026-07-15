package controller;

import dal.DBContext;
import dal.OrderDAO;
import dal.StaffTableDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import model.Employee;
import model.Invoices;
import model.MenuItem;
import model.Order;
import model.OrderItem;

@WebServlet(name = "StaffTableController", urlPatterns = {"/staff/tables"})
public class StaffTableController extends HttpServlet {

    private static final String VIEW = "/views/staff/my-tables.jsp";
    private static final String DETAIL_VIEW = "/views/staff/order-detail.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Employee employee = getLoggedInEmployee(request);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/login?type=employee");
            return;
        }

        String action = request.getParameter("action");
        if ("detail".equals(action)) {
            showOrderDetail(request, response, employee);
            return;
        }

        // [PHAN QUYEN PHUC VU] Staff chi nhin thay don va ban cua chinh minh.
        request.setAttribute("assignedTables",
                new StaffTableDAO().getTablesForEmployee(employee.getEmployeeID()));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Employee employee = getLoggedInEmployee(request);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/login?type=employee");
            return;
        }

        String message = "Thao tác không hợp lệ.";
        try {
            int orderID = Integer.parseInt(request.getParameter("orderID"));
            String action = request.getParameter("action");

            if ("checkout".equals(action)) {
                // [NHAN VIEN THANH TOAN] Chuyen nut thanh toan tu khach sang
                // nhan vien phuc vu ban do. DAO chan nhan vien bam nham don.
                StaffTableDAO dao = new StaffTableDAO();
                if (!dao.isOrderAssignedToEmployee(orderID, employee.getEmployeeID())) {
                    request.getSession().setAttribute("staffTableMessage",
                            "Bạn không có quyền thanh toán đơn này.");
                    response.sendRedirect(request.getContextPath() + "/staff/tables");
                    return;
                }

                HttpSession session = request.getSession();
                session.setAttribute("orderID", orderID);
                session.removeAttribute("invoiceID");
                response.sendRedirect(request.getContextPath() + "/checkout");
                return;

            } else if ("updateItem".equals(action)) {
                // [KIEM TRA DON] Tru bot so luong neu khach hoan tra mon cho quan.
                int orderItemID = Integer.parseInt(request.getParameter("orderItemID"));
                int quantity = Math.max(0,
                        Integer.parseInt(request.getParameter("quantity")));
                message = new StaffTableDAO().updateOrderItemQuantityForEmployee(
                        orderItemID, orderID, employee.getEmployeeID(), quantity)
                        ? "Đã cập nhật số lượng món."
                        : "Không thể cập nhật món này.";
                request.getSession().setAttribute("staffTableMessage", message);
                response.sendRedirect(request.getContextPath()
                        + "/staff/tables?action=detail&orderID=" + orderID);
                return;

            } else if ("cleaned".equals(action)) {
                // [PHAN QUYEN PHUC VU] DAO kiem tra order phai thuoc staff nay.
                message = new StaffTableDAO().markCleaningCompleted(
                        orderID, employee.getEmployeeID())
                        ? "clean_success" : "Không thể hoàn tất dọn bàn này.";

            } else if ("checkin".equals(action) || "open_table".equals(action)) {
                // Khach dat truoc den -> arrived. Khach vang lai duoc xac nhan -> occupied.
                String newStatus = "checkin".equals(action) ? "arrived" : "occupied";
                String sql = "UPDATE `Order` SET tableStatus = ? WHERE orderID = ?";

                try (Connection conn = new DBContext().getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setString(1, newStatus);
                    ps.setInt(2, orderID);
                    int rowsAffected = ps.executeUpdate();

                    if (rowsAffected > 0) {
                        message = "checkin".equals(action)
                                ? "checkin_success"
                                : "Đã mở bàn thành công. Khách có thể xem menu và gọi món.";
                    } else {
                        message = "Không tìm thấy đơn hàng để thao tác.";
                    }
                } catch (Exception e) {
                    System.err.println("Loi khi mo ban/checkin: " + e.getMessage());
                    message = "Lỗi hệ thống: Không thể kết nối cơ sở dữ liệu.";
                }
            }
        } catch (NumberFormatException e) {
            message = "Mã đơn không hợp lệ.";
        }

        request.getSession().setAttribute("staffTableMessage", message);
        response.sendRedirect(request.getContextPath() + "/staff/tables");
    }

    private void showOrderDetail(HttpServletRequest request,
            HttpServletResponse response, Employee employee)
            throws ServletException, IOException {
        try {
            int orderID = Integer.parseInt(request.getParameter("orderID"));
            StaffTableDAO staffTableDAO = new StaffTableDAO();
            if (!staffTableDAO.isOrderAssignedToEmployee(orderID, employee.getEmployeeID())) {
                request.getSession().setAttribute("staffTableMessage",
                        "Bạn không có quyền xem đơn này.");
                response.sendRedirect(request.getContextPath() + "/staff/tables");
                return;
            }

            OrderDAO orderDAO = new OrderDAO();
            Order order = orderDAO.getOrderById(orderID);
            List<OrderItem> orderItems = orderDAO.getOrderItemsByOrderId(orderID);
            List<MenuItem> menuItems = orderDAO.getMenuItemsByOrderId(orderID);

            request.setAttribute("order", order);
            request.setAttribute("orderItems", orderItems);
            request.setAttribute("menuItems", menuItems);
            request.setAttribute("invoicePreview",
                    buildInvoicePreview(order, orderItems, menuItems));
            request.getRequestDispatcher(DETAIL_VIEW).forward(request, response);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("staffTableMessage",
                    "Mã đơn không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/staff/tables");
        }
    }

    private Invoices buildInvoicePreview(Order order, List<OrderItem> orderItems,
            List<MenuItem> menuItems) {
        long subTotal = 0;
        if (orderItems != null && menuItems != null) {
            int size = Math.min(orderItems.size(), menuItems.size());
            for (int i = 0; i < size; i++) {
                MenuItem mi = menuItems.get(i);
                int unitPrice = mi.getDiscountedPrice() > 0
                        ? mi.getDiscountedPrice() : mi.getPrice();
                subTotal += (long) unitPrice * orderItems.get(i).getQuantity();
            }
        }

        long taxAmount = subTotal * 10 / 100;
        long depositDeducted = order != null ? order.getDepositAmount() : 0;
        long finalAmount = subTotal + taxAmount - depositDeducted;
        if (finalAmount < 0) {
            finalAmount = 0;
        }

        Invoices invoice = new Invoices();
        invoice.setSubTotal(subTotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setDepositDeducted(depositDeducted);
        invoice.setFinalAmount(finalAmount);
        return invoice;
    }

    private Employee getLoggedInEmployee(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null
                : (Employee) session.getAttribute("employee");
    }
}
