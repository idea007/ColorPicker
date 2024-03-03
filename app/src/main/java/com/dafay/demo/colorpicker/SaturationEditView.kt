package com.dafay.demo.colorpicker

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewTreeObserver.OnScrollChangedListener
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import com.dafay.demo.lib.base.utils.dp2px
import com.example.demo.live.wallpaper.utils.toHexRGB
import com.google.android.material.animation.AnimationUtils
import com.google.android.material.internal.DescendantOffsetUtils
import com.google.android.material.internal.ThemeEnforcement
import com.google.android.material.internal.ViewUtils
import com.google.android.material.motion.MotionUtils
import com.google.android.material.tooltip.TooltipDrawable


/**
 * <a herf="https://www.jianshu.com/p/75cc93195f7a?from=jiantop.com">
 */
/**
 * 饱和度(Saturation) + 明度(Value)
 */
class SaturationEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.tooltipDrawableViewStyle
) : View(context, attrs, defStyleAttr) {

    private val TAG = SaturationEditView::class.java.simpleName

    private val DEFAULT_LABEL_ANIMATION_ENTER_DURATION = 83
    private val DEFAULT_LABEL_ANIMATION_EXIT_DURATION = 117
    private val LABEL_ANIMATION_ENTER_DURATION_ATTR = com.google.android.material.R.attr.motionDurationMedium4
    private val LABEL_ANIMATION_EXIT_DURATION_ATTR = com.google.android.material.R.attr.motionDurationShort3
    private val LABEL_ANIMATION_ENTER_EASING_ATTR = com.google.android.material.R.attr.motionEasingEmphasizedInterpolator
    private val LABEL_ANIMATION_EXIT_EASING_ATTR = com.google.android.material.R.attr.motionEasingEmphasizedAccelerateInterpolator

    // 拖动的指示器画笔
    private lateinit var thumbPaint: Paint
    private var thumbRadius = 16.dp2px.toFloat()
    private var thumbStrokeWidth = 2.dp2px.toFloat()
    private var thumbLimit = 1.dp2px

    // 饱和度区域画笔
    private lateinit var saturationPaint: Paint
    private var saturationRectF = RectF()

    // 光环画笔
    private lateinit var haloPaint: Paint
    private var haloRippleDrawable: RippleDrawable? = null

    // hsb 的 b
    private var brightnessShader: Shader? = null
    private var saturationShader: Shader? = null


    private var viewWidth = 0f
    private var viewHeight = 0f
    private var centerX = 0f
    private var centerY = 0f

    // 边距
    private var thumbSidePadding = 16.dp2px

    // 当前色相值 [0, 360]
    private var hue = 0f
    private var saturation = 1f
    private var brightness = 1f


    private var labelStyle = 0
    private val labels = ArrayList<TooltipDrawable>()

    private var labelsAreAnimatedIn = false
    private var labelsInAnimator: ValueAnimator? = null
    private var labelsOutAnimator: ValueAnimator? = null

    var listener: OnHsbChangedListener? = null

    private val pointThumb = PointF(0f, 0f)
    private var attachPoint: PointF? = null
    private var thumbIsPressed = false
    private var scaledTouchSlop = 0
    private var haloRadius = 0f
    private val haloAlpha = 0xB3

    // 把手响应范围系数
    private val legalZoom = 1.6f

    private var lastEnvent: MotionEvent? = null

    init {
        processAttributes(context, attrs, defStyleAttr)
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            init()
        }
        Log.i(TAG, "init() 1.dp2px=${1.dp2px}")
    }

    @SuppressLint("RestrictedApi")
    private fun processAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a = ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.TooltipDrawableView,
            defStyleAttr,
            com.google.android.material.R.style.Widget_MaterialComponents_Slider
        )
        labelStyle = a.getResourceId(R.styleable.TooltipDrawableView_labelTooltipStyle, com.google.android.material.R.style.Widget_MaterialComponents_Tooltip)
    }

    @RequiresApi(VERSION_CODES.M)
    private fun init() {
        scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        thumbPaint.isAntiAlias = true
        thumbPaint.color = Color.WHITE
        thumbPaint.style = Paint.Style.STROKE
        thumbPaint.strokeWidth = thumbStrokeWidth

        saturationPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        saturationPaint.isAntiAlias = true

        haloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        haloPaint.style = Paint.Style.FILL
        haloPaint.color = Color.BLUE

        createLabelPool()

        haloRippleDrawable = RippleDrawable(ColorStateList.valueOf(Color.RED), null, null)
    }

    // 创建
    @SuppressLint("RestrictedApi")
    private fun createLabelPool() {
        // Because there's currently no way to copy the TooltipDrawable we use this to make more
        // if more thumbs are added.
        val tooltipDrawable = TooltipDrawable.createFromAttributes(context, null, 0, com.google.android.material.R.style.Widget_Material3_Tooltip)
        labels.add(tooltipDrawable)
        if (ViewCompat.isAttachedToWindow(this)) {
            attachLabelToContentView(tooltipDrawable)
        }
        // Add a stroke if there is more than one label for when they overlap.
        val strokeWidth = if (labels.size == 1) 0 else 1
        for (label in labels) {
            label.strokeWidth = strokeWidth.toFloat()
            label.setRevealFraction(1f)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun attachLabelToContentView(label: TooltipDrawable) {
        label.setRelativeToView(ViewUtils.getContentView(this))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // The label is attached on the Overlay relative to the content.
        for (label in labels) {
            attachLabelToContentView(label)
        }
    }

    override fun onDetachedFromWindow() {
        labelsAreAnimatedIn = false
        for (label in labels) {
            detachLabelFromContentView(label)
        }
        viewTreeObserver.removeOnScrollChangedListener(onScrollChangedListener)
        super.onDetachedFromWindow()

    }

    @SuppressLint("RestrictedApi")
    private val onScrollChangedListener = OnScrollChangedListener {
        if (false && isEnabled) {
            val contentViewBounds = Rect()
            ViewUtils.getContentView(this)!!.getHitRect(contentViewBounds)
            val isSliderVisibleOnScreen = getLocalVisibleRect(contentViewBounds)
            for (i in labels.indices) {
                val label = labels[i]
                // Get associated value for label
                positionLabel(label)
                if (isSliderVisibleOnScreen) {
                    ViewUtils.getContentViewOverlay(this)!!.add(label)
                } else {
                    ViewUtils.getContentViewOverlay(this)!!.remove(label)
                }
            }
        }
    }


    @SuppressLint("RestrictedApi")
    private fun detachLabelFromContentView(label: TooltipDrawable) {
        val contentViewOverlay = ViewUtils.getContentViewOverlay(this)
        if (contentViewOverlay != null) {
            contentViewOverlay.remove(label)
            label.detachView(ViewUtils.getContentView(this))
        }
    }

    @RequiresApi(VERSION_CODES.M)
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.i(TAG, "onSizeChanged(w:$w h:$h oldW:$oldw oldh:$oldh)")

        viewWidth = w.toFloat()
        viewHeight = h.toFloat()
        centerX = viewWidth / 2
        centerY = viewHeight / 2

        haloRadius = thumbRadius * 1.5f

        saturationRectF = RectF(thumbRadius, thumbRadius, w - thumbRadius, h - thumbRadius)
        initSaturationPaint()
        haloRippleDrawable?.radius = haloRadius.toInt()
        updateHaloHotspot()
        adjustPointF(pointThumb, viewWidth, 0f)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastEnvent = MotionEvent.obtain(event)
                if (attachPoint == null) {
                    attachPoint = getLegalControlPoint(event.x, event.y)
                }
                if (attachPoint != null) {
                    thumbIsPressed = true
                    parent.requestDisallowInterceptTouchEvent(true)
                    requestFocus()
                    nodifyHSBListenerByPoint(attachPoint)
                    updateHaloAndLabelsColor()
                    updateHaloHotspot()
                    invalidate()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (!thumbIsPressed) {
                    // Check if we're trying to scroll vertically instead of dragging this Slider
                    if (Math.abs(x - (attachPoint?.x ?: x)) < scaledTouchSlop && Math.abs(y - (attachPoint?.y ?: y)) < scaledTouchSlop) {
                        return false
                    }
                    parent.requestDisallowInterceptTouchEvent(true)
                }

                if (attachPoint != null) {
                    var offsetX = event.x - lastEnvent!!.x
                    var offsetY = event.y - lastEnvent!!.y
                    lastEnvent = MotionEvent.obtain(event)
                    adjustPointF(attachPoint, attachPoint!!.x + offsetX, attachPoint!!.y + offsetY)
                    nodifyHSBListenerByPoint(attachPoint)
                    updateHaloAndLabelsColor()
                    updateHaloHotspot()
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                thumbIsPressed = false
                attachPoint = null
                lastEnvent = null
                updateHaloHotspot()
                invalidate()
            }
        }

        // Set if the thumb is pressed. This will cause the ripple to be drawn.
        isPressed = thumbIsPressed
        return true
    }

    private fun nodifyHSBListenerByPoint(pointF: PointF?) {
        pointF ?: return
        saturation = (pointF.x - saturationRectF.left - thumbLimit) / (saturationRectF.width() - 2 * thumbLimit)
        brightness = 1 - (pointF.y - saturationRectF.top - thumbLimit) / (saturationRectF.height() - 2 * thumbLimit)
        listener?.hsbChanged(hue, saturation, brightness)
    }

    /**
     * 是否按压了 thumb
     * @param x
     * @param y
     * @return
     */
    private fun getLegalControlPoint(x: Float, y: Float): PointF? {
        val rectF = RectF()
        rectF.set(
            pointThumb.x - thumbRadius * legalZoom,
            pointThumb.y - thumbRadius * legalZoom,
            pointThumb.x + thumbRadius * legalZoom,
            pointThumb.y + thumbRadius * legalZoom
        )
        if (rectF.contains(x, y)) {
            return pointThumb
        }
        return null
    }

    private fun updateHaloHotspot() {
        // Set the hotspot as the halo if RippleDrawable is being used.
        if (measuredWidth > 0) {
            val background = background
            if (background is RippleDrawable) {
                val x: Int = pointThumb.x.toInt()
                val y: Int = pointThumb.y.toInt()
                DrawableCompat.setHotspotBounds(
                    background, (x - haloRadius).toInt(), (y - haloRadius).toInt(), (x + haloRadius).toInt(), (y + haloRadius).toInt()
                )
            }
        }
    }

    /**
     * 更改 attachPoint 位置，并发送监听
     */
    private fun adjustPointF(pointF: PointF?, eventX: Float, eventY: Float) {
        var tempX = eventX
        var tempY = eventY
        if (tempX < saturationRectF.left + thumbLimit) {
            tempX = saturationRectF.left + thumbLimit
        } else if (tempX > saturationRectF.right - thumbLimit) {
            tempX = saturationRectF.right - thumbLimit
        }

        if (tempY < saturationRectF.top + thumbLimit) {
            tempY = saturationRectF.top + thumbLimit
        } else if (tempY > saturationRectF.bottom - thumbLimit) {
            tempY = saturationRectF.bottom - thumbLimit
        }
        pointF?.set(tempX, tempY)
    }

    private fun adjustThumbPosition(pointF: PointF?) {
        pointF ?: return
        if (pointF.x < thumbRadius + thumbLimit) {
            pointF.x = thumbRadius + thumbLimit
        }
        if (pointF.x > viewWidth - thumbRadius - thumbLimit) {
            pointF.x = viewWidth - thumbRadius - thumbLimit
        }

        if (pointF.y < thumbRadius + thumbLimit) {
            pointF.y = thumbRadius + thumbLimit
        }

        if (pointF.y > viewHeight - thumbRadius - thumbLimit) {
            pointF.y = viewHeight - thumbRadius - thumbLimit
        }
    }

    // hsb 模式
    private fun initSaturationPaint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            brightnessShader = LinearGradient(
                saturationRectF.left,
                saturationRectF.top,
                saturationRectF.left,
                saturationRectF.bottom,
                -0x1,
                -0x1000000,
                Shader.TileMode.CLAMP
            )


            val colorSat = intArrayOf(Color.parseColor("#ffffff"), Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
            saturationShader =
                LinearGradient(
                    saturationRectF.left,
                    saturationRectF.top,
                    saturationRectF.right,
                    saturationRectF.top,
                    colorSat,
                    null,
                    TileMode.CLAMP
                )
            val multiplyShader = ComposeShader(brightnessShader!!, saturationShader!!, PorterDuff.Mode.MULTIPLY)
            saturationPaint.setShader(multiplyShader)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun updateHaloAndLabelsColor() {
        haloPaint.color = MColorConvertUtils.HSVToColorWithAlpha(haloAlpha, hue, saturation, 0.2f+brightness*0.6f)
        labels.forEach {
            it.fillColor = ColorStateList.valueOf(MColorConvertUtils.HSVToColorWithAlpha(haloAlpha, hue, saturation, 0.2f+brightness*0.6f))
            var color = MColorConvertUtils.HSVToColor(hue, saturation, brightness)
            it.setText(color.toHexRGB)
        }
    }

    fun updateHue(@FloatRange(from = 0.0, to = 360.0) hue: Float) {
        this.hue = hue
        haloPaint.color = MColorConvertUtils.HSVToColorWithAlpha(haloAlpha, hue, saturation, brightness)
        labels.forEach {
            it.fillColor = ColorStateList.valueOf(MColorConvertUtils.HSVToColorWithAlpha(haloAlpha, hue, saturation, brightness))
        }
        initSaturationPaint()
        invalidate()
    }

    fun updateHSB(
        @FloatRange(from = 0.0, to = 360.0) hue: Float,
        @FloatRange(from = 0.0, to = 1.0) saturation: Float,
        @FloatRange(from = 0.0, to = 1.0) brightness: Float
    ) {
        this.hue = hue
        this.brightness = brightness
        this.saturation = saturation

        updateHue(hue)
        updateThumbPosition()
    }

    fun getHub(): Float {
        return hue
    }

    fun getSaturation(): Float {
        return saturation
    }

    fun getBrightness(): Float {
        return brightness
    }

    private fun updateThumbPosition() {

    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.i(TAG, "onDraw()")
        drawSaturationPanel(canvas)

        if ((thumbIsPressed || isFocused) && isEnabled) {
            maybeDrawCompatHalo(canvas)
        }

        drawThumb(canvas)

        // Draw labels if there is an active thumb or the labels are always visible.
        if (attachPoint != null && isEnabled) {
            ensureLabelsAdded()
        } else {
            ensureLabelsRemoved()
        }
    }

    private fun ensureLabelsAdded() {

        // If the labels are not animating in, start an animator to show them. ensureLabelsAdded will
        // be called multiple times by BaseSlider's draw method, making this check necessary to avoid
        // creating and starting an animator for each draw call.
        if (!labelsAreAnimatedIn) {
            labelsAreAnimatedIn = true
            labelsInAnimator = createLabelAnimator(true)
            labelsOutAnimator = null
            labelsInAnimator!!.start()
        }

        labels.forEach {
            setValueForLabel(it)
        }
    }

    private fun ensureLabelsRemoved() {
        // If the labels are animated in or in the process of animating in, create and start a new
        // animator to animate out the labels and remove them once the animation ends.
        if (labelsAreAnimatedIn) {
            labelsAreAnimatedIn = false
            labelsOutAnimator = createLabelAnimator(false)
            labelsInAnimator = null
            labelsOutAnimator!!.addListener(
                object : AnimatorListenerAdapter() {
                    @SuppressLint("RestrictedApi")
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        val contentViewOverlay = ViewUtils.getContentViewOverlay(this@SaturationEditView)
                        for (label in labels) {
                            contentViewOverlay!!.remove(label)
                        }
                    }
                })
            labelsOutAnimator!!.start()
        }
    }


    @SuppressLint("RestrictedApi")
    private fun setValueForLabel(label: TooltipDrawable) {
        var color = MColorConvertUtils.HSVToColor(hue, saturation, brightness)
        label.setText(color.toHexRGB)
        positionLabel(label)
        ViewUtils.getContentViewOverlay(this)?.add(label)
    }

    @SuppressLint("RestrictedApi")
    private fun createLabelAnimator(enter: Boolean): ValueAnimator? {
        var startFraction = if (enter) 0f else 1f
        // Update the start fraction to the current animated value of the label, if any.
        startFraction = getAnimatorCurrentValueOrDefault(
            if (enter) labelsOutAnimator else labelsInAnimator, startFraction
        )
        val endFraction = if (enter) 1f else 0f
        val animator = ValueAnimator.ofFloat(startFraction, endFraction)
        val duration: Int
        val interpolator: TimeInterpolator
        if (enter) {
            duration = MotionUtils.resolveThemeDuration(
                context,
                LABEL_ANIMATION_ENTER_DURATION_ATTR,
                DEFAULT_LABEL_ANIMATION_ENTER_DURATION
            )
            interpolator = MotionUtils.resolveThemeInterpolator(
                context,
                LABEL_ANIMATION_ENTER_EASING_ATTR,
                AnimationUtils.DECELERATE_INTERPOLATOR
            )
        } else {
            duration = MotionUtils.resolveThemeDuration(
                context,
                LABEL_ANIMATION_EXIT_DURATION_ATTR,
                DEFAULT_LABEL_ANIMATION_EXIT_DURATION
            )
            interpolator = MotionUtils.resolveThemeInterpolator(
                context,
                LABEL_ANIMATION_EXIT_EASING_ATTR,
                AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
            )
        }
        animator.duration = duration.toLong()
        animator.interpolator = interpolator
        animator.addUpdateListener { animation ->
            val fraction = animation.animatedValue as Float
            for (label in labels) {
                label.setRevealFraction(fraction)
            }
            // Ensure the labels are redrawn even if the slider has stopped moving
            ViewCompat.postInvalidateOnAnimation(this)
        }
        return animator
    }

    private fun getAnimatorCurrentValueOrDefault(
        animator: ValueAnimator?, defaultValue: Float
    ): Float {
        // If the in animation is interrupting the out animation, attempt to smoothly interrupt by
        // getting the current value of the out animator.
        if (animator != null && animator.isRunning) {
            val value = animator.animatedValue as Float
            animator.cancel()
            return value
        }
        return defaultValue
    }


    private fun maybeDrawCompatHalo(canvas: Canvas) {
        // Only draw the halo for devices that aren't using the ripple.
        if (true) {
            if (VERSION.SDK_INT < VERSION_CODES.P) {
                // In this case we can clip the rect to allow drawing outside the bounds.
                canvas.clipRect(
                    (
                            pointThumb.x - haloRadius),
                    (
                            pointThumb.y - haloRadius),
                    (
                            pointThumb.x + haloRadius),
                    (
                            pointThumb.y + haloRadius),
                    Region.Op.UNION
                )
            }
            canvas.drawCircle(pointThumb.x, pointThumb.y, haloRadius, haloPaint)
        }
    }


    @SuppressLint("RestrictedApi")
    private fun positionLabel(label: TooltipDrawable) {

        val left: Float = pointThumb.x - label.intrinsicWidth / 2
        val top: Float = pointThumb.y - thumbRadius - 16.dp2px
        label.setBounds(left.toInt(), top.toInt() - label.intrinsicHeight, left.toInt() + label.intrinsicWidth, top.toInt())

        // Calculate the difference between the bounds of this view and the bounds of the root view to
        // correctly position this view in the overlay layer.
        val rect = Rect(label.bounds)
        DescendantOffsetUtils.offsetDescendantRect(ViewUtils.getContentView(this)!!, this, rect)
        label.bounds = rect
    }

    private fun drawThumb(canvas: Canvas) {
        canvas.drawCircle(pointThumb.x, pointThumb.y, thumbRadius - 7.dp2px, thumbPaint)
    }

    private fun drawSaturationPanel(canvas: Canvas) {
        canvas.drawRoundRect(saturationRectF, thumbRadius / 2, thumbRadius / 2, saturationPaint)
    }

    interface OnHsbChangedListener {
        fun hsbChanged(h: Float, s: Float, b: Float)
    }
}


