package controller;

import dal.CookingMethodDAO;
import dal.MenuCategoryDAO;
import dal.MenuItemDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import model.MenuItem;

@WebServlet(name = "HomeController", urlPatterns = {"/home"})
public class HomeController extends HttpServlet {
    
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    // Khởi tạo thêm các DAO để lấy dữ liệu đổ vào nút lọc select
    private final MenuCategoryDAO menuCategoryDAO = new MenuCategoryDAO();
    private final CookingMethodDAO cookingMethodDAO = new CookingMethodDAO();
    
    private static final int PAGE_SIZE = 8;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Đọc các bộ lọc được gửi từ client
        String keyword = request.getParameter("keyword");
        if (keyword == null) {
            keyword = "";
        }
        keyword = keyword.trim();
        
        String catParam = request.getParameter("category");
        String methodParam = request.getParameter("cookingMethod");
        String pageParam = request.getParameter("page");

        //Nếu truyền bất kỳ tham số nào invalid
        if ((catParam != null && !catParam.isBlank() && !isNumeric(catParam)
                || (methodParam != null && !methodParam.isBlank() && !isNumeric(methodParam))
                || (pageParam != null && !pageParam.isBlank() && !isNumeric(pageParam)))) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        // Đọc danh mục, phương thức nấu, kiểu sắp xếp từ Request
        int categoryId = parseIntOrDefault(catParam, 0);
        int methodID = parseIntOrDefault(methodParam, 0);
        
        String priceType = request.getParameter("price");
        if (priceType == null || priceType.isBlank()) {
            priceType = "discountedPrice"; // Mặc định sắp xếp theo giá thực tế
        }
        
        String sort = request.getParameter("sort");
        if (sort == null || sort.isBlank()) {
            sort = "asc"; // Mặc định tăng dần
        }

        // Cố định khoảng giá và trạng thái (Ngoại trừ khoảng giá như yêu cầu)
        int status = 1; // Chỉ lấy món đang bán trên trang chủ
        int minPrice = 0;
        int maxPrice = Integer.MAX_VALUE;

        // 2. Xử lý phân trang
        int page = parseIntOrDefault(request.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }
        //số lượng món bị bỏ qua khi vô page
        int offset = (page - 1) * PAGE_SIZE;

        // 3. Gọi DAO truy vấn dữ liệu theo đúng các bộ lọc đã chọn
        List<MenuItem> menuItems = menuItemDAO.searchMenuItemPaging(
                keyword, categoryId, methodID, status,
                minPrice, maxPrice, sort, priceType, offset, PAGE_SIZE);
        
        int totalItems = menuItemDAO.countSearchMenuItem(
                keyword, categoryId, methodID, status,
                minPrice, maxPrice, priceType);
        
        int totalPages = (int) Math.ceil(totalItems / (double) PAGE_SIZE);
        if (totalPages < 1) {
            totalPages = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }

        // 4. Lấy danh sách Categories và Methods để hiển thị lên thanh Lọc dữ liệu
        request.setAttribute("listCategory", menuCategoryDAO.getAllMenuCategory());
        request.setAttribute("listMethod", cookingMethodDAO.getAllCookingMethod());

        // 5. Đẩy các thuộc tính ngược lại trang JSP
        request.setAttribute("menuItems", menuItems);
        request.setAttribute("keyword", keyword);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        
        request.getRequestDispatcher("/views/home.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    private boolean isNumeric(String str) {
        if (str == null || str.isBlank()) {
            return false;
        }
        try {
            Integer.parseInt(str.trim());
            return true; // Nếu đổi thành số thành công
        } catch (NumberFormatException e) {
            return false; // Nếu chuỗi chứa ký tự chữ như "abc"
        }
    }
    
    private int parseIntOrDefault(String string, int def) {
        if (string == null || string.isBlank()) {
            return def;
        }
        try {
            return Integer.parseInt(string.trim());
        } catch (NumberFormatException ex) {
            return def;
        }
    }
}
