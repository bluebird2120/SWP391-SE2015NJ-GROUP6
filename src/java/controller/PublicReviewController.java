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
    private static final int DEFAULT_LIMIT = 30;
    private static final int MAX_LIMIT = 100;

    private final ReviewDAO reviewDAO = new ReviewDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int limit = parseLimit(request.getParameter("limit"));
        List<PublicReview> reviews = reviewDAO.getPublicReviews(limit);
        int totalPublicReviews = reviewDAO.countPublicReviews();
        double averageRating = reviewDAO.getPublicAverageRating();

        request.setAttribute("reviews", reviews);
        request.setAttribute("limit", limit);
        request.setAttribute("totalPublicReviews", totalPublicReviews);
        request.setAttribute("totalPublicReviewsText", formatTotalReviews(totalPublicReviews));
        request.setAttribute("averageRatingText", formatAverageRating(averageRating));
        request.setAttribute("fullAverageStars", fullAverageStars(averageRating));
        request.setAttribute("hasHalfAverageStar", hasHalfAverageStar(averageRating));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private int parseLimit(String rawLimit) {
        if (rawLimit == null || rawLimit.isBlank()) {
            return DEFAULT_LIMIT;
        }
        try {
            int parsed = Integer.parseInt(rawLimit.trim());
            if (parsed < 1) {
                return DEFAULT_LIMIT;
            }
            return Math.min(parsed, MAX_LIMIT);
        } catch (NumberFormatException ex) {
            return DEFAULT_LIMIT;
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
