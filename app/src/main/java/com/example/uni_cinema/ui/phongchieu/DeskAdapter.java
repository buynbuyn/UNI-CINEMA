package com.example.uni_cinema.ui.phongchieu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uni_cinema.R;

import java.util.List;

public class DeskAdapter extends RecyclerView.Adapter<DeskAdapter.DeskViewHolder> {

    private List<Desk> deskList;
    private OnDeskClickListener onDeskClickListener;
    private TextView infoTextView;

    public interface OnDeskClickListener {
        void onDeskClick(Desk desk);
    }

    public DeskAdapter(List<Desk> deskList, OnDeskClickListener listener, TextView infoTextView) {
        this.deskList = deskList;
        this.onDeskClickListener = listener;
        this.infoTextView = infoTextView;
    }

    @NonNull
    @Override
    public DeskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GridLayout gridLayout = new GridLayout(parent.getContext());
        gridLayout.setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        ));
        gridLayout.setRowCount(9); // 9 rows (A-K)
        gridLayout.setColumnCount(14); // Maximum columns based on your layout
        return new DeskViewHolder(gridLayout, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull DeskViewHolder holder, int position) {
        holder.bind(deskList);
    }

    @Override
    public int getItemCount() {
        return 1; // One GridLayout for the entire layout
    }

    public void updateData(List<Desk> newDeskList) {
        this.deskList = newDeskList;
        notifyDataSetChanged();
    }

    class DeskViewHolder extends RecyclerView.ViewHolder {
        private GridLayout gridLayout;

        public DeskViewHolder(@NonNull GridLayout gridLayout, Context context) {
            super(gridLayout);
            this.gridLayout = gridLayout;
            gridLayout.setBackgroundColor(android.graphics.Color.parseColor("#1C2526"));
        }

        public void bind(List<Desk> desks) {
            gridLayout.removeAllViews();
            char[] rows = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'K'};
            int[][] seatCounts = {{4, 6, 4}, {4, 6, 4}, {4, 6, 4}, {4, 6, 4}, {4, 6, 4}, {4, 6, 4}, {4, 6, 4}, {4, 6, 4}, {2, 6, 2}};

            for (int row = 0; row < 9; row++) {
                for (int colBlock = 0; colBlock < 3; colBlock++) {
                    LinearLayout rowLayout = new LinearLayout(gridLayout.getContext());
                    LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    rowParams.bottomMargin = dpToPx(8);
                    rowLayout.setLayoutParams(rowParams);
                    rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                    rowLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

                    if (colBlock == 0) {
                        TextView rowLabel = new TextView(gridLayout.getContext());
                        rowLabel.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(30), dpToPx(40)));
                        rowLabel.setText(String.valueOf(rows[row]));
                        rowLabel.setTextColor(android.graphics.Color.WHITE);
                        rowLabel.setTextSize(16);
                        rowLabel.setGravity(android.view.Gravity.CENTER);
                        rowLayout.addView(rowLabel);
                    }

                    boolean isVip = row <= 2; // A, B, C
                    boolean isCouple = row == 8; // K
                    int seatCount = seatCounts[row][colBlock];
                    int startIndex = colBlock == 0 ? 1 : (colBlock == 1 ? 5 : 11);

                    for (int col = 0; col < seatCount; col++) {
                        if (row == 0 && colBlock == 1 && (col == 2 || col == 3)) {
                            Space space = new Space(gridLayout.getContext());
                            space.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(36), dpToPx(36)));
                            rowLayout.addView(space);
                        } else {
                            int adjustedCol = (row == 0 && colBlock == 1 && col >= 2) ? col + 2 : col;
                            String deskId = rows[row] + String.format("%02d", startIndex + adjustedCol);
                            rowLayout.addView(createDeskButton(deskId, isVip, isCouple, desks));
                        }
                    }

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.rowSpec = GridLayout.spec(row);
                    params.columnSpec = GridLayout.spec(colBlock);
                    gridLayout.addView(rowLayout, params);
                }
            }
        }

        private Button createDeskButton(String deskId, boolean isVip, boolean isCouple, List<Desk> desks) {
            Button desk = new Button(gridLayout.getContext());
            desk.setId(View.generateViewId());
            Desk deskData = desks.stream().filter(d -> d.getIdDesk().equals(deskId)).findFirst().orElse(null);
            desk.setText(deskId);
            int width = dpToPx(36);
            desk.setLayoutParams(new LinearLayout.LayoutParams(width, dpToPx(36)));
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) desk.getLayoutParams();
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            desk.setBackgroundColor(isVip ? android.graphics.Color.parseColor("#FFD700") : android.graphics.Color.parseColor("#2196F3"));
            desk.setTextColor(android.graphics.Color.WHITE);
            desk.setTextSize(12);
            desk.setPadding(0, 0, 0, 0);

            if (deskData != null) {
                desk.setEnabled(deskData.isAvailable());
                if (!deskData.isAvailable()) {
                    desk.setBackgroundColor(android.graphics.Color.parseColor("#FF5722")); // Booked
                }
                if (isCouple) {
                    desk.setBackgroundColor(android.graphics.Color.parseColor("#D81B60")); // Couple
                }
            }

            desk.setOnClickListener(v -> {
                if (deskData != null && deskData.isAvailable()) {
                    v.setSelected(!v.isSelected());
                    v.setBackgroundColor(v.isSelected() ? android.graphics.Color.GREEN : (isVip ? android.graphics.Color.parseColor("#FFD700") : android.graphics.Color.parseColor("#2196F3")));
                    if (onDeskClickListener != null) {
                        onDeskClickListener.onDeskClick(deskData);
                    }
                    if (infoTextView != null) {
                        infoTextView.setText("Seat: " + deskId + "\nPrice: " + (deskData != null ? deskData.getPrice() : 0) + " VND\nCategory: " + (deskData != null ? deskData.getCategoryName() : "N/A"));
                    }
                }
            });
            return desk;
        }

        private int dpToPx(int dp) {
            float density = gridLayout.getContext().getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}