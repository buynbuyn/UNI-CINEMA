package com.example.uni_cinema.ui.lichsu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uni_cinema.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WatchedMovieAdapter extends RecyclerView.Adapter<WatchedMovieAdapter.ViewHolder> {
    private final List<WatchedMovie> watchedMovies;

    public WatchedMovieAdapter(List<WatchedMovie> watchedMovies) {
        this.watchedMovies = watchedMovies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_phim_da_xem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WatchedMovie movie = watchedMovies.get(position);
        holder.tvScreenRoom.setText("Phòng chiếu: " + movie.getScreenRoomName());
        holder.tvOrderId.setText("Mã vé: " + movie.getOrderId());
        holder.tvPrice.setText("Tổng tiền: " + movie.getTotalPrice() + " VND");
        holder.tvDate.setText("Ngày đặt: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(movie.getDateTimeOrder()));
        holder.tvPaymentStatus.setText("Trạng thái thanh toán: " + movie.getPaymentStatus());
        holder.tvTransactionId.setText("Mã giao dịch: " + movie.getTransactionId());
    }

    @Override
    public int getItemCount() {
        return watchedMovies.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvScreenRoom, tvPrice, tvDate, tvOrderId, tvPaymentStatus, tvTransactionId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvScreenRoom = itemView.findViewById(R.id.tv_screen_room);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvPaymentStatus = itemView.findViewById(R.id.tv_payment_status);
            tvTransactionId = itemView.findViewById(R.id.tv_transaction_id);
        }
    }
}
