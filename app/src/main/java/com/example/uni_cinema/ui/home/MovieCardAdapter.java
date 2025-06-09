package com.example.uni_cinema.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;


import com.example.uni_cinema.R;

import java.util.List;

public class MovieCardAdapter extends RecyclerView.Adapter<MovieCardAdapter.ViewHolder> {
    private List<Movie> movieList;

    public MovieCardAdapter(List<Movie> movieList) {
        this.movieList = movieList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        // Các TextView khác nếu có (ví dụ: rating, duration)
        // TextView rating;
        // TextView duration;

        public ViewHolder(View view) {
            super(view);
            poster = view.findViewById(R.id.posterImage);
            title = view.findViewById(R.id.titleText);
            // Ánh xạ các TextView khác nếu có
            // rating = view.findViewById(R.id.ratingText);
            // duration = view.findViewById(R.id.durationText);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Sử dụng toán tử modulo để lặp lại danh sách
        // Đảm bảo movieList không rỗng để tránh lỗi chia cho 0
        if (movieList == null || movieList.isEmpty()) {
            return;
        }
        int actualPosition = position % movieList.size();
        Movie movie = movieList.get(actualPosition);

        Glide.with(holder.poster.getContext())
                .load(movie.getImageUrl())
                .placeholder(R.drawable.loading_image) // ảnh tạm khi đang load
                .error(R.drawable.error_image)         // ảnh khi lỗi
                .into(holder.poster);

        holder.title.setText(movie.getTitle());
        // Set dữ liệu cho các TextView khác nếu có
        // holder.rating.setText(String.valueOf(movie.getRating()));
        // holder.duration.setText(movie.getDuration());
    }

    @Override
    public int getItemCount() {
        // Trả về một số lượng rất lớn để tạo hiệu ứng lặp vô tận
        // Integer.MAX_VALUE là một lựa chọn an toàn và hiệu quả
        return Integer.MAX_VALUE;
    }
}