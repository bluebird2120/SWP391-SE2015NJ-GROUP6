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

    private static final String LIST_VIEW = "/views/owner/shift-template-list.jsp";
    private static final String FORM_VIEW = "/views/owner/shift-template-form.jsp";

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

        int usedCount = dao.countShiftsUsing(id);

        req.setAttribute("template", t);
        req.setAttribute("usedCount", usedCount);
        req.setAttribute("mode", "edit");
        req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Map<String, String> errors = new HashMap<>();
        ShiftTemplates t = bindAndValidate(req, errors, true);
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

        ShiftTemplates t = bindAndValidate(req, errors, used == 0);
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
            ok = dao.updateName(id, t.getShiftName())
                    && dao.updateTimes(id, t.getStartTime(), t.getEndTime());
        } else {
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

    private ShiftTemplates bindAndValidate(HttpServletRequest req, Map<String, String> errors, boolean canEditTimes) {
        String name = trim(req.getParameter("shiftName"));
        String startStr = trim(req.getParameter("startTime"));
        String endStr = trim(req.getParameter("endTime"));

        ShiftTemplates t = new ShiftTemplates();
        t.setShiftName(name);

        if (name == null || name.isBlank()) {
            errors.put("shiftName", "Tên ca không được để trống.");
        } else if (name.length() > 100) {
            errors.put("shiftName", "Tên ca tối đa 100 ký tự.");
        }

        //Code ĐB
        if (canEditTimes) {
            LocalTime start = parseLocalTime(startStr);
            LocalTime end = parseLocalTime(endStr);

            LocalTime DAY_START = LocalTime.of(6, 0);
            LocalTime NIGHT_START = LocalTime.of(22, 0);

            if (start == null) {
                errors.put("startTime", "Giờ bắt đầu không hợp lệ (HH:mm).");
            }
            if (end == null) {
                errors.put("endTime", "Giờ kết thúc không hợp lệ (HH:mm).");
            }

            if (start != null && end != null) {
                if (!end.isAfter(start)) {
                    errors.put("endTime", "Giờ kết thúc phải sau giờ bắt đầu (ca qua đêm không được hỗ trợ).");
                } else {
                    if (start.isBefore(DAY_START)) {
                        errors.put("startTime", "Giờ bắt đầu không được trước 06:00 — ca làm đêm không được hỗ trợ.");
                    } else if (!start.isBefore(NIGHT_START)) {
                        errors.put("startTime", "Giờ bắt đầu không được từ 22:00 trở đi — ca làm đêm không được hỗ trợ.");
                    } else if (end.isAfter(NIGHT_START)) {
                        errors.put("endTime", "Giờ kết thúc không được sau 22:00 — ca làm đêm không được hỗ trợ.");
                    } else {
                        t.setStartTime(Time.valueOf(start));
                        t.setEndTime(Time.valueOf(end));
                    }
                }
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
