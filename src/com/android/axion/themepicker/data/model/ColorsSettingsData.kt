package com.android.axion.themepicker.data.model

import androidx.compose.ui.graphics.Color
import com.android.axion.themepicker.data.model.ThemeStyle

data class ColorsSettingsData(
    val seedColor: Color = Color(0xFF6750A4),
    val style: ThemeStyle = ThemeStyle.TONAL_SPOT,
    val useWallpaperColors: Boolean = true,
    val contrastLevel: Float = 0f,
    val fidelity: Boolean = true,
    val advancedSettings: Boolean = false,
    val chromaBoost: Float = 0f,
)
