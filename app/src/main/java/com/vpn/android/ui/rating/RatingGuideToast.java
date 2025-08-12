package com.vpn.android.ui.rating;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.vpn.android.R;
import com.vpn.android.base.utils.ThreadUtils;

public class RatingGuideToast extends Toast {
    private final Context mContext;
    private final View mCustomView;
    private final ViewGroup mTranslateLayout;

    private ValueAnimator mValueAnim;
    private Interpolator mInterpolator;

    public static void go(Context context) {
        showRateGuideAnimation(context.getApplicationContext());
    }

    private static void showRateGuideAnimation(Context context) {
        if (context == null) {
            return;
        }

        ThreadUtils.delayRunOnMainThread(() -> new RatingGuideToast(context).show(), 1200);
    }

    private RatingGuideToast(Context context) {
        super(context);

        mContext = context;
        mCustomView = LayoutInflater.from(context).inflate(
                R.layout.ui_rating_guide_toast_layout, null);
        mTranslateLayout = mCustomView.findViewById(R.id.translation_layout);
        mInterpolator = new AccelerateDecelerateInterpolator();

        setGravity(Gravity.FILL_HORIZONTAL | Gravity.FILL_VERTICAL, 0, 0);
        setDuration(LENGTH_LONG);
    }

    private void setCustomView() {
        super.setView(mCustomView);
    }

    @Override
    public void show() {
        setCustomView();
        super.show();
        showAnimation();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void cancel() {
        if (mValueAnim != null) {
            mValueAnim.cancel();
        }

        mValueAnim = null;
        super.cancel();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void showAnimation() {
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        int startPosition = wm.getDefaultDisplay().getHeight() / 2
                + wm.getDefaultDisplay().getHeight() / 4;
        int endPosition = wm.getDefaultDisplay().getHeight() / 4;
        int offset = startPosition - endPosition;

        if (mValueAnim != null && mValueAnim.isRunning()) {
            mValueAnim.cancel();
        }

        mValueAnim = ValueAnimator.ofFloat(0, 1);
        mValueAnim
                .addUpdateListener(animation -> {
                    float value = (Float) animation.getAnimatedValue();

                    float alpha = 0;
                    float scale = 1f;
                    float translate = value;

                    if (value <= 0.25) {
                        alpha = 4 * value;
                        scale = 1.5f - 0.5f * (4 * value);
                    } else if (value >= 0.75) {
                        scale = 1f;
                        alpha = 4 * (1 - value);
                    } else {
                        alpha = 1f;
                        scale = 1f;
                    }

                    translate = mInterpolator.getInterpolation(Math.max(
                            translate - 0.25f, 0) / 0.75f);
                    int translateY = (int) (offset * translate);

                    mTranslateLayout.setAlpha(alpha);
                    mTranslateLayout.setScaleX(scale);
                    mTranslateLayout.setScaleY(scale);
                    mTranslateLayout.setTranslationY(-translateY);
                });

        mValueAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mValueAnim.cancel();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mValueAnim.setDuration(1500);
        mValueAnim.setRepeatCount(1);
        mValueAnim.setRepeatMode(ValueAnimator.RESTART);
        mValueAnim.setInterpolator(new LinearInterpolator());
        mValueAnim.start();
    }
}
