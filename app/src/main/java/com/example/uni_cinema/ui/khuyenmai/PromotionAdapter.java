package com.example.uni_cinema.ui.khuyenmai;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uni_cinema.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.ViewHolder> {
    private final Context context;
    private List<Promotion> promotionList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public PromotionAdapter(Context context, List<Promotion> promotionList) {
        this.context = context;
        this.promotionList = promotionList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBanner, icPromotionType;
        TextView tvTitle, tvTime;

        public ViewHolder(View view) {
            super(view);
            imgBanner = view.findViewById(R.id.imgPromotionBanner);
            tvTitle = view.findViewById(R.id.tvPromotionTitle);
            icPromotionType = view.findViewById(R.id.icPromotionType); // calendar icon
            tvTime = view.findViewById(R.id.tvPromotionTime);
        }
    }

    @Override
    public PromotionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_khuyenmai, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PromotionAdapter.ViewHolder holder, int position) {
        Promotion promo = promotionList.get(position);

        holder.tvTitle.setText(promo.getTitle());

        String dateRange = dateFormat.format(promo.getStartDate().toDate())
                + " - " + dateFormat.format(promo.getEndDate().toDate());
        holder.tvTime.setText(dateRange);

        Glide.with(context)
                .load(promo.getBannerImage())
                .placeholder(R.drawable.loading_image)
                .error(R.drawable.error_image)
                .into(holder.imgBanner);

        holder.itemView.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("promotion_id", promo.getId());
            Navigation.findNavController(v).navigate(R.id.chitiet_KhuyenmaiFragment, args);
        });
    }

    @Override
    public int getItemCount() {
        return (promotionList != null) ? promotionList.size() : 0;
    }

    public void updateData(List<Promotion> newList) {
        this.promotionList = newList;
        notifyDataSetChanged();
    }

}

