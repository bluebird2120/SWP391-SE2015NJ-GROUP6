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

        /* Custom confirmation modal style */
        .custom-confirm-modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.4);
            z-index: 100000;
            align-items: center;
            justify-content: center;
            opacity: 0;
            transition: opacity 0.2s ease;
        }
        .custom-confirm-modal.show {
            display: flex;
            opacity: 1;
        }
        .custom-confirm-content {
            background-color: #fff;
            border-radius: 12px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
            padding: 24px;
            width: 100%;
            max-width: 400px;
            text-align: center;
            transform: translateY(-20px);
            transition: transform 0.2s ease;
            border: 1px solid #ede0d8;
        }
        .custom-confirm-modal.show .custom-confirm-content {
            transform: translateY(0);
        }
        .custom-confirm-message {
            font-size: 15px;
            color: #4A3B32;
            margin-bottom: 24px;
            font-weight: 500;
            line-height: 1.5;
        }
        .custom-confirm-buttons {
            display: flex;
            justify-content: center;
            gap: 12px;
        }
        .custom-confirm-btn {
            padding: 10px 20px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            border: none;
            transition: all 0.2s ease;
        }
        .custom-confirm-btn-cancel {
            background-color: #f1ebd9;
            color: #76493b;
        }
        .custom-confirm-btn-cancel:hover {
            background-color: #e5dac1;
        }
        .custom-confirm-btn-ok {
            background-color: #76493b;
            color: #fff;
        }
        .custom-confirm-btn-ok:hover {
            background-color: #5f3a2f;
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
                    <h1 class="page-title">Shift Templates</h1>
                    <p class="page-sub">Quản lý các ca làm việc cố định</p>
                </div>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/owner/shift-templates?action=create">
                    <i class="fas fa-plus"></i> Add Template
                </a>
            </div>

            <!-- Local Custom confirmation modal -->
            <div id="localConfirmModal" class="custom-confirm-modal">
                <div class="custom-confirm-content">
                    <div id="localConfirmMessage" class="custom-confirm-message"></div>
                    <div class="custom-confirm-buttons">
                        <button id="localConfirmCancelBtn" class="custom-confirm-btn custom-confirm-btn-cancel">Huỷ</button>
                        <button id="localConfirmOkBtn" class="custom-confirm-btn custom-confirm-btn-ok">Đồng ý</button>
                    </div>
                </div>
            </div>

            <!-- Local Custom confirmation modal -->
            <div id="localConfirmModal" class="custom-confirm-modal">
                <div class="custom-confirm-content">
                    <div id="localConfirmMessage" class="custom-confirm-message"></div>
                    <div class="custom-confirm-buttons">
                        <button id="localConfirmCancelBtn" class="custom-confirm-btn custom-confirm-btn-cancel">Huỷ</button>
                        <button id="localConfirmOkBtn" class="custom-confirm-btn custom-confirm-btn-ok">Đồng ý</button>
                    </div>
                </div>
            </div>

            <c:if test="${param.msg == 'created'}"><div class="alert alert-success">Tạo template thành công.</div></c:if>
            <c:if test="${param.msg == 'updated'}"><div class="alert alert-success">Cập nhật thành công.</div></c:if>
            <c:if test="${param.msg == 'deleted'}"><div class="alert alert-success">Đã xoá template.</div></c:if>
            <c:if test="${param.msg == 'template_in_use'}"><div class="alert alert-warn">Template đã được sử dụng — không thể xoá.</div></c:if>

            <div class="table-card">
                <table>
                    <thead>
                        <tr><th>#</th><th>Tên ca</th><th>Bắt đầu</th><th>Kết thúc</th><th>Thao tác</th></tr>
                    </thead>
                    <tbody>
                        <c:forEach var="t" items="${templates}" varStatus="st">
                            <tr>
                                <td>${st.count}</td>
                                <td>${t.shiftName}</td>
                                <td><fmt:formatDate value="${t.startTime}" pattern="HH:mm"/></td>
                                <td><fmt:formatDate value="${t.endTime}" pattern="HH:mm"/></td>
                                <td class="actions">
                                    <a class="btn btn-sm btn-edit" href="${pageContext.request.contextPath}/owner/shift-templates?action=edit&id=${t.templateID}">
                                        Sửa
                                    </a>
                                    <form class="inline" method="post" action="${pageContext.request.contextPath}/owner/shift-templates"
                                          onsubmit="return showCustomConfirm(this, event, 'Xoá template này?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${t.templateID}">
                                        <button type="submit" class="btn btn-sm btn-danger">Xoá</button>
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
    <script>
        var activeConfirmForm = null;

        function showInlineConfirm(form, event, message) {
            if (event) {
                event.preventDefault();
            }
            activeConfirmForm = form;
            
            var modal = document.getElementById('localConfirmModal');
            var msgEl = document.getElementById('localConfirmMessage');
            if (modal && msgEl) {
                msgEl.textContent = message;
                modal.classList.add('show');
            }
            return false;
        }

        document.addEventListener('DOMContentLoaded', function() {
            var cancelBtn = document.getElementById('localConfirmCancelBtn');
            var okBtn = document.getElementById('localConfirmOkBtn');
            var modal = document.getElementById('localConfirmModal');

            if (cancelBtn) {
                cancelBtn.addEventListener('click', function() {
                    if (modal) modal.classList.remove('show');
                    activeConfirmForm = null;
                });
            }

            if (okBtn) {
                okBtn.addEventListener('click', function() {
                    if (modal) modal.classList.remove('show');
                    if (activeConfirmForm) {
                        var form = activeConfirmForm;
                        form.submit();
                    }
                    activeConfirmForm = null;
                });
            }
        });
    </script>
    <script>
        var activeConfirmForm = null;

        function showCustomConfirm(form, event, message) {
            if (event) {
                event.preventDefault();
            }
            activeConfirmForm = form;
            
            var modal = document.getElementById('localConfirmModal');
            var msgEl = document.getElementById('localConfirmMessage');
            if (modal && msgEl) {
                msgEl.textContent = message;
                modal.classList.add('show');
            }
            return false;
        }

        document.addEventListener('DOMContentLoaded', function() {
            var cancelBtn = document.getElementById('localConfirmCancelBtn');
            var okBtn = document.getElementById('localConfirmOkBtn');
            var modal = document.getElementById('localConfirmModal');

            if (cancelBtn) {
                cancelBtn.addEventListener('click', function() {
                    if (modal) modal.classList.remove('show');
                    activeConfirmForm = null;
                });
            }

            if (okBtn) {
                okBtn.addEventListener('click', function() {
                    if (modal) modal.classList.remove('show');
                    if (activeConfirmForm) {
                        var form = activeConfirmForm;
                        form.submit();
                    }
                    activeConfirmForm = null;
                });
            }
        });
    </script>
</body>
</html>
