package com.example.uni_cinema.ui.voucher;

public class Voucher {
    private String code;
    private String timeStart;
    private String timeEnd;
    private int discount;

    public Voucher() {}

    public Voucher(String code, String timeStart, String timeEnd, int discount) {
        this.code = code;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.discount = discount;
    }

    // Getters
    public String getCode() {
        return code;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public int getDiscount() {
        return discount;
    }

    // Setters
    public void setCode(String code) {
        this.code = code;
    }

    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

}
