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

import model.MenuCategory;
import java.util.List;
import dal.MenuCategoryDAO;

/**
 *
 * @author Admin
 */
@WebServlet(name = "CategoryController", urlPatterns = {"/category-management"})
public class CategoryController extends HttpServlet {

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
            out.println("<title>Servlet CategoryController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet CategoryController at " + request.getContextPath() + "</h1>");
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
    private MenuCategoryDAO menuCategoryDAO = new MenuCategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<MenuCategory> categoryList = menuCategoryDAO.getAllMenuCategory();
        for (MenuCategory mc : categoryList) {
            int activeDish = menuCategoryDAO.countDishByStatus(mc.getCategoryID(), 1);
            int inactiveDish = menuCategoryDAO.countDishByStatus(mc.getCategoryID(), 0);
            int totalDish = menuCategoryDAO.countDishByCategory(mc.getCategoryID());
            mc.setActiveMenuItem(activeDish);
            mc.setInactiveMenuItem(inactiveDish);
            mc.setTotalDish(totalDish);
        }
        request.setAttribute("categoryList", categoryList);
        request.getRequestDispatcher("views/admin/category-list.jsp").forward(request, response);
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
        String categoryID = request.getParameter("categoryID");
        String categoryName = request.getParameter("categoryName");
        int id = checkEmpty(categoryID) ? Integer.parseInt(categoryID) : 0;

        //validate
        String errorName = isValidString(categoryName, 100, "Tên loại không được để trống", "Tên loại phải ít hơn 100 kí tự");
        if (!errorName.isEmpty()) {
            List<MenuCategory> categoryList = menuCategoryDAO.getAllMenuCategory();
            for (MenuCategory mc : categoryList) {
                int activeDish = menuCategoryDAO.countDishByStatus(mc.getCategoryID(), 1);
                int inactiveDish = menuCategoryDAO.countDishByStatus(mc.getCategoryID(), 0);

                mc.setActiveMenuItem(activeDish);
                mc.setInactiveMenuItem(inactiveDish);
            }
            request.setAttribute("categoryList", categoryList);
            request.setAttribute("errorName", errorName);
            request.getRequestDispatcher("views/admin/category-list.jsp").forward(request, response);
            return;
        }

        //call update and create method
        if (id > 0) {
            menuCategoryDAO.updateCategory(categoryName, id);
        } else {
            menuCategoryDAO.insertCategory(categoryName);
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
