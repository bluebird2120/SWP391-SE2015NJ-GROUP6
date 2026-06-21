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
        if (action == null) action = "";
        switch (action) {
            case "assign":      handleAssign(req, resp); break;
            case "unassign":    handleUnassign(req, resp); break;
            case "assignMonth": handleAssignMonth(req, resp); break;
            case "cancelPlan":  handleCancelPlan(req, resp); break;
            case "approveRequest": handleApproveRequest(req, resp); break;
            case "rejectRequest":  handleRejectRequest(req, resp); break;
            default: resp.sendRedirect(req.getContextPath() + "/owner/shift-roster");
        }
    }

    /* ==================== VIEW ==================== */

    private void showRoster(HttpServletRequest req, HttpServletResponse resp, String error, String success)
            throws ServletException, IOException {
        LocalDate date = parseDateOrToday(req.getParameter("date"));
        Date sqlDate = Date.valueOf(date);

        // Tháng đang xem cho bảng plan; mặc định = tháng hiện tại
        YearMonth viewYm = parseYearMonth(req.getParameter("planYear"), req.getParameter("planMonth"),
                YearMonth.now());

        EmployeeDAO empDao = new EmployeeDAO();
        ShiftTemplateDAO tplDao = new ShiftTemplateDAO();
        EmployeeShiftDAO shiftDao = new EmployeeShiftDAO();
        MonthlyShiftPlanDAO planDao = new MonthlyShiftPlanDAO();

        List<Employee> staff = empDao.listActiveStaff();
        List<ShiftTemplates> templates = tplDao.findAll();
        List<ShiftRow> roster = shiftDao.listByDate(sqlDate);
        List<MonthlyShiftPlan> monthlyPlans = planDao.listByMonth(viewYm.getYear(), viewYm.getMonthValue());

        // Load pending shift swap/leave requests
        ShiftSwapRequestDAO requestDAO = new ShiftSwapRequestDAO();
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

        // Xem lịch chi tiết 1 nhân viên theo tháng
        int viewEmpID = parseInt(req.getParameter("viewEmployeeID"), 0);
        int viewYear  = parseInt(req.getParameter("viewYear"), LocalDate.now().getYear());
        int viewMonth = parseInt(req.getParameter("viewMonth"), LocalDate.now().getMonthValue());
        req.setAttribute("viewEmployeeID", viewEmpID);
        req.setAttribute("viewYear", viewYear);
        req.setAttribute("viewMonth", viewMonth);
        if (viewEmpID > 0) {
            List<ShiftRow> staffSchedule = shiftDao.listByEmployeeAndMonth(viewEmpID, viewYear, viewMonth);
            req.setAttribute("staffSchedule", staffSchedule);
            for (Employee e : staff) {
                if (e.getEmployeeID() == viewEmpID) {
                    req.setAttribute("viewEmployeeName", e.getFullName());
                    break;
                }
            }
        }

        if (error != null) req.setAttribute("error", error);
        if (success != null) req.setAttribute("success", success);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    /* ==================== ASSIGN 1 NGÀY (giữ nguyên nhưng hỗ trợ nhiều nhân viên) ==================== */

    /**
     * Xử lý gán ca theo ngày cho một hoặc nhiều nhân viên cùng lúc.
     * Phương thức này nhận danh sách mã nhân viên (employeeIDs), mã ca (templateID) và ngày gán ca.
     * Tiến hành kiểm tra điều kiện trùng ca và thực hiện gán ca lần lượt cho từng nhân viên.
     *
     * @param req  đối tượng HttpServletRequest chứa các tham số yêu cầu (employeeIDs, templateID, date)
     * @param resp đối tượng HttpServletResponse để điều hướng hoặc trả về phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong servlet
     * @throws IOException      nếu có lỗi vào ra dữ liệu
     */
    private void handleAssign(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String[] empIDsStr = req.getParameterValues("employeeIDs");
        int templateID = parseInt(req.getParameter("templateID"), 0);
        LocalDate date = parseDateOrToday(req.getParameter("date"));
        Date sqlDate = Date.valueOf(date);

        if (empIDsStr == null || empIDsStr.length == 0 || templateID <= 0) {
            showRoster(req, resp, "Vui lòng chọn ít nhất một nhân viên và ca.", null);
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            showRoster(req, resp, "Không thể gán ca cho ngày quá khứ.", null);
            return;
        }

        EmployeeShiftDAO dao = new EmployeeShiftDAO();
        int successCount = 0;
        int overlapCount = 0;
        int failedCount = 0;

        for (String empIdStr : empIDsStr) {
            int employeeID = parseInt(empIdStr, 0);
            if (employeeID <= 0) continue;

            if (dao.hasOverlap(employeeID, sqlDate, templateID)) {
                overlapCount++;
                continue;
            }
            int shiftID = dao.assign(employeeID, templateID, sqlDate);
            if (shiftID < 0) {
                failedCount++;
            } else {
                successCount++;
            }
        }

        String successMsg = null;
        String errorMsg = null;
        if (successCount > 0) {
            successMsg = "Đã gán thành công cho " + successCount + " nhân viên.";
        }
        if (overlapCount > 0 || failedCount > 0) {
            errorMsg = (overlapCount > 0
                    ? overlapCount + " nhân viên đã có ca trong ngày này (mỗi nhân viên chỉ được làm 1 ca/ngày). "
                    : "")
                    + (failedCount > 0 ? failedCount + " nhân viên gán lỗi." : "");
        }

        if (successCount > 0 && overlapCount == 0 && failedCount == 0) {
            resp.sendRedirect(req.getContextPath() + "/owner/shift-roster?date=" + date + "&msg=assigned_multi&cnt=" + successCount);
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
        EmployeeShiftDAO dao = new EmployeeShiftDAO();
        boolean ok = dao.unassign(shiftID);
        String msg = ok ? "unassigned" : "unassign_failed";
        resp.sendRedirect(req.getContextPath() + "/owner/shift-roster?date=" + date + "&msg=" + msg);
    }

    /* ==================== PHÂN CA THEO THÁNG ==================== */

    /**
     * Xử lý phân ca tự động cả tháng cho một hoặc nhiều nhân viên cùng lúc.
     * Cho phép gán hàng loạt ca lặp lại cho các ngày trong tháng (bỏ qua ngày đã có hoặc thay thế toàn bộ).
     * Tạo bản ghi lịch sử phân ca hàng tháng (MonthlyShiftPlan) sau khi gán thành công.
     *
     * @param req  đối tượng HttpServletRequest chứa các tham số (employeeIDs, templateID, year, month, assignMode)
     * @param resp đối tượng HttpServletResponse để điều hướng hoặc trả về phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong servlet
     * @throws IOException      nếu có lỗi vào ra dữ liệu
     */
    private void handleAssignMonth(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String[] empIDsStr = req.getParameterValues("employeeIDs");
        int templateID = parseInt(req.getParameter("templateID"), 0);
        int year       = parseInt(req.getParameter("year"), 0);
        int month      = parseInt(req.getParameter("month"), 0);
        String mode    = req.getParameter("assignMode");
        if (mode == null) mode = "SKIP_EXISTING";

        if (empIDsStr == null || empIDsStr.length == 0 || templateID <= 0 || month < 1 || month > 12 || year < 2024) {
            showRoster(req, resp, "Tham số phân ca tháng không hợp lệ hoặc chưa chọn nhân viên.", null);
            return;
        }

        YearMonth target = YearMonth.of(year, month);
        YearMonth nowYm  = YearMonth.now();

        Integer ownerId = currentEmployeeID(req);
        if (ownerId == null) {
            showRoster(req, resp, "Phiên đăng nhập không hợp lệ.", null);
            return;
        }

        if (target.isAfter(nowYm.plusMonths(1))) {
            showRoster(req, resp, "Chỉ được phân ca cho tháng hiện tại hoặc tháng kế tiếp.", null);
            return;
        }

        EmployeeShiftDAO shiftDao = new EmployeeShiftDAO();
        MonthlyShiftPlanDAO planDao = new MonthlyShiftPlanDAO();
        int totalAssignedRows = 0;
        int successEmployees = 0;
        int skippedEmployees = 0;
        int failedEmployees = 0;

        for (String empIdStr : empIDsStr) {
            int employeeID = parseInt(empIdStr, 0);
            if (employeeID <= 0) continue;

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
                // Lưu plan history
                try {
                    int planID = planDao.saveOrUpdate(employeeID, templateID, year, month, ownerId);
                    if (planID > 0) {
                        planDao.updateStatus(planID, MonthlyShiftPlan.APPLIED);
                    }
                } catch (Exception ignore) {}
            }
        }

        if (successEmployees == 0) {
            String errorReason = "Không thể phân ca theo tháng.";
            if (skippedEmployees > 0) {
                errorReason = "Tất cả nhân viên được chọn đã có ca đầy đủ trong tháng " + month + "/" + year + ".";
            }
            showRoster(req, resp, errorReason, null);
            return;
        }

        String msg = "month_assigned_multi&cnt=" + successEmployees;
        if (skippedEmployees > 0 || failedEmployees > 0) {
            msg += "&skip=" + skippedEmployees + "&fail=" + failedEmployees;
        }
        redirectToPlanMonth(req, resp, year, month, msg);
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

        ShiftSwapRequestDAO reqDAO = new ShiftSwapRequestDAO();
        ShiftSwapRequestDetail detail = reqDAO.getDetailById(swapID);
        if (detail == null) {
            showRoster(req, resp, "Không tìm thấy yêu cầu.", null);
            return;
        }
        if ("swap".equals(detail.getRequestType()) && detail.getTargetShiftID() != null) {
            EmployeeShiftDAO esDAO = new EmployeeShiftDAO();
            if (esDAO.hasConflictingShift(detail.getReqEmployeeID(), detail.getTargetWorkDate(), detail.getRequesterShiftID())) {
                showRoster(req, resp, "Không thể phê duyệt: Nhân viên " + detail.getReqEmployeeName() + " đã có ca làm việc khác vào ngày " + detail.getTargetWorkDate() + "!", null);
                return;
            }
            if (esDAO.hasConflictingShift(detail.getTargetEmployeeID(), detail.getReqWorkDate(), detail.getTargetShiftID())) {
                showRoster(req, resp, "Không thể phê duyệt: Nhân viên " + detail.getTargetEmployeeName() + " đã có ca làm việc khác vào ngày " + detail.getReqWorkDate() + "!", null);
                return;
            }
        }

        boolean success = reqDAO.updateStatus(swapID, "approved", emp.getEmployeeID());
        if (success) {
            // Notify employee(s) about the approval!
            NotificationDAO notifDAO = new NotificationDAO();
            
            // Notify requester
            Notifications n1 = new Notifications();
            n1.setRecipientID(detail.getReqEmployeeID());
            n1.setRecipientType("staff");
            n1.setType("shift_request_approved");
            String reqTypeStr = "leave".equals(detail.getRequestType()) ? "xin nghỉ" : "đổi ca";
            n1.setMessage("Yêu cầu " + reqTypeStr + " của bạn cho ca ngày " + detail.getReqWorkDate() + " đã được phê duyệt.");
            n1.setIsRead(0);
            notifDAO.insert(n1);

            // If it is a swap, notify the target employee as well!
            if ("swap".equals(detail.getRequestType()) && detail.getTargetEmployeeID() != null) {
                Notifications n2 = new Notifications();
                n2.setRecipientID(detail.getTargetEmployeeID());
                n2.setRecipientType("staff");
                n2.setType("shift_request_approved");
                n2.setMessage("Ca làm việc của bạn ngày " + detail.getTargetWorkDate() + " đã được đổi với " + detail.getReqEmployeeName() + ".");
                n2.setIsRead(0);
                notifDAO.insert(n2);
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

        ShiftSwapRequestDAO reqDAO = new ShiftSwapRequestDAO();
        ShiftSwapRequestDetail detail = reqDAO.getDetailById(swapID);
        if (detail == null) {
            showRoster(req, resp, "Không tìm thấy yêu cầu.", null);
            return;
        }

        boolean success = reqDAO.updateStatus(swapID, "rejected", emp.getEmployeeID());
        if (success) {
            // Notify requester
            NotificationDAO notifDAO = new NotificationDAO();
            Notifications n1 = new Notifications();
            n1.setRecipientID(detail.getReqEmployeeID());
            n1.setRecipientType("staff");
            n1.setType("shift_request_rejected");
            String reqTypeStr = "leave".equals(detail.getRequestType()) ? "xin nghỉ" : "đổi ca";
            n1.setMessage("Yêu cầu " + reqTypeStr + " của bạn cho ca ngày " + detail.getReqWorkDate() + " đã bị từ chối.");
            n1.setIsRead(0);
            notifDAO.insert(n1);

            resp.sendRedirect(req.getContextPath() + "/owner/shift-roster?msg=rejected_success");
        } else {
            showRoster(req, resp, "Từ chối yêu cầu thất bại.", null);
        }
    }

    private static int parseInt(String s, int def) {
        if (s == null || s.isBlank()) return def;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
