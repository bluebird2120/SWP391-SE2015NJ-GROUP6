<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Schedule</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin:0; font-family:'Inter',sans-serif; background:#faf6f2; }
        .main { flex:1; padding:24px 32px; min-width:0; }
        .page-title { font-family:'Playfair Display',serif; color:#76493b; font-size:1.6rem; margin:0 0 4px; }
        .page-sub { color:#a0714f; font-size:0.9rem; margin-bottom:18px; }
        .card { background:#fff; border:1px solid #ede0d8; border-radius:12px; padding:18px; margin-bottom:16px; }
        .toolbar { display:flex; align-items:center; gap:12px; flex-wrap:wrap; }
        .month-label { font-family:'Playfair Display',serif; color:#76493b; font-size:1.2rem; font-weight:700; flex:1; text-align:center; }
        .btn { padding:8px 14px; border-radius:8px; border:1px solid #d7bfa4; background:#fff; cursor:pointer; font-size:0.85rem; font-weight:600; color:#76493b; text-decoration:none; display:inline-flex; gap:6px; align-items:center; }
        .btn:hover { background:#f5ece4; }
        .btn-primary { background:#76493b; color:#fff; border-color:#76493b; }
        .btn-primary:hover { background:#5d3a2e; }
        .form-inline { display:flex; gap:8px; align-items:center; flex-wrap:wrap; margin-top:14px; }
        select, input[type="number"] { padding:7px 10px; border:1px solid #d7bfa4; border-radius:7px; font-family:inherit; font-size:0.85rem; min-width:90px; }
        .form-inline-error {
            flex-basis: 100%;
            width: 100%;
            margin-top: 4px;
            padding: 8px 10px;
            border-radius: 7px;
            border: 1px solid #f5c2c7;
            background: #f8d7da;
            color: #721c24;
            font-size: 0.82rem;
            font-weight: 600;
        }
        .stats { color:#8a6e5a; font-size:0.85rem; }
        .calendar { display:grid; grid-template-columns:repeat(7,1fr); gap:6px; }
        .dow-head { text-align:center; font-size:0.75rem; font-weight:700; color:#a0714f; text-transform:uppercase; letter-spacing:0.05em; padding:8px 0; }
        .dow-head.sun { color:#c14b4b; }
        .day-cell { background:#fff; border:1px solid #ede0d8; border-radius:8px; min-height:110px; padding:6px 8px; display:flex; flex-direction:column; gap:4px; }
        .day-cell.empty { background:#faf6f2; border-style:dashed; border-color:#ede0d8; }
        .day-cell.today { border-color:#76493b; box-shadow:0 0 0 2px rgba(118,73,59,0.15); }
        .day-num { font-size:0.82rem; font-weight:700; color:#5d3a2e; }
        .day-num.sun { color:#c14b4b; }
        .day-cell.today .day-num { color:#76493b; }
        .shift-pill { font-size:0.72rem; line-height:1.25; padding:6px 8px; border-radius:6px; background:#f5ece4; border:1px solid #ede0d8; display:flex; flex-direction:column; gap:2px; }
        .shift-name { font-weight:600; color:#5d3a2e; }
        .shift-time { color:#8a6e5a; font-size:0.68rem; }
        .badge { display:inline-block; padding:2px 6px; border-radius:10px; font-size:0.62rem; font-weight:700; text-align:center; }
        .badge-scheduled { background:#e2e3e5; color:#41464b; }
        .badge-present   { background:#d4edda; color:#155724; }
        .badge-late      { background:#fff3cd; color:#856404; }
        .badge-absent    { background:#f8d7da; color:#721c24; }
        .empty-state { text-align:center; color:#8a6e5a; padding:24px 0; font-size:0.92rem; }

        /* Modal Styles */
        .modal-overlay {
            position: fixed;
            top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0,0,0,0.45);
            display: none;
            align-items: center;
            justify-content: center;
            z-index: 1000;
        }
        .modal-content {
            background: #fff;
            border-radius: 12px;
            width: 90%;
            max-width: 500px;
            box-shadow: 0 8px 30px rgba(0,0,0,0.15);
            animation: slideDown 0.25s ease-out;
        }
        @keyframes slideDown {
            from { transform: translateY(-20px); opacity: 0; }
            to { transform: translateY(0); opacity: 1; }
        }
        .modal-header {
            background: #76493b;
            color: #fff;
            padding: 16px 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-top-left-radius: 12px;
            border-top-right-radius: 12px;
        }
        .modal-title {
            font-family: 'Playfair Display', serif;
            font-size: 1.2rem;
            margin: 0;
        }
        .close-modal-btn {
            background: none;
            border: none;
            color: #fff;
            font-size: 1.25rem;
            cursor: pointer;
            padding: 0;
            line-height: 1;
        }
        .modal-tabs {
            display: flex;
            border-bottom: 1px solid #ede0d8;
            background: #fdfaf7;
        }
        .modal-tab {
            flex: 1;
            text-align: center;
            padding: 12px;
            cursor: pointer;
            font-weight: 600;
            color: #8a6e5a;
            transition: all 0.2s;
            font-size: 0.85rem;
        }
        .modal-tab.active {
            color: #76493b;
            border-bottom: 3px solid #76493b;
            background: #fff;
        }
        .modal-body {
            padding: 20px;
        }
        .modal-pane {
            display: none;
        }
        .modal-pane.active {
            display: block;
        }
        .form-group {
            margin-bottom: 16px;
        }
        .form-group label {
            display: block;
            font-size: 0.82rem;
            font-weight: 600;
            color: #5d3a2e;
            margin-bottom: 6px;
        }
        .form-group select, .form-group textarea {
            width: 100%;
            padding: 10px;
            border: 1px solid #d7bfa4;
            border-radius: 8px;
            font-family: inherit;
            font-size: 0.82rem;
            background-color: #fff;
        }
        .form-group textarea {
            resize: none;
            height: 100px;
        }
        .modal-footer {
            padding: 12px 20px;
            background: #fdfaf7;
            border-top: 1px solid #ede0d8;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
            border-bottom-left-radius: 12px;
            border-bottom-right-radius: 12px;
        }
        .error-feedback {
            color: #c14b4b;
            font-size: 0.75rem;
            margin-top: 4px;
            display: none;
        }
        
        /* Custom Single-select Dropdown for cover staff */
        .single-select-container {
            position: relative;
            user-select: none;
        }
        .single-select-trigger {
            padding: 10px;
            border: 1px solid #d7bfa4;
            border-radius: 8px;
            background: #fff;
            cursor: pointer;
            font-size: 0.82rem;
            color: #5d3a2e;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .single-select-panel {
            position: absolute;
            top: 100%; /* Force downward flow */
            left: 0;
            right: 0;
            z-index: 1050;
            background: #fff;
            border: 1px solid #d7bfa4;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(118,73,59,0.15);
            display: none;
            margin-top: 4px;
        }
        .single-select-list {
            max-height: 180px;
            overflow-y: auto;
        }
        .single-select-option {
            padding: 10px;
            cursor: pointer;
            font-size: 0.82rem;
            color: #5d3a2e;
            border-bottom: 1px solid #fdfaf7;
            text-align: left;
        }
        .single-select-option:hover {
            background: #fdfaf7;
            color: #76493b;
        }
        .single-select-option.selected {
            background: #fdfaf7;
            color: #76493b;
            font-weight: bold;
        }

        /* Custom confirmation modal style */
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
            <h1 class="page-title">My Schedule</h1>
            <p class="page-sub">Lịch ca làm việc theo tháng — cập nhật trạng thái điểm danh</p>

            <!-- Local Custom confirmation modal -->
            <div id="localConfirmModal" class="custom-confirm-modal">
                <div class="custom-confirm-content">
                    <div id="localConfirmMessage" class="custom-confirm-message"></div>
                    <div class="custom-confirm-buttons">
                        <button id="localConfirmCancelBtn" class="custom-confirm-btn custom-confirm-btn-cancel">Huỷ</button>
                        <button id="localConfirmOkBtn" class="custom-confirm-btn custom-confirm-btn-ok">Đồng ý</button>
                    </div>
                </div>
            </div>

            <!-- Status Alert Messages -->
            <c:if test="${not empty sessionScope.successMsg}">
                <div style="background:#d4edda; border:1px solid #c3e6cb; color:#155724; padding:12px; border-radius:8px; margin-bottom:16px; display:flex; align-items:center; gap:8px; font-size:0.9rem;">
                    <i class="fas fa-check-circle"></i>
                    <span>${sessionScope.successMsg}</span>
                </div>
                <c:remove var="successMsg" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.errorMsg}">
                <div style="background:#f8d7da; border:1px solid #f5c6cb; color:#721c24; padding:12px; border-radius:8px; margin-bottom:16px; display:flex; align-items:center; gap:8px; font-size:0.9rem;">
                    <i class="fas fa-exclamation-circle"></i>
                    <span>${sessionScope.errorMsg}</span>
                </div>
                <c:remove var="errorMsg" scope="session"/>
            </c:if>
            <c:if test="${not empty requestScope.errorMsg}">
                <div style="background:#f8d7da; border:1px solid #f5c6cb; color:#721c24; padding:12px; border-radius:8px; margin-bottom:16px; display:flex; align-items:center; gap:8px; font-size:0.9rem;">
                    <i class="fas fa-exclamation-circle"></i>
                    <span>${requestScope.errorMsg}</span>
                </div>
            </c:if>

            <div class="card">
                <div class="toolbar">
                    <a class="btn" href="${pageContext.request.contextPath}/staff/my-schedule?year=${prevYear}&month=${prevMonth}">
                        <i class="fas fa-chevron-left"></i> Tháng trước
                    </a>
                    <div class="month-label">Tháng ${month} / ${year}</div>
                    <a class="btn" href="${pageContext.request.contextPath}/staff/my-schedule?year=${nextYear}&month=${nextMonth}">
                        Tháng sau <i class="fas fa-chevron-right"></i>
                    </a>
                </div>
                <form method="get" action="${pageContext.request.contextPath}/staff/my-schedule" class="form-inline" novalidate>
                    <label style="font-size:0.78rem; color:#8a6e5a; font-weight:600;">Đi tới:</label>
                    <select name="month">
                        <c:forEach var="m" begin="1" end="12">
                            <option value="${m}" ${m == month ? 'selected' : ''}>Tháng ${m}</option>
                        </c:forEach>
                    </select>
                    <input type="number" name="year" value="${year}" min="2024">
                    <button type="submit" class="btn btn-primary"><i class="fas fa-search"></i> Xem</button>
                    <span class="stats" style="margin-left:auto;">
                        <i class="fas fa-calendar-check"></i> ${totalShifts} ca trong tháng
                    </span>
                    <c:if test="${not empty scheduleFilterError}">
                        <div class="form-inline-error">${scheduleFilterError}</div>
                    </c:if>
                </form>
            </div>

            <div class="card">
                <div class="calendar">
                    <div class="dow-head sun">CN</div>
                    <div class="dow-head">T2</div>
                    <div class="dow-head">T3</div>
                    <div class="dow-head">T4</div>
                    <div class="dow-head">T5</div>
                    <div class="dow-head">T6</div>
                    <div class="dow-head">T7</div>

                    <c:forEach var="cell" begin="0" end="41">
                        <c:set var="day" value="${cell - firstDow + 1}" />
                        <c:choose>
                            <c:when test="${day < 1 || day > daysInMonth}">
                                <div class="day-cell empty"></div>
                            </c:when>
                            <c:otherwise>
                                <c:set var="isToday" value="${year == currentYear && month == currentMonth && day == currentDay}" />
                                <c:set var="dow" value="${cell % 7}" />
                                <div class="day-cell ${isToday ? 'today' : ''}">
                                    <div class="day-num ${dow == 0 ? 'sun' : ''}">${day}</div>
                                    <c:set var="dayKey" value="${day}" />
                                    <c:set var="shifts" value="${scheduleMap[dayKey.toString()]}" />
                                    <c:if test="${not empty shifts}">
                                        <c:forEach var="s" items="${shifts}">
                                            <div class="shift-pill">
                                                <span class="shift-name">${s.shiftName}</span>
                                                <span class="shift-time">
                                                    <fmt:formatDate value="${s.startTime}" pattern="HH:mm"/> - <fmt:formatDate value="${s.endTime}" pattern="HH:mm"/>
                                                </span>
                                                <span class="badge badge-${s.status}">${s.status}</span>
                                                
                                                <!-- Request Status or Button -->
                                                <c:set var="req" value="${pendingRequests[s.shiftID]}" />
                                                <c:choose>
                                                    <c:when test="${not empty req}">
                                                        <c:choose>
                                                            <c:when test="${req.status == 'pending_colleague'}">
                                                                <span class="badge" style="background:#fff3cd; color:#664d03; margin-top:4px;">
                                                                    <i class="fas fa-hourglass-half"></i> Chờ đồng nghiệp
                                                                </span>
                                                            </c:when>
                                                            <c:when test="${req.requestType != 'leave'}">
                                                                <span class="badge" style="background:#cfe2ff; color:#084298; margin-top:4px;">
                                                                    <i class="fas fa-user-clock"></i> Chờ làm thay
                                                                </span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge" style="background:#f8d7da; color:#842029; margin-top:4px;">
                                                                    <i class="fas fa-calendar-minus"></i> Chờ nghỉ ca
                                                                </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <fmt:formatDate var="shiftDateStr" value="${s.workDate}" pattern="yyyy-MM-dd"/>
                                                        <c:if test="${s.status == 'scheduled' && shiftDateStr >= today}">
                                                            <button type="button" class="request-btn"
                                                                    data-shift-id="${s.shiftID}"
                                                                    data-shift-name="${s.shiftName}"
                                                                    data-work-date="${shiftDateStr}"
                                                                    onclick="openRequestModalFromButton(this)"
                                                                    style="font-size:0.65rem; color:#a0714f; text-decoration:underline; font-weight:600; margin-top:4px; display:inline-block; background:none; border:0; padding:0; cursor:pointer; font-family:inherit; text-align:left;">
                                                                <i class="fas fa-paper-plane"></i> Đổi lịch/Xin nghỉ
                                                            </button>
                                                        </c:if>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </c:forEach>
                                    </c:if>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </div>

                <c:if test="${totalShifts == 0}">
                    <div class="empty-state">
                        <i class="fas fa-calendar-xmark"></i>
                        Chưa có ca nào được gán cho tháng ${month}/${year}.
                    </div>
                </c:if>
            </div>

            <!-- Yêu cầu làm thay từ đồng nghiệp -->
            <div class="card" style="margin-top: 24px; overflow-x: auto;">
                <div class="section-title" style="font-size: 1.1rem; color: #76493b; margin-bottom: 16px; font-weight: 600;">
                    Yêu cầu làm thay từ đồng nghiệp đang chờ bạn xác nhận
                </div>
                <table class="table" style="width: 100%; border-collapse: collapse; font-size: 0.9rem;">
                    <thead>
                        <tr style="border-bottom: 2px solid #e2d2c2; text-align: left; color: #8a6e5a;">
                            <th style="padding: 10px;">Đồng nghiệp</th>
                            <th style="padding: 10px;">Ca cần làm thay</th>
                            <th style="padding: 10px;">Lý do</th>
                            <th style="padding: 10px;">Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="cr" items="${colleagueRequests}">
                            <tr style="border-bottom: 1px solid #f0e6db;">
                                <td style="padding: 10px;"><strong>${cr.reqEmployeeName}</strong></td>
                                <td style="padding: 10px;">
                                    <div style="font-weight: 600; color: #76493b;">${cr.reqShiftName}</div>
                                    <div style="font-size: 0.75rem; color: #8a6e5a;">
                                        <fmt:formatDate value="${cr.reqWorkDate}" pattern="dd/MM/yyyy" /> (<fmt:formatDate value="${cr.reqStartTime}" pattern="HH:mm" /> - <fmt:formatDate value="${cr.reqEndTime}" pattern="HH:mm" />)
                                    </div>
                                </td>
                                <td style="padding: 10px; color: #5d3a2e;"><c:out value="${cr.reason}"/></td>
                                <td style="padding: 10px;">
                                    <div style="display: flex; gap: 8px;">
                                        <form method="post" action="${pageContext.request.contextPath}/staff/my-schedule" style="margin:0;" onsubmit="return showCustomConfirm(this, event, 'Chấp nhận làm thay ca này?');">
                                            <input type="hidden" name="action" value="acceptCoverRequest">
                                            <input type="hidden" name="requestID" value="${cr.requestID}">
                                            <button type="submit" class="btn btn-sm" style="background:#28a745; color:#fff; border-color:#28a745; font-size:0.75rem; padding:4px 8px; border-radius:4px; cursor:pointer;">
                                                Đồng ý
                                            </button>
                                        </form>
                                        <form method="post" action="${pageContext.request.contextPath}/staff/my-schedule" style="margin:0;" onsubmit="return showCustomConfirm(this, event, 'Từ chối làm thay ca này?');">
                                            <input type="hidden" name="action" value="rejectCoverRequest">
                                            <input type="hidden" name="requestID" value="${cr.requestID}">
                                            <button type="submit" class="btn btn-sm" style="background:#dc3545; color:#fff; border-color:#dc3545; font-size:0.75rem; padding:4px 8px; border-radius:4px; cursor:pointer;">
                                                Từ chối
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty colleagueRequests}">
                            <tr>
                                <td colspan="4" style="text-align: center; padding: 20px; color:#8a6e5a;">Không có yêu cầu làm thay nào từ đồng nghiệp cần xác nhận.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </main>
    </div>

    <!-- Request Modal Overlay -->
    <div id="requestModal" class="modal-overlay">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title">Gửi Yêu Cầu Lịch Làm Việc</h3>
                <button type="button" class="close-modal-btn" onclick="closeModal()">&times;</button>
            </div>
            <div class="modal-tabs">
                <div id="tabCover" class="modal-tab active" onclick="switchTab('cover')">
                    <i class="fas fa-sync-alt"></i> Đổi Lịch
                </div>
                <div id="tabLeave" class="modal-tab" onclick="switchTab('leave')">
                    <i class="fas fa-calendar-minus"></i> Xin Nghỉ Ca
                </div>
            </div>
            
            <!-- Tab Cover Pane -->
            <div id="paneCover" class="modal-pane active">
                <form id="coverForm" method="post" action="${pageContext.request.contextPath}/staff/my-schedule" onsubmit="return validateCoverForm()">
                    <input type="hidden" name="action" value="requestCover">
                    <input type="hidden" id="modalRequesterShiftID" name="requesterShiftID">
                    <div class="modal-body">
                        <div class="form-group">
                            <label>Ca làm việc của bạn:</label>
                            <div id="selectedShiftInfo" style="font-size:0.85rem; font-weight:600; color:#76493b; padding:8px 0;"></div>
                        </div>
                        <div class="form-group">
                            <label>Chọn nhân viên làm thay (chưa có ca ngày này):</label>
                            <input type="hidden" id="targetEmployeeSelect" name="targetEmployeeID" value="">
                            <div class="single-select-container">
                                <div class="single-select-trigger" id="customCoverTrigger" onclick="toggleCustomCoverSelect(event)">
                                    <span id="customCoverTriggerLabel">-- Chọn nhân viên làm thay --</span>
                                    <i class="fas fa-chevron-down" style="font-size: 0.75rem; color: #8a6e5a;"></i>
                                </div>
                                <div class="single-select-panel" id="customCoverPanel">
                                    <div class="single-select-list" id="customCoverList">
                                        <div class="single-select-option" data-static="true" data-value="" onclick="selectCustomCoverOption(this, '', '-- Chọn nhân viên làm thay --')">-- Chọn --</div>
                                        <div class="single-select-option" id="noCoverStaffOption" data-empty="true" style="display:none; color:#8a6e5a; cursor:default;">
                                            Không có nhân viên rảnh trong ngày này
                                        </div>
                                        <c:forEach var="entry" items="${availableCoverStaffByDate}">
                                            <c:forEach var="staff" items="${entry.value}">
                                                <div class="single-select-option cover-staff-option" data-date="${entry.key}" data-value="${staff.employeeID}" onclick="selectCustomCoverOption(this, '${staff.employeeID}', '${staff.fullName}')">
                                                    ${staff.fullName}
                                                </div>
                                            </c:forEach>
                                        </c:forEach>
                                    </div>
                                </div>
                            </div>
                            <span id="targetError" class="error-feedback">Vui lòng chọn nhân viên làm thay!</span>
                        </div>
                        <div class="form-group">
                            <label for="coverReason">Lý do nhờ làm thay:</label>
                            <textarea id="coverReason" name="reason" placeholder="Nhập lý do nhờ làm thay của bạn..." maxlength="500"></textarea>
                            <span id="coverReasonError" class="error-feedback">Vui lòng nhập lý do nhờ làm thay!</span>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn" onclick="closeModal()">Hủy</button>
                        <button type="submit" class="btn btn-primary">Gửi Yêu Cầu</button>
                    </div>
                </form>
            </div>

            <!-- Tab Leave Pane -->
            <div id="paneLeave" class="modal-pane">
                <form id="leaveForm" method="post" action="${pageContext.request.contextPath}/staff/my-schedule" onsubmit="return validateLeaveForm()">
                    <input type="hidden" name="action" value="requestLeave">
                    <input type="hidden" id="modalRequesterShiftIDLeave" name="requesterShiftID">
                    <div class="modal-body">
                        <div class="form-group">
                            <label>Ca làm việc của bạn:</label>
                            <div id="selectedShiftInfoLeave" style="font-size:0.85rem; font-weight:600; color:#76493b; padding:8px 0;"></div>
                        </div>
                        <div class="form-group">
                            <label for="leaveReason">Lý do xin nghỉ ca:</label>
                            <textarea id="leaveReason" name="reason" placeholder="Nhập lý do xin nghỉ của bạn..." maxlength="500"></textarea>
                            <span id="leaveReasonError" class="error-feedback">Vui lòng nhập lý do xin nghỉ!</span>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn" onclick="closeModal()">Hủy</button>
                        <button type="submit" class="btn btn-primary">Gửi Yêu Cầu</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <%@ include file="/views/includes/footer.jsp" %>
    
    <script>
        function openRequestModalFromButton(btn) {
            openRequestModal(btn.dataset.shiftId, btn.dataset.shiftName, btn.dataset.workDate);
        }

        function openRequestModal(shiftID, shiftName, workDate) {
            document.getElementById('modalRequesterShiftID').value = shiftID;
            document.getElementById('modalRequesterShiftIDLeave').value = shiftID;
            document.getElementById('selectedShiftInfo').innerText = shiftName + ' (' + formatDateVN(workDate) + ')';
            document.getElementById('selectedShiftInfoLeave').innerText = shiftName + ' (' + formatDateVN(workDate) + ')';
            document.getElementById('requestModal').style.display = 'flex';
            
            // Clear input states
            document.getElementById('coverReason').value = '';
            document.getElementById('leaveReason').value = '';
            document.getElementById('targetEmployeeSelect').value = '';
            var customCoverTriggerLabel = document.getElementById('customCoverTriggerLabel');
            if (customCoverTriggerLabel) {
                customCoverTriggerLabel.innerText = '-- Chọn nhân viên làm thay --';
            }
            document.querySelectorAll('#customCoverList .single-select-option').forEach(function(o) {
                o.classList.remove('selected');
            });
            filterCoverStaffOptions(workDate);
            var panel = document.getElementById('customCoverPanel');
            if (panel) panel.style.display = 'none';
            
            hideErrors();
        }

        function closeModal() {
            document.getElementById('requestModal').style.display = 'none';
        }

        function switchTab(type) {
            const tabs = document.querySelectorAll('.modal-tab');
            const panes = document.querySelectorAll('.modal-pane');
            
            tabs.forEach(t => t.classList.remove('active'));
            panes.forEach(p => p.classList.remove('active'));
            
            if (type === 'cover') {
                document.getElementById('tabCover').classList.add('active');
                document.getElementById('paneCover').classList.add('active');
            } else {
                document.getElementById('tabLeave').classList.add('active');
                document.getElementById('paneLeave').classList.add('active');
            }
            hideErrors();
        }

        function hideErrors() {
            document.querySelectorAll('.error-feedback').forEach(el => el.style.display = 'none');
        }

        function validateCoverForm() {
            hideErrors();
            let isValid = true;
            const target = document.getElementById('targetEmployeeSelect').value;
            const reason = document.getElementById('coverReason').value.trim();
            
            if (!target) {
                document.getElementById('targetError').style.display = 'block';
                isValid = false;
            }
            if (!reason) {
                document.getElementById('coverReasonError').innerText = 'Vui lòng nhập lý do nhờ làm thay!';
                document.getElementById('coverReasonError').style.display = 'block';
                isValid = false;
            } else if (reason.length > 500) {
                document.getElementById('coverReasonError').innerText = 'Lý do không được vượt quá 500 ký tự!';
                document.getElementById('coverReasonError').style.display = 'block';
                isValid = false;
            }
            return isValid;
        }

        function validateLeaveForm() {
            hideErrors();
            let isValid = true;
            const reason = document.getElementById('leaveReason').value.trim();
            
            if (!reason) {
                document.getElementById('leaveReasonError').innerText = 'Vui lòng nhập lý do xin nghỉ!';
                document.getElementById('leaveReasonError').style.display = 'block';
                isValid = false;
            } else if (reason.length > 500) {
                document.getElementById('leaveReasonError').innerText = 'Lý do không được vượt quá 500 ký tự!';
                document.getElementById('leaveReasonError').style.display = 'block';
                isValid = false;
            }
            return isValid;
        }

        function formatDateVN(dateStr) {
            if (!dateStr) return '';
            const p = dateStr.split('-');
            if (p.length === 3) return p[2] + '/' + p[1] + '/' + p[0];
            return dateStr;
        }

        function toggleCustomCoverSelect(event) {
            event.stopPropagation();
            var panel = document.getElementById('customCoverPanel');
            if (panel) {
                if (panel.style.display === 'block') {
                    panel.style.display = 'none';
                } else {
                    panel.style.display = 'block';
                }
            }
        }

        function selectCustomCoverOption(el, value, label) {
            document.getElementById('targetEmployeeSelect').value = value;
            document.getElementById('customCoverTriggerLabel').innerText = label;
            
            document.querySelectorAll('#customCoverList .single-select-option').forEach(function(o) {
                o.classList.remove('selected');
            });
            el.classList.add('selected');
            
            document.getElementById('customCoverPanel').style.display = 'none';
        }

        function filterCoverStaffOptions(workDate) {
            var visibleCount = 0;
            document.querySelectorAll('#customCoverList .cover-staff-option').forEach(function(option) {
                var visible = option.getAttribute('data-date') === workDate;
                option.style.display = visible ? '' : 'none';
                if (visible) {
                    visibleCount++;
                }
            });

            var emptyOption = document.getElementById('noCoverStaffOption');
            if (emptyOption) {
                emptyOption.style.display = visibleCount === 0 ? '' : 'none';
            }
        }

        document.addEventListener('click', function(event) {
            var panel = document.getElementById('customCoverPanel');
            if (panel && !event.target.closest('.single-select-container')) {
                panel.style.display = 'none';
            }
        });

        // Close modal when clicking outside content
        window.onclick = function(event) {
            const modal = document.getElementById('requestModal');
            if (event.target === modal) {
                closeModal();
            }
        }
    </script>
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

