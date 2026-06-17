package model;

import java.util.Date;

public class DailyInventory {

    private int inventoryID;
    private int itemID;
    private Date workingDate;
    private int quantityInStock;

    public DailyInventory() {
    }

    public DailyInventory(int inventoryID, int itemID, Date workingDate, int quantityInStock) {
        this.inventoryID = inventoryID;
        this.itemID = itemID;
        this.workingDate = workingDate;
        this.quantityInStock = quantityInStock;
    }

    public int getInventoryID() {
        return inventoryID;
    }

    public void setInventoryID(int inventoryID) {
        this.inventoryID = inventoryID;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public Date getWorkingDate() {
        return workingDate;
    }

    public void setWorkingDate(Date workingDate) {
        this.workingDate = workingDate;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }
}
