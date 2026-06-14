package controller;

import dal.EmployeeDAO;
import dal.EmployeeShiftDAO;
import dal.MonthlyShiftPlanDAO;
import dal.NotificationDAO;
import dal.ShiftRow;
import dal.ShiftTemplateDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import model.Employee;
import model.MonthlyShiftPlan;
import model.Notifications;
import model.ShiftTemplates;

@WebServlet(name = "ShiftRosterController", urlPatterns = {"/owner/shift-roster"})
public class ShiftRosterController extends HttpServlet {

    private static final String VIEW = "/views/owner/shift-roster.jsp";

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
            case "assign":      handleAssign(req, resp); break;
            case "unassign":    handleUnassign(req, resp); break;
            case "assignMonth": handleAssignMonth(req, resp); break;
            case "cancelPlan":  handleCancelPlan(req, resp); break;
            default: resp.sendRedirect(req.getContextPath() + "/owner/shift-roster");
        }
    }

    /* ==================== VIEW ==================== */

    private void showRoster(HttpServletRequest req, HttpServletResponse resp, String error, String success)
            throws ServletException, IOException {
        LocalDate date = parseDateOrToday(req.getParameter("date"));
        Date sqlDate = Date.valueOf(date);

        // Tháng đang xem cho bảng plan; mặc định = tháng kế tiếp (vì plan chỉ áp dụng cho tháng tương lai)
        YearMonth viewYm = parseYearMonth(req.getParameter("planYear"), req.getParameter("planMonth"),
                YearMonth.now().plusMonths(1));

        EmployeeDAO empDao = new EmployeeDAO();
        ShiftTemplateDAO tplDao = new ShiftTemplateDAO();
        EmployeeShiftDAO shiftDao = new EmployeeShiftDAO();
        MonthlyShiftPlanDAO planDao = new MonthlyShiftPlanDAO();

        List<Employee> staff = empDao.listActiveStaff();
        List<ShiftTemplates> templates = tplDao.findAll();
        List<ShiftRow> roster = shiftDao.listByDate(sqlDate);
        List<MonthlyShiftPlan> monthlyPlans = planDao.listByMonth(viewYm.getYear(), viewYm.getMonthValue());

        req.setAttribute("date", date.toString());
        req.setAttribute("today", LocalDate.now().toString());
        req.setAttribute("staffList", staff);
        req.setAttribute("templates", templates);
        req.setAttribute("roster", roster);
        req.setAttribute("monthlyPlans", monthlyPlans);
        req.setAttribute("planYear", viewYm.getYear());
        req.setAttribute("planMonth", viewYm.getMonthValue());
        req.setAttribute("currentYear", LocalDate.now().getYear());
        req.setAttribute("currentMonth", LocalDate.now().getMonthValue());
        if (error != null) req.setAttribute("error", error);
        if (success != null) req.setAttribute("success", success);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    /* ==================== ASSIGN 1 NGÀY (giữ nguyên) ==================== */

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
        resp.sendRedirect(req.getContextPath() + "/owner/shift-roster?date=" + date + "&msg=assigned");
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
        resp.sendRedirect(req.getContextPath() + "/owner/shift-roster?date=" + date + "&msg=" + msg);
    }

    /* ==================== PHÂN CA THEO THÁNG ==================== */

    /**
     * Owner submit (employeeID, templateID, year, month).
     *  - Tháng hiện tại / quá khứ → batch insert thẳng vào EmployeeShifts (nếu chưa có ca nào).
     *  - Tháng kế tiếp → upsert vào MonthlyShiftPlan (DRAFT). Nếu đã sát hạn (today >= notifyDate) → notify ngay.
     */
    private void handleAssignMonth(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int employeeID = parseInt(req.getParameter("employeeID"), 0);
        int templateID = parseInt(req.getParameter("templateID"), 0);
        int year       = parseInt(req.getParameter("year"), 0);
        int month      = parseInt(req.getParameter("month"), 0);

        if (employeeID <= 0 || templateID <= 0 || month < 1 || month > 12 || year < 2024) {
            showRoster(req, resp, "Tham số phân ca tháng không hợp lệ.", null);
            return;
        }

        YearMonth target = YearMonth.of(year, month);
        YearMonth nowYm  = YearMonth.now();

        Integer ownerId = currentEmployeeID(req);
        if (ownerId == null) {
            showRoster(req, resp, "Phiên đăng nhập không hợp lệ.", null);
            return;
        }

        if (!target.isAfter(nowYm)) {
            // Tháng hiện tại hoặc quá khứ → áp dụng ngay, không ghi đè lịch đã có.
            EmployeeShiftDAO shiftDao = new EmployeeShiftDAO();
            if (shiftDao.hasAnyShiftInMonth(employeeID, year, month)) {
                showRoster(req, resp,
                        "Nhân viên đã có ca trong tháng " + month + "/" + year + ". Hãy huỷ ca cũ trước khi phân ca theo tháng.",
                        null);
                return;
            }
            int rows = shiftDao.assignMonth(employeeID, templateID, year, month);
            if (rows <= 0) {
                showRoster(req, resp, "Không thể phân ca theo tháng. Vui lòng thử lại.", null);
                return;
            }
            redirectToPlanMonth(req, resp, year, month, "month_assigned");
            return;
        }

        if (target.isAfter(nowYm.plusMonths(1))) {
            showRoster(req, resp, "Chỉ được lập kế hoạch ca cho tháng kế tiếp.", null);
            return;
        }

        // Tháng kế tiếp → lưu plan
        MonthlyShiftPlanDAO planDao = new MonthlyShiftPlanDAO();
        int planID = planDao.saveOrUpdate(employeeID, templateID, year, month, ownerId);
        if (planID == -2) {
            showRoster(req, resp, "Kế hoạch ca tháng này đã được áp dụng, không thể chỉnh sửa.", null);
            return;
        }
        if (planID < 0) {
            showRoster(req, resp, "Không thể lưu kế hoạch ca tháng.", null);
            return;
        }

        // Nếu đã sát hạn → notify ngay (scheduler có thể chưa kịp chạy).
        // Chỉ notify khi plan đang DRAFT để tránh gửi trùng cho plan đã NOTIFIED.
        LocalDate today = LocalDate.now();
        LocalDate notifyDate = LocalDate.of(year, month, 1).minusDays(3);
        MonthlyShiftPlan currentPlan = planDao.findByEmployee(employeeID, year, month);
        if (currentPlan != null
                && MonthlyShiftPlan.DRAFT.equals(currentPlan.getStatus())
                && !today.isBefore(notifyDate)) {
            sendShiftNotificationNow(planID, employeeID, templateID, year, month);
            planDao.updateStatus(planID, MonthlyShiftPlan.NOTIFIED);
        }

        redirectToPlanMonth(req, resp, year, month, "plan_saved");
    }

    private void handleCancelPlan(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int planID = parseInt(req.getParameter("planID"), 0);
        int year   = parseInt(req.getParameter("planYear"), LocalDate.now().getYear());
        int month  = parseInt(req.getParameter("planMonth"), LocalDate.now().getMonthValue());
        if (planID <= 0) {
            showRoster(req, resp, "planID không hợp lệ.", null);
            return;
        }
        MonthlyShiftPlanDAO dao = new MonthlyShiftPlanDAO();
        boolean ok = dao.cancelIfPending(planID);
        redirectToPlanMonth(req, resp, year, month, ok ? "plan_cancelled" : "plan_cancel_failed");
    }

    /* ==================== HELPERS ==================== */

    private void sendShiftNotificationNow(int planID, int employeeID, int templateID, int year, int month) {
        ShiftTemplateDAO tplDao = new ShiftTemplateDAO();
        ShiftTemplates tpl = tplDao.findById(templateID);
        String shiftName = tpl == null ? "?" : tpl.getShiftName();
        String startStr  = tpl == null || tpl.getStartTime() == null ? "?" : tpl.getStartTime().toString();
        String endStr    = tpl == null || tpl.getEndTime()   == null ? "?" : tpl.getEndTime().toString();

        Notifications n = new Notifications();
        n.setRecipientID(employeeID);
        n.setRecipientType("staff");
        n.setType("shift_plan");
        n.setMessage(String.format(
                "Lịch ca tháng %02d/%d của bạn: ca %s (%s - %s), bắt đầu ngày 01/%02d/%d.",
                month, year, shiftName, startStr, endStr, month, year));
        n.setIsRead(0);
        new NotificationDAO().insert(n);
    }

    private void redirectToPlanMonth(HttpServletRequest req, HttpServletResponse resp,
                                     int year, int month, String msg) throws IOException {
        resp.sendRedirect(req.getContextPath()
                + "/owner/shift-roster?planYear=" + year + "&planMonth=" + month + "&msg=" + msg);
    }

    private static Integer currentEmployeeID(HttpServletRequest req) {
        HttpSession ss = req.getSession(false);
        if (ss == null) return null;
        Object v = ss.getAttribute("employeeID");
        if (v instanceof Integer) return (Integer) v;
        Object emp = ss.getAttribute("employee");
        if (emp instanceof Employee) return ((Employee) emp).getEmployeeID();
        return null;
    }

    private static LocalDate parseDateOrToday(String s) {
        if (s == null || s.isBlank()) return LocalDate.now();
        try { return LocalDate.parse(s); } catch (Exception e) { return LocalDate.now(); }
    }

    private static YearMonth parseYearMonth(String yStr, String mStr, YearMonth defVal) {
        try {
            if (yStr == null || mStr == null) return defVal;
            int y = Integer.parseInt(yStr);
            int m = Integer.parseInt(mStr);
            return YearMonth.of(y, m);
        } catch (Exception e) {
            return defVal;
        }
    }

    private static int parseInt(String s, int def) {
        if (s == null || s.isBlank()) return def;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
