package controller;

import dal.EmployeeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import model.Employee;
import util.PasswordUtil;
import util.UserRole;

@WebServlet(name = "StaffManagementController", urlPatterns = { "/owner/staff" })
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 2L * 1024 * 1024, maxRequestSize = 5L * 1024 * 1024)
public class StaffManagementController extends HttpServlet {

    private static final String LIST_VIEW = "/views/owner/staff-list.jsp";
    private static final String FORM_VIEW = "/views/owner/staff-form.jsp";
    private static final int PAGE_SIZE = 5;
    private static final String UPLOAD_DIR = "uploads/staff";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,11}$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }
        switch (action) {
            case "create":
                showCreateForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            default:
                showList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        switch (action) {
            case "create":
                handleCreate(request, response);
                break;
            case "edit":
                handleEdit(request, response);
                break;
            case "deactivate":
                handleDeactivate(request, response);
                break;
            case "reactivate":
                handleReactivate(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/owner/staff?action=list");
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String statusStr = request.getParameter("status");
        Integer status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = Integer.parseInt(statusStr);
            } catch (NumberFormatException ignored) {
            }
        }
        int page = parseIntOrDefault(request.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }

        EmployeeDAO dao = new EmployeeDAO();
        int total = dao.countStaff(keyword, status);
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        if (totalPages == 0) {
            totalPages = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }

        List<Employee> staffList = dao.listStaffPaged(keyword, status, page, PAGE_SIZE);

        request.setAttribute("staffList", staffList);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", statusStr);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", total);
        request.getRequestDispatcher(LIST_VIEW).forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("mode", "create");
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Employee e = new Employee();
        e.setRoleID(UserRole.RESTAURANT_STAFF.getRoleID());
        e.setIsActive(1);
        e.setMustChangePassword(1);

        Map<String, String> errors = bindAndValidate(request, e, true, 0);

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("staff", e);
            request.setAttribute("mode", "create");
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
            return;
        }

        String rawPassword = request.getParameter("password");
        e.setPassword(PasswordUtil.hash(rawPassword));

        String imagePath = handleImageUpload(request, 0);
        if (imagePath != null) {
            e.setImage(imagePath);
        }

        EmployeeDAO dao = new EmployeeDAO();
        int newId = dao.insert(e);
        if (newId < 0) {
            errors.put("_global", "Unable to create staff. Please try again.");
            request.setAttribute("errors", errors);
            request.setAttribute("staff", e);
            request.setAttribute("mode", "create");
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/owner/staff?action=list&msg=created");
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntOrDefault(request.getParameter("id"), 0);
        if (id <= 0) {
            response.sendRedirect(request.getContextPath() + "/owner/staff?action=list");
            return;
        }
        EmployeeDAO dao = new EmployeeDAO();
        Employee e = dao.findById(id);
        if (e == null || e.getRoleID() != UserRole.RESTAURANT_STAFF.getRoleID()) {
            response.sendRedirect(request.getContextPath() + "/owner/staff?action=list");
            return;
        }
        request.setAttribute("staff", e);
        request.setAttribute("mode", "edit");
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntOrDefault(request.getParameter("id"), 0);
        if (id <= 0) {
            response.sendRedirect(request.getContextPath() + "/owner/staff?action=list");
            return;
        }
        EmployeeDAO dao = new EmployeeDAO();
        Employee existing = dao.findById(id);
        if (existing == null || existing.getRoleID() != UserRole.RESTAURANT_STAFF.getRoleID()) {
            response.sendRedirect(request.getContextPath() + "/owner/staff?action=list");
            return;
        }

        Employee e = new Employee();
        e.setEmployeeID(id);
        e.setRoleID(existing.getRoleID());
        e.setIsActive(existing.getIsActive());
        e.setPassword(existing.getPassword());
        e.setImage(existing.getImage());
        e.setMustChangePassword(existing.getMustChangePassword());

        Map<String, String> errors = bindAndValidate(request, e, false, id);

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("staff", e);
            request.setAttribute("mode", "edit");
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
            return;
        }

        String imagePath = handleImageUpload(request, id);
        if (imagePath != null) {
            e.setImage(imagePath);
        }

        boolean ok = dao.update(e);
        if (!ok) {
            errors.put("_global", "Unable to update. Please try again.");
            request.setAttribute("errors", errors);
            request.setAttribute("staff", e);
            request.setAttribute("mode", "edit");
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/owner/staff?action=list&msg=updated");
    }

    private void handleDeactivate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = parseIntOrDefault(request.getParameter("id"), 0);
        if (id > 0) {
            new EmployeeDAO().softDelete(id);
        }
        response.sendRedirect(buildListBackUrl(request, "deactivated"));
    }

    private void handleReactivate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = parseIntOrDefault(request.getParameter("id"), 0);
        if (id > 0) {
            new EmployeeDAO().reactivate(id);
        }
        response.sendRedirect(buildListBackUrl(request, "reactivated"));
    }

    private Map<String, String> bindAndValidate(HttpServletRequest request, Employee e, boolean isCreate,
            int excludeId) {
        Map<String, String> errors = new HashMap<>();

        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phoneNumber"));
        String dobStr = trim(request.getParameter("dob"));
        String salaryStr = trim(request.getParameter("salary"));
        String address = trim(request.getParameter("address"));
        String password = request.getParameter("password");

        e.setFullName(fullName);
        e.setEmail(email);
        e.setPhoneNumber(phone);
        e.setAddress(address);

        if (fullName == null || fullName.isBlank()) {
            errors.put("fullName", "Full name is required.");
        } else if (fullName.length() < 2 || fullName.length() > 150) {
            errors.put("fullName", "Full name must be 2-150 characters.");
        }

        EmployeeDAO dao = new EmployeeDAO();
        if (email == null || email.isBlank()) {
            errors.put("email", "Email is required.");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.put("email", "Invalid email format.");
        } else if (dao.isEmailExists(email, excludeId)) {
            errors.put("email", "Email already exists.");
        }

        if (phone == null || phone.isBlank()) {
            errors.put("phoneNumber", "Phone number is required.");
        } else if (!PHONE_PATTERN.matcher(phone).matches()) {
            errors.put("phoneNumber", "Phone number must be 10-11 digits.");
        } else if (dao.isPhoneExists(phone, excludeId)) {
            errors.put("phoneNumber", "Phone number already exists.");
        }

        if (isCreate) {
            if (password == null || password.isBlank()) {
                errors.put("password", "Password is required.");
            } else if (password.length() < 6) {
                errors.put("password", "Password must be at least 6 characters.");
            }
        }

        if (dobStr != null && !dobStr.isBlank()) {
            try {
                LocalDate dob = LocalDate.parse(dobStr);
                if (!dob.isBefore(LocalDate.now())) {
                    errors.put("dob", "Date of birth must be in the past.");
                } else if (dob.isAfter(LocalDate.now().minusYears(18))) {
                    errors.put("dob", "Employee must be at least 18 years old.");
                } else {
                    e.setDob(Date.valueOf(dob));
                }
            } catch (Exception ex) {
                errors.put("dob", "Invalid date of birth.");
            }
        }

        if (salaryStr != null && !salaryStr.isBlank()) {
            try {
                int sal = Integer.parseInt(salaryStr);
                if (sal < 0) {
                    errors.put("salary", "Salary cannot be negative.");
                } else {
                    e.setSalary(sal);
                }
            } catch (NumberFormatException ex) {
                errors.put("salary", "Invalid salary value.");
            }
        }

        if (address != null && address.length() > 255) {
            errors.put("address", "Address must not exceed 255 characters.");
        }

        return errors;
    }

    private String handleImageUpload(HttpServletRequest request, int employeeId) {
        try {
            Part filePart = request.getPart("image");
            if (filePart == null || filePart.getSize() == 0) {
                return null;
            }

            String submitted = filePart.getSubmittedFileName();
            if (submitted == null || submitted.isBlank()) {
                return null;
            }

            String ext = "";
            int dot = submitted.lastIndexOf('.');
            if (dot >= 0) {
                ext = submitted.substring(dot + 1).toLowerCase();
            }
            if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png")) {
                return null;
            }

            String appPath = request.getServletContext().getRealPath("/");
            Path uploadFolder = Paths.get(appPath, UPLOAD_DIR);
            Files.createDirectories(uploadFolder);

            String fileName = "staff_" + employeeId + "_" + System.currentTimeMillis() + "." + ext;
            Path target = uploadFolder.resolve(fileName);
            try (InputStream in = filePart.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return UPLOAD_DIR + "/" + fileName;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static int parseIntOrDefault(String s, int def) {
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

    private String buildListBackUrl(HttpServletRequest request, String msg) {
        StringBuilder url = new StringBuilder(request.getContextPath()).append("/owner/staff?action=list");
        String kw = request.getParameter("keyword");
        String st = request.getParameter("status");
        String pg = request.getParameter("page");
        if (kw != null && !kw.isBlank()) {
            url.append("&keyword=").append(kw);
        }
        if (st != null && !st.isBlank()) {
            url.append("&status=").append(st);
        }
        if (pg != null && !pg.isBlank()) {
            url.append("&page=").append(pg);
        }
        if (msg != null) {
            url.append("&msg=").append(msg);
        }
        return url.toString();
    }
}
