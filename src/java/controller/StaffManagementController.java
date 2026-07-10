package controller;

import dal.EmployeeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import model.Employee;
import util.PasswordUtil;
import util.UserRole;

@WebServlet(name = "StaffManagementController", urlPatterns = { "/owner/staff" })
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize       = 50L * 1024 * 1024,
        maxRequestSize    = 55L * 1024 * 1024
)
public class StaffManagementController extends HttpServlet {

    /*
     * NGHIỆP VỤ: Owner quản lý tài khoản nhân viên.
     *
     * Controller này phục vụ màn /owner/staff:
     * - Xem danh sách nhân viên, tìm kiếm, lọc trạng thái, lọc role.
     * - Tạo tài khoản nhân viên mới.
     * - Sửa thông tin nhân viên.
     * - Vô hiệu hóa / kích hoạt lại tài khoản nhân viên.
     *
     * Giới hạn quyền:
     * - Màn này chỉ quản lý role Staff và Receptionist.
     * - Không cho tạo/sửa/vô hiệu hóa tài khoản Owner qua request thủ công.
     *
     * Lưu ý upload:
     * - Ảnh nhân viên được validate đuôi file, content-type và nội dung ảnh thật.
     * - File upload tối đa 2MB dù MultipartConfig cho request lớn hơn để bắt lỗi tốt hơn.
     */
    private static final String LIST_VIEW = "/views/owner/staff-list.jsp";
    private static final String FORM_VIEW = "/views/owner/staff-form.jsp";
    private static final int PAGE_SIZE = 10;
    private static final String UPLOAD_DIR = "uploads/staff";
    private static final long MAX_PROFILE_IMAGE_SIZE = 2L * 1024 * 1024;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,10}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,11}$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // GET chỉ điều hướng màn hình: danh sách, form tạo mới hoặc form chỉnh sửa.
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
        try {
            // Với multipart/form-data, action đôi khi nằm trên query string nên dùng getAction().
            String action = getAction(request);
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
        } catch (IllegalStateException ex) {
            showUploadError(request, response);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Nhận bộ lọc từ JSP: keyword, status, role và page hiện tại.
        String keyword = request.getParameter("keyword");
        String statusStr = request.getParameter("status");
        String roleStr = request.getParameter("role");

        Integer status = null;

        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = Integer.parseInt(statusStr);
            } catch (NumberFormatException ignored) {
            }
        }

        // Chỉ chấp nhận lọc theo role nhân viên được owner quản lý: Staff hoặc Receptionist.
        Integer roleID = null;
        if (roleStr != null && !roleStr.isBlank()) {
            int parsedRole = parseIntOrDefault(roleStr, 0);
            if (isManagedStaffRole(parsedRole)) {
                roleID = parsedRole;
            } else {
                roleStr = "";
            }
        }

        int page = parseIntOrDefault(request.getParameter("page"), 1);

        if (page < 1) {
            page = 1;
        }

        EmployeeDAO dao = new EmployeeDAO();

        // Đếm tổng bản ghi trước để tính phân trang, sau đó lấy đúng trang cần hiển thị.
        int total = dao.countStaff(keyword, status, roleID);
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);

        if (totalPages == 0) {
            totalPages = 1;
        }

        if (page > totalPages) {
            page = totalPages;
        }

        List<Employee> staffList = dao.listStaffPaged(
                keyword, status, roleID, page, PAGE_SIZE);

        request.setAttribute("staffList", staffList);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", statusStr);
        request.setAttribute("role", roleStr);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", PAGE_SIZE);
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

        // Role mặc định chỉ dùng để fill form ban đầu.
        // Role lưu thật sẽ được lấy từ roleID của form trong bindAndValidate()
        // và có thể là Staff (2) hoặc Lễ tân/Receptionist (3).
        e.setRoleID(UserRole.RESTAURANT_STAFF.getRoleID());
        e.setIsActive(1);
        e.setMustChangePassword(1);

        // Bind dữ liệu text trước; nếu sai thì trả lại form cùng errors.
        Map<String, String> errors = bindAndValidate(request, e, true, 0);

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("staff", e);
            request.setAttribute("mode", "create");
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
            return;
        }

        String rawPassword = request.getParameter("password");
        // Mật khẩu lưu DB là hash SHA-256 + salt, không lưu raw password.
        e.setPassword(PasswordUtil.hash(rawPassword));

        // Ảnh là optional; nếu có upload thì validate và lưu file.
        String imagePath = handleImageUpload(request, 0, errors);

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("staff", e);
            request.setAttribute("mode", "create");
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
            return;
        }

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
        // id là employeeID của nhân viên owner chọn sửa.
        int id = parseIntOrDefault(request.getParameter("id"), 0);

        if (id <= 0) {
            response.sendRedirect(request.getContextPath() + "/owner/staff?action=list");
            return;
        }

        EmployeeDAO dao = new EmployeeDAO();
        Employee e = dao.findById(id);

        if (e == null || !isManagedStaffRole(e.getRoleID())) {
            response.sendRedirect(request.getContextPath() + "/owner/staff?action=list");
            return;
        }

        request.setAttribute("staff", e);
        request.setAttribute("mode", "edit");
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Khi sửa không update password tại đây; password giữ nguyên.
        int id = parseIntOrDefault(request.getParameter("id"), 0);

        if (id <= 0) {
            response.sendRedirect(request.getContextPath() + "/owner/staff?action=list");
            return;
        }

        EmployeeDAO dao = new EmployeeDAO();
        Employee existing = dao.findById(id);

        if (existing == null || !isManagedStaffRole(existing.getRoleID())) {
            response.sendRedirect(request.getContextPath() + "/owner/staff?action=list");
            return;
        }

        Employee e = new Employee();

        // Copy các trường hệ thống từ bản ghi cũ để tránh form sửa ghi đè sai.
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

        String imagePath = handleImageUpload(request, id, errors);

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("staff", e);
            request.setAttribute("mode", "edit");
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
            return;
        }

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
        // Vô hiệu hóa là soft delete: đổi isActive, không xóa record để giữ lịch/sổ sử.
        int id = parseIntOrDefault(request.getParameter("id"), 0);

        if (id > 0) {
            EmployeeDAO dao = new EmployeeDAO();
            Employee target = dao.findById(id);
            // [PHAN QUYEN NHAN SU] Chan request gia mao khoa tai khoan Owner.
            if (target != null && isManagedStaffRole(target.getRoleID())) {
                dao.softDelete(id);
            }
        }

        response.sendRedirect(buildListBackUrl(request, "deactivated"));
    }

    private void handleReactivate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Kích hoạt lại tài khoản đã bị vô hiệu hóa.
        int id = parseIntOrDefault(request.getParameter("id"), 0);

        if (id > 0) {
            EmployeeDAO dao = new EmployeeDAO();
            Employee target = dao.findById(id);
            // [PHAN QUYEN NHAN SU] Chi mo lai tai khoan role 2/3.
            if (target != null && isManagedStaffRole(target.getRoleID())) {
                dao.reactivate(id);
            }
        }

        response.sendRedirect(buildListBackUrl(request, "reactivated"));
    }

    private Map<String, String> bindAndValidate(
            HttpServletRequest request,
            Employee e,
            boolean isCreate,
            int excludeId) {
        // Method này gom dữ liệu form vào Employee và trả về map lỗi theo từng field.
        Map<String, String> errors = new HashMap<>();

        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phoneNumber"));
        String dobStr = trim(request.getParameter("dob"));
        String address = trim(request.getParameter("address"));
        String password = request.getParameter("password");
        String roleIDStr = trim(request.getParameter("roleID"));

        // Form chỉ được gán Staff hoặc Receptionist; chặn cả request tự sửa roleID.
        int selectedRoleID = parseIntOrDefault(roleIDStr, 0);
        if (!isManagedStaffRole(selectedRoleID)) {
            errors.put("roleID", "Please select Staff or Receptionist.");
        } else {
            e.setRoleID(selectedRoleID);
        }

        request.setAttribute("dobValue", dobStr);

        e.setFullName(fullName);
        e.setEmail(email);
        e.setPhoneNumber(phone);
        e.setAddress(address);

        EmployeeDAO dao = new EmployeeDAO();

        // Validate dữ liệu định danh và kiểm tra trùng email/số điện thoại trong DB.
        if (fullName == null || fullName.isBlank()) {
            errors.put("fullName", "Full name is required.");
        } else if (fullName.length() < 2 || fullName.length() > 50) {
            errors.put("fullName", "Full name must be 2-50 characters.");
        }

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
            errors.put("phoneNumber", "Invalid phone number format. Phone number must be 10-11 digits.");
        } else if (dao.isPhoneExists(phone, excludeId)) {
            errors.put("phoneNumber", "Phone number already exists.");
        }

        if (isCreate) {
            if (password == null || password.isBlank()) {
                errors.put("password", "Password is required.");
            } else if (password.length() < 6 || password.length() > 50) {
                errors.put("password", "Password must be 6-50 characters.");
            }
        }

        if (dobStr == null || dobStr.isBlank()) {
            if (isCreate) {
                errors.put("dob", "Date of birth is required.");
            }
        } else {
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

        if (address == null || address.isBlank()) {
            if (isCreate) {
                errors.put("address", "Address is required.");
            }
        } else if (address.length() > 255) {
            errors.put("address", "Address must not exceed 255 characters.");
        }

        return errors;
    }

    private boolean isValidImageFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".png");
    }

    private String handleImageUpload(HttpServletRequest request, int employeeId, Map<String, String> errors) {
        try {
            // Input name="image" đến từ staff-form.jsp; không chọn ảnh thì bỏ qua.
            Part filePart = request.getPart("image");

            if (filePart == null || filePart.getSize() == 0) {
                return null;
            }

            if (filePart.getSize() > MAX_PROFILE_IMAGE_SIZE) {
                errors.put("image", "Ảnh nhân viên không được vượt quá 2MB.");
                return null;
            }

            //Lấy tên file gốc mà user upload
            String submitted = filePart.getSubmittedFileName();

            if (submitted == null || submitted.isBlank()) {
                return null;
            }
            //Chỉ lấy tên file
            String safeSubmitted = Paths.get(submitted).getFileName().toString();

            // Chặn file không phải jpg/jpeg/png ngay từ tên file.
            if (!isValidImageFile(safeSubmitted)) {
                errors.put("image", "Vui lòng chọn file ảnh (jpg, jpeg, png).");
                return null;
            }
            
            //Lấy kiểu file trình duyệt gửi lên
            String contentType = filePart.getContentType();
            //Ktra content type phải là ảnh
            if (contentType == null
                    || (!contentType.equals("image/jpeg")
                        && !contentType.equals("image/png"))) {
                errors.put("image", "File không hợp lệ. Chỉ chấp nhận ảnh JPG, PNG.");
                return null;
            }
            
            if (!isValidImageContent(filePart)) {
                errors.put("image", "File không hợp lệ. Vui lòng chọn file ảnh thật JPG hoặc PNG.");
                return null;
            }

            //Lấy phần đuôi
            String ext = safeSubmitted.substring(safeSubmitted.lastIndexOf('.') + 1).toLowerCase();
            String fileName = "staff_" + System.currentTimeMillis() + "." + ext; //Tạo tên file để tránh trùng

            // Hiện tại ảnh được lưu vào thư mục deploy của web app theo UPLOAD_DIR.
            String runtimePath = request.getServletContext().getRealPath("/");
            Path uploadFolder = Paths.get(runtimePath, UPLOAD_DIR);
            Files.createDirectories(uploadFolder);
            Path target = uploadFolder.resolve(fileName);

            //Đọc dữ liệu ảnh upload và copy và thư mục server
            try (InputStream in = filePart.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            return UPLOAD_DIR + "/" + fileName; //trả về đường dẫn lưu vào DB

        } catch (Exception ex) {
            ex.printStackTrace();
            errors.put("image", "Không thể upload ảnh. Vui lòng thử lại.");
            return null;
        }
    }

    private boolean isValidImageContent(Part imagePart) throws IOException {
        // ImageIO đọc được nghĩa là file có nội dung ảnh thật, không chỉ đổi đuôi giả.
        if (imagePart == null || imagePart.getSize() == 0) {
            return false;
        }

        try (InputStream input = imagePart.getInputStream()) {
            return ImageIO.read(input) != null; 
            //Nếu file ko phải ảnh thật thì đọc ko được
        }
    }

    private String getAction(HttpServletRequest request) {
        // Multipart request có thể khiến request.getParameter("action") không đủ tin cậy.
        // Vì vậy ưu tiên lấy action từ query string trước, rồi mới lấy từ body.
        String action = getQueryParameter(request, "action");
        if (action != null && !action.isBlank()) {
            return action;
        }
        return request.getParameter("action");
    }

    private String getQueryParameter(HttpServletRequest request, String name) {
        String query = request.getQueryString();
        if (query == null || query.isBlank()) {
            return null;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            if (name.equals(key)) {
                return parts.length > 1
                        ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                        : "";
            }
        }
        return null;
    }

    private void showUploadError(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Bắt lỗi file quá lớn từ MultipartConfig và trả về đúng form đang thao tác.
        Map<String, String> errors = new HashMap<>();
        errors.put("image", "File upload quá lớn hoặc không hợp lệ. Vui lòng chọn ảnh JPG/PNG tối đa 2MB.");

        String action = getQueryParameter(request, "action");
        if ("edit".equals(action)) {
            int id = parseIntOrDefault(getQueryParameter(request, "id"), 0);
            Employee staff = id > 0 ? new EmployeeDAO().findById(id) : null;
            if (staff == null) {
                response.sendRedirect(request.getContextPath() + "/owner/staff?action=list");
                return;
            }
            request.setAttribute("staff", staff);
            request.setAttribute("mode", "edit");
        } else {
            Employee staff = new Employee();
            request.setAttribute("staff", staff);
            request.setAttribute("mode", "create");
        }

        request.setAttribute("errors", errors);
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
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

    /**
     * Man hinh nay chi quan ly role 2 va role 3,
     * tuyet doi khong tao/chinh sua tai khoan Owner.
     */
    private static boolean isManagedStaffRole(int roleID) {
        return roleID == UserRole.RESTAURANT_STAFF.getRoleID()
                || roleID == UserRole.RECEPTIONIST.getRoleID();
    }

    private String buildListBackUrl(HttpServletRequest request, String msg) {
        StringBuilder url = new StringBuilder(request.getContextPath())
                .append("/owner/staff?action=list");

        String kw = request.getParameter("keyword");
        String st = request.getParameter("status");
        String role = request.getParameter("role");
        String pg = request.getParameter("page");

        if (kw != null && !kw.isBlank()) {
            url.append("&keyword=").append(kw);
        }

        if (st != null && !st.isBlank()) {
            url.append("&status=").append(st);
        }

        // [LOC THEO ROLE] Giu bo loc sau khi khoa/mo khoa tai khoan.
        if (role != null && !role.isBlank()) {
            url.append("&role=").append(role);
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
