package com.example.uni_cinema.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uni_cinema.R;

public class SuatchieuFragment extends Fragment {

    public SuatchieuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout XML làm container
        View rootView = inflater.inflate(R.layout.fragment_suatchieu, container, false);

        // Tìm container trong fragment_rap.xml
        LinearLayout containerLayout = rootView.findViewById(R.id.fragment_container);

        // Tạo HorizontalScrollView lớn bao quanh 3 khối
        HorizontalScrollView scrollView = new HorizontalScrollView(getContext());
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        scrollView.setBackgroundColor(Color.parseColor("#1C2526"));
        scrollView.setFillViewport(true);
        scrollView.setNestedScrollingEnabled(true);

        // Tạo LinearLayout chính chứa 3 khối
        LinearLayout mainLayout = new LinearLayout(getContext());
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        // Thêm TextView cho màn hình
        TextView screen = new TextView(getContext());
        screen.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(40)));
        screen.setBackgroundColor(Color.BLACK);
        screen.setText("MÀN HÌNH");
        screen.setTextColor(Color.WHITE);
        screen.setTextSize(18);
        screen.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams screenParams = (LinearLayout.LayoutParams) screen.getLayoutParams();
        screenParams.bottomMargin = dpToPx(16);
        containerLayout.addView(screen);

        // Tạo 3 khối (trái, giữa, phải)
        LinearLayout leftBlock = new LinearLayout(getContext());
        leftBlock.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        leftBlock.setOrientation(LinearLayout.VERTICAL);

        LinearLayout middleBlock = new LinearLayout(getContext());
        middleBlock.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        middleBlock.setOrientation(LinearLayout.VERTICAL);

        LinearLayout rightBlock = new LinearLayout(getContext());
        rightBlock.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        rightBlock.setOrientation(LinearLayout.VERTICAL);

        // Tạo 9 hàng (A, B, C, D, E, F, G, H, K) cho từng khối
        char[] rows = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'K'};
        for (int row = 0; row < 9; row++) {
            // Tạo LinearLayout cho mỗi hàng trong khối
            LinearLayout rowLayoutLeft = new LinearLayout(getContext());
            LinearLayout rowLayoutMiddle = new LinearLayout(getContext());
            LinearLayout rowLayoutRight = new LinearLayout(getContext());
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = dpToPx(8); // Đều nhau cho tất cả các hàng
            rowLayoutLeft.setLayoutParams(rowParams);
            rowLayoutMiddle.setLayoutParams(rowParams);
            rowLayoutRight.setLayoutParams(rowParams);
            rowLayoutLeft.setOrientation(LinearLayout.HORIZONTAL);
            rowLayoutMiddle.setOrientation(LinearLayout.HORIZONTAL);
            rowLayoutRight.setOrientation(LinearLayout.HORIZONTAL);
            rowLayoutLeft.setGravity(android.view.Gravity.CENTER_VERTICAL);
            rowLayoutMiddle.setGravity(android.view.Gravity.CENTER_VERTICAL);
            rowLayoutRight.setGravity(android.view.Gravity.CENTER_VERTICAL);

            // Thêm nhãn hàng
            TextView rowLabel = new TextView(getContext());
            rowLabel.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(30), LinearLayout.LayoutParams.WRAP_CONTENT));
            rowLabel.setText(String.valueOf(rows[row]));
            rowLabel.setTextColor(Color.WHITE);
            rowLabel.setTextSize(16);
            rowLabel.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams labelParams = (LinearLayout.LayoutParams) rowLabel.getLayoutParams();
            labelParams.rightMargin = dpToPx(8);
            rowLayoutLeft.addView(rowLabel);

            // Xác định loại ghế: VIP (A, B, C), thường (D, E, F, G, H), couple (K)
            boolean isVip = row <= 2; // A, B, C
            boolean isCouple = row == 8; // K

            // Khối trái: 4 ghế cho A, B, C, D-H; 2 ghế cho K
            for (int i = 1; i <= (isCouple ? 2 : 4); i++) {
                rowLayoutLeft.addView(createSeatButton(rows[row] + String.format("%02d", i), isVip, isCouple));
            }

            // Khối giữa: 6 ghế cho tất cả các hàng
            int startIndex = 5;
            for (int i = 0; i < 6; i++) {
                if (row == 0 && (i == 2 || i == 3)) { // Ẩn A07 và A08, tạo khoảng trống
                    Space space = new Space(getContext());
                    space.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40)));
                    rowLayoutMiddle.addView(space);
                } else {
                    int adjustedIndex = (row == 0 && i >= 2) ? i + 2 : i; // Điều chỉnh chỉ số cho hàng A
                    rowLayoutMiddle.addView(createSeatButton(rows[row] + String.format("%02d", startIndex + adjustedIndex), isVip, isCouple));
                }
            }

            // Khối phải: 4 ghế cho A, B, C, D-H; 2 ghế cho K
            startIndex = 11;
            int rightSeats = isCouple ? 2 : 4;
            for (int i = 0; i < rightSeats; i++) {
                rowLayoutRight.addView(createSeatButton(rows[row] + String.format("%02d", startIndex + i), isVip, isCouple));
            }

            // Thêm các hàng vào các khối
            leftBlock.addView(rowLayoutLeft);
            middleBlock.addView(rowLayoutMiddle);
            rightBlock.addView(rowLayoutRight);
        }

        // Thêm lối đi giữa các khối
        Space aisle1 = new Space(getContext());
        aisle1.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(24), LinearLayout.LayoutParams.WRAP_CONTENT));
        Space aisle2 = new Space(getContext());
        aisle2.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(24), LinearLayout.LayoutParams.WRAP_CONTENT));

        // Thêm 3 khối vào mainLayout
        mainLayout.addView(leftBlock);
        mainLayout.addView(aisle1);
        mainLayout.addView(middleBlock);
        mainLayout.addView(aisle2);
        mainLayout.addView(rightBlock);

        // Thêm mainLayout vào HorizontalScrollView
        scrollView.addView(mainLayout);
        // Thêm HorizontalScrollView vào container
        containerLayout.addView(scrollView);

        return rootView;
    }

    // Hàm tạo Button cho ghế
    private Button createSeatButton(String seatId, boolean isVip, boolean isCouple) {
        Button seat = new Button(getContext());
        seat.setId(View.generateViewId());
        seat.setText(seatId);
        int width = dpToPx(40); // Đều nhau cho tất cả ghế
        seat.setLayoutParams(new LinearLayout.LayoutParams(width, dpToPx(40)));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) seat.getLayoutParams();
        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4)); // Đều nhau cho tất cả các hàng
        seat.setBackgroundColor(isVip ? Color.parseColor("#FFD700") : Color.parseColor("#2196F3"));
        seat.setTextColor(Color.WHITE);
        seat.setTextSize(12);
        seat.setPadding(0, 0, 0, 0);
        seat.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            v.setBackgroundColor(v.isSelected() ? Color.GREEN : (isVip ? Color.parseColor("#FFD700") : Color.parseColor("#2196F3")));
        });
        return seat;
    }

    // Chuyển đổi dp sang px
    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}