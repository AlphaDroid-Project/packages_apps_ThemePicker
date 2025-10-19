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
package com.android.axion.themepicker.ui.dialogs

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
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.axion.themepicker.data.model.ThemeStyle
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.utils.colors.toArgb
import kotlin.math.*

@Composable
fun StylePickerDialog(
    currentStyle: ThemeStyle,
    onDismiss: () -> Unit,
    onStyleSelected: (ThemeStyle) -> Unit
) {
    val colors = LocalAxColorScheme.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colors.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .width(320.dp)
            ) {
                Text(
                    text = "Theme Style",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeStyle.values().forEach { style ->
                        val isSelected = style == currentStyle
                        Surface(
                            onClick = { onStyleSelected(style) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) 
                                colors.primaryContainer 
                            else 
                                colors.surface,
                            border = if (isSelected) 
                                null 
                            else 
                                BorderStroke(
                                    1.dp, 
                                    colors.outline
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = style.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected)
                                        colors.onPrimaryContainer
                                    else
                                        colors.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colors.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val colors = LocalAxColorScheme.current
    var selectedColor by remember { mutableStateOf(initialColor) }
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(0.5f) }
    var brightness by remember { mutableStateOf(0.5f) }

    LaunchedEffect(initialColor) {
        val hsv = FloatArray(3)
        GraphicsColor.colorToHSV(initialColor.toArgb(), hsv)
        hue = hsv[0]
        saturation = hsv[1]
        brightness = hsv[2]
        selectedColor = Color.hsv(hue, saturation, brightness)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colors.surfaceContainerLowest,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .width(320.dp)
            ) {
                Text(
                    text = "Choose Seed Color",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(selectedColor)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Hue",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                HuePicker(
                    hue = hue,
                    onHueChange = { newHue ->
                        hue = newHue
                        selectedColor = Color.hsv(hue, saturation, brightness)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "saturation",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ColorSlider(
                    value = saturation,
                    onValueChange = { newsaturation ->
                        saturation = newsaturation
                        selectedColor = Color.hsv(hue, saturation, brightness)
                    },
                    startColor = Color.hsv(hue, 0f, brightness),
                    endColor = Color.hsv(hue, 1f, brightness)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Brightness",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ColorSlider(
                    value = brightness,
                    onValueChange = { newValue ->
                        brightness = newValue
                        selectedColor = Color.hsv(hue, saturation, brightness)
                    },
                    startColor = Color.hsv(hue, saturation, 0f),
                    endColor = Color.hsv(hue, saturation, 1f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "HEX: #${String.format("%06X", 0xFFFFFF and selectedColor.toArgb())} saturation: $saturation brightness: $brightness",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onColorSelected(selectedColor) }) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun HuePicker(hue: Float, onHueChange: (Float) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val newHue = (change.position.x / size.width * 360f).coerceIn(0f, 360f)
                        onHueChange(newHue)
                    }
                }
        ) {
            val gradient = Brush.horizontalGradient(
                colors = listOf(
                    Color.hsv(0f, 1f, 1f),
                    Color.hsv(60f, 1f, 1f),
                    Color.hsv(120f, 1f, 1f),
                    Color.hsv(180f, 1f, 1f),
                    Color.hsv(240f, 1f, 1f),
                    Color.hsv(300f, 1f, 1f),
                    Color.hsv(360f, 1f, 1f)
                )
            )
            drawRect(gradient)

            val selectorX = (hue / 360f) * size.width
            drawCircle(
                color = Color.White,
                radius = 12.dp.toPx(),
                center = Offset(selectorX, size.height / 2),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
private fun ColorSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    startColor: Color,
    endColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val newValue = (change.position.x / size.width).coerceIn(0f, 1f)
                        onValueChange(newValue)
                    }
                }
        ) {
            val gradient = Brush.horizontalGradient(
                colors = listOf(startColor, endColor)
            )
            drawRect(gradient)

            val selectorX = value * size.width
            drawCircle(
                color = Color.White,
                radius = 12.dp.toPx(),
                center = Offset(selectorX, size.height / 2),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
