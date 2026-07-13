<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Giờ hoạt động</title>
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
                padding: 28px;
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
            .panel {
                background: #fff;
                border: 1px solid #eaded6;
                border-radius: 12px;
                padding: 16px;
                margin-bottom: 18px;
            }
            table {
                width: 100%;
                border-collapse: collapse;
            }
            th, td {
                padding: 11px 9px;
                border-bottom: 1px solid #eee2da;
                text-align: left;
                vertical-align: middle;
            }
            th {
                background: #fbf7f3;
                color: #76493b;
            }
            input[type="time"], input[type="date"], input[type="text"] {
                padding: 8px;
                border: 1px solid #d8c6ba;
                border-radius: 7px;
            }
            input[type="text"] { min-width: 220px; }
            button {
                border: 0;
                border-radius: 7px;
                padding: 9px 12px;
                background: #76493b;
                color: #fff;
                cursor: pointer;
                font-weight: 600;
            }
            button.danger { background: #dc3545; }
            .inline {
                display: flex;
                gap: 8px;
                align-items: center;
                flex-wrap: wrap;
            }
            .badge {
                display: inline-block;
                padding: 4px 9px;
                border-radius: 999px;
                font-size: .78rem;
                font-weight: bold;
            }
            .open { background: #dff5e5; color: #176b36; }
            .closed { background: #ffe0e0; color: #9b1c1c; }
            .message {
                padding: 12px 14px;
                border-radius: 8px;
                margin-bottom: 16px;
                background: #fff2cc;
            }
            .readonly {
                color: #8a6e5a;
                font-style: italic;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div class="layout">
            <%@ include file="/views/includes/dashboard.jsp" %>
            <main class="content">

                <h1>Giờ hoạt động</h1>
                <p class="sub">
                    Thiết lập giờ mở cửa, đóng cửa và ngày nghỉ để hệ thống kiểm tra khi khách đặt bàn.
                </p>

                <c:if test="${not empty pageMsg}">
                    <div class="message">
                        <c:choose>
                            <c:when test="${pageMsg == 'saved'}">Đã lưu lịch hoạt động.</c:when>
                            <c:when test="${pageMsg == 'deleted'}">Đã xóa ngày đặc biệt.</c:when>
                            <c:when test="${pageMsg == 'forbidden'}">Bạn chỉ có quyền xem, không được cập nhật.</c:when>
                            <c:otherwise>Không thể xử lý yêu cầu. Vui lòng kiểm tra lại dữ liệu.</c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <h2>Lịch hoạt động theo tuần</h2>
                <div class="panel">
                    <table>
                        <thead>
                            <tr>
                                <th>Thứ</th>
                                <th>Mở cửa</th>
                                <th>Đóng cửa</th>
                                <th>Trạng thái</th>
                                <th>Ghi chú</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>

                            <c:forEach var="entry" items="${weeklySchedules}">
                                <c:set var="s" value="${entry.value}" />
                                <fmt:formatDate var="openValue" value="${s.openTime}" pattern="HH:mm"/>
                                <fmt:formatDate var="closeValue" value="${s.closeTime}" pattern="HH:mm"/>
                                <tr>

                                    <form method="post" action="${pageContext.request.contextPath}/business-hours">

                                        <input type="hidden" name="action" value="saveWeekly">

                                        <input type="hidden" name="dayOfWeek" value="${entry.key}">
                                        <td>${entry.key}</td>
                                        <td>

                                            <input type="time" name="openTime"
                                                   value="${openValue}"
                                                   ${!isOwner ? 'disabled' : ''}>
                                        </td>
                                        <td>

                                            <input type="time" name="closeTime"
                                                   value="${closeValue}"
                                                   ${!isOwner ? 'disabled' : ''}>
                                        </td>
                                        <td>
                                            <label class="inline">

                                                <input type="checkbox" name="isClosed" value="1"
                                                       ${s.isClosed == 1 ? 'checked' : ''}
                                                       ${!isOwner ? 'disabled' : ''}>
                                                <span class="badge ${s.isClosed == 1 ? 'closed' : 'open'}">
                                                    ${s.isClosed == 1 ? 'Nghỉ' : 'Mở'}
                                                </span>
                                            </label>
                                        </td>
                                        <td>

                                            <input type="text" name="reason" value="${s.reason}"
                                                   placeholder="Lý do / ghi chú"
                                                   ${!isOwner ? 'disabled' : ''}>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${isOwner}">

                                                    <button type="submit">Lưu</button>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="readonly">Chỉ xem</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </form>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <h2>Ngày đặc biệt</h2>
                <div class="panel">
                    <c:if test="${isOwner}">


                        <form method="post" action="${pageContext.request.contextPath}/business-hours" class="inline" style="margin-bottom:14px;">

                            <input type="hidden" name="action" value="saveSpecial">

                            <input type="date" name="specificDate" required>

                            <input type="time" name="openTime">
                            <input type="time" name="closeTime">
                            <label class="inline">
                                <input type="checkbox" name="isClosed" value="1">
                                Nghỉ cả ngày
                            </label>
                            <input type="text" name="reason" placeholder="Lý do / ghi chú">
                            <button type="submit">Thêm / cập nhật</button>
                        </form>
                    </c:if>

                    <table>
                        <thead>
                            <tr>
                                <th>Ngày</th>
                                <th>Mở cửa</th>
                                <th>Đóng cửa</th>
                                <th>Trạng thái</th>
                                <th>Lý do</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>

                            <c:forEach var="s" items="${specialSchedules}">
                                <tr>
                                    <td><fmt:formatDate value="${s.specificDate}" pattern="dd/MM/yyyy"/></td>
                                    <td><fmt:formatDate value="${s.openTime}" pattern="HH:mm"/></td>
                                    <td><fmt:formatDate value="${s.closeTime}" pattern="HH:mm"/></td>
                                    <td>
                                        <span class="badge ${s.isClosed == 1 ? 'closed' : 'open'}">
                                            ${s.isClosed == 1 ? 'Nghỉ' : 'Mở'}
                                        </span>
                                    </td>
                                    <td>${s.reason}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${isOwner}">

                                                <form method="post" action="${pageContext.request.contextPath}/business-hours"
                                                      onsubmit="return confirm('Xóa ngày đặc biệt này?')">

                                                    <input type="hidden" name="action" value="deleteSpecial">

                                                    <input type="hidden" name="scheduleID" value="${s.scheduleID}">
                                                    <button class="danger" type="submit">Xóa</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="readonly">Chỉ xem</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty specialSchedules}">
                                <tr><td colspan="6" class="readonly">Chưa có ngày đặc biệt.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </main>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>
