<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt Bàn Trước</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f8f5f0; }
        .card-wrap {
            max-width: 580px; margin: 50px auto;
            background: #fff; border-radius: 16px;
            box-shadow: 0 4px 24px rgba(0,0,0,.09);
            padding: 38px 36px;
        }
        .area-btn {
            border: 2px solid #dee2e6; border-radius: 12px;
            padding: 16px 18px; cursor: pointer;
            background: #fff; width: 100%; text-align: left; transition: all .2s;
        }
        .area-btn:hover, .area-btn.selected { border-color: #c0392b; background: #fff5f5; }
        .table-card {
            border: 2px solid #dee2e6; border-radius: 12px; padding: 20px;
            text-align: center; cursor: pointer; transition: all .2s; background: #fff;
        }
        .table-card:hover, .table-card.selected { border-color: #27ae60; background: #f1f9f5; }
        .btn-main {
            background: #c0392b; color: #fff; border: none; padding: 12px 30px;
            border-radius: 30px; font-weight: bold; width: 100%; transition: all .2s;
        }
        .btn-main:hover { background: #a83226; color: #fff; }
        .btn-back { color: #7f8c8d; text-decoration: none; font-size: .9rem; display: inline-block; }
        .btn-back:hover { color: #333; }
        .step-indicator { font-size: .85rem; font-weight: bold; color: #c0392b; text-transform: uppercase; letter-spacing: 1px; }
        
        /* Cấu trúc CSS riêng cho hộp menu giả định, tạo viền đứt đoạn mượt mà */
        .menu-placeholder {
            padding: 2rem;
            border: 2px dashed #cbd5e1;
            background-color: #f8fafc;
            border-radius: 12px;
            text-align: center;
            margin-bottom: 1.5rem;
        }
    </style>
</head>
<body>

<div class="container">
<div class="card-wrap">

    <%-- =================================================================
         BƯỚC 1: CHỌN THỜI GIAN VÀ KHU VỰC
         ================================================================= --%>
    <c:if test="${step == 'pick-time'}">
        <span class="step-indicator">Bước 1 trên 3</span>
        <h3 class="fw-bold mt-1 mb-4">Đặt Bàn Trước</h3>

        <c:if test="${not empty error}">
            <div class="alert alert-danger p-2 small">${error}</div>
        </c:if>

        <form method="get" action="${pageContext.request.contextPath}/reservation" onsubmit="return validateStep1()">
            <input type="hidden" name="action" value="choosetable">
            <input type="hidden" name="areaType" id="areaType" value="${param.areaType}">

            <div class="mb-4">
                <label class="form-label fw-semibold text-secondary">Chọn ngày & giờ đến</label>
                <input type="datetime-local" class="form-control form-control-lg rounded-3" 
                       name="orderTime" value="${param.orderTime}" required>
            </div>

            <div class="mb-4">
                <label class="form-label fw-semibold text-secondary">Chọn khu vực mong muốn</label>
                <div class="row g-3">
                    <c:forEach var="area" items="${areaTypes}">
                        <div class="col-6">
                            <div class="area-btn ${param.areaType == area ? 'selected' : ''}" 
                                 onclick="selectArea(this, '${area}')">
                                <c:choose>
                                    <c:when test="${area == 'public'}">
                                        <span class="fs-4 d-block mb-1">🌿</span>
                                        <strong>Ngoài sảnh</strong>
                                        <small class="text-muted d-block" style="font-size: .75rem;">Không gian thoáng đãng</small>
                                    </c:when>
                                    <c:when test="${area == 'private'}">
                                        <span class="fs-4 d-block mb-1">🚪</span>
                                        <strong>Trong phòng</strong>
                                        <small class="text-muted d-block" style="font-size: .75rem;">Riêng tư, ấm cúng</small>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="fs-4 d-block mb-1">📍</span>
                                        <strong>${area}</strong>
                                        <small class="text-muted d-block" style="font-size: .75rem;">Khu vực nhà hàng</small>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </c:forEach>
                </div>
                <div id="areaError" class="text-danger small mt-2" style="display:none">Vui lòng chọn khu vực.</div>
            </div>

            <button type="submit" class="btn-main">Xem bàn trống →</button>
        </form>
    </c:if>


    <%-- =================================================================
         BƯỚC 2: CHỌN SƠ ĐỒ BÀN CÒN TRỐNG
         ================================================================= --%>
    <c:if test="${step == 'choose-table'}">
        <a href="${pageContext.request.contextPath}/reservation" class="btn-back">← Quay lại chọn thời gian</a>
        <div class="mt-3">
            <span class="step-indicator">Bước 2 trên 3</span>
            <h3 class="fw-bold mt-1 mb-1">Chọn vị trí bàn</h3>
            <p class="text-muted small mb-4">Khu vực: <strong class="text-dark">${areaLabel}</strong> | Thời gian: <strong class="text-dark">${orderTime}</strong></p>
        </div>

        <c:if test="${empty tables}">
            <div class="alert alert-warning">Rất tiếc, khung giờ này tại khu vực này đã hết bàn trống. Vui lòng chọn giờ khác.</div>
        </c:if>

        <c:if test="${not empty tables}">
            <form method="get" action="${pageContext.request.contextPath}/reservation" onsubmit="return checkTable()">
                <input type="hidden" name="action" value="choosefood">
                <input type="hidden" name="orderTime" value="${orderTime}">
                <input type="hidden" name="tableID" id="tableID" value="">

                <div class="row g-3 mb-4">
                    <c:forEach var="t" items="${tables}">
                        <div class="col-6 col-md-4">
                            <div class="table-card" onclick="selectTable(this, '${t.tableID}')">
                                <div class="fw-bold text-dark mb-1">🪑 ${t.tableName}</div>
                                <span class="badge bg-secondary rounded-pill fw-normal" style="font-size: .75rem;">${t.capacity} chỗ</span>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <div id="tableError" class="text-danger small mb-3" style="display:none">Vui lòng bấm chọn một bàn cụ thể bên trên.</div>
                <button type="submit" class="btn-main">Tiếp theo (Chọn món đặt trước) →</button>
            </form>
        </c:if>
    </c:if>


    <%-- =================================================================
         BƯỚC CHỌN MÓN (ĐÃ FIX LỖI CÚ PHÁP ĐỂ CHỜ ĐỔ ĐÚNG CODE MENU SAU)
         ================================================================= --%>
    <c:if test="${step == 'choose-food'}">
        <a href="${pageContext.request.contextPath}/reservation?action=choosetable&orderTime=${orderTime}&areaType=${param.areaType}" class="btn-back">← Quay lại chọn bàn</a>
        <div class="mt-3">
            <span class="step-indicator">Bước tùy chọn</span>
            <h3 class="fw-bold mt-1 mb-1">🍔 Đặt trước món ăn</h3>
            <p class="text-muted small mb-4">Giúp nhà hàng chuẩn bị sẵn sàng chu đáo nhất khi bạn đến nơi.</p>
        </div>

        <form method="post" action="${pageContext.request.contextPath}/reservation">
            <input type="hidden" name="orderTime" value="${orderTime}">
            <input type="hidden" name="tableID" value="${tableID}">

            <div class="menu-placeholder">
                <div class="fs-2 text-muted mb-2">🍽️</div>
                <h6 class="fw-bold text-secondary">Hệ thống thực đơn trực tuyến</h6>
                <p class="text-muted small mb-0">Chức năng chọn món ăn đang được tích hợp. Bạn có thể bấm nút dưới đây để hoàn tất đặt chỗ ngay mà không cần đặt món trước.</p>
                
                <%-- Sau này code xong Module Menu, bạn chỉ cần ném vòng lặp hoặc thẻ include vào đây --%>
            </div>
            <div class="row g-2">
                <div class="col-12">
                    <button type="submit" class="btn-main">Xác nhận đặt chỗ ngay →</button>
                </div>
            </div>
        </form>
    </c:if>


    <%-- =================================================================
         BƯỚC 3: TRANG XÁC NHẬN THÀNH CÔNG
         ================================================================= --%>
    <c:if test="${step == 'success'}">
        <div class="text-center py-4">
            <div class="display-3 text-success mb-3">🎉</div>
            <h3 class="fw-bold text-success">Đặt bàn thành công!</h3>
            <p class="text-muted">Mã hóa đơn đặt bàn của bạn: <strong>#${order.orderID}</strong></p>

            <div class="bg-light rounded-3 p-3 text-start my-4" style="font-size: .95rem;">
                <div class="d-flex justify-content-between mb-2">
                    <span class="text-secondary">Vị trí bàn:</span>
                    <span class="fw-bold text-dark">${table.tableName} (${table.areaType == 'public' ? 'Ngoài sảnh' : 'Trong phòng'})</span>
                </div>
                <div class="d-flex justify-content-between mb-2">
                    <span class="text-secondary">Thời gian đến:</span>
                    <span class="fw-bold text-dark">
                        <fmt:formatDate value="${order.orderTime}" pattern="HH:mm - dd/MM/yyyy"/>
                    </span>
                </div>
                <div class="d-flex justify-content-between">
                    <span class="text-secondary">Trạng thái đơn:</span>
                    <span class="badge bg-warning text-dark">Chờ xác nhận</span>
                </div>
            </div>

            <div class="d-grid gap-2">
                <a href="${pageContext.request.contextPath}/" class="btn btn-dark rounded-pill">Trở về Trang chủ</a>
                <a href="${pageContext.request.contextPath}/reservation?action=history" class="btn btn-outline-secondary rounded-pill btn-sm">Xem lịch sử đặt bàn</a>
            </div>
        </div>
    </c:if>


    <%-- =================================================================
         LỊCH SỬ ĐẶT BÀN CỦA KHÁCH HÀNG
         ================================================================= --%>
    <c:if test="${step == 'history'}">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h4 class="fw-bold m-0">Lịch sử đặt bàn</h4>
            <a href="${pageContext.request.contextPath}/reservation" class="btn btn-danger btn-sm rounded-pill px-3">Đặt bàn mới</a>
        </div>

        <c:choose>
            <c:when test="${not empty orders}">
                <c:forEach var="o" items="${orders}">
                    <div class="card mb-3 shadow-sm border-0 bg-light">
                        <div class="card-body p-3">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <span class="fw-bold small text-secondary">Mã đơn #${o.orderID}</span>
                                <span class="badge ${o.orderStatus == 'pending' ? 'bg-warning text-dark' : (o.orderStatus == 'reserved' ? 'bg-success' : 'bg-danger')}">
                                    ${o.orderStatus}
                                </span>
                            </div>
                            <p class="mb-1 small">📅 Thời gian đặt: 
                                <strong><fmt:formatDate value="${o.orderTime}" pattern="HH:mm - dd/MM/yyyy"/></strong>
                            </p>
                            <c:if test="${o.orderStatus == 'pending' or o.orderStatus == 'reserved'}">
                                <div class="text-end mt-2">
                                    <a href="${pageContext.request.contextPath}/reservation?action=cancel&orderID=${o.orderID}" 
                                       class="btn btn-link btn-sm text-danger p-0 text-decoration-none" 
                                       onclick="return confirm('Bạn chắc chắn muốn huỷ đơn đặt bàn này?')">Huỷ đặt bàn</a>
                                </div>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="text-center py-5 text-muted">
                    <div style="font-size:2.5rem">📭</div>
                    <p class="mt-2">Bạn chưa có đơn đặt bàn nào.</p>
                    <a href="${pageContext.request.contextPath}/reservation" class="btn btn-outline-danger btn-sm rounded-pill px-4 mt-2">Đặt ngay</a>
                </div>
            </c:otherwise>
        </c:choose>
    </c:if>

</div>
</div>

<script>
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
    function selectTable(card, tableID) {
        document.querySelectorAll('.table-card').forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');
        document.getElementById('tableID').value = tableID;
        document.getElementById('tableError').style.display = 'none';
    }
    function checkTable() {
        if (!document.getElementById('tableID').value) {
            document.getElementById('tableError').style.display = 'block';
            return false;
        }
        return true;
    }
</script>
</body>
</html>