package com.Z.NovelReaderKT.slider

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.annotation.*
import androidx.annotation.IntRange
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.getColorStateListOrThrow
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.withTranslation
import androidx.core.math.MathUtils
import com.Z.NovelReader.R
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.Z.NovelReaderKT.slider.Anims.ThumbValueAnimation
import com.Z.NovelReaderKT.slider.Anims.TipViewAnimator
import com.Z.NovelReaderKT.slider.Widgets.TipViewContainer
import java.lang.Integer.max
import java.lang.reflect.InvocationTargetException
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

abstract class BaseSlider constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {

    private var trackPaint: Paint
    private var trackSecondaryPaint: Paint
    private var ticksPaint: Paint
    private var inactiveTicksPaint: Paint
    private var inactiveTrackPaint: Paint
    private var thumbTextPaint: Paint
    private var haloPaint: Paint
    private var debugPaint: Paint


    private lateinit var trackColor: ColorStateList
    private lateinit var trackSecondaryColor: ColorStateList
    private lateinit var trackColorInactive: ColorStateList
    private lateinit var ticksColor: ColorStateList
    private lateinit var ticksColorInactive: ColorStateList
    private lateinit var thumbTextColor: ColorStateList
    private lateinit var haloColor: ColorStateList

    private val defaultThumbDrawable = MaterialShapeDrawable()
    private var customThumbDrawable: Drawable? = null

    private var thumbWidth = -1
    private var thumbHeight = -1
    private var thumbVOffset = 0
    private var thumbElevation = 0f
    private var isThumbWithinTrackBounds = false
    private var thumbText: String? = null
    private val thumbAnimation = ThumbValueAnimation()

    private var enableDrawHalo = true
    private var haloDrawable: RippleDrawable? = null
    private var haloRadius = 0
    private var tickRadius = 0f


    private val trackRectF = RectF()
    private var thumbOffset = 0

    private var trackInnerHPadding = 0
    private var trackInnerVPadding = 0
    private var trackCornerRadius = -1


    private var lastTouchEvent: MotionEvent? = null
    private var scaledTouchSlop = 0
    private var touchDownX = 0f
    private var isDragging = false
    private var isTackingStart = false

    private var tipView: TipViewContainer = TipViewContainer(context)
    private var isShowTipView = false

    private var hasDirtyData = false

    var enableHapticFeedback = false

    var valueFrom = 0f
        set(value) {
            if (field != value) {
                field = value
                hasDirtyData = true
                postInvalidate()
            }
        }

    var valueTo = 0f
        set(value) {
            if (field != value) {
                field = value
                hasDirtyData = true
                postInvalidate()
            }
        }

    var value = 0f
        private set

    var secondaryValue = 0f
        private set

    var stepSize = 0.0f
        set(value) {
            if (field != value && value > 0) {
                field = value
                hasDirtyData = true
                postInvalidate()
            }
        }

    var tickVisible = false

    //用户设置的高度
    private var sourceViewHeight = 0

    //修正后的真实高度，会根据thumb、thumb shadow、track的高度来进行调整
    private var viewHeight = 0

    var trackHeight = 0
        set(@IntRange(from = 0) value) {
            if (value != field) {
                field = value
                updateViewLayout()
            }
        }

    var trackWidth = 0

    companion object {
        var DEBUG_MODE = false

        private const val HIGH_QUALITY_FLAGS = Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG

        private const val HALO_ALPHA = 63
    }


    abstract fun onStartTacking()
    abstract fun onStopTacking()

    abstract fun onValueChanged(value: Float, fromUser: Boolean)

    abstract fun dispatchDrawInactiveTrackBefore(canvas: Canvas, trackRect: RectF, yCenter: Float): Boolean
    abstract fun drawInactiveTrackAfter(canvas: Canvas, trackRect: RectF, yCenter: Float)

    abstract fun dispatchDrawTrackBefore(canvas: Canvas, trackRect: RectF, yCenter: Float): Boolean

    abstract fun dispatchDrawSecondaryTrackBefore(canvas: Canvas, trackRect: RectF, yCenter: Float): Boolean
    abstract fun drawTrackAfter(canvas: Canvas, trackRect: RectF, yCenter: Float)

    abstract fun drawSecondaryTrackAfter(canvas: Canvas, trackRect: RectF, yCenter: Float)


    abstract fun dispatchDrawThumbBefore(canvas: Canvas, cx: Float, cy: Float): Boolean
    abstract fun drawThumbAfter(canvas: Canvas, cx: Float, cy: Float)


    init {
        inactiveTrackPaint = Paint(HIGH_QUALITY_FLAGS).apply {
            style = Paint.Style.FILL
        }

        trackPaint = Paint(HIGH_QUALITY_FLAGS).apply {
            style = Paint.Style.FILL
        }

        trackSecondaryPaint = Paint(HIGH_QUALITY_FLAGS).apply {
            style = Paint.Style.FILL
        }

        ticksPaint = Paint(HIGH_QUALITY_FLAGS).apply {
            style = Paint.Style.FILL
        }

        inactiveTicksPaint = Paint(HIGH_QUALITY_FLAGS).apply {
            style = Paint.Style.FILL
        }

        thumbTextPaint = Paint(HIGH_QUALITY_FLAGS).apply {
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
        }

        haloPaint = Paint(HIGH_QUALITY_FLAGS).apply {
            style = Paint.Style.FILL
        }

        debugPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        defaultThumbDrawable.shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS

        processAttributes(context, attrs, defStyleAttr)

        thumbAnimation.apply {
            addUpdateListener {
                adjustThumbDrawableBounds((getAnimatedValueAbsolute() * thumbRadius).toInt())
                postInvalidate()
            }
        }
    }

    private fun processAttributes(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int) {
        context.withStyledAttributes(attrs, R.styleable.NiftySlider, defStyleAttr, R.style.Widget_NiftySlider) {
            valueFrom = getFloat(R.styleable.NiftySlider_android_valueFrom, 0.0f)
            valueTo = getFloat(R.styleable.NiftySlider_android_valueTo, 1.0f)
            value = getFloat(R.styleable.NiftySlider_android_value, 0.0f)
            stepSize = getFloat(R.styleable.NiftySlider_android_stepSize, 0.0f)

            tickVisible = getBoolean(R.styleable.NiftySlider_ticksVisible, false)
            enableHapticFeedback = getBoolean(R.styleable.NiftySlider_android_hapticFeedbackEnabled, false)

            sourceViewHeight = getLayoutDimension(R.styleable.NiftySlider_android_layout_height, 0)
            trackHeight = getDimensionPixelOffset(R.styleable.NiftySlider_trackHeight, 0)

            setTrackTintList(
                    getColorStateList(R.styleable.NiftySlider_trackColor) ?: AppCompatResources.getColorStateList(
                            context,
                            R.color.default_track_color
                    )
            )
            setTrackSecondaryTintList(
                    getColorStateList(R.styleable.NiftySlider_trackSecondaryColor) ?: AppCompatResources.getColorStateList(
                            context,
                            R.color.default_track_color
                    )
            )
            setTrackInactiveTintList(
                    getColorStateList(R.styleable.NiftySlider_trackColorInactive) ?: AppCompatResources.getColorStateList(
                            context,
                            R.color.default_track_inactive_color
                    )
            )

            setTicksTintList(
                    getColorStateList(R.styleable.NiftySlider_ticksColor) ?: AppCompatResources.getColorStateList(
                            context,
                            R.color.default_ticks_color
                    )
            )
            setTicksInactiveTintList(
                    getColorStateList(R.styleable.NiftySlider_ticksColorInactive) ?: AppCompatResources.getColorStateList(
                            context,
                            R.color.default_ticks_inactive_color
                    )
            )


            val thumbW = getDimensionPixelOffset(R.styleable.NiftySlider_thumbWidth, -1)
            val thumbH = getDimensionPixelOffset(R.styleable.NiftySlider_thumbHeight, -1)

            setThumbTintList(getColorStateListOrThrow(R.styleable.NiftySlider_thumbColor))
            thumbRadius = getDimensionPixelOffset(R.styleable.NiftySlider_thumbRadius, 0)
            setThumbWidthAndHeight(thumbW, thumbH)

            setThumbVOffset(getDimensionPixelOffset(R.styleable.NiftySlider_thumbVOffset, 0))
            setThumbWithinTrackBounds(getBoolean(R.styleable.NiftySlider_thumbWithinTrackBounds, false))
            setThumbElevation(getDimension(R.styleable.NiftySlider_thumbElevation, 0f))
            setThumbShadowColor(getColor(R.styleable.NiftySlider_thumbShadowColor, Color.GRAY))
            setThumbStrokeColor(getColorStateList(R.styleable.NiftySlider_thumbStrokeColor))
            setThumbStrokeWidth(getDimension(R.styleable.NiftySlider_thumbStrokeWidth, 0f))
            setThumbText(getString(R.styleable.NiftySlider_thumbText) ?: "")
            setThumbTextTintList(
                    getColorStateList(R.styleable.NiftySlider_thumbTextColor) ?: ColorStateList.valueOf(
                            Color.WHITE
                    )
            )
            setThumbTextSize(getDimension(R.styleable.NiftySlider_thumbTextSize, 10f))
            setThumbTextBold(getBoolean(R.styleable.NiftySlider_thumbTextBold, false))

            setTrackInnerHPadding(getDimensionPixelOffset(R.styleable.NiftySlider_trackInnerHPadding, -1))
            setTrackInnerVPadding(getDimensionPixelOffset(R.styleable.NiftySlider_trackInnerVPadding, -1))
            setTrackCornersRadius(getDimensionPixelOffset(R.styleable.NiftySlider_trackCornersRadius, -1))
            setEnableDrawHalo(getBoolean(R.styleable.NiftySlider_enableDrawHalo, true))
            setHaloTintList(getColorStateListOrThrow(R.styleable.NiftySlider_haloColor))
            setHaloRadius(getDimensionPixelOffset(R.styleable.NiftySlider_haloRadius, 0))
            setTickRadius(getDimension(R.styleable.NiftySlider_tickRadius, 0.0f))

            setTipViewVisibility(getBoolean(R.styleable.NiftySlider_tipViewVisible,false))
            setTipVerticalOffset(getDimensionPixelOffset(R.styleable.NiftySlider_tipViewVerticalOffset,0))
            setTipBackground(getColor(R.styleable.NiftySlider_tipViewBackground,Color.WHITE))
            setTipTextColor(getColor(R.styleable.NiftySlider_tipViewTextColor,Color.BLACK))
            setTipTextAutoChange(getBoolean(R.styleable.NiftySlider_tipTextAutoChange,true))
            setTipViewClippingEnabled(getBoolean(R.styleable.NiftySlider_isTipViewClippingEnabled,false))

        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateTrackWidth(w)
        updateHaloHotspot()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        trackPaint.color = getColorForState(trackColor)
        trackSecondaryPaint.color = getColorForState(trackSecondaryColor)
        ticksPaint.color = getColorForState(ticksColor)
        inactiveTicksPaint.color = getColorForState(ticksColorInactive)
        inactiveTrackPaint.color = getColorForState(trackColorInactive)
        if (defaultThumbDrawable.isStateful) {
            defaultThumbDrawable.state = drawableState
        }
        thumbTextPaint.color = getColorForState(thumbTextColor)
        haloPaint.color = getColorForState(haloColor)
        haloPaint.alpha = HALO_ALPHA
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (hasDirtyData) {
            validateDirtyData()
        }

        drawDebugArea(canvas)

        val yCenter = measuredHeight / 2f
        val width = measuredWidth
        drawInactiveTrack(canvas, width, yCenter)
        drawSecondaryTrack(canvas, width, yCenter)
        drawTrack(canvas, width, yCenter)
        drawTicks(canvas, trackWidth, yCenter)

        if ((isDragging || isFocused) && isEnabled) {
            //仅在v23以下版本启用此逻辑
            drawCompatHaloIfNeed(canvas, trackWidth, yCenter)
        }

        drawThumb(canvas, trackWidth, yCenter)
    }

    override fun invalidateDrawable(drawable: Drawable) {
        super.invalidateDrawable(drawable)
        invalidate()
    }

    override fun onAttachedToWindow() {
        if (isShowTipView) {
            tipView.attachTipView(this)
        }
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        tipView.detachTipView(this)
        super.onDetachedFromWindow()
    }

    private fun drawDebugArea(canvas: Canvas) {
        val offset = 1
        if (DEBUG_MODE) {
            debugPaint.color = Color.RED
            canvas.drawRect(
                    0f + offset,
                    0f + offset,
                    canvas.width.toFloat() - offset,
                    canvas.height.toFloat() - offset,
                    debugPaint
            )
            debugPaint.color = Color.BLUE
            canvas.drawLine(
                    0f,
                    canvas.height / 2f,
                    canvas.width.toFloat(),
                    canvas.height / 2f,
                    debugPaint
            )
            canvas.drawLine(
                    canvas.width / 2f,
                    0f,
                    canvas.width / 2f,
                    canvas.height.toFloat(),
                    debugPaint
            )

        }
    }

    /**
     * draw active track
     * 需要考虑如果使用半透明颜色时会与下层[trackColorInactive]颜色进行叠加，需注意叠加后的效果是否满足要求
     */
    private fun drawTrack(canvas: Canvas, width: Int, yCenter: Float) {
        trackRectF.set(
                0f + paddingLeft + trackInnerHPadding,
                yCenter - trackHeight / 2f,
                paddingLeft + trackInnerHPadding + thumbOffset * 2 + (trackWidth - thumbOffset * 2) * percentValue(value),
                yCenter + trackHeight / 2f
        )

        if (!dispatchDrawTrackBefore(canvas, trackRectF, yCenter)) {

            val cornerRadius = if (trackCornerRadius == -1) trackHeight / 2f else trackCornerRadius.toFloat()

            if (value > valueFrom) {
                canvas.drawRoundRect(
                        trackRectF,
                        cornerRadius,
                        cornerRadius,
                        trackPaint
                )
            }
        }

        drawTrackAfter(canvas, trackRectF, yCenter)
    }

    /**
     * draw secondary track
     * 需要考虑如果使用半透明颜色时会与下层[trackSecondaryColor]颜色进行叠加，需注意叠加后的效果是否满足要求
     */
    private fun drawSecondaryTrack(canvas: Canvas, width: Int, yCenter: Float) {
        trackRectF.set(
                0f + paddingLeft + trackInnerHPadding,
                yCenter - trackHeight / 2f,
                paddingLeft + trackInnerHPadding + thumbOffset * 2 + (trackWidth - thumbOffset * 2) * percentValue(
                        secondaryValue
                ),
                yCenter + trackHeight / 2f
        )

        if (!dispatchDrawSecondaryTrackBefore(canvas, trackRectF, yCenter)) {
            val cornerRadius = if (trackCornerRadius == -1) trackHeight / 2f else trackCornerRadius.toFloat()

            if (secondaryValue > valueFrom) {
                canvas.drawRoundRect(
                        trackRectF,
                        cornerRadius,
                        cornerRadius,
                        trackSecondaryPaint
                )
            }
        }

        drawSecondaryTrackAfter(canvas, trackRectF, yCenter)
    }

    /**
     * draw inactive track
     */
    private fun drawInactiveTrack(canvas: Canvas, width: Int, yCenter: Float) {
        trackRectF.set(
                0f + paddingLeft + trackInnerHPadding,
                yCenter - trackHeight / 2f,
                width.toFloat() - paddingRight - trackInnerHPadding,
                yCenter + trackHeight / 2f
        )

        if (!dispatchDrawInactiveTrackBefore(canvas, trackRectF, yCenter)) {

            val cornerRadius = if (trackCornerRadius == -1) trackHeight / 2f else trackCornerRadius.toFloat()

            canvas.drawRoundRect(
                    trackRectF,
                    cornerRadius,
                    cornerRadius,
                    inactiveTrackPaint
            )
        }

        drawInactiveTrackAfter(canvas, trackRectF, yCenter)
    }

    /**
     * Draw thumb
     * 在[setThumbWithinTrackBounds]模式下，thumb会向内缩进[thumbRadius]距离
     */
    private fun drawThumb(canvas: Canvas, width: Int, yCenter: Float) {

        if (!thumbAnimation.isThumbHidden()) {

            val thumbDrawable = customThumbDrawable ?: defaultThumbDrawable

            val cx = paddingLeft + trackInnerHPadding + thumbOffset + (percentValue(value) * (width - thumbOffset * 2))
            val cy = yCenter - (thumbDrawable.bounds.height() / 2f) + thumbVOffset
            val tx = cx - thumbDrawable.bounds.width() / 2f
            if (!dispatchDrawThumbBefore(canvas, cx, yCenter)) {
                canvas.withTranslation(tx, cy) {
                    thumbDrawable.draw(canvas)
                }

                //draw thumb text if needed
                thumbText?.let {
                    val baseline =
                            yCenter - (thumbTextPaint.fontMetricsInt.bottom + thumbTextPaint.fontMetricsInt.top) / 2
                    canvas.drawText(
                            it,
                            cx,
                            baseline,
                            thumbTextPaint
                    )
                }

            }

            drawThumbAfter(canvas, cx, yCenter)
        }
    }

    /**
     * Draw compat halo
     * 绘制滑块的光环效果
     */
    private fun drawCompatHaloIfNeed(canvas: Canvas, width: Int, yCenter: Float) {
        if (shouldDrawCompatHalo() && enableDrawHalo) {
            val centerX =
                    paddingLeft + trackInnerHPadding + thumbOffset + percentValue(value) * (width - thumbOffset * 2)

            //允许光环绘制到边界以外
            if (parent is ViewGroup) {
                (parent as ViewGroup).clipChildren = false
            }

            canvas.drawCircle(centerX, yCenter, haloRadius.toFloat(), haloPaint)
        }
    }


    /**
     * draw tick
     */
    private fun drawTicks(canvas: Canvas, width: Int, yCenter: Float) {
        if (enableStepMode() && tickVisible) {
            val drawWidth = width - thumbOffset * 2 - tickRadius * 2
            val tickCount: Int = ((valueTo - valueFrom) / stepSize + 1).toInt()
            val stepWidth = drawWidth / (tickCount - 1).toFloat()
            val activeWidth = percentValue(value) * width + paddingLeft + trackInnerHPadding + thumbOffset

            for (i in 0 until tickCount) {
                val starLeft = paddingLeft + trackInnerHPadding + thumbOffset + tickRadius
                val cx = starLeft + i * stepWidth

                val circlePaint = if (cx <= activeWidth) {
                    ticksPaint
                } else {
                    inactiveTicksPaint
                }

                canvas.drawCircle(
                        starLeft + i * stepWidth,
                        yCenter,
                        tickRadius,
                        circlePaint
                )
            }
        }
    }

    /**
     * Returns a number between 0 and 1 with [BaseSlider.value]
     * 通过value返回当前滑动百分比，0为最左、1为最右
     */
    fun percentValue(v: Float = value): Float {
        return (v - valueFrom) / (valueTo - valueFrom)
    }


    /**
     * This method is called before the onDraw.make sure parameter is valid
     * 对可能存在的脏数据进行校验或修正
     */
    private fun validateDirtyData() {
        if (hasDirtyData) {
            validateValueFrom()
            validateValueTo()
            validateValue()
            hasDirtyData = false
        }
    }

    /**
     * 校验[valueFrom]合法性
     */
    private fun validateValueFrom() {
        if (valueFrom > valueTo) {
            throw IllegalStateException("valueFrom($valueFrom) must be smaller than valueTo($valueTo)")
        }
    }

    /**
     * 校验[valueTo]合法性
     */
    private fun validateValueTo() {
        if (valueTo <= valueFrom) {
            throw IllegalStateException("valueTo($valueTo) must be greater than valueFrom($valueFrom)")
        }
    }

    /**
     * 校验[BaseSlider.value]合法性，对不合法数据进行修正
     */
    private fun validateValue() {
        //value 超出起始结束范围则进行修正
        value = MathUtils.clamp(value,valueFrom,valueTo)
        secondaryValue = MathUtils.clamp(secondaryValue,valueFrom,valueTo)
    }

    fun updateViewLayout() {
        updateTrackWidth(width)
        if (viewHeightChanged()) {
            requestLayout()
        } else {
            invalidate()
        }
    }


    /**
     * Returns true if view height changed
     * 检查高度是否发生变化，变化后更新当前记录高度
     */
    fun viewHeightChanged(): Boolean {
        val topBottomPadding = paddingTop + paddingBottom
        val minHeightWithTrack = topBottomPadding + trackHeight
        val minHeightWithThumb = topBottomPadding + thumbRadius * 2 + trackInnerVPadding * 2

        val tempHeight = max(minHeightWithTrack, minHeightWithThumb)

        return if (tempHeight == viewHeight) {
            false
        } else {
            viewHeight = max(tempHeight, sourceViewHeight)
            true
        }
    }

    /**
     * update track real draw width
     * 更新滑轨真实绘制宽度，真实宽度不仅受左右padding影响，还会受内部[trackInnerHPadding]影响
     */
    fun updateTrackWidth(viewWidth: Int) {
        trackWidth = max(viewWidth - paddingLeft - paddingRight - trackInnerHPadding * 2, 0)
    }

    /**
     * Sets the slider's [BaseSlider.value]
     * 如果存在step size时 value 可能会根据step size进行修正
     *
     * @param value 必须小于等于 [valueTo] 大于等于 [valueFrom]
     */
    fun setValue(value: Float) {
        //用户滑动过程禁止改变value
        if (this.value != value && !isDragging) {
            this.value = value
            hasDirtyData = true
            valueChanged(value, false)
            postInvalidate()
        }
    }


    /**
     * Sets the slider's [BaseSlider.secondaryValue]
     *
     * @param secondaryValue 必须小于等于 [valueTo] 大于等于 [valueFrom]
     */
    fun setSecondaryValue(secondaryValue: Float){
        if (this.secondaryValue != secondaryValue){
            this.secondaryValue = secondaryValue
            hasDirtyData = true
            postInvalidate()
        }
    }

    /**
     * Sets the horizontal inner padding of the track.
     * 主要处理thumb超出部分的视图，使thumb展示正常
     * 也可以使用 [BaseSlider.setThumbWithinTrackBounds] 来将thumb直接控制在track内部
     *
     * @see R.attr.trackInnerHPadding
     *
     * @param padding track左右的padding值，
     */
    fun setTrackInnerHPadding(padding: Int = -1) {
        val innerHPadding = if (padding == -1) {
            if (isThumbWithinTrackBounds) {
                //thumb with in track bounds 模式下只需要要考虑超出阴影视图
                kotlin.math.ceil(thumbElevation).toInt()
            } else {
                thumbRadius + kotlin.math.ceil(thumbElevation).toInt()
            }

        } else {
            padding
        }

        if (innerHPadding == trackInnerHPadding) {
            return
        }

        trackInnerHPadding = innerHPadding
        updateViewLayout()
    }

    /**
     * Sets the vertical inner padding of the track.
     * 主要处理thumb阴影超出部分的视图，使thumb展示正常
     *
     * @see R.attr.trackInnerVPadding
     *
     * @param padding track左右的padding值，
     */
    fun setTrackInnerVPadding(padding: Int) {
        val innerVPadding = if (padding == -1) {
            ceil(thumbElevation).toInt()
        } else {
            padding
        }

        if (innerVPadding == trackInnerVPadding) {
            return
        }

        trackInnerVPadding = innerVPadding
        updateViewLayout()
    }


    /**
     * Sets the radius of the track corners.
     *
     * 设置滑轨转角圆角值
     * @see R.attr.trackCornersRadius
     *
     * @param radius 圆角半径
     */
    fun setTrackCornersRadius(@IntRange(from = 0) @Dimension radius: Int) {
        if (radius == trackCornerRadius) {
            return
        }
        trackCornerRadius = radius
        postInvalidate()
    }

    /**
     * Sets the color for the track
     *
     * @see R.attr.trackColor
     */
    fun setTrackTintList(color: ColorStateList) {
        if (this::trackColor.isInitialized && color == trackColor) {
            return
        }
        trackColor = color
        trackPaint.color = getColorForState(trackColor)
        invalidate()
    }

    /**
     * Sets the color for the secondary track
     *
     * @see R.attr.trackSecondaryColor
     *
     * eg.视频滑动进度条可能存在缓存进度,通过此方法来改变二级滑轨颜色
     */
    fun setTrackSecondaryTintList(color: ColorStateList) {
        if (this::trackSecondaryColor.isInitialized && color == trackSecondaryColor) {
            return
        }
        trackSecondaryColor = color
        trackSecondaryPaint.color = getColorForState(trackSecondaryColor)
        invalidate()
    }

    /**
     * Sets the inactive color for the track
     *
     * @see R.attr.trackColorInactive
     */
    fun setTrackInactiveTintList(color: ColorStateList) {
        if (this::trackColorInactive.isInitialized && color == trackColorInactive) {
            return
        }
        trackColorInactive = color
        inactiveTrackPaint.color = getColorForState(trackColorInactive)
        invalidate()
    }

    /**
     * Sets the color for the tick
     *
     * @see R.attr.ticksColor
     */
    fun setTicksTintList(color: ColorStateList) {
        if (this::ticksColor.isInitialized && color == ticksColor) {
            return
        }
        ticksColor = color
        ticksPaint.color = getColorForState(ticksColor)
        invalidate()
    }

    /**
     * Sets the inactive color for the tick
     *
     * @see R.attr.ticksColorInactive
     */
    fun setTicksInactiveTintList(color: ColorStateList) {
        if (this::ticksColorInactive.isInitialized && color == ticksColorInactive) {
            return
        }
        ticksColorInactive = color
        inactiveTicksPaint.color = getColorForState(ticksColorInactive)
        invalidate()
    }

    /**
     * Sets the radius of the tick in pixels.
     * 设置刻度半径大小
     *
     * @see R.attr.tickRadius
     */
    fun setTickRadius(@FloatRange(from = 0.0) @Dimension tickRadius: Float) {
        if (this.tickRadius != tickRadius) {
            this.tickRadius = tickRadius
            postInvalidate()
        }
    }

    /**
     * Sets the text of the thumb
     *
     * @see R.attr.thumbText
     */
    fun setThumbText(text: String?) {
        if (this.thumbText != text) {
            this.thumbText = text
            postInvalidate()
        }
    }

    /**
     * Sets the radius of the thumb in pixels.
     * 设置滑块半径大小
     * 如果使用自定义drawable时为长边半径
     *
     * @see R.attr.thumbRadius
     *
     * @param radius 滑块半径
     */
    var thumbRadius = 0
        set(@IntRange(from = 0) @Dimension radius) {
            if (field == radius) {
                return
            }
            field = radius
            defaultThumbDrawable.shapeAppearanceModel =
                    ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, radius.toFloat()).build()
            adjustThumbDrawableBounds(radius)
            updateViewLayout()
        }


    /**
     * Sets the width and height of the thumb.this conflicts with the [thumbRadius]
     * 设置滑块宽高
     * 不适用于自定义thumb drawable
     *
     * @see R.attr.thumbWidth
     * @see R.attr.thumbHeight
     *
     * @param radius 滑块半径
     */
    fun setThumbWidthAndHeight(thumbWidth: Int, thumbHeight: Int, radius: Int = thumbRadius) {
        if ((this.thumbWidth == thumbWidth && this.thumbHeight == thumbHeight) || (thumbHeight < 0 && thumbWidth <= 0)) {
            return
        }
        if (thumbWidth >= 0) {
            this.thumbWidth = thumbWidth
        } else {
            this.thumbWidth = thumbRadius * 2
        }

        if (thumbHeight >= 0) {
            this.thumbHeight = thumbHeight
        } else {
            this.thumbHeight = thumbRadius * 2
        }

        if (radius != thumbRadius) {
            defaultThumbDrawable.shapeAppearanceModel =
                    ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, radius.toFloat()).build()
        }

        defaultThumbDrawable.setBounds(
                0,
                0,
                this.thumbWidth,
                this.thumbHeight
        )
        updateViewLayout()
    }


    /**
     * Sets the vertical offset of the thumb
     * 设置thumb纵向的偏移量
     *
     * @see R.attr.thumbVOffset
     *
     * @param offset 偏移量
     */
    fun setThumbVOffset(offset: Int) {
        if (offset == thumbVOffset) {
            return
        }
        thumbVOffset = offset
        postInvalidate()
    }

    /**
     * Sets whether the thumb within track bounds
     * 正常模式下滑块thumb是以track的起始位置为中心,thumb较大时左半部分会超出视图边界
     * 某些样式下，thumb需要控制在track的范围以内，可通过此方法来启用此项功能
     *
     * @see R.attr.thumbWithinTrackBounds
     *
     * @param isInBounds thumb 是否需要绘制在 track 范围以内
     */
    fun setThumbWithinTrackBounds(isInBounds: Boolean) {

        isThumbWithinTrackBounds = isInBounds

        val offset = if (isInBounds) {
            //启用状态下直接使用thumb的半径做为向内偏移的具体数值
            thumbRadius
        } else {
            0
        }

        if (thumbOffset == offset) {
            return
        }
        thumbOffset = offset
        setTrackInnerHPadding()
        updateViewLayout()
    }

    /**
     * Sets the color of the thumb.
     *
     * @see R.attr.thumbColor
     */
    fun setThumbTintList(thumbColor: ColorStateList) {
        if (thumbColor == defaultThumbDrawable.fillColor) {
            return
        }
        defaultThumbDrawable.fillColor = thumbColor
        invalidate()
    }

    /**
     * Sets the color of the thumb text.
     *
     * @see R.attr.thumbTextColor
     */
    fun setThumbTextTintList(color: ColorStateList?) {
        if (color != null) {
            if (this::thumbTextColor.isInitialized && thumbTextColor == color) {
                return
            }
            thumbTextColor = color
            thumbTextPaint.color = getColorForState(thumbTextColor)
            invalidate()
        }
    }

    /**
     * Sets the text size of the thumb text.
     *
     * @see R.attr.thumbTextSize
     */
    fun setThumbTextSize(size: Float) {
        if (thumbTextPaint.textSize != size) {
            thumbTextPaint.textSize = size
            invalidate()
        }
    }

    fun setThumbTextBold(isBold: Boolean) {
        if (thumbTextPaint.isFakeBoldText != isBold) {
            thumbTextPaint.isFakeBoldText = isBold
            invalidate()
        }
    }


    fun setThumbCustomDrawable(@DrawableRes drawableResId: Int) {
        ContextCompat.getDrawable(context, drawableResId)?.also {
            setThumbCustomDrawable(it)
        }
    }

    fun setThumbCustomDrawable(drawable: Drawable) {
        customThumbDrawable = initializeCustomThumbDrawable(drawable)
        postInvalidate()
    }


    /**
     * Sets the color of the halo.
     * 设置滑块点击后光环颜色
     *
     * @see R.attr.haloColor
     */
    fun setHaloTintList(haloColor: ColorStateList) {
        if (this::haloColor.isInitialized && this.haloColor == haloColor) {
            return
        }

        this.haloColor = haloColor
        //v23以下通过绘制实现，仅修改画笔颜色即可
        if (!shouldDrawCompatHalo() && background is RippleDrawable) {
            (background as RippleDrawable).setColor(haloColor)
            return
        }

        haloPaint.apply {
            color = getColorForState(haloColor)
            alpha = HALO_ALPHA
        }

        invalidate()

    }

    /**
     * Sets the radius of the halo in pixels.
     * 设置滑块点击后光环的半径
     *
     * @see R.attr.haloRadius
     */
    fun setHaloRadius(@IntRange(from = 0) @Dimension radius: Int) {
        if (haloRadius == radius) {
            return
        }

        haloRadius = radius
        //v23以下通过绘制实现，v23以上通过hook ripple effect background来修改半径
        if (!shouldDrawCompatHalo() && enableDrawHalo && background is RippleDrawable) {
            hookRippleRadius(background as RippleDrawable, haloRadius)
            return
        }
        postInvalidate()
    }


    /**
     * Sets the elevation of the thumb.
     *
     * @see R.attr.thumbElevation
     */
    fun setThumbElevation(elevation: Float) {
        defaultThumbDrawable.elevation = elevation

        thumbElevation = elevation
    }

    /**
     * Sets the stroke color for the thumbs
     *
     * @see R.attr.thumbStrokeColor
     */
    fun setThumbStrokeColor(thumbStrokeColor: ColorStateList?) {
        defaultThumbDrawable.strokeColor = thumbStrokeColor
        postInvalidate()
    }

    /**
     * Sets the stroke width for the thumb
     *
     * @see R.attr.thumbStrokeWidth
     */
    fun setThumbStrokeWidth(thumbStrokeWidth: Float) {
        defaultThumbDrawable.strokeWidth = thumbStrokeWidth
        postInvalidate()
    }

    /**
     * Sets the shadow width for the thumb
     *
     * @see R.attr.thumbShadowColor
     */
    fun setThumbShadowColor(@ColorInt shadowColor: Int) {
        if (shadowColor == Color.TRANSPARENT){
            defaultThumbDrawable.shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_NEVER
        }else{
            defaultThumbDrawable.shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
            defaultThumbDrawable.setShadowColor(shadowColor)
        }
    }


    /**
     * Sets whether the halo should be draw
     * 启用光环效果
     *
     * @see R.attr.enableDrawHalo
     *
     * @param enable True if this enable draw halo
     */
    fun setEnableDrawHalo(enable: Boolean) {
        enableDrawHalo = enable
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && haloDrawable == null && enable) {
            background = ContextCompat.getDrawable(context, R.drawable.halo_background)
            haloDrawable = background as RippleDrawable
        }
    }

    /**
     * Sets whether the tip view are visible
     *
     * @see R.attr.tipViewVisible
     */
    fun setTipViewVisibility(visibility: Boolean){
        if (isShowTipView == visibility){
            return
        }
        isShowTipView = visibility
        if (visibility) {
            tipView.attachTipView(this)
        }
    }

    /**
     * Sets the tip view vertical offset
     *
     * Default is thumb radius + [TipViewContainer.defaultSpace]
     */
    fun setTipVerticalOffset(offset: Int) {
        if (offset != 0) {
            tipView.verticalOffset = offset
        }
    }

    /**
     * Sets the tip view background color
     *
     * @see R.attr.tipViewBackground
     */
    fun setTipBackground(@ColorInt color:Int){
        tipView.setTipBackground(color)
    }

    /**
     * Sets the tip view text color
     *
     * @see R.attr.tipViewTextColor
     */
    fun setTipTextColor(@ColorInt color:Int){
        tipView.setTipTextColor(color)
    }


    /**
     * Sets the tip text auto change
     *
     * @see R.attr.tipTextAutoChange
     */
    fun setTipTextAutoChange(isAutoChange:Boolean){
        tipView.isTipTextAutoChange = isAutoChange
    }

    /**
     *  Sets whether the tip view will be fully within the bounds
     *
     *  是否将tip view 始终限制在屏幕内 ，默认为 false , tip view将根据滑块位置来计算真实位置，可能会移动到屏幕外
     *
     *  @see R.attr.isTipViewClippingEnabled
     */
    fun setTipViewClippingEnabled(enable :Boolean){
        tipView.isClippingEnabled = enable
    }


    /**
     * Add a custom tip view
     */
    fun addCustomTipView(view: View){
        tipView.customTipView = view
    }

    /**
     * Create tip view show/hide animation
     */
    fun createTipAnimation(animator: TipViewAnimator){
        tipView.animator = animator
    }

    /**
     * Returns true if step mode enable
     * 是否启用了刻度功能
     */
    fun enableStepMode(): Boolean {
        return stepSize > 0
    }

    /**
     * Update halo Hotspot coordinate
     *
     * 仅在v23及以上生效，更新ripple effect坐标
     */
    fun shouldDrawCompatHalo(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || background !is RippleDrawable
    }


    @ColorInt
    fun getColorForState(colorStateList: ColorStateList): Int {
        return colorStateList.getColorForState(drawableState, colorStateList.defaultColor)
    }


    /**
     * Get the thumb center x-coordinates
     */
    fun getThumbCenterX():Float{
        return paddingLeft + trackInnerHPadding + thumbOffset + (percentValue(value) * (trackWidth - thumbOffset * 2))
    }

    /**
     * Get the thumb center y-coordinates
     */
    fun getThumbCenterY():Float{
        return measuredHeight/2f + thumbVOffset
    }

    /**
     * show thumb on sliders
     *
     * @param animated Whether to update the thumb visibility with the animation
     * @param delayMillis The delay the show thumb Runnable will be executed
     */
    fun showThumb(animated:Boolean = true,delayMillis:Long = 0){
        thumbAnimation.show(animated,delayMillis)
    }

    /**
     * hide thumb on sliders
     *
     * @param animated Whether to update the thumb visibility with the animation
     * @param delayMillis The delay the hide thumb Runnable will be executed
     */
    fun hideThumb(animated:Boolean = true,delayMillis:Long = 0){
        thumbAnimation.hide(animated,delayMillis)
    }


    fun toggleThumbVisibility(animated:Boolean = true){
        thumbAnimation.toggle(animated)
    }

    /**
     * 如果启用了刻度功能，对当前滑动坐标进行修改，定位到临近刻度
     */
    private fun snapStepPos(pos: Float): Float {
        if (enableStepMode()) {
            val stepCount = ((valueTo - valueFrom) / stepSize).toInt()
            return (pos * stepCount).roundToInt() / stepCount.toFloat()
        }
        return pos
    }

    /**
     * 通过当前滑动位置百分比转化为准确的value值
     * 起始、结束值可能存在多种类型
     *
     * eg.
     * valueFrom = -40  valueTo = -20
     * valueFrom = -1  valueTo = 1
     * valueFrom = 1  valueTo = 100
     * valueFrom = 50  valueTo = 80
     *
     */
    private fun getValueByTouchPos(pos: Float): Float {
        val position = snapStepPos(pos)
        return position * (valueTo - valueFrom) + valueFrom
    }


    /**
     * 通过当前坐标获取滑动位置百分比
     */
    private fun getTouchPosByX(touchX: Float): Float {
        return MathUtils.clamp((touchX - paddingLeft - trackInnerHPadding) / trackWidth, 0f, 1f)
    }

    /**
     * 是否在纵向滚动的容器中
     * 此情况下需要对touch event做特殊处理
     */
    private fun isInVerticalScrollingContainer(): Boolean {
        var p = parent
        while (p is ViewGroup) {
            val parent = p
            val canScrollVertically = parent.canScrollVertically(1) || parent.canScrollVertically(-1)
            if (canScrollVertically && parent.shouldDelayChildPressedState()) {
                return true
            }
            p = p.getParent()
        }
        return false
    }


    private fun initializeCustomThumbDrawable(originalDrawable: Drawable): Drawable? {
        val drawable = originalDrawable.mutate()
        if (drawable != null) {
            adjustCustomThumbDrawableBounds(drawable)
        }
        return drawable
    }


    private fun adjustThumbDrawableBounds(radius:Int){
        defaultThumbDrawable.setBounds(
                0,
                0,
                radius * 2,
                radius * 2
        )

        customThumbDrawable?.let {
            adjustCustomThumbDrawableBounds(it,radius)
        }
    }


    private fun adjustCustomThumbDrawableBounds(drawable: Drawable,radius: Int = thumbRadius) {
        val thumbDiameter = radius * 2
        val originalWidth = drawable.intrinsicWidth
        val originalHeight = drawable.intrinsicHeight
        if (originalWidth == -1 && originalHeight == -1) {
            drawable.setBounds(0, 0, thumbDiameter, thumbDiameter)
        } else {
            val scaleRatio = thumbDiameter.toFloat() / max(originalWidth, originalHeight)
            drawable.setBounds(
                    0, 0, (originalWidth * scaleRatio).toInt(), (originalHeight * scaleRatio).toInt()
            )
        }
    }


    /**
     * Start drag slider
     */
    private fun startTacking(event: MotionEvent) {
        isTackingStart = true
        onStartTacking()
        tipView.show()
    }

    /**
     * stop drag slider
     */
    private fun stopTacking(event: MotionEvent) {
        if (isTackingStart) {
            onStopTacking()
        }
        isTackingStart = false
        tipView.hide()
        invalidate()
    }

    private fun valueChanged(value: Float, fromUser: Boolean, touchX: Float = 0f, touchRawX: Float = 0f) {
        onValueChanged(value, fromUser)
        tipView.onLocationChanged(getThumbCenterX(),getThumbCenterY(),value)

    }


    private fun updateHaloHotspot() {
        if (enableDrawHalo) {
            if (!shouldDrawCompatHalo() && measuredWidth > 0) {
                if (background is RippleDrawable) {
                    val haloX =
                            (paddingLeft + trackInnerHPadding + thumbOffset + (percentValue(value) * (trackWidth - thumbOffset * 2)).toInt())
                    val haloY = viewHeight / 2

                    DrawableCompat.setHotspotBounds(
                            background,
                            haloX - haloRadius,
                            haloY - haloRadius,
                            haloX + haloRadius,
                            haloY + haloRadius
                    )
                }
            }
        }
    }


    private fun trackTouchEvent(event: MotionEvent) {
        val touchPos = getTouchPosByX(event.x)
        val touchValue = getValueByTouchPos(touchPos)
        if (this.value != touchValue) {
            value = touchValue
            valueChanged(value, true,event.x,event.rawX)
            updateHaloHotspot()
            invalidate()
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        val currentX = event.x

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = currentX

                if (isInVerticalScrollingContainer()) {
                    //在纵向滑动布局中不处理down事件，优先外层滑动
                } else {
                    parent.requestDisallowInterceptTouchEvent(true)
                    requestFocus()
                    isDragging = true
                    startTacking(event)
                    trackTouchEvent(event)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDragging) {
                    if (isInVerticalScrollingContainer() && abs(currentX - touchDownX) < scaledTouchSlop) {
                        return false
                    }
                    parent.requestDisallowInterceptTouchEvent(true)
                    startTacking(event)
                }

                isDragging = true
                trackTouchEvent(event)

            }
            MotionEvent.ACTION_UP -> {
                isDragging = false

                lastTouchEvent?.let {
                    if (it.action == MotionEvent.ACTION_DOWN && isClickTouch(it, event)) {
                        startTacking(event)
                        trackTouchEvent(event)
                    }
                }

                stopTacking(event)

            }
            MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                stopTacking(event)
            }
        }

        isPressed = isDragging
        lastTouchEvent = MotionEvent.obtain(event)
        return true
    }


    /**
     * Returns true if current touch event is click event
     *
     * @param startEvent 滑动过程的down事件
     * @param endEvent   滑动过程的up事件
     */
    private fun isClickTouch(startEvent: MotionEvent, endEvent: MotionEvent): Boolean {
        val differenceX = abs(startEvent.x - endEvent.x)
        val differenceY = abs(startEvent.y - endEvent.y)
        return !(differenceX > scaledTouchSlop || differenceY > scaledTouchSlop)
    }

    private fun hookRippleRadius(drawable: RippleDrawable, radius: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            drawable.radius = radius
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val setMaxRadiusMethod =
                        RippleDrawable::class.java.getDeclaredMethod("setMaxRadius", Int::class.javaPrimitiveType)
                setMaxRadiusMethod.invoke(drawable, radius)
            } catch (e: NoSuchMethodException) {
                throw IllegalStateException("Couldn't set RippleDrawable radius", e)
            } catch (e: InvocationTargetException) {
                throw IllegalStateException("Couldn't set RippleDrawable radius", e)
            } catch (e: IllegalAccessException) {
                throw IllegalStateException("Couldn't set RippleDrawable radius", e)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val sliderState = SavedState(superState)
        sliderState.value = value
        sliderState.secondaryValue = secondaryValue
        return sliderState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val sliderState = state as SavedState
        super.onRestoreInstanceState(sliderState.superState)
        value = sliderState.value
        secondaryValue = sliderState.secondaryValue
    }


    internal class SavedState : BaseSavedState {
        var value = 0f
        var secondaryValue = 0f

        constructor(superState: Parcelable?) : super(superState) {}

        constructor(parcel: Parcel) : super(parcel) {
            value = parcel.readFloat()
            secondaryValue = parcel.readFloat()
        }


        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeFloat(value)
            parcel.writeFloat(secondaryValue)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }

    }


}