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
import model.MenuItem;

/**
 *
 * @author Admin
 */
@WebServlet(name = "MenuItemController", urlPatterns = {"/menu-management"})
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

        if (!checkEmpty(priceType)) {
            List<MenuCategory> list = md.getAllMenuCategory();
            List<MenuItem> listItem = mi.getAllMenuItem();
            request.setAttribute("listItem", listItem);
            request.setAttribute("list", list);
            request.getRequestDispatcher("/views/admin/dish-list.jsp").forward(request, response);
            return;
        } else {
            int categoryId = checkEmpty(category_raw) ? Integer.parseInt(category_raw) : 0;
            int status = checkEmpty(status_raw) ? Integer.parseInt(status_raw) : 1;
            int minPrice = checkEmpty(minPrice_raw) ? Integer.parseInt(minPrice_raw) : 0;
            int maxPrice = checkEmpty(maxPrice_raw) ? Integer.parseInt(maxPrice_raw) : 999999999;

            if (checkPriceInput(minPrice, maxPrice) != null || isValidString(search, 100, 
                    "Tìm kiếm không vượt quá 100 kí tự") != null) {
                List<MenuCategory> list = md.getAllMenuCategory();
                List<MenuItem> listItem = mi.getAllMenuItem();
                request.setAttribute("listItem", listItem);
                request.setAttribute("list", list);
                request.setAttribute("errorPrice", checkPriceInput(minPrice, maxPrice));
                request.setAttribute("errorSearch", isValidString(search, 100, "Tìm kiếm không vượt quá 100 kí tự"));
                request.getRequestDispatcher("/views/admin/dish-list.jsp").forward(request, response);
                return;
            }
            //load lại list category
            List<MenuCategory> list = md.getAllMenuCategory();
            request.setAttribute("list", list);
            //load ra search list
            List<MenuItem> listItem = mi.searchMenuItem(search, categoryId, status, minPrice, maxPrice, sort, priceType);
            request.setAttribute("listItem", listItem);
            request.getRequestDispatcher("/views/admin/dish-list.jsp").forward(request, response);
        }

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
        return (data != null && !data.isEmpty());
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
