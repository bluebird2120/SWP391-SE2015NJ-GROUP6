package model;

import java.sql.Timestamp;

public class TableJoinRequest {
    private int requestID;
    private int orderID;
    private String guestSessionID;
    private String guestName;
    private String status;
    private Timestamp createdAt;

    public TableJoinRequest() {
    }

    public TableJoinRequest(int requestID, int orderID, String guestSessionID, String guestName, String status, Timestamp createdAt) {
        this.requestID = requestID;
        this.orderID = orderID;
        this.guestSessionID = guestSessionID;
        this.guestName = guestName;
        this.status = status;
        this.createdAt = createdAt;
    }

    // --- Getters & Setters ---
    public int getRequestID() { return requestID; }
    public void setRequestID(int requestID) { this.requestID = requestID; }

    public int getOrderID() { return orderID; }
    public void setOrderID(int orderID) { this.orderID = orderID; }

    public String getGuestSessionID() { return guestSessionID; }
    public void setGuestSessionID(String guestSessionID) { this.guestSessionID = guestSessionID; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}