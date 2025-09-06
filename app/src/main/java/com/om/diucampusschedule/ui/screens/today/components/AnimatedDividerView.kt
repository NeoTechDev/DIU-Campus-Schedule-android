package com.om.diucampusschedule.ui.screens.today.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt

class AnimatedDividerView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var mWaveData: FloatArray? = null

    private val linePaint = Paint().apply {
        color = "#4285F4".toColorInt() // Modern blue color
        strokeWidth = 2.5f
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND // Rounded ends for smoother appearance
        strokeJoin = Paint.Join.ROUND // Rounded joins for smoother curves
    }

    private val wavePath = Path()

    // Call this to update the wave data from your Composable
    fun updateData(data: FloatArray?) {
        mWaveData = data
        invalidate() // Request a redraw of the view
    }

    // Optional: If you want to control color from Compose later
    fun setLineColor(color: Int) {
        linePaint.color = color
        invalidate()
    }

    // Optional: If you want to control stroke width from Compose later
    fun setStrokeWidth(width: Float) {
        linePaint.strokeWidth = width
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        wavePath.reset()

        if (mWaveData == null || mWaveData?.isEmpty() == true) {
            // Draw a straight line if no data or data is empty
            val centerY = height / 2f
            canvas.drawLine(0f, centerY, width.toFloat(), centerY, linePaint)
            return
        }

        val centerY = height / 2f

        mWaveData?.let { currentData ->
            if (currentData.isEmpty()) return

            // More refined amplitude control
            val amplitudeMultiplier = 0.6f
            val waveVisualHeight = (height / 2f) * amplitudeMultiplier

            // Use cubic bezier curves for ultra-smooth wave rendering
            if (currentData.size >= 2) {
                // Start at the first point
                var x = 0f
                var y = centerY + (currentData[0] * waveVisualHeight)
                wavePath.moveTo(x, y.coerceIn(0f, height.toFloat()))

                // Create smooth curves using quadratic bezier curves
                for (i in 1 until currentData.size) {
                    val nextX = (i.toFloat() / (currentData.size - 1)) * width.toFloat()
                    val nextY = centerY + (currentData[i] * waveVisualHeight)

                    // Calculate control point for smooth curve
                    val controlX = (x + nextX) / 2f
                    val controlY = (y + nextY) / 2f

                    // Use quadratic bezier for smooth interpolation
                    wavePath.quadTo(controlX, controlY, nextX, nextY.coerceIn(0f, height.toFloat()))

                    x = nextX
                    y = nextY
                }
            }
        }

        canvas.drawPath(wavePath, linePaint)
    }
}
