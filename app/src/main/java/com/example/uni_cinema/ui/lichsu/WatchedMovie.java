package com.example.uni_cinema.ui.lichsu;

import java.util.Date;

public class WatchedMovie {
    private String screenRoomName;
    private long totalPrice;
    private Date dateTimeOrder;
    private String orderId;
    private String paymentStatus;
    private String transactionId;

    public WatchedMovie(String screenRoomName, long totalPrice, Date dateTimeOrder,
                        String orderId, String paymentStatus, String transactionId) {
        this.screenRoomName = screenRoomName;
        this.totalPrice = totalPrice;
        this.dateTimeOrder = dateTimeOrder;
        this.orderId = orderId;
        this.paymentStatus = paymentStatus;
        this.transactionId = transactionId;
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

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
