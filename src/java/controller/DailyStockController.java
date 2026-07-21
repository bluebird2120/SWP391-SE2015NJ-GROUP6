package controller;

import dal.CookingMethodDAO;
import dal.DailyInventoryDAO;
import dal.MenuCategoryDAO;
import dal.MenuItemDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.CookingMethod;
import model.MenuCategory;
import model.MenuItem;

@WebServlet(name = "DailyStockController", urlPatterns = {"/owner/daily-stock"})
public class DailyStockController extends HttpServlet {

    private MenuCategoryDAO menuCategoryDAO = new MenuCategoryDAO();
    private MenuItemDAO menuItemDAO = new MenuItemDAO();
    private DailyInventoryDAO dailyInventoryDAO = new DailyInventoryDAO();
    private CookingMethodDAO cookingMethodDAO = new CookingMethodDAO();
    private static final int PAGE_SIZE = 100;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String search = request.getParameter("search");
        String category_raw = request.getParameter("categoryID");
        String page_raw = request.getParameter("page");
        String method_raw = request.getParameter("cookingMethod");
        String date = request.getParameter("date");

        // XỬ LÝ SEARCH
        if (!checkEmpty(search)) {
            search = "";
        }
        String errorSearch = search.length() > 100 ? "Tìm kiếm không vượt quá 100 kí tự" : "";
        String currentSearch = search;
        String searchForDAO = !errorSearch.isEmpty() ? "" : search;

        if (!errorSearch.isEmpty()) {
            request.setAttribute("errorSearch", errorSearch);
        }

        // XỬ LÝ NGÀY THÁNG
        String errorDate = "";
        java.sql.Date dateSql = null;

        if (!checkEmpty(date)) {
            date = LocalDate.now().toString();
        }

        try {
            dateSql = java.sql.Date.valueOf(date.trim());
            if (LocalDate.parse(date.trim()).isAfter(LocalDate.now())) {
                errorDate = "Không thể xem hoặc cấu hình kho cho ngày ở tương lai!";
                date = LocalDate.now().toString();
                dateSql = java.sql.Date.valueOf(date);
            }
        } catch (Exception e) {
            errorDate = "Định dạng ngày tháng không hợp lệ!";
            date = LocalDate.now().toString();
            dateSql = java.sql.Date.valueOf(date);
        }

        if (!errorDate.isEmpty()) {
            request.setAttribute("errorDate", errorDate);
        }

        // XỬ LÝ BỘ LỌC SỐ NGUYÊN
        int categoryID = parseIntSafe(category_raw, 0, 0);
        int methodID = parseIntSafe(method_raw, 0, 0);
        int page = parseIntSafe(page_raw, 1, 1);

        // LẤY DỮ LIỆU TỪ DAO VÀ PHÂN TRANG
        int totalItem = menuItemDAO.countSearchDish(searchForDAO, dateSql, categoryID, methodID);
        int totalPage = (int) Math.ceil((double) totalItem / PAGE_SIZE);

        if (page > totalPage && totalPage > 0) {
            page = totalPage;
        }

        int offSet = (page - 1) * PAGE_SIZE;
        List<CookingMethod> listMethod = cookingMethodDAO.getAllCookingMethod();
        List<MenuCategory> list = menuCategoryDAO.getAllMenuCategory();
        List<MenuItem> listItem = menuItemDAO.searchDishPaging(searchForDAO, dateSql, categoryID, methodID, offSet, PAGE_SIZE);

        // TRẢ DỮ LIỆU SANG JSP ĐỒNG BỘ
        request.setAttribute("currentSearch", currentSearch);
        request.setAttribute("currentCategory", categoryID);
        request.setAttribute("currentMethod", methodID);

        request.setAttribute("date", date);
        request.setAttribute("listMethod", listMethod);
        request.setAttribute("hasLowStock", checkHasLowStock(listItem));
        request.setAttribute("isConfigYet", isConfigYet(listItem, date));
        request.setAttribute("categoryList", list);
        request.setAttribute("menuItemList", listItem);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("currentPage", page);

