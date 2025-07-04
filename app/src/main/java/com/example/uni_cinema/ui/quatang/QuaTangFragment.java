package com.example.uni_cinema.ui.quatang;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.uni_cinema.R;

public class QuaTangFragment extends Fragment {

    public QuaTangFragment() {}

    private final NavOptions fadeAnim = new NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qua_tang, container, false);
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

        // Nút mua vé 2D
        view.findViewById(R.id.btn_buy_ticket_2d).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("ticket_type", "Vé quà tặng 2D");
            bundle.putInt("price", 120000);
            bundle.putInt("image_res", R.drawable.ticket01);
            navController.navigate(R.id.action_quaTangFragment_to_bankQuaTangFragment, bundle, fadeAnim);
        });

        // Nút mua vé 3D
        view.findViewById(R.id.btn_buy_ticket_3d).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("ticket_type", "Vé quà tặng 3D");
            bundle.putInt("price", 220000);
            bundle.putInt("image_res", R.drawable.ticket02);
            navController.navigate(R.id.action_quaTangFragment_to_bankQuaTangFragment, bundle, fadeAnim);
        });
    }
}
