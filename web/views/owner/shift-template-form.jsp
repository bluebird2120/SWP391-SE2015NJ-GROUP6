<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><c:choose><c:when test="${mode == 'edit'}">Edit</c:when><c:otherwise>Create</c:otherwise></c:choose> Shift Template</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin:0; font-family:'Inter',sans-serif; background:#faf6f2; }
        .main { flex:1; padding:24px 32px; }
        .page-title { font-family:'Playfair Display',serif; color:#76493b; font-size:1.6rem; margin:0 0 18px; }
        .form-card { background:#fff; border:1px solid #ede0d8; border-radius:12px; padding:24px; max-width:560px; }
        .form-group { margin-bottom:16px; }
        .form-group label { display:block; font-size:0.85rem; font-weight:600; color:#6b4c3b; margin-bottom:6px; }
        .form-group input { width:100%; padding:10px 14px; border:1px solid #d7bfa4; border-radius:8px; font-family:inherit; font-size:0.95rem; }
        .form-group input:focus { outline:none; border-color:#76493b; }
        .form-group input:disabled { background:#f5f0eb; color:#8a6e5a; }
        .err { color:#dc3545; font-size:0.82rem; margin-top:4px; }
        .alert { padding:11px 14px; border-radius:8px; margin-bottom:14px; font-size:0.88rem; }
        .alert-error { background:#f8d7da; color:#721c24; border:1px solid #f5c2c7; }
        .alert-warn { background:#fff3cd; color:#856404; border:1px solid #ffeeba; }
        .btn { padding:10px 18px; border-radius:8px; border:none; font-weight:600; cursor:pointer; text-decoration:none; display:inline-flex; gap:6px; align-items:center; }
        .btn-primary { background:#76493b; color:#fff; }
        .btn-cancel { background:#e9ecef; color:#444; }
    </style>
</head>
<body>
    <%@ include file="/views/includes/header.jsp" %>
    <div style="display:flex;">
        <%@ include file="/views/includes/dashboard.jsp" %>
        <main class="main">
            <h1 class="page-title">
                <c:choose><c:when test="${mode == 'edit'}">Edit Template</c:when><c:otherwise>New Template</c:otherwise></c:choose>
            </h1>

            <c:if test="${not empty errors['_global']}"><div class="alert alert-error">${errors['_global']}</div></c:if>
            <c:if test="${mode == 'edit' && usedCount > 0}">
                <div class="alert alert-warn">
                    Template đã được dùng trong ${usedCount} ca điểm danh — chỉ cho đổi <b>tên</b>, không cho đổi giờ.
                </div>
            </c:if>

            <div class="form-card">
                <form method="post" action="${pageContext.request.contextPath}/owner/shift-templates">
                    <input type="hidden" name="action" value="${mode}">
                    <c:if test="${mode == 'edit'}">
                        <input type="hidden" name="id" value="${template.templateID}">
                    </c:if>

                    <div class="form-group">
                        <label>Tên ca *</label>
                        <input type="text" name="shiftName" value="${template.shiftName}" maxlength="100" required>
                        <c:if test="${not empty errors.shiftName}"><div class="err">${errors.shiftName}</div></c:if>
                    </div>

                    <div class="form-group">
                        <label>Giờ bắt đầu *</label>
                        <input type="time" name="startTime"
                               value="<fmt:formatDate value='${template.startTime}' pattern='HH:mm'/>"
                               <c:if test="${mode == 'edit' && usedCount > 0}">disabled</c:if>>
                        <c:if test="${not empty errors.startTime}"><div class="err">${errors.startTime}</div></c:if>
                    </div>

                    <div class="form-group">
                        <label>Giờ kết thúc *</label>
                        <input type="time" name="endTime"
                               value="<fmt:formatDate value='${template.endTime}' pattern='HH:mm'/>"
                               <c:if test="${mode == 'edit' && usedCount > 0}">disabled</c:if>>
                        <c:if test="${not empty errors.endTime}"><div class="err">${errors.endTime}</div></c:if>
                    </div>

                    <div style="display:flex; gap:10px; margin-top:20px;">
                        <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Lưu</button>
                        <a class="btn btn-cancel" href="${pageContext.request.contextPath}/owner/shift-templates">Huỷ</a>
                    </div>
                </form>
            </div>
        </main>
    </div>
    <%@ include file="/views/includes/footer.jsp" %>
</body>
</html>
