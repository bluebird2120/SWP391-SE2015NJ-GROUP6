<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${mode == 'edit' ? 'Sửa' : 'Thêm'} nhân viên</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: 'Inter', sans-serif; background: #faf6f2; }
        .main { flex: 1; padding: 24px 32px; min-width: 0; }
        .page-title { font-family: 'Playfair Display', serif; color: #76493b; font-size: 1.6rem; margin: 0 0 6px; }
        .breadcrumb { color: #a0714f; font-size: 0.85rem; margin-bottom: 18px; }
        .breadcrumb a { color: #76493b; text-decoration: none; }
        .breadcrumb a:hover { text-decoration: underline; }
        .form-card {
            background: #fff; border: 1px solid #ede0d8; border-radius: 12px;
            padding: 28px; max-width: 760px;
        }
        .row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
        @media (max-width: 700px) { .row { grid-template-columns: 1fr; } }
        .form-group { margin-bottom: 16px; }
        .form-group label { display: block; font-size: 0.85rem; color: #6b4c3b; margin-bottom: 6px; font-weight: 600; }
        .required { color: #dc3545; }
        .form-group input, .form-group select, .form-group textarea {
            width: 100%; padding: 10px 12px;
            border: 1px solid #d7bfa4; border-radius: 8px;
            font-family: inherit; font-size: 0.92rem;
        }
        .form-group input:focus, .form-group select:focus, .form-group textarea:focus {
            outline: none; border-color: #76493b;
            box-shadow: 0 0 0 3px rgba(118, 73, 59, 0.12);
        }
        .form-group .err-msg { color: #dc3545; font-size: 0.8rem; margin-top: 4px; min-height: 14px; }
        .form-group.has-error input, .form-group.has-error select, .form-group.has-error textarea { border-color: #dc3545; }
        .form-actions { display: flex; gap: 10px; margin-top: 8px; }
        .btn { padding: 10px 20px; border-radius: 8px; border: none; cursor: pointer;
               font-size: 0.9rem; font-weight: 600; text-decoration: none;
               display: inline-flex; align-items: center; gap: 6px; }
        .btn-primary { background: #76493b; color: #fff; }
        .btn-primary:hover { background: #5d3a2e; }
        .btn-secondary { background: #ede0d8; color: #76493b; }
        .btn-secondary:hover { background: #d7bfa4; }
        .alert { padding: 11px 14px; border-radius: 8px; margin-bottom: 16px; font-size: 0.88rem; }
        .alert-error { background: #fde7e9; color: #b00020; border: 1px solid #f5c2c7; }
        .current-img { width: 80px; height: 80px; border-radius: 8px; object-fit: cover; margin-top: 6px; border: 1px solid #ede0d8; }
        .help-text { font-size: 0.78rem; color: #8a6e5a; margin-top: 4px; }
    </style>
</head>
<body>
    <%@ include file="/views/includes/header.jsp" %>
    <div style="display: flex;">
        <%@ include file="/views/includes/dashboard.jsp" %>
        <main class="main">

            <div class="breadcrumb">
                <a href="${pageContext.request.contextPath}/owner/staff?action=list">Quản lý nhân viên</a>
                &raquo; <span>${mode == 'edit' ? 'Sửa' : 'Thêm mới'}</span>
            </div>
            <h1 class="page-title">
                <c:choose>
                    <c:when test="${mode == 'edit'}"><i class="fas fa-edit"></i> Sửa nhân viên</c:when>
                    <c:otherwise><i class="fas fa-user-plus"></i> Thêm nhân viên mới</c:otherwise>
                </c:choose>
            </h1>

            <div class="form-card">
                <c:if test="${not empty errors['_global']}">
                    <div class="alert alert-error"><i class="fas fa-exclamation-circle"></i> ${errors['_global']}</div>
                </c:if>


                <form id="staffForm" method="post" enctype="multipart/form-data" autocomplete="off" novalidate
                      action="${pageContext.request.contextPath}/owner/staff?action=${mode == 'edit' ? 'edit' : 'create'}${mode == 'edit' ? '&id=' : ''}${mode == 'edit' ? staff.employeeID : ''}">

                    <input type="hidden" name="action" value="${mode == 'edit' ? 'edit' : 'create'}">
                    <c:if test="${mode == 'edit'}">

                        <input type="hidden" name="id" value="${staff.employeeID}">
                    </c:if>

                    <div class="row">
                        <div class="form-group ${not empty errors['fullName'] ? 'has-error' : ''}">
                            <label>Họ và tên <span class="required">*</span></label>

                            <input type="text" name="fullName" value="${staff.fullName}" maxlength="150" autocomplete="off">
                            <div class="err-msg" id="fullNameError">${errors['fullName']}</div>
                        </div>
                        <div class="form-group ${not empty errors['email'] ? 'has-error' : ''}">
                            <label>Email <span class="required">*</span></label>

                            <input type="email" name="email" value="${staff.email}" placeholder="abc1122@gmail.com" maxlength="150" autocomplete="off">
                            <div class="err-msg" id="emailError">${errors['email']}</div>
                        </div>
                    </div>

                    <%-- Chỉ hiển thị/chọn role khi tạo mới; màn edit bỏ hẳn role để không cập nhật quyền. --%>
                    <c:if test="${mode != 'edit'}">
                        <div class="form-group ${not empty errors['roleID'] ? 'has-error' : ''}">
                            <label>Vai trò <span class="required">*</span></label>

                            <select name="roleID">
                                <option value="2" ${empty staff.roleID || staff.roleID == 2 ? 'selected' : ''}>
                                    Nhân viên phục vụ
                                </option>
                                <option value="3" ${staff.roleID == 3 ? 'selected' : ''}>
                                    Lễ tân
                                </option>
                            </select>
                            <c:if test="${not empty errors['roleID']}">
                                <div class="err-msg">${errors['roleID']}</div>
                            </c:if>
                        </div>
                    </c:if>

                    <div class="row">
                        <div class="form-group ${not empty errors['phoneNumber'] ? 'has-error' : ''}">
                            <label>Số điện thoại <span class="required">*</span></label>

                            <input type="text" name="phoneNumber" value="${staff.phoneNumber}" maxlength="20" placeholder="0901234567" autocomplete="off">
                            <div class="err-msg" id="phoneNumberError">${errors['phoneNumber']}</div>
                        </div>
                        <div class="form-group ${not empty errors['dob'] ? 'has-error' : ''}">
                            <label>Ngày sinh <span class="required">*</span></label>

                            <input type="date" name="dob" value="${dobValue != null ? dobValue : staff.dob}" required>
                            <div class="err-msg" id="dobError">${errors['dob']}</div>
                        </div>
                    </div>

                    <c:if test="${mode != 'edit'}">
                        <div class="form-group ${not empty errors['password'] ? 'has-error' : ''}">
                            <label>Mật khẩu <span class="required">*</span></label>

                            <input type="password" name="password" minlength="6" autocomplete="new-password">
                            <div class="help-text">Tối thiểu 6 ký tự. Nhân viên sẽ phải đổi mật khẩu trong lần đăng nhập đầu tiên.</div>
                            <div class="err-msg" id="passwordError">${errors['password']}</div>
                        </div>
                    </c:if>

                    <div class="form-group">
                        <label>Ảnh đại diện</label>

                        <input type="file" id="staffImage" name="image" accept=".jpg,.jpeg,.png,image/jpeg,image/png">
                        <div class="help-text">Không bắt buộc. JPG, PNG — tối đa 2MB.</div>
                        <div class="err-msg" id="imageError">${errors['image']}</div>
                        <c:if test="${mode == 'edit' && not empty staff.image}">
                            <img src="${pageContext.request.contextPath}/${staff.image}" class="current-img" alt="Ảnh hiện tại">
                        </c:if>
                    </div>

                    <div class="form-group ${not empty errors['address'] ? 'has-error' : ''}">
                        <label>Địa chỉ <span class="required">*</span></label>

                        <textarea name="address" rows="2" maxlength="255" required>${staff.address}</textarea>
                        <div class="err-msg" id="addressError">${errors['address']}</div>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-save"></i> ${mode == 'edit' ? 'Cập nhật' : 'Tạo mới'}
                        </button>
                        <a href="${pageContext.request.contextPath}/owner/staff?action=list" class="btn btn-secondary">
                            <i class="fas fa-arrow-left"></i> Quay lại
                        </a>
                    </div>
                </form>
            </div>
        </main>
    </div>
    <%@ include file="/views/includes/footer.jsp" %>
</body>
</html>
