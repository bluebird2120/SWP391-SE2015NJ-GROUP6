package model;

import java.sql.Timestamp;

public class Reviews {

    private int reviewID;
    private int customerID;
    private int rating;
    private String comment;
    private Timestamp createdAt;  // DATETIME

    public Reviews() {
    }

    public Reviews(int reviewID, int customerID, int rating, String comment, Timestamp createdAt) {
        this.reviewID = reviewID;
        this.customerID = customerID;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public int getReviewID() {
        return reviewID;
    }

    public void setReviewID(int reviewID) {
        this.reviewID = reviewID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
