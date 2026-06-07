package model;

import java.math.BigDecimal;

public class MenuItem {

    private int itemID;
    private int categoryID;
    private String itemName;
    private String description;
    private int price;
    private int discountPercent;
    private int discountedPrice;
    private String image;
    private int isAvailable;
    private String allergyNotes;
    private String categoryName;

    public MenuItem() {
    }

    public MenuItem(int itemID, int categoryID, String itemName, String description, int price, int discountPercent, int discountedPrice, String image, int isAvailable, String allergyNotes, String categoryName) {
        this.itemID = itemID;
        this.categoryID = categoryID;
        this.itemName = itemName;
        this.description = description;
        this.price = price;
        this.discountPercent = discountPercent;
        this.discountedPrice = discountedPrice;
        this.image = image;
        this.isAvailable = isAvailable;
        this.allergyNotes = allergyNotes;
        this.categoryName = categoryName;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(int discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(int isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getAllergyNotes() {
        return allergyNotes;
    }

    public void setAllergyNotes(String allergyNotes) {
        this.allergyNotes = allergyNotes;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

}
