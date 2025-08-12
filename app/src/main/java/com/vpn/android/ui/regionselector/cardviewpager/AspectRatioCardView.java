package com.vpn.android.ui.regionselector.cardviewpager;

import android.content.Context;
import android.util.AttributeSet;

import androidx.cardview.widget.CardView;

/**
 * 锁定宽高比的CardView
 *
 */
public class AspectRatioCardView extends CardView {
    private float mRatio = 1.05f;

    public AspectRatioCardView(Context context) {
        this(context, null);
    }

    public AspectRatioCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioCardView(Context context, AttributeSet attrs, int defStyleAttr) {
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
