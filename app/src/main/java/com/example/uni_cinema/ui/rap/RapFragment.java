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
    private List<String> theaterList = new ArrayList<>();
    private FirebaseFirestore db;
    private AppCompatButton btnContinue;
    private String selectedTheaterName = null;

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
        String filmId = getArguments() != null ? getArguments().getString("filmId") : null;

        if (filmId != null) {
            // Lọc danh sách rạp chiếu phim này hoặc chỉ hiển thị
        }
        db = FirebaseFirestore.getInstance();

        btnContinue = view.findViewById(R.id.btnContinue);
        btnContinue.setVisibility(View.GONE); // Ẩn nút từ đầu
        btnContinue.setOnClickListener(v -> {
            if (selectedTheaterName != null) {
                Toast.makeText(getContext(), "Rạp đã chọn: " + selectedTheaterName, Toast.LENGTH_SHORT).show();
                // TODO: Chuyển màn ở đây nếu cần
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

                    // Khởi tạo adapter mới mỗi lần load dữ liệu
                    theaterAdapter = new TheaterAdapter(theaterList, nameTheater -> {
                        selectedTheaterName = nameTheater;
                        btnContinue.setVisibility(View.VISIBLE);
                    });
                    recyclerViewTheater.setAdapter(theaterAdapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Không tải được rạp cho " + provinceName, Toast.LENGTH_SHORT).show());
    }

}
