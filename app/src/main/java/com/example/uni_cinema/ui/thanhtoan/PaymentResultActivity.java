package com.example.uni_cinema.ui.thanhtoan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uni_cinema.MainActivity;
import com.example.uni_cinema.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaymentResultActivity extends AppCompatActivity {

    private static final String TAG = "PaymentResultActivity";
    private static final String SERVER_URL = "https://your-secure-api.com/payment/check-order-status"; // Replace with your secure server URL

    private TextView tvMovieName, tvDateTime, tvScreenRoom, tvSelectedSeats, tvTotalAmount;
    private TextView tvPaymentStatus, tvPaymentMessage;
    private TextView tvMomoTransactionId, tvMomoResponseCode, tvMomoMessage;
    private ImageButton btnBack;
    private Button btnCompletePayment;

    private ArrayList<String> selectedDeskIds = new ArrayList<>();
    private int totalAmount;
    private String movieName = "";
    private String screeningDateTime = "";
    private String screenRoomName = "";
    private String orderId;
    private String uid;
    private String idMethodPayment;
    private String discount;
    private boolean paymentSuccess;
    private FirebaseFirestore db;
    private CollectionReference ordersRef; // Changed from DocumentReference to CollectionReference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        ordersRef = db.collection("orders"); // This is now correct

        initializeViews();
        handlePaymentResult();
        setupButtonListeners();
    }

    private void initializeViews() {
        try {
            tvMovieName = findViewById(R.id.tv_movie_name_result);
            tvDateTime = findViewById(R.id.tv_date_time_result);
            tvScreenRoom = findViewById(R.id.tv_screen_room_result);
            tvSelectedSeats = findViewById(R.id.tv_selected_seats_result);
            tvTotalAmount = findViewById(R.id.tv_total_amount_result);
            tvPaymentStatus = findViewById(R.id.tv_payment_status);
            tvPaymentMessage = findViewById(R.id.tv_payment_message);
            tvMomoTransactionId = findViewById(R.id.tv_momo_transaction_id);
            tvMomoResponseCode = findViewById(R.id.tv_momo_response_code);
            tvMomoMessage = findViewById(R.id.tv_momo_message);
            btnBack = findViewById(R.id.btn_back_result);
            btnCompletePayment = findViewById(R.id.btn_complete_payment);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể khởi tạo giao diện. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void handlePaymentResult() {
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            orderId = appLinkData.getQueryParameter("orderId");
            String resultCode = appLinkData.getQueryParameter("resultCode");
            String amount = appLinkData.getQueryParameter("amount");
            String orderInfo = appLinkData.getQueryParameter("orderInfo");
            String transId = appLinkData.getQueryParameter("transId");
            String message = appLinkData.getQueryParameter("message");

            // Initialize user-specific variables (replace with actual logic)
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            uid = prefs.getString("uid", "");
            idMethodPayment = "MOMO"; // Replace with actual payment method
            discount = "0"; // Replace with actual discount
            paymentSuccess = "0".equals(resultCode);

            displayPaymentResult(resultCode, orderId, amount, orderInfo, transId, message);

            if (orderId != null && !orderId.isEmpty()) {
                verifyPaymentWithServer(orderId);
            } else {
                tvPaymentStatus.setText("Lỗi: Không có mã đơn hàng");
                tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
                tvPaymentMessage.setText("Vui lòng kiểm tra lại quy trình thanh toán.");
            }
        } else {
            tvPaymentStatus.setText("Không có dữ liệu thanh toán");
            tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
            tvPaymentMessage.setText("Vui lòng kiểm tra lại quy trình thanh toán.");
        }
    }

    private void displayPaymentResult(String resultCode, String orderId, String amount, String orderInfo, String transId, String message) {
        String statusMessage = "Không xác định";
        String detailedMessage = "Không có thông tin giao dịch.";
        int statusColor = android.R.color.black;

        if ("0".equals(resultCode)) {
            statusMessage = "Thanh toán thành công!";
            detailedMessage = "Giao dịch đã hoàn tất.";
            statusColor = android.R.color.holo_green_dark;
        } else if ("1".equals(resultCode)) {
            statusMessage = "Thanh toán thất bại!";
            detailedMessage = "Vui lòng thử lại.";
            statusColor = android.R.color.holo_red_dark;
        } else if ("3".equals(resultCode)) {
            statusMessage = "Giao dịch đã bị huỷ";
            detailedMessage = "Bạn đã huỷ giao dịch.";
            statusColor = android.R.color.holo_orange_dark;
        } else {
            statusMessage = "Không rõ kết quả";
            detailedMessage = "Lỗi mã: " + (resultCode != null ? resultCode : "N/A");
            statusColor = android.R.color.holo_red_dark;
        }

        tvPaymentStatus.setText(statusMessage);
        tvPaymentStatus.setTextColor(getResources().getColor(statusColor, getTheme()));
        tvPaymentMessage.setText(detailedMessage);
        tvMomoTransactionId.setText("Mã giao dịch MOMO: " + (transId != null ? transId : "N/A"));
        tvMomoResponseCode.setText("Mã phản hồi MOMO: " + (resultCode != null ? resultCode : "N/A"));
        tvMomoMessage.setText("Thông tin đơn hàng MOMO: " + (orderInfo != null ? orderInfo : "N/A"));

        try {
            if (amount != null && !amount.isEmpty()) {
                double amt = Double.parseDouble(amount);
                tvTotalAmount.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", amt));
            } else {
                tvTotalAmount.setText("Số tiền: N/A");
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid amount format: " + amount, e);
            tvTotalAmount.setText("Số tiền: N/A");
        }
    }

    private void verifyPaymentWithServer(String orderId) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(SERVER_URL + "?orderId=" + orderId);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(response.toString());

                    totalAmount = json.optInt("amount", 0);
                    movieName = json.optString("movie", "N/A");
                    screeningDateTime = json.optString("dateTime", "N/A");
                    screenRoomName = json.optString("screenRoom", "N/A");

                    JSONArray seatArray = json.optJSONArray("desk");
                    selectedDeskIds.clear();
                    if (seatArray != null && seatArray.length() > 0) {
                        for (int i = 0; i < seatArray.length(); i++) {
                            selectedDeskIds.add(seatArray.getString(i));
                        }
                    }

                    String status = json.optString("status", "FAILED");

                    runOnUiThread(() -> {
                        if (!isFinishing()) {
                            if ("SUCCESS".equals(status)) {
                                tvPaymentStatus.setText("Đã xác nhận thanh toán");
                                tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
                            } else {
                                tvPaymentStatus.setText("Thanh toán thất bại hoặc chưa hoàn tất");
                                tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
                            }
                            displayOrderInfo();
                        }
                    });
                } else {
                    Log.e(TAG, "Server response code: " + responseCode);
                    runOnUiThread(() -> Toast.makeText(this, "Lỗi xác nhận từ máy chủ", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "verifyPaymentWithServer error: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show());
            } finally {
                if (conn != null) {
                    conn.disconnect(); // Ensure connection is closed
                }
            }
        }).start();
    }

    private void displayOrderInfo() {
        tvMovieName.setText(movieName != null ? movieName : "N/A");
        tvDateTime.setText(screeningDateTime != null ? screeningDateTime : "N/A");
        tvScreenRoom.setText(screenRoomName != null ? screenRoomName : "N/A");

        if (selectedDeskIds != null && !selectedDeskIds.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < selectedDeskIds.size(); i++) {
                if (i > 0) builder.append(", ");
                builder.append(selectedDeskIds.get(i));
            }
            tvSelectedSeats.setText(builder.toString());
        } else {
            tvSelectedSeats.setText("Không có ghế nào");
        }

        tvTotalAmount.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", (double) totalAmount));
    }

    private void setupButtonListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCompletePayment.setOnClickListener(v -> {
            if (orderId != null && !orderId.isEmpty()) {
                DocumentReference orderDocRef = ordersRef.document(orderId);
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("screenRoomName", screenRoomName != null ? screenRoomName : "N/A");
                orderData.put("totalPrice", totalAmount);
                orderData.put("idUser", uid != null ? uid : "");
                orderData.put("stateOrder", paymentSuccess);
                orderData.put("idMethodPayment", idMethodPayment != null ? idMethodPayment : "N/A");
                orderData.put("idDiscount", discount != null ? discount : "0");
                orderData.put("timestamp", System.currentTimeMillis());

                orderDocRef.set(orderData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Order saved successfully with ID: " + orderId);
                            saveSeatsToSubcollection();
                            updateUserPoints();
                            Intent mainIntent = new Intent(PaymentResultActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mainIntent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error saving order: " + e.getMessage(), e);
                            Toast.makeText(this, "Lỗi lưu hóa đơn. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Không có ID đơn hàng để lưu.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSeatsToSubcollection() {
        if (orderId != null && !orderId.isEmpty() && selectedDeskIds != null && !selectedDeskIds.isEmpty()) {
            DocumentReference orderDocRef = ordersRef.document(orderId);
            for (String seatId : selectedDeskIds) {
                Map<String, Object> seatData = new HashMap<>();
                seatData.put("seatId", seatId);
                seatData.put("timestamp", System.currentTimeMillis());
                orderDocRef.collection("seats").document(seatId).set(seatData)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Seat " + seatId + " saved successfully"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error saving seat " + seatId + ": " + e.getMessage(), e));
            }
        } else {
            Log.w(TAG, "No seats or orderId to save to subcollection");
        }
    }

    private void updateUserPoints() {
        if (uid != null && !uid.isEmpty()) {
            DocumentReference userDocRef = db.collection("users").document(uid);
            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    int currentPoints = documentSnapshot.getLong("point") != null ? documentSnapshot.getLong("point").intValue() : 0;
                    int newPoints = currentPoints;

                    for (String seatId : selectedDeskIds) {
                        if (seatId.contains("J")) {
                            newPoints += 2;
                        } else {
                            newPoints += 1;
                        }
                    }

                    userDocRef.update("point", newPoints);
                } else {
                    Log.w(TAG, "User document does not exist for uid: " + uid);
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Error fetching user document: " + e.getMessage(), e));
        } else {
            Log.w(TAG, "No uid available to update points");
        }
    }
}