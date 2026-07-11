/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author admin
 */
public class HourStat {

    private int hour;
    private int orderCount;

    public HourStat(int hour, int orderCount) {
        this.hour = hour;
        this.orderCount = orderCount;
    }

    public int getHour() {
        return hour;
    }

    public int getOrderCount() {
        return orderCount;
    }
}
