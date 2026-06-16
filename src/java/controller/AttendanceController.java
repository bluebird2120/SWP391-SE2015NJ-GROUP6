package controller;

import dal.EmployeeShiftDAO;
import dal.ShiftRow;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import model.EmployeeShifts;

/**
 * Owner-driven attendance.
 * Today-only rule: chỉ cho check-in/out/absent/reset khi shift.workDate = hôm nay.
 * Date-picker cho phép xem lịch sử nhưng không edit.
 */
@WebServlet(name = "AttendanceController", urlPatterns = {"/owner/attendance"})
public class AttendanceController extends HttpServlet {

    private static final String VIEW = "/views/owner/attendance.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        showAttendance(req, resp, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) action = "";
        switch (action) {
            case "checkin":  handle(req, resp, "checkin"); break;
            case "checkout": handle(req, resp, "checkout"); break;
            case "absent":   handle(req, resp, "absent"); break;
            case "reset":    handle(req, resp, "reset"); break;
            default: resp.sendRedirect(req.getContextPath() + "/owner/attendance");
        }
    }

    private void showAttendance(HttpServletRequest req, HttpServletResponse resp, String error, String success)
            throws ServletException, IOException {
        LocalDate date = parseDateOrToday(req.getParameter("date"));
        Date sqlDate = Date.valueOf(date);

        EmployeeShiftDAO dao = new EmployeeShiftDAO();
        List<ShiftRow> rows = dao.listByDate(sqlDate);

        req.setAttribute("date", date.toString());
        req.setAttribute("today", LocalDate.now().toString());
        req.setAttribute("isToday", date.equals(LocalDate.now()));
        req.setAttribute("rows", rows);
        if (error != null) req.setAttribute("error", error);
        if (success != null) req.setAttribute("success", success);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp, String action)
            throws ServletException, IOException {
        int shiftID = parseInt(req.getParameter("shiftID"), 0);
        LocalDate date = parseDateOrToday(req.getParameter("date"));
        if (shiftID <= 0) {
            showAttendance(req, resp, "shiftID không hợp lệ.", null);
            return;
        }

        EmployeeShiftDAO dao = new EmployeeShiftDAO();
        // Layer 1 — controller check
        EmployeeShifts s = dao.findById(shiftID);
        if (s == null) { showAttendance(req, resp, "Không tìm thấy ca.", null); return; }
        Date today = Date.valueOf(LocalDate.now());
        if (!s.getWorkDate().toString().equals(today.toString())) {
            showAttendance(req, resp, "Chỉ được sửa điểm danh của ca trong ngày hôm nay.", null);
            return;
        }

        // Layer 2 — DAO update với WHERE workDate = CURRENT_DATE
        boolean ok;
        String successMsg;
        switch (action) {
            case "checkin":
                ok = dao.checkIn(shiftID, new Timestamp(System.currentTimeMillis()));
                successMsg = "checkedin";
                break;
            case "checkout":
                ok = dao.checkOut(shiftID, new Timestamp(System.currentTimeMillis()));
                successMsg = "checkedout";
                break;
            case "absent":
                ok = dao.markAbsent(shiftID);
                successMsg = "absent";
                break;
            case "reset":
                ok = dao.reset(shiftID);
                successMsg = "reset";
                break;
            default:
                showAttendance(req, resp, "Action không hợp lệ.", null);
                return;
        }

        if (!ok) {
            showAttendance(req, resp, "Ca đã được xử lý trước đó hoặc không hợp lệ để " + action + ".", null);
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/owner/attendance?date=" + date + "&msg=" + successMsg);
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