package com.example.uni_cinema.ui.home;

public class Movie {
    private String title;
    private String id;
    private String imageUrl; // dùng để nhận từ Firestore (ví dụ: imageMovie1)


    public Movie(String id, String title, String imageUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }
    public String getId() {
        return id;
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
    private int timeMovie;

    public int getTimeMovie() {
        return timeMovie;
    }

    public void setTimeMovie(int timeMovie) {
        this.timeMovie = timeMovie;
    }
}
