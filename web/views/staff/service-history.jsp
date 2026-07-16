<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Lịch sử phục vụ</title>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
        <style>
            * { box-sizing: border-box; }
            body {
                margin: 0;
                font-family: Arial, sans-serif;
                background: #faf6f2;
                color: #3f3028;
            }
            .layout { display: flex; }
            .content {
                flex: 1;
                min-width: 0;
                padding: 26px;
            }
            h1 {
                margin: 0 0 6px;
                color: #76493b;
            }
            .sub {
                color: #8a6e5a;
                margin-bottom: 20px;
            }
            .top-actions {
                display: flex;
                gap: 10px;
                margin-bottom: 16px;
                flex-wrap: wrap;
            }
            .filter-bar {
                display: flex;
                flex-wrap: wrap;
                gap: 12px;
                align-items: end;
                background: #fff;
                border: 1px solid #eaded6;
                border-radius: 12px;
                padding: 14px;
                margin-bottom: 16px;
            }
            .filter-group {
                display: flex;
                flex-direction: column;
                gap: 6px;
            }
            .filter-group label {
                font-size: 12px;
                font-weight: 700;
                color: #76493b;
                text-transform: uppercase;
            }
            .filter-group input {
                min-width: 190px;
                padding: 9px 10px;
                border: 1px solid #d9c9bf;
                border-radius: 7px;
                background: #fff;
            }
            .filter-btn {
                border: 0;
                border-radius: 7px;
                padding: 10px 14px;
                color: #fff;
                background: #76493b;
                text-decoration: none;
                cursor: pointer;
                display: inline-block;
            }
            .reset-btn {
                background: #d6b894;
                color: #5a3428;
            }
            .panel {
                background: #fff;
                border: 1px solid #eaded6;
                border-radius: 12px;
                overflow: hidden;
            }
            table {
                width: 100%;
                border-collapse: collapse;
            }
            th, td {
                padding: 13px;
                border-bottom: 1px solid #eee3dc;
                text-align: left;
            }
            th {
                background: #fbf7f4;
                color: #76493b;
            }
            .badge {
                display: inline-block;
                padding: 4px 9px;
                border-radius: 12px;
                background: #e8efe8;
            }
            .cleaning { background: #fff0c7; }
            .serving { background: #e4f4ff; }
            .reserved { background: #e8edff; }
            .pending { background: #ffe7c2; }
            .empty {
                padding: 28px;
                text-align: center;
                color: #8a6e5a;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div class="layout">
            <%@ include file="/views/includes/dashboard.jsp" %>
            <main class="content">
                <h1>Lịch sử phục vụ</h1>
                <p class="sub">Xem lại những đơn/bàn đã được hệ thống giao cho bạn.</p>

                <div class="top-actions">
                    <a class="filter-btn" href="${pageContext.request.contextPath}/staff/tables">
                        <i class="fa-solid fa-arrow-left"></i> Quay lại bàn đang phục vụ
                    </a>
                </div>

                <%-- [LICH SU BAN PHUC VU] Loc lai cac ban/don da duoc giao cho nhan vien nay. --%>
                <form class="filter-bar" method="get" action="${pageContext.request.contextPath}/staff/tables">
                    <input type="hidden" name="action" value="history">
                    <div class="filter-group">
                        <label>Ngày phục vụ</label>
                        <input type="date" name="date" value="${filterDate}">
                    </div>
                    <div class="filter-group">
                        <label>Mã đơn</label>
                        <input type="number" name="orderID" min="1" value="${filterOrderID}" placeholder="VD: 231">
                    </div>
                    <button type="submit" class="filter-btn">
                        <i class="fa-solid fa-magnifying-glass"></i> Tìm kiếm
                    </button>
                    <a class="filter-btn reset-btn" href="${pageContext.request.contextPath}/staff/tables?action=history">
                        <i class="fa-solid fa-rotate-left"></i> Hôm nay
                    </a>
                </form>

                <div class="panel">
                    <table>
                        <thead>
                            <tr><th>Đơn</th><th>Bàn</th><th>Khu vực</th><th>Thời gian</th><th>Trạng thái</th><th>Ghi chú</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${assignedTables}">
                                <tr>
                                    <td>#${t.orderID}</td>
                                    <td>${t.tableName} (${t.capacity} chỗ)</td>
                                    <td>${t.areaType}</td>
                                    <td><fmt:formatDate value="${t.orderTime}" pattern="HH:mm - dd/MM/yyyy"/></td>
                                    <td>
                                        <span class="badge ${t.physicalStatus}">
                                            <c:choose>
                                                <c:when test="${t.physicalStatus == 'available'}">Đã dọn xong</c:when>
                                                <c:when test="${t.physicalStatus == 'reserved'}">Đã đặt trước</c:when>
                                                <c:when test="${t.physicalStatus == 'serving'}">Đang phục vụ</c:when>
                                                <c:when test="${t.physicalStatus == 'cleaning'}">Chờ dọn</c:when>
                                                <c:when test="${t.physicalStatus == 'completed'}">Hoàn tất</c:when>
                                                <c:when test="${t.physicalStatus == 'pending'}">Chờ xác nhận</c:when>
                                                <c:otherwise>${t.physicalStatus}</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </td>
                                    <td>Đã lưu</td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty assignedTables}">
                                <tr><td colspan="6" class="empty">Không có lịch sử phục vụ phù hợp.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </main>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>
