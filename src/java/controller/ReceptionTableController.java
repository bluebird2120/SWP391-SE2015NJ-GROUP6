package controller;

import dal.EmployeeShiftDAO;
import dal.StaffTableDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.Employee;

@WebServlet(name = "ReceptionTableController", urlPatterns = {"/reception/tables"})
public class ReceptionTableController extends HttpServlet {

    private static final String VIEW = "/views/reception/table-dashboard.jsp";
    private static final int OWNER_ROLE_ID = 1;
    private static final int RECEPTIONIST_ROLE_ID = 3;

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
            if (!canOperateReceptionTable(employee)) {
                request.getSession().setAttribute("staffTableMessage",
                        "Ban chua co lich lam hom nay nen khong the thao tac van hanh ban.");
                response.sendRedirect(request.getContextPath() + "/reception/tables");
                return;
            }
            if ("assign".equals(action)) {
                int tableID = Integer.parseInt(request.getParameter("tableID"));
                // [PHAN QUYEN LE TAN] Khong truyen ID le tan vao Order.
                String error = new StaffTableDAO().assignTable(orderID, tableID);
                message = error == null ? "assign_success" : error;
            } else if ("checkin".equals(action) || "open_table".equals(action)) {
                StaffTableDAO dao = new StaffTableDAO();
                boolean ok = "checkin".equals(action)
                        ? dao.checkinArrivedReservation(orderID)
                        : dao.openTableForWalkIn(orderID);
                message = ok
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

    private Employee getLoggedInEmployee(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null
                : (Employee) session.getAttribute("employee");
    }

    private boolean canOperateReceptionTable(Employee employee) {
        if (employee.getRoleID() == OWNER_ROLE_ID) {
            return true;
        }
        if (employee.getRoleID() == RECEPTIONIST_ROLE_ID) {
            // [LICH LAM LE TAN] Le tan chi duoc tiep nhan/gan ban khi co ca lam hom nay.
            return new EmployeeShiftDAO().isEmployeeOnShift(employee.getEmployeeID());
        }
        return false;
    }
}
