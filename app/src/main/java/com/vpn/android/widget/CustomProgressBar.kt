package com.vpn.android.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.vpn.android.R
import com.vpn.android.utils.SystemPropertyUtils

class CustomProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val completedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val uncompletedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var progressBitmap: Bitmap? = null
    private var progressRect = RectF()
    private var imageRect = RectF()
    
    // 进度条属性
    private var _progress: Float = 0f // 0.0 - 1.0
    private var progressBarHeight: Float = 0f
    private var cornerRadius: Float = 0f
    private var strokeWidth = SystemPropertyUtils.dp2px(context, 4f)
    
    // 颜色配置
    private var completedColor: Int = ContextCompat.getColor(context, R.color.progress_completed)
    private var completedBorderColor: Int = ContextCompat.getColor(context, R.color.progress_completed_border)
    private var uncompletedColor: Int = ContextCompat.getColor(context, R.color.progress_uncompleted)
    
    // 图片配置
    private var progressImageResId: Int = R.drawable.ic_progress_indicator

    init {
        initPaints()
    }
    
    private fun initPaints() {
        // 已完成部分的填充颜色
        completedPaint.style = Paint.Style.FILL
        completedPaint.color = completedColor
        
        // 未完成部分的颜色
        uncompletedPaint.style = Paint.Style.FILL
        uncompletedPaint.color = uncompletedColor
    }
    
    private fun loadProgressImage() {
        try {
            val originalBitmap = BitmapFactory.decodeResource(resources, progressImageResId)
            // 创建一个与当前高度一样大小的 bitmap
            val targetSize = height
            if (targetSize > 0 && originalBitmap != null) {
                progressBitmap = Bitmap.createScaledBitmap(originalBitmap, targetSize, targetSize, true)
                originalBitmap.recycle() // 释放原始 bitmap
            } else {
                progressBitmap = originalBitmap
            }
        } catch (e: Exception) {  }
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressBarHeight = h.toFloat()
        cornerRadius = progressBarHeight / 2f
        
        // 更新进度条区域
        progressRect.set(0f, progressBarHeight/4, w.toFloat(), progressBarHeight-progressBarHeight/4)

        loadProgressImage()

        // 更新图片区域
        val imageHeight = progressBarHeight
        val imageWidth = imageHeight
        imageRect.set(0f, 0f, imageWidth, progressBarHeight)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val completedWidth = width * _progress
        
        // 绘制未完成部分
        canvas.drawRoundRect(progressRect, cornerRadius, cornerRadius, uncompletedPaint)
        
        // 绘制已完成部分
        if (completedWidth > 0) {
            val completedRect = RectF(0f + strokeWidth, progressBarHeight/4 + strokeWidth, completedWidth - strokeWidth, progressBarHeight-progressBarHeight/4 - strokeWidth)
            canvas.drawRoundRect(completedRect, cornerRadius, cornerRadius, completedPaint)
        }
        
        // 绘制进度指示器图片
        drawProgressIndicator(canvas, completedWidth)
    }
    
    private fun drawProgressIndicator(canvas: Canvas, completedWidth: Float) {
        progressBitmap?.let { bitmap ->
            // 计算图片尺寸，确保图片不会太大
            val imageHeight = progressBarHeight
            val imageWidth = imageHeight

            // 计算图片位置，确保图片完整显示在进度条上方
            val imageX = completedWidth - imageWidth / 2f
            
            // 确保图片不会超出左右边界
            val clampedX = imageX.coerceIn(0f, width - imageWidth)
            
            val srcRect = Rect(0, 0, imageWidth.toInt(), imageHeight.toInt())
            val dstRect = RectF(clampedX, 0f, clampedX + imageWidth, imageHeight)
            
            canvas.drawBitmap(bitmap, srcRect, dstRect, null)
        }
    }
    
    /**
     * 设置进度 (0.0 - 1.0)
     */
    fun setProgress(progress: Float) {
        this._progress = progress.coerceIn(0f, 1f)
        invalidate()
    }
    
    /**
     * 获取当前进度
     */
    fun getProgress(): Float = _progress
    
    /**
     * 设置已完成部分的颜色
     */
    fun setCompletedColor(color: Int) {
        completedColor = color
        completedPaint.color = color
        invalidate()
    }
    
    /**
     * 设置未完成部分的颜色
     */
    fun setUncompletedColor(color: Int) {
        uncompletedColor = color
        uncompletedPaint.color = color
        invalidate()
    }
    
    /**
     * 设置进度指示器图片
     */
    fun setProgressImage(resId: Int) {
        progressImageResId = resId
        loadProgressImage()
        invalidate()
    }
}
