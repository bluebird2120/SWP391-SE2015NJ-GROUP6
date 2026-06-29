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

@WebServlet(name = "MethodCookingController", urlPatterns = {"/method-management"})
public class MethodCookingController extends HttpServlet {

    private CookingMethodDAO cookingMethodDAO = new CookingMethodDAO();
    private static final int PAGE_SIZE = 8;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String search = request.getParameter("search");
        String page_raw = request.getParameter("page");

        if (!checkEmpty(search)) {
            search = "";
        }
        int page = checkEmpty(page_raw) ? Integer.parseInt(page_raw) : 1;
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

        int totalCategory = cookingMethodDAO.countSearchMethod(search);
        int totalPage = (int) Math.ceil((double) totalCategory / PAGE_SIZE);

        if (page > totalPage && totalPage > 0) {
            page = totalPage;
        }

        int offset = (page - 1) * PAGE_SIZE;
        List<CookingMethod> methodList = cookingMethodDAO.searchMethodPaging(search, offset, PAGE_SIZE);
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
        request.getRequestDispatcher("views/admin/method-list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String methodID = request.getParameter("methodID");
        String methodName = request.getParameter("methodName");
        String status_raw = request.getParameter("status");
        int id = checkEmpty(methodID) ? Integer.parseInt(methodID) : 0;
        
        //lấy các dữ liệu cũ bên jsp để khi vô hiệu hóa trả về đúng trang
        String page_raw = request.getParameter("page");
        String search_raw = request.getParameter("search");
        String currentPage = checkEmpty(page_raw) ? page_raw : "1";
        String currentSearch = checkEmpty(search_raw) ? search_raw : "";

        if (checkEmpty(status_raw) && id > 0) {
            int status = checkEmpty(status_raw) ? Integer.parseInt(status_raw) : 0;
            cookingMethodDAO.changeStatusMethod(id, status);
            
            response.sendRedirect(request.getContextPath() + "/method-management?page=" + currentPage + "&search=" + java.net.URLEncoder.encode(currentSearch, "UTF-8"));
            return;
        }

        // 1. Validate định dạng chuỗi chữ
        String errorName = isValidString(methodName, 100, "Tên cách chế biến không được để trống", "Tên cách chế biến phải ít hơn 100 kí tự");

        // 2. Kiểm tra trùng lặp tên phương thức chế biến dưới DB
        if (!checkEmpty(errorName)) {
            if (cookingMethodDAO.checkDuplicateMethod(methodName, id)) {
                errorName = "Tên cách chế biến này đã tồn tại trên thực đơn!";
            }
        }

        if (!errorName.isEmpty()) {
            request.setAttribute("errorName", errorName);
            doGet(request, response);
            return;
        }

        // 3. Thực hiện gọi DAO lưu trữ dữ liệu
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

        response.sendRedirect(request.getContextPath() + "/method-management");
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
