<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Phương thức chế biến - Vị An</title>
        <style>
            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background-color: #fdfbf7;
                color: #333;
                margin: 0;
            }
            .layout {
                background-color: white;
                padding: 40px;
                max-width: 1000px;
                margin: 30px auto;
                box-shadow: 0 4px 12px rgba(0,0,0,0.03);
                border-radius: 12px;
            }
            .page-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-bottom: 2px solid #f1ece6;
                padding-bottom: 15px;
                margin-bottom: 25px;
                width: 100%;
                max-width: 100%;
            }
            h2 {
                color: #78493b;
                margin: 0;
                font-size: 24px;
                font-weight: 600;
            }
            .search-container {
                background-color: #fdfaf7;
                border: 1px solid #ebdcd0;
                padding: 12px 20px;
                border-radius: 12px;
                margin-bottom: 20px;
                display: flex;
                align-items: center;
                gap: 10px;
            }
            .search-input {
                padding: 8px 12px;
                border: 1px solid #cbd5e1;
                border-radius: 8px;
                font-size: 14px;
                width: 250px;
                outline: none;
                transition: border-color 0.2s;
            }
            .search-input:focus {
                border-color: #78493b;
            }
            .btn-search {
                background-color: #78493b;
                color: white;
                border: none;
                padding: 8px 16px;
                font-size: 14px;
                font-weight: 600;
                border-radius: 8px;
                cursor: pointer;
                transition: background-color 0.2s;
            }
            .btn-search:hover {
                background-color: #5c352d;
            }
            .btn-create {
                background-color: #4b6b40;
                color: white;
                border: none;
                padding: 10px 20px;
                font-size: 14px;
                font-weight: 600;
                border-radius: 8px;
                cursor: pointer;
                transition: background-color 0.2s;
            }
            .btn-create:hover {
                background-color: #395231;
            }
            table {
                width: 100%;
                max-width: 100%;
                border-collapse: collapse;
                background-color: white;
                box-shadow: 0 2px 8px rgba(120,73,59,0.02);
                border-radius: 10px;
                overflow: hidden;
                border: none;
                margin-top: 15px;
            }
            th {
                background-color: #fcf9f5;
                color: #4a3f35;
                font-weight: 600;
                padding: 14px 15px;
                text-align: left;
                border-bottom: 2px solid #ebdcd0;
            }
            td {
                padding: 14px 15px;
                border-bottom: 1px solid #f1ece6;
                color: #4a3f35;
            }
            form {
                display: inline-block;
            }
            .btn-table {
                padding: 6px 14px;
                font-size: 12px;
                font-weight: bold;
                border: none;
                border-radius: 6px;
                cursor: pointer;
                margin-right: 5px;
                transition: all 0.2s;
            }
            .btn-edit {
                background-color: #fdfbf7;
                border: 1px solid #ebdcd0;
                color: #78493b;
            }
            .btn-edit:hover {
                background-color: #f5ece5;
            }
            .btn-disable {
                background-color: #fdeaea;
                color: #c62828;
            }
            .btn-disable:hover {
                background-color: #fbc4c4;
            }
            .btn-enable {
                background-color: #edf7ed;
                color: #1e4620;
            }
            .btn-enable:hover {
                background-color: #d1ebd1;
            }
            .pagination {
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 5px;
                margin-top: 25px;
            }
            .pagination a, .pagination span {
                padding: 6px 12px;
                border: 1px solid #ebdcd0;
                border-radius: 4px;
                text-decoration: none;
                color: #4a3f35;
                font-size: 14px;
                font-weight: 500;
                background-color: #ffffff;
                transition: all 0.2s;
            }
            .pagination a:hover {
                background-color: #78493b;
                color: white;
                border-color: #78493b;
            }
            .pagination .page-info {
                background-color: #fdfaf7;
                border-color: #ebdcd0;
                cursor: default;
            }
            .pagination .page-info b {
                color: #de6b48;
            }
            .pagination .disabled {
                color: #cbd5e1;
                background-color: #f8fafc;
                border-color: #e2e8f0;
                pointer-events: none;
            }

            .error-message {
                background-color: #fdeaea;
                border-left: 4px solid #dc3545;
                padding: 12px 15px;
                border-radius: 6px;
                margin-bottom: 20px;
                font-size: 14px;
                color: #c62828;
                font-weight: 600;
                display: block;
            }
            .success-message {
                background-color: #edf7ed;
                border-left: 4px solid #28a745;
                padding: 12px 15px;
                border-radius: 6px;
                margin-bottom: 20px;
                font-size: 14px;
                color: #1e4620;
                font-weight: 600;
                display: block;
            }

            /* Modal Popup */
            .modal-wrapper {
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
                width: 380px;
                border-radius: 12px;
                box-shadow: 0 4px 20px rgba(0,0,0,0.15);
                position: relative;
                box-sizing: border-box;
            }
            h3 {
                border-bottom: 2px solid #f1ece6;
                margin-top: 0;
                padding-bottom: 10px;
                margin-bottom: 15px;
                color: #78493b;
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
                margin: 12px 0 5px 0;
                border: 1px solid #d1d5db;
                border-radius: 6px;
                font-size: 14px;
                box-sizing: border-box;
            }
            .modal-box input[type="text"]:focus {
                border-color: #78493b;
                outline: none;
            }
            .btn-submit {
                width: 100%;
                background-color: #de6b48;
                color: white;
                border: none;
                padding: 12px;
                font-size: 14px;
                font-weight: bold;
                border-radius: 6px;
                cursor: pointer;
                transition: background-color 0.2s;
                margin-top: 15px;
            }
            .btn-submit:hover {
                background-color: #c44d2d;
            }
            .modal-error-text {
                color: #dc3545;
                font-size: 12px;
                font-weight: 500;
                margin-bottom: 10px;
                display: block;
            }
            .filter-select {
                padding: 8px 12px;
                border: 1px solid #cbd5e1;
                border-radius: 8px;
                font-size: 14px;
                outline: none;
                background-color: white;
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
                        <h2>DANH SÁCH CÁC PHƯƠNG PHÁP CHẾ BIẾN</h2>
                        <input class="btn-create" type="button" value="THÊM MỚI CÁCH CHẾ BIẾN" onclick="openCreateModal()"/>
                    </div>

                    <form id="searchForm" action="${pageContext.request.contextPath}/owner/method-management" method="get" class="search-container">
                        <input type="text" name="search" value="${currentSearch}" placeholder="Tìm kiếm cách chế biến..." class="search-input"/>

                        <select name="isAvailable" class="filter-select" onchange="document.getElementById('searchForm').submit();">
                            <option value="-1" ${currentAvailable == -1 ? 'selected' : ''}>TẤT CẢ</option>
                            <option value="1" ${currentAvailable == 1 ? 'selected' : ''}>HOẠT ĐỘNG</option>
                            <option value="0" ${currentAvailable == 0 ? 'selected' : ''}>TẠM NGƯNG</option>
                        </select>
                        <button type="submit" class="btn-search">Tìm kiếm</button>
                    </form>

                    <div id="jsErrorSearch" class="error-message" style="display: none;"></div>

                    <c:if test="${not empty updateSuccess}">
                        <div class="success-message">✅ <b>Thành công:</b> ${updateSuccess}</div>
                    </c:if>
                    <c:if test="${not empty updateFail}">
                        <div class="error-message">❌ <b>Thất bại:</b> ${updateFail}</div>
                    </c:if>
                    <c:if test="${not empty errorName and empty modalErrorID}">
                        <div class="error-message">⚠️ <b>Lỗi nhập liệu:</b> ${errorName}</div>
                    </c:if>
                    <c:if test="${not empty errorSearch}">
                        <div class="error-message">⚠️ <b>Lỗi hệ thống:</b> ${errorSearch}</div>
                    </c:if>

                    <table>
                        <thead>
                            <tr>
                                <th>Tên Cách Chế Biến</th>
                                <th>Tổng Số Món</th>
                                <th>Hoạt Động</th>
                                <th>Tạm Ngưng</th>
                                <th>Hành Động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="method" items="${methodList}">
                                <tr>
                                    <td><b style="color: #1e293b;">${method.methodName}</b></td>
                                    <td>${method.totalDish} món</td>
                                    <td><span style="font-weight: 500;">${method.activeMenuItem} món</span></td>
                                    <td><span style="font-weight: 500;">${method.inactiveMenuItem} món</span></td>
                                    <td>
                                        <input class="btn-table btn-edit" type="button" value="SỬA TÊN" onclick="openEditModal('${method.methodID}', '${method.methodName}')"/>

                                        <!-- 🌟 ĐÃ TÍNH HỢP: Thêm sự kiện onsubmit để kích hoạt cảnh báo khi vô hiệu hóa phương thức -->
                                        <form action="${pageContext.request.contextPath}/owner/method-management" method="post" onsubmit="return confirmDisableMethod('${method.methodName}');">
                                            <input type="hidden" value="${method.methodID}" name="methodID"/>
                                            <input type="hidden" value="${currentPage}" name="page"/>
                                            <input type="hidden" value="${currentSearch}" name="search"/>
                                            <input type="hidden" value="${currentAvailable}" name="isAvailable"/>

                                            <c:choose>
                                                <c:when test="${method.activeMenuItem > 0 || method.isAvailable == 1}">
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
                        </tbody>
                    </table>

                    <c:if test="${totalPage > 1}">
                        <div class="pagination">
                            <c:choose>
                                <c:when test="${currentPage > 1}">
                                    <a href="${pageContext.request.contextPath}/owner/method-management?page=1&search=${currentSearch}&isAvailable=${currentAvailable}">Đầu</a>
                                    <a href="${pageContext.request.contextPath}/owner/method-management?page=${currentPage - 1}&search=${currentSearch}&isAvailable=${currentAvailable}">Trước</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="disabled">Đầu</span>
                                    <span class="disabled">Trước</span>
                                </c:otherwise>
                            </c:choose>

                            <span class="page-info">Trang <b>${currentPage}</b> / ${totalPage}</span>

                            <c:choose>
                                <c:when test="${currentPage < totalPage}">
                                    <a href="${pageContext.request.contextPath}/owner/method-management?page=${currentPage + 1}&search=${currentSearch}&isAvailable=${currentAvailable}">Sau</a>
                                    <a href="${pageContext.request.contextPath}/owner/method-management?page=${totalPage}&search=${currentSearch}&isAvailable=${currentAvailable}">Cuối</a>
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

        <%-- Modal Popup Chỉnh sửa cách chế biến --%>
        <div id="editModal" class="modal-wrapper">
            <div class="modal-box">
                <div class="close-icon" onclick="closeEditModal()">&times;</div>
                <h3>Chỉnh sửa tên chế biến</h3>
                <form id="editForm" action="${pageContext.request.contextPath}/owner/method-management" method="post">
                    <input type="hidden" id="modalMethodID" name="methodID" value="${modalErrorID}"/>
                    <input type="hidden" value="${currentPage}" name="page"/>
                    <input type="hidden" value="${currentSearch}" name="search"/>
                    <input type="hidden" value="${currentAvailable}" name="isAvailable"/>
                    <label class="form-label">Tên cách chế biến:</label>
                    <input type="text" id="modalMethodName" name="methodName" value="${not empty errorName and modalErrorID > 0 ? modalErrorName : ''}"/>
                    <span class="modal-error-text" id="editMethodError">
                        <c:if test="${not empty errorName and modalErrorID > 0}">
                            <c:out value="${errorName}"/>
                        </c:if>
                    </span>
                    <input class="btn-submit" type="submit" value="LƯU THAY ĐỔI"/>
                </form>
            </div>
        </div>

        <%-- Modal Popup Thêm mới cách chế biến --%>
        <div id="createModal" class="modal-wrapper">
            <div class="modal-box">
                <div class="close-icon" onclick="closeCreateModal()">&times;</div>
                <h3>Thêm mới cách chế biến</h3>
                <form id="createForm" action="${pageContext.request.contextPath}/owner/method-management" method="post">
                    <input type="hidden" name="methodID" value="0"/>
                    <input type="hidden" value="${currentPage}" name="page"/>
                    <input type="hidden" value="${currentSearch}" name="search"/>
                    <input type="hidden" value="${currentAvailable}" name="isAvailable"/>
                    <label class="form-label">Nhập cách chế biến mới:</label>
                    <input type="text" id="createMethodName" name="methodName" value="${not empty errorName and modalErrorID == 0 ? modalErrorName : ''}"/>
                    <span class="modal-error-text" id="createMethodError">
                        <c:if test="${not empty errorName and modalErrorID == 0}">
                            <c:out value="${errorName}"/>
                        </c:if>
                    </span>
                    <input class="btn-submit" type="submit" value="LƯU THAY ĐỔI"/>
                </form>
            </div>
        </div>

        <script>
            // 🌟 ĐÃ TÍCH HỢP: Hàm kiểm tra hành động gửi form và kích hoạt hộp thoại xác nhận khi chọn VÔ HIỆU HÓA
            function confirmDisableMethod(methodName) {
                const activeSubmitButton = document.activeElement;
                if (activeSubmitButton && activeSubmitButton.name === "status" && activeSubmitButton.value === "0") {
                    const message = "Bạn có chắc chắn muốn vô hiệu hóa không?\nNếu bạn vô hiệu hóa, tất cả các món ăn thuộc phương thức \"" + methodName + "\" cũng sẽ bị vô hiệu hóa.";
                    return confirm(message);
                }
                return true;
            }

            function openEditModal(id, name) {
                document.getElementById('modalMethodID').value = id;
                document.getElementById('modalMethodName').value = name;
                document.getElementById('editMethodError').innerHTML = "";
                document.getElementById('editModal').style.display = "block";
            }

            function openCreateModal() {
                document.getElementById('createMethodName').value = "";
                document.getElementById('createMethodError').innerHTML = "";
                document.getElementById('createModal').style.display = "block";
            }

            function closeEditModal() {
                document.getElementById('editModal').style.display = "none";
            }

            function closeCreateModal() {
                document.getElementById('createModal').style.display = "none";
            }

//            function validateMethodInput(inputElement, errorElement) {
//                const value = inputElement.value.trim();
//                if (value === "") {
//                    errorElement.innerHTML = "Tên cách chế biến không được để trống";
//                    return false;
//                }
//                if (value.length > 100) {
//                    errorElement.innerHTML = "Tên cách chế biến phải ít hơn 100 kí tự";
//                    return false;
//                }
//                errorElement.innerHTML = "";
//                return true;
//            }
//
//            document.getElementById("createForm").onsubmit = function (event) {
//                const input = document.getElementById("createMethodName");
//                const error = document.getElementById("createErrorName");
//                if (!validateMethodInput(input, error)) {
//                    event.preventDefault();
//                }
//            };
//
//            document.getElementById("editForm").onsubmit = function (event) {
//                const input = document.getElementById("modalMethodName");
//                const error = document.getElementById("editErrorName");
//                if (!validateMethodInput(input, error)) {
//                    event.preventDefault();
//                }
//            };
//
//            document.getElementById("searchForm").onsubmit = function (event) {
//                const searchInput = this.elements["search"].value.trim();
//                const errorBox = document.getElementById("jsErrorSearch");
//
//                if (searchInput.length > 100) {
//                    errorBox.innerHTML = "⚠️ <b>Lỗi tìm kiếm:</b> Tìm kiếm không vượt quá 100 kí tự";
//                    errorBox.style.display = "block";
//                    event.preventDefault();
//                } else {
//                    errorBox.innerHTML = "";
//                    errorBox.style.display = "none";
//                }
//            };

            function validateMethodModal(inputElement, errorElement) {
                const value = inputElement.value.trim();
                if (value === "") {
                    errorElement.innerHTML = "Tên cách chế biến không được để trống";
                    return false;
                }
                if (value.length > 100) {
                    errorElement.innerHTML = "Tên cách chế biến phải ít hơn 100 kí tự";
                    return false;
                }
                errorElement.innerHTML = "";
                return true;
            }

            document.getElementById("createForm").onsubmit = function (event) {
                if (!validateMethodModal(
                        document.getElementById("createMethodName"),
                        document.getElementById("createMethodError"))) {
                    event.preventDefault();
                }
            };

            document.getElementById("editForm").onsubmit = function (event) {
                if (!validateMethodModal(
                        document.getElementById("modalMethodName"),
                        document.getElementById("editMethodError"))) {
                    event.preventDefault();
                }
            };

            document.addEventListener("DOMContentLoaded", function () {
                const hasError = "${not empty errorName}";
                const errorID = "${modalErrorID}";
                if (hasError === "true") {
                    if (errorID !== "" && parseInt(errorID) > 0) {
                        document.getElementById("editModal").style.display = "block";
                    } else if (errorID !== "" && parseInt(errorID) === 0) {
                        document.getElementById("createModal").style.display = "block";
                    }
                }
            });
        </script>
        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>
