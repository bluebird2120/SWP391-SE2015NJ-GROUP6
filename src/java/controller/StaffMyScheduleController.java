package controller;

import dal.EmployeeDAO;
import dal.EmployeeShiftDAO;
import dal.NotificationDAO;
import dal.ShiftRow;
import dal.ShiftSwapRequestDAO;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Employee;
import model.Notifications;
import model.ShiftSwapRequests;
import model.ShiftSwapRequestDetail;

@WebServlet(name = "StaffMyScheduleController", urlPatterns = {"/staff/my-schedule"})
public class StaffMyScheduleController extends HttpServlet {

    /*
     * NGHIỆP VỤ: Nhân viên xem lịch cá nhân và gửi yêu cầu đổi lịch/xin nghỉ.
     *
     * Màn /staff/my-schedule có ba nhóm chức năng:
     * - Xem lịch ca của chính nhân viên theo tháng.
     * - Gửi yêu cầu xin nghỉ ca cho owner duyệt.
     * - Gửi yêu cầu nhờ đồng nghiệp làm thay; đồng nghiệp có thể nhận hoặc từ chối.
     *
     * Các rule bảo vệ quan trọng:
     * - Nhân viên chỉ thao tác trên ca thuộc chính mình.
     * - Chỉ ca status scheduled và chưa qua ngày hiện tại mới được gửi yêu cầu.
     * - Một ca đang có yêu cầu pending thì không được gửi yêu cầu mới.
     * - Người làm thay phải là nhân viên active, không phải chính người gửi,
     *   và không có ca trùng ngày.
     */
    private static final String VIEW = "/views/staff/my-schedule.jsp";

