<<<<<<< Updated upstream
<%@page contentType="text/html" pageEncoding="UTF-8" %>
=======
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
>>>>>>> Stashed changes
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Danh sách khách hàng</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
        body {
            background-color: #f8f5f0;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        .page-container {
            max-width: 1200px;
            margin: 40px auto;
            background: white;
            padding: 30px;
            border-radius: 16px;
            box-shadow: 0 4px 18px rgba(0,0,0,0.08);
        }

        .page-title {
            font-weight: 700;
            color: #5c3a21;
            margin-bottom: 25px;
        }

        .table th {
            background-color: #8c6239;
            color: white;
            text-align: center;
        }

        .table td {
            vertical-align: middle;
        }

        .badge-local {
            background-color: #198754;
        }

        .badge-google {
            background-color: #dc3545;
        }
    </style>
</head>

<body>
<<<<<<< Updated upstream
    <%@include file="/views/includes/header.jsp" %>
=======
    
      <%@ include file="/views/includes/header.jsp" %>
   
        <%@ include file="/views/includes/dashboard.jsp" %>

>>>>>>> Stashed changes
<div class="page-container">

    <h2 class="page-title">Danh sách khách hàng</h2>

    <form method="get" action="${pageContext.request.contextPath}/owner/customers-list" class="row g-3 mb-4">

        <div class="col-md-5">
            <input type="text"
                   name="search"
                   value="${search}"
                   class="form-control"
                   placeholder="Tìm theo tên, số điện thoại hoặc email">
        </div>

        <div class="col-md-3">
            <select name="loginProvider" class="form-select">
                <option value="all" ${loginProvider == 'all' ? 'selected' : ''}>
                    Tất cả loại tài khoản
                </option>

                <option value="local" ${loginProvider == 'local' ? 'selected' : ''}>
                    Tài khoản thường
                </option>

                <option value="google" ${loginProvider == 'google' ? 'selected' : ''}>
                    Google
                </option>
            </select>
        </div>

        <div class="col-md-2">
            <button type="submit" class="btn btn-primary w-100">
                Lọc
            </button>
        </div>

        <div class="col-md-2">
            <a href="${pageContext.request.contextPath}/owner/customers-list" class="btn btn-secondary w-100">
                Reset
            </a>
        </div>

    </form>

    <div class="mb-3">
        <strong>Tổng số khách hàng:</strong> ${totalRows}
    </div>

    <table class="table table-bordered table-hover">
        <thead>
        <tr>
            <th>ID</th>
            <th>Tên khách hàng</th>
            <th>Số điện thoại</th>
            <th>Email</th>
            <th>Loại tài khoản</th>
            <th>Ngày tạo</th>
        </tr>
        </thead>

        <tbody>
        <c:choose>
            <c:when test="${empty customers}">
                <tr>
                    <td colspan="6" class="text-center text-muted">
                        Không tìm thấy khách hàng nào.
                    </td>
                </tr>
            </c:when>

            <c:otherwise>
                <c:forEach var="c" items="${customers}">
                    <tr>
                        <td class="text-center">${c.customerID}</td>
                        <td>${c.userName}</td>
                        <td>${c.phoneNumber}</td>
                        <td>${c.email}</td>
                        <td class="text-center">
                            <c:choose>
                                <c:when test="${c.loginProvider == 'google'}">
                                    <span class="badge badge-google">Google</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-local">Local</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>${c.createdAt}</td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>

    <nav>
        <ul class="pagination justify-content-center">

            <c:if test="${page > 1}">
                <c:url var="prevUrl" value="/owner/customers-list">
                    <c:param name="page" value="${page - 1}" />
                    <c:param name="search" value="${search}" />
                    <c:param name="loginProvider" value="${loginProvider}" />
                </c:url>

                <li class="page-item">
                    <a class="page-link" href="${prevUrl}">Trước</a>
                </li>
            </c:if>

            <c:forEach begin="1" end="${totalPages}" var="i">
                <c:url var="pageUrl" value="/owner/customers-list">
                    <c:param name="page" value="${i}" />
                    <c:param name="search" value="${search}" />
                    <c:param name="loginProvider" value="${loginProvider}" />
                </c:url>

                <li class="page-item ${i == page ? 'active' : ''}">
                    <a class="page-link" href="${pageUrl}">${i}</a>
                </li>
            </c:forEach>

            <c:if test="${page < totalPages}">
                <c:url var="nextUrl" value="/owner/customers-list">
                    <c:param name="page" value="${page + 1}" />
                    <c:param name="search" value="${search}" />
                    <c:param name="loginProvider" value="${loginProvider}" />
                </c:url>

                <li class="page-item">
                    <a class="page-link" href="${nextUrl}">Sau</a>
                </li>
            </c:if>

        </ul>
    </nav>

</div>
<<<<<<< Updated upstream
     <%@include file="/views/includes/footer.jsp" %>  
=======
<%@ include file="/views/includes/footer.jsp" %>

>>>>>>> Stashed changes
</body>
</html>