package com.angcyo.library

import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View

/**
 * 获取Int对应颜色的透明颜色
 * @param alpha [0..255] 值越小,越透明
 * */
public fun Int.tranColor(alpha: Int): Int {
    return Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))
}

public fun MotionEvent.isDown(): Boolean {
    return this.actionMasked == MotionEvent.ACTION_DOWN
}

public fun MotionEvent.isFinish(): Boolean {
    return this.actionMasked == MotionEvent.ACTION_UP || this.actionMasked == MotionEvent.ACTION_CANCEL
}

public fun Float.abs() = Math.abs(this)
public fun Int.abs() = Math.abs(this)

public val View.density: Float
    get() = resources.displayMetrics.density

/**Match_Parent*/
public fun View.exactlyMeasure(size: Int): Int = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)

public fun View.exactlyMeasure(size: Float): Int = this.exactlyMeasure(size.toInt())

public val View.debugPaint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
}

