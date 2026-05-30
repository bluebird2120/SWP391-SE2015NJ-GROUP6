package model;

import java.math.BigDecimal;

public class MenuItem {

    private int itemID;
    private int categoryID;
    private String itemName;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPercent;
    private BigDecimal discountedPrice;
    private String image;
    private int isAvailable;
    private String allergyNotes;

    public MenuItem() {
    }

    public MenuItem(int itemID, int categoryID, String itemName, String description,
            BigDecimal price, BigDecimal discountPercent, BigDecimal discountedPrice,
            String image, int isAvailable, String allergyNotes) {
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(BigDecimal discountedPrice) {
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
}
