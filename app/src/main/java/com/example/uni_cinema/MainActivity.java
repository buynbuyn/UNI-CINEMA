package com.example.uni_cinema;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.uni_cinema.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;
    private FirebaseUser user;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Cấu hình cho Navigation Drawer
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_phim, R.id.nav_qua_tang, R.id.nav_rap, R.id.nav_khuyen_mai
        ).setOpenableLayout(drawer)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Xử lý custom bottom menu
        setupCustomBottomMenu();

        // Ẩn/hiện menu dưới và toolbar tùy Fragment
        View customBottomMenu = findViewById(R.id.custom_bottom_menu);
        View toolbar = binding.appBarMain.toolbar;
        Set<Integer> fragmentsToHideMenu = new HashSet<>(Arrays.asList(
                R.id.nav_phim, R.id.nav_qua_tang, R.id.nav_rap, R.id.nav_khuyen_mai
        ));
        Set<Integer> fragmentsToHideToolbar = new HashSet<>(Arrays.asList(
                R.id.nav_phim, R.id.nav_qua_tang, R.id.nav_rap, R.id.nav_khuyen_mai
        ));

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            customBottomMenu.setVisibility(fragmentsToHideMenu.contains(destination.getId()) ? View.GONE : View.VISIBLE);
            toolbar.setVisibility(fragmentsToHideToolbar.contains(destination.getId()) ? View.GONE : View.VISIBLE);
        });

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Lấy các view từ header của NavigationView
        View headerView = navigationView.getHeaderView(0);
        FrameLayout frameLayout = headerView.findViewById(R.id.account);
        LinearLayout linearLayout = headerView.findViewById(R.id.account2);
        LinearLayout linearLayout1 = headerView.findViewById(R.id.account3);
        ImageView userAvatar = headerView.findViewById(R.id.userAvatar);
        TextView userName = headerView.findViewById(R.id.userName);
        TextView userLevel = headerView.findViewById(R.id.userLevel);
        TextView txt_coin = headerView.findViewById(R.id.txt_coin);
        TextView point = headerView.findViewById(R.id.point);
        ImageView qr = headerView.findViewById(R.id.qr);
        TextView cost = headerView.findViewById(R.id.cost);
        ProgressBar progress = headerView.findViewById(R.id.progress);
        Button buttonAuth = headerView.findViewById(R.id.buttonAuth);

        if (user != null) {
            // Người dùng đã đăng nhập
            // Hiển thị tất cả các view
            userAvatar.setVisibility(View.VISIBLE);
            userName.setVisibility(View.VISIBLE);
            userLevel.setVisibility(View.VISIBLE);
            txt_coin.setVisibility(View.VISIBLE);
            point.setVisibility(View.VISIBLE);
            qr.setVisibility(View.VISIBLE);
            cost.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
            buttonAuth.setVisibility(View.VISIBLE);
            buttonAuth.setText("Đăng xuất");
            buttonAuth.setOnClickListener(v -> {
                auth.signOut(); // Đăng xuất khỏi Firebase
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa stack Activity
                startActivity(intent);
                finish();
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String firstName = bundle.getString("firstName");
                String lastName = bundle.getString("lastName");
                if (firstName != null && lastName != null) {
                    userName.setText(firstName + " " + lastName);
                } else {
                    userName.setText(user.getDisplayName() != null ? user.getDisplayName() : user.getEmail());
                }
            } else {
                userName.setText(user.getDisplayName() != null ? user.getDisplayName() : user.getEmail());
            }
        } else {
            // Người dùng chưa đăng nhập
            // Chỉ hiển thị nút đăng nhập, ẩn các view khác
            frameLayout.setVisibility(View.GONE);
            linearLayout1.setVisibility(View.GONE);
            linearLayout.setVisibility(View.GONE);
            userAvatar.setVisibility(View.GONE);
            userName.setVisibility(View.GONE);
            userLevel.setVisibility(View.GONE);
            txt_coin.setVisibility(View.GONE);
            point.setVisibility(View.GONE);
            qr.setVisibility(View.GONE);
            cost.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            buttonAuth.setVisibility(View.VISIBLE);
            buttonAuth.setText("Đăng nhập");
            // Thiết lập marginTop 10dp cho buttonAuth khi chưa đăng nhập
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) buttonAuth.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            int marginTopInPx = (int) (50 * getResources().getDisplayMetrics().density); // Chuyển 10dp sang pixel
            params.setMargins(params.leftMargin, marginTopInPx, params.rightMargin, params.bottomMargin);
            buttonAuth.setLayoutParams(params);
            buttonAuth.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }
    }

    // Hiệu ứng chuyển trang mờ
    private final NavOptions fadeAnim = new NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build();

    private void setupCustomBottomMenu() {
        LinearLayout phim = findViewById(R.id.menu_phim);
        LinearLayout quaTang = findViewById(R.id.menu_qua_tang);
        LinearLayout rap = findViewById(R.id.menu_rap);
        LinearLayout khuyenMai = findViewById(R.id.menu_khuyen_mai);

        phim.setOnClickListener(v -> navController.navigate(R.id.nav_phim, null, fadeAnim));
        quaTang.setOnClickListener(v -> navController.navigate(R.id.nav_qua_tang, null, fadeAnim));
        rap.setOnClickListener(v -> navController.navigate(R.id.nav_rap, null, fadeAnim));
        khuyenMai.setOnClickListener(v -> navController.navigate(R.id.nav_khuyen_mai, null, fadeAnim));
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}