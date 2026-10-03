package com.example.tastee

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

class SimpleWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val path = Path()

    private var waveHeightPercent = 0.05f
    private var waveCenterPercent = 0.25f

    fun setWaveHeightPercent(value: Float) {
        waveHeightPercent = value
        buildWave()
        invalidate()
    }

    fun setWaveCenterPercent(value: Float) {
        waveCenterPercent = value
        buildWave()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildWave()
    }

    private fun buildWave() {
        if (width == 0 || height == 0) return

        val density = resources.displayMetrics.density

        val waveHeight = 20f * density
        val centerY = 100f * density

        path.reset()
        path.moveTo(0f, centerY)

        var x = 0f
        val step = 20f

        while (x <= width) {
            val y = (sin((x / width.toFloat()) * 2 * Math.PI) * waveHeight).toFloat() + centerY
            path.lineTo(x, y)
            x += step
        }

        path.lineTo(width.toFloat(), height.toFloat())
        path.lineTo(0f, height.toFloat())
        path.close()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }
}