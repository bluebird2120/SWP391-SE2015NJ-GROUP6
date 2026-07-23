/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dal.MenuItemDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.MenuItem;
import model.MenuItemImages;

/**
 *
 * @author Admin
 */
@WebServlet(name = "DishDetailController", urlPatterns = {"/dish-detail"})
public class DishDetailController extends HttpServlet {

    private MenuItemDAO menuItemDao = new MenuItemDAO();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet DishDetailController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet DishDetailController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // 1. Lấy backUrl từ session, nếu null thì set mặc định về trang chủ hoặc menu
        String backUrl = (String) session.getAttribute("lastDishListUrl");
        System.out.println(backUrl);
        if (backUrl == null || backUrl.trim().isEmpty()) {
            backUrl = "menu"; // Thay "menu" bằng URL mặc định trang danh sách món ăn của bạn
        }

        // 2. Validate URL ID an toàn
        String id_raw = request.getParameter("id");
        int itemID = parseIntSafe(id_raw, 0);

        // Nếu người dùng cố tình nhập ?id=abc hoặc không truyền id, tự động đá về trang trước
        if (itemID == 0) {
            response.sendRedirect(backUrl);
            return;
        }

        // 3. Truy vấn DB
        MenuItem mi = menuItemDao.getMenuItemById(itemID);

        // Nếu cố tình nhập ID không tồn tại (vd: ?id=9999), đá về trang trước để tránh lỗi Null
        if (mi == null) {
            response.sendRedirect(backUrl);
            return;
        }

        List<MenuItemImages> subImageList = menuItemDao.getImagesByMenuItemId(itemID);

        Object tableObj = session.getAttribute("currentTableID");
        int tableID = (tableObj != null) ? (Integer) tableObj : 0;

        // 4. Đẩy dữ liệu sang JSP
        request.setAttribute("currentTableID", tableID);
        request.setAttribute("dish", mi);
        request.setAttribute("imageList", subImageList);
        request.setAttribute("backUrl", backUrl); // Đẩy backUrl sang để nút Back trên JSP dùng

        request.getRequestDispatcher("/views/user/dish-detail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

    // Hàm tiện ích bẫy lỗi URL (Ép kiểu an toàn)
    private int parseIntSafe(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
