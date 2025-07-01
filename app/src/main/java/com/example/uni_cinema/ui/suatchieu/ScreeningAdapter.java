package com.example.uni_cinema.ui.suatchieu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uni_cinema.R;

import java.util.List;

public class ScreeningAdapter extends RecyclerView.Adapter<ScreeningAdapter.ScreeningViewHolder>{
    private List<Screening> screeningList;
    private final Context context;

    public ScreeningAdapter(Context context, List<Screening> screeningList) {
        this.context = context;
        this.screeningList = screeningList;
    }

    public void updateData(List<Screening> newList) {
        screeningList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScreeningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_screening, parent, false);
        return new ScreeningViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScreeningViewHolder holder, int position) {
        Screening item = screeningList.get(position);
        holder.tvMovieTitle.setText(item.movieTitle);
        holder.containerSuatChieu.removeAllViews();

        for (Screening.TimeSlot slot : item.timeSlots) {
            View slotView = LayoutInflater.from(context).inflate(R.layout.item_booking_slot, holder.containerSuatChieu, false);
            TextView tvRoom = slotView.findViewById(R.id.tvScreenRoom);
            TextView tvTime = slotView.findViewById(R.id.tvTimeRange);
            TextView tvSeats = slotView.findViewById(R.id.tvSeats);

            tvRoom.setText(slot.screenRoomName);
            tvTime.setText(slot.timeRangeDisplay);
            tvSeats.setText(slot.bookedSeats + "/" + slot.totalSeats + " Ghế");

            holder.containerSuatChieu.addView(slotView);
        }
    }

    @Override
    public int getItemCount() {
        return screeningList.size();
    }

    static class ScreeningViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle;
        LinearLayout containerSuatChieu;

        public ScreeningViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            containerSuatChieu = itemView.findViewById(R.id.container_suatchieu);
        }
    }
}
