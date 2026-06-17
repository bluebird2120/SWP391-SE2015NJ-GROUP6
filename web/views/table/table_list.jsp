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
                                <c:when test="${t.areaType == 'public'}">Sảnh chung</c:when>
                                <c:when test="${t.areaType == 'private'}">Phòng VIP</c:when>
                                <c:when test="${t.areaType == 'outdoor'}">Ngoài trời</c:when>
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

    </div> </div> <%@ include file="/views/includes/footer.jsp" %>