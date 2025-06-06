package com.example.uni_cinema.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.uni_cinema.R;
import com.example.uni_cinema.databinding.FragmentHomeBinding;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Handler handler = new Handler(Looper.getMainLooper());
    private CenterItemAnimator centerItemAnimator;
    private RecyclerView carousel;
    private LinearLayoutManager layoutManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Khởi tạo slider (Promotion Slider)
        ViewPager2 slider = binding.promotionSlider;
        List<Integer> images = Arrays.asList(
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3,
                R.drawable.banner4
        );
        PromotionSliderAdapter adapter = new PromotionSliderAdapter(images);
        slider.setAdapter(adapter);

        // Tự động chuyển slide cho Promotion Slider
        Runnable sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int current = slider.getCurrentItem();
                int next = (current + 1) % images.size();
                slider.setCurrentItem(next, true);
                handler.postDelayed(this, 3000); // 3 giây
            }
        };
        handler.post(sliderRunnable);

        // Khởi tạo carousel phim mới (Movie Carousel)
        carousel = binding.cardCarousel;
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        carousel.setLayoutManager(layoutManager);

        List<Movie> movies = Arrays.asList(
                new Movie("SOLO Jennie", R.drawable.poster1),
                new Movie("KILL THIS LOVE", R.drawable.poster2),
                new Movie("SHUT DOWN", R.drawable.poster3),
                new Movie("ICE CREAM", R.drawable.poster4)
        );

        MovieCardAdapter movieAdapter = new MovieCardAdapter(movies);
        carousel.setAdapter(movieAdapter);

        // Cuộn đến một vị trí trung tâm để tạo cảm giác lặp vô tận ngay từ đầu
        if (!movies.isEmpty()) {
            int startPosition = Integer.MAX_VALUE / 2;
            startPosition = startPosition - (startPosition % movies.size());
            carousel.scrollToPosition(startPosition);
        }

        // Gắn PagerSnapHelper để đảm bảo chỉ cuộn từng trang
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(carousel);

        // Gắn CenterItemAnimator vào RecyclerView
        centerItemAnimator = new CenterItemAnimator(layoutManager);
        carousel.addOnScrollListener(centerItemAnimator);

        // *** ĐOẠN CODE ĐỂ ÁP DỤNG HIỆU ỨNG NGAY LẬP TỨC SAU KHI LAYOUT HOÀN TẤT ***
        // Sử dụng ViewTreeObserver và postDelayed để đảm bảo hiệu ứng được áp dụng đúng lúc
        carousel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Đảm bảo chỉ chạy một lần
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    carousel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    carousel.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                // Gọi phương thức để áp dụng hiệu ứng sau một khoảng delay ngắn
                // Điều này giúp đảm bảo RecyclerView đã hoàn thành việc vẽ các view con
                carousel.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        centerItemAnimator.scaleAlphaAndRotateViewsSubtly(carousel);
                    }
                }, 100); // Delay 100ms, bạn có thể điều chỉnh nếu cần thiết
            }
        });
        // ***********************************************************************************

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