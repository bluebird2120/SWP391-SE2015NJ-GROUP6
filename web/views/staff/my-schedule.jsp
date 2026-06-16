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
        .stats { color:#8a6e5a; font-size:0.85rem; }
        .calendar { display:grid; grid-template-columns:repeat(7,1fr); gap:6px; }
        .dow-head { text-align:center; font-size:0.75rem; font-weight:700; color:#a0714f; text-transform:uppercase; letter-spacing:0.05em; padding:8px 0; }
        .dow-head.sun { color:#c14b4b; }
        .day-cell { background:#fff; border:1px solid #ede0d8; border-radius:8px; min-height:96px; padding:6px 8px; display:flex; flex-direction:column; gap:4px; }
        .day-cell.empty { background:#faf6f2; border-style:dashed; border-color:#ede0d8; }
        .day-cell.today { border-color:#76493b; box-shadow:0 0 0 2px rgba(118,73,59,0.15); }
        .day-num { font-size:0.82rem; font-weight:700; color:#5d3a2e; }
        .day-num.sun { color:#c14b4b; }
        .day-cell.today .day-num { color:#76493b; }
        .shift-pill { font-size:0.72rem; line-height:1.25; padding:4px 6px; border-radius:6px; background:#f5ece4; border:1px solid #ede0d8; display:flex; flex-direction:column; gap:2px; }
        .shift-name { font-weight:600; color:#5d3a2e; }
        .shift-time { color:#8a6e5a; font-size:0.68rem; }
        .badge { display:inline-block; padding:1px 6px; border-radius:10px; font-size:0.62rem; font-weight:700; }
        .badge-scheduled { background:#e2e3e5; color:#41464b; }
        .badge-present   { background:#d4edda; color:#155724; }
        .badge-late      { background:#fff3cd; color:#856404; }
        .badge-absent    { background:#f8d7da; color:#721c24; }
        .empty-state { text-align:center; color:#8a6e5a; padding:24px 0; font-size:0.92rem; }
    </style>
</head>
<body>
    <%@ include file="/views/includes/header.jsp" %>
    <div style="display:flex;">
        <%@ include file="/views/includes/dashboard.jsp" %>
        <main class="main">
            <h1 class="page-title">My Schedule</h1>
            <p class="page-sub">Lịch ca làm việc theo tháng — cập nhật trạng thái điểm danh</p>

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
                <form method="get" action="${pageContext.request.contextPath}/staff/my-schedule" class="form-inline">
                    <label style="font-size:0.78rem; color:#8a6e5a; font-weight:600;">Đi tới:</label>
                    <select name="month">
                        <c:forEach var="m" begin="1" end="12">
                            <option value="${m}" ${m == month ? 'selected' : ''}>Tháng ${m}</option>
                        </c:forEach>
                    </select>
                    <input type="number" name="year" value="${year}" min="2024" max="2100">
                    <button type="submit" class="btn btn-primary"><i class="fas fa-search"></i> Xem</button>
                    <span class="stats" style="margin-left:auto;">
                        <i class="fas fa-calendar-check"></i> ${totalShifts} ca trong tháng
                    </span>
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
        </main>
    </div>
    <%@ include file="/views/includes/footer.jsp" %>
</body>
</html>
