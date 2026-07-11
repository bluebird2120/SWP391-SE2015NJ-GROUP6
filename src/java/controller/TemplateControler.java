package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@WebServlet(name = "TemplateControler", urlPatterns = {"/page/*"})
public class TemplateControler extends HttpServlet {

    // Danh sách các trang hợp lệ (viết thường toàn bộ)
    private static final Set<String> ALLOWED_PAGES = Set.of(
            "about",   
            "menu", 
            "album"
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo(); // Lấy phần sau "/page" (ví dụ: "/about" hoặc "/")

        //Người dùng chỉ gõ "/page" hoặc "/page/" -> Mặc định chuyển hướng sang trang "home"
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // Cắt bỏ dấu "/" ở đầu chuỗi (ví dụ: "/about" -> "about")
        String page = pathInfo.substring(1).toLowerCase();

        //Trang không nằm trong danh sách cho phép
        if (!ALLOWED_PAGES.contains(page)) {
            ///page/abc... đẩy về 403
            response.sendRedirect(request.getContextPath() + "/unauthorized");
            return;
        }

        request.getRequestDispatcher("/views/template/" + page + ".jsp")
                .forward(request, response);
    }
}