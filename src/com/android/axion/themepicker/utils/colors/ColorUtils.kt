package com.android.axion.themepicker.utils.colors

import android.annotation.AttrRes
import android.annotation.ColorInt
import android.app.Activity
import android.content.Context
import android.graphics.Color as GraphicsColor
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*

object ColorUtils {
    fun getContentColor(backgroundColor: Color): Color {
        return if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White
    }
    
    fun createOverlayColor(statusBarColor: Color, alpha: Float = 0.28f): Color {
        return statusBarColor.copy(alpha = alpha)
    }
    
    fun getStatusBarColor(activity: Activity?): Color {
        val colorInt = activity?.window?.statusBarColor ?: GraphicsColor.BLACK
        return Color(colorInt)
    }
}

@Composable
@ReadOnlyComposable
fun colorAttr(@AttrRes attribute: Int): Color {
    return colorAttr(LocalContext.current, attribute)
}

fun colorAttr(context: Context, @AttrRes attr: Int): Color {
    val ta = context.obtainStyledAttributes(intArrayOf(attr))
    @ColorInt val color = ta.getColor(0, 0)
    ta.recycle()
    return Color(color)
}

fun Color.toArgb(): Int {
    return GraphicsColor.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}
