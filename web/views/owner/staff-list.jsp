<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Quản lý nhân viên</title>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
        <style>
            * {
                box-sizing: border-box;
            }
            body {
                margin:0;
                font-family:'Inter',sans-serif;
                background:#faf6f2;
                color:#4a3528;
            }
            .main {
                flex:1;
                min-width:0;
                padding:24px 32px;
            }
            .page-head {
                display:flex;
                justify-content:space-between;
                align-items:flex-end;
                gap:12px;
                flex-wrap:wrap;
                margin-bottom:18px;
            }
            .page-title {
                margin:0;
                color:#76493b;
                font:700 1.6rem 'Playfair Display',serif;
            }
            .page-sub {
                color:#a0714f;
                font-size:.9rem;
                margin-top:4px;
            }
            .btn {
                border:0;
                border-radius:8px;
                padding:9px 16px;
                display:inline-flex;
                align-items:center;
                gap:6px;
                cursor:pointer;
                text-decoration:none;
                font-weight:600;
                font-size:.88rem;
            }
            .btn-primary {
                background:#76493b;
                color:#fff;
            }
            .btn-edit {
                background:#d7bfa4;
                color:#76493b;
            }
            .btn-danger {
                background:#dc3545;
                color:#fff;
            }
            .btn-success {
                background:#198754;
                color:#fff;
            }
            .btn-sm {
                padding:6px 11px;
                font-size:.8rem;
            }
            .filter-bar {
                display:flex;
                align-items:end;
                gap:12px;
                flex-wrap:wrap;
                background:#fff;
                border:1px solid #ede0d8;
                border-radius:12px;
                padding:16px;
                margin-bottom:16px;
            }
            .field {
                display:flex;
                flex-direction:column;
                gap:4px;
            }
            .field label {
                color:#8a6e5a;
                font-size:.78rem;
                font-weight:600;
                text-transform:uppercase;
            }
            .field input,.field select {
                min-width:200px;
                padding:8px 12px;
                border:1px solid #d7bfa4;
                border-radius:7px;
                font:inherit;
            }
            .table-card {
                background:#fff;
                border:1px solid #ede0d8;
                border-radius:12px;
                overflow:auto;
            }
            table {
                width:100%;
                border-collapse:collapse;
                min-width:980px;
            }
            th {
                background:#faf6f2;
                padding:12px;
                text-align:left;
                color:#76493b;
                font-size:.8rem;
                text-transform:uppercase;
                border-bottom:1px solid #ede0d8;
            }
            td {
                padding:12px;
                border-bottom:1px solid #f5ece4;
                font-size:.9rem;
                vertical-align:middle;
            }
            tr:hover {
                background:#faf6f2;
            }
            .avatar {
                width:36px;
                height:36px;
                border-radius:50%;
                object-fit:cover;
                background:#ede0d8;
            }
            .avatar-fallback {
                display:inline-flex;
                align-items:center;
                justify-content:center;
                color:#76493b;
            }
            .badge {
                padding:4px 10px;
                border-radius:12px;
                font-size:.75rem;
                font-weight:600;
                background:#f1e8df;
            }
            .badge-active {
                background:#d4edda;
                color:#155724;
            }
            .badge-inactive {
                background:#f8d7da;
                color:#721c24;
            }
            .badge-reception {
                background:#e8e1ff;
                color:#5944a8;
            }
            .actions-cell {
                display:flex;
                gap:6px;
                flex-wrap:wrap;
            }
            .inline-form {
                display:inline;
            }
            .empty {
                text-align:center;
                padding:40px;
                color:#a0714f;
            }
            .alert {
                padding:11px 14px;
                border-radius:8px;
                margin-bottom:14px;
                background:#d4edda;
                color:#155724;
            }
            .pagination {
                display:flex;
                gap:6px;
                padding:14px 16px;
                align-items:center;
                flex-wrap:wrap;
            }
            .pagination a,.pagination span {
                padding:6px 12px;
                border:1px solid #ede0d8;
                border-radius:6px;
                color:#76493b;
                text-decoration:none;
                font-size:.85rem;
            }
            .pagination .active {
                background:#76493b;
                color:#fff;
            }
            .pagination .info {
                margin-left:auto;
                border:0;
                color:#8a6e5a;
            }
            .confirm-modal {
                display:none;
                position:fixed;
                inset:0;
                z-index:100000;
                background:rgba(0,0,0,.4);
                align-items:center;
                justify-content:center;
            }
            .confirm-modal.show {
                display:flex;
            }
            .confirm-box {
                width:min(400px,90vw);
                background:#fff;
                border-radius:12px;
                padding:24px;
                text-align:center;
                box-shadow:0 8px 32px rgba(0,0,0,.15);
            }
            .confirm-actions {
                display:flex;
                justify-content:center;
                gap:12px;
                margin-top:22px;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div style="display:flex;">
            <%@ include file="/views/includes/dashboard.jsp" %>
            <main class="main">

                <div class="page-head">
                    <div>
                        <h1 class="page-title">Quản lý nhân viên</h1>
                        <div class="page-sub">Tổng cộng: <strong>${totalRecords}</strong> nhân viên</div>
                    </div>

                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/owner/staff?action=create">
                        <i class="fas fa-plus"></i> Thêm nhân viên
                    </a>
                </div>

                <c:if test="${not empty param.msg}">
                    <div class="alert">
                        <c:choose>
                            <c:when test="${param.msg == 'created'}">Tạo nhân viên thành công.</c:when>
                            <c:when test="${param.msg == 'updated'}">Cập nhật nhân viên thành công.</c:when>
                            <c:when test="${param.msg == 'deactivated'}">Đã vô hiệu hóa nhân viên.</c:when>
                            <c:when test="${param.msg == 'reactivated'}">Đã kích hoạt lại nhân viên.</c:when>
                        </c:choose>
                    </div>
                </c:if>


                <form method="get" action="${pageContext.request.contextPath}/owner/staff" class="filter-bar">

                    <input type="hidden" name="action" value="list">
                    <div class="field">
                        <label>Tìm kiếm</label>

                        <input type="text" name="keyword" value="${keyword}" placeholder="Tên, số điện thoại hoặc email...">
                    </div>
                    <div class="field">
                        <label>Trạng thái</label>

                        <select name="status">
                            <option value="" ${empty status ? 'selected' : ''}>Tất cả</option>
                            <option value="1" ${status == '1' ? 'selected' : ''}>Đang hoạt động</option>
                            <option value="0" ${status == '0' ? 'selected' : ''}>Ngừng hoạt động</option>
                        </select>
                    </div>

                    <div class="field">
                        <label>Vai trò</label>

                        <select name="role">
                            <option value="" ${empty role ? 'selected' : ''}>Tất cả</option>
                            <option value="2" ${role == '2' ? 'selected' : ''}>Nhân viên phục vụ</option>
                            <option value="3" ${role == '3' ? 'selected' : ''}>Lễ tân</option>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary"><i class="fas fa-search"></i> Tìm kiếm</button>
                    <a href="${pageContext.request.contextPath}/owner/staff?action=list" class="btn btn-edit">
                        <i class="fas fa-rotate-left"></i> Đặt lại
                    </a>
                </form>


                <div class="table-card">
                    <table>
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Ảnh</th>
                                <th>Họ và tên</th>
                                <th>Vai trò</th>
                                <th>Email</th>
                                <th>Số điện thoại</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty staffList}">
                                    <tr><td colspan="8" class="empty"><i class="fas fa-inbox"></i> Không tìm thấy nhân viên.</td></tr>
                                </c:when>
                                <c:otherwise>

                                    <c:forEach var="s" items="${staffList}" varStatus="loop">
                                        <tr>
                                            <td>${(currentPage - 1) * pageSize + loop.index +1}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty s.image}">
                                                        <img src="${pageContext.request.contextPath}/${s.image}" class="avatar" alt=""
                                                             onerror="this.style.display='none';this.nextElementSibling.style.display='inline-flex';">
                                                        <span class="avatar avatar-fallback" style="display:none;"><i class="fas fa-user"></i></span>
                                                        </c:when>
                                                        <c:otherwise>
                                                        <span class="avatar avatar-fallback"><i class="fas fa-user"></i></span>
                                                        </c:otherwise>
                                                    </c:choose>
                                            </td>
                                            <td><strong>${s.fullName}</strong></td>
                                            <td>
                                                <span class="badge ${s.roleID == 3 ? 'badge-reception' : ''}">
                                                    ${s.roleID == 3 ? 'Lễ tân' : 'Nhân viên phục vụ'}
                                                </span>
                                            </td>
                                            <td>${s.email}</td>
                                            <td>${s.phoneNumber}</td>
                                            <td>
                                                <span class="badge ${s.isActive == 1 ? 'badge-active' : 'badge-inactive'}">
                                                    ${s.isActive == 1 ? 'Đang hoạt động' : 'Ngừng hoạt động'}
                                                </span>
                                            </td>
                                            <td>
                                                <div class="actions-cell">

                                                    <a class="btn btn-sm btn-edit" href="${pageContext.request.contextPath}/owner/staff?action=edit&id=${s.employeeID}">
                                                        <i class="fas fa-edit"></i> Sửa
                                                    </a>

                                                    <form method="post" class="inline-form" action="${pageContext.request.contextPath}/owner/staff"
                                                          onsubmit="return showConfirm(this, event, '${s.isActive == 1 ? 'Bạn có chắc muốn vô hiệu hóa nhân viên này?' : 'Bạn có chắc muốn kích hoạt lại nhân viên này?'}');">

                                                        <input type="hidden" name="action" value="${s.isActive == 1 ? 'deactivate' : 'reactivate'}">

                                                        <input type="hidden" name="id" value="${s.employeeID}">

                                                        <input type="hidden" name="keyword" value="${keyword}">
                                                        <input type="hidden" name="status" value="${status}">
                                                        <input type="hidden" name="role" value="${role}">
                                                        <input type="hidden" name="page" value="${currentPage}">
                                                        <button class="btn btn-sm ${s.isActive == 1 ? 'btn-danger' : 'btn-success'}" type="submit">
                                                            <i class="fas ${s.isActive == 1 ? 'fa-lock' : 'fa-unlock'}"></i>
                                                            ${s.isActive == 1 ? 'Vô hiệu hóa' : 'Kích hoạt lại'}
                                                        </button>
                                                    </form>
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
                            <c:set var="rl" value="${role != null ? role : ''}"/>

                            <c:set var="qs" value="action=list&keyword=${kw}&status=${st}&role=${rl}"/>
                            <!--chỉ hiện mũi tên khi ko phải trang dau-->
                            <c:if test="${currentPage > 1}">
                                <a href="${pageContext.request.contextPath}/owner/staff?${qs}&page=${currentPage - 1}"><i class="fas fa-chevron-left"></i></a>
                                </c:if>
                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <c:choose>
                                        <c:when test="${i == 1}">
                                        <a href="${pageContext.request.contextPath}/owner/staff?${qs}&page=1">1</a>
                                        <span>...</span>
                                    </c:when>
                                    <c:when test="${i == currentPage && i != 1 && i != totalPages}">
                                        <span class="active">${i}</span>
                                    </c:when>
                                    <c:when test="${i == currentPage - 1 && currentPage > 3}">
                                    </c:when>
                                    <c:when test="${i == currentPage + 1 && currentPage < totalPages - 2}">

                                    </c:when>
                                    <c:when test="${i == totalPages}">
                                        <a href="${pageContext.request.contextPath}/owner/staff?${qs}&page=${totalPages}">${totalPages}</a>
                                    </c:when>
                                </c:choose>
                            </c:forEach>
                            <!--Chi hien mui ten khi chua phai trang cuoi-->
                            <c:if test="${currentPage < totalPages}">
                                <a href="${pageContext.request.contextPath}/owner/staff?${qs}&page=${currentPage + 1}"><i class="fas fa-chevron-right"></i></a>
                                </c:if>
                            <span class="info">Trang ${currentPage} / ${totalPages}</span>
                        </div>
                    </c:if>
                </div>
            </main>
        </div>

        <div id="confirmModal" class="confirm-modal">
            <div class="confirm-box">
                <div id="confirmMessage"></div>
                <div class="confirm-actions">
                    <button type="button" class="btn btn-edit" onclick="closeConfirm()">Hủy</button>
                    <button type="button" class="btn btn-primary" onclick="submitConfirmed()">Xác nhận</button>
                </div>
            </div>
        </div>

        <%@ include file="/views/includes/footer.jsp" %>
        <script>
            let pendingForm = null;
            function showConfirm(form, event, message) {
                event.preventDefault();
                pendingForm = form;
                document.getElementById('confirmMessage').textContent = message;
                document.getElementById('confirmModal').classList.add('show');
                return false;
            }
            function closeConfirm() {
                document.getElementById('confirmModal').classList.remove('show');
                pendingForm = null;
            }
            function submitConfirmed() {
                const form = pendingForm;
                closeConfirm();
                if (form)
                    form.submit();
            }
        </script>
    </body>
</html>
