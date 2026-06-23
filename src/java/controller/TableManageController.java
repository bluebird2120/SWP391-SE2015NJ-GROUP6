package controller;

import dal.TableDAO;
import model.Employee;
import model.Table;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "TableManageController", urlPatterns = {"/manage-table"})
public class TableManageController extends HttpServlet {

    private final TableDAO tableDAO = new TableDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. KIỂM TRA ĐĂNG NHẬP (Authentication)
        HttpSession session = request.getSession();
        Employee loginUser = (Employee) session.getAttribute("employee");

        if (loginUser == null) {
            // Chưa đăng nhập thì đá văng ra trang login
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int roleID = loginUser.getRoleID(); // 1: Owner, 2: Employee
        String action = request.getParameter("action");
        if (action == null) {
            action = "list"; // Mặc định là xem danh sách
        }

        // 2. ĐIỀU HƯỚNG THEO ACTION VÀ KIỂM TRA QUYỀN (Authorization)
        switch (action) {
            case "add":
                if (roleID != 1) { // KHÔNG PHẢI OWNER
                    response.sendRedirect("manage-table?error=unauthorized");
                    return;
                }
                request.setAttribute("mode", "add"); // Truyền cờ trạng thái "Thêm"
                request.getRequestDispatcher("/views/table/table_form.jsp").forward(request, response);
                break;

            case "edit":
                if (roleID != 1) { // KHÔNG PHẢI OWNER
                    response.sendRedirect("manage-table?error=unauthorized");
                    return;
                }
                int editId = Integer.parseInt(request.getParameter("id"));
                Table tableToEdit = tableDAO.getTableByTableID(editId);
                request.setAttribute("table", tableToEdit);
                request.setAttribute("mode", "edit"); // Truyền cờ trạng thái "Sửa"
                request.getRequestDispatcher("/views/table/table_form.jsp").forward(request, response);
                break;

            case "detail":
                int detailId = Integer.parseInt(request.getParameter("id"));
                Table tableDetail = tableDAO.getTableByTableID(detailId);
                request.setAttribute("table", tableDetail);
                request.setAttribute("mode", "detail"); // Truyền cờ trạng thái "Chi tiết"

                // ĐÃ SỬA: Không dùng table_detail.jsp nữa, trỏ chung về table_form.jsp
                request.getRequestDispatcher("/views/table/table_form.jsp").forward(request, response);
                break;

            case "list":
            default:
                // 1. Đọc các tham số lọc từ URL gửi lên
                String searchName = request.getParameter("searchName");
                searchName = (searchName != null) ? searchName.trim() : "";

                String capParam = request.getParameter("searchCapacity");
                Integer searchCapacity = (capParam != null && !capParam.isEmpty() && !"all".equals(capParam)) 
                        ? Integer.parseInt(capParam) : null;

                String searchArea = request.getParameter("searchArea");
                if ("all".equals(searchArea)) {
                    searchArea = null;
                }

                String statusParam = request.getParameter("searchStatus");
                Integer searchStatus = (statusParam != null && !statusParam.isEmpty() && !"all".equals(statusParam)) 
                        ? Integer.parseInt(statusParam) : null;

                // 2. BACKEND VALIDATION: Chặn tìm kiếm chuỗi quá dài hoặc chứa ký tự độc hại
                if (searchName.length() > 30) {
                    request.setAttribute("errorMessage", "Từ khóa tìm kiếm không được vượt quá 30 ký tự!");
                    searchName = ""; // Reset bộ lọc tên nếu vi phạm quy định
                }

                // =========================================================
                // --- BẮT ĐẦU: XỬ LÝ PHÂN TRANG (CÓ KÈM THEO BỘ LỌC) ---
                // =========================================================
                final int PAGE_SIZE = 10; // Bạn có thể đổi thành 10 bàn trên 1 trang tùy ý
                String page_raw = request.getParameter("page");
                int page = (page_raw != null && !page_raw.trim().isEmpty()) ? Integer.parseInt(page_raw) : 1;

                // 3.1. Đếm tổng số bàn thỏa mãn điều kiện lọc
                int totalItem = tableDAO.countSearchTables(searchName, searchCapacity, searchArea, searchStatus);
                int totalPage = (int) Math.ceil((double) totalItem / PAGE_SIZE);

                if (page > totalPage && totalPage > 0) {
                    page = totalPage;
                }

                int offSet = (page - 1) * PAGE_SIZE;

                // 3.2. Gọi hàm DAO để truy vấn dữ liệu đã lọc và cắt trang
                List<Table> list = tableDAO.searchTablesPaging(searchName, searchCapacity, searchArea, searchStatus, offSet, PAGE_SIZE);
                // =========================================================
                // --- KẾT THÚC: XỬ LÝ PHÂN TRANG ---
                // =========================================================
                
                // 4. Đẩy ngược các giá trị đã chọn ra Request để giữ trạng thái Form sau khi F5
                request.setAttribute("searchName", searchName);
                request.setAttribute("searchCapacity", request.getParameter("searchCapacity"));
                request.setAttribute("searchArea", request.getParameter("searchArea"));
                request.setAttribute("searchStatus", request.getParameter("searchStatus"));
                
                // Đẩy thông tin phân trang sang JSP
                request.setAttribute("tableList", list);
                request.setAttribute("totalPage", totalPage);
                request.setAttribute("currentPage", page);
                request.setAttribute("userRole", roleID); 
                
                request.getRequestDispatcher("/views/table/table_list.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Thiết lập tiếng Việt cho form nhập liệu
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        Employee loginUser = (Employee) session.getAttribute("employee");

        // BẢO MẬT CẤP 2: Chặn ngay nếu nhân viên (Role != 1) cố tình gửi POST request bằng tool
        if (loginUser == null || loginUser.getRoleID() != 1) {
            response.sendRedirect("manage-table?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");

        // 1. LẤY DỮ LIỆU TỪ FORM VÀ LÀM SẠCH (TRIM)
        String tableName = request.getParameter("tableName");
        tableName = (tableName != null) ? tableName.trim() : "";

        int capacity = 2; // Giá trị mặc định
        try {
            capacity = Integer.parseInt(request.getParameter("capacity"));
        } catch (NumberFormatException e) {
            capacity = 2;
        }

        String areaType = request.getParameter("areaType");
        int isActive = Integer.parseInt(request.getParameter("isActive"));

        int tableID = 0;
        String tableIdStr = request.getParameter("tableID");
        if (tableIdStr != null && !tableIdStr.isEmpty()) {
            tableID = Integer.parseInt(tableIdStr);
        }

        // 2. VALIDATE DỮ LIỆU (BACKEND VALIDATION)
        boolean hasError = false;
        String errorMessage = "";

        if (tableName.isEmpty() || tableName.length() > 30) {
            hasError = true;
            errorMessage = "Tên bàn không được để trống và tối đa chỉ 30 ký tự.";
        } else if (capacity != 2 && capacity != 4 && capacity != 6 && capacity != 8 && capacity != 10) {
            hasError = true;
            errorMessage = "Sức chứa không hợp lệ! Chỉ được chọn 2, 4, 6, 8, hoặc 10 người.";
        }

        // 3. NẾU CÓ LỖI: TRẢ VỀ FORM CŨ KÈM DỮ LIỆU ĐÃ NHẬP
        if (hasError) {
            // Tạo một object tạm thời chứa đúng những gì user vừa gõ để fill lại vào thẻ input
            Table tempTable = new Table();
            tempTable.setTableID(tableID);
            tempTable.setTableName(tableName);
            tempTable.setCapacity(capacity);
            tempTable.setAreaType(areaType);
            tempTable.setIsActive(isActive);

            request.setAttribute("table", tempTable); // Giữ lại dữ liệu
            request.setAttribute("errorMessage", errorMessage); // Truyền câu báo lỗi
            request.setAttribute("mode", action); // Giữ nguyên trạng thái đang là add hay edit

            // Forward lại về trang form (không dùng sendRedirect)
            request.getRequestDispatcher("/views/table/table_form.jsp").forward(request, response);
            return; // Ngắt luồng không cho chạy xuống code DB bên dưới
        }

        // 4. NẾU KHÔNG CÓ LỖI: LƯU VÀO DATABASE
        Table t = new Table();
        t.setTableID(tableID);
        t.setEmployeeID(0);
        t.setTableName(tableName);
        t.setCapacity(capacity);
        t.setAreaType(areaType);
        t.setIsActive(isActive);

        // BẮT ĐẦU SỬA TỪ ĐÂY
        if ("add".equals(action)) {
            tableDAO.addTable(t);
            response.sendRedirect("manage-table?msg=add_success");
            
        } else if ("edit".equals(action) || "update".equals(action)) { 
            // ĐÃ SỬA: Chấp nhận cả "edit" và "update"
            tableDAO.updateTable(t);
            response.sendRedirect("manage-table?msg=update_success");
            
        } else {
            // BỔ SUNG: Cứu cánh cuối cùng. Nếu vì lý do nào đó action bị sai, 
            // nó sẽ tự động đá về trang danh sách thay vì hiện trang trắng
            response.sendRedirect("manage-table");
        }
    }
}