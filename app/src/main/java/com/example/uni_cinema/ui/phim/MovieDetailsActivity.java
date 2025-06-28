package com.example.uni_cinema.ui.phim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.example.uni_cinema.R;
import com.example.uni_cinema.ui.rap.RapFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class MovieDetailsActivity extends AppCompatActivity {

    private TextView txtTieuDe, txtThoiLuong, txtTheLoai, txtThongKeTuoi, txtThongKeGioiTinh, txtMoTa;
    private ImageView imgPoster;
    private Button btn_order;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_details);

        // Initialize views
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtTieuDe = findViewById(R.id.txtTieuDe);
        txtThoiLuong = findViewById(R.id.txtThoiLuong);
        txtTheLoai = findViewById(R.id.txtTheLoai);
        txtThongKeTuoi = findViewById(R.id.txtThongKeTuoi);
        txtThongKeGioiTinh = findViewById(R.id.txtThongKeGioiTinh);
        txtMoTa = findViewById(R.id.txtMoTa);
        imgPoster = findViewById(R.id.imgPoster);

        db = FirebaseFirestore.getInstance();

        // Lấy id phim
        String movieId = getIntent().getStringExtra("movieId");
        if (movieId != null) {
            taiDuLieuPhim(movieId);
        }

        ImageButton btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> {
            finish();
        });

        btn_order.setOnClickListener(v -> {
            Intent intent = new Intent(MovieDetailsActivity.this, RapFragment.class);
            intent.putExtra("idMovie", movieId);
            startActivity(intent);
        });
    }

    private void taiDuLieuPhim(String movieId) {
        db.collection("movies").document(movieId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String tieuDe = documentSnapshot.getString("nameMovie");
                        String thoiLuong = "Thời lượng: " + documentSnapshot.getLong("timeMovie") + " Phút";
                        String idCategory = documentSnapshot.getString("idCategory");
                        String moTa = documentSnapshot.getString("descriptionMovie");
                        String posterUrl = documentSnapshot.getString("imageMovie1");
                        String trailerUrl = documentSnapshot.getString("trailer");

                        // Lấy thống kê tuổi và giới tính như trước
                        String tuoiStats = "> 10 tuổi: " + (documentSnapshot.getString("stat10") != null ? documentSnapshot.getString("stat10") : "0") + "%, " +
                                "> 20 tuổi: " + (documentSnapshot.getString("stat20") != null ? documentSnapshot.getString("stat20") : "0") + "%, " +
                                "> 30 tuổi: " + (documentSnapshot.getString("stat30") != null ? documentSnapshot.getString("stat30") : "0") + "%, " +
                                "> 40 tuổi: " + (documentSnapshot.getString("stat40") != null ? documentSnapshot.getString("stat40") : "0") + "%";
                        String gioiTinhStats = "Nam: " + (documentSnapshot.getString("statMale") != null ? documentSnapshot.getString("statMale") : "0") + "%, " +
                                "Nữ: " + (documentSnapshot.getString("statFemale") != null ? documentSnapshot.getString("statFemale") : "0") + "%";

                        // Truy vấn collection categories dựa trên idCategory
                        db.collection("categories").document(idCategory).get()
                                .addOnSuccessListener(categorySnapshot -> {
                                    String tenTheLoai = "Thể loại: ";
                                    if (categorySnapshot.exists()) {
                                        String nameCategory = categorySnapshot.getString("nameCategory");
                                        if (nameCategory != null) {
                                            tenTheLoai += nameCategory;
                                        } else {
                                            tenTheLoai += idCategory; // fallback nếu không có nameCategory
                                        }
                                    } else {
                                        tenTheLoai += idCategory; // fallback nếu không tìm thấy
                                    }

                                    // Set dữ liệu lên UI
                                    txtTieuDe.setText(tieuDe);
                                    txtThoiLuong.setText(thoiLuong);
                                    txtTheLoai.setText(tenTheLoai);
                                    txtThongKeTuoi.setText("Số tích theo tuổi\n" + tuoiStats);
                                    txtThongKeGioiTinh.setText("Số tích theo giới tính\n" + gioiTinhStats);
                                    txtMoTa.setText(moTa);

                                    Glide.with(this).load(posterUrl).into(imgPoster);

                                    YouTubePlayerView youTubePlayerView = findViewById(R.id.youtubePlayerView);
                                    getLifecycle().addObserver(youTubePlayerView);

                                    youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                                        @Override
                                        public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                                            youTubePlayer.loadVideo(trailerUrl, 0);
                                        }
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    // Nếu không lấy được thể loại thì vẫn set dữ liệu phim bình thường
                                    txtTheLoai.setText("Thể loại: " + idCategory);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    txtMoTa.setText("Không thể tải dữ liệu phim.");
                });
    }

}