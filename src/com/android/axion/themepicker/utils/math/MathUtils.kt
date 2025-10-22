package com.android.axion.themepicker.utils.math

import android.content.Context

fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

val Context.scaleRatio: Float
    get() {
        val displayMetrics = resources.displayMetrics
        val sw = minOf(displayMetrics.widthPixels, displayMetrics.heightPixels) / displayMetrics.density
        val ratio = sw / 420f
        return ratio
    }
