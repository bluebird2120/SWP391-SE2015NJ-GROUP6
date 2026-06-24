/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.util.ArrayList;
import java.util.List;
import model.CookingMethod;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
/**
 *
 * @author Admin
 */
public class CookingMethodDAO extends DBContext{
    
    public List<CookingMethod> getAllCookingMethod(){
        List<CookingMethod> list = new ArrayList<>();
        String sql = "select * from CookingMethod";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                CookingMethod cm = new CookingMethod(
                        rs.getInt("methodID"),
                        rs.getString("methodName"));
                list.add(cm);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