    /*
     * GET: tải lịch tháng của nhân viên đang đăng nhập.
     * Controller gom dữ liệu thành map theo ngày để JSP dựng lịch dạng calendar.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee emp = session == null ? null : (Employee) session.getAttribute("employee");
        if (emp == null) {
            resp.sendRedirect(req.getContextPath() + "/login?type=employee");
            return;
        }

        String yearParam = req.getParameter("year");
        String monthParam = req.getParameter("month");
        // Validate tháng/năm nhân viên nhập ở form "Đi tới".
        // Nếu sai thì hiển thị lỗi và dùng tháng hiện tại để trang vẫn render được.
        String scheduleFilterError = validateScheduleFilter(yearParam, monthParam);
        YearMonth ym = scheduleFilterError == null
                ? parseYearMonth(yearParam, monthParam, YearMonth.now())
                : YearMonth.now();
        int year  = ym.getYear();
        int month = ym.getMonthValue();
        req.setAttribute("scheduleFilterError", scheduleFilterError);

        EmployeeShiftDAO shiftDAO = new EmployeeShiftDAO();
        ShiftSwapRequestDAO requestDAO = new ShiftSwapRequestDAO();
        EmployeeDAO employeeDAO = new EmployeeDAO();

        // Nếu database không kết nối được thì vẫn forward JSP với dữ liệu rỗng và lỗi rõ ràng.
        if (!shiftDAO.isConnectionAvailable() || !requestDAO.isConnectionAvailable() || !employeeDAO.isConnectionAvailable()) {
            setEmptyScheduleAttributes(req, year, month, ym, "Không thể kết nối database. Vui lòng kiểm tra MySQL và cấu hình DBContext.");
            req.getRequestDispatcher(VIEW).forward(req, resp);
            return;
        }

        List<ShiftRow> rows;
        try {
            rows = shiftDAO.listByEmployeeAndMonth(emp.getEmployeeID(), year, month);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            setEmptyScheduleAttributes(req, year, month, ym, "Không thể tải lịch làm việc. Vui lòng kiểm tra database.");
            req.getRequestDispatcher(VIEW).forward(req, resp);
            return;
        }

        Map<String, List<ShiftRow>> byDay = new LinkedHashMap<>();
        // Key là ngày trong tháng ("1", "2", ...), value là danh sách ca của ngày đó.
        for (ShiftRow r : rows) {
            String day = String.valueOf(r.getWorkDate().toLocalDate().getDayOfMonth());
            byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(r);
        }

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int firstDow = firstDay.getDayOfWeek().getValue() % 7;
        int daysInMonth = ym.lengthOfMonth();

        YearMonth prev = ym.minusMonths(1);
        YearMonth next = ym.plusMonths(1);

        Map<Integer, ShiftSwapRequests> pendingRequests = requestDAO.getPendingRequestsMap(emp.getEmployeeID());
        Map<String, List<Employee>> availableCoverStaffByDate = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        // Với từng ca còn có thể đổi/xin nghỉ, tìm danh sách đồng nghiệp rảnh cùng ngày.
        for (ShiftRow row : rows) {
            LocalDate workDate = row.getWorkDate().toLocalDate();
            String key = workDate.toString();
            if ("scheduled".equals(row.getStatus()) && !workDate.isBefore(today) && !availableCoverStaffByDate.containsKey(key)) {
                availableCoverStaffByDate.put(key, employeeDAO.listAvailableStaffByDate(row.getWorkDate(), emp.getEmployeeID()));
            }
        }
        // Danh sách yêu cầu làm thay mà đồng nghiệp gửi cho nhân viên hiện tại xác nhận.
        List<ShiftSwapRequestDetail> colleagueRequests = requestDAO.listPendingColleagueRequests(emp.getEmployeeID());

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

        req.setAttribute("pendingRequests", pendingRequests);
        req.setAttribute("availableCoverStaffByDate", availableCoverStaffByDate);
        req.setAttribute("colleagueRequests", colleagueRequests);

        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private void setEmptyScheduleAttributes(HttpServletRequest req, int year, int month, YearMonth ym, String errorMessage) {
        // Set đủ attribute tối thiểu để JSP không lỗi khi không tải được dữ liệu thật.
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int firstDow = firstDay.getDayOfWeek().getValue() % 7;
        YearMonth prev = ym.minusMonths(1);
        YearMonth next = ym.plusMonths(1);

        req.setAttribute("scheduleMap", Collections.emptyMap());
        req.setAttribute("year", year);
        req.setAttribute("month", month);
        req.setAttribute("daysInMonth", ym.lengthOfMonth());
        req.setAttribute("firstDow", firstDow);
        req.setAttribute("today", LocalDate.now().toString());
        req.setAttribute("currentYear", LocalDate.now().getYear());
        req.setAttribute("currentMonth", LocalDate.now().getMonthValue());
        req.setAttribute("currentDay", LocalDate.now().getDayOfMonth());
        req.setAttribute("prevYear", prev.getYear());
        req.setAttribute("prevMonth", prev.getMonthValue());
        req.setAttribute("nextYear", next.getYear());
        req.setAttribute("nextMonth", next.getMonthValue());
        req.setAttribute("totalShifts", 0);
        req.setAttribute("pendingRequests", Collections.emptyMap());
        req.setAttribute("availableCoverStaffByDate", Collections.emptyMap());
        req.setAttribute("colleagueRequests", Collections.emptyList());
        req.setAttribute("errorMsg", errorMessage);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // POST xử lý 4 action: accept/reject yêu cầu làm thay, requestCover, requestLeave.
        HttpSession session = req.getSession(false);
        Employee emp = session == null ? null : (Employee) session.getAttribute("employee");
        if (emp == null) {
            resp.sendRedirect(req.getContextPath() + "/login?type=employee");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) {
            resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
            return;
        }

        ShiftSwapRequestDAO requestDAO = new ShiftSwapRequestDAO();
        if (!requestDAO.isConnectionAvailable()) {
            session.setAttribute("errorMsg", "Không thể kết nối database. Vui lòng kiểm tra MySQL và cấu hình DBContext.");
            resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
            return;
        }

        if ("acceptCoverRequest".equals(action) || "rejectCoverRequest".equals(action)) {
            try {
                // requestID là mã yêu cầu làm thay mà đồng nghiệp gửi đến nhân viên hiện tại.
                int requestID = Integer.parseInt(req.getParameter("requestID"));
                ShiftSwapRequestDetail detail = requestDAO.getDetailById(requestID);
                
                // Chỉ người được nhờ làm thay (targetEmployeeID) mới được accept/reject.
                if (detail == null || detail.getTargetEmployeeID() == null || detail.getTargetEmployeeID() != emp.getEmployeeID()) {
                    session.setAttribute("errorMsg", "Không tìm thấy yêu cầu làm thay phù hợp!");
                    resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                    return;
                }

                NotificationDAO notifDAO = new NotificationDAO();

                if ("acceptCoverRequest".equals(action)) {
                    // Nhận làm thay: kiểm tra nhân viên nhận không có ca khác cùng ngày.
                    EmployeeShiftDAO esDAO = new EmployeeShiftDAO();
                    if (!esDAO.isConnectionAvailable()) {
                        session.setAttribute("errorMsg", "Không thể kết nối database. Vui lòng kiểm tra MySQL và cấu hình DBContext.");
                        resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                        return;
                    }
                    if (!"cover".equals(detail.getRequestType())) {
                        session.setAttribute("errorMsg", "Yêu cầu này không phải yêu cầu làm thay.");
                        resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                        return;
                    }
                    if (esDAO.hasConflictingShift(emp.getEmployeeID(), detail.getReqWorkDate(), 0)) {
                        session.setAttribute("errorMsg", "Không thể nhận làm thay: Bạn đã có ca khác vào ngày " + detail.getReqWorkDate() + "!");
                        resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                        return;
                    }

                    boolean success = requestDAO.updateStatus(requestID, "approved", null);
                    if (success) {
                        // DAO đổi trạng thái approved và chuyển ca sang nhân viên nhận làm thay.
                        session.setAttribute("successMsg", "Đã nhận làm thay thành công! Ca làm việc đã được chuyển sang lịch của bạn.");

                        Notifications n1 = new Notifications();
                        n1.setRecipientID(detail.getReqEmployeeID());
                        n1.setRecipientType("staff");
                        n1.setType("shift_request_approved");
                        n1.setMessage("Đồng nghiệp " + emp.getFullName() + " đã đồng ý làm thay ca ngày "
                                + detail.getReqWorkDate() + " của bạn. Ca này đã được chuyển sang lịch của đồng nghiệp.");
                        n1.setIsRead(0);
                        notifDAO.insert(n1);

                        Notifications n2 = new Notifications();
                        n2.setRecipientID(emp.getEmployeeID());
                        n2.setRecipientType("staff");
                        n2.setType("shift_request_approved");
                        n2.setMessage("Bạn đã đồng ý làm thay ca ngày " + detail.getReqWorkDate()
                                + " của " + detail.getReqEmployeeName() + ".");
                        n2.setIsRead(0);
                        notifDAO.insert(n2);
                    } else {
                        session.setAttribute("errorMsg", "Lỗi khi nhận làm thay. Vui lòng thử lại!");
                    }
                } else {
                    // Từ chối làm thay: chỉ cập nhật trạng thái yêu cầu và báo lại người gửi.
                    boolean success = requestDAO.updateColleagueStatus(requestID, "colleague_rejected");
                    if (success) {
                        session.setAttribute("successMsg", "Đã từ chối yêu cầu làm thay từ đồng nghiệp.");

                        Notifications n1 = new Notifications();
                        n1.setRecipientID(detail.getReqEmployeeID());
                        n1.setRecipientType("staff");
                        n1.setType("shift_request_colleague_rejected");
                        n1.setMessage("Đồng nghiệp " + emp.getFullName() + " đã từ chối yêu cầu làm thay ca ngày " + detail.getReqWorkDate() + " của bạn.");
                        n1.setIsRead(0);
                        notifDAO.insert(n1);
                    } else {
                        session.setAttribute("errorMsg", "Lỗi khi cập nhật trạng thái yêu cầu!");
                    }
                }
            } catch (Exception e) {
                session.setAttribute("errorMsg", "Có lỗi xảy ra: " + e.getMessage());
            }
            resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
            return;
        }
    
        // Các action còn lại là nhân viên chủ động gửi yêu cầu nhờ làm thay hoặc xin nghỉ.
        if (!"requestCover".equals(action) && !"requestLeave".equals(action)) {
            resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
            return;
        }

        String requesterShiftStr = req.getParameter("requesterShiftID");
        String reason = req.getParameter("reason");

        if (requesterShiftStr == null || requesterShiftStr.trim().isEmpty() || reason == null || reason.trim().isEmpty()) {
            session.setAttribute("errorMsg", "Dữ liệu yêu cầu không hợp lệ hoặc bị thiếu!");
            resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
            return;
        }

        if (reason.trim().length() > 500) {
            session.setAttribute("errorMsg", "Lý do không được vượt quá 500 ký tự!");
            resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
            return;
        }

        try {
            int requesterShiftID = Integer.parseInt(requesterShiftStr);
            EmployeeShiftDAO esDAO = new EmployeeShiftDAO();
            if (!esDAO.isConnectionAvailable()) {
                session.setAttribute("errorMsg", "Không thể kết nối database. Vui lòng kiểm tra MySQL và cấu hình DBContext.");
                resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                return;
            }
            ShiftRow reqShift = esDAO.getShiftByID(requesterShiftID);

            // Kiểm tra ca tồn tại, thuộc nhân viên đang đăng nhập và đang ở trạng thái scheduled.
            if (reqShift == null) {
                session.setAttribute("errorMsg", "Không tìm thấy ca làm việc cần đổi!");
                resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                return;
            }

            if (reqShift.getEmployeeID() != emp.getEmployeeID()) {
                session.setAttribute("errorMsg", "Ca làm việc này không thuộc về bạn!");
                resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                return;
            }

            if (!"scheduled".equals(reqShift.getStatus())) {
                session.setAttribute("errorMsg", "Chỉ có thể gửi yêu cầu cho ca ở trạng thái scheduled!");
                resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                return;
            }

            Map<Integer, ShiftSwapRequests> pendingMap = requestDAO.getPendingRequestsMap(emp.getEmployeeID());
            if (pendingMap.containsKey(requesterShiftID)) {
                session.setAttribute("errorMsg", "Ca làm việc này đang có yêu cầu chờ duyệt!");
                resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                return;
            }

            Integer coverEmployeeID = null;
            Employee coverEmployee = null;

            if ("requestCover".equals(action)) {
                // Nhờ làm thay cần thêm targetEmployeeID của đồng nghiệp được chọn.
                String targetEmployeeStr = req.getParameter("targetEmployeeID");
                if (targetEmployeeStr == null || targetEmployeeStr.trim().isEmpty()) {
                    session.setAttribute("errorMsg", "Vui lòng chọn nhân viên làm thay!");
                    resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                    return;
                }
                int targetEmployeeID = Integer.parseInt(targetEmployeeStr);
                EmployeeDAO employeeDAO = new EmployeeDAO();
                if (!employeeDAO.isConnectionAvailable()) {
                    session.setAttribute("errorMsg", "Không thể kết nối database. Vui lòng kiểm tra MySQL và cấu hình DBContext.");
                    resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                    return;
                }
                coverEmployee = employeeDAO.findById(targetEmployeeID);

                if (coverEmployee == null || !employeeDAO.isActiveStaff(targetEmployeeID)) {
                    session.setAttribute("errorMsg", "Không tìm thấy nhân viên làm thay phù hợp!");
                    resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                    return;
                }

                if (targetEmployeeID == emp.getEmployeeID()) {
                    session.setAttribute("errorMsg", "Không thể tự chọn chính mình làm thay!");
                    resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                    return;
                }

                if (esDAO.hasConflictingShift(targetEmployeeID, reqShift.getWorkDate(), 0)) {
                    session.setAttribute("errorMsg", "Nhân viên " + coverEmployee.getFullName() + " đã có ca làm việc vào ngày " + reqShift.getWorkDate() + "!");
                    resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                    return;
                }

                coverEmployeeID = targetEmployeeID;
            }

            ShiftSwapRequests reqObj = new ShiftSwapRequests();
            // Với requestCover, approvedByID đang được dùng để lưu targetEmployeeID.
            // Với requestLeave, approvedByID để null và owner sẽ duyệt/từ chối sau.
            reqObj.setRequesterShiftID(requesterShiftID);
            reqObj.setApprovedByID(coverEmployeeID);
            reqObj.setStatus("requestCover".equals(action) ? "pending_colleague" : "pending");
            reqObj.setReason(reason.trim());
            reqObj.setRequestType("requestCover".equals(action) ? "cover" : "leave");

            boolean success = requestDAO.insert(reqObj);
            if (success) {
                session.setAttribute("successMsg", "Gửi yêu cầu thành công!");

                NotificationDAO notifDAO = new NotificationDAO();
                if ("requestCover".equals(action) && coverEmployee != null) {
                    // Nhờ làm thay: gửi thông báo đến đồng nghiệp được chọn.
                    Notifications n = new Notifications();
                    n.setRecipientID(coverEmployee.getEmployeeID());
                    n.setRecipientType("staff");
                    n.setType("shift_request_colleague_pending");
                    n.setMessage("Đồng nghiệp " + emp.getFullName() + " muốn nhờ bạn làm thay ca ngày "
                            + reqShift.getWorkDate() + " (" + reqShift.getShiftName() + "). Vui lòng xác nhận.");
                    n.setIsRead(0);
                    notifDAO.insert(n);
                } else {
                    // Xin nghỉ: gửi thông báo cho tất cả owner active để owner duyệt.
                    EmployeeDAO empDAO = new EmployeeDAO();
                    List<Integer> ownerIDs = empDAO.getActiveOwnerIDs();
                    String msgText = String.format("Nhân viên %s vừa gửi yêu cầu xin nghỉ cho ca ngày %s (%s).", 
                                                    emp.getFullName(), reqShift.getWorkDate().toString(), reqShift.getShiftName());
                    for (int ownerID : ownerIDs) {
                        Notifications n = new Notifications();
                        n.setRecipientID(ownerID);
                        n.setRecipientType("staff");
                        n.setType("shift_request");
                        n.setMessage(msgText);
                        n.setIsRead(0);
                        notifDAO.insert(n);
                    }
                }
            } else {
                session.setAttribute("errorMsg", "Lỗi khi lưu yêu cầu vào cơ sở dữ liệu!");
            }

            LocalDate d = reqShift.getWorkDate().toLocalDate();
            resp.sendRedirect(req.getContextPath() + "/staff/my-schedule?year=" + d.getYear() + "&month=" + d.getMonthValue());

        } catch (NumberFormatException e) {
            session.setAttribute("errorMsg", "Tham số định dạng mã ca không hợp lệ!");
            resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
        }
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

    private static String validateScheduleFilter(String yStr, String mStr) {
        if (yStr == null && mStr == null) {
            return null;
        }
        if (mStr == null || mStr.isBlank() || !isInteger(mStr)) {
            return "Vui lòng chọn tháng xem lịch hợp lệ.";
        }
        if (yStr == null || yStr.isBlank() || !isInteger(yStr)) {
            return "Vui lòng nhập năm xem lịch hợp lệ.";
        }

        int month = Integer.parseInt(mStr);
        int year = Integer.parseInt(yStr);
        if (month < 1 || month > 12) {
            return "Vui lòng chọn tháng xem lịch hợp lệ.";
        }
        if (year < 2024) {
            return "Vui lòng nhập năm xem lịch từ 2024 trở đi.";
        }
        return null;
    }

    private static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
