/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dal.CookingMethodDAO;
import dal.MenuCategoryDAO;
import dal.MenuItemDAO;
import java.io.IOException;
import java.io.PrintWriter;
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

/**
 *
 * @author Admin
 */
@WebServlet(name = "MenuPerformaneController", urlPatterns = {"/menu-performance"})
public class MenuPerformaneController extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet MenuPerformaneController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet MenuPerformaneController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }
    private MenuCategoryDAO menuCategoryDAO = new MenuCategoryDAO();
    private MenuItemDAO menuItemDAO = new MenuItemDAO();
    private CookingMethodDAO cookingMethodDAO = new CookingMethodDAO();
    private static final int PAGE_SIZE = 8;

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String search = request.getParameter("search");
        String category_raw = request.getParameter("category");
        String method_raw = request.getParameter("cookingMethod");
        String page_raw = request.getParameter("page");
        String filterType = request.getParameter("filterType"); // today, week, month
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        //validate
        if (!checkEmpty(search)) {
            search = "";
        }

        int categoryId = checkEmpty(category_raw) ? Integer.parseInt(category_raw) : 0;
        int methodID = checkEmpty(method_raw) ? Integer.parseInt(method_raw) : 0;
        int page = checkEmpty(page_raw) ? Integer.parseInt(page_raw) : 1;

        String errorSearch = isValidString(search, 100, "Tìm kiếm không vượt quá 100 kí tự");
        if (errorSearch != null) {
            request.setAttribute("errorSearch", errorSearch);
            search = "";
        }
        
        //Xử lí quy đổi ngày tháng
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
        } 
        else {
            if (startDate != null && !startDate.trim().isEmpty()
                    && endDate != null && !endDate.trim().isEmpty()) {
                filterType = "custom"; 
            } else {
                startDate = today.toString();
                endDate = today.toString();
                filterType = "today";
            }
        }
        //gọi dao và thực hiện thuật toán phân màu cho món ăn
        int totalItem = menuItemDAO.getTotalPerformanceDishCount(search, categoryId, methodID, startDate, endDate);
        int totalPage = (int) Math.ceil((double) totalItem / PAGE_SIZE);
        if (page > totalPage && totalPage > 0) {
            page = totalPage;
        }

        int offSet = (page - 1) * PAGE_SIZE;
        List<CookingMethod> methodList = cookingMethodDAO.getAllCookingMethod();
        List<MenuCategory> categoryList = menuCategoryDAO.getAllMenuCategory();
        List<MenuItem> menuItemList = menuItemDAO.getPerformanceDish(search, categoryId, methodID, startDate, endDate, offSet, PAGE_SIZE);

        if (menuItemList != null && !menuItemList.isEmpty()) {
            int totalItems = menuItemList.size();
            int totalQtyAllDish = 0;
            //tổng lượng bán của tất cả các món
            for (MenuItem mi : menuItemList) {
                totalQtyAllDish += mi.getTotalQuantity();
            }
            //tính mốc trung bình
            double avgQuantity = (double) totalQtyAllDish / totalItems;
            //tính mốc cảnh báo
            double lowQuantity = avgQuantity * 0.2;
            // Vòng lặp gán nhãn cho các món ăn
            for (MenuItem mi : menuItemList) {
                int qty = mi.getTotalQuantity();

                if (qty >= avgQuantity * 1.5) {
                    mi.setMenuTag("Bán Chạy Nhất");
                    mi.setTagClass("tag-star");       // Màu xanh lá
                } else if (qty >= avgQuantity) {
                    mi.setMenuTag("Tiêu Thụ Ổn Định");
                    mi.setTagClass("tag-plowhorse"); // Màu xanh dương
                } else if (qty > lowQuantity) {
                    mi.setMenuTag("Tiêu Thụ Chậm");
                    mi.setTagClass("tag-puzzle");    // Màu vàng cam
                } else {
                    mi.setMenuTag("Cảnh Báo: Ế Ẩm");
                    mi.setTagClass("tag-dog");       // Màu đỏ
                }
            }
        }

        request.setAttribute("methodList", methodList);
        request.setAttribute("categoryList", categoryList);
        request.setAttribute("menuItemList", menuItemList);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("currentPage", page);
        request.getRequestDispatcher("views/admin/top-selling.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private String isValidString(String data, int length, String ms) {
        if (data.length() > length) {
            return ms;
        }
        return null;
    }

    private boolean checkEmpty(String data) {
        return (data != null && !data.trim().isEmpty());
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
