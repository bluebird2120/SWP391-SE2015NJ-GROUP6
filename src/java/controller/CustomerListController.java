package controller;

import dal.CustomerDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import model.Customer;

@WebServlet(name = "CustomerListController", urlPatterns = {"/owner/customer-list"})
public class CustomerListController extends HttpServlet {

    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("employee") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String search = request.getParameter("search");
        String loginProvider = request.getParameter("loginProvider");

        if (loginProvider == null || loginProvider.trim().isEmpty()) {
            loginProvider = "all";
        }

        int page = parseInt(request.getParameter("page"), 1);
        int pageSize = 10;

        try {
            int totalRows = customerDAO.countCustomerList(search, loginProvider);
            int totalPages = (int) Math.ceil((double) totalRows / pageSize);

            if (totalPages == 0) {
                totalPages = 1;
            }

            if (page < 1) {
                page = 1;
            }

            if (page > totalPages) {
                page = totalPages;
            }

            List<Customer> customers = customerDAO.getCustomerList(search, loginProvider, page, pageSize);

            request.setAttribute("customers", customers);
            request.setAttribute("search", search);
            request.setAttribute("loginProvider", loginProvider);
            request.setAttribute("page", page);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("totalRows", totalRows);
            request.setAttribute("totalPages", totalPages);

            request.getRequestDispatcher("/views/owner/customer-list.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Không thể tải danh sách khách hàng.");
            request.getRequestDispatcher("/views/error.jsp").forward(request, response);
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
