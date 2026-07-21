<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Phân tích giờ cao điểm</title>
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
            .page-container {
                display: flex;
            }
            .main {
                flex: 1;
                padding: 24px 32px;
                min-width: 0;
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
            .page-sub   {
                color: #a0714f;
                font-size: 0.95rem;
                margin: 4px 0 0;
            }

            /* ── BỘ LỌC ── */
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
            .filter-bar input {
                padding: 8px 12px;
                border: 1px solid #d7bfa4;
                border-radius: 7px;
                font-family: inherit;
                font-size: 0.9rem;
            }
            .filter-bar input:focus {
                outline: none;
                border-color: #76493b;
            }

            .btn {
                padding: 9px 16px;
                border-radius: 8px;
                border: none;
                cursor: pointer;
                font-family: inherit;
                font-size: 0.88rem;
                font-weight: 600;
                display: inline-flex;
                align-items: center;
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

            /* ── METRIC CARDS ── */
            .metric-row {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 14px;
                margin-bottom: 20px;
            }
            .metric-card {
                background: #fff;
                border: 1px solid #ede0d8;
                border-radius: 12px;
                padding: 16px 18px;
            }
            .metric-label {
                font-size: 0.75rem;
                color: #a0714f;
                text-transform: uppercase;
                letter-spacing: 0.05em;
                margin-bottom: 6px;
            }
            .metric-value {
                font-size: 1.5rem;
                font-weight: 700;
                color: #76493b;
                margin-bottom: 2px;
            }
            .metric-sub {
                font-size: 0.82rem;
                color: #8a6e5a;
            }

            /* ── CHART CARD ── */
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
                font-size: 1.1rem;
                margin-bottom: 16px;
            }

            /* ── TABLE ── */
            .table-card {
                background: #fff;
                border: 1px solid #ede0d8;
                border-radius: 12px;
                overflow: hidden;
            }
            .table-card h3 {
                margin: 0;
                padding: 16px 20px;
                color: #76493b;
                font-size: 1.1rem;
                border-bottom: 1px solid #ede0d8;
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
            }
            tr:last-child td {
                border-bottom: none;
            }

            /* ── BADGE MỨC ĐỘ ── */
            .badge {
                padding: 4px 10px;
                border-radius: 20px;
                font-size: 0.78rem;
                font-weight: 600;
                display: inline-block;
            }
            .badge-high   {
                background: #f8d7da;
                color: #842029;
            }
            .badge-medium {
                background: #fff3cd;
                color: #856404;
            }
            .badge-low    {
                background: #e4edff;
                color: #2456a6;
            }

            /* ── PROGRESS BAR ── */
            .bar-wrap {
                background: #f5ece4;
                border-radius: 4px;
                height: 8px;
                width: 120px;
                overflow: hidden;
                display: inline-block;
                vertical-align: middle;
            }
            .bar-fill {
                height: 100%;
                border-radius: 4px;
                background: #76493b;
            }

            /* ── ERROR ── */
            .error-msg {
                background: #fef2f2;
                color: #b91c1c;
                border: 1px solid #fee2e2;
                border-radius: 8px;
                padding: 11px 14px;
                font-size: 0.88rem;
                font-weight: 500;
                margin-bottom: 16px;
            }

            .empty {
                text-align: center;
                padding: 40px;
                color: #a0714f;
                font-size: 0.95rem;
            }

            @media (max-width: 900px) {
                .metric-row {
                    grid-template-columns: repeat(2, 1fr);
                }
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
                        <h1 class="page-title">Phân tích giờ cao điểm</h1>
                        <p class="page-sub">Thống kê lượng đơn theo khung giờ trong ngày</p>
                    </div>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="error-msg">${errorMsg}</div>
                </c:if>

                <%-- BỘ LỌC --%>
                <form action="${pageContext.request.contextPath}/owner/peak-hours-analysis"
                      method="get" class="filter-bar" id="filterForm">

                    <div class="quick-filter-group">
                        <button type="submit" name="filterType" value="today"
                                class="btn-quick ${filterType == 'today' ? 'active' : ''}">Hôm nay</button>
                        <button type="submit" name="filterType" value="week"
                                class="btn-quick ${filterType == 'week' ? 'active' : ''}">Tuần này</button>
                        <button type="submit" name="filterType" value="month"
                                class="btn-quick ${filterType == 'month' ? 'active' : ''}">Tháng này</button>
                        <button type="submit" name="filterType" value="year"
                                class="btn-quick ${filterType == 'year' ? 'active' : ''}">Năm nay</button>
                    </div>

                    <div class="field">
                        <label>Từ ngày</label>
                        <input type="date" name="startDate" id="startDate" value="${startDate}">
                    </div>
                    <div class="field">
                        <label>Đến ngày</label>
                        <input type="date" name="endDate" id="endDate" value="${endDate}">
                    </div>

                    <button type="submit" name="filterType" value="custom" class="btn btn-primary">
                        Lọc theo ngày
                    </button>
                </form>

                <%-- METRIC CARDS --%>
                <div class="metric-row">
                    <div class="metric-card">
                        <div class="metric-label">Giờ cao điểm nhất</div>
                        <c:choose>
                            <c:when test="${peakHour >= 0}">
                                <div class="metric-value">${peakHour}:00</div>
                                <div class="metric-sub">${peakCount} đơn</div>
                            </c:when>
                            <c:otherwise>
                                <div class="metric-value">--</div>
                                <div class="metric-sub">Chưa có dữ liệu</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="metric-card">
                        <div class="metric-label">Tổng đơn</div>
                        <div class="metric-value">${totalOrders}</div>
                        <div class="metric-sub">đơn trong kỳ</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-label">Trung bình / giờ</div>
                        <div class="metric-value">${avgPerHour}</div>
                        <div class="metric-sub">đơn / giờ hoạt động</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-label">Số khung giờ có đơn</div>
                        <div class="metric-value">${hourStats.size()}</div>
                        <div class="metric-sub">/ 24 khung giờ</div>
                    </div>
                </div>

                <%-- BIỂU ĐỒ --%>
                <div class="chart-card">
                    <h3>Số đơn theo giờ</h3>
                    <c:choose>
                        <c:when test="${not empty hourStats}">
                            <canvas id="peakChart" height="80"></canvas>
                            </c:when>
                            <c:otherwise>
                            <div class="empty">Không có dữ liệu trong kỳ đã chọn.</div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <%-- BẢNG CHI TIẾT --%>
                <div class="table-card">
                    <h3>Chi tiết theo khung giờ</h3>
                    <table>
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Khung giờ</th>
                                <th>Số đơn</th>
                                <th>Tỷ lệ</th>
                                <th>So sánh</th>
                                <th>Mức độ</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty hourStats}">
                                    <c:forEach items="${hourStats}" var="s" varStatus="loop">
                                        <c:set var="pct" value="${totalOrders > 0 ? (s.orderCount * 100 / totalOrders) : 0}" />
                                        <c:set var="ratio" value="${peakCount > 0 ? (s.orderCount * 100 / peakCount) : 0}" />
                                        <tr>
                                            <td style="color:#a0714f;">${loop.index + 1}</td>
                                            <td><strong>${s.hour}:00 – ${s.hour + 1}:00</strong></td>
                                            <td><strong>${s.orderCount}</strong> đơn</td>
                                            <td>
                                                <strong>
                                                    <fmt:formatNumber value="${pct}" maxFractionDigits="1"/>%
                                                </strong>
                                            </td>

                                            <td>
                                                <div class="bar-wrap">
                                                    <div class="bar-fill" style="width:${ratio}%;
                                                         background: ${ratio >= 85 ? '#e34948' : ratio >= 60 ? '#eda100' : '#76493b'};"></div>
                                                </div>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${ratio >= 85}">
                                                        <span class="badge badge-high">Rất cao</span>
                                                    </c:when>
                                                    <c:when test="${ratio >= 60}">
                                                        <span class="badge badge-medium">Cao</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge-low">Trung bình</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="6" class="empty">Không có dữ liệu trong kỳ đã chọn.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

            </main>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>

        <script>
            // ── Validate ngày trước submit ──────────────────────────────
            document.getElementById('filterForm').addEventListener('submit', function (e) {
                const s = document.getElementById('startDate').value;
                const d = document.getElementById('endDate').value;
                if (s && d && s > d) {
                    e.preventDefault();
                    alert('Ngày bắt đầu không được lớn hơn ngày kết thúc.');
                }
            });

            // ── Chart.js ────────────────────────────────────────────────
            const hourLabels = [];
            const hourData = [];

            <c:forEach items="${hourStats}" var="s">
            hourLabels.push('${s.hour}:00');
            hourData.push(${s.orderCount});
            </c:forEach>

            if (hourLabels.length > 0) {
                const maxVal = Math.max(...hourData);
                const barColors = hourData.map(v => {
                    const r = v / maxVal;
                    if (r >= 0.85)
                        return 'rgba(227, 73, 72, 0.8)';
                    if (r >= 0.60)
                        return 'rgba(237, 161, 0, 0.8)';
                    return 'rgba(118, 73, 59, 0.75)';
                });

                new Chart(document.getElementById('peakChart'), {
                    type: 'bar',
                    data: {
                        labels: hourLabels,
                        datasets: [{
                                label: 'Số đơn',
                                data: hourData,
                                backgroundColor: barColors,
                                borderRadius: 4,
                                borderSkipped: 'bottom'
                            }]
                    },
                    options: {
                        responsive: true,
                        plugins: {
                            legend: {display: false},
                            tooltip: {
                                callbacks: {
                                    label: ctx => ' ' + ctx.raw + ' đơn'
                                }
                            }
                        },
                        scales: {
                            x: {
                                grid: {display: false},
                                ticks: {color: '#8a6e5a', font: {size: 11}}
                            },
                            y: {
                                beginAtZero: true,
                                ticks: {
                                    color: '#8a6e5a',
                                    font: {size: 11},
                                    stepSize: 1
                                },
                                grid: {color: '#f5ece4'}
                            }
                        }
                    }
                });
            }
        </script>
    </body>
</html>