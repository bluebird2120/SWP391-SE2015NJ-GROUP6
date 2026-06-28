package controller;

import dal.EmployeeDAO;
import dal.PermissionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Employee;

@WebServlet(name = "PermissionController", urlPatterns = {"/owner/permissions"})
public class PermissionController extends HttpServlet {

    private final PermissionDAO permissionDAO = new PermissionDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Danh sách nhân viên Staff (roleID = 2)
        List<Employee> staffList = employeeDAO.listStaff(null);
        request.setAttribute("staffList", staffList);

        //Map employeeID -> số quyền bổ sung đang có (dùng để hiện badge)
        Map<Integer, Integer> staffPermCount = new HashMap<>();
        for (Employee employee : staffList) {
            Set<String> perms = permissionDAO.getExtraPermissionsByEmployee(employee.getEmployeeID());
            staffPermCount.put(employee.getEmployeeID(), perms.size());
        }
        request.setAttribute("staffPermCounts", staffPermCount);
        
        //Các quyền có thể cấp (không bao gồm owner.access, staff.access)
        request.setAttribute("grantablePermissions", permissionDAO.getGrantablePermissions());

        //Nếu đang chọn nhân viên cụ thể → load quyền hiện tại của họ
        String empIdStr = request.getParameter("empID");
        if (empIdStr != null && !empIdStr.isBlank()) {
            try {
                int empID = Integer.parseInt(empIdStr);
                Employee selectedEmployee = employeeDAO.findById(empID);
                if (selectedEmployee != null && selectedEmployee.getRoleID() == 2) {
                    request.setAttribute("selectedEmployee", selectedEmployee);
                    request.setAttribute("currentPerms", permissionDAO.getExtraPermissionsByEmployee(empID));
                }
            } catch (NumberFormatException e) {
            }
        }

        // Flash message sau khi lưu
        HttpSession session = request.getSession(false);
        if (session != null) {
            String msg = (String) session.getAttribute("permissionMsg");
            if (msg != null) {
                request.setAttribute("permissionMsg", msg);
                session.removeAttribute("permissionMsg");
            }
        }

        request.getRequestDispatcher("/views/owner/permissions.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Employee owner = (session != null) ? (Employee) session.getAttribute("employee") : null;

        if (owner == null || owner.getRoleID() != 1) {
            response.sendRedirect(request.getContextPath() + "/unauthorized");
            return;
        }

        String empIdStr = request.getParameter("empID");
        if (empIdStr == null || empIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/owner/permissions");
            return;
        }

        int empID;
        try {
            empID = Integer.parseInt(empIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/owner/permissions");
            return;
        }

        // Kiểm tra nhân viên tồn tại và là Staff
        Employee target = employeeDAO.findById(empID);
        if (target == null || target.getRoleID() != 2) { // chỉ cấp cho Staff (roleID=2)
            session.setAttribute("permissionMsg", "error:Không thể cấp quyền cho tài khoản này.");
            response.sendRedirect(request.getContextPath() + "/owner/permissions?empID=" + empID);
            return;
        }

        // Lấy danh sách quyền được tick từ form (checkboxes name="perms")
        String[] checked = request.getParameterValues("perms");     //getParamValues lấy nhiều giá trị cùng tên
        Set<String> newPerms = new HashSet<>();
        if (checked != null) {
            for (String perms : checked) {
                newPerms.add(perms);
            }
        }

        boolean ok = permissionDAO.setEmployeePermissions(empID, newPerms, owner.getEmployeeID());
        if (ok) {
            session.setAttribute("permissionMsg", "success:Cập nhật quyền thành công.");
        } else {
            session.setAttribute("permissionMsg", "error:Lưu quyền thất bại, vui lòng thử lại.");
        }

        response.sendRedirect(request.getContextPath() + "/owner/permissions?empID=" + empID);
    }
}
