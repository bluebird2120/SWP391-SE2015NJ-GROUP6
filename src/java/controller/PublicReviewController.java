package controller;

import dal.ReviewDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import model.PublicReview;

@WebServlet(name = "PublicReviewController", urlPatterns = {"/reviews"})
public class PublicReviewController extends HttpServlet {

    private static final String VIEW = "/views/customer/public-reviews.jsp";

    private static final int REVIEW_PAGE_SIZE = 3;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (ReviewDAO reviewDAO = new ReviewDAO()) {
            int totalPublicReviews = reviewDAO.countPublicReviews();
            boolean showAll = "all".equalsIgnoreCase(request.getParameter("show"));
            int limit;

            if (showAll) {
                limit = totalPublicReviews;
            } else {
                limit = REVIEW_PAGE_SIZE;
            }

            List<PublicReview> reviews = reviewDAO.getPublicReviews(limit);

            double averageRating = reviewDAO.getPublicAverageRating();

            request.setAttribute("reviews", reviews);
            request.setAttribute("limit", limit);
            request.setAttribute("showAll", showAll);
            request.setAttribute("hasMoreReviews", !showAll && totalPublicReviews > REVIEW_PAGE_SIZE);
            request.setAttribute("totalPublicReviews", totalPublicReviews);
            request.setAttribute("totalPublicReviewsText", formatTotalReviews(totalPublicReviews));
            request.setAttribute("averageRatingText", formatAverageRating(averageRating));
            request.setAttribute("fullAverageStars", fullAverageStars(averageRating));
            request.setAttribute("hasHalfAverageStar", hasHalfAverageStar(averageRating));
            request.getRequestDispatcher(VIEW).forward(request, response);
        }
    }

    private String formatAverageRating(double averageRating) {
        return String.format(Locale.US, "%.1f", averageRating).replace('.', ',');
    }

    private String formatTotalReviews(int totalReviews) {
        return NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(totalReviews);
    }

    private int fullAverageStars(double averageRating) {
        return Math.max(0, Math.min(5, (int) Math.floor(averageRating)));
    }

    private boolean hasHalfAverageStar(double averageRating) {
        return averageRating - Math.floor(averageRating) >= 0.5 && averageRating < 5;
    }
}
