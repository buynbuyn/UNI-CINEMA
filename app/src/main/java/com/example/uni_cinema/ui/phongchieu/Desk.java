package com.example.uni_cinema.ui.phongchieu;

public class Desk {
    private String idDesk;          // Mã số ghế từ document Desk
    private int row;                // Hàng của ghế
    private int column;             // Cột của ghế
    private int state;              // Trạng thái ghế
    private String categoryName;    // Tên thể loại từ deskCategories
    private int price;              // Giá tiền
    private int point;              // Điểm từ deskCategories

    public Desk() {
        // Constructor mặc định cho Firestore
    }

    public Desk(String idDesk, String seatId, int row, int column, int state, String categoryName, int price, int point) {
        this.idDesk = idDesk;
        this.row = row;
        this.column = column;
        this.state = state;
        this.categoryName = categoryName;
        this.price = price;
        this.point = point;
    }

    // Getters
    public String getIdDesk() { return idDesk; }
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public int getState() { return state; }
    public String getCategoryName() { return categoryName; }
    public int getPrice() { return price; }
    public int getPoint() { return point; }

    // Setters
    public void setIdDesk(String idDesk) { this.idDesk = idDesk; }
    public void setRow(int row) { this.row = row; }
    public void setColumn(int column) { this.column = column; }
    public void setState(int state) { this.state = state; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setPrice(int price) { this.price = price; }
    public void setPoint(int point) { this.point = point; }

    public boolean isAvailable() { return state == 0; }

    @Override
    public String toString() {
        return "Seat{" +
                "idDesk='" + idDesk + '\'' +
                ", row=" + row +
                ", column=" + column +
                ", state=" + state +
                ", categoryName='" + categoryName + '\'' +
                ", price=" + price +
                ", point=" + point +
                '}';
    }
}