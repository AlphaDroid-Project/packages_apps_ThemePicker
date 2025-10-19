package com.android.axion.themepicker.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class LayoutScreen : Parcelable {
    @Parcelize
    object Root : LayoutScreen()
    
    @Parcelize
    object AppGridSettings : LayoutScreen()

    @Parcelize
    object SystemIcons : LayoutScreen()

    @Parcelize
    object Font : LayoutScreen()

    @Parcelize
    object Shape : LayoutScreen()
}
