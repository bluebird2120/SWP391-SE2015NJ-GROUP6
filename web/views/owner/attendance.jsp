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
            /* === Filter bar === */
            .filter-bar {
                display:flex;
                align-items:center;
                gap:10px;
                margin-bottom:12px;
                flex-wrap:wrap;
            }
            .filter-bar label {
                font-size:0.82rem;
                font-weight:600;
                color:#76493b;
                text-transform:none;
                letter-spacing:0;
                white-space:nowrap;
            }
            .filter-bar select {
                padding:7px 32px 7px 10px;
                border:1px solid #d7bfa4;
                border-radius:8px;
                font-family:inherit;
                font-size:0.88rem;
                background:#fff url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%2376493b' stroke-width='2' fill='none' stroke-linecap='round'/%3E%3C/svg%3E") no-repeat right 10px center;
                -webkit-appearance:none;
                appearance:none;
                color:#333;
                cursor:pointer;
                min-width:160px;
            }
            .filter-bar select:focus { outline:none; border-color:#76493b; }
            .filter-count {
                font-size:0.78rem;
                color:#8a6e5a;
                background:#f5ece4;
                border-radius:12px;
                padding:3px 10px;
                font-weight:600;
            }
            /* === Bulk action bar === */
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
            /* checkbox col */
            th.col-cb, td.col-cb { width:36px; text-align:center; padding:8px 4px; }
            input[type=checkbox] { width:16px; height:16px; accent-color:#76493b; cursor:pointer; }
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
                <div class="filter-bar">
                    <label for="filterShift"><i class="fas fa-filter"></i>&nbsp;Lọc theo ca:</label>
                    <select id="filterShift" onchange="filterAttendance(this.value)">
                        <option value="">-- Tất cả ca --</option>
                        <c:forEach var="t" items="${templates}">
                            <option value="${t.shiftName}">${t.shiftName}</option>
                        </c:forEach>
                    </select>
                    <span class="filter-count" id="attendanceCountBadge"></span>
                </div>
                <%-- Bulk action bar (chỉ hiện khi đang là hôm nay) --%>
                <c:if test="${isToday}">
                    <div class="bulk-bar" id="bulkBar">
                        <span><span class="sel-count" id="selCount">0</span> đã chọn</span>
                        <button type="button" class="btn-bulk btn-bulk-ci" onclick="submitBulk('bulk_checkin')">
                            <i class="fas fa-sign-in-alt"></i> Check-in tất cả
                        </button>
                        <button type="button" class="btn-bulk btn-bulk-ab" onclick="submitBulk('bulk_absent')">
                            <i class="fas fa-user-slash"></i> Vắng tất cả
                        </button>
                        <button type="button" class="btn-bulk btn-bulk-cancel" onclick="clearAll()">
                            <i class="fas fa-times"></i> Bỏ chọn
                        </button>
                    </div>
                    <%-- Hidden form để submit bulk --%>
                    <form id="bulkForm" method="post" action="${pageContext.request.contextPath}/owner/attendance" style="display:none;">
                        <input type="hidden" name="action" id="bulkAction">
                        <input type="hidden" name="date" value="${date}">
                        <div id="bulkIdsContainer"></div>
                    </form>
                </c:if>
                <div class="card" style="padding:0;">
                    <table>
                        <thead>
                            <tr>
                                <th class="col-cb"><c:if test="${isToday}"><input type="checkbox" id="checkAll" title="Chọn tất cả" onchange="toggleAll(this)"></c:if></th>
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
                                <tr data-shift="${r.shiftName}">
                                    <td class="col-cb">
                                        <c:if test="${isToday && r.status == 'scheduled'}">
                                            <input type="checkbox" class="row-cb" value="${r.shiftID}" onchange="onCbChange()">
                                        </c:if>
                                    </td>
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
            </main>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>
        <script>
            /* ============ Filter ============ */
            function filterAttendance(shiftName) {
                var tbody = document.getElementById('attendanceBody');
                if (!tbody) return;
                var rows = tbody.querySelectorAll('tr[data-shift]');
                var visible = 0;
                rows.forEach(function(tr) {
                    if (!shiftName || tr.dataset.shift === shiftName) {
                        tr.style.display = '';
                        visible++;
                    } else {
                        tr.style.display = 'none';
                    }
                });
                var badge = document.getElementById('attendanceCountBadge');
                if (badge) {
                    badge.textContent = shiftName
                        ? visible + ' nhân viên'
                        : rows.length + ' nhân viên';
                }
                // Bỏ check những row bị ẩn
                rows.forEach(function(tr) {
                    if (tr.style.display === 'none') {
                        var cb = tr.querySelector('.row-cb');
                        if (cb) cb.checked = false;
                    }
                });
                onCbChange();
            }

            /* ============ Bulk select ============ */
            function toggleAll(master) {
                var visibleCbs = getVisibleCheckboxes();
                visibleCbs.forEach(function(cb) { cb.checked = master.checked; });
                onCbChange();
            }

            function getVisibleCheckboxes() {
                var tbody = document.getElementById('attendanceBody');
                if (!tbody) return [];
                return Array.from(tbody.querySelectorAll('.row-cb')).filter(function(cb) {
                    return cb.closest('tr').style.display !== 'none';
                });
            }

            function onCbChange() {
                var checked = Array.from(document.querySelectorAll('.row-cb:checked'));
                var bar = document.getElementById('bulkBar');
                var cnt = document.getElementById('selCount');
                if (!bar) return;
                if (checked.length > 0) {
                    bar.classList.add('active');
                    cnt.textContent = checked.length;
                } else {
                    bar.classList.remove('active');
                }
            }

            function clearAll() {
                document.querySelectorAll('.row-cb').forEach(function(cb) { cb.checked = false; });
                var master = document.getElementById('checkAll');
                if (master) master.checked = false;
                onCbChange();
            }

            function submitBulk(action) {
                var checked = Array.from(document.querySelectorAll('.row-cb:checked'));
                if (checked.length === 0) { alert('Chưa chọn nhân viên nào.'); return; }
                if (action === 'bulk_absent') {
                    if (!confirm('Xác nhận đánh vắng ' + checked.length + ' nhân viên?')) return;
                }
                var form = document.getElementById('bulkForm');
                document.getElementById('bulkAction').value = action;
                var container = document.getElementById('bulkIdsContainer');
                container.innerHTML = '';
                checked.forEach(function(cb) {
                    var inp = document.createElement('input');
                    inp.type = 'hidden';
                    inp.name = 'shiftIDs';
                    inp.value = cb.value;
                    container.appendChild(inp);
                });
                form.submit();
            }

            /* ============ Init ============ */
            document.addEventListener('DOMContentLoaded', function() {
                filterAttendance('');
            });
        </script>
    </body>
</html>
