package controller;

import dal.CookingMethodDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.CookingMethod;

@WebServlet(name = "MethodCookingController", urlPatterns = {"/owner/method-management"})
public class MethodCookingController extends HttpServlet {

    private CookingMethodDAO cookingMethodDAO = new CookingMethodDAO();
    private static final int PAGE_SIZE = 8;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String search = request.getParameter("search");
        String page_raw = request.getParameter("page");
        String isAvailable_raw = request.getParameter("isAvailable");

        //Validate
        if (!checkEmpty(search)) {
            search = "";
        }

        int page = parseIntSafe(page_raw, 1, 1);
        int isAvailable = parseIntSafe(isAvailable_raw, -1, -1);

        String errorSearch = search.length() > 100 ? "Tìm kiếm vượt quá 100 kí tự" : "";
        String currentSearch = search;
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

        int totalMethod = cookingMethodDAO.countSearchMethod(search, isAvailable);
        int totalPage = (int) Math.ceil((double) totalMethod / PAGE_SIZE);

        if (page > totalPage && totalPage > 0) {
            page = totalPage;
        }

        int offset = (page - 1) * PAGE_SIZE;
        List<CookingMethod> methodList = cookingMethodDAO.searchMethodPaging(search, isAvailable, offset, PAGE_SIZE);

        //Trả các biến dữ liệu số và chuỗi sạch sang cho JSP hiển thị lại bộ lọc
        request.setAttribute("currentSearch", currentSearch);
        request.setAttribute("currentAvailable", isAvailable);
        request.setAttribute("methodList", methodList);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("currentPage", page);

        for (CookingMethod cookingMethod : methodList) {
            int activeDish = cookingMethodDAO.countMethodByStatus(cookingMethod.getMethodID(), 1);
            int inactiveDish = cookingMethodDAO.countMethodByStatus(cookingMethod.getMethodID(), 0);
            int totalDish = cookingMethodDAO.countDishByMethod(cookingMethod.getMethodID());
            cookingMethod.setActiveMenuItem(activeDish);
            cookingMethod.setInactiveMenuItem(inactiveDish);
            cookingMethod.setTotalDish(totalDish);
        }
        request.getRequestDispatcher("/views/owner/method-list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String methodID = request.getParameter("methodID");
        String methodName = request.getParameter("methodName");
        String status_raw = request.getParameter("status");
        String page_raw = request.getParameter("page");
        String search_raw = request.getParameter("search");
        String isAvailable_raw = request.getParameter("isAvailable");

        //Validate
        int id = parseIntSafe(methodID, 0, 0);
        int status = parseIntSafe(status_raw, -1, -1);
        int currentPage = parseIntSafe(page_raw, 1, 1);
        String currentSearch = checkEmpty(search_raw) ? search_raw : "";
        int currentAvailable = parseIntSafe(isAvailable_raw, -1, -1);
        String errorName = isValidString(methodName, 100, "Tên cách chế biến không được để trống", "Tên cách chế biến phải ít hơn 100 kí tự");
        
        // Logic đổi trạng thái danh mục chính và món ăn ăn theo
        if (status != -1 && id > 0) {
            boolean isMethodChanged = cookingMethodDAO.changeStatusMethod(id, status);
            boolean isItemsChanged = cookingMethodDAO.changeStatusItemsByMethod(id, status);

            if (isMethodChanged && isItemsChanged) {
                session.setAttribute("updateSuccess", (status == 1 ? "Kích hoạt" : "Vô hiệu hóa") + " cách chế biến và các món ăn liên quan thành công!");
            } else {
                session.setAttribute("updateFail", "Thay đổi trạng thái thất bại!");
            }

            response.sendRedirect(request.getContextPath() + "/owner/method-management?page=" + currentPage + "&search=" + java.net.URLEncoder.encode(currentSearch, "UTF-8") + "&isAvailable=" + currentAvailable);
            return;
        }

        // Validate tên trước khi update và create
        if (!checkEmpty(errorName)) {
            if (cookingMethodDAO.checkDuplicateMethod(methodName, id)) {
                errorName = "Tên cách chế biến này đã tồn tại trên thực đơn!";
            }
        }

        // Nếu có lỗi thì gửi sang jsp
        if (checkEmpty(errorName)) {
            int totalMethod = cookingMethodDAO.countSearchMethod(currentSearch, currentAvailable);
            int totalPage = (int) Math.ceil((double) totalMethod / PAGE_SIZE);
            if (currentPage > totalPage && totalPage > 0) {
                currentPage = totalPage;
            }

            int offset = (currentPage - 1) * PAGE_SIZE;
            List<CookingMethod> methodList = cookingMethodDAO.searchMethodPaging(
                    currentSearch, currentAvailable, offset, PAGE_SIZE);

            for (CookingMethod cookingMethod : methodList) {
                int activeDish = cookingMethodDAO.countMethodByStatus(cookingMethod.getMethodID(), 1);
                int inactiveDish = cookingMethodDAO.countMethodByStatus(cookingMethod.getMethodID(), 0);
                int totalDish = cookingMethodDAO.countDishByMethod(cookingMethod.getMethodID());
                cookingMethod.setActiveMenuItem(activeDish);
                cookingMethod.setInactiveMenuItem(inactiveDish);
                cookingMethod.setTotalDish(totalDish);
            }

            request.setAttribute("errorName", errorName);
            request.setAttribute("currentSearch", currentSearch);
            request.setAttribute("currentAvailable", currentAvailable);
            request.setAttribute("methodList", methodList);
            request.setAttribute("totalPage", totalPage);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("modalErrorID", id);
            request.setAttribute("modalErrorName", methodName);
            request.getRequestDispatcher("/views/owner/method-list.jsp").forward(request, response);
            return;
        }

        // Thực hiện lưu dữ liệu khi thêm mới hoặc sửa tên
        boolean isSuccess = false;
        if (id > 0) {
            isSuccess = cookingMethodDAO.updateMethod(methodName, id);
            if (isSuccess) {
                session.setAttribute("updateSuccess", "Cập nhật cách chế biến thành công!");
            } else {
                session.setAttribute("updateFail", "Cập nhật thất bại hoặc không phát hiện thay đổi!");
            }
        } else {
            isSuccess = cookingMethodDAO.insertMethod(methodName);
            if (isSuccess) {
                session.setAttribute("updateSuccess", "Thêm mới cách chế biến thành công!");
            } else {
                session.setAttribute("updateFail", "Thêm mới cách chế biến thất bại, vui lòng thử lại!");
            }
        }

        response.sendRedirect(request.getContextPath() + "/owner/method-management");
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
