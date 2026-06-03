<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- KHÔNG cần taglib fn nữa, lấy chữ cái đầu bằng Java scriptlet --%>

<%
    // Lấy chữ cái đầu của tên để hiển thị avatar chữ
    String customerInitial  = "";
    String employeeInitial  = "";
    model.Customer  cust = (model.Customer)  session.getAttribute("customer");
    model.Employee  emp  = (model.Employee)  session.getAttribute("employee");

    if (cust != null && cust.getUserName() != null && !cust.getUserName().isEmpty()) {
        customerInitial = String.valueOf(cust.getUserName().charAt(0)).toUpperCase();
    }
    if (emp != null && emp.getFullName() != null && !emp.getFullName().isEmpty()) {
        employeeInitial = String.valueOf(emp.getFullName().charAt(0)).toUpperCase();
    }
%>

<!-- Google Font + FontAwesome -->
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Playfair+Display:wght@400;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>

<style>
    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
    }

    body {
        padding-top: 78px;
    }

    /* ── HEADER ── */
    .header {
        width: 100%;
        height: 78px;
        background: #76493b;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 50px;
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        z-index: 9999;
    }

    /* ── LOGO ── */
    .logo img {
        height: 58px;
        object-fit: contain;
    }

    /* ── NAV ── */
    .navbar {
        display: flex;
        gap: 50px;
    }
    .navbar a {
        text-decoration: none;
        color: #d7bfa4;
        font-size: 16px;
        font-weight: 600;
        text-transform: uppercase;
        transition: 0.3s;
    }
    .navbar a:hover {
        color: #f0dcc2;
    }
    .navbar a .fa-gauge-high {
        font-size: 13px;
        margin-right: 5px;
    }

    /* ── RIGHT ── */
    .right-header {
        display: flex;
        align-items: center;
        gap: 16px;
    }

    /* ── SEARCH ── */
    .search-box {
        position: relative;
    }
    .search-box input {
        width: 240px;
        height: 40px;
        border: none;
        outline: none;
        border-radius: 6px;
        background: #d7bfa4;
        padding: 0 42px 0 14px;
        font-size: 14px;
    }
    .search-box i {
        position: absolute;
        right: 13px;
        top: 50%;
        transform: translateY(-50%);
        color: #76493b;
        cursor: pointer;
    }

    /* ── CHƯA ĐĂNG NHẬP ── */
    .auth-buttons {
        display: flex;
        gap: 10px;
    }
    .auth-link {
        text-decoration: none;
        padding: 8px 16px;
        border: 1.5px solid #d7bfa4;
        color: #d7bfa4;
        border-radius: 6px;
        font-size: 14px;
        font-weight: 500;
        transition: 0.3s;
    }
    .auth-link:hover           {
        background: #d7bfa4;
        color: #76493b;
    }
    .auth-link.btn-register    {
        background: #d7bfa4;
        color: #76493b;
    }
    .auth-link.btn-register:hover {
        background: #f0dcc2;
    }

    /* ── USER MENU ── */
    .user-menu {
        position: relative;
    }

    .user-trigger {
        display: flex;
        align-items: center;
        gap: 9px;
        cursor: pointer;
        padding: 5px 10px;
        border-radius: 8px;
        transition: 0.3s;
    }
    .user-trigger:hover {
        background: rgba(255,255,255,0.1);
    }

    .user-avatar {
        width: 38px;
        height: 38px;
        border-radius: 50%;
        background: #d7bfa4;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        font-weight: 700;
        color: #76493b;
        overflow: hidden;
    }
    .user-avatar img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .user-info {
        display: flex;
        flex-direction: column;
    }
    .user-name  {
        color: #f0dcc2;
        font-size: 13px;
        font-weight: 600;
        line-height: 1.2;
    }

    .role-badge {
        font-size: 10px;
        font-weight: 600;
        padding: 1px 7px;
        border-radius: 20px;
        margin-top: 2px;
        width: fit-content;
    }
    .badge-customer {
        background: #e8cfae;
        color: #76493b;
    }
    .badge-staff    {
        background: #3498db;
        color: #fff;
    }
    .badge-owner    {
        background: #e74c3c;
        color: #fff;
    }

    .fa-chevron-down {
        color: #c9a98a;
        font-size: 11px;
        transition: 0.3s;
    }
    .user-menu.open .fa-chevron-down {
        transform: rotate(180deg);
    }

    /* ── DROPDOWN ── */
    .dropdown {
        position: absolute;
        top: calc(100% + 8px);
        right: 0;
        min-width: 210px;
        background: #fff;
        border-radius: 10px;
        box-shadow: 0 8px 24px rgba(0,0,0,0.15);
        display: none;
        overflow: hidden;
        z-index: 1000;
    }
    .dropdown.show {
        display: block;
        animation: fadeIn 0.15s ease;
    }

    @keyframes fadeIn {
        from {
            opacity:0;
            transform: translateY(-6px);
        }
        to {
            opacity:1;
            transform: translateY(0);
        }
    }

    .dd-header {
        padding: 14px 18px 11px;
        border-bottom: 1px solid #f0f0f0;
    }
    .dd-header .dd-name  {
        font-weight: 700;
        color: #333;
        font-size: 14px;
    }
    .dd-header .dd-email {
        color: #999;
        font-size: 11px;
        margin-top: 2px;
    }

    .dd-section {
        padding: 5px 0;
    }
    .dd-section + .dd-section {
        border-top: 1px solid #f0f0f0;
    }

    .dropdown a {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 18px;
        text-decoration: none;
        color: #444;
        font-size: 13px;
        transition: 0.15s;
    }
    .dropdown a i {
        width: 15px;
        color: #76493b;
        font-size: 13px;
    }
    .dropdown a:hover  {
        background: #fdf6f0;
        color: #76493b;
    }
    .dropdown a.logout {
        color: #e74c3c;
    }
    .dropdown a.logout i {
        color: #e74c3c;
    }
    .dropdown a.logout:hover {
        background: #fff5f5;
    }

    /* ── NOTIFICATION BELL ── */
    .notif-btn {
        position: relative;
        width: 38px;
        height: 38px;
        border-radius: 50%;
        background: rgba(255,255,255,0.1);
        display: flex;
        align-items: center;
        justify-content: center;
        text-decoration: none;
        transition: 0.3s;
    }
    .notif-btn:hover {
        background: rgba(255,255,255,0.2);
    }
    .notif-btn i {
        color: #d7bfa4;
        font-size: 17px;
    }
    .notif-count {
        position: absolute;
        top: -2px;
        right: -2px;
        background: #e74c3c;
        color: #fff;
        font-size: 9px;
        font-weight: 700;
        width: 17px;
        height: 17px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
    }
