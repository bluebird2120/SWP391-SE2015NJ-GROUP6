<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<style>
    .sidebar {
        min-height: calc(100vh - 78px);
        background: #ffffff;
        border-right: 1px solid #ede0d8;
        padding: 1.25rem 0;
        width: 220px;
        flex-shrink: 0;
    }
    .sidebar-header {
        padding: 0 1.25rem 1rem;
        border-bottom: 1px solid #ede0d8;
    }
    .sidebar-header h5 {
        color: #76493b;
        font-weight: 700;
        font-size: 0.95rem;
        margin: 0;
        display: flex;
        align-items: center;
        gap: 0.5rem;
    }
    .sidebar-header h5 i {
        color: #76493b;
    }
    .sidebar .nav-link {
        color: #6b4c3b;
        padding: 0.55rem 1rem;
        margin: 0.1rem 0.6rem;
        border-radius: 8px;
        font-weight: 500;
        font-size: 0.85rem;
        transition: all 0.2s ease;
        display: flex;
        align-items: center;
        gap: 0.6rem;
        text-decoration: none;
    }
    .sidebar .nav-link i {
        font-size: 0.88rem;
        width: 16px;
        text-align: center;
        color: #a0714f;
    }
    .sidebar .nav-link:hover {
        background: rgba(118, 73, 59, 0.08);
        color: #76493b;
        transform: translateX(2px);
    }
    .sidebar .nav-link:hover i {
        color: #76493b;
    }
    .sidebar .nav-link.active {
        background: rgba(118, 73, 59, 0.12);
        color: #76493b;
        font-weight: 600;
        border-left: 3px solid #76493b;
        padding-left: calc(1rem - 3px);
    }
    .sidebar .nav-link.active i {
        color: #76493b;
    }
    .nav-section-title {
        padding: 0.75rem 1.25rem 0.2rem;
        font-size: 0.68rem;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: #b09080;
        display: flex;
        align-items: center;
        gap: 0.35rem;
    }
    .nav-section-title i {
        font-size: 0.68rem;
    }
    .sidebar ul.nav {
        list-style: none;
        padding: 0;
        margin: 0;
    }
    .sidebar li.nav-item {
        list-style: none;
        padding: 0;
        margin: 0;
    }
</style>

<%-- Phân quyền cứng theo Role: Owner (roleID=1) / Staff (roleID=2) --%>
<c:set var="isOwner" value="${sessionScope.employee.roleID == 1}" />
<c:set var="isStaff" value="${sessionScope.employee.roleID == 2}" />
<%-- [PHAN QUYEN LE TAN] Role 3 co menu tiep nhan rieng. --%>
<c:set var="isReceptionist" value="${sessionScope.employee.roleID == 3}" />

