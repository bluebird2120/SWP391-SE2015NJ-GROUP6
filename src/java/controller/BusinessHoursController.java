package controller;

import dal.BusinessScheduleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import model.Employee;

@WebServlet(name = "BusinessHoursController", urlPatterns = {"/owner/business-hours"})
public class BusinessHoursController extends HttpServlet {

    private static final String VIEW = "/views/owner/business-hours.jsp";
    private static final int OWNER_ROLE_ID = 1;
    private final BusinessScheduleDAO dao = new BusinessScheduleDAO();
    private final Set<String> validDays = new HashSet<>(
            Arrays.asList(BusinessScheduleDAO.WEEK_DAYS));

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Employee employee = getEmployee(request);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=required");
            return;
        }

        loadPageData(request, employee);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Employee employee = getEmployee(request);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=required");
            return;
        }

        // [OPERATING HOURS] Staff chi duoc xem, Owner moi duoc cap nhat gio hoat dong.
        if (employee.getRoleID() != OWNER_ROLE_ID) {
            response.sendRedirect(request.getContextPath()
                    + "/owner/business-hours?action=list&msg=forbidden");
            return;
        }

        String action = request.getParameter("action");
        String message = "failed";
        if ("saveWeekly".equals(action)) {
            message = saveWeekly(request) ? "saved" : "failed";
        } else if ("saveSpecial".equals(action)) {
            message = saveSpecial(request) ? "saved" : "failed";
        } else if ("deleteSpecial".equals(action)) {
            int scheduleID = toInt(request.getParameter("scheduleID"), -1);
            message = scheduleID > 0 && dao.deleteSpecialDate(scheduleID)
                    ? "deleted" : "failed";
        }

        response.sendRedirect(request.getContextPath()
                + "/owner/business-hours?action=list&msg=" + message);
    }

    private boolean saveWeekly(HttpServletRequest request) {
        String dayOfWeek = request.getParameter("dayOfWeek");
        if (dayOfWeek == null) {
            return false;
        }
        dayOfWeek = dayOfWeek.trim().toUpperCase();
        if (!validDays.contains(dayOfWeek)) {
            return false;
        }

        int isClosed = "1".equals(request.getParameter("isClosed")) ? 1 : 0;
        Time openTime = parseTime(request.getParameter("openTime"));
        Time closeTime = parseTime(request.getParameter("closeTime"));
        if (isClosed == 0 && !isValidTimeRange(openTime, closeTime)) {
            return false;
        }

        return dao.saveWeeklySchedule(dayOfWeek, openTime, closeTime,
                isClosed, trimToNull(request.getParameter("reason")));
    }

    private boolean saveSpecial(HttpServletRequest request) {
        Date specificDate = parseDate(request.getParameter("specificDate"));
        if (specificDate == null) {
            return false;
        }

        int isClosed = "1".equals(request.getParameter("isClosed")) ? 1 : 0;
        Time openTime = parseTime(request.getParameter("openTime"));
        Time closeTime = parseTime(request.getParameter("closeTime"));
        if (isClosed == 0 && !isValidTimeRange(openTime, closeTime)) {
            return false;
        }

        return dao.saveSpecialDate(specificDate, openTime, closeTime,
                isClosed, trimToNull(request.getParameter("reason")));
    }

    private void loadPageData(HttpServletRequest request, Employee employee) {
        request.setAttribute("weeklySchedules", dao.getWeeklyScheduleMap());
        request.setAttribute("specialSchedules", dao.getSpecialDates());
        request.setAttribute("isOwner", employee.getRoleID() == OWNER_ROLE_ID);
        request.setAttribute("pageMsg", request.getParameter("msg"));
    }

    private Employee getEmployee(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Employee) session.getAttribute("employee");
    }

    private Time parseTime(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Time.valueOf(value + ":00");
        } catch (Exception e) {
            return null;
        }
    }

    private Date parseDate(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Date.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isValidTimeRange(Time openTime, Time closeTime) {
        return openTime != null && closeTime != null
                && openTime.before(closeTime);
    }

    private int toInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}