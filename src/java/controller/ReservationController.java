package controller;

import dal.OrderDAOSon;
import dal.OrderDAO;
import dal.MenuItemDAO;
import dal.TableDAO;
import dal.BusinessScheduleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
//import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Customer;
import model.MenuItem;
import model.Order;
import model.OrderItem;
import model.OrderReservationDetail;
import model.Table;

@WebServlet(name = "ReservationController", urlPatterns = { "/reservation" })
public class ReservationController extends HttpServlet {

    private final TableDAO tableDAO = new TableDAO();
    private final OrderDAOSon orderDAO = new OrderDAOSon();
    private final OrderDAO preorderDAO = new OrderDAO();
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final BusinessScheduleDAO businessScheduleDAO = new BusinessScheduleDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        orderDAO.synchronizeDepositStatus();
        cleanFinishedReservationSession(request);
        String action = request.getParameter("action");

        // Hiển thị giỏ riêng của đơn đặt bàn online.
        if ("preorderCart".equals(action)) {
            showPreorderCart(request, response);
            return;
        }

        // [PREORDER AFTER DEPOSIT] Khách chỉ được chọn món sau khi
        // tiền cọc bàn đã thanh toán và đơn chuyển sang reserved.
        if ("preorder".equals(action)) {
            openPreorder(request, response);
            return;
        }

        if ("history".equals(action)) {
            Customer customer = getCustomer(request);
            if (customer == null) {
                saveRedirectAndGoLogin(request, response,
                        request.getContextPath() + "/reservation?action=history");
                return;
            }

            int customerID = customer.getCustomerID();
            List<Order> reservations = orderDAO.getReservationsByCustomer(customerID);
            request.setAttribute("orders", reservations);
            request.setAttribute("reservationDetails",
                    orderDAO.getReservationDetailsByCustomer(customerID));
            // Gắn món đặt trước vào từng đơn đặt bàn
            // để khách xem lịch sử là thấy luôn bàn + món + tổng tiền món.
            Map<Integer, List<OrderItem>> preorderItemsByOrder = new HashMap<>();
            Map<Integer, List<MenuItem>> preorderMenusByOrder = new HashMap<>();
            Map<Integer, Integer> preorderTotalsByOrder = new HashMap<>();
            for (Order reservation : reservations) {
                List<OrderItem> orderItems
                        = preorderDAO.getOrderItemsByOrderId(reservation.getOrderID());
                List<MenuItem> menuItems
                        = preorderDAO.getMenuItemsByOrderId(reservation.getOrderID());
                int preorderTotal = 0;
                for (OrderItem item : orderItems) {
                    preorderTotal += item.getPrice() * item.getQuantity();
                }
                preorderItemsByOrder.put(reservation.getOrderID(), orderItems);
                preorderMenusByOrder.put(reservation.getOrderID(), menuItems);
                preorderTotalsByOrder.put(reservation.getOrderID(), preorderTotal);
            }
            request.setAttribute("preorderItemsByOrder", preorderItemsByOrder);
            request.setAttribute("preorderMenusByOrder", preorderMenusByOrder);
            request.setAttribute("preorderTotalsByOrder", preorderTotalsByOrder);
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
            // [OPERATING HOURS] Khong cho khach chon ban ngoai gio hoat dong cua nha hang.
            String businessHourError = businessScheduleDAO.validateReservationTime(orderTime);
            if (businessHourError != null) {
                showPickTime(request, response, businessHourError);
                return;
            }

            Map<String, Integer> selectedQuantities
                    = parseSelectedQuantities(request);
            request.setAttribute("orderTime", dateTimeStr);
            request.setAttribute("areaType", areaType);
            request.setAttribute("tableGroups",
                    tableDAO.findAvailableTableGroups(areaType, orderTime));
            request.setAttribute("selectedQuantities", selectedQuantities);
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

        orderDAO.synchronizeDepositStatus();
        cleanFinishedReservationSession(request);
        String action = request.getParameter("action");

        //  Các thao tác giỏ đặt trước được xử lý trước
        // form đặt bàn để không yêu cầu lại orderTime/areaType.
        if ("addPreorderItem".equals(action)) {
            addPreorderItem(request, response);
            return;
        }
        if ("updatePreorderItem".equals(action)) {
            updatePreorderItem(request, response);
            return;
        }
        if ("removePreorderItem".equals(action)) {
            removePreorderItem(request, response);
            return;
        }
        if ("confirmPreorder".equals(action)) {
            confirmPreorder(request, response);
            return;
        }

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
        // [OPERATING HOURS] Kiem tra lai truoc khi tao don de tranh khach bypass buoc chon ban.
        String businessHourError = businessScheduleDAO.validateReservationTime(orderTime);
        if (businessHourError != null) {
            showPickTime(request, response, businessHourError);
            return;
        }

        Customer customer = getCustomer(request);

        if (customer == null) {
            HttpSession session = request.getSession(true);
            try {
                StringBuilder redirectUrl = new StringBuilder();
                redirectUrl.append(request.getContextPath())
                        .append("/reservation?action=choosetable")
                        .append("&orderTime=")
                        .append(URLEncoder.encode(dateTimeStr, "UTF-8"))
                        .append("&areaType=")
                        .append(URLEncoder.encode(areaType, "UTF-8"));
                for (Map.Entry<String, Integer> entry
                        : parseSelectedQuantities(request).entrySet()) {
                    redirectUrl.append("&selection_")
                            .append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                            .append("=")
                            .append(entry.getValue());
                }
                session.setAttribute("redirectAfterLogin", redirectUrl.toString());
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
        Map<String, Integer> selectedQuantities
                = parseSelectedQuantities(request);

        for (Map.Entry<String, Integer> entry : selectedQuantities.entrySet()) {
            SelectionKey selection = parseSelectionKey(entry.getKey());
            if (selection != null) {
                details.add(new OrderReservationDetail(
                        0, 0, selection.capacity, selection.areaType,
                        entry.getValue()));
            }
        }

        if (details.isEmpty()) {
            showChooseTable(request, response, tableGroups, dateTimeStr, areaType,
                    selectedQuantities, "Vui lòng chọn ít nhất một bàn.");
            return;
        }

        Map<String, List<Table>> groupsByArea = new HashMap<>();
        groupsByArea.put(areaType, tableGroups);
        for (OrderReservationDetail detail : details) {
            List<Table> areaGroups = groupsByArea.computeIfAbsent(
                    detail.getAreaType(),
                    key -> tableDAO.findAvailableTableGroups(key, orderTime));
            Table group = findGroup(areaGroups, detail.getCapacity());
            if (group == null || detail.getQuantity() > group.getIsActive()) {
                showChooseTable(request, response, tableGroups, dateTimeStr, areaType,
                        selectedQuantities,
                        "Số lượng bàn " + detail.getCapacity()
                                + " chỗ không còn đủ. Vui lòng chọn lại.");
                return;
            }
        }

        int orderID = orderDAO.createReservation(
                customer.getCustomerID(), orderTime, details,
                Integer.valueOf(OrderDAOSon.DEFAULT_DEPOSIT_AMOUNT));

        if (orderID < 0) {
            showChooseTable(request, response, tableGroups, dateTimeStr, areaType,
                    selectedQuantities, "Lỗi hệ thống. Vui lòng thử lại.");
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("orderID", orderID);
        session.setAttribute("reservationOrderID", orderID);
        session.setAttribute("reservationFlow", true);
        session.setAttribute("depositAmount",
                OrderDAOSon.DEFAULT_DEPOSIT_AMOUNT);
        session.setAttribute("reservationHoldExpiresAt",
                System.currentTimeMillis()
                + OrderDAOSon.HOLD_MINUTES * 60_000L);
        session.removeAttribute("redirectAfterLogin");

        // [DEPOSIT FIRST] Sau khi chọn bàn luôn thanh toán cọc bàn ngay.
        // Thời gian giữ chỗ 5 phút chỉ dành cho bước thanh toán này.
        int invoiceID = orderDAO.createDepositInvoice(
                orderID, OrderDAOSon.DEFAULT_DEPOSIT_AMOUNT);
        if (invoiceID < 0) {
            orderDAO.cancelReservation(
                    orderID, customer.getCustomerID());
            cleanFinishedReservationSession(request);
            showChooseTable(request, response, tableGroups,
                    dateTimeStr, areaType, selectedQuantities,
                    "Không thể tạo hóa đơn cọc. Vui lòng thử lại.");
            return;
        }
        session.setAttribute("invoiceID", invoiceID);
        response.sendRedirect(request.getContextPath() + "/payment");
    }

    private void openPreorder(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Customer customer = getCustomer(request);
        if (customer == null) {
            saveRedirectAndGoLogin(request, response,
                    request.getContextPath() + "/reservation?action=history");
            return;
        }

        int orderID = toInt(request.getParameter("orderID"), -1);
        Order reservation = orderDAO.getOrderByID(orderID);
        if (!isEditableReservedOrder(reservation, customer)) {
            response.sendRedirect(request.getContextPath()
                    + "/reservation?action=history");
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("reservationOrderID", orderID);
        session.setAttribute("reservationFlow", true);
        response.sendRedirect(request.getContextPath()
                + "/menu?reservation=true&orderID=" + orderID);
    }

    /**
     *  Mở trang giỏ món riêng của khách đặt bàn trước.
     */
    private void showPreorderCart(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        Integer orderID = getPendingReservationOrderID(request);
        if (orderID == null) {
            response.sendRedirect(request.getContextPath() + "/reservation");
            return;
        }

        request.setAttribute("orderID", orderID);
        request.setAttribute("orderItems",
                preorderDAO.getOrderItemsByOrderId(orderID));
        request.setAttribute("menuItems",
                preorderDAO.getMenuItemsByOrderId(orderID));
        Order reservation = orderDAO.getOrderByID(orderID);
        request.setAttribute("depositAmount",
                reservation == null ? 0 : reservation.getDepositAmount());
        //  Hiển thị lỗi nhập số lượng món ăn .
        if ("invalid_quantity".equals(request.getParameter("error"))) {
            request.setAttribute("cartError",
                    "Số lượng món phải là số nguyên dương từ 1 đến 99.");
        }
        request.getRequestDispatcher(
                "/views/customer/reservation-cart.jsp")
                .forward(request, response);
    }

    private void addPreorderItem(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Integer orderID = getPendingReservationOrderID(request);
        int itemID = toInt(request.getParameter("itemID"), -1);
        int quantity = Math.max(1,
                Math.min(99, toInt(request.getParameter("quantity"), 1)));

        if (orderID == null || itemID <= 0) {
            response.sendRedirect(request.getContextPath() + "/reservation");
            return;
        }

        MenuItem item = menuItemDAO.getMenuItemById(itemID);
        if (item == null || item.getIsAvailable() != 1) {
            response.sendRedirect(request.getContextPath()
                    + "/menu?reservation=true&orderID=" + orderID
                    + "&error=item_unavailable");
            return;
        }

        int price = item.getDiscountPercent() > 0
                ? item.getDiscountedPrice() : item.getPrice();
        preorderDAO.addOrderItem(
                orderID, itemID, null, quantity, price, null);

        response.sendRedirect(request.getContextPath()
                + "/menu?reservation=true&orderID=" + orderID
                + "&success=added");
    }

    private void updatePreorderItem(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Integer orderID = getPendingReservationOrderID(request);
        int orderItemID = toInt(request.getParameter("orderItemID"), -1);
        String quantityText = request.getParameter("quantity");

        // validate nhập số lượng chọn món
        if (orderID == null || orderItemID <= 0
                || quantityText == null
                || !quantityText.matches("\\d+")) {
            response.sendRedirect(request.getContextPath()
                    + "/reservation?action=preorderCart"
                    + "&error=invalid_quantity");
            return;
        }

        int quantity = toInt(quantityText, -1);
        if (quantity >= 1 && quantity <= 99
                && ownsPreorderItem(orderID, orderItemID)) {
            preorderDAO.updateOrderItemQuantity(orderItemID, quantity);
        } else {
            response.sendRedirect(request.getContextPath()
                    + "/reservation?action=preorderCart"
                    + "&error=invalid_quantity");
            return;
        }
        redirectToPreorderCart(request, response);
    }

    private void removePreorderItem(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Integer orderID = getPendingReservationOrderID(request);
        int orderItemID = toInt(request.getParameter("orderItemID"), -1);

        if (orderID != null && ownsPreorderItem(orderID, orderItemID)) {
            preorderDAO.removeOrderItem(orderItemID);
        }
        redirectToPreorderCart(request, response);
    }

    private void confirmPreorder(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Integer orderID = getPendingReservationOrderID(request);
        Customer customer = getCustomer(request);
        if (orderID == null || customer == null
                || preorderDAO.getOrderItemsByOrderId(orderID).isEmpty()) {
            redirectToPreorderCart(request, response);
            return;
        }

        // [PREORDER AFTER DEPOSIT] Món đã nằm trong OrderItem của orderID.
        // Chỉ kết thúc phiên chọn món, không tạo/cập nhật hóa đơn cọc.
        HttpSession session = request.getSession();
        session.removeAttribute("reservationOrderID");
        session.removeAttribute("reservationFlow");
        response.sendRedirect(request.getContextPath()
                + "/reservation?action=history&preorderSaved=true");
    }

    /**
     * Chỉ cho thao tác món trên đơn đã thanh toán cọc và đang giữ bàn.
     */
    private Integer getPendingReservationOrderID(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Customer customer = getCustomer(request);
        Integer orderID = session == null ? null
                : (Integer) session.getAttribute("reservationOrderID");
        if (orderID == null || customer == null) {
            return null;
        }

        Order reservation = orderDAO.getOrderByID(orderID);
        if (reservation == null
                || reservation.getCustomerID() == null
                || !isEditableReservedOrder(reservation, customer)) {
            return null;
        }
        return orderID;
    }

    private boolean isEditableReservedOrder(
            Order reservation, Customer customer) {
        return reservation != null
                && customer != null
                && reservation.getCustomerID() != null
                && reservation.getCustomerID() == customer.getCustomerID()
                && "reserved".equals(reservation.getOrderStatus())
                && "reserved".equals(reservation.getTableStatus())
                && reservation.getInvoiceID() != null
                && reservation.getDepositAmount() > 0;
    }

    private boolean ownsPreorderItem(int orderID, int orderItemID) {
        for (OrderItem item : preorderDAO.getOrderItemsByOrderId(orderID)) {
            if (item.getOrderItemID() == orderItemID) {
                return true;
            }
        }
        return false;
    }

    private void redirectToPreorderCart(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath()
                + "/reservation?action=preorderCart");
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
            Map<String, Integer> selectedQuantities, String error)
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

    private void cleanFinishedReservationSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        Integer reservationOrderID
                = (Integer) session.getAttribute("reservationOrderID");
        if (reservationOrderID == null) {
            return;
        }

        Order reservation = orderDAO.getOrderByID(reservationOrderID);
        boolean editingPreorder
                = Boolean.TRUE.equals(session.getAttribute("reservationFlow"));
        if (reservation == null
                || "cancelled".equals(reservation.getOrderStatus())
                || ("reserved".equals(reservation.getOrderStatus())
                    && !editingPreorder)) {
            Integer sessionOrderID
                    = (Integer) session.getAttribute("orderID");
            if (reservationOrderID.equals(sessionOrderID)) {
                session.removeAttribute("orderID");
            }
            session.removeAttribute("reservationOrderID");
            session.removeAttribute("reservationFlow");
            session.removeAttribute("depositAmount");
            session.removeAttribute("reservationHoldExpiresAt");
        }
    }

    private Map<String, Integer> parseSelectedQuantities(
            HttpServletRequest request) {
        Map<String, Integer> selections = new LinkedHashMap<>();

        for (Map.Entry<String, String[]> parameter
                : request.getParameterMap().entrySet()) {
            String name = parameter.getKey();
            if (!name.startsWith("selection_")) {
                continue;
            }

            String key = name.substring("selection_".length());
            SelectionKey selection = parseSelectionKey(key);
            String[] values = parameter.getValue();
            int quantity = values == null || values.length == 0
                    ? 0 : toInt(values[values.length - 1], 0);

            if (selection != null && quantity > 0) {
                selections.put(key, quantity);
            }
        }

        return selections;
    }

    private SelectionKey parseSelectionKey(String key) {
        if (key == null) {
            return null;
        }

        int separator = key.lastIndexOf('_');
        if (separator <= 0 || separator == key.length() - 1) {
            return null;
        }

        String selectedArea = key.substring(0, separator);
        int capacity = toInt(key.substring(separator + 1), -1);
        if (selectedArea.isBlank() || capacity <= 0) {
            return null;
        }

        return new SelectionKey(selectedArea, capacity);
    }

    private static class SelectionKey {

        private final String areaType;
        private final int capacity;

        private SelectionKey(String areaType, int capacity) {
            this.areaType = areaType;
            this.capacity = capacity;
        }
    }
}
