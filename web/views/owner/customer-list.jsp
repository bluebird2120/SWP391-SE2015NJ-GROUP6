<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý khách hàng</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: 'Inter', sans-serif; background: #faf6f2; }
        .main { flex: 1; padding: 24px 32px; min-width: 0; }
        .page-head { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 18px; flex-wrap: wrap; gap: 12px; }
        .page-title { font-family: 'Playfair Display', serif; color: #76493b; font-size: 1.6rem; margin: 0; }
        .page-sub { color: #a0714f; font-size: 0.9rem; margin-top: 4px; }
        .btn {
            padding: 9px 16px; border-radius: 8px; border: none; cursor: pointer;
            font-family: inherit; font-size: 0.88rem; font-weight: 600;
            text-decoration: none; display: inline-flex; align-items: center;
            justify-content: center; gap: 6px; transition: all 0.2s;
        }
        .btn-primary { background: #76493b; color: #fff; }
        .btn-primary:hover { background: #5d3a2e; }
        .btn-reset { background: #d7bfa4; color: #76493b; }
        .btn-reset:hover { background: #c5a98a; }
        .filter-bar {
            background: #fff; border: 1px solid #ede0d8; border-radius: 12px;
            padding: 16px; margin-bottom: 16px;
            display: flex; gap: 12px; align-items: end; flex-wrap: wrap;
        }
        .filter-bar .field { display: flex; flex-direction: column; gap: 4px; }
        .filter-bar label {
            font-size: 0.78rem; color: #8a6e5a; font-weight: 600;
            text-transform: uppercase; letter-spacing: 0.04em;
        }
        .filter-bar input, .filter-bar select {
            padding: 8px 12px; border: 1px solid #d7bfa4; border-radius: 7px;
            font-family: inherit; font-size: 0.9rem; min-width: 220px;
        }
        .filter-bar input:focus, .filter-bar select:focus {
            outline: none; border-color: #76493b;
        }
        .table-card {
            background: #fff; border: 1px solid #ede0d8;
            border-radius: 12px; overflow: hidden;
        }
        table { width: 100%; border-collapse: collapse; }
        th {
            background: #faf6f2; padding: 12px; text-align: left;
            font-size: 0.8rem; color: #76493b; text-transform: uppercase;
            letter-spacing: 0.04em; border-bottom: 1px solid #ede0d8;
        }
        td {
            padding: 12px; border-bottom: 1px solid #f5ece4;
            font-size: 0.9rem; color: #4a3528; vertical-align: middle;
        }
        tr:last-child td { border-bottom: none; }
        tbody tr:hover { background: #faf6f2; }
        .badge {
            padding: 4px 10px; border-radius: 12px;
            font-size: 0.75rem; font-weight: 600;
        }
        .badge-local { background: #d4edda; color: #155724; }
        .badge-google { background: #e4edff; color: #2456a6; }
        .badge-active { background: #d4edda; color: #155724; }
        .badge-locked { background: #f8d7da; color: #842029; }
        .customer-cell { display: flex; align-items: center; gap: 10px; }
        .customer-avatar {
            width: 42px; height: 42px; border-radius: 50%;
            object-fit: cover; background: #eaded6; color: #76493b;
            display: inline-flex; align-items: center; justify-content: center;
            font-weight: 700;
        }
        .btn-detail { background: #d7bfa4; color: #5a3428; }
        .btn-detail:hover { background: #c5a98a; }
        .btn-lock { background: #dc3545; color: #fff; }
        .btn-lock:hover { background: #bb2d3b; }
        .btn-unlock { background: #198754; color: #fff; }
        .btn-unlock:hover { background: #157347; }
        .message {
            background: #fff3cd; color: #664d03; border: 1px solid #ffecb5;
            border-radius: 8px; padding: 11px 14px; margin-bottom: 14px;
        }
        .empty { text-align: center; padding: 40px; color: #a0714f; }
        .pagination {
            display: flex; gap: 6px; padding: 14px 16px;
            background: #fff; border-top: 1px solid #ede0d8;
            align-items: center; flex-wrap: wrap;
        }
        .pagination a, .pagination span {
            padding: 6px 12px; border-radius: 6px; text-decoration: none;
            color: #76493b; font-size: 0.85rem; font-weight: 500;
            border: 1px solid #ede0d8;
        }
        .pagination a:hover { background: #f5ece4; }
        .pagination .active {
            background: #76493b; color: #fff; border-color: #76493b;
        }
        .pagination .info {
            margin-left: auto; color: #8a6e5a;
            font-size: 0.82rem; border: none; padding: 6px 0;
        }
    </style>
</head>
<body>
    <%@ include file="/views/includes/header.jsp" %>
    <div style="display: flex;">
        <%@ include file="/views/includes/dashboard.jsp" %>
        <main class="main">

            <div class="page-head">
                <div>
                    <h1 class="page-title">Quản lý khách hàng</h1>
                    <div class="page-sub">
                        Tổng cộng: <strong>${totalRows}</strong> khách hàng
                    </div>
                </div>
            </div>

            <c:if test="${not empty sessionScope.customerStatusMessage}">
                <div class="message">${sessionScope.customerStatusMessage}</div>
                <c:remove var="customerStatusMessage" scope="session"/>
            </c:if>


            <form method="get"
                  action="${pageContext.request.contextPath}/owner/customer-list"
                  class="filter-bar">
                <div class="field">
                    <label>Tìm kiếm</label>

                    <input type="text" name="search" value="${search}"
                           placeholder="Tên, số điện thoại hoặc email...">
                </div>
                <div class="field">
                    <label>Loại tài khoản</label>

                    <select name="loginProvider">
                        <option value="all" ${loginProvider == 'all' ? 'selected' : ''}>
                            Tất cả
                        </option>
                        <option value="local" ${loginProvider == 'local' ? 'selected' : ''}>
                            Tài khoản thường
                        </option>
                        <option value="google" ${loginProvider == 'google' ? 'selected' : ''}>
                            Google
                        </option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-search"></i> Tìm kiếm
                </button>
                <a href="${pageContext.request.contextPath}/owner/customer-list"
                   class="btn btn-reset">
                    <i class="fas fa-rotate-left"></i> Đặt lại
                </a>
            </form>

            <div class="table-card">
                <table>
                    <thead>
                        <tr>
                            <th style="width: 60px;">#</th>
                            <th>Khách hàng</th>
                            <th>Loại tài khoản</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty customers}">
                                <tr>
                                    <td colspan="5" class="empty">
                                        <i class="fas fa-inbox"></i>
                                        Không tìm thấy khách hàng nào.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>

                                <c:forEach var="customer" items="${customers}" varStatus="loop">
                                    <tr>
                                        <td>${(page - 1) * pageSize + loop.index + 1}</td>
                                        <td>
                                            <div class="customer-cell">
                                                <c:choose>
                                                    <c:when test="${not empty customer.image}">
                                                        <img class="customer-avatar"
                                                             src="${pageContext.request.contextPath}/${customer.image}"
                                                             alt="${customer.userName}">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="customer-avatar">
                                                            <i class="fas fa-user"></i>
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                                <strong>${customer.userName}</strong>
                                            </div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${customer.loginProvider == 'google'}">
                                                    <span class="badge badge-google">Google</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-local">Local</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${customer.isActive == 1}">
                                                    <span class="badge badge-active">Hoạt động</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-locked">Đã khóa</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/owner/customer-list?action=detail&customerID=${customer.customerID}"
                                               class="btn btn-detail">
                                                <i class="fas fa-eye"></i> Chi tiết
                                            </a>
                                            <form method="post"
                                                  action="${pageContext.request.contextPath}/owner/customer-list"
                                                  style="display:inline-flex; margin-left:6px;">

                                                <input type="hidden" name="customerID"
                                                       value="${customer.customerID}">

                                                <input type="hidden" name="page" value="${page}">
                                                <c:choose>
                                                    <c:when test="${customer.isActive == 1}">

                                                        <input type="hidden" name="isActive" value="0">
                                                        <button type="submit" class="btn btn-lock"
                                                                onclick="return confirm('Bạn chắc chắn muốn khóa tài khoản này?')">
                                                            <i class="fas fa-lock"></i> Khóa
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>

                                                        <input type="hidden" name="isActive" value="1">
                                                        <button type="submit" class="btn btn-unlock"
                                                                onclick="return confirm('Bạn chắc chắn muốn mở khóa tài khoản này?')">
                                                            <i class="fas fa-lock-open"></i> Mở khóa
                                                        </button>
                                                    </c:otherwise>
                                                </c:choose>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <c:if test="${totalPages > 1}">
                    <div class="pagination">
                        <c:if test="${page > 1}">

                            <c:url var="prevUrl" value="/owner/customer-list">
                                <c:param name="page" value="${page - 1}"/>
                                <c:param name="search" value="${search}"/>
                                <c:param name="loginProvider" value="${loginProvider}"/>
                            </c:url>
                            <a href="${prevUrl}" title="Trang trước">
                                <i class="fas fa-chevron-left"></i>
                            </a>
                        </c:if>

                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <c:url var="pageUrl" value="/owner/customer-list">
                                <c:param name="page" value="${i}"/>
                                <c:param name="search" value="${search}"/>
                                <c:param name="loginProvider" value="${loginProvider}"/>
                            </c:url>
                            <c:choose>
                                <c:when test="${i == page}">
                                    <span class="active">${i}</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageUrl}">${i}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <c:if test="${page < totalPages}">
                            <c:url var="nextUrl" value="/owner/customer-list">
                                <c:param name="page" value="${page + 1}"/>
                                <c:param name="search" value="${search}"/>
                                <c:param name="loginProvider" value="${loginProvider}"/>
                            </c:url>
                            <a href="${nextUrl}" title="Trang sau">
                                <i class="fas fa-chevron-right"></i>
                            </a>
                        </c:if>

                        <span class="info">Trang ${page} / ${totalPages}</span>
                    </div>
                </c:if>
            </div>
        </main>
    </div>
    <%@ include file="/views/includes/footer.jsp" %>
</body>
</html>
