package com.example.uni_cinema.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uni_cinema.MainActivity;
import com.example.uni_cinema.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView goToRegisterText;
    TextView forgotPasswordTextView;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        goToRegisterText = findViewById(R.id.goToRegisterText);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordText);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        // Lấy UID của người dùng hiện tại
                        String uid = mAuth.getCurrentUser().getUid();

                        // Lấy tài liệu của người dùng từ Firestore
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String firstName = documentSnapshot.getString("firstName");
                                        String lastName = documentSnapshot.getString("lastName");
                                        String point = documentSnapshot.getString("pointUser");
                                        String idMember = documentSnapshot.getString("idMemberShip");
                                        String birthDay = documentSnapshot.getString("birthOfDateUser");

                                        // Tạo Bundle và thêm dữ liệu
                                        Bundle bundle = new Bundle();
                                        bundle.putString("firstName", firstName != null ? firstName : "");
                                        bundle.putString("lastName", lastName != null ? lastName : "");
                                        bundle.putString("pointUser", point != null ? point : "");
                                        bundle.putString("idMemberShip", idMember != null ? idMember : "");
                                        bundle.putString("birthOfDateUser", birthDay != null ? birthDay : "");

                                        // Tạo Intent và gắn Bundle vào
                                        Intent intent = new Intent(this, MainActivity.class);
                                        intent.putExtras(bundle);

                                        // Khởi chạy Activity
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                                        // Vẫn chuyển sang MainActivity nếu cần
                                        startActivity(new Intent(this, MainActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi lấy dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    // Vẫn chuyển sang MainActivity nếu cần
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                        Log.e("AUTH_FAIL", "Firebase login error", e);
                    });
        });


        goToRegisterText.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
        forgotPasswordTextView.setOnClickListener(v -> {
            startActivity(new Intent(this, QuenMKActivity.class));
        });
    }
}