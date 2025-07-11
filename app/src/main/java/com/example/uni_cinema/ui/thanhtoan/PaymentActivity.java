package com.example.uni_cinema.ui.thanhtoan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uni_cinema.R;
import com.example.uni_cinema.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentActivity extends AppCompatActivity {
    private static final String TAG = "PaymentActivity";
    private static final String DEEP_LINK_SCHEME = "unicinema";
    private static final String DEEP_LINK_HOST = "payment-result";
    private static final String DEEP_LINK_URL = DEEP_LINK_SCHEME + "://" + DEEP_LINK_HOST;
    private static final String PREFS_PAYMENT_DATA = "PaymentData";
    private static final long DEEP_LINK_TIMEOUT = 10 * 60 * 1000; // 10 phút
    private static final long CHECK_INTERVAL = 30 * 1000; // 30 giây
    private static final String BASE_URL = "http://192.168.165.79:5000/payment/"; // Thay bằng URL server thực tế

    private Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Handler checkHandler = new Handler(Looper.getMainLooper());
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private Runnable timeoutRunnable;
    private Runnable checkRunnable;

    private boolean isWaitingForDeepLink = false;
    private boolean isPaymentInProgress = false;
    private String currentOrderReferenceId;

    private TextView tvMovieName, tvDateTime, tvScreenRoom, tvSelectedSeats, tvTotalAmount;
    private Button btnConfirmPayment;
    private ImageButton btnBack;

    private ArrayList<String> selectedDeskIds;
    private ArrayList<String> selectedDeskCategories;
    private ArrayList<Integer> selectedDeskPrices;
    private double totalAmount;
    private String movieName, screeningDateTime, screenRoomName;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Debug thông tin intent
        debugIntentData();

        initializeViews();
        if (retrieveIntentData()) {
            updateOrderSummaryUI();
            setupButtonListeners();
        } else {
            // Dữ liệu không hợp lệ, đóng activity
            finish();
            return;
        }
    }

    private void debugIntentData() {
        Intent intent = getIntent();
        Log.d(TAG, "=== DEBUG INTENT DATA ===");
        Log.d(TAG, "Intent: " + (intent != null ? "Not null" : "NULL"));

        if (intent != null) {
            Bundle extras = intent.getExtras();
            Log.d(TAG, "Extras: " + (extras != null ? "Not null" : "NULL"));

            if (extras != null) {
                Log.d(TAG, "All extras keys: " + extras.keySet().toString());
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    Log.d(TAG, "Key: " + key + ", Value: " + value + ", Type: " + (value != null ? value.getClass().getSimpleName() : "null"));
                }
            }
        }
        Log.d(TAG, "=== END DEBUG ===");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPaymentInProgress) {
            showPaymentInProgressDialog();
        }
        checkPaymentStatusFromPrefs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupHandlers();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void initializeViews() {
        try {
            tvMovieName = findViewById(R.id.tv_movie_name);
            tvDateTime = findViewById(R.id.tv_date_time);
            tvScreenRoom = findViewById(R.id.tv_screen_room);
            tvSelectedSeats = findViewById(R.id.tv_selected_seats);
            tvTotalAmount = findViewById(R.id.tv_total_amount);
            btnConfirmPayment = findViewById(R.id.btnThanhToan);
            btnBack = findViewById(R.id.btn_back);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo giao diện: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi tải giao diện. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean retrieveIntentData() {
        try {
            Intent intent = getIntent();
            if (intent == null) {
                Log.e(TAG, "Intent is null");
                Toast.makeText(this, "Không nhận được thông tin đơn hàng. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                return false;
            }

            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.e(TAG, "Bundle extras is null");
                Toast.makeText(this, "Không có dữ liệu đơn hàng. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                return false;
            }

            // Log all available keys for debugging
            Log.d(TAG, "Available keys in bundle: " + extras.keySet().toString());

            // Lấy dữ liệu với xử lý lỗi từng trường
            selectedDeskIds = extras.getStringArrayList("selectedDeskIds");
            if (selectedDeskIds == null) {
                selectedDeskIds = new ArrayList<>();
                Log.w(TAG, "selectedDeskIds is null, using empty list");
            }

            // Thử với cả int và double cho totalPrice
            try {
                totalAmount = extras.getInt("totalPrice", 0);
            } catch (Exception e) {
                try {
                    totalAmount = extras.getInt("totalPrice", 0);
                } catch (Exception e2) {
                    Log.w(TAG, "Cannot get totalPrice as double or int, using 0.0");
                    totalAmount = 0.0;
                }
            }

            movieName = extras.getString("movieName");
            if (movieName == null) {
                movieName = "N/A";
                Log.w(TAG, "movieName is null, using 'N/A'");
            }

            screeningDateTime = extras.getString("screeningDateTime");
            if (screeningDateTime == null) {
                screeningDateTime = "N/A";
                Log.w(TAG, "screeningDateTime is null, using 'N/A'");
            }

            screenRoomName = extras.getString("screenRoomName");
            if (screenRoomName == null) {
                screenRoomName = "N/A";
                Log.w(TAG, "screenRoomName is null, using 'N/A'");
            }

            selectedDeskCategories = extras.getStringArrayList("selectedDeskCategories");
            if (selectedDeskCategories == null) {
                selectedDeskCategories = new ArrayList<>();
                Log.w(TAG, "selectedDeskCategories is null, using empty list");
            }

            selectedDeskPrices = extras.getIntegerArrayList("selectedDeskPrices");
            if (selectedDeskPrices == null) {
                selectedDeskPrices = new ArrayList<>();
                Log.w(TAG, "selectedDeskPrices is null, using empty list");
            }

            // Log dữ liệu đã nhận
            Log.d(TAG, "Retrieved data:");
            Log.d(TAG, "selectedDeskIds size: " + selectedDeskIds.size());
            Log.d(TAG, "totalAmount: " + totalAmount);
            Log.d(TAG, "movieName: " + movieName);
            Log.d(TAG, "screeningDateTime: " + screeningDateTime);
            Log.d(TAG, "screenRoomName: " + screenRoomName);
            Log.d(TAG, "selectedDeskCategories size: " + selectedDeskCategories.size());
            Log.d(TAG, "selectedDeskPrices size: " + selectedDeskPrices.size());

            // Kiểm tra dữ liệu cơ bản
            if (selectedDeskIds.isEmpty()) {
                Log.e(TAG, "No selected seats");
                Toast.makeText(this, "Không có ghế được chọn. Vui lòng chọn ghế trước khi thanh toán.", Toast.LENGTH_LONG).show();
                return false;
            }

            if (totalAmount <= 0) {
                Log.e(TAG, "Invalid total amount: " + totalAmount);
                Toast.makeText(this, "Tổng tiền không hợp lệ. Vui lòng kiểm tra lại.", Toast.LENGTH_LONG).show();
                return false;
            }

            // Đảm bảo số lượng các mảng khớp với số ghế đã chọn
            while (selectedDeskCategories.size() < selectedDeskIds.size()) {
                selectedDeskCategories.add("Standard");
            }
            while (selectedDeskPrices.size() < selectedDeskIds.size()) {
                selectedDeskPrices.add(0);
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi lấy dữ liệu intent: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi xử lý dữ liệu đơn hàng. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void updateOrderSummaryUI() {
        try {
            tvMovieName.setText(movieName != null ? movieName : "N/A");
            tvDateTime.setText(screeningDateTime != null ? screeningDateTime : "N/A");
            tvScreenRoom.setText(screenRoomName != null ? screenRoomName : "N/A");

            StringBuilder seatsInfo = new StringBuilder();
            if (selectedDeskIds != null && !selectedDeskIds.isEmpty()) {
                for (int i = 0; i < selectedDeskIds.size(); i++) {
                    String seatId = selectedDeskIds.get(i);
                    String category = i < selectedDeskCategories.size() ? selectedDeskCategories.get(i) : "N/A";
                    int price = i < selectedDeskPrices.size() ? selectedDeskPrices.get(i) : 0;
                    seatsInfo.append(seatId).append(" (").append(category).append(", ")
                            .append(String.format(Locale.getDefault(), "%,d VND", price)).append(")");
                    if (i < selectedDeskIds.size() - 1) {
                        seatsInfo.append("\n");
                    }
                }
            } else {
                seatsInfo.append("Chưa chọn ghế nào");
            }
            tvSelectedSeats.setText(seatsInfo.toString());
            tvTotalAmount.setText(String.format(Locale.getDefault(), "%,d VND", (int) totalAmount));
        } catch (Exception e) {
            Log.e(TAG, "Lỗi cập nhật giao diện: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi cập nhật thông tin đơn hàng.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtonListeners() {
        btnConfirmPayment.setOnClickListener(v -> initiateMoMoPayment());
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void initiateMoMoPayment() {
        if (isPaymentInProgress) {
            Toast.makeText(this, "Đang xử lý thanh toán. Vui lòng chờ.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDeskIds == null || selectedDeskIds.isEmpty() || totalAmount <= 0) {
            Toast.makeText(this, "Dữ liệu đơn hàng không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        isPaymentInProgress = true;
        btnConfirmPayment.setEnabled(false);
        btnConfirmPayment.setText("Đang xử lý...");

        currentOrderReferenceId = "MOMO" + System.currentTimeMillis();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String idUser = currentUser.getUid();

        if (currentUser == null) {
            startActivity(new Intent(PaymentActivity.this, LoginActivity.class));
        }

        // Gọi API server để lấy payUrl
        executorService.execute(() -> {
            try {
                String payUrl = createPaymentRequest(idUser);
                if (payUrl != null && !payUrl.isEmpty()) {
                    runOnUiThread(() -> {
                        openPaymentGateway(payUrl);
                        startDeepLinkTimeout();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi khởi tạo thanh toán.", Toast.LENGTH_LONG).show();
                        resetPaymentState();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi gọi API thanh toán: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Không thể khởi tạo thanh toán MoMo.", Toast.LENGTH_LONG).show();
                    resetPaymentState();
                });
            }
        });
    }

    private String createPaymentRequest(String idUser) throws Exception {
        // Kiểm tra dữ liệu đầu vào
        if (idUser == null || movieName == null || selectedDeskIds == null || totalAmount <= 0) {
            Log.e(TAG, "Dữ liệu đầu vào không hợp lệ: idUser=" + idUser + ", movieName=" + movieName + ", totalAmount=" + totalAmount);
            throw new IllegalArgumentException("Dữ liệu đầu vào không hợp lệ");
        }

        URL url = new URL(BASE_URL + idUser);
        Log.d(TAG, "Request URL: " + url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000); // 30 giây
        conn.setReadTimeout(30000); // 30 giây

        // Tạo JSONArray cho idDesk
        JSONArray deskIdsArray = new JSONArray();
        for (String deskId : selectedDeskIds) {
            if (deskId != null) {
                deskIdsArray.put(deskId);
            }
        }

        // Tạo JSON request
        JSONObject json = new JSONObject();
        json.put("amount", (long) totalAmount);
        json.put("orderId", currentOrderReferenceId);
        json.put("orderInfo", "Thanh toán vé xem phim");
        json.put("redirectUrl", DEEP_LINK_URL);
        json.put("ipnUrl", BASE_URL + "payment/ipn");
        json.put("requestType", "captureWallet");
        json.put("movie", movieName);
        json.put("idDesk", deskIdsArray);
        json.put("extraData", movieName);
        json.put("screenRoom", screenRoomName);
        json.put("dateTime", screeningDateTime);

        // Log JSON để debug
        Log.d(TAG, "Request JSON: " + json.toString());

        // Gửi request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = readResponse(conn);
            Log.d(TAG, "Server response: " + response);
            JSONObject responseJson = new JSONObject(response);
            return responseJson.optString("payUrl", null);
        } else {
            String errorResponse = readErrorResponse(conn);
            Log.e(TAG, "Server error: " + responseCode + ", Response: " + errorResponse);
            throw new IOException("Server error: " + responseCode + ", Response: " + errorResponse);
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    private String readErrorResponse(HttpURLConnection conn) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    private void openPaymentGateway(String payUrl) {
        if (payUrl != null && !payUrl.isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Log.d(TAG, "Mở ứng dụng MoMo với payUrl: " + payUrl);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi mở ứng dụng MoMo: " + e.getMessage(), e);
                Toast.makeText(this, "Không thể mở ứng dụng MoMo. Vui lòng kiểm tra cài đặt MoMo.", Toast.LENGTH_LONG).show();
                resetPaymentState();
            }
        } else {
            Toast.makeText(this, "URL thanh toán MoMo trống.", Toast.LENGTH_SHORT).show();
            resetPaymentState();
        }
    }

    private void startDeepLinkTimeout() {
        isWaitingForDeepLink = true;
        timeoutRunnable = () -> {
            if (isWaitingForDeepLink) {
                Log.w(TAG, "Hết thời gian chờ deep link cho đơn hàng: " + currentOrderReferenceId);
                updatePaymentStatus("TIMEOUT");
                showTimeoutDialog();
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, DEEP_LINK_TIMEOUT);
    }

    private void showTimeoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Không nhận được kết quả")
                .setMessage("Giao dịch MoMo có thể đã hoàn tất nhưng chưa nhận được phản hồi. Kiểm tra trạng thái?")
                .setPositiveButton("Kiểm tra", (dialog, which) -> checkPaymentStatusManually())
                .setNegativeButton("Hủy", (dialog, which) -> cancelPayment())
                .setCancelable(false)
                .show();
    }

    private void handleDeepLink(Intent intent) {
        if (intent == null || intent.getData() == null) return;

        Uri uri = intent.getData();
        if (DEEP_LINK_SCHEME.equals(uri.getScheme()) && DEEP_LINK_HOST.equals(uri.getHost())) {
            resetPaymentState();
            cleanupHandlers();

            String resultCode = uri.getQueryParameter("resultCode");
            String orderId = uri.getQueryParameter("orderId");
            String message = uri.getQueryParameter("message");

            Log.d(TAG, "Deep link nhận được - resultCode: " + resultCode + ", orderId: " + orderId + ", message: " + message);

            if ("0".equals(resultCode)) {
                // Thanh toán thành công, xác nhận với server
                verifyPaymentWithServer(orderId);
            } else {
                // Thanh toán thất bại
                updatePaymentStatus("FAILED");
                String errorMessage = message != null ? message : "Thanh toán MoMo thất bại!";
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void verifyPaymentWithServer(String orderId) {
        executorService.execute(() -> {
            try {
                URL url = new URL(BASE_URL + "/check-order-status?orderId=" + orderId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String response = readResponse(conn);
                    JSONObject jsonResponse = new JSONObject(response);
                    String status = jsonResponse.optString("status", "UNKNOWN");

                    runOnUiThread(() -> {
                        if ("SUCCESS".equals(status)) {
                            updatePaymentStatus("SUCCESS");
                            updateSeatAvailability();
                            Toast.makeText(this, "Thanh toán MoMo thành công!", Toast.LENGTH_LONG).show();
                            // Có thể chuyển sang màn hình kết quả hoặc đóng activity
                            finish();
                        } else {
                            updatePaymentStatus("FAILED");
                            Toast.makeText(this, "Thanh toán thất bại. Vui lòng kiểm tra lại!", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi xác nhận trạng thái thanh toán.", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi xác nhận trạng thái: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi xác nhận trạng thái thanh toán.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Cập nhật trạng thái ghế trong Firestore sau khi thanh toán thành công
     */
    private void updateSeatAvailability() {
        if (screenRoomName == null || selectedDeskIds == null || selectedDeskIds.isEmpty()) {
            Log.e(TAG, "Không thể cập nhật trạng thái ghế: thiếu screenRoomName hoặc selectedDeskIds");
            return;
        }

        Log.d(TAG, "Đang cập nhật trạng thái ghế cho screening room: " + screenRoomName);

        // Truy vấn collection screening để tìm document có idScreening tương ứng
        db.collection("screening")
                .whereEqualTo("idScreening", screenRoomName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Lấy document đầu tiên (giả sử idScreening là unique)
                        DocumentSnapshot screeningDoc = querySnapshot.getDocuments().get(0);
                        String screeningDocId = screeningDoc.getString("idScreenRoom");

                        Log.d(TAG, "Tìm thấy screening document: " + screeningDocId);

                        // Sử dụng WriteBatch để cập nhật nhiều ghế cùng lúc
                        WriteBatch batch = db.batch();

                        for (String deskId : selectedDeskIds) {
                            Log.d("DeskId", deskId);
                            if (deskId != null && !deskId.isEmpty()) {
                                DocumentReference deskRef = db.collection("screeningRoom")
                                        .document(screeningDocId)
                                        .collection("desk")
                                        .document(deskId);

                                deskRef.get().addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        batch.update(deskRef, "available", false);
                                        Log.d(TAG, "Thêm vào batch cập nhật ghế: " + deskId);
                                    } else {
                                        Log.e(TAG, "Ghế " + deskId + " không tồn tại trong Firestore");
                                    }


                                }).addOnFailureListener(e -> {
                                    Log.e(TAG, "Lỗi kiểm tra ghế " + deskId + ": " + e.getMessage(), e);
                                });

                                batch.update(deskRef, "available", false);
                                Log.d(TAG, "Thêm vào batch cập nhật ghế: " + deskId);
                            }
                        }

                        // Commit batch
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Cập nhật trạng thái ghế thành công cho " + selectedDeskIds.size() + " ghế");
                                    Toast.makeText(this, "Đã cập nhật trạng thái ghế thành công!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Lỗi cập nhật trạng thái ghế: " + e.getMessage(), e);
                                    Toast.makeText(this, "Lỗi cập nhật trạng thái ghế. Vui lòng liên hệ hỗ trợ.", Toast.LENGTH_LONG).show();
                                });

                    } else {
                        Log.e(TAG, "Không tìm thấy screening document với idScreening: " + screenRoomName);
                        Toast.makeText(this, "Không tìm thấy phòng chiếu!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi truy vấn screening collection: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi truy cập dữ liệu. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                });
    }


    private void updatePaymentStatus(String status) {
        SharedPreferences prefs = getSharedPreferences(PREFS_PAYMENT_DATA, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("paymentStatus", status);
        editor.putString("orderId", currentOrderReferenceId);
        editor.putLong("paymentEndTime", System.currentTimeMillis());
        editor.apply();
    }

    private void navigateToPaymentStatusCheck() {
        Intent intent = new Intent(this, PaymentResultActivity.class);
        intent.putExtra("orderId", currentOrderReferenceId);
        intent.putExtra("isTimeout", true);
        startActivity(intent);
    }

    private void checkPaymentStatusFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_PAYMENT_DATA, MODE_PRIVATE);
        String status = prefs.getString("paymentStatus", "UNKNOWN");
        String orderId = prefs.getString("orderId", "");

        // Chỉ xử lý nếu là order hiện tại
        if (currentOrderReferenceId != null && currentOrderReferenceId.equals(orderId)) {
            if ("SUCCESS".equals(status)) {
                Toast.makeText(this, "Thanh toán MoMo thành công!", Toast.LENGTH_LONG).show();
                resetPaymentState();
                finish();
            } else if ("FAILED".equals(status)) {
                Toast.makeText(this, "Thanh toán MoMo thất bại!", Toast.LENGTH_LONG).show();
                resetPaymentState();
            }
        }
    }

    private void showPaymentInProgressDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thanh toán đang xử lý")
                .setMessage("Bạn đang trong quá trình thanh toán MoMo. Nếu đã hoàn tất, bạn có thể kiểm tra trạng thái hoặc hủy.")
                .setPositiveButton("Tôi đã thanh toán", (dialog, which) -> checkPaymentStatusManually())
                .setNegativeButton("Hủy thanh toán", (dialog, which) -> cancelPayment())
                .setNeutralButton("Tiếp tục chờ", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void checkPaymentStatusManually() {
        if (currentOrderReferenceId == null) {
            Toast.makeText(this, "Không có thông tin đơn hàng để kiểm tra.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Kiểm tra trạng thái cho đơn hàng: " + currentOrderReferenceId);
        Toast.makeText(this, "Đang kiểm tra trạng thái thanh toán...", Toast.LENGTH_SHORT).show();

        // Gọi API để kiểm tra trạng thái
        verifyPaymentWithServer(currentOrderReferenceId);
    }

    private void cancelPayment() {
        resetPaymentState();
        cleanupHandlers();
        updatePaymentStatus("CANCELLED");
        Toast.makeText(this, "Đã hủy thanh toán MoMo", Toast.LENGTH_SHORT).show();
    }

    private void resetPaymentState() {
        isPaymentInProgress = false;
        isWaitingForDeepLink = false;
        btnConfirmPayment.setEnabled(true);
        btnConfirmPayment.setText("Thanh toán");
    }

    private void cleanupHandlers() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        if (checkRunnable != null) {
            checkHandler.removeCallbacks(checkRunnable);
        }
    }

    @Override
    public void onBackPressed() {
        if (isPaymentInProgress) {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Thanh toán MoMo đang được xử lý. Bạn có chắc muốn thoát?")
                    .setPositiveButton("Thoát", (dialog, which) -> {
                        cancelPayment();
                        super.onBackPressed();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}