<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Shift Roster</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin:0; font-family:'Inter',sans-serif; background:#faf6f2; }
        .main { flex:1; padding:24px 32px; }
        .page-title { font-family:'Playfair Display',serif; color:#76493b; font-size:1.6rem; margin:0 0 4px; }
        .page-sub { color:#a0714f; font-size:0.9rem; margin-bottom:18px; }
        .card { background:#fff; border:1px solid #ede0d8; border-radius:12px; padding:18px; margin-bottom:16px; }
        .row { display:flex; gap:12px; flex-wrap:wrap; align-items:end; }
        .field { display:flex; flex-direction:column; gap:4px; }
        label { font-size:0.78rem; font-weight:600; color:#8a6e5a; text-transform:uppercase; letter-spacing:0.04em; }
        input, select { padding:9px 12px; border:1px solid #d7bfa4; border-radius:7px; font-family:inherit; font-size:0.9rem; min-width:200px; }
        input:focus, select:focus { outline:none; border-color:#76493b; }
        .btn { padding:9px 16px; border-radius:8px; border:none; cursor:pointer; font-size:0.88rem; font-weight:600; text-decoration:none; display:inline-flex; gap:6px; align-items:center; }
        .btn-primary { background:#76493b; color:#fff; }
        .btn-danger { background:#dc3545; color:#fff; }
        .btn-disabled { background:#e9ecef; color:#6c757d; cursor:not-allowed; }
        .btn-sm { padding:5px 10px; font-size:0.78rem; }
        table { width:100%; border-collapse:collapse; }
        th { background:#faf6f2; padding:11px; text-align:left; font-size:0.78rem; color:#76493b; text-transform:uppercase; }
        td { padding:11px; border-bottom:1px solid #f5ece4; font-size:0.9rem; color:#4a3528; }
        tr:hover { background:#faf6f2; }
        .badge { padding:3px 9px; border-radius:12px; font-size:0.72rem; font-weight:600; }
        .badge-scheduled { background:#e2e3e5; color:#41464b; }
        .badge-present { background:#d4edda; color:#155724; }
        .badge-late { background:#fff3cd; color:#856404; }
        .badge-absent { background:#f8d7da; color:#721c24; }
        .alert { padding:11px 14px; border-radius:8px; margin-bottom:14px; font-size:0.88rem; }
        .alert-error { background:#f8d7da; color:#721c24; border:1px solid #f5c2c7; }
        .alert-success { background:#d4edda; color:#155724; border:1px solid #c3e6cb; }
    </style>
</head>
<body>
    <%@ include file="/views/includes/header.jsp" %>
    <div style="display:flex;">
        <%@ include file="/views/includes/dashboard.jsp" %>
        <main class="main">
            <h1 class="page-title">Shift Assignments</h1>
            <p class="page-sub">Phân ca cho nhân viên theo ngày</p>

            <c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
            <c:if test="${param.msg == 'assigned'}"><div class="alert alert-success">Đã gán ca thành công.</div></c:if>
            <c:if test="${param.msg == 'unassigned'}"><div class="alert alert-success">Đã huỷ ca.</div></c:if>
            <c:if test="${param.msg == 'unassign_failed'}"><div class="alert alert-error">Không thể huỷ — ca đã được điểm danh.</div></c:if>

            <!-- Date picker -->
            <div class="card">
                <form method="get" action="${pageContext.request.contextPath}/owner/shift-assignments" class="row">
                    <div class="field">
                        <label>Ngày</label>
                        <input type="date" name="date" value="${date}" onchange="this.form.submit()">
                    </div>
                </form>
            </div>

            <!-- Assign form -->
            <div class="card">
                <h3 style="margin:0 0 12px; color:#76493b;">Gán ca mới</h3>
                <form method="post" action="${pageContext.request.contextPath}/owner/shift-assignments" class="row">
                    <input type="hidden" name="action" value="assign">
                    <input type="hidden" name="date" value="${date}">
                    <div class="field">
                        <label>Nhân viên</label>
                        <select name="employeeID" required>
                            <option value="">-- Chọn nhân viên --</option>
                            <c:forEach var="s" items="${staffList}">
                                <option value="${s.employeeID}">${s.fullName}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label>Ca</label>
                        <select name="templateID" required>
                            <option value="">-- Chọn ca --</option>
                            <c:forEach var="t" items="${templates}">
                                <option value="${t.templateID}">
                                    ${t.shiftName}
                                    (<fmt:formatDate value="${t.startTime}" pattern="HH:mm"/>
                                    - <fmt:formatDate value="${t.endTime}" pattern="HH:mm"/>)
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary"><i class="fas fa-plus"></i> Gán ca</button>
                </form>
            </div>

            <!-- Roster table -->
            <div class="card" style="padding:0;">
                <table>
                    <thead>
                        <tr>
                            <th>Nhân viên</th>
                            <th>Ca</th>
                            <th>Giờ</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="r" items="${roster}">
                            <tr>
                                <td>${r.fullName}</td>
                                <td>${r.shiftName}</td>
                                <td>
                                    <fmt:formatDate value="${r.startTime}" pattern="HH:mm"/>
                                    -
                                    <fmt:formatDate value="${r.endTime}" pattern="HH:mm"/>
                                </td>
                                <td><span class="badge badge-${r.status}">${r.status}</span></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${r.status == 'scheduled'}">
                                            <form method="post" action="${pageContext.request.contextPath}/owner/shift-assignments"
                                                  onsubmit="return confirm('Huỷ ca này?');">
                                                <input type="hidden" name="action" value="unassign">
                                                <input type="hidden" name="shiftID" value="${r.shiftID}">
                                                <input type="hidden" name="date" value="${date}">
                                                <button type="submit" class="btn btn-sm btn-danger">
                                                    <i class="fas fa-times"></i> Huỷ
                                                </button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <button class="btn btn-sm btn-disabled" disabled>Đã xử lý</button>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty roster}">
                            <tr><td colspan="5" style="text-align:center; padding:24px; color:#8a6e5a;">
                                Chưa có ca nào trong ngày này.
                            </td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
                    </main>
                </div>
    <%@ include file="/views/includes/footer.jsp" %>
</body>
</html>
