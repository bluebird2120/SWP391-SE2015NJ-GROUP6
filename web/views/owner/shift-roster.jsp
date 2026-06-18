<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
        <%@page contentType="text/html" pageEncoding="UTF-8" %>
            <!DOCTYPE html>
            <html>

            <head>
                <meta charset="UTF-8">
                <title>Shift Roster</title>
                <link
                    href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap"
                    rel="stylesheet">
                <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
                <style>
                    * {
                        box-sizing: border-box;
                    }

                    body {
                        margin: 0;
                        font-family: 'Inter', sans-serif;
                        background: #faf6f2;
                    }

                    .main {
                        flex: 1;
                        padding: 24px 32px;
                        min-width: 0;
                    }

                    .page-title {
                        font-family: 'Playfair Display', serif;
                        color: #76493b;
                        font-size: 1.6rem;
                        margin: 0 0 4px;
                    }

                    .page-sub {
                        color: #a0714f;
                        font-size: 0.9rem;
                        margin-bottom: 18px;
                    }

                    .card {
                        background: #fff;
                        border: 1px solid #ede0d8;
                        border-radius: 12px;
                        padding: 18px;
                        margin-bottom: 16px;
                    }

                    .row {
                        display: flex;
                        gap: 10px;
                        flex-wrap: wrap;
                        align-items: end;
                    }

                    .field {
                        display: flex;
                        flex-direction: column;
                        gap: 3px;
                        flex: 1;
                        min-width: 140px;
                    }

                    .field-sm {
                        flex: 0 0 auto;
                        min-width: 100px;
                    }

                    label {
                        font-size: 0.72rem;
                        font-weight: 600;
                        color: #8a6e5a;
                        text-transform: uppercase;
                        letter-spacing: 0.04em;
                    }

                    input,
                    select {
                        padding: 8px 10px;
                        border: 1px solid #d7bfa4;
                        border-radius: 7px;
                        font-family: inherit;
                        font-size: 0.85rem;
                        width: 100%;
                    }

                    input:focus,
                    select:focus {
                        outline: none;
                        border-color: #76493b;
                    }

                    .btn {
                        padding: 8px 14px;
                        border-radius: 8px;
                        border: none;
                        cursor: pointer;
                        font-size: 0.82rem;
                        font-weight: 600;
                        text-decoration: none;
                        display: inline-flex;
                        gap: 5px;
                        align-items: center;
                        white-space: nowrap;
                    }

                    .btn-primary {
                        background: #76493b;
                        color: #fff;
                    }

                    .btn-primary:hover {
                        background: #5d3a2e;
                    }

                    .btn-danger {
                        background: #dc3545;
                        color: #fff;
                    }

                    .btn-disabled {
                        background: #e9ecef;
                        color: #6c757d;
                        cursor: not-allowed;
                    }

                    .btn-sm {
                        padding: 4px 9px;
                        font-size: 0.74rem;
                    }

                    table {
                        width: 100%;
                        border-collapse: collapse;
                    }

                    th {
                        background: #faf6f2;
                        padding: 10px;
                        text-align: left;
                        font-size: 0.74rem;
                        color: #76493b;
                        text-transform: uppercase;
                    }

                    td {
                        padding: 10px;
                        border-bottom: 1px solid #f5ece4;
                        font-size: 0.85rem;
                        color: #4a3528;
                    }

                    tr:hover {
                        background: #faf6f2;
                    }

                    .badge {
                        padding: 3px 8px;
                        border-radius: 12px;
                        font-size: 0.7rem;
                        font-weight: 600;
                    }

                    .badge-scheduled {
                        background: #e2e3e5;
                        color: #41464b;
                    }

                    .badge-present {
                        background: #d4edda;
                        color: #155724;
                    }

                    .badge-late {
                        background: #fff3cd;
                        color: #856404;
                    }

                    .badge-absent {
                        background: #f8d7da;
                        color: #721c24;
                    }

                    .badge-DRAFT {
                        background: #e2e3e5;
                        color: #41464b;
                    }

                    .badge-NOTIFIED {
                        background: #cce5ff;
                        color: #004085;
                    }

                    .badge-APPLIED {
                        background: #d4edda;
                        color: #155724;
                    }

                    .badge-CANCELLED {
                        background: #f8d7da;
                        color: #721c24;
                    }

                    .alert {
                        padding: 10px 14px;
                        border-radius: 8px;
                        margin-bottom: 14px;
                        font-size: 0.85rem;
                    }

                    .alert-error {
                        background: #f8d7da;
                        color: #721c24;
                        border: 1px solid #f5c2c7;
                    }

                    .alert-success {
                        background: #d4edda;
                        color: #155724;
                        border: 1px solid #c3e6cb;
                    }

                    /* Tabs */
                    .tabs {
                        display: flex;
                        gap: 0;
                        border-bottom: 2px solid #ede0d8;
                        margin-bottom: 18px;
                    }

                    .tab-btn {
                        padding: 10px 20px;
                        font-size: 0.88rem;
                        font-weight: 600;
                        color: #8a6e5a;
                        background: none;
                        border: none;
                        cursor: pointer;
                        border-bottom: 3px solid transparent;
                        margin-bottom: -2px;
                        transition: all 0.2s;
                    }

                    .tab-btn:hover {
                        color: #76493b;
                        background: #f5ece4;
                    }

                    .tab-btn.active {
                        color: #76493b;
                        border-bottom-color: #76493b;
                    }

                    .tab-content {
                        display: none;
                    }

                    .tab-content.active {
                        display: block;
                    }

                    .section-title {
                        font-size: 0.95rem;
                        font-weight: 700;
                        color: #76493b;
                        margin: 0 0 12px;
                        display: flex;
                        align-items: center;
                        gap: 8px;
                    }

                    .section-title i {
                        font-size: 0.85rem;
                    }

                    /* Filter bar */
                    .filter-bar {
                        display: flex;
                        align-items: center;
                        gap: 10px;
                        padding: 10px 18px;
                        background: #fdf8f5;
                        border-bottom: 1px solid #ede0d8;
                        flex-wrap: wrap;
                    }

                    .filter-bar label {
                        font-size: 0.72rem;
                        font-weight: 600;
                        color: #76493b;
                        text-transform: uppercase;
                        letter-spacing: 0.04em;
                        white-space: nowrap;
                        margin: 0;
                    }

                    .filter-bar select {
                        width: auto;
                        min-width: 160px;
                        padding: 6px 10px;
                        font-size: 0.82rem;
                        border: 1px solid #d7bfa4;
                        border-radius: 7px;
                        background: #fff;
                        color: #4a3528;
                    }

                    .filter-count {
                        margin-left: auto;
                        font-size: 0.78rem;
                        color: #8a6e5a;
                        white-space: nowrap;
                    }
                    
                    /* Custom Multi-select Dropdown */
                    .multi-select-container {
                        position: relative;
                        width: 100%;
                    }
                    .multi-select-trigger {
                        padding: 8px 30px 8px 10px;
                        border: 1px solid #d7bfa4;
                        border-radius: 7px;
                        font-size: 0.85rem;
                        background: #fff url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%2376493b' stroke-width='2' fill='none' stroke-linecap='round'/%3E%3C/svg%3E") no-repeat right 10px center;
                        color: #4a3528;
                        cursor: pointer;
                        user-select: none;
                        white-space: nowrap;
                        overflow: hidden;
                        text-overflow: ellipsis;
                    }
                    .multi-select-dropdown {
                        display: none;
                        position: absolute;
                        top: 100%;
                        left: 0;
                        right: 0;
                        background: #fff;
                        border: 1px solid #d7bfa4;
                        border-radius: 7px;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                        z-index: 1000;
                        max-height: 200px;
                        overflow-y: auto;
                        padding: 6px 0;
                        margin-top: 4px;
                    }
                    .multi-select-dropdown.active {
                        display: block;
                    }
                    .multi-select-dropdown label {
                        display: flex;
                        align-items: center;
                        gap: 8px;
                        padding: 8px 12px;
                        font-size: 0.85rem;
                        color: #4a3528;
                        cursor: pointer;
                        text-transform: none;
                        letter-spacing: 0;
                        font-weight: 500;
                        border-bottom: none;
                        width: 100%;
                        margin: 0;
                    }
                    .multi-select-dropdown label:hover {
                        background: #faf6f2;
                    }
                    .multi-select-dropdown input[type="checkbox"] {
                        width: 16px;
                        height: 16px;
                        accent-color: #76493b;
                        cursor: pointer;
                    }

                    /* Custom Single-select Dropdown (Tab Xem lịch NV) */
                    .single-select-container {
                        position: relative;
                        width: 100%;
                    }
                    .single-select-trigger {
                        padding: 8px 30px 8px 10px;
                        border: 1px solid #d7bfa4;
                        border-radius: 7px;
                        font-size: 0.85rem;
                        background: #fff url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%2376493b' stroke-width='2' fill='none' stroke-linecap='round'/%3E%3C/svg%3E") no-repeat right 10px center;
                        color: #4a3528;
                        cursor: pointer;
                        user-select: none;
                        white-space: nowrap;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        width: 100%;
                    }
                    .single-select-panel {
                        display: none;
                        position: fixed;
                        background: #fff;
                        border: 1px solid #d7bfa4;
                        border-radius: 7px;
                        box-shadow: 0 6px 20px rgba(0,0,0,0.15);
                        z-index: 9999;
                        min-width: 220px;
                        max-height: 260px;
                        overflow: hidden;
                        flex-direction: column;
                    }
                    .single-select-panel.active {
                        display: flex;
                    }
                    .single-select-search {
                        padding: 8px 10px;
                        border: none;
                        border-bottom: 1px solid #ede0d8;
                        font-size: 0.83rem;
                        outline: none;
                        width: 100%;
                        border-radius: 0;
                        flex-shrink: 0;
                    }
                    .single-select-list {
                        overflow-y: auto;
                        flex: 1;
                        padding: 4px 0;
                    }
                    .single-select-option {
                        padding: 9px 14px;
                        font-size: 0.85rem;
                        color: #4a3528;
                        cursor: pointer;
                        white-space: nowrap;
                    }
                    .single-select-option:hover,
                    .single-select-option.selected {
                        background: #f5ece4;
                        color: #76493b;
                        font-weight: 600;
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

                                <c:if test="${not empty error}">
                                    <div class="alert alert-error">${error}</div>
                                </c:if>
                                <c:if test="${not empty success}">
                                    <div class="alert alert-success">${success}</div>
                                </c:if>
                                <c:if test="${param.msg == 'assigned'}">
                                    <div class="alert alert-success">Đã gán ca thành công.</div>
                                </c:if>
                                <c:if test="${param.msg == 'assigned_multi'}">
                                    <div class="alert alert-success">Đã gán ca thành công cho ${param.cnt} nhân viên.</div>
                                </c:if>
                                <c:if test="${param.msg == 'unassigned'}">
                                    <div class="alert alert-success">Đã huỷ ca.</div>
                                </c:if>
                                <c:if test="${param.msg == 'unassign_failed'}">
                                    <div class="alert alert-error">Không thể huỷ — ca đã được điểm danh.</div>
                                </c:if>
                                <c:if test="${param.msg == 'plan_saved'}">
                                    <div class="alert alert-success">Đã lưu kế hoạch ca tháng.</div>
                                </c:if>
                                <c:if test="${param.msg == 'month_assigned'}">
                                    <div class="alert alert-success">Đã áp dụng phân ca cho cả tháng.</div>
                                </c:if>
                                <c:if test="${param.msg == 'month_assigned_multi'}">
                                    <div class="alert alert-success">Đã áp dụng phân ca cho cả tháng cho ${param.cnt} nhân viên.</div>
                                </c:if>
                                <c:if test="${param.msg == 'plan_cancelled'}">
                                    <div class="alert alert-success">Đã huỷ kế hoạch ca tháng.</div>
                                </c:if>
                                <c:if test="${param.msg == 'plan_cancel_failed'}">
                                    <div class="alert alert-error">Không thể huỷ kế hoạch.</div>
                                </c:if>
                                <c:if test="${param.msg == 'approved_success'}">
                                    <div class="alert alert-success">Đã phê duyệt yêu cầu thành công.</div>
                                </c:if>
                                <c:if test="${param.msg == 'rejected_success'}">
                                    <div class="alert alert-success">Đã từ chối yêu cầu thành công.</div>
                                </c:if>

                                <!-- TABS -->
                                <div class="tabs">
                                    <button class="tab-btn active" onclick="switchTab('daily')"><i
                                            class="fas fa-calendar-day"></i> Phân ca ngày</button>
                                    <button class="tab-btn" onclick="switchTab('monthly')"><i
                                            class="fas fa-calendar-alt"></i> Phân ca tháng</button>
                                    <button class="tab-btn" onclick="switchTab('view')"><i
                                            class="fas fa-user-clock"></i> Xem lịch NV</button>
                                    <button class="tab-btn" onclick="switchTab('requests')">
                                        <i class="fas fa-paper-plane"></i> Yêu cầu xin nghỉ
                                        <c:if test="${not empty pendingRequests}">
                                            <span style="background:#c14b4b; color:#fff; border-radius:50%; padding:2px 6px; font-size:0.65rem; margin-left:4px; font-weight:700;">${pendingRequests.size()}</span>
                                        </c:if>
                                    </button>
                                </div>

                                <!-- ===== TAB 1: PHÂN CA THEO NGÀY ===== -->
                                <div id="tab-daily" class="tab-content active">
                                    <div class="card">
                                        <div class="section-title"><i class="fas fa-plus-circle"></i> Gán ca theo ngày
                                        </div>
                                        <form method="post"
                                            action="${pageContext.request.contextPath}/owner/shift-roster" class="row"
                                            onsubmit="return validateRosterForm(this);">
                                            <input type="hidden" name="action" value="assign">
                                            <input type="hidden" name="date" value="${date}">
                                            <div class="field field-sm" style="min-width:150px;">
                                                <label>Ngày</label>
                                                <input type="date" name="dateDisplay" value="${date}"
                                                     onchange="document.querySelector('[name=date]').value = this.value; this.form.action = '${pageContext.request.contextPath}/owner/shift-roster'; this.form.method = 'get'; this.form.submit();">
                                            </div>
                                            <div class="field">
                                                <label>Nhân viên</label>
                                                <div class="multi-select-container">
                                                    <div class="multi-select-trigger" onclick="toggleMultiSelect(this, event)">-- Chọn nhân viên (0) --</div>
                                                    <div class="multi-select-dropdown">
                                                        <label><input type="checkbox" onchange="toggleSelectAllStaff(this)"> <b>Chọn tất cả</b></label>
                                                        <c:forEach var="s" items="${staffList}">
                                                            <label><input type="checkbox" name="employeeIDs" value="${s.employeeID}" onchange="updateMultiSelectLabel(this)"> ${s.fullName}</label>
                                                        </c:forEach>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="field">
                                                <label>Ca</label>
                                                <select name="templateID" required>
                                                    <option value="">-- Chọn ca --</option>
                                                    <c:forEach var="t" items="${templates}">
                                                        <option value="${t.templateID}">${t.shiftName} (
                                                            <fmt:formatDate value="${t.startTime}" pattern="HH:mm" />-
                                                            <fmt:formatDate value="${t.endTime}" pattern="HH:mm" />)
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="field field-sm" style="min-width:auto;">
                                                <label>&nbsp;</label>
                                                <button type="submit" class="btn btn-primary"><i
                                                        class="fas fa-plus"></i> Gán</button>
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
                                                        <td>
                                                            <fmt:formatDate value="${r.startTime}" pattern="HH:mm" /> -
                                                            <fmt:formatDate value="${r.endTime}" pattern="HH:mm" />
                                                        </td>
                                                        <td><span class="badge badge-${r.status}">${r.status}</span>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${r.status == 'scheduled'}">
                                                                    <form method="post"
                                                                        action="${pageContext.request.contextPath}/owner/shift-roster"
                                                                        style="margin:0;"
                                                                        onsubmit="return confirm('Huỷ ca này?');">
                                                                        <input type="hidden" name="action"
                                                                            value="unassign">
                                                                        <input type="hidden" name="shiftID"
                                                                            value="${r.shiftID}">
                                                                        <input type="hidden" name="date"
                                                                            value="${date}">
                                                                        <button type="submit"
                                                                            class="btn btn-sm btn-danger"><i
                                                                                class="fas fa-times"></i> Huỷ</button>
                                                                    </form>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="badge badge-${r.status}"
                                                                        style="font-size:0.7rem;">Đã xử lý</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                                <c:if test="${empty roster}">
                                                    <tr>
                                                        <td colspan="5"
                                                            style="text-align:center; padding:20px; color:#8a6e5a;">Chưa
                                                            có ca nào trong ngày ${date}.</td>
                                                    </tr>
                                                </c:if>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>

                                <!-- ===== TAB 2: PHÂN CA THEO THÁNG ===== -->
                                <div id="tab-monthly" class="tab-content">
                                    <div class="card">
                                        <div class="section-title"><i class="fas fa-calendar-check"></i> Gán ca cả tháng
                                        </div>
                                        <form method="post"
                                            action="${pageContext.request.contextPath}/owner/shift-roster" class="row"
                                            onsubmit="return validateRosterForm(this);">
                                            <input type="hidden" name="action" value="assignMonth">
                                            <div class="field">
                                                <label>Nhân viên</label>
                                                <div class="multi-select-container">
                                                    <div class="multi-select-trigger" onclick="toggleMultiSelect(this, event)">-- Chọn nhân viên (0) --</div>
                                                    <div class="multi-select-dropdown">
                                                        <label><input type="checkbox" onchange="toggleSelectAllStaff(this)"> <b>Chọn tất cả</b></label>
                                                        <c:forEach var="s" items="${staffList}">
                                                            <label><input type="checkbox" name="employeeIDs" value="${s.employeeID}" onchange="updateMultiSelectLabel(this)"> ${s.fullName}</label>
                                                        </c:forEach>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="field">
                                                <label>Ca</label>
                                                <select name="templateID" required>
                                                    <option value="">-- Chọn ca --</option>
                                                    <c:forEach var="t" items="${templates}">
                                                        <option value="${t.templateID}">${t.shiftName} (
                                                            <fmt:formatDate value="${t.startTime}" pattern="HH:mm" />-
                                                            <fmt:formatDate value="${t.endTime}" pattern="HH:mm" />)
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="field field-sm">
                                                <label>Tháng</label>
                                                <select name="month" required>
                                                    <c:forEach var="m" begin="1" end="12">
                                                        <option value="${m}" ${m==planMonth ? 'selected' : '' }>${m}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="field field-sm">
                                                <label>Năm</label>
                                                <input type="number" name="year" min="2024" value="${planYear}" required
                                                    style="width:80px;">
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
                                                <button type="submit" class="btn btn-primary"><i
                                                        class="fas fa-calendar-check"></i> Phân ca</button>
                                            </div>
                                        </form>
                                    </div>

                                    <!-- Bảng kế hoạch tháng -->
                                    <div class="card" style="padding:0;">
                                        <div
                                            style="padding:12px 18px; border-bottom:1px solid #f5ece4; display:flex; align-items:center; gap:10px; flex-wrap:wrap;">
                                            <span class="section-title" style="margin:0;"><i class="fas fa-list"></i> Kế
                                                hoạch tháng</span>
                                            <form method="get"
                                                action="${pageContext.request.contextPath}/owner/shift-roster"
                                                style="display:flex; gap:8px; align-items:center; margin-left:auto;">
                                                <input type="hidden" name="date" value="${date}">
                                                <select name="planMonth"
                                                    style="width:auto; padding:5px 8px; font-size:0.8rem;"
                                                    onchange="this.form.submit()">
                                                    <c:forEach var="m" begin="1" end="12">
                                                        <option value="${m}" ${m==planMonth ? 'selected' : '' }>T${m}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                                <input type="number" name="planYear" min="2024" value="${planYear}"
                                                    style="width:70px; padding:5px 8px; font-size:0.8rem;"
                                                    onchange="this.form.submit()">
                                            </form>
                                        </div>
                                        <div class="filter-bar">
                                            <label for="filterShiftMonthly">Lọc theo
                                                ca:</label>
                                            <select id="filterShiftMonthly"
                                                onchange="filterRosterTable('monthlyPlanBody', this.value, 'monthlyCountBadge')">
                                                <option value="">-- Tất cả ca --</option>
                                                <c:forEach var="t" items="${templates}">
                                                    <option value="${t.shiftName}">${t.shiftName}</option>
                                                </c:forEach>
                                            </select>
                                            <span class="filter-count" id="monthlyCountBadge"></span>
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
                                            <tbody id="monthlyPlanBody">
                                                <c:forEach var="p" items="${monthlyPlans}">
                                                    <tr data-shift="${p.templateName}">
                                                        <td>${p.employeeName}</td>
                                                        <td>${p.templateName}</td>
                                                        <td>
                                                            <fmt:formatDate value="${p.startTime}" pattern="HH:mm" /> -
                                                            <fmt:formatDate value="${p.endTime}" pattern="HH:mm" />
                                                        </td>
                                                        <td>${p.effectiveMonth}/${p.effectiveYear}</td>
                                                        <td><span class="badge badge-${p.status}">${p.status}</span>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when
                                                                    test="${p.status == 'DRAFT' or p.status == 'NOTIFIED'}">
                                                                    <form method="post"
                                                                        action="${pageContext.request.contextPath}/owner/shift-roster"
                                                                        style="margin:0;"
                                                                        onsubmit="return confirm('Huỷ kế hoạch này?');">
                                                                        <input type="hidden" name="action"
                                                                            value="cancelPlan">
                                                                        <input type="hidden" name="planID"
                                                                            value="${p.planID}">
                                                                        <input type="hidden" name="planYear"
                                                                            value="${p.effectiveYear}">
                                                                        <input type="hidden" name="planMonth"
                                                                            value="${p.effectiveMonth}">
                                                                        <button type="submit"
                                                                            class="btn btn-sm btn-danger"><i
                                                                                class="fas fa-times"></i> Huỷ</button>
                                                                    </form>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="badge badge-${p.status}"
                                                                        style="font-size:0.68rem;">Đã ${p.status ==
                                                                        'APPLIED' ? 'áp dụng' : 'huỷ'}</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                                <c:if test="${empty monthlyPlans}">
                                                    <tr>
                                                        <td colspan="6"
                                                            style="text-align:center; padding:20px; color:#8a6e5a;">Chưa
                                                            có kế hoạch ca cho tháng ${planMonth}/${planYear}.</td>
                                                    </tr>
                                                </c:if>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>

                                <!-- ===== TAB 3: XEM LỊCH NHÂN VIÊN ===== -->
                                <div id="tab-view" class="tab-content">
                                    <div class="card">
                                        <div class="section-title"><i class="fas fa-user-clock"></i> Xem lịch ca nhân
                                            viên</div>
                                        <form method="get"
                                            action="${pageContext.request.contextPath}/owner/shift-roster" class="row">
                                            <input type="hidden" name="date" value="${date}">
                                            <input type="hidden" name="planYear" value="${planYear}">
                                            <input type="hidden" name="planMonth" value="${planMonth}">
                                            <input type="hidden" name="activeTab" value="view">
                                            <div class="field">
                                                <label>Nhân viên</label>
                                                <!-- Hidden input gửi giá trị khi submit -->
                                                <input type="hidden" name="viewEmployeeID" id="viewEmployeeIDInput" value="${viewEmployeeID}">
                                                <div class="single-select-container" id="singleSelectContainer">
                                                    <div class="single-select-trigger" id="singleSelectTrigger"
                                                         onclick="toggleSingleSelect(event)">
                                                        <c:choose>
                                                            <c:when test="${not empty viewEmployeeName}">${viewEmployeeName}</c:when>
                                                            <c:otherwise>-- Chọn nhân viên --</c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                    <div class="single-select-panel" id="singleSelectPanel">
                                                        <div class="single-select-list" id="singleSelectList">
                                                            <div class="single-select-option" data-value="" onclick="selectSingleOption(this, '', '-- Chọn nhân viên --')">-- Chọn --</div>
                                                            <c:forEach var="s" items="${staffList}">
                                                                <div class="single-select-option ${s.employeeID==viewEmployeeID ? 'selected' : ''}"
                                                                     data-value="${s.employeeID}"
                                                                     onclick="selectSingleOption(this, '${s.employeeID}', '${s.fullName}')">${s.fullName}</div>
                                                            </c:forEach>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="field field-sm">
                                                <label>Tháng</label>
                                                <select name="viewMonth">
                                                    <c:forEach var="m" begin="1" end="12">
                                                        <option value="${m}" ${m==viewMonth ? 'selected' : '' }>${m}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="field field-sm">
                                                <label>Năm</label>
                                                <input type="number" name="viewYear" min="2024" value="${viewYear}"
                                                    style="width:80px;">
                                            </div>
                                            <div class="field field-sm" style="min-width:auto;">
                                                <label>&nbsp;</label>
                                                <button type="submit" class="btn btn-primary"><i class="fas fa-eye"></i>
                                                    Xem</button>
                                            </div>
                                        </form>
                                    </div>

                                    <c:if test="${not empty staffSchedule}">
                                        <div class="card" style="padding:0;">
                                            <div style="padding:12px 18px; border-bottom:1px solid #f5ece4;">
                                                <strong style="color:#76493b;"><i class="fas fa-user"></i>
                                                    ${viewEmployeeName} — Tháng ${viewMonth}/${viewYear}</strong>
                                                <span
                                                    style="float:right; font-size:0.8rem; color:#8a6e5a;">${staffSchedule.size()}
                                                    ca</span>
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
                                                            <td>
                                                                <fmt:formatDate value="${s.workDate}" pattern="dd/MM" />
                                                            </td>
                                                            <td>
                                                                <fmt:formatDate value="${s.workDate}" pattern="u"
                                                                    var="dowNum" />
                                                                <c:choose>
                                                                    <c:when test="${dowNum == '1'}">T2</c:when>
                                                                    <c:when test="${dowNum == '2'}">T3</c:when>
                                                                    <c:when test="${dowNum == '3'}">T4</c:when>
                                                                    <c:when test="${dowNum == '4'}">T5</c:when>
                                                                    <c:when test="${dowNum == '5'}">T6</c:when>
                                                                    <c:when test="${dowNum == '6'}">T7</c:when>
                                                                    <c:when test="${dowNum == '7'}"><span
                                                                            style="color:#c14b4b; font-weight:600;">CN</span>
                                                                    </c:when>
                                                                </c:choose>
                                                            </td>
                                                            <td>${s.shiftName}</td>
                                                            <td>
                                                                <fmt:formatDate value="${s.startTime}"
                                                                    pattern="HH:mm" /> -
                                                                <fmt:formatDate value="${s.endTime}" pattern="HH:mm" />
                                                            </td>
                                                            <td><span class="badge badge-${s.status}">${s.status}</span>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </c:if>
                                    <c:if test="${viewEmployeeID > 0 && empty staffSchedule}">
                                        <div class="card">
                                            <p style="text-align:center; color:#8a6e5a; padding:16px 0;">
                                                <strong>${viewEmployeeName}</strong> chưa có ca nào trong tháng
                                                ${viewMonth}/${viewYear}.</p>
                                        </div>
                                    </c:if>
                                </div>

                                <!-- ===== TAB 4: YÊU CẦU ĐỔI CA / XIN NGHỈ ===== -->
                                <div id="tab-requests" class="tab-content">
                                    <div class="card" style="overflow-x:auto;">
                                        <div class="section-title"><i class="fas fa-paper-plane"></i> Yêu cầu đổi ca / xin nghỉ đang chờ duyệt</div>
                                        <table>
                                            <thead>
                                                <tr>
                                                    <th>Loại</th>
                                                    <th>Nhân viên</th>
                                                    <th>Ca yêu cầu</th>
                                                    <th>Ca muốn đổi</th>
                                                    <th>Lý do</th>
                                                    <th>Thời gian gửi</th>
                                                    <th>Thao tác</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="pr" items="${pendingRequests}">
                                                    <tr>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${pr.requestType == 'swap'}">
                                                                    <span class="badge" style="background:#cfe2ff; color:#084298;">Đổi ca</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="badge" style="background:#f8d7da; color:#842029;">Xin nghỉ</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td><strong>${pr.reqEmployeeName}</strong></td>
                                                        <td>
                                                            <div style="font-weight:600; color:#76493b;">${pr.reqShiftName}</div>
                                                            <div style="font-size:0.75rem; color:#8a6e5a; margin-top:2px;">
                                                                <fmt:formatDate value="${pr.reqWorkDate}" pattern="dd/MM/yyyy" /> (<fmt:formatDate value="${pr.reqStartTime}" pattern="HH:mm" /> - <fmt:formatDate value="${pr.reqEndTime}" pattern="HH:mm" />)
                                                            </div>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${pr.requestType == 'swap' && not empty pr.targetShiftID}">
                                                                    <div style="font-weight:600; color:#5d3a2e;">${pr.targetEmployeeName}</div>
                                                                    <div style="font-size:0.75rem; color:#8a6e5a; margin-top:2px;">
                                                                        ${pr.targetShiftName} - <fmt:formatDate value="${pr.targetWorkDate}" pattern="dd/MM/yyyy" /> (<fmt:formatDate value="${pr.targetStartTime}" pattern="HH:mm" /> - <fmt:formatDate value="${pr.targetEndTime}" pattern="HH:mm" />)
                                                                    </div>
                                                                    <div style="font-size:0.7rem; color:#28a745; margin-top:4px; font-weight:600;">
                                                                        <i class="fas fa-check-double"></i> Đồng nghiệp đã đồng ý
                                                                    </div>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span style="color:#a0a0a0;">N/A (Nghỉ ca)</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td style="max-width: 220px; word-wrap: break-word; white-space: normal; font-size:0.82rem; color:#5d3a2e;">
                                                            <c:out value="${pr.reason}" />
                                                        </td>
                                                        <td style="font-size:0.78rem; color:#8a6e5a;">
                                                            <fmt:formatDate value="${pr.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                                        </td>
                                                        <td>
                                                            <div style="display:flex; gap:6px;">
                                                                <form method="post" action="${pageContext.request.contextPath}/owner/shift-roster" style="margin:0;" onsubmit="return confirm('Duyệt yêu cầu này?');">
                                                                    <input type="hidden" name="action" value="approveRequest">
                                                                    <input type="hidden" name="swapID" value="${pr.swapID}">
                                                                    <input type="hidden" name="date" value="${date}">
                                                                    <input type="hidden" name="activeTab" value="requests">
                                                                    <button type="submit" class="btn btn-sm" style="background:#28a745; color:#fff; border-color:#28a745; font-size:0.75rem; padding:5px 10px;">
                                                                        <i class="fas fa-check"></i> Duyệt
                                                                    </button>
                                                                </form>
                                                                <form method="post" action="${pageContext.request.contextPath}/owner/shift-roster" style="margin:0;" onsubmit="return confirm('Từ chối yêu cầu này?');">
                                                                    <input type="hidden" name="action" value="rejectRequest">
                                                                    <input type="hidden" name="swapID" value="${pr.swapID}">
                                                                    <input type="hidden" name="date" value="${date}">
                                                                    <input type="hidden" name="activeTab" value="requests">
                                                                    <button type="submit" class="btn btn-sm btn-danger" style="font-size:0.75rem; padding:5px 10px;">
                                                                        <i class="fas fa-times"></i> Từ chối
                                                                    </button>
                                                                </form>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                                <c:if test="${empty pendingRequests}">
                                                    <tr>
                                                        <td colspan="7" style="text-align:center; padding:24px; color:#8a6e5a;">Chưa có yêu cầu nào đang chờ duyệt.</td>
                                                    </tr>
                                                </c:if>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>

                            </main>
                    </div>
                    <%@ include file="/views/includes/footer.jsp" %>

                        <script>
                            function toggleMultiSelect(trigger, event) {
                                event.stopPropagation();
                                document.querySelectorAll('.multi-select-dropdown').forEach(function(dp) {
                                    if (dp !== trigger.nextElementSibling) {
                                        dp.classList.remove('active');
                                    }
                                });
                                trigger.nextElementSibling.classList.toggle('active');
                            }

                            function toggleSelectAllStaff(masterCheckbox) {
                                var container = masterCheckbox.closest('.multi-select-container');
                                var checkboxes = container.querySelectorAll('input[name="employeeIDs"]');
                                checkboxes.forEach(function(cb) {
                                    cb.checked = masterCheckbox.checked;
                                });
                                updateMultiSelectLabel(container.querySelector('.multi-select-trigger'));
                            }

                            function updateMultiSelectLabel(trigger) {
                                var container = trigger.closest('.multi-select-container');
                                var checkboxes = container.querySelectorAll('input[name="employeeIDs"]');
                                var checkedCount = 0;
                                var selectedNames = [];
                                
                                checkboxes.forEach(function(cb) {
                                    if (cb.checked) {
                                        checkedCount++;
                                        selectedNames.push(cb.parentNode.textContent.trim());
                                    }
                                });

                                var master = container.querySelector('.multi-select-dropdown label:first-child input');
                                if (master) {
                                    master.checked = (checkedCount === checkboxes.length && checkboxes.length > 0);
                                }

                                if (checkedCount === 0) {
                                    trigger.textContent = "-- Chọn nhân viên (0) --";
                                } else if (checkedCount === 1) {
                                    trigger.textContent = selectedNames[0];
                                } else if (checkedCount === checkboxes.length) {
                                    trigger.textContent = "Tất cả (" + checkedCount + ")";
                                } else {
                                    trigger.textContent = "Đã chọn (" + checkedCount + ")";
                                }
                            }

                            function validateRosterForm(form) {
                                var checked = form.querySelectorAll('input[name="employeeIDs"]:checked');
                                if (checked.length === 0) {
                                    alert('Vui lòng chọn ít nhất một nhân viên.');
                                    return false;
                                }
                                return true;
                            }

                            // Close dropdown when clicking outside
                            document.addEventListener('click', function(event) {
                                if (!event.target.closest('.multi-select-container')) {
                                    document.querySelectorAll('.multi-select-dropdown').forEach(function(dp) {
                                        dp.classList.remove('active');
                                    });
                                }
                                if (!event.target.closest('.single-select-container') &&
                                    !event.target.closest('.single-select-panel')) {
                                    closeSingleSelect();
                                }
                            });

                            /* ===== Single-select dropdown (Tab Xem lịch NV) ===== */
                            function toggleSingleSelect(event) {
                                event.stopPropagation();
                                var panel = document.getElementById('singleSelectPanel');
                                if (panel.classList.contains('active')) {
                                    closeSingleSelect();
                                } else {
                                    openSingleSelect();
                                }
                            }

                            function openSingleSelect() {
                                var trigger = document.getElementById('singleSelectTrigger');
                                var panel   = document.getElementById('singleSelectPanel');
                                var rect    = trigger.getBoundingClientRect();
                                var panelH  = 260; // max-height
                                var spaceBelow = window.innerHeight - rect.bottom - 8;
                                var spaceAbove = rect.top - 8;

                                panel.style.width = Math.max(rect.width, 220) + 'px';
                                panel.style.left  = rect.left + 'px';

                                if (spaceBelow >= Math.min(panelH, 160) || spaceBelow >= spaceAbove) {
                                    // Mở xuống
                                    panel.style.top    = (rect.bottom + window.scrollY + 4) + 'px';
                                    panel.style.bottom = 'auto';
                                } else {
                                    // Mở lên (chỉ khi thực sự không đủ chỗ xuống)
                                    panel.style.top    = 'auto';
                                    panel.style.bottom = (window.innerHeight - rect.top + 4) + 'px';
                                }
                                panel.classList.add('active');
                                // Focus search box
                                var search = panel.querySelector('.single-select-search');
                                if (search) { search.value = ''; filterSingleSelect(search); search.focus(); }
                                // Scroll selected item into view
                                var sel = panel.querySelector('.selected');
                                if (sel) sel.scrollIntoView({block: 'nearest'});
                            }

                            function closeSingleSelect() {
                                var panel = document.getElementById('singleSelectPanel');
                                if (panel) panel.classList.remove('active');
                            }

                            function selectSingleOption(el, value, label) {
                                document.getElementById('viewEmployeeIDInput').value = value;
                                document.getElementById('singleSelectTrigger').textContent = label || '-- Chọn nhân viên --';
                                // Update selected highlight
                                document.querySelectorAll('#singleSelectList .single-select-option').forEach(function(o) {
                                    o.classList.remove('selected');
                                });
                                el.classList.add('selected');
                                closeSingleSelect();
                            }

                            function filterSingleSelect(input) {
                                var q = input.value.toLowerCase();
                                document.querySelectorAll('#singleSelectList .single-select-option').forEach(function(opt) {
                                    var text = opt.textContent.toLowerCase();
                                    opt.style.display = (!q || text.includes(q)) ? '' : 'none';
                                });
                            }

                            // Reposition panel on scroll/resize
                            window.addEventListener('scroll', function() {
                                var panel = document.getElementById('singleSelectPanel');
                                if (panel && panel.classList.contains('active')) { openSingleSelect(); }
                            }, true);
                            window.addEventListener('resize', function() {
                                var panel = document.getElementById('singleSelectPanel');
                                if (panel && panel.classList.contains('active')) { openSingleSelect(); }
                            });

                            /**
                             * Lọc các hàng trong bảng theo tên ca.
                             * @param {string} tbodyId   - id của <tbody> cần lọc
                             * @param {string} shiftName - giá trị ca được chọn ('' = tất cả)
                             * @param {string} badgeId   - id của span hiển thị số hàng
                             */
                            function filterRosterTable(tbodyId, shiftName, badgeId) {
                                var tbody = document.getElementById(tbodyId);
                                if (!tbody) return;
                                var rows = tbody.querySelectorAll('tr[data-shift]');
                                var visibleCount = 0;
                                rows.forEach(function (row) {
                                    var rowShift = row.getAttribute('data-shift') || '';
                                    var match = !shiftName || rowShift === shiftName;
                                    row.style.display = match ? '' : 'none';
                                    if (match) visibleCount++;
                                });
                                // Cập nhật badge đếm
                                var badge = document.getElementById(badgeId);
                                if (badge) {
                                    if (shiftName) {
                                        badge.textContent = 'Hiển thị ' + visibleCount + ' / ' + rows.length + ' bản ghi';
                                    } else {
                                        badge.textContent = '';
                                    }
                                }
                                // Ẩn/hiện dòng "chưa có dữ liệu" nếu filter lọc hết
                                var emptyRow = tbody.querySelector('tr:not([data-shift])');
                                if (emptyRow) emptyRow.style.display = '';
                            }

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
                                    btns.forEach(function(btn) {
                                        var onclickAttr = btn.getAttribute('onclick') || '';
                                        if (onclickAttr.indexOf("'" + tab + "'") !== -1) {
                                            btn.classList.add('active');
                                        }
                                    });
                                }
                                // Auto-switch to view tab if staffSchedule loaded
                                <c:if test="${not empty staffSchedule || (viewEmployeeID > 0 && empty staffSchedule)}">
                                    if (!params.get('activeTab')) {
                                        document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
                                        document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
                                        document.getElementById('tab-view').classList.add('active');
                                        
                                        var btns = document.querySelectorAll('.tab-btn');
                                        btns.forEach(function(btn) {
                                            var onclickAttr = btn.getAttribute('onclick') || '';
                                            if (onclickAttr.indexOf("'view'") !== -1) {
                                                btn.classList.add('active');
                                            }
                                        });
                                    }
                                </c:if>
                            })();
                        </script>
            </body>

            </html>