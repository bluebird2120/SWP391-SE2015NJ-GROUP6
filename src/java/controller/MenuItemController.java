/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import model.MenuCategory;
import dal.MenuCategoryDAO;
import dal.MenuItemDAO;
import jakarta.servlet.http.HttpSession;
import model.MenuItem;

/**
 *
 * @author Admin
 */
@WebServlet(name = "MenuItemController", urlPatterns = {"/menu"})
public class MenuItemController extends HttpServlet {

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
            out.println("<title>Servlet MenuItemController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet MenuItemController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

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
        String status_raw = request.getParameter("status");
        String minPrice_raw = request.getParameter("minPrice");
        String maxPrice_raw = request.getParameter("maxPrice");
        String priceType = request.getParameter("price");
        String sort = request.getParameter("sort");
        String page_raw = request.getParameter("page");
        String tableID_raw = request.getParameter("tableID");
             
        if (!checkEmpty(search)) {
            search = "";
        }
        if (!checkEmpty(priceType)) {
            priceType = "discountedPrice";
        }
        if (!checkEmpty(sort)) {
            sort = "asc";
        }

        int status = checkEmpty(status_raw) ? Integer.parseInt(status_raw) : 1;
        int categoryId = checkEmpty(category_raw) ? Integer.parseInt(category_raw) : 0;
        int minPrice = 0;
        int maxPrice = 0;
        try {
            minPrice = checkEmpty(minPrice_raw) ? Integer.parseInt(minPrice_raw) : 0;
        } catch (NumberFormatException e) {
            minPrice = 0; 
            request.setAttribute("errorPrice", "Giá tiền nhập vào vượt quá giới hạn cho phép!");
        }

        try {
            maxPrice = checkEmpty(maxPrice_raw) ? Integer.parseInt(maxPrice_raw) : Integer.MAX_VALUE;
        } catch (NumberFormatException e) {
            maxPrice = Integer.MAX_VALUE;
            request.setAttribute("errorPrice", "Giá tiền nhập vào vượt quá giới hạn cho phép!");
        }
        int page = checkEmpty(page_raw) ? Integer.parseInt(page_raw) : 1;
        int tableID = checkEmpty(tableID_raw) ? Integer.parseInt(tableID_raw) : 0;
        String errorPrice = checkPriceInput(minPrice, maxPrice);
        String errorSearch = isValidString(search, 100, "Tìm kiếm không vượt quá 100 kí tự");

        if (errorPrice != null) {
            request.setAttribute("errorPrice", errorPrice);
            minPrice = 0;
            maxPrice = Integer.MAX_VALUE;
        }
        if (errorSearch != null) {
            request.setAttribute("errorSearch", errorSearch);
            search = "";
        }

        int totalItem = mi.countSearchMenuItem(search, categoryId, status, minPrice, maxPrice, priceType);
        int totalPage = (int) Math.ceil((double) totalItem / PAGE_SIZE);

        if (page > totalPage && totalPage > 0) {
            page = totalPage;
        }

        int offSet = (page - 1) * PAGE_SIZE;

        List<MenuCategory> list = md.getAllMenuCategory();
        List<MenuItem> listItem = mi.searchMenuItemPaging(search, categoryId, status, minPrice, maxPrice, sort, priceType, offSet, PAGE_SIZE);

        request.setAttribute("list", list);
        request.setAttribute("listItem", listItem);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("currentPage", page);
        HttpSession session = request.getSession();
        session.setAttribute("currentTableID", tableID);
        
        if (checkEmpty(tableID_raw)) {
            request.getRequestDispatcher("/views/user/menu.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/views/admin/dish-list.jsp").forward(request, response);
        }
    }

    private MenuCategoryDAO md = new MenuCategoryDAO();
    private MenuItemDAO mi = new MenuItemDAO();
    private static final int PAGE_SIZE = 8;

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

    }

    private String isValidString(String data, int length, String ms) {
        if (data.length() > length) {
            return ms;
        }
        return null;
    }

    private String checkPriceInput(int min, int max) {
        if (min < 0 || max < 0) {
            return "Giá món ăn không được là số âm";
        } else {
            if (min > max) {
                return "Giá max phải lớn hơn giá Min";
            }
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
