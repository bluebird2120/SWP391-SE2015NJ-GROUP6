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
                min-width:0;
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
            .row {
                display:flex;
                gap:10px;
                flex-wrap:wrap;
                align-items:end;
            }
            .field {
                display:flex;
                flex-direction:column;
                gap:3px;
                flex:1;
                min-width:140px;
            }
            .field-sm {
                flex:0 0 auto;
                min-width:100px;
            }
            label {
                font-size:0.72rem;
                font-weight:600;
                color:#8a6e5a;
                text-transform:uppercase;
                letter-spacing:0.04em;
            }
            input, select {
                padding:8px 10px;
                border:1px solid #d7bfa4;
                border-radius:7px;
                font-family:inherit;
                font-size:0.85rem;
                width:100%;
            }
            input:focus, select:focus {
                outline:none;
                border-color:#76493b;
            }
            .btn {
                padding:8px 14px;
                border-radius:8px;
                border:none;
                cursor:pointer;
                font-size:0.82rem;
                font-weight:600;
                text-decoration:none;
                display:inline-flex;
                gap:5px;
                align-items:center;
                white-space:nowrap;
            }
            .btn-primary {
                background:#76493b;
                color:#fff;
            }
            .btn-primary:hover {
                background:#5d3a2e;
            }
            .btn-danger {
                background:#dc3545;
                color:#fff;
            }
            .btn-disabled {
                background:#e9ecef;
                color:#6c757d;
                cursor:not-allowed;
            }
            .btn-sm {
                padding:4px 9px;
                font-size:0.74rem;
            }
            table {
                width:100%;
                border-collapse:collapse;
            }
            th {
                background:#faf6f2;
                padding:10px;
                text-align:left;
                font-size:0.74rem;
                color:#76493b;
                text-transform:uppercase;
            }
            td {
                padding:10px;
                border-bottom:1px solid #f5ece4;
                font-size:0.85rem;
                color:#4a3528;
            }
            tr:hover {
                background:#faf6f2;
            }
            .badge {
                padding:3px 8px;
                border-radius:12px;
                font-size:0.7rem;
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
            .badge-DRAFT {
                background:#e2e3e5;
                color:#41464b;
            }
            .badge-NOTIFIED {
                background:#cce5ff;
                color:#004085;
            }
            .badge-APPLIED {
                background:#d4edda;
                color:#155724;
            }
            .badge-CANCELLED {
                background:#f8d7da;
                color:#721c24;
            }
            .alert {
                padding:10px 14px;
                border-radius:8px;
                margin-bottom:14px;
                font-size:0.85rem;
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

            /* Tabs */
            .tabs {
                display:flex;
                gap:0;
                border-bottom:2px solid #ede0d8;
                margin-bottom:18px;
            }
            .tab-btn {
                padding:10px 20px;
                font-size:0.88rem;
                font-weight:600;
                color:#8a6e5a;
                background:none;
                border:none;
                cursor:pointer;
                border-bottom:3px solid transparent;
                margin-bottom:-2px;
                transition:all 0.2s;
            }
            .tab-btn:hover {
                color:#76493b;
                background:#f5ece4;
            }
            .tab-btn.active {
                color:#76493b;
                border-bottom-color:#76493b;
            }
            .tab-content {
                display:none;
            }
            .tab-content.active {
                display:block;
            }
            .section-title {
                font-size:0.95rem;
                font-weight:700;
                color:#76493b;
                margin:0 0 12px;
                display:flex;
                align-items:center;
                gap:8px;
            }
            .section-title i {
                font-size:0.85rem;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div style="display:flex;">
            <%@ include file="/views/includes/dashboard.jsp" %>
            <main class="main">
                <h1 class="page-title">Shift Roster</h1>
                <p class="page-sub">Quản lý phân ca nhân viên</p>

                <c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
                <c:if test="${not empty success}"><div class="alert alert-success">${success}</div></c:if>
                <c:if test="${param.msg == 'assigned'}"><div class="alert alert-success">Đã gán ca thành công.</div></c:if>
                <c:if test="${param.msg == 'unassigned'}"><div class="alert alert-success">Đã huỷ ca.</div></c:if>
                <c:if test="${param.msg == 'unassign_failed'}"><div class="alert alert-error">Không thể huỷ — ca đã được điểm danh.</div></c:if>
                <c:if test="${param.msg == 'plan_saved'}"><div class="alert alert-success">Đã lưu kế hoạch ca tháng.</div></c:if>
                <c:if test="${param.msg == 'month_assigned'}"><div class="alert alert-success">Đã áp dụng phân ca cho cả tháng.</div></c:if>
                <c:if test="${param.msg == 'plan_cancelled'}"><div class="alert alert-success">Đã huỷ kế hoạch ca tháng.</div></c:if>
                <c:if test="${param.msg == 'plan_cancel_failed'}"><div class="alert alert-error">Không thể huỷ kế hoạch.</div></c:if>

                    <!-- TABS -->
                    <div class="tabs">
                        <button class="tab-btn active" onclick="switchTab('daily')"><i class="fas fa-calendar-day"></i> Phân ca ngày</button>
                        <button class="tab-btn" onclick="switchTab('monthly')"><i class="fas fa-calendar-alt"></i> Phân ca tháng</button>
                        <button class="tab-btn" onclick="switchTab('view')"><i class="fas fa-user-clock"></i> Xem lịch NV</button>
                    </div>

                    <!-- ===== TAB 1: PHÂN CA THEO NGÀY ===== -->
                    <div id="tab-daily" class="tab-content active">
                        <div class="card">
                            <div class="section-title"><i class="fas fa-plus-circle"></i> Gán ca theo ngày</div>
                            <form method="post" action="${pageContext.request.contextPath}/owner/shift-roster" class="row">
                            <input type="hidden" name="action" value="assign">
                            <input type="hidden" name="date" value="${date}">
                            <div class="field field-sm" style="min-width:150px;">
                                <label>Ngày</label>
                                <input type="date" name="dateDisplay" value="${date}" onchange="document.querySelector('[name=date]').value = this.value; this.form.action = '${pageContext.request.contextPath}/owner/shift-roster'; this.form.method = 'get'; this.form.submit();">
                            </div>
                            <div class="field">
                                <label>Nhân viên</label>
                                <select name="employeeID" required>
                                    <option value="">-- Chọn --</option>
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
                                        <option value="${t.templateID}">${t.shiftName} (<fmt:formatDate value="${t.startTime}" pattern="HH:mm"/>-<fmt:formatDate value="${t.endTime}" pattern="HH:mm"/>)</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="field field-sm" style="min-width:auto;">
                                <label>&nbsp;</label>
                                <button type="submit" class="btn btn-primary"><i class="fas fa-plus"></i> Gán</button>
                            </div>
                        </form>
                    </div>

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
                                        <td><fmt:formatDate value="${r.startTime}" pattern="HH:mm"/> - <fmt:formatDate value="${r.endTime}" pattern="HH:mm"/></td>
                                        <td><span class="badge badge-${r.status}">${r.status}</span></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${r.status == 'scheduled'}">
                                                    <form method="post" action="${pageContext.request.contextPath}/owner/shift-roster" style="margin:0;" onsubmit="return confirm('Huỷ ca này?');">
                                                        <input type="hidden" name="action" value="unassign">
                                                        <input type="hidden" name="shiftID" value="${r.shiftID}">
                                                        <input type="hidden" name="date" value="${date}">
                                                        <button type="submit" class="btn btn-sm btn-danger"><i class="fas fa-times"></i> Huỷ</button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-${r.status}" style="font-size:0.7rem;">Đã xử lý</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty roster}">
                                    <tr><td colspan="5" style="text-align:center; padding:20px; color:#8a6e5a;">Chưa có ca nào trong ngày ${date}.</td></tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- ===== TAB 2: PHÂN CA THEO THÁNG ===== -->
                <div id="tab-monthly" class="tab-content">
                    <div class="card">
                        <div class="section-title"><i class="fas fa-calendar-check"></i> Gán ca cả tháng</div>
                        <form method="post" action="${pageContext.request.contextPath}/owner/shift-roster" class="row">
                            <input type="hidden" name="action" value="assignMonth">
                            <div class="field">
                                <label>Nhân viên</label>
                                <select name="employeeID" required>
                                    <option value="">-- Chọn --</option>
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
                                        <option value="${t.templateID}">${t.shiftName} (<fmt:formatDate value="${t.startTime}" pattern="HH:mm"/>-<fmt:formatDate value="${t.endTime}" pattern="HH:mm"/>)</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="field field-sm">
                                <label>Tháng</label>
                                <select name="month" required>
                                    <c:forEach var="m" begin="1" end="12">
                                        <option value="${m}" ${m == planMonth ? 'selected' : ''}>${m}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="field field-sm">
                                <label>Năm</label>
                                <input type="number" name="year" min="2024" value="${planYear}" required style="width:80px;">
                            </div>
                            <div class="field field-sm" style="min-width:170px;">
                                <label>Chế độ</label>
                                <select name="assignMode">
                                    <option value="SKIP_EXISTING">Bỏ qua ngày đã có</option>
                                    <option value="REPLACE_ALL">Thay thế cả tháng</option>
                                </select>
                            </div>
                            <div class="field field-sm" style="min-width:auto;">
                                <label>&nbsp;</label>
                                <button type="submit" class="btn btn-primary"><i class="fas fa-calendar-check"></i> Phân ca</button>
                            </div>
                        </form>
                    </div>

                    <!-- Bảng kế hoạch tháng -->
                    <div class="card" style="padding:0;">
                        <div style="padding:12px 18px; border-bottom:1px solid #f5ece4; display:flex; align-items:center; gap:10px; flex-wrap:wrap;">
                            <span class="section-title" style="margin:0;"><i class="fas fa-list"></i> Kế hoạch tháng</span>
                            <form method="get" action="${pageContext.request.contextPath}/owner/shift-roster" style="display:flex; gap:8px; align-items:center; margin-left:auto;">
                                <input type="hidden" name="date" value="${date}">
                                <select name="planMonth" style="width:auto; padding:5px 8px; font-size:0.8rem;" onchange="this.form.submit()">
                                    <c:forEach var="m" begin="1" end="12">
                                        <option value="${m}" ${m == planMonth ? 'selected' : ''}>T${m}</option>
                                    </c:forEach>
                                </select>
                                <input type="number" name="planYear" min="2024" value="${planYear}" style="width:70px; padding:5px 8px; font-size:0.8rem;" onchange="this.form.submit()">
                            </form>
                        </div>
                        <table>
                            <thead>
                                <tr>
                                    <th>Nhân viên</th>
                                    <th>Ca</th>
                                    <th>Giờ</th>
                                    <th>Tháng</th>
                                    <th>Trạng thái</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${monthlyPlans}">
                                    <tr>
                                        <td>${p.employeeName}</td>
                                        <td>${p.templateName}</td>
                                        <td><fmt:formatDate value="${p.startTime}" pattern="HH:mm"/> - <fmt:formatDate value="${p.endTime}" pattern="HH:mm"/></td>
                                        <td>${p.effectiveMonth}/${p.effectiveYear}</td>
                                        <td><span class="badge badge-${p.status}">${p.status}</span></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.status == 'DRAFT' or p.status == 'NOTIFIED'}">
                                                    <form method="post" action="${pageContext.request.contextPath}/owner/shift-roster" style="margin:0;" onsubmit="return confirm('Huỷ kế hoạch này?');">
                                                        <input type="hidden" name="action" value="cancelPlan">
                                                        <input type="hidden" name="planID" value="${p.planID}">
                                                        <input type="hidden" name="planYear" value="${p.effectiveYear}">
                                                        <input type="hidden" name="planMonth" value="${p.effectiveMonth}">
                                                        <button type="submit" class="btn btn-sm btn-danger"><i class="fas fa-times"></i> Huỷ</button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-${p.status}" style="font-size:0.68rem;">Đã ${p.status == 'APPLIED' ? 'áp dụng' : 'huỷ'}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty monthlyPlans}">
                                    <tr><td colspan="6" style="text-align:center; padding:20px; color:#8a6e5a;">Chưa có kế hoạch ca cho tháng ${planMonth}/${planYear}.</td></tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- ===== TAB 3: XEM LỊCH NHÂN VIÊN ===== -->
                <div id="tab-view" class="tab-content">
                    <div class="card">
                        <div class="section-title"><i class="fas fa-user-clock"></i> Xem lịch ca nhân viên</div>
                        <form method="get" action="${pageContext.request.contextPath}/owner/shift-roster" class="row">
                            <input type="hidden" name="date" value="${date}">
                            <input type="hidden" name="planYear" value="${planYear}">
                            <input type="hidden" name="planMonth" value="${planMonth}">
                            <input type="hidden" name="activeTab" value="view">
                            <div class="field">
                                <label>Nhân viên</label>
                                <select name="viewEmployeeID" required>
                                    <option value="">-- Chọn --</option>
                                    <c:forEach var="s" items="${staffList}">
                                        <option value="${s.employeeID}" ${s.employeeID == viewEmployeeID ? 'selected' : ''}>${s.fullName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="field field-sm">
                                <label>Tháng</label>
                                <select name="viewMonth">
                                    <c:forEach var="m" begin="1" end="12">
                                        <option value="${m}" ${m == viewMonth ? 'selected' : ''}>${m}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="field field-sm">
                                <label>Năm</label>
                                <input type="number" name="viewYear" min="2024" value="${viewYear}" style="width:80px;">
                            </div>
                            <div class="field field-sm" style="min-width:auto;">
                                <label>&nbsp;</label>
                                <button type="submit" class="btn btn-primary"><i class="fas fa-eye"></i> Xem</button>
                            </div>
                        </form>
                    </div>

                    <c:if test="${not empty staffSchedule}">
                        <div class="card" style="padding:0;">
                            <div style="padding:12px 18px; border-bottom:1px solid #f5ece4;">
                                <strong style="color:#76493b;"><i class="fas fa-user"></i> ${viewEmployeeName} — Tháng ${viewMonth}/${viewYear}</strong>
                                <span style="float:right; font-size:0.8rem; color:#8a6e5a;">${staffSchedule.size()} ca</span>
                            </div>
                            <table>
                                <thead>
                                    <tr>
                                        <th>Ngày</th>
                                        <th>Thứ</th>
                                        <th>Ca</th>
                                        <th>Giờ</th>
                                        <th>Trạng thái</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="s" items="${staffSchedule}">
                                        <tr>
                                            <td><fmt:formatDate value="${s.workDate}" pattern="dd/MM"/></td>
                                            <td>
                                                <fmt:formatDate value="${s.workDate}" pattern="u" var="dowNum"/>
                                                <c:choose>
                                                    <c:when test="${dowNum == '1'}">T2</c:when>
                                                    <c:when test="${dowNum == '2'}">T3</c:when>
                                                    <c:when test="${dowNum == '3'}">T4</c:when>
                                                    <c:when test="${dowNum == '4'}">T5</c:when>
                                                    <c:when test="${dowNum == '5'}">T6</c:when>
                                                    <c:when test="${dowNum == '6'}">T7</c:when>
                                                    <c:when test="${dowNum == '7'}"><span style="color:#c14b4b; font-weight:600;">CN</span></c:when>
                                                </c:choose>
                                            </td>
                                            <td>${s.shiftName}</td>
                                            <td><fmt:formatDate value="${s.startTime}" pattern="HH:mm"/> - <fmt:formatDate value="${s.endTime}" pattern="HH:mm"/></td>
                                            <td><span class="badge badge-${s.status}">${s.status}</span></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:if>
                    <c:if test="${viewEmployeeID > 0 && empty staffSchedule}">
                        <div class="card">
                            <p style="text-align:center; color:#8a6e5a; padding:16px 0;"><strong>${viewEmployeeName}</strong> chưa có ca nào trong tháng ${viewMonth}/${viewYear}.</p>
                        </div>
                    </c:if>
                </div>

            </main>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>

        <script>
            function switchTab(name) {
                document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
                document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
                document.getElementById('tab-' + name).classList.add('active');
                event.currentTarget.classList.add('active');
                sessionStorage.setItem('shiftRosterTab', name);
            }

            // Restore tab from URL param or sessionStorage
            (function () {
                var params = new URLSearchParams(window.location.search);
                var tab = params.get('activeTab') || sessionStorage.getItem('shiftRosterTab');
                if (tab && document.getElementById('tab-' + tab)) {
                    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
                    document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
                    document.getElementById('tab-' + tab).classList.add('active');
                    var btns = document.querySelectorAll('.tab-btn');
                    var idx = tab === 'daily' ? 0 : tab === 'monthly' ? 1 : 2;
                    if (btns[idx])
                        btns[idx].classList.add('active');
                }
                // Auto-switch to view tab if staffSchedule loaded
            <c:if test="${not empty staffSchedule || (viewEmployeeID > 0 && empty staffSchedule)}">
                if (!params.get('activeTab')) {
                    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
                    document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
                    document.getElementById('tab-view').classList.add('active');
                    document.querySelectorAll('.tab-btn')[2].classList.add('active');
                }
            </c:if>
            })();
        </script>
    </body>
</html>
