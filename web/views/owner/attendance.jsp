<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Attendance</title>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
        <style>
            * {
                box-sizing: border-box;
            }
            body {
                margin:0;
                font-family:'Inter',sans-serif;
                background:#faf6f2;
            }
            .main {
                flex:1;
                padding:24px 32px;
            }
            .page-title {
                font-family:'Playfair Display',serif;
                color:#76493b;
                font-size:1.6rem;
                margin:0 0 4px;
            }
            .page-sub {
                color:#a0714f;
                font-size:0.9rem;
                margin-bottom:18px;
            }
            .card {
                background:#fff;
                border:1px solid #ede0d8;
                border-radius:12px;
                padding:18px;
                margin-bottom:16px;
            }
            .field {
                display:flex;
                flex-direction:column;
                gap:4px;
            }
            label {
                font-size:0.78rem;
                font-weight:600;
                color:#8a6e5a;
                text-transform:uppercase;
                letter-spacing:0.04em;
            }
            input {
                padding:9px 12px;
                border:1px solid #d7bfa4;
                border-radius:7px;
                font-family:inherit;
                font-size:0.9rem;
            }
            .btn {
                padding:7px 12px;
                border-radius:7px;
                border:none;
                cursor:pointer;
                font-size:0.82rem;
                font-weight:600;
                text-decoration:none;
                display:inline-flex;
                gap:5px;
                align-items:center;
            }
            .btn-checkin  {
                background:#198754;
                color:#fff;
            }
            .btn-checkout {
                background:#0d6efd;
                color:#fff;
            }
            .btn-absent   {
                background:#dc3545;
                color:#fff;
            }
            .btn-reset    {
                background:#6c757d;
                color:#fff;
            }
            .btn-disabled {
                background:#e9ecef;
                color:#6c757d;
                cursor:not-allowed;
            }
            table {
                width:100%;
                border-collapse:collapse;
            }
            th {
                background:#faf6f2;
                padding:11px;
                text-align:left;
                font-size:0.78rem;
                color:#76493b;
                text-transform:uppercase;
            }
            td {
                padding:11px;
                border-bottom:1px solid #f5ece4;
                font-size:0.9rem;
                color:#4a3528;
                vertical-align:middle;
            }
            tr:hover {
                background:#faf6f2;
            }
            .badge {
                padding:3px 9px;
                border-radius:12px;
                font-size:0.72rem;
                font-weight:600;
            }
            .badge-scheduled {
                background:#e2e3e5;
                color:#41464b;
            }
            .badge-present {
                background:#d4edda;
                color:#155724;
            }
            .badge-late {
                background:#fff3cd;
                color:#856404;
            }
            .badge-absent {
                background:#f8d7da;
                color:#721c24;
            }
            .alert {
                padding:11px 14px;
                border-radius:8px;
                margin-bottom:14px;
                font-size:0.88rem;
            }
            .alert-error {
                background:#f8d7da;
                color:#721c24;
                border:1px solid #f5c2c7;
            }
            .alert-success {
                background:#d4edda;
                color:#155724;
                border:1px solid #c3e6cb;
            }
            .alert-info {
                background:#cff4fc;
                color:#055160;
                border:1px solid #b6effb;
            }
            .actions form {
                display:inline;
                margin-right:4px;
            }
            .readonly-tag {
                color:#8a6e5a;
                font-style:italic;
                font-size:0.82rem;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div style="display:flex;">
            <%@ include file="/views/includes/dashboard.jsp" %>
            <main class="main">
                <h1 class="page-title">Attendance</h1>
                <p class="page-sub">Điểm danh nhân viên theo ca</p>

                <c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
                <c:if test="${param.msg == 'checkedin'}"><div class="alert alert-success">Check-in thành công.</div></c:if>
                <c:if test="${param.msg == 'checkedout'}"><div class="alert alert-success">Check-out thành công.</div></c:if>
                <c:if test="${param.msg == 'absent'}"><div class="alert alert-success">Đã đánh dấu vắng.</div></c:if>
                <c:if test="${param.msg == 'reset'}"><div class="alert alert-success">Đã reset về scheduled.</div></c:if>
                <c:if test="${not isToday}">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i>
                        Đang xem ngày <b>${date}</b> (không phải hôm nay) — chế độ chỉ đọc.
                        Owner chỉ được sửa điểm danh trong ngày <b>${today}</b>.
                    </div>
                </c:if>

                <div class="card">
                    <form method="get" action="${pageContext.request.contextPath}/owner/attendance" style="display:flex; gap:12px; align-items:end;">
                        <div class="field">
                            <label>Ngày</label>
                            <input type="date" name="date" value="${date}" onchange="this.form.submit()">
                        </div>
                    </form>
                </div>

                <div class="card" style="padding:0;">
                    <table>
                        <thead>
                            <tr>
                                <th>Nhân viên</th>
                                <th>Ca</th>
                                <th>Khung giờ</th>
                                <th>Check-in</th>
                                <th>Check-out</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="r" items="${rows}">
                                <tr>
                                    <td>${r.fullName}</td>
                                    <td>${r.shiftName}</td>
                                    <td>
                                        <fmt:formatDate value="${r.startTime}" pattern="HH:mm"/>
                                        -
                                        <fmt:formatDate value="${r.endTime}" pattern="HH:mm"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty r.checkInTime}">
                                                <fmt:formatDate value="${r.checkInTime}" pattern="HH:mm:ss"/>
                                            </c:when>
                                            <c:otherwise><span style="color:#aaa;">—</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty r.checkOutTime}">
                                                <fmt:formatDate value="${r.checkOutTime}" pattern="HH:mm:ss"/>
                                            </c:when>
                                            <c:otherwise><span style="color:#aaa;">—</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><span class="badge badge-${r.status}">${r.status}</span></td>
                                    <td class="actions">
                                        <c:choose>
                                            <c:when test="${not isToday}">
                                                <span class="readonly-tag"><i class="fas fa-lock"></i> Read-only</span>
                                            </c:when>
                                            <c:otherwise>
                                                <c:choose>
                                                    <c:when test="${r.status == 'scheduled'}">
                                                        <form method="post" action="${pageContext.request.contextPath}/owner/attendance">
                                                            <input type="hidden" name="action" value="checkin">
                                                            <input type="hidden" name="shiftID" value="${r.shiftID}">
                                                            <input type="hidden" name="date" value="${date}">
                                                            <button type="submit" class="btn btn-checkin"><i class="fas fa-sign-in-alt"></i> Check-in</button>
                                                        </form>
                                                        <form method="post" action="${pageContext.request.contextPath}/owner/attendance"
                                                              onsubmit="return confirm('Đánh dấu vắng mặt?');">
                                                            <input type="hidden" name="action" value="absent">
                                                            <input type="hidden" name="shiftID" value="${r.shiftID}">
                                                            <input type="hidden" name="date" value="${date}">
                                                            <button type="submit" class="btn btn-absent"><i class="fas fa-user-slash"></i> Vắng</button>
                                                        </form>
                                                    </c:when>
                                                    <c:when test="${(r.status == 'present' || r.status == 'late') && empty r.checkOutTime}">
                                                        <form method="post" action="${pageContext.request.contextPath}/owner/attendance">
                                                            <input type="hidden" name="action" value="checkout">
                                                            <input type="hidden" name="shiftID" value="${r.shiftID}">
                                                            <input type="hidden" name="date" value="${date}">
                                                            <button type="submit" class="btn btn-checkout"><i class="fas fa-sign-out-alt"></i> Check-out</button>
                                                        </form>
                                                        <form method="post" action="${pageContext.request.contextPath}/owner/attendance"
                                                              onsubmit="return confirm('Reset về scheduled?');">
                                                            <input type="hidden" name="action" value="reset">
                                                            <input type="hidden" name="shiftID" value="${r.shiftID}">
                                                            <input type="hidden" name="date" value="${date}">
                                                            <button type="submit" class="btn btn-reset"><i class="fas fa-undo"></i> Reset</button>
                                                        </form>
                                                    </c:when>
                                                    <c:when test="${r.status == 'absent'}">
                                                        <form method="post" action="${pageContext.request.contextPath}/owner/attendance"
                                                              onsubmit="return confirm('Reset về scheduled?');">
                                                            <input type="hidden" name="action" value="reset">
                                                            <input type="hidden" name="shiftID" value="${r.shiftID}">
                                                            <input type="hidden" name="date" value="${date}">
                                                            <button type="submit" class="btn btn-reset"><i class="fas fa-undo"></i> Reset</button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="readonly-tag"><i class="fas fa-check-double"></i> Đã hoàn tất</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty rows}">
                                <tr><td colspan="7" style="text-align:center; padding:24px; color:#8a6e5a;">
                                        Chưa có ca nào trong ngày này.
                                    </td></tr>
                                </c:if>
                        </tbody>
                    </table>
                </div>
                <!--        </main>
                    </div>-->
                <%@ include file="/views/includes/footer.jsp" %>
                </body>
                </html>
