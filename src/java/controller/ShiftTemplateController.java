package controller;

import dal.ShiftTemplateDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import model.ShiftTemplates;

@WebServlet(name = "ShiftTemplateController", urlPatterns = {"/owner/shift-templates"})
public class ShiftTemplateController extends HttpServlet {

    /*
     * NGHIỆP VỤ: Owner quản lý mẫu ca làm việc.
     *
     * Mẫu ca là cấu hình giờ làm như "Ca chiều 13:00 - 17:00" hoặc "Ca tối
     * 18:00 - 22:00". Các màn phân ca theo ngày/tháng sẽ dùng templateID của
     * mẫu ca này để gán lịch cho nhân viên.
     *
     * Quy tắc quan trọng:
     * - Tên ca không được trùng.
     * - Không hỗ trợ ca qua đêm.
     * - Không cho tạo/sửa ca đêm trước 13:00 hoặc từ 22:00 trở đi.
     * - Nếu mẫu ca đã được dùng trong EmployeeShifts thì chỉ cho sửa tên,
     *   không cho sửa giờ để tránh làm sai lịch đã gán trước đó.
     */
    private static final String LIST_VIEW = "/views/owner/shift-template-list.jsp";
    private static final String FORM_VIEW = "/views/owner/shift-template-form.jsp";