        request.getRequestDispatcher("/views/owner/daily-stock.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String[] initialQuantity = request.getParameterValues("initialQuantity");
        String[] itemID = request.getParameterValues("itemID");

        String search_raw = request.getParameter("search");
        String category_raw = request.getParameter("categoryID");
        String page_raw = request.getParameter("page");
        String method_raw = request.getParameter("cookingMethod");
        String date_raw = request.getParameter("date");

        boolean hasError = false;
        String errorMessage = "";

        // Trích xuất bộ lọc ẩn để phục hồi hiện trạng trang
        int categoryID = parseIntSafe(category_raw, 0, 0);
        int methodID = parseIntSafe(method_raw, 0, 0);
        int page = parseIntSafe(page_raw, 1, 1);
        String currentSearch = checkEmpty(search_raw) ? search_raw : "";
        String date = checkEmpty(date_raw) ? date_raw : LocalDate.now().toString();
        java.sql.Date dateSql = java.sql.Date.valueOf(date);

        // 🌟 ĐÃ SỬA CHUẨN: Băm chuỗi và đảo ngược YYYY-MM-DD hoặc YYYY/MM/DD thành DD-MM-YYYY trong thông báo lỗi của Servlet
        if (!LocalDate.now().toString().equals(date.trim())) {
            hasError = true;
            String displayDate = date.trim();
            if (date.contains("-")) {
                String[] parts = date.trim().split("-");
                if (parts.length == 3) {
                    displayDate = parts[2] + "-" + parts[1] + "-" + parts[0];
                }
            } else if (date.contains("/")) {
                String[] parts = date.trim().split("/");
                if (parts.length == 3) {
                    displayDate = parts[2] + "-" + parts[1] + "-" + parts[0];
                }
            }
            errorMessage = "Hệ thống chặn thao tác! Bạn không thể chỉnh sửa hoặc cấu hình lại số lượng món ăn của ngày đã qua (Ngày " + displayDate + ").";
        }

        int totalDish = menuItemDAO.countSearchDish("", java.sql.Date.valueOf(LocalDate.now().toString()), 0, 0);

        // Nếu chưa dính lỗi ngày cũ thì mới kiểm tra tính hợp lệ của mảng số lượng
        if (!hasError) {
            if (initialQuantity == null || itemID == null || initialQuantity.length != totalDish) {
                hasError = true;
                errorMessage = "Thao tác bị chặn! Bạn đang bật bộ lọc ẩn món ăn. Vui lòng chọn 'Tất cả loại món', 'Tất cả phương thức' và nhập đủ tất cả món ăn trước khi chốt kho.";
            } else {
                for (int i = 0; i < initialQuantity.length; i++) {
                    String qty = initialQuantity[i];

                    if (!checkEmpty(qty)) {
                        hasError = true;
                        errorMessage = "Lỗi hệ thống: Bạn bắt buộc phải nhập đầy đủ số lượng các món ăn";
                        break;
                    }

                    errorMessage = checkQuantity(qty);
                    if (!errorMessage.isEmpty()) {
                        hasError = true;
                        break;
                    }
                }
            }
        }

        // LUỒNG XỬ LÝ KHI DÍNH LỖI (Bao gồm cả lỗi sửa ngày cũ)
        if (hasError) {
            Map<Integer, String> saveInputData = new HashMap<>();
            if (itemID != null && initialQuantity != null) {
                for (int i = 0; i < itemID.length; i++) {
                    try {
                        int itemId = Integer.parseInt(itemID[i]);
                        String qtyValue = initialQuantity[i];
                        saveInputData.put(itemId, qtyValue);
                    } catch (Exception e) {
                    }
                }
            }

            // Tải lại dữ liệu bảng theo đúng ngày người dùng đang chọn để giữ hiện trạng
            int totalItem = menuItemDAO.countSearchDish(currentSearch, dateSql, categoryID, methodID);
            int totalPage = (int) Math.ceil((double) totalItem / PAGE_SIZE);
            if (page > totalPage && totalPage > 0) {
                page = totalPage;
            }
            int offSet = (page - 1) * PAGE_SIZE;
            List<CookingMethod> listMethod = cookingMethodDAO.getAllCookingMethod();
            List<MenuCategory> list = menuCategoryDAO.getAllMenuCategory();
            List<MenuItem> listItem = menuItemDAO.searchDishPaging(currentSearch, dateSql, categoryID, methodID, offSet, PAGE_SIZE);

            request.setAttribute("errorMessage", errorMessage);
            request.setAttribute("saveInputData", saveInputData);

            request.setAttribute("currentSearch", currentSearch);
            request.setAttribute("currentCategory", categoryID);
            request.setAttribute("currentMethod", methodID);
            request.setAttribute("date", date);

            request.setAttribute("listMethod", listMethod);
            request.setAttribute("hasLowStock", checkHasLowStock(listItem));
            request.setAttribute("isConfigYet", isConfigYet(listItem, date));
            request.setAttribute("categoryList", list);
            request.setAttribute("menuItemList", listItem);
            request.setAttribute("totalPage", totalPage);
            request.setAttribute("currentPage", page);

            request.getRequestDispatcher("/views/owner/daily-stock.jsp").forward(request, response);
            return;
        }

        // LUỒNG LƯU THÀNH CÔNG (Chỉ chạy khi cấu hình đúng ngày hôm nay)
        HttpSession session = request.getSession();
        boolean isAllSuccess = true;

        for (int i = 0; i < itemID.length; i++) {
            boolean result = dailyInventoryDAO.updateStockMenuItem(Integer.parseInt(itemID[i]), Integer.parseInt(initialQuantity[i]));
            if (!result) {
                isAllSuccess = false;
                break;
            }
        }

        if (isAllSuccess) {
            session.setAttribute("updateSuccess", "Bạn đã cập nhật thành công số lượng món ăn cho ngày hôm nay");
        } else {
            session.setAttribute("updateFail", "Cập nhật thất bại vì bạn đã nhập số lượng cho ngày hôm nay");
        }

        response.sendRedirect(request.getContextPath() + "/owner/daily-stock?page=" + page + "&search=" + java.net.URLEncoder.encode(currentSearch, "UTF-8") + "&categoryID=" + categoryID + "&cookingMethod=" + methodID + "&date=" + date);
    }

    private int parseIntSafe(String value, int defaultValue, int minValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int result = Integer.parseInt(value.trim());
            return (result < minValue) ? minValue : result;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean checkHasLowStock(List<MenuItem> list) {
        int flag = 0;
        if (list != null) {
            for (MenuItem mi : list) {
                if (mi.getQuantityInStock() > 0 && mi.getInitialQuantity() > 0) {
                    if (mi.getQuantityInStock() * 100 / mi.getInitialQuantity() < 20) {
                        flag++;
                    }
                }
            }
        }
        return flag > 0;
    }

    private boolean isConfigYet(List<MenuItem> list, String date) {
        if (list != null && !list.isEmpty() && LocalDate.now().toString().equals(date)) {
            for (MenuItem item : list) {
                if (item.getInitialQuantity() > 0) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private boolean checkEmpty(String data) {
        return (data != null && !data.trim().isEmpty());
    }

    private String checkQuantity(String quantity) {
        try {
            int qty = Integer.parseInt(quantity);
            if (qty < 0) {
                return "Số lượng món ăn phải là số nguyên lớn hơn hoặc bằng 0!";
            }
        } catch (Exception e) {
            return "Số lượng nhập vào phải là số nguyên hợp lệ!";
        }
        return "";
    }
}