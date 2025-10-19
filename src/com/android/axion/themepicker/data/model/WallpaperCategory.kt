package com.android.axion.themepicker.data.model

import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
@Immutable
data class WallpaperCategory(
    val id: String,
    val title: String,
    val wallpapers: List<WallpaperInfo>
) : Parcelable
