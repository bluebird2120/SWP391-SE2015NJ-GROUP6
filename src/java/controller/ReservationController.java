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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Customer;
import model.Order;
import model.OrderReservationDetail;
import model.Table;

@WebServlet(name = "ReservationController", urlPatterns = { "/reservation" })
public class ReservationController extends HttpServlet {

    private final TableDAO tableDAO = new TableDAO();
    private final OrderDAOSon orderDAO = new OrderDAOSon();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("history".equals(action)) {
            Customer customer = getCustomer(request);
            if (customer == null) {
                saveRedirectAndGoLogin(request, response,
                        request.getContextPath() + "/reservation?action=history");
                return;
            }

            int customerID = customer.getCustomerID();
            request.setAttribute("orders", orderDAO.getReservationsByCustomer(customerID));
            request.setAttribute("reservationDetails",
                    orderDAO.getReservationDetailsByCustomer(customerID));
            request.setAttribute("step", "history");
            forward(request, response);
            return;
        }

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

        if ("choosetable".equals(action)) {
            String dateTimeStr = request.getParameter("orderTime");
            String areaType = request.getParameter("areaType");

            if (dateTimeStr == null || dateTimeStr.isBlank()
                    || areaType == null || areaType.isBlank()) {
                showPickTime(request, response,
                        "Vui lòng chọn ngày giờ và khu vực.");
                return;
            }

            String dtError = validateDateTime(dateTimeStr);
            if (dtError != null) {
                showPickTime(request, response, dtError);
                return;
            }

            Timestamp orderTime = parseTimestamp(dateTimeStr);
            request.setAttribute("orderTime", dateTimeStr);
            request.setAttribute("areaType", areaType);
            request.setAttribute("tableGroups",
                    tableDAO.findAvailableTableGroups(areaType, orderTime));
            request.setAttribute("step", "choose-table");
            request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
            forward(request, response);
            return;
        }

        if ("success".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("lastReservation") == null) {
                response.sendRedirect(request.getContextPath() + "/reservation");
                return;
            }

            Order order = (Order) session.getAttribute("lastReservation");
            @SuppressWarnings("unchecked")
            List<OrderReservationDetail> details = (List<OrderReservationDetail>) session.getAttribute(
                    "lastReservationDetails");