<nav class="sidebar">
    <div class="sidebar-header">
        <h5><i class="fas fa-store"></i> Quản lý nhà hàng</h5>
    </div>
    <ul class="nav flex-column mt-1">

        <%-- ===== VẬN HÀNH ===== --%>
        <li class="nav-item">
            <div class="nav-section-title"><i class="fas fa-cog"></i> Vận hành</div>
        </li>

        <%-- Staff: trang tổng quan + đơn hàng --%>
        <c:if test="${isStaff}">
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('staff/dashboard') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/staff/dashboard">
                    <i class="fas fa-house"></i> Trang tổng quan
                </a>
            </li>
           
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('staff/tables') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/staff/tables">
                    <i class="fas fa-chair"></i> Bàn phục vụ 
                </a>
            </li>
        </c:if>

        <%-- Owner: lịch sử đơn + bàn nhà hàng --%>
        <c:if test="${isReceptionist}">
            <%-- [PHAN QUYEN LE TAN] Dashboard giong nhan vien nhung menu gon hon. --%>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('staff/dashboard') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/staff/dashboard">
                    <i class="fas fa-house"></i> Trang tổng quan
                </a>
            </li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('reception/tables') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/reception/tables">
                    <i class="fas fa-chair"></i> Tiếp nhận và gán bàn
                </a>
            </li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('my-schedule') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/staff/my-schedule">
                    <i class="fas fa-calendar-week"></i> Lịch làm việc
                </a>
            </li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('staff/notifications') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/staff/notifications">
                    <i class="fas fa-bell"></i> Thông báo
                    <c:if test="${sessionScope.unreadCount > 0}">
                        <span style="background:#dc3545;color:#fff;font-size:.65rem;padding:1px 7px;border-radius:10px;margin-left:auto;">${sessionScope.unreadCount}</span>
                    </c:if>
                </a>
            </li>
        </c:if>

        <c:if test="${isOwner}">
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('reception/tables') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/reception/tables">
                    <i class="fas fa-concierge-bell"></i> Tiếp nhận và gán bàn
                </a>
            </li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('owner/order-history') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/owner/order-history">
                    <i class="fas fa-history"></i> Lịch sử đơn hàng
                </a>
            </li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('manage-table') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/manage-table">
                    <i class="fas fa-chair"></i> Bàn nhà hàng
                </a>
            </li>
        </c:if>

        <c:if test="${!isReceptionist}">
        <li>
            <a class="nav-link ${pageContext.request.requestURI.contains('business-hours') ? 'active' : ''}"
               href="${pageContext.request.contextPath}/business-hours?action=list">
                <i class="fas fa-clock"></i> Giờ hoạt động
            </a>
        </li>
        </c:if>

        <%-- [PHAN QUYEN PHUC VU] Giu nguyen cac menu rieng cua Staff. --%>
        <c:if test="${isStaff}">
            <li>
                <div class="nav-section-title"><i class="fas fa-user-clock"></i> Không gian làm việc</div>
            </li>
            <li class="nav-item">
                <a class="nav-link ${pageContext.request.requestURI.contains('my-schedule') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/staff/my-schedule">
                    <i class="fas fa-calendar-week"></i> Lịch làm việc
                </a>
            </li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('staff/notifications') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/staff/notifications">
                    <i class="fas fa-bell"></i> Thông báo
                    <c:if test="${sessionScope.unreadCount > 0}">
                        <span style="background:#dc3545; color:#fff; font-size:0.65rem; font-weight:700; padding:1px 7px; border-radius:10px; margin-left:auto;">${sessionScope.unreadCount}</span>
                    </c:if>
                </a>
            </li>

            <li><div class="nav-section-title"><i class="fas fa-utensils"></i> Thực đơn</div></li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('items') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/menu">
                    <i class="fas fa-hamburger"></i> Món ăn
                </a>
            </li>
        </c:if>

        <%-- ===== NHÂN SỰ (chỉ Owner) ===== --%>
        <c:if test="${isOwner}">
            <li>
                <div class="nav-section-title"><i class="fas fa-users"></i> Nhân sự</div>
            </li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('shift-template') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/owner/shift-templates">
                    <i class="fas fa-calendar-alt"></i> Mẫu ca làm việc
                </a>
            </li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('owner/shift-roster') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/owner/shift-roster">
                    <i class="fas fa-user-clock"></i> Phân công ca
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${pageContext.request.requestURI.contains('owner/staff') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/owner/staff?action=list">
                    <i class="fas fa-user-tie"></i> Quản lý nhân viên
                </a>
            </li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('owner/attendance') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/owner/attendance">
                    <i class="fas fa-clipboard-check"></i> Chấm công
                </a>
            </li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('owner/customer-list') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/owner/customer-list">
                    <i class="fas fa-users"></i> Danh sách khách hàng
                </a>
            </li>
        </c:if>

        <%-- ===== TÀI CHÍNH (chỉ Owner) ===== --%>
        <c:if test="${isOwner}">
            <li><div class="nav-section-title"><i class="fas fa-money-bill-wave"></i> Tài chính</div></li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('invoice') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/owner/invoices">
                    <i class="fas fa-file-invoice"></i> Hóa đơn
                </a>
            </li>
        </c:if>

        <%-- ===== THỰC ĐƠN (chỉ Owner) ===== --%>
        <c:if test="${isOwner}">
            <li><div class="nav-section-title"><i class="fas fa-utensils"></i> Thực đơn</div></li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('categor') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/category-management">
                    <i class="fas fa-list"></i> Danh mục món
                </a>
            </li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('items') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/menu">
                    <i class="fas fa-hamburger"></i> Món ăn
                </a>
            </li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('daily-inventory') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/daily-stock">
                    <i class="fas fa-boxes"></i> Tồn kho hàng ngày
                </a>
            </li>
            <li>
                <a class="nav-link ${pageContext.request.requestURI.contains('method-management') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/method-management">
                    <i class="fas fa-fire-burner"></i> Phương thức chế biến
                </a>
            </li>
        </c:if>

        <%-- ===== THỐNG KÊ (chỉ Owner) ===== --%>
        <c:if test="${isOwner}">
            <li class="nav-item">
                <div class="nav-section-title"><i class="fas fa-chart-pie"></i> Thống kê & Báo cáo</div>
            </li>
            <li class="nav-item">
                <a class="nav-link ${pageContext.request.requestURI.contains('restaurant-analytics-dashboard') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/restaurant-analytics-dashboard">
                    <i class="fas fa-chart-line"></i> Tổng quan
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${pageContext.request.requestURI.contains('top-dishes-report') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/menu-performance">
                    <i class="fas fa-award"></i> Món bán chạy
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${pageContext.request.requestURI.contains('peak-hours-analysis') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/peak-hours-analysis">
                    <i class="fas fa-clock"></i> Giờ cao điểm
                </a>
            </li>
        </c:if>

    </ul>
</nav>
