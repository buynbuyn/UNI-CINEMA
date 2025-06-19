package com.example.uni_cinema.ui.phim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.bumptech.glide.Glide;
import com.example.uni_cinema.R;
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movies;

    public MovieAdapter(List<Movie> movies) {
        this.movies = movies;
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtTitle, txtDuration, txtDate, txtGenre;
        Button btnBuyTicket;

        public MovieViewHolder(View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDuration = itemView.findViewById(R.id.txtDuration);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtGenre = itemView.findViewById(R.id.txtGenre);
            btnBuyTicket = itemView.findViewById(R.id.btnBuyTicket);
        }
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.txtTitle.setText(movie.getTitle());
        holder.txtDuration.setText("Thời lượng: " + movie.getTimeMovie() + " Phút");
        holder.txtDate.setText("Khởi chiếu: " + movie.getReleaseDate());
        holder.txtGenre.setText("Thể loại: " + movie.getGenre());

        // Load ảnh bằng Glide
        Glide.with(holder.itemView.getContext())
                .load(movie.getImageUrl())
                .placeholder(R.drawable.error_image)
                .into(holder.imgPoster);
    }


    @Override
    public int getItemCount() {
        return movies.size();
    }
}

