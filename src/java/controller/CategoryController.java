package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.MenuCategory;
import java.util.List;
import dal.MenuCategoryDAO;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CategoryController", urlPatterns = {"/category-management"})
public class CategoryController extends HttpServlet {

    private MenuCategoryDAO menuCategoryDAO = new MenuCategoryDAO();
    private static final int PAGE_SIZE = 8;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String search = request.getParameter("search");
        String page_raw = request.getParameter("page");
        String isAvailable_raw = request.getParameter("isAvailable");

        // Validate đầu vào bộ lọc
        if (!checkEmpty(search)) {
            search = "";
        }
        int page = parseIntSafe(page_raw, 1, 1);
        int isAvailable = parseIntSafe(isAvailable_raw, -1, -1);

        String errorSearch = search.length() > 100 ? "Tìm kiếm không vượt quá 100 kí tự" : "";

        if (checkEmpty(errorSearch)) {
            request.setAttribute("errorSearch", errorSearch);
            search = "";
        }

        HttpSession session = request.getSession();
        if (session.getAttribute("updateSuccess") != null) {
            request.setAttribute("updateSuccess", session.getAttribute("updateSuccess"));
            session.removeAttribute("updateSuccess");
        }
        if (session.getAttribute("updateFail") != null) {
            request.setAttribute("updateFail", session.getAttribute("updateFail"));
            session.removeAttribute("updateFail");
        }

        int totalCategory = menuCategoryDAO.countSearchCategory(search, isAvailable);
        int totalPage = (int) Math.ceil((double) totalCategory / PAGE_SIZE);

        if (page > totalPage && totalPage > 0) {
            page = totalPage;
        }

        int offset = (page - 1) * PAGE_SIZE;
        List<MenuCategory> categoryList = menuCategoryDAO.searchCategoryPaging(search, isAvailable, offset, PAGE_SIZE);

        for (MenuCategory mc : categoryList) {
            int activeDish = menuCategoryDAO.countDishByStatus(mc.getCategoryID(), 1);
            int inactiveDish = menuCategoryDAO.countDishByStatus(mc.getCategoryID(), 0);
            int totalDish = menuCategoryDAO.countDishByCategory(mc.getCategoryID());
            mc.setActiveMenuItem(activeDish);
            mc.setInactiveMenuItem(inactiveDish);
            mc.setTotalDish(totalDish);
        }

        // Đồng bộ hóa việc truyền biến trạng thái lọc an toàn sang JSP
        request.setAttribute("currentSearch", search);
        request.setAttribute("currentAvailable", isAvailable);
        request.setAttribute("categoryList", categoryList);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("currentPage", page);

