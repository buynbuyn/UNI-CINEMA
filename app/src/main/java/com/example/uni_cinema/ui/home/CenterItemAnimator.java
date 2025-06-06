package com.example.uni_cinema.ui.home;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CenterItemAnimator extends RecyclerView.OnScrollListener {

    private LinearLayoutManager layoutManager;

    // Các biến điều chỉnh hiệu ứng:
    private float mShrinkAmount = 0.1f;   // Mức độ thu nhỏ/mờ (10%)
    private float mShrinkDistance = 1.0f; // Khoảng cách từ tâm để bắt đầu hiệu ứng
    private float mMinAlpha = 0.7f;       // Ngưỡng alpha tối thiểu (70% rõ)

    // Góc nghiêng tối đa theo trục Y
    private float mMaxRotationY = 2f;     // Góc nghiêng tinh tế (2 độ)

    public CenterItemAnimator(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        scaleAlphaAndRotateViewsSubtly(recyclerView);
    }

    public void scaleAlphaAndRotateViewsSubtly(RecyclerView recyclerView) {
        if (recyclerView.getWidth() == 0 || recyclerView.getHeight() == 0) {
            return;
        }

        float midpoint = recyclerView.getWidth() / 2f;
        float d1 = mShrinkDistance * midpoint;

        // Giữ camera distance lớn để hiệu ứng 3D không quá rõ ràng
        recyclerView.setCameraDistance(recyclerView.getWidth() * 3f);

        for (int i = 0; i < layoutManager.getChildCount(); i++) {
            View child = layoutManager.getChildAt(i);
            if (child == null) continue;

            float childMidpoint = (layoutManager.getDecoratedLeft(child) + layoutManager.getDecoratedRight(child)) / 2f;
            float d = Math.min(d1, Math.abs(midpoint - childMidpoint));

            // Tính toán tỷ lệ dựa trên khoảng cách
            float ratio = d / d1;

            // 1. Scale (Thu nhỏ)
            float scale = 1f - mShrinkAmount * ratio;
            child.setScaleX(scale);
            child.setScaleY(scale);

            // 2. Alpha (Làm mờ)
            float alpha = 1f - mShrinkAmount * ratio;
            alpha = Math.max(mMinAlpha, alpha);
            child.setAlpha(alpha);

            // Đặt tâm xoay ở giữa item
            child.setPivotX(child.getWidth() / 2f);
            child.setPivotY(child.getHeight() / 2f);

            // 3. RotationY (Nghiêng theo trục Y)
            float rotationY = mMaxRotationY * ratio;

            // Xác định hướng nghiêng:
            // Item bên trái tâm nghiêng sang phải (góc dương)
            // Item bên phải tâm nghiêng sang trái (góc âm)
            if (childMidpoint < midpoint) {
                child.setRotationY(rotationY);
            } else {
                child.setRotationY(-rotationY);
            }

            // Loại bỏ các transform khác để tập trung vào hiệu ứng nghiêng ngang
            child.setRotationX(0f);
            child.setTranslationZ(0f);
        }
    }
}