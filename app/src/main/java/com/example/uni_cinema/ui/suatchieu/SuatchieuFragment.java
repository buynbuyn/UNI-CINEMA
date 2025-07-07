package com.example.uni_cinema.ui.suatchieu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.uni_cinema.ui.phongchieu.DeskActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uni_cinema.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SuatchieuFragment extends Fragment {
    private TextView tvTheaterName;
    private RecyclerView recyclerDate, recyclerScreening;
    private DateAdapter dateAdapter;
    private ScreeningAdapter screeningAdapter;
    private FirebaseFirestore db;

    private String theaterId, movieId, movieTitle;
    private LocalDate selectedDate;
    private final ZoneId zoneVN = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suatchieu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }

        View bottomMenu = requireActivity().findViewById(R.id.custom_bottom_menu);
        if (bottomMenu != null) bottomMenu.setVisibility(View.GONE);

        tvTheaterName = view.findViewById(R.id.tvTheaterName);
        recyclerDate = view.findViewById(R.id.recyclerDate);
        recyclerScreening = view.findViewById(R.id.recyclerScreening);
        db = FirebaseFirestore.getInstance();

        Bundle args = getArguments();
        if (args != null) {
            theaterId = args.getString("theaterId");
            movieId = args.getString("movieId");
            movieTitle = args.getString("movieTitle");
            tvTheaterName.setText(args.getString("theaterName", "RẠP CHIẾU PHIM"));
            Log.d("DEBUG_BUNDLE", "🎯 theaterId=" + theaterId + ", movieId=" + movieId + ", movieTitle=" + movieTitle);
        }

        recyclerDate.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerScreening.setLayoutManager(new LinearLayoutManager(getContext()));

        selectedDate = LocalDate.now();
        List<LocalDate> dateList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            dateList.add(selectedDate.plusDays(i));
        }

        dateAdapter = new DateAdapter(dateList, date -> {
            selectedDate = date;
            Log.d("DEBUG_DATE", "📅 Ngày được chọn: " + date);
            loadScreeningsByDate(date);
        });
        recyclerDate.setAdapter(dateAdapter);

        screeningAdapter = new ScreeningAdapter(requireContext(), new ArrayList<>());
        recyclerScreening.setAdapter(screeningAdapter);

        loadScreeningsByDate(selectedDate);

        ImageButton btnBack = view.findViewById(R.id.btn_back_home);
        btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_rap)
        );
    }

    private void loadScreeningsByDate(LocalDate date) {
        db.collection("movies").get().addOnSuccessListener(movieSnap -> {
            Map<String, String> movieIdToName = new HashMap<>();
            for (DocumentSnapshot movieDoc : movieSnap) {
                String id = movieDoc.getId();
                String name = movieDoc.getString("nameMovie");
                movieIdToName.put(id, name);
            }

            db.collection("screening")
                    .whereEqualTo("stateScreening", true)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        List<Screening> result = new ArrayList<>();
                        Map<String, List<Screening.TimeSlot>> movieSlotMap = new HashMap<>();
                        Set<String> roomIdSet = new HashSet<>();
                        List<DocumentSnapshot> screeningDocs = new ArrayList<>();
                        Date now = new Date();
                        for (DocumentSnapshot doc : snapshot) {
                            String movieIdInDoc = doc.getString("idMovie");
                            String screenRoomId = doc.getString("idScreenRoom");
                            Timestamp tsStart = doc.getTimestamp("dateTimeStart");

                            Log.d("CHECK_DOC", "📍 screeningId = " + doc.getId()
                                    + "\n→ idMovie = " + movieIdInDoc
                                    + "\n→ screenRoomId = " + screenRoomId
                                    + "\n→ tsStart = " + tsStart);

                            if (tsStart == null || screenRoomId == null) {
                                Log.d("FILTER_OUT", "⛔ Bỏ vì thiếu tsStart hoặc screenRoomId");
                                continue;
                            }

                            LocalDate showDate = tsStart.toDate().toInstant().atZone(zoneVN).toLocalDate();
                            if (!showDate.equals(date)) {
                                Log.d("FILTER_OUT", "⛔ Bỏ vì khác ngày: " + showDate);
                                continue;
                            }

                            if (movieId != null && !movieId.equals(movieIdInDoc)) {
                                Log.d("FILTER_OUT", "⛔ Bỏ vì khác movieId");
                                continue;
                            }

                            screeningDocs.add(doc);
                            roomIdSet.add(screenRoomId);
                        }

                        Log.d("DEBUG_SCREENING", "✅ Tổng suất chiếu hợp lệ: " + screeningDocs.size());
                        Log.d("DEBUG_ROOMIDS", "📦 roomIdSet = " + roomIdSet);

                        if (screeningDocs.isEmpty()) {
                            screeningAdapter.updateData(result);
                            return;
                        }

                        db.collection("screeningRoom")
                                .whereIn("__name__", new ArrayList<>(roomIdSet))
                                .get()
                                .addOnSuccessListener(roomSnap -> {
                                    Map<String, String> roomIdToName = new HashMap<>();
                                    Map<String, String> roomIdToTheater = new HashMap<>();
                                    Map<String, Long> roomIdToSeats = new HashMap<>();

                                    Log.d("DEBUG_ROOMSNAP", "🏢 Phòng trả về: " + roomSnap.size());
                                    for (DocumentSnapshot roomDoc : roomSnap) {
                                        String id = roomDoc.getId();
                                        String name = roomDoc.getString("nameScreenRoom");
                                        String idTheater = roomDoc.getString("idTheater");
                                        Long seats = roomDoc.getLong("quantityDesk");

                                        Log.d("ROOM_MAP", "→ roomId = " + id + ", name = " + name + ", theater = " + idTheater);

                                        roomIdToName.put(id, name);
                                        roomIdToTheater.put(id, idTheater);
                                        roomIdToSeats.put(id, seats);
                                    }

                                    for (DocumentSnapshot doc : screeningDocs) {
                                        String screeningId = doc.getId();
                                        String screenRoomId = doc.getString("idScreenRoom");
                                        String movieIdInDoc = doc.getString("idMovie");

                                        if (screenRoomId == null) continue;
                                        String mappedTheater = roomIdToTheater.get(screenRoomId);
                                        if (theaterId != null && !Objects.equals(mappedTheater, theaterId)) {
                                            Log.d("FILTER_OUT", "⛔ Rớt vì khác rạp: mapping = " + mappedTheater);
                                            continue;
                                        }

                                        Timestamp tsStart = doc.getTimestamp("dateTimeStart");
                                        Timestamp tsEnd = doc.getTimestamp("dateTimeEnd");
                                        if (tsStart == null || tsEnd == null) continue;

                                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
                                        String timeDisplay = tsStart.toDate().toInstant().atZone(zoneVN).toLocalTime().format(fmt)
                                                + " - " + tsEnd.toDate().toInstant().atZone(zoneVN).toLocalTime().format(fmt);

                                        String roomName = roomIdToName.getOrDefault(screenRoomId, "Phòng ?");
                                        int totalSeats = roomIdToSeats.getOrDefault(screenRoomId, 120L).intValue();

                                        Screening.TimeSlot slot = new Screening.TimeSlot(
                                                roomName,
                                                timeDisplay,
                                                totalSeats,
                                                0, // bookedSeats
                                                screeningId,
                                                screenRoomId // Thêm screenRoomId
                                        );
                                        movieSlotMap.computeIfAbsent(movieIdInDoc, k -> new ArrayList<>()).add(slot);

                                        Log.d("DEBUG_SLOT", "🎞 Slot: " + roomName + " | " + timeDisplay + " | Movie = " + movieIdInDoc);
                                    }

                                    for (Map.Entry<String, List<Screening.TimeSlot>> entry : movieSlotMap.entrySet()) {
                                        String mid = entry.getKey();
                                        String title = (movieId != null && movieId.equals(mid) && movieTitle != null)
                                                ? movieTitle
                                                : movieIdToName.getOrDefault(mid, "Phim chưa rõ");
                                        result.add(new Screening(title, mid, entry.getValue()));
                                    }

                                    Log.d("FINAL_RESULT", "🎬 Tổng phim hiển thị: " + result.size());
                                    screeningAdapter.updateData(result);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Lỗi tải phòng chiếu", Toast.LENGTH_SHORT).show();
                                    Log.e("DEBUG_ROOM", "Lỗi load room: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi tải suất chiếu", Toast.LENGTH_SHORT).show();
                        Log.e("DEBUG_SC", "Lỗi load screening: " + e.getMessage());
                    });
        });
    }
}
