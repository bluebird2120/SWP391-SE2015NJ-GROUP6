/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dal.CookingMethodDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import model.CookingMethod;
import model.MenuCategory;

/**
 *
 * @author Admin
 */
@WebServlet(name = "MethodCookingController", urlPatterns = {"/method-management"})
public class MethodCookingController extends HttpServlet {

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
            out.println("<title>Servlet MethodCookingController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet MethodCookingController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

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
        String methodID = request.getParameter("methodID");
        String methodName = request.getParameter("methodName");
        String status_raw = request.getParameter("status");
        int id = checkEmpty(methodID) ? Integer.parseInt(methodID) : 0;

        if (checkEmpty(status_raw) && id > 0) {
            int status = checkEmpty(status_raw) ? Integer.parseInt(status_raw) : 0;
            cookingMethodDAO.changeStatusMethod(id, status);
            response.sendRedirect(request.getContextPath() + "/method-management");
            return;
        }
        //validate
        String errorName = isValidString(methodName, 100, "Tên loại không được để trống", "Tên loại phải ít hơn 100 kí tự");
        if (!errorName.isEmpty()) {
            List<CookingMethod> methodList = cookingMethodDAO.getAllCookingMethod();
            for (CookingMethod mc : methodList) {
                int activeDish = cookingMethodDAO.countMethodByStatus(id, 1);
                int inactiveDish = cookingMethodDAO.countMethodByStatus(id, 0);
                int totalDish = cookingMethodDAO.countDishByMethod(id);
                mc.setActiveMenuItem(activeDish);
                mc.setInactiveMenuItem(inactiveDish);
                mc.setTotalDish(totalDish);
            }
            request.setAttribute("methodList", methodList);
            request.setAttribute("errorName", errorName);
            request.getRequestDispatcher("views/admin/method-list.jsp").forward(request, response);
            return;
        }

        //call update and create method
        boolean isSuccess = false;
        if (id > 0) {
            isSuccess = cookingMethodDAO.updateMethod(methodName, id);
            if (isSuccess) {
                request.setAttribute("annouce", "Cập nhật cách chế biến thành công");
            }else{
                request.setAttribute("annouce", "Cập nhật thất bại vui lòng thử lại");
            }
        } else {
            isSuccess = cookingMethodDAO.insertMethod(methodName);
            if (isSuccess) {
                request.setAttribute("annouce", "Thêm mới cách chế biến thành công");
            }else{
                request.setAttribute("annouce", "Thêm mới thất bại vui lòng thử lại");
            }
        }
        request.setAttribute("isSuccess", isSuccess);
        doGet(request, response);
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
