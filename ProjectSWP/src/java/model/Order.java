package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Order {

    private int orderID;
    private int customerID;
    private int reservationID;
    private int tableID;
    private int invoiceID;
    private String tableStatus;          // available / occupied / reserved / cleaning
    private String orderStatus;          // pending / confirmed / preparing / served / checkout
    private BigDecimal totalAmount;
    private Timestamp checkoutRequestAt; // DATETIME
    private int isStaffConfirmed;
    private Timestamp createdAt;         // DATETIME

    public Order() {
    }

    public Order(int orderID, int customerID, int reservationID, int tableID, int invoiceID,
            String tableStatus, String orderStatus, BigDecimal totalAmount,
            Timestamp checkoutRequestAt, int isStaffConfirmed, Timestamp createdAt) {
        this.orderID = orderID;
        this.customerID = customerID;
        this.reservationID = reservationID;
        this.tableID = tableID;
        this.invoiceID = invoiceID;
        this.tableStatus = tableStatus;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.checkoutRequestAt = checkoutRequestAt;
        this.isStaffConfirmed = isStaffConfirmed;
        this.createdAt = createdAt;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getReservationID() {
        return reservationID;
    }

    public void setReservationID(int reservationID) {
        this.reservationID = reservationID;
    }

    public int getTableID() {
        return tableID;
    }

    public void setTableID(int tableID) {
        this.tableID = tableID;
    }

    public int getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(int invoiceID) {
        this.invoiceID = invoiceID;
    }

    public String getTableStatus() {
        return tableStatus;
    }

    public void setTableStatus(String tableStatus) {
        this.tableStatus = tableStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getCheckoutRequestAt() {
        return checkoutRequestAt;
    }

    public void setCheckoutRequestAt(Timestamp checkoutRequestAt) {
        this.checkoutRequestAt = checkoutRequestAt;
    }

    public int getIsStaffConfirmed() {
        return isStaffConfirmed;
    }

    public void setIsStaffConfirmed(int isStaffConfirmed) {
        this.isStaffConfirmed = isStaffConfirmed;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
