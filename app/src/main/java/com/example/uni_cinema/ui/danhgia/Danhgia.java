package com.example.uni_cinema.model;

import com.google.gson.annotations.SerializedName;

public class Danhgia {
    @SerializedName("id_comment")
    private int idComment;
    @SerializedName("id_movie")
    private String idMovie;
    @SerializedName("uid")
    private String idUser;
    @SerializedName("comment")
    private String comment;
    @SerializedName("rating")
    private int rating;
    @SerializedName("date_time")
    private String dateTime;

    public Danhgia(int idComment, String idMovie, String idUser, String comment, int rating, String dateTime) {
        this.idComment = idComment;
        this.idMovie = idMovie;
        this.idUser = idUser;
        this.comment = comment;
        this.rating = rating;
        this.dateTime = dateTime;
    }

    // Getters and setters
    public int getIdComment() {
        return idComment;
    }

    public void setIdComment(int idComment) {
        this.idComment = idComment;
    }

    public String getIdMovie() {
        return idMovie;
    }

    public void setIdMovie(String idMovie) {
        this.idMovie = idMovie;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}