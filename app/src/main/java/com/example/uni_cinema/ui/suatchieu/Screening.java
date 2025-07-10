package com.example.uni_cinema.ui.suatchieu;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;

public class Screening {
    public String movieTitle;
    public String movieId;
    public List<TimeSlot> timeSlots;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Screening(String movieTitle, String movieId, List<TimeSlot> timeSlots) {
        this.movieTitle = movieTitle;
        this.movieId = movieId;
        this.timeSlots = timeSlots;
    }

    public void updateBookedSeats() {
        for (TimeSlot timeSlot : timeSlots) {
            // Query the 'orders' subcollection where screenRoomName matches
            db.collection("screenings") // Adjust to your collection structure
                    .document(timeSlot.screeningId)
                    .collection("orders")
                    .whereEqualTo("screenRoomName", timeSlot.screenRoomName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                // Count the number of documents (booked seats)
                                int bookedSeatsCount = querySnapshot.size();
                                // Update the bookedSeats field in the TimeSlot
                                timeSlot.bookedSeats = bookedSeatsCount;
                                System.out.println("Updated bookedSeats for screenRoomName " +
                                        timeSlot.screenRoomName + ": " + bookedSeatsCount);
                            }
                        } else {
                            System.err.println("Error fetching orders: " + task.getException());
                        }
                    });
        }
    }

    public static class TimeSlot {
        public String screenRoomName;
        public String timeRangeDisplay;
        public int totalSeats;
        public int bookedSeats;
        public String screeningId;
        public String screenRoomId;

        public TimeSlot(String screenRoomName, String timeRangeDisplay, int totalSeats, int bookedSeats,
                        String screeningId, String screenRoomId) {
            this.screenRoomName = screenRoomName;
            this.timeRangeDisplay = timeRangeDisplay;
            this.totalSeats = totalSeats;
            this.bookedSeats = bookedSeats;
            this.screeningId = screeningId;
            this.screenRoomId = screenRoomId;
        }
    }
}