package io.cyonic.loadingball

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import java.lang.Float.max
import java.lang.Float.min

class LoadingBall @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    var innerRadius: Float = Float.MIN_VALUE
    var outerRadius: Float = Float.MIN_VALUE

    var rotationDelta: Float = Float.MIN_VALUE
    var currentRotation: Float = Float.MIN_VALUE

    var sweepHead: Float = 0f
    var sweepTail: Float = 0f

    var wiperRotationMatrix: Matrix = Matrix()
    var sweepMatrix: Matrix = Matrix()

    val topSweepPath: Path = Path()
    val bottomSweepPath: Path = Path()
    val wipersPath: Path = Path()

    val paintBlack: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.black)
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }

    val paintRed: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.red)
        style = Paint.Style.FILL
    }

    val paintWhite: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.white)
        style = Paint.Style.FILL
    }

    val animator: ValueAnimator = ValueAnimator.ofInt(0, 3599).apply {
        repeatCount = ValueAnimator.INFINITE
        duration = 2000
        addUpdateListener { update(it.animatedValue as Int) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        prepareForDraw(measuredWidth, measuredHeight)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {

            drawPath(topSweepPath, paintRed)
            drawPath(bottomSweepPath, paintWhite)

            drawPath(wipersPath, paintBlack)

            drawCircle(width / 2f, height / 2f, innerRadius, paintBlack)
            drawCircle(width / 2f, height / 2f, (innerRadius - STROKE_WIDTH / 2f)+3f, paintWhite)
        }
    }

    private fun prepareForDraw(width: Int, height: Int) {
        val radius: Float = (if (width > height) height / 2f else width / 2f)

        outerRadius = radius - 1f
        innerRadius = outerRadius / 3.5f

        wipersPath.moveTo(.5f, height / 2f)
        wipersPath.lineTo(width.toFloat() - 1f, height / 2f)

    }

    private fun update(newRotation: Int) {

        (newRotation.toFloat() / 10f).let { rotation ->
            rotationDelta = rotation - currentRotation
            currentRotation = rotation
        }

        when (newRotation) {
            in 0..1799 -> {
                sweepTail = 0f
                sweepHead = currentRotation

            }
            in 1800..3599 -> {
                sweepTail = min(sweepTail + rotationDelta * 2f, 360f)
                sweepHead = max(currentRotation - sweepTail, 0f)
            }
        }

        wiperRotationMatrix.setRotate(rotationDelta, width / 2f, height / 2f)
        wipersPath.transform(wiperRotationMatrix)

        bottomSweepPath.reset()
        bottomSweepPath.moveTo(width / 2f, height / 2f)
        bottomSweepPath.arcTo(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            sweepTail,
            sweepHead,
            false
        )

        topSweepPath.reset()
        topSweepPath.moveTo(width / 2f, height / 2f)
        topSweepPath.arcTo(0f, 0f, width.toFloat(), height.toFloat(), sweepTail, sweepHead, false)

        sweepMatrix.setRotate(180f, width / 2f, height / 2f)
        topSweepPath.transform(sweepMatrix)

        invalidate()
    }

    fun start() {
        animator.start()
    }

    companion object {
        const val STROKE_WIDTH: Float = 16f
    }
}