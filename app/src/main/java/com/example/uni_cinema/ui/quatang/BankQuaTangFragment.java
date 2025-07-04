package com.example.uni_cinema.ui.quatang;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class BankQuaTangFragment extends Fragment {

    public BankQuaTangFragment() {
        // Required empty public constructor
    }
    private final NavOptions fadeAnim = new NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bank_qua_tang, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        // Nút quay lại
        ImageButton btnBack = view.findViewById(R.id.btn_back_home);
        btnBack.setOnClickListener(v -> {
            navController.navigate(R.id.nav_qua_tang,null, fadeAnim);
        });

        // Hiển thị thông tin vé
        TextView tvType = view.findViewById(R.id.tv_ticket_type);
        TextView tvPrice = view.findViewById(R.id.tv_ticket_price);
        ImageView imgTicket = view.findViewById(R.id.img_ticket);

        Bundle args = getArguments();
        if (args != null) {
            String ticketType = args.getString("ticket_type");
            int price = args.getInt("price");
            int imageRes = args.getInt("image_res", R.drawable.ticket01); // fallback ảnh

            tvType.setText("Loại vé: " + ticketType);
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedPrice = formatter.format(price);
            tvPrice.setText("Giá: " + formattedPrice + " VND");
            imgTicket.setImageResource(imageRes);

            // Ánh xạ TabLayout
            TabLayout tabLayout = view.findViewById(R.id.tab_layout);
            LinearLayout layoutDetail = view.findViewById(R.id.layout_detail);
            LinearLayout layoutInstruction = view.findViewById(R.id.layout_instruction);

            // Thêm tab thủ công
            tabLayout.addTab(tabLayout.newTab().setText("Nội dung chi tiết"));
            tabLayout.addTab(tabLayout.newTab().setText("Hướng dẫn sử dụng"));
            // Lắng nghe khi chọn tab
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

            // Mặc định chọn tab 0
            layoutDetail.setVisibility(View.VISIBLE);
            layoutInstruction.setVisibility(View.GONE);
        }
    }
}
