package controller;

import dal.EmployeeDAO;
import dal.EmployeeShiftDAO;
import dal.ShiftRow;
import dal.ShiftTemplateDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import model.Employee;
import model.ShiftTemplates;

@WebServlet(name = "ShiftAssignmentsController", urlPatterns = {"/owner/shift-assignments"})
public class ShiftAssignmentsController extends HttpServlet {

    private static final String VIEW = "/views/owner/shift-assignments.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        showRoster(req, resp, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) action = "";
        switch (action) {
            case "assign":   handleAssign(req, resp); break;
            case "unassign": handleUnassign(req, resp); break;
            default: resp.sendRedirect(req.getContextPath() + "/owner/shift-assignments");
        }
    }

    private void showRoster(HttpServletRequest req, HttpServletResponse resp, String error, String success)
            throws ServletException, IOException {
        LocalDate date = parseDateOrToday(req.getParameter("date"));
        Date sqlDate = Date.valueOf(date);

        EmployeeDAO empDao = new EmployeeDAO();
        ShiftTemplateDAO tplDao = new ShiftTemplateDAO();
        EmployeeShiftDAO shiftDao = new EmployeeShiftDAO();

        List<Employee> staff = empDao.listActiveStaff();
        List<ShiftTemplates> templates = tplDao.findAll();
        List<ShiftRow> roster = shiftDao.listByDate(sqlDate);

        req.setAttribute("date", date.toString());
        req.setAttribute("today", LocalDate.now().toString());
        req.setAttribute("staffList", staff);
        req.setAttribute("templates", templates);
        req.setAttribute("roster", roster);
        if (error != null) req.setAttribute("error", error);
        if (success != null) req.setAttribute("success", success);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private void handleAssign(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int employeeID = parseInt(req.getParameter("employeeID"), 0);
        int templateID = parseInt(req.getParameter("templateID"), 0);
        LocalDate date = parseDateOrToday(req.getParameter("date"));
        Date sqlDate = Date.valueOf(date);

        if (employeeID <= 0 || templateID <= 0) {
            showRoster(req, resp, "Vui lòng chọn nhân viên và ca.", null);
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            showRoster(req, resp, "Không thể gán ca cho ngày quá khứ.", null);
            return;
        }

        EmployeeShiftDAO dao = new EmployeeShiftDAO();
        if (dao.hasOverlap(employeeID, sqlDate, templateID)) {
            showRoster(req, resp, "Nhân viên đã có ca chồng giờ trong ngày này.", null);
            return;
        }

        int shiftID = dao.assign(employeeID, templateID, sqlDate);
        if (shiftID < 0) {
            showRoster(req, resp, "Không thể gán ca. Vui lòng thử lại.", null);
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/owner/shift-assignments?date=" + date + "&msg=assigned");
    }

    private void handleUnassign(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int shiftID = parseInt(req.getParameter("shiftID"), 0);
        LocalDate date = parseDateOrToday(req.getParameter("date"));
        if (shiftID <= 0) {
            showRoster(req, resp, "shiftID không hợp lệ.", null);
            return;
        }
        EmployeeShiftDAO dao = new EmployeeShiftDAO();
        boolean ok = dao.unassign(shiftID);
        String msg = ok ? "unassigned" : "unassign_failed";
        resp.sendRedirect(req.getContextPath() + "/owner/shift-assignments?date=" + date + "&msg=" + msg);
    }

    private static LocalDate parseDateOrToday(String s) {
        if (s == null || s.isBlank()) return LocalDate.now();
        try { return LocalDate.parse(s); } catch (Exception e) { return LocalDate.now(); }
    }

    private static int parseInt(String s, int def) {
        if (s == null || s.isBlank()) return def;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
