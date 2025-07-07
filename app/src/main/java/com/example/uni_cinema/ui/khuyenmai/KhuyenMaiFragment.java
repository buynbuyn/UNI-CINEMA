package com.example.uni_cinema.ui.khuyenmai;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.uni_cinema.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KhuyenMaiFragment extends Fragment {
    private RecyclerView recyclerPromotion;
    private PromotionAdapter adapter;
    private FirebaseFirestore db;

    public KhuyenMaiFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_khuyen_mai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerPromotion = view.findViewById(R.id.recyclerPromotion);
        recyclerPromotion.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PromotionAdapter(getContext(), new ArrayList<>());
        recyclerPromotion.setAdapter(adapter);
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_home));
        db = FirebaseFirestore.getInstance();
        loadPromotions();
    }

    private void loadPromotions() {
        db.collection("promotionNews")

                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Promotion> promotionList = new ArrayList<>();
                    Log.d("KHUYENMAI", "🔥 Tổng số documents: " + querySnapshot.size());
                    Date now = new Date();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Timestamp endDate = doc.getTimestamp("endDate");

                        // Bỏ nếu thiếu endDate hoặc đã hết hạn
                        if (endDate == null || endDate.toDate().before(now)) {
                            continue;
                        }
                        String id = doc.getId();
                        String title = doc.getString("title");
                        String description = doc.getString("description");
                        String bannerImage = doc.getString("bannerImage");
                        Timestamp startDate = doc.getTimestamp("startDate");

                        Log.d("KHUYENMAI", "📄 docId: " + id);

                        if (title != null && description != null && startDate != null && endDate != null && bannerImage != null) {
                            Promotion promo = new Promotion(id, title, description, startDate, endDate, bannerImage);
                            promotionList.add(promo);
                            Log.d("KHUYENMAI", "✅ Đã thêm: " + title);
                        } else {
                            Log.w("KHUYENMAI", "⚠️ Bỏ qua document thiếu dữ liệu: " + id);
                        }
                    }

                    Log.d("KHUYENMAI", "📦 Tổng khuyến mãi sau lọc: " + promotionList.size());
                    adapter.updateData(promotionList);
                })
                .addOnFailureListener(e -> {
                    Log.e("KHUYENMAI", "❌ Lỗi khi load Firestore: " + e.getMessage());
                });
    }

}
