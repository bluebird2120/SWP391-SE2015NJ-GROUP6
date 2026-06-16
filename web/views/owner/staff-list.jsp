<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Staff Management</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: 'Inter', sans-serif; background: #faf6f2; }
        .main { flex: 1; padding: 24px 32px; min-width: 0; }
        .page-head { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 18px; flex-wrap: wrap; gap: 12px; }
        .page-title { font-family: 'Playfair Display', serif; color: #76493b; font-size: 1.6rem; margin: 0; }
        .page-sub { color: #a0714f; font-size: 0.9rem; margin-top: 4px; }
        .btn { padding: 9px 16px; border-radius: 8px; border: none; cursor: pointer; font-size: 0.88rem; font-weight: 600; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; transition: all 0.2s; }
        .btn-primary { background: #76493b; color: #fff; }
        .btn-primary:hover { background: #5d3a2e; }
        .btn-sm { padding: 6px 11px; font-size: 0.8rem; }
        .btn-edit { background: #d7bfa4; color: #76493b; }
        .btn-edit:hover { background: #c5a98a; }
        .btn-danger { background: #dc3545; color: #fff; }
        .btn-danger:hover { background: #b02a37; }
        .btn-success { background: #198754; color: #fff; }
        .btn-success:hover { background: #146c43; }
        .filter-bar {
            background: #fff; border: 1px solid #ede0d8; border-radius: 12px;
            padding: 16px; margin-bottom: 16px;
            display: flex; gap: 12px; align-items: end; flex-wrap: wrap;
        }
        .filter-bar .field { display: flex; flex-direction: column; gap: 4px; }
        .filter-bar label { font-size: 0.78rem; color: #8a6e5a; font-weight: 600; text-transform: uppercase; letter-spacing: 0.04em; }
        .filter-bar input, .filter-bar select {
            padding: 8px 12px; border: 1px solid #d7bfa4; border-radius: 7px;
            font-family: inherit; font-size: 0.9rem; min-width: 200px;
        }
        .filter-bar input:focus, .filter-bar select:focus { outline: none; border-color: #76493b; }
        .table-card { background: #fff; border: 1px solid #ede0d8; border-radius: 12px; overflow: hidden; }
        table { width: 100%; border-collapse: collapse; }
        th { background: #faf6f2; padding: 12px; text-align: left; font-size: 0.8rem; color: #76493b; text-transform: uppercase; letter-spacing: 0.04em; border-bottom: 1px solid #ede0d8; }
        td { padding: 12px; border-bottom: 1px solid #f5ece4; font-size: 0.9rem; color: #4a3528; vertical-align: middle; }
        tr:last-child td { border-bottom: none; }
        tr:hover { background: #faf6f2; }
        .avatar { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; background: #ede0d8; display: inline-flex; align-items: center; justify-content: center; color: #76493b; }
        .badge { padding: 4px 10px; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }
        .badge-active { background: #d4edda; color: #155724; }
        .badge-inactive { background: #f8d7da; color: #721c24; }
        .alert { padding: 11px 14px; border-radius: 8px; margin-bottom: 14px; font-size: 0.88rem; }
        .alert-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .pagination { display: flex; gap: 6px; padding: 14px 16px; background: #fff; border-top: 1px solid #ede0d8; align-items: center; flex-wrap: wrap; }
        .pagination a, .pagination span {
            padding: 6px 12px; border-radius: 6px; text-decoration: none;
            color: #76493b; font-size: 0.85rem; font-weight: 500;
            border: 1px solid #ede0d8;
        }
        .pagination a:hover { background: #f5ece4; }
        .pagination .active { background: #76493b; color: #fff; border-color: #76493b; }
        .pagination .info { margin-left: auto; color: #8a6e5a; font-size: 0.82rem; border: none; padding: 6px 0; }
        .empty { text-align: center; padding: 40px; color: #a0714f; }
        .actions-cell { display: flex; gap: 6px; flex-wrap: wrap; }
        .inline-form { display: inline; }
    </style>
</head>
<body>
    <%@ include file="/views/includes/header.jsp" %>
    <div style="display: flex;">
        <%@ include file="/views/includes/dashboard.jsp" %>
        <main class="main">
            <div class="page-head">
                <div>
                    <h1 class="page-title">Staff Management</h1>
                    <div class="page-sub">Total: <strong>${totalRecords}</strong> staff member(s)</div>
                </div>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/owner/staff?action=create">
                    <i class="fas fa-plus"></i> Add Staff
                </a>
            </div>

            <c:if test="${param.msg == 'created'}">
                <div class="alert alert-success"><i class="fas fa-check-circle"></i> Staff created successfully.</div>
            </c:if>
            <c:if test="${param.msg == 'updated'}">
                <div class="alert alert-success"><i class="fas fa-check-circle"></i> Staff updated successfully.</div>
            </c:if>
            <c:if test="${param.msg == 'deactivated'}">
                <div class="alert alert-success"><i class="fas fa-check-circle"></i> Staff has been deactivated.</div>
            </c:if>
            <c:if test="${param.msg == 'reactivated'}">
                <div class="alert alert-success"><i class="fas fa-check-circle"></i> Staff has been reactivated.</div>
            </c:if>

            <form method="get" action="${pageContext.request.contextPath}/owner/staff" class="filter-bar">
                <input type="hidden" name="action" value="list">
                <div class="field">
                    <label>Search</label>
                    <input type="text" name="keyword" value="${keyword}" placeholder="Name, phone or email...">
                </div>
                <div class="field">
                    <label>Status</label>
                    <select name="status">
                        <option value="" ${empty status ? 'selected' : ''}>All</option>
                        <option value="1" ${status == '1' ? 'selected' : ''}>Active</option>
                        <option value="0" ${status == '0' ? 'selected' : ''}>Inactive</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-search"></i> Search
                </button>
                <a href="${pageContext.request.contextPath}/owner/staff?action=list" class="btn btn-edit">
                    <i class="fas fa-rotate-left"></i> Reset
                </a>
            </form>

            <div class="table-card">
                <table>
                    <thead>
                        <tr>
                            <th style="width: 60px;">#</th>
                            <th style="width: 60px;">Photo</th>
                            <th>Full Name</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Status</th>
                            <th style="width: 200px;">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty staffList}">
                                <tr><td colspan="7" class="empty"><i class="fas fa-inbox"></i> No staff found.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="s" items="${staffList}" varStatus="loop">
                                    <tr>
                                        <td>${(currentPage - 1) * 5 + loop.index + 1}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty s.image}">
                                                    <img src="${pageContext.request.contextPath}/${s.image}" class="avatar" alt="">
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="avatar"><i class="fas fa-user"></i></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><strong>${s.fullName}</strong></td>
                                        <td>${s.email}</td>
                                        <td>${s.phoneNumber}</td>

                                        <td>
                                            <c:choose>
                                                <c:when test="${s.isActive == 1}">
                                                    <span class="badge badge-active">Active</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-inactive">Inactive</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="actions-cell">
                                                <a class="btn btn-sm btn-edit" href="${pageContext.request.contextPath}/owner/staff?action=edit&id=${s.employeeID}">
                                                    <i class="fas fa-edit"></i> Edit
                                                </a>
                                                <c:choose>
                                                    <c:when test="${s.isActive == 1}">
                                                        <form method="post" class="inline-form" action="${pageContext.request.contextPath}/owner/staff" onsubmit="return confirm('Deactivate this staff member?');">
                                                            <input type="hidden" name="action" value="deactivate">
                                                            <input type="hidden" name="id" value="${s.employeeID}">
                                                            <input type="hidden" name="keyword" value="${keyword}">
                                                            <input type="hidden" name="status" value="${status}">
                                                            <input type="hidden" name="page" value="${currentPage}">
                                                            <button class="btn btn-sm btn-danger" type="submit">
                                                                <i class="fas fa-lock"></i> Deactivate
                                                            </button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <form method="post" class="inline-form" action="${pageContext.request.contextPath}/owner/staff">
                                                            <input type="hidden" name="action" value="reactivate">
                                                            <input type="hidden" name="id" value="${s.employeeID}">
                                                            <input type="hidden" name="keyword" value="${keyword}">
                                                            <input type="hidden" name="status" value="${status}">
                                                            <input type="hidden" name="page" value="${currentPage}">
                                                            <button class="btn btn-sm btn-success" type="submit">
                                                                <i class="fas fa-unlock"></i> Reactivate
                                                            </button>
                                                        </form>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <c:if test="${totalPages > 1}">
                    <div class="pagination">
                        <c:set var="kw" value="${keyword != null ? keyword : ''}"/>
                        <c:set var="st" value="${status != null ? status : ''}"/>
                        <c:set var="qs" value="action=list&keyword=${kw}&status=${st}"/>

                        <c:if test="${currentPage > 1}">
                            <a href="${pageContext.request.contextPath}/owner/staff?${qs}&page=${currentPage - 1}">
                                <i class="fas fa-chevron-left"></i>
                            </a>
                        </c:if>

                        <c:forEach var="i" begin="1" end="${totalPages}">
                            <c:choose>
                                <c:when test="${i == currentPage}">
                                    <span class="active">${i}</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/owner/staff?${qs}&page=${i}">${i}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <c:if test="${currentPage < totalPages}">
                            <a href="${pageContext.request.contextPath}/owner/staff?${qs}&page=${currentPage + 1}">
                                <i class="fas fa-chevron-right"></i>
                            </a>
                        </c:if>

                        <span class="info">Page ${currentPage} / ${totalPages}</span>
                    </div>
                </c:if>
            </div>
                            </main>
                        </div>
    <%@ include file="/views/includes/footer.jsp" %>
</body>
</html>
