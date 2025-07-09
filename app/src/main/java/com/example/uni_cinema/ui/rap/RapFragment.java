package com.example.uni_cinema.ui.rap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.navigation.NavOptions;
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
    private List<Region> theaterList = new ArrayList<>();
    private FirebaseFirestore db;
    private AppCompatButton btnContinue;
    private String selectedTheaterName = null;
    private String selectedTheaterId = null;
    private String selectedAddressTheater = null;
    private String movieTitle;
    private String movieId;



    private final NavOptions fadeAnim = new NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build();

    public RapFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rap, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            movieId = getArguments().getString("movieId");
            movieTitle = getArguments().getString("movieTitle");
        }
        db = FirebaseFirestore.getInstance();

        btnContinue = view.findViewById(R.id.btnContinue);
        btnContinue.setVisibility(View.GONE); // Ẩn nút từ đầu

        btnContinue.setOnClickListener(v -> {
            if (selectedTheaterName != null) {
                Bundle bundle = new Bundle();
                bundle.putString("theaterName", selectedTheaterName);
                bundle.putString("addressTheater", selectedAddressTheater);
                bundle.putString("theaterId", selectedTheaterId);

                if (movieId != null) bundle.putString("movieId", movieId);
                if (movieTitle != null) bundle.putString("movieTitle", movieTitle);


                Navigation.findNavController(v).navigate(R.id.nav_suatchieu, bundle, fadeAnim);
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn rạp trước khi tiếp tục", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton btnBack = view.findViewById(R.id.btn_back_home);
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_home, null, fadeAnim);
        });

        // Vùng (bên trái)
        recyclerView = view.findViewById(R.id.recyclerView_region);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        regionAdapter = new RegionAdapter(regionList, getContext(), selectedProvince -> {
            loadTheatersByProvince(selectedProvince);
        });
        recyclerView.setAdapter(regionAdapter);

        // Rạp (bên phải)
        recyclerViewTheater = view.findViewById(R.id.recyclerView_theater);
        recyclerViewTheater.setLayoutManager(new LinearLayoutManager(getContext()));

        // KHÔNG gán adapter ở đây — sẽ gán sau khi có dữ liệu
        fetchRegionsFromFirestore();
    }

    private void fetchRegionsFromFirestore() {
        db.collection("theaters")
                .get()
                .addOnSuccessListener(snapshot -> {
                    regionList.clear();
                    Set<String> addedProvinces = new HashSet<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String province = doc.getString("nameProvince");
                        if (province != null && !addedProvinces.contains(province)) {
                            regionList.add(new Region(province));
                            addedProvinces.add(province);
                        }
                    }

                    regionAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Không tải được danh sách vùng", Toast.LENGTH_SHORT).show());
    }

    private void loadTheatersByProvince(String provinceName) {
        db.collection("theaters")
                .whereEqualTo("nameProvince", provinceName)
                .get()
                .addOnSuccessListener(snapshot -> {
                    theaterList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String name = doc.getString("nameTheater");
                        String address = doc.getString("addressTheater");
                        String id = doc.getId(); // lấy ID Firestore của document
                        if (name != null) {
                            theaterList.add(new Region(provinceName, name, id, address));
                        }
                    }

                    // Khởi tạo adapter mới mỗi lần load dữ liệu
                    theaterAdapter = new TheaterAdapter(theaterList, selectedRegion -> {
                        selectedTheaterName = selectedRegion.getNameTheater();
                        selectedTheaterId = selectedRegion.getTheaterId();
                        selectedAddressTheater = selectedRegion.getAddressTheater();
                        btnContinue.setVisibility(View.VISIBLE);
                    });
                    recyclerViewTheater.setAdapter(theaterAdapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Không tải được rạp cho " + provinceName, Toast.LENGTH_SHORT).show());
    }

}
