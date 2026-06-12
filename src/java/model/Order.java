package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Order {

private int orderID;
private int customerID;
private int tableID;
private int invoiceID;
private String tableStatus;
private long totalAmount;
private Timestamp checkoutRequestAt;
private int isStaffConfirmed;
private Timestamp createdAt;
private int orderType;
private Timestamp orderTime;
private long depositAmount;
private String orderStatus;
private String areaType;   // thêm thuộc tính 
private int capacity;     //  thêm thuộc tính 

    public Order() {
    }

    public Order(int orderID, int customerID, int tableID, int invoiceID, String tableStatus, long totalAmount, Timestamp checkoutRequestAt, int isStaffConfirmed, Timestamp createdAt, int orderType, Timestamp orderTime, long depositAmount, String orderStatus, String areaType, int capacity) {
        this.orderID = orderID;
        this.customerID = customerID;
        this.tableID = tableID;
        this.invoiceID = invoiceID;
        this.tableStatus = tableStatus;
        this.totalAmount = totalAmount;
        this.checkoutRequestAt = checkoutRequestAt;
        this.isStaffConfirmed = isStaffConfirmed;
        this.createdAt = createdAt;
        this.orderType = orderType;
        this.orderTime = orderTime;
        this.depositAmount = depositAmount;
        this.orderStatus = orderStatus;
        this.areaType = areaType;
        this.capacity = capacity;
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

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
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

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public Timestamp getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Timestamp orderTime) {
        this.orderTime = orderTime;
    }

    public long getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(long depositAmount) {
        this.depositAmount = depositAmount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getAreaType() {
        return areaType;
    }

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    

}
