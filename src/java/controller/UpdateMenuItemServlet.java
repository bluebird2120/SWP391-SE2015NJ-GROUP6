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
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.util.List;
import model.MenuItem;

/**
 *
 * @author Admin
 */
@WebServlet(name = "UpdateMenuItemServlet", urlPatterns = {"/update-menu"})
@MultipartConfig
public class UpdateMenuItemServlet extends HttpServlet {

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
            out.println("<title>Servlet UpdateMenuItemServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet UpdateMenuItemServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }
    private MenuItemDAO menuItemDAO = new MenuItemDAO();
    private MenuCategoryDAO menuCategoryDAO = new MenuCategoryDAO();
    private MenuItem mi = new MenuItem();
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
        String id_raw = request.getParameter("id");
        int id = ((id_raw != null) && (!id_raw.isEmpty())) ? Integer.parseInt(id_raw) : 0;
        mi = menuItemDAO.getMenuItemById(id);
        List categoryList = menuCategoryDAO.getAllMenuCategory();
        request.setAttribute("dish", mi);
        request.setAttribute("list", categoryList);
        request.getRequestDispatcher("views/admin/dish-update.jsp").forward(request, response);
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
        
        String itemName = request.getParameter("name");
        String menuItemId_raw = request.getParameter("id");
        String categoryId_raw = request.getParameter("category");
        String description_raw = request.getParameter("description");
        String price_raw = request.getParameter("price");
        String discountPercent_raw = request.getParameter("discountPercent");
        String isAvailable_raw = request.getParameter("isAvailable");
        String allergyNotes_raw = request.getParameter("allergyNotes");
        Part image = request.getPart("image");
        String oldImage = request.getParameter("image");
        
        String errorName = ((itemName != null) && !(itemName.isEmpty())) ? "" : "Tên món ăn rỗng!";
        String errorDescription = ((description_raw != null) && !(description_raw.isEmpty())) ? "" : "Mô tả món ăn rỗng!";
        String errorPrice = ((price_raw != null) && !(price_raw.isEmpty())) ? "" : "Giá món ăn rỗng!";  
        String errorDiscountPercent = ((discountPercent_raw != null) && !(discountPercent_raw.isEmpty())) ? "" : "Giảm giá món ăn rỗng!";
        String errorAllergyNotes = ((allergyNotes_raw != null) && !(allergyNotes_raw.isEmpty())) ? "" : "Ghi chú dị ứng món ăn rỗng!";
        
        int itemId = Integer.parseInt(menuItemId_raw);
        int categoryId = Integer.parseInt(categoryId_raw);
        int price = Integer.parseInt(price_raw);
        errorPrice = (price >= 0) ? "" : "Giá món ăn phải là số nguyên";
        int discountPercent = Integer.parseInt(discountPercent_raw);
        int status = ((isAvailable_raw != null) && (!isAvailable_raw.isEmpty())) ? 1 : 0;
        String fileName = (image != null) ? image.getSubmittedFileName() : oldImage;
        
        boolean result = menuItemDAO.updateMenuItemById(itemId, categoryId, itemName, description_raw, price, 
                discountPercent, fileName, status, allergyNotes_raw);
        request.setAttribute("errorName", errorName);
        request.setAttribute("errorDescription", errorDescription);
        request.setAttribute("errorPrice", errorPrice);
        request.setAttribute("errorDiscountPercent", errorDiscountPercent);
        request.setAttribute("errorAllergyNotes", errorAllergyNotes);
        request.setAttribute("result", result);
        request.setAttribute("dish", mi);
        request.getRequestDispatcher("views/admin/dish-update.jsp").forward(request, response);
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
