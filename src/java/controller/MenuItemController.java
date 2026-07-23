package controller;

import dal.CookingMethodDAO;
import java.io.IOException;
import java.io.PrintWriter;
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

// === BẮT ĐẦU PHẦN THÊM MỚI (IMPORT): Nhập thêm thư viện cần thiết ===
import dal.TableDAO;
import dal.OrderDAO;
import model.CookingMethod;
import model.Table;
import model.Order;
import model.Employee;
// === KẾT THÚC PHẦN THÊM MỚI ===

@WebServlet(name = "MenuItemController", urlPatterns = "/menu")
public class MenuItemController extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Servlet MenuItemController</title></head>");
            out.println("<body><h1>Servlet MenuItemController at " + request.getContextPath() + "</h1></body></html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // === BẮT ĐẦU PHẦN THÊM MỚI: XỬ LÝ QUÉT MÃ QR BẰNG TOKEN (HOST/GUEST) ===
        String token = request.getParameter("token");
        if (token != null && !token.isEmpty()) {
            // [QR FLOW] Tat ca luong quet QR di qua ScanQRController de tranh bypass buoc xac nhan ban.
            response.sendRedirect(request.getContextPath() + "/scan?token="
                    + URLEncoder.encode(token, "UTF-8"));
            return;
        }
        int tableIdFromToken = 0;

        //logic gộp bàn
        if (token != null && !token.isEmpty()) {
            TableDAO tableDAO = new TableDAO();
            Table currentTable = tableDAO.getTableByToken(token);

            if (currentTable != null && currentTable.getIsActive() == 1) {
                tableIdFromToken = currentTable.getTableID();

                // Lưu thông tin cơ bản vào session
                session.setAttribute("tableID", currentTable.getTableID());
                session.setAttribute("currentTableID", currentTable.getTableID());
                session.setAttribute("areaType", currentTable.getAreaType());

                OrderDAO orderDAO = new OrderDAO();
                Order activeOrder = orderDAO.getActiveOrderByTableId(currentTable.getTableID());

                String role = (String) session.getAttribute("roleInTable");
                Integer sessionOrderID = (Integer) session.getAttribute("orderID");

                // =========================================================
                // TÍNH NĂNG MỚI: CHỦ BÀN QUÉT MÃ QR ĐỂ GỘP THÊM BÀN TRỐNG
                // =========================================================
                if ("HOST".equals(role) && sessionOrderID != null) {
                    // Nếu khách quét QR của một bàn ĐANG TRỐNG
                    if (activeOrder == null) {
                        boolean isAdded = orderDAO.addTableToExistingOrder(sessionOrderID, currentTable.getTableID());
                        if (isAdded) {
                            session.setAttribute("successMsg", "Đã gộp thêm bàn thành công vào hóa đơn của bạn!");
                            // Chuyển thẳng vào Menu gọi món
                            response.sendRedirect(request.getContextPath() + "/menu");
                            return;
                        }
                    } // Nếu khách quét QR của bàn ĐANG CÓ NGƯỜI LẠ NGỒI
                    else if (activeOrder.getOrderID() != sessionOrderID) {
                        session.setAttribute("errorMsg", "Bàn này đang có khách ngồi, không thể gộp!");
                        response.sendRedirect(request.getContextPath() + "/menu");
                        return;
                    }
                    // Nếu activeOrder.getOrderID() == sessionOrderID nghĩa là bàn này khách ĐÃ GỘP TỪ TRƯỚC RỒI
                    // Hệ thống sẽ không làm gì cả, cứ thả cho code chạy tiếp xuống dưới để vào Menu bình thường.
                }
                // =========================================================

                if (activeOrder != null) {

//                     // 👉 TRƯỜNG HỢP 1: BÀN ĐÃ CÓ ORDER
//
//                     // --- BẮT ĐẦU: BARIE CHẶN CHỜ NHÂN VIÊN DUYỆT BÀN ---
//                     if (activeOrder.getIsStaffConfirmed() == 0) {
//                         session.setAttribute("pendingOrderID", activeOrder.getOrderID());
//                         request.getRequestDispatcher("/views/user/waiting_staff.jsp").forward(request, response);
//                         return; // Khóa luồng, không cho load Menu!
//                     }
//                     // --- KẾT THÚC BARIE ---                                        
                    if (role != null && sessionOrderID != null && sessionOrderID == activeOrder.getOrderID()) {
                        // Host hoặc Guest đã duyệt -> Cho vào Menu gọi món bình thường
                    } else {
//                         // === BẮT ĐẦU VÁ LỖ HỔNG ĐƠN ĐẶT TRƯỚC (RESERVATION) ===
//                         // Giả sử orderType == 2 là mã của Đơn đặt trước trong Database của bạn
//                         if (activeOrder.getOrderType() == 2) {
//                             session.setAttribute("pendingOrderID", activeOrder.getOrderID());
//                             // Chuyển sang trang yêu cầu nhập Số điện thoại để lấy lại quyền Host
//                             request.getRequestDispatcher("/views/user/claim_host.jsp").forward(request, response);
//                             return; 
//                         }
//                         // === KẾT THÚC PHẦN VÁ ===
                        // === BẮT ĐẦU VÁ LỖ HỔNG ĐƠN ĐẶT TRƯỚC (RESERVATION) ===
                        // Nhận diện đơn đặt trước qua trạng thái 'reserved'
                        if ("reserved".equals(activeOrder.getTableStatus())) {
                            session.setAttribute("pendingOrderID", activeOrder.getOrderID());
                            // Chuyển sang trang yêu cầu nhập Số điện thoại để lấy lại quyền Host
                            request.getRequestDispatcher("/views/user/claim_host.jsp").forward(request, response);
                            return;
                        }
                        // === KẾT THÚC PHẦN VÁ ===

                        // Người lạ -> Xin phép Host (Dành cho các bàn khách walk-in bình thường, orderType == 1)
                        session.setAttribute("pendingOrderID", activeOrder.getOrderID());
                        request.getRequestDispatcher("/views/user/join_table.jsp").forward(request, response);
                        return;
                    }
                } else {
                    // 👉 TRƯỜNG HỢP 2: BÀN TRỐNG (Người đầu tiên quét)
                    Order newOrder = new Order();

                    // [TABLE STATUS FLOW] Khach vang lai da vao menu thi ban da co HOST => occupied.
                    newOrder.setTableStatus("occupied");

                    newOrder.setOrderType(1);

                    // SỬA SỐ 0 THÀNH SỐ 1: FIX CỨNG ĐÃ ĐƯỢC DUYỆT
                    newOrder.setIsStaffConfirmed(1);

                    newOrder.setOrderStatus("ordering");
                    newOrder.setTotalAmount(0);
                    newOrder.setDepositAmount(0);

                    int newOrderID = orderDAO.createOrder(newOrder);
                    if (newOrderID > 0) {
                        orderDAO.linkOrderAndTable(newOrderID, currentTable.getTableID());
                        session.setAttribute("orderID", newOrderID);
                        session.setAttribute("roleInTable", "HOST");

                        // TẠM THỜI COMMENT ĐOẠN ĐẨY RA MÀN HÌNH CHỜ
                        /* session.setAttribute("pendingOrderID", newOrderID);
                        request.getRequestDispatcher("/views/user/waiting_staff.jsp").forward(request, response);
                        return;
                         */
                    }
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/error.jsp");
                return;
            }
        }
        // === KẾT THÚC PHẦN THÊM MỚI ===

        String search = request.getParameter("search");
        String category_raw = request.getParameter("category");
        String method_raw = request.getParameter("cookingMethod");
        String status_raw = request.getParameter("status");
        String minPrice_raw = request.getParameter("minPrice");
        String maxPrice_raw = request.getParameter("maxPrice");
        String price_raw = request.getParameter("price");
        String sort_raw = request.getParameter("sort");
        String page_raw = request.getParameter("page");
        String tableID_raw = request.getParameter("tableID");
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
        int tableID = parseIntSafe(tableID_raw, 0, 0);

        // Cập nhật lại tableID để tương thích với Token (nếu quét Token thì ưu tiên lấy ID từ Token)
        if (tableIdFromToken > 0) {
            tableID = tableIdFromToken;
        }

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

        // Xóa dòng gán currentTableID gốc vì phần token đã gán chuẩn hơn
        if (tableID > 0) {
            session.setAttribute("currentTableID", tableID);
        }

        // === BẮT ĐẦU CHÈN THÊM: LẤY DANH SÁCH BÀN ĐỂ CHỌN LÚC GỌI MÓN ===
        Integer currentOrderID = (Integer) session.getAttribute("orderID");
        if (currentOrderID != null) {
            TableDAO tDAO = new TableDAO();
            List<Table> assignedTables = tDAO.getTablesByOrderId(currentOrderID);
            request.setAttribute("assignedTables", assignedTables);
        }
        // === KẾT THÚC CHÈN THÊM ===

        // === BẮT ĐẦU PHẦN CHỈNH SỬA: ĐIỀU HƯỚNG MÀN HÌNH ===
        Integer sessionTableID = (Integer) session.getAttribute("currentTableID");

        // [GIU TRANG MENU] Gui URL hien tai sang form them mon de sau khi
        // xu ly gio hang, khach quay lai dung trang va bo loc dang xem.
        String currentMenuUrl = request.getRequestURI();
        if (request.getQueryString() != null
                && !request.getQueryString().isBlank()) {
            currentMenuUrl += "?" + request.getQueryString();
        }
        request.setAttribute("returnUrl", currentMenuUrl);

        // Nếu là Quản lý/Nhân viên VÀ không đang xem với tư cách Khách bàn nào -> Trỏ vào trang Admin
        if (sessionTableID == null) {
            String currentUrl = request.getRequestURI();
            if (request.getQueryString() != null) {
                currentUrl += "?" + request.getQueryString();
            }
            request.getSession().setAttribute("lastDishListUrl", currentUrl);
            request.getRequestDispatcher("/views/owner/dish-list.jsp").forward(request, response);
        } else {
            // Còn lại (Khách vãng lai, Khách quét QR) -> Trỏ vào trang Menu User
            request.getRequestDispatcher("/views/user/menu.jsp").forward(request, response);
        }

    }

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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

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
                return trimmedValue;
            }
        }
        return defaultValue;
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
