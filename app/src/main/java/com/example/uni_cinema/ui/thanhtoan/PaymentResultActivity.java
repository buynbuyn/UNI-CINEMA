package com.example.uni_cinema.ui.thanhtoan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uni_cinema.R; // Đảm bảo import đúng
import com.example.uni_cinema.MainActivity; // Để quay về trang chính sau khi hoàn tất

import java.util.Locale;

public class PaymentResultActivity extends AppCompatActivity {

    private static final String TAG = "PaymentResultActivity";

    private TextView tvMovieName, tvDateTime, tvScreenRoom, tvSelectedSeats, tvTotalAmount;
    private TextView tvPaymentStatus, tvPaymentMessage;
    private TextView tvVnpayTransactionId, tvVnpayResponseCode, tvVnpayMessage;
    private ImageButton btnBack;
    private android.widget.Button btnCompletePayment; // Sử dụng android.widget.Button hoặc androidx.appcompat.widget.AppCompatButton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result); // Đảm bảo layout là activity_payment_result

        // Ánh xạ các views
        try {
            tvMovieName = findViewById(R.id.tv_movie_name_result);
            tvDateTime = findViewById(R.id.tv_date_time_result);
            tvScreenRoom = findViewById(R.id.tv_screen_room_result);
            tvSelectedSeats = findViewById(R.id.tv_selected_seats_result);
            tvTotalAmount = findViewById(R.id.tv_total_amount_result);
            tvPaymentStatus = findViewById(R.id.tv_payment_status);
            tvPaymentMessage = findViewById(R.id.tv_payment_message);
            tvVnpayTransactionId = findViewById(R.id.tv_vnpay_transaction_id);
            tvVnpayResponseCode = findViewById(R.id.tv_vnpay_response_code);
            tvVnpayMessage = findViewById(R.id.tv_vnpay_message);
            btnBack = findViewById(R.id.btn_back_result);
            btnCompletePayment = findViewById(R.id.btn_complete_payment);

            if (tvMovieName == null || tvDateTime == null || tvScreenRoom == null ||
                    tvSelectedSeats == null || tvTotalAmount == null || tvPaymentStatus == null ||
                    tvPaymentMessage == null || tvVnpayTransactionId == null ||
                    tvVnpayResponseCode == null || tvVnpayMessage == null ||
                    btnBack == null || btnCompletePayment == null) {
                throw new IllegalStateException("One or more views not found in layout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi tải giao diện kết quả thanh toán. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Lấy dữ liệu từ Deep Link (khi VNPAY trả về)
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            Log.d(TAG, "Deep Link received! Host: " + appLinkData.getHost() + ", Scheme: " + appLinkData.getScheme());

            // Lấy các tham số từ VNPAY response
            String vnp_ResponseCode = appLinkData.getQueryParameter("vnp_ResponseCode");
            String vnp_TxnRef = appLinkData.getQueryParameter("vnp_TxnRef");
            String vnp_Amount = appLinkData.getQueryParameter("vnp_Amount");
            String vnp_OrderInfo = appLinkData.getQueryParameter("vnp_OrderInfo");
            String vnp_BankCode = appLinkData.getQueryParameter("vnp_BankCode");
            String vnp_PayDate = appLinkData.getQueryParameter("vnp_PayDate");
            String vnp_TransactionNo = appLinkData.getQueryParameter("vnp_TransactionNo");
            String vnp_CardType = appLinkData.getQueryParameter("vnp_CardType");
            String vnp_TmnCode = appLinkData.getQueryParameter("vnp_TmnCode");
            String vnp_SecureHash = appLinkData.getQueryParameter("vnp_SecureHash"); // Hash để xác minh tính toàn vẹn dữ liệu

            // Log tất cả các tham số nhận được
            Log.d(TAG, "VNPAY Response Code: " + vnp_ResponseCode);
            Log.d(TAG, "VNPAY TxnRef: " + vnp_TxnRef);
            Log.d(TAG, "VNPAY Amount: " + vnp_Amount);
            Log.d(TAG, "VNPAY Order Info: " + vnp_OrderInfo);
            Log.d(TAG, "VNPAY Transaction No: " + vnp_TransactionNo);
            Log.d(TAG, "VNPAY Secure Hash: " + vnp_SecureHash);
            // ... log thêm các tham số khác nếu cần

            displayPaymentResult(vnp_ResponseCode, vnp_TxnRef, vnp_Amount, vnp_OrderInfo, vnp_TransactionNo);

            // TODO: RẤT QUAN TRỌNG - XÁC MINH TÍNH TOÀN VẸN DỮ LIỆU BẰNG SECURE HASH
            // Bạn cần tính toán lại SecureHash từ các tham số nhận được và so sánh với vnp_SecureHash.
            // Nếu không khớp, dữ liệu có thể đã bị giả mạo. Việc này TỐT NHẤT nên làm ở backend.
            // Nếu bạn làm ở client, hãy đảm bảo HashSecret không bị lộ.
            // Tham khảo hàm hmacSHA512 của bạn trong PaymentActivity để tái tạo SecureHash.
            // Tuy nhiên, việc này KHÔNG AN TOÀN nếu chỉ làm ở client-side vì HashSecret bị lộ.
            // Một giải pháp tốt hơn là VNPAY gửi kết quả về một server API của bạn, sau đó server gửi thông báo về app.

        } else {
            Log.d(TAG, "Activity started without Deep Link. Possibly from direct navigation.");
            // Trường hợp Activity được mở trực tiếp mà không qua Deep Link (ví dụ: quay lại từ back stack)
            // Bạn có thể hiển thị thông báo mặc định hoặc lấy dữ liệu từ SharedPreferences nếu bạn lưu trước đó.
            tvPaymentStatus.setText("Thông tin đơn hàng");
            tvPaymentMessage.setText("Không có kết quả thanh toán chi tiết từ VNPAY.");
            // Cố gắng hiển thị thông tin đơn hàng từ SharedPreferences nếu có
            loadOrderInfoFromSharedPreferences();
        }

        // TODO: Lấy thông tin đơn hàng gốc từ SharedPreferences hoặc Intent nếu PaymentActivity đã truyền qua
        // Bạn cần một cơ chế để chuyển thông tin phim, ghế, tổng tiền từ PaymentActivity sang đây
        // Phù hợp nhất là lưu vào SharedPreferences HOẶC truyền lại qua Intent khi khởi tạo Deep Link
        // Hiện tại code này chỉ lấy dữ liệu VNPAY response. Dữ liệu phim/ghế cần được truyền riêng.
        loadOrderInfoFromSharedPreferences();

        // Xử lý nút quay lại
        btnBack.setOnClickListener(v -> finish()); // Đóng PaymentResultActivity

        // Xử lý nút hoàn tất, quay về màn hình chính hoặc màn hình lịch sử đơn hàng
        btnCompletePayment.setOnClickListener(v -> {
            Intent mainIntent = new Intent(PaymentResultActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Xóa các activity trên stack
            startActivity(mainIntent);
            finish(); // Đóng PaymentResultActivity
        });
    }

    private void loadOrderInfoFromSharedPreferences() {
    }

    private void displayPaymentResult(String responseCode, String txnRef, String amount, String orderInfo, String transactionNo) {
        String statusMessage = "Không xác định";
        String detailedMessage = "Vui lòng kiểm tra lại.";
        int statusColor = R.color.black; // Màu mặc định

        if (responseCode != null) {
            switch (responseCode) {
                case "00":
                    statusMessage = "Thanh toán thành công!";
                    detailedMessage = "Giao dịch của bạn đã được thực hiện thành công.";
                    statusColor = android.R.color.holo_green_dark;
                    break;
                case "07":
                    statusMessage = "Trừ tiền thành công, giao dịch bị nghi ngờ (liên quan tới lừa đảo)";
                    detailedMessage = "Giao dịch có dấu hiệu bất thường, vui lòng liên hệ VNPAY.";
                    statusColor = android.R.color.holo_orange_dark;
                    break;
                case "09":
                    statusMessage = "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.";
                    detailedMessage = "Vui lòng đăng ký dịch vụ Internet Banking.";
                    statusColor = android.R.color.holo_red_dark;
                    break;
                case "10":
                    statusMessage = "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
                    detailedMessage = "Bạn đã nhập sai thông tin thẻ/tài khoản nhiều lần. Vui lòng thử lại sau.";
                    statusColor = android.R.color.holo_red_dark;
                    break;
                case "11":
                    statusMessage = "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Vui lòng thực hiện lại giao dịch.";
                    detailedMessage = "Thời gian chờ thanh toán đã hết. Vui lòng thử lại.";
                    statusColor = android.R.color.holo_red_dark;
                    break;
                case "12":
                    statusMessage = "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.";
                    detailedMessage = "Thẻ/tài khoản của bạn đã bị khóa. Vui lòng liên hệ ngân hàng.";
                    statusColor = android.R.color.holo_red_dark;
                    break;
                case "13":
                    statusMessage = "Giao dịch không thành công do: Sai mã xác thực OTP.";
                    detailedMessage = "Mã OTP không đúng. Vui lòng thử lại.";
                    statusColor = android.R.color.holo_red_dark;
                    break;
                case "24":
                    statusMessage = "Giao dịch không thành công do: Khách hàng hủy giao dịch.";
                    detailedMessage = "Bạn đã hủy giao dịch.";
                    statusColor = android.R.color.holo_red_dark;
                    break;
                case "51":
                    statusMessage = "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
                    detailedMessage = "Số dư tài khoản không đủ. Vui lòng kiểm tra lại.";
                    statusColor = android.R.color.holo_red_dark;
                    break;
                case "65":
                    statusMessage = "Giao dịch không thành công do: Tài khoản của Quý khách vượt quá hạn mức giao dịch trong ngày.";
                    detailedMessage = "Bạn đã vượt quá hạn mức giao dịch trong ngày.";
                    statusColor = android.R.color.holo_red_dark;
                    break;
                case "75":
                    statusMessage = "Ngân hàng đang bảo trì.";
                    detailedMessage = "Hệ thống ngân hàng đang bảo trì. Vui lòng thử lại sau.";
                    statusColor = android.R.color.holo_orange_dark;
                    break;
                case "79":
                    statusMessage = "Giao dịch không thành công do: KH nhập sai mật khẩu quá số lần quy định của ngân hàng.";
                    detailedMessage = "Bạn đã nhập sai mật khẩu quá số lần cho phép.";
                    statusColor = android.R.color.holo_red_dark;
                    break;
                default:
                    statusMessage = "Giao dịch không thành công";
                    detailedMessage = "Có lỗi xảy ra trong quá trình thanh toán. Mã lỗi: " + responseCode;
                    statusColor = android.R.color.holo_red_dark;
                    break;
            }
        }

        tvPaymentStatus.setText(statusMessage);
        tvPaymentStatus.setTextColor(getResources().getColor(statusColor));
        tvPaymentMessage.setText(detailedMessage);

        tvVnpayTransactionId.setText("Mã giao dịch VNPAY: " + (transactionNo != null ? transactionNo : "N/A"));
        tvVnpayResponseCode.setText("Mã phản hồi VNPAY: " + (responseCode != null ? responseCode : "N/A"));
        tvVnpayMessage.setText("Thông tin đơn hàng VNPAY: " + (orderInfo != null ? orderInfo : "N/A"));

        // Hiển thị số tiền thanh toán (VNPAY trả về theo đơn vị nhỏ nhất, vd: 1000000 = 10.000 VNĐ)
        if (amount != null && !amount.isEmpty()) {
            try {
                long amountInXu = Long.parseLong(amount);
                double actualAmount = (double) amountInXu / 100; // Chuyển đổi lại về VNĐ
                tvTotalAmount.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", actualAmount));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing amount: " + e.getMessage());
                tvTotalAmount.setText("Số tiền: Lỗi định dạng");
            }
        }
    }

    // Hàm này để tải lại thông tin đơn hàng (phim, ghế, tổng tiền)
    // từ SharedPreferences nếu PaymentActivity đã lưu trữ chúng trước khi gọi VNPAY
//    private void loadOrderInfoFromSharedPreferences() {
//        SharedPreferences prefs = getSharedPreferences("PaymentData", MODE_PRIVATE);
//        // Lấy thông tin đã lưu trữ
//        String movieName = prefs.getString("movieName", "N/A");
//        String screeningDateTime = prefs.getString("screeningDateTime", "N/A");
//        String screenRoomName = prefs.getString("screenRoomName", "N/A");
//        String selectedSeats = prefs.getString("selectedSeatsDisplay", "Chưa chọn ghế nào"); // Lưu chuỗi hiển thị ghế
//        double totalAmount = prefs.getFloat("totalAmount", 0); // Lấy số tiền từ SharedPreferences
//
//        // Hiển thị lên UI
//        tvMovieName.setText(movieName);
//        tvDateTime.setText(screeningDateTime);
//        tvScreenRoom.setText(screenRoomName);
//        tvSelectedSeats.setText(selectedSeats);
//        tvTotalAmount.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", totalAmount)); // Hiển thị lại tổng tiền đã lưu
//
//        // Sau khi sử dụng, có thể xóa dữ liệu từ SharedPreferences để tránh lưu trữ cũ
//        // SharedPreferences.Editor editor = prefs.edit();
//        // editor.clear();
//        // editor.apply();
//    }
}