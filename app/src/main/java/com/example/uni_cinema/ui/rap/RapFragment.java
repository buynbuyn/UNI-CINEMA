package com.example.uni_cinema.ui.rap;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uni_cinema.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RapFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView recyclerViewTheater;
    private RegionAdapter regionAdapter;
    private TheaterAdapter theaterAdapter;
    private List<Region> regionList = new ArrayList<>();
    private List<String> theaterList = new ArrayList<>();

    public RapFragment() {}

    private final NavOptions fadeAnim = new NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rap, container, false);

        ImageButton btnBack = rootView.findViewById(R.id.btn_back_home);
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_home, null, fadeAnim);
        });

        // Danh sách vùng (trái)
        recyclerView = rootView.findViewById(R.id.recyclerView_region);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        regionAdapter = new RegionAdapter(regionList, getContext(), selectedProvince -> {
            loadTheatersByProvince(selectedProvince); // 👉 lọc rạp theo tỉnh đã chọn
        });
        recyclerView.setAdapter(regionAdapter);

        // Danh sách rạp (phải)
        recyclerViewTheater = rootView.findViewById(R.id.recyclerView_theater);
        recyclerViewTheater.setLayoutManager(new LinearLayoutManager(getContext()));
        theaterAdapter = new TheaterAdapter(theaterList);
        recyclerViewTheater.setAdapter(theaterAdapter);

        fetchRegionsFromFirestore(); // 👉 chỉ load danh sách vùng ban đầu

        return rootView;
    }

    private void fetchRegionsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("theaters")
                .get()
                .addOnSuccessListener(snapshot -> {
                    regionList.clear();
                    Set<String> addedProvinces = new HashSet<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String province = doc.getString("nameProvince");
                        if (province != null && !addedProvinces.contains(province)) {
                            regionList.add(new Region(province, null));
                            addedProvinces.add(province);
                        }
                    }

                    regionAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Không tải được danh sách vùng", Toast.LENGTH_SHORT).show());
    }

    private void loadTheatersByProvince(String provinceName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("theaters")
                .whereEqualTo("nameProvince", provinceName)
                .get()
                .addOnSuccessListener(snapshot -> {
                    theaterList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String name = doc.getString("nameTheater");
                        if (name != null) {
                            theaterList.add(name);
                        }
                    }
                    theaterAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Không tải được rạp cho " + provinceName, Toast.LENGTH_SHORT).show());
    }
}