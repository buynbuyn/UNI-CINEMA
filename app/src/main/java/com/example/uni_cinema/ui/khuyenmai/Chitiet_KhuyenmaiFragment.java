package com.example.uni_cinema.ui.khuyenmai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.navigation.fragment.NavHostFragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.uni_cinema.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class Chitiet_KhuyenmaiFragment extends Fragment {
    private static final String ARG_PROMOTION_ID = "promotion_id";
    private String promotionId;

    private TextView tvTitle, tvDescription;
    private ImageView imgBanner;
    private final NavOptions fadeAnim = new NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build();
    public static Chitiet_KhuyenmaiFragment newInstance(String id) {
        Chitiet_KhuyenmaiFragment fragment = new Chitiet_KhuyenmaiFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROMOTION_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            promotionId = getArguments().getString(ARG_PROMOTION_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chitiet_khuyenmai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvTitle = view.findViewById(R.id.tvPromoDetailTitle);
        tvDescription = view.findViewById(R.id.tvPromoDetailDesc);
        imgBanner = view.findViewById(R.id.imgPromoDetailBanner);

        loadData();
        ImageView btnBack = view.findViewById(R.id.btn_back_home);
        btnBack.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
        });
        requireActivity().findViewById(R.id.custom_bottom_menu).setVisibility(View.GONE);
        requireActivity().findViewById(R.id.toolbar).setVisibility(View.GONE); // 👈 nếu có
    }

    private void loadData() {
        FirebaseFirestore.getInstance()
                .collection("promotionNews")
                .document(promotionId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        tvTitle.setText(snapshot.getString("title"));
                        tvDescription.setText(snapshot.getString("description"));

                        Glide.with(requireContext())
                                .load(snapshot.getString("bannerImage"))
                                .placeholder(R.drawable.loading_image)
                                .error(R.drawable.error_image)
                                .into(imgBanner);
                    }
                });
    }

}
