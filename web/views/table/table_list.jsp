<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<style>
    /* Tổng thể chung */
    .vian-container {
        padding: 30px 50px;
        background-color: #FCF9F2;
        color: #4A3B32;
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    }
    .vian-title {
        color: #8B4513;
        font-family: 'Georgia', serif;
        border-bottom: 2px solid #D4A373;
        padding-bottom: 10px;
        margin-bottom: 20px;
    }
    .vian-table {
        width: 100%;
        border-collapse: collapse;
        background: #FFFFFF;
        box-shadow: 0 4px 10px rgba(0,0,0,0.05);
        border-radius: 8px;
        overflow: hidden;
    }
    .vian-table th {
        background-color: #D4A373;
        color: #FFFFFF;
        padding: 15px;
        text-transform: uppercase;
        font-size: 14px;
        letter-spacing: 0.5px;
    }
    .vian-table td {
        padding: 15px;
        border-bottom: 1px solid #F0E6D2;
        text-align: center;
        vertical-align: middle;
    }
    .vian-table tr:hover {
        background-color: #FAF4E8;
    }
    .btn-vian {
        padding: 8px 16px;
        text-decoration: none;
        border-radius: 5px;
        font-weight: 500;
        display: inline-block;
        transition: all 0.3s ease;
        border: none;
        cursor: pointer;
    }
    .btn-add {
        background-color: #556B2F;
        color: white;
        margin-bottom: 20px;
        font-size: 15px;
    }
    .btn-add:hover {
        background-color: #3E4E22;
    }
    .btn-detail {
        background-color: #8B7355;
        color: white;
    }
    .btn-detail:hover {
        background-color: #695640;
    }
    .btn-edit {
        background-color: #E07A5F;
        color: white;
    }
    .btn-edit:hover {
        background-color: #C0644D;
    }
    .status-active {
        color: #556B2F;
        font-weight: bold;
    }
    .status-inactive {
        color: #D9534F;
        font-weight: bold;
    }
    .alert-success {
        color: #556B2F;
        background: #E8F5E9;
        padding: 10px;
        border-radius: 5px;
    }
    .alert-error {
        color: #D9534F;
        background: #FDE8E8;
        padding: 10px;
        border-radius: 5px;
    }
    /* Thanh bộ lọc phong cách Vị An */
    .vian-filter-bar {
        background-color: #FFFFFF;
        padding: 15px 20px;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        margin-bottom: 20px;
        border: 1px solid #E6DEC9;
    }
    .filter-form {
        display: flex;
        flex-wrap: wrap;
        gap: 15px;
        align-items: flex-end;
    }
    .filter-group {
        display: flex;
        flex-direction: column;
        flex: 1;
        min-width: 150px;
    }
    .filter-group label {
        font-size: 13px;
        font-weight: bold;
        color: #5C4033;
        margin-bottom: 5px;
    }
    .filter-input {
        width: 100%;
        padding: 8px 12px;
        border: 1px solid #D4A373;
        border-radius: 4px;
        background-color: #FCF9F2;
        color: #4A3B32;
        font-size: 14px;
        box-sizing: border-box;
    }
    .filter-input:focus {
        outline: none;
        border-color: #8B4513;
        background-color: #FFF;
    }
    .btn-filter {
        background-color: #8B7355;
        color: white;
        padding: 9px 20px;
        border: none;
        border-radius: 4px;
        font-weight: bold;
        cursor: pointer;
        transition: 0.3s;
    }
    .btn-filter:hover {
        background-color: #695640;
    }
    .btn-clear {
        background-color: #E6DEC9;
        color: #4A3B32;
        padding: 9px 15px;
        text-decoration: none;
        border-radius: 4px;
        font-size: 14px;
        text-align: center;
    }
    .btn-clear:hover {
        background-color: #D4C9A8;
    }
    .pagination {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 5px;
        margin: 20px 0;
    }
    .pagination a, .pagination span {
        padding: 6px 14px;
        border: 1px solid #cbd5e1;
        border-radius: 6px;
        text-decoration: none;
        color: #334155;
        font-size: 14px;
        font-weight: 500;
        background-color: #ffffff;
        transition: all 0.2s ease;
    }
    .pagination a:hover {
        background-color: #76493b;
        border-color: #76493b;
        color: #ffffff;
    }
    .pagination .page-info {
        background-color: #f1f5f9;
        color: #1e293b;
    }
    .pagination .page-info b {
        color: #76493b;
    }
    .pagination .disabled {
        color: #94a3b8;
        background-color: #f8fafc;
        pointer-events: none;
    }
</style>

<%@ include file="/views/includes/header.jsp" %>

