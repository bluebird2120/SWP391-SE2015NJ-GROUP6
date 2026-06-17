package model;

import java.sql.Timestamp;

public class Order {

    private int orderID;
    private Integer customerID; // Dùng Integer để chấp nhận null
    private Integer employeeID; // THÊM MỚI: Liên kết với nhân viên phụ trách
    private Integer invoiceID;
    // Đã xóa: private int tableID;
    private int orderType;
    private String tableStatus;
    private long totalAmount; 
    private Timestamp checkoutRequestAt;
    private int isStaffConfirmed;
    private Timestamp createdAt;
    private Timestamp orderTime;
    private long depositAmount; 
    private String orderStatus;

    public Order() {
    }

    public Order(int orderID, Integer customerID, Integer employeeID, Integer invoiceID, 
                 int orderType, String tableStatus, long totalAmount, Timestamp checkoutRequestAt, 
                 int isStaffConfirmed, Timestamp createdAt, Timestamp orderTime, 
                 long depositAmount, String orderStatus) {
        this.orderID = orderID;
        this.customerID = customerID;
        this.employeeID = employeeID;
        this.invoiceID = invoiceID;
        this.orderType = orderType;
        this.tableStatus = tableStatus;
        this.totalAmount = totalAmount;
        this.checkoutRequestAt = checkoutRequestAt;
        this.isStaffConfirmed = isStaffConfirmed;
        this.createdAt = createdAt;
        this.orderTime = orderTime;
        this.depositAmount = depositAmount;
        this.orderStatus = orderStatus;
    }

    // --- Getters & Setters ---
    public int getOrderID() { return orderID; }
    public void setOrderID(int orderID) { this.orderID = orderID; }

    public Integer getCustomerID() { return customerID; }
    public void setCustomerID(Integer customerID) { this.customerID = customerID; }

    public Integer getEmployeeID() { return employeeID; }
    public void setEmployeeID(Integer employeeID) { this.employeeID = employeeID; }

    public Integer getInvoiceID() { return invoiceID; }
    public void setInvoiceID(Integer invoiceID) { this.invoiceID = invoiceID; }

    public int getOrderType() { return orderType; }
    public void setOrderType(int orderType) { this.orderType = orderType; }

    public String getTableStatus() { return tableStatus; }
    public void setTableStatus(String tableStatus) { this.tableStatus = tableStatus; }

    public long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(long totalAmount) { this.totalAmount = totalAmount; }

    public Timestamp getCheckoutRequestAt() { return checkoutRequestAt; }
    public void setCheckoutRequestAt(Timestamp checkoutRequestAt) { this.checkoutRequestAt = checkoutRequestAt; }

    public int getIsStaffConfirmed() { return isStaffConfirmed; }
    public void setIsStaffConfirmed(int isStaffConfirmed) { this.isStaffConfirmed = isStaffConfirmed; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getOrderTime() { return orderTime; }
    public void setOrderTime(Timestamp orderTime) { this.orderTime = orderTime; }

    public long getDepositAmount() { return depositAmount; }
    public void setDepositAmount(long depositAmount) { this.depositAmount = depositAmount; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
}