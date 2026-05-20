/*
 * Copyright (C) 2025-2026 AxionOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.alpha.themepicker.utils.settings

import android.content.Context
import android.graphics.Color as GraphicsColor
import android.provider.Settings
import androidx.compose.ui.graphics.Color
import com.android.alpha.themepicker.data.model.ColorsSettingsData
import com.android.alpha.themepicker.data.model.ThemeStyle
import com.android.alpha.themepicker.utils.colors.toArgb
import org.json.JSONObject

fun getColorSettings(context: Context): JSONObject {
    return runCatching {
            val currentJson =
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                ) ?: "{}"
            JSONObject(currentJson)
        }
        .getOrElse { JSONObject("{}") }
}

fun applyColorSettings(context: Context, json: JSONObject) {
    Settings.Secure.putString(
        context.contentResolver,
        Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
        json.toString(),
    )
}

fun applyColorSettings(context: Context, settings: ColorsSettingsData) {
    val json = getColorSettings(context)
    if (settings.useWallpaperColors) {
        json.remove("android.theme.customization.system_palette")
        json.remove("android.theme.customization.accent_color")
        json.remove("_base_seed_color")
        json.put("android.theme.customization.color_source", "home")
    } else {
        val color = String.format("%06X", 0xFFFFFF and settings.seedColor.toArgb())
        json.put("_base_seed_color", color)
        json.put("android.theme.customization.system_palette", color)
        json.put("android.theme.customization.accent_color", color)
        json.put("android.theme.customization.color_source", "preset")
    }
    if (settings.chromaBoost == 0f) {
        json.remove("android.theme.customization.chroma_factor")
    } else {
        json.put("android.theme.customization.chroma_factor", 1.0 + settings.chromaBoost / 100.0)
    }
    json.remove("_chroma_boost")
    json.put("_contrast_level", settings.contrastLevel.toDouble())
    json.put("_fidelity_enabled", settings.fidelity)
    json.put("android.theme.customization.fidelity", if (settings.fidelity) 1 else 0)
    json.put("android.theme.customization.theme_style", settings.style.systemValue)
    // Sync luminance_factor for AlphaVisuals compatibility (stored as 1.0 + delta/100)
    if (settings.luminance == 0) {
        json.remove("android.theme.customization.luminance_factor")
    } else {
        json.put("android.theme.customization.luminance_factor", 1.0 + settings.luminance / 100.0)
    }
    json.put("_applied_timestamp", System.currentTimeMillis())
    applyColorSettings(context, json)
}

fun loadCurrentSettings(context: Context, defaultSeed: Color): ColorsSettingsData {
    runCatching {
            val json = getColorSettings(context)

            val colorSource = json.optString("android.theme.customization.color_source", "home")
            val useWallpaper = colorSource == "home" || colorSource == "lock"

            val styleStr = json.optString("android.theme.customization.theme_style", "TONAL_SPOT")
            val style =
                ThemeStyle.values().find { it.systemValue == styleStr } ?: ThemeStyle.TONAL_SPOT

            val seedColor: Color =
                runCatching {
                        val seedColorHex = json.optString("_base_seed_color", null)
                        if (seedColorHex.isNullOrBlank()) {
                            defaultSeed
                        } else {
                            Color(GraphicsColor.parseColor("#${seedColorHex.removePrefix("#")}"))
                        }
                    }
                    .getOrElse { defaultSeed }

            val fidelity = if (json.has("android.theme.customization.fidelity")) {
                json.optInt("android.theme.customization.fidelity", 0) == 1
            } else {
                json.optBoolean("_fidelity_enabled", false)
            }

            val contrast = json.optDouble("_contrast_level", 0.0).toFloat()
            val chromaFactor = json.optDouble("android.theme.customization.chroma_factor", 1.0)
            val chroma = if (chromaFactor == 1.0 && json.has("_chroma_boost")) {
                json.optDouble("_chroma_boost", 0.0).toFloat().coerceIn(-80f, 100f)
            } else {
                ((chromaFactor - 1.0) * 100.0).toFloat().coerceIn(-80f, 100f)
            }

            // Read luminance_factor (stored as 1.0 + delta/100 by AlphaVisuals)
            val luminanceFactor = json.optDouble("android.theme.customization.luminance_factor", 1.0)
            val luminance = ((luminanceFactor - 1.0) * 100.0).toInt()

            return ColorsSettingsData(
                seedColor = seedColor,
                style = style,
                useWallpaperColors = useWallpaper,
                contrastLevel = contrast,
                fidelity = fidelity,
                chromaBoost = chroma,
                luminance = luminance,
            )
        }
        .getOrElse {
            return ColorsSettingsData()
        }
}
