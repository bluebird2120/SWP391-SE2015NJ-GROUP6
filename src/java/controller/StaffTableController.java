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

    private static final String VIEW = "/views/staff/table-dashboard.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Employee employee = getLoggedInEmployee(request);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/login?type=employee");
            return;
        }

        StaffTableDAO dao = new StaffTableDAO();
        request.setAttribute("physicalTables", dao.getPhysicalTables());
        request.setAttribute("tableSummary", dao.getSummaryByTableType());
        request.setAttribute("reservationRequirements",
                dao.getReservationsWaitingForTables());
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

        StaffTableDAO dao = new StaffTableDAO();
        String message;
        try {
            int orderID = Integer.parseInt(request.getParameter("orderID"));
            String action = request.getParameter("action");
            if ("assign".equals(action)) {
                int tableID = Integer.parseInt(request.getParameter("tableID"));
                String error = dao.assignTable(orderID, tableID,
                        employee.getEmployeeID());
                message = error == null ? "assign_success" : error;
            } else if ("cleaned".equals(action)) {
                // [STAFF TABLE] Chi xu ly don da cleaning, khong sua thanh toan.
                message = dao.markCleaningCompleted(orderID)
                        ? "clean_success" : "Không thể hoàn tất dọn bàn.";
            } else {
                message = "Thao tác không hợp lệ.";
            }
        } catch (NumberFormatException e) {
            message = "Mã đơn hoặc mã bàn không hợp lệ.";
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
