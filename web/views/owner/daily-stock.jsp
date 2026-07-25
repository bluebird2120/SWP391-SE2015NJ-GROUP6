<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Chốt Số Lượng Món Trong Ngày - Vị An</title>
        <style>
            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background-color: #fdfbf7;
                color: #333;
                margin: 0;
            }
            .layout {
                background-color: white;
                padding: 35px;
                max-width: 1050px;
                margin: 30px auto;
                box-shadow: 0 4px 12px rgba(120,73,59,0.03);
                border-radius: 12px;
            }
            .page-header {
                border-bottom: 2px solid #f1ece6;
                padding-bottom: 15px;
                margin-bottom: 20px;
            }
            .page-header h2 {
                margin: 0 0 5px 0;
                color: #78493b;
            }
            .alert-box {
                background-color: #fffaf0;
                border-left: 4px solid #dd6b20;
                padding: 15px;
                border-radius: 4px;
                margin-bottom: 15px;
                font-size: 14px;
                color: #7b341e;
                line-height: 1.5;
            }
            .danger-warning-box {
                background-color: #fdeaea;
                border-left: 4px solid #dc3545;
                padding: 12px 15px;
                border-radius: 6px;
                margin-bottom: 20px;
                font-size: 14px;
                color: #c62828;
                font-weight: 600;
            }
            .success-annouce-box {
                background-color: #edf7ed;
                border-left: 4px solid #28a745;
                padding: 12px 15px;
                border-radius: 6px;
                margin-bottom: 20px;
                font-size: 14px;
                color: #1e4620;
                font-weight: 600;
            }
            .error-validation-box {
                display: none;
                background-color: #fdeaea;
                border: 1px solid #fbc4c4;
                padding: 15px;
                border-radius: 6px;
                margin-bottom: 20px;
                font-size: 14px;
                color: #c62828;
            }
            .filter-form {
                display: flex;
                gap: 12px;
                align-items: center;
                background: #fdfaf7;
                border: 1px solid #ebdcd0;
                padding: 15px;
                border-radius: 12px;
                margin-bottom: 20px;
            }
            .filter-input, .filter-select {
                padding: 8px 12px;
                border: 1px solid #cbd5e1;
                border-radius: 8px;
                font-size: 14px;
                background-color: white;
                transition: border-color 0.2s;
            }
            .filter-input:focus, .filter-select:focus {
                border-color: #78493b;
                outline: none;
            }
            .btn-search {
                background-color: #78493b;
                color: white;
                border: none;
                padding: 8px 16px;
                border-radius: 8px;
                cursor: pointer;
                font-weight: 600;
                transition: background-color 0.2s;
            }
            .btn-search:hover {
                background-color: #5c352d;
            }
            .fast-input-box {
                background-color: #fdfaf7;
                border: 1px solid #ebdcd0;
                padding: 12px 20px;
                border-radius: 12px;
                margin-bottom: 20px;
                display: flex;
                align-items: center;
                gap: 12px;
                font-size: 14px;
            }
            .input-all-number {
                width: 80px;
                padding: 6px;
                border: 1px solid #cbd5e1;
                border-radius: 6px;
                text-align: center;
            }
            .input-all-number:focus {
                border-color: #78493b;
                outline: none;
            }
            .btn-apply-all {
                background-color: #4b6b40;
                color: white;
                border: none;
                padding: 6px 14px;
                border-radius: 6px;
                cursor: pointer;
                font-weight: 600;
                transition: background-color 0.2s;
            }
            .btn-apply-all:hover {
                background-color: #395231;
            }
            table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 25px;
            }
            th {
                background-color: #fcf9f5;
                color: #4a3f35;
                font-weight: 600;
                padding: 14px 12px;
                text-align: left;
                border-bottom: 2px solid #ebdcd0;
            }
            td {
                padding: 14px 12px;
                border-bottom: 1px solid #f1ece6;
                color: #4a3f35;
            }
            .input-item-qty {
                width: 90px;
                padding: 6px;
                border: 1px solid #cbd5e1;
                border-radius: 6px;
                font-size: 14px;
                text-align: center;
                transition: border-color 0.2s;
            }
            .input-item-qty:focus {
                border-color: #78493b;
                outline: none;
            }
            .stock-label {
                background-color: #f1ece6;
                color: #4a3f35;
                padding: 4px 10px;
                border-radius: 12px;
                font-weight: 600;
                font-size: 13px;
            }
            .btn-submit {
                background-color: #de6b48;
                color: white;
                border: none;
                padding: 12px 40px;
                font-size: 15px;
                font-weight: bold;
                border-radius: 8px;
                cursor: pointer;
                float: right;
                transition: background-color 0.2s;
            }
            .btn-submit:hover {
                background-color: #c44d2d;
            }
            .pagination {
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 5px;
                margin: 30px 0;
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
                border-color: #78493b;
                color: white;
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
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>

        <div style="display: flex; align-items: flex-start;">
            <%@ include file="/views/includes/dashboard.jsp" %>

            <div style="flex: 1; min-width: 0;">
                <div class="layout">

                    <div class="page-header">
                        <h2>QUẢN LÝ SỐ LƯỢNG MÓN ĂN PHIÊN HÔM NAY</h2>
                        <jsp:useBean id="currentDate" class="java.util.Date" />

                        <span style="font-size: 14px; color: #7c7267;">
                            Phiên làm việc ngày: 
                            <b style="color: #de6b48;">
                                <!-- 🌟 ĐÃ SỬA CHUẨN: Tự động bóc tách và định dạng lại ngày của thanh lọc sang dạng dd-MM-yyyy xuôi tăm tắp -->
                                <c:choose>
                                    <c:when test="${not empty date}">
                                        <fmt:parseDate value="${date}" pattern="yyyy-MM-dd" var="parsedFilterDate" />
                                        <fmt:formatDate value="${parsedFilterDate}" pattern="dd-MM-yyyy" />
                                    </c:when>
                                    <c:otherwise>
                                        <fmt:formatDate value="${currentDate}" pattern="dd-MM-yyyy" />
                                    </c:otherwise>
                                </c:choose>
                            </b>
                        </span>
                    </div>

                    <c:if test="${not empty errorMessage}">
                        <div class="danger-warning-box">${errorMessage}</div>
                    </c:if>
                    <c:if test="${not empty errorSearch}">
                        <div class="danger-warning-box">${errorSearch}</div>
                    </c:if>
                    <c:if test="${not empty errorDate}">
                        <div class="danger-warning-box">${errorDate}</div>
                    </c:if>
                    <c:if test="${not empty updateFail}">
                        <div class="danger-warning-box">${updateFail}</div>
                    </c:if>
                    <c:if test="${not empty updateSuccess}">
                        <div class="success-annouce-box">${updateSuccess}</div>
                    </c:if>

                    <c:if test="${hasLowStock == true}">
                        <div class="danger-warning-box">
                            ⚠️ <b>HỆ THỐNG CẢNH BÁO:</b> Hiện tại trong kho đang có món ăn bị giảm xuống <b>dưới 20%</b> so với số lượng chốt ban đầu! Vui lòng kiểm tra lại cột Số lượng hiện tại bên dưới.
                        </div>
                    </c:if>

                    <c:if test="${isConfigYet == false}">
                        <div class="danger-warning-box">
                            <b>THÔNG BÁO HỆ THỐNG:</b> Bạn chưa chốt số lượng món ăn cho phiên hôm nay!
                        </div>
                    </c:if>

                    <div class="error-validation-box" id="errorMessageBox">
                        ❌ <b>Không thể lưu thay đổi!</b> Có lỗi xảy ra với dữ liệu nhập vào:
                        <ul id="missingItemsList" style="margin: 5px 0 0 0; padding-left: 20px; font-weight: 600;"></ul>
                    </div>

                    <form action="${pageContext.request.contextPath}/owner/daily-stock" method="get" class="filter-form" id="filterFormID">
                        <input type="text" name="search" value="${currentSearch}" placeholder="Tìm tên món ăn..." class="filter-input" style="width: 220px;"/>

                        <select name="categoryID" class="filter-select" id="jsSelectCategory">
                            <option value="0">Tất cả loại món</option>
                            <c:forEach var="cat" items="${categoryList}">
                                <option value="${cat.categoryID}" ${currentCategory == cat.categoryID ? 'selected' : ''}>${cat.categoryName}</option>
                            </c:forEach>
                        </select>

                        <select name="cookingMethod" class="filter-select" id="jsSelectMethod">
                            <option value="0">Tất cả phương thức</option>
                            <c:forEach var="method" items="${listMethod}">
                                <option value="${method.methodID}" ${currentMethod == method.methodID ? "selected" : "" }>
                                    ${method.methodName}
                                </option>
                            </c:forEach>
                        </select>

                        <div style="display: flex; flex-direction: column; gap: 4px;">
                            <c:set var="todayString" value="<%= java.time.LocalDate.now().toString() %>" />
                            <input type="date" name="date" value="${date}" max="${todayString}" class="filter-input" onchange="this.form.submit()"/>
                        </div>

                        <button type="submit" class="btn-search">Lọc kết quả</button>
                    </form>

                    <div class="fast-input-box">
                        <span>⚡ <b>Nhập nhanh:</b> Điền số lượng cho <b>tất cả ô nhập</b> bên dưới thành:</span>
                        <input type="number" id="inputDefaultAll" min="0" value="50" class="input-all-number">
                        <button type="button" class="btn-apply-all" onclick="applyQuantityToAllFields()">Áp dụng</button>
                    </div>

                    <form id="stockMainForm" action="${pageContext.request.contextPath}/owner/daily-stock" method="post" style="display: block; width: 100%;">
                        <input type="hidden" name="search" value="${currentSearch}"/>
                        <input type="hidden" name="categoryID" value="${currentCategory}"/>
                        <input type="hidden" name="cookingMethod" value="${currentMethod}"/>
                        <input type="hidden" name="page" value="${currentPage}"/>
                        <input type="hidden" name="date" value="${date}"/>

                        <table>
                            <thead>
                                <tr>
                                    <th>Tên Món Ăn</th>
                                    <th style="text-align: center; width: 180px;">Số Lượng Hiện Tại</th>
                                    <th style="text-align: center; width: 220px;">Nhập Số Lượng Mới</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${menuItemList}">
                                    <tr class="dish-data-row">
                                        <td>
                                            <div class="dish-name-text" style="font-weight: 600; color: #1e293b;">${item.itemName}</div>
                                            <small style="color: #7c7267;">Loại: ${item.categoryName}</small>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="stock-label">
                                                ${not empty item.quantityInStock ? item.quantityInStock : 0} đĩa
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <input type="hidden" name="itemID" value="${item.itemID}"/>
                                            <input type="number" name="initialQuantity" 
                                                   value="${not empty saveInputData ? saveInputData[item.itemID] : (item.initialQuantity >= 0 ? item.initialQuantity : '')}" 
                                                   class="input-item-qty field-stock-input"/>
                                            <c:if test="${item.initialQuantity > 0 && item.quantityInStock < item.initialQuantity * 20/100}">
                                                <br/><small style="color: #dc3545; font-weight: bold;">🚨 Sắp hết (&lt; 20%)</small>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty menuItemList}">
                                    <tr>
                                        <td colspan="3" style="text-align: center; color: #94a3b8; padding: 25px;">Không tìm thấy món ăn nào khớp bộ lọc.</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>

                        <button type="submit" class="btn-submit">XÁC NHẬN LƯU THAY ĐỔI</button>
                    </form>

                    <div style="clear: both;"></div>

                    <c:if test="${totalPage > 1}">
                        <div class="pagination">
                            <c:choose>
                                <c:when test="${currentPage > 1}">
                                    <a href="${pageContext.request.contextPath}/owner/daily-stock?page=1&search=${currentSearch}&categoryID=${currentCategory}&cookingMethod=${currentMethod}&date=${date}">Đầu</a>
                                    <a href="${pageContext.request.contextPath}/owner/daily-stock?page=${currentPage - 1}&search=${currentSearch}&categoryID=${currentCategory}&cookingMethod=${currentMethod}&date=${date}">Trước</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="disabled">Đầu</span>
                                    <span class="disabled">Trước</span>
                                </c:otherwise>
                            </c:choose>

                            <span class="page-info">Trang <b>${currentPage}</b> / ${totalPage}</span>

                            <c:choose>
                                <c:when test="${currentPage < totalPage}">
                                    <a href="${pageContext.request.contextPath}/owner/daily-stock?page=${currentPage + 1}&search=${currentSearch}&categoryID=${currentCategory}&cookingMethod=${currentMethod}&date=${date}">Sau</a>
                                    <a href="${pageContext.request.contextPath}/owner/daily-stock?page=${totalPage}&search=${currentSearch}&categoryID=${currentCategory}&cookingMethod=${currentMethod}&date=${date}">Cuối</a>
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

        <%@ include file="/views/includes/footer.jsp" %>

        <script>
            function applyQuantityToAllFields() {
                const defaultValue = document.getElementById('inputDefaultAll').value;

                if (defaultValue === "" || parseInt(defaultValue) < 0 || isNaN(defaultValue)) {
                    alert("Số lượng nhập nhanh phải là một số nguyên không âm hợp lệ!");
                    return;
                }

                const inputFields = document.getElementsByClassName('field-stock-input');
                for (let i = 0; i < inputFields.length; i++) {
                    inputFields[i].value = defaultValue;
                    inputFields[i].style.borderColor = "#cbd5e1";
                }
            }

            const stockMainForm = document.getElementById('stockMainForm');
            const errorMessageBox = document.getElementById('errorMessageBox');
            const missingItemsList = document.getElementById('missingItemsList');

            stockMainForm.onsubmit = function (event) {
                let isFormValid = true;
                missingItemsList.innerHTML = "";

                const selectCat = document.getElementById('jsSelectCategory').value;
                const selectMethod = document.getElementById('jsSelectMethod').value;

                if ((selectCat !== "" && selectCat !== "0") || (selectMethod !== "" && selectMethod !== "0")) {
                    isFormValid = false;
                    const li = document.createElement('li');
                    li.innerText = "Hệ thống phát hiện bạn đang bật bộ lọc ẩn danh mục món ăn! Vui lòng trả bộ lọc về trạng thái 'Tất cả loại món' và 'Tất cả phương thức' để tiến hành chốt toàn bộ kho.";
                    missingItemsList.appendChild(li);
                }

                const rows = document.getElementsByClassName('dish-data-row');

                for (let i = 0; i < rows.length; i++) {
                    const nameText = rows[i].getElementsByClassName('dish-name-text')[0].innerText;
                    const inputField = rows[i].getElementsByClassName('field-stock-input')[0];
                    const valueTrim = inputField.value.trim();

                    if (valueTrim === "") {
                        isFormValid = false;
                        inputField.style.borderColor = "#dc3545";
                        const li = document.createElement('li');
                        li.innerText = "Món '" + nameText + "' đang bị bỏ trống số lượng!";
                        missingItemsList.appendChild(li);
                    } else {
                        const parsedValue = parseInt(valueTrim);
                        if (isNaN(parsedValue) || parsedValue < 0 || parseFloat(valueTrim) !== parsedValue) {
                            isFormValid = false;
                            inputField.style.borderColor = "#dc3545";
                            const li = document.createElement('li');
                            li.innerText = "Món '" + nameText + "' có giá trị nhập vào không hợp lệ (Phải là số nguyên lớn hơn hoặc bằng 0)!";
                            missingItemsList.appendChild(li);
                        } else {
                            inputField.style.borderColor = "#cbd5e1";
                        }
                    }
                }

                if (!isFormValid) {
                    event.preventDefault();
                    errorMessageBox.style.display = "block";
                    window.scrollTo({top: 0, behavior: 'smooth'});
                } else {
                    errorMessageBox.style.display = "none";
                }
            };
        </script>
    </body>
</html>