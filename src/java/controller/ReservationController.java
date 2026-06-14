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
import java.net.URLEncoder;
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

    // ═══════════════════════════════════════════════════════════════
    //  GET
    // ═══════════════════════════════════════════════════════════════
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // ── 1. Lịch sử đặt bàn ──────────────────────────────────
        if ("history".equals(action)) {
            Customer customer = getCustomer(request);
            if (customer == null) {
                saveRedirectAndGoLogin(request, response,
                        request.getContextPath() + "/reservation?action=history");
                return;
            }
            List<Order> orders = orderDAO.getReservationsByCustomer(customer.getCustomerID());
            request.setAttribute("orders", orders);
            request.setAttribute("step", "history");
            forward(request, response);
            return;
        }

        // ── 2. Hủy đơn ──────────────────────────────────────────
        if ("cancel".equals(action)) {
            Customer customer = getCustomer(request);
            if (customer == null) {
                saveRedirectAndGoLogin(request, response,
                        request.getContextPath() + "/reservation?action=history");
                return;
            }
            int orderID = toInt(request.getParameter("orderID"), -1);
            if (orderID > 0) {
                orderDAO.cancelReservation(orderID, customer.getCustomerID());
            }
            response.sendRedirect(request.getContextPath() + "/reservation?action=history");
            return;
        }

        // ── 3. Hiển thị danh sách loại bàn theo capacity ────────
        if ("choosetable".equals(action)) {
            String dateTimeStr = request.getParameter("orderTime");
            String areaType = request.getParameter("areaType");
            String capacityStr = request.getParameter("capacity");

            if (dateTimeStr == null || dateTimeStr.isBlank()
                    || areaType == null || areaType.isBlank()) {
                request.setAttribute("error", "Vui lòng chọn ngày giờ và khu vực.");
                request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
                request.setAttribute("step", "pick-time");
                forward(request, response);
                return;
            }

            Timestamp orderTime = parseTimestamp(dateTimeStr);
            List<Table> tableGroups = tableDAO.findAvailableTableGroups(areaType, orderTime);

            request.setAttribute("orderTime", dateTimeStr);
            request.setAttribute("areaType", areaType);
            request.setAttribute("capacity", capacityStr);
            request.setAttribute("tableGroups", tableGroups);
            request.setAttribute("step", "choose-table");
            request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
            forward(request, response);
            return;
        }

        // ── 4. Trang thành công ──────────────────────────────────
        if ("success".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("lastReservation") == null) {
                response.sendRedirect(request.getContextPath() + "/reservation");
                return;
            }
            Order order = (Order) session.getAttribute("lastReservation");
            session.removeAttribute("lastReservation");

            request.setAttribute("order", order);
            request.setAttribute("step", "success");
            forward(request, response);
            return;
        }

        // ── 5. Mặc định: trang chọn ngày giờ ────────────────────
        request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
        request.setAttribute("step", "pick-time");
        forward(request, response);
    }

    // ═══════════════════════════════════════════════════════════════
    //  POST — Xác nhận đặt bàn
    // ═══════════════════════════════════════════════════════════════
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String dateTimeStr = request.getParameter("orderTime");
        String areaType = request.getParameter("areaType");
        int capacity = toInt(request.getParameter("capacity"), -1);

        // ── Chưa đăng nhập → lưu params rồi redirect login ──────
        Customer customer = getCustomer(request);
        if (customer == null) {
            HttpSession session = request.getSession(true);
            try {
                String redirectUrl = request.getContextPath()
                        + "/reservation?action=choosetable"
                        + "&orderTime=" + URLEncoder.encode(dateTimeStr != null ? dateTimeStr : "", "UTF-8")
                        + "&areaType=" + URLEncoder.encode(areaType != null ? areaType : "", "UTF-8")
                        + "&capacity=" + capacity;
                session.setAttribute("redirectAfterLogin", redirectUrl);
            } catch (Exception e) {
                session.setAttribute("redirectAfterLogin",
                        request.getContextPath() + "/reservation");
            }
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // ── Validate capacity ────────────────────────────────────
        if (capacity <= 0) {
            request.setAttribute("error", "Vui lòng chọn quy mô bàn mong muốn.");
            request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
            request.setAttribute("step", "pick-time");
            forward(request, response);
            return;
        }

        // ── Validate datetime ────────────────────────────────────
        String dtError = validateDateTime(dateTimeStr);
        if (dtError != null) {
            request.setAttribute("error", dtError);
            request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
            request.setAttribute("step", "pick-time");
            forward(request, response);
            return;
        }

        Timestamp orderTime = parseTimestamp(dateTimeStr);

        // ── Kiểm tra còn bàn trống không trước khi tạo đơn ──────
        // findAvailableTableGroups tự động lazy-expire rồi mới tính
        List<Table> tableGroups = tableDAO.findAvailableTableGroups(areaType, orderTime);
        boolean hasAvailable = tableGroups.stream()
                .anyMatch(t -> t.getCapacity() == capacity && t.getIsActive() > 0);

        if (!hasAvailable) {
            request.setAttribute("error",
                    "Xin lỗi, không còn bàn " + capacity
                    + " chỗ trống tại khu vực này. Vui lòng chọn loại bàn khác.");
            request.setAttribute("tableGroups", tableGroups);
            request.setAttribute("orderTime", dateTimeStr);
            request.setAttribute("areaType", areaType);
            request.setAttribute("capacity", String.valueOf(capacity));
            request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
            request.setAttribute("step", "choose-table");
            forward(request, response);
            return;
        }

        // ── Tạo đơn: orderStatus = 'reserved', tableStatus = 'reserved' ──
        int orderID = orderDAO.createReservation(
                customer.getCustomerID(),
                capacity,
                areaType,
                orderTime,
                BigDecimal.ZERO
        );

        if (orderID < 0) {
            request.setAttribute("error", "Lỗi hệ thống. Vui lòng thử lại.");
            request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
            request.setAttribute("step", "pick-time");
            forward(request, response);
            return;
        }

        // ── Thành công ───────────────────────────────────────────
        Order order = orderDAO.getOrderByID(orderID);
        HttpSession session = request.getSession(true);
        session.setAttribute("lastReservation", order);
        session.removeAttribute("redirectAfterLogin");

        response.sendRedirect(request.getContextPath() + "/reservation?action=success");
    }

    // ═══════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════
    private void forward(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.getRequestDispatcher("/views/customer/reservation.jsp").forward(req, res);
    }

    private Customer getCustomer(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        return s == null ? null : (Customer) s.getAttribute("customer");
    }

    private void saveRedirectAndGoLogin(HttpServletRequest request,
            HttpServletResponse response,
            String redirectUrl) throws IOException {
        HttpSession session = request.getSession(true);
        session.setAttribute("redirectAfterLogin", redirectUrl);
        response.sendRedirect(request.getContextPath() + "/login");
    }

    /**
     * Thời gian đặt bàn phải trong tương lai (cho phép trễ 5 phút).
     */
    private String validateDateTime(String s) {
        if (s == null || s.isBlank()) {
            return "Vui lòng chọn ngày và giờ đến.";
        }
        try {
            long t = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(s).getTime();
            if (t < System.currentTimeMillis() - 5 * 60 * 1000) {
                return "Thời gian đặt bàn phải là tương lai.";
            }
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
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return def;
        }
    }
}
