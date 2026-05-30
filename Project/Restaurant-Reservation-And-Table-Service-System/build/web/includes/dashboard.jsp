<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<style>
    .sidebar {
        min-height: calc(100vh - 76px);
        background: #ffffff;
        box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
        padding: 1.5rem 0;
    }

    .sidebar-header {
        padding: 0 1.5rem 1.25rem;
        border-bottom: 2px solid #f1f5f9;
    }

    .sidebar-header h5 {
        color: #1e293b;
        font-weight: 600;
        font-size: 1rem;
        margin: 0;
        display: flex;
        align-items: center;
        gap: 0.6rem;
    }

    .sidebar-header h5 i {
        color: #e31837;
    }

    .sidebar .nav-link {
        color: #475569;
        padding: 0.6rem 1.25rem;
        margin: 0.15rem 0.75rem;
        border-radius: 10px;
        font-weight: 500;
        font-size: 0.875rem;
        transition: all 0.25s ease;
        display: flex;
        align-items: center;
        gap: 0.65rem;
    }

    .sidebar .nav-link i {
        font-size: 0.95rem;
        width: 18px;
        text-align: center;
        color: #e31837;
        opacity: 0.8;
    }

    .sidebar .nav-link:hover {
        background: rgba(227, 24, 55, 0.08);
        color: #e31837;
        transform: translateX(3px);
    }

    .sidebar .nav-link:hover i {
        opacity: 1;
    }

    .sidebar .nav-link.active {
        background: linear-gradient(135deg, rgba(227, 24, 55, 0.15), rgba(220, 53, 69, 0.12));
        color: #e31837;
        font-weight: 600;
        box-shadow: 0 2px 8px rgba(227, 24, 55, 0.15);
    }

    .sidebar .nav-link.active i {
        opacity: 1;
        color: #e31837;
    }

    .nav-section-title {
        padding: 0.6rem 1.5rem 0.2rem;
        font-size: 0.7rem;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: #94a3b8;
        display: flex;
        align-items: center;
        gap: 0.4rem;
    }

    .nav-section-title i {
        font-size: 0.7rem;
    }
</style>

<!-- Sidebar -->
<nav class="col-md-3 col-lg-2 sidebar">
    <div class="sidebar-header">
        <h5><i class="fas fa-store"></i> Restaurant Management</h5>
    </div>
    <ul class="nav flex-column mt-2">

        <!-- Operations Section -->
        <li class="nav-item mt-2">
            <div class="nav-section-title">
                <i class="fas fa-cog"></i> Operations
            </div>
        </li>
        <c:if test="${sessionScope.employee.roleID == 3}">
            <li class="nav-item">
                <a class="nav-link ${pageContext.request.requestURI.contains('order-management') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/order-management">
                    <i class="fas fa-clipboard-list"></i>
                    Orders
                </a>
            </li>
        </c:if>
        <c:if test="${sessionScope.employee.roleID == 2}">
            <li class="nav-item">
                <a class="nav-link ${pageContext.request.requestURI.contains('owner/order-history') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/owner/order-history">
                    <i class="fas fa-history"></i>
                    Order History
                </a>
            </li>
        </c:if>
        <li class="nav-item">
            <a class="nav-link ${pageContext.request.requestURI.contains('business-hours') ? 'active' : ''}"
               href="${pageContext.request.contextPath}/business-hours?action=list">
                <i class="fas fa-clock"></i>
                Business Hours
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${pageContext.request.requestURI.contains('temporary-closure') ? 'active' : ''}"
               href="${pageContext.request.contextPath}/temporary-closure?action=list">
                <i class="fas fa-door-closed"></i>
                Temporary Closure
            </a>
        </li>
        <c:if test="${sessionScope.user.roleID == 2}">
            <li class="nav-item">
                <a class="nav-link ${pageContext.request.requestURI.contains('owner/tables') ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/owner/tables">
                    <i class="fas fa-chair"></i>
                    Restaurant Tables
                </a>
            </li>
        </c:if>
        <!-- Workforce Section -->
        <li class="nav-item mt-2">
            <div class="nav-section-title">
                <i class="fas fa-users"></i> Workforce
            </div>
        </li>
        <li class="nav-item">
            <a class="nav-link ${pageContext.request.requestURI.contains('shift-management') && param.action == 'templates' ? 'active' : ''}"
               href="${pageContext.request.contextPath}/shift-management?action=templates">
                <i class="fas fa-calendar-alt"></i>
                Shift Templates
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${pageContext.request.requestURI.contains('shift-management') && param.action == 'assignments' ? 'active' : ''}"
               href="${pageContext.request.contextPath}/shift-management?action=assignments">
                <i class="fas fa-user-clock"></i>
                Shift Assignments
            </a>
        </li>
        <!-- Financial Section -->
        <li class="nav-item mt-2">
            <div class="nav-section-title">
                <i class="fas fa-money-bill-wave"></i> Finance
            </div>
        </li>
        <li class="nav-item">
            <a class="nav-link ${pageContext.request.requestURI.contains('invoice') ? 'active' : ''}"
               href="${pageContext.request.contextPath}/invoice?action=list">
                <i class="fas fa-file-invoice"></i>
                Invoices
            </a>
        </li>

        <!-- Menu Section -->
        <li class="nav-item mt-2">
            <div class="nav-section-title">
                <i class="fas fa-utensils"></i> Menu
            </div>
        </li>
        <li class="nav-item">
            <a class="nav-link ${pageContext.request.requestURI.contains('categor') ? 'active' : ''}"
               href="${pageContext.request.contextPath}/categories?action=list">
                <i class="fas fa-list"></i>
                Categories
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${pageContext.request.requestURI.contains('items') ? 'active' : ''}"
               href="${pageContext.request.contextPath}/items?action=list">
                <i class="fas fa-hamburger"></i>
                Menu Items
            </a>
        </li>

        <!-- Analytics Section -->
        <li class="nav-item mt-2">
            <div class="nav-section-title">
                <i class="fas fa-chart-pie"></i> Analytics & Reports
            </div>
        </li>
        <li class="nav-item">
            <a class="nav-link ${pageContext.request.requestURI.contains('restaurant-analytics-dashboard') ? 'active' : ''}"
               href="${pageContext.request.contextPath}/restaurant-analytics-dashboard">
                <i class="fas fa-chart-line"></i>
                Overview Dashboard
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${pageContext.request.requestURI.contains('top-dishes-report') ? 'active' : ''}"
               href="${pageContext.request.contextPath}/top-dishes-report">
                <i class="fas fa-award"></i>
                Top Selling Dishes
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${pageContext.request.requestURI.contains('peak-hours-analysis') ? 'active' : ''}"
               href="${pageContext.request.contextPath}/peak-hours-analysis">
                <i class="fas fa-clock"></i>
                Peak Hours
            </a>
        </li>

    </ul>
</nav>