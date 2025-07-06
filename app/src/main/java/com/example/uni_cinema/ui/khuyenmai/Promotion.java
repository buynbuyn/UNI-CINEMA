package com.example.uni_cinema.ui.khuyenmai;

import com.google.firebase.Timestamp;

public class Promotion {
    private String id;
    private String title;
    private String description;
    private Timestamp startDate;
    private Timestamp endDate;
    private String bannerImage;

    public Promotion() {} // Firestore cần constructor rỗng

    public Promotion(String id, String title, String description,
                     Timestamp startDate, Timestamp endDate, String bannerImage) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bannerImage = bannerImage;
    }

    // Getter + Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Timestamp getStartDate() { return startDate; }
    public Timestamp getEndDate() { return endDate; }
    public String getBannerImage() { return bannerImage; }

}
