package `in`.testpress.testpress.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

class DonutProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var progress: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    var strokeWidthPx: Float = 6.5f * resources.displayMetrics.density
        set(value) {
            field = value
            invalidate()
        }

    var progressColor: Int = Color.parseColor("#F59E0B")
        set(value) {
            field = value
            invalidate()
        }

    var trackColor: Int = Color.parseColor("#F1F5F9")
        set(value) {
            field = value
            invalidate()
        }

    var text: String = ""
        set(value) {
            field = value
            invalidate()
        }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val rectF = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val d = Math.min(width, height).toFloat()
        if (d <= 0f) return
        val pad = strokeWidthPx / 2f + 2f
        rectF.set(pad, pad, d - pad, d - pad)

        trackPaint.color = trackColor
        trackPaint.strokeWidth = strokeWidthPx
        canvas.drawArc(rectF, 0f, 360f, false, trackPaint)

        progressPaint.color = progressColor
        progressPaint.strokeWidth = strokeWidthPx
        val sweep = (progress / 100f).coerceIn(0f, 1f) * 360f
        canvas.drawArc(rectF, -90f, sweep, false, progressPaint)

        textPaint.color = progressColor
        textPaint.textSize = d * 0.22f
        val yPos = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(text, width / 2f, yPos, textPaint)
    }
}
