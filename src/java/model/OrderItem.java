package model;

public class OrderItem {

    private int orderItemID;
    private int orderID;
    private int itemID;
    private Integer tableID; // THÊM MỚI: Dùng Integer để chấp nhận null
    private int quantity;
    private int price;
    private String note;

    public OrderItem() {
    }

    public OrderItem(int orderItemID, int orderID, int itemID, Integer tableID, 
                     int quantity, int price, String note) {
        this.orderItemID = orderItemID;
        this.orderID = orderID;
        this.itemID = itemID;
        this.tableID = tableID;
        this.quantity = quantity;
        this.price = price;
        this.note = note;
    }

    public int getOrderItemID() { return orderItemID; }
    public void setOrderItemID(int orderItemID) { this.orderItemID = orderItemID; }

    public int getOrderID() { return orderID; }
    public void setOrderID(int orderID) { this.orderID = orderID; }

    public int getItemID() { return itemID; }
    public void setItemID(int itemID) { this.itemID = itemID; }

    public Integer getTableID() { return tableID; }
    public void setTableID(Integer tableID) { this.tableID = tableID; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}