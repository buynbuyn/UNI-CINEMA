package com.example.uni_cinema.ui.home;

public class Movie {
    private String title;
    private String imageUrl; // dùng để nhận từ Firestore (ví dụ: imageMovie1)

    public Movie() {
        // Bắt buộc để Firestore có thể tự mapping dữ liệu
    }

    public Movie(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
