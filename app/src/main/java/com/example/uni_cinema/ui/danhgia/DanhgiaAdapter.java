package com.example.uni_cinema.ui.danhgia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uni_cinema.R;
import com.example.uni_cinema.model.Danhgia;

import java.util.List;

public class DanhgiaAdapter extends RecyclerView.Adapter<DanhgiaAdapter.ViewHolder> {

    private Context context;
    private List<Danhgia> danhgiaList;
    private String currentUserId;
    private String currentUserEmail;
    private OnCommentActionListener listener;

    public interface OnCommentActionListener {
        void onEditComment(Danhgia danhgia);
    }

    public DanhgiaAdapter(Context context, List<Danhgia> danhgiaList, String currentUserId, OnCommentActionListener listener) {
        this.context = context;
        this.danhgiaList = danhgiaList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setCurrentUserEmail(String currentUserEmail) {
        this.currentUserEmail = currentUserEmail;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_danhgia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Danhgia danhgia = danhgiaList.get(position);
        holder.tvComment.setText(danhgia.getComment());
        holder.tvRating.setRating(danhgia.getRating());
        holder.tvDateTime.setText(danhgia.getDateTime());

        // Ẩn một phần UID: hiện 6 ký tự đầu, ..., 4 ký tự cuối
        String uid = danhgia.getIdUser();
        String displayUid = uid;
        if (uid != null && uid.length() > 10) {
            displayUid = uid.substring(0, 6) + "..." + uid.substring(uid.length() - 4);
        }
        holder.tvEmail.setText(displayUid);

        // Hiển thị nút sửa nếu bình luận thuộc về người dùng hiện tại
        if (currentUserId != null && currentUserId.equals(danhgia.getIdUser())) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditComment(danhgia);
                }
            });
        } else {
            holder.btnEdit.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return danhgiaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvComment, tvDateTime, tvEmail;
        RatingBar tvRating;
        Button btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}