            session.removeAttribute("lastReservation");
            session.removeAttribute("lastReservationDetails");
            request.setAttribute("order", order);
            request.setAttribute("orderDetails", details);
            request.setAttribute("step", "success");
            forward(request, response);
            return;
        }

        request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
        request.setAttribute("step", "pick-time");
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String dateTimeStr = request.getParameter("orderTime");
        String areaType = request.getParameter("areaType");

        if (areaType == null || areaType.isBlank()) {
            showPickTime(request, response, "Vui lòng chọn khu vực.");
            return;
        }

        String dtError = validateDateTime(dateTimeStr);
        if (dtError != null) {
            showPickTime(request, response, dtError);
            return;
        }

        Timestamp orderTime = parseTimestamp(dateTimeStr);
        Customer customer = getCustomer(request);

        if (customer == null) {
            HttpSession session = request.getSession(true);
            try {
                String redirectUrl = request.getContextPath()
                        + "/reservation?action=choosetable"
                        + "&orderTime=" + URLEncoder.encode(dateTimeStr, "UTF-8")
                        + "&areaType=" + URLEncoder.encode(areaType, "UTF-8");
                session.setAttribute("redirectAfterLogin", redirectUrl);
            } catch (Exception e) {
                session.setAttribute("redirectAfterLogin",
                        request.getContextPath() + "/reservation");
            }

            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Luôn kiểm tra lại ngay trước khi tạo đơn vì số bàn trống có thể đổi.
        List<Table> tableGroups = tableDAO.findAvailableTableGroups(areaType, orderTime);
        List<OrderReservationDetail> details = new ArrayList<>();
        Map<Integer, Integer> selectedQuantities = new HashMap<>();

        for (Table group : tableGroups) {
            int quantity = toInt(
                    request.getParameter("quantity_" + group.getCapacity()), 0);
            if (quantity > 0) {
                selectedQuantities.put(group.getCapacity(), quantity);
                details.add(new OrderReservationDetail(
                        0, 0, group.getCapacity(), areaType, quantity));
            }
        }

        if (details.isEmpty()) {
            showChooseTable(request, response, tableGroups, dateTimeStr, areaType,
                    selectedQuantities, "Vui lòng chọn ít nhất một bàn.");
            return;
        }

        for (OrderReservationDetail detail : details) {
            Table group = findGroup(tableGroups, detail.getCapacity());
            if (group == null || detail.getQuantity() > group.getIsActive()) {
                showChooseTable(request, response, tableGroups, dateTimeStr, areaType,
                        selectedQuantities,
                        "Số lượng bàn " + detail.getCapacity()
                                + " chỗ không còn đủ. Vui lòng chọn lại.");
                return;
            }
        }

        int orderID = orderDAO.createReservation(
                customer.getCustomerID(), orderTime, details, BigDecimal.ZERO);

        if (orderID < 0) {
            showChooseTable(request, response, tableGroups, dateTimeStr, areaType,
                    selectedQuantities, "Lỗi hệ thống. Vui lòng thử lại.");
            return;
        }

        Order order = orderDAO.getOrderByID(orderID);
        HttpSession session = request.getSession(true);
        session.setAttribute("lastReservation", order);
        session.setAttribute("lastReservationDetails", details);
        session.removeAttribute("redirectAfterLogin");

        response.sendRedirect(request.getContextPath() + "/reservation?action=success");
    }

    private Table findGroup(List<Table> groups, int capacity) {
        for (Table group : groups) {
            if (group.getCapacity() == capacity) {
                return group;
            }
        }
        return null;
    }

    private void showPickTime(HttpServletRequest request, HttpServletResponse response,
            String error) throws ServletException, IOException {
        request.setAttribute("error", error);
        request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
        request.setAttribute("step", "pick-time");
        forward(request, response);
    }

    private void showChooseTable(HttpServletRequest request,
            HttpServletResponse response, List<Table> tableGroups,
            String dateTimeStr, String areaType,
            Map<Integer, Integer> selectedQuantities, String error)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        request.setAttribute("tableGroups", tableGroups);
        request.setAttribute("orderTime", dateTimeStr);
        request.setAttribute("areaType", areaType);
        request.setAttribute("selectedQuantities", selectedQuantities);
        request.setAttribute("areaTypes", tableDAO.getAllAreaTypes());
        request.setAttribute("step", "choose-table");
        forward(request, response);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/customer/reservation.jsp")
                .forward(request, response);
    }

    private Customer getCustomer(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Customer) session.getAttribute("customer");
    }

    private void saveRedirectAndGoLogin(HttpServletRequest request,
            HttpServletResponse response, String redirectUrl) throws IOException {
        HttpSession session = request.getSession(true);
        session.setAttribute("redirectAfterLogin", redirectUrl);
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private String validateDateTime(String value) {
        if (value == null || value.isBlank()) {
            return "Vui lòng chọn ngày và giờ đến.";
        }

        try {
            long selectedTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
                    .parse(value).getTime();
            if (selectedTime < System.currentTimeMillis() - 5 * 60 * 1000) {
                return "Thời gian đặt bàn phải là tương lai.";
            }
        } catch (Exception e) {
            return "Định dạng ngày giờ không hợp lệ.";
        }

        return null;
    }

    private Timestamp parseTimestamp(String value) {
        try {
            return new Timestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
                    .parse(value).getTime());
        } catch (Exception e) {
            return new Timestamp(System.currentTimeMillis());
        }
    }

    private int toInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
