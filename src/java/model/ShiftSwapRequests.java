package model;

import java.sql.Timestamp;

public class ShiftSwapRequests {

    private int swapID;
    private int requesterShiftID;
    private int targetShiftID;
    private int approvedByID;
    private String status;     // pending / approved / rejected
    private String reason;
    private Timestamp createdAt;  // DATETIME

    public ShiftSwapRequests() {
    }

    public ShiftSwapRequests(int swapID, int requesterShiftID, int targetShiftID,
            int approvedByID, String status, String reason, Timestamp createdAt) {
        this.swapID = swapID;
        this.requesterShiftID = requesterShiftID;
        this.targetShiftID = targetShiftID;
        this.approvedByID = approvedByID;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public int getSwapID() {
        return swapID;
    }

    public void setSwapID(int swapID) {
        this.swapID = swapID;
    }

    public int getRequesterShiftID() {
        return requesterShiftID;
    }

    public void setRequesterShiftID(int requesterShiftID) {
        this.requesterShiftID = requesterShiftID;
    }

    public int getTargetShiftID() {
        return targetShiftID;
    }

    public void setTargetShiftID(int targetShiftID) {
        this.targetShiftID = targetShiftID;
    }

    public int getApprovedByID() {
        return approvedByID;
    }

    public void setApprovedByID(int approvedByID) {
        this.approvedByID = approvedByID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
