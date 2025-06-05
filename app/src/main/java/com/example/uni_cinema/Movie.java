package com.example.uni_cinema;

public class Movie {
    private String title;
    private int imageRes;

    public Movie(String title, int imageRes) {
        this.title = title;
        this.imageRes = imageRes;
    }

    public String getTitle() {
        return title;
    }

    public int getImageRes() {
        return imageRes;
    }
}

