package com.example.uni_cinema.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.uni_cinema.Movie;
import com.example.uni_cinema.MovieCardAdapter;
import com.example.uni_cinema.PromotionSliderAdapter;
import com.example.uni_cinema.R;
import com.example.uni_cinema.databinding.FragmentHomeBinding;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Handler handler = new Handler();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Khởi tạo slider
        ViewPager2 slider = binding.promotionSlider;
        List<Integer> images = Arrays.asList(
                R.drawable.poster1,
                R.drawable.poster2
        );
        PromotionSliderAdapter adapter = new PromotionSliderAdapter(images);
        slider.setAdapter(adapter);

        // Tự động chuyển slide
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int current = slider.getCurrentItem();
                int next = (current + 1) % images.size();
                slider.setCurrentItem(next, true);
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(runnable);

        // Khởi tạo carousel phim mới
        RecyclerView carousel = binding.cardCarousel; // View đã binding từ ID
        carousel.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        List<Movie> movies = Arrays.asList(
                new Movie("SOLO \nJennie", R.drawable.poster1),
                new Movie("KILL THIS LOVE", R.drawable.poster2),
                new Movie("SHUT DOWN", R.drawable.poster3),
                new Movie("ICE CREAM", R.drawable.poster4)
        );
        MovieCardAdapter movieAdapter = new MovieCardAdapter(movies);
        carousel.setAdapter(movieAdapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
