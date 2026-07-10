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

        if (!checkEmpty(search)) {
            search = "";
        }
        int page = checkEmpty(page_raw) ? Integer.parseInt(page_raw) : 1;

        // Mặc định gán là -1 (TẤT CẢ)
        int isAvailable = checkEmpty(isAvailable_raw) ? Integer.parseInt(isAvailable_raw) : -1;
        String errorSearch = search.length() > 100 ? "Tìm kiếm vượt quá 100 kí tự" : "";

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

        request.setAttribute("currentAvailable", isAvailable);
        request.setAttribute("categoryList", categoryList);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("currentPage", page);

        for (MenuCategory mc : categoryList) {
            int activeDish = menuCategoryDAO.countDishByStatus(mc.getCategoryID(), 1);
            int inactiveDish = menuCategoryDAO.countDishByStatus(mc.getCategoryID(), 0);
            int totalDish = menuCategoryDAO.countDishByCategory(mc.getCategoryID());
            mc.setActiveMenuItem(activeDish);
            mc.setInactiveMenuItem(inactiveDish);
            mc.setTotalDish(totalDish);
        }
        request.getRequestDispatcher("views/admin/category-list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String categoryID = request.getParameter("categoryID");
        String categoryName = request.getParameter("categoryName");
        String status_raw = request.getParameter("status");
        int id = checkEmpty(categoryID) ? Integer.parseInt(categoryID) : 0;

        String page_raw = request.getParameter("page");
        String search_raw = request.getParameter("search");
        String isAvailable_raw = request.getParameter("isAvailable");

        String currentPage = checkEmpty(page_raw) ? page_raw : "1";
        String currentSearch = checkEmpty(search_raw) ? search_raw : "";
        String currentAvailable = checkEmpty(isAvailable_raw) ? isAvailable_raw : "-1";

        // Logic đổi trạng thái danh mục và món ăn đi kèm
        if (checkEmpty(status_raw) && id > 0) {
            int status = Integer.parseInt(status_raw);

            boolean isCategoryChanged = menuCategoryDAO.changeStatusCategory(id, status);
            boolean isItemsChanged = menuCategoryDAO.changeStatusItemsByCategory(id, status);

            if (isCategoryChanged && isItemsChanged) {
                session.setAttribute("updateSuccess", (status == 1 ? "Kích hoạt" : "Vô hiệu hóa") + " loại món và các món ăn liên quan thành công!");
            } else {
                session.setAttribute("updateFail", "Thay đổi trạng thái thất bại!");
            }
            response.sendRedirect(request.getContextPath() + "/category-management?page=" + currentPage + "&search=" + java.net.URLEncoder.encode(currentSearch, "UTF-8") + "&isAvailable=" + currentAvailable);
            return;
        }

        String errorName = isValidString(categoryName, 100, "Tên loại không được để trống", "Tên loại phải ít hơn 100 kí tự");
        if (!checkEmpty(errorName)) {
            if (menuCategoryDAO.checkDuplicateCategory(categoryName, id)) {
                errorName = "Tên loại món ăn đã tồn tại";
            }
        }
        if (!errorName.isEmpty()) {
            request.setAttribute("errorName", errorName);
            doGet(request, response);
            return;
        }

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

        response.sendRedirect(request.getContextPath() + "/category-management");
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
