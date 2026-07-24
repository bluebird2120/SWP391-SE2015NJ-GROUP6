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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import model.Employee;
import model.MonthlyShiftPlan;
import model.Notifications;
import model.ShiftTemplates;
import dal.ShiftSwapRequestDAO;
import model.ShiftSwapRequestDetail;

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
        if (action == null) {
            action = "";
        }
        switch (action) {
            case "assign":
                handleAssign(req, resp);
                break;
            case "unassign":
                handleUnassign(req, resp);
                break;
            case "assignMonth":
                handleAssignMonth(req, resp);
                break;
            case "cancelPlan":
                handleCancelPlan(req, resp);
                break;
            case "approveRequest":
                handleApproveRequest(req, resp);
                break;
            case "rejectRequest":
                handleRejectRequest(req, resp);
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/owner/shift-roster");
        }
    }

    private void showRoster(HttpServletRequest req, HttpServletResponse resp, String error, String success)
            throws ServletException, IOException {

        LocalDate date = parseDateOrToday(req.getParameter("date"));
        Date sqlDate = Date.valueOf(date);

        YearMonth viewYm = parseYearMonth(firstNonBlank(req.getParameter("planYear"), req.getParameter("year")),
                firstNonBlank(req.getParameter("planMonth"), req.getParameter("month")),
                YearMonth.now());

        try (EmployeeDAO empDao = new EmployeeDAO();
             ShiftTemplateDAO tplDao = new ShiftTemplateDAO();
             EmployeeShiftDAO shiftDao = new EmployeeShiftDAO();
             MonthlyShiftPlanDAO planDao = new MonthlyShiftPlanDAO();
             ShiftSwapRequestDAO requestDAO = new ShiftSwapRequestDAO()) {

            if (empDao.getConnection() == null || tplDao.getConnection() == null
                    || shiftDao.getConnection() == null || planDao.getConnection() == null
                    || requestDAO.getConnection() == null) {
                req.setAttribute("date", date.toString());
                req.setAttribute("today", LocalDate.now().toString());
                req.setAttribute("staffList", Collections.emptyList());
                req.setAttribute("templates", Collections.emptyList());
                req.setAttribute("roster", Collections.emptyList());
                req.setAttribute("monthlyPlans", Collections.emptyList());
                req.setAttribute("planYear", viewYm.getYear());
                req.setAttribute("planMonth", viewYm.getMonthValue());
                req.setAttribute("currentYear", LocalDate.now().getYear());
                req.setAttribute("currentMonth", LocalDate.now().getMonthValue());
                req.setAttribute("pendingRequests", Collections.emptyList());
                req.setAttribute("viewEmployeeID", parseInt(req.getParameter("viewEmployeeID"), 0));
                req.setAttribute("viewYear", parseInt(req.getParameter("viewYear"), LocalDate.now().getYear()));
                req.setAttribute("viewMonth", parseInt(req.getParameter("viewMonth"), LocalDate.now().getMonthValue()));
                req.setAttribute("error", "Không thể kết nối database. Vui lòng kiểm tra MySQL, tên database, tài khoản và mật khẩu trong DBContext.");
                req.getRequestDispatcher(VIEW).forward(req, resp);
                return;
            }

            List<Employee> staff = empDao.listActiveStaff();
            List<ShiftTemplates> templates = tplDao.findAll();

            List<ShiftRow> roster = shiftDao.listByDate(sqlDate);

            List<MonthlyShiftPlan> monthlyPlans = planDao.listByMonth(viewYm.getYear(), viewYm.getMonthValue());

            List<ShiftSwapRequestDetail> pendingRequests = requestDAO.listPendingRequests();

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
            req.setAttribute("pendingRequests", pendingRequests);

            int viewEmpID = parseInt(req.getParameter("viewEmployeeID"), 0);
            String viewYearParam = req.getParameter("viewYear");
            String viewMonthParam = req.getParameter("viewMonth");
            int viewYear = parseInt(req.getParameter("viewYear"), LocalDate.now().getYear());
            int viewMonth = parseInt(req.getParameter("viewMonth"), LocalDate.now().getMonthValue());
            String activeTab = req.getParameter("activeTab");
            String viewError = null;
            if ("view".equals(activeTab) && viewEmpID <= 0) {
                viewError = "Vui lòng chọn nhân viên cần xem lịch.";
            } else if ("view".equals(activeTab)
                    && (viewYearParam == null || viewYearParam.isBlank() || !isInteger(viewYearParam) || viewYear < 2024)) {
                viewError = "Vui lòng chọn năm xem lịch từ 2024 trở đi.";
            } else if ("view".equals(activeTab)
                    && (viewMonthParam == null || viewMonthParam.isBlank() || !isInteger(viewMonthParam)
                    || viewMonth < 1 || viewMonth > 12)) {
                viewError = "Vui lòng chọn tháng xem lịch hợp lệ.";
            }
            req.setAttribute("viewEmployeeID", viewEmpID);
            req.setAttribute("viewYear", viewYear);
            req.setAttribute("viewMonth", viewMonth);
            req.setAttribute("viewError", viewError);
            if (viewEmpID > 0 && viewError == null) {
                List<ShiftRow> staffSchedule = shiftDao.listByEmployeeAndMonth(viewEmpID, viewYear, viewMonth);
                req.setAttribute("staffSchedule", staffSchedule);
                for (Employee e : staff) {
                    if (e.getEmployeeID() == viewEmpID) {
                        req.setAttribute("viewEmployeeName", e.getFullName());
                        break;
                    }
                }
            }

            if (error != null) {
                req.setAttribute("error", error);
            }
            if (success != null) {
                req.setAttribute("success", success);
            }
            applyFlashMessages(req);
            req.getRequestDispatcher(VIEW).forward(req, resp);
        }
    }

    private void handleAssign(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String[] empIDsStr = req.getParameterValues("employeeIDs");

        int templateID = parseInt(req.getParameter("templateID"), 0);

        LocalDate date = parseDateOrToday(req.getParameter("date"));

        LocalDate toDate = parseDateOrDefault(req.getParameter("toDate"), date);

        if (empIDsStr == null || empIDsStr.length == 0 || templateID <= 0) {
            showRoster(req, resp, "Vui lòng chọn ít nhất một nhân viên và ca.", null);
            return;
        }

        if (date.isBefore(LocalDate.now())) {
            showRoster(req, resp, "Không thể gán ca cho ngày quá khứ.", null);
            return;
        }

        if (toDate.isBefore(date)) {
            showRoster(req, resp, "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.", null);
            return;
        }
        YearMonth startYm = YearMonth.from(date);
        YearMonth endYm = YearMonth.from(toDate);

        if (!startYm.equals(endYm)) {
            showRoster(req, resp, "Khoảng ngày phân ca phải nằm trong cùng một tháng.", null);
            return;
        }

        YearMonth nowYm = YearMonth.now();

        if (startYm.isBefore(nowYm) || endYm.isAfter(nowYm.plusMonths(1))) {
            showRoster(req, resp, "Chỉ được phân ca cho tháng hiện tại hoặc tháng kế tiếp.", null);
            return;
        }

        int successCount = 0;

        int overlapCount = 0;

        int failedCount = 0;

        Set<Integer> assignedEmployeeIDs = new LinkedHashSet<>();

        try (EmployeeShiftDAO dao = new EmployeeShiftDAO();
             ShiftTemplateDAO tplDao = new ShiftTemplateDAO();
             NotificationDAO notifDao = new NotificationDAO()) {

            ShiftTemplates template = tplDao.findById(templateID);
            String shiftLabel = (template != null) ? template.getShiftName() : "ca làm việc";

            for (String empIdStr : empIDsStr) {
                int employeeID = parseInt(empIdStr, 0);
                if (employeeID <= 0) {
                    continue;
                }
                boolean assignedForThisEmployee = false;
                LocalDate current = date;
                while (!current.isAfter(toDate)) {
                    Date sqlDate = Date.valueOf(current);
                    if (dao.hasOverlap(employeeID, sqlDate, templateID)) {
                        overlapCount++;
                        current = current.plusDays(1);
                        continue;
                    }
                    int shiftID = dao.assign(employeeID, templateID, sqlDate);
                    if (shiftID < 0) {
                        failedCount++;
                    } else {
                        successCount++;
                        assignedForThisEmployee = true;
                    }
                    current = current.plusDays(1);
                }
                if (assignedForThisEmployee) {
                    assignedEmployeeIDs.add(employeeID);
                }
            }

            // Gửi thông báo "có lịch làm mới" cho từng nhân viên vừa được gán ít nhất 1 ca
            String dateRangeText = date.equals(toDate)
                    ? "ngày " + date
                    : "từ ngày " + date + " đến ngày " + toDate;
            for (Integer employeeID : assignedEmployeeIDs) {
                Notifications n = new Notifications();
                n.setRecipientID(employeeID);
                n.setRecipientType("staff");
                n.setType("shift_assigned");
                n.setMessage("Bạn có lịch làm mới: " + shiftLabel + " " + dateRangeText + ".");
                n.setIsRead(0);
                notifDao.insert(n);
            }
        }

        String successMsg = null;
        String errorMsg = null;

        if (successCount > 0) {
            successMsg = "Đã gán thành công " + successCount + " ca.";
        }

        if (overlapCount > 0 || failedCount > 0) {
            errorMsg = (overlapCount > 0
                    ? overlapCount + " ca bị bỏ qua vì nhân viên đã có ca trong ngày đó. "
                    : "")
                    + (failedCount > 0 ? failedCount + " ca gán lỗi." : "");
        }

        if (successCount > 0 && overlapCount == 0 && failedCount == 0) {
            resp.sendRedirect(req.getContextPath() + "/owner/shift-roster?date=" + date + "&msg=assigned_multi&cnt="
                    + successCount);
        } else {

            showRoster(req, resp, errorMsg, successMsg);
        }
    }

    private void handleUnassign(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int shiftID = parseInt(req.getParameter("shiftID"), 0);
        LocalDate date = parseDateOrToday(req.getParameter("date"));
        if (shiftID <= 0) {
            showRoster(req, resp, "shiftID không hợp lệ.", null);
            return;
        }
        boolean ok;
        try (EmployeeShiftDAO dao = new EmployeeShiftDAO()) {
            ok = dao.unassign(shiftID);
        }
        String msg = ok ? "unassigned" : "unassign_failed";
        resp.sendRedirect(req.getContextPath() + "/owner/shift-roster?date=" + date + "&msg=" + msg);
    }

    private void handleAssignMonth(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String[] empIDsStr = req.getParameterValues("employeeIDs");

        int templateID = parseInt(req.getParameter("templateID"), 0);

        int year = parseInt(req.getParameter("year"), 0);

        int month = parseInt(req.getParameter("month"), 0);

        String mode = req.getParameter("assignMode");

        if (mode == null) {
            mode = "SKIP_EXISTING";
        }

        if (empIDsStr == null || empIDsStr.length == 0 || templateID <= 0 || month < 1 || month > 12 || year < 2024) {
            showRoster(req, resp, "Tham số phân ca tháng không hợp lệ hoặc chưa chọn nhân viên.", null);
            return;
        }

        if (year < LocalDate.now().getYear() || year > LocalDate.now().getYear() + 1) {
            showRoster(req, resp, "Chỉ được phân ca tháng trong năm hiện tại hoặc năm kế tiếp.", null);
            return;
        }

        YearMonth target = YearMonth.of(year, month);

        YearMonth nowYm = YearMonth.now();

        Integer ownerId = currentEmployeeID(req);

        if (ownerId == null) {
            showRoster(req, resp, "Phiên đăng nhập không hợp lệ.", null);
            return;
        }

        if (target.isBefore(nowYm) || target.isAfter(nowYm.plusMonths(1))) {
            showRoster(req, resp, "Chỉ được phân ca cho tháng hiện tại hoặc tháng kế tiếp.", null);
            return;
        }

        int totalAssignedRows = 0;

        int successEmployees = 0;

        int skippedEmployees = 0;

        int failedEmployees = 0;

        try (EmployeeShiftDAO shiftDao = new EmployeeShiftDAO();
             MonthlyShiftPlanDAO planDao = new MonthlyShiftPlanDAO();
             ShiftTemplateDAO tplDao = new ShiftTemplateDAO();
             NotificationDAO notifDao = new NotificationDAO()) {

            ShiftTemplates template = tplDao.findById(templateID);
            String shiftLabel = (template != null) ? template.getShiftName() : "ca làm việc";

            for (String empIdStr : empIDsStr) {

                int employeeID = parseInt(empIdStr, 0);

                if (employeeID <= 0) {
                    continue;
                }

                int rows;

                if ("REPLACE_ALL".equals(mode)) {

                    rows = shiftDao.replaceMonth(employeeID, templateID, year, month);
                } else {

                    rows = shiftDao.assignMonthSkipExisting(employeeID, templateID, year, month);
                }

                if (rows < 0) {
                    failedEmployees++;

                } else if (rows == 0) {
                    skippedEmployees++;
                } else {

                    successEmployees++;

                    totalAssignedRows += rows;
                    try {

                        int planID = planDao.saveOrUpdate(employeeID, templateID, year, month, ownerId);

                        if (planID > 0) {

                            java.time.YearMonth planYm = java.time.YearMonth.of(year, month);

                            java.time.YearMonth currentYm = java.time.YearMonth.now();

                            if (!planYm.isAfter(currentYm)) {

                                planDao.updateStatus(planID, MonthlyShiftPlan.APPLIED);
                            }
                        }
                    } catch (Exception ignore) {

                    }

                    // Gửi thông báo "có lịch làm mới" cho nhân viên vừa được phân ca cả tháng
                    Notifications n = new Notifications();
                    n.setRecipientID(employeeID);
                    n.setRecipientType("staff");
                    n.setType("shift_assigned");
                    n.setMessage("Bạn có lịch làm mới: " + shiftLabel + " cho tháng " + month + "/" + year + ".");
                    n.setIsRead(0);
                    notifDao.insert(n);
                }
            }
        }

        if (successEmployees == 0) {

            String errorReason = "Không thể phân ca theo tháng.";

            if (skippedEmployees > 0) {
                errorReason = "Các nhân viên được chọn đã có ca đầy đủ trong tháng " + month + "/" + year + ".";
            }
            showRoster(req, resp, errorReason, null);
            return;
        }

        if (skippedEmployees > 0 || failedEmployees > 0) {
            String successMsg = "Đã áp dụng phân ca cho cả tháng cho " + successEmployees + " nhân viên.";
            String errorMsg = (skippedEmployees > 0
                    ? skippedEmployees + " nhân viên bị bỏ qua vì đã có đủ ca trong tháng đó. "
                    : "")
                    + (failedEmployees > 0 ? failedEmployees + " nhân viên phân ca lỗi." : "");
            setFlashMessages(req, errorMsg, successMsg);
            redirectToPlanMonth(req, resp, year, month, "month_result");
            return;
        }

        String successMsg = "Đã áp dụng phân ca cho cả tháng cho " + successEmployees + " nhân viên.";
        setFlashMessages(req, null, successMsg);
        redirectToPlanMonth(req, resp, year, month, "month_result");
    }

    private void handleCancelPlan(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int planID = parseInt(req.getParameter("planID"), 0);

        int year = parseInt(req.getParameter("planYear"), java.time.LocalDate.now().getYear());

        int month = parseInt(req.getParameter("planMonth"), java.time.LocalDate.now().getMonthValue());

        if (planID <= 0) {
            showRoster(req, resp, "planID không hợp lệ.", null);
            return;
        }

        boolean ok;
        try (MonthlyShiftPlanDAO dao = new MonthlyShiftPlanDAO()) {
            ok = dao.cancelPlan(planID);
        }

        redirectToPlanMonth(req, resp, year, month, ok ? "plan_cancelled" : "plan_cancel_failed");
    }

    private void redirectToPlanMonth(HttpServletRequest req, HttpServletResponse resp,
            int year, int month, String msg) throws IOException {
        resp.sendRedirect(req.getContextPath()
                + "/owner/shift-roster?planYear=" + year + "&planMonth=" + month + "&msg=" + msg);
    }

    private static void setFlashMessages(HttpServletRequest req, String error, String success) {
        HttpSession session = req.getSession();
        if (error != null && !error.isBlank()) {
            session.setAttribute("flashError", error);
        }
        if (success != null && !success.isBlank()) {
            session.setAttribute("flashSuccess", success);
        }
    }

    private static void applyFlashMessages(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }

        Object flashError = session.getAttribute("flashError");
        Object flashSuccess = session.getAttribute("flashSuccess");
        session.removeAttribute("flashError");
        session.removeAttribute("flashSuccess");

        if (flashError instanceof String && !((String) flashError).isBlank()) {
            req.setAttribute("error", flashError);
        }
        if (flashSuccess instanceof String && !((String) flashSuccess).isBlank()) {
            req.setAttribute("success", flashSuccess);
        }
    }

    private static Integer currentEmployeeID(HttpServletRequest req) {
        HttpSession ss = req.getSession(false);
        if (ss == null) {
            return null;
        }
        Object v = ss.getAttribute("employeeID");
        if (v instanceof Integer) {
            return (Integer) v;
        }
        Object emp = ss.getAttribute("employee");
        if (emp instanceof Employee) {
            return ((Employee) emp).getEmployeeID();
        }
        return null;
    }

    private void handleApproveRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        Employee emp = session == null ? null : (Employee) session.getAttribute("employee");

        if (emp == null) {
            resp.sendRedirect(req.getContextPath() + "/login?type=employee");
            return;
        }

        int swapID = parseInt(req.getParameter("swapID"), 0);

        if (swapID <= 0) {
            showRoster(req, resp, "Mã yêu cầu không hợp lệ.", null);
            return;
        }

        ShiftSwapRequestDetail detail;
        boolean success;
        try (ShiftSwapRequestDAO reqDAO = new ShiftSwapRequestDAO()) {
            detail = reqDAO.getDetailById(swapID);

            if (detail == null) {
                showRoster(req, resp, "Không tìm thấy yêu cầu.", null);
                return;
            }

            if (!"leave".equals(detail.getRequestType())) {
                showRoster(req, resp, "Owner chỉ được duyệt yêu cầu xin nghỉ.", null);
                return;
            }

            success = reqDAO.updateStatus(swapID, "approved", emp.getEmployeeID());
        }

        if (success) {

            try (NotificationDAO notifDAO = new NotificationDAO()) {

                Notifications n1 = new Notifications();

                n1.setRecipientID(detail.getReqEmployeeID());

                n1.setRecipientType("staff");

                n1.setType("shift_request_approved");

                n1.setMessage("Yêu cầu xin nghỉ của bạn cho ca ngày " + detail.getReqWorkDate() + " đã được phê duyệt.");

                n1.setIsRead(0);

                notifDAO.insert(n1);
            }

            resp.sendRedirect(req.getContextPath() + "/owner/shift-roster?msg=approved_success");
        } else {

            showRoster(req, resp, "Duyệt yêu cầu thất bại.", null);
        }
    }

    private void handleRejectRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        Employee emp = session == null ? null : (Employee) session.getAttribute("employee");

        if (emp == null) {
            resp.sendRedirect(req.getContextPath() + "/login?type=employee");
            return;
        }

        int swapID = parseInt(req.getParameter("swapID"), 0);

        if (swapID <= 0) {
            showRoster(req, resp, "Mã yêu cầu không hợp lệ.", null);
            return;
        }

        ShiftSwapRequestDetail detail;
        boolean success;
        try (ShiftSwapRequestDAO reqDAO = new ShiftSwapRequestDAO()) {
            detail = reqDAO.getDetailById(swapID);

            if (detail == null) {
                showRoster(req, resp, "Không tìm thấy yêu cầu.", null);
                return;
            }

            if (!"leave".equals(detail.getRequestType())) {
                showRoster(req, resp, "Owner chỉ được từ chối yêu cầu xin nghỉ.", null);
                return;
            }

            success = reqDAO.updateStatus(swapID, "rejected", emp.getEmployeeID());
        }

        if (success) {

            try (NotificationDAO notifDAO = new NotificationDAO()) {

                Notifications n1 = new Notifications();

                n1.setRecipientID(detail.getReqEmployeeID());

                n1.setRecipientType("staff");

                n1.setType("shift_request_rejected");

                n1.setMessage("Yêu cầu xin nghỉ của bạn cho ca ngày " + detail.getReqWorkDate() + " đã bị từ chối.");

                n1.setIsRead(0);

                notifDAO.insert(n1);
            }

            resp.sendRedirect(req.getContextPath() + "/owner/shift-roster?msg=rejected_success");
        } else {

            showRoster(req, resp, "Từ chối yêu cầu thất bại.", null);
        }
    }

    private static int parseInt(String s, int def) {
        if (s == null || s.isBlank()) {
            return def;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static LocalDate parseDateOrToday(String s) {
        if (s == null || s.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private static LocalDate parseDateOrDefault(String s, LocalDate defVal) {
        if (s == null || s.isBlank()) {
            return defVal;
        }
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return defVal;
        }
    }

    private static YearMonth parseYearMonth(String yStr, String mStr, YearMonth defVal) {
        try {
            if (yStr == null || mStr == null) {
                return defVal;
            }
            int y = Integer.parseInt(yStr);
            int m = Integer.parseInt(mStr);
            return YearMonth.of(y, m);
        } catch (Exception e) {
            return defVal;
        }
    }

    private static String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }
}