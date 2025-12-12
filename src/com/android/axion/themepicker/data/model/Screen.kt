package com.android.axion.themepicker.data.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class Screen : Parcelable {
    @Parcelize
    object Main : Screen()

    @Parcelize
    data class Preview(val wallpaper: WallpaperInfo, val bitmap: Bitmap? = null) : Screen()

    @Parcelize
    data class Apply(
        val wallpaper: WallpaperInfo,
        val zoomProperties: ZoomProperties = ZoomProperties(),
        val bitmap: Bitmap? = null
    ) : Screen()

    @Parcelize
    object EditCurrent : Screen()

    @Parcelize
    object ColorsSettings : Screen()
    
    @Parcelize
    object WallpaperGallery : Screen()
    
    @Parcelize
    object Layout : Screen()
    
    @Parcelize
    data class Lockscreen(
        val wallpaper: WallpaperInfo? = null,
        val entryPoint: EntryPoint = EntryPoint.DEFAULT
    ) : Screen()

    @Parcelize
    enum class EntryPoint : Parcelable {
        DEFAULT,
        WIDGETS,
        SHORTCUTS
    }
}
