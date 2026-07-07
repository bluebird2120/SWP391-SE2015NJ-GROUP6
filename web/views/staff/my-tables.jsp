<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Bàn tôi phục vụ</title>
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
            .sub {
                color: #8a6e5a;
                margin-bottom: 20px;
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
            .cleaning {
                background: #fff0c7;
            }
            button {
                border: 0;
                border-radius: 7px;
                padding: 8px 12px;
                color: #fff;
                background: #2f855a;
                cursor: pointer;
            }
            .message {
                padding: 12px;
                margin-bottom: 15px;
                background: #fff2cc;
                border-radius: 8px;
            }
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
                <h1>Bàn tôi đang phục vụ</h1>
                <p class="sub">Chỉ hiển thị những đơn được hệ thống giao cho bạn.</p>

                <c:if test="${not empty sessionScope.staffTableMessage}">
                    <div class="message">
                        <c:choose>
                            <c:when test="${sessionScope.staffTableMessage == 'clean_success'}">Đã xác nhận dọn bàn xong.</c:when>
                            <c:otherwise>${sessionScope.staffTableMessage}</c:otherwise>
                        </c:choose>
                    </div>
                    <c:remove var="staffTableMessage" scope="session"/>
                </c:if>

                <%-- [PHAN QUYEN PHUC VU] Danh sach nay da duoc loc theo employeeID tai DAO. --%>
                <div class="panel">
                    <table>
                        <thead>
                            <tr><th>Đơn</th><th>Bàn</th><th>Khu vực</th><th>Thời gian</th><th>Trạng thái</th><th>Thao tác</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${assignedTables}">
                                <tr>
                                    <td>#${t.orderID}</td>
                                    <td>${t.tableName} (${t.capacity} chỗ)</td>
                                    <td>${t.areaType}</td>
                                    <td><fmt:formatDate value="${t.orderTime}" pattern="HH:mm - dd/MM/yyyy"/></td>
                                    <td><span class="badge ${t.physicalStatus}">${t.physicalStatus}</span></td>
                                    <td>
                                        <c:if test="${t.physicalStatus == 'cleaning'}">
                                            <form method="post" action="${pageContext.request.contextPath}/staff/tables">
                                                <input type="hidden" name="action" value="cleaned">
                                                <input type="hidden" name="orderID" value="${t.orderID}">
                                                <button type="submit" onclick="return confirm('Xác nhận đã dọn xong bàn này?')">
                                                    Đã dọn dẹp xong
                                                </button>
                                            </form>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty assignedTables}">
                                <tr><td colspan="6" class="empty">Bạn chưa có bàn nào đang phục vụ.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </main>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>
