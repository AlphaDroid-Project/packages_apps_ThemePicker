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

package com.android.axion.themepicker.utils.settings

import android.content.Context
import android.graphics.Color as GraphicsColor
import android.provider.Settings
import androidx.compose.ui.graphics.Color
import com.android.axion.themepicker.data.model.ColorsSettingsData
import com.android.axion.themepicker.data.model.ThemeStyle
import com.android.axion.themepicker.utils.colors.toArgb
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
    json.put("_chroma_boost", settings.chromaBoost.toDouble())
    json.put("_contrast_level", settings.contrastLevel.toDouble())
    json.put("_fidelity_enabled", settings.fidelity)
    json.put("android.theme.customization.theme_style", settings.style.systemValue)
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

            val fidelity = json.optBoolean("_fidelity_enabled", true)

            val contrast = json.optDouble("_contrast_level", 0.0).toFloat()
            val chroma = json.optDouble("_chroma_boost", 0.0).toFloat()

            return ColorsSettingsData(
                seedColor = seedColor,
                style = style,
                useWallpaperColors = useWallpaper,
                contrastLevel = contrast,
                fidelity = fidelity,
                chromaBoost = chroma,
            )
        }
        .getOrElse {
            return ColorsSettingsData()
        }
}
