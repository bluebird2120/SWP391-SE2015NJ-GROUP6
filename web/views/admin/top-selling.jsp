<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Báo Cáo Hiệu Suất Món Ăn</title>
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <style>
            body {
                font-family: 'Segoe UI', sans-serif;
                background-color: #f4f6f9;
                margin: 0;
                padding: 20px;
            }
            .dashboard-container {
                max-width: 1200px;
                margin: 0 auto;
                background: #fff;
                padding: 25px;
                border-radius: 10px;
                box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            }
            h2 {
                color: #2c3e50;
            }
            .filter-form {
                background: #f8f9fa;
                padding: 20px;
                border-radius: 8px;
                border: 1px solid #e9ecef;
                margin-bottom: 30px;
            }
            .quick-filter-group, .advanced-filter-group, .date-picker-group {
                display: flex;
                gap: 12px;
                align-items: center;
                margin-bottom: 15px;
                flex-wrap: wrap;
            }
            .btn-quick {
                padding: 8px 16px;
                border: 1px solid #007bff;
                background: #fff;
                color: #007bff;
                border-radius: 4px;
                cursor: pointer;
                font-weight: 500;
            }
            .btn-quick.active, .btn-quick:hover {
                background: #007bff;
                color: #fff;
            }
            .input-text, .select-box, .input-date {
                padding: 8px 12px;
                border: 1px solid #ced4da;
                border-radius: 4px;
            }
            .input-text {
                width: 250px;
            }
            .select-box {
                width: 200px;
            }
            .btn-submit {
                padding: 8px 20px;
                background: #28a745;
                color: white;
                border: none;
                border-radius: 4px;
                cursor: pointer;
                font-weight: bold;
            }
            .chart-box {
                background: #fff;
                border: 1px solid #e3e6f0;
                padding: 20px;
                border-radius: 8px;
                margin-bottom: 30px;
            }
            .report-table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 20px;
            }
            .report-table th, .report-table td {
                padding: 12px 15px;
                text-align: left;
                border-bottom: 1px solid #dee2e6;
            }
            .report-table th {
                background-color: #f1f3f5;
            }
            .qty-badge {
                background: #e2e3e5;
                padding: 4px 8px;
                border-radius: 12px;
                font-weight: bold;
            }
            .status-tag {
                padding: 6px 12px;
                border-radius: 20px;
                font-size: 13px;
                font-weight: bold;
                display: inline-block;
            }
            .tag-star {
                background-color: #d4edda;
                color: #155724;
            }
            .tag-plowhorse {
                background-color: #cce5ff;
                color: #004085;
            }
            .tag-puzzle {
                background-color: #fff3cd;
                color: #856404;
            }
            .tag-dog {
                background-color: #f8d7da;
                color: #721c24;
            }
            .no-data {
                text-align: center;
                color: #868e96;
                padding: 30px !important;
            }
            .pagination {
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 15px;
                margin-top: 25px;
            }
            .pagination a {
                padding: 8px 16px;
                border: 1px solid #dee2e6;
                color: #007bff;
                text-decoration: none;
                border-radius: 4px;
            }
        </style>
    </head>
    <body>
        <div class="dashboard-container">
            <h2>📊 Báo Cáo Hiệu Suất & Phân Nhóm Menu</h2>

            <form action="${pageContext.request.contextPath}/menu-performance" method="get" class="filter-form">
                <div class="quick-filter-group">
                    <button type="submit" name="filterType" value="today" class="btn-quick ${filterType == 'today' ? 'active' : ''}">Hôm nay</button>
                    <button type="submit" name="filterType" value="week" class="btn-quick ${filterType == 'week' ? 'active' : ''}">Tuần này</button>
                    <button type="submit" name="filterType" value="month" class="btn-quick ${filterType == 'month' ? 'active' : ''}">Tháng này</button>
                    <button type="submit" name="filterType" value="year" class="btn-quick ${filterType == 'year' ? 'active' : ''}">Năm nay</button>
                </div>

                <div class="advanced-filter-group">
                    <input type="text" name="search" value="${search}" placeholder="Tìm tên món ăn..." class="input-text" />

                    <select name="category" class="select-box">
                        <option value="0">--- Chọn danh mục ---</option>
                        <c:forEach items="${categoryList}" var="cat">
                            <option value="${cat.categoryID}" ${selectedCategory == cat.categoryID ? 'selected' : ''}>${cat.categoryName}</option>
                        </c:forEach>
                    </select>

                    <select name="cookingMethod" class="select-box">
                        <option value="0">--- Cách chế biến ---</option>
                        <c:forEach items="${methodList}" var="method">
                            <option value="${method.methodID}" ${selectedMethod == method.methodID ? 'selected' : ''}>${method.methodName}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="date-picker-group">
                    <label>Từ ngày:</label>
                    <input type="date" name="startDate" value="${startDate}" class="input-date" />
                    <label>Đến ngày:</label>
                    <input type="date" name="endDate" value="${endDate}" class="input-date" />
                    <button type="submit" class="btn-submit">Áp Dụng Bộ Lọc</button>
                </div>
            </form>

            <c:if test="${not empty errorSearch}">
                <p style="color: #dc3545; font-size: 14px; margin-top: -10px; margin-bottom: 15px;">${errorSearch}</p>
            </c:if>

            <div class="chart-box">
                <h3>📈 Top Sản Lượng Tiêu Thụ Xuất Sắc</h3>
                <canvas id="performanceChart" height="100"></canvas>
            </div>

            <div class="table-box">
                <table class="report-table">
                    <thead>
                        <tr>
                            <th>Tên Món Ăn</th>
                            <th>Danh Mục</th>
                            <th>Cách Chế Biến</th>
                            <th>Số Lượng Đã Bán</th>
                            <th>Trạng Thái Hiệu Suất</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty menuItemList}">
                                <c:forEach items="${menuItemList}" var="item">
                                    <tr>
                                        <td><strong>${item.itemName}</strong></td>
                                        <td>${item.categoryName}</td>
                                        <td>${item.methodName}</td>
                                        <td><span class="qty-badge">${item.totalQuantity} đĩa</span></td>
                                        <td><span class="status-tag ${item.tagClass}">${item.menuTag}</span></td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="5" class="no-data">Không tìm thấy dữ liệu món ăn thỏa mãn.</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <c:if test="${totalPage > 1}">
                <div class="pagination">
                    <c:if test="${currentPage > 1}">
                        <a href="?page=${currentPage - 1}&search=${search}&category=${selectedCategory}&cookingMethod=${selectedMethod}&filterType=${filterType}&startDate=${startDate}&endDate=${endDate}">Trước</a>
                    </c:if>
                    <span>Trang <b>${currentPage}</b> / ${totalPage}</span>
                    <c:if test="${currentPage < totalPage}">
                        <a href="?page=${currentPage + 1}&search=${search}&category=${selectedCategory}&cookingMethod=${selectedMethod}&filterType=${filterType}&startDate=${startDate}&endDate=${endDate}">Sau</a>
                    </c:if>
                </div>
            </c:if>
        </div>

        <script>
            document.addEventListener("DOMContentLoaded", function () {
                const chartLabels = [];
                const chartData = [];

                // 🌟 Sử dụng topChartList cố định từ Servlet sang để bốc Top 5, không bị lệch khi bấm đổi trang của bảng
            <c:forEach items="${topChartList}" var="item">
                if (${item.totalQuantity > 0}) {
                    chartLabels.push('${item.itemName}');
                    chartData.push(${item.totalQuantity});
                }
            </c:forEach>

                if (chartData.length > 0) {
                    const ctx = document.getElementById('performanceChart').getContext('2d');
                    new Chart(ctx, {
                        type: 'bar',
                        data: {
                            labels: chartLabels,
                            datasets: [{
                                    label: 'Sản lượng tiêu thụ (đĩa)',
                                    data: chartData,
                                    backgroundColor: 'rgba(54, 162, 235, 0.7)',
                                    borderColor: 'rgba(54, 162, 235, 1)',
                                    borderWidth: 1
                                }]
                        },
                        options: {responsive: true, scales: {y: {beginAtZero: true}}}
                    });
                }
            });
        </script>
    </body>
</html>