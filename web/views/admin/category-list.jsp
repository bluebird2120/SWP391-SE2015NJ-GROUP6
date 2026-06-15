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
            }
            h2 {
                color: #2c3e50;
                margin-bottom: 12px;
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
                overflow: hidden; /* Khi bo vẫn còn đường kẻ cái này để làm mất */
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
                display: none; /* Mặc định ẩn */
                position: fixed;
                z-index: 1000;
                left: 0;
                top: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.4); /* Làm tối nền phía sau */
            }
            .modal-box {
                background-color: white;
                margin: 12% auto;
                padding: 25px;
                width: 350px;
                border-radius: 8px;
                box-shadow: 0 4px 15px rgba(0,0,0,0.2);
                position: relative;
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
                width: 93%;
                padding: 10px;
                margin: 12px 0 20px 0;
                border: 1px solid #ccc;
                border-radius: 4px;
                font-size: 14px;
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
    <%@ include file="/views/includes/header.jsp" %>
    <%@ include file="/views/includes/dashboard.jsp" %>
    <body>
        <div class="layout">
            <div class="page-header">
                <h2>DANH SÁCH LOẠI MÓN ĂN</h2>
                <input class="btn-create" type="button" value="THÊM MỚI LOẠI MÓN ĂN" onclick="openCreateModal()"/>
            </div>
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