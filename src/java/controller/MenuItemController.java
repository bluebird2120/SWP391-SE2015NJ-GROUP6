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
import java.math.BigDecimal;
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
        List<MenuCategory> list = md.getAllMenuCategory();
        request.setAttribute("list", list);
        request.getRequestDispatcher("/views/admin/dish-list.jsp").forward(request, response);
    }

    private MenuCategoryDAO md = new MenuCategoryDAO();
    private MenuItemDAO mi = new MenuItemDAO();
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
        String search = request.getParameter("search");
        String category_raw = request.getParameter("category");
        String status_raw = request.getParameter("status");
        String minPrice_raw = request.getParameter("minPrice");
        String maxPrice_raw = request.getParameter("maxPrice");
        String priceType = request.getParameter("price");
        String sort = request.getParameter("sort");
               
        int categoryId = ((category_raw != null) && !(category_raw.isEmpty())) ? Integer.parseInt(category_raw) : 0;
        int status = ((status_raw != null) && !(status_raw.isEmpty())) ? Integer.parseInt(status_raw) : 1;
        BigDecimal minPrice = ((minPrice_raw != null) && !(minPrice_raw.isEmpty())) ? new BigDecimal(minPrice_raw) : BigDecimal.ZERO;
        BigDecimal maxPrice = ((maxPrice_raw != null) && !(maxPrice_raw.isEmpty())) ? new BigDecimal(maxPrice_raw) : new BigDecimal("999999999");
        System.out.println("Search = " + search);
        //load lại list category
        List<MenuCategory> list = md.getAllMenuCategory();
        request.setAttribute("list", list);
        //load ra search list
        List<MenuItem> listItem = mi.searchMenuItem(search, categoryId, status, minPrice, maxPrice, sort, priceType);
        request.setAttribute("listItem", listItem);
        request.getRequestDispatcher("/views/admin/dish-list.jsp").forward(request, response);
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
