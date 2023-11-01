package com.ironmeta.one.ui.regionselector.cardviewpager;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

public class AspectRatioCardView2 extends CardView {
    private float mRatio = 1.0f;

    public AspectRatioCardView2(@NonNull Context context) {
        super(context);
    }

    public AspectRatioCardView2(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioCardView2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRatio > 0) {
            int ratioHeight = (int) (MeasureSpec.getSize(widthMeasureSpec) * mRatio);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(ratioHeight, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
