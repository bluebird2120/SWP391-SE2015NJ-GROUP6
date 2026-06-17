package model;

public class OrderTable {
    
    private int orderID;
    private int tableID;

    public OrderTable() {
    }

    public OrderTable(int orderID, int tableID) {
        this.orderID = orderID;
        this.tableID = tableID;
    }

    public int getOrderID() { return orderID; }
    public void setOrderID(int orderID) { this.orderID = orderID; }

    public int getTableID() { return tableID; }
    public void setTableID(int tableID) { this.tableID = tableID; }
}