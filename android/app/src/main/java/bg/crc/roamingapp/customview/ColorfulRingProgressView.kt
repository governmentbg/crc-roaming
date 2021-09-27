package bg.crc.roamingapp.customview

import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import bg.crc.roamingapp.R

internal class ColorfulRingProgressView(private val mContext: Context, attrs: AttributeSet?) :
    View(mContext, attrs) {
    private var mPercent = 75f
    private var mStrokeWidth = 0f
    private var mBgBorderColor = -0x1e1e1f
    private var mBgFillColor = Color.parseColor("#F5F5F5")
    private var mStartAngle = 0f
    private var mFgColorStart = -0x1c00
    private var mFgColorEnd = -0xb800
    private var mTextColor = -0x1c00
    private var mShader: LinearGradient? = null
    private var mOval: RectF? = null
    private var mPaint: Paint? = null
    private var animator: ObjectAnimator? = null
    private val textPaint = Paint()
    private var paddingOnProgress = 3f
    private var isHidePercent = false
    private var hideBorder = false
    private fun init() {
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeWidth = mStrokeWidth
        mPaint!!.strokeCap = Paint.Cap.SQUARE


        textPaint.color = mTextColor
        textPaint.textSize = 30f
        textPaint.textAlign = Paint.Align.CENTER
    }

    private fun dp2px(dp: Float): Int {
        return (mContext.resources.displayMetrics.density * dp + 0.5f).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint!!.shader = null
        mPaint!!.color = mBgBorderColor

        //Draw outer circle for border
        mPaint!!.strokeWidth = strokeWidth + paddingOnProgress
        canvas.drawArc(mOval!!, 0f, 360f, false, mPaint!!)

        //Draw inner white portion
        mPaint!!.strokeWidth = strokeWidth
        mPaint!!.color = mBgFillColor
        canvas.drawArc(mOval!!, 0f, 360f, false, mPaint!!)

        //Paint shade of progress
        mPaint!!.strokeWidth = strokeWidth - paddingOnProgress
        mPaint!!.shader = mShader
        canvas.drawArc(mOval!!, mStartAngle, mPercent * 3.6f, false, mPaint!!)


        if (!isHidePercent) {
            //Show percentages of progress
            textPaint.textSize = height / 5f
            val yPos = (height / 2 - (textPaint.descent() + textPaint.ascent()) / 2).toInt()
            canvas.drawText(
                percent.toInt().toString() + "%",
                width / 2.toFloat(),
                yPos.toFloat(),
                textPaint
            )
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateOval()

        //mShader = SweepGradient(mOval!!.left,mOval!!.top, intArrayOf(mFgColorStart,mFgColorEnd),floatArrayOf(1.0f,1.20f))

        mShader = LinearGradient(
            mOval!!.left,
            mOval!!.top,
            mOval!!.left,
            mOval!!.bottom,
            mFgColorStart,
            mFgColorEnd,
            Shader.TileMode.MIRROR
        )
    }

    var percent: Float
        get() = mPercent
        set(mPercent) {
            this.mPercent = mPercent
            refreshTheLayout()
        }

    var strokeWidth: Float
        get() = mStrokeWidth
        set(mStrokeWidth) {
            this.mStrokeWidth = mStrokeWidth
            mPaint!!.strokeWidth = mStrokeWidth
            updateOval()
            refreshTheLayout()
        }

    private fun updateOval() {
        val xp = paddingLeft + paddingRight
        val yp = paddingBottom + paddingTop
        mOval = RectF(
            paddingLeft + mStrokeWidth,
            paddingTop + mStrokeWidth,
            paddingLeft + (width - xp) - mStrokeWidth,
            paddingTop + (height - yp) - mStrokeWidth
        )
    }

    fun setStrokeWidthDp(dp: Float) {
        mStrokeWidth = dp2px(dp).toFloat()
        mPaint!!.strokeWidth = mStrokeWidth
        updateOval()
        refreshTheLayout()
    }

    fun refreshTheLayout() {
        invalidate()
        requestLayout()
    }

    var fgColorStart: Int
        get() = mFgColorStart
        set(mFgColorStart) {
            this.mFgColorStart = mFgColorStart
            //mShader = SweepGradient(mOval!!.left,mOval!!.top, intArrayOf(mFgColorStart,mFgColorEnd),floatArrayOf(0f,1f))
            mShader = LinearGradient(
                mOval!!.left,
                mOval!!.top,
                mOval!!.left,
                mOval!!.bottom,
                mFgColorStart,
                mFgColorEnd,
                Shader.TileMode.MIRROR
            )
            refreshTheLayout()
        }

    var fgColorEnd: Int
        get() = mFgColorEnd
        set(mFgColorEnd) {
            this.mFgColorEnd = mFgColorEnd
            //mShader = SweepGradient(mOval!!.left,mOval!!.top, intArrayOf(mFgColorStart,mFgColorEnd),floatArrayOf(0f,1f))
            mShader = LinearGradient(
                mOval!!.left,
                mOval!!.top,
                mOval!!.left,
                mOval!!.bottom,
                mFgColorStart,
                mFgColorEnd,
                Shader.TileMode.MIRROR
            )
            refreshTheLayout()
        }

    var startAngle: Float
        get() = mStartAngle
        set(mStartAngle) {
            this.mStartAngle = mStartAngle + 270
            refreshTheLayout()
        }

    @JvmOverloads
    fun animateIndeterminate(
        durationOneCircle: Int = 800,
        interpolator: TimeInterpolator? = AccelerateDecelerateInterpolator()
    ) {
        animator = ObjectAnimator.ofFloat(this, "startAngle", startAngle, startAngle + 360)
        if (interpolator != null) animator!!.interpolator = interpolator
        animator!!.duration = durationOneCircle.toLong()
        animator!!.repeatCount = ValueAnimator.INFINITE
        animator!!.repeatMode = ValueAnimator.RESTART
        animator!!.start()
    }

    fun stopAnimateIndeterminate() {
        if (animator != null) {
            animator!!.cancel()
            animator = null
        }
    }

    init {
        val a =
            mContext.theme.obtainStyledAttributes(attrs, R.styleable.ColorfulRingProgressView, 0, 0)
        try {
            mBgBorderColor =
                a.getColor(R.styleable.ColorfulRingProgressView_bgBorderColor, -0x1e1e1f)
            mFgColorEnd = a.getColor(R.styleable.ColorfulRingProgressView_fgColorEnd, -0xb800)
            mFgColorStart = a.getColor(R.styleable.ColorfulRingProgressView_fgColorStart, -0x1c00)
            mTextColor = a.getColor(R.styleable.ColorfulRingProgressView_textColor, -0x1c00)
            mPercent = a.getFloat(R.styleable.ColorfulRingProgressView_percent, 75f)
            mBgFillColor =
                a.getColor(R.styleable.ColorfulRingProgressView_bgFillColor, mBgFillColor)
            isHidePercent =
                a.getBoolean(R.styleable.ColorfulRingProgressView_hideText, isHidePercent)
            hideBorder = a.getBoolean(R.styleable.ColorfulRingProgressView_hideBorder, hideBorder)
            mStartAngle = a.getFloat(R.styleable.ColorfulRingProgressView_startAngle, 0f) + 270
            mStrokeWidth =
                a.getDimensionPixelSize(R.styleable.ColorfulRingProgressView_strokeWidth, dp2px(6f))
                    .toFloat()
            if (hideBorder) {
                paddingOnProgress = 0f
            }
        } finally {
            a.recycle()
        }
        init()
    }
}