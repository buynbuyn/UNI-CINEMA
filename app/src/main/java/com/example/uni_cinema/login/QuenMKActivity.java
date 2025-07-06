package com.example.uni_cinema.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uni_cinema.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class QuenMKActivity extends AppCompatActivity {

    EditText edtEmail;
    Button btnResetPassword;
    Button backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quen_mk);

        edtEmail = findViewById(R.id.edtEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        backButton= findViewById(R.id.back);

        btnResetPassword.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra xem email có trong Firestore không
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Email tồn tại trong Firestore, gửi email reset
                            auth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(this, "Đã gửi email khôi phục mật khẩu", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(this, "Gửi email thất bại gửi lại sau" , Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Email không tồn tại
                            Toast.makeText(this, "Email chưa tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi kiểm tra email", Toast.LENGTH_SHORT).show();
                    });

        });
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}
