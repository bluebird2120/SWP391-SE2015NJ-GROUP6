<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    String customerInitial = "";
    String employeeInitial = "";
    model.Customer cust = (model.Customer) session.getAttribute("customer");
    model.Employee emp  = (model.Employee) session.getAttribute("employee");
    
    // Lấy chữ cái đầu làm avatar mặc định nếu không có ảnh
    if (cust != null && cust.getUserName() != null && !cust.getUserName().isEmpty())
        customerInitial = String.valueOf(cust.getUserName().charAt(0)).toUpperCase();
    if (emp != null && emp.getFullName() != null && !emp.getFullName().isEmpty())
        employeeInitial = String.valueOf(emp.getFullName().charAt(0)).toUpperCase();

    int custUnreadCount = 0;
    if (cust != null) {
        custUnreadCount = new dal.NotificationDAO().countUnread(cust.getCustomerID(), "customer");
    }
    int empUnreadCount = 0;
    if (emp != null) {
        empUnreadCount = new dal.NotificationDAO().countUnread(emp.getEmployeeID(), "staff");
        session.setAttribute("unreadCount", empUnreadCount);
    }
%>


<style>
    *, *::before, *::after {
        box-sizing: border-box;
        margin: 0;
        padding: 0;
    }

    body {
        font-family: 'Inter', sans-serif;
        /* padding-top đúng bằng chiều cao header fixed */
        padding-top: 78px;
    }

    /* ── HEADER fixed ── */
    .header {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        width: 100%;
        height: 78px;
        background: #76493b;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 24px;
        z-index: 9999;
    }

    /* ── LOGO ── */
    .logo img {
        height: 58px;
        object-fit: contain;
    }

    /* ── NAV ── */
    .navbar {
        flex: 1;
        display: flex;
        justify-content: center;
        gap: 30px;
    }
    .navbar a {
        text-decoration: none;
        color: #d7bfa4;
        font-size: 15px;
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

    /* ── AUTH BUTTONS ── */
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
    .auth-link:hover {
        background: #d7bfa4;
        color: #76493b;
    }
    .auth-link.btn-register {
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
    .user-name {
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
        z-index: 10000;
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
        to   {
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
    .dropdown a:hover {
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

    /* ── BELL ── */
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

    /* Wrapper nằm ngang: sidebar | content */
    .admin-body-wrapper {
        display: flex;
        min-height: calc(100vh - 78px);
        background: #f8f4f2;
    }

    /* Sidebar cố định bên trái */
    .admin-sidebar {
        width: 220px;
        flex-shrink: 0;
        background: #fff;
        border-right: 1px solid #ede0d8;
        overflow-y: auto;
    }

    /* Vùng nội dung bên phải sidebar */
    .admin-main-content {
        flex: 1;
        padding: 2rem;
        min-width: 0;
        overflow-x: auto;
    }

    .public-content {
        flex: 1;
    }
</style>

<header class="header">
    <a href="${pageContext.request.contextPath}/" class="logo">
        <img src="${pageContext.request.contextPath}/images/logo.png" alt="Logo">
    </a>

    <nav class="navbar">
        <a href="${pageContext.request.contextPath}/page/about">Giới thiệu</a>
        <a href="${pageContext.request.contextPath}/page/menu">Thực đơn</a>
        <c:if test="${sessionScope.employee == null}">
            <a href="${pageContext.request.contextPath}/reservation">Đặt bàn</a>
        </c:if>
        <a href="${pageContext.request.contextPath}/reviews">Đánh giá</a>
        <c:if test="${sessionScope.employee != null}">
            <a href="${pageContext.request.contextPath}${sessionScope.employee.roleID == 1 ? '/owner/dashboard' : '/staff/dashboard'}">
                <i class="fa-solid fa-gauge-high"></i>Quản lý
            </a>
        </c:if>
    </nav>

    <div class="right-header">
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

        <!-- KHU VỰC KHÁCH HÀNG (CUSTOMER) ĐÃ ĐĂNG NHẬP -->
        <c:if test="${sessionScope.customer != null}">
            <a href="${pageContext.request.contextPath}/customer/notifications" class="notif-btn">
                <i class="fa-solid fa-bell"></i>
                <% if (custUnreadCount > 0) { %>
                <span class="notif-count"><%= custUnreadCount %></span>
                <% } %>
            </a>
            <div class="user-menu" id="menuCustomer">
                <div class="user-trigger" onclick="toggleMenu('dropCustomer', 'menuCustomer')">
                    <div class="user-avatar">
                        <!-- ĐOẠN SỬA ĐỔI: Check hiển thị ảnh đại diện cho Customer -->
                        <c:choose>
                            <c:when test="${not empty sessionScope.customer.image}">
                                <img src="${pageContext.request.contextPath}/${sessionScope.customer.image}" alt="avatar">
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
                        <a href="${pageContext.request.contextPath}/profile"><i class="fa-solid fa-user"></i>Hồ sơ của tôi</a>
                        <a href="${pageContext.request.contextPath}/reservation?action=history"><i class="fa-solid fa-calendar-check"></i>Đơn đặt bàn</a>
                    </div>
                    <div class="dd-section">
                        <a href="${pageContext.request.contextPath}/customer/reviews"><i class="fa-solid fa-star"></i>Đánh giá của tôi</a>
                    </div>
                    <div class="dd-section">
                        <a href="${pageContext.request.contextPath}/logout" class="logout"><i class="fa-solid fa-right-from-bracket"></i>Đăng xuất</a>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- KHU VỰC NHÂN VIÊN (EMPLOYEE) ĐÃ ĐĂNG NHẬP -->
        <c:if test="${sessionScope.employee != null}">
            <a href="${pageContext.request.contextPath}/${sessionScope.employee.roleID == 1 ? 'owner' : 'staff'}/notifications" class="notif-btn">
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
                        <c:choose>
                            <c:when test="${sessionScope.employee.roleID == 1}">
                                <span class="role-badge badge-owner">Chủ nhà hàng</span>
                            </c:when>
                            <c:when test="${sessionScope.employee.roleID == 3}">
                                <span class="role-badge badge-staff">Lễ tân</span>
                            </c:when>
                            <c:otherwise>
                                <span class="role-badge badge-staff">Nhân viên</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <i class="fa-solid fa-chevron-down"></i>
                </div>
                <div class="dropdown" id="dropEmployee">
                    <div class="dd-header">
                        <div class="dd-name">${sessionScope.employee.fullName}</div>
                        <div class="dd-email">${sessionScope.employee.email}</div>
                    </div>
                    <div class="dd-section">
                        <a href="${pageContext.request.contextPath}/profile"><i class="fa-solid fa-user"></i>Hồ sơ của tôi</a>
                    </div>
                    <div class="dd-section">
                        <c:if test="${sessionScope.employee.roleID == 1}">
                            <a href="${pageContext.request.contextPath}/owner/reviews"><i class="fa-solid fa-comment-dots"></i>Phản hồi</a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/logout" class="logout"><i class="fa-solid fa-right-from-bracket"></i>Đăng xuất</a>
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

    setInterval(function () {
        fetch('${pageContext.request.contextPath}/api/unread-count')
                .then(res => res.json())
                .then(data => {
                    const badge = document.querySelector('.notif-count');
                    if (data.unread > 0) {
                        if (badge) {
                            badge.textContent = data.unread;
                            badge.style.display = 'flex';
                        } else {
                            const btn = document.querySelector('.notif-btn');
                            if (btn) {
                                const span = document.createElement('span');
                                span.className = 'notif-count';
                                span.textContent = data.unread;
                                btn.appendChild(span);
                            }
                        }
                    } else {
                        if (badge)
                            badge.style.display = 'none';
                    }
                })
                .catch(() => {
                });
    }, 30000);
</script>