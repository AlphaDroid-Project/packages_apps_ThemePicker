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

package com.android.axion.themepicker.ui.preview

import android.graphics.Bitmap
import android.graphics.Point
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.axion.themepicker.R
import com.android.axion.themepicker.ui.lockscreen.SimpleLockscreenPreview

@Composable
fun DualDisplayPreviewWithOverlay(
    wallpaperBitmap: Bitmap?,
    foldedDisplaySize: Point,
    unfoldedDisplaySize: Point,
    isHome: Boolean,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    spacing: Dp = 12.dp,
) {
    val foldedAR = foldedDisplaySize.x.toFloat() / foldedDisplaySize.y
    val unfoldedAR = unfoldedDisplaySize.x.toFloat() / unfoldedDisplaySize.y

    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        DisplayPreviewCardWithOverlay(
            wallpaperBitmap = wallpaperBitmap,
            aspectRatio = unfoldedAR,
            isHome = isHome,
            label = stringResource(R.string.unfolded_display),
            cornerRadius = cornerRadius,
            modifier = Modifier.weight(unfoldedAR),
        )

        DisplayPreviewCardWithOverlay(
            wallpaperBitmap = wallpaperBitmap,
            aspectRatio = foldedAR,
            isHome = isHome,
            label = stringResource(R.string.folded_display),
            cornerRadius = cornerRadius,
            modifier = Modifier.weight(foldedAR),
        )
    }
}

@Composable
private fun DisplayPreviewCardWithOverlay(
    wallpaperBitmap: Bitmap?,
    aspectRatio: Float,
    isHome: Boolean,
    label: String,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(cornerRadius)

    Card(
        modifier = modifier.aspectRatio(aspectRatio),
        shape = shape,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize().clip(shape)) {
            if (isHome) {
                SimpleHomescreenPreview(
                    wallpaperBitmap = wallpaperBitmap,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                SimpleLockscreenPreview(
                    wallpaperBitmap = wallpaperBitmap,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
                                        )
                                ),
                            shape =
                                RoundedCornerShape(
                                    bottomStart = cornerRadius,
                                    bottomEnd = cornerRadius,
                                ),
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                )
            }
        }
    }
}
