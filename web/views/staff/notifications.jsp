<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Notifications</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin:0; font-family:'Inter',sans-serif; background:#faf6f2; }
        /*.main { flex:1; padding:24px 32px; min-width:0; }*/
        .page-title { font-family:'Playfair Display',serif; color:#76493b; font-size:1.6rem; margin:0 0 4px; }
        .page-sub { color:#a0714f; font-size:0.9rem; margin-bottom:18px; }
        .summary { background:#fff; border:1px solid #ede0d8; border-radius:12px; padding:14px 18px; margin-bottom:14px; display:flex; gap:12px; align-items:center; }
        .summary i { color:#76493b; }
        .summary .count { font-weight:700; color:#5d3a2e; }
        .list { display:flex; flex-direction:column; gap:10px; }
        .item { background:#fff; border:1px solid #ede0d8; border-radius:12px; padding:14px 16px; display:flex; gap:14px; align-items:flex-start; }
        .item.unread { border-left:4px solid #76493b; background:#fffaf4; }
        .item-icon { width:38px; height:38px; border-radius:9px; background:#f5ece4; display:flex; align-items:center; justify-content:center; color:#76493b; flex-shrink:0; }
        .item-body { flex:1; }
        .item-meta { display:flex; gap:10px; font-size:0.72rem; color:#a0714f; text-transform:uppercase; letter-spacing:0.04em; margin-bottom:4px; }
        .item-type { font-weight:700; color:#76493b; }
        .item-msg { color:#4a3528; font-size:0.92rem; line-height:1.5; }
        .item-time { color:#8a6e5a; font-size:0.78rem; margin-top:4px; }
        .btn { padding:6px 12px; border-radius:7px; border:none; cursor:pointer; font-size:0.78rem; font-weight:600; text-decoration:none; display:inline-flex; gap:5px; align-items:center; }
        .btn-mark { background:#76493b; color:#fff; }
        .btn-mark:hover { background:#5d3a2e; }
        .read-tag { color:#8a6e5a; font-size:0.78rem; font-style:italic; display:inline-flex; gap:4px; align-items:center; }
        .empty-state { text-align:center; color:#8a6e5a; padding:32px; background:#fff; border:1px dashed #ede0d8; border-radius:12px; }
    </style>
</head>
<body>
    <%@ include file="/views/includes/header.jsp" %>
    <!--<div style="display:flex;">-->
        <%@ include file="/views/includes/dashboard.jsp" %>
        <!--<main class="main">-->
    <div style="display:flex;">
        <%@ include file="/views/includes/dashboard.jsp" %>
        <main class="main">
            <h1 class="page-title">Notifications</h1>
            <p class="page-sub">Thông báo lịch ca và cập nhật từ quản lý</p>

            <div class="summary">
                <i class="fas fa-bell fa-lg"></i>
                <span>
                    <span class="count">${unreadCount}</span> chưa đọc
                    / tổng <span class="count">${fn:length(notifications)}</span> thông báo gần nhất
                </span>
            </div>

            <c:choose>
                <c:when test="${empty notifications}">
                    <div class="empty-state">
                        <i class="fas fa-inbox fa-2x" style="color:#d7bfa4;"></i>
                        <p style="margin-top:10px;">Chưa có thông báo nào.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="list">
                        <c:forEach var="n" items="${notifications}">
                            <div class="item ${n.isRead == 0 ? 'unread' : ''}">
                                <div class="item-icon"><i class="fas fa-calendar-alt"></i></div>
                                <div class="item-body">
                                    <div class="item-meta">
                                        <span class="item-type">${n.type}</span>
                                        <c:if test="${n.isRead == 0}">
                                            <span style="color:#dc3545; font-weight:700;">• Mới</span>
                                        </c:if>
                                    </div>
                                    <div class="item-msg">${n.message}</div>
                                    <div class="item-time">
                                        <i class="far fa-clock"></i>
                                        <fmt:formatDate value="${n.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                    </div>
                                </div>
                                <div>
                                    <c:choose>
                                        <c:when test="${n.isRead == 0}">
                                            <form method="post" action="${pageContext.request.contextPath}/staff/notifications">
                                                <input type="hidden" name="action" value="markRead">
                                                <input type="hidden" name="notificationID" value="${n.notificationID}">
                                                <button type="submit" class="btn btn-mark">
                                                    <i class="fas fa-check"></i> Đánh dấu đã đọc
                                                </button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="read-tag"><i class="fas fa-check-double"></i> Đã đọc</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
<!--        </main>
    </div>-->
    <%@ include file="/views/includes/footer.jsp" %>
</body>
</html>
