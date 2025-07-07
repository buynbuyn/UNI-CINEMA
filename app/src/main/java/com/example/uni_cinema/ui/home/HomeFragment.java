package com.example.uni_cinema.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.uni_cinema.R;
import com.example.uni_cinema.databinding.FragmentHomeBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Handler handler = new Handler(Looper.getMainLooper());
    private CenterItemAnimator centerItemAnimator;
    private RecyclerView carousel;
    private LinearLayoutManager layoutManager;
    private MovieCardAdapter movieAdapter;
    private List<Movie> movieList = new ArrayList<>();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup Promotion Slider
        ViewPager2 slider = binding.promotionSlider;
        List<Integer> images = Arrays.asList(
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3,
                R.drawable.banner4
        );
        PromotionSliderAdapter adapter = new PromotionSliderAdapter(images);
        slider.setAdapter(adapter);

        Runnable sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int current = slider.getCurrentItem();
                int next = (current + 1) % images.size();
                slider.setCurrentItem(next, true);
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(sliderRunnable);

        // Setup Movie Carousel
        carousel = binding.cardCarousel;
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        carousel.setLayoutManager(layoutManager);

        movieAdapter = new MovieCardAdapter(movieList);
        carousel.setAdapter(movieAdapter);

        // Fetch movies from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("movies")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Timestamp tsEnd = doc.getTimestamp("dateTimeEnd");
                        if (tsEnd == null || tsEnd.toDate().before(new Date())) {
                            continue;
                        }
                        String id = doc.getId();
                        String title = doc.getString("nameMovie");
                        String imageUrl = doc.getString("imageMovie1");
                        Long timeMovieLong = doc.getLong("timeMovie");

                        if (title != null && imageUrl != null && timeMovieLong != null) {
                            int timeMovie = timeMovieLong.intValue();
                            Movie movie = new Movie(id, title, imageUrl);
                            movie.setTimeMovie(timeMovie);
                            movieList.add(movie);
                        }
                    }
                    movieAdapter.notifyDataSetChanged();

                    // Scroll to center
                    if (!movieList.isEmpty()) {
                        int startPosition = Integer.MAX_VALUE / 2;
                        startPosition = startPosition - (startPosition % movieList.size());
                        carousel.scrollToPosition(startPosition);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Không tải được danh sách phim", Toast.LENGTH_SHORT).show()
                );

        // Snap and animation
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(carousel);

        centerItemAnimator = new CenterItemAnimator(layoutManager);
        carousel.addOnScrollListener(centerItemAnimator);

        carousel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    carousel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    carousel.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                carousel.postDelayed(() ->
                        centerItemAnimator.scaleAlphaAndRotateViewsSubtly(carousel), 100);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        if (binding != null) {
            if (binding.cardCarousel != null && centerItemAnimator != null) {
                binding.cardCarousel.removeOnScrollListener(centerItemAnimator);
            }
            binding = null;
        }
    }
}
