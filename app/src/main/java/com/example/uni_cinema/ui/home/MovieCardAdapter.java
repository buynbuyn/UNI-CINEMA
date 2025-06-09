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
        TextView duration;
        // TextView rating;

        public ViewHolder(View view) {
            super(view);
            poster = view.findViewById(R.id.posterImage);
            title = view.findViewById(R.id.titleText);
            duration = view.findViewById(R.id.durationText);
            // rating = view.findViewById(R.id.ratingText);
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
        if (movieList == null || movieList.isEmpty()) {
            return;
        }
        int actualPosition = position % movieList.size();
        Movie movie = movieList.get(actualPosition);

        Glide.with(holder.poster.getContext())
                .load(movie.getImageUrl())
                .placeholder(R.drawable.loading_image)
                .error(R.drawable.error_image)
                .into(holder.poster);

        holder.title.setText(movie.getTitle());
        holder.duration.setText(movie.getTimeMovie() + " phút");
        // holder.rating.setText(String.valueOf(movie.getRating()));
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }
}