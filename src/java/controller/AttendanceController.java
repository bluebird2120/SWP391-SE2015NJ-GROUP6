package controller;

import dal.EmployeeShiftDAO;
import dal.ShiftRow;
import dal.ShiftTemplateDAO;
import model.ShiftTemplates;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
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
            case "checkin":       handle(req, resp, "checkin"); break;
            case "checkout":      handle(req, resp, "checkout"); break;
            case "absent":        handle(req, resp, "absent"); break;
            case "reset":         handle(req, resp, "reset"); break;
            case "bulk_checkin":  handleBulk(req, resp, "checkin"); break;
            case "bulk_absent":   handleBulk(req, resp, "absent"); break;
            default: resp.sendRedirect(req.getContextPath() + "/owner/attendance");
        }
    }

    private void showAttendance(HttpServletRequest req, HttpServletResponse resp, String error, String success)
            throws ServletException, IOException {
        LocalDate date = parseDateOrToday(req.getParameter("date"));
        Date sqlDate = Date.valueOf(date);

        EmployeeShiftDAO dao = new EmployeeShiftDAO();
        List<ShiftRow> rows = dao.listByDate(sqlDate);

        // Load danh sách ca template để render dropdown lọc ca
        ShiftTemplateDAO tplDao = new ShiftTemplateDAO();
        List<ShiftTemplates> templates = tplDao.findAll();

        req.setAttribute("date", date.toString());
        req.setAttribute("today", LocalDate.now().toString());
        req.setAttribute("isToday", date.equals(LocalDate.now()));
        req.setAttribute("rows", rows);
        req.setAttribute("templates", templates);
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

    /**
     * Xử lý hành động điểm danh hàng loạt (bulk check-in hoặc bulk absent) cho nhiều mã ca cùng lúc.
     * Danh sách mã ca làm việc được nhận từ tham số "shiftIDs" dưới dạng mảng.
     * Quy tắc today-only áp dụng nghiêm ngặt: chỉ cho phép điểm danh hàng loạt các ca trong ngày hiện tại.
     *
     * @param req    đối tượng HttpServletRequest chứa các tham số yêu cầu (shiftIDs, date)
     * @param resp   đối tượng HttpServletResponse dùng để thực hiện điều hướng sau khi cập nhật thành công
     * @param action hành động điểm danh cần thực hiện ("checkin" hoặc "absent")
     * @throws ServletException nếu có lỗi xảy ra trong servlet
     * @throws IOException      nếu có lỗi vào ra dữ liệu
     */
    private void handleBulk(HttpServletRequest req, HttpServletResponse resp, String action)
            throws ServletException, IOException {
        String[] ids = req.getParameterValues("shiftIDs");
        LocalDate date = parseDateOrToday(req.getParameter("date"));

        if (ids == null || ids.length == 0) {
            showAttendance(req, resp, "Chưa chọn nhân viên nào.", null);
            return;
        }

        // Chỉ chấp nhận cà hôm nay
        if (!date.equals(LocalDate.now())) {
            showAttendance(req, resp, "Chỉ được điểm danh trong ngày hôm nay.", null);
            return;
        }

        List<Integer> shiftIDs = new ArrayList<>();
        for (String idStr : ids) {
            int id = parseInt(idStr, 0);
            if (id > 0) shiftIDs.add(id);
        }
        if (shiftIDs.isEmpty()) {
            showAttendance(req, resp, "shiftID không hợp lệ.", null);
            return;
        }

        EmployeeShiftDAO dao = new EmployeeShiftDAO();
        int updated;
        String successMsg;
        if ("checkin".equals(action)) {
            updated = dao.bulkCheckIn(shiftIDs, new Timestamp(System.currentTimeMillis()));
            successMsg = "bulk_checkedin";
        } else {
            updated = dao.bulkMarkAbsent(shiftIDs);
            successMsg = "bulk_absent";
        }

        resp.sendRedirect(req.getContextPath() + "/owner/attendance?date=" + date
                + "&msg=" + successMsg + "&cnt=" + updated);
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
