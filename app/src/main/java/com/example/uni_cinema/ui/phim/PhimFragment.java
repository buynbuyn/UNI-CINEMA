package com.example.uni_cinema.ui.phim;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhimFragment extends Fragment {

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movie> movieList = new ArrayList<>();

    public PhimFragment() {}

    private final NavOptions fadeAnim = new NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_phim, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btn_back_home);
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_home, null, fadeAnim);
        });

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MovieAdapter(movieList);
        recyclerView.setAdapter(adapter);

        // Load movie list from Firebase
        fetchMoviesFromFirebase();
    }

    private void fetchMoviesFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Bước 1: Lấy danh sách thể loại
        db.collection("categories")
                .get()
                .addOnSuccessListener(categorySnapshot -> {
                    // Tạo map để tra tên thể loại theo ID
                    Map<String, String> categoryMap = new HashMap<>();
                    for (DocumentSnapshot doc : categorySnapshot.getDocuments()) {
                        categoryMap.put(doc.getId(), doc.getString("nameCategory")); // "name" là tên thể loại
                    }

                    // Bước 2: Lấy danh sách phim
                    db.collection("movies")
                            .get()
                            .addOnSuccessListener(movieSnapshot -> {
                                movieList.clear();
                                for (DocumentSnapshot doc : movieSnapshot.getDocuments()) {
                                    String title = doc.getString("nameMovie");
                                    String imageUrl = doc.getString("imageMovie1");
                                    Long timeMovieLong = doc.getLong("timeMovie");
                                    String idCategory = doc.getString("idCategory");

                                    if (title != null && imageUrl != null && timeMovieLong != null) {
                                        int timeMovie = timeMovieLong.intValue();
                                        String nameCategory = categoryMap.getOrDefault(idCategory, "Không rõ");

                                        Movie movie = new Movie(title, imageUrl);
                                        movie.setTimeMovie(timeMovie);
                                        movie.setGenre(nameCategory); // ← Gán tên thể loại
                                        movieList.add(movie);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Không tải được danh sách phim", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Không tải được danh sách thể loại", Toast.LENGTH_SHORT).show()
                );
    }
}
