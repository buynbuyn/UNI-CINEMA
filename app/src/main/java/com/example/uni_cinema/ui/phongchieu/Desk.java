package com.example.uni_cinema.ui.phongchieu;

public class Desk {
    private String idDesk;
    private String categoryName;
    private int price;
    private boolean isAvailable;
    private int rowDesk;    // Hàng ghế
    private int coloumnDesk; // Cột ghế (giữ nguyên lỗi chính tả để khớp Firebase)

    // Constructor với 4 tham số (cho tương thích cũ, nếu cần)
    public Desk(String idDesk, String categoryName, int price, boolean isAvailable) {
        this.idDesk = idDesk;
        this.categoryName = categoryName;
        this.price = price;
        this.isAvailable = isAvailable;
        this.rowDesk = 0;      // Giá trị mặc định
        this.coloumnDesk = 0;  // Giá trị mặc định
    }

    // Constructor với 6 tham số (dùng cho dữ liệu mới từ Firebase)
    public Desk(String idDesk, String categoryName, int price, boolean isAvailable, int rowDesk, int coloumnDesk) {
        this.idDesk = idDesk;
        this.categoryName = categoryName;
        this.price = price;
        this.isAvailable = isAvailable;
        this.rowDesk = rowDesk;
        this.coloumnDesk = coloumnDesk;
    }

    // Getters
    public String getIdDesk() { return idDesk; }
    public String getCategoryName() { return categoryName; }
    public int getPrice() { return price; }
    public boolean isAvailable() { return isAvailable; }
    public int getRowDesk() { return rowDesk; }
    public int getColoumnDesk() { return coloumnDesk; }}