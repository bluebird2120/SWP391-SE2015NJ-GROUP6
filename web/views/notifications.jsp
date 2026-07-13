<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Thông báo – Vị An Restaurant</title>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
        <style>
            * {
                box-sizing: border-box;
            }
            body {
                margin: 0;
                font-family: 'Inter', sans-serif;
                background: #faf6f2;
                color: #4a3528;
            }
            .notif-page {
                max-width: 1080px;
                margin: 0 auto;
                padding: 24px 20px 60px;
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

            .summary {
                background: #fff;
                border: 1px solid #ede0d8;
                border-radius: 12px;
                padding: 14px 18px;
                margin-bottom: 14px;
                display: flex;
                justify-content: space-between;
                align-items: center;
                gap: 12px;
            }
            .summary-info {
                display: flex;
                align-items: center;
                gap: 12px;
            }
            .summary i {
                color: #76493b;
            }
            .summary .count {
                font-weight: 700;
                color: #5d3a2e;
            }

            .list {
                display: flex;
                flex-direction: column;
                gap: 10px;
            }
            .item {
                background: #fff;
                border: 1px solid #ede0d8;
                border-radius: 12px;
                padding: 14px 16px;
                display: flex;
                gap: 14px;
                align-items: flex-start;
            }
            .item.unread {
                border-left: 4px solid #76493b;
                background: #fffaf4;
            }

            .item-icon {
                width: 38px;
                height: 38px;
                border-radius: 9px;
                background: #f5ece4;
                display: flex;
                align-items: center;
                justify-content: center;
                color: #76493b;
                flex-shrink: 0;
            }
            .item-body {
                flex: 1;
            }
            .item-meta {
                display: flex;
                gap: 10px;
                font-size: 0.72rem;
                color: #a0714f;
                text-transform: uppercase;
                letter-spacing: 0.04em;
                margin-bottom: 4px;
            }
            .item-type {
                font-weight: 700;
                color: #76493b;
            }
            .item-msg-link {
                text-decoration: none;
                display: block;
            }
            .item-msg-link:hover .item-msg {
                color: #76493b;
            }
            .item-msg {
                color: #4a3528;
                font-size: 0.92rem;
                line-height: 1.5;
                margin: 0;
                transition: color 0.15s;
            }
            .item-time {
                color: #8a6e5a;
                font-size: 0.78rem;
                margin-top: 4px;
            }

            .btn {
                padding: 6px 12px;
                border-radius: 7px;
                border: none;
                cursor: pointer;
                font-size: 0.78rem;
                font-weight: 600;
                text-decoration: none;
                display: inline-flex;
                gap: 5px;
                align-items: center;
                transition: background 0.15s;
            }
            .btn-mark {
                background: #76493b;
                color: #fff;
            }
            .btn-mark:hover {
                background: #5d3a2e;
            }
            .btn-muted {
                background: #f4ebe4;
                color: #76493b;
            }
            .btn-muted:hover {
                background: #e8dbd0;
            }

            .read-tag {
                color: #8a6e5a;
                font-size: 0.78rem;
                font-style: italic;
                display: inline-flex;
                gap: 4px;
                align-items: center;
            }
            .empty-state {
                text-align: center;
                color: #8a6e5a;
                padding: 32px;
                background: #fff;
                border: 1px dashed #ede0d8;
                border-radius: 12px;
            }
        </style>
    </head>
    <body>
        <c:set var="isCustomer" value="${not empty sessionScope.customer}" />
        <c:set var="isOwner" value="${not empty sessionScope.employee && sessionScope.employee.roleID == 1}" />
        <c:set var="isStaff" value="${not empty sessionScope.employee && sessionScope.employee.roleID != 1}" />
        <c:set var="isStaffOrOwner" value="${isOwner || isStaff}" />

        <c:choose>
            <c:when test="${isCustomer}">
                <c:set var="pageSub" value="Cập nhật mới nhất về các đơn đặt bàn và đánh giá của bạn" />
                <c:set var="postAction" value="${pageContext.request.contextPath}/customer/notifications" />
            </c:when>
            <c:when test="${isOwner}">
                <c:set var="pageSub" value="Thông báo từ hệ thống, đánh giá khách hàng và yêu cầu của nhân viên" />
                <c:set var="postAction" value="${pageContext.request.contextPath}/owner/notifications" />
            </c:when>
            <c:otherwise>
                <c:set var="pageSub" value="Thông báo lịch ca và cập nhật từ quản lý" />
                <c:set var="postAction" value="${pageContext.request.contextPath}/staff/notifications" />
            </c:otherwise>
        </c:choose>

        <%@ include file="/views/includes/header.jsp" %>

        <c:if test="${isStaffOrOwner}">
            <div style="display:flex;">
                <%@ include file="/views/includes/dashboard.jsp" %>
            </c:if>

            <main class="${isStaffOrOwner ? 'main' : 'notif-page'}">
                <h1 class="page-title">Notifications</h1>
                <p class="page-sub">${pageSub}</p>

                <c:choose>
                    <c:when test="${empty notifications}">
                        <div class="empty-state">
                            <i class="fas fa-inbox fa-2x" style="color:#d7bfa4;"></i>
                            <p style="margin-top:10px;">Chưa có thông báo nào.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="summary">
                            <div class="summary-info">
                                <i class="fas fa-bell fa-lg"></i>
                                <span>
                                    <span class="count">${unreadCount}</span> chưa đọc / tổng <span class="count">${fn:length(notifications)}</span> thông báo gần nhất
                                </span>
                            </div>
                            <c:if test="${isCustomer && unreadCount > 0}">
                                <form method="post" action="${postAction}" style="margin: 0;">
                                    <input type="hidden" name="action" value="markAllRead">
                                    <button type="submit" class="btn btn-muted">
                                        <i class="fas fa-check-double"></i> Đánh dấu tất cả đã đọc
                                    </button>
                                </form>
                            </c:if>
                        </div>

                        <div class="list">
                            <c:forEach var="n" items="${notifications}" varStatus="status">
                                <div class="item ${n.isRead == 0 ? 'unread' : ''} ${status.index >= 5 ? 'previous-notif' : ''}"
                                     ${status.index >= 5 ? 'style="display: none;"' : ''}>

                                    <div class="item-icon">
                                        <c:choose>
                                            <c:when test="${n.type == 'reservation_needs_table'}">
                                                <i class="fas fa-clipboard-list" style="color: #e67e22;"></i> </c:when>
                                            <c:when test="${n.type == 'table_assigned' || n.type == 'table_assigned_offline'}">
                                                <i class="fas fa-utensils" style="color: #2ec4b6;"></i> </c:when>
                                            <c:when test="${n.type == 'reservation_confirmed'}">
                                                <i class="fas fa-calendar-alt"></i>
                                            </c:when>
                                        </c:choose>
                                    </div>

                                    <div class="item-body">
                                        <div class="item-meta">
                                            <span class="item-type">${n.type}</span>
                                            <c:if test="${n.isRead == 0}">
                                                <span style="color: #dc3545; font-weight: 700;">• Mới</span>
                                            </c:if>
                                        </div>
                                        <%-- Tất cả role đều bấm vào message để readAndRedirect đúng trang --%>
                                        <form method="post" action="${postAction}" style="margin: 0;">
                                            <input type="hidden" name="action" value="readAndRedirect">
                                            <input type="hidden" name="notificationID" value="${n.notificationID}">
                                            <button type="submit" class="item-msg-link" style="background:none;border:none;padding:0;cursor:pointer;text-align:left;width:100%;">
                                                <p class="item-msg"><c:out value="${n.message}" /></p>
                                            </button>
                                        </form>
                                        <div class="item-time">
                                            <i class="far fa-clock"></i>
                                            <fmt:formatDate value="${n.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                        </div>
                                    </div>

                                    <div>
                                        <c:choose>
                                            <c:when test="${n.isRead == 0}">
                                                <form method="post" action="${postAction}" style="margin: 0;">
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

                        <c:if test="${fn:length(notifications) > 5}">
                            <div id="showMoreContainer" style="text-align: center; margin-top: 18px;">
                                <button type="button" id="btnShowMore" onclick="showAllNotifications()" style="background: none; border: none; color: #76493b; font-weight: 600; cursor: pointer; font-size: 0.9rem; display: inline-flex; align-items: center; gap: 6px; padding: 8px 16px; transition: all 0.2s; text-decoration: underline;">
                                    Xem các thông báo trước đó <i class="fas fa-chevron-down" style="font-size: 0.8rem;"></i>
                                </button>
                            </div>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </main>

            <c:if test="${isStaffOrOwner}">
            </div>
        </c:if>

        <%@ include file="/views/includes/footer.jsp" %>

        <script>
            function showAllNotifications() {
                var prevNotifs = document.querySelectorAll('.previous-notif');
                prevNotifs.forEach(function (el) {
                    el.style.display = 'flex';
                    el.style.opacity = '0';
                    el.style.transition = 'opacity 0.3s ease';
                    setTimeout(function () {
                        el.style.opacity = '1';
                    }, 10);
                });
                var container = document.getElementById('showMoreContainer');
                if (container) {
                    container.style.display = 'none';
                }
            }
        </script>
    </body>
</html>
