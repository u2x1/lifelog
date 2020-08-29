package com.nutr1t07.lifelog.view


import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.nutr1t07.lifelog.R
import com.nutr1t07.lifelog.data.Event
import com.nutr1t07.lifelog.data.getEventColor
import java.util.*


class EventCircle(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val _padding: Float = 16F
    private var padding = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        _padding,
        resources.displayMetrics
    )
    private val _strokeWidth: Float = 2F
    private var strokeWidth = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        _strokeWidth,
        resources.displayMetrics
    )

    init {
        setWillNotDraw(false)
    }

    private var _events: MutableList<Event> = arrayListOf()
    var events: MutableList<Event>
        set(value) {
            _events = value
            invalidate()
        }
        get() = _events

    private val ovalLeft = RectF()
    private val ovalRight = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val radius: Float = height / 2 - strokeWidth

        // centerX is also the radius of circle
        val centerX1: Float = padding + radius
        val centerX2: Float = width - padding - radius

        val centerY: Float = (height / 2).toFloat()

        val eventRadius = radius * 0.9F

        // draw background circle
        ovalLeft.set(
            centerX1 - radius,
            centerY - radius,
            centerX1 + radius,
            centerY + radius
        )
        ovalRight.set(
            centerX2 - radius,
            centerY - radius,
            centerX2 + radius,
            centerY + radius
        )
        paintBorder.color = context.getColor(R.color.colorEventBlack)
        paintBorder.strokeWidth = strokeWidth
        paintBorder.style = Paint.Style.STROKE



        paint.color = context.getColor(R.color.colorEventLightBlack)
        paint.style = Paint.Style.FILL_AND_STROKE
        canvas?.drawArc(ovalLeft, 0F, 360F, true, paint)
        canvas?.drawArc(ovalRight, 0F, 360F, true, paint)

        // draw circle border
        canvas?.drawArc(ovalLeft, 0F, 360F, true, paintBorder)
        canvas?.drawArc(ovalRight, 0F, 360F, true, paintBorder)

        ovalLeft.set(
            centerX1 - eventRadius,
            centerY - eventRadius,
            centerX1 + eventRadius,
            centerY + eventRadius
        )
        ovalRight.set(
            centerX2 - eventRadius,
            centerY - eventRadius,
            centerX2 + eventRadius,
            centerY + eventRadius
        )

        // draw inner circle border
        canvas?.drawArc(ovalLeft, 0F, 360F, true, paintBorder)
        canvas?.drawArc(ovalRight, 0F, 360F, true, paintBorder)

        for (event in events) {
            paint.color = context.getColor(
                getEventColor(
                    event.type
                )
            )
            val startCal = Calendar.getInstance()
            startCal.time = event.startTime
            var startHour = startCal.get(Calendar.HOUR_OF_DAY)
            val startMin = startCal.get(Calendar.MINUTE)

            val startAngle: Float
            val duration: Long
            val oval: RectF
            when {
                startHour in 6..18 -> {
                    startHour -= 6
                    paint.color = context.getColor(
                        getEventColor(
                            event.type
                        )
                    )
                    startAngle = startHour * 30 + (startMin / 2) + 90F
                    duration = (event.endTime.time - event.startTime.time) / 1000 / 60 / 2
                    oval = ovalLeft
                }
                startHour < 6 -> {
                    paint.color = context.getColor(
                        getEventColor(
                            event.type
                        )
                    )
                    startAngle = startHour * 30 + (startMin / 2) + 270F
                    duration = (event.endTime.time - event.startTime.time) / 1000 / 60 / 2
                    oval = ovalRight
                }
                else -> {
                    startHour -= 18
                    paint.color = context.getColor(
                        getEventColor(
                            event.type
                        )
                    )
                    startAngle = startHour * 30 + (startMin / 2) + 90F
                    duration = (event.endTime.time - event.startTime.time) / 1000 / 60 / 2
                    oval = ovalRight
                }
            }
            canvas?.drawArc(oval, startAngle, duration.toFloat(), true, paint)
            canvas?.drawArc(oval, startAngle, duration.toFloat(), true, paintBorder)
        }
    }

}