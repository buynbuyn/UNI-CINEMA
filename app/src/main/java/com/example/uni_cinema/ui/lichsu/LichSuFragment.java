package com.example.uni_cinema.ui.lichsu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LichSuFragment extends Fragment {

    private final NavOptions fadeAnim = new NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lich_su, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        ImageButton btnBack = view.findViewById(R.id.btn_back_home);
        btnBack.setOnClickListener(v -> {
            navController.navigate(R.id.nav_home, null, fadeAnim);
        });

        RecyclerView recyclerView = view.findViewById(R.id.rcv_watched_movies);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<WatchedMovie> watchedMovies = new ArrayList<>();
        WatchedMovieAdapter adapter = new WatchedMovieAdapter(watchedMovies);
        recyclerView.setAdapter(adapter);

        // Lấy UID của người dùng đang đăng nhập
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("orders")
                .whereEqualTo("idUser", uid)
                .whereEqualTo("stateOrder", "Thanh toán thành công!")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String room = doc.getString("screenRoomName");
                        long price = doc.getLong("totalPrice");
                        Date date = doc.getDate("dateTimeOrder");

                        watchedMovies.add(new WatchedMovie(room, price, date));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
