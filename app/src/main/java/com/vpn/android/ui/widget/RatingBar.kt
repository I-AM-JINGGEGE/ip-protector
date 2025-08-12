package com.vpn.android.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.view.LayoutInflater
import com.vpn.android.databinding.LayoutRatingBarBinding

class RatingBar(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    FrameLayout(
        context!!, attrs, defStyleAttr, defStyleRes
    ) {
    private var binding: LayoutRatingBarBinding =
        LayoutRatingBarBinding.inflate(LayoutInflater.from(getContext()), this, true)
    private var mOnItemSelectedListener: OnItemSelectedListener? = null
    private var mSelectedStar: Type? = null
    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    val selectedSmiley: Type?
        get() = mSelectedStar

    init {
        binding.apply {
            star1.setOnClickListener {
                star1.isSelected = true
                star2.isSelected = false
                star3.isSelected = false
                star4.isSelected = false
                star5.isSelected = false
                mSelectedStar = Type.TERRIBLE
                mOnItemSelectedListener?.onItemSelected(mSelectedStar!!)
            }
            star2.setOnClickListener {
                star1.isSelected = true
                star2.isSelected = true
                star3.isSelected = false
                star4.isSelected = false
                star5.isSelected = false
                mSelectedStar = Type.BAD
                mOnItemSelectedListener?.onItemSelected(mSelectedStar!!)
            }
            star3.setOnClickListener {
                star1.isSelected = true
                star2.isSelected = true
                star3.isSelected = true
                star4.isSelected = false
                star5.isSelected = false
                mSelectedStar = Type.OK
                mOnItemSelectedListener?.onItemSelected(mSelectedStar!!)
            }
            star4.setOnClickListener {
                star1.isSelected = true
                star2.isSelected = true
                star3.isSelected = true
                star4.isSelected = true
                star5.isSelected = false
                mSelectedStar = Type.GOOD
                mOnItemSelectedListener?.onItemSelected(mSelectedStar!!)
            }
            star5.setOnClickListener {
                star1.isSelected = true
                star2.isSelected = true
                star3.isSelected = true
                star4.isSelected = true
                star5.isSelected = true
                mSelectedStar = Type.GREAT
                mOnItemSelectedListener?.onItemSelected(mSelectedStar!!)
            }
        }
    }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener) {
        mOnItemSelectedListener = listener
    }

    interface OnItemSelectedListener {
        fun onItemSelected(type: Type)
    }

    enum class Type {
        TERRIBLE, BAD, OK, GOOD, GREAT
    }
}