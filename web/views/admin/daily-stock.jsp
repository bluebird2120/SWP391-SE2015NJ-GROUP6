<%-- 
    Document   : daily-stock
    Author     : Admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Chốt Số Lượng Món Trong Ngày</title>
        <style>
            body {
                font-family: system-ui, -apple-system, "Segoe UI", Arial, sans-serif;
                background-color: #f8f9fa;
                color: #333;
                margin: 0;
            }
            .layout {
                background-color: white;
                padding: 35px;
                max-width: 1050px;
                margin: 30px auto;
                box-shadow: 0 4px 12px rgba(0,0,0,0.05);
                border-radius: 12px;
            }
            .page-header {
                border-bottom: 2px solid #e5e7eb;
                padding-bottom: 15px;
                margin-bottom: 20px;
            }
            .page-header h2 {
                margin: 0 0 5px 0;
                color: #1e293b;
            }

            /* ⚠️ HỘP THÔNG BÁO QUY ĐỊNH NGHIỆP VỤ */
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

            /* 🚨 DÒNG THÔNG BÁO CẢNH BÁO MÓN DƯỚI 20% (MỚI THÊM) */
            .danger-warning-box {
                background-color: #fde8e8;
                border-left: 4px solid #e11d48;
                padding: 12px 15px;
                border-radius: 4px;
                margin-bottom: 20px;
                font-size: 14px;
                color: #9f1239;
                font-weight: 600;
            }

            .success-annouce-box {
                background-color: #dcfce7;
                border-left: 4px solid #16a34a;
                padding: 12px 15px;
                border-radius: 4px;
                margin-bottom: 20px;
                font-size: 14px;
                color: #166534;
                font-weight: 600;
            }

            /* ⚠️ HỘP BÁO LỖI CHƯA NHẬP ĐỦ MÓN (JAVASCRIPT ĐIỀU KHIỂN) */
            .error-validation-box {
                display: none; /* Mặc định ẩn, phát hiện lỗi mới hiện */
                background-color: #fff5f5;
                border: 1px solid #feb2b2;
                padding: 15px;
                border-radius: 6px;
                margin-bottom: 20px;
                font-size: 14px;
                color: #c53030;
            }

            /* 🔍 THANH BỘ LỌC TÌM KIẾM */
            .filter-form {
                display: flex;
                gap: 12px;
                align-items: center;
                background: #f1f5f9;
                padding: 15px;
                border-radius: 8px;
                margin-bottom: 20px;
            }
            .filter-input, .filter-select {
                padding: 8px 12px;
                border: 1px solid #cbd5e1;
                border-radius: 6px;
                font-size: 14px;
                background-color: white;
            }
            .btn-search {
                background-color: #475569;
                color: white;
                border: none;
                padding: 8px 16px;
                border-radius: 6px;
                cursor: pointer;
                font-weight: 600;
            }

            /* ⚡ KHỐI NHẬP NHANH SỐ LƯỢNG MẶC ĐỊNH */
            .fast-input-box {
                background-color: #f8fafc;
                border: 1px solid #e2e8f0;
                padding: 12px 20px;
                border-radius: 8px;
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
                border-radius: 4px;
                text-align: center;
            }
            .btn-apply-all {
                background-color: #0284c7;
                color: white;
                border: none;
                padding: 6px 14px;
                border-radius: 4px;
                cursor: pointer;
                font-weight: 600;
            }

            /* BẢNG DỮ LIỆU TỐI GIẢN */
            table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 25px;
            }
            th {
                background-color: #f8fafc;
                color: #475569;
                font-weight: 600;
                padding: 12px;
                text-align: left;
                border-bottom: 2px solid #cbd5e1;
            }
            td {
                padding: 12px;
                border-bottom: 1px solid #e2e8f0;
            }
            .input-item-qty {
                width: 90px;
                padding: 6px;
                border: 1px solid #cbd5e1;
                border-radius: 6px;
                font-size: 14px;
                text-align: center;
            }
            .stock-label {
                background-color: #e2e8f0;
                color: #334155;
                padding: 4px 10px;
                border-radius: 12px;
                font-weight: 600;
                font-size: 14px;
            }

            .btn-submit {
                background-color: #28a745;
                color: white;
                border: none;
                padding: 12px 40px;
                font-size: 15px;
                font-weight: bold;
                border-radius: 6px;
                cursor: pointer;
                float: right;
            }
            .btn-submit:hover {
                background-color: #218838;
            }

            /* 📄 PHÂN TRANG */
            .pagination {
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 5px;
                margin: 30px 0;
            }
            .pagination a, .pagination span {
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
                background-color: #f8fafc;
                border-color: #94a3b8;
            }
            .pagination .page-info {
                background-color: #f1f5f9;
                border-color: #cbd5e1;
                cursor: default;
            }
            .pagination .disabled {
                color: #94a3b8;
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

                        <span style="font-size: 14px; color: #64748b;">
                            Phiên làm việc ngày: 
                            <b style="color: #0284c7;">
                                <fmt:formatDate value="${currentDate}" pattern="dd-MM-yyyy" />
                            </b>
                        </span>
                    </div>

                    <c:if test="${not empty errorMessage}">
                        <div class="danger-warning-box">
                            ${errorMessage}
                        </div>
                    </c:if>

                    <c:if test="${not empty errorSearch}">
                        <div class="danger-warning-box">
                            ${errorSearch}
                        </div>
                    </c:if>

                    <c:if test="${not empty updateFail}">
                        <div class="danger-warning-box">
                            ${updateFail}
                        </div>
                    </c:if>

                    <c:if test="${not empty updateSuccess}">
                        <div class="success-annouce-box">
                            ${updateSuccess}
                        </div>
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

                    <%-- Khung hiển thị thông báo lỗi bằng JavaScript --%>
                    <div class="error-validation-box" id="errorMessageBox">
                        ❌ <b>Không thể lưu thay đổi!</b> Có lỗi xảy ra với dữ liệu nhập vào:
                        <ul id="missingItemsList" style="margin: 5px 0 0 0; padding-left: 20px; font-weight: 600;"></ul>
                    </div>

                    <form action="${pageContext.request.contextPath}/daily-stock" method="get" class="filter-form" id="filterFormID">
                        <input type="text" name="search" value="${param.search}" placeholder="Tìm tên món ăn..." class="filter-input" style="width: 220px;"/>

                        <select name="categoryID" class="filter-select" id="jsSelectCategory">
                            <option value="">Tất cả loại món</option>
                            <c:forEach var="cat" items="${categoryList}">
                                <option value="${cat.categoryID}" ${param.categoryID == cat.categoryID ? 'selected' : ''}>${cat.categoryName}</option>
                            </c:forEach>
                        </select>

                        <select name="cookingMethod" class="filter-select" id="jsSelectMethod">
                            <option value="">Tất cả phương thức</option>
                            <c:forEach var="method" items="${listMethod}">
                                <option value="${method.methodID}" ${param.cookingMethod == method.methodID ? "selected" : "" }>
                                    ${method.methodName}
                                </option>
                            </c:forEach>
                        </select>

                        <div style="display: flex; flex-direction: column; gap: 4px;">
                            <%-- 🌟 Thêm thuộc tính max bằng ngày hôm nay để chặn chọn ngày tương lai ở giao diện --%>
                            <c:set var="todayString" value="<%= java.time.LocalDate.now().toString() %>" />
                            <input type="date" name="date" value="${date}" max="${todayString}" class="filter-input" onchange="this.form.submit()"/>
                        </div>

                        <button type="submit" class="btn-search">Lọc kết quả</button>
                    </form>

                    <div class="fast-input-box">
                        <span>⚡ <b>Nhập nhanh:</b> Điền số lượng cho <b>tất cả ô nhập</b> bên dưới thành:</span>
                        <input type="number" id="inputDefaultAll" min="1" value="50" class="input-all-number">
                        <button type="button" class="btn-apply-all" onclick="applyQuantityToAllFields()">Áp dụng</button>
                    </div>

                    <form id="stockMainForm" action="${pageContext.request.contextPath}/daily-stock" method="post" style="display: block; width: 100%;">

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
                                            <small style="color: #64748b;">Loại: ${item.categoryName}</small>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="stock-label">
                                                ${not empty item.quantityInStock ? item.quantityInStock : 0} đĩa
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <input type="hidden" name="itemID" value="${item.itemID}"/>
                                            <input  type="number" name="initialQuantity" 
                                                    value="${not empty saveInputData ? saveInputData[item.itemID] : ""}" 
                                                    class="input-item-qty field-stock-input"/>
                                            <c:if test="${item.quantityInStock < item.initialQuantity * 20/100}">
                                                <br/><small style="color: #e11d48; font-weight: bold;">🚨 Sắp hết (< 20%)</small>
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
                                    <a href="${pageContext.request.contextPath}/daily-stock?page=1&search=${param.search}&categoryID=${param.categoryID}&date=${date}">Đầu</a>
                                    <a href="${pageContext.request.contextPath}/daily-stock?page=${currentPage - 1}&search=${param.search}&categoryID=${param.categoryID}&date=${date}">Trước</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="disabled">Đầu</span>
                                    <span class="disabled">Trước</span>
                                </c:otherwise>
                            </c:choose>

                            <span class="page-info">Trang <b>${currentPage}</b> / ${totalPage}</span>

                            <c:choose>
                                <c:when test="${currentPage < totalPage}">
                                    <a href="${pageContext.request.contextPath}/daily-stock?page=${currentPage + 1}&search=${param.search}&categoryID=${param.categoryID}&date=${date}">Sau</a>
                                    <a href="${pageContext.request.contextPath}/daily-stock?page=${totalPage}&search=${param.search}&categoryID=${param.categoryID}&date=${date}">Cuối</a>
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

                // Validate giá trị nhập nhanh trước khi áp dụng
                if (defaultValue === "" || parseInt(defaultValue) <= 0 || isNaN(defaultValue)) {
                    alert("Số lượng nhập nhanh phải là một số nguyên dương hợp lệ!");
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
                missingItemsList.innerHTML = ""; // Reset danh sách lỗi cũ

                // 1. KIỂM TRA BỘ LỌC ĐANG BẬT (Chặn lưu thiếu món tương tự như Controller)
                const selectCat = document.getElementById('jsSelectCategory').value;
                const selectMethod = document.getElementById('jsSelectMethod').value;

                if (selectCat !== "" || selectMethod !== "") {
                    isFormValid = false;
                    const li = document.createElement('li');
                    li.innerText = "Hệ thống phát hiện bạn đang bật bộ lọc ẩn danh mục món ăn! Vui lòng trả bộ lọc về trạng thái 'Tất cả loại món' và 'Tất cả phương thức' để tiến hành chốt toàn bộ kho.";
                    missingItemsList.appendChild(li);
                }

                // 2. DUYỆT QUA TỪNG HÀNG ĐỂ KIỂM TRA GIÁ TRỊ NHẬP VÀO
                const rows = document.getElementsByClassName('dish-data-row');

                for (let i = 0; i < rows.length; i++) {
                    const nameText = rows[i].getElementsByClassName('dish-name-text')[0].innerText;
                    const inputField = rows[i].getElementsByClassName('field-stock-input')[0];
                    const valueTrim = inputField.value.trim();

                    // 2a. Kiểm tra nếu để trống ô nhập
                    if (valueTrim === "") {
                        isFormValid = false;
                        inputField.style.borderColor = "#dc3545";
                        const li = document.createElement('li');
                        li.innerText = "Món '" + nameText + "' đang bị bỏ trống số lượng!";
                        missingItemsList.appendChild(li);
                    }
                    // 2b. Kiểm tra nếu nhập gõ chữ, ký tự đặc biệt hoặc số không nguyên dương (<= 0)
                    else {
                        const parsedValue = parseInt(valueTrim);
                        if (isNaN(parsedValue) || parsedValue <= 0 || parseFloat(valueTrim) !== parsedValue) {
                            isFormValid = false;
                            inputField.style.borderColor = "#dc3545";
                            const li = document.createElement('li');
                            li.innerText = "Món '" + nameText + "' có giá trị nhập vào không hợp lệ (Phải là số nguyên lớn hơn 0)!";
                            missingItemsList.appendChild(li);
                        } else {
                            inputField.style.borderColor = "#cbd5e1"; // Trả lại màu viền xám khi hợp lệ
                        }
                    }
                }

                // Nếu phát hiện bất kỳ lỗi nào, chặn form và cuộn trang lên xem thông báo lỗi
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