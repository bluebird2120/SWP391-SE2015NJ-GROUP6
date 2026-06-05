package controller;

import dal.OrderDAOSon;
import dal.TableDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import model.Customer;
import model.Order;
import model.Table;

@WebServlet(name = "ReservationController", urlPatterns = {"/reservation"})
public class ReservationController extends HttpServlet {

    private final TableDAO tableDAO = new TableDAO();
    private final OrderDAOSon orderDAO = new OrderDAOSon();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // ── 1. Lịch sử đặt bàn ─────────────────────────────────────────────
        if ("history".equals(action)) {
            Customer customer = getCustomer(request);
            if (customer == null) { goLogin(request, response); return; }

            List<Order> orders = orderDAO.getReservationsByCustomer(customer.getCustomerID());
            request.setAttribute("orders", orders);
            request.setAttribute("step", "history");
            forward(request, response);
            return;
        }

        // ── 2. Huỷ đơn ─────────────────────────────────────────────────────
        if ("cancel".equals(action)) {
            Customer customer = getCustomer(request);
            if (customer == null) { goLogin(request, response); return; }

            int orderID = toInt(request.getParameter("orderID"), -1);
            if (orderID > 0) {
                orderDAO.cancelReservation(orderID, customer.getCustomerID());
            }
            response.sendRedirect(request.getContextPath() + "/reservation?action=history");
            return;
        }

        // ── 3. Xử lý hiển thị bàn trống / gợi ý trực tiếp ngay bên dưới form ──
        if ("choosetable".equals(action) || "choosefood".equals(action)) {
            String dateTimeStr = request.getParameter("orderTime");
            String areaType    = request.getParameter("areaType");
            String tableID     = request.getParameter("tableID");

            String error = validateDateTime(dateTimeStr);
            if (error != null || areaType == null || areaType.isBlank()) {
                request.setAttribute("error", error != null ? error : "Vui lòng chọn khu vực.");
                request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
                request.setAttribute("step", "pick-time");
                forward(request, response);
                return;
            }

            Timestamp orderTime = parseTimestamp(dateTimeStr);
            List<Table> tables  = tableDAO.findAvailableTables(areaType, orderTime);

            request.setAttribute("orderTime", dateTimeStr);
            request.setAttribute("areaType",  areaType);
            request.setAttribute("tableID",   tableID); // Lưu giữ vết bàn được chọn nếu có

            if (tables.isEmpty()) {
                // Kích hoạt hộp gợi ý đổi vị trí thông minh
                List<Table> higherTables = tableDAO.findAlternativeTablesHigherCapacity(areaType, orderTime, 2);
                List<Table> otherAreaTables = tableDAO.findAlternativeTablesOtherArea(areaType, orderTime);
                request.setAttribute("higherTables", higherTables);
                request.setAttribute("otherAreaTables", otherAreaTables);
                request.setAttribute("step", "no-table-suggest"); 
            } else {
                request.setAttribute("tables", tables);
                request.setAttribute("step", "choose-table");
            }
            request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
            forward(request, response);
            return;
        }

        // ── 4. Thành công ──────────────────────────────────────────────────
        if ("success".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("lastReservation") == null) {
                response.sendRedirect(request.getContextPath() + "/reservation");
                return;
            }
            Order order = (Order) session.getAttribute("lastReservation");
            Table table = (Table) session.getAttribute("assignedTable");

            session.removeAttribute("lastReservation");
            session.removeAttribute("assignedTable");

            request.setAttribute("order", order);
            request.setAttribute("table", table);
            request.setAttribute("step", "success");
            forward(request, response);
            return;
        }

        // ── 5. Khôi phục dữ liệu tự động sau khi đăng nhập xong quay trở lại ──
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("customer") != null) {
            String tempOrderTime = (String) session.getAttribute("tempOrderTime");
            Integer tempTableID  = (Integer) session.getAttribute("tempTableID");

            if (tempOrderTime != null && tempTableID != null && tempTableID > 0) {
                Table savedTable = tableDAO.getTableByID(tempTableID);
                if (savedTable != null) {
                    // Chuyển hướng nội bộ để vẽ lại form kèm trạng thái bàn đã click sẵn trước đó
                    response.sendRedirect(request.getContextPath() + "/reservation?action=choosefood&orderTime=" 
                            + tempOrderTime + "&areaType=" + savedTable.getAreaType() + "&tableID=" + tempTableID);
                    return;
                }
            }
        }

        // Mặc định ban đầu vào trang
        request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
        request.setAttribute("step", "pick-time");
        forward(request, response);
    }

    // ── POST: Xử lý chốt đơn cuối cùng (Chỉ chặn đăng nhập tại đây) ──
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String dateTimeStr = request.getParameter("orderTime");
        int    tableID     = toInt(request.getParameter("tableID"), -1);

        Customer customer = getCustomer(request);
        if (customer == null) {
            // Chưa đăng nhập: Cất tạm dữ liệu vào tủ đồ Session chờ khách login quay lại
            HttpSession session = request.getSession(true);
            session.setAttribute("tempOrderTime", dateTimeStr);
            session.setAttribute("tempTableID", tableID);
            goLogin(request, response);
            return;
        }

        if (tableID < 0) {
            request.setAttribute("error", "Vui lòng chọn vị trí bàn mong muốn.");
            request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
            request.setAttribute("step", "pick-time");
            forward(request, response);
            return;
        }

        String error = validateDateTime(dateTimeStr);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
            request.setAttribute("step", "pick-time");
            forward(request, response);
            return;
        }

        Timestamp orderTime = parseTimestamp(dateTimeStr);
        int orderID = orderDAO.createReservation(
                customer.getCustomerID(), tableID, orderTime, BigDecimal.ZERO);

        if (orderID < 0) {
            request.setAttribute("error", "Lỗi tạo đơn hệ thống. Vui lòng thử lại.");
            request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
            request.setAttribute("step", "pick-time");
            forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("lastReservation", orderDAO.getOrderByID(orderID));
        session.setAttribute("assignedTable",   tableDAO.getTableByID(tableID));
        
        session.removeAttribute("tempOrderTime");
        session.removeAttribute("tempTableID");
        
        response.sendRedirect(request.getContextPath() + "/reservation?action=success");
    }

    private void forward(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.getRequestDispatcher("/views/customer/reservation.jsp").forward(req, res);
    }

    private Customer getCustomer(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        return s == null ? null : (Customer) s.getAttribute("customer");
    }

    private void goLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.getSession(true).setAttribute("redirectAfterLogin",
                request.getContextPath() + "/reservation");
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private String validateDateTime(String s) {
        if (s == null || s.isBlank()) return "Vui lòng chọn ngày và giờ đến.";
        try {
            if (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(s).getTime()
                    < System.currentTimeMillis())
                return "Thời gian đặt bàn phải là tương lai.";
        } catch (Exception e) {
            return "Định dạng ngày giờ không hợp lệ.";
        }
        return null;
    }

    private Timestamp parseTimestamp(String s) {
        try {
            return new Timestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(s).getTime());
        } catch (Exception e) {
            return new Timestamp(System.currentTimeMillis());
        }
    }

    private int toInt(String value, int def) {
        try { return Integer.parseInt(value); } catch (Exception e) { return def; }
    }
    
    // update
}