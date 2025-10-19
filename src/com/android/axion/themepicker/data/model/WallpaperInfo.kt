package com.android.axion.themepicker.data.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@OptIn(kotlinx.parcelize.Experimental::class)
@Parcelize
data class WallpaperInfo(
    val id: String,
    val title: String?,
    val drawableRes: Int
): Parcelable
