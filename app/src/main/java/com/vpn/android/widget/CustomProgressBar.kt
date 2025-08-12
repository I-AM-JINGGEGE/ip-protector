package com.vpn.android.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.vpn.android.R

class CustomProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val completedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val uncompletedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var progressBitmap: Bitmap? = null
    private var progressRect = RectF()
    private var imageRect = RectF()
    
    // 进度条属性
    private var _progress: Float = 0f // 0.0 - 1.0
    private var progressBarHeight: Float = 0f
    private var cornerRadius: Float = 0f
    
    // 颜色配置
    private var completedColor: Int = ContextCompat.getColor(context, R.color.progress_completed)
    private var completedBorderColor: Int = ContextCompat.getColor(context, R.color.progress_completed_border)
    private var uncompletedColor: Int = ContextCompat.getColor(context, R.color.progress_uncompleted)
    
    // 图片配置
    private var progressImageResId: Int = R.drawable.ic_progress_indicator
    private var imageHeightRatio: Float = 2.2f // 图片高度是进度条高度的2.2倍
    
    init {
        initPaints()
        loadProgressImage()
    }
    
    private fun initPaints() {
        // 已完成部分的填充颜色
        completedPaint.style = Paint.Style.FILL
        completedPaint.color = completedColor
        
        // 已完成部分的边框颜色
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 4f
        borderPaint.color = completedBorderColor
        
        // 未完成部分的颜色
        uncompletedPaint.style = Paint.Style.FILL
        uncompletedPaint.color = uncompletedColor
    }
    
    private fun loadProgressImage() {
        try {
            progressBitmap = BitmapFactory.decodeResource(resources, progressImageResId)
        } catch (e: Exception) {
            // 如果图片加载失败，创建一个默认的圆形指示器
            createDefaultProgressIndicator()
        }
    }
    
    private fun createDefaultProgressIndicator() {
        val size = (progressBarHeight * imageHeightRatio).toInt()
        progressBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(progressBitmap!!)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = completedBorderColor
        paint.style = Paint.Style.FILL
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressBarHeight = h.toFloat()
        cornerRadius = progressBarHeight / 2f
        
        // 更新进度条区域
        progressRect.set(0f, 0f, w.toFloat(), progressBarHeight)
        
        // 更新图片区域
        val imageHeight = progressBarHeight * imageHeightRatio
        val imageWidth = progressBitmap?.width?.toFloat() ?: imageHeight
        val imageY = (progressBarHeight - imageHeight) / 2f
        imageRect.set(0f, imageY, imageWidth, imageY + imageHeight)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val completedWidth = width * _progress
        
        // 绘制未完成部分
        canvas.drawRoundRect(progressRect, cornerRadius, cornerRadius, uncompletedPaint)
        
        // 绘制已完成部分
        if (completedWidth > 0) {
            val completedRect = RectF(0f, 0f, completedWidth, progressBarHeight)
            canvas.drawRoundRect(completedRect, cornerRadius, cornerRadius, completedPaint)
        }
        
        // 绘制已完成部分的边框
        if (completedWidth > 0) {
            val borderRect = RectF(0f, 0f, completedWidth, progressBarHeight)
            canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint)
        }
        
        // 绘制进度指示器图片
        drawProgressIndicator(canvas, completedWidth)
    }
    
    private fun drawProgressIndicator(canvas: Canvas, completedWidth: Float) {
        progressBitmap?.let { bitmap ->
            val imageHeight = progressBarHeight * imageHeightRatio
            val imageWidth = bitmap.width * (imageHeight / bitmap.height)
            val imageY = (progressBarHeight - imageHeight) / 2f
            val imageX = completedWidth - imageWidth / 2f
            
            // 确保图片不会超出边界
            val clampedX = imageX.coerceIn(0f, width - imageWidth)
            
            val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
            val dstRect = RectF(clampedX, imageY, clampedX + imageWidth, imageY + imageHeight)
            
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
     * 设置已完成部分的边框颜色
     */
    fun setCompletedBorderColor(color: Int) {
        completedBorderColor = color
        borderPaint.color = color
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
    
    /**
     * 设置图片高度比例
     */
    fun setImageHeightRatio(ratio: Float) {
        imageHeightRatio = ratio
        invalidate()
    }
}
