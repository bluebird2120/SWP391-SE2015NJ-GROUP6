<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Trang cá nhân – Vị An Restaurant</title>
        <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;600;700&family=Nunito:wght@300;400;600;700&display=swap" rel="stylesheet">
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

            .page-body {
                flex: 1;
                display: flex;
                justify-content: center;
                padding: 48px 20px;
            }

            .profile-wrapper {
                width: 100%;
                max-width: 780px;
                display: flex;
                flex-direction: column;
                gap: 24px;
            }

            /* ── CARD ── */
            .card {
                background: #fff;
                border-radius: 16px;
                box-shadow: 0 4px 24px rgba(90,45,12,0.08);
                overflow: hidden;
            }

            .card-header {
                background: #76493b;
                color: #f0dcc2;
                padding: 16px 28px;
                display: flex;
                align-items: center;
                justify-content: space-between;
                font-family: 'Playfair Display', serif;
                font-size: 1.05rem;
                font-weight: 600;
            }

            .card-header-left {
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .card-body {
                padding: 28px;
            }

            /* ── AVATAR ── */
            .avatar-block {
                display: flex;
                align-items: center;
                gap: 24px;
                margin-bottom: 28px;
                padding-bottom: 24px;
                border-bottom: 1px solid #f0e6dc;
            }

            .avatar-circle {
                width: 80px;
                height: 80px;
                border-radius: 50%;
                background: #76493b;
                color: #f0dcc2;
                font-family: 'Playfair Display', serif;
                font-size: 2rem;
                font-weight: 700;
                display: flex;
                align-items: center;
                justify-content: center;
                flex-shrink: 0;
                overflow: hidden;
            }

            .avatar-circle img {
                width: 80px;
                height: 80px;
                object-fit: cover;
            }

            /* avatar preview khi chọn ảnh mới */
            #avatar-preview {
                width: 80px;
                height: 80px;
                border-radius: 50%;
                object-fit: cover;
                display: none;
                flex-shrink: 0;
                border: 3px solid #76493b;
            }

            .avatar-info h3 {
                font-family: 'Playfair Display', serif;
                font-size: 1.2rem;
                color: #3d2318;
                margin-bottom: 6px;
            }

            .badge-role {
                display: inline-block;
                font-size: 0.72rem;
                font-weight: 700;
                padding: 3px 10px;
                border-radius: 20px;
                text-transform: uppercase;
                letter-spacing: 0.05em;
            }
            .badge-customer {
                background: #e8f4e8;
                color: #2d7a2d;
            }
            .badge-owner    {
                background: #fdf0e0;
                color: #a05c00;
            }
            .badge-staff    {
                background: #e8eef8;
                color: #1a4080;
            }

            /* ── INFO GRID (view mode) ── */
            .info-grid {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 18px;
            }

            .info-item {
                display: flex;
                flex-direction: column;
                gap: 4px;
            }
            .info-item.full-width {
                grid-column: 1 / -1;
            }

            .info-label {
                font-size: 0.72rem;
                font-weight: 700;
                text-transform: uppercase;
                letter-spacing: 0.07em;
                color: #b09080;
            }

            .info-value {
                font-size: 0.95rem;
                color: #3d2318;
                font-weight: 600;
                padding: 10px 14px;
                background: #fdf6f0;
                border-radius: 8px;
                border: 1px solid #ede0d8;
            }

            .info-value.empty {
                color: #bbb;
                font-style: italic;
                font-weight: 400;
            }

            /* ── FORM GRID (edit mode) ── */
            .form-grid {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 18px;
            }

            .form-group {
                display: flex;
                flex-direction: column;
                gap: 6px;
            }
            .form-group.full-width {
                grid-column: 1 / -1;
            }

            .form-label {
                font-size: 0.72rem;
                font-weight: 700;
                text-transform: uppercase;
                letter-spacing: 0.07em;
                color: #76493b;
            }

            .form-control {
                font-family: 'Nunito', sans-serif;
                font-size: 0.95rem;
                font-weight: 600;
                color: #3d2318;
                padding: 10px 14px;
                background: #fff;
                border: 1.5px solid #d4b9ae;
                border-radius: 8px;
                outline: none;
                transition: border-color 0.2s, box-shadow 0.2s;
                width: 100%;
            }

            .form-control:focus {
                border-color: #76493b;
                box-shadow: 0 0 0 3px rgba(118,73,59,0.12);
            }

            .form-control.error-field {
                border-color: #c0392b;
            }

            .error-msg {
                font-size: 0.78rem;
                color: #c0392b;
                font-weight: 600;
            }

            /* ── READONLY (không edit được) ── */
            .form-control-readonly {
                font-size: 0.95rem;
                color: #3d2318;
                font-weight: 600;
                padding: 10px 14px;
                background: #f5f0ed;
                border: 1.5px solid #ede0d8;
                border-radius: 8px;
                display: flex;
                align-items: center;
                gap: 8px;
            }

            .readonly-note {
                font-size: 0.72rem;
                color: #b09080;
                font-style: italic;
                margin-top: 3px;
            }

            /* ── IMAGE UPLOAD ── */
            .image-upload-wrap {
                display: flex;
                align-items: center;
                gap: 16px;
                flex-wrap: wrap;
            }

            .upload-btn-label {
                display: inline-flex;
                align-items: center;
                gap: 8px;
                padding: 9px 18px;
                background: #f0e6dc;
                color: #76493b;
                border-radius: 8px;
                font-size: 0.85rem;
                font-weight: 700;
                cursor: pointer;
                transition: background 0.2s;
            }

            .upload-btn-label:hover {
                background: #e0d0c4;
            }
            #image {
                display: none;
            }

            .upload-hint {
                font-size: 0.76rem;
                color: #b09080;
            }

            /* ── BUTTONS ── */
            .btn {
                display: inline-flex;
                align-items: center;
                gap: 8px;
                padding: 10px 22px;
                border-radius: 10px;
                font-family: 'Nunito', sans-serif;
                font-size: 0.88rem;
                font-weight: 700;
                text-decoration: none;
                cursor: pointer;
                border: none;
                transition: 0.2s;
            }

            .btn-edit {
                background: rgba(240,220,194,0.25);
                color: #f0dcc2;
                border: 1.5px solid rgba(240,220,194,0.5);
                font-size: 0.82rem;
                padding: 7px 16px;
            }
            .btn-edit:hover {
                background: rgba(240,220,194,0.4);
            }

            .btn-primary {
                background: #76493b;
                color: #fff;
            }
            .btn-primary:hover {
                background: #5a3329;
            }

            .btn-cancel {
                background: transparent;
                color: #76493b;
                border: 1.5px solid #76493b;
            }
            .btn-cancel:hover {
                background: #76493b;
                color: #fff;
            }

            .btn-outline {
                background: transparent;
                color: #76493b;
                border: 1.5px solid #76493b;
            }
            .btn-outline:hover {
                background: #76493b;
                color: #fff;
            }

            .action-row {
                display: flex;
                gap: 12px;
                flex-wrap: wrap;
            }

            .form-actions {
                display: flex;
                gap: 12px;
                margin-top: 24px;
                flex-wrap: wrap;
            }

            /* ── ALERT ── */
            .alert {
                padding: 12px 16px;
                border-radius: 10px;
                font-size: 0.88rem;
                display: flex;
                align-items: center;
                gap: 10px;
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

            /* ── SECTION SEPARATOR ── */
            .section-divider {
                height: 1px;
                background: #f0e6dc;
                margin: 24px 0;
            }

            @media (max-width: 600px) {
                .info-grid, .form-grid {
                    grid-template-columns: 1fr;
                }
                .avatar-block {
                    flex-direction: column;
                    text-align: center;
                }
                .action-row, .form-actions {
                    flex-direction: column;
                }
                .btn {
                    justify-content: center;
                }
                .image-upload-wrap {
                    flex-direction: column;
                    align-items: flex-start;
                }
            }
        </style>
    </head>
    <body>

        <%@ include file="/views/includes/header.jsp" %>

        <div class="page-body">
            <div class="profile-wrapper">

                <%-- Alert --%>
                <c:if test="${not empty successMessage}">
                    <div class="alert alert-success">
                        <i class="fas fa-circle-check"></i> ${successMessage}
                    </div>
                </c:if>
                <c:if test="${not empty errors['_global']}">
                    <div class="alert alert-error">
                        <i class="fas fa-circle-exclamation"></i> ${errors['_global']}
                    </div>
                </c:if>

                <%-- ══════════════════════════════
                     CUSTOMER PROFILE
                ══════════════════════════════ --%>
                <c:if test="${sessionScope.customer != null}">

                    <%-- Xác định edit mode --%>
                    <c:set var="isEdit" value="${not empty editMode or param.edit == 'true'}"/>

                    <div class="card">
                        <div class="card-header">
                            <div class="card-header-left">
                                <i class="fas fa-user"></i> Thông tin cá nhân
                            </div>
                            <c:if test="${not isEdit}">
                                <a href="${pageContext.request.contextPath}/profile?edit=true" class="btn btn-edit">
                                    <i class="fas fa-pen"></i> Chỉnh sửa
                                </a>
                            </c:if>
                        </div>
                        <div class="card-body">

                            <%-- Avatar --%>
                            <div class="avatar-block">
                                <div class="avatar-circle" id="avatar-current">
                                    <c:choose>
                                        <c:when test="${not empty sessionScope.customer.image}">
                                            <img id="current-img" src="${pageContext.request.contextPath}/${sessionScope.customer.image}" alt="avatar">
                                        </c:when>
                                        <c:otherwise>
                                            <span id="avatar-initial">${sessionScope.customer.userName.substring(0,1).toUpperCase()}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <%-- Preview ảnh mới (chỉ hiển thị khi edit và người dùng chọn file) --%>
                                <c:if test="${isEdit}">
                                    <img id="avatar-preview" src="#" alt="preview">
                                </c:if>
                                <div class="avatar-info">
                                    <h3>${sessionScope.customer.userName}</h3>
                                    <span class="badge-role badge-customer">
                                        <i class="fas fa-user-check"></i> Khách hàng
                                    </span>
                                    <c:if test="${sessionScope.customer.loginProvider == 'google'}">
                                        &nbsp;<span class="badge-role" style="background:#fce8e8;color:#c0392b;">
                                            <i class="fab fa-google"></i> Google
                                        </span>
                                    </c:if>
                                </div>
                            </div>

                            <%-- VIEW MODE --%>
                            <c:if test="${not isEdit}">
                                <div class="info-grid">
                                    <div class="info-item">
                                        <span class="info-label"><i class="fas fa-user"></i> Tên hiển thị</span>
                                        <span class="info-value">${sessionScope.customer.userName}</span>
                                    </div>
                                    <div class="info-item">
                                        <span class="info-label"><i class="fas fa-envelope"></i> Email</span>
                                        <span class="info-value ${empty sessionScope.customer.email ? 'empty' : ''}">
                                            <c:choose>
                                                <c:when test="${not empty sessionScope.customer.email}">${sessionScope.customer.email}</c:when>
                                                <c:otherwise>Chưa cập nhật</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div class="info-item">
                                        <span class="info-label"><i class="fas fa-phone"></i> Số điện thoại</span>
                                        <span class="info-value ${empty sessionScope.customer.phoneNumber ? 'empty' : ''}">
                                            <c:choose>
                                                <c:when test="${not empty sessionScope.customer.phoneNumber}">${sessionScope.customer.phoneNumber}</c:when>
                                                <c:otherwise>Chưa cập nhật</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div class="info-item">
                                        <span class="info-label"><i class="fas fa-birthday-cake"></i> Ngày sinh</span>
                                        <span class="info-value ${empty sessionScope.customer.dob ? 'empty' : ''}">
                                            <c:choose>
                                                <c:when test="${not empty sessionScope.customer.dob}">
                                                    <fmt:formatDate value="${sessionScope.customer.dob}" pattern="dd/MM/yyyy"/>
                                                </c:when>
                                                <c:otherwise>Chưa cập nhật</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div class="info-item full-width">
                                        <span class="info-label"><i class="fas fa-map-marker-alt"></i> Địa chỉ</span>
                                        <span class="info-value ${empty sessionScope.customer.address ? 'empty' : ''}">
                                            <c:choose>
                                                <c:when test="${not empty sessionScope.customer.address}">${sessionScope.customer.address}</c:when>
                                                <c:otherwise>Chưa cập nhật</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div class="info-item">
                                        <span class="info-label"><i class="fas fa-calendar"></i> Ngày tham gia</span>
                                        <span class="info-value">${sessionScope.customer.createdAt}</span>
                                    </div>
                                </div>
                            </c:if>

                            <%-- EDIT MODE --%>
                            <c:if test="${isEdit}">
                                <form action="${pageContext.request.contextPath}/profile" method="post" enctype="multipart/form-data">
                                    <div class="form-grid">

                                        <%-- Tên hiển thị — CÓ THỂ SỬA --%>
                                        <div class="form-group">
                                            <label class="form-label" for="userName">
                                                <i class="fas fa-user"></i> Tên hiển thị <span style="color:#c0392b;">*</span>
                                            </label>
                                            <input type="text" id="userName" name="userName"
                                                   class="form-control ${not empty errors['userName'] ? 'error-field' : ''}"
                                                   value="${not empty param.userName ? param.userName : sessionScope.customer.userName}"
                                                   maxlength="50" required>
                                            <c:if test="${not empty errors['userName']}">
                                                <span class="error-msg"><i class="fas fa-triangle-exclamation"></i> ${errors['userName']}</span>
                                            </c:if>
                                        </div>

                                        <%-- Email — KHÔNG SỬA ĐƯỢC --%>
                                        <div class="form-group">
                                            <label class="form-label"><i class="fas fa-envelope"></i> Email</label>
                                            <div class="form-control-readonly">
                                                <c:choose>
                                                    <c:when test="${not empty sessionScope.customer.email}">${sessionScope.customer.email}</c:when>
                                                    <c:otherwise><span style="color:#bbb;font-style:italic;">Chưa cập nhật</span></c:otherwise>
                                                </c:choose>
                                            </div>
                                            <span class="readonly-note"><i class="fas fa-lock"></i> Email không thể thay đổi tại đây</span>
                                        </div>

                                        <%-- Số điện thoại — KHÔNG SỬA ĐƯỢC --%>
                                        <div class="form-group">
                                            <label class="form-label"><i class="fas fa-phone"></i> Số điện thoại</label>
                                            <div class="form-control-readonly">
                                                <c:choose>
                                                    <c:when test="${not empty sessionScope.customer.phoneNumber}">${sessionScope.customer.phoneNumber}</c:when>
                                                    <c:otherwise><span style="color:#bbb;font-style:italic;">Chưa cập nhật</span></c:otherwise>
                                                </c:choose>
                                            </div>
                                            <span class="readonly-note"><i class="fas fa-lock"></i> Số điện thoại không thể thay đổi tại đây</span>
                                        </div>

                                        <%-- Ngày sinh — CÓ THỂ SỬA, KHÔNG BẮT BUỘC --%>
                                        <div class="form-group">
                                            <label class="form-label" for="dob">
                                                <i class="fas fa-birthday-cake"></i> Ngày sinh
                                            </label>
                                            <input type="date" id="dob" name="dob"
                                                   class="form-control ${not empty errors['dob'] ? 'error-field' : ''}"
                                                   value="${not empty param.dob ? param.dob : sessionScope.customer.dob}"
                                                   max="<fmt:formatDate value='<%= new java.util.Date() %>' pattern='yyyy-MM-dd'/>">
                                            <c:if test="${not empty errors['dob']}">
                                                <span class="error-msg"><i class="fas fa-triangle-exclamation"></i> ${errors['dob']}</span>
                                            </c:if>
                                        </div>

                                        <%-- Ngày tham gia — KHÔNG SỬA ĐƯỢC --%>
                                        <div class="form-group">
                                            <label class="form-label"><i class="fas fa-calendar"></i> Ngày tham gia</label>
                                            <div class="form-control-readonly">${sessionScope.customer.createdAt}</div>
                                        </div>

                                        <%-- Địa chỉ — CÓ THỂ SỬA, KHÔNG BẮT BUỘC --%>
                                        <div class="form-group full-width">
                                            <label class="form-label" for="address">
                                                <i class="fas fa-map-marker-alt"></i> Địa chỉ
                                            </label>
                                            <input type="text" id="address" name="address"
                                                   class="form-control ${not empty errors['address'] ? 'error-field' : ''}"
                                                   value="${not empty param.address ? param.address : sessionScope.customer.address}"
                                                   maxlength="255"
                                                   placeholder="Nhập địa chỉ của bạn ...">
                                            <c:if test="${not empty errors['address']}">
                                                <span class="error-msg"><i class="fas fa-triangle-exclamation"></i> ${errors['address']}</span>
                                            </c:if>
                                        </div>

                                        <%-- Ảnh đại diện — CÓ THỂ SỬA, KHÔNG BẮT BUỘC --%>
                                        <div class="form-group full-width">
                                            <label class="form-label"><i class="fas fa-camera"></i> Ảnh đại diện</label>
                                            <div class="image-upload-wrap">
                                                <label for="image" class="upload-btn-label">
                                                    <i class="fas fa-upload"></i> Chọn ảnh mới
                                                </label>
                                                <input type="file" id="image" name="image" accept="image/jpg,image/jpeg,image/png,image/webp">
                                                <span class="upload-hint">JPG, PNG, WEBP — tối đa 2MB. Không bắt buộc.</span>
                                            </div>

                                            <c:if test="${not empty sessionScope.customer.image}">
                                                <div style="margin-top:10px;">
                                                    <input type="checkbox"
                                                           id="removeImage"
                                                           name="removeImage"
                                                           value="true">

                                                    <label for="removeImage">
                                                        Xóa ảnh hiện tại
                                                    </label>
                                                </div>
                                            </c:if>

                                            <c:if test="${not empty errors['image']}">
                                                <span class="error-msg"><i class="fas fa-triangle-exclamation"></i> ${errors['image']}</span>
                                            </c:if>
                                        </div>

                                    </div>

                                    <div class="form-actions">
                                        <button type="submit" class="btn btn-primary">
                                            <i class="fas fa-floppy-disk"></i> Lưu thay đổi
                                        </button>
                                        <a href="${pageContext.request.contextPath}/profile" class="btn btn-cancel">
                                            <i class="fas fa-xmark"></i> Hủy
                                        </a>
                                    </div>
                                </form>
                            </c:if>

                        </div>
                    </div>

                    <%-- Actions cho Customer --%>
                    <div class="card">
                        <div class="card-header">
                            <div class="card-header-left">
                                <i class="fas fa-cog"></i> Tùy chọn tài khoản
                            </div>
                        </div>
                        <div class="card-body">
                            <div class="action-row">
                                <c:if test="${sessionScope.customer.loginProvider != 'google'}">
                                    <a href="${pageContext.request.contextPath}/change-password" class="btn btn-primary">
                                        <i class="fas fa-key"></i> Đổi mật khẩu
                                    </a>
                                </c:if>
                                <a href="${pageContext.request.contextPath}/" class="btn btn-outline">
                                    <i class="fas fa-house"></i> Về trang chủ
                                </a>
                            </div>
                        </div>
                    </div>
                </c:if>

                <%-- ══════════════════════════════
                     EMPLOYEE PROFILE
                ══════════════════════════════ --%>
                <c:if test="${sessionScope.employee != null}">

                    <c:set var="isEdit" value="${not empty editMode or param.edit == 'true'}"/>

                    <div class="card">
                        <div class="card-header">
                            <div class="card-header-left">
                                <i class="fas fa-id-badge"></i> Thông tin nhân viên
                            </div>
                            <c:if test="${not isEdit}">
                                <a href="${pageContext.request.contextPath}/profile?edit=true" class="btn btn-edit">
                                    <i class="fas fa-pen"></i> Chỉnh sửa
                                </a>
                            </c:if>
                        </div>
                        <div class="card-body">

                            <%-- Avatar --%>
                            <div class="avatar-block">
                                <div class="avatar-circle" id="avatar-current" style="${isEdit ? '' : ''}">
                                    <c:choose>
                                        <c:when test="${not empty sessionScope.employee.image}">
                                            <img id="current-img" src="${pageContext.request.contextPath}/${sessionScope.employee.image}" alt="avatar">
                                        </c:when>
                                        <c:otherwise>
                                            <span id="avatar-initial">${sessionScope.employee.fullName.substring(0,1).toUpperCase()}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <%-- Preview ảnh mới (chỉ hiển thị khi edit và người dùng chọn file) --%>
                                <c:if test="${isEdit}">
                                    <img id="avatar-preview" src="#" alt="preview">
                                </c:if>
                                <div class="avatar-info">
                                    <h3>${sessionScope.employee.fullName}</h3>
                                    <c:choose>
                                        <c:when test="${sessionScope.employee.roleID == 1}">
                                            <span class="badge-role badge-owner"><i class="fas fa-crown"></i> Owner</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-role badge-staff"><i class="fas fa-user-tie"></i> Nhân viên</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <%-- VIEW MODE --%>
                            <c:if test="${not isEdit}">
                                <div class="info-grid">
                                    <div class="info-item">
                                        <span class="info-label"><i class="fas fa-user"></i> Họ và tên</span>
                                        <span class="info-value">${sessionScope.employee.fullName}</span>
                                    </div>
                                    <div class="info-item">
                                        <span class="info-label"><i class="fas fa-envelope"></i> Email</span>
                                        <span class="info-value">${sessionScope.employee.email}</span>
                                    </div>
                                    <div class="info-item">
                                        <span class="info-label"><i class="fas fa-phone"></i> Số điện thoại</span>
                                        <span class="info-value ${empty sessionScope.employee.phoneNumber ? 'empty' : ''}">
                                            <c:choose>
                                                <c:when test="${not empty sessionScope.employee.phoneNumber}">${sessionScope.employee.phoneNumber}</c:when>
                                                <c:otherwise>Chưa cập nhật</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div class="info-item">
                                        <span class="info-label"><i class="fas fa-birthday-cake"></i> Ngày sinh</span>
                                        <span class="info-value ${empty sessionScope.employee.dob ? 'empty' : ''}">
                                            <c:choose>
                                                <c:when test="${not empty sessionScope.employee.dob}">
                                                    <fmt:formatDate value="${sessionScope.employee.dob}" pattern="dd/MM/yyyy"/>
                                                </c:when>
                                                <c:otherwise>Chưa cập nhật</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div class="info-item full-width">
                                        <span class="info-label"><i class="fas fa-map-marker-alt"></i> Địa chỉ</span>
                                        <span class="info-value ${empty sessionScope.employee.address ? 'empty' : ''}">
                                            <c:choose>
                                                <c:when test="${not empty sessionScope.employee.address}">${sessionScope.employee.address}</c:when>
                                                <c:otherwise>Chưa cập nhật</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div class="info-item">
                                        <span class="info-label"><i class="fas fa-calendar"></i> Ngày tạo</span>
                                        <span class="info-value">${sessionScope.employee.createdAt}</span>
                                    </div>
                                    <div class="info-item">
                                        <span class="info-label"><i class="fas fa-key"></i> Đổi mật khẩu lần cuối</span>
                                        <span class="info-value ${empty sessionScope.employee.lastPasswordChangedAt ? 'empty' : ''}">
                                            <c:choose>
                                                <c:when test="${not empty sessionScope.employee.lastPasswordChangedAt}">${sessionScope.employee.lastPasswordChangedAt}</c:when>
                                                <c:otherwise>Chưa đổi lần nào</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                </div>
                            </c:if>

                            <%-- EDIT MODE --%>
                            <c:if test="${isEdit}">
                                <form action="${pageContext.request.contextPath}/profile" method="post" enctype="multipart/form-data">
                                    <div class="form-grid">

                                        <%-- Họ và tên — CÓ THỂ SỬA --%>
                                        <div class="form-group">
                                            <label class="form-label" for="fullName">
                                                <i class="fas fa-user"></i> Họ và tên <span style="color:#c0392b;">*</span>
                                            </label>
                                            <input type="text" id="fullName" name="fullName"
                                                   class="form-control ${not empty errors['fullName'] ? 'error-field' : ''}"
                                                   value="${not empty param.fullName ? param.fullName : sessionScope.employee.fullName}"
                                                   maxlength="150" required>
                                            <c:if test="${not empty errors['fullName']}">
                                                <span class="error-msg"><i class="fas fa-triangle-exclamation"></i> ${errors['fullName']}</span>
                                            </c:if>
                                        </div>

                                        <%-- Email — KHÔNG SỬA ĐƯỢC --%>
                                        <div class="form-group">
                                            <label class="form-label"><i class="fas fa-envelope"></i> Email</label>
                                            <div class="form-control-readonly">${sessionScope.employee.email}</div>
                                            <span class="readonly-note"><i class="fas fa-lock"></i> Liên hệ chủ cửa hàng để thay đổi</span>
                                        </div>

                                        <%-- Số điện thoại — KHÔNG SỬA ĐƯỢC --%>
                                        <div class="form-group">
                                            <label class="form-label"><i class="fas fa-phone"></i> Số điện thoại</label>
                                            <div class="form-control-readonly">
                                                <c:choose>
                                                    <c:when test="${not empty sessionScope.employee.phoneNumber}">${sessionScope.employee.phoneNumber}</c:when>
                                                    <c:otherwise><span style="color:#bbb;font-style:italic;">Chưa cập nhật</span></c:otherwise>
                                                </c:choose>
                                            </div>
                                            <span class="readonly-note"><i class="fas fa-lock"></i> Liên hệ chủ cửa hàng để thay đổi</span>
                                        </div>

                                        <%-- Ngày sinh — KHÔNG SỬA ĐƯỢC --%>
                                        <div class="form-group">
                                            <label class="form-label">
                                                <i class="fas fa-birthday-cake"></i> Ngày sinh
                                            </label>
                                            <div class="form-control-readonly">
                                                <c:choose>
                                                    <c:when test="${not empty sessionScope.employee.dob}">
                                                        <fmt:formatDate value="${sessionScope.employee.dob}" pattern="dd/MM/yyyy"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span style="color:#bbb;font-style:italic;">Chưa cập nhật</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <span class="readonly-note">
                                                <i class="fas fa-lock"></i> Liên hệ chủ cửa hàng để thay đổi
                                            </span>
                                        </div>

                                        <%-- Địa chỉ — CÓ THỂ SỬA --%>
                                        <div class="form-group full-width">
                                            <label class="form-label" for="address">
                                                <i class="fas fa-map-marker-alt"></i> Địa chỉ
                                            </label>
                                            <input type="text" id="address" name="address"
                                                   class="form-control"
                                                   value="${not empty param.address ? param.address : sessionScope.employee.address}"
                                                   maxlength="255"
                                                   placeholder="Nhập địa chỉ của bạn...">
                                        </div>

                                        <%-- Ảnh đại diện — CÓ THỂ SỬA --%>
                                        <div class="form-group full-width">
                                            <label class="form-label"><i class="fas fa-camera"></i> Ảnh đại diện</label>
                                            <div class="image-upload-wrap">
                                                <label for="image" class="upload-btn-label">
                                                    <i class="fas fa-upload"></i> Chọn ảnh mới
                                                </label>
                                                <input type="file" id="image" name="image" accept="image/jpg,image/jpeg,image/png,image/webp">
                                                <span class="upload-hint">JPG, PNG, WEBP — tối đa 2MB. Để trống nếu không muốn thay đổi.</span>
                                            </div>

                                            <c:if test="${not empty sessionScope.employee.image}">
                                                <div style="margin-top:10px;">
                                                    <input type="checkbox"
                                                           id="removeImage"
                                                           name="removeImage"
                                                           value="true">

                                                    <label for="removeImage">
                                                        Xóa ảnh hiện tại
                                                    </label>
                                                </div>
                                            </c:if>

                                            <c:if test="${not empty errors['image']}">
                                                <span class="error-msg"><i class="fas fa-triangle-exclamation"></i> ${errors['image']}</span>
                                            </c:if>
                                        </div>

                                    </div>

                                    <div class="form-actions">
                                        <button type="submit" class="btn btn-primary">
                                            <i class="fas fa-floppy-disk"></i> Lưu thay đổi
                                        </button>
                                        <a href="${pageContext.request.contextPath}/profile" class="btn btn-cancel">
                                            <i class="fas fa-xmark"></i> Hủy
                                        </a>
                                    </div>
                                </form>
                            </c:if>

                        </div>
                    </div>

                    <%-- Actions cho Employee --%>
                    <div class="card">
                        <div class="card-header">
                            <div class="card-header-left">
                                <i class="fas fa-cog"></i> Tùy chọn tài khoản
                            </div>
                        </div>
                        <div class="card-body">
                            <div class="action-row">
                                <a href="${pageContext.request.contextPath}/change-password" class="btn btn-primary">
                                    <i class="fas fa-key"></i> Đổi mật khẩu
                                </a>
                                <c:choose>
                                    <c:when test="${sessionScope.employee.roleID == 1}">
                                        <a href="${pageContext.request.contextPath}/owner/dashboard" class="btn btn-outline">
                                            <i class="fas fa-chart-line"></i> Về Dashboard
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${pageContext.request.contextPath}/staff/dashboard" class="btn btn-outline">
                                            <i class="fas fa-house"></i> Về Dashboard
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </c:if>
            </div>
        </div>

        <%@ include file="/views/includes/footer.jsp" %>

        <script>
            // Preview ảnh khi chọn file mới
            (function () {
                var input = document.getElementById('image');
                if (!input)
                    return;

                input.addEventListener('change', function () {
                    var file = this.files[0];
                    if (!file)
                        return;

                    var preview = document.getElementById('avatar-preview');
                    var current = document.getElementById('avatar-current');
                    var reader = new FileReader();

                    reader.onload = function (e) {
                        preview.src = e.target.result;
                        preview.style.display = 'block';
                        if (current)
                            current.style.display = 'none';
                    };
                    reader.readAsDataURL(file);
                });
            })();

            document.getElementById('removeImage')?.addEventListener('change', function () {
                var input = document.getElementById('image');
                var preview = document.getElementById('avatar-preview');
                var current = document.getElementById('avatar-current');

                if (this.checked) {
                    input.value = '';
                    input.disabled = true;

                    if (preview)
                        preview.style.display = 'none';

                    if (current)
                        current.style.display = 'none';
                } else {
                    input.disabled = false;

                    if (current)
                        current.style.display = '';
                }
            });

            // ── VALIDATE FORM CUSTOMER ──────────────────────────────
            const customerForm = document.querySelector('form[action*="/profile"]:not([enctype])');
            if (customerForm) {
                customerForm.addEventListener('submit', function (e) {
                    let hasError = false;
                    clearErrors(this);

                    const userName = document.getElementById('userName');
                    if (!userName)
                        return;

                    const val = userName.value.trim();
                    if (val === '') {
                        showProfileError(userName, 'Vui lòng nhập tên hiển thị.');
                        hasError = true;
                    } else if (val.length < 2 || val.length > 50) {
                        showProfileError(userName, 'Tên hiển thị phải từ 2 đến 50 ký tự.');
                        hasError = true;
                    }

                    if (hasError)
                        e.preventDefault();
                });
            }

            // ── VALIDATE FORM EMPLOYEE ──────────────────────────────
            const employeeForm = document.querySelector('form[enctype="multipart/form-data"]');
            if (employeeForm) {
                employeeForm.addEventListener('submit', function (e) {
                    let hasError = false;
                    clearErrors(this);

                    // FULL NAME
                    const fullName = document.getElementById('fullName');
                    if (fullName) {
                        const val = fullName.value.trim();
                        if (val === '') {
                            showProfileError(fullName, 'Vui lòng nhập họ tên.');
                            hasError = true;
                        } else if (val.length < 2 || val.length > 50) {
                            showProfileError(fullName, 'Họ tên phải từ 2 đến 50 ký tự.');
                            hasError = true;
                        }
                    }

                    // ĐỊA CHỈ
                    const address = document.getElementById('address');
                    if (address && address.value.trim().length > 255) {
                        showProfileError(address, 'Địa chỉ không được vượt quá 255 ký tự.');
                        hasError = true;
                    }

                    // ẢNH (nếu chọn file mới)
                    const imageInput = document.getElementById('image');
                    if (imageInput && imageInput.files.length > 0 && !imageInput.disabled) {
                        const file = imageInput.files[0];
                        const allowedExt = /\.(jpg|jpeg|png|webp)$/i;
                        if (!allowedExt.test(file.name)) {
                            showProfileError(imageInput, 'Chỉ chấp nhận ảnh jpg, jpeg, png, webp.');
                            hasError = true;
                        } else if (file.size > 2 * 1024 * 1024) {
                            showProfileError(imageInput, 'Ảnh không vượt quá 2MB.');
                            hasError = true;
                        }
                    }

                    if (hasError)
                        e.preventDefault();
                });
            }

            // ── HELPER ──────────────────────────────────────────────
            function showProfileError(input, message) {
                input.classList.add('error-field');
                // Tìm error-msg gần nhất
                const group = input.closest('.form-group');
                if (group) {
                    let errSpan = group.querySelector('.error-msg');
                    if (!errSpan) {
                        errSpan = document.createElement('span');
                        errSpan.className = 'error-msg';
                        errSpan.style.color = '#c0392b';
                        errSpan.style.fontSize = '0.8rem';
                        errSpan.style.marginTop = '4px';
                        errSpan.style.display = 'block';
                        input.parentNode.appendChild(errSpan);
                    }
                    errSpan.textContent = message;
                }
            }

            function clearErrors(form) {
                form.querySelectorAll('.error-field').forEach(el => el.classList.remove('error-field'));
                form.querySelectorAll('.error-msg').forEach(el => el.textContent = '');
            }


        </script>

    </body>
</html>