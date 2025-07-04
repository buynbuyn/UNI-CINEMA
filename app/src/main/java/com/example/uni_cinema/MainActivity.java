package com.example.uni_cinema;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.firestore.FirebaseFirestore;

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

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_lichsu, R.id.nav_khuyen_mai,
                R.id.nav_phim, R.id.nav_qua_tang, R.id.nav_rap,
                R.id.nav_thongtin, R.id.nav_chinh_sach, R.id.nav_chinhsua
        ).setOpenableLayout(drawer).build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        setupCustomBottomMenu();

        View customBottomMenu = findViewById(R.id.custom_bottom_menu);
        View toolbar = binding.appBarMain.toolbar;

        Set<Integer> fragmentsToHideMenu = new HashSet<>(Arrays.asList(
                R.id.nav_phim, R.id.nav_qua_tang, R.id.nav_rap,
                R.id.nav_khuyen_mai, R.id.bankQuaTangFragment,
                R.id.bankThanhToanFragment, R.id.nav_thongtin,
                R.id.nav_lichsu, R.id.nav_chinh_sach, R.id.nav_chinhsua
        ));
        Set<Integer> fragmentsToHideToolbar = new HashSet<>(fragmentsToHideMenu);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            customBottomMenu.setVisibility(fragmentsToHideMenu.contains(destination.getId()) ? View.GONE : View.VISIBLE);
            toolbar.setVisibility(fragmentsToHideToolbar.contains(destination.getId()) ? View.GONE : View.VISIBLE);
        });

        // Firebase Auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Header views
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

        if (user != null) {
            // Người dùng đã đăng nhập
            userAvatar.setVisibility(View.VISIBLE);
            userName.setVisibility(View.VISIBLE);
            userLevel.setVisibility(View.VISIBLE);
            txt_coin.setVisibility(View.VISIBLE);
            point.setVisibility(View.VISIBLE);
            qr.setVisibility(View.VISIBLE);
            cost.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("full_name");
                            if (fullName != null && !fullName.isEmpty()) {
                                userName.setText(fullName);
                            } else {
                                userName.setText(user.getEmail());
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        userName.setText(user.getEmail());
                    });
        } else {
            // Người dùng chưa đăng nhập
            frameLayout.setVisibility(View.VISIBLE);
            linearLayout1.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
            userAvatar.setVisibility(View.VISIBLE);
            userName.setVisibility(View.VISIBLE);
            userLevel.setVisibility(View.VISIBLE);
            txt_coin.setVisibility(View.VISIBLE);
            point.setVisibility(View.VISIBLE);
            qr.setVisibility(View.VISIBLE);
            cost.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);

            userName.setText("Khách");
        }
    }

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
