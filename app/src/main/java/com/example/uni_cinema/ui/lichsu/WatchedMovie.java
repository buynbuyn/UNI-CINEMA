package com.example.uni_cinema.ui.lichsu;

import java.util.Date;

public class WatchedMovie {
    private String screenRoomName;
    private long totalPrice;
    private Date dateTimeOrder;

    public WatchedMovie(String screenRoomName, long totalPrice, Date dateTimeOrder) {
        this.screenRoomName = screenRoomName;
        this.totalPrice = totalPrice;
        this.dateTimeOrder = dateTimeOrder;
    }

    public String getScreenRoomName() {
        return screenRoomName;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public Date getDateTimeOrder() {
        return dateTimeOrder;
    }
}
