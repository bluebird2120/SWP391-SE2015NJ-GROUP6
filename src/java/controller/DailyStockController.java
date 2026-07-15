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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.CookingMethod;
import model.MenuCategory;
import model.MenuItem;

@WebServlet(name = "DailyStockController", urlPatterns = {"/daily-stock"})
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
            // Ép kiểu thử để bắt lỗi định dạng bẩn (abc, gachien,...)
            dateSql = java.sql.Date.valueOf(date.trim());

            // Kiểm tra logic ngày tương lai
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

        request.getRequestDispatcher("/views/admin/daily-stock.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String[] initialQuantity = request.getParameterValues("initialQuantity");
        String[] itemID = request.getParameterValues("itemID");

        boolean hasError = false;
        String errorMessage = "";

        java.sql.Date dateSql = java.sql.Date.valueOf(LocalDate.now().toString());
        int totalDish = menuItemDAO.countSearchDish("", dateSql, 0, 0);

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
            request.setAttribute("errorMessage", errorMessage);
            request.setAttribute("saveInputData", saveInputData);
            doGet(request, response);
        } else {
            for (int i = 0; i < itemID.length; i++) {
                boolean result = dailyInventoryDAO.updateStockMenuItem(Integer.parseInt(itemID[i]), Integer.parseInt(initialQuantity[i]));
                if (!result) {
                    request.setAttribute("updateFail", "Cập nhật thất bại vì bạn đã nhập số lượng cho ngày hôm nay");
                    doGet(request, response);
                    return;
                }
            }
            request.setAttribute("updateSuccess", "Bạn đã cập nhập thành công số lượng món ăn cho ngày hôm nay");
            doGet(request, response);
        }
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

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
