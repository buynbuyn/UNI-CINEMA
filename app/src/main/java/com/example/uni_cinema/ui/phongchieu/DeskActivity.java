package com.example.uni_cinema.ui.phongchieu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.uni_cinema.R;
import com.example.uni_cinema.ui.thanhtoan.PaymentActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.text.DecimalFormat;

public class DeskActivity extends AppCompatActivity {

    private GridLayout seatContainer;
    private GridLayout coupleSeatContainer;
    private TextView infoTextView;
    private TextView totalPriceTextView;
    private Button confirmerButton;
    private List<Desk> deskList;
    private Map<String, Desk> selectedDesks;
    private FirebaseFirestore db;
    private String screeningId, screenRoomId, movie, dateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_desk);

        registryView();
        db = FirebaseFirestore.getInstance();

        movie = getIntent().getStringExtra("movieTitle");
        dateTime = getIntent().getStringExtra("timeRange");
        screeningId = getIntent().getStringExtra("screeningId");
        screenRoomId = getIntent().getStringExtra("idScreeningRoom");
        Log.d("DESK_DEBUG", "Received screeningId: " + screeningId + ", screenRoomId: " + screenRoomId);

        deskList = new ArrayList<>();
        selectedDesks = new HashMap<>();

        generateDesks();

        confirmerButton.setOnClickListener(v -> {
            if (selectedDesks.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ghế", Toast.LENGTH_SHORT).show();
            } else {
                int totalPrice = 0;
                ArrayList<String> selectedDeskIds = new ArrayList<>();

                for (Desk desk : selectedDesks.values()) {
                    selectedDeskIds.add(desk.getIdDesk());
                    totalPrice += desk.getPrice();
                }

                Bundle bundle = new Bundle();
                bundle.putStringArrayList("selectedDeskIds", selectedDeskIds);
                bundle.putInt("totalPrice", totalPrice);
                bundle.putString("movieName", movie);
                bundle.putString("screeningDateTime", dateTime);
                bundle.putString("screenRoomName", screeningId);

                Intent intent = new Intent(DeskActivity.this, PaymentActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void registryView() {
        seatContainer = findViewById(R.id.seat_container);
        coupleSeatContainer = findViewById(R.id.couple_seat_container);
        infoTextView = findViewById(R.id.info_text_view);
        totalPriceTextView = findViewById(R.id.total_price_text); // Initialize totalPriceTextView
        confirmerButton = findViewById(R.id.confirm_button);

        if (seatContainer == null || coupleSeatContainer == null || infoTextView == null || totalPriceTextView == null || confirmerButton == null) {
            Log.e("DESK_DEBUG", "One or more views not found: seatContainer=" + seatContainer + ", coupleSeatContainer=" + coupleSeatContainer +
                    ", infoTextView=" + infoTextView + ", totalPriceTextView=" + totalPriceTextView + ", confirmerButton=" + confirmerButton);
            finish();
            return;
        }

        infoTextView.setText("Vui lòng chọn ghế");
        totalPriceTextView.setText("Tạm tính: 0 VND");
    }

    private void generateDesks() {
        if (screeningId == null || screenRoomId == null) {
            Toast.makeText(this, "Dữ liệu suất chiếu hoặc phòng chiếu không hợp lệ", Toast.LENGTH_SHORT).show();
            Log.e("DESK_DEBUG", "screeningId or screenRoomId is null");
            return;
        }

        seatContainer.removeAllViews();
        coupleSeatContainer.removeAllViews(); // Clear coupleSeatContainer
        deskList.clear();
        selectedDesks.clear();

        db.collection("screeningRoom").document(screenRoomId).collection("desk")
                .get()
                .addOnSuccessListener(deskSnapshot -> {
                    List<String> deskIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : deskSnapshot) {
                        String idDesk = doc.getId();
                        if (idDesk != null) deskIds.add(idDesk);
                    }
                    Log.d("DESK_DEBUG", "Fetched desk IDs: " + deskIds);

                    if (deskIds.isEmpty()) {
                        Toast.makeText(this, "Không tìm thấy ghế nào", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("deskCategories").get()
                            .addOnSuccessListener(catSnapshot -> {
                                Map<String, String> deskCategoryMap = new HashMap<>();
                                for (QueryDocumentSnapshot doc : catSnapshot) {
                                    String categoryName = doc.getString("nameDeskCategory");
                                    if (categoryName != null) {
                                        deskCategoryMap.put(doc.getId(), categoryName);
                                    }
                                }
                                Log.d("DESK_DEBUG", "Fetched desk categories: " + deskCategoryMap);

                                List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                                for (String idDesk : deskIds) {
                                    tasks.add(db.collection("desks").document(idDesk).get());
                                }

                                Tasks.whenAllSuccess(tasks)
                                        .addOnSuccessListener(results -> {
                                            List<Desk> tempDeskList = new ArrayList<>();
                                            for (Object result : results) {
                                                DocumentSnapshot deskDoc = (DocumentSnapshot) result;
                                                if (deskDoc.exists()) {
                                                    try {
                                                        String idDesk = deskDoc.getId();
                                                        String idCategory = deskDoc.getString("idDeskCategory");
                                                        String categoryName = deskCategoryMap.getOrDefault(idCategory, "Standard");
                                                        Long price = deskDoc.getLong("price");
                                                        Long rowDesk = deskDoc.getLong("rowDesk");
                                                        Long coloumnDesk = deskDoc.getLong("coloumnDesk");
                                                        Boolean state = deskDoc.getBoolean("stateDesk");
                                                        boolean isAvailable = state != null ? state : true;

                                                        if (price == null) {
                                                            Log.w("DESK_DEBUG", "Price is null for desk: " + idDesk);
                                                            price = 0L;
                                                        }
                                                        if (rowDesk == null) {
                                                            Log.w("DESK_DEBUG", "rowDesk is null for desk: " + idDesk);
                                                            rowDesk = 0L;
                                                        }
                                                        if (coloumnDesk == null) {
                                                            Log.w("DESK_DEBUG", "coloumnDesk is null for desk: " + idDesk);
                                                            coloumnDesk = 0L;
                                                        }

                                                        tempDeskList.add(new Desk(
                                                                idDesk,
                                                                categoryName,
                                                                price.intValue(),
                                                                isAvailable,
                                                                rowDesk.intValue(),
                                                                coloumnDesk.intValue()
                                                        ));
                                                    } catch (Exception e) {
                                                        Log.e("DESK_DEBUG", "Error parsing desk document: " + deskDoc.getId() + ", error: " + e.getMessage(), e);
                                                    }
                                                }
                                            }

                                            Collections.sort(tempDeskList, Comparator
                                                    .comparingInt(Desk::getRowDesk)
                                                    .thenComparingInt(Desk::getColoumnDesk));
                                            deskList.clear();
                                            deskList.addAll(tempDeskList);
                                            Log.d("DESK_DEBUG", "Sorted deskList: " + deskList);
                                            loadBookedSeatsAndRender();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("DESK_DEBUG", "Error fetching desk details: " + e.getMessage(), e);
                                            Toast.makeText(this, "Lỗi khi tải dữ liệu ghế", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("DESK_DEBUG", "Error fetching desk categories: " + e.getMessage(), e);
                                Toast.makeText(this, "Lỗi khi tải danh mục ghế", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("DESK_DEBUG", "Error fetching desk IDs: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi tải dữ liệu phòng chiếu", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadBookedSeatsAndRender() {
        db.collection("bookings")
                .whereEqualTo("screeningId", screeningId)
                .get()
                .addOnSuccessListener(bookingsSnapshot -> {
                    Map<String, Boolean> bookedSeats = new HashMap<>();
                    for (QueryDocumentSnapshot doc : bookingsSnapshot) {
                        String seatId = doc.getString("seatId");
                        if (seatId != null) bookedSeats.put(seatId, true);
                    }
                    Log.d("DESK_DEBUG", "Booked seats: " + bookedSeats.keySet());

                    // Tách ghế đôi và ghế thường
                    List<Desk> coupleDesks = new ArrayList<>();
                    List<Desk> regularDesks = new ArrayList<>();
                    for (Desk desk : deskList) {
                        if ("Couple".equalsIgnoreCase(desk.getCategoryName())) {
                            coupleDesks.add(desk);
                        } else {
                            regularDesks.add(desk);
                        }
                    }

                    // Thiết lập cấu hình cho coupleSeatContainer
                    int maxCoupleRows = coupleDesks.stream().mapToInt(Desk::getRowDesk).max().orElse(0);
                    int maxCoupleColumns = coupleDesks.stream().mapToInt(Desk::getColoumnDesk).max().orElse(0);
                    coupleSeatContainer.setRowCount(maxCoupleRows > 0 ? maxCoupleRows : 1);
                    coupleSeatContainer.setColumnCount(maxCoupleColumns > 0 ? maxCoupleColumns : 1);
                    coupleSeatContainer.removeAllViews();

                    // Thiết lập cấu hình cho seatContainer
                    int maxRegularRows = regularDesks.stream().mapToInt(Desk::getRowDesk).max().orElse(0);
                    int maxRegularColumns = regularDesks.stream().mapToInt(Desk::getColoumnDesk).max().orElse(0);
                    seatContainer.setRowCount(maxRegularRows > 0 ? maxRegularRows : 1);
                    seatContainer.setColumnCount(maxRegularColumns > 0 ? maxRegularColumns : 1);
                    seatContainer.removeAllViews();

                    // Render ghế đôi
                    for (Desk desk : coupleDesks) {
                        if (desk.getRowDesk() <= 0 || desk.getColoumnDesk() <= 0) {
                            Log.e("DESK_DEBUG", "Invalid row or column for couple desk: " + desk.getIdDesk() +
                                    ", row: " + desk.getRowDesk() + ", column: " + desk.getColoumnDesk());
                            continue;
                        }

                        Button deskButton = createDeskButton(desk.getIdDesk(), "VIP".equalsIgnoreCase(desk.getCategoryName()),
                                "Couple".equalsIgnoreCase(desk.getCategoryName()), bookedSeats, desk.getPrice());
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.rowSpec = GridLayout.spec(desk.getRowDesk() - 1);
                        params.columnSpec = GridLayout.spec(desk.getColoumnDesk() - 1);
                        params.width = dpToPx(80);
                        params.height = dpToPx(40);
                        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
                        deskButton.setLayoutParams(params);
                        coupleSeatContainer.addView(deskButton);
                    }

                    // Render ghế thường
                    for (Desk desk : regularDesks) {
                        if (desk.getRowDesk() <= 0 || desk.getColoumnDesk() <= 0) {
                            Log.e("DESK_DEBUG", "Invalid row or column for regular desk: " + desk.getIdDesk() +
                                    ", row: " + desk.getRowDesk() + ", column: " + desk.getColoumnDesk());
                            continue;
                        }

                        Button deskButton = createDeskButton(desk.getIdDesk(), "VIP".equalsIgnoreCase(desk.getCategoryName()),
                                "Couple".equalsIgnoreCase(desk.getCategoryName()), bookedSeats, desk.getPrice());
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.rowSpec = GridLayout.spec(desk.getRowDesk() - 1);
                        params.columnSpec = GridLayout.spec(desk.getColoumnDesk() - 1);
                        params.width = dpToPx(40);
                        params.height = dpToPx(40);
                        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
                        deskButton.setLayoutParams(params);
                        seatContainer.addView(deskButton);
                    }

                    Log.d("DESK_DEBUG", "Rendered couple grid with " + coupleDesks.size() + " desks and regular grid with " + regularDesks.size() + " desks");
                })
                .addOnFailureListener(e -> {
                    Log.e("DESK_DEBUG", "Error fetching bookings: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi kiểm tra ghế đã đặt", Toast.LENGTH_SHORT).show();
                });
    }

    private Button createDeskButton(String deskId, boolean isVip, boolean isCouple, Map<String, Boolean> bookedSeats, int price) {
        Button desk = new Button(this);
        desk.setId(View.generateViewId());
        String displayText = deskId.length() > 1 ? deskId.substring(6) : deskId;
        desk.setText(displayText);

        int defaultColor;
        if (isCouple) {
            defaultColor = android.graphics.Color.parseColor("#FF69B4");
        } else if (isVip) {
            defaultColor = android.graphics.Color.parseColor("#FFA500");
        } else {
            defaultColor = android.graphics.Color.parseColor("#607D8B");
        }
        desk.setBackgroundColor(defaultColor);
        desk.setTag(defaultColor);
        desk.setTextColor(android.graphics.Color.WHITE);
        desk.setTextSize(12);

        boolean isAvailable = !bookedSeats.containsKey(deskId);
        desk.setEnabled(isAvailable);
        if (!isAvailable) {
            desk.setBackgroundColor(android.graphics.Color.parseColor("#FF5722"));
            desk.setTag(android.graphics.Color.parseColor("#FF5722"));
        }

        desk.setOnClickListener(v -> {
            Desk deskData = deskList.stream().filter(d -> d.getIdDesk().equals(deskId)).findFirst().orElse(null);
            if (deskData != null && deskData.isAvailable()) {
                if (selectedDesks.containsKey(deskId)) {
                    selectedDesks.remove(deskId);
                    desk.setBackgroundColor((Integer) desk.getTag());
                } else {
                    selectedDesks.put(deskId, deskData);
                    desk.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
                }
                updateInfoText();
            }
        });

        return desk;
    }

    private void updateInfoText() {
        if (!selectedDesks.isEmpty()) {
            StringBuilder seatsInfo = new StringBuilder("Ghế đã chọn: ");
            int totalPrice = 0;
            List<String> seatNames = new ArrayList<>();

            for (Desk desk : selectedDesks.values()) {
                String displayText = desk.getIdDesk().length() > 1 ? desk.getIdDesk().substring(6) : desk.getIdDesk();
                seatNames.add(displayText);
                totalPrice += desk.getPrice();
            }

            seatsInfo.append(String.join(", ", seatNames));
            infoTextView.setText(seatsInfo.toString());
            DecimalFormat formatter = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.forLanguageTag("vi-VN")));
            String formattedPrice = formatter.format(totalPrice);
            totalPriceTextView.setText("Tạm tính: " + formattedPrice + " VND");
            Log.d("DESK_DEBUG", "Total price calculated: " + totalPrice);
        } else {
            infoTextView.setText("Vui lòng chọn ghế");
            totalPriceTextView.setText("Tạm tính: 0 VND");
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}