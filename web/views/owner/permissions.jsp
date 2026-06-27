<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Phân quyền – Vị An Restaurant</title>
        <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@600;700&family=Nunito:wght@400;500;600;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>
        <style>
            *, *::before, *::after {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
            }
            body {
                font-family: 'Nunito', sans-serif;
                background: #fdf6f0;
                display: flex;
                flex-direction: column;
                min-height: 100vh;
            }
            .layout {
                display: flex;
                flex: 1;
            }
            .main-content {
                flex: 1;
                padding: 28px 32px;
                overflow-y: auto;
                min-width: 0;
            }

            .page-header {
                display: flex;
                align-items: center;
                gap: 14px;
                margin-bottom: 24px;
                padding-bottom: 18px;
                border-bottom: 1px solid #ede0d8;
            }
            .page-header-icon {
                width: 44px;
                height: 44px;
                border-radius: 12px;
                background: #76493b;
                color: #f0dcc2;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 1.1rem;
                flex-shrink: 0;
            }
            .page-header h1 {
                font-family: 'Playfair Display', serif;
                font-size: 1.35rem;
                color: #3d2318;
                margin-bottom: 3px;
            }
            .page-header p {
                font-size: 0.8rem;
                color: #b09080;
            }

            .alert {
                padding: 11px 16px;
                border-radius: 10px;
                font-size: 0.85rem;
                display: flex;
                align-items: center;
                gap: 10px;
                margin-bottom: 18px;
            }
            .alert-success {
                background: #eafaf1;
                border: 1px solid #b7e4c7;
                color: #1e6b3a;
            }
            .alert-error   {
                background: #fcebeb;
                border: 1px solid #f5c1c1;
                color: #791f1f;
            }

            /* ── 2-COLUMN: trái = quyền (hẹp, sticky), phải = bảng nhân viên ── */
            .perm-layout {
                display: grid;
                grid-template-columns: 1fr 290px;
                gap: 20px;
                align-items: start;
            }

            /* ─── CỘT PHẢI: Panel quyền ─── */
            .perm-panel {
                background: #fff;
                border-radius: 14px;
                box-shadow: 0 3px 16px rgba(90,45,12,0.07);
                overflow: hidden;
                position: sticky;
                top: 20px;
            }
            .panel-header {
                background: #76493b;
                color: #f0dcc2;
                padding: 13px 18px;
                display: flex;
                align-items: center;
                gap: 9px;
                font-family: 'Playfair Display', serif;
                font-size: 0.92rem;
                font-weight: 600;
            }
            .panel-body {
                padding: 18px;
            }

            /* Mini bar nhân viên đang chọn */
            .selected-bar {
                background: #fdf6f0;
                border: 1px solid #ede0d8;
                border-radius: 10px;
                padding: 11px 13px;
                display: flex;
                align-items: center;
                gap: 11px;
                margin-bottom: 16px;
            }
            .sel-avatar {
                width: 38px;
                height: 38px;
                border-radius: 50%;
                flex-shrink: 0;
                background: #76493b;
                color: #f0dcc2;
                font-family: 'Playfair Display', serif;
                font-size: 1rem;
                font-weight: 700;
                display: flex;
                align-items: center;
                justify-content: center;
                overflow: hidden;
            }
            .sel-avatar img {
                width: 38px;
                height: 38px;
                object-fit: cover;
            }
            .sel-info h4 {
                font-size: 0.87rem;
                color: #3d2318;
                font-weight: 700;
                margin-bottom: 2px;
            }
            .sel-info p  {
                font-size: 0.74rem;
                color: #b09080;
            }

            /* Checkbox quyền */
            .perm-list {
                display: flex;
                flex-direction: column;
                gap: 9px;
                margin-bottom: 16px;
            }
            .perm-item {
                border: 2px solid #ede0d8;
                border-radius: 10px;
                padding: 12px 13px;
                cursor: pointer;
                transition: all 0.2s;
                display: flex;
                align-items: center;
                gap: 11px;
            }
            .perm-item:hover  {
                border-color: #76493b;
                background: #fdf6f0;
            }
            .perm-item.checked{
                border-color: #76493b;
                background: rgba(118,73,59,0.06);
            }
            .perm-item input[type="checkbox"] {
                display: none;
            }
            .perm-check {
                width: 19px;
                height: 19px;
                border-radius: 5px;
                border: 2px solid #d4b9ae;
                background: #fff;
                flex-shrink: 0;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: all 0.2s;
                color: #fff;
                font-size: 0.68rem;
            }
            .perm-item.checked .perm-check {
                background: #76493b;
                border-color: #76493b;
            }
            .perm-icon {
                width: 32px;
                height: 32px;
                border-radius: 8px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 0.85rem;
                flex-shrink: 0;
            }
            .perm-icon.inventory {
                background: #e8f4e8;
                color: #2d7a2d;
            }
            .perm-icon.workforce {
                background: #e8eef8;
                color: #1a4080;
            }
            .perm-icon.finance   {
                background: #fdf0e0;
                color: #a05c00;
            }
            .perm-text {
                flex: 1;
                min-width: 0;
            }
            .perm-key  {
                font-size: 0.78rem;
                font-weight: 700;
                color: #3d2318;
                margin-bottom: 2px;
            }
            .perm-desc {
                font-size: 0.71rem;
                color: #b09080;
                line-height: 1.35;
            }

            .action-col {
                display: flex;
                flex-direction: column;
                gap: 7px;
            }
            .btn-save {
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 7px;
                padding: 9px;
                width: 100%;
                background: #76493b;
                color: #fff;
                border: none;
                border-radius: 9px;
                font-family: 'Nunito', sans-serif;
                font-size: 0.87rem;
                font-weight: 700;
                cursor: pointer;
                transition: background 0.2s;
            }
            .btn-save:hover {
                background: #5a3329;
            }
            .btn-clear {
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 7px;
                padding: 8px;
                width: 100%;
                background: transparent;
                color: #c0392b;
                border: 1.5px solid #f5c1c1;
                border-radius: 9px;
                font-family: 'Nunito', sans-serif;
                font-size: 0.81rem;
                font-weight: 700;
                cursor: pointer;
                transition: all 0.2s;
            }
            .btn-clear:hover {
                background: #fcebeb;
            }

            .empty-perm {
                text-align: center;
                padding: 36px 14px;
                color: #c0a898;
            }
            .empty-perm i {
                font-size: 1.8rem;
                margin-bottom: 10px;
                color: #e0d0c8;
                display: block;
            }
            .empty-perm p {
                font-size: 0.8rem;
                line-height: 1.6;
            }

            /* ─── CỘT TRÁI: Bảng nhân viên ─── */
            .staff-panel {
                background: #fff;
                border-radius: 14px;
                box-shadow: 0 3px 16px rgba(90,45,12,0.07);
                overflow: hidden;
            }
            .staff-panel-header {
                background: #76493b;
                color: #f0dcc2;
                padding: 13px 20px;
                display: flex;
                align-items: center;
                gap: 9px;
                font-family: 'Playfair Display', serif;
                font-size: 0.92rem;
                font-weight: 600;
            }
            .staff-count {
                margin-left: auto;
                font-size: 0.74rem;
                font-family: 'Nunito', sans-serif;
                opacity: 0.8;
            }

            /* Search */
            .search-wrap {
                padding: 12px 16px;
                border-bottom: 1px solid #f0e6dc;
                position: relative;
            }
            .search-input {
                width: 100%;
                padding: 8px 12px 8px 34px;
                font-family: 'Nunito', sans-serif;
                font-size: 0.84rem;
                color: #3d2318;
                background: #fdf6f0;
                border: 1.5px solid #ede0d8;
                border-radius: 8px;
                outline: none;
                transition: border-color 0.2s;
            }
            .search-input:focus {
                border-color: #76493b;
            }
            .search-icon {
                position: absolute;
                left: 26px;
                top: 50%;
                transform: translateY(-50%);
                color: #b09080;
                font-size: 0.8rem;
            }

            /* Bảng nhân viên */
            .staff-table {
                width: 100%;
                border-collapse: collapse;
            }
            .staff-table thead th {
                padding: 10px 16px;
                text-align: left;
                font-size: 0.7rem;
                font-weight: 700;
                text-transform: uppercase;
                letter-spacing: 0.07em;
                color: #b09080;
                border-bottom: 1px solid #f0e6dc;
                background: #fdf6f0;
            }
            .staff-table thead th:first-child {
                width: 44px;
                text-align: center;
            }
            .staff-table tbody tr {
                border-bottom: 1px solid #f9f0ea;
                cursor: pointer;
                transition: background 0.15s;
            }
            .staff-table tbody tr:last-child {
                border-bottom: none;
            }
            .staff-table tbody tr:hover {
                background: #fdf6f0;
            }
            .staff-table tbody tr.active-row {
                background: rgba(118,73,59,0.07);
                border-left: 3px solid #76493b;
            }
            .staff-table td {
                padding: 12px 16px;
                vertical-align: middle;
            }
            .staff-table td:first-child {
                text-align: center;
                font-size: 0.8rem;
                color: #b09080;
                font-weight: 700;
            }

            /* Avatar trong bảng */
            .tbl-avatar {
                width: 38px;
                height: 38px;
                border-radius: 50%;
                background: #76493b;
                color: #f0dcc2;
                font-family: 'Playfair Display', serif;
                font-size: 1rem;
                font-weight: 700;
                display: flex;
                align-items: center;
                justify-content: center;
                overflow: hidden;
            }
            .tbl-avatar img {
                width: 38px;
                height: 38px;
                object-fit: cover;
            }

            .tbl-name  {
                font-size: 0.88rem;
                font-weight: 700;
                color: #3d2318;
            }
            .tbl-phone {
                font-size: 0.78rem;
                color: #b09080;
            }

            .perm-badge {
                font-size: 0.67rem;
                font-weight: 700;
                padding: 2px 8px;
                border-radius: 20px;
                white-space: nowrap;
            }
            .perm-badge.has-perm {
                background: #e8f4e8;
                color: #2d7a2d;
            }
            .perm-badge.no-perm  {
                background: #f0ede9;
                color: #b09080;
            }

            .no-result-row {
                display: none;
            }
            .no-result-row td {
                padding: 28px;
                text-align: center;
                color: #b09080;
                font-size: 0.82rem;
            }

            /* Phân trang */
            .pagination-wrap {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 12px 18px;
                border-top: 1px solid #f0e6dc;
                flex-wrap: wrap;
                gap: 8px;
            }
            .pagination {
                display: flex;
                gap: 5px;
            }
            .pg-btn {
                min-width: 32px;
                height: 32px;
                padding: 0 8px;
                display: flex;
                align-items: center;
                justify-content: center;
                border-radius: 8px;
                border: 1.5px solid #ede0d8;
                background: #fff;
                font-family: 'Nunito', sans-serif;
                font-size: 0.82rem;
                font-weight: 700;
                color: #76493b;
                cursor: pointer;
                transition: all 0.15s;
            }
            .pg-btn:hover:not([disabled]) {
                background: #fdf6f0;
                border-color: #76493b;
            }
            .pg-btn.active {
                background: #76493b;
                color: #fff;
                border-color: #76493b;
            }
            .pg-btn[disabled] {
                opacity: 0.35;
                cursor: default;
            }
            .pg-info {
                font-size: 0.78rem;
                color: #b09080;
                font-weight: 600;
            }

            @media (max-width: 900px) {
                .perm-layout {
                    grid-template-columns: 1fr;
                }
                .perm-panel  {
                    position: static;
                }
            }
        </style>
    </head>
    <body>

        <%@ include file="/views/includes/header.jsp" %>
        <div class="layout">
            <%@ include file="/views/includes/dashboard.jsp" %>

            <div class="main-content">

                <div class="page-header">
                    <div class="page-header-icon"><i class="fas fa-shield-alt"></i></div>
                    <div>
                        <h1>Phân quyền nhân viên</h1>
                        <p>Cấp hoặc thu hồi quyền truy cập bổ sung cho từng nhân viên</p>
                    </div>
                </div>

                <c:if test="${not empty permissionMsg}">
                    <c:choose>
                        <c:when test="${permissionMsg.startsWith('success:')}">
                            <div class="alert alert-success"><i class="fas fa-circle-check"></i> ${permissionMsg.substring(8)}</div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-error"><i class="fas fa-circle-exclamation"></i> ${permissionMsg.substring(6)}</div>
                        </c:otherwise>
                    </c:choose>
                </c:if>

                <div class="perm-layout">

                    <!-- ══ CỘT TRÁI: Bảng nhân viên ══ -->
                    <div class="staff-panel">
                        <div class="staff-panel-header">
                            <i class="fas fa-users"></i> Danh sách nhân viên
                            <span class="staff-count">${staffList.size()} người</span>
                        </div>

                        <div class="search-wrap">
                            <i class="fas fa-magnifying-glass search-icon"></i>
                            <input type="text" id="staffSearch" class="search-input"
                                   placeholder="Tìm theo tên hoặc số điện thoại..."
                                   oninput="filterStaff(this.value)" autocomplete="off">
                        </div>

                        <table class="staff-table">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Ảnh</th>
                                    <th>Họ và tên</th>
                                    <th>Số điện thoại</th>
                                    <th>Quyền</th>
                                </tr>
                            </thead>
                            <tbody id="staffTbody">
                                <c:forEach items="${staffList}" var="s" varStatus="loop">
                                    <c:set var="permCount" value="${staffPermCounts[s.employeeID]}"/>
                                    <tr class="${selectedEmployee.employeeID == s.employeeID ? 'active-row' : ''}"
                                        data-name="${s.fullName.toLowerCase()}"
                                        data-phone="${not empty s.phoneNumber ? s.phoneNumber : ''}"
                                        onclick="location.href = '${pageContext.request.contextPath}/owner/permissions?empID=${s.employeeID}'">
                                        <td>${loop.index + 1}</td>
                                        <td>
                                            <div class="tbl-avatar">
                                                <c:choose>
                                                    <c:when test="${not empty s.image}">
                                                        <img src="${pageContext.request.contextPath}/${s.image}" alt="av">
                                                    </c:when>
                                                    <c:otherwise>${s.fullName.substring(0,1).toUpperCase()}</c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                        <td><div class="tbl-name">${s.fullName}</div></td>
                                        <td>
                                            <div class="tbl-phone">
                                                <c:choose>
                                                    <c:when test="${not empty s.phoneNumber}">${s.phoneNumber}</c:when>
                                                    <c:otherwise><em style="color:#ccc">Chưa có</em></c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${permCount != null && permCount > 0}">
                                                    <span class="perm-badge has-perm">${permCount} quyền</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="perm-badge no-perm">Mặc định</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <tr class="no-result-row" id="noResultRow">
                                    <td colspan="5"><i class="fas fa-user-slash"></i> Không tìm thấy nhân viên</td>
                                </tr>
                            </tbody>
                        </table>
                        <div class="pagination-wrap" id="paginationWrap">
                            <div class="pagination" id="paginationBtns"></div>
                            <span class="pg-info" id="pgInfo"></span>
                        </div>
                    </div>
                        
                    <!-- ══ CỘT PHẢI: Panel phân quyền ══ -->
                    <div class="perm-panel">
                        <div class="panel-header">
                            <i class="fas fa-key"></i>
                            <c:choose>
                                <c:when test="${selectedEmployee != null}">Quyền của nhân viên</c:when>
                                <c:otherwise>Phân quyền</c:otherwise>
                            </c:choose>
                        </div>
                        <div class="panel-body">
                            <c:choose>
                                <c:when test="${selectedEmployee != null}">
                                    <!-- Mini bar -->
                                    <div class="selected-bar">
                                        <div class="sel-avatar">
                                            <c:choose>
                                                <c:when test="${not empty selectedEmployee.image}">
                                                    <img src="${pageContext.request.contextPath}/${selectedEmployee.image}" alt="av">
                                                </c:when>
                                                <c:otherwise>${selectedEmployee.fullName.substring(0,1).toUpperCase()}</c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="sel-info">
                                            <h4>${selectedEmployee.fullName}</h4>
                                            <p><c:choose>
                                                    <c:when test="${not empty selectedEmployee.phoneNumber}">${selectedEmployee.phoneNumber}</c:when>
                                                    <c:otherwise>${selectedEmployee.email}</c:otherwise>
                                                </c:choose></p>
                                        </div>
                                    </div>

                                    <!-- Form quyền -->
                                    <form method="post" action="${pageContext.request.contextPath}/owner/permissions">
                                        <input type="hidden" name="empID" value="${selectedEmployee.employeeID}">
                                        <div class="perm-list">
                                            <c:forEach items="${grantablePermissions}" var="perm">
                                                <c:set var="isChecked" value="${currentPerms.contains(perm[0])}"/>
                                                <c:choose>
                                                    <c:when test='${perm[0] == "inventory.access"}'><c:set var="ic" value="inventory"/><c:set var="fa" value="fa-utensils"/></c:when>
                                                    <c:when test='${perm[0] == "workforce.access"}'><c:set var="ic" value="workforce"/><c:set var="fa" value="fa-users"/></c:when>
                                                    <c:otherwise><c:set var="ic" value="finance"/><c:set var="fa" value="fa-chart-line"/></c:otherwise>
                                                </c:choose>
                                                <label class="perm-item ${isChecked ? 'checked' : ''}" id="item_${perm[0].replace('.','_')}">
                                                    <input type="checkbox" name="perms" value="${perm[0]}" ${isChecked ? 'checked' : ''} onchange="toggleItem(this)">
                                                    <div class="perm-check"><c:if test="${isChecked}"><i class="fas fa-check"></i></c:if></div>
                                                    <div class="perm-icon ${ic}"><i class="fas ${fa}"></i></div>
                                                    <div class="perm-text">
                                                        <div class="perm-key">${perm[0]}</div>
                                                        <div class="perm-desc">${perm[1]}</div>
                                                    </div>
                                                </label>
                                            </c:forEach>
                                        </div>
                                        <div class="action-col">
                                            <button type="submit" class="btn-save"><i class="fas fa-floppy-disk"></i> Lưu phân quyền</button>
                                            <button type="button" class="btn-clear" onclick="clearAll()"><i class="fas fa-xmark"></i> Bỏ tất cả quyền</button>
                                        </div>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <div class="empty-perm">
                                        <i class="fas fa-arrow-left"></i>
                                        <p>Chọn một nhân viên từ danh sách bên trái để bắt đầu phân quyền tạm thời.</p>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <%@ include file="/views/includes/footer.jsp" %>

        <script>
        // ── Phân trang + tìm kiếm ──
            var PAGE_SIZE = 5;
            var currentPage = 1;
            var filteredRows = [];

            function getAllDataRows() {
                //lấy tất cả tr có bên trong và loại bỏ tr có class: no-result-row
                return Array.from(document.querySelectorAll('#staffTbody tr:not(.no-result-row)'));
            }

            function applyFilter(keyword) {
                var kw = (keyword || '').toLowerCase().trim();
                //trả về những dòng thỏa mãn điều kiện lọc
                filteredRows = getAllDataRows().filter(function (row) {
                    if (!kw)
                        return true;
                    var name = row.getAttribute('data-name') || '';
                    var phone = row.getAttribute('data-phone') || '';
                    return name.includes(kw) || phone.includes(kw);
                });
                currentPage = 1;
                renderPage();
            }

            function renderPage() {
                var allRows = getAllDataRows();
                // Ẩn tất cả trước
                allRows.forEach(function (r) {
                    r.style.display = 'none';
                });

                var noResult = document.getElementById('noResultRow');

                if (filteredRows.length === 0) {
                    noResult.style.display = '';
                    document.getElementById('paginationWrap').style.display = 'none';
                    return;
                }
                noResult.style.display = 'none';

                var totalPages = Math.ceil(filteredRows.length / PAGE_SIZE);
                if (currentPage > totalPages)
                    currentPage = totalPages;

                var start = (currentPage - 1) * PAGE_SIZE;
                var end = Math.min(start + PAGE_SIZE, filteredRows.length);
                filteredRows.slice(start, end).forEach(function (r) {
                    r.style.display = '';
                });

                renderPagination(totalPages);
                document.getElementById('pgInfo').textContent =
                        'Trang ' + currentPage + ' / ' + totalPages;
                document.getElementById('paginationWrap').style.display = 'flex';
            }

            function renderPagination(totalPages) {
                var wrap = document.getElementById('paginationBtns');
                wrap.innerHTML = '';

                // Nút Prev
                var prev = document.createElement('button');
                prev.className = 'pg-btn';
                prev.innerHTML = '<i class="fas fa-chevron-left"></i>';
                if (currentPage === 1)
                    prev.setAttribute('disabled', true);
                prev.onclick = function () {
                    goPage(currentPage - 1);
                };
                wrap.appendChild(prev);

                // Số trang
                for (var i = 1; i <= totalPages; i++) {
                    (function (p) {
                        var btn = document.createElement('button');
                        btn.className = 'pg-btn' + (p === currentPage ? ' active' : '');
                        btn.textContent = p;
                        btn.onclick = function () {
                            goPage(p);
                        };
                        wrap.appendChild(btn);
                    })(i);
                }

                // Nút Next
                var next = document.createElement('button');
                next.className = 'pg-btn';
                next.innerHTML = '<i class="fas fa-chevron-right"></i>';
                if (currentPage === totalPages)
                    next.setAttribute('disabled', true);
                next.onclick = function () {
                    goPage(currentPage + 1);
                };
                wrap.appendChild(next);
            }

            function goPage(p) {
                currentPage = p;
                renderPage();
            }

            function filterStaff(keyword) {
                applyFilter(keyword);
            }

        // Init
            window.addEventListener('DOMContentLoaded', function () {
                filteredRows = getAllDataRows();
                renderPage();
            });

        // ── Toggle checkbox quyền ──
            function toggleItem(checkbox) {
                var key = checkbox.value.replace(/\./g, '_');
                var label = document.getElementById('item_' + key);
                var icon = label.querySelector('.perm-check');
                if (checkbox.checked) {
                    label.classList.add('checked');
                    icon.innerHTML = '<i class="fas fa-check"></i>';
                } else {
                    label.classList.remove('checked');
                    icon.innerHTML = '';
                }
            }

            function clearAll() {
                document.querySelectorAll('.perm-list input[type="checkbox"]').forEach(function (cb) {
                    cb.checked = false;
                    toggleItem(cb);
                });
            }
        </script>
    </body>
</html>
