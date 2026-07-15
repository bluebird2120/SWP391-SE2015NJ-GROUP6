<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Điểm danh</title>
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
            .bulk-bar {
                display:none;
                align-items:center;
                gap:10px;
                background:#76493b;
                color:#fff;
                border-radius:10px;
                padding:10px 16px;
                margin-bottom:10px;
                font-size:0.88rem;
                flex-wrap:wrap;
            }
            .bulk-bar.active { display:flex; }
            .bulk-bar .sel-count { font-weight:700; margin-right:4px; }
            .bulk-bar .btn-bulk {
                padding:6px 14px;
                border-radius:7px;
                border:none;
                font-weight:600;
                font-size:0.82rem;
                cursor:pointer;
                display:inline-flex;
                align-items:center;
                gap:5px;
            }
            .btn-bulk-ci  { background:#d4edda; color:#155724; }
            .btn-bulk-ab  { background:#f8d7da; color:#721c24; }
            .btn-bulk-cancel { background:rgba(255,255,255,0.2); color:#fff; }
            th.col-cb, td.col-cb { width:36px; text-align:center; padding:8px 4px; }
            input[type=checkbox] { width:16px; height:16px; accent-color:#76493b; cursor:pointer; }

            .custom-confirm-modal {
                display: none;
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.4);
                z-index: 100000;
                align-items: center;
                justify-content: center;
                opacity: 0;
                transition: opacity 0.2s ease;
            }
            .custom-confirm-modal.show {
                display: flex;
                opacity: 1;
            }
            .custom-confirm-content {
                background-color: #fff;
                border-radius: 12px;
                box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
                padding: 24px;
                width: 100%;
                max-width: 400px;
                text-align: center;
                transform: translateY(-20px);
                transition: transform 0.2s ease;
                border: 1px solid #ede0d8;
            }
            .custom-confirm-modal.show .custom-confirm-content {
                transform: translateY(0);
            }
            .custom-confirm-message {
                font-size: 15px;
                color: #4A3B32;
                margin-bottom: 24px;
                font-weight: 500;
                line-height: 1.5;
            }
            .custom-confirm-buttons {
                display: flex;
                justify-content: center;
                gap: 12px;
            }
            .custom-confirm-btn {
                padding: 10px 20px;
                border-radius: 8px;
                font-size: 14px;
                font-weight: 600;
                cursor: pointer;
                border: none;
                transition: all 0.2s ease;
            }
            .custom-confirm-btn-cancel {
                background-color: #f1ebd9;
                color: #76493b;
            }
            .custom-confirm-btn-cancel:hover {
                background-color: #e5dac1;
            }
            .custom-confirm-btn-ok {
                background-color: #76493b;
                color: #fff;
            }
            .custom-confirm-btn-ok:hover {
                background-color: #5f3a2f;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div style="display:flex;">
            <%@ include file="/views/includes/dashboard.jsp" %>
            <main class="main">
                <h1 class="page-title">Điểm danh</h1>
                <p class="page-sub">Điểm danh nhân viên theo ca</p>


                <div id="localConfirmModal" class="custom-confirm-modal">
                    <div class="custom-confirm-content">
                        <div id="localConfirmMessage" class="custom-confirm-message"></div>
                        <div class="custom-confirm-buttons">
                            <button id="localConfirmCancelBtn" class="custom-confirm-btn custom-confirm-btn-cancel">Huỷ</button>
                            <button id="localConfirmOkBtn" class="custom-confirm-btn custom-confirm-btn-ok">Đồng ý</button>
                        </div>
                    </div>
                </div>

                <c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
                <c:if test="${param.msg == 'checkedin'}"><div class="alert alert-success">Check-in thành công.</div></c:if>
                <c:if test="${param.msg == 'checkedout'}"><div class="alert alert-success">Check-out thành công.</div></c:if>
                <c:if test="${param.msg == 'absent'}"><div class="alert alert-success">Đã đánh dấu vắng.</div></c:if>
                <c:if test="${param.msg == 'reset'}"><div class="alert alert-success">Đã reset về scheduled.</div></c:if>
                <c:if test="${param.msg == 'bulk_checkedin'}"><div class="alert alert-success"><i class="fas fa-check-circle"></i> Check-in thành công cho ${param.cnt} nhân viên.</div></c:if>
                <c:if test="${param.msg == 'bulk_absent'}"><div class="alert alert-success"><i class="fas fa-user-slash"></i> Đã đánh vắng ${param.cnt} nhân viên.</div></c:if>
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
                        <tbody id="attendanceBody">

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
                                                <span class="readonly-tag">Read-only</span>
                                            </c:when>
                                            <c:otherwise>
                                                <c:choose>
                                                    <c:when test="${r.status == 'scheduled'}">

                                                        <form method="post" action="${pageContext.request.contextPath}/owner/attendance">

                                                            <input type="hidden" name="action" value="checkin">

                                                            <input type="hidden" name="shiftID" value="${r.shiftID}">

                                                            <input type="hidden" name="date" value="${date}">
                                                            <button type="submit" class="btn btn-checkin">Check-in</button>
                                                        </form>

                                                        <form method="post" action="${pageContext.request.contextPath}/owner/attendance"
                                                              onsubmit="return showCustomConfirm(this, event, 'Đánh dấu vắng mặt?');">

                                                            <input type="hidden" name="action" value="absent">

                                                            <input type="hidden" name="shiftID" value="${r.shiftID}">

                                                            <input type="hidden" name="date" value="${date}">
                                                            <button type="submit" class="btn btn-absent">Vắng</button>
                                                        </form>
                                                    </c:when>
                                                    <c:when test="${(r.status == 'present' || r.status == 'late') && empty r.checkOutTime}">

                                                        <form method="post" action="${pageContext.request.contextPath}/owner/attendance">

                                                            <input type="hidden" name="action" value="checkout">

                                                            <input type="hidden" name="shiftID" value="${r.shiftID}">

                                                            <input type="hidden" name="date" value="${date}">
                                                            <button type="submit" class="btn btn-checkout">Check-out</button>
                                                        </form>

                                                        <form method="post" action="${pageContext.request.contextPath}/owner/attendance"
                                                              onsubmit="return showCustomConfirm(this, event, 'Reset về scheduled?');">

                                                            <input type="hidden" name="action" value="reset">

                                                            <input type="hidden" name="shiftID" value="${r.shiftID}">

                                                            <input type="hidden" name="date" value="${date}">
                                                            <button type="submit" class="btn btn-reset">Reset</button>
                                                        </form>
                                                    </c:when>
                                                    <c:when test="${r.status == 'absent'}">

                                                        <form method="post" action="${pageContext.request.contextPath}/owner/attendance"
                                                              onsubmit="return showCustomConfirm(this, event, 'Reset về scheduled?');">

                                                            <input type="hidden" name="action" value="reset">

                                                            <input type="hidden" name="shiftID" value="${r.shiftID}">

                                                            <input type="hidden" name="date" value="${date}">
                                                            <button type="submit" class="btn btn-reset">Reset</button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="readonly-tag">Đã hoàn tất</span>
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
            </main>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>
        <script>
            var activeConfirmForm = null;

            function showCustomConfirm(form, event, message) {
                if (event) {
                    event.preventDefault();
                }
                activeConfirmForm = form;

                var modal = document.getElementById('localConfirmModal');
                var msgEl = document.getElementById('localConfirmMessage');
                if (modal && msgEl) {
                    msgEl.textContent = message;
                    modal.classList.add('show');
                }
                return false;
            }

            document.addEventListener('DOMContentLoaded', function() {
                var cancelBtn = document.getElementById('localConfirmCancelBtn');
                var okBtn = document.getElementById('localConfirmOkBtn');
                var modal = document.getElementById('localConfirmModal');

                if (cancelBtn) {
                    cancelBtn.addEventListener('click', function() {
                        if (modal) modal.classList.remove('show');
                        activeConfirmForm = null;
                    });
                }

                if (okBtn) {
                    okBtn.addEventListener('click', function() {
                        if (modal) modal.classList.remove('show');
                        if (activeConfirmForm) {
                            var form = activeConfirmForm;
                            form.submit();
                        }
                        activeConfirmForm = null;
                    });
                }
            });
        </script>
    </body>
</html>
