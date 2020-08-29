package com.nutr1t07.lifelog.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioGroup


// stole from https://github.com/jevonbeck/AbstractMachine/blob/jevon_dev/app/src/main/java/org/ricts/abstractmachine/ui/utils/MultiLineRadioGroup.java
class MultiLineRadioGroup(context: Context, attrs: AttributeSet?) : RadioGroup(context, attrs) {
    val viewRectMap: MutableMap<View, Rect> = mutableMapOf()
    val defaultDims = Rect(0, 0, 0, 0)

    constructor(context: Context) : this(context, null)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var heightMeasurement = MeasureSpec.getSize(heightMeasureSpec)
        val widthMeasurement = MeasureSpec.getSize(widthMeasureSpec)

        when (orientation) {
            LinearLayout.HORIZONTAL -> heightMeasurement =
                findHorizontalHeight(widthMeasureSpec, heightMeasureSpec)
        }
        setMeasuredDimension(widthMeasurement, heightMeasurement)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val btnView = getChildAt(i)
            val dims = viewRectMap[btnView] ?: defaultDims
            btnView.layout(dims.left, dims.top, dims.right, dims.bottom)
        }

    }

    fun findHorizontalHeight(pWidthMeasureSpec: Int, pHeightMeasureSpec: Int): Int {
        val maxWidth = MeasureSpec.getSize(pWidthMeasureSpec) - paddingRight - paddingLeft
        val maxHeight = MeasureSpec.getSize(pHeightMeasureSpec) - paddingTop - paddingBottom


        val newWidthMeasureSpec =
            MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.getMode(pWidthMeasureSpec))
        val newHeightMeasureSpec =
            MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.getMode(pHeightMeasureSpec))

        var maxRowHeight = 0

        var btnPosLeft = paddingLeft
        var btnPosTop = paddingTop

        val maxRightPos = MeasureSpec.getSize(pWidthMeasureSpec) - paddingRight
        for (i in 0 until childCount) {
            val btnView = getChildAt(i)
            measureChild(btnView, newWidthMeasureSpec, newHeightMeasureSpec)

            maxRowHeight = Math.max(maxRowHeight, btnView.measuredHeight)
            var btnPosRight = btnPosLeft + btnView.measuredWidth
            // if current button would exceed border then
            if (btnPosRight > maxRightPos) {
                btnPosLeft = paddingLeft
                btnPosTop += maxRowHeight
                btnPosRight = btnPosLeft + btnView.measuredWidth
                maxRowHeight = btnView.measuredHeight
            }
            val btnPosBottom = btnPosTop + btnView.measuredHeight

            viewRectMap[btnView] = Rect(btnPosLeft, btnPosTop, btnPosRight, btnPosBottom)

            // get ready for the next button
            btnPosLeft = btnPosRight
        }

        val idealHeight = btnPosTop + maxRowHeight + paddingBottom
        val pHeight = MeasureSpec.getSize(pHeightMeasureSpec)
        return when (MeasureSpec.getMode(pHeightMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> idealHeight
            MeasureSpec.AT_MOST -> Math.min(idealHeight, pHeight)
            MeasureSpec.EXACTLY -> pHeight
            else -> pHeight
        }
    }
}

