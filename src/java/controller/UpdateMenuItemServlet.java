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
import java.io.File;
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
        MenuItem mi = menuItemDAO.getMenuItemById(id);
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
        String oldImage = request.getParameter("oldImage");
        Part newImage = request.getPart("newImage");

        String errorName = isEmpty(itemName, "Tên món ăn rỗng!");
        String errorDescription = isEmpty(description_raw, "Mô tả món ăn rỗng!");
        String errorPrice = isValidPositive(price_raw, "Giá món ăn rỗng!", "Giá món ăn là số nguyên dương!");
        String errorDiscountPercent = isValidPositive(discountPercent_raw, "Giảm giá món ăn rỗng!", "Giảm giá món ăn là số nguyên dương!");
        String errorAllergyNotes = isEmpty(allergyNotes_raw, "Mô tả dị ứng ăn rỗng!");

        if (!errorName.isEmpty() || !errorDescription.isEmpty() || !errorPrice.isEmpty()
                || !errorDiscountPercent.isEmpty() || !errorAllergyNotes.isEmpty()) {
            int id = ((menuItemId_raw != null) && (!menuItemId_raw.isEmpty())) ? Integer.parseInt(menuItemId_raw) : 0;
            MenuItem mi = menuItemDAO.getMenuItemById(id);
            List categoryList = menuCategoryDAO.getAllMenuCategory();
            request.setAttribute("dish", mi);
            request.setAttribute("list", categoryList);

            request.setAttribute("errorName", errorName);
            request.setAttribute("errorDescription", errorDescription);
            request.setAttribute("errorPrice", errorPrice);
            request.setAttribute("errorDiscountPercent", errorDiscountPercent);
            request.setAttribute("errorAllergyNotes", errorAllergyNotes);
            request.getRequestDispatcher("views/admin/dish-update.jsp").forward(request, response);
            return;
        } else {
            int itemId = Integer.parseInt(menuItemId_raw);
            int categoryId = Integer.parseInt(categoryId_raw);
            int price = Integer.parseInt(price_raw);
            int discountPercent = Integer.parseInt(discountPercent_raw);
            int status = ((isAvailable_raw != null) && (!isAvailable_raw.isEmpty())) ? 1 : 0;

            String fileName = oldImage;
            if(newImage != null && !newImage.getSubmittedFileName().isEmpty()){
                String imageName = newImage.getSubmittedFileName();
                fileName = "images/" + imageName;
                
                String upLoadSource = "D:\\Knowledge\\ki5\\SWP\\Project\\Restaurant-Reservation-And-Table-Service-System\\build\\web\\images";
                File sourceFile = new File(upLoadSource + File.separator + imageName);
                
                newImage.write(sourceFile.getAbsolutePath());
                
                String upLoadServer = getServletContext().getRealPath("/images");
                File cachFile = new File(upLoadServer);
                
                try (java.io.FileInputStream fis = new java.io.FileInputStream(sourceFile);
                     java.io.FileOutputStream fos = new java.io.FileOutputStream(cachFile)){
                    fis.transferTo(fos);
                } catch (Exception e) {
                }
            }
            menuItemDAO.updateMenuItem(itemId, categoryId, itemName, description_raw, price,
                    discountPercent, fileName, status, allergyNotes_raw);
            response.sendRedirect("update-menu?id=" + itemId);
        }

    }

    public String isEmpty(String data, String ms) {
        return ((data != null) && (!data.trim().isEmpty()) ? "" : ms);
    }

    public String isValidPositive(String data, String ms1, String ms2) {
        try {
            if (data != null && !data.trim().isEmpty()) {
                if (Integer.parseInt(data) < 0) {
                    return ms2;
                }
            } else {
                return ms1;
            }
        } catch (NumberFormatException e) {
            return ms2;
        }
        return "";
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
