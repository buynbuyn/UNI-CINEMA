package com.example.uni_cinema.ui.thanhtoan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uni_cinema.R;
import com.example.uni_cinema.MainActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Date;

public class PaymentResultActivity extends AppCompatActivity {

    private static final String TAG = "PaymentResultActivity";

    private TextView tvMovieName, tvDateTime, tvScreenRoom, tvSelectedSeats, tvTotalAmount;
    private TextView tvPaymentStatus, tvPaymentMessage;
    private TextView tvMomoTransactionId, tvMomoResponseCode, tvMomoMessage;
    private ImageButton btnBack;
    private android.widget.Button btnCompletePayment;

    private ArrayList<String> selectedDeskIds = new ArrayList<>();
    private ArrayList<String> selectedDeskCategories = new ArrayList<>();
    private int totalAmount;
    private String movieName = "";
    private String screeningDateTime = "";
    private String screenRoomName = "";
    private String orderId = "";
    private String transactionId = "";
    private String paymentStatus = "";
    private FirebaseFirestore db;
    private String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

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
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Không thể khởi tạo giao diện", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void handlePaymentResult() {
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            String resultCode = appLinkData.getQueryParameter("resultCode");
            orderId = appLinkData.getQueryParameter("orderId");
            String amount = appLinkData.getQueryParameter("amount");
            String orderInfo = appLinkData.getQueryParameter("orderInfo");
            transactionId = appLinkData.getQueryParameter("transId");
            String message = appLinkData.getQueryParameter("message");

            displayPaymentResult(resultCode, orderId, amount, orderInfo, transactionId, message);

            if (orderId != null && !orderId.isEmpty()) {
                verifyPaymentWithServer(orderId);
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
            paymentStatus = "SUCCESS";
        } else if ("1".equals(resultCode)) {
            statusMessage = "Thanh toán thất bại!";
            detailedMessage = "Vui lòng thử lại.";
            statusColor = android.R.color.holo_red_dark;
            paymentStatus = "FAILED";
        } else if ("3".equals(resultCode)) {
            statusMessage = "Giao dịch đã bị huỷ";
            detailedMessage = "Bạn đã huỷ giao dịch.";
            statusColor = android.R.color.holo_orange_dark;
            paymentStatus = "CANCELLED";
        } else {
            statusMessage = "Không rõ kết quả";
            detailedMessage = "Lỗi mã: " + resultCode;
            statusColor = android.R.color.holo_red_dark;
            paymentStatus = "FAILED";
        }

        tvPaymentStatus.setText(statusMessage);
        tvPaymentStatus.setTextColor(getResources().getColor(statusColor, getTheme()));
        tvPaymentMessage.setText(detailedMessage);
        tvMomoTransactionId.setText("Mã giao dịch MOMO: " + (transId != null ? transId : "N/A"));
        tvMomoResponseCode.setText("Mã phản hồi MOMO: " + (resultCode != null ? resultCode : "N/A"));
        tvMomoMessage.setText("Thông tin đơn hàng MOMO: " + (orderInfo != null ? orderInfo : "N/A"));

        try {
            if (amount != null) {
                double amt = Double.parseDouble(amount);
                totalAmount = (int) amt;
                tvTotalAmount.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", amt));
            }
        } catch (Exception e) {
            tvTotalAmount.setText("Số tiền: N/A");
        }
    }

    private void verifyPaymentWithServer(String orderId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.165.79:5000/payment/check-order-status?orderId=" + orderId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                    reader.close();

                    JSONObject json = new JSONObject(response.toString());

                    totalAmount = json.optInt("amount", 0);
                    movieName = json.optString("movie", "N/A");
                    screeningDateTime = json.optString("dateTime", "N/A");
                    screenRoomName = json.optString("screenRoom", "N/A");
                    uid = json.optString("user", "N/A");

                    JSONArray seatArray = json.optJSONArray("desk");
                    selectedDeskIds.clear();
                    if (seatArray != null) {
                        for (int i = 0; i < seatArray.length(); i++) {
                            selectedDeskIds.add(seatArray.getString(i));
                        }
                    }

                    String status = json.optString("status", "FAILED");
                    paymentStatus = status;

                    runOnUiThread(() -> {
                        if (!isFinishing()) {
                            if ("SUCCESS".equals(status)) {
                                tvPaymentStatus.setText("Đã xác nhận thanh toán");
                                tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));

                                // Lưu đơn hàng vào Firebase khi thanh toán thành công
                                saveOrderToFirebase();
                            } else {
                                tvPaymentStatus.setText("Thanh toán thất bại hoặc chưa hoàn tất");
                                tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
                            }
                            displayOrderInfo();
                        }
                    });

                } else {
                    Log.e(TAG, "Server response code: " + responseCode);
                }

            } catch (Exception e) {
                Log.e(TAG, "verifyPaymentWithServer error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void saveOrderToFirebase() {
        if (orderId == null || orderId.isEmpty()) {
            Log.e(TAG, "Order ID is null or empty");
            return;
        }

        try {
            // Tạo order data
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("dateTimeOrder", Timestamp.now());
            orderData.put("idDiscount", "idDiscount0000001"); // Có thể thay đổi theo logic business
            orderData.put("idMethodPayment", "idMethodPayment01"); // MoMo payment
            orderData.put("idScreening", "idScreening0000000000000000001"); // Lấy từ server hoặc SharedPreferences
            orderData.put("idUser", uid); // Lấy từ SharedPreferences hoặc Firebase Auth
            orderData.put("screenRoomName", screenRoomName);
            orderData.put("stateOrder", "Thanh toán thành công");
            orderData.put("totalPrice", totalAmount);
            orderData.put("transactionId", transactionId != null ? transactionId : "");
            orderData.put("paymentStatus", paymentStatus);

            // Lưu order chính
            db.collection("orders")
                    .document(orderId)
                    .set(orderData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Order saved successfully");

                        // Lưu thông tin ghế đã chọn
                        saveDeskSelections();

                        Toast.makeText(this, "Đơn hàng đã được lưu thành công!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving order", e);
                        Toast.makeText(this, "Lỗi lưu đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error preparing order data", e);
            Toast.makeText(this, "Lỗi chuẩn bị dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDeskSelections() {
        if (selectedDeskIds == null || selectedDeskIds.isEmpty()) {
            Log.w(TAG, "No desk selections to save");
            return;
        }

        // Lưu thông tin ghế đã chọn vào subcollection
        for (String deskId : selectedDeskIds) {
            Map<String, Object> deskData = new HashMap<>();
            deskData.put("deskId", deskId);
            deskData.put("orderId", orderId);
            deskData.put("selectedAt", Timestamp.now());

            db.collection("orders")
                    .document(orderId)
                    .collection("desk")
                    .document(deskId)
                    .set(deskData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Desk selection saved: " + deskId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving desk selection: " + deskId, e);
                    });
        }
    }

    private String getCurrentUserId() {
        // Lấy user ID từ SharedPreferences hoặc Firebase Auth
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", "idUser1"); // Default user
        return userId;
    }

    private void displayOrderInfo() {
        tvMovieName.setText(movieName);
        tvDateTime.setText(screeningDateTime);
        tvScreenRoom.setText(screenRoomName);

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
            Intent intent = new Intent(PaymentResultActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}