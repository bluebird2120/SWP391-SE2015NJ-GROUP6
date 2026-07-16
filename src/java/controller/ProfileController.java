package controller;

import dal.CustomerDAO;
import dal.EmployeeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import model.Customer;
import model.Employee;

@WebServlet(name = "ProfileController", urlPatterns = {"/profile"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, //Ngưỡng phân loại < lưu vào RAM, > lưu vào ổ cứng
        maxFileSize = 5L * 1024 * 1024, //Kích thước tối đa ảnh
        maxRequestSize = 6L * 1024 * 1024 //Kích thước tối đa bao gồm ảnh, tên, địa chỉ của 1 request
)
public class ProfileController extends HttpServlet {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null
                || (session.getAttribute("customer") == null
                && session.getAttribute("employee") == null)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String successMessage = (String) session.getAttribute("successMessage");
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }

        request.getRequestDispatcher("/views/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Customer customer = (Customer) session.getAttribute("customer");
        Employee employee = (Employee) session.getAttribute("employee");

        if (customer != null) {
            handleCustomerUpdate(request, response, session, customer);
        } else if (employee != null) {
            handleEmployeeUpdate(request, response, session, employee);
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    // ── Customer: sửa userName (bắt buộc) + dob, address, image (tùy chọn) ──
    private void handleCustomerUpdate(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, Customer customer) throws ServletException, IOException {

        String userName = trim(request.getParameter("userName"));
        String addressParam = trim(request.getParameter("address"));
        String dobParam = trim(request.getParameter("dob"));
        boolean removeImage = "true".equals(request.getParameter("removeImage"));
        Map<String, String> errors = new HashMap<>();

        if (userName.isEmpty()) {
            errors.put("userName", "Vui lòng nhập tên hiển thị.");
        } else if (userName.length() < 2 || userName.length() > 50) {
            errors.put("userName", "Tên hiển thị phải từ 2-50 ký tự.");
        } else if (customerDAO.isUserNameExists(userName, customer.getCustomerID())) {
            errors.put("userName", "Tên này đã được sử dụng.");
        }

        if (addressParam.length() > 255) {
            errors.put("address", "Địa chỉ không được vượt quá 255 ký tự.");
        }

        java.sql.Date dob = null;
        if (!dobParam.isEmpty()) {
            try {
                java.time.LocalDate parsed = java.time.LocalDate.parse(dobParam);
                if (parsed.isAfter(java.time.LocalDate.now())) {
                    errors.put("dob", "Ngày sinh không được ở tương lai.");
                } else {
                    dob = java.sql.Date.valueOf(parsed);
                }
            } catch (Exception ex) {
                errors.put("dob", "Ngày sinh không hợp lệ.");
            }
        }

        String imagePath = null;
        if (!removeImage) {
            imagePath = handleImageUpload(request, errors, "customer", "customer");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("editMode", true);
            request.getRequestDispatcher("/views/profile.jsp").forward(request, response);
            return;
        }

        customer.setUserName(userName);
        customer.setAddress(addressParam.isEmpty() ? null : addressParam);
        customer.setDob(dob);

        if (removeImage) {
            deleteOldImageFile(request, customer.getImage());
            customer.setImage(null);
        } else if (imagePath != null) {
            deleteOldImageFile(request, customer.getImage());
            customer.setImage(imagePath);
        }

        boolean ok = customerDAO.updateProfile(customer);
        if (!ok) {
            errors.put("_global", "Không thể cập nhật. Vui lòng thử lại.");
            request.setAttribute("errors", errors);
            request.setAttribute("editMode", true);
            request.getRequestDispatcher("/views/profile.jsp").forward(request, response);
            return;
        }

        session.setAttribute("customer", customer);
        session.setAttribute("successMessage", "Cập nhật hồ sơ thành công.");
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    // ── Employee: sửa fullName, address, image ──────────────────────────
    private void handleEmployeeUpdate(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, Employee employee) throws ServletException, IOException {

        String fullName = trim(request.getParameter("fullName"));
        String address = trim(request.getParameter("address"));
        boolean removeImage = "true".equals(request.getParameter("removeImage"));
        Map<String, String> errors = new HashMap<>();

        if (fullName.isEmpty()) {
            errors.put("fullName", "Vui lòng nhập họ tên.");
        } else if (fullName.length() < 2 || fullName.length() > 50) {
            errors.put("fullName", "Họ tên phải từ 2 đến 50 ký tự.");
        }

        if (address.length() > 255) {
            errors.put("address", "Địa chỉ không được vượt quá 255 ký tự.");
        }

        String imagePath = null;

        if (!removeImage) {
            imagePath = handleImageUpload(request, errors, "staff", "staff");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("editMode", true);
            request.getRequestDispatcher("/views/profile.jsp").forward(request, response);
            return;
        }

        employee.setFullName(fullName);
        employee.setAddress(address.isEmpty() ? null : address);

        if (removeImage) {
            // Xóa ảnh khỏi server
            deleteOldImageFile(request, employee.getImage());
            // Xóa ảnh trong DB
            employee.setImage(null);
        } else if (imagePath != null) {
            // Nếu upload ảnh mới thì xóa ảnh cũ trước
            deleteOldImageFile(request, employee.getImage());
            employee.setImage(imagePath);
        }

        boolean ok = employeeDAO.update(employee);
        if (!ok) {
            errors.put("_global", "Không thể cập nhật. Vui lòng thử lại.");
            request.setAttribute("errors", errors);
            request.setAttribute("editMode", true);
            request.getRequestDispatcher("/views/profile.jsp").forward(request, response);
            return;
        }

        session.setAttribute("employee", employee);
        session.setAttribute("successMessage", "Cập nhật hồ sơ thành công.");
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private String handleImageUpload(HttpServletRequest request, Map<String, String> errors,
            String subFolder, String filePrefix) {
        try {
            Part filePart = request.getPart("image");
            if (filePart == null || filePart.getSize() == 0) {
                return null;
            }

            //Kiểm tra kích thước
            if (filePart.getSize() > 2L * 1024 * 1024) {
                errors.put("image", "Ảnh không vượt quá 2MB.");
                return null;
            }

            //Kiểm tra danh tính file "anh.png"
            String submitted = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            if (submitted.isBlank()) {
                return null;
            }

            // Đọc 12 byte đầu để check magic bytes
            byte[] header = new byte[12];
            //đọc dữ liệu nhị phân trong file
            try (InputStream in = filePart.getInputStream()) {
                //lấy ra 12 byte đầy tiên trong file
                int bytesRead = in.read(header);
                if (bytesRead < 4) {
                    errors.put("image", "File không hợp lệ.");
                    return null;
                }
            }

            // Xác định loại file thật sự qua magic bytes
            String detectedType = detectImageType(header);
            if (detectedType == null) {
                errors.put("image", "File không phải ảnh hợp lệ (jpg, jpeg, png, webp).");
                return null;
            }

            // Tên file dùng extension thật sự từ magic bytes (không tin đuôi file user đặt)
            String fileName = filePrefix + "_" + System.currentTimeMillis() + "." + detectedType;
            Path uploadFolder = Paths.get(request.getServletContext().getRealPath("/"), "uploads/" + subFolder);
            Files.createDirectories(uploadFolder);

            // Upload lại từ đầu (vì đã đọc stream rồi, cần đọc lại)
            try (InputStream in2 = filePart.getInputStream()) {
                //Ném ảnh vô folder đã tạo
                Files.copy(in2, uploadFolder.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }

            return "uploads/" + subFolder + "/" + fileName;

        } catch (Exception ex) {
            ex.printStackTrace();
            errors.put("image", "Không thể upload ảnh. Vui lòng thử lại.");
            return null;
        }
    }

    /**
     * Check magic bytes để xác định loại file thật sự. Trả về "jpg", "png",
     * "webp" hoặc null nếu không phải ảnh hợp lệ.
     */
    private String detectImageType(byte[] header) {
        // JPEG: FF D8 FF
        if (header[0] == (byte) 0xFF
                && header[1] == (byte) 0xD8
                && header[2] == (byte) 0xFF) {
            return "jpg";
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (header[0] == (byte) 0x89
                && header[1] == (byte) 0x50
                && header[2] == (byte) 0x4E
                && header[3] == (byte) 0x47) {
            return "png";
        }
        // WebP: 52 49 46 46 ?? ?? ?? ?? 57 45 42 50    //?? tùy ảnh nặng nhẹ khác nhau
        if (header[0] == (byte) 0x52 && header[1] == (byte) 0x49
                && header[2] == (byte) 0x46 && header[3] == (byte) 0x46
                && header[8] == (byte) 0x57 && header[9] == (byte) 0x45
                && header[10] == (byte) 0x42 && header[11] == (byte) 0x50) {
            return "webp";
        }
        // Không phải ảnh hợp lệ
        return null;
    }

    private void deleteOldImageFile(HttpServletRequest request, String imagePath) {

        if (imagePath == null || imagePath.isBlank()) {
            return;
        }

        try {

            String absolutePath = request.getServletContext().getRealPath("/") + imagePath;

            File file = new File(absolutePath);

            if (file.exists()) {
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}