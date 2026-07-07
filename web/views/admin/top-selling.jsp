<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Báo Cáo Hiệu Suất Món Ăn</title>
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

        <style>
            * {
                box-sizing: border-box;
            }
            body {
                margin: 0;
                font-family: sans-serif;
                background: #faf6f2;
            }
            .main {
                flex: 1;
                padding: 24px 32px;
                min-width: 0;
            }
            .page-container {
                display: flex;
            }
            .page-head {
                display: flex;
                justify-content: space-between;
                align-items: flex-end;
                margin-bottom: 18px;
                flex-wrap: wrap;
                gap: 12px;
            }
            .page-title {
                color: #76493b;
                font-size: 1.6rem;
                margin: 0;
            }

            /* Bộ lọc */
            .filter-bar {
                background: #fff;
                border: 1px solid #ede0d8;
                border-radius: 12px;
                padding: 16px;
                margin-bottom: 20px;
                display: flex;
                gap: 12px;
                align-items: flex-end;
                flex-wrap: wrap;
            }
            .quick-filter-group {
                display: flex;
                gap: 8px;
                width: 100%;
                margin-bottom: 8px;
            }
            .btn-quick {
                padding: 6px 14px;
                border: 1px solid #d7bfa4;
                background: #fff;
                color: #76493b;
                border-radius: 6px;
                cursor: pointer;
                font-weight: 500;
                font-size: 0.85rem;
                transition: all 0.2s;
            }
            .btn-quick.active, .btn-quick:hover {
                background: #76493b;
                color: #fff;
                border-color: #76493b;
            }

            .filter-bar .field {
                display: flex;
                flex-direction: column;
                gap: 4px;
            }
            .filter-bar label {
                font-size: 0.78rem;
                color: #8a6e5a;
                font-weight: 600;
                text-transform: uppercase;
                letter-spacing: 0.04em;
            }
            .filter-bar input, .filter-bar select {
                padding: 8px 12px;
                border: 1px solid #d7bfa4;
                border-radius: 7px;
                font-family: inherit;
                font-size: 0.9rem;
                min-width: 180px;
            }
            .filter-bar input:focus, .filter-bar select:focus {
                outline: none;
                border-color: #76493b;
            }

            /* Nút bấm */
            .btn {
                padding: 9px 16px;
                border-radius: 8px;
                border: none;
                cursor: pointer;
                font-family: inherit;
                font-size: 0.88rem;
                font-weight: 600;
                text-decoration: none;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                gap: 6px;
                transition: all 0.2s;
            }
            .btn-primary {
                background: #76493b;
                color: #fff;
            }
            .btn-primary:hover {
                background: #5d3a2e;
            }
            .btn-reset {
                background: #d7bfa4;
                color: #76493b;
            }
            .btn-reset:hover {
                background: #c5a98a;
            }

            /* Khối thông báo lỗi */
            .error-box {
                display: flex;
                flex-direction: column;
                gap: 8px;
                margin-bottom: 16px;
                width: 100%;
            }
            .error-msg {
                background: #fef2f2;
                color: #b91c1c;
                border: 1px solid #fee2e2;
                border-radius: 8px;
                padding: 11px 14px;
                font-size: 0.88rem;
                font-weight: 500;
                display: inline-flex;
                align-items: center;
                gap: 8px;
                width: fit-content;
            }
            .server-error {
                display: flex;
            }

            /* Đồ thị */
            .chart-card {
                background: #fff;
                border: 1px solid #ede0d8;
                border-radius: 12px;
                padding: 20px;
                margin-bottom: 20px;
            }
            .chart-card h3 {
                margin-top: 0;
                color: #76493b;
                font-size: 1.15rem;
            }

            /* Bảng dữ liệu */
            .table-card {
                background: #fff;
                border: 1px solid #ede0d8;
                border-radius: 12px;
                overflow: hidden;
            }
            table {
                width: 100%;
                border-collapse: collapse;
            }
            th {
                background: #faf6f2;
                padding: 12px 16px;
                text-align: left;
                font-size: 0.8rem;
                color: #76493b;
                text-transform: uppercase;
                letter-spacing: 0.04em;
                border-bottom: 1px solid #ede0d8;
            }
            td {
                padding: 12px 16px;
                border-bottom: 1px solid #f5ece4;
                font-size: 0.9rem;
                color: #4a3528;
                vertical-align: middle;
            }
            tr:last-child td {
                border-bottom: none;
            }

            /* CSS click trọn vẹn dòng TR bằng màng phủ stretched-link */
            .clickable-row {
                position: relative;
            }
            tbody tr.clickable-row:hover {
                background: #faf6f2;
            }
            .dish-click-link {
                color: #76493b;
                text-decoration: none;
                cursor: pointer;
            }
            .stretched-link::after {
                position: absolute;
                top: 0;
                right: 0;
                bottom: 0;
                left: 0;
                z-index: 1;
                content: "";
            }

            /* Nhãn trạng thái */
            .qty-badge {
                background: #f5ece4;
                color: #4a3528;
                padding: 4px 8px;
                border-radius: 12px;
                font-weight: bold;
                font-size: 0.85rem;
            }
            .status-tag {
                padding: 5px 12px;
                border-radius: 20px;
                font-size: 0.78rem;
                font-weight: 600;
                display: inline-block;
                text-transform: uppercase;
            }
            .tag-star {
                background-color: #d4edda;
                color: #155724;
            }
            .tag-plowhorse {
                background-color: #e4edff;
                color: #2456a6;
            }
            .tag-puzzle {
                background-color: #fff3cd;
                color: #856404;
            }
            .tag-dog {
                background-color: #f8d7da;
                color: #842029;
            }
            .empty {
                text-align: center;
                padding: 40px;
                color: #a0714f;
                font-size: 0.95rem;
            }

            /* Định dạng phân trang chuẩn cũ */
            .pagination {
                display: flex;
                gap: 6px;
                padding: 14px 16px;
                background: #fff;
                border-top: 1px solid #ede0d8;
                align-items: center;
                flex-wrap: wrap;
                justify-content: center;
            }
            .pagination a, .pagination span {
                padding: 6px 12px;
                border-radius: 6px;
                text-decoration: none;
                color: #76493b;
                font-size: 0.85rem;
                font-weight: 500;
                border: 1px solid #ede0d8;
            }
            .pagination a:hover {
                background: #f5ece4;
            }
            .pagination .disabled {
                background: #f5ece4;
                color: #a0714f;
                opacity: 0.6;
                cursor: not-allowed;
            }
            .pagination .page-info {
                border: none;
                background: transparent;
                color: #4a3528;
            }

            /* Khung ô cửa sổ hiển thị lịch sử (Modal) */
            .modal-overlay {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.4);
                display: none;
                justify-content: center;
                align-items: center;
                z-index: 9999;
            }
            .modal-overlay.active {
                display: flex;
            }
            .modal-box {
                background: #fff;
                padding: 24px;
                border-radius: 12px;
                border: 1px solid #ede0d8;
                width: 420px;
                max-height: 75vh;
                box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            }
            .modal-title {
                color: #76493b;
                font-size: 1.2rem;
                margin-top: 0;
                margin-bottom: 16px;
                font-weight: bold;
            }
            .btn-close-modal {
                float: right;
                text-decoration: none;
                color: #a0714f;
                font-weight: bold;
                font-size: 1.1rem;
            }
            .btn-close-modal:hover {
                color: #76493b;
            }

            /* Khối bọc riêng cái bảng để cuộn nội bộ, giữ tiêu đề đứng yên */
            .modal-table-container {
                max-height: 380px;
                overflow-y: auto;
                border: 1px solid #ede0d8;
                border-radius: 6px;
            }
            .modal-table {
                width: 100%;
                border-collapse: collapse;
            }
            /* Cố định thanh tiêu đề bảng khi cuộn */
            .modal-table th {
                position: sticky;
                top: 0;
                background: #faf6f2;
                color: #76493b;
                padding: 10px;
                font-size: 0.85rem;
                border-bottom: 1px solid #ede0d8;
                z-index: 10;
            }
            .modal-table td {
                padding: 10px;
                border-bottom: 1px solid #f5ece4;
                color: #4a3528;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div class="page-container">
            <%@ include file="/views/includes/dashboard.jsp" %>
            <main class="main">

                <div class="page-head">
                    <div>
                        <h1 class="page-title">Báo Cáo Hiệu Suất Thực Đơn</h1>
                    </div>
                </div>

                <div class="error-box" id="errorBox">
                    <c:if test="${not empty errorSearch}">
                        <div class="error-msg server-error" id="serverSearchError">
                            ${errorSearch}
                        </div>
                    </c:if>
                </div>

                <form id="filterForm" action="${pageContext.request.contextPath}/menu-performance" method="get" class="filter-bar">
                    <div class="quick-filter-group">
                        <button type="submit" name="filterType" value="today" class="btn-quick ${filterType == 'today' ? 'active' : ''}">Hôm nay</button>
                        <button type="submit" name="filterType" value="week" class="btn-quick ${filterType == 'week' ? 'active' : ''}">Tuần này</button>
                        <button type="submit" name="filterType" value="month" class="btn-quick ${filterType == 'month' ? 'active' : ''}">Tháng này</button>
                        <button type="submit" name="filterType" value="year" class="btn-quick ${filterType == 'year' ? 'active' : ''}">Năm nay</button>
                    </div>

                    <div class="field">
                        <label>Tìm tên món ăn</label>
                        <input type="text" id="searchInput" name="search" value="${search}" placeholder="Gõ tên món cần tìm..." />
                    </div>

                    <div class="field">
                        <label>Danh mục</label>
                        <select name="category">
                            <option value="0">TẤT CẢ DANH MỤC</option>
                            <c:forEach items="${categoryList}" var="cat">
                                <option value="${cat.categoryID}" ${selectedCategory == cat.categoryID ? 'selected' : ''}>${cat.categoryName}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="field">
                        <label>Cách chế biến</label>
                        <select name="cookingMethod">
                            <option value="0">TẤT CẢ CÁCH CHẾ BIẾN</option>
                            <c:forEach items="${methodList}" var="method">
                                <option value="${method.methodID}" ${selectedMethod == method.methodID ? 'selected' : ''}>${method.methodName}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="field">
                        <label>Từ ngày</label>
                        <input type="date" id="startDateInput" name="startDate" value="${startDate}" />
                    </div>

                    <div class="field">
                        <label>Đến ngày</label>
                        <input type="date" id="endDateInput" name="endDate" value="${endDate}" />
                    </div>

                    <button type="submit" class="btn btn-primary">Lọc dữ liệu</button>
                </form>

                <div class="chart-card">
                    <h3>Top 5 Sản Lượng Tiêu Thụ Cao Nhất</h3>
                    <canvas id="performanceChart" height="90"></canvas>
                </div>

                <div class="table-card">
                    <table>
                        <thead>
                            <tr>
                                <th>Tên Món Ăn</th>
                                <th>Danh Mục</th>
                                <th>Cách Chế Biến</th>
                                <th>Sản Lượng Đã Bán</th>
                                <th>Nhãn Hiệu Suất</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty menuItemList}">
                                    <c:forEach items="${menuItemList}" var="item">
                                        <tr class="clickable-row">
                                            <td>
                                                <a class="dish-click-link stretched-link" href="?page=${currentPage}&search=${search}&category=${selectedCategory}&cookingMethod=${selectedMethod}&filterType=${filterType}&startDate=${startDate}&endDate=${endDate}&viewHistoryItemID=${item.itemID}">
                                                    <strong>${item.itemName}</strong>
                                                </a>
                                            </td>
                                            <td>${item.categoryName}</td>
                                            <td>${item.methodName}</td>
                                            <td><span class="qty-badge">${item.totalQuantity} đĩa</span></td>
                                            <td><span class="status-tag ${item.tagClass}">${item.menuTag}</span></td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="5" class="empty">Không tìm thấy dữ liệu món ăn nào thỏa mãn bộ lọc.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>

                    <c:if test="${totalPage > 1}">
                        <div class="pagination">
                            <c:choose>
                                <c:when test="${currentPage > 1}">
                                    <a href="?page=1&search=${search}&category=${selectedCategory}&cookingMethod=${selectedMethod}&filterType=${filterType}&startDate=${startDate}&endDate=${endDate}">Đầu</a>
                                    <a href="?page=${currentPage - 1}&search=${search}&category=${selectedCategory}&cookingMethod=${selectedMethod}&filterType=${filterType}&startDate=${startDate}&endDate=${endDate}">Trước</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="disabled">Đầu</span>
                                    <span class="disabled">Trước</span>
                                </c:otherwise>
                            </c:choose>

                            <span class="page-info">Trang <b>${currentPage}</b> / ${totalPage}</span>

                            <c:choose>
                                <c:when test="${currentPage < totalPage}">
                                    <a href="?page=${currentPage + 1}&search=${search}&category=${selectedCategory}&cookingMethod=${selectedMethod}&filterType=${filterType}&startDate=${startDate}&endDate=${endDate}">Sau</a>
                                    <a href="?page=${totalPage}&search=${search}&category=${selectedCategory}&cookingMethod=${selectedMethod}&filterType=${filterType}&startDate=${startDate}&endDate=${endDate}">Cuối</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="disabled">Sau</span>
                                    <span class="disabled">Cuối</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>
                </div>

                <div class="modal-overlay ${not empty historyList ? 'active' : ''}">
                    <div class="modal-box">
                        <a href="?page=${currentPage}&search=${search}&category=${selectedCategory}&cookingMethod=${selectedMethod}&filterType=${filterType}&startDate=${startDate}&endDate=${endDate}" class="btn-close-modal">&times;</a>
                        <div class="modal-title">📈 Lịch Sử: ${currentViewDishName}</div>

                        <div class="modal-table-container">
                            <table class="modal-table">
                                <thead>
                                    <tr>
                                        <th>Ngày bán</th>
                                        <th>Sản lượng</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${historyList}" var="h">
                                        <tr>
                                            <td>${h.workingDate}</td>
                                            <td><span class="qty-badge">${h.totalQuantity} đĩa</span></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

            </main>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>

        <script>
            document.addEventListener("DOMContentLoaded", function () {
                const form = document.getElementById("filterForm");
                const searchInput = document.getElementById("searchInput");
                const startDateInput = document.getElementById("startDateInput");
                const endDateInput = document.getElementById("endDateInput");
                const errorBox = document.getElementById("errorBox");

                form.addEventListener("submit", function (event) {
                    const oldJsErrors = errorBox.querySelectorAll(".js-generated-error");
                    oldJsErrors.forEach(el => el.remove());

                    let hasError = false;
                    let errorMessages = [];

                    if (searchInput.value.trim().length > 100) {
                        hasError = true;
                        errorMessages.push("Tìm kiếm không vượt quá 100 kí tự.");
                    }

                    if (startDateInput.value && endDateInput.value) {
                        if (startDateInput.value > endDateInput.value) {
                            hasError = true;
                            errorMessages.push("Ngày bắt đầu không được lớn hơn ngày kết thúc.");
                        }
                    }

                    if (hasError) {
                        event.preventDefault();

                        const serverErr = document.getElementById("serverSearchError");
                        if (serverErr)
                            serverErr.style.display = 'none';

                        errorMessages.forEach(msg => {
                            const errDiv = document.createElement("div");
                            errDiv.className = "error-msg js-generated-error";
                            errDiv.innerHTML = msg;
                            errorBox.appendChild(errDiv);
                        });
                    }
                });

                const labels = [];
                const data = [];

            <c:forEach items="${topChartList}" var="item">
                <c:if test="${item.totalQuantity > 0}">
                labels.push("${item.itemName}");
                data.push(${item.totalQuantity});
                </c:if>
            </c:forEach>

                const ctx = document.getElementById("performanceChart");

                new Chart(ctx, {
                    type: "bar",
                    data: {
                        labels: labels,
                        datasets: [{
                                label: "Sản lượng",
                                data: data,
                                backgroundColor: 'rgba(118, 73, 59, 0.75)',
                                borderColor: 'rgba(118, 73, 59, 1)'
                            }]
                    }
                });
            });
        </script>
    </body>
</html>