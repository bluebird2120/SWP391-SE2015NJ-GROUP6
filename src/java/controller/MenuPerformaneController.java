package controller;

import dal.CookingMethodDAO;
import dal.MenuCategoryDAO;
import dal.MenuItemDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import model.CookingMethod;
import model.MenuCategory;
import model.MenuItem;

@WebServlet(name = "MenuPerformaneController", urlPatterns = {"/owner/menu-performance"})
public class MenuPerformaneController extends HttpServlet {

    private MenuCategoryDAO menuCategoryDAO = new MenuCategoryDAO();
    private MenuItemDAO menuItemDAO = new MenuItemDAO();
    private CookingMethodDAO cookingMethodDAO = new CookingMethodDAO();
    private static final int PAGE_SIZE = 8;
    private static final int CHART_SIZE = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String search = request.getParameter("search");
        String category_raw = request.getParameter("category");
        String method_raw = request.getParameter("cookingMethod");
        String page_raw = request.getParameter("page");
        String filterType = request.getParameter("filterType");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String viewHistoryItemID_raw = request.getParameter("viewHistoryItemID");

        //Validate
        int categoryId = parseIntSafe(category_raw, 0, 0);
        int methodID = parseIntSafe(method_raw, 0, 0);
        int page = parseIntSafe(page_raw, 1, 1);
        int viewHistoryItemID = parseIntSafe(viewHistoryItemID_raw, 0, 0);

        if (!checkEmpty(search)) {
            search = "";
        }

        String errorSearch = isValidString(search, 100, "Tìm kiếm không vượt quá 100 kí tự");

        if (errorSearch != null) {
            request.setAttribute("errorSearch", errorSearch);
            search = "";
        }

        // Xử lý quy đổi khoảng ngày báo cáo
        LocalDate today = LocalDate.now();

        if ("today".equals(filterType)) {
            startDate = today.toString();
            endDate = today.toString();
        } else if ("week".equals(filterType)) {
            startDate = today.with(DayOfWeek.MONDAY).toString();
            endDate = today.toString();
        } else if ("month".equals(filterType)) {
            startDate = today.withDayOfMonth(1).toString();
            endDate = today.toString();
        } else if ("year".equals(filterType)) {
            startDate = today.withDayOfYear(1).toString();
            endDate = today.toString();
        } else {
            // Lịch tay hoặc load lần đầu: bẫy định dạng Regex
            if (startDate == null || !startDate.trim().matches("\\d{4}-\\d{2}-\\d{2}")
                    || endDate == null || !endDate.trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
                startDate = today.toString();
                endDate = today.toString();
            } else if (startDate.compareTo(endDate) > 0) { // Ngày ngược -> tự đảo
                String temp = startDate;
                startDate = endDate;
                endDate = temp;
            }
        }

        // Gọi DAO phân trang dữ liệu báo cáo
        int totalItem = menuItemDAO.getTotalPerformanceDishCount(search, categoryId, methodID, startDate, endDate);
        int totalPage = (int) Math.ceil((double) totalItem / PAGE_SIZE);
        if (page > totalPage && totalPage > 0) {
            page = totalPage;
        }

        int offSet = (page - 1) * PAGE_SIZE;
        List<CookingMethod> methodList = cookingMethodDAO.getAllCookingMethod();
        List<MenuCategory> categoryList = menuCategoryDAO.getAllMenuCategory();

        // List A: Chỉ lấy 8 món để hiển thị lên bảng của trang hiện tại
        List<MenuItem> menuItemList = menuItemDAO.getPerformanceDish(search, categoryId, methodID, startDate, endDate, offSet, PAGE_SIZE);

        // List B: Chỉ lấy đúng 5 item cao nhất để vẽ biểu đồ
        List<MenuItem> topChartList = menuItemDAO.getPerformanceDish("", categoryId, methodID, startDate, endDate, 0, CHART_SIZE);

        // List C: Lấy toàn bộ item để tính toán các món
        List<MenuItem> allItemsForCalc = menuItemDAO.getPerformanceDish("", 0, 0, startDate, endDate, 0, menuItemDAO.totalMenuItem());

        //Tính tổng số lượng món có doanh thu 
        if (menuItemList != null && !menuItemList.isEmpty()) {
            int activeItemsCount = 0;
            int totalQtyAllDish = 0;

            for (MenuItem mi : allItemsForCalc) {
                if (mi.getTotalQuantity() > 0) {
                    totalQtyAllDish += mi.getTotalQuantity();
                    activeItemsCount++;
                }
            }

            // Mốc trung bình cộng của các món có doanh thu
            double avgQuantity = (activeItemsCount == 0) ? 0 : (double) totalQtyAllDish / activeItemsCount;
            double lowQuantity = avgQuantity * 0.5;

            // Vòng lặp gán nhãn
            for (MenuItem mi : menuItemList) {
                int qty = mi.getTotalQuantity();

                if (qty == 0) {
                    mi.setMenuTag("Cảnh Báo: Món Chết");
                    mi.setTagClass("tag-dog");
                } else if (qty >= avgQuantity * 1.5) {
                    mi.setMenuTag("Bán Chạy Nhất");
                    mi.setTagClass("tag-star");
                } else if (qty >= avgQuantity) {
                    mi.setMenuTag("Tiêu Thụ Ổn Định");
                    mi.setTagClass("tag-plowhorse");
                } else if (qty >= lowQuantity) {
                    mi.setMenuTag("Tiêu Thụ Chậm");
                    mi.setTagClass("tag-puzzle");
                } else {
                    mi.setMenuTag("Hiệu Suất Kém");
                    mi.setTagClass("tag-dog");
                }
            }
        }

        if (viewHistoryItemID > 0) {
            // Lấy danh sách lịch sử 
            List<MenuItem> historyList = menuItemDAO.getSaleHistoryByItem(viewHistoryItemID, startDate, endDate);
            request.setAttribute("historyList", historyList);

            // Lấy luôn tên món ăn 
            String currentViewDishName = "Món ăn";
            if (historyList != null && !historyList.isEmpty()) {
                currentViewDishName = historyList.get(0).getItemName();
            }
            request.setAttribute("currentViewDishName", currentViewDishName);
        }

        // 6. Đẩy thuộc tính sạch sang JSP
        request.setAttribute("search", search);
        request.setAttribute("selectedCategory", categoryId);
        request.setAttribute("selectedMethod", methodID);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("filterType", filterType);
        request.setAttribute("methodList", methodList);
        request.setAttribute("categoryList", categoryList);
        request.setAttribute("menuItemList", menuItemList);
        request.setAttribute("topChartList", topChartList);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("currentPage", page);

        request.getRequestDispatcher("/views/owner/top-selling.jsp").forward(request, response);
    }

    // Hàm tiện ích bẫy lỗi gộp 
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

    //Hàm check rỗng
    private boolean checkEmpty(String data) {
        return (data != null && !data.trim().isEmpty());
    }

    //Hàm kiểm tra độ dài string
    private String isValidString(String data, int length, String ms) {
        if (data.length() > length) {
            return ms;
        }
        return null;
    }
}