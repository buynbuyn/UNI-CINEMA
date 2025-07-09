package com.example.uni_cinema.login;

import android.content.Intent;
import android.content.SharedPreferences;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView goToRegisterText;
    TextView forgotPasswordTextView;
    FirebaseAuth mAuth;
    private static final String BASE_URL = "http://10.0.2.2:5000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);


        System: loginButton = findViewById(R.id.loginButton);
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

                        // Store UID and fetch JWT token
                        SharedPreferences sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("user_uid", uid);
                        editor.apply();

                        // Fetch JWT token from server
                        new Thread(() -> {
                            try {
                                URL url = new URL(BASE_URL + "/login");
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("Content-Type", "application/json");
                                conn.setDoOutput(true);

                                JSONObject json = new JSONObject();
                                json.put("email", email);
                                json.put("uid", uid);

                                OutputStream os = conn.getOutputStream();
                                os.write(json.toString().getBytes());
                                os.flush();
                                os.close();

                                int responseCode = conn.getResponseCode();
                                if (responseCode == HttpURLConnection.HTTP_OK) {
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                    StringBuilder response = new StringBuilder();
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        response.append(line);
                                    }
                                    reader.close();

                                    JSONObject responseJson = new JSONObject(response.toString());
                                    String jwtToken = responseJson.getString("token");

                                    // Store JWT token in SharedPreferences
                                    editor.putString("jwt_token", jwtToken);
                                    editor.apply();
                                } else {
                                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Không thể lấy JWT token", Toast.LENGTH_SHORT).show());
                                }
                                conn.disconnect();
                            } catch (Exception e) {
                                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Lỗi khi lấy JWT token: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }).start();

                        // Lấy tài liệu của người dùng từ Firestore
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String firstName = documentSnapshot.getString("firstName");
                                        String lastName = documentSnapshot.getString("lastName");
                                        String point = documentSnapshot.getString("pointUser");
                                        String idMember = documentSnapshot.getString("idMemberShip");
                                        String birthDay = documentSnapshot.getString("birthOfDateUser");

                                        Bundle bundle = new Bundle();
                                        bundle.putString("firstName", firstName != null ? firstName : "");
                                        bundle.putString("lastName", lastName != null ? lastName : "");
                                        bundle.putString("pointUser", point != null ? point : "");
                                        bundle.putString("idMemberShip", idMember != null ? idMember : "");
                                        bundle.putString("birthOfDateUser", birthDay != null ? birthDay : "");

                                        Intent intent = new Intent(this, MainActivity.class);
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, MainActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi lấy dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                        Log.e("AUTH_FAIL", "Firebase login error", e);
                    });
        });

        goToRegisterText.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        forgotPasswordTextView.setOnClickListener(v -> startActivity(new Intent(this, QuenMKActivity.class)));
    }
}