    /*
     * GET: điều hướng màn hình danh sách, form tạo hoặc form sửa mẫu ca.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }
        switch (action) {
            case "create":
                showCreate(req, resp);
                break;
            case "edit":
                showEdit(req, resp);
                break;
            default:
                showList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "";
        }
        switch (action) {
            case "create":
                handleCreate(req, resp);
                break;
            case "edit":
                handleEdit(req, resp);
                break;
            case "delete":
                handleDelete(req, resp);
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/owner/shift-templates");
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Lấy toàn bộ mẫu ca để owner xem, sửa hoặc xóa.
        ShiftTemplateDAO dao = new ShiftTemplateDAO();
        req.setAttribute("templates", dao.findAll());
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }

    private void showCreate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("mode", "create");
        req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
    }

    private void showEdit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // id là templateID của mẫu ca owner chọn sửa trên danh sách.
        int id = parseInt(req.getParameter("id"), 0);
        if (id <= 0) {
            resp.sendRedirect(req.getContextPath() + "/owner/shift-templates");
            return;
        }

        ShiftTemplateDAO dao = new ShiftTemplateDAO();
        ShiftTemplates t = dao.findById(id);
        if (t == null) {
            resp.sendRedirect(req.getContextPath() + "/owner/shift-templates");
            return;
        }
        
        // Đếm số ca đã dùng template này để quyết định có khóa phần giờ hay không.
        int usedCount = dao.countShiftsUsing(id);

        req.setAttribute("template", t);
        req.setAttribute("usedCount", usedCount);
        req.setAttribute("mode", "edit");
        req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Map<String, String> errors = new HashMap<>();
        // Tạo mới nên canEditTimes = true, cho phép validate và lưu cả giờ bắt đầu/kết thúc.
        ShiftTemplates t = bindAndValidate(req, errors, true, 0);
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("template", t);
            req.setAttribute("mode", "create");
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
            return;
        }

        ShiftTemplateDAO dao = new ShiftTemplateDAO();
        int id = dao.insert(t);
        if (id < 0) {
            errors.put("_global", "Không thể tạo template. Vui lòng thử lại.");
            req.setAttribute("errors", errors);
            req.setAttribute("template", t);
            req.setAttribute("mode", "create");
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/owner/shift-templates?msg=created");
    }

    private void handleEdit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Khi sửa, controller phải kiểm tra mẫu ca tồn tại và có đang được dùng không.
        int id = parseInt(req.getParameter("id"), 0);
        if (id <= 0) {
            resp.sendRedirect(req.getContextPath() + "/owner/shift-templates");
            return;
        }

        ShiftTemplateDAO dao = new ShiftTemplateDAO();
        ShiftTemplates existing = dao.findById(id);
        if (existing == null) {
            resp.sendRedirect(req.getContextPath() + "/owner/shift-templates");
            return;
        }

        int used = dao.countShiftsUsing(id);
        Map<String, String> errors = new HashMap<>();

        ShiftTemplates t = bindAndValidate(req, errors, used == 0, id);
        t.setTemplateID(id);

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("template", t);
            req.setAttribute("usedCount", used);
            req.setAttribute("mode", "edit");
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
            return;
        }

        boolean ok;
        if (used == 0) {
            // Chưa dùng ở lịch nào: cho sửa cả tên và giờ.
            ok = dao.updateName(id, t.getShiftName())
                    && dao.updateTimes(id, t.getStartTime(), t.getEndTime());
        } else {
            // Đã dùng ở lịch: chỉ sửa tên, giữ nguyên giờ để không làm lệch dữ liệu lịch cũ.
            ok = dao.updateName(id, t.getShiftName());
            t.setStartTime(existing.getStartTime());
            t.setEndTime(existing.getEndTime());
        }

        if (!ok) {
            errors.put("_global", "Không thể cập nhật.");
            req.setAttribute("errors", errors);
            req.setAttribute("template", t);
            req.setAttribute("usedCount", used);
            req.setAttribute("mode", "edit");
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/owner/shift-templates?msg=updated");
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Chỉ xóa template chưa từng được dùng; nếu đã có ca dùng template thì chặn.
        int id = parseInt(req.getParameter("id"), 0);
        if (id <= 0) {
            resp.sendRedirect(req.getContextPath() + "/owner/shift-templates");
            return;
        }

        ShiftTemplateDAO dao = new ShiftTemplateDAO();
        if (dao.countShiftsUsing(id) > 0) {
            resp.sendRedirect(req.getContextPath() + "/owner/shift-templates?msg=template_in_use");
            return;
        }
        dao.delete(id);
        resp.sendRedirect(req.getContextPath() + "/owner/shift-templates?msg=deleted");
    }

    private ShiftTemplates bindAndValidate(HttpServletRequest req, Map<String, String> errors, boolean canEditTimes, int excludeId) {
        // Gom dữ liệu từ form JSP vào model ShiftTemplates và validate nghiệp vụ.
        String name = trim(req.getParameter("shiftName"));
        String startStr = trim(req.getParameter("startTime"));
        String endStr = trim(req.getParameter("endTime"));

        ShiftTemplates t = new ShiftTemplates();
        t.setShiftName(name);

        if (name == null || name.isBlank()) {
            errors.put("shiftName", "Tên ca không được để trống.");
        } else if (name.length() > 100) {
            errors.put("shiftName", "Tên ca tối đa 100 ký tự.");
        } else if (new ShiftTemplateDAO().isShiftNameExists(name, excludeId)) {
            errors.put("shiftName", "Tên ca đã tồn tại.");
        }

        // Chỉ validate giờ khi tạo mới hoặc khi template chưa được gán cho ca nào.
        if (canEditTimes) {
            LocalTime start = parseLocalTime(startStr);
            LocalTime end = parseLocalTime(endStr);

            // Project chỉ cho phép ca làm trong khung 13:00 - 22:00.
            LocalTime DAY_START = LocalTime.of(13, 0);
            LocalTime NIGHT_START = LocalTime.of(22, 0);

            if (start == null) {
                errors.put("startTime", "Giờ bắt đầu không hợp lệ (HH:mm).");
            }
            if (end == null) {
                errors.put("endTime", "Giờ kết thúc không hợp lệ (HH:mm).");
            }

            if (start != null && end != null) {
                // Giữ lại giờ người dùng đã chọn để khi validate lỗi, JSP vẫn fill lại giá trị sai đó.
                t.setStartTime(Time.valueOf(start));
                t.setEndTime(Time.valueOf(end));

//                if (!end.isAfter(start)) {
//                    errors.put("endTime", "Giờ kết thúc phải sau giờ bắt đầu (ca qua đêm không được hỗ trợ).");
//                } else {
//                    if (start.isBefore(DAY_START)) {
//                        errors.put("startTime", "Giờ bắt đầu không được trước 13:00");
//                    } else if (!start.isBefore(NIGHT_START)) {
//                        errors.put("startTime", "Giờ bắt đầu không được từ 22:00 trở đi — ca làm đêm không được hỗ trợ.");
//                    } else if (end.isAfter(NIGHT_START)) {
//                        errors.put("endTime", "Giờ kết thúc không được sau 22:00 — ca làm đêm không được hỗ trợ.");
//                    }
//                }
            }
        }
        return t;
    }

    private static LocalTime parseLocalTime(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return s.length() == 5 ? LocalTime.parse(s + ":00") : LocalTime.parse(s);
        } catch (Exception e) {
            return null;
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

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
