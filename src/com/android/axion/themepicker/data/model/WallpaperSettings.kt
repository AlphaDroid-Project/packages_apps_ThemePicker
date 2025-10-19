package com.android.axion.themepicker.data.model

import com.android.axion.themepicker.data.model.ZoomProperties

data class WallpaperSettings(
    val wallpaperId: Int = -1,
    val lockscreen: Boolean = true,
    val homescreen: Boolean = true,
    val atmosphere: Boolean = false,
    val glass: Boolean = false,
    val zoomProperties: ZoomProperties = ZoomProperties()
)
