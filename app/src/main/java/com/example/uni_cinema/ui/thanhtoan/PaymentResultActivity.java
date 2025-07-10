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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

// Giữ nguyên phần package và import như bạn đã viết

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
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

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
            String orderId = appLinkData.getQueryParameter("orderId");
            String amount = appLinkData.getQueryParameter("amount");
            String orderInfo = appLinkData.getQueryParameter("orderInfo");
            String transId = appLinkData.getQueryParameter("transId");
            String message = appLinkData.getQueryParameter("message");

            displayPaymentResult(resultCode, orderId, amount, orderInfo, transId, message);

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
            detailedMessage = "Lỗi mã: " + resultCode;
            statusColor = android.R.color.holo_red_dark;
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
                tvTotalAmount.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", amt));
            }
        } catch (Exception e) {
            tvTotalAmount.setText("Số tiền: N/A");
        }
    }

    private void verifyPaymentWithServer(String orderId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.88.175:5000/payment/check-order-status?orderId=" + orderId);
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

                    JSONArray seatArray = json.optJSONArray("desk");
                    selectedDeskIds.clear();
                    if (seatArray != null) {
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
                }

            } catch (Exception e) {
                Log.e(TAG, "verifyPaymentWithServer error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show());
            }
        }).start();
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