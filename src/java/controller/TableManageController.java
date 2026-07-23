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

@WebServlet(name = "TableManageController", urlPatterns = {"/owner/manage-table"})
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
                    response.sendRedirect(request.getContextPath() + "/owner/manage-table?error=unauthorized");
                    return;
                }
                request.setAttribute("mode", "add"); // Truyền cờ trạng thái "Thêm"
                request.getRequestDispatcher("/views/table/table_form.jsp").forward(request, response);
                break;

            case "edit":
                if (roleID != 1) { // KHÔNG PHẢI OWNER
                    response.sendRedirect(request.getContextPath() + "/owner/manage-table?error=unauthorized");
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

                request.getRequestDispatcher("/views/table/table_form.jsp").forward(request, response);
                break;

            case "list":
            default:
                // 1. Đọc các tham số lọc từ URL gửi lên
                String searchName = request.getParameter("searchName");
                searchName = (searchName != null) ? searchName.trim() : "";

                // Validate sức chứa từ URL vì người dùng có thể sửa query string thủ công.
                String capParam = request.getParameter("searchCapacity");
                Integer searchCapacity = null;
                if (capParam != null && !capParam.trim().isEmpty() && !"all".equals(capParam)) {
                    try {
                        int parsedCapacity = Integer.parseInt(capParam.trim());
                        if (parsedCapacity >= 1 && parsedCapacity <= 50) {
                            searchCapacity = parsedCapacity;
                        } else {
                            request.setAttribute("errorMessage", "Sức chứa tìm kiếm phải từ 1 đến 50 người.");
                        }
                    } catch (NumberFormatException e) {
                        request.setAttribute("errorMessage", "Sức chứa tìm kiếm không hợp lệ.");
                    }
                }

                String searchArea = request.getParameter("searchArea");
                // Khu vực là danh mục nghiệp vụ cố định, chỉ chấp nhận public/private.
                if (searchArea == null || searchArea.trim().isEmpty() || "all".equals(searchArea)) {
                    searchArea = null;
                } else if (!"public".equals(searchArea) && !"private".equals(searchArea)) {
                    request.setAttribute("errorMessage", "Khu vực tìm kiếm không hợp lệ.");
                    searchArea = null;
                }

                // Trạng thái chỉ có hai giá trị hợp lệ: 0 (tạm ngưng) và 1 (hoạt động).
                String statusParam = request.getParameter("searchStatus");
                Integer searchStatus = null;
                if (statusParam != null && !statusParam.trim().isEmpty() && !"all".equals(statusParam)) {
                    try {
                        int parsedStatus = Integer.parseInt(statusParam.trim());
                        if (parsedStatus == 0 || parsedStatus == 1) {
                            searchStatus = parsedStatus;
                        } else {
                            request.setAttribute("errorMessage", "Trạng thái tìm kiếm không hợp lệ.");
                        }
                    } catch (NumberFormatException e) {
                        request.setAttribute("errorMessage", "Trạng thái tìm kiếm không hợp lệ.");
                    }
                }

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
                // Truyền Integer đã validate (hoặc null khi chọn "Tất cả") sang JSP.
                // Không truyền chuỗi "all" vì JSP sẽ lỗi ép kiểu khi so sánh với capacity Integer.
                request.setAttribute("searchCapacity", searchCapacity);
                request.setAttribute("searchArea", request.getParameter("searchArea"));
                request.setAttribute("searchStatus", request.getParameter("searchStatus"));
                
                // Đẩy thông tin phân trang sang JSP
                request.setAttribute("tableList", list);
                request.setAttribute("totalPage", totalPage);
                request.setAttribute("currentPage", page);
                request.setAttribute("userRole", roleID); 

                // Danh sách sức chứa được lấy động từ DB thay vì fix cứng 2, 4, 6, 8, 10.
                request.setAttribute("capacityOptions", tableDAO.getDistinctCapacities());
                
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
            response.sendRedirect(request.getContextPath() + "/owner/manage-table?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");

        // 1. LẤY DỮ LIỆU TỪ FORM VÀ LÀM SẠCH (TRIM)
        String tableName = request.getParameter("tableName");
        tableName = (tableName != null) ? tableName.trim() : "";

        // Không tự đổi dữ liệu sai thành 2; mọi giá trị ngoài 1-50 phải báo lỗi.
        int capacity = 0;
        boolean capacityInvalid = false;
        try {
            String capacityParam = request.getParameter("capacity");
            if (capacityParam == null || capacityParam.trim().isEmpty()) {
                capacityInvalid = true;
            } else {
                capacity = Integer.parseInt(capacityParam.trim());
                capacityInvalid = capacity < 1 || capacity > 50;
            }
        } catch (NumberFormatException e) {
            capacityInvalid = true;
        }

        String areaType = request.getParameter("areaType");
        areaType = areaType != null ? areaType.trim() : "";
        boolean areaInvalid = !"public".equals(areaType) && !"private".equals(areaType);

        // Validate trạng thái ở backend để chặn request giả mạo ngoài form HTML.
        int isActive = -1;
        boolean statusInvalid = false;
        try {
            String activeParam = request.getParameter("isActive");
            if (activeParam == null || activeParam.trim().isEmpty()) {
                statusInvalid = true;
            } else {
                isActive = Integer.parseInt(activeParam.trim());
                statusInvalid = isActive != 0 && isActive != 1;
            }
        } catch (NumberFormatException e) {
            statusInvalid = true;
        }

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
        } else if (capacityInvalid) {
            hasError = true;
            errorMessage = "Sức chứa phải là số nguyên từ 1 đến 50 người.";
        } else if (areaInvalid) {
            hasError = true;
            errorMessage = "Khu vực không hợp lệ.";
        } else if (statusInvalid) {
            hasError = true;
            errorMessage = "Trạng thái bàn không hợp lệ.";
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
            response.sendRedirect(request.getContextPath() + "/owner/manage-table?msg=add_success");
            
        } else if ("edit".equals(action) || "update".equals(action)) { 
            // ĐÃ SỬA: Chấp nhận cả "edit" và "update"
            tableDAO.updateTable(t);
            response.sendRedirect(request.getContextPath() + "/owner/manage-table?msg=update_success");
            
        } else {
            // BỔ SUNG: Cứu cánh cuối cùng. Nếu vì lý do nào đó action bị sai, 
            // nó sẽ tự động đá về trang danh sách thay vì hiện trang trắng
            response.sendRedirect(request.getContextPath() + "/owner/manage-table");
        }
    }
}
