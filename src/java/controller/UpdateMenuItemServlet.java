/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dal.CookingMethodDAO;
import dal.MenuCategoryDAO;
import dal.MenuItemDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import model.CookingMethod;
import model.MenuCategory;
import model.MenuItem;
import model.MenuItemImages;

/**
 *
 * @author Admin
 */
@WebServlet(name = "UpdateMenuItemServlet", urlPatterns = {"/update-menu"})
@MultipartConfig(
        fileSizeThreshold = 2 * 1024 * 1024,
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 15 * 1024 * 1024)

public class UpdateMenuItemServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet UpdateMenuItemServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet UpdateMenuItemServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }
    private MenuItemDAO menuItemDAO = new MenuItemDAO();
    private MenuCategoryDAO menuCategoryDAO = new MenuCategoryDAO();
    private CookingMethodDAO cm = new CookingMethodDAO();

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String id_raw = request.getParameter("id");
        String backUrl = (String) session.getAttribute("lastDishListUrl");
        int id = ((id_raw != null) && (!id_raw.isEmpty())) ? Integer.parseInt(id_raw) : 0;
        MenuItem mi;
        if (id == 0) {
            //Tạo mới
            mi = new MenuItem();
        } else {
            //Cập nhật
            mi = menuItemDAO.getMenuItemById(id);
            List<MenuItemImages> subImages = menuItemDAO.getImagesByMenuItemId(id);
            request.setAttribute("subImages", subImages);
        }

        if (session.getAttribute("updateSuccess") != null) {
            request.setAttribute("updateSuccess", session.getAttribute("updateSuccess"));
            session.removeAttribute("updateSuccess");
        }
        if (session.getAttribute("updateFail") != null) {
            request.setAttribute("updateFail", session.getAttribute("updateFail"));
            session.removeAttribute("updateFail");
        }

        List<CookingMethod> listMethod = cm.getAllCookingMethod();
        List<MenuCategory> categoryList = menuCategoryDAO.getAllMenuCategory();
        request.setAttribute("dish", mi);
        request.setAttribute("list", categoryList);
        request.setAttribute("listMethod", listMethod);
        request.setAttribute("backUrl", backUrl);
        request.getRequestDispatcher("views/admin/dish-update.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        MenuItem mi;
        double MAX_FILE_SIZE = 5 * 1024 * 1024;
        String itemName = request.getParameter("name");
        String menuItemId_raw = request.getParameter("id");
        String categoryId_raw = request.getParameter("category");
        String method_raw = request.getParameter("methodCooking");
        String description_raw = request.getParameter("description");
        String price_raw = request.getParameter("price");
        String discountPercent_raw = request.getParameter("discountPercent");
        String isAvailable_raw = request.getParameter("isAvailable");
        String allergyNotes_raw = request.getParameter("allergyNotes");
        String backUrl = (String) session.getAttribute("lastDishListUrl");
        request.setAttribute("backUrl", backUrl);
        //lấy ảnh chính cũ
        String oldImage = request.getParameter("oldImage");
        //lấy ảnh chính
        Part mainImage = request.getPart("newMainImage");
        //tạo danh sách lưu trữ ảnh phụ
        List<Part> subImage = new ArrayList<>();
        //validate dữ liệu số và chữ
        String errorName = isValidString(itemName, 150, "Tên món ăn không được để trống", "Tên món ăn không được vượt quá 150 ký tự");
        String errorDescription = isValidString(description_raw, 500, "Mô tả món ăn không được để trống", "Mô tả món ăn không được vượt quá 500 ký tự");
        String errorPrice = isValidPositive(price_raw, "Giá món ăn không được để trống", "Giá món ăn từ 0-1000000000");
        String errorDiscountPercent = isValidPositive(discountPercent_raw, "Giảm giá món ăn không được để trống", "Giảm giá món ăn phải từ 0-100");
        String errorAllergyNotes = isValidString(allergyNotes_raw, 500, "Mô tả dị ứng ăn không được để trống", "Mô tả dị ứng không được vượt quá 500 ký tự");

        //kiểm tra trùng tên khi update
        int itemId = checkEmpty(menuItemId_raw) ? Integer.parseInt(menuItemId_raw) : 0;
        if (!checkEmpty(errorName)) {
            if (menuItemDAO.checkDuplicateMenuItem(itemName, itemId)) {
                errorName = "Tên món ăn đã tồn tại";
            }
        }
        //validate ảnh chính
        String errorMainImage = "";
        if (mainImage != null && !mainImage.getSubmittedFileName().trim().isEmpty()) {
            String fileName = mainImage.getSubmittedFileName();
            if (!isValidImageFile(fileName)) {
                errorMainImage = "Ảnh chính không đúng định dạng (.jpg, .png, .webp, .jpeg)";
            } else {
                if (!isValidImage(mainImage)) {
                    errorMainImage = "File ảnh bị hỏng, vui lòng kiểm tra lại";
                } else {
                    if (mainImage.getSize() > MAX_FILE_SIZE) {
                        errorMainImage = "Dung lượng ảnh chính vượt quá " + MAX_FILE_SIZE + "MB!";
                    }
                }
            }
        } else {
            if (itemId == 0) {
                errorMainImage = "Vui lòng tải ảnh chính đại diện cho món ăn mới!";
            }
        }
        //validate ảnh phụ
        String errorSubImage = "";
        for (Part p : request.getParts()) {
            if (p.getName().equals("newSubImage") && p.getSubmittedFileName() != null && !p.getSubmittedFileName().isEmpty()) {
                subImage.add(p);
            }
        }

        if (itemId == 0 && subImage.isEmpty()) {
            errorSubImage = "Món ăn mới bắt buộc phải có từ 1 đến 3 ảnh phụ!";
        }
        if (subImage.size() > 3) {
            errorSubImage = "Hệ thống chỉ cho phép tải lên tối đa 3 ảnh phụ!";
        }

        if (subImage != null && errorSubImage.isEmpty()) {
            for (Part p : subImage) {
                if (!isValidImageFile(p.getSubmittedFileName())) {
                    errorSubImage = "Có file ảnh phụ không đúng định dạng (.jpg, .png, .webp, .jpeg)";
                    break;
                } else {
                    if (!isValidImage(p)) {
                        errorSubImage = "File ảnh bị hỏng, vui lòng kiểm tra lại";
                        break;
                    } else {
                        if (p.getSize() > MAX_FILE_SIZE) {
                            errorSubImage = "Dung lượng ảnh phụ vượt quá " + MAX_FILE_SIZE + "MB!";
                            break;
                        }
                    }
                }
            }
        }
        //chặn lỗi và trả về jsp
        if (!errorName.isEmpty() || !errorDescription.isEmpty() || !errorPrice.isEmpty()
                || !errorDiscountPercent.isEmpty() || !errorAllergyNotes.isEmpty() || !errorMainImage.isEmpty() || !errorSubImage.isEmpty()) {
            int id = checkEmpty(menuItemId_raw) ? Integer.parseInt(menuItemId_raw) : 0;
            if (id == 0) {
                mi = new MenuItem();
            } else {
                mi = menuItemDAO.getMenuItemById(id);
                List<MenuItemImages> subImages = menuItemDAO.getImagesByMenuItemId(id);
                request.setAttribute("subImages", subImages);
            }
            List categoryList = menuCategoryDAO.getAllMenuCategory();
            request.setAttribute("dish", mi);
            request.setAttribute("list", categoryList);
            request.setAttribute("errorName", errorName);
            request.setAttribute("errorDescription", errorDescription);
            request.setAttribute("errorPrice", errorPrice);
            request.setAttribute("errorDiscountPercent", errorDiscountPercent);
            request.setAttribute("errorAllergyNotes", errorAllergyNotes);
            request.setAttribute("errorMainImage", errorMainImage);
            request.setAttribute("errorSubImage", errorSubImage);
            request.getRequestDispatcher("views/admin/dish-update.jsp").forward(request, response);
            return;
        }
        int categoryId = Integer.parseInt(categoryId_raw);
        int methodID = checkEmpty(method_raw) ? Integer.parseInt(method_raw) : 0;
        int price = Integer.parseInt(price_raw);
        int discountPercent = Integer.parseInt(discountPercent_raw);
        int status = ((isAvailable_raw != null) && (!isAvailable_raw.isEmpty())) ? 1 : 0;
        //tạo đường dẫn
        String upLoadSource = "D:\\Knowledge\\ki5\\SWP\\Project\\Restaurant-Reservation-And-Table-Service-System\\web\\images";
        String upLoadServer = getServletContext().getRealPath("/images");
        //tạo folder
        File sourceFolder = new File(upLoadSource);
        if (!sourceFolder.exists()) {
            sourceFolder.mkdirs();
        }
        File serverFolder = new File(upLoadServer);
        if (!serverFolder.exists()) {
            serverFolder.mkdirs();
        }
        //lưu ảnh chính và ảnh phụ khi tạo mới
        String fileMainImage = mainImage.getSubmittedFileName();
        if (itemId == 0) {
            File sourceFile = new File(upLoadSource + File.separator + fileMainImage);
            mainImage.write(sourceFile.getAbsolutePath());
            File cachFile = new File(upLoadServer + File.separator + fileMainImage);
            try (java.io.FileInputStream fis = new java.io.FileInputStream(sourceFile); java.io.FileOutputStream fos = new java.io.FileOutputStream(cachFile)) {
                fis.transferTo(fos);
            } catch (Exception e) {
            }
            int id = menuItemDAO.insertMenuItem(categoryId, itemName, description_raw, price, discountPercent, "images/" + fileMainImage, status, allergyNotes_raw, methodID);
            if (id > 0) { // insert thành công món ăn
                for (Part p : subImage) {
                    String saveDbPath = p.getSubmittedFileName();
                    File sourceSubFile = new File(upLoadSource + File.separator + saveDbPath);
                    p.write(sourceSubFile.getAbsolutePath());
                    File cachSubFile = new File(upLoadServer + File.separator + saveDbPath);
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(sourceSubFile); java.io.FileOutputStream fos = new java.io.FileOutputStream(cachSubFile)) {
                        fis.transferTo(fos);
                    } catch (Exception e) {
                    }
                    menuItemDAO.insertMenuItemImage(id, "images/" + saveDbPath);
                }
                session.setAttribute("updateSuccess", "Thêm mới món ăn vào thực đơn thành công!");
            } else {
                session.setAttribute("updateFail", "Thêm mới món ăn thất bại, vui lòng kiểm tra lại dữ liệu!");
            }
        } else { //lưu ảnh chính và phụ khi cập nhật
            String mainImagePath = oldImage;

            if (mainImage != null && !mainImage.getSubmittedFileName().isEmpty()) {
                mainImagePath = "images/" + mainImage.getSubmittedFileName();
                File sourceFile = new File(upLoadSource + File.separator + fileMainImage);
                mainImage.write(sourceFile.getAbsolutePath());
                File cacheFile = new File(upLoadServer + File.separator + fileMainImage);
                try (java.io.FileInputStream fis = new java.io.FileInputStream(sourceFile); java.io.FileOutputStream fos = new java.io.FileOutputStream(cacheFile)) {
                    fis.transferTo(fos);
                } catch (Exception e) {
                }
            }

            if (!subImage.isEmpty() && subImage != null) {
                menuItemDAO.deleteMenuItemImages(itemId);

                for (Part p : subImage) {
                    String subName = p.getSubmittedFileName();
                    String saveDbPath = "images/" + subName;

                    File sourceFile = new File(upLoadSource + File.separator + subName);
                    p.write(sourceFile.getAbsolutePath());
                    File cacheFile = new File(upLoadServer + File.separator + subName);
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(sourceFile); java.io.FileOutputStream fos = new java.io.FileOutputStream(cacheFile)) {
                        fis.transferTo(fos);
                    } catch (Exception e) {
                    }

                    menuItemDAO.insertMenuItemImage(itemId, saveDbPath);
                }
            }
            boolean isUpdated = menuItemDAO.updateMenuItem(itemId, categoryId, itemName, description_raw, price, discountPercent, mainImagePath, status, allergyNotes_raw, methodID);
            if (isUpdated) {
                session.setAttribute("updateSuccess", "Cập nhật thông tin món ăn thành công!");
            } else {
                session.setAttribute("updateFail", "Cập nhật món ăn thất bại hoặc không có thay đổi nào được thực hiện!");
            }
        }
        response.sendRedirect(request.getContextPath() + "/update-menu?id=" + itemId);
    }

    private String isValidString(String data, int length, String ms1, String ms2) {
        if (data == null || data.trim().isEmpty()) {
            return ms1;
        }
        if (data.length() > length) {
            return ms2;
        }
        return "";
    }

    private boolean checkEmpty(String data) {
        return (data != null && !data.trim().isEmpty());
    }

    private boolean isValidImageFile(String imageName) {
        String image = imageName.toLowerCase();
        return (image.endsWith(".webp") || image.endsWith(".jpg") || image.endsWith(".jpeg") || image.endsWith(".png"));
    }

    private boolean isValidImage(Part filePart) {
        try {
            return ImageIO.read(filePart.getInputStream()) != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String isValidPositive(String data, String ms1, String ms2) {
        try {
            if (data != null && !data.trim().isEmpty()) {
                if (Integer.parseInt(data) < 0 || Integer.parseInt(data) > Integer.MAX_VALUE) {
                    return ms2;
                }
            } else {
                return ms1;
            }
        } catch (NumberFormatException e) {
            return ms2;
        }
        return "";
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
