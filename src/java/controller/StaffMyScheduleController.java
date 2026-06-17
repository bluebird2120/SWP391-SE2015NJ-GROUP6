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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Employee;
import model.Notifications;
import model.ShiftSwapRequests;
import model.ShiftSwapRequestDetail;

/**
 * Staff xem lịch ca của bản thân theo tháng (calendar view) và gửi yêu cầu đổi ca / xin nghỉ.
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

        EmployeeShiftDAO shiftDAO = new EmployeeShiftDAO();
        List<ShiftRow> rows = shiftDAO.listByEmployeeAndMonth(emp.getEmployeeID(), year, month);

        Map<String, List<ShiftRow>> byDay = new LinkedHashMap<>();
        for (ShiftRow r : rows) {
            String day = String.valueOf(r.getWorkDate().toLocalDate().getDayOfMonth());
            byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(r);
        }

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int firstDow = firstDay.getDayOfWeek().getValue() % 7;
        int daysInMonth = ym.lengthOfMonth();

        YearMonth prev = ym.minusMonths(1);
        YearMonth next = ym.plusMonths(1);

        // Fetch pending requests map and eligible other shifts for swap dropdown
        ShiftSwapRequestDAO swapDAO = new ShiftSwapRequestDAO();
        Map<Integer, ShiftSwapRequests> pendingRequests = swapDAO.getPendingRequestsMap(emp.getEmployeeID());
        List<ShiftRow> otherShifts = shiftDAO.listEligibleSwaps(emp.getEmployeeID());
        List<ShiftSwapRequestDetail> colleagueRequests = swapDAO.listPendingColleagueRequests(emp.getEmployeeID());

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
        req.setAttribute("otherShifts", otherShifts);
        req.setAttribute("colleagueRequests", colleagueRequests);

        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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

        ShiftSwapRequestDAO swapDAO = new ShiftSwapRequestDAO();

        // Handle colleague approval/rejection actions
        if ("acceptColleagueSwap".equals(action) || "rejectColleagueSwap".equals(action)) {
            try {
                int swapID = Integer.parseInt(req.getParameter("swapID"));
                ShiftSwapRequestDetail detail = swapDAO.getDetailById(swapID);
                if (detail == null || detail.getTargetEmployeeID() != emp.getEmployeeID()) {
                    session.setAttribute("errorMsg", "Không tìm thấy yêu cầu đổi ca phù hợp!");
                    resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                    return;
                }

                NotificationDAO notifDAO = new NotificationDAO();

                if ("acceptColleagueSwap".equals(action)) {
                    boolean success = swapDAO.updateColleagueStatus(swapID, "pending");
                    if (success) {
                        session.setAttribute("successMsg", "Đã đồng ý đổi ca. Yêu cầu đang được gửi tới quản lý để phê duyệt.");

                        // 1. Notify Requester
                        Notifications n1 = new Notifications();
                        n1.setRecipientID(detail.getReqEmployeeID());
                        n1.setRecipientType("staff");
                        n1.setType("shift_request_colleague_accepted");
                        n1.setMessage("Đồng nghiệp " + emp.getFullName() + " đã đồng ý đổi ca ngày " + detail.getReqWorkDate() + " của bạn. Yêu cầu đang chờ quản lý phê duyệt.");
                        n1.setIsRead(0);
                        notifDAO.insert(n1);

                        // 2. Notify Owners
                        EmployeeDAO empDAO = new EmployeeDAO();
                        List<Integer> ownerIDs = empDAO.getActiveOwnerIDs();
                        String ownerMsgText = String.format("Có yêu cầu đổi ca mới giữa %s và %s đang chờ phê duyệt.",
                                                        detail.getReqEmployeeName(), emp.getFullName());
                        for (int ownerID : ownerIDs) {
                            Notifications n = new Notifications();
                            n.setRecipientID(ownerID);
                            n.setRecipientType("staff");
                            n.setType("shift_request");
                            n.setMessage(ownerMsgText);
                            n.setIsRead(0);
                            notifDAO.insert(n);
                        }
                    } else {
                        session.setAttribute("errorMsg", "Lỗi khi cập nhật trạng thái yêu cầu!");
                    }
                } else {
                    boolean success = swapDAO.updateColleagueStatus(swapID, "colleague_rejected");
                    if (success) {
                        session.setAttribute("successMsg", "Đã từ chối yêu cầu đổi ca từ đồng nghiệp.");

                        // Notify Requester
                        Notifications n1 = new Notifications();
                        n1.setRecipientID(detail.getReqEmployeeID());
                        n1.setRecipientType("staff");
                        n1.setType("shift_request_colleague_rejected");
                        n1.setMessage("Đồng nghiệp " + emp.getFullName() + " đã từ chối yêu cầu đổi ca ngày " + detail.getReqWorkDate() + " của bạn.");
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

        if (!"requestSwap".equals(action) && !"requestLeave".equals(action)) {
            resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
            return;
        }

        String requesterShiftStr = req.getParameter("requesterShiftID");
        String reason = req.getParameter("reason");

        // Backend Validations
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
            ShiftRow reqShift = esDAO.getShiftByID(requesterShiftID);

            if (reqShift == null) {
                session.setAttribute("errorMsg", "Không tìm thấy ca làm việc cần đổi/xin nghỉ!");
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

            // Check if there is already a pending request for this shift
            Map<Integer, ShiftSwapRequests> pendingMap = swapDAO.getPendingRequestsMap(emp.getEmployeeID());
            if (pendingMap.containsKey(requesterShiftID)) {
                session.setAttribute("errorMsg", "Ca làm việc này đang có yêu cầu chờ duyệt!");
                resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                return;
            }

            Integer targetShiftID = null;
            ShiftRow targetShift = null;
            if ("requestSwap".equals(action)) {
                String targetShiftStr = req.getParameter("targetShiftID");
                if (targetShiftStr == null || targetShiftStr.trim().isEmpty()) {
                    session.setAttribute("errorMsg", "Vui lòng chọn ca muốn đổi cùng!");
                    resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                    return;
                }
                int tarShiftID = Integer.parseInt(targetShiftStr);
                targetShift = esDAO.getShiftByID(tarShiftID);

                if (targetShift == null) {
                    session.setAttribute("errorMsg", "Không tìm thấy ca muốn đổi cùng!");
                    resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                    return;
                }

                if (targetShift.getEmployeeID() == emp.getEmployeeID()) {
                    session.setAttribute("errorMsg", "Không thể tự đổi ca với chính mình!");
                    resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                    return;
                }

                if (!"scheduled".equals(targetShift.getStatus())) {
                    session.setAttribute("errorMsg", "Ca muốn đổi cùng phải ở trạng thái scheduled!");
                    resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                    return;
                }

                // Overlap validation
                if (esDAO.hasConflictingShift(emp.getEmployeeID(), targetShift.getWorkDate(), requesterShiftID)) {
                    session.setAttribute("errorMsg", "Bạn đã có ca làm việc khác vào ngày " + targetShift.getWorkDate() + "!");
                    resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                    return;
                }
                if (esDAO.hasConflictingShift(targetShift.getEmployeeID(), reqShift.getWorkDate(), targetShift.getShiftID())) {
                    session.setAttribute("errorMsg", "Đồng nghiệp " + targetShift.getFullName() + " đã có ca làm việc khác vào ngày " + reqShift.getWorkDate() + "!");
                    resp.sendRedirect(req.getContextPath() + "/staff/my-schedule");
                    return;
                }

                targetShiftID = tarShiftID;
            }

            // Save request
            ShiftSwapRequests reqObj = new ShiftSwapRequests();
            reqObj.setRequesterShiftID(requesterShiftID);
            reqObj.setTargetShiftID(targetShiftID);
            reqObj.setStatus("requestSwap".equals(action) ? "pending_colleague" : "pending");
            reqObj.setReason(reason.trim());
            reqObj.setRequestType("requestSwap".equals(action) ? "swap" : "leave");

            boolean success = swapDAO.insert(reqObj);
            if (success) {
                session.setAttribute("successMsg", "Gửi yêu cầu thành công!");

                NotificationDAO notifDAO = new NotificationDAO();
                if ("requestSwap".equals(action) && targetShift != null) {
                    // Notify target colleague
                    Notifications n = new Notifications();
                    n.setRecipientID(targetShift.getEmployeeID());
                    n.setRecipientType("staff");
                    n.setType("shift_request_colleague_pending");
                    n.setMessage("Đồng nghiệp " + emp.getFullName() + " muốn đổi ca ngày " + reqShift.getWorkDate() + " với ca ngày " + targetShift.getWorkDate() + " của bạn. Vui lòng xác nhận.");
                    n.setIsRead(0);
                    notifDAO.insert(n);
                } else {
                    // Notify Owners (for leave request)
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

            // Redirect back to the calendar month of the requester shift
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
}

