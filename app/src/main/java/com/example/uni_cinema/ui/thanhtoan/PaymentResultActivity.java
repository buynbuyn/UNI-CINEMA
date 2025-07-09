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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class PaymentResultActivity extends AppCompatActivity {

    private static final String TAG = "PaymentResultActivity";

    private TextView tvMovieName, tvDateTime, tvScreenRoom, tvSelectedSeats, tvTotalAmount;
    private TextView tvPaymentStatus, tvPaymentMessage;
    private TextView tvMomoTransactionId, tvMomoResponseCode, tvMomoMessage;
    private TextView tvPaymentMethod, tvUserId; // Thêm các trường mới
    private ImageButton btnBack;
    private android.widget.Button btnCompletePayment;

    private ArrayList<String> selectedDeskIds;
    private ArrayList<String> selectedDeskCategories;
    private ArrayList<Integer> selectedDeskPrices;
    private int totalAmount;
    private String movieName;
    private String screeningDateTime;
    private String screenRoomName;
    private String orderId;
    private String idUser;
    private boolean paymentSuccess;
    private String idMethodPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        initializeViews();
        retrieveIntentData();
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

            if (tvMovieName == null || tvDateTime == null || tvScreenRoom == null ||
                    tvSelectedSeats == null || tvTotalAmount == null || tvPaymentStatus == null ||
                    tvPaymentMessage == null || tvMomoTransactionId == null ||
                    tvMomoResponseCode == null || tvMomoMessage == null ||
                    btnBack == null || btnCompletePayment == null) {
                throw new IllegalStateException("One or more views not found in layout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi tải giao diện kết quả thanh toán. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void retrieveIntentData() {
        try {
            Intent intent = getIntent();
            if (intent != null && intent.getExtras() != null) {
                Bundle extras = intent.getExtras();
                selectedDeskIds = extras.getStringArrayList("selectedDeskIds");
                totalAmount = extras.getInt("totalPrice", 0);
                movieName = extras.getString("movieName", "N/A");
                screeningDateTime = extras.getString("screeningDateTime", "N/A");
                screenRoomName = extras.getString("screenRoomName", "N/A");
                selectedDeskCategories = extras.getStringArrayList("selectedDeskCategories");
                selectedDeskPrices = extras.getIntegerArrayList("selectedDeskPrices");
                orderId = extras.getString("orderId", "N/A");
                idUser = extras.getString("idUser", "N/A");
                paymentSuccess = extras.getBoolean("payment_success", false);
                idMethodPayment = extras.getString("idMethodPayment", "N/A");

                if (selectedDeskIds == null) selectedDeskIds = new ArrayList<>();
                if (selectedDeskCategories == null) selectedDeskCategories = new ArrayList<>();
                if (selectedDeskPrices == null) selectedDeskPrices = new ArrayList<>();
            } else {
                Log.d(TAG, "No intent data, loading from SharedPreferences");
                loadOrderInfoFromSharedPreferences();
            }
            displayOrderInfo();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi lấy dữ liệu intent: " + e.getMessage(), e);
            loadOrderInfoFromSharedPreferences();
            displayOrderInfo();
        }
    }

    private void loadOrderInfoFromSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("PaymentData", MODE_PRIVATE);
        movieName = prefs.getString("movieName", "N/A");
        screeningDateTime = prefs.getString("screeningDateTime", "N/A");
        screenRoomName = prefs.getString("screenRoomName", "N/A");
        totalAmount = prefs.getInt("totalAmount", 0);
    }

    private void displayOrderInfo() {
        if (tvMovieName != null) tvMovieName.setText(movieName);
        if (tvDateTime != null) tvDateTime.setText(screeningDateTime);
        if (tvScreenRoom != null) tvScreenRoom.setText(screenRoomName);

        if (tvSelectedSeats != null) {
            if (selectedDeskIds != null && !selectedDeskIds.isEmpty()) {
                StringBuilder seatsDisplay = new StringBuilder();
                for (int i = 0; i < selectedDeskIds.size(); i++) {
                    if (i > 0) seatsDisplay.append(", ");
                    seatsDisplay.append(selectedDeskIds.get(i));
                    if (selectedDeskCategories != null && i < selectedDeskCategories.size()) {
                        seatsDisplay.append(" (").append(selectedDeskCategories.get(i)).append(")");
                    }
                }
                tvSelectedSeats.setText(seatsDisplay.toString());
            } else {
                tvSelectedSeats.setText("Chưa chọn ghế nào");
            }
        }

        if (tvTotalAmount != null) {
            tvTotalAmount.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", (double) totalAmount));
        }

        if (tvPaymentMethod != null) {
            tvPaymentMethod.setText("Phương thức thanh toán: " + idMethodPayment);
        }
        if (tvUserId != null) {
            tvUserId.setText("ID Người dùng: " + idUser);
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
        } else if (paymentSuccess) {
            // Xử lý khi chuyển trực tiếp từ PaymentActivity với payment_success
            displayPaymentResult("0", orderId, String.valueOf(totalAmount), "Thanh toán vé xem phim", null, "Thanh toán thành công qua " + idMethodPayment);
            if (orderId != null && !orderId.isEmpty()) {
                verifyPaymentWithServer(orderId);
            }
        } else {
            tvPaymentStatus.setText("Chưa có kết quả thanh toán");
            tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
            tvPaymentMessage.setText("Vui lòng kiểm tra lại quy trình thanh toán.");
            tvMomoTransactionId.setText("Mã giao dịch MOMO: N/A");
            tvMomoResponseCode.setText("Mã phản hồi MOMO: N/A");
            tvMomoMessage.setText("Thông tin đơn hàng MOMO: N/A");
        }
    }

    private void displayPaymentResult(String resultCode, String orderId, String amount, String orderInfo, String transId, String message) {
        String statusMessage = "Không xác định";
        String detailedMessage = "Vui lòng kiểm tra lại.";
        int statusColor = android.R.color.black;

        if (resultCode != null) {
            switch (resultCode) {
                case "0":
                case "00": // Thêm cho VNPay
                    statusMessage = "Thanh toán thành công!";
                    detailedMessage = "Giao dịch của bạn đã được thực hiện thành công qua " + idMethodPayment + ".";
                    statusColor = android.R.color.holo_green_dark;
                    break;
                case "1":
                    statusMessage = "Giao dịch thất bại";
                    detailedMessage = "Giao dịch " + idMethodPayment + " không thành công. Vui lòng thử lại.";
                    statusColor = android.R.color.holo_red_dark;
                    break;
                case "3":
                    statusMessage = "Giao dịch bị hủy";
                    detailedMessage = "Bạn đã hủy giao dịch " + idMethodPayment + ".";
                    statusColor = android.R.color.holo_orange_dark;
                    break;
                default:
                    statusMessage = "Giao dịch không thành công";
                    detailedMessage = "Có lỗi xảy ra trong quá trình thanh toán " + idMethodPayment + ". Mã lỗi: " + resultCode;
                    statusColor = android.R.color.holo_red_dark;
                    break;
            }
        }

        tvPaymentStatus.setText(statusMessage);
        tvPaymentStatus.setTextColor(getResources().getColor(statusColor, getTheme()));
        tvPaymentMessage.setText(detailedMessage);
        tvMomoTransactionId.setText("Mã giao dịch " + idMethodPayment + ": " + (transId != null ? transId : "N/A"));
        tvMomoResponseCode.setText("Mã phản hồi " + idMethodPayment + ": " + (resultCode != null ? resultCode : "N/A"));
        tvMomoMessage.setText("Thông tin đơn hàng " + idMethodPayment + ": " + (orderInfo != null ? orderInfo : "N/A"));

        if (amount != null && !amount.isEmpty()) {
            try {
                double actualAmount = Double.parseDouble(amount);
                tvTotalAmount.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", actualAmount));
            } catch (NumberFormatException e) {
                tvTotalAmount.setText("Số tiền: Lỗi định dạng");
            }
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
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    JSONObject json = new JSONObject(response.toString());
                    String status = json.optString("status", "UNKNOWN");

                    runOnUiThread(() -> {
                        if ("SUCCESS".equals(status)) {
                            Toast.makeText(this, "Xác nhận từ server: Thanh toán thành công!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Xác nhận từ server: Giao dịch chưa hoàn tất hoặc không tìm thấy.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Lỗi khi xác minh thanh toán từ server.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Lỗi kết nối tới server kiểm tra thanh toán.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void setupButtonListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCompletePayment.setOnClickListener(v -> {
            Intent mainIntent = new Intent(PaymentResultActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });
    }
}