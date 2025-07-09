package com.example.uni_cinema.ui.suatchieu;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uni_cinema.R;
import com.example.uni_cinema.ui.phongchieu.DeskActivity;

import java.util.List;

public class ScreeningAdapter extends RecyclerView.Adapter<ScreeningAdapter.ScreeningViewHolder> {
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

        List<Screening.TimeSlot> slots = item.timeSlots;
        LinearLayout rowLayout = null;

        for (int i = 0; i < slots.size(); i++) {
            if (i % 3 == 0) {
                // Tạo dòng mới mỗi 3 slot
                rowLayout = new LinearLayout(context);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                rowLayout.setPadding(0, 8, 0, 8);
                holder.containerSuatChieu.addView(rowLayout);
            }

            View slotView = LayoutInflater.from(context)
                    .inflate(R.layout.item_booking_slot, rowLayout, false);

            Screening.TimeSlot slot = slots.get(i);
            TextView tvRoom = slotView.findViewById(R.id.tvScreenRoom);
            TextView tvTime = slotView.findViewById(R.id.tvTimeRange);
            TextView tvSeats = slotView.findViewById(R.id.tvSeats);

            tvRoom.setText(slot.screenRoomName);
            tvTime.setText(slot.timeRangeDisplay);
            tvSeats.setText(slot.bookedSeats + "/" + slot.totalSeats + " Ghế");

            slotView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DeskActivity.class);
                intent.putExtra("idScreeningRoom", slot.screenRoomId);
                intent.putExtra("screeningId", slot.screeningId);
                intent.putExtra("movieTitle", item.movieTitle);
                intent.putExtra("timeRange", slot.timeRangeDisplay);
                context.startActivity(intent);
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            params.setMarginEnd(20);
            slotView.setLayoutParams(params);

            if (rowLayout != null) {
                rowLayout.addView(slotView);
            }
        }

        // Bù slot trống nếu dòng cuối chưa đủ 3
        int remainder = slots.size() % 3;
        if (remainder != 0 && rowLayout != null) {
            for (int j = 0; j < 3 - remainder; j++) {
                View emptyView = new View(context);
                LinearLayout.LayoutParams emptyParams = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                rowLayout.addView(emptyView, emptyParams);
            }
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