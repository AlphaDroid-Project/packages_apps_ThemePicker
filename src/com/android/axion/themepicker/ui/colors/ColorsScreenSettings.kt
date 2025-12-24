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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import com.android.axion.themepicker.R
import com.android.axion.themepicker.data.model.ColorsSettingsData
import com.android.axion.themepicker.ui.dialogs.ColorPickerDialog
import com.android.axion.themepicker.ui.dialogs.StylePickerDialog
import com.android.axion.themepicker.ui.theme.*
import com.android.axion.themepicker.utils.colors.toArgb
import com.android.axion.themepicker.utils.settings.applyColorSettings
import com.android.axion.themepicker.utils.settings.loadCurrentSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicColorsSettings() {
    val context = LocalContext.current
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    val accent = colors.primary
    
    var settings by remember { mutableStateOf(loadCurrentSettings(context, accent)) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showStylePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ColorSourceCard(
            useWallpaper = settings.useWallpaperColors,
            seedColor = settings.seedColor,
            onToggleWallpaper = { enabled ->
                settings = settings.copy(useWallpaperColors = enabled)
                applyColorSettings(context, settings)
            },
            onPickColor = { showColorPicker = true }
        )
        
        ThemeStyleCard(
            currentStyle = settings.style.displayName,
            onClick = { showStylePicker = true }
        )
        
        AdvancedColorsCard(
            fidelity = settings.fidelity,
            contrastLevel = settings.contrastLevel,
            chromaBoost = settings.chromaBoost,
            onFidelityChange = { enabled ->
                settings = settings.copy(fidelity = enabled)
                applyColorSettings(context, settings)
            },
            onContrastChange = { value ->
                settings = settings.copy(contrastLevel = value)
                applyColorSettings(context, settings)
            },
            onChromaChange = { value ->
                settings = settings.copy(chromaBoost = value)
                applyColorSettings(context, settings)
            }
        )
        
        InfoFooter()
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
private fun ColorSourceCard(
    useWallpaper: Boolean,
    seedColor: Color,
    onToggleWallpaper: (Boolean) -> Unit,
    onPickColor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(design.shapes.cardCorner),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(design.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(design.spacing.medium)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(design.spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = colors.primaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Palette,
                            contentDescription = null,
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "Color Source",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            ExpressiveToggleRow(
                title = stringResource(R.string.use_wallpaper_colors_title),
                subtitle = stringResource(R.string.use_wallpaper_colors_desc),
                checked = useWallpaper,
                onCheckedChange = onToggleWallpaper
            )
            
            AnimatedVisibility(
                visible = !useWallpaper,
                enter = fadeIn(animationSpec = tween(150)) + expandVertically(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(100)) + shrinkVertically(animationSpec = tween(100))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onPickColor),
                    shape = RoundedCornerShape(design.shapes.cornerMedium),
                    color = colors.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(design.spacing.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.custom_seed_color_title),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "#${String.format("%06X", 0xFFFFFF and seedColor.toArgb())}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(seedColor)
                                .border(2.dp, colors.outline.copy(alpha = 0.3f), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeStyleCard(
    currentStyle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "style_card_scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(design.shapes.cardCorner),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(design.spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(design.spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = colors.secondaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Style,
                            contentDescription = null,
                            tint = colors.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.theme_style_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = currentStyle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdvancedColorsCard(
    fidelity: Boolean,
    contrastLevel: Float,
    chromaBoost: Float,
    onFidelityChange: (Boolean) -> Unit,
    onContrastChange: (Float) -> Unit,
    onChromaChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(design.shapes.cardCorner),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(design.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(design.spacing.medium)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(design.spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = colors.tertiaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Tune,
                            contentDescription = null,
                            tint = colors.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "Fine Tuning",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Divider(color = colors.outlineVariant.copy(alpha = 0.5f))
            
            ExpressiveToggleRow(
                title = stringResource(R.string.fidelity_title),
                subtitle = stringResource(R.string.fidelity_desc),
                checked = fidelity,
                onCheckedChange = onFidelityChange
            )
            
            Divider(color = colors.outlineVariant.copy(alpha = 0.5f))
            
            ExpressiveSlider(
                title = stringResource(R.string.contrast_level_title),
                value = contrastLevel,
                onValueChange = onContrastChange,
                valueRange = -1f..1f,
                valueLabel = when {
                    contrastLevel < -0.3f -> "Low"
                    contrastLevel > 0.3f -> "High"
                    else -> "Normal"
                }
            )
            
            Divider(color = colors.outlineVariant.copy(alpha = 0.5f))
            
            ExpressiveSlider(
                title = stringResource(R.string.chroma_boost_title),
                value = chromaBoost,
                onValueChange = onChromaChange,
                valueRange = 0f..100f,
                valueLabel = "${chromaBoost.toInt()}%"
            )
        }
    }
}

@Composable
private fun ExpressiveToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ExpressiveSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    var sliderValue by remember(value) { mutableStateOf(value) }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = colors.primaryContainer
            ) {
                Text(
                    text = valueLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChange(sliderValue) },
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.primaryContainer
            )
        )
    }
}

@Composable
private fun InfoFooter(
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(design.shapes.cornerMedium),
        color = colors.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(design.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(design.spacing.small),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = stringResource(id = R.string.about_color_customization_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Text(
                    text = stringResource(id = R.string.about_color_customization_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}
