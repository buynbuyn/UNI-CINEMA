package com.example.uni_cinema.ui.phongchieu;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Intent;

import java.text.DecimalFormat;


public class DeskActivity extends AppCompatActivity {

    private GridLayout seatContainer;
    private TextView infoTextView;
    private Button confirmerButton;
    private TextView totalPriceTextView;
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
        infoTextView = findViewById(R.id.info_text_view);
        totalPriceTextView = findViewById(R.id.total_price_text);
        confirmerButton = findViewById(R.id.confirm_button);
        infoTextView.setText("Vui lòng chọn ghế");
    }

    private void generateDesks() {
        if (screeningId == null || screenRoomId == null) {
            Toast.makeText(this, "Dữ liệu suất chiếu hoặc phòng chiếu không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        seatContainer.removeAllViews();
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

                                                        tempDeskList.add(new Desk(
                                                                idDesk,
                                                                categoryName,
                                                                price != null ? price.intValue() : 0,
                                                                isAvailable,
                                                                rowDesk != null ? rowDesk.intValue() : 0,
                                                                coloumnDesk != null ? coloumnDesk.intValue() : 0
                                                        ));
                                                    } catch (Exception e) {
                                                        Log.e("DESK_DEBUG", "Error parsing desk document", e);
                                                    }
                                                }
                                            }

                                            Collections.sort(tempDeskList, Comparator
                                                    .comparingInt(Desk::getRowDesk)
                                                    .thenComparingInt(Desk::getColoumnDesk));
                                            deskList.clear();
                                            deskList.addAll(tempDeskList);
                                            loadBookedSeatsAndRender();
                                        });
                            });
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

                    int maxRows = deskList.stream().mapToInt(Desk::getRowDesk).max().orElse(0);
                    int maxColumns = deskList.stream().mapToInt(Desk::getColoumnDesk).max().orElse(0);
                    seatContainer.setRowCount(maxRows > 0 ? maxRows : 1);
                    seatContainer.setColumnCount(maxColumns > 0 ? maxColumns : 1);
                    seatContainer.removeAllViews();

                    for (Desk desk : deskList) {
                        if (desk.getRowDesk() <= 0 || desk.getColoumnDesk() <= 0) continue;

                        Button deskButton = createDeskButton(desk.getIdDesk(),
                                "VIP".equalsIgnoreCase(desk.getCategoryName()),
                                "Couple".equalsIgnoreCase(desk.getCategoryName()),
                                bookedSeats, desk.getPrice());

                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.rowSpec = GridLayout.spec(desk.getRowDesk() - 1);
                        params.columnSpec = GridLayout.spec(desk.getColoumnDesk() - 1);
                        params.width = dpToPx(40);
                        params.height = dpToPx(40);
                        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
                        deskButton.setLayoutParams(params);
                        seatContainer.addView(deskButton);
                    }
                });
    }

    private Button createDeskButton(String deskId, boolean isVip, boolean isCouple, Map<String, Boolean> bookedSeats, int price) {
        Button desk = new Button(this);
        desk.setId(View.generateViewId());
        String displayText = deskId.length() > 1 ? deskId.substring(6) : deskId;
        desk.setText(displayText);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = dpToPx(50);
        params.height = dpToPx(50);
        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        desk.setLayoutParams(params);

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
                    if (selectedDesks.size() >= 6) {
                        Toast.makeText(this, "Chỉ được chọn tối đa 6 ghế", Toast.LENGTH_SHORT).show();
                        return;
                    }
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
            DecimalFormat formatter = new DecimalFormat("#,###");
            String formattedPrice = formatter.format(totalPrice).replace(',', '.');
            totalPriceTextView.setText("Tạm tính: " + formattedPrice + " VND");

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