package com.example.uni_cinema.ui.rap;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uni_cinema.R;

import java.util.List;

public class TheaterAdapter extends RecyclerView.Adapter<TheaterAdapter.TheaterViewHolder> {
    private List<String> theaterList;
    private int selectedPosition = -1;
    private OnTheaterClickListener listener;
    public interface OnTheaterClickListener {
        void onTheaterClick(String theaterName);
    }
    public TheaterAdapter(List<String> theaterList, OnTheaterClickListener listener) {
        this.theaterList = theaterList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TheaterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theater, parent, false);
        return new TheaterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TheaterViewHolder holder, int position) {
        holder.tvTheaterName.setText(theaterList.get(position));

        // Đổi nền khi được chọn
        if (position == selectedPosition) {
            holder.container.setSelected(true);
            holder.tvTheaterName.setTextColor(Color.WHITE);
        } else {
            holder.container.setSelected(false);
            holder.tvTheaterName.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onTheaterClick(theaterList.get(selectedPosition)); // 💥 gửi rạp được chọn
            }

        });

    }

    @Override
    public int getItemCount() {
        return theaterList.size();
    }

    static class TheaterViewHolder extends RecyclerView.ViewHolder {
        TextView tvTheaterName;
        View container;

        public TheaterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTheaterName = itemView.findViewById(R.id.tvTheaterName);
            container = itemView.findViewById(R.id.theater_container); // 🎯 View dùng để đổi nền
        }
    }
}
