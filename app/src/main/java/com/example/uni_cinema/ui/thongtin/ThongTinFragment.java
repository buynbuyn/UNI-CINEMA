package com.example.uni_cinema.ui.thongtin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.uni_cinema.MainActivity;
import com.example.uni_cinema.R;
import com.google.firebase.auth.FirebaseAuth;

public class ThongTinFragment extends Fragment {

    private final NavOptions fadeAnim = new NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thong_tin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        // Nút quay lại
        ImageButton btnBack = view.findViewById(R.id.btn_back_home);
        btnBack.setOnClickListener(v -> {
            navController.navigate(R.id.nav_home, null, fadeAnim);
        });

        // Nút "Thông tin cá nhân"
        LinearLayout layoutInfo = view.findViewById(R.id.layout_info);
        layoutInfo.setOnClickListener(v -> {
            navController.navigate(R.id.nav_chinhsua, null, fadeAnim);
        });
        // Nút "Đổi mât khẩu"
        LinearLayout layoutChangePW = view.findViewById(R.id.layout_change_password);
        layoutChangePW.setOnClickListener(v -> {
            navController.navigate(R.id.nav_change_password, null, fadeAnim);
        });
        LinearLayout layoutLogout = view.findViewById(R.id.logoutLayout);
        layoutLogout.setOnClickListener(v -> {
            // 1. Đăng xuất người dùng khỏi Firebase
            FirebaseAuth.getInstance().signOut();

            // 2. Chuyển về trang chủ (MainActivity) dưới vai trò khách
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear stack
            startActivity(intent);
        });
    }
}
