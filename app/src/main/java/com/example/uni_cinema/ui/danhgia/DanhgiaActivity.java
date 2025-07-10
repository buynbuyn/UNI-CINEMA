package com.example.uni_cinema.ui.danhgia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uni_cinema.R;
import com.example.uni_cinema.login.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DanhgiaActivity extends AppCompatActivity implements DanhgiaAdapter.OnCommentActionListener {

    private RecyclerView rvComments;
    private TextInputEditText etComment;
    private RatingBar ratingBar;
    private Button btnSubmit;
    private ImageButton btnBack;
    private DanhgiaAdapter adapter;
    private List<com.example.uni_cinema.model.Danhgia> danhgiaList;
    private String idMovie;
    private String idUser;
    private String userEmail;
    private String token;
    private com.example.uni_cinema.model.Danhgia editingDanhgia;
    private static final String BASE_URL = "http://10.0.2.2:5000";
    private static final int LOGIN_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danhgia);

        // Khởi tạo views
        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } else {
            Toast.makeText(this, "Không tìm thấy view gốc, kiểm tra layout", Toast.LENGTH_SHORT).show();
        }

        rvComments = findViewById(R.id.rvComments);
        etComment = findViewById(R.id.etComment);
        ratingBar = findViewById(R.id.ratingBar);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btn_back);

        // Lấy idMovie từ Intent
        idMovie = getIntent().getStringExtra("idMovie");
        if (idMovie == null) {
            Toast.makeText(this, "Không tìm thấy ID phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Kiểm tra trạng thái đăng nhập và lấy UID
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            idUser = user.getUid();
            userEmail = user.getEmail();
            token = prefs.getString("jwt_token", null);
        } else {
            idUser = prefs.getString("user_uid", null);
            userEmail = null;
            token = prefs.getString("jwt_token", null);
        }
        Log.d("DanhgiaActivity", "Khởi tạo - Token: " + token + ", UID: " + idUser);

        // Khởi tạo RecyclerView
        danhgiaList = new ArrayList<>();
        adapter = new DanhgiaAdapter(this, danhgiaList, idUser, this);
        adapter.setCurrentUserEmail(userEmail);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(adapter);

        // Tải danh sách bình luận
        loadComments();

        // Xử lý nút Gửi/Sửa bình luận
        btnSubmit.setOnClickListener(v -> {
            if (editingDanhgia == null) {
                createComment();
            } else {
                updateComment();
            }
        });

        // Xử lý nút Quay lại
        btnBack.setOnClickListener(v -> finish());
    }

    private boolean checkLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        token = prefs.getString("jwt_token", null);
        idUser = user != null ? user.getUid() : prefs.getString("user_uid", null);

        if (idUser == null || token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để gửi/đánh giá!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_REQUEST_CODE);
            return false;
        }

        if (user != null) {
            userEmail = user.getEmail();
        }
        Log.d("DanhgiaActivity", "CheckLoginStatus - Token: " + token + ", UID: " + idUser);
        return true;
    }

    private boolean refreshToken() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return false;
        }

        try {
            URL url = new URL(BASE_URL + "/refresh-token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("uid", idUser);
            json.put("email", user.getEmail());

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
                String newToken = responseJson.getString("token");

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("jwt_token", newToken);
                editor.apply();
                token = newToken;
                Log.d("DanhgiaActivity", "Refreshed token: " + token);
                conn.disconnect();
                return true;
            } else {
                Log.e("DanhgiaActivity", "Refresh token failed: HTTP " + responseCode);
                conn.disconnect();
                return false;
            }
        } catch (Exception e) {
            Log.e("DanhgiaActivity", "Refresh token error: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            // Cập nhật thông tin sau khi đăng nhập
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                idUser = user.getUid();
                userEmail = user.getEmail();
                token = prefs.getString("jwt_token", null);
                Log.d("DanhgiaActivity", "onActivityResult - New Token: " + token + ", UID: " + idUser);
            } else {
                idUser = prefs.getString("user_uid", null);
                userEmail = null;
                token = prefs.getString("jwt_token", null);
            }
            adapter.setCurrentUserId(idUser);
            adapter.setCurrentUserEmail(userEmail);
            adapter.notifyDataSetChanged();
            loadComments();
        } else {
            Log.d("DanhgiaActivity", "Đăng nhập bị hủy hoặc thất bại");
        }
    }

    private void loadComments() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/comments/movie/" + idMovie);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    Gson gson = new Gson();
                    List<com.example.uni_cinema.model.Danhgia> comments = gson.fromJson(response.toString(), new TypeToken<List<com.example.uni_cinema.model.Danhgia>>(){}.getType());
                    runOnUiThread(() -> {
                        danhgiaList.clear();
                        danhgiaList.addAll(comments);
                        adapter.notifyDataSetChanged();
                    });
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    runOnUiThread(() -> {
                        Toast.makeText(DanhgiaActivity.this, "Token hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        prefs.edit().remove("jwt_token").apply();
                        Intent intent = new Intent(DanhgiaActivity.this, LoginActivity.class);
                        startActivityForResult(intent, LOGIN_REQUEST_CODE);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(DanhgiaActivity.this, "Không tìm thấy bình luận", Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(DanhgiaActivity.this, "Lỗi khi tải bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void createComment() {
        if (!checkLoginStatus()) {
            return;
        }

        // Thử làm mới token nếu có endpoint refresh-token
        if (token != null && !refreshToken()) {
            runOnUiThread(() -> {
                Toast.makeText(DanhgiaActivity.this, "Token hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                prefs.edit().remove("jwt_token").apply();
                Intent intent = new Intent(DanhgiaActivity.this, LoginActivity.class);
                startActivityForResult(intent, LOGIN_REQUEST_CODE);
            });
            return;
        }

        String commentText = etComment.getText().toString().trim();
        int rating = (int) ratingBar.getRating();
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
            return;
        }
        if (rating < 1 || rating > 5) {
            Toast.makeText(this, "Điểm đánh giá phải từ 1 đến 5", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        com.example.uni_cinema.model.Danhgia danhgia = new com.example.uni_cinema.model.Danhgia(0, idMovie, idUser, commentText, rating, dateTime);

        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/comments");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                conn.setDoOutput(true);

                Gson gson = new Gson();
                String json = gson.toJson(danhgia);
                Log.d("DanhgiaActivity", "CreateComment JSON: " + json);
                OutputStream os = conn.getOutputStream();
                os.write(json.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    runOnUiThread(() -> {
                        Toast.makeText(DanhgiaActivity.this, "Gửi bình luận thành công", Toast.LENGTH_SHORT).show();
                        etComment.setText("");
                        ratingBar.setRating(0);
                        loadComments();
                    });
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    runOnUiThread(() -> {
                        Toast.makeText(DanhgiaActivity.this, "Token hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        prefs.edit().remove("jwt_token").apply();
                        Intent intent = new Intent(DanhgiaActivity.this, LoginActivity.class);
                        startActivityForResult(intent, LOGIN_REQUEST_CODE);
                    });
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    JSONObject error = new JSONObject(response.toString());
                    String errorMessage = error.optString("message", "Lỗi khi gửi bình luận");
                    Log.e("DanhgiaActivity", "CreateComment Error: " + errorMessage);
                    runOnUiThread(() -> Toast.makeText(DanhgiaActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(DanhgiaActivity.this, "Lỗi khi gửi bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateComment() {
        if (!checkLoginStatus()) {
            return;
        }

        // Thử làm mới token nếu có endpoint refresh-token
        if (token != null && !refreshToken()) {
            runOnUiThread(() -> {
                Toast.makeText(DanhgiaActivity.this, "Token hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                prefs.edit().remove("jwt_token").apply();
                Intent intent = new Intent(DanhgiaActivity.this, LoginActivity.class);
                startActivityForResult(intent, LOGIN_REQUEST_CODE);
            });
            return;
        }

        String commentText = etComment.getText().toString().trim();
        int rating = (int) ratingBar.getRating();
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
            return;
        }
        if (rating < 1 || rating > 5) {
            Toast.makeText(this, "Điểm đánh giá phải từ 1 đến 5", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        com.example.uni_cinema.model.Danhgia updatedDanhgia = new com.example.uni_cinema.model.Danhgia(editingDanhgia.getIdComment(), idMovie, idUser, commentText, rating, dateTime);

        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/comments/" + editingDanhgia.getIdComment());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                conn.setDoOutput(true);

                Gson gson = new Gson();
                String json = gson.toJson(updatedDanhgia);
                Log.d("DanhgiaActivity", "UpdateComment JSON: " + json);
                OutputStream os = conn.getOutputStream();
                os.write(json.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(DanhgiaActivity.this, "Sửa bình luận thành công", Toast.LENGTH_SHORT).show();
                        etComment.setText("");
                        ratingBar.setRating(0);
                        btnSubmit.setText("Gửi bình luận");
                        editingDanhgia = null;
                        loadComments();
                    });
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    runOnUiThread(() -> {
                        Toast.makeText(DanhgiaActivity.this, "Token hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        prefs.edit().remove("jwt_token").apply();
                        Intent intent = new Intent(DanhgiaActivity.this, LoginActivity.class);
                        startActivityForResult(intent, LOGIN_REQUEST_CODE);
                    });
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    JSONObject error = new JSONObject(response.toString());
                    String errorMessage = error.optString("message", "Lỗi khi sửa bình luận");
                    Log.e("DanhgiaActivity", "UpdateComment Error: " + errorMessage);
                    runOnUiThread(() -> Toast.makeText(DanhgiaActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(DanhgiaActivity.this, "Lỗi khi sửa bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public void onEditComment(com.example.uni_cinema.model.Danhgia danhgia) {
        if (!checkLoginStatus()) {
            return;
        }
        editingDanhgia = danhgia;
        etComment.setText(danhgia.getComment());
        ratingBar.setRating(danhgia.getRating());
        btnSubmit.setText("Sửa bình luận");
    }
}