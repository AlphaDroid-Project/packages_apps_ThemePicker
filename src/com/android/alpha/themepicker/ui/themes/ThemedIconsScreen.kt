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

package com.android.alpha.themepicker.ui.themes

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.axion.compose.scaffold.AxionScaffold
import com.android.alpha.themepicker.R
import com.android.alpha.themepicker.ui.theme.LocalAdaptiveLayoutInfo
import com.android.alpha.themepicker.utils.math.scaleRatio
import com.android.alpha.themepicker.viewmodel.MainScreenViewModel
import kotlin.math.cos
import kotlin.math.sin

private const val SETTING_THEMED_ICON_STYLE = "themed_icon_style"
private const val SETTING_THEMED_ICONS_ENABLED = "themed_icons"
private const val STYLE_AXION = "axion"
private const val STYLE_AOSP = "aosp"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemedIconsScreen(mainScreenViewModel: MainScreenViewModel) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val scale = context.scaleRatio
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    val resolver = context.contentResolver

    var enabled by remember {
        mutableStateOf(Settings.Secure.getInt(resolver, SETTING_THEMED_ICONS_ENABLED, 0) == 1)
    }
    var currentStyle by remember {
        mutableStateOf(
            Settings.Secure.getString(resolver, SETTING_THEMED_ICON_STYLE) ?: STYLE_AXION
        )
    }

    BackHandler { mainScreenViewModel.goBack() }

    AxionScaffold(
        title = stringResource(R.string.themed_icons_title),
        onBackClick = { mainScreenViewModel.goBack() },
        modifier = Modifier.background(colors.background),
    ) { paddingValues ->
        if (layoutInfo.isDualPane) {
            Row(
                modifier =
                    Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Box(
                    modifier =
                        Modifier.weight(0.4f)
                            .fillMaxHeight()
                            .padding(vertical = 24.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(colors.surfaceContainerLow),
                    contentAlignment = Alignment.Center,
                ) {
                    ThemedIconPreviewLarge(
                        isAxIcons = currentStyle == STYLE_AXION,
                        enabled = enabled,
                    )
                }

                Column(
                    modifier =
                        Modifier.weight(0.6f)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ThemedIconSettingsCard(
                        enabled = enabled,
                        currentStyle = currentStyle,
                        onEnabledChange = { newEnabled ->
                            enabled = newEnabled
                            Settings.Secure.putInt(
                                resolver,
                                SETTING_THEMED_ICONS_ENABLED,
                                if (newEnabled) 1 else 0,
                            )
                        },
                        onStyleChange = { newStyle ->
                            currentStyle = newStyle
                            Settings.Secure.putString(resolver, SETTING_THEMED_ICON_STYLE, newStyle)
                        },
                    )
                }
            }
        } else {
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp * scale),
                    contentAlignment = Alignment.Center,
                ) {
                    ThemedIconPreviewLarge(
                        isAxIcons = currentStyle == STYLE_AXION,
                        enabled = enabled,
                    )
                }

                ThemedIconSettingsCard(
                    enabled = enabled,
                    currentStyle = currentStyle,
                    onEnabledChange = { newEnabled ->
                        enabled = newEnabled
                        Settings.Secure.putInt(
                            resolver,
                            SETTING_THEMED_ICONS_ENABLED,
                            if (newEnabled) 1 else 0,
                        )
                    },
                    onStyleChange = { newStyle ->
                        currentStyle = newStyle
                        Settings.Secure.putString(resolver, SETTING_THEMED_ICON_STYLE, newStyle)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp * scale),
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ThemedIconPreviewLarge(isAxIcons: Boolean, enabled: Boolean) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.size(200.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceBright),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (enabled) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PreviewIcon(isAxIcons, IconType.PHONE)
                        PreviewIcon(isAxIcons, IconType.MESSAGES)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PreviewIcon(isAxIcons, IconType.CAMERA)
                        PreviewIcon(isAxIcons, IconType.SETTINGS)
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.off),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemedIconSettingsCard(
    enabled: Boolean,
    currentStyle: String,
    onEnabledChange: (Boolean) -> Unit,
    onStyleChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val scale = LocalContext.current.scaleRatio

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceBright),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp * scale)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.themed_icons_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.themed_icons_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors =
                        SwitchDefaults.colors(
                            checkedThumbColor = colors.primary,
                            checkedTrackColor = colors.primaryContainer,
                        ),
                )
            }

            AnimatedVisibility(
                visible = enabled,
                enter =
                    fadeIn(animationSpec = MaterialTheme.motionScheme.slowEffectsSpec()) +
                        expandVertically(
                            animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
                        ),
                exit =
                    fadeOut(animationSpec = MaterialTheme.motionScheme.slowEffectsSpec()) +
                        shrinkVertically(
                            animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
                        ),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp * scale))
                    HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(20.dp * scale))

                    Text(
                        text = stringResource(R.string.themed_icons_style),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                    )

                    Spacer(modifier = Modifier.height(16.dp * scale))

                    val options = listOf(STYLE_AXION, STYLE_AOSP)
                    val labels = listOf("AxIcons", "AOSP")
                    val subtitles =
                        listOf(
                            stringResource(R.string.themed_icons_neutral),
                            stringResource(R.string.themed_icons_accent),
                        )
                    val selectedIndex = options.indexOf(currentStyle).coerceAtLeast(0)

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        options.forEachIndexed { index, style ->
                            SegmentedButton(
                                selected = selectedIndex == index,
                                onClick = { onStyleChange(style) },
                                shape =
                                    SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = options.size,
                                    ),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                ) {
                                    Text(
                                        text = labels[index],
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight =
                                            if (selectedIndex == index) FontWeight.Bold
                                            else FontWeight.Medium,
                                    )
                                    Text(
                                        text = subtitles[index],
                                        style = MaterialTheme.typography.labelSmall,
                                        color =
                                            if (selectedIndex == index) colors.onSecondaryContainer
                                            else colors.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class IconType {
    PHONE,
    MESSAGES,
    CAMERA,
    SETTINGS,
}

@Composable
private fun PreviewIcon(isAxIcons: Boolean, type: IconType) {
    val bgColor =
        if (isAxIcons) {
            MaterialTheme.colorScheme.surfaceContainerLowest
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }

    val fgColor =
        if (isAxIcons) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }

    val iconSize = if (isAxIcons) 22.dp else 28.dp

    Box(
        modifier = Modifier.size(52.dp).clip(MaterialTheme.shapes.extraLarge).background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        val surfaceBrightColor = MaterialTheme.colorScheme.surfaceBright
        Canvas(modifier = Modifier.size(iconSize)) {
            val canvasSize = size.minDimension
            val strokeWidth = canvasSize * 0.12f

            when (type) {
                IconType.PHONE -> {
                    val path =
                        Path().apply {
                            val w = canvasSize
                            val h = canvasSize
                            moveTo(w * 0.35f, h * 0.15f)
                            cubicTo(w * 0.25f, h * 0.15f, w * 0.2f, h * 0.2f, w * 0.2f, h * 0.3f)
                            lineTo(w * 0.2f, h * 0.7f)
                            cubicTo(w * 0.2f, h * 0.8f, w * 0.25f, h * 0.85f, w * 0.35f, h * 0.85f)
                            lineTo(w * 0.65f, h * 0.85f)
                            cubicTo(w * 0.75f, h * 0.85f, w * 0.8f, h * 0.8f, w * 0.8f, h * 0.7f)
                            lineTo(w * 0.8f, h * 0.3f)
                            cubicTo(w * 0.8f, h * 0.2f, w * 0.75f, h * 0.15f, w * 0.65f, h * 0.15f)
                            close()
                        }
                    drawPath(path, fgColor, style = Stroke(strokeWidth))
                    drawCircle(
                        fgColor,
                        radius = canvasSize * 0.05f,
                        center = Offset(canvasSize * 0.5f, canvasSize * 0.75f),
                    )
                }
                IconType.MESSAGES -> {
                    val path =
                        Path().apply {
                            val w = canvasSize
                            val h = canvasSize
                            moveTo(w * 0.15f, h * 0.3f)
                            cubicTo(w * 0.15f, h * 0.2f, w * 0.2f, h * 0.15f, w * 0.3f, h * 0.15f)
                            lineTo(w * 0.7f, h * 0.15f)
                            cubicTo(w * 0.8f, h * 0.15f, w * 0.85f, h * 0.2f, w * 0.85f, h * 0.3f)
                            lineTo(w * 0.85f, h * 0.6f)
                            cubicTo(w * 0.85f, h * 0.7f, w * 0.8f, h * 0.75f, w * 0.7f, h * 0.75f)
                            lineTo(w * 0.55f, h * 0.75f)
                            lineTo(w * 0.45f, h * 0.85f)
                            lineTo(w * 0.45f, h * 0.75f)
                            lineTo(w * 0.3f, h * 0.75f)
                            cubicTo(w * 0.2f, h * 0.75f, w * 0.15f, h * 0.7f, w * 0.15f, h * 0.6f)
                            close()
                        }
                    drawPath(path, fgColor)
                }
                IconType.CAMERA -> {
                    drawRoundRect(
                        fgColor,
                        topLeft = Offset(canvasSize * 0.15f, canvasSize * 0.3f),
                        size = Size(canvasSize * 0.7f, canvasSize * 0.5f),
                        cornerRadius = CornerRadius(canvasSize * 0.08f),
                        style = Stroke(strokeWidth),
                    )
                    drawCircle(
                        fgColor,
                        radius = canvasSize * 0.15f,
                        center = Offset(canvasSize * 0.5f, canvasSize * 0.55f),
                        style = Stroke(strokeWidth),
                    )
                    drawRect(
                        fgColor,
                        topLeft = Offset(canvasSize * 0.35f, canvasSize * 0.2f),
                        size = Size(canvasSize * 0.3f, canvasSize * 0.1f),
                    )
                }
                IconType.SETTINGS -> {
                    val centerX = canvasSize * 0.5f
                    val centerY = canvasSize * 0.5f
                    val outerRadius = canvasSize * 0.35f
                    val innerRadius = canvasSize * 0.15f

                    val path = Path()
                    for (i in 0 until 6) {
                        val angle = (i * 60f - 90f) * (Math.PI / 180f).toFloat()
                        val x = centerX + outerRadius * cos(angle)
                        val y = centerY + outerRadius * sin(angle)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

                        val nextAngle = ((i + 1) * 60f - 90f) * (Math.PI / 180f).toFloat()
                        val midAngle = (angle + nextAngle) / 2f
                        val midX = centerX + innerRadius * cos(midAngle)
                        val midY = centerY + innerRadius * sin(midAngle)
                        path.lineTo(midX, midY)
                    }
                    path.close()

                    drawPath(path, fgColor)
                    drawCircle(
                        surfaceBrightColor,
                        radius = canvasSize * 0.12f,
                        center = Offset(centerX, centerY),
                    )
                }
            }
        }
    }
}
