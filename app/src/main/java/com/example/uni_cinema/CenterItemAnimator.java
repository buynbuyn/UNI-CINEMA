package com.example.uni_cinema;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CenterItemAnimator extends RecyclerView.OnScrollListener {

    private LinearLayoutManager layoutManager;

    // Các biến điều chỉnh hiệu ứng:
    private float mShrinkAmount = 0.2f;   // Mức độ thu nhỏ/mờ (20%)
    private float mShrinkDistance = 0.9f; // Khoảng cách từ tâm để bắt đầu hiệu ứng
    private float mMinAlpha = 0.5f;       // Ngưỡng alpha tối thiểu (50% rõ)
    // Đã điều chỉnh lại góc nghiêng tối đa. Tăng nhẹ hơn so với lần trước.
    private float mMaxRotation = 8f;    // Góc nghiêng tối đa (8 độ) - Đã điều chỉnh

    public CenterItemAnimator(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        scaleAndAlphaAndRotateViews(recyclerView);
    }

    public void scaleAndAlphaAndRotateViews(RecyclerView recyclerView) {
        if (recyclerView.getWidth() == 0 || recyclerView.getHeight() == 0) {
            return;
        }

        float midpoint = recyclerView.getWidth() / 2f;
        float d1 = mShrinkDistance * midpoint;

        for (int i = 0; i < layoutManager.getChildCount(); i++) {
            View child = layoutManager.getChildAt(i);
            if (child == null) continue;

            float childMidpoint = (layoutManager.getDecoratedLeft(child) + layoutManager.getDecoratedRight(child)) / 2f;
            float d = Math.min(d1, Math.abs(midpoint - childMidpoint));

            // 1. Scale (Thu nhỏ)
            float scale = 1f - mShrinkAmount * (d / d1);
            child.setScaleX(scale);
            child.setScaleY(scale);

            // 2. Alpha (Làm mờ)
            float alpha = 1f - mShrinkAmount * (d / d1);
            alpha = Math.max(mMinAlpha, alpha);
            child.setAlpha(alpha);

            // 3. Rotation (Nghiêng) - Đã điều chỉnh toàn bộ logic này
            float rotation = mMaxRotation * (d / d1); // Vẫn tính góc nghiêng dựa trên khoảng cách

            // Đặt tâm xoay dọc ở giữa item (giống trước)
            child.setPivotY(child.getHeight() / 2f);

            // Quan trọng: Đặt tâm xoay ngang (pivotX) để tạo hiệu ứng "chốc đít vô"
            if (childMidpoint < midpoint) {
                // Item ở bên trái tâm:
                // Để nó nghiêng "chốc đít vô", tâm xoay phải là cạnh phải của item.
                // Góc quay phải là âm để xoay vào trong.
                child.setPivotX(child.getWidth());
                child.setRotationY(-rotation); // Góc âm
            } else {
                // Item ở bên phải tâm:
                // Để nó nghiêng "chốc đít vô", tâm xoay phải là cạnh trái của item.
                // Góc quay phải là dương để xoay vào trong.
                child.setPivotX(0f);
                child.setRotationY(rotation); // Góc dương
            }
        }
    }
}