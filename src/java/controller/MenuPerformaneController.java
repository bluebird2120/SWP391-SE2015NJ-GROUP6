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

@WebServlet(name = "MenuPerformaneController", urlPatterns = {"/menu-performance"})
public class MenuPerformaneController extends HttpServlet {

    private MenuCategoryDAO menuCategoryDAO = new MenuCategoryDAO();
    private MenuItemDAO menuItemDAO = new MenuItemDAO();
    private CookingMethodDAO cookingMethodDAO = new CookingMethodDAO();
    private static final int PAGE_SIZE = 8;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Nhận tham số từ Client
        String search = request.getParameter("search");
        String category_raw = request.getParameter("category");
        String method_raw = request.getParameter("cookingMethod");
        String page_raw = request.getParameter("page");
        String filterType = request.getParameter("filterType");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        // 2. Validate dữ liệu số an toàn (Dùng hàm gộp tối giản ở dưới)
        int categoryId = parseIntSafe(category_raw, 0, 0);
        int methodID = parseIntSafe(method_raw, 0, 0);
        int page = parseIntSafe(page_raw, 1, 1);

        search = (search == null || search.trim().length() > 100) ? "" : search.trim();

        // 3. Xử lý quy đổi khoảng ngày báo cáo
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

        // 4. Gọi DAO phân trang dữ liệu báo cáo
        int totalItem = menuItemDAO.getTotalPerformanceDishCount(search, categoryId, methodID, startDate, endDate);
        int totalPage = (int) Math.ceil((double) totalItem / PAGE_SIZE);
        if (page > totalPage && totalPage > 0) {
            page = totalPage;
        }

        int offSet = (page - 1) * PAGE_SIZE;
        List<CookingMethod> methodList = cookingMethodDAO.getAllCookingMethod();
        List<MenuCategory> categoryList = menuCategoryDAO.getAllMenuCategory();
        List<MenuItem> menuItemList = menuItemDAO.getPerformanceDish(search, categoryId, methodID, startDate, endDate, offSet, PAGE_SIZE);
        List<MenuItem> topChartList = menuItemDAO.getPerformanceDish(search, categoryId, methodID, startDate, endDate, 0, 5);
        
        // 5. Thuật toán phân màu thông minh (Sửa lỗi 0 đĩa bán chạy)
        if (menuItemList != null && !menuItemList.isEmpty()) {
            int totalItems = menuItemList.size();
            int totalQtyAllDish = 0;
            for (MenuItem mi : menuItemList) {
                totalQtyAllDish += mi.getTotalQuantity();
            }

            double avgQuantity = (double) totalQtyAllDish / totalItems;
            double lowQuantity = avgQuantity * 0.2;

            for (MenuItem mi : menuItemList) {
                int qty = mi.getTotalQuantity();

                // Nếu tổng sản lượng bằng 0 hoặc chưa từng bán đĩa nào -> Gom vào nhóm Ế Ẩm
                if (totalQtyAllDish == 0 || qty == 0) {
                    mi.setMenuTag("Cảnh Báo: Ế Ẩm");
                    mi.setTagClass("tag-dog");
                } else if (qty >= avgQuantity * 1.5) {
                    mi.setMenuTag("Bán Chạy Nhất");
                    mi.setTagClass("tag-star");
                } else if (qty >= avgQuantity) {
                    mi.setMenuTag("Tiêu Thụ Ổn Định");
                    mi.setTagClass("tag-plowhorse");
                } else if (qty > lowQuantity) {
                    mi.setMenuTag("Tiêu Thụ Chậm");
                    mi.setTagClass("tag-puzzle");
                } else {
                    mi.setMenuTag("Cảnh Báo: Ế Ẩm");
                    mi.setTagClass("tag-dog");
                }
            }
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

        request.getRequestDispatcher("views/admin/top-selling.jsp").forward(request, response);
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
}
