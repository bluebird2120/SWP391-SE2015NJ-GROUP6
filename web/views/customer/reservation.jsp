<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đặt Bàn Trước - Không Gian Ẩm Thực Truyền Thống</title>

        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

        <style>
            html, body {
                margin: 0;
                padding: 0;
                width: 100%;
                background-color: #fff8e7;
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                color: #4a3621;
            }

            .wrapper-fullwidth {
                width: 100%;
                min-height: 100vh;
                display: flex;
                flex-direction: column;
                background-color: #ffffff;
            }

            .banner-header {
                background: linear-gradient(rgba(255,248,231,0.55), rgba(255,248,231,0.55)),
                    url('https://mia.vn/media/uploads/blog-du-lich/kham-pha-cau-long-bien-bieu-tuong-van-hoa-lich-su-cua-ha-noi-1639844231.jpg') center/cover no-repeat;
                padding: 45px 20px;
                text-align: center;
                border-bottom: 3px solid #8c6239;
                width: 100%;
            }

            .banner-title {
                font-size: 2.6rem;
                font-weight: 800;
                color: #5c3a21;
                letter-spacing: 6px;
                text-transform: uppercase;
                margin-bottom: 8px;
            }

            .banner-subtitle {
                font-size: 1rem;
                color: #6e4e37;
                font-weight: 500;
                max-width: 800px;
                margin: 0 auto;
            }

            .main-layout {
                display: flex;
                flex: 1;
                width: 100%;
                background-color: #ffffff;
            }

            @media (max-width: 768px) {
                .main-layout {
                    flex-direction: column;
                }

                .sidebar {
                    width: 100% !important;
                    flex: 0 0 auto !important;
                }
            }

            .sidebar {
                width: 340px;
                flex: 0 0 340px;
                background-color: #fffaf0;
                border-right: 1px solid #eedec4;
                padding: 35px 25px;
            }

            .search-box-custom {
                width: 100%;
                padding: 10px 12px;
                border: 2px solid #8c6239;
                border-radius: 6px;
                background-color: #ffffff;
                color: #4a3621;
                font-weight: 600;
                outline: none;
            }

            .search-box-custom:focus {
                box-shadow: 0 0 8px rgba(140,98,57,0.3);
            }

            .main-content {
                flex: 1;
                padding: 40px;
                background-color: #ffffff;
            }

            .area-btn {
                border: 2px solid #dcd0bc;
                border-radius: 6px;
                padding: 12px;
                cursor: pointer;
                text-align: center;
                background-color: #ffffff;
                transition: all 0.2s;
                font-weight: 700;
                color: #6e553f;
            }

            .area-btn:hover {
                border-color: #8c6239;
                background-color: #fffdf9;
            }

            .area-btn.selected {
                border-color: #8c6239;
                background-color: #8c6239;
                color: #fff8e7;
            }

            .section-title {
                font-size: 1.15rem;
                font-weight: 700;
                color: #5c3a21;
                margin-bottom: 25px;
                padding-bottom: 8px;
                border-bottom: 2px solid #8c6239;
                text-transform: uppercase;
            }

            .table-card {
                border: 2px solid #ced4da;
                border-radius: 8px;
                padding: 24px 15px;
                text-align: center;
                cursor: pointer;
                transition: all 0.2s;
                background-color: #ffffff;
            }

            .table-card:hover {
                border-color: #22c55e;
            }

            .table-card.selected {
                background-color: #f0fdf4;
                border-color: #16a34a;
                box-shadow: 0 0 10px rgba(22,163,74,0.2);
            }

            .btn-confirm-booking {
                background-color: #5c3a21;
                color: #fff8e7;
                border: none;
                padding: 15px;
                border-radius: 4px;
                font-weight: 700;
                font-size: 1.1rem;
                width: 100%;
                text-transform: uppercase;
                box-shadow: 0 4px 0 #3b2514;
                cursor: pointer;
            }

            .btn-confirm-booking:hover {
                background-color: #4a2e1a;
                color: #ffffff;
            }

            .menu-placeholder {
                padding: 2.5rem;
                border: 2px dashed #dcd0bc;
                background-color: #fffdf9;
                border-radius: 8px;
                text-align: center;
            }

            .btn-sidebar-submit {
                background-color: #8c6239;
                color: #fff8e7;
                border: none;
                font-weight: 700;
                padding: 10px;
                border-radius: 6px;
            }

            .btn-sidebar-submit:hover {
                background-color: #704d2b;
            }

            .success-card {
                max-width: 480px;
                margin: 0 auto;
                border: none;
                border-left: 4px solid #8c6239 !important;
                background-color: #fffdf9;
                border-radius: 8px;
            }

            .badge-reserved {
                background-color: #2563eb;
                color: #fff;
            }

            .badge-pending {
                background-color: #f59e0b;
                color: #1a1a1a;
            }

            .badge-cancelled {
                background-color: #dc2626;
                color: #fff;
            }

            .badge-serving {
                background-color: #16a34a;
                color: #fff;
            }

            .badge-completed {
                background-color: #6b7280;
                color: #fff;
            }
        </style>
    </head>

    <body>
        <%@include file="/views/includes/header.jsp" %>

        <div class="wrapper-fullwidth">

            <div class="banner-header">
                <h1 class="banner-title">ĐẶT BÀN TRƯỚC</h1>
                <p class="banner-subtitle">
                    Đặt bàn trước sẽ giúp quý khách lựa chọn được chỗ ngồi ưng ý và sự chuẩn bị chu đáo nhất.
                </p>
            </div>

            <div class="main-layout">

                <%-- SIDEBAR --%>
                <div class="sidebar">
                    <h5 class="fw-bold text-dark mb-2"
                        style="font-size:1.05rem; border-bottom:2px solid #8c6239; padding-bottom:6px; letter-spacing:1px;">
                        🔍 TÌM KIẾM NHANH
                    </h5>

                    <div class="mb-4">
                        <input type="text"
                               id="keywordSearch"
                               class="search-box-custom"
                               placeholder="Nhập quy mô bàn cần lọc..."
                               onkeyup="filterTablesByKeyword()">
                    </div>

                    <form id="checkForm"
                          method="get"
                          action="${pageContext.request.contextPath}/reservation"
                          onsubmit="return validateStep1()">

                        <input type="hidden" name="action" value="choosetable">
                        <input type="hidden" name="areaType" id="areaType" value="${areaType}">

                        <div class="mb-3">
                            <label class="form-label small fw-bold text-secondary">
                                Thời gian đến:
                            </label>

                            <input type="datetime-local"
                                   class="form-control rounded-2 border-secondary-subtle py-2"
                                   name="orderTime"
                                   id="orderTime"
                                   value="${orderTime}"
                                   required>
                        </div>

                        <div class="mb-4">
                            <label class="form-label small fw-bold text-secondary">
                                Vị trí không gian:
                            </label>

                            <div class="d-flex flex-column gap-2">
                                <c:choose>
                                    <c:when test="${not empty areaTypes}">
                                        <c:forEach var="area" items="${areaTypes}">
                                            <div class="area-btn ${areaType == area ? 'selected' : ''}"
                                                 onclick="selectArea(this, '${area}')">

                                                <c:choose>
                                                    <c:when test="${area == 'public'}">
                                                        <span>🌿 Ngoài sảnh</span>
                                                    </c:when>

                                                    <c:when test="${area == 'private'}">
                                                        <span>🚪 Trong phòng</span>
                                                    </c:when>

                                                    <c:otherwise>
                                                        <span>${area}</span>
                                                    </c:otherwise>
                                                </c:choose>

                                            </div>
                                        </c:forEach>
                                    </c:when>

                                    <c:otherwise>
                                        <div class="area-btn ${areaType == 'public' ? 'selected' : ''}"
                                             onclick="selectArea(this, 'public')">
                                            🌿 Ngoài sảnh
                                        </div>

                                        <div class="area-btn ${areaType == 'private' ? 'selected' : ''}"
                                             onclick="selectArea(this, 'private')">
                                            🚪 Trong phòng
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div id="areaError" class="text-danger small mt-1" style="display:none">
                                ⚠️ Vui lòng click chọn một khu vực.
                            </div>
                        </div>

                        <button type="submit" class="btn btn-sidebar-submit w-100 py-2">
                            🔍 KIỂM TRA TRỐNG
                        </button>
                    </form>

                    <div class="mt-4 pt-3 text-muted small" style="border-top: 1px dashed #dcd0bc;">
                        <p class="mb-1 text-dark">
                            📍 <strong>Địa điểm áp dụng:</strong>
                        </p>
                        <p class="fst-italic text-secondary mb-0">
                            Cơ sở 1: 145 Hoàng Cầu, Q. Đống Đa, Hà Nội
                        </p>
                    </div>
                </div>

                <%-- MAIN CONTENT --%>
                <div class="main-content">

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger p-2 small mb-4">
                            ⚠️ ${error}
                        </div>
                    </c:if>

                    <%-- TRANG THÀNH CÔNG --%>
                    <c:if test="${step == 'success'}">
                        <div class="text-center py-4">
                            <div class="display-3 text-success mb-2">🎉</div>

                            <h4 class="fw-bold text-success">
                                ĐẶT BÀN THÀNH CÔNG!
                            </h4>

                            <p class="text-muted small">
                                Mã đơn hàng: <strong>#${order.orderID}</strong>
                            </p>

                            <div class="card shadow-sm my-4 success-card">
                                <div class="card-body p-4" style="font-size:0.95rem; text-align:left;">
                                    <div class="mb-2">
                                        <strong>Loại bàn:</strong> Bàn ${order.capacity} chỗ
                                    </div>

                                    <div class="mb-2">
                                        <strong>Khu vực:</strong>
                                        <c:choose>
                                            <c:when test="${order.areaType == 'public'}">
                                                🌿 Ngoài sảnh
                                            </c:when>

                                            <c:when test="${order.areaType == 'private'}">
                                                🚪 Trong phòng
                                            </c:when>

                                            <c:otherwise>
                                                ${order.areaType}
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="mb-2">
                                        <strong>Thời gian đến:</strong>
                                        <fmt:formatDate value="${order.orderTime}" pattern="HH:mm - dd/MM/yyyy"/>
                                    </div>

                                    <div class="mb-2">
                                        <strong>Trạng thái:</strong>
                                        <span class="badge badge-reserved rounded-pill">
                                            ✅ Đã giữ bàn
                                        </span>
                                    </div>

                                    <hr style="border-color:#eedec4;">

                                    <p class="text-muted mb-0" style="font-size:0.85rem;">
                                        ℹ️ Bàn đã được giữ cho quý khách.
                                        Vui lòng đến đúng giờ và cho nhân viên biết mã đơn
                                        <strong>#${order.orderID}</strong>.
                                        Bàn sẽ tự động hủy nếu quý khách không đến sau
                                        <strong>30 phút</strong> kể từ giờ hẹn.
                                    </p>
                                </div>
                            </div>

                            <div class="d-flex gap-2 justify-content-center">
                                <a href="${pageContext.request.contextPath}/reservation?action=history"
                                   class="btn btn-outline-secondary rounded-pill px-4 btn-sm">
                                    📅 Xem lịch sử
                                </a>

                                <a href="${pageContext.request.contextPath}/"
                                   class="btn btn-dark rounded-pill px-4 btn-sm"
                                   style="background-color:#3d2514; border:none;">
                                    🏠 Trang chủ
                                </a>
                            </div>
                        </div>
                    </c:if>

                    <%-- LỊCH SỬ ĐẶT BÀN --%>
                    <c:if test="${step == 'history'}">
                        <div class="section-title">
                            Lịch sử đơn đặt bàn của bạn
                        </div>

                        <c:choose>
                            <c:when test="${not empty orders}">
                                <div class="row g-3">
                                    <c:forEach var="o" items="${orders}">
                                        <div class="col-md-6">
                                            <div class="card border-0 shadow-sm" style="background-color:#fffdf9;">
                                                <div class="card-body p-3">

                                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                                        <span class="fw-bold text-secondary small">
                                                            Đơn #${o.orderID}
                                                        </span>

                                                        <c:choose>
                                                            <c:when test="${o.orderStatus == 'reserved'}">
                                                                <span class="badge badge-reserved rounded-pill">
                                                                    ✅ Đã giữ bàn
                                                                </span>
                                                            </c:when>

                                                            <c:when test="${o.orderStatus == 'pending'}">
                                                                <span class="badge badge-pending rounded-pill">
                                                                    ⏳ Chờ xác nhận
                                                                </span>
                                                            </c:when>

                                                            <c:when test="${o.orderStatus == 'cancelled'}">
                                                                <span class="badge badge-cancelled rounded-pill">
                                                                    ❌ Đã hủy
                                                                </span>
                                                            </c:when>

                                                            <c:when test="${o.orderStatus == 'serving'}">
                                                                <span class="badge badge-serving rounded-pill">
                                                                    🍽 Đang phục vụ
                                                                </span>
                                                            </c:when>

                                                            <c:otherwise>
                                                                <span class="badge badge-completed rounded-pill">
                                                                    ${o.orderStatus}
                                                                </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>

                                                    <p class="m-0 small">
                                                        🪑 Loại bàn:
                                                        <strong>${o.capacity} chỗ</strong>
                                                    </p>

                                                    <p class="m-0 small">
                                                        📍 Khu vực:
                                                        <strong>
                                                            <c:choose>
                                                                <c:when test="${o.areaType == 'public'}">
                                                                    Ngoài sảnh
                                                                </c:when>

                                                                <c:when test="${o.areaType == 'private'}">
                                                                    Trong phòng
                                                                </c:when>

                                                                <c:otherwise>
                                                                    ${o.areaType}
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </strong>
                                                    </p>

                                                    <p class="m-0 small mt-1">
                                                        📅 Ngày đến:
                                                        <strong>
                                                            <fmt:formatDate value="${o.orderTime}" pattern="HH:mm - dd/MM/yyyy"/>
                                                        </strong>
                                                    </p>

                                                    <c:if test="${o.orderStatus == 'reserved'}">
                                                        <div class="text-end mt-2">
                                                            <a href="${pageContext.request.contextPath}/reservation?action=cancel&orderID=${o.orderID}"
                                                               class="text-danger small text-decoration-none"
                                                               onclick="return confirm('Bạn chắc chắn muốn hủy đặt bàn #${o.orderID}?')">
                                                                ❌ Hủy vé giữ chỗ
                                                            </a>
                                                        </div>
                                                    </c:if>

                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>

                            <c:otherwise>
                                <div class="text-center py-5 text-muted">
                                    Bạn chưa thực hiện giao dịch đặt trước nào.
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:if>

                    <%-- FORM ĐẶT BÀN --%>
                    <c:if test="${step != 'success' && step != 'history'}">
                        <form id="mainBookingForm"
                              method="post"
                              action="${pageContext.request.contextPath}/reservation"
                              onsubmit="return validateFinalForm()">

                            <input type="hidden" name="orderTime" value="${orderTime}">
                            <input type="hidden" name="areaType" value="${areaType}">
                            <input type="hidden" name="capacity" id="finalCapacity" value="${capacity}">

                            <%-- STEP CHỌN BÀN --%>
                            <c:if test="${step == 'choose-table'}">
                                <div class="mb-4">
                                    <div class="section-title">
                                        3. Chọn quy mô bàn phù hợp
                                    </div>

                                    <c:choose>
                                        <c:when test="${not empty tableGroups}">
                                            <div class="row g-3" id="tableGridContainer">
                                                <c:forEach var="g" items="${tableGroups}">
                                                    <div class="col-sm-6 col-md-4 card-item-filter"
                                                         data-name="bàn ${g.capacity} chỗ">

                                                        <c:choose>
                                                            <c:when test="${g.isActive <= 0}">
                                                                <div class="table-card"
                                                                     style="background:#e2e8f0; color:#94a3b8; border-color:#cbd5e1; cursor:not-allowed; opacity:0.65;"
                                                                     onclick="alert('⚠️ Loại bàn ${g.capacity} chỗ tại khu vực này đã hết chỗ trống!')">

                                                                    <div class="fw-bold text-secondary mb-1">
                                                                        ❌ Bàn ${g.capacity} chỗ
                                                                    </div>

                                                                    <span class="badge bg-danger rounded-pill fw-normal"
                                                                          style="font-size:.7rem;">
                                                                        Hết bàn
                                                                    </span>
                                                                </div>
                                                            </c:when>

                                                            <c:otherwise>
                                                                <div class="table-card ${capacity == g.capacity ? 'selected' : ''}"
                                                                     onclick="selectCapacity(this, '${g.capacity}')">

                                                                    <div class="fw-bold text-dark mb-1">
                                                                        🪑 Bàn ${g.capacity} chỗ
                                                                    </div>

                                                                    <span class="badge bg-success rounded-pill fw-normal"
                                                                          style="font-size:.7rem;">
                                                                        Còn ${g.isActive} bàn trống
                                                                    </span>
                                                                </div>
                                                            </c:otherwise>
                                                        </c:choose>

                                                    </div>
                                                </c:forEach>
                                            </div>

                                            <div id="tableError" class="text-danger small mt-2" style="display:none">
                                                ⚠️ Quý khách vui lòng chọn một loại bàn trống màu xanh.
                                            </div>

                                            <div class="mb-4 mt-4">
                                                <div class="section-title">
                                                    4. Đặt trước thực đơn (Tùy chọn)
                                                </div>

                                                <div class="menu-placeholder">
                                                    <span class="fs-4">🍲</span>

                                                    <h6 class="fw-bold text-secondary small mt-1">
                                                        Hệ thống thực đơn chuẩn bị trước
                                                    </h6>

                                                    <p class="text-muted mb-0" style="font-size:.8rem;">
                                                        Module chọn món đang được cập nhật.
                                                        Quý khách có thể bấm xác nhận phía dưới để hoàn tất đặt chỗ.
                                                    </p>
                                                </div>
                                            </div>

                                            <button type="submit" class="btn-confirm-booking mt-2">
                                                XÁC NHẬN ĐẶT BÀN
                                            </button>
                                        </c:when>

                                        <c:otherwise>
                                            <div class="alert alert-warning small">
                                                ⚠️ Không tìm thấy loại bàn nào trong khu vực này.
                                                Vui lòng kiểm tra dữ liệu bảng <strong>Table</strong>
                                                hoặc kiểm tra giá trị <strong>areaType</strong> trong database.
                                            </div>

                                            <div id="tableError" class="text-danger small mt-2" style="display:none">
                                                ⚠️ Không có loại bàn nào để chọn.
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:if>

                            <%-- STEP CHỌN THỜI GIAN --%>
                            <c:if test="${step == 'pick-time'}">
                                <div class="text-center py-5 text-muted">
                                    <div class="fs-2 mb-2">📅</div>

                                    <p style="font-size:0.95rem; line-height:1.6;">
                                        Vui lòng điền ngày giờ đến và khu vực ở thanh tìm kiếm bên trái,<br>
                                        sau đó bấm <strong>"Kiểm tra trống"</strong>
                                        để hiển thị danh sách bàn khả dụng.
                                    </p>
                                </div>
                            </c:if>

                        </form>
                    </c:if>

                </div>
                <%-- end main-content --%>

            </div>
            <%-- end main-layout --%>

        </div>
        <%-- end wrapper-fullwidth --%>

        <script>
            function filterTablesByKeyword() {
                var input = document.getElementById("keywordSearch");

                if (!input) {
                    return;
                }

                var filter = input.value.toLowerCase();
                var cards = document.getElementsByClassName("card-item-filter");

                for (var i = 0; i < cards.length; i++) {
                    var name = cards[i].getAttribute("data-name");

                    if (!name) {
                        cards[i].style.display = "";
                    } else {
                        cards[i].style.display = name.toLowerCase().indexOf(filter) > -1 ? "" : "none";
                    }
                }
            }

            function selectArea(btn, value) {
                document.querySelectorAll('.area-btn').forEach(function (b) {
                    b.classList.remove('selected');
                });

                btn.classList.add('selected');

                var areaInput = document.getElementById('areaType');

                if (areaInput) {
                    areaInput.value = value;
                }

                var areaError = document.getElementById('areaError');

                if (areaError) {
                    areaError.style.display = 'none';
                }
            }

            function validateStep1() {
                var areaInput = document.getElementById('areaType');
                var areaError = document.getElementById('areaError');

                if (!areaInput || !areaInput.value) {
                    if (areaError) {
                        areaError.style.display = 'block';
                    }

                    return false;
                }

                return true;
            }

            function selectCapacity(card, capacityValue) {
                document.querySelectorAll('.table-card').forEach(function (c) {
                    c.classList.remove('selected');
                });

                card.classList.add('selected');

                var finalCapacity = document.getElementById('finalCapacity');

                if (finalCapacity) {
                    finalCapacity.value = capacityValue;
                }

                var tableError = document.getElementById('tableError');

                if (tableError) {
                    tableError.style.display = 'none';
                }
            }

            function validateFinalForm() {
                var finalCapacity = document.getElementById('finalCapacity');

                if (!finalCapacity) {
                    return true;
                }

                var cap = finalCapacity.value;
                var tableError = document.getElementById('tableError');

                if (!cap || cap === "" || cap === "-1") {
                    if (tableError) {
                        tableError.style.display = 'block';
                    }

                    return false;
                }

                return true;
            }
        </script>

        <%@include file="/views/includes/footer.jsp" %>
    </body>
</html>