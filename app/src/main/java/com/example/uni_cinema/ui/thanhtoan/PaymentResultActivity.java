package com.example.uni_cinema.ui.thanhtoan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uni_cinema.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PaymentResultActivity extends AppCompatActivity {
    private TextView tvStatus, tvMessage, tvTotalAmount;
    private Button btnCompletePayment;

    private boolean paymentSuccess;
    private String orderId;
    private String idUser, idMethodPayment, movieName, screeningDateTime, screenRoomName;
    private double totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        tvStatus = findViewById(R.id.tv_payment_status);
        tvMessage = findViewById(R.id.tv_payment_message);
        tvTotalAmount = findViewById(R.id.tv_total_amount_result);
        btnCompletePayment = findViewById(R.id.btn_complete_payment);

        // Nhận dữ liệu từ intent
        paymentSuccess = getIntent().getBooleanExtra("payment_success", false);
        orderId = getIntent().getStringExtra("order_id");

        idUser = getIntent().getStringExtra("idUser");
        idMethodPayment = getIntent().getStringExtra("idMethodPayment");
        totalPrice = getIntent().getDoubleExtra("totalPrice", 0);

        // UI
        tvStatus.setText(paymentSuccess ? "Thanh toán thành công!" : "Thanh toán thất bại");
        tvMessage.setText(paymentSuccess ? "Cảm ơn bạn đã thanh toán." : "Vui lòng thử lại hoặc liên hệ hỗ trợ.");
        tvTotalAmount.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", totalPrice));

        // Nút Hoàn tất
        btnCompletePayment.setOnClickListener(v -> {
            if (paymentSuccess) {
                saveOrderToFirestore();
            } else {
                finish(); // nếu thất bại, chỉ thoát màn hình
            }
        });
    }

    private void saveOrderToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> orderData = new HashMap<>();
        orderData.put("idUser", idUser);
        orderData.put("idMethodPayment", idMethodPayment);
        orderData.put("idDiscount", ""); // discount rỗng
        orderData.put("totalPrice", totalPrice);
        orderData.put("stateOrder", "Thanh toán thành công!");
        orderData.put("dateTimeOrder", new SimpleDateFormat("MMMM d, yyyy 'at' hh:mm:ss a z", Locale.getDefault()).format(new Date()));

        db.collection("orders").document(orderId)
                .set(orderData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã lưu đơn hàng!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, com.example.uni_cinema.MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi lưu đơn hàng", e);
                    Toast.makeText(this, "Lưu đơn hàng thất bại", Toast.LENGTH_SHORT).show();
                });
    }
}