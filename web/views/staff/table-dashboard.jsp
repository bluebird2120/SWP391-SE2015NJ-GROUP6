F<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Vận hành bàn</title>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
        <style>
            * {
                box-sizing: border-box;
            }
            body {
                margin: 0;
                font-family: Arial, sans-serif;
                background: #faf6f2;
                color: #3f3028;
            }
            .layout {
                display: flex;
            }
            .content {
                flex: 1;
                min-width: 0;
                padding: 26px;
            }
            h1 {
                margin: 0 0 6px;
                color: #76493b;
            }
            h2 {
                margin: 28px 0 12px;
                color: #5d3a2e;
                font-size: 1.15rem;
            }
            .sub {
                margin: 0 0 20px;
                color: #8a6e5a;
            }
            .message {
                padding: 12px 14px;
                border-radius: 8px;
                margin-bottom: 16px;
                background: #fff2cc;
            }
            .summary-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
                gap: 12px;
            }
            .summary-card, .panel {
                background: white;
                border: 1px solid #eaded6;
                border-radius: 12px;
                padding: 16px;
            }
            .summary-card h3 {
                margin: 0 0 12px;
                color: #76493b;
                font-size: 1rem;
            }
            .counts {
                display: grid;
                grid-template-columns: repeat(2, 1fr);
                gap: 8px;
                font-size: .88rem;
            }
            .count {
                background: #f8f2ed;
                padding: 8px;
                border-radius: 7px;
            }
            table {
                width: 100%;
                border-collapse: collapse;
            }
            th, td {
                padding: 11px 9px;
                text-align: left;
                border-bottom: 1px solid #eee2da;
                vertical-align: middle;
            }
            th {
                color: #76493b;
                background: #fbf7f3;
            }
            .badge {
                display: inline-block;
                padding: 4px 9px;
                border-radius: 999px;
                font-size: .78rem;
                font-weight: bold;
            }
            .available {
                background: #dff5e5;
                color: #176b36;
            }
            .reserved {
                background: #e4edff;
                color: #2456a6;
            }
            .serving {
                background: #fff0cc;
                color: #8a5700;
            }
            .cleaning {
                background: #f5e2ff;
                color: #71368a;
            }
            select {
                padding: 8px;
                border: 1px solid #d8c6ba;
                border-radius: 7px;
                min-width: 170px;
            }
            button {
                border: 0;
                border-radius: 7px;
                padding: 9px 12px;
                background: #76493b;
                color: white;
                cursor: pointer;
            }
            button.clean {
                background: #176b55;
            }
            .empty {
                color: #927b6d;
                padding: 18px 4px;
            }
            .inline {
                display: flex;
                gap: 8px;
                align-items: center;
                flex-wrap: wrap;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div class="layout">
            <%@ include file="/views/includes/dashboard.jsp" %>
            <main class="content">
                <h1>Vận hành bàn</h1>
                <p class="sub">Theo dõi bàn đặt trước, bàn đang phục vụ và bàn chờ dọn.</p>

                <c:if test="${not empty sessionScope.staffTableMessage}">
                    <div class="message">
                        <c:choose>
                            <c:when test="${sessionScope.staffTableMessage == 'assign_success'}">Đã gán bàn thành công.</c:when>
                            <c:when test="${sessionScope.staffTableMessage == 'clean_success'}">Đã xác nhận dọn bàn xong.</c:when>
                            <c:otherwise>${sessionScope.staffTableMessage}</c:otherwise>
                        </c:choose>
                    </div>
                    <c:remove var="staffTableMessage" scope="session"/>
                </c:if>

                <div class="summary-grid">
                    <c:forEach var="entry" items="${tableSummary}">
                        <div class="summary-card">
                            <h3>${entry.key}</h3>
                            <div class="counts">
                                <div class="count">Tổng bàn: <strong>${entry.value[0]}</strong></div>
                                <div class="count">Đang dùng: <strong>${entry.value[1]}</strong></div>
                                <div class="count">Đã gán trước: <strong>${entry.value[2]}</strong></div>
                                <div class="count">Chờ dọn: <strong>${entry.value[3]}</strong></div>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <h2>Đơn đặt trước cần gán bàn</h2>
                <div class="panel">
                    <table>
                        <thead>
                            <tr><th>Đơn</th><th>Giờ đến</th><th>Loại bàn</th><th>Đã gán</th><th>Thao tác</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="r" items="${reservationRequirements}">
                                <tr>
                                    <td>#${r.orderID}</td>
                                    <td><fmt:formatDate value="${r.orderTime}" pattern="HH:mm - dd/MM/yyyy"/></td>
                                    <td>${r.capacity} chỗ - ${r.areaType}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty r.assignedTableNames}">
                                                ${r.assignedTableNames}
                                            </c:when>
                                            <c:otherwise>
                                                <span class="empty">Chưa gán</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${r.remainingQuantity > 0}">
                                                <form method="post" action="${pageContext.request.contextPath}/staff/tables" class="inline">
                                                    <input type="hidden" name="action" value="assign">
                                                    <input type="hidden" name="orderID" value="${r.orderID}">
                                                    <select name="tableID" required>
                                                        <option value="">Chọn bàn phù hợp</option>
                                                        <c:forEach var="t" items="${physicalTables}">
                                                            <c:if test="${t.physicalStatus == 'available' && t.capacity == r.capacity && t.areaType == r.areaType}">
                                                                <option value="${t.tableID}">${t.tableName}</option>
                                                            </c:if>
                                                        </c:forEach>
                                                    </select>
                                                    <button type="submit" onclick="return confirm('Xác nhận gán bàn cho đơn #${r.orderID}?')">Gán bàn</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise><span class="badge serving">Đã gán đủ</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty reservationRequirements}">
                                <tr><td colspan="5" class="empty">Không có đơn đặt trước đang chờ gán bàn.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <h2>Trạng thái bàn vật lý</h2>
                <div class="panel">
                    <table>
                        <thead>
                            <tr><th>Bàn</th><th>Loại</th><th>Khu vực</th><th>Trạng thái</th><th>Đơn</th><th>Thao tác</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${physicalTables}">
                                <tr>
                                    <td>${t.tableName}</td>
                                    <td>${t.capacity} chỗ</td>
                                    <td>${t.areaType}</td>
                                    <td><span class="badge ${t.physicalStatus}">${t.physicalStatus}</span></td>
                                    <td><c:if test="${not empty t.orderID}">#${t.orderID}</c:if></td>
                                        <td>
                                        <c:if test="${t.physicalStatus == 'cleaning'}">
                                            <form method="post" action="${pageContext.request.contextPath}/staff/tables">
                                                <input type="hidden" name="action" value="cleaned">
                                                <input type="hidden" name="orderID" value="${t.orderID}">
                                                <button class="clean" type="submit"
                                                        onclick="return confirm('Xác nhận đã dọn xong toàn bộ bàn của đơn #${t.orderID}?')">
                                                    Đã dọn dẹp xong
                                                </button>
                                            </form>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </main>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>
