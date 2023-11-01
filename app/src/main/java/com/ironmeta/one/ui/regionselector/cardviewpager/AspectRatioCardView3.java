package com.ironmeta.one.ui.regionselector.cardviewpager;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

public class AspectRatioCardView3  extends CardView {
    private float mRatio = 0.57f;

    public AspectRatioCardView3(@NonNull Context context) {
        super(context);
    }

    public AspectRatioCardView3(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioCardView3(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
