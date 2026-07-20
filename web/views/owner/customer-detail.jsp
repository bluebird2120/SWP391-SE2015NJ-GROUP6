<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Chi tiết khách hàng</title>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
        <style>
            * {
                box-sizing: border-box;
            }
            body {
                margin: 0;
                font-family: 'Inter', sans-serif;
                background: #faf6f2;
                color: #3f3028;
            }
            .main {
                flex: 1;
                padding: 24px 32px;
                min-width: 0;
            }
            .page-title {
                font-family: 'Playfair Display', serif;
                color: #76493b;
                font-size: 1.7rem;
                margin: 0 0 6px;
            }
            .page-sub {
                color: #a0714f;
                margin-bottom: 18px;
            }
            .detail-card {
                background: #fff;
                border: 1px solid #ede0d8;
                border-radius: 14px;
                padding: 24px;
                max-width: 920px;
            }
            .profile-head {
                display: flex;
                gap: 18px;
                align-items: center;
                padding-bottom: 18px;
                border-bottom: 1px solid #f0e4dc;
                margin-bottom: 18px;
            }
            .avatar {
                width: 88px;
                height: 88px;
                border-radius: 50%;
                object-fit: cover;
                background: #eaded6;
                color: #76493b;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                font-size: 2rem;
            }
            .name {
                font-size: 1.35rem;
                font-weight: 700;
                color: #3f3028;
                margin-bottom: 8px;
            }
            .badge {
                padding: 4px 10px;
                border-radius: 12px;
                font-size: 0.78rem;
                font-weight: 700;
                display: inline-block;
                margin-right: 6px;
            }
            .badge-local, .badge-active {
                background: #d4edda;
                color: #155724;
            }
            .badge-google {
                background: #e4edff;
                color: #2456a6;
            }
            .badge-locked {
                background: #f8d7da;
                color: #842029;
            }
            .info-grid {
                display: grid;
                grid-template-columns: repeat(2, minmax(240px, 1fr));
                gap: 14px;
            }
            .info-box {
                border: 1px solid #f0e4dc;
                border-radius: 10px;
                padding: 13px 14px;
                background: #fffaf6;
            }
            .label {
                font-size: 0.78rem;
                color: #8a6e5a;
                font-weight: 700;
                text-transform: uppercase;
                margin-bottom: 6px;
            }
            .value {
                color: #3f3028;
                word-break: break-word;
            }
            .actions {
                margin-top: 20px;
                display: flex;
                gap: 10px;
                flex-wrap: wrap;
            }
            .btn {
                padding: 9px 16px;
                border-radius: 8px;
                border: none;
                cursor: pointer;
                font-family: inherit;
                font-size: 0.88rem;
                font-weight: 600;
                text-decoration: none;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                gap: 6px;
            }
            .btn-back {
                background: #d7bfa4;
                color: #5a3428;
            }
            .btn-lock {
                background: #dc3545;
                color: #fff;
            }
            .btn-unlock {
                background: #198754;
                color: #fff;
            }
            @media (max-width: 760px) {
                .info-grid {
                    grid-template-columns: 1fr;
                }
                .profile-head {
                    align-items: flex-start;
                    flex-direction: column;
                }
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div style="display: flex;">
            <%@ include file="/views/includes/dashboard.jsp" %>
            <main class="main">
                <h1 class="page-title">Chi tiết khách hàng</h1>
                <div class="page-sub">Thông tin đầy đủ của tài khoản khách hàng.</div>

                <div class="detail-card">
                    <div class="profile-head">
                        <c:choose>
                            <c:when test="${not empty customer.image}">
                                <img class="avatar" src="${pageContext.request.contextPath}/${customer.image}" alt="${customer.userName}">
                            </c:when>
                            <c:otherwise>
                                <span class="avatar"><i class="fas fa-user"></i></span>
                                </c:otherwise>
                            </c:choose>
                        <div>
                            <div class="name">${customer.userName}</div>
                            <c:choose>
                                <c:when test="${customer.loginProvider == 'google'}">
                                    <span class="badge badge-google">Google</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-local">Local</span>
                                </c:otherwise>
                            </c:choose>
                            <c:choose>
                                <c:when test="${customer.isActive == 1}">
                                    <span class="badge badge-active">Hoạt động</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-locked">Đã khóa</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="info-grid">
                        <div class="info-box">
                            <div class="label">Mã khách hàng</div>
                            <div class="value">#${customer.customerID}</div>
                        </div>
                        <div class="info-box">
                            <div class="label">Email</div>
                            <div class="value">${empty customer.email ? 'Chưa cập nhật' : customer.email}</div>
                        </div>
                        <div class="info-box">
                            <div class="label">Số điện thoại</div>
                            <div class="value">${empty customer.phoneNumber ? 'Chưa cập nhật' : customer.phoneNumber}</div>
                        </div>
                        <div class="info-box">
                            <div class="label">Ngày sinh</div>
                            <div class="value">
                                <c:choose>
                                    <c:when test="${not empty customer.dob}">
                                        <fmt:formatDate value="${customer.dob}" pattern="dd/MM/yyyy"/>
                                    </c:when>
                                    <c:otherwise>Chưa cập nhật</c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="info-box">
                            <div class="label">Địa chỉ</div>
                            <div class="value">${empty customer.address ? 'Chưa cập nhật' : customer.address}</div>
                        </div>
                        <div class="info-box">
                            <div class="label">Ngày tạo</div>
                            <div class="value">
                                <fmt:formatDate value="${customer.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                            </div>
                        </div>
                    </div>

                    <div class="actions">
                        <a class="btn btn-back" href="${pageContext.request.contextPath}/owner/customer-list">
                            <i class="fas fa-arrow-left"></i> Quay lại danh sách
                        </a>
                        <form method="post" action="${pageContext.request.contextPath}/owner/customer-list" style="margin:0;">
                            <input type="hidden" name="customerID" value="${customer.customerID}">
                            <c:choose>
                                <c:when test="${customer.isActive == 1}">
                                    <input type="hidden" name="isActive" value="0">
                                    <button type="submit" class="btn btn-lock"
                                            onclick="return confirm('Bạn chắc chắn muốn khóa tài khoản này?')">
                                        <i class="fas fa-lock"></i> Khóa tài khoản
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <input type="hidden" name="isActive" value="1">
                                    <button type="submit" class="btn btn-unlock"
                                            onclick="return confirm('Bạn chắc chắn muốn mở khóa tài khoản này?')">
                                        <i class="fas fa-lock-open"></i> Mở khóa tài khoản
                                    </button>
                                </c:otherwise>
                            </c:choose>
                        </form>
                    </div>
                </div>
            </main>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>
