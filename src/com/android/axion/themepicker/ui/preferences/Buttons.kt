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

import android.os.*
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.carousel.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.painter.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.*
import androidx.compose.material3.MaterialTheme
import com.android.axion.themepicker.utils.math.scaleRatio

@Composable
fun ToggleButton(
    icon: ImageVector? = null,
    bitmap: ImageBitmap? = null,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    backgroundColor: Color? = null,
    primaryTint: Color? = null
) {
    val colors = MaterialTheme.colorScheme
    val ratio = LocalContext.current.scaleRatio
    val iconSize = 32.dp * ratio

    val scale by animateFloatAsState(
        targetValue = if (enabled) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                enabled = !isLoading,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(iconSize * 2)
                .clip(CircleShape)
                .background(
                    backgroundColor ?: if (enabled) colors.primary.copy(alpha = 0.2f)
                    else colors.surface
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp * ratio),
                    color = primaryTint ?: contentColorFor(backgroundColor ?: Color.Gray),
                    strokeWidth = 2.dp
                )
            } else {
                when {
                    bitmap != null -> {
                        Image(
                            bitmap = bitmap,
                            contentDescription = label,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    icon != null -> {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = primaryTint ?: if (enabled) colors.primary
                            else colors.secondary,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp * ratio))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = primaryTint ?: colors.onSurface
        )

        Text(
            text = if (enabled) "On" else "Off",
            style = MaterialTheme.typography.bodySmall,
            color = primaryTint?.copy(alpha = 0.85f) ?: colors.onSurfaceVariant
        )
    }
}


@Composable
fun IconButtonCircle(
    icon: ImageVector? = null,
    bitmap: ImageBitmap? = null,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val scale = LocalContext.current.scaleRatio
    val buttonSize = 72.dp * scale
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable (
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(buttonSize)
                .background(
                    color = colors.surface,
                    shape = CircleShape
                )
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) colors.primary else Color.Transparent,
                    shape = CircleShape
                )
                .padding(12.dp * scale)
        ) {
            when {
                bitmap != null -> Image(bitmap = bitmap, contentDescription = label)
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) colors.primary else colors.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp * scale))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) colors.primary else colors.onSurface
        )
    }
}