        request.getRequestDispatcher("/views/admin/category-list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String categoryID = request.getParameter("categoryID");
        String categoryName = request.getParameter("categoryName");
        String status_raw = request.getParameter("status");
        String page_raw = request.getParameter("page");
        String search_raw = request.getParameter("search");
        String isAvailable_raw = request.getParameter("isAvailable");

        // Ép kiểu an toàn dữ liệu trạng thái
        int id = parseIntSafe(categoryID, 0, 0);
        int status = parseIntSafe(status_raw, -1, -1);
        int page = parseIntSafe(page_raw, 1, 1);
        int isAvailable = parseIntSafe(isAvailable_raw, -1, -1);
        String currentSearch = checkEmpty(search_raw) ? search_raw : "";

        // LUỒNG 1: Xử lý kích hoạt/vô hiệu hóa trạng thái danh mục chính và món ăn đi kèm
        if (status != -1 && id > 0) {
            boolean isCategoryChanged = menuCategoryDAO.changeStatusCategory(id, status);
            boolean isItemsChanged = menuCategoryDAO.changeStatusItemsByCategory(id, status);

            if (isCategoryChanged && isItemsChanged) {
                session.setAttribute("updateSuccess", (status == 1 ? "Kích hoạt" : "Vô hiệu hóa") + " loại món và các món ăn liên quan thành công!");
            } else {
                session.setAttribute("updateFail", "Thay đổi trạng thái thất bại!");
            }
            response.sendRedirect(request.getContextPath() + "/category-management?page=" + page + "&search=" + java.net.URLEncoder.encode(currentSearch, "UTF-8") + "&isAvailable=" + isAvailable);
            return;
        }

        // Khớp hoàn toàn thông báo text kiểm tra dữ liệu với file JSP
        String errorName = isValidString(categoryName, 100, "Tên loại không được để trống", "Tên loại phải ít hơn 100 kí tự");
        if (!checkEmpty(errorName)) {
            if (menuCategoryDAO.checkDuplicateCategory(categoryName, id)) {
                errorName = "Tên loại món ăn đã tồn tại";
            }
        }

        // LUỒNG 2: Xử lý chặn lỗi và tái dựng lại dữ liệu bảng tại chỗ, KHÔNG GỌI BẮC CẦU DOGET TRỰC TIẾP
        if (checkEmpty(errorName)) {
            int totalCategory = menuCategoryDAO.countSearchCategory(currentSearch, isAvailable);
            int totalPage = (int) Math.ceil((double) totalCategory / PAGE_SIZE);
            if (page > totalPage && totalPage > 0) {
                page = totalPage;
            }
            int offset = (page - 1) * PAGE_SIZE;
            List<MenuCategory> categoryList = menuCategoryDAO.searchCategoryPaging(currentSearch, isAvailable, offset, PAGE_SIZE);

            for (MenuCategory mc : categoryList) {
                int activeDish = menuCategoryDAO.countDishByStatus(mc.getCategoryID(), 1);
                int inactiveDish = menuCategoryDAO.countDishByStatus(mc.getCategoryID(), 0);
                int totalDish = menuCategoryDAO.countDishByCategory(mc.getCategoryID());
                mc.setActiveMenuItem(activeDish);
                mc.setInactiveMenuItem(inactiveDish);
                mc.setTotalDish(totalDish);
            }

            // Đẩy lại toàn bộ dữ liệu bẩn và lỗi trực tiếp sang View
            request.setAttribute("errorName", errorName);
            request.setAttribute("currentSearch", currentSearch);
            request.setAttribute("currentAvailable", isAvailable);
            request.setAttribute("categoryList", categoryList);
            request.setAttribute("totalPage", totalPage);
            request.setAttribute("currentPage", page);

            // Găm lại tên vừa nhập lỗi để JavaScript tự động mở lại đúng Modal Popup cho người dùng sửa
            request.setAttribute("modalErrorID", id);
            request.setAttribute("modalErrorName", categoryName);

            request.getRequestDispatcher("/views/admin/category-list.jsp").forward(request, response);
            return;
        }

        // LUỒNG 3: Lưu dữ liệu vào hệ thống khi kiểm tra hoàn tất
        boolean isSuccess = false;
        if (id > 0) {
            isSuccess = menuCategoryDAO.updateCategory(categoryName, id);
            if (isSuccess) {
                session.setAttribute("updateSuccess", "Cập nhật tên loại món ăn thành công!");
            } else {
                session.setAttribute("updateFail", "Cập nhật loại món thất bại hoặc không có thay đổi!");
            }
        } else {
            isSuccess = menuCategoryDAO.insertCategory(categoryName);
            if (isSuccess) {
                session.setAttribute("updateSuccess", "Thêm mới loại món ăn vào thực đơn thành công!");
            } else {
                session.setAttribute("updateFail", "Thêm mới loại món ăn thất bại, vui lòng thử lại!");
            }
        }

        response.sendRedirect(request.getContextPath() + "/category-management?page=" + page + "&search=" + java.net.URLEncoder.encode(currentSearch, "UTF-8") + "&isAvailable=" + isAvailable);
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

    private String isValidString(String data, int length, String ms1, String ms2) {
        if (data == null || data.trim().isEmpty()) {
            return ms1;
        }
        if (data.length() > length) {
            return ms2;
        }
        return "";
    }

    private boolean checkEmpty(String data) {
        return (data != null && !data.trim().isEmpty());
    }
}
