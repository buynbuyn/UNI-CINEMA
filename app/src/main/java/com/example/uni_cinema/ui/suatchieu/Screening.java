package com.example.uni_cinema.ui.suatchieu;

import java.util.List;

public class Screening {
    public String movieTitle;
    public String movieId;
    public List<TimeSlot> timeSlots;

    public Screening(String movieTitle, String movieId, List<TimeSlot> timeSlots) {
        this.movieTitle = movieTitle;
        this.movieId = movieId;
        this.timeSlots = timeSlots;
    }

    public static class TimeSlot {
        public String screenRoomName;
        public String timeRangeDisplay;
        public int totalSeats;
        public int bookedSeats;
        public String screeningId;


        public TimeSlot(String screenRoomName, String timeRangeDisplay, int totalSeats, int bookedSeats, String screeningId) {
            this.screenRoomName = screenRoomName;
            this.timeRangeDisplay = timeRangeDisplay;
            this.totalSeats = totalSeats;
            this.bookedSeats = bookedSeats;
            this.screeningId = screeningId;

        }
    }
}
