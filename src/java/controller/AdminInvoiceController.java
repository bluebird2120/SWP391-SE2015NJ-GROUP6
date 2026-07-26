package controller;

import dal.InvoicesDAO;
import model.Employee;
import model.Invoices;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;

@WebServlet(name = "AdminInvoiceController", urlPatterns = {"/owner/invoices"})
public class AdminInvoiceController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        // 1. KIỂM TRA PHÂN QUYỀN (CHỈ CHO PHÉP NHÂN VIÊN QUẢN TRỊ/ADMIN TRUY CẬP)
        HttpSession session = request.getSession();
        Employee employee = (Employee) session.getAttribute("employee");
        
        // Đảm bảo employee tồn tại và có roleID = 1 (Ví dụ quy ước của bạn là Admin)
        if (employee == null || employee.getRoleID() != 1) {
            session.setAttribute("errorMsg", "Bạn không có quyền truy cập khu vực quản lý hóa đơn!");
            response.sendRedirect(request.getContextPath() + "/login?type=employee");
            return;
        }

        // 2. LẤY THAM SỐ BỘ LỌC ĐƯỢC GỬI LÊN TỪ GIAO DIỆN
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String status = request.getParameter("status");

        // [INVOICE FILTER VALIDATION] Chỉ nhận ngày ISO và trạng thái có trong UI.
        if (status == null || status.isBlank()) {
            status = "all";
        }
        if (!Set.of("all", "paid", "unpaid").contains(status)) {
            request.setAttribute("errorMessage", "Trạng thái hóa đơn không hợp lệ.");
            status = "all";
        }
        LocalDate parsedStart = parseDate(startDate);
        LocalDate parsedEnd = parseDate(endDate);
        if ((startDate != null && !startDate.isBlank() && parsedStart == null)
                || (endDate != null && !endDate.isBlank() && parsedEnd == null)) {
            request.setAttribute("errorMessage", "Ngày lọc hóa đơn không hợp lệ.");
            startDate = null;
            endDate = null;
        } else if (parsedStart != null && parsedEnd != null
                && parsedStart.isAfter(parsedEnd)) {
            request.setAttribute("errorMessage",
                    "Ngày bắt đầu không được sau ngày kết thúc.");
            startDate = null;
            endDate = null;
        }

        // 3. XỬ LÝ LOGIC PHÂN TRANG
        int page = 1;
        int recordsPerPage = 10; // Quy định hiển thị tối đa 10 hóa đơn trên một trang
        
        if (request.getParameter("page") != null) {
            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        if (page < 1) {
            page = 1;
        }

        // 4. TRUY VẤN DỮ LIỆU QUA DAO
        InvoicesDAO invoicesDAO = new InvoicesDAO();
        int totalRecords = invoicesDAO.getTotalFilteredInvoices(startDate, endDate, status);
        int totalPages = (int) Math.ceil((double) totalRecords / recordsPerPage);
        // [PAGINATION FIX] Không tạo offset âm hoặc trang vượt quá kết quả.
        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }
        int offset = (page - 1) * recordsPerPage;
        
        List<Invoices> listInvoices = invoicesDAO.getFilteredInvoices(startDate, endDate, status, offset, recordsPerPage);

        // 5. GẮN TOÀN BỘ THUỘC TÍNH ĐỂ TRUYỀN XUỐNG FILE JSP HIỂN THỊ
        request.setAttribute("listInvoices", listInvoices);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("status", status);
        
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", totalRecords);
        
        request.getRequestDispatcher("/views/owner/invoices.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
