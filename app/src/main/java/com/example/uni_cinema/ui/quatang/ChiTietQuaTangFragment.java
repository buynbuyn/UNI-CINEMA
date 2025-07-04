package com.example.uni_cinema.ui.quatang;
import com.example.uni_cinema.LoginActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.uni_cinema.R;
import com.google.android.material.tabs.TabLayout;

import java.text.NumberFormat;
import java.util.Locale;

import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.Toast;

public class ChiTietQuaTangFragment extends Fragment {

    public ChiTietQuaTangFragment() {}

    private final NavOptions fadeAnim = new NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chi_tiet_qua_tang, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        // Nút quay lại
        ImageButton btnBack = view.findViewById(R.id.btn_back_home);
        btnBack.setOnClickListener(v -> {
            navController.navigate(R.id.nav_qua_tang, null, fadeAnim);
        });

        // Hiển thị thông tin vé
        TextView tvType = view.findViewById(R.id.tv_ticket_type);
        TextView tvPrice = view.findViewById(R.id.tv_ticket_price);
        ImageView imgTicket = view.findViewById(R.id.img_ticket);

        // Khai báo để lưu dữ liệu vé
        String ticketType = "";
        int price = 0;
        int imageRes = R.drawable.ticket01;

        Bundle args = getArguments();
        if (args != null) {
            ticketType = args.getString("ticket_type");
            price = args.getInt("price");
            imageRes = args.getInt("image_res", R.drawable.ticket01);

            tvType.setText("Loại vé: " + ticketType);
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedPrice = formatter.format(price);
            tvPrice.setText("Giá: " + formattedPrice + " VND");
            imgTicket.setImageResource(imageRes);
        }

        // TabLayout điều khiển nội dung chi tiết / hướng dẫn
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        LinearLayout layoutDetail = view.findViewById(R.id.layout_detail);
        LinearLayout layoutInstruction = view.findViewById(R.id.layout_instruction);

        tabLayout.addTab(tabLayout.newTab().setText("Nội dung chi tiết"));
        tabLayout.addTab(tabLayout.newTab().setText("Hướng dẫn sử dụng"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutDetail.setVisibility(View.VISIBLE);
                    layoutInstruction.setVisibility(View.GONE);
                } else {
                    layoutDetail.setVisibility(View.GONE);
                    layoutInstruction.setVisibility(View.VISIBLE);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        layoutDetail.setVisibility(View.VISIBLE);
        layoutInstruction.setVisibility(View.GONE);

        // Nút chuyển sang màn hình thanh toán
        Button btnThanhToan = view.findViewById(R.id.btn_mua_ngay);
        if (btnThanhToan != null) {
            String finalTicketType = ticketType;
            int finalPrice = price;
            int finalImageRes = imageRes;

            btnThanhToan.setOnClickListener(v -> {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser == null) {
                    Toast.makeText(requireContext(), "Vui lòng đăng nhập trước khi mua quà tặng", Toast.LENGTH_SHORT).show();

                    // Mở LoginActivity
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return;
                }

                // Đã đăng nhập -> tiếp tục thanh toán
                Bundle bundle = new Bundle();
                bundle.putString("ticket_type", finalTicketType);
                bundle.putInt("price", finalPrice);
                bundle.putInt("image_res", finalImageRes);
                navController.navigate(R.id.action_bankQuaTangFragment_to_thanhToanFragment, bundle, fadeAnim);
            });
        }
    }
}