<div style="display: flex; align-items: stretch; min-height: 80vh; background-color: #FCF9F2;">

    <%@ include file="/views/includes/dashboard.jsp" %>

    <div class="vian-container" style="flex: 1;">

        <h2 class="vian-title">Danh sách Bàn Ăn</h2>

        <c:if test="${not empty param.msg}">
            <div class="alert-success">✓ Thao tác thành công!</div><br>
        </c:if>
        <c:if test="${param.error == 'unauthorized'}">
            <div class="alert-error">⚠ Cảnh báo: Bạn không có quyền thực hiện chức năng này!</div><br>
        </c:if>

        <c:if test="${userRole == 1}">
            <a href="${pageContext.request.contextPath}/manage-table?action=add" class="btn-vian btn-add">
                + Thêm bàn mới
            </a>
        </c:if>
        <div class="vian-filter-bar">
            <form action="${pageContext.request.contextPath}/owner/manage-table" method="GET" class="filter-form" onsubmit="return validateFilterForm();">
                <input type="hidden" name="action" value="list">

                <div class="filter-group">
                    <label>Tên bàn:</label>
                    <input type="text" id="searchName" name="searchName" class="filter-input" 
                           placeholder="Nhập tên bàn..." value="${searchName}" maxlength="30">
                </div>

                <div class="filter-group">
                    <label>Sức chứa:</label>
                    <select name="searchCapacity" class="filter-input">
                        <option value="all" ${searchCapacity == 'all' || empty searchCapacity ? 'selected' : ''}>-- Tất cả số ghế --</option>
                        <option value="2" ${searchCapacity == '2' ? 'selected' : ''}>2 người</option>
                        <option value="4" ${searchCapacity == '4' ? 'selected' : ''}>4 người</option>
                        <option value="6" ${searchCapacity == '6' ? 'selected' : ''}>6 người</option>
                        <option value="8" ${searchCapacity == '8' ? 'selected' : ''}>8 người</option>
                        <option value="10" ${searchCapacity == '10' ? 'selected' : ''}>10 người</option>
                    </select>
                </div>

                <div class="filter-group">
                    <label>Khu vực:</label>
                    <select name="searchArea" class="filter-input">
                        <option value="all" ${searchArea == 'all' || empty searchArea ? 'selected' : ''}>-- Tất cả khu vực --</option>
                        <option value="public" ${searchArea == 'public' ? 'selected' : ''}>Ngoài Sảnh</option>
                        <option value="private" ${searchArea == 'private' ? 'selected' : ''}>Trong Phòng</option>                       
                    </select>
                </div>

                <div class="filter-group">
                    <label>Trạng thái:</label>
                    <select name="searchStatus" class="filter-input">
                        <option value="all" ${searchStatus == 'all' || empty searchStatus ? 'selected' : ''}>-- Tất cả trạng thái --</option>
                        <option value="1" ${searchStatus == '1' ? 'selected' : ''}>Mở bán</option>
                        <option value="0" ${searchStatus == '0' ? 'selected' : ''}>Tạm ngưng</option>
                    </select>
                </div>

                <div style="display: flex; gap: 8px;">
                    <button type="submit" class="btn-filter">Tìm kiếm</button>
                    <a href="${pageContext.request.contextPath}/owner/manage-table" class="btn-clear">Xóa bộ lọc</a>
                </div>
            </form>
        </div>

        <script>
            function validateFilterForm() {
                var nameInput = document.getElementById("searchName").value;
                if (nameInput.length > 30) {
                    alert("Cảnh báo: Từ khóa tìm kiếm không được vượt quá 30 ký tự!");
                    return false; // Hủy sự kiện gửi form dữ liệu lên Server
                }
                return true;
            }
        </script>    

        <table class="vian-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Tên Bàn</th>
                    <th>Sức chứa</th>
                    <th>Khu vực</th>
                    <th>Trạng thái</th>
                    <th>Hành động</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${tableList}" var="t">
                    <tr>
                        <td>${t.tableID}</td>
                        <td><strong>${t.tableName}</strong></td>
                        <td>${t.capacity} người</td>
                        <td>
                            <c:choose>
                                <c:when test="${t.areaType == 'public'}">Ngoài Sảnh</c:when>
                                <c:when test="${t.areaType == 'private'}">Trong Phòng</c:when>
                                <c:otherwise>${t.areaType}</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${t.isActive == 1}"><span class="status-active">Mở bán</span></c:when>
                                <c:otherwise><span class="status-inactive">Tạm ngưng</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <a href="${pageContext.request.contextPath}/manage-table?action=detail&id=${t.tableID}" class="btn-vian btn-detail">Chi tiết</a>

                            <c:if test="${userRole == 1}">
                                <a href="${pageContext.request.contextPath}/manage-table?action=edit&id=${t.tableID}" class="btn-vian btn-edit">Sửa</a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>          
        </table>
        <c:if test="${totalPage > 1}">
            <div class="pagination">
                <c:choose>
                    <c:when test="${currentPage > 1}">
                        <a href="${pageContext.request.contextPath}/manage-table?page=1&searchName=${param.searchName}&searchCapacity=${param.searchCapacity}&searchArea=${param.searchArea}&searchStatus=${param.searchStatus}" title="Về trang đầu">Đầu</a>
                        <a href="${pageContext.request.contextPath}/manage-table?page=${currentPage - 1}&searchName=${param.searchName}&searchCapacity=${param.searchCapacity}&searchArea=${param.searchArea}&searchStatus=${param.searchStatus}" title="Trang trước">Trước</a>
                    </c:when>
                    <c:otherwise>
                        <span class="disabled">Đầu</span>
                        <span class="disabled">Trước</span>
                    </c:otherwise>
                </c:choose>

                <span class="page-info">Trang <b>${currentPage}</b> / ${totalPage}</span>

                <c:choose>
                    <c:when test="${currentPage < totalPage}">
                        <a href="${pageContext.request.contextPath}/manage-table?page=${currentPage + 1}&searchName=${param.searchName}&searchCapacity=${param.searchCapacity}&searchArea=${param.searchArea}&searchStatus=${param.searchStatus}" title="Trang sau">Sau</a>
                        <a href="${pageContext.request.contextPath}/manage-table?page=${totalPage}&searchName=${param.searchName}&searchCapacity=${param.searchCapacity}&searchArea=${param.searchArea}&searchStatus=${param.searchStatus}" title="Đến trang cuối">Cuối</a>
                    </c:when>
                    <c:otherwise>
                        <span class="disabled">Sau</span>
                        <span class="disabled">Cuối</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>

    </div> </div> <%@ include file="/views/includes/footer.jsp" %>