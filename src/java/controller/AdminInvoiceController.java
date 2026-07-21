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
        int offset = (page - 1) * recordsPerPage;

        // 4. TRUY VẤN DỮ LIỆU QUA DAO
        InvoicesDAO invoicesDAO = new InvoicesDAO();
        int totalRecords = invoicesDAO.getTotalFilteredInvoices(startDate, endDate, status);
        int totalPages = (int) Math.ceil((double) totalRecords / recordsPerPage);
        
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
}