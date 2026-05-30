package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Reservation {

    private int reservationID;
    private int customerID;
    private Timestamp reservationDateTime;  // DATETIME
    private String reservationStatus;       // pending / confirmed / cancelled / completed
    private int hasPreOrder;
    private BigDecimal depositAmount;
    private String depositStatus;           // unpaid / paid / refunded
    private Timestamp cancelledAt;          // DATETIME
    private String cancellationReason;
    private String tableStatus;             // available / occupied / reserved / cleaning

    public Reservation() {
    }

    public Reservation(int reservationID, int customerID, Timestamp reservationDateTime,
            String reservationStatus, int hasPreOrder, BigDecimal depositAmount,
            String depositStatus, Timestamp cancelledAt, String cancellationReason,
            String tableStatus) {
        this.reservationID = reservationID;
        this.customerID = customerID;
        this.reservationDateTime = reservationDateTime;
        this.reservationStatus = reservationStatus;
        this.hasPreOrder = hasPreOrder;
        this.depositAmount = depositAmount;
        this.depositStatus = depositStatus;
        this.cancelledAt = cancelledAt;
        this.cancellationReason = cancellationReason;
        this.tableStatus = tableStatus;
    }

    public int getReservationID() {
        return reservationID;
    }

    public void setReservationID(int reservationID) {
        this.reservationID = reservationID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public Timestamp getReservationDateTime() {
        return reservationDateTime;
    }

    public void setReservationDateTime(Timestamp reservationDateTime) {
        this.reservationDateTime = reservationDateTime;
    }

    public String getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public int getHasPreOrder() {
        return hasPreOrder;
    }

    public void setHasPreOrder(int hasPreOrder) {
        this.hasPreOrder = hasPreOrder;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public String getDepositStatus() {
        return depositStatus;
    }

    public void setDepositStatus(String depositStatus) {
        this.depositStatus = depositStatus;
    }

    public Timestamp getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Timestamp cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getTableStatus() {
        return tableStatus;
    }

    public void setTableStatus(String tableStatus) {
        this.tableStatus = tableStatus;
    }
}
