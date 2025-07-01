package com.example.uni_cinema.ui.suatchieu;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uni_cinema.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {
    private final List<LocalDate> dateList;
    private int selectedPosition = 0;
    private final OnDateClickListener listener;

    public interface OnDateClickListener {
        void onDateSelected(LocalDate date);
    }

    public DateAdapter(List<LocalDate> dateList, OnDateClickListener listener) {
        this.dateList = dateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_datesuatchieu, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        LocalDate date = dateList.get(position);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd");

        holder.tvDayOfWeek.setText(date.format(dayFormatter).toUpperCase());
        holder.tvDate.setText(date.format(dateFormatter));

        boolean isSelected = (position == selectedPosition);

        if (isSelected) {
            holder.container.setBackgroundResource(R.drawable.bg_date_selected);
            holder.tvDate.setTypeface(null, Typeface.BOLD);
            holder.tvDayOfWeek.setTypeface(null, Typeface.BOLD);
            holder.tvDate.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.tvDayOfWeek.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_date_unselected);
            holder.tvDate.setTypeface(null, Typeface.BOLD);
            holder.tvDayOfWeek.setTypeface(null, Typeface.BOLD);
            holder.tvDate.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
            holder.tvDayOfWeek.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            int prev = selectedPosition;
            selectedPosition = pos;
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            listener.onDateSelected(dateList.get(pos));
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayOfWeek, tvDate;
        View container;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            tvDate = itemView.findViewById(R.id.tvDate);
            container = itemView.findViewById(R.id.container_date);
        }
    }
}
