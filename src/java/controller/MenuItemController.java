package controller;

import dal.CookingMethodDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import model.MenuCategory;
import dal.MenuCategoryDAO;
import dal.MenuItemDAO;
import jakarta.servlet.http.HttpSession;
import model.MenuItem;

// === BẮT ĐẦU PHẦN THÊM MỚI (IMPORT): Nhập thêm thư viện cần thiết ===
import dal.TableDAO;
import dal.OrderDAO;
import model.CookingMethod;
import model.Table;
import model.Order;
import model.Employee;
// === KẾT THÚC PHẦN THÊM MỚI ===

@WebServlet(name = "MenuItemController", urlPatterns = {"/menu", "/scan"})
public class MenuItemController extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Servlet MenuItemController</title></head>");
            out.println("<body><h1>Servlet MenuItemController at " + request.getContextPath() + "</h1></body></html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // === BẮT ĐẦU PHẦN THÊM MỚI: XỬ LÝ QUÉT MÃ QR BẰNG TOKEN (HOST/GUEST) ===
        String token = request.getParameter("token");
        int tableIdFromToken = 0;

        //logic gộp bàn
        if (token != null && !token.isEmpty()) {
            TableDAO tableDAO = new TableDAO();
            Table currentTable = tableDAO.getTableByToken(token);

            if (currentTable != null && currentTable.getIsActive() == 1) {
                tableIdFromToken = currentTable.getTableID();

                // Lưu thông tin cơ bản vào session
                session.setAttribute("tableID", currentTable.getTableID());
                session.setAttribute("currentTableID", currentTable.getTableID());
                session.setAttribute("areaType", currentTable.getAreaType());

                OrderDAO orderDAO = new OrderDAO();
                Order activeOrder = orderDAO.getActiveOrderByTableId(currentTable.getTableID());

                String role = (String) session.getAttribute("roleInTable");
                Integer sessionOrderID = (Integer) session.getAttribute("orderID");

                // =========================================================
                // TÍNH NĂNG MỚI: CHỦ BÀN QUÉT MÃ QR ĐỂ GỘP THÊM BÀN TRỐNG
                // =========================================================
                if ("HOST".equals(role) && sessionOrderID != null) {
                    // Nếu khách quét QR của một bàn ĐANG TRỐNG
                    if (activeOrder == null) {
                        boolean isAdded = orderDAO.addTableToExistingOrder(sessionOrderID, currentTable.getTableID());
                        if (isAdded) {
                            session.setAttribute("successMsg", "Đã gộp thêm bàn thành công vào hóa đơn của bạn!");
                            // Chuyển thẳng vào Menu gọi món
                            response.sendRedirect(request.getContextPath() + "/menu");
                            return;
                        }
                    } 
                    // Nếu khách quét QR của bàn ĐANG CÓ NGƯỜI LẠ NGỒI
                    else if (activeOrder.getOrderID() != sessionOrderID) {
                        session.setAttribute("errorMsg", "Bàn này đang có khách ngồi, không thể gộp!");
                        response.sendRedirect(request.getContextPath() + "/menu");
                        return;
                    }
                    // Nếu activeOrder.getOrderID() == sessionOrderID nghĩa là bàn này khách ĐÃ GỘP TỪ TRƯỚC RỒI
                    // Hệ thống sẽ không làm gì cả, cứ thả cho code chạy tiếp xuống dưới để vào Menu bình thường.
                }
                // =========================================================

                if (activeOrder != null) {
                    
//                    // 👉 TRƯỜNG HỢP 1: BÀN ĐÃ CÓ ORDER
//
//                    // --- BẮT ĐẦU: BARIE CHẶN CHỜ NHÂN VIÊN DUYỆT BÀN ---
//                    if (activeOrder.getIsStaffConfirmed() == 0) {
//                        session.setAttribute("pendingOrderID", activeOrder.getOrderID());
//                        request.getRequestDispatcher("/views/user/waiting_staff.jsp").forward(request, response);
//                        return; // Khóa luồng, không cho load Menu!
//                    }
//                    // --- KẾT THÚC BARIE ---                                       

                    if (role != null && sessionOrderID != null && sessionOrderID == activeOrder.getOrderID()) {
                        // Host hoặc Guest đã duyệt -> Cho vào Menu gọi món bình thường
                    } else {
//                        // === BẮT ĐẦU VÁ LỖ HỔNG ĐƠN ĐẶT TRƯỚC (RESERVATION) ===
//                        // Giả sử orderType == 2 là mã của Đơn đặt trước trong Database của bạn
//                        if (activeOrder.getOrderType() == 2) {
//                            session.setAttribute("pendingOrderID", activeOrder.getOrderID());
//                            // Chuyển sang trang yêu cầu nhập Số điện thoại để lấy lại quyền Host
//                            request.getRequestDispatcher("/views/user/claim_host.jsp").forward(request, response);
//                            return; 
//                        }
//                        // === KẾT THÚC PHẦN VÁ ===
                        // === BẮT ĐẦU VÁ LỖ HỔNG ĐƠN ĐẶT TRƯỚC (RESERVATION) ===
                        // Nhận diện đơn đặt trước qua trạng thái 'reserved'
                        if ("reserved".equals(activeOrder.getTableStatus())) {
                            session.setAttribute("pendingOrderID", activeOrder.getOrderID());
                            // Chuyển sang trang yêu cầu nhập Số điện thoại để lấy lại quyền Host
                            request.getRequestDispatcher("/views/user/claim_host.jsp").forward(request, response);
                            return; 
                        }
                        // === KẾT THÚC PHẦN VÁ ===

                        // Người lạ -> Xin phép Host (Dành cho các bàn khách walk-in bình thường, orderType == 1)
                        session.setAttribute("pendingOrderID", activeOrder.getOrderID());
                        request.getRequestDispatcher("/views/user/join_table.jsp").forward(request, response);
                        return;
                    }
                } else {
                    // 👉 TRƯỜNG HỢP 2: BÀN TRỐNG (Người đầu tiên quét)
                    Order newOrder = new Order();
                    newOrder.setTableStatus("occupied");
                    newOrder.setOrderType(1);

                    // SỬA SỐ 0 THÀNH SỐ 1: FIX CỨNG ĐÃ ĐƯỢC DUYỆT
                    newOrder.setIsStaffConfirmed(1);

                    newOrder.setOrderStatus("ordering");
                    newOrder.setTotalAmount(0);
                    newOrder.setDepositAmount(0);

                    // === BẮT ĐẦU: TỰ ĐỘNG GÁN NHÂN VIÊN CA HIỆN TẠI ===
                    dal.EmployeeShiftDAO esDAO = new dal.EmployeeShiftDAO();
                    Integer assignedStaffId = esDAO.getActiveEmployeeForCurrentShift();
                    if (assignedStaffId != null) {
                        newOrder.setEmployeeID(assignedStaffId);
                    }
                    // === KẾT THÚC: TỰ ĐỘNG GÁN NHÂN VIÊN CA HIỆN TẠI ===

                    int newOrderID = orderDAO.createOrder(newOrder);
                    if (newOrderID > 0) {
                        orderDAO.linkOrderAndTable(newOrderID, currentTable.getTableID());
                        session.setAttribute("orderID", newOrderID);
                        session.setAttribute("roleInTable", "HOST");

                        // === BẮT ĐẦU: TỰ ĐỘNG GỬI THÔNG BÁO CHO NHÂN VIÊN ĐƯỢC GÁN ===
                        if (newOrder.getEmployeeID() != null) {
                            try {
                                dal.NotificationDAO notifDAO = new dal.NotificationDAO();
                                model.Notifications n = new model.Notifications();
                                n.setRecipientID(newOrder.getEmployeeID());
                                n.setRecipientType("staff");
                                n.setType("new_order");
                                n.setMessage("Bạn được phân công phục vụ Đơn hàng #" + newOrderID + " (Bàn " + currentTable.getTableID() + ") mới tạo.");
                                n.setIsRead(0);
                                notifDAO.insert(n);
                            } catch (Exception e) {
                                System.err.println("[MenuItemController] Gửi thông báo thất bại: " + e.getMessage());
                            }
                        }
                        // === KẾT THÚC: TỰ ĐỘNG GỬI THÔNG BÁO CHO NHÂN VIÊN ĐƯỢC GÁN ===

                        // TẠM THỜI COMMENT ĐOẠN ĐẨY RA MÀN HÌNH CHỜ
                        /* session.setAttribute("pendingOrderID", newOrderID);
                        request.getRequestDispatcher("/views/user/waiting_staff.jsp").forward(request, response);
                        return;
                         */
                    }
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/error.jsp");
                return;
            }
        }
        // === KẾT THÚC PHẦN THÊM MỚI ===

        // --- LOGIC GỐC CỦA BẠN CỦA BẠN (GIỮ NGUYÊN 100%) ---
        String search = request.getParameter("search");
        String category_raw = request.getParameter("category");
        String method_raw = request.getParameter("cookingMethod");
        String status_raw = request.getParameter("status");
        String minPrice_raw = request.getParameter("minPrice");
        String maxPrice_raw = request.getParameter("maxPrice");
        String priceType = request.getParameter("price");
        String sort = request.getParameter("sort");
        String page_raw = request.getParameter("page");
        String tableID_raw = request.getParameter("tableID");
        
        if (!checkEmpty(search)) {
            search = "";
        }
        if (!checkEmpty(priceType)) {
            priceType = "discountedPrice";
        }
        if (!checkEmpty(sort)) {
            sort = "asc";
        }

        int status = checkEmpty(status_raw) ? Integer.parseInt(status_raw) : 1;
        int categoryId = checkEmpty(category_raw) ? Integer.parseInt(category_raw) : 0;
        int methodID = checkEmpty(method_raw) ? Integer.parseInt(method_raw) : 0;
        int minPrice = 0;
        int maxPrice = 0;
        try {
            minPrice = checkEmpty(minPrice_raw) ? Integer.parseInt(minPrice_raw) : 0;
        } catch (NumberFormatException e) {
            minPrice = 0;
            request.setAttribute("errorPrice", "Giá tiền nhập vào vượt quá giới hạn cho phép!");
        }

        try {
            maxPrice = checkEmpty(maxPrice_raw) ? Integer.parseInt(maxPrice_raw) : Integer.MAX_VALUE;
        } catch (NumberFormatException e) {
            maxPrice = Integer.MAX_VALUE;
            request.setAttribute("errorPrice", "Giá tiền nhập vào vượt quá giới hạn cho phép!");
        }
        int page = checkEmpty(page_raw) ? Integer.parseInt(page_raw) : 1;

        // Cập nhật lại tableID để tương thích với Token (nếu quét Token thì ưu tiên lấy ID từ Token)
        int tableID = (tableIdFromToken > 0) ? tableIdFromToken : (checkEmpty(tableID_raw) ? Integer.parseInt(tableID_raw) : 0);

        String errorPrice = checkPriceInput(minPrice, maxPrice);
        String errorSearch = isValidString(search, 100, "Tìm kiếm không vượt quá 100 kí tự");

        if (errorPrice != null) {
            request.setAttribute("errorPrice", errorPrice);
            minPrice = 0;
            maxPrice = Integer.MAX_VALUE;
        }
        if (errorSearch != null) {
            request.setAttribute("errorSearch", errorSearch);
            search = "";
        }

        int totalItem = mi.countSearchMenuItem(search, categoryId, methodID, status, minPrice, maxPrice, priceType);
        int totalPage = (int) Math.ceil((double) totalItem / PAGE_SIZE);

        if (page > totalPage && totalPage > 0) {
            page = totalPage;
        }

        int offSet = (page - 1) * PAGE_SIZE;
        List<CookingMethod> listMethod = cm.getAllCookingMethod();
        List<MenuCategory> list = md.getAllMenuCategory();
        List<MenuItem> listItem = mi.searchMenuItemPaging(search, categoryId, methodID, status, minPrice, maxPrice, sort, priceType, offSet, PAGE_SIZE);
        
        request.setAttribute("listMethod", listMethod);
        request.setAttribute("list", list);
        request.setAttribute("listItem", listItem);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("currentPage", page);

        // Xóa dòng gán currentTableID gốc vì phần token đã gán chuẩn hơn
        if (tableID > 0) {
            session.setAttribute("currentTableID", tableID);
        }

        // === BẮT ĐẦU CHÈN THÊM: LẤY DANH SÁCH BÀN ĐỂ CHỌN LÚC GỌI MÓN ===
        Integer currentOrderID = (Integer) session.getAttribute("orderID");
        if (currentOrderID != null) {
            TableDAO tDAO = new TableDAO();
            List<Table> assignedTables = tDAO.getTablesByOrderId(currentOrderID);
            request.setAttribute("assignedTables", assignedTables);
        }
        // === KẾT THÚC CHÈN THÊM ===

        // === BẮT ĐẦU PHẦN CHỈNH SỬA: ĐIỀU HƯỚNG MÀN HÌNH ===
        Employee loginUser = (Employee) session.getAttribute("employee");
        Integer sessionTableID = (Integer) session.getAttribute("currentTableID");

        // Nếu là Quản lý/Nhân viên VÀ không đang xem với tư cách Khách bàn nào -> Trỏ vào trang Admin
        if (sessionTableID == null) {
            request.getRequestDispatcher("/views/admin/dish-list.jsp").forward(request, response);
        } else {
            // Còn lại (Khách vãng lai, Khách quét QR) -> Trỏ vào trang Menu User
            request.getRequestDispatcher("/views/user/menu.jsp").forward(request, response);
        }
        // === KẾT THÚC PHẦN CHỈNH SỬA ===
    }

    private MenuCategoryDAO md = new MenuCategoryDAO();
    private MenuItemDAO mi = new MenuItemDAO();
    private CookingMethodDAO cm = new CookingMethodDAO();
    private static final int PAGE_SIZE = 8;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    private String isValidString(String data, int length, String ms) {
        if (data.length() > length) {
            return ms;
        }
        return null;
    }

    private String checkPriceInput(int min, int max) {
        if (min < 0 || max < 0) {
            return "Giá món ăn không được là số âm";
        } else {
            if (min > max) {
                return "Giá max phải lớn hơn giá Min";
            }
        }
        return null;
    }

    private boolean checkEmpty(String data) {
        return (data != null && !data.trim().isEmpty());
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}