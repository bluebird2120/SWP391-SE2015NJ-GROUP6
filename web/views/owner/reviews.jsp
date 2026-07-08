<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý đánh giá - Owner</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: 'Inter', sans-serif; background: #faf6f2; color: #4a3528; }
        .main { flex: 1; padding: 28px 36px; }
        .page-title { font-family: 'Playfair Display', serif; color: #76493b; font-size: 1.8rem; margin: 0 0 6px; }
        .page-sub { color: #a0714f; font-size: 0.95rem; margin-bottom: 18px; }
        .alert { border-radius: 10px; padding: 10px 14px; margin-bottom: 14px; font-size: 0.9rem; }
        .alert-danger { background: #fff1f1; border: 1px solid #f3c1c1; color: #9f2727; }
        .alert-success { background: #effaf1; border: 1px solid #bfe7c5; color: #226c34; }
        .rating-report { background:#fff; border:1px solid #ede0d8; border-radius:12px; padding:16px 18px; margin-bottom:18px; box-shadow:0 4px 16px rgba(118,73,59,0.06); }
        .rating-report-head { display:flex; justify-content:space-between; gap:12px; align-items:center; flex-wrap:wrap; margin-bottom:12px; }
        .rating-report-title { color:#5d3a2e; font-weight:700; }
        .rating-report-clear { color:#76493b; background:#f4ebe4; border:1px solid #e4d3c4; border-radius:999px; padding:6px 11px; text-decoration:none; font-size:0.82rem; font-weight:700; }
        .rating-report-clear.active { background:#76493b; color:#fff; border-color:#76493b; }
        .rating-row { display:grid; grid-template-columns:58px 86px minmax(120px, 1fr) 42px; gap:10px; align-items:center; min-height:28px; padding:4px 8px; border-radius:8px; text-decoration:none; color:#4a3528; }
        .rating-row:hover, .rating-row.active { background:#fff7e6; }
        .rating-label { font-size:0.9rem; white-space:nowrap; }
        .rating-stars { color:#d49b2f; font-size:0.82rem; white-space:nowrap; }
        .rating-bar-track { height:17px; background:#f4ebe4; border-radius:3px; overflow:hidden; }
        .rating-bar-fill { height:100%; min-width:0; background:#76493b; border-radius:3px; }
        .rating-row.active .rating-bar-fill { background:#d49b2f; }
        .rating-count { font-size:0.9rem; font-weight:700; color:#5d3a2e; }
        .review-list { display: flex; flex-direction: column; gap: 14px; }
        .review-card { background: #fff; border: 1px solid #ede0d8; border-radius: 12px; padding: 16px 18px; box-shadow: 0 4px 16px rgba(118,73,59,0.06); }
        .review-card.hidden { background: #fbf3f3; border-color: #ecc7c7; }
        .review-head { display: flex; justify-content: space-between; gap: 12px; flex-wrap: wrap; align-items: flex-start; margin-bottom: 8px; }
        .review-head .left { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
        .badge { font-size: 0.72rem; padding: 3px 9px; border-radius: 999px; font-weight: 600; }
        .badge-visible { background: #effaf1; color: #226c34; }
        .badge-hidden { background: #f4dada; color: #9f2727; }
        .stars { color: #d49b2f; font-size: 0.95rem; }
        .meta { color: #9a745c; font-size: 0.78rem; }
        .comment { background: #fdf6ec; border-radius: 8px; padding: 10px 12px; margin: 8px 0 12px; white-space: pre-wrap; line-height: 1.55; }
        .reply-box { background: #fff7e6; border-left: 3px solid #d49b2f; border-radius: 6px; padding: 10px 12px; margin-bottom: 10px; }
        .reply-box .label { font-weight: 700; color: #76493b; font-size: 0.82rem; margin-bottom: 4px; }
        .actions { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
        .inline-form { display: inline-block; }
        .reply-form { width: 100%; margin-top: 8px; display: flex; flex-direction: column; gap: 6px; }
        .reply-form textarea { width: 100%; min-height: 80px; padding: 10px 12px; border: 1px solid #e4d3c4; border-radius: 8px; font-family: inherit; resize: vertical; color: #4a3528; }
        .reply-form textarea:focus { outline: none; border-color: #a0714f; box-shadow: 0 0 0 3px rgba(160,113,79,.14); }
        .btn { border: none; border-radius: 999px; padding: 8px 14px; font-weight: 600; cursor: pointer; font-size: 0.85rem; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; }
        .btn-primary { background: #76493b; color: #fff; }
        .btn-primary:hover { background: #5d3a2e; }
        .btn-muted { background: #f4ebe4; color: #76493b; }
        .btn-warn { background: #fff1d4; color: #9b6700; }
        .btn-danger { background: #fff1f1; color: #b42323; }
        .empty { text-align: center; padding: 42px 20px; color: #8a6e5a; border: 1px dashed #decabd; border-radius: 12px; background: #fff; }
        .pagination { display: flex; gap: 6px; justify-content: center; margin-top: 22px; flex-wrap: wrap; }
        .pagination a, .pagination span { padding: 7px 12px; border-radius: 8px; text-decoration: none; font-size: 0.85rem; border: 1px solid #ede0d8; color: #76493b; background: #fff; }
        .pagination .active { background: #76493b; color: #fff; border-color: #76493b; }
        .confirm-panel { display:none; position:fixed; inset:0; z-index:3000; background:rgba(74,53,40,.34); align-items:center; justify-content:center; padding:20px; }
        .confirm-panel.active { display:flex; }
        .confirm-box { background:#fff; border:1px solid #ede0d8; border-radius:10px; padding:18px; max-width:380px; width:100%; box-shadow:0 16px 40px rgba(74,53,40,.18); }
        .confirm-message { color:#4a3528; font-weight:600; margin-bottom:14px; }
        .confirm-actions { display:flex; justify-content:flex-end; gap:8px; }
        @media (max-width: 700px) {
            .main { padding: 22px 18px; }
            .rating-row { grid-template-columns:52px 78px minmax(90px, 1fr) 34px; gap:8px; }
            .rating-stars { font-size:0.76rem; }
        }
    </style>
</head>
<body>
    <%@ include file="/views/includes/header.jsp" %>
    <div style="display: flex;">
        <%@ include file="/views/includes/dashboard.jsp" %>
        <main class="main">
            <h1 class="page-title">Quản lý đánh giá</h1>
            <p class="page-sub">Tổng cộng <c:out value="${totalReviews}" /> đánh giá. Ẩn nội dung không phù hợp hoặc phản hồi công khai từ nhà hàng.</p>

            <c:if test="${not empty param.error}">
                <div class="alert alert-danger"><c:out value="${param.error}" /></div>
            </c:if>
            <c:if test="${not empty param.success}">
                <div class="alert alert-success">
                    <c:choose>
                        <c:when test="${param.success == 'hidden'}">Đã ẩn đánh giá.</c:when>
                        <c:when test="${param.success == 'unhidden'}">Đã hiện lại đánh giá.</c:when>
                        <c:when test="${param.success == 'replied'}">Đã lưu phản hồi.</c:when>
                        <c:when test="${param.success == 'reply_deleted'}">Đã xóa phản hồi.</c:when>
                        <c:otherwise>Thao tác đã được xử lý.</c:otherwise>
                    </c:choose>
                </div>
            </c:if>

            <section class="rating-report">
                <div class="rating-report-head">
                    <div class="rating-report-title">Báo cáo đánh giá</div>
                    <c:url var="allReviewsUrl" value="/owner/reviews" />
                    <a class="rating-report-clear ${selectedRating == 0 ? 'active' : ''}" href="${allReviewsUrl}">
                        Tất cả đánh giá
                    </a>
                </div>
                <c:forEach var="level" items="${ratingLevels}">
                    <c:url var="ratingUrl" value="/owner/reviews">
                        <c:param name="rating" value="${level}" />
                    </c:url>
                    <a class="rating-row ${selectedRating == level ? 'active' : ''}" href="${ratingUrl}">
                        <span class="rating-label">${level} sao</span>
                        <span class="rating-stars">
                            <c:forEach var="star" begin="1" end="5">
                                <i class="${star <= level ? 'fa-solid' : 'fa-regular'} fa-star"></i>
                            </c:forEach>
                        </span>
                        <span class="rating-bar-track">
                            <span class="rating-bar-fill" style="width:${maxRatingCount == 0 ? 0 : ratingCounts[level] * 100 / maxRatingCount}%;"></span>
                        </span>
                        <span class="rating-count"><c:out value="${ratingCounts[level]}" /></span>
                    </a>
                </c:forEach>
            </section>

            <c:choose>
                <c:when test="${empty reviews}">
                    <div class="empty">
                        <i class="fa-regular fa-comment fa-2x" style="color:#d7bfa4;"></i>
                        <p>Chưa có đánh giá nào.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="review-list">
                        <c:forEach var="review" items="${reviews}" varStatus="status">
                            <article class="review-card ${review.isHidden == 1 ? 'hidden' : ''} ${status.index >= 3 ? 'previous-review' : ''}"
                                     style="${status.index >= 3 ? 'display:none;' : ''}">
                                <div class="review-head">
                                    <div class="left">
                                        <strong>#<c:out value="${review.reviewID}" /></strong>
                                        <span><i class="fa-solid fa-user" style="color:#a0714f;"></i> <c:out value="${review.customerUserName}" /></span>
                                        <span class="meta">Đơn #<c:out value="${review.orderID}" /></span>
                                        <span class="meta"><fmt:formatDate value="${review.createdAt}" pattern="dd/MM/yyyy HH:mm" /></span>
                                    </div>
                                    <div>
                                        <c:choose>
                                            <c:when test="${review.isHidden == 1}">
                                                <span class="badge badge-hidden"><i class="fa-solid fa-eye-slash"></i> Đã ẩn</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-visible"><i class="fa-solid fa-eye"></i> Đang hiển thị</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="stars">
                                    <c:forEach var="star" begin="1" end="5">
                                        <i class="${star <= review.rating ? 'fa-solid' : 'fa-regular'} fa-star"></i>
                                    </c:forEach>
                                </div>
                                <div class="comment"><c:out value="${review.comment}" /></div>

                                <c:if test="${not empty review.ownerReply}">
                                    <div class="reply-box">
                                        <div class="label"><i class="fa-solid fa-reply"></i> Phản hồi từ nhà hàng
                                            <span class="meta" style="font-weight:400; margin-left:6px;">
                                                <fmt:formatDate value="${review.ownerReplyAt}" pattern="dd/MM/yyyy HH:mm" />
                                            </span>
                                        </div>
                                        <div><c:out value="${review.ownerReply}" /></div>
                                    </div>
                                </c:if>

                                <div class="actions">
                                    <c:choose>
                                        <c:when test="${review.isHidden == 1}">
                                            <form class="inline-form" method="post" action="${pageContext.request.contextPath}/owner/reviews">
                                                <input type="hidden" name="action" value="unhide">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="reviewID" value="${review.reviewID}">
                                                <input type="hidden" name="page" value="${currentPage}">
                                                <input type="hidden" name="rating" value="${selectedRating}">
                                                <button type="submit" class="btn btn-muted"><i class="fa-solid fa-eye"></i> Hiện lại</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <form class="inline-form" method="post" action="${pageContext.request.contextPath}/owner/reviews"
                                                  onsubmit="return showLocalConfirm(this, 'Ẩn đánh giá này khỏi trang công khai?')">
                                                <input type="hidden" name="action" value="hide">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="reviewID" value="${review.reviewID}">
                                                <input type="hidden" name="page" value="${currentPage}">
                                                <input type="hidden" name="rating" value="${selectedRating}">
                                                <button type="submit" class="btn btn-warn"><i class="fa-solid fa-eye-slash"></i> Ẩn</button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
                                    <c:if test="${not empty review.ownerReply}">
                                        <form class="inline-form" method="post" action="${pageContext.request.contextPath}/owner/reviews"
                                              onsubmit="return showLocalConfirm(this, 'Xóa phản hồi này?')">
                                            <input type="hidden" name="action" value="delete-reply">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="reviewID" value="${review.reviewID}">
                                            <input type="hidden" name="page" value="${currentPage}">
                                            <input type="hidden" name="rating" value="${selectedRating}">
                                            <button type="submit" class="btn btn-danger"><i class="fa-solid fa-trash"></i> Xóa phản hồi</button>
                                        </form>
                                    </c:if>
                                </div>

                                <form class="reply-form" method="post" action="${pageContext.request.contextPath}/owner/reviews">
                                    <input type="hidden" name="action" value="reply">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="reviewID" value="${review.reviewID}">
                                    <input type="hidden" name="page" value="${currentPage}">
                                    <input type="hidden" name="rating" value="${selectedRating}">
                                    <textarea name="reply" maxlength="1000"
                                              placeholder="Nhập phản hồi công khai..."><c:out value="${review.ownerReply}" /></textarea>
                                    <div>
                                        <button type="submit" class="btn btn-primary">
                                            <i class="fa-solid fa-paper-plane"></i>
                                            <c:choose>
                                                <c:when test="${not empty review.ownerReply}">Cập nhật phản hồi</c:when>
                                                <c:otherwise>Gửi phản hồi</c:otherwise>
                                            </c:choose>
                                        </button>
                                    </div>
                                </form>
                            </article>
                        </c:forEach>
                    </div>

                    <c:if test="${fn:length(reviews) > 3}">
                        <div id="showMoreReviewsContainer" style="text-align: center; margin-top: 18px;">
                            <button type="button" id="btnShowMoreReviews" onclick="showAllReviews()" style="background: none; border: none; color: #76493b; font-weight: 600; cursor: pointer; font-size: 0.9rem; display: inline-flex; align-items: center; gap: 6px; padding: 8px 16px; transition: all 0.2s; text-decoration: underline;">
                                Xem các đánh giá trước đó <i class="fas fa-chevron-down" style="font-size: 0.8rem;"></i>
                            </button>
                        </div>
                    </c:if>

                    <c:if test="${totalPages > 1}">
                        <nav class="pagination">
                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <c:choose>
                                    <c:when test="${i == currentPage}">
                                        <span class="active">${i}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <c:url var="pageUrl" value="/owner/reviews">
                                            <c:param name="page" value="${i}" />
                                            <c:if test="${selectedRating > 0}">
                                                <c:param name="rating" value="${selectedRating}" />
                                            </c:if>
                                        </c:url>
                                        <a href="${pageUrl}">${i}</a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </nav>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </main>
    </div>
    <%@ include file="/views/includes/footer.jsp" %>
    <div id="localConfirmPanel" class="confirm-panel">
        <div class="confirm-box">
            <div id="localConfirmMessage" class="confirm-message"></div>
            <div class="confirm-actions">
                <button type="button" class="btn btn-muted" onclick="closeLocalConfirm()">Hủy</button>
                <button type="button" class="btn btn-danger" onclick="submitLocalConfirm()">Đồng ý</button>
            </div>
        </div>
    </div>
    <script>
        var pendingConfirmForm = null;
        var confirmSubmitting = false;

        function showLocalConfirm(form, message) {
            if (confirmSubmitting) {
                confirmSubmitting = false;
                return true;
            }
            pendingConfirmForm = form;
            document.getElementById('localConfirmMessage').textContent = message;
            document.getElementById('localConfirmPanel').classList.add('active');
            return false;
        }

        function closeLocalConfirm() {
            pendingConfirmForm = null;
            document.getElementById('localConfirmPanel').classList.remove('active');
        }

        function submitLocalConfirm() {
            if (!pendingConfirmForm) return;
            confirmSubmitting = true;
            pendingConfirmForm.submit();
        }

        function showAllReviews() {
            var prevReviews = document.querySelectorAll('.previous-review');
            prevReviews.forEach(function(el) {
                el.style.display = 'block';
                el.style.opacity = '0';
                el.style.transition = 'opacity 0.3s ease';
                setTimeout(function() {
                    el.style.opacity = '1';
                }, 10);
            });
            var container = document.getElementById('showMoreReviewsContainer');
            if (container) {
                container.style.display = 'none';
            }
        }
    </script>
</body>
</html>
