<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <style>
            .layout{
                background-color: white;
                padding: 40px;
                max-width: 1000px;
                margin: 30px auto;
                box-shadow: 0 4px 12px rgba(0,0,0,0.05);
                border-radius: 12px;
            }
            .page-header{
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-bottom: 2px solid #e5e7eb;
                padding-bottom: 15px;
                margin-bottom: 25px;
                width: 100%;
                max-width: 100%;
            }
            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background-color: #f8f9fa;
                color: #333;
                margin: 0;
            }
            h2 {
                color: #2c3e50;
                margin-bottom: 12px;
            }
            .search-container {
                background-color: #f8fafc;
                border: 1px solid #e2e8f0;
                padding: 12px 20px;
                border-radius: 8px;
                margin-bottom: 20px;
                display: flex;
                align-items: center;
                gap: 10px;
            }
            .search-input {
                padding: 8px 12px;
                border: 1px solid #cbd5e1;
                border-radius: 6px;
                font-size: 14px;
                width: 250px;
                outline: none;
            }
            .btn-search {
                background-color: #6c757d;
                color: white;
                border: none;
                padding: 8px 16px;
                font-size: 14px;
                font-weight: 600;
                border-radius: 6px;
                cursor: pointer;
            }
            .btn-create {
                background-color: #007bff;
                color: white;
                border: none;
                padding: 10px 20px;
                font-size: 14px;
                font-weight: 600;
                border-radius: 6px;
                cursor: pointer;
            }
            table {
                width: 100%;
                max-width: 100%;
                border-collapse: collapse;
                background-color: white;
                box-shadow: 0 2px 8px rgba(0,0,0,0.05);
                border-radius: 10px;
                overflow: hidden;
                border: none;
            }
            th {
                background-color: #f1f3f5;
                color: #495057;
                font-weight: 600;
                padding: 12px 15px;
                text-align: left;
                border-bottom: 2px solid #dee2e6;
            }
            td {
                padding: 12px 15px;
                border-bottom: 1px solid #dee2e6;
                color: #495057;
            }
            form{
                display: inline-block;
            }
            .btn-table {
                padding: 6px 12px;
                font-size: 12px;
                font-weight: bold;
                border: none;
                border-radius: 4px;
                cursor: pointer;
                margin-right: 5px;
            }
            .btn-edit {
                background-color: #ffc107;
                color: #212529;
            }
            .btn-disable {
                background-color: #dc3545;
                color: white;
            }
            .btn-enable{
                background-color: #28a745;
                color: white;
            }
            .pagination {
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 5px;
                margin-top: 25px;
            }
            .pagination a,
            .pagination span {
                padding: 6px 12px;
                border: 1px solid #cbd5e1;
                border-radius: 4px;
                text-decoration: none;
                color: #334155;
                font-size: 14px;
                font-weight: 500;
                background-color: #ffffff;
            }
            .pagination a:hover {
                background-color: #f1f5f9;
                border-color: #94a3b8;
            }
            .pagination .page-info {
                background-color: #f1f5f9;
                border-color: #cbd5e1;
                cursor: default;
            }
            .pagination .page-info b {
                color: #007bff;
            }
            .pagination .disabled {
                color: #94a3b8;
                background-color: #f8fafc;
                border-color: #e2e8f0;
                pointer-events: none;
            }
            .error-message {
                color: #dc3545;
                font-weight: bold;
                margin-bottom: 15px;
                background-color: #fce8e6;
                border: 1px solid #fbc4c4;
                padding: 5px 16px;
                border-radius: 4px;
                display: inline-block;
                font-size: 12px
            }
            .modal-wrapper{
                display: none;
                position: fixed;
                z-index: 1000;
                left: 0;
                top: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.4);
            }
            .modal-box {
                background-color: white;
                margin: 12% auto;
                padding: 25px;
                width: 350px;
                border-radius: 8px;
                box-shadow: 0 4px 15px rgba(0,0,0,0.2);
                position: relative;
                box-sizing: border-box;
            }
            h3{
                border-bottom: 2px solid #e5e7eb;
                margin-bottom: 5px;
            }
            .close-icon {
                position: absolute;
                right: 20px;
                top: 15px;
                font-size: 24px;
                font-weight: bold;
                color: #aaa;
                cursor: pointer;
            }
            .modal-box input[type="text"] {
                width: 100%;
                padding: 10px;
                margin: 12px 0 20px 0;
                border: 1px solid #ccc;
                border-radius: 4px;
                font-size: 14px;
                box-sizing: border-box;
            }
            .btn-submit {
                width: 100%;
                background-color: #28a745;
                color: white;
                border: none;
                padding: 10px;
                font-size: 14px;
                font-weight: bold;
                border-radius: 4px;
                cursor: pointer;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>

        <div style="display: flex; align-items: flex-start;">
            <%@ include file="/views/includes/dashboard.jsp" %>

            <div style="flex: 1; min-width: 0;">
                <div class="layout">
                    <div class="page-header">
                        <h2>DANH SÁCH LOẠI MÓN ĂN</h2>
                        <input class="btn-create" type="button" value="THÊM MỚI LOẠI MÓN ĂN" onclick="openCreateModal()"/>
                    </div>

                    <form action="${pageContext.request.contextPath}/category-management" method="get" class="search-container">
                        <input type="text" name="search" value="${param.search}" placeholder="Tìm kiếm loại món ăn..." class="search-input"/>
                        <button type="submit" class="btn-search">Tìm kiếm</button>
                    </form>

                    <c:if test="${errorName != null && !errorName.trim().isEmpty()}"><div class="error-message">${errorName}</div></c:if>

                        <table border="1">
                            <thead>
                                <tr>
                                    <th>Tên Loại Món</th>
                                    <th>Tổng Số món</th>
                                    <th>Hoạt Động</th>
                                    <th>Tạm Ngưng</th>
                                    <th>Hành Động</th>
                                </tr>
                            </thead>
                        <c:forEach var="cat" items="${categoryList}">
                            <tr>
                                <td><div>${cat.categoryName}</div></td>
                                <td><div>${cat.totalDish}</div></td>
                                <td><div>${cat.activeMenuItem}</div></td>
                                <td><div>${cat.inactiveMenuItem}</div></td>
                                <td>
                                    <input class="btn-table btn-edit" type="button" value="SỬA TÊN" onclick="openEditModal('${cat.categoryID}', '${cat.categoryName}')"/>
                                    <form action="${pageContext.request.contextPath}/category-management" method="post">
                                        <input type="hidden" value="${cat.categoryID}" name="categoryID"/>
                                        <c:choose>
                                            <c:when test="${cat.activeMenuItem > 0}">
                                                <button class="btn-table btn-disable" type="submit" name="status" value="0">VÔ HIỆU HÓA</button>
                                            </c:when>
                                            <c:otherwise>
                                                <button class="btn-table btn-enable" type="submit" name="status" value="1">KÍCH HOẠT</button>
                                            </c:otherwise>
                                        </c:choose>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                    
                    <c:if test="${totalPage > 1}">
                        <div class="pagination">
                            <c:choose>
                                <c:when test="${currentPage > 1}">
                                    <a href="${pageContext.request.contextPath}/category-management?page=1&search=${param.search}">Đầu</a>
                                    <a href="${pageContext.request.contextPath}/category-management?page=${currentPage - 1}&search=${param.search}">Trước</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="disabled">Đầu</span>
                                    <span class="disabled">Trước</span>
                                </c:otherwise>
                            </c:choose>

                            <span class="page-info">Trang <b>${currentPage}</b> / ${totalPage}</span>

                            <c:choose>
                                <c:when test="${currentPage < totalPage}">
                                    <a href="${pageContext.request.contextPath}/category-management?page=${currentPage + 1}&search=${param.search}">Sau</a>
                                    <a href="${pageContext.request.contextPath}/category-management?page=${totalPage}&search=${param.search}">Cuối</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="disabled">Sau</span>
                                    <span class="disabled">Cuối</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>

        <div id="editModal" class="modal-wrapper">
            <div class="modal-box">
                <div class="close-icon" onclick="closeEditModal()">&times;</div>
                <h3>Chỉnh sửa tên loại món</h3>
                <form action="category-management" method="post">
                    <input type="hidden" id="modalCategoryID" name="categoryID"/>
                    <label>Tên loại:</label><br/>
                    <input type="text" id="modalCategoryName" name="categoryName" required/><br/>
                    <input class="btn-submit" type="submit" value="LƯU THAY ĐỔI"/>
                </form>
            </div>
        </div>

        <div id="createModal" class="modal-wrapper">
            <div class="modal-box">
                <div class="close-icon" onclick="closeCreateModal()">&times;</div>
                <h3>Thêm mới loại món ăn</h3>
                <form action="category-management" method="post">
                    <input type="hidden" name="categoryID" value="0"/>
                    <label>Nhập loại mới:</label><br/>
                    <input type="text" name="categoryName" required/><br/>
                    <input class="btn-submit" type="submit" value="LƯU THAY ĐỔI"/>
                </form>
            </div>
        </div>

        <script>
            function openEditModal(id, name) {
                document.getElementById('modalCategoryID').value = id;
                document.getElementById('modalCategoryName').value = name;
                document.getElementById('editModal').style.display = "block";
            }

            function openCreateModal() {
                document.getElementById('createModal').style.display = "block";
            }

            function closeEditModal() {
                document.getElementById('editModal').style.display = "none";
            }

            function closeCreateModal() {
                document.getElementById('createModal').style.display = "none";
            }
        </script>
        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>