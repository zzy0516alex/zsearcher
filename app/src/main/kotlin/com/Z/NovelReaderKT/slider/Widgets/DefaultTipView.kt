package com.Z.NovelReaderKT.slider.Widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import com.Z.NovelReader.R

class DefaultTipView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        ConstraintLayout(context, attrs, defStyleAttr) {
    private lateinit var tipTextView: TextView
    private lateinit var arrowView: ArrowView

    init {
        View.inflate(context, R.layout.layout_default_tip_view,this)
        initView()
    }


    private fun initView(){
        tipTextView = findViewById(R.id.tip_text)
        arrowView = findViewById(R.id.arrow_view)
        val defaultBackgroundColor = Color.WHITE
        val defaultTextColor = Color.BLACK

        tipTextView.apply {
            setTipBackground(defaultBackgroundColor)
            setTextColor(defaultTextColor)
        }
    }

    fun setTipBackground(@ColorInt color:Int){
        tipTextView.background = GradientDrawable().apply {
            cornerRadius = Utils.dpToPx(18).toFloat()
            setColor(color)
        }
        arrowView.setArrowColor(color)
    }

    fun setTipTextColor(@ColorInt color:Int){
        tipTextView.setTextColor(color)
    }

    fun setTipText(text:CharSequence){
        tipTextView.text = text
    }

}