package controller;

import dal.DBContext;
import dal.StaffTableDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import model.Employee;

@WebServlet(name = "ReceptionTableController", urlPatterns = {"/reception/tables"})
public class ReceptionTableController extends HttpServlet {

    private static final String VIEW = "/views/reception/table-dashboard.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Employee employee = getLoggedInEmployee(request);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/login?type=employee");
            return;
        }

        // [PHAN QUYEN LE TAN] Le tan xem tong quan va cac don cho gan ban.
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

        String message;
        try {
            int orderID = Integer.parseInt(request.getParameter("orderID"));
            String action = request.getParameter("action");
            if ("assign".equals(action)) {
                int tableID = Integer.parseInt(request.getParameter("tableID"));
                // [PHAN QUYEN LE TAN] Khong truyen ID le tan vao Order.
                String error = new StaffTableDAO().assignTable(orderID, tableID);
                message = error == null ? "assign_success" : error;
            } else if ("checkin".equals(action) || "open_table".equals(action)) {
                message = openTable(orderID)
                        ? ("checkin".equals(action)
                                ? "checkin_success" : "open_table_success")
                        : "Khong the mo ban cho don nay.";
            } else {
                message = "Thao tac khong hop le.";
            }
        } catch (NumberFormatException e) {
            message = "Ma don hoac ma ban khong hop le.";
        }

        request.getSession().setAttribute("staffTableMessage", message);
        response.sendRedirect(request.getContextPath() + "/reception/tables");
    }

    private boolean openTable(int orderID) {
        String sql = "UPDATE `Order` SET tableStatus='occupied' WHERE orderID=?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Employee getLoggedInEmployee(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null
                : (Employee) session.getAttribute("employee");
    }
}
