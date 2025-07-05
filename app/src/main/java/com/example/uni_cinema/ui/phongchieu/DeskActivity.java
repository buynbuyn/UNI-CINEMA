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

public class DeskActivity extends AppCompatActivity {

    private GridLayout seatContainer; // Sửa thành GridLayout để khớp với XML
    private TextView infoTextView;
    private Button confirmerButton; // Sửa chính tả, dùng confirmerButton
    private List<Desk> deskList;
    private Map<String, Desk> selectedDesks; // Sửa thành Map để tránh trùng lặp ghế
    private FirebaseFirestore db;
    private String screeningId, screenRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_desk);

        registryView();
        db = FirebaseFirestore.getInstance();

        screeningId = getIntent().getStringExtra("screeningId");
        screenRoomId = getIntent().getStringExtra("idScreeningRoom");
        Log.d("DESK_DEBUG", "Received screeningId: " + screeningId + ", screenRoomId: " + screenRoomId);

        deskList = new ArrayList<>();
        selectedDesks = new HashMap<>(); // Khởi tạo Map

        generateDesks();

        confirmerButton.setOnClickListener(v -> {
            if (selectedDesks.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ghế", Toast.LENGTH_SHORT).show();
            } else {
                StringBuilder selectedSeats = new StringBuilder("Ghế đã chọn: ");
                int totalPrice = 0;
                for (Desk desk : selectedDesks.values()) { // Dùng values() để lấy danh sách Desk
                    String displayText = desk.getIdDesk().length() > 1 ? desk.getIdDesk().substring(1) : desk.getIdDesk();
                    selectedSeats.append(displayText).append(" ");
                    totalPrice += desk.getPrice();
                }
                selectedSeats.append("\nTổng giá: ").append(totalPrice).append(" VND");
                Toast.makeText(this, selectedSeats.toString(), Toast.LENGTH_LONG).show();
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
        if (seatContainer == null) {
            Log.e("DESK_DEBUG", "seatContainer not found");
            finish();
            return;
        }
        infoTextView = findViewById(R.id.info_text_view);
        confirmerButton = findViewById(R.id.confirm_button); // Sửa thành confirmerButton

        // Cập nhật giao diện nếu cần
        infoTextView.setText("Vui lòng chọn ghế");
    }

    private void generateDesks() {
        if (screeningId == null || screenRoomId == null) {
            Toast.makeText(this, "Dữ liệu suất chiếu hoặc phòng chiếu không hợp lệ", Toast.LENGTH_SHORT).show();
            Log.e("DESK_DEBUG", "screeningId or screenRoomId is null");
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

                                                        tempDeskList.add(new Desk(
                                                                idDesk,
                                                                categoryName,
                                                                price != null ? price.intValue() : 0,
                                                                isAvailable,
                                                                rowDesk != null ? rowDesk.intValue() : 0,
                                                                coloumnDesk != null ? coloumnDesk.intValue() : 0
                                                        ));
                                                    } catch (Exception e) {
                                                        Log.e("DESK_DEBUG", "Error parsing desk document: " + deskDoc.getId(), e);
                                                    }
                                                }
                                            }

                                            Collections.sort(tempDeskList, (d1, d2) -> {
                                                int rowCompare = d1.getRowDesk() - d2.getRowDesk();
                                                return rowCompare != 0 ? rowCompare : d1.getColoumnDesk() - d2.getColoumnDesk();
                                            });
                                            deskList.clear();
                                            deskList.addAll(tempDeskList);
                                            Log.d("DESK_DEBUG", "Sorted deskList: " + deskList);
                                            loadBookedSeatsAndRender();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("DESK_DEBUG", "Error fetching desk details", e);
                                            Toast.makeText(this, "Lỗi khi tải dữ liệu ghế", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("DESK_DEBUG", "Error fetching desk categories", e);
                                Toast.makeText(this, "Lỗi khi tải danh mục ghế", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("DESK_DEBUG", "Error fetching desk IDs", e);
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

                    int maxRows = deskList.stream().mapToInt(Desk::getRowDesk).max().orElse(0);
                    int maxColumns = deskList.stream().mapToInt(Desk::getColoumnDesk).max().orElse(0);
                    seatContainer.setRowCount(maxRows > 0 ? maxRows : 1);
                    seatContainer.setColumnCount(maxColumns > 0 ? maxColumns : 1);
                    seatContainer.removeAllViews();

                    for (Desk desk : deskList) {
                        // Kiểm tra chỉ số hàng và cột hợp lệ
                        if (desk.getRowDesk() <= 0 || desk.getColoumnDesk() <= 0) {
                            Log.e("DESK_DEBUG", "Invalid row or column for desk: " + desk.getIdDesk() +
                                    ", row: " + desk.getRowDesk() + ", column: " + desk.getColoumnDesk());
                            continue; // Bỏ qua ghế có chỉ số không hợp lệ
                        }

                        Button deskButton = createDeskButton(desk.getIdDesk(), "VIP".equalsIgnoreCase(desk.getCategoryName()),
                                "Couple".equalsIgnoreCase(desk.getCategoryName()), bookedSeats, desk.getPrice());
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.rowSpec = GridLayout.spec(desk.getRowDesk() - 1); // Chỉ số bắt đầu từ 0
                        params.columnSpec = GridLayout.spec(desk.getColoumnDesk() - 1);
                        params.width = dpToPx(40);
                        params.height = dpToPx(40);
                        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
                        deskButton.setLayoutParams(params);
                        seatContainer.addView(deskButton);
                    }

                    Log.d("DESK_DEBUG", "Rendered grid with " + deskList.size() + " desks");
                })
                .addOnFailureListener(e -> {
                    Log.e("DESK_DEBUG", "Error fetching bookings", e);
                    Toast.makeText(this, "Lỗi khi kiểm tra ghế đã đặt", Toast.LENGTH_SHORT).show();
                });
    }

    private Button createDeskButton(String deskId, boolean isVip, boolean isCouple, Map<String, Boolean> bookedSeats, int price) {
        Button desk = new Button(this);
        desk.setId(View.generateViewId());
        String displayText = deskId.length() > 1 ? deskId.substring(1) : deskId;
        desk.setText(displayText);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams(); // Sử dụng GridLayout.LayoutParams
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
                if (selectedDesks.containsKey(deskId)) { // Kiểm tra bằng key
                    selectedDesks.remove(deskId);
                    desk.setBackgroundColor((Integer) desk.getTag());
                } else {
                    selectedDesks.put(deskId, deskData); // Thêm vào Map
                    desk.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
                }
                updateInfoText();
            }
        });

        return desk;
    }

    private void updateInfoText() {
        if (!selectedDesks.isEmpty()) {
            StringBuilder seatsInfo = new StringBuilder("Ghế đã chọn:\n");
            int totalPrice = 0;
            for (Desk desk : selectedDesks.values()) {
                String displayText = desk.getIdDesk().length() > 1 ? desk.getIdDesk().substring(1) : desk.getIdDesk();
                seatsInfo.append(displayText).append(" (").append(desk.getCategoryName()).append(", ").append(desk.getPrice()).append(" VND)\n");
                totalPrice += desk.getPrice();
            }
            seatsInfo.append("Tổng giá: ").append(totalPrice).append(" VND");
            infoTextView.setText(seatsInfo.toString());
        } else {
            infoTextView.setText("Vui lòng chọn ghế");
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}