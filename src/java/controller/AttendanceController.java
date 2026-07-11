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
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import model.EmployeeShifts;

@WebServlet(name = "AttendanceController", urlPatterns = {"/owner/attendance"})
public class AttendanceController extends HttpServlet {

    /*
     * NGHIỆP VỤ: Owner chấm công nhân viên theo từng ngày.
     *
     * Trang /owner/attendance hiển thị toàn bộ ca làm của một ngày, sau đó owner
     * có thể check-in, check-out, đánh dấu vắng mặt hoặc reset trạng thái ca.
     *
     * Luồng dữ liệu chính:
     * - JSP gửi date, shiftID và action lên controller.
     * - Controller kiểm tra ca có tồn tại và có thuộc ngày hôm nay không.
     * - Controller gọi EmployeeShiftDAO để cập nhật trạng thái/chấm công.
     * - Sau khi xử lý xong thì redirect về đúng ngày đang xem.
     */
    private static final String VIEW = "/views/owner/attendance.jsp";

    /*
     * GET: mở màn hình điểm danh.
     * Nếu URL có ?date=yyyy-MM-dd thì xem ngày đó, sai/rỗng thì dùng ngày hiện tại.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        showAttendance(req, resp, null, null);
    }

    /*
     * POST: nhận thao tác từ các nút điểm danh trên JSP.
     * action quyết định controller sẽ gọi check-in, check-out, absent hay reset.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "";
        }
        switch (action) {
            case "checkin":
                handle(req, resp, "checkin");
                break;
            case "checkout":
                handle(req, resp, "checkout");
                break;
            case "absent":
                handle(req, resp, "absent");
                break;
            case "reset":
                handle(req, resp, "reset");
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/owner/attendance");
        }
    }

    private void showAttendance(HttpServletRequest req, HttpServletResponse resp, String error, String success)
            throws ServletException, IOException {
        LocalDate date = parseDateOrToday(req.getParameter("date"));
        Date sqlDate = Date.valueOf(date);

        // Lấy danh sách ca làm trong ngày để JSP render bảng điểm danh.
        EmployeeShiftDAO dao = new EmployeeShiftDAO();
        List<ShiftRow> rows = dao.listByDate(sqlDate);

        // Lấy mẫu ca để JSP có đủ thông tin tên ca/giờ ca nếu cần hiển thị.
        ShiftTemplateDAO tplDao = new ShiftTemplateDAO();
        List<ShiftTemplates> templates = tplDao.findAll();

        req.setAttribute("date", date.toString());
        req.setAttribute("today", LocalDate.now().toString());
        req.setAttribute("isToday", date.equals(LocalDate.now()));
        req.setAttribute("rows", rows);
        req.setAttribute("templates", templates);
        if (error != null) {
            req.setAttribute("error", error);
        }
        if (success != null) {
            req.setAttribute("success", success);
        }
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp, String action)
            throws ServletException, IOException {
        // shiftID là mã ca cụ thể mà owner bấm thao tác trên bảng điểm danh.
        int shiftID = parseInt(req.getParameter("shiftID"), 0);
        LocalDate date = parseDateOrToday(req.getParameter("date"));
        if (shiftID <= 0) {
            showAttendance(req, resp, "shiftID không hợp lệ.", null);
            return;
        }

        EmployeeShiftDAO dao = new EmployeeShiftDAO();

        EmployeeShifts s = dao.findById(shiftID);
        if (s == null) {
            showAttendance(req, resp, "Không tìm thấy ca.", null);
            return;
        }

        // Nghiệp vụ hiện tại chỉ cho sửa điểm danh của ngày hôm nay.
        // Các ca quá khứ/tương lai chỉ được xem, không được check-in/check-out.
        Date today = Date.valueOf(LocalDate.now());
        if (!s.getWorkDate().toString().equals(today.toString())) {
            showAttendance(req, resp, "Chỉ được sửa điểm danh của ca trong ngày hôm nay.", null);
            return;
        }

        boolean ok;
        String successMsg;
        // Mỗi action tương ứng với một method cập nhật trạng thái trong EmployeeShiftDAO.
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
        if (s == null || s.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return LocalDate.now();
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
}
