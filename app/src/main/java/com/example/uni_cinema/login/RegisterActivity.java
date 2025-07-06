package com.example.uni_cinema.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uni_cinema.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText, confirmPasswordEditText, fullNameEditText;
    Button registerButton;
    TextView goToLoginText;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ view
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        registerButton = findViewById(R.id.registerButton);
        goToLoginText = findViewById(R.id.goToLoginText);

        // Xử lý đăng ký
        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            String fullName = fullNameEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = mAuth.getCurrentUser().getUid();

                        // Tạo dữ liệu người dùng mặc định
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("full_name", fullName);
                        userData.put("birthday", "");
                        userData.put("phone", "");
                        userData.put("address", "");

                        // Lưu vào Firestore
                        db.collection("users").document(uid)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi đăng ký: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        // Chuyển sang màn hình đăng nhập
        goToLoginText.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
