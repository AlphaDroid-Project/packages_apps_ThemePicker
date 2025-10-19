package com.android.axion.themepicker.data.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

import com.android.axion.themepicker.data.model.WallpaperCategory

@Parcelize
sealed class GalleryState : Parcelable {
    @Parcelize
    object Overview : GalleryState()
    @Parcelize
    data class CategoryList(val categories: List<WallpaperCategory>) : GalleryState()
    @Parcelize
    data class CategoryDetail(val category: WallpaperCategory) : GalleryState()
}
