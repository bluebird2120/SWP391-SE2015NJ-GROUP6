package controller;

import dal.EmployeeShiftDAO;
import dal.ShiftRow;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Employee;


/**
 * Staff xem lịch ca của bản thân theo tháng (calendar view).
 * URL: /staff/my-schedule?year=YYYY&month=M (default = tháng hiện tại).
 *
 * Backend build sẵn scheduleMap[day -> list<ShiftRow>] để JSP render lưới 6x7.
 * AuthFilter cho phép cả OWNER và STAFF — nếu owner truy cập sẽ thấy lịch của chính họ.
 */
@WebServlet(name = "StaffMyScheduleController", urlPatterns = {"/staff/my-schedule"})
public class StaffMyScheduleController extends HttpServlet {

    private static final String VIEW = "/views/staff/my-schedule.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee emp = session == null ? null : (Employee) session.getAttribute("employee");
        if (emp == null) {
            resp.sendRedirect(req.getContextPath() + "/login?type=employee");
            return;
        }

        YearMonth ym = parseYearMonth(req.getParameter("year"), req.getParameter("month"),
                                      YearMonth.now());
        int year  = ym.getYear();
        int month = ym.getMonthValue();

        List<ShiftRow> rows = new EmployeeShiftDAO()
                .listByEmployeeAndMonth(emp.getEmployeeID(), year, month);

        Map<String, List<ShiftRow>> byDay = new LinkedHashMap<>();
        for (ShiftRow r : rows) {
            String day = String.valueOf(r.getWorkDate().toLocalDate().getDayOfMonth());
            byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(r);
        }

        LocalDate firstDay = LocalDate.of(year, month, 1);
        // getDayOfWeek().getValue(): Mon=1..Sun=7. Map sang Sun=0..Sat=6 cho lưới CN..T7.
        int firstDow = firstDay.getDayOfWeek().getValue() % 7;
        int daysInMonth = ym.lengthOfMonth();

        YearMonth prev = ym.minusMonths(1);
        YearMonth next = ym.plusMonths(1);

        req.setAttribute("scheduleMap", byDay);
        req.setAttribute("year", year);
        req.setAttribute("month", month);
        req.setAttribute("daysInMonth", daysInMonth);
        req.setAttribute("firstDow", firstDow);
        req.setAttribute("today", LocalDate.now().toString());
        req.setAttribute("currentYear", LocalDate.now().getYear());
        req.setAttribute("currentMonth", LocalDate.now().getMonthValue());
        req.setAttribute("currentDay", LocalDate.now().getDayOfMonth());
        req.setAttribute("prevYear",  prev.getYear());
        req.setAttribute("prevMonth", prev.getMonthValue());
        req.setAttribute("nextYear",  next.getYear());
        req.setAttribute("nextMonth", next.getMonthValue());
        req.setAttribute("totalShifts", rows.size());

        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private static YearMonth parseYearMonth(String yStr, String mStr, YearMonth def) {
        try {
            if (yStr == null || mStr == null) return def;
            int y = Integer.parseInt(yStr);
            int m = Integer.parseInt(mStr);
            return YearMonth.of(y, m);
        } catch (Exception e) {
            return def;
        }
    }
}
