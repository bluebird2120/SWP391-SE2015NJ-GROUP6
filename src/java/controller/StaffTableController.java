package controller;

import dal.StaffTableDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.Employee;

@WebServlet(name = "StaffTableController", urlPatterns = {"/staff/tables"})
public class StaffTableController extends HttpServlet {

    private static final String VIEW = "/views/staff/my-tables.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Employee employee = getLoggedInEmployee(request);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/login?type=employee");
            return;
        }

        // [PHAN QUYEN PHUC VU] Staff chi nhin thay don va ban cua chinh minh.
        request.setAttribute("assignedTables",
                new StaffTableDAO().getTablesForEmployee(employee.getEmployeeID()));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Employee employee = getLoggedInEmployee(request);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/login?type=employee");
            return;
        }

        String message = "Thao tac khong hop le.";
        try {
            int orderID = Integer.parseInt(request.getParameter("orderID"));
            if ("cleaned".equals(request.getParameter("action"))) {
                // [PHAN QUYEN PHUC VU] DAO kiem tra order phai thuoc staff nay.
                message = new StaffTableDAO().markCleaningCompleted(
                        orderID, employee.getEmployeeID())
                        ? "clean_success" : "Khong the hoan tat don ban nay.";
            }
        } catch (NumberFormatException e) {
            message = "Ma don khong hop le.";
        }

        request.getSession().setAttribute("staffTableMessage", message);
        response.sendRedirect(request.getContextPath() + "/staff/tables");
    }

    private Employee getLoggedInEmployee(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null
                : (Employee) session.getAttribute("employee");
    }
}
