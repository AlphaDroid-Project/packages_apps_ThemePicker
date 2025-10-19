package com.android.axion.themepicker.data.model

import android.graphics.Typeface
import com.android.axion.themepicker.data.model.OverlayOption

data class FontOverlayOption(
    override val packageName: String?,
    override val label: String,
    val headlineFont: Typeface,
    val bodyFont: Typeface,
    override val isActive: Boolean = false
) : OverlayOption(packageName, label, isActive)
