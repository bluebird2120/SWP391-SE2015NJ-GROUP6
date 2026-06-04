package controller;

import dal.OrderDAO;
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
    private final OrderDAO orderDAO = new OrderDAO();

    // ── GET ──────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // ── Lịch sử đặt bàn (BẮT BUỘC ĐĂNG NHẬP) ─────────────────────────────
        if ("history".equals(action)) {
            Customer customer = getCustomer(request);
            if (customer == null) { goLogin(request, response); return; }

            List<Order> orders = orderDAO.getReservationsByCustomer(customer.getCustomerID());
            request.setAttribute("orders", orders);
            request.setAttribute("step", "history");
            forward(request, response);
            return;
        }

        // ── Huỷ đơn (BẮT BUỘC ĐĂNG NHẬP) ─────────────────────────────────────
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

        // ── Bước 2: Hiển thị danh sách bàn trống + Xử lý gợi ý thông minh ──
        if ("choosetable".equals(action)) {
            String dateTimeStr = request.getParameter("orderTime");
            String areaType    = request.getParameter("areaType");

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
            request.setAttribute("areaLabel", "public".equals(areaType) ? "Ngoài sảnh" : "Trong phòng");

            // Kịch bản: Hết loại bàn yêu cầu (Ví dụ hết bàn 2 chỗ) -> Kích hoạt gợi ý thông minh
            if (tables.isEmpty()) {
                List<Table> higherTables = tableDAO.findAlternativeTablesHigherCapacity(areaType, orderTime, 2);
                List<Table> otherAreaTables = tableDAO.findAlternativeTablesOtherArea(areaType, orderTime);

                request.setAttribute("higherTables", higherTables);
                request.setAttribute("otherAreaTables", otherAreaTables);
                request.setAttribute("step", "no-table-suggest"); 
            } else {
                request.setAttribute("tables", tables);
                request.setAttribute("step", "choose-table");
            }
            forward(request, response);
            return;
        }

        // ── Bước chọn món ăn đặt trước (Tạm thời để trống chờ nạp Menu) ──
        if ("choosefood".equals(action)) {
            String dateTimeStr = request.getParameter("orderTime");
            String tableID     = request.getParameter("tableID");

            request.setAttribute("orderTime", dateTimeStr);
            request.setAttribute("tableID",    tableID);
            request.setAttribute("step",       "choose-food");
            forward(request, response);
            return;
        }

        // ── Bước 3: Trang xác nhận thành công ───────────────────────────
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

        // ── Bước 1 (mặc định): Hoặc Tự động khôi phục dữ liệu sau khi đăng nhập thành công ──
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("customer") != null) {
            String tempOrderTime = (String) session.getAttribute("tempOrderTime");
            Integer tempTableID  = (Integer) session.getAttribute("tempTableID");

            // Nếu trong tủ đồ session có lưu thông tin chọn bàn dở trước khi bị bắt login
            if (tempOrderTime != null && tempTableID != null && tempTableID > 0) {
                // Tự động nhảy cóc đưa khách sang thẳng Bước chọn món với data cũ luôn!
                request.setAttribute("orderTime", tempOrderTime);
                request.setAttribute("tableID",    String.valueOf(tempTableID));
                request.setAttribute("step",       "choose-food");
                forward(request, response);
                return;
            }
        }

        // Nếu là khách mới tinh chưa chọn gì hoặc không có đơn chờ, hiển thị Bước 1 như cũ
        request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
        request.setAttribute("step", "pick-time");
        forward(request, response);
    }

    // ── POST: Khách xác nhận bước cuối → Tạo đơn (BẮT ĐẦU CHECK LOGIN) ──
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String dateTimeStr = request.getParameter("orderTime");
        int    tableID     = toInt(request.getParameter("tableID"), -1);

        // Kiểm tra xem người dùng hiện tại đã đăng nhập hay chưa
        Customer customer = getCustomer(request);
        
        if (customer == null) {
            // CHƯA ĐĂNG NHẬP: Lưu trữ các tham số đã chọn vào session tạm thời
            HttpSession session = request.getSession(true);
            session.setAttribute("tempOrderTime", dateTimeStr);
            session.setAttribute("tempTableID", tableID);
            
            // Đẩy hướng người dùng tới trang đăng nhập/đăng ký tài khoản
            goLogin(request, response);
            return;
        }

        // NẾU ĐÃ ĐĂNG NHẬP CHUẨN CHỈ: Tiến hành xử lý tạo đơn đặt chỗ
        if (tableID < 0) {
            request.setAttribute("error", "Vui lòng chọn một bàn.");
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
            request.setAttribute("error", "Đã có lỗi khi tạo đơn. Vui lòng thử lại.");
            request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
            request.setAttribute("step", "pick-time");
            forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("lastReservation", orderDAO.getOrderByID(orderID));
        session.setAttribute("assignedTable",   tableDAO.getTableByID(tableID));
        
        // Dọn dẹp session tạm sau khi hoàn tất đặt bàn thành công
        session.removeAttribute("tempOrderTime");
        session.removeAttribute("tempTableID");
        
        response.sendRedirect(request.getContextPath() + "/reservation?action=success");
    }

    // ── Helpers ──────────────────────────────────────────────────────────

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
        try { return Integer.parseInt(value); }
        catch (Exception e) { return def; }
    }
}