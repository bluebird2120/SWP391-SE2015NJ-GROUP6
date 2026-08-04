package controller;

import dal.CookingMethodDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;
import model.MenuCategory;
import dal.MenuCategoryDAO;
import dal.MenuItemDAO;
import jakarta.servlet.http.HttpSession;
import model.MenuItem;
import dal.TableDAO;
import dal.OrderDAO;
import model.CookingMethod;
import model.Table;
import model.Order;
import model.Employee;

@WebServlet(name = "MenuItemController", urlPatterns = "/menu")
/**
 * HIỂN THỊ MENU SAU KHI QUÉT QR.
 *
 * <p>doGet kiểm tra trạng thái bàn, đọc bộ lọc/phân trang, lấy danh sách món
 * và forward user/menu.jsp. Form thêm giỏ POST sang OrderController.</p>
 */
public class MenuItemController extends HttpServlet {

    @Override
    /** Chuẩn bị menu, bộ lọc và URL quay lại để giữ trang sau khi thêm món. */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        // [QR ROUTING] Link QR cũ dạng /menu?token=... cũng phải đi qua
        // ScanQRController; MenuItemController chỉ chịu trách nhiệm hiển thị menu.
        String token = request.getParameter("token");
        if (token != null && !token.isEmpty()) {
            // [QR FLOW] Tat ca luong quet QR di qua ScanQRController de tranh bypass buoc xac nhan ban.
            response.sendRedirect(request.getContextPath() + "/scan?token="
                    + URLEncoder.encode(token, "UTF-8"));
            return;
        }

        // [SECURITY FIX - QR FLOW] Khách đã quét QR nhưng bàn còn pending
        // không được tự gõ /menu để bỏ qua màn hình chờ nhân viên mở bàn.
        Integer waitingOrderID = (Integer) session.getAttribute("orderID");
        Employee sessionEmployee = (Employee) session.getAttribute("employee");
        if (sessionEmployee == null && waitingOrderID != null
                && session.getAttribute("currentTableID") != null) {
            Order waitingOrder = new OrderDAO().getOrderById(waitingOrderID);
            if (waitingOrder != null
                    && ("pending".equals(waitingOrder.getTableStatus())
                    || waitingOrder.getIsStaffConfirmed() != 1)) {
                session.setAttribute("pendingOrderID", waitingOrderID);
                request.getRequestDispatcher("/views/user/waiting_staff.jsp")
                        .forward(request, response);
                return;
            }
        }
        String search = request.getParameter("search");
        String category_raw = request.getParameter("category");
        String method_raw = request.getParameter("cookingMethod");
        String status_raw = request.getParameter("status");
        String minPrice_raw = request.getParameter("minPrice");
        String maxPrice_raw = request.getParameter("maxPrice");
        String price_raw = request.getParameter("price");
        String sort_raw = request.getParameter("sort");
        String page_raw = request.getParameter("page");
        //Validate
        if (!checkEmpty(search)) {
            search = "";
        }
        String sort = validateStringWhitelist(sort_raw, "asc", "asc", "desc");
        String priceType = validateStringWhitelist(price_raw, "discountedPrice", "price", "discountedPrice");

        int status = parseIntSafe(status_raw, -1, -1);
        // Lấy tài khoản nhân viên trước khi truy vấn dữ liệu
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null) {
            status = 1;
        }
        int categoryId = parseIntSafe(category_raw, 0, 0);
        int methodID = parseIntSafe(method_raw, 0, 0);

        int minPrice = parseIntSafe(minPrice_raw, 0, 0);
        int maxPrice = parseIntSafe(maxPrice_raw, Integer.MAX_VALUE, 0);

        int page = parseIntSafe(page_raw, 1, 1);
        String errorPrice = checkPriceInput(minPrice, maxPrice);
        String errorSearch = isValidString(search, 100, "Tìm kiếm không vượt quá 100 kí tự");

        if (errorPrice != null) {
            request.setAttribute("errorPrice", errorPrice);
            minPrice = 0;
            maxPrice = Integer.MAX_VALUE;
        }
        if (errorSearch != null) {
            request.setAttribute("errorSearch", errorSearch);
            search = "";
        }

        int totalItem = mi.countSearchMenuItem(search, categoryId, methodID, status, minPrice, maxPrice, priceType);
        int totalPage = (int) Math.ceil((double) totalItem / PAGE_SIZE);

        if (page > totalPage && totalPage > 0) {
            page = totalPage;
        }

        int offSet = (page - 1) * PAGE_SIZE;
        List<CookingMethod> listMethod = cm.getAllCookingMethod();
        List<MenuCategory> Categorylist = md.getAllMenuCategory();
        List<MenuItem> listItem = mi.searchMenuItemPaging(search, categoryId, methodID, status, minPrice, maxPrice, sort, priceType, offSet, PAGE_SIZE);

        request.setAttribute("listMethod", listMethod);
        request.setAttribute("list", Categorylist);
        request.setAttribute("listItem", listItem);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("currentPage", page);

        request.setAttribute("currentSearch", search);
        request.setAttribute("currentCategory", categoryId);
        request.setAttribute("currentMethod", methodID);
        request.setAttribute("currentStatus", status);
        request.setAttribute("currentMinPrice", (!checkEmpty(minPrice_raw) || errorPrice != null) ? "" : minPrice);
        request.setAttribute("currentMaxPrice", (!checkEmpty(maxPrice_raw) || errorPrice != null) ? "" : maxPrice);
        request.setAttribute("currentPriceType", priceType);
        request.setAttribute("currentSort", sort);

        // Lấy các bàn đã ghép vào order để khách chọn đúng bàn khi gọi món.
        Integer currentOrderID = (Integer) session.getAttribute("orderID");
        if (currentOrderID != null) {
            TableDAO tDAO = new TableDAO();
            List<Table> assignedTables = tDAO.getTablesByOrderId(currentOrderID);
            request.setAttribute("assignedTables", assignedTables);
        }
        Integer sessionTableID = (Integer) session.getAttribute("currentTableID");
        boolean isReservationPreorder = "true".equals(request.getParameter("reservation"))
                || Boolean.TRUE.equals(session.getAttribute("reservationFlow"));

        // Lưu url trang hiện tại để gửi sang trang update và create
        String currentMenuUrl = request.getRequestURI();
        if (request.getQueryString() != null
                && !request.getQueryString().isBlank()) {
            currentMenuUrl += "?" + request.getQueryString();
        }
        request.setAttribute("returnUrl", currentMenuUrl);
        // [DISH DETAIL BACK URL] Ghi nhớ đúng trang menu đang xem, gồm cả
        // phân trang và bộ lọc, để nút "Quay lại Menu" không rơi về /home.
        session.setAttribute("lastDishListUrl", currentMenuUrl);

        // [PREORDER ROUTING FIX] Luong dat mon truoc sau khi coc ban phai dung
        // owner/dish-list.jsp vi form o trang nay submit ve /reservation?action=addPreorderItem.
        // Khach quet QR tai ban van co currentTableID nen tiep tuc di vao user/menu.jsp nhu cu.
        if (sessionTableID == null && (loginUser != null || isReservationPreorder)) {
            String currentUrl = request.getRequestURI();
            if (request.getQueryString() != null) {
                currentUrl += "?" + request.getQueryString();
            }
            session.setAttribute("lastDishListUrl", currentUrl);
            request.getRequestDispatcher("/views/owner/dish-list.jsp").forward(request, response);
        } else {
            // [MENU ROUTING FIX] Khách chưa quét QR vẫn phải thấy menu công khai,
            // không được forward nhầm sang màn quản lý món của Owner.
            // Còn lại (Khách vãng lai, Khách quét QR) -> Trỏ vào trang Menu User
            request.getRequestDispatcher("/views/user/menu.jsp").forward(request, response);
        }

    }

    /** Parse số nguyên an toàn cho page và các tham số số từ query string. */
    private int parseIntSafe(String value, int defaultValue, int minValue) {
        if (!checkEmpty(value)) {
            return defaultValue;
        }
        try {
            int result = Integer.parseInt(value.trim());
            return (result < minValue) ? minValue : result;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private MenuCategoryDAO md = new MenuCategoryDAO();
    private MenuItemDAO mi = new MenuItemDAO();
    private CookingMethodDAO cm = new CookingMethodDAO();
    private static final int PAGE_SIZE = 8;

    private String isValidString(String data, int length, String ms) {
        if (data.length() > length) {
            return ms;
        }
        return null;
    }

    private String checkPriceInput(int min, int max) {
        if (min < 0 || max < 0) {
            return "Giá món ăn không được là số âm";
        } else {
            if (min > max) {
                return "Giá max phải lớn hơn giá Min";
            }
        }
        return null;
    }

    private boolean checkEmpty(String data) {
        return (data != null && !data.trim().isEmpty());
    }

    private String validateStringWhitelist(String value, String defaultValue, String... allowedValues) {
        if (!checkEmpty(value)) {
            return defaultValue;
        }
        String trimmedValue = value.trim().toLowerCase();
        for (String allowed : allowedValues) {
            if (trimmedValue.equals(allowed.toLowerCase())) {
                return allowed;
            }
        }
        return defaultValue;
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
