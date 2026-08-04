package controller;

import dal.TableDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.Employee;
import model.Table;

/**
 * Luồng quản lý bàn của Owner.
 *
 * <p>Các chức năng chính được đặt trực tiếp trong doGet và doPost theo thứ tự:
 * List -> Add -> Edit -> Detail. Bên ngoài chỉ giữ các hàm validate/parse nhỏ
 * được dùng chung, nhờ đó có thể đọc toàn bộ luồng từ trên xuống dưới.</p>
 */
@WebServlet(name = "TableManageController",
        urlPatterns = {"/owner/manage-table"})
public class TableManageController extends HttpServlet {

    private static final int OWNER_ROLE_ID = 1;
    private static final int PAGE_SIZE = 10;
    private static final String TABLE_FORM_VIEW
            = "/views/table/table_form.jsp";
    private static final String TABLE_LIST_VIEW
            = "/views/table/table_list.jsp";

    private final TableDAO tableDAO = new TableDAO();

    // ==================== GET: LIST -> ADD -> EDIT -> DETAIL ====================

    /** Xử lý toàn bộ màn hình trong chức năng quản lý bàn của Owner. */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Employee loginUser = (Employee) session.getAttribute("employee");

        // Chưa đăng nhập thì không được truy cập chức năng quản lý bàn.
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = trimToEmpty(request.getParameter("action"));
        if (action.isEmpty()) {
            action = "list";
        }

