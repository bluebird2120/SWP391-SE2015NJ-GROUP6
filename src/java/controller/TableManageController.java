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
import util.CsrfUtil;

/**
 * LUỒNG TABLE MANAGEMENT CỦA OWNER.
 *
 * <p>Thứ tự feature trong file:
 * 1) List Table; 2) Add Table; 3) Edit Table; 4) Table Detail + QR;
 * 5) lưu Add/Edit; 6) validation helpers.</p>
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

    // ==================== 1. ENTRY POINT / ROUTING ====================

    /**
     * GET điều hướng theo đúng thứ tự: List -> Add -> Edit -> Detail.
     * @param request
     * @param response
     * @throws jakarta.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doGet(HttpServletRequest request,HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        CsrfUtil.ensureToken(session);
        Employee loginUser = (Employee) session.getAttribute("employee");

        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = trimToEmpty(request.getParameter("action"));
        if (action.isEmpty()) {
            action = "list";
        }

        switch (action) {
            case "list":
                showTableList(request, response, loginUser.getRoleID());
                break;
            case "add":
                showAddForm(request, response, loginUser);
                break;
            case "edit":
                showEditForm(request, response, loginUser);
                break;
            case "detail":
                showTableDetail(request, response);
                break;
            default:
                response.sendRedirect(
                        request.getContextPath() + "/owner/manage-table");
                break;
        }
    }

    // ==================== 2. LIST TABLE ====================

    /**
     * Đọc filter, đếm tổng bản ghi, lấy đúng một trang và mở table_list.jsp.
     */
    private void showTableList(HttpServletRequest request,
            HttpServletResponse response, int roleID)
            throws ServletException, IOException {

        String searchName = trimToEmpty(request.getParameter("searchName"));
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

        Integer parsedPage = parsePositiveInt(request.getParameter("page"));
        int page = parsedPage != null ? parsedPage : 1;

        int totalItem = tableDAO.countSearchTables(
                searchName, searchCapacity, searchArea, searchStatus);
        int totalPage = (int) Math.ceil((double) totalItem / PAGE_SIZE);
        if (totalPage > 0 && page > totalPage) {
            page = totalPage;
        }

        int offset = (page - 1) * PAGE_SIZE;
        List<Table> tables = tableDAO.searchTablesPaging(
                searchName, searchCapacity, searchArea, searchStatus,
                offset, PAGE_SIZE);

        request.setAttribute("searchName", searchName);
        request.setAttribute("searchCapacity", searchCapacity);
        request.setAttribute("searchArea",
                request.getParameter("searchArea"));
        request.setAttribute("searchStatus",
                request.getParameter("searchStatus"));
        request.setAttribute("tableList", tables);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("currentPage", page);
        request.setAttribute("userRole", roleID);
        request.setAttribute("capacityOptions",
                tableDAO.getDistinctCapacities());

        request.getRequestDispatcher(TABLE_LIST_VIEW)
                .forward(request, response);
    }

    // ==================== 3. ADD TABLE ====================

    /** Mở form thêm bàn; chỉ Owner được phép truy cập. */
    private void showAddForm(HttpServletRequest request,
            HttpServletResponse response, Employee loginUser)
            throws ServletException, IOException {

        if (!isOwner(loginUser)) {
            redirectUnauthorized(request, response);
            return;
        }
        request.setAttribute("mode", "add");
        request.getRequestDispatcher(TABLE_FORM_VIEW)
                .forward(request, response);
    }

    // ==================== 4. EDIT TABLE ====================

    /** Tải dữ liệu hiện tại và mở form edit; QRCodeToken được giữ nguyên. */
    private void showEditForm(HttpServletRequest request,
            HttpServletResponse response, Employee loginUser)
            throws ServletException, IOException {

        if (!isOwner(loginUser)) {
            redirectUnauthorized(request, response);
            return;
        }

        Table table = loadRequestedTable(request, response);
        if (table == null) {
            return;
        }
        request.setAttribute("table", table);
        request.setAttribute("mode", "edit");
        request.getRequestDispatcher(TABLE_FORM_VIEW)
                .forward(request, response);
    }

    // ==================== 5. TABLE DETAIL + QR ====================

    /** Hiển thị thông tin bàn read-only cùng mã QR. */
    private void showTableDetail(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Table table = loadRequestedTable(request, response);
        if (table == null) {
            return;
        }
        request.setAttribute("table", table);
        request.setAttribute("mode", "detail");
        request.getRequestDispatcher(TABLE_FORM_VIEW)
                .forward(request, response);
    }

    // ==================== 6. SAVE ADD / EDIT ====================

    /**
     * POST: CSRF -> quyền Owner -> đọc/validate form -> addTable/updateTable.
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "CSRF token không hợp lệ.");
            return;
        }

        Employee loginUser = (Employee) request.getSession()
                .getAttribute("employee");
        if (!isOwner(loginUser)) {
            redirectUnauthorized(request, response);
            return;
        }

        String action = trimToEmpty(request.getParameter("action"));
        String tableName = trimToEmpty(request.getParameter("tableName"));
        Integer capacity = parsePositiveInt(request.getParameter("capacity"));
        String areaType = trimToEmpty(request.getParameter("areaType"));
        Integer isActive = parseNonNegativeInt(
                request.getParameter("isActive"));
        Integer parsedTableID = parsePositiveInt(
                request.getParameter("tableID"));
        int tableID = parsedTableID != null ? parsedTableID : 0;

        String errorMessage = validateTableForm(
                action, tableName, capacity, areaType, isActive, tableID);
        if (errorMessage != null) {
            forwardInvalidForm(request, response, action, tableID,
                    tableName, capacity, areaType, isActive, errorMessage);
            return;
        }

        Table table = new Table();
        table.setTableID(tableID);
        table.setEmployeeID(0);
        table.setTableName(tableName);
        table.setCapacity(capacity);
        table.setAreaType(areaType);
        table.setIsActive(isActive);

        boolean saved;
        String successMessage;
        if ("add".equals(action)) {
            saved = tableDAO.addTable(table);
            successMessage = "msg=add_success";
        } else if ("edit".equals(action) || "update".equals(action)) {
            saved = tableDAO.updateTable(table);
            successMessage = "msg=update_success";
        } else {
            response.sendRedirect(
                    request.getContextPath() + "/owner/manage-table");
            return;
        }

        response.sendRedirect(request.getContextPath()
                + "/owner/manage-table?"
                + (saved ? successMessage : "error=save_failed"));
    }

    // ==================== 7. VALIDATION / SHARED HELPERS ====================

    /** Dùng chung cho Edit và Detail: validate id rồi lấy bàn từ DAO. */
    private Table loadRequestedTable(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        Integer tableID = parsePositiveInt(request.getParameter("id"));
        if (tableID == null) {
            response.sendRedirect(request.getContextPath()
                    + "/owner/manage-table?error=invalid_id");
            return null;
        }

        Table table = tableDAO.getTableByTableID(tableID);
        if (table == null) {
            response.sendRedirect(request.getContextPath()
                    + "/owner/manage-table?error=not_found");
        }
        return table;
    }

    /** Trả thông báo lỗi đầu tiên; null nghĩa là form hợp lệ. */
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

    /** Giữ dữ liệu người dùng vừa nhập và hiển thị lại form có lỗi. */
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

    /** Chuyển chuỗi thành số nguyên dương; sai trả về null. */
    private Integer parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(trimToEmpty(value));
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Chuyển chuỗi thành số nguyên không âm; sai trả về null. */
    private Integer parseNonNegativeInt(String value) {
        try {
            int parsed = Integer.parseInt(trimToEmpty(value));
            return parsed >= 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
