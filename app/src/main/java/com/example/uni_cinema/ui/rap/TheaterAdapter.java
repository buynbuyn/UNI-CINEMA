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
    private List<Region> theaterList;
    private int selectedPosition = -1;
    private OnTheaterClickListener listener;

    public interface OnTheaterClickListener {
        void onTheaterClick(Region selectedRegion); // ✅ trả Region để lấy name + ID
    }

    public TheaterAdapter(List<Region> theaterList, OnTheaterClickListener listener) {
        this.theaterList = theaterList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TheaterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theater, parent, false);
        return new TheaterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TheaterViewHolder holder, int position) {
        Region region = theaterList.get(position);
        holder.tvTheaterName.setText(region.getNameTheater()); // ✅ hiển thị tên rạp

        // Nền được chọn
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
                listener.onTheaterClick(region); // ✅ gửi Region về
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
            container = itemView.findViewById(R.id.theater_container);
        }
    }
}
