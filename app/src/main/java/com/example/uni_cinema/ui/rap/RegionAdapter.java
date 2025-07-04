package com.example.uni_cinema.ui.rap;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uni_cinema.R;
import com.example.uni_cinema.ui.phim.MovieAdapter;

import java.util.List;

public class RegionAdapter extends RecyclerView.Adapter<RegionAdapter.RegionViewHolder> {
    private List<Region> regionList;
    private Context context;

    private int selectedPosition = -1;
    private OnRegionClickListener listener;

    public interface OnRegionClickListener {
        void onClick(String selectedProvince);
    }

    public RegionAdapter(List<Region> regionList, Context context, OnRegionClickListener listener) {
        this.regionList = regionList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RegionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rap, parent, false);
        return new RegionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegionViewHolder holder, int position) {
        Region region = regionList.get(position);
        holder.tvRegionName.setText(region.getNameProvince());

        // Hiệu ứng chọn vùng
        if (position == selectedPosition) {
            holder.container.setBackgroundResource(R.drawable.bg_region_selected);
            holder.tvRegionName.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.tvRegionName.setTypeface(null, Typeface.BOLD);
            holder.container.setSelected(true);
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_region_unselected);
            holder.tvRegionName.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.tvRegionName.setTypeface(null, Typeface.NORMAL);
            holder.container.setSelected(false);
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            int previous = selectedPosition;
            selectedPosition = currentPos;
            notifyItemChanged(previous);
            notifyItemChanged(currentPos);

            if (listener != null) {
                listener.onClick(regionList.get(currentPos).getNameProvince());
            }
        });

    }

    @Override
    public int getItemCount() {
        return regionList.size();
    }

    public static class RegionViewHolder extends RecyclerView.ViewHolder {
        TextView tvRegionName;
        LinearLayout container;

        public RegionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRegionName = itemView.findViewById(R.id.tvRegionName);
            container = itemView.findViewById(R.id.region_container);
        }
    }

}
