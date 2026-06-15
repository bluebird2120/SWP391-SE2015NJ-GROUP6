<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Shift Templates</title>
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
        .btn-danger { background: #dc3545; color: #fff; }
        .btn-disabled { background: #e9ecef; color: #6c757d; cursor: not-allowed; }
        .table-card { background: #fff; border: 1px solid #ede0d8; border-radius: 12px; overflow: hidden; }
        table { width: 100%; border-collapse: collapse; }
        th { background: #faf6f2; padding: 12px; text-align: left; font-size: 0.8rem; color: #76493b; text-transform: uppercase; letter-spacing: 0.04em; border-bottom: 1px solid #ede0d8; }
        td { padding: 12px; border-bottom: 1px solid #f5ece4; font-size: 0.9rem; color: #4a3528; }
        tr:last-child td { border-bottom: none; }
        tr:hover { background: #faf6f2; }
        .alert { padding: 11px 14px; border-radius: 8px; margin-bottom: 14px; font-size: 0.88rem; }
        .alert-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .alert-warn { background: #fff3cd; color: #856404; border: 1px solid #ffeeba; }
        .actions { display: flex; gap: 6px; }
        form.inline { display: inline; }
    </style>
</head>
<body>
    <%@ include file="/views/includes/header.jsp" %>
    <div style="display:flex;">
        <%@ include file="/views/includes/dashboard.jsp" %>
        <main class="main">
            <div class="page-head">
                <div>
                    <h1 class="page-title">Shift Templates</h1>
                    <p class="page-sub">Quản lý các ca làm việc cố định</p>
                </div>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/owner/shift-templates?action=create">
                    <i class="fas fa-plus"></i> Add Template
                </a>
            </div>

            <c:if test="${param.msg == 'created'}"><div class="alert alert-success">Tạo template thành công.</div></c:if>
            <c:if test="${param.msg == 'updated'}"><div class="alert alert-success">Cập nhật thành công.</div></c:if>
            <c:if test="${param.msg == 'deleted'}"><div class="alert alert-success">Đã xoá template.</div></c:if>
            <c:if test="${param.msg == 'template_in_use'}"><div class="alert alert-warn">Template đã được sử dụng — không thể xoá.</div></c:if>

            <div class="table-card">
                <table>
                    <thead>
                        <tr><th>ID</th><th>Tên ca</th><th>Bắt đầu</th><th>Kết thúc</th><th>Thao tác</th></tr>
                    </thead>
                    <tbody>
                        <c:forEach var="t" items="${templates}">
                            <tr>
                                <td>${t.templateID}</td>
                                <td>${t.shiftName}</td>
                                <td><fmt:formatDate value="${t.startTime}" pattern="HH:mm"/></td>
                                <td><fmt:formatDate value="${t.endTime}" pattern="HH:mm"/></td>
                                <td class="actions">
                                    <a class="btn btn-sm btn-edit" href="${pageContext.request.contextPath}/owner/shift-templates?action=edit&id=${t.templateID}">
                                        <i class="fas fa-edit"></i> Sửa
                                    </a>
                                    <form class="inline" method="post" action="${pageContext.request.contextPath}/owner/shift-templates"
                                          onsubmit="return confirm('Xoá template này?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${t.templateID}">
                                        <button type="submit" class="btn btn-sm btn-danger"><i class="fas fa-trash"></i> Xoá</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty templates}">
                            <tr><td colspan="5" style="text-align:center; padding:24px; color:#8a6e5a;">Chưa có template nào.</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </main>
    </div>
    <%@ include file="/views/includes/footer.jsp" %>
</body>
</html>