        switch (action) {
            // 1. LIST TABLES
            case "list": {
                // Đọc và validate các điều kiện lọc từ URL.
                String searchName = trimToEmpty(
                        request.getParameter("searchName"));
                if (searchName.length() > 30) {
                    request.setAttribute("errorMessage",
                            "Từ khóa tìm kiếm không được vượt quá 30 ký tự!");
                    searchName = "";
                }

                Integer searchCapacity = parseCapacityFilter(
                        request.getParameter("searchCapacity"), request);
                String searchArea = parseAreaFilter(
                        request.getParameter("searchArea"), request);
                Integer searchStatus = parseStatusFilter(
                        request.getParameter("searchStatus"), request);

                // Tính trang hiện tại rồi chỉ lấy đúng dữ liệu của trang đó.
                Integer parsedPage = parsePositiveInt(
                        request.getParameter("page"));
                int page = parsedPage != null ? parsedPage : 1;
                int totalItem = tableDAO.countSearchTables(
                        searchName, searchCapacity, searchArea, searchStatus);
                int totalPage = (int) Math.ceil(
                        (double) totalItem / PAGE_SIZE);
                if (totalPage > 0 && page > totalPage) {
                    page = totalPage;
                }

                int offset = (page - 1) * PAGE_SIZE;
                List<Table> tables = tableDAO.searchTablesPaging(
                        searchName, searchCapacity, searchArea, searchStatus,
                        offset, PAGE_SIZE);

                // Chuyển dữ liệu, filter và phân trang sang table_list.jsp.
                request.setAttribute("searchName", searchName);
                request.setAttribute("searchCapacity", searchCapacity);
                request.setAttribute("searchArea",
                        request.getParameter("searchArea"));
                request.setAttribute("searchStatus",
                        request.getParameter("searchStatus"));
                request.setAttribute("tableList", tables);
                request.setAttribute("totalPage", totalPage);
                request.setAttribute("currentPage", page);
                request.setAttribute("userRole", loginUser.getRoleID());
                request.setAttribute("capacityOptions",
                        tableDAO.getDistinctCapacities());
                request.getRequestDispatcher(TABLE_LIST_VIEW)
                        .forward(request, response);
                break;
            }

            //2. ADD TABLE: MỞ FORM TRỐNG
            case "add": {
                if (!isOwner(loginUser)) {
                    redirectUnauthorized(request, response);
                    return;
                }

                request.setAttribute("mode", "add");
                request.getRequestDispatcher(TABLE_FORM_VIEW)
                        .forward(request, response);
                break;
            }

            //3. EDIT TABLE: LẤY BÀN RỒI MỞ FORM
            case "edit": {
                if (!isOwner(loginUser)) {
                    redirectUnauthorized(request, response);
                    return;
                }

                Integer tableID = parsePositiveInt(request.getParameter("id"));
                if (tableID == null) {
                    response.sendRedirect(request.getContextPath()
                            + "/owner/manage-table?error=invalid_id");
                    return;
                }

                Table table = tableDAO.getTableByTableID(tableID);
                if (table == null) {
                    response.sendRedirect(request.getContextPath()
                            + "/owner/manage-table?error=not_found");
                    return;
                }

                request.setAttribute("table", table);
                request.setAttribute("mode", "edit");
                request.getRequestDispatcher(TABLE_FORM_VIEW)
                        .forward(request, response);
                break;
            }

            // ---------- 4. DETAIL: HIỂN THỊ READ-ONLY VÀ QR ----------
            case "detail": {
                Integer tableID = parsePositiveInt(request.getParameter("id"));
                if (tableID == null) {
                    response.sendRedirect(request.getContextPath()
                            + "/owner/manage-table?error=invalid_id");
                    return;
                }

                Table table = tableDAO.getTableByTableID(tableID);
                if (table == null) {
                    response.sendRedirect(request.getContextPath()
                            + "/owner/manage-table?error=not_found");
                    return;
                }

                request.setAttribute("table", table);
                request.setAttribute("mode", "detail");
                request.getRequestDispatcher(TABLE_FORM_VIEW)
                        .forward(request, response);
                break;
            }

            default:
                response.sendRedirect(
                        request.getContextPath() + "/owner/manage-table");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        Employee loginUser = (Employee) request.getSession()
                .getAttribute("employee");
        if (!isOwner(loginUser)) {
            redirectUnauthorized(request, response);
            return;
        }

        String action = trimToEmpty(request.getParameter("action"));

        // ---------- 1. ADD TABLE: ĐỌC -> VALIDATE -> INSERT ----------
        if ("add".equals(action)) {
            String tableName = trimToEmpty(
                    request.getParameter("tableName"));
            Integer capacity = parsePositiveInt(
                    request.getParameter("capacity"));
            String areaType = trimToEmpty(
                    request.getParameter("areaType"));
            Integer isActive = parseNonNegativeInt(
                    request.getParameter("isActive"));

            String errorMessage = validateTableForm(
                    "add", tableName, capacity, areaType, isActive, 0);
            if (errorMessage != null) {
                forwardInvalidForm(request, response, "add", 0,
                        tableName, capacity, areaType, isActive, errorMessage);
                return;
            }

            Table newTable = new Table();
            newTable.setEmployeeID(0);
            newTable.setTableName(tableName);
            newTable.setCapacity(capacity);
            newTable.setAreaType(areaType);
            newTable.setIsActive(isActive);

            boolean added = tableDAO.addTable(newTable);
            response.sendRedirect(request.getContextPath()
                    + "/owner/manage-table?"
                    + (added ? "msg=add_success" : "error=save_failed"));
            return;
        }

        // ---------- 2. EDIT TABLE: ĐỌC -> VALIDATE -> UPDATE ----------
        if ("edit".equals(action) || "update".equals(action)) {
            String tableName = trimToEmpty(
                    request.getParameter("tableName"));
            Integer capacity = parsePositiveInt(
                    request.getParameter("capacity"));
            String areaType = trimToEmpty(
                    request.getParameter("areaType"));
            Integer isActive = parseNonNegativeInt(
                    request.getParameter("isActive"));
            Integer parsedTableID = parsePositiveInt(
                    request.getParameter("tableID"));
            int tableID = parsedTableID != null ? parsedTableID : 0;

            String errorMessage = validateTableForm(
                    "edit", tableName, capacity, areaType, isActive, tableID);
            if (errorMessage != null) {
                forwardInvalidForm(request, response, "edit", tableID,
                        tableName, capacity, areaType, isActive, errorMessage);
                return;
            }

            Table updatedTable = new Table();
            updatedTable.setTableID(tableID);
            updatedTable.setEmployeeID(0);
            updatedTable.setTableName(tableName);
            updatedTable.setCapacity(capacity);
            updatedTable.setAreaType(areaType);
            updatedTable.setIsActive(isActive);

            boolean updated = tableDAO.updateTable(updatedTable);
            response.sendRedirect(request.getContextPath()
                    + "/owner/manage-table?"
                    + (updated
                            ? "msg=update_success" : "error=save_failed"));
            return;
        }

        // Action POST không hợp lệ: quay lại danh sách bàn an toàn.
        response.sendRedirect(
                request.getContextPath() + "/owner/manage-table");
    }

    // ==================== CÁC HÀM VALIDATE/PARSE DÙNG CHUNG ====================

    /** Trả về lỗi đầu tiên; null nghĩa là form hợp lệ. */
    private String validateTableForm(String action, String tableName,
            Integer capacity, String areaType, Integer isActive, int tableID) {

        if (tableName.isEmpty() || tableName.length() > 30) {
            return "Tên bàn không được để trống và tối đa chỉ 30 ký tự.";
        }
        if (capacity == null || capacity > 50) {
            return "Sức chứa phải là số nguyên từ 1 đến 50 người.";
        }
        if (!"public".equals(areaType) && !"private".equals(areaType)) {
            return "Khu vực không hợp lệ.";
        }
        if (isActive == null || (isActive != 0 && isActive != 1)) {
            return "Trạng thái bàn không hợp lệ.";
        }
        if (("edit".equals(action) || "update".equals(action))
                && tableID <= 0) {
            return "Mã bàn cần cập nhật không hợp lệ.";
        }
        return null;
    }

    /** Giữ dữ liệu vừa nhập và mở lại form khi validate thất bại. */
    private void forwardInvalidForm(HttpServletRequest request,
            HttpServletResponse response, String action, int tableID,
            String tableName, Integer capacity, String areaType,
            Integer isActive, String errorMessage)
            throws ServletException, IOException {

        Table tempTable = new Table();
        tempTable.setTableID(tableID);
        tempTable.setTableName(tableName);
        tempTable.setCapacity(capacity != null ? capacity : 0);
        tempTable.setAreaType(areaType);
        tempTable.setIsActive(isActive != null ? isActive : -1);

        request.setAttribute("table", tempTable);
        request.setAttribute("errorMessage", errorMessage);
        request.setAttribute("mode", action);
        request.getRequestDispatcher(TABLE_FORM_VIEW)
                .forward(request, response);
    }

    /** Filter sức chứa chỉ nhận số nguyên từ 1 đến 50. */
    private Integer parseCapacityFilter(String value,
            HttpServletRequest request) {

        if (isEmptyFilter(value)) {
            return null;
        }
        Integer capacity = parsePositiveInt(value);
        if (capacity == null || capacity > 50) {
            request.setAttribute("errorMessage",
                    "Sức chứa tìm kiếm phải từ 1 đến 50 người.");
            return null;
        }
        return capacity;
    }

    /** Filter khu vực chỉ nhận public hoặc private. */
    private String parseAreaFilter(String value,
            HttpServletRequest request) {

        if (isEmptyFilter(value)) {
            return null;
        }
        if (!"public".equals(value) && !"private".equals(value)) {
            request.setAttribute("errorMessage",
                    "Khu vực tìm kiếm không hợp lệ.");
            return null;
        }
        return value;
    }

    /** Filter trạng thái chỉ nhận 0 hoặc 1. */
    private Integer parseStatusFilter(String value,
            HttpServletRequest request) {

        if (isEmptyFilter(value)) {
            return null;
        }
        Integer status = parseNonNegativeInt(value);
        if (status == null || (status != 0 && status != 1)) {
            request.setAttribute("errorMessage",
                    "Trạng thái tìm kiếm không hợp lệ.");
            return null;
        }
        return status;
    }

    private boolean isOwner(Employee employee) {
        return employee != null && employee.getRoleID() == OWNER_ROLE_ID;
    }

    private void redirectUnauthorized(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath()
                + "/owner/manage-table?error=unauthorized");
    }

    private boolean isEmptyFilter(String value) {
        return value == null || value.trim().isEmpty()
                || "all".equals(value.trim());
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /** Chuyển chuỗi thành số nguyên dương; dữ liệu sai trả về null. */
    private Integer parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(trimToEmpty(value));
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Chuyển chuỗi thành số nguyên không âm; dữ liệu sai trả về null. */
    private Integer parseNonNegativeInt(String value) {
        try {
            int parsed = Integer.parseInt(trimToEmpty(value));
            return parsed >= 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