</style>

<header class="header">

    <!-- LOGO -->
    <a href="${pageContext.request.contextPath}/" class="logo">
        <img src="${pageContext.request.contextPath}/images/logo.png" alt="Logo">
    </a>

    <!-- NAV -->
    <nav class="navbar">
        <a href="#">Giới thiệu</a>
        <a href="#">Thực đơn</a>

        <%-- Đặt bàn: chỉ khách / chưa đăng nhập --%>
        <c:if test="${sessionScope.employee == null}">
            <a href="#">Đặt bàn</a>
        </c:if>

        <a href="#">Album ảnh</a>
        <a href="#">Liên hệ</a>

        <%-- Dashboard: chỉ Staff và Owner --%>
        <c:if test="${sessionScope.employee != null}">
            <a href="${pageContext.request.contextPath}/staff/dashboard">
                <i class="fa-solid fa-gauge-high"></i>Quản lý
            </a>
        </c:if>
    </nav>

    <!-- RIGHT SIDE -->
    <div class="right-header">

        <%-- Search: ẩn với staff/owner --%>
        <c:if test="${sessionScope.employee == null}">
            <div class="search-box">
                <input type="text" placeholder="Tìm kiếm món ăn...">
                <i class="fa-solid fa-magnifying-glass"></i>
            </div>
        </c:if>

        <!-- ========== CHƯA ĐĂNG NHẬP ========== -->
        <c:if test="${sessionScope.customer == null && sessionScope.employee == null}">
            <div class="auth-buttons">
                <a href="${pageContext.request.contextPath}/login" class="auth-link">
                    <i class="fa-solid fa-right-to-bracket" style="margin-right:5px"></i>Đăng nhập
                </a>
                <a href="${pageContext.request.contextPath}/register" class="auth-link btn-register">
                    <i class="fa-solid fa-user-plus" style="margin-right:5px"></i>Đăng ký
                </a>
            </div>
        </c:if>

        <!-- ========== KHÁCH HÀNG (Customer) ========== -->
        <c:if test="${sessionScope.customer != null}">

            <%-- Chuông thông báo --%>
            <a href="${pageContext.request.contextPath}/customer/notifications" class="notif-btn">
                <i class="fa-solid fa-bell"></i>
                <c:if test="${sessionScope.unreadCount > 0}">
                    <span class="notif-count">${sessionScope.unreadCount}</span>
                </c:if>
            </a>

            <div class="user-menu" id="menuCustomer">
                <div class="user-trigger" onclick="toggleMenu('dropCustomer', 'menuCustomer')">
                    <div class="user-avatar">
                        <c:choose>
                            <c:when test="${not empty sessionScope.customer.image}">
                                <img src="${sessionScope.customer.image}" alt="avatar">
                            </c:when>
                            <c:otherwise><%= customerInitial %></c:otherwise>
                        </c:choose>
                    </div>
                    <div class="user-info">
                        <span class="user-name">${sessionScope.customer.userName}</span>
                        <span class="role-badge badge-customer">Khách hàng</span>
                    </div>
                    <i class="fa-solid fa-chevron-down"></i>
                </div>

                <div class="dropdown" id="dropCustomer">
                    <div class="dd-header">
                        <div class="dd-name">${sessionScope.customer.userName}</div>
                        <div class="dd-email">${sessionScope.customer.email}</div>
                    </div>
                    <div class="dd-section">
                        <a href="${pageContext.request.contextPath}/customer/profile">
                            <i class="fa-solid fa-user"></i>Hồ sơ của tôi
                        </a>
                        <a href="${pageContext.request.contextPath}/customer/reservations">
                            <i class="fa-solid fa-calendar-check"></i>Đơn đặt bàn
                        </a>
                        <a href="${pageContext.request.contextPath}/customer/orders">
                            <i class="fa-solid fa-receipt"></i>Lịch sử đặt món
                        </a>
                    </div>
                    <div class="dd-section">
                        <a href="${pageContext.request.contextPath}/customer/reviews">
                            <i class="fa-solid fa-star"></i>Đánh giá của tôi
                        </a>
                        <a href="${pageContext.request.contextPath}/customer/feedback">
                            <i class="fa-solid fa-comment-dots"></i>Phản hồi
                        </a>
                    </div>
                    <div class="dd-section">
                        <a href="${pageContext.request.contextPath}/logout" class="logout">
                            <i class="fa-solid fa-right-from-bracket"></i>Đăng xuất
                        </a>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- ========== NHÂN VIÊN / OWNER (Employee) ========== -->
        <c:if test="${sessionScope.employee != null}">

            <%--
                Chuông thông báo cho cả Staff lẫn Owner.
                Cả 2 đều là Employee nên dùng chung URL /staff/notifications.
                Owner KHÔNG cần /owner/notifications riêng vì Owner đi qua /staff/* bình thường.
            --%>
            <a href="${pageContext.request.contextPath}/staff/notifications" class="notif-btn">
                <i class="fa-solid fa-bell"></i>
                <c:if test="${sessionScope.unreadCount > 0}">
                    <span class="notif-count">${sessionScope.unreadCount}</span>
                </c:if>
            </a>

            <div class="user-menu" id="menuEmployee">
                <div class="user-trigger" onclick="toggleMenu('dropEmployee', 'menuEmployee')">
                    <div class="user-avatar">
                        <c:choose>
                            <c:when test="${not empty sessionScope.employee.image}">
                                <img src="${pageContext.request.contextPath}/${sessionScope.employee.image}" alt="avatar">
                            </c:when>
                            <c:otherwise><%= employeeInitial %></c:otherwise>
                        </c:choose>
                    </div>
                    <div class="user-info">
                        <span class="user-name">${sessionScope.employee.fullName}</span>
                        <%-- roleID=1 là Owner, còn lại là Staff. Sửa số này cho đúng với DB. --%>
                        <c:choose>
                            <c:when test="${sessionScope.employee.roleID == 1}">
                                <span class="role-badge badge-owner">Owner</span>
                            </c:when>
                            <c:otherwise>
                                <span class="role-badge badge-staff">Nhân viên</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <i class="fa-solid fa-chevron-down"></i>
                </div>

                <%--
                    Dropdown Staff và Owner giống hệt nhau: chỉ có 3 mục.
                    Các chức năng quản lý (nhân viên, báo cáo...) được đặt trong
                    trang dashboard riêng, truy cập qua link "Quản lý" trên nav bar.
                --%>
                <div class="dropdown" id="dropEmployee">
                    <div class="dd-header">
                        <div class="dd-name">${sessionScope.employee.fullName}</div>
                        <div class="dd-email">${sessionScope.employee.email}</div>
                    </div>
                    <div class="dd-section">
                        <a href="${pageContext.request.contextPath}/staff/profile">
                            <i class="fa-solid fa-user"></i>Hồ sơ của tôi
                        </a>
                    </div>
                    <div class="dd-section">
                        <a href="${pageContext.request.contextPath}/staff/change-password">
                            <i class="fa-solid fa-lock"></i>Đổi mật khẩu
                        </a>
                        <a href="${pageContext.request.contextPath}/logout" class="logout">
                            <i class="fa-solid fa-right-from-bracket"></i>Đăng xuất
                        </a>
                    </div>
                </div>
            </div>
        </c:if>

    </div>
</header>

<script>
    function toggleMenu(dropId, menuId) {
        document.getElementById(dropId).classList.toggle('show');
        document.getElementById(menuId).classList.toggle('open');
    }
    window.addEventListener('click', function (e) {
        if (!e.target.closest('.user-menu')) {
            document.querySelectorAll('.dropdown').forEach(d => d.classList.remove('show'));
            document.querySelectorAll('.user-menu').forEach(m => m.classList.remove('open'));
        }
    });
</script>
