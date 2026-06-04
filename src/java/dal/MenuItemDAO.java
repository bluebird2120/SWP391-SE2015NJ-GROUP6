/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import model.MenuItem;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

/**
 *
 * @author Admin
 */
public class MenuItemDAO extends DBContext {

    public List<MenuItem> getAllMenuItem() {

        List<MenuItem> list = new ArrayList<>();
        String sql = "select * from MenuItem mi "
                + "join MenuCategory mc on mi.categoryID = mc.categoryID";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MenuItem mi = new MenuItem(rs.getInt("itemID"),
                        rs.getInt("categoryID"),
                        rs.getString("itemName"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getBigDecimal("discountPercent"),
                        rs.getBigDecimal("discountedPrice"),
                        rs.getString("image"),
                        rs.getInt("isAvailable"),
                        rs.getString("allergyNotes"),
                        rs.getString("categoryName"));

                list.add(mi);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<MenuItem> searchMenuItem(String search, int categoryId, int status,
            BigDecimal minPrice, BigDecimal maxPrice, String sort, String priceType) {

        List<MenuItem> list = new ArrayList<>();
        String sql = "select * from MenuItem mi "
                + "join MenuCategory mc on mi.categoryID = mc.categoryID "
                + "where mi.itemName like ? ";
        if (categoryId > 0) {
            sql += "and mc.categoryId = ? ";
        }
        if (priceType.equals("price")) {
            sql += "and mi.isAvailable = ? "
                    + "and mi.price >= ? "
                    + "and mi.price <= ? "
                    + "order by price " + sort;           
        } else {
            sql += "and mi.isAvailable = ? "
                    + "and mi.discountedPrice >= ? "
                    + "and mi.discountedPrice <= ? "
                    + "order by discountedPrice " + sort;       
        }
        System.out.println(sql);
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            if(categoryId > 0){
                ps.setString(1, "%" + search + "%");
                ps.setInt(2, categoryId);
                ps.setInt(3, status);
                ps.setBigDecimal(4, minPrice);
                ps.setBigDecimal(5, maxPrice);
            }else{
                ps.setString(1,  "%" + search + "%");
                ps.setInt(2, status);
                ps.setBigDecimal(3, minPrice);
                ps.setBigDecimal(4, maxPrice);
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MenuItem mi = new MenuItem(rs.getInt("itemID"),
                        rs.getInt("categoryID"),
                        rs.getString("itemName"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getBigDecimal("discountPercent"),
                        rs.getBigDecimal("discountedPrice"),
                        rs.getString("image"),
                        rs.getInt("isAvailable"),
                        rs.getString("allergyNotes"),
                        rs.getString("categoryName"));

                list.add(mi);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public MenuItem getMenuItemById(int id){
        MenuItem mi;
        String sql = "select * from MenuItem mi "
                + "join MenuCategory mc on mi.categoryID = mc.categoryID "
                + "where mi.itemID = ?";
        try { 
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();  
            if(rs.next()){
                mi = new MenuItem(rs.getInt("itemID"),
                        rs.getInt("categoryID"),
                        rs.getString("itemName"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getBigDecimal("discountPercent"),
                        rs.getBigDecimal("discountedPrice"),
                        rs.getString("image"),
                        rs.getInt("isAvailable"),
                        rs.getString("allergyNotes"),
                        rs.getString("categoryName"));
                return mi;
            }
        } catch (Exception e) {
        }
        return null;
    }
}
