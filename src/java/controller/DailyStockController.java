/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dal.CookingMethodDAO;
import dal.DailyInventoryDAO;
import dal.MenuCategoryDAO;
import dal.MenuItemDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.CookingMethod;
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
    private DailyInventoryDAO dailyInventoryDAO = new DailyInventoryDAO();
    private CookingMethodDAO cm = new CookingMethodDAO();
    private static final int PAGE_SIZE = 100;

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
        String method_raw = request.getParameter("cookingMethod");

        if (!checkEmpty(search)) {
            search = "";
        }

        String errorSearch = checkLength(search, 100) ? "Tìm kiếm không vượt quá 100 kí tự" : "";

        int categoryID = checkEmpty(category_raw) ? Integer.parseInt(category_raw) : 0;
        int methodID = checkEmpty(method_raw) ? Integer.parseInt(method_raw) : 0;
        int page = checkEmpty(page_raw) ? Integer.parseInt(page_raw) : 1;

        if (!errorSearch.trim().isEmpty()) {
            request.setAttribute("errorSearch", errorSearch);
            search = "";
        }

        int totalItem = menuItemDAO.countSearchDish(search, categoryID, methodID);
        int totalPage = (int) Math.ceil((double) totalItem / PAGE_SIZE);
        
        if (page > totalPage && totalPage > 0) {
            page = totalPage;
        }

        int offSet = (page - 1) * PAGE_SIZE;
        List<CookingMethod> listMethod = cm.getAllCookingMethod();
        List<MenuCategory> list = menuCategoryDAO.getAllMenuCategory();
        List<MenuItem> listItem = menuItemDAO.searchDishPaging(search, categoryID, methodID, offSet, PAGE_SIZE);
        
        request.setAttribute("listMethod", listMethod);
        request.setAttribute("hasLowStock", checkHasLowStock(listItem));
        request.setAttribute("isConfigYet", checkConfigYet(listItem));
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
        String[] initialQuantity = request.getParameterValues("initialQuantity");
        String[] itemID = request.getParameterValues("itemID");

        boolean hasError = false;
        String errorMessage = "";

        if (initialQuantity == null || itemID == null || initialQuantity.length != itemID.length) {
            hasError = true;
            errorMessage = "Dữ liệu đầu vào không hợp lệ!";
        } else {
            for (int i = 0; i < initialQuantity.length; i++) {
                String qty = initialQuantity[i];

                if (!checkEmpty(qty)) {
                    hasError = true;
                    errorMessage = "Lỗi hệ thống: Bạn bắt buộc phải nhập đầy đủ số lượng các món ăn";
                    break;
                }

                errorMessage = checkQuantity(qty);
                if (checkEmpty(errorMessage)) {
                    hasError = true;
                    break;
                }
            }
        }
        if (hasError) {
            Map<Integer, String> saveInputData = new HashMap<>();
            if (itemID != null && initialQuantity != null) {
                for (int i = 0; i < itemID.length; i++) {
                    try {
                        int itemId = Integer.parseInt(itemID[i]);
                        String qtyValue = initialQuantity[i];
                        saveInputData.put(itemId, qtyValue);
                    } catch (Exception e) {
                    }
                }
            }
            request.setAttribute("errorMessage", errorMessage);
            request.setAttribute("saveInputData", saveInputData);
            doGet(request, response);
        } else {
            for (int i = 0; i < itemID.length; i++) {
                dailyInventoryDAO.updateStockMenuItem(Integer.parseInt(itemID[i]), Integer.parseInt(initialQuantity[i]));
            }
            response.sendRedirect(request.getContextPath() + "/daily-stock");
        }
    }

    private boolean checkHasLowStock(List<MenuItem> list) {
        int flag = 0;
        if (list != null) {
            for (MenuItem mi : list) {
                if (mi.getQuantityInStock() > 0) {
                    if (mi.getQuantityInStock() * 100 / mi.getInitialQuantity() < 20) {
                        flag++;
                    }
                }
            }
        }
        if (flag > 0) {
            return true;
        }
        return false;
    }

    private boolean checkConfigYet(List<MenuItem> list) {
        if (list != null && !list.isEmpty()) {
            for (MenuItem item : list) {
                if (item.getInitialQuantity() > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkEmpty(String data) {
        return (data != null && !data.trim().isEmpty());
    }

    private String checkQuantity(String quantity) {
        String msg = "";
        try {
            int qty = Integer.parseInt(quantity);
            if (qty < 0) {
                msg = "Số lượng món ăn không được phép là số âm!";
            }
        } catch (Exception e) {
            msg = "Số lượng nhập vào phải là ký tự số hợp lệ!";
        }
        return msg;
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
