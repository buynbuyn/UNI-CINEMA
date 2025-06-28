package com.example.uni_cinema.ui.phim;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uni_cinema.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movies;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Map<String, String> categoryMap = new HashMap<>();

    public MovieAdapter(List<Movie> movies) {
        this.movies = movies;
        loadCategories();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtTitle, txtDuration, txtDate, txtGenre;
        Button btnBuyTicket;
        private List<Movie> movieList;
        public MovieViewHolder(View itemView, List<Movie> movies) {
            super(itemView);
            this.movieList = movies;
            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDuration = itemView.findViewById(R.id.txtDuration);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtGenre = itemView.findViewById(R.id.txtGenre);
            btnBuyTicket = itemView.findViewById(R.id.btnBuyTicket);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && movieList != null && position < movieList.size()) {
                    Intent intent = new Intent(itemView.getContext(), MovieDetailsActivity.class);
                    intent.putExtra("movieId", movieList.get(position).getId());
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view, movies); // Truyền movies vào constructor
    }

    private void loadCategories() {
        db.collection("categories").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();
                        String name = doc.getString("nameCategory");
                        if (name != null) {
                            categoryMap.put(id, name);
                        }
                    }
                    notifyDataSetChanged(); // cập nhật lại adapter để hiển thị thể loại đúng
                })
                .addOnFailureListener(e -> {
                    // xử lý lỗi tải categories nếu cần
                });
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        if (movies != null && position < movies.size()) {
            Movie movie = movies.get(position);
            holder.txtTitle.setText(movie.getTitle());
            holder.txtDuration.setText("Thời lượng: " + movie.getTimeMovie() + " Phút");
            holder.txtDate.setText("Khởi chiếu: " + movie.getReleaseDate());
            String idCategory = movie.getGenre();
            String tenTheLoai = categoryMap.getOrDefault(idCategory, idCategory);
            holder.txtGenre.setText("Thể loại: " + tenTheLoai);

            // Load ảnh bằng Glide
            Glide.with(holder.itemView.getContext())
                    .load(movie.getImageUrl())
                    .placeholder(R.drawable.error_image)
                    .into(holder.imgPoster);
        }
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }
}