<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tổng quan thống kê</title>
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: sans-serif; background: #faf6f2; }
        .page-container { display: flex; }
        .main { flex: 1; padding: 24px 32px; min-width: 0; }
        .page-head {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            gap: 12px;
            flex-wrap: wrap;
            margin-bottom: 18px;
        }
        .page-title { color: #76493b; font-size: 1.6rem; margin: 0; }
        .page-sub { color: #8a6e5a; margin: 6px 0 0; font-size: 0.92rem; }
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
        .quick-filter-group { display: flex; gap: 8px; width: 100%; margin-bottom: 8px; flex-wrap: wrap; }
        .btn-quick {
            padding: 6px 14px;
            border: 1px solid #d7bfa4;
            background: #fff;
            color: #76493b;
            border-radius: 6px;
            cursor: pointer;
            font-weight: 500;
            font-size: 0.85rem;
            text-decoration: none;
        }
        .btn-quick.active, .btn-quick:hover { background: #76493b; color: #fff; border-color: #76493b; }
        .filter-bar .field { display: flex; flex-direction: column; gap: 4px; }
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
            min-width: 180px;
        }
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
            gap: 6px;
        }
        .btn-primary { background: #76493b; color: #fff; }
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
            gap: 16px;
            margin-bottom: 20px;
        }
        .stat-card {
            background: #fff;
            border: 1px solid #ede0d8;
            border-radius: 12px;
            padding: 18px;
            box-shadow: 0 4px 12px rgba(118, 73, 59, 0.05);
        }
        .stat-label { color: #8a6e5a; font-size: 0.82rem; font-weight: 600; margin-bottom: 8px; }
        .stat-value { color: #76493b; font-size: 1.55rem; font-weight: 700; line-height: 1.2; }
        .stat-note { color: #a0714f; font-size: 0.78rem; margin-top: 6px; }
        .content-grid {
            display: grid;
            grid-template-columns: 1.2fr 0.8fr;
            gap: 18px;
        }
        .panel {
            background: #fff;
            border: 1px solid #ede0d8;
            border-radius: 12px;
            overflow: hidden;
        }
        .panel-head { padding: 16px 18px; border-bottom: 1px solid #ede0d8; }
        .panel-head h3 { margin: 0; color: #76493b; font-size: 1.05rem; }
        table { width: 100%; border-collapse: collapse; }
        th {
            background: #faf6f2;
            padding: 12px 16px;
            text-align: left;
            font-size: 0.78rem;
            color: #76493b;
            text-transform: uppercase;
            border-bottom: 1px solid #ede0d8;
        }
        td { padding: 12px 16px; border-bottom: 1px solid #f5ece4; color: #4a3528; font-size: 0.9rem; }
        tr:last-child td { border-bottom: none; }
        .summary-list { padding: 8px 18px 18px; }
        .summary-row { display: flex; justify-content: space-between; padding: 11px 0; border-bottom: 1px solid #f5ece4; }
        .summary-row:last-child { border-bottom: none; }
        .summary-label { color: #8a6e5a; }
        .summary-value { color: #76493b; font-weight: 700; }
        .alert {
            background: #fff7ed;
            color: #9a3412;
            border: 1px solid #fed7aa;
            border-radius: 8px;
            padding: 11px 14px;
            margin-bottom: 16px;
            font-size: 0.9rem;
        }
        .empty { color: #8a6e5a; padding: 18px; font-size: 0.9rem; }
        @media (max-width: 900px) { .content-grid { grid-template-columns: 1fr; } }
    </style>
</head>
<body>
    <%@ include file="/views/includes/header.jsp" %>
    <div class="page-container">
        <%@ include file="/views/includes/dashboard.jsp" %>
        <main class="main">
            <div class="page-head">
                <div>
                    <h1 class="page-title">Tổng quan thống kê</h1>
                    <p class="page-sub">Theo dõi nhanh tình hình doanh thu, đơn hàng và vận hành nhà hàng.</p>
                </div>
            </div>

            <c:if test="${not empty dashboardError}">
                <div class="alert">${dashboardError}</div>
            </c:if>
            <c:if test="${not empty dateMessage}">
                <div class="alert">${dateMessage}</div>
            </c:if>

            <form action="${pageContext.request.contextPath}/restaurant-analytics-dashboard" method="get" class="filter-bar">
                <div class="quick-filter-group">
                    <a class="btn-quick ${filterType == 'today' ? 'active' : ''}" href="${pageContext.request.contextPath}/restaurant-analytics-dashboard?filterType=today">Hôm nay</a>
                    <a class="btn-quick ${filterType == 'week' ? 'active' : ''}" href="${pageContext.request.contextPath}/restaurant-analytics-dashboard?filterType=week">Tuần này</a>
                    <a class="btn-quick ${filterType == 'month' ? 'active' : ''}" href="${pageContext.request.contextPath}/restaurant-analytics-dashboard?filterType=month">Tháng này</a>
                    <a class="btn-quick ${filterType == 'year' ? 'active' : ''}" href="${pageContext.request.contextPath}/restaurant-analytics-dashboard?filterType=year">Năm nay</a>
                </div>
                <input type="hidden" name="filterType" value="custom">
                <div class="field">
                    <label>Từ ngày</label>
                    <input type="date" name="startDate" value="${startDate}">
                </div>
                <div class="field">
                    <label>Đến ngày</label>
                    <input type="date" name="endDate" value="${endDate}">
                </div>
                <button type="submit" class="btn btn-primary">Lọc dữ liệu</button>
            </form>

            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-label">Doanh thu đã thanh toán</div>
                    <div class="stat-value"><fmt:formatNumber value="${totalRevenue}" type="number" groupingUsed="true" /> đ</div>
                    <div class="stat-note">Tính theo hóa đơn trạng thái paid</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Hóa đơn paid</div>
                    <div class="stat-value">${paidInvoices}</div>
                    <div class="stat-note">Số hóa đơn thanh toán trong kỳ</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Đơn hàng</div>
                    <div class="stat-value">${totalOrders}</div>
                    <div class="stat-note">Không tính đơn cancelled</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Đơn hoàn tất</div>
                    <div class="stat-value">${completedOrders}</div>
                    <div class="stat-note">Order status completed</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Bàn đang hoạt động</div>
                    <div class="stat-value">${activeTables}/${totalTables}</div>
                    <div class="stat-note">Bàn active trên tổng số bàn</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Nhân viên phục vụ active</div>
                    <div class="stat-value">${activeStaff}</div>
                    <div class="stat-note">Chỉ tính role Staff</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Đánh giá</div>
                    <div class="stat-value">${totalReviews}</div>
                    <div class="stat-note">Tổng đánh giá khách hàng</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Rating trung bình</div>
                    <div class="stat-value"><fmt:formatNumber value="${averageRating}" minFractionDigits="1" maxFractionDigits="1" /></div>
                    <div class="stat-note">Trung bình từ 1 đến 5 sao</div>
                </div>
            </div>

            <div class="content-grid">
                <section class="panel">
                    <div class="panel-head">
                        <h3>Top món bán chạy trong kỳ</h3>
                    </div>
                    <c:choose>
                        <c:when test="${not empty topDishes}">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Món ăn</th>
                                        <th>Danh mục</th>
                                        <th>Phương thức</th>
                                        <th>Số lượng</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="dish" items="${topDishes}">
                                        <tr>
                                            <td>${dish.itemName}</td>
                                            <td>${dish.categoryName}</td>
                                            <td>${dish.methodName}</td>
                                            <td>${dish.totalQuantity}</td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise>
                            <div class="empty">Chưa có dữ liệu món bán trong khoảng thời gian này.</div>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section class="panel">
                    <div class="panel-head">
                        <h3>Tóm tắt vận hành</h3>
                    </div>
                    <div class="summary-list">
                        <div class="summary-row">
                            <span class="summary-label">Khoảng thời gian</span>
                            <span class="summary-value">${startDate} → ${endDate}</span>
                        </div>
                        <div class="summary-row">
                            <span class="summary-label">Tỷ lệ đơn hoàn tất</span>
                            <span class="summary-value">
                                <c:choose>
                                    <c:when test="${totalOrders > 0}">
                                        <fmt:formatNumber value="${completedOrders * 100.0 / totalOrders}" maxFractionDigits="1" />%
                                    </c:when>
                                    <c:otherwise>0%</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="summary-row">
                            <span class="summary-label">Doanh thu trung bình / hóa đơn</span>
                            <span class="summary-value">
                                <c:choose>
                                    <c:when test="${paidInvoices > 0}">
                                        <fmt:formatNumber value="${totalRevenue / paidInvoices}" type="number" groupingUsed="true" /> đ
                                    </c:when>
                                    <c:otherwise>0 đ</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="summary-row">
                            <span class="summary-label">Tỷ lệ bàn active</span>
                            <span class="summary-value">
                                <c:choose>
                                    <c:when test="${totalTables > 0}">
                                        <fmt:formatNumber value="${activeTables * 100.0 / totalTables}" maxFractionDigits="1" />%
                                    </c:when>
                                    <c:otherwise>0%</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>
                </section>
            </div>
        </main>
    </div>
</body>
</html>
