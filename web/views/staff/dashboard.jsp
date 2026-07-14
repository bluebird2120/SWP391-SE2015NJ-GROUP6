<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${sessionScope.employee.roleID == 3 ? 'Receptionist Dashboard' : 'Staff Dashboard'}</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin:0; font-family:'Inter',sans-serif; background:#faf6f2; }
        .main { flex:1; padding:24px 32px; }
        .page-title { font-family:'Playfair Display',serif; color:#76493b; font-size:1.6rem; margin:0 0 4px; }
        .page-sub { color:#a0714f; font-size:.95rem; margin-bottom:22px; }
        .greeting { color:#5d3a2e; font-size:1.05rem; margin-bottom:18px; }
        .grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(260px,1fr)); gap:16px; }
        .card-link { display:flex; flex-direction:column; gap:10px; background:#fff; border:1px solid #ede0d8; border-radius:12px; padding:22px; text-decoration:none; color:#4a3528; transition:.2s; }
        .card-link:hover { transform:translateY(-2px); box-shadow:0 8px 20px rgba(118,73,59,.08); border-color:#d7bfa4; }
        .card-icon { width:46px; height:46px; border-radius:10px; background:#f5ece4; display:flex; align-items:center; justify-content:center; color:#76493b; font-size:1.2rem; }
        .card-title { font-family:'Playfair Display',serif; color:#76493b; font-size:1.15rem; font-weight:700; }
        .card-desc { color:#8a6e5a; font-size:.88rem; line-height:1.45; }
        .card-badge { display:inline-block; background:#dc3545; color:#fff; font-size:.72rem; font-weight:700; padding:2px 8px; border-radius:10px; margin-left:6px; }
    </style>
</head>
<body>
    <%@ include file="/views/includes/header.jsp" %>
    <div style="display:flex;">
        <%@ include file="/views/includes/dashboard.jsp" %>
        <main class="main">

            <h1 class="page-title">${sessionScope.employee.roleID == 3 ? 'Receptionist Dashboard' : 'Staff Dashboard'}</h1>
            <p class="page-sub">Khu vực làm việc của bạn</p>
            <p class="greeting">Xin chào, <b>${sessionScope.employee.fullName}</b>!</p>

            <div class="grid">
                <c:choose>
                    <c:when test="${sessionScope.employee.roleID == 3}">


                        <a class="card-link" href="${pageContext.request.contextPath}/reception/tables">
                            <div class="card-icon"><i class="fas fa-chair"></i></div>
                            <div class="card-title">Tiếp nhận và gán bàn</div>
                            <div class="card-desc">Xem trạng thái bàn, tiếp nhận khách đặt trước và gán bàn phù hợp.</div>
                        </a>
                    </c:when>
                    <c:otherwise>


                        <a class="card-link" href="${pageContext.request.contextPath}/staff/tables">
                            <div class="card-icon"><i class="fas fa-chair"></i></div>
                            <div class="card-title">Bàn phục vụ</div>
                            <div class="card-desc">Theo dõi bàn được giao và xác nhận dọn bàn sau khi khách thanh toán.</div>
                        </a>
                    </c:otherwise>
                </c:choose>


                <a class="card-link" href="${pageContext.request.contextPath}/staff/my-schedule">
                    <div class="card-icon"><i class="fas fa-calendar-week"></i></div>
                    <div class="card-title">Lịch làm việc</div>
                    <div class="card-desc">Xem lịch ca làm việc theo tháng và trạng thái điểm danh.</div>
                </a>

                <a class="card-link" href="${pageContext.request.contextPath}/staff/notifications">
                    <div class="card-icon"><i class="fas fa-bell"></i></div>
                    <div class="card-title">Thông báo
                        <c:if test="${sessionScope.unreadCount > 0}">
                            <span class="card-badge">${sessionScope.unreadCount}</span>
                        </c:if>
                    </div>
                    <div class="card-desc">Thông báo lịch ca và các cập nhật từ quản lý.</div>
                </a>
            </div>
        </main>
    </div>
    <%@ include file="/views/includes/footer.jsp" %>
</body>
</html>
