package controller;

import dal.NotificationDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Customer;
import model.Employee;
import java.io.IOException;

@WebServlet("/api/unread-count")
public class UnreadCountController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        int count = 0;

        if (session != null) {
            Customer customer = (Customer) session.getAttribute("customer");
            Employee employee = (Employee) session.getAttribute("employee");
            NotificationDAO dao = new NotificationDAO();

            if (customer != null) {
                count = dao.countUnread(customer.getCustomerID(), "customer");
            } else if (employee != null) {
                count = dao.countUnread(employee.getEmployeeID(), "staff");
            }
        }

        response.getWriter().write("{\"unread\":" + count + "}");
    }
}