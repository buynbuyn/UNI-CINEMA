package com.example.uni_cinema.ui.suatchieu;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SuatchieuFragment extends Fragment {
    private TextView tvTheaterName;
    private RecyclerView recyclerDate, recyclerScreening;
    private DateAdapter dateAdapter;
    private ScreeningAdapter screeningAdapter;
    private FirebaseFirestore db;

    private String theaterId, filmId, movieTitle;
    private LocalDate selectedDate;
    private final ZoneId zoneVN = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suatchieu, container, false); // đúng layout tên bạn đặt
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
            filmId = args.getString("filmId");
            movieTitle = args.getString("movieTitle");
            tvTheaterName.setText(args.getString("theaterName"));
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
            Log.d("DEBUG_DATE", "Ngày được chọn: " + date);
            loadScreeningsByDate(date);
        });
        recyclerDate.setAdapter(dateAdapter);

        screeningAdapter = new ScreeningAdapter(requireContext(), new ArrayList<>());
        recyclerScreening.setAdapter(screeningAdapter);

        loadScreeningsByDate(selectedDate);

        view.findViewById(R.id.btn_back_home).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_rap));
    }

    private void loadScreeningsByDate(LocalDate date) {
        db.collection("screeningRoom").get().addOnSuccessListener(roomSnap -> {
            Map<String, String> roomIdToName = new HashMap<>();
            Map<String, String> roomIdToTheater = new HashMap<>();
            Map<String, Long> roomIdToSeats = new HashMap<>();

            for (DocumentSnapshot roomDoc : roomSnap) {
                String roomId = roomDoc.getId();
                roomIdToName.put(roomId, roomDoc.getString("nameScreenRoom"));
                roomIdToTheater.put(roomId, roomDoc.getString("idTheater"));
                roomIdToSeats.put(roomId, roomDoc.getLong("quantityDesk"));
            }

            db.collection("screening")
                    .whereEqualTo("stateScreening", true)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        Log.d("DEBUG_SC", "Tổng số suất chiếu trả về: " + snapshot.size());
                        List<Screening> result = new ArrayList<>();
                        Map<String, List<Screening.TimeSlot>> movieSlotMap = new HashMap<>();

                        for (DocumentSnapshot doc : snapshot) {
                            String movieId = doc.getString("idMovie");
                            String screenRoomId = doc.getString("idScreenRoom");
                            Timestamp tsStart = doc.getTimestamp("dateTimeStart");
                            Timestamp tsEnd = doc.getTimestamp("dateTimeEnd");
                            String screeningId = doc.getId();

                            if (!Objects.equals(roomIdToTheater.get(screenRoomId), theaterId)) {
                                Log.d("FILTER", "Rớt do sai theaterId → " + roomIdToTheater.get(screenRoomId));
                                continue;
                            }

                            LocalDate showDate = tsStart.toDate().toInstant().atZone(zoneVN).toLocalDate();
                            if (!showDate.equals(date)) {
                                Log.d("FILTER", "Rớt do sai ngày → showDate = " + showDate + " | selected = " + date);
                                continue;
                            }
                            if (filmId != null && !filmId.equals(movieId)) {
                                Log.d("FILTER", "Rớt do sai phim → " + movieId);
                                continue;
                            }

                            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
                            String timeDisplay = tsStart.toDate().toInstant().atZone(zoneVN).toLocalTime().format(fmt)
                                    + " - " + tsEnd.toDate().toInstant().atZone(zoneVN).toLocalTime().format(fmt);

                            String screenRoomName = roomIdToName.getOrDefault(screenRoomId, "SCREEN ?");
                            Log.d("BUG_ROOM", "Không tìm thấy screenRoomId: " + screenRoomId);

                            int totalSeats = roomIdToSeats.getOrDefault(screenRoomId, 120L).intValue();

                            Screening.TimeSlot slot = new Screening.TimeSlot(screenRoomName, timeDisplay, totalSeats, 0, screeningId);
                            movieSlotMap.computeIfAbsent(movieId, k -> new ArrayList<>()).add(slot);
                        }

                        for (Map.Entry<String, List<Screening.TimeSlot>> entry : movieSlotMap.entrySet()) {
                            String mId = entry.getKey();
                            String mTitle;
                            if (filmId != null && movieTitle != null) {
                                mTitle = movieTitle;
                            } else {
                                mTitle = "Phim chưa rõ";
                            }
                            result.add(new Screening(mTitle, mId, entry.getValue()));
                        }

                        Log.d("DEBUG_SC", "Render tổng số phim suất: " + result.size());
                        screeningAdapter.updateData(result);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DEBUG_SC", "Lỗi load screening: " + e.getMessage());
                        Toast.makeText(getContext(), "Lỗi tải suất chiếu", Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
            Log.e("DEBUG_SC", "Lỗi load screeningRoom: " + e.getMessage());
            Toast.makeText(getContext(), "Lỗi tải phòng chiếu", Toast.LENGTH_SHORT).show();
        });
    }
}
