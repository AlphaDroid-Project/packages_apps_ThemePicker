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

package com.android.axion.themepicker.ui.lockscreen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.unit.*

@Composable
fun DummyNotifications(isPreview: Boolean, scale: Float, modifier: Modifier = Modifier) {
    val cardCorner = Dimens.NotificationCardCorner * scale
    val cardHeight = Dimens.NotificationCardHeight * scale
    val gap = Dimens.NotificationCardGap * scale
    val padding = 16.dp * scale

    Column(
        modifier = modifier.padding(horizontal = padding),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        repeat(2) { index ->
            val lineWidthFraction =
                when (index) {
                    0 -> 0.7f
                    else -> 0.55f
                }
            NotificationCard(
                scale = scale,
                cardHeight = cardHeight,
                cardCorner = cardCorner,
                titleWidthFraction = lineWidthFraction,
                bodyWidthFraction = lineWidthFraction * 0.7f,
            )
        }
    }
}

@Composable
private fun NotificationCard(
    scale: Float,
    cardHeight: Dp,
    cardCorner: Dp,
    titleWidthFraction: Float,
    bodyWidthFraction: Float,
) {
    val colors = MaterialTheme.colorScheme
    val iconSize = 28.dp * scale
    val shimmerColor = colors.onSurface.copy(alpha = 0.12f)
    val titleHeight = 10.dp * scale
    val bodyHeight = 8.dp * scale
    val shimmerCorner = 4.dp * scale

    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(cardHeight)
                .clip(RoundedCornerShape(cardCorner))
                .background(colors.surfaceBright)
                .padding(horizontal = 12.dp * scale, vertical = 10.dp * scale)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp * scale),
        ) {
            Box(modifier = Modifier.size(iconSize).clip(CircleShape).background(shimmerColor))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp * scale),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxWidth(titleWidthFraction)
                            .height(titleHeight)
                            .clip(RoundedCornerShape(shimmerCorner))
                            .background(shimmerColor)
                )
                Box(
                    modifier =
                        Modifier.fillMaxWidth(bodyWidthFraction)
                            .height(bodyHeight)
                            .clip(RoundedCornerShape(shimmerCorner))
                            .background(shimmerColor)
                )
            }
        }
    }
}
