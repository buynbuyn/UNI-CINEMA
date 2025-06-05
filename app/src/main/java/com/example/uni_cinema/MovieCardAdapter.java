package com.example.uni_cinema;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class MovieCardAdapter extends RecyclerView.Adapter<MovieCardAdapter.ViewHolder> {
    private List<Movie> movieList;

    public MovieCardAdapter(List<Movie> movieList) {
        this.movieList = movieList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;

        public ViewHolder(View view) {
            super(view);
            poster = view.findViewById(R.id.posterImage);
            title = view.findViewById(R.id.titleText);
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
        Movie movie = movieList.get(position);
        holder.poster.setImageResource(movie.getImageRes());
        holder.title.setText(movie.getTitle());
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }
}

