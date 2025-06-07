package com.example.uni_cinema;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.uni_cinema.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;

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

        // 👉 Ẩn/hiện menu dưới tùy Fragment
        View customBottomMenu = findViewById(R.id.custom_bottom_menu);
        // Danh sách các Fragment ẩn menu dưới
        Set<Integer> fragmentsToHideMenu = new HashSet<>(Arrays.asList(
                R.id.nav_phim,
                R.id.nav_khuyen_mai,
                R.id.nav_rap,
                R.id.nav_qua_tang

        ));
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (fragmentsToHideMenu.contains(destination.getId())) {
                customBottomMenu.setVisibility(View.GONE);
            } else {
                customBottomMenu.setVisibility(View.VISIBLE);
            }
        });

// Ẩn/hiện toolbar tùy Fragment
        View toolbar = binding.appBarMain.toolbar;

// Các fragment KHÔNG cần hiển thị toolbar (ví dụ: chi tiết phim)
        Set<Integer> fragmentsToHideToolbar = new HashSet<>(Arrays.asList(
                R.id.nav_phim,
                R.id.nav_khuyen_mai,
                R.id.nav_rap,
                R.id.nav_qua_tang
        ));

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Xử lý ẩn menu dưới cùng
            if (fragmentsToHideMenu.contains(destination.getId())) {
                customBottomMenu.setVisibility(View.GONE);
            } else {
                customBottomMenu.setVisibility(View.VISIBLE);
            }

            // Xử lý ẩn toolbar
            if (fragmentsToHideToolbar.contains(destination.getId())) {
                toolbar.setVisibility(View.GONE);
            } else {
                toolbar.setVisibility(View.VISIBLE);
            }
        });



    }
    // hiệu ứng chuyển trang mờ
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
