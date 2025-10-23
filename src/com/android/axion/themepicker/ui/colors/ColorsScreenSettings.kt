/*
 * Copyright (C) 2025 AxionOS
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
package com.android.axion.themepicker.ui.colors

import android.content.Context
import android.graphics.Color as GraphicsColor
import android.provider.Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.axion.themepicker.ui.components.FooterCard
import com.android.axion.themepicker.data.model.ColorsSettingsData
import com.android.axion.themepicker.data.model.PaletteItem
import com.android.axion.themepicker.ui.dialogs.ColorPickerDialog
import com.android.axion.themepicker.ui.dialogs.StylePickerDialog
import com.android.axion.themepicker.ui.preferences.PreferenceCard
import com.android.axion.themepicker.ui.preferences.PreferenceGroupCard
import com.android.axion.themepicker.ui.preferences.SliderCard
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.utils.colors.toArgb
import com.android.axion.themepicker.utils.settings.applyAdvancedSettings
import com.android.axion.themepicker.utils.settings.applyColorSettings
import com.android.axion.themepicker.utils.settings.applyPaletteOverride
import com.android.axion.themepicker.utils.settings.clearPaletteOverrides
import com.android.axion.themepicker.utils.settings.getColorSettings
import com.android.axion.themepicker.utils.settings.loadCurrentSettings
import com.android.axion.themepicker.R
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicColorsSettings() {
    val context = LocalContext.current
    val colors = LocalAxColorScheme.current
    val accent = colors.primary
    var settings by remember { mutableStateOf(loadCurrentSettings(context, accent)) }
    var advancedEnabled by remember { mutableStateOf(settings.advancedSettings) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showStylePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!advancedEnabled) {
            PreferenceGroupCard {
                WallpaperColorPreference(settings, onSettingsChange = {
                    settings = it
                    applyColorSettings(context, settings)
                })
                
                if (!settings.useWallpaperColors) {
                    Divider()
                    SeedColorPreference(settings, onClick = { showColorPicker = true })
                }
                
                Divider()
                StylePickerPref(settings, onClick = { showStylePicker = true })
            }
        }

        PreferenceGroupCard {
            FidelityPreference(settings, onSettingsChange = {
                settings = it
                applyColorSettings(context, settings)
            })
            
            Divider()
            SliderCard(
                title = stringResource(R.string.contrast_level_title),
                value = settings.contrastLevel,
                onValueChangeFinished = {
                    settings = settings.copy(contrastLevel = it)
                    applyColorSettings(context, settings)
                },
                valueRange = -1f..1f,
                defaultValue = 0f
            )
            Divider()
            SliderCard(
                title = stringResource(R.string.chroma_boost_title),
                value = settings.chromaBoost,
                onValueChangeFinished = {
                    settings = settings.copy(chromaBoost = it)
                    applyColorSettings(context, settings)
                },
                valueRange = 0f..100f,
                defaultValue = 0f
            )
        }

        FooterCard(
            title = stringResource(id = R.string.about_color_customization_title),
            description = stringResource(id = R.string.about_color_customization_desc)
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = settings.seedColor,
            onDismiss = { showColorPicker = false },
            onColorSelected = { color ->
                settings = settings.copy(seedColor = color)
                applyColorSettings(context, settings)
                showColorPicker = false
            }
        )
    }

    if (showStylePicker) {
        StylePickerDialog(
            currentStyle = settings.style,
            onDismiss = { showStylePicker = false },
            onStyleSelected = { style ->
                settings = settings.copy(style = style)
                applyColorSettings(context, settings)
                showStylePicker = false
            }
        )
    }
}

@Composable
fun AdvancedColorsSettings() {
    val context = LocalContext.current
    val colors = LocalAxColorScheme.current
    val accent = colors.primary
    val settings by remember { mutableStateOf(loadCurrentSettings(context, accent)) }
    var advancedEnabled by remember { mutableStateOf(settings.advancedSettings) }

    val json = getColorSettings(context)

    fun loadOverride(key: String, fallback: Color): Color {
        return json.optString("_override_$key", null)?.let { hex ->
            runCatching { Color(GraphicsColor.parseColor("#$hex")) }.getOrNull()
        } ?: fallback
    }

    val accent1Title = stringResource(R.string.accent1_title)
    val accent1Desc = stringResource(R.string.accent1_desc)
    val accent2Title = stringResource(R.string.accent2_title)
    val accent2Desc = stringResource(R.string.accent2_desc)
    val accent3Title = stringResource(R.string.accent3_title)
    val accent3Desc = stringResource(R.string.accent3_desc)
    val neutral1Title = stringResource(R.string.neutral1_title)
    val neutral1Desc = stringResource(R.string.neutral1_desc)
    val neutral2Title = stringResource(R.string.neutral2_title)
    val neutral2Desc = stringResource(R.string.neutral2_desc)

    var palette by remember {
        mutableStateOf(
            listOf(
                PaletteItem("accent1", accent1Title, accent1Desc, loadOverride("accent1", accent)),
                PaletteItem("accent2", accent2Title, accent2Desc, loadOverride("accent2", colors.secondary)),
                PaletteItem("accent3", accent3Title, accent3Desc, loadOverride("accent3", colors.tertiary)),
                PaletteItem("neutral1", neutral1Title, neutral1Desc, loadOverride("neutral1", colors.surface)),
                PaletteItem("neutral2", neutral2Title, neutral2Desc, loadOverride("neutral2", colors.surfaceVariant))
            )
        )
    }

    var showPicker by remember { mutableStateOf(false) }
    var activePickerIndex by remember { mutableStateOf(-1) }
    var activeColor by remember { mutableStateOf(Color.Black) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PreferenceCard(
            title = stringResource(R.string.enable_advanced_settings_title),
            description = stringResource(R.string.enable_advanced_settings_desc),
            checked = advancedEnabled,
            onCheckedChange = { enabled ->
                advancedEnabled = enabled
                applyAdvancedSettings(context, advancedEnabled)
            }
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (advancedEnabled) 1f else 0.5f),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                palette.forEachIndexed { index, item ->
                    PaletteOverrideItem(
                        title = item.title,
                        description = item.description,
                        color = item.color,
                        onClick = {
                            if (advancedEnabled) {
                                activePickerIndex = index
                                activeColor = item.color
                                showPicker = true
                            }
                        }
                    )
                    if (index < palette.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        if (advancedEnabled) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
            ) {
                OutlinedButton(
                    onClick = {
                        clearPaletteOverrides(context)
                        palette = palette.map { item ->
                            val defaultColor = when (item.key) {
                                "accent1" -> colors.primary
                                "accent2" -> colors.secondary
                                "accent3" -> colors.tertiary
                                "neutral1" -> colors.surface
                                "neutral2" -> colors.surfaceVariant
                                else -> item.color
                            }
                            item.copy(color = defaultColor)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.reset_all_overrides))
                }
            }
        }
    }

    if (showPicker && activePickerIndex >= 0) {
        ColorPickerDialog(
            initialColor = activeColor,
            onDismiss = { showPicker = false },
            onColorSelected = { color ->
                palette = palette.toMutableList().also { it[activePickerIndex] = it[activePickerIndex].copy(color = color) }
                if (advancedEnabled) applyPaletteOverride(context, palette[activePickerIndex].key, color)
                showPicker = false
            }
        )
    }
}

@Composable
private fun PaletteOverrideItem(
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    val colors = LocalAxColorScheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
            Text(
                text = "#${String.format("%06X", 0xFFFFFF and color.toArgb())}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun PaletteOverridesCard(
    title: String,
    description: String,
    items: List<PaletteItem>,
    onClick: (String) -> Unit,
    enabledModifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(enabledModifier),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            items.forEachIndexed { index, item ->
                PaletteOverrideItem(
                    title = item.title,
                    description = item.description,
                    color = item.color,
                    onClick = { onClick(item.key) }
                )
                if (index < items.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun WallpaperColorPreference(settings: ColorsSettingsData, onSettingsChange: (ColorsSettingsData) -> Unit) {
    val colors = LocalAxColorScheme.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.use_wallpaper_colors_title),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.use_wallpaper_colors_desc),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        Switch(
            checked = settings.useWallpaperColors,
            onCheckedChange = { enabled -> onSettingsChange(settings.copy(useWallpaperColors = enabled)) }
        )
    }
}

@Composable
private fun SeedColorPreference(settings: ColorsSettingsData, onClick: () -> Unit) {
    val colors = LocalAxColorScheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.custom_seed_color_title),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "#${String.format("%06X", 0xFFFFFF and settings.seedColor.toArgb())}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(settings.seedColor)
        )
    }
}

@Composable
private fun StylePickerPref(settings: ColorsSettingsData, onClick: () -> Unit) {
    val colors = LocalAxColorScheme.current
    Column {
        Text(
            text = stringResource(R.string.theme_style_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = stringResource(R.string.theme_style_desc),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = settings.style.displayName)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun FidelityPreference(settings: ColorsSettingsData, onSettingsChange: (ColorsSettingsData) -> Unit) {
    val colors = LocalAxColorScheme.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.fidelity_title),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.fidelity_desc),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        Switch(
            checked = settings.fidelity,
            onCheckedChange = { enabled -> onSettingsChange(settings.copy(fidelity = enabled)) }
        )
    }
}
