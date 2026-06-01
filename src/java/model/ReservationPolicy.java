package model;

import java.sql.Timestamp;

public class ReservationPolicy {

    private int policyID;
    private String policyKey;
    private String policyValue;
    private String description;
    private Timestamp updatedAt;  // DATETIME

    public ReservationPolicy() {
    }

    public ReservationPolicy(int policyID, String policyKey, String policyValue,
            String description, Timestamp updatedAt) {
        this.policyID = policyID;
        this.policyKey = policyKey;
        this.policyValue = policyValue;
        this.description = description;
        this.updatedAt = updatedAt;
    }

    public int getPolicyID() {
        return policyID;
    }

    public void setPolicyID(int policyID) {
        this.policyID = policyID;
    }

    public String getPolicyKey() {
        return policyKey;
    }

    public void setPolicyKey(String policyKey) {
        this.policyKey = policyKey;
    }

    public String getPolicyValue() {
        return policyValue;
    }

    public void setPolicyValue(String policyValue) {
        this.policyValue = policyValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
