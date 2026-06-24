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
        }
        return false;
    }
}
