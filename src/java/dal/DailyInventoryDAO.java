/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.sql.PreparedStatement;

/**
 *
 * @author Admin
 */
public class DailyInventoryDAO extends DBContext {

    public boolean updateStockMenuItem(int itemID, int initialQuantity) {
        String sql = "INSERT INTO DailyInventory (itemID, workingDate, initialQuantity, quantityInStock) "
            + "VALUES (?, CURDATE(), ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, itemID);
            ps.setInt(2, initialQuantity);
            ps.setInt(3, initialQuantity);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 1. Method to check current stock (Use this when Customer clicks "Add to cart")
    public int getQuantityInStock(int itemID) {
        String sql = "SELECT quantityInStock FROM DailyInventory WHERE itemID = ? AND workingDate = CURDATE()";
        
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, itemID);
            
            // Assuming you have a ResultSet named 'rs' declared globally in DBContext, 
            // or just declare locally if not.
            java.sql.ResultSet rs = ps.executeQuery(); 
            
            if (rs.next()) {
                return rs.getInt("quantityInStock");
            }
        } catch (Exception e) {
            System.out.println("Error in getQuantityInStock: " + e.getMessage());
        }
        return 0; // Return 0 if the item is out of stock or not found for today
    }

    // 2. Method to deduct stock safely (Use this when Customer clicks "Send to kitchen")
    public boolean decreaseQuantityInStock(int itemID, int quantityToDeduct) {
        // The condition "quantityInStock >= ?" prevents negative inventory 
        // in case multiple tables order the same item at the exact same second.
        String sql = "UPDATE DailyInventory "
                   + "SET quantityInStock = quantityInStock - ? "
                   + "WHERE itemID = ? AND workingDate = CURDATE() AND quantityInStock >= ?";
                   
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, quantityToDeduct);
            ps.setInt(2, itemID);
            ps.setInt(3, quantityToDeduct);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0; // Returns true if stock was successfully deducted
        } catch (Exception e) {
            System.out.println("Error in decreaseQuantityInStock: " + e.getMessage());
            return false;
        }
    }
}
