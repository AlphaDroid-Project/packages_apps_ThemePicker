package com.android.axion.themepicker.data.model

import android.graphics.drawable.Drawable
import com.android.axion.themepicker.data.model.OverlayOption

data class IconPackOption(
    override val packageName: String?,
    override val label: String,
    val previewIcons: List<Drawable> = emptyList(),
    val sysUiPackageName: String? = null,
    override val isActive: Boolean = false
) : OverlayOption(packageName, label, isActive)
