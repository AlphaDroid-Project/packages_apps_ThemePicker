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
package com.android.axion.themepicker.ui.preferences

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
import kotlin.math.*
import androidx.compose.material3.MaterialTheme
import com.android.axion.themepicker.utils.math.scaleRatio

@Composable
fun PreferenceCard(
    title: String,
    description: String? = null,
    enabled: Boolean = true,
    checked: Boolean? = null,
    color: Color? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val scale = LocalContext.current.scaleRatio

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            when {
                checked != null && onCheckedChange != null -> {
                    Switch(
                        checked = checked,
                        onCheckedChange = onCheckedChange,
                        enabled = enabled
                    )
                }
                color != null -> {
                    Box(
                        modifier = Modifier
                            .size(48.dp * scale)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
fun SliderCard(
    title: String,
    description: String? = null,
    value: Float,
    onValueChange: ((Float) -> Unit) = {},
    onValueChangeFinished: ((Float) -> Unit) = {},
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    defaultValue: Float
) {
    val colors = MaterialTheme.colorScheme
    val scale = LocalContext.current.scaleRatio

    var internalValue by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        internalValue = value
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            description?.let {
                Spacer(modifier = Modifier.height(4.dp * scale))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp * scale))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp * scale)
            ) {
                Text(
                    text = "Value: ${internalValue.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )

                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset to default",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier
                        .size(14.dp * scale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    internalValue = defaultValue
                                    onValueChange(defaultValue)
                                    onValueChangeFinished(defaultValue)
                                }
                            )
                        }
                )
            }

            Slider(
                value = internalValue,
                onValueChange = {
                    internalValue = it
                    onValueChange(it)
                },
                onValueChangeFinished = { onValueChangeFinished(internalValue) },
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    valueRange.start.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
                Text(
                    valueRange.endInclusive.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PreferenceGroupCard(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val scale = LocalContext.current.scaleRatio
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp * scale),
            verticalArrangement = Arrangement.spacedBy(12.dp * scale),
            content = content
        )
    }
}
