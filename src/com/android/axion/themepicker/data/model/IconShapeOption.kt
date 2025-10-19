package com.android.axion.themepicker.data.model

import android.graphics.Path
import android.graphics.drawable.Drawable

data class IconShapeOption(
    override val packageName: String?,
    override val label: String,
    val shapePath: Path?,
    val shapeDrawable: Drawable,
    val shapedAppIcons: List<Drawable> = emptyList(),
    override val isActive: Boolean = false
) : OverlayOption(packageName, label, isActive)
