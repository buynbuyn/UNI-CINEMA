<<<<<<< HEAD
=======
package com.example.uni_cinema.ui.thanhtoan;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri; // Để mở trình duyệt hoặc ứng dụng thanh toán
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.uni_cinema.R;
import com.google.gson.Gson;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale; // Để định dạng tiền tệ
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ThanhToanActivity extends AppCompatActivity {
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
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
            return "";
        }
    }

    private TextView tvTrainRoute, tvDateTime, tvSelectedSeats, tvTotalAmount;
    private RadioGroup radioGroupPaymentMethods;
    private Button btnConfirmPayment, btnBack;

    // Biến để lưu thông tin đơn hàng nhận được từ Intent
    private String trainId;
    private String startLocation;
    private String endLocation;
    private String trainName;
    private String date;
    private String time;
    private double pricePerSeat;
    private ArrayList<String> selectedSeatIds;

    private double totalAmount;

    // Biến để lưu phương thức thanh toán đã chọn (chúng ta sẽ chỉ tập trung vào VNPay)
    private String selectedPaymentMethod = "VNPay"; // Luôn mặc định là VNPay trong ví dụ này

    // === Tùy chọn: Để kiểm tra trạng thái sau khi quay lại từ cổng thanh toán ===
    private String currentOrderReferenceId; // Lưu trữ ID giao dịch nội bộ từ backend

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_payment); // Sử dụng layout activity_payment.xml


        // Lấy dữ liệu từ Intent được truyền từ SeatSelectionActivity
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            trainId = extras.getString("train_id");
            startLocation = extras.getString("start_location");
            endLocation = extras.getString("end_location");
            trainName = extras.getString("train_name");
            date = extras.getString("date");
            time = extras.getString("time");
            pricePerSeat = extras.getDouble("price_per_seat", 0.0);
            selectedSeatIds = extras.getStringArrayList("selected_seat_ids");
            totalAmount = extras.getDouble("total_amount", 0.0);

            // Truyền List<Seat> qua Intent


            // Cập nhật thông tin lên TextViews
            updateOrderSummaryUI();
        } else {
            Toast.makeText(this, "Không nhận được thông tin đơn hàng.", Toast.LENGTH_LONG).show();
            finish(); // Quay lại nếu không có thông tin
            return;
        }


        RadioButton rbVNPay = findViewById(R.id.btnThanhToan);
        if (rbVNPay != null) {
            rbVNPay.setChecked(true);
        }

        // Xử lý sự kiện chọn phương thức thanh toán (vẫn giữ lại nếu muốn mở rộng sau)
        radioGroupPaymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = findViewById(checkedId);
            if (checkedRadioButton != null) {
                selectedPaymentMethod = checkedRadioButton.getText().toString().replace("Thanh toán qua ", "");
                // Chỉ cho phép chọn VNPay trong ví dụ này
                if (!"VNPay".equals(selectedPaymentMethod)) {
                    Toast.makeText(ThanhToanActivity.this, "Hiện tại chỉ hỗ trợ thanh toán VNPay.", Toast.LENGTH_SHORT).show();
                    rbVNPay.setChecked(true); // Luôn đưa về VNPay
                    selectedPaymentMethod = "VNPay";
                }
            }
        });

        // Xử lý sự kiện nút Xác nhận thanh toán
        btnConfirmPayment.setOnClickListener(v -> {
            if (totalAmount <= 0 || selectedSeatIds == null || selectedSeatIds.isEmpty()) {
                Toast.makeText(this, "Không có đơn hàng để thanh toán.", Toast.LENGTH_SHORT).show();
                return;
            }
            initiatePayment();
        });

        // Xử lý sự kiện nút Quay lại
        btnBack.setOnClickListener(v -> {
            finish(); // Quay lại màn hình trước đó
        });
    }

    // Cập nhật giao diện tóm tắt đơn hàng
    private void updateOrderSummaryUI() {

    }

    // Phương thức bắt đầu quá trình thanh toán VNPay
    private void initiatePayment() {
        Toast.makeText(this, "Đang chuẩn bị thanh toán qua VNPay...", Toast.LENGTH_LONG).show();

        SharedPreferences prefs = getSharedPreferences("PaymentData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.apply();

        String dummyVNPAYUrl = generateDummyVNPAYUrl(totalAmount, selectedSeatIds.get(0)); // Dùng ID ghế đầu tiên làm ví dụ order ID

        openPaymentGateway(dummyVNPAYUrl);
    }

    // Phương thức giả lập tạo URL VNPay.
    // LƯU Ý QUAN TRỌNG: Phần `vnp_SecureHash` BẮT BUỘT PHẢI ĐƯỢC TẠO TRÊN BACKEND CỦA BẠN.
    // Đây chỉ là một URL demo để bạn hình dung cấu trúc.
    private String generateDummyVNPAYUrl(double amount, String orderId) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TmnCode = "YYEMP9YC"; // Lấy từ tài khoản test VNPay của bạn
        String vnp_Amount = String.valueOf((long) (amount * 100));
        String vnp_CurrCode = "VND";
        String vnp_TxnRef = System.currentTimeMillis() + "_" + orderId;
        String vnp_OrderInfo = "ThanhToanVeTau_" + vnp_TxnRef;
        String vnp_OrderType = "billpayment";
        String vnp_Locale = "vn";
        String vnp_ReturnUrl = "uni_cinima://payment-result"; // Deep link để quay lại app (nếu bạn có cấu hình)
        String vnp_IpAddr = "127.0.0.1"; // IP giả lập

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
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                } catch(Exception e) {
                    // Handle exception
                }
            }
        }
        String queryUrl = query.toString();
        // **KHÓA BÍ MẬT CỦA BẠN ĐẶT Ở ĐÂY - RẤT NGUY HIỂM**
        String vnp_HashSecret = "ANDG1IXPFGZL9MBYJDJRUMDZ83L79GCJ"; // Thay bằng khóa thật từ tài khoản test của bạn
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());

        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html" + "?" + queryUrl;
    }



    // Mở cổng thanh toán (trình duyệt hoặc ứng dụng cổng thanh toán)
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

    // Phương thức này được gọi khi Activity trở lại foreground
    @Override
    protected void onResume() {
        super.onResume();
        // === BƯỚC QUAN TRỌNG: KIỂM TRA TRẠNG THÁI THANH TOÁN SAU KHI QUAY LẠI ===
        // KHÔNG NÊN DỰA HOÀN TOÀN VÀO ONRESUME ĐỂ XÁC NHẬN KẾT QUẢ CHÍNH XÁC.
        // KẾT QUẢ CHÍNH XÁC NHẤT ĐẾN TỪ IPN (Instant Payment Notification) GỬI VỀ BACKEND CỦA BẠN.
        // Tuy nhiên, bạn có thể sử dụng onResume để kích hoạt một lời gọi API đến backend
        // để hỏi trạng thái của giao dịch `currentOrderReferenceId` (nếu có).

        if (currentOrderReferenceId != null && !currentOrderReferenceId.isEmpty()) {
            Toast.makeText(this, "Đang kiểm tra trạng thái thanh toán cho đơn hàng: " + currentOrderReferenceId + "...", Toast.LENGTH_LONG).show();
            // TODO: GỌI API BACKEND ĐỂ KIỂM TRA TRẠNG THÁI CỦA currentOrderReferenceId
            // Ví dụ: MyBackendService.checkPaymentStatus(currentOrderReferenceId)
            // Trong callback của API đó, bạn sẽ quyết định hiển thị màn hình thành công hay thất bại.
        }
    }
}
>>>>>>> ed52fbe311397b5ad1760974e7db2916fa1036c2
