/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dal.MenuCategoryDAO;
import dal.MenuItemDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import model.MenuCategory;
import model.MenuItem;

/**
 *
 * @author Admin
 */
@WebServlet(name = "DailyStockController", urlPatterns = {"/daily-stock"})
public class DailyStockController extends HttpServlet {

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
            out.println("<title>Servlet DailyStockController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet DailyStockController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }
    private MenuCategoryDAO menuCategoryDAO = new MenuCategoryDAO();
    private MenuItemDAO menuItemDAO = new MenuItemDAO();
    private static final int PAGE_SIZE = 10;

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
        String category_raw = request.getParameter("categoryID");
        String page_raw = request.getParameter("page");
        
        if (!checkEmpty(search)) {
            search = "";
        }
        
        String errorSearch = checkLength(search, 100) ? "Tìm kiếm không vượt quá 100 kí tự" : "";

        int categoryID = checkEmpty(category_raw) ? Integer.parseInt(category_raw) : 0;
        int page = checkEmpty(page_raw) ? Integer.parseInt(page_raw) : 1;

        if (!errorSearch.trim().isEmpty()) {
            request.setAttribute("errorSearch", errorSearch);
            search = "";
        }

        int totalItem = menuItemDAO.countSearchDish(search, categoryID);
        int totalPage = (int) Math.ceil((double) totalItem / PAGE_SIZE);

        if (page > totalPage && totalPage > 0) {
            page = totalPage;
        }

        int offSet = (page - 1) * PAGE_SIZE;
        List<MenuCategory> list = menuCategoryDAO.getAllMenuCategory();
        List<MenuItem> listItem = menuItemDAO.searchDishPaging(search, categoryID, offSet, PAGE_SIZE);
        
        request.setAttribute("categoryList", list);
        request.setAttribute("menuItemList", listItem);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("currentPage", page);
        request.getRequestDispatcher("/views/admin/daily-stock.jsp").forward(request, response);
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
        String quantity_raw = request.getParameter("quantity");
        String itemID_raw = request.getParameter("itemID");
        
        int quantity = checkEmpty(quantity_raw) ? Integer.parseInt(quantity_raw) : 0;
        int itemID = checkEmpty(itemID_raw) ? Integer.parseInt(itemID_raw) : 0;
    }

    private boolean checkEmpty(String data) {
        return (data != null && !data.trim().isEmpty());
    }

    private boolean checkLength(String data, int length) {
        return data.length() > length;
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
