package model;

import java.sql.Timestamp;

public class Notifications {

    private int notificationID;
    private int recipientID;
    private String recipientType;  // customer / staff / owner
    private String type;           // reservation_confirmed / order_ready / shift_reminder...
    private String message;
    private int isRead;
    private Timestamp createdAt;   // DATETIME

    public Notifications() {
    }

    public Notifications(int notificationID, int recipientID, String recipientType,
            String type, String message, int isRead, Timestamp createdAt) {
        this.notificationID = notificationID;
        this.recipientID = recipientID;
        this.recipientType = recipientType;
        this.type = type;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public int getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(int notificationID) {
        this.notificationID = notificationID;
    }

    public int getRecipientID() {
        return recipientID;
    }

    public void setRecipientID(int recipientID) {
        this.recipientID = recipientID;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
