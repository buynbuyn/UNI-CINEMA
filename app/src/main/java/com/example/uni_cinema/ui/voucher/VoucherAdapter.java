package com.example.uni_cinema.ui.voucher;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uni_cinema.R;

import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {
    private List<Voucher> voucherList;

    public VoucherAdapter(List<Voucher> voucherList) {
        this.voucherList = voucherList;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        String timeStart = voucher.getTimeStart() != null ? voucher.getTimeStart() : "Không rõ";
        String timeEnd = voucher.getTimeEnd() != null ? voucher.getTimeEnd() : "Không rõ";

        holder.tvCode.setText("Mã: " + voucher.getCode());
        holder.tvTime.setText("Hiệu lực: " + voucher.getTimeStart() + " - " + voucher.getTimeEnd());
        holder.tvDiscount.setText("Giảm: " + voucher.getDiscount() + "₫");

    }

    @Override
    public int getItemCount() {
        return voucherList != null ? voucherList.size() : 0;
    }

    public static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvTime, tvDiscount;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvVoucherCode);
            tvTime = itemView.findViewById(R.id.tvTimeRange);
            tvDiscount = itemView.findViewById(R.id.tvDiscountValue);
        }
    }

}
