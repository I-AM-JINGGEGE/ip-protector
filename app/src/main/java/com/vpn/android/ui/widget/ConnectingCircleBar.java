package com.vpn.android.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.vpn.android.utils.SystemPropertyUtils;

public class ConnectingCircleBar extends View {

    private RectF mColorWheelRectangle = new RectF();
    private Paint mDefaultWheelPaint;
    private Paint mColorWheelPaint;
    private float circleStrokeWidth;
    private float progress = 0;
    private float progressMax = 100;
    private float mSweepAnglePer = progress * 360 / progressMax;
    private float angleOffset = 0;
    private int mColors[];

    public ConnectingCircleBar(Context context) {
        this(context, null);
    }

    public ConnectingCircleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConnectingCircleBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mColorWheelPaint = new Paint();
        mColors = new int[]{0xFF14E3FD, 0xFF2DBBFF, 0xFF2554FE};
        mColorWheelPaint.setStyle(Paint.Style.STROKE);
        mColorWheelPaint.setStrokeCap(Paint.Cap.ROUND);
        mColorWheelPaint.setAntiAlias(true);

        mDefaultWheelPaint = new Paint();
        mDefaultWheelPaint.setColor(Color.parseColor("#515151"));
        mDefaultWheelPaint.setStyle(Paint.Style.STROKE);
        mDefaultWheelPaint.setStrokeCap(Paint.Cap.ROUND);
        mDefaultWheelPaint.setAntiAlias(true);

        circleStrokeWidth = SystemPropertyUtils.dp2px(getContext(), 8);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);

        mColorWheelRectangle.set(0 + circleStrokeWidth/2, 0 + circleStrokeWidth/2, min - circleStrokeWidth/2, min - circleStrokeWidth/2);
        mColorWheelPaint.setStrokeWidth(circleStrokeWidth);
        mDefaultWheelPaint.setStrokeWidth(circleStrokeWidth);

        angleOffset = (float) Math.toDegrees(Math.asin(circleStrokeWidth / 2f / (min / 2 - circleStrokeWidth / 2f)));

        setProgressShaderColor(mColors);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawArc(mColorWheelRectangle, 0, 359, false, mDefaultWheelPaint);
        canvas.drawArc(mColorWheelRectangle, 270 + angleOffset, mSweepAnglePer, false, mColorWheelPaint);
    }

    public void setStrokeWidth(float width) {
        circleStrokeWidth = width;
    }

    public float Textscale(float n, float m) {
        return n / 500 * m;
    }

    public void update(float progress) {
        this.progress = progress;
        mSweepAnglePer = progress * 360 / progressMax;
        postInvalidate();
    }

    public void setProgressMax(int progressMax) {
        this.progressMax = progressMax;
    }

    public void setProgressColor(int color) {
        mColorWheelPaint.setShader(null);
        mColorWheelPaint.setColor(color);
    }

    public void setDefaultColor(int color) {
        mDefaultWheelPaint.setShader(null);
        mDefaultWheelPaint.setColor(color);
    }

    public void setProgressShaderColor(int[] shaderColor) {
        this.mColors = shaderColor;
        Shader newShader = new SweepGradient(mColorWheelRectangle.centerX(), mColorWheelRectangle.centerY(), mColors, null);
        Matrix matrix = new Matrix();
        matrix.setRotate(-90f, mColorWheelRectangle.centerX(), mColorWheelRectangle.centerY());
        newShader.setLocalMatrix(matrix);
        mColorWheelPaint.setShader(newShader);
    }
}
