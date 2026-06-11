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
            background-color: #fff8e7; /* Tone màu kem sáng sủa theo yêu cầu */
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
            background: linear-gradient(rgba(255, 248, 231, 0.55), rgba(255, 248, 231, 0.55)), 
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

        .nav-menu {
            background-color: #8c6239;
            padding: 14px 40px;
            display: flex;
            gap: 30px;
            width: 100%;
        }
        .nav-menu a {
            color: #fff8e7;
            text-decoration: none;
            font-weight: 700;
            font-size: 1rem;
        }
        .nav-menu a:hover {
            color: #ffffff;
            text-decoration: underline;
        }

        .main-layout {
            display: flex;
            flex: 1;
            width: 100%;
            background-color: #ffffff;
        }
        @media (max-width: 768px) {
            .main-layout { flex-direction: column; }
        }

        /* SIDEBAR BÊN TRÁI */
        .sidebar {
            width: 340px;
            flex: 0 0 340px;
            background-color: #fffaf0; 
            border-right: 1px solid #eedec4;
            padding: 35px 25px;
        }

        /* Ô VUÔNG NHẬP LIỆU TÌM KIẾM MỚI */
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
            box-shadow: 0 0 8px rgba(140, 98, 57, 0.3);
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
            box-shadow: 0 0 10px rgba(22, 163, 74, 0.2);
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
        }
        .btn-confirm-booking:hover {
            background-color: #4a2e1a;
            color: #ffffff;
        }
        
        .footer {
            background-color: #3d2514;
            color: #d9c5b2;
            text-align: center;
            padding: 20px;
            font-size: 0.88rem;
            border-top: 2px solid #8c6239;
            width: 100%;
            margin-top: auto;
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
    </style>
</head>
<body>
 <%@include file="/views/includes/header.jsp" %>
<div class="wrapper-fullwidth">
    
    <div class="banner-header">
        <h1 class="banner-title">ĐẶT BÀN TRƯỚC</h1>
        <p class="banner-subtitle">Đặt bàn trước sẽ giúp quý khách lựa chọn được chỗ ngồi ưng ý và sự chuẩn bị chu đáo nhất.</p>
    </div>

    <div class="nav-menu">
        <a href="${pageContext.request.contextPath}/">🏠 Trang chủ</a>
       
        <a href="${pageContext.request.contextPath}/reservation?action=history">📅 Lịch sử của tôi</a>
    </div>

    <div class="main-layout">
        
        <div class="sidebar">
            <h5 class="fw-bold text-dark mb-2" style="font-size: 1.05rem; border-bottom: 2px solid #8c6239; padding-bottom: 6px; letter-spacing: 1px;">🔍 TÌM KIẾM NHANH</h5>
            
            <div class="mb-4">
                <input type="text" id="keywordSearch" class="search-box-custom" placeholder="Nhập quy mô bàn cần lọc..." onkeyup="filterTablesByKeyword()">
            </div>
            
            <form id="checkForm" method="get" action="${pageContext.request.contextPath}/reservation" onsubmit="return validateStep1()">
                <input type="hidden" name="action" value="choosetable">
                <input type="hidden" name="areaType" id="areaType" value="${areaType}">

                <div class="mb-3">
                    <label class="form-label small fw-bold text-secondary">Thời gian đến:</label>
                    <input type="datetime-local" class="form-control rounded-2 border-secondary-subtle py-2" name="orderTime" id="orderTime" value="${orderTime}" required>
                </div>

                <div class="mb-4">
                    <label class="form-label small fw-bold text-secondary">Vị trí không gian:</label>
                    <div class="d-flex flex-column gap-2">
                        <c:choose>
                            <c:when test="${not empty areaTypes}">
                                <c:forEach var="area" items="${areaTypes}">
                                    <div class="area-btn ${areaType == area ? 'selected' : ''}" onclick="selectArea(this, '${area}')">
                                        <span>${area == 'public' ? '🌿 Ngoài sảnh' : '🚪 Trong phòng'}</span>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="area-btn ${areaType == 'private' ? 'selected' : ''}" onclick="selectArea(this, 'private')">
                                    <span>🚪 Trong phòng</span>
                                </div>
                                <div class="area-btn ${areaType == 'public' ? 'selected' : ''}" onclick="selectArea(this, 'public')">
                                    <span>🌿 Ngoài sảnh</span>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div id="areaError" class="text-danger small mt-1" style="display:none">⚠️ Vui lòng click chọn một khu vực.</div>
                </div>

                <button type="submit" class="btn btn-sidebar-submit w-100 py-2.5">🔍 KIỂM TRA TRỐNG</button>
            </form>

            <div class="mt-4 pt-3 text-muted small" style="border-top: 1px dashed #dcd0bc;">
                <p class="mb-1 text-dark">📍 <strong>Địa điểm áp dụng:</strong></p>
                <p class="fst-italic text-secondary mb-0">Cơ sở 1: 145 Hoàng Cầu, Q. Đống Đa, Hà Nội</p>
            </div>
        </div>

        <div class="main-content">
            
            <c:if test="${not empty error}">
                <div class="alert alert-danger p-2 small mb-4">⚠️ ${error}</div>
            </c:if>

            <c:if test="${step == 'success'}">
                <div class="text-center py-4">
                    <div class="display-3 text-success mb-2">🎉</div>
                    <h4 class="fw-bold text-success">ĐẶT BÀN THÀNH CÔNG!</h4>
                    <p class="text-muted small">Hệ thống đã ghi nhận mã đơn hàng: <strong>#${order.orderID}</strong></p>
                    
                    <div class="card my-4 border-0 text-start mx-auto shadow-sm" style="max-width: 450px; border-left: 4px solid #8c6239 !important; background-color: #fffdf9;">
                        <div class="card-body p-3" style="font-size: 0.95rem;">
                            <div class="mb-2"><strong>Quy mô đặt:</strong> Bàn loại ${table.capacity} chỗ</div>
                            <div class="mb-2"><strong>Không gian vị trí:</strong> ${areaType == 'public' ? 'Ngoài sảnh' : 'Trong phòng'}</div>
                            <div><strong>Thời gian đến:</strong> <fmt:formatDate value="${order.orderTime}" pattern="HH:mm - dd/MM/yyyy"/></div>
                        </div>
                    </div>
                    <a href="${pageContext.request.contextPath}/" class="btn btn-dark rounded-pill px-4 btn-sm" style="background-color: #3d2514; border: none;">Quay lại trang chủ</a>
                </div>
            </c:if>

            <c:if test="${step == 'history'}">
                <div class="section-title">Lịch sử đơn đặt bàn của bạn</div>
                <c:choose>
                    <c:when test="${not empty orders}">
                        <div class="row g-3">
                            <c:forEach var="o" items="${orders}">
                                <div class="col-md-6">
                                    <div class="card border-0 shadow-sm" style="background-color: #fffdf9;">
                                        <div class="card-body p-3">
                                            <div class="d-flex justify-content-between mb-2">
                                                <span class="fw-bold text-secondary small">Đơn hàng #${o.orderID}</span>
                                                <span class="badge bg-warning text-dark">${o.orderStatus}</span>
                                            </div>
                                            <p class="m-0 small">📅 Ngày đến: <strong><fmt:formatDate value="${o.orderTime}" pattern="HH:mm - dd/MM/yyyy"/></strong></p>
                                            <c:if test="${o.orderStatus == 'pending' or o.orderStatus == 'reserved'}">
                                                <div class="text-end mt-2">
                                                    <a href="${pageContext.request.contextPath}/reservation?action=cancel&orderID=${o.orderID}" class="text-danger small text-decoration-none" onclick="return confirm('Bạn chắc chắn muốn hủy đặt bàn?')">❌ Hủy vé giữ chỗ</a>
                                                </div>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center py-5 text-muted">Bạn chưa thực hiện giao dịch đặt trước nào.</div>
                    </c:otherwise>
                </c:choose>
            </c:if>

            <c:if test="${step != 'success' && step != 'history'}">
                <form id="mainBookingForm" method="post" action="${pageContext.request.contextPath}/reservation" onsubmit="return validateFinalForm()">
                    <input type="hidden" name="orderTime" value="${orderTime}">
                    <input type="hidden" name="areaType" value="${areaType}">
                    <input type="hidden" name="tableID" id="finalTableID" value="${tableID}">

                    <c:if test="${step == 'choose-table'}">
                        <div class="mb-4">
                            <div class="section-title">3. Chọn quy mô sức chứa bàn ăn phù hợp</div>
                            <div class="row g-3" id="tableGridContainer">
                                <c:forEach var="g" items="${tableGroups}">
                                    <div class="col-sm-6 col-md-4 card-item-filter" data-name="bàn ${g.capacity} chỗ">
                                        <c:choose>
                                            <%-- TRƯỜNG HỢP HẾT BÀN --%>
                                            <c:when test="${g.isActive <= 0}">
                                                <div class="table-card" style="background: #e2e8f0; color: #94a3b8; border-color: #cbd5e1; cursor: not-allowed; opacity: 0.65;" onclick="alert('⚠️ Loại bàn quy mô ${g.capacity} chỗ tại khu vực này đã hết sạch phòng trống!')">
                                                    <div class="fw-bold text-secondary mb-1">❌ Bàn ${g.capacity} chỗ</div>
                                                    <span class="badge bg-danger rounded-pill fw-normal" style="font-size: .7rem;">Hết bàn</span>
                                                </div>
                                            </c:when>

                                            <%-- TRƯỜNG HỢP CÒN BÀN --%>
                                            <c:otherwise>
                                                <div class="table-card ${tableID == g.tableID ? 'selected' : ''}" onclick="selectFinalTable(this, '${g.tableID}')">
                                                    <div class="fw-bold text-dark mb-1">🪑 Bàn ${g.capacity} chỗ</div>
                                                    <span class="badge bg-success rounded-pill fw-normal" style="font-size: .7rem;">Còn ${g.isActive} bàn trống</span>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:forEach>
                            </div>
                            <div id="tableError" class="text-danger small mt-2" style="display:none">⚠️ Quý khách vui lòng chọn một quy mô bàn trống màu xanh.</div>
                        </div>

                        <div class="mb-4">
                            <div class="section-title">4. Đặt trước thực đơn (Tùy chọn)</div>
                            <div class="menu-placeholder">
                                <span class="fs-4">🍲</span>
                                <h6 class="fw-bold text-secondary small mt-1">Hệ thống thực đơn chuẩn bị trước</h6>
                                <p class="text-muted mb-0" style="font-size: .8rem;">Module chọn món đang được cập nhật liên kết. Quý khách có thể bấm nút chốt đơn phía dưới để hoàn tất đặt chỗ trống trực tiếp.</p>
                            </div>
                        </div>

                        <button type="submit" class="btn-confirm-booking mt-2">XÁC NHẬN ĐẶT BÀN</button>
                    </c:if>
                    
                    <c:if test="${step == 'pick-time'}">
                        <div class="text-center py-5 text-muted">
                            <div class="fs-2 mb-2">📅</div>
                            <p style="font-size: 0.95rem; line-height: 1.6;">Vui lòng điền ngày giờ đến và khu vực ở thanh tìm kiếm bên trái,<br>sau đó bấm <strong>"Kiểm tra trống"</strong> để hiển thị danh sách bàn ăn khả dụng.</p>
                        </div>
                    </c:if>
                </form>
            </c:if>

        </div>
    </div>

    
</div>

<script>
    //  Gõ từ khóa tự động ẩn/hiện các ô vuông bàn ăn
    function filterTablesByKeyword() {
        var input = document.getElementById("keywordSearch");
        var filter = input.value.toLowerCase(); // Chuyển chữ hoa thành chữ thường để so sánh
        var cards = document.getElementsByClassName("card-item-filter");

        for (var i = 0; i < cards.length; i++) {
            var tableNameData = cards[i].getAttribute("data-name");
            if (tableNameData.indexOf(filter) > -1) {
                cards[i].style.display = "";  
            } else {
                cards[i].style.display = "none"; 
            }
        }
    }

    function selectArea(btn, value) {
        document.querySelectorAll('.area-btn').forEach(b => b.classList.remove('selected'));
        btn.classList.add('selected');
        document.getElementById('areaType').value = value;
        document.getElementById('areaError').style.display = 'none';
    }

    function validateStep1() {
        if (!document.getElementById('areaType').value) {
            document.getElementById('areaError').style.display = 'block';
            return false;
        }
        return true;
    }

    function selectFinalTable(card, tableID) {
        document.querySelectorAll('.table-card').forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');
        document.getElementById('finalTableID').value = tableID;
        document.getElementById('tableError').style.display = 'none';
    }

    function validateFinalForm() {
        var tableID = document.getElementById('finalTableID').value;
        if (!tableID || tableID === "" || tableID === "-1") {
            document.getElementById('tableError').style.display = 'block';
            return false;
        }
        return true;
    }
</script>

 <%@include file="/views/includes/footer.jsp" %>  
</body>
</html>