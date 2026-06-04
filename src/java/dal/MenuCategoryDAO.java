/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.MenuCategory;

/**
 *
 * @author Admin
 */
public class MenuCategoryDAO extends DBContext{

    public List<MenuCategory> getAllMenuCategory() {

        List<MenuCategory> list = new ArrayList<>();
        String sql = "select * from MenuCategory";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MenuCategory mc = new MenuCategory(
                        rs.getInt("categoryID"), 
                        rs.getString("categoryName"));
                list.add(mc);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
