package com.android.axion.themepicker.data.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@OptIn(kotlinx.parcelize.Experimental::class)
@Parcelize
data class ZoomProperties(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
) : Parcelable {
    fun isZoomed(): Boolean = scale > 1f
    fun reset(): ZoomProperties = ZoomProperties()
}
