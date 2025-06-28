package com.example.uni_cinema.ui.phim;

public class Movie {
    private String id;
    private String title;
    private String imageUrl;
    private int timeMovie;
    private String genre;
    private String releaseDate;
    private int ageLimit;

    // Constructor
    public Movie(String id, String title, String imageUrl, int timeMovie, String genre, String releaseDate, int ageLimit) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.timeMovie = timeMovie;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.ageLimit = ageLimit;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getTimeMovie() {
        return timeMovie;
    }

    public String getGenre() {
        return genre;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public int getAgeLimit() {
        return ageLimit;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTimeMovie(int timeMovie) {
        this.timeMovie = timeMovie;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setAgeLimit(int ageLimit) {
        this.ageLimit = ageLimit;
    }
}