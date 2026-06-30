package model;

import java.sql.Timestamp;

/**
 * Du lieu rieng cho man hinh van hanh ban cua nhan vien.
 */
public class StaffTableDTO {

    private int tableID;
    private String tableName;
    private int capacity;
    private String areaType;
    private String physicalStatus;
    private Integer orderID;
    private String orderStatus;
    private String tableStatus;
    private Timestamp orderTime;
    private int requiredQuantity;
    private int assignedQuantity;
    private String assignedTableNames;

    public int getTableID() { return tableID; }
    public void setTableID(int tableID) { this.tableID = tableID; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }
    public String getPhysicalStatus() { return physicalStatus; }
    public void setPhysicalStatus(String physicalStatus) { this.physicalStatus = physicalStatus; }
    public Integer getOrderID() { return orderID; }
    public void setOrderID(Integer orderID) { this.orderID = orderID; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getTableStatus() { return tableStatus; }
    public void setTableStatus(String tableStatus) { this.tableStatus = tableStatus; }
    public Timestamp getOrderTime() { return orderTime; }
    public void setOrderTime(Timestamp orderTime) { this.orderTime = orderTime; }
    public int getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(int requiredQuantity) { this.requiredQuantity = requiredQuantity; }
    public int getAssignedQuantity() { return assignedQuantity; }
    public void setAssignedQuantity(int assignedQuantity) { this.assignedQuantity = assignedQuantity; }
    public String getAssignedTableNames() { return assignedTableNames; }
    public void setAssignedTableNames(String assignedTableNames) {
        this.assignedTableNames = assignedTableNames;
    }
    public int getRemainingQuantity() {
        return Math.max(0, requiredQuantity - assignedQuantity);
    }
}
