package com.example.uni_cinema.ui.thanhtoan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.TextView;

import com.example.uni_cinema.R;
import com.example.uni_cinema.login.LoginActivity; // Import LoginActivity
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PaymentActivity extends AppCompatActivity {
    private static final String TAG = "PaymentActivity";

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException("Key or data is null");
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            Log.e(TAG, "Error in hmacSHA512: " + ex.getMessage(), ex);
            return "";
        }
    }

    private TextView tvMovieName, tvDateTime, tvScreenRoom, tvSelectedSeats, tvTotalAmount;
    private RadioGroup radioGroupPaymentMethods;
    private Button btnConfirmPayment;
    private ImageButton btnBack;

    private ArrayList<String> selectedDeskIds;
    private ArrayList<String> selectedDeskCategories;
    private ArrayList<Integer> selectedDeskPrices;
    private double totalAmount;

    private String movieName;
    private String screeningDateTime;
    private String screenRoomName;

    private String selectedPaymentMethod = "mã QR";
    private String currentOrderReferenceId;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        try {
            tvMovieName = findViewById(R.id.tv_movie_name);
            tvDateTime = findViewById(R.id.tv_date_time);
            tvScreenRoom = findViewById(R.id.tv_screen_room);
            tvSelectedSeats = findViewById(R.id.tv_selected_seats);
            tvTotalAmount = findViewById(R.id.tv_total_amount);
            radioGroupPaymentMethods = findViewById(R.id.radioGroupPaymentMethods);
            btnConfirmPayment = findViewById(R.id.btnThanhToan);
            btnBack = findViewById(R.id.btn_back);

            if (tvMovieName == null || tvDateTime == null || tvScreenRoom == null ||
                    tvSelectedSeats == null || tvTotalAmount == null || radioGroupPaymentMethods == null ||
                    btnConfirmPayment == null || btnBack == null) {
                throw new IllegalStateException("One or more views not found in layout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi tải giao diện. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

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
                if (selectedDeskCategories == null) selectedDeskCategories = new ArrayList<>();
                if (selectedDeskPrices == null) selectedDeskPrices = new ArrayList<>();
            } else {
                throw new IllegalStateException("No intent or extras found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving intent data: " + e.getMessage(), e);
            Toast.makeText(this, "Không nhận được thông tin đơn hàng. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        updateOrderSummaryUI();

        RadioButton rbDefault = findViewById(R.id.btnThanhToanQR);
        if (rbDefault != null) {
            rbDefault.setChecked(true);
        } else {
            Log.w(TAG, "Default RadioButton (btnThanhToanQR) not found");
        }

        radioGroupPaymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
            try {
                RadioButton checkedRadioButton = findViewById(checkedId);
                if (checkedRadioButton != null) {
                    selectedPaymentMethod = checkedRadioButton.getText().toString().replace("Thanh toán bằng ", "");
                    Log.d(TAG, "Selected payment method: " + selectedPaymentMethod);
                } else {
                    throw new IllegalStateException("Checked RadioButton is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling payment method selection: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi chọn phương thức thanh toán.", Toast.LENGTH_SHORT).show();
            }
        });

        btnConfirmPayment.setOnClickListener(v -> {
            try {
                if (totalAmount <= 0 || selectedDeskIds == null || selectedDeskIds.isEmpty()) {
                    Toast.makeText(this, "Thông tin đơn hàng không hợp lệ.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Retrieve UID from SharedPreferences
                SharedPreferences sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                String userUid = sharedPref.getString("user_uid", null); // Default to null if not found

                if (userUid == null || userUid.isEmpty()) {
                    // UID is empty or not found, redirect to LoginActivity
                    Toast.makeText(this, "Bạn cần đăng nhập để thanh toán.", Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    startActivity(loginIntent);
                    // Optionally, you might want to finish PaymentActivity here if login is mandatory
                    // finish();
                } else {
                    // UID exists, proceed with payment
                    initiatePayment();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initiating payment: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi bắt đầu thanh toán. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void updateOrderSummaryUI() {
        try {
            tvMovieName.setText(movieName != null ? movieName : "N/A");
            tvDateTime.setText(screeningDateTime != null ? screeningDateTime : "N/A");
            tvScreenRoom.setText(screenRoomName != null ? screenRoomName : "N/A");

            StringBuilder seatsInfo = new StringBuilder();
            if (selectedDeskIds != null && !selectedDeskIds.isEmpty()) {
                int size = selectedDeskIds.size();
                for (int i = 0; i < size; i++) {
                    String seatId = selectedDeskIds.get(i);
                    String category = (selectedDeskCategories != null && i < selectedDeskCategories.size()) ? selectedDeskCategories.get(i) : "N/A";
                    int price = (selectedDeskPrices != null && i < selectedDeskPrices.size()) ? selectedDeskPrices.get(i) : 0;
                    seatsInfo.append(seatId).append(" (").append(category).append(", ")
                            .append(String.format(Locale.getDefault(), "%,d VND", price)).append(")\n");
                }
            } else {
                seatsInfo.append("Chưa chọn ghế nào");
            }
            tvSelectedSeats.setText(seatsInfo.toString());
            tvTotalAmount.setText(String.format(Locale.getDefault(), "%,d VND", (int) totalAmount));
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi cập nhật thông tin đơn hàng.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initiatePayment() {
        try {
            Log.d(TAG, "Initiating payment with totalAmount: " + totalAmount + ", selectedDeskIds: " + (selectedDeskIds != null ? selectedDeskIds.toString() : "null"));
            Toast.makeText(this, "Đang chuẩn bị thanh toán qua " + selectedPaymentMethod + "...", Toast.LENGTH_LONG).show();

            SharedPreferences prefs = getSharedPreferences("PaymentData", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.apply();

            String orderIdForVNPay = System.currentTimeMillis() + "_" + (selectedDeskIds != null && !selectedDeskIds.isEmpty() ? selectedDeskIds.get(0) : "BOOKING");
            currentOrderReferenceId = orderIdForVNPay;
            String dummyVNPAYUrl = generateDummyVNPAYUrl(totalAmount, orderIdForVNPay);

            if (dummyVNPAYUrl == null || dummyVNPAYUrl.isEmpty()) {
                throw new IllegalStateException("Generated VNPAY URL is invalid");
            }

            openPaymentGateway(dummyVNPAYUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error initiating payment process: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi trong quá trình thanh toán. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
        }
    }

    private String generateDummyVNPAYUrl(double amount, String orderId) {
        try {
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String vnp_TmnCode = "YYEMP9YC";
            String vnp_Amount = String.valueOf((long) (amount * 100));
            String vnp_CurrCode = "VND";
            String vnp_TxnRef = orderId;
            String vnp_OrderInfo = "ThanhToanVePhim_" + vnp_TxnRef;
            String vnp_OrderType = "billpayment";
            String vnp_Locale = "vn";
            String vnp_ReturnUrl = "https://sandbox.vnpayment.vn"; // hoặc trang trống
            String vnp_IpAddr = "127.0.0.1";

            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(new java.util.Date());

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", vnp_Amount);
            vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", vnp_OrderType);
            vnp_Params.put("vnp_Locale", vnp_Locale);
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            String queryUrl = query.toString();
            String vnp_HashSecret = "ANDG1IXPFGZL9MBYJDJRUMDZ83L79GCJ";
            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
                throw new IllegalStateException("Failed to generate secure hash");
            }
            queryUrl = queryUrl.isEmpty() ? "vnp_SecureHash=" + vnp_SecureHash : queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;
            String finalUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?" + queryUrl;
            Log.d(TAG, "Generated VNPAY URL: " + finalUrl);
            return finalUrl;
        } catch (Exception e) {
            Log.e(TAG, "Error generating VNPAY URL: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể tạo URL thanh toán. Vui lòng kiểm tra log.", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void openPaymentGateway(String url) {
        if (url != null && !url.isEmpty()) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Không thể mở cổng thanh toán. Vui lòng kiểm tra lại URL hoặc cài đặt ứng dụng.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "URL thanh toán trống. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference ordersRef = db.collection("orders");
            // Lấy URL từ Clipboard hoặc kiểm tra trạng thái đơn hàng nếu cần (Cách 1: tự kiểm tra)
            SharedPreferences prefs = getSharedPreferences("PaymentData", MODE_PRIVATE);
            String lastOrderId = currentOrderReferenceId; // Dùng orderId hiện tại để kiểm tra trạng thái

            if (lastOrderId != null && !lastOrderId.isEmpty()) {
                Log.d(TAG, "Đang kiểm tra trạng thái đơn hàng: " + lastOrderId);

                // GIẢ LẬP KẾT QUẢ (bạn cần thay bằng gọi API server nếu có)
                boolean isPaymentSuccess = true; // Hoặc false nếu muốn giả lập thất bại

                if (isPaymentSuccess) {
                    Toast.makeText(this, "Thanh toán thành công cho đơn hàng " + lastOrderId, Toast.LENGTH_LONG).show();
                    Task<QuerySnapshot> querySnapshotTask = ordersRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            long count = task.getResult().size(); // Get the number of documents
                            String OrderId = "idOrder" + String.format("%027d", count + 1); // Generate lastOrderId
                            // TODO: Cập nhật trạng thái đơn hàng vào Firebase hoặc Server nếu cần

                            Intent intent = new Intent(this, PaymentResultActivity.class);
                            intent.putExtra("payment_success", true);
                            intent.putExtra(git "order_id", OrderId);
                            intent.putExtra("idUser", getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("user_uid", ""));
                            intent.putExtra("totalPrice", totalAmount);
                            intent.putExtra("idMethodPayment", selectedPaymentMethod);
                            intent.putStringArrayListExtra("selectedDeskIds", selectedDeskIds);
                            intent.putExtra("movieName", movieName);
                            intent.putExtra("screeningDateTime", screeningDateTime);
                            intent.putExtra("screenRoomName", screenRoomName);
                            startActivity(intent);
                            finish();
                        }
                    })
                ;} else {
                    Toast.makeText(this, "Thanh toán thất bại hoặc bị hủy!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(this, PaymentResultActivity.class);
                    intent.putExtra("payment_success", false);
                    intent.putExtra("order_id", lastOrderId);
                    startActivity(intent);
                    finish();
                }
            } else {
                Log.w(TAG, "Không có mã đơn hàng để kiểm tra.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi kiểm tra kết quả thanh toán: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi kiểm tra kết quả thanh toán.", Toast.LENGTH_SHORT).show();
        }
    }
}
