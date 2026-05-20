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

package com.android.alpha.themepicker.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.android.alpha.themepicker.utils.math.scaleRatio

@Composable
fun FooterIndicator(tabCount: Int, currentPage: Int) {
    val scale = LocalContext.current.scaleRatio
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.wrapContentHeight().fillMaxWidth().padding(bottom = 16.dp * scale),
    ) {
        repeat(tabCount) { index ->
            val selected = currentPage == index
            Box(
                modifier =
                    Modifier.size(8.dp * scale)
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                        .padding(4.dp * scale)
            )
            if (index < tabCount - 1) Spacer(modifier = Modifier.width(8.dp * scale))
        }
    }
}

@Composable
fun FooterCard(title: String, description: String, modifier: Modifier = Modifier.fillMaxWidth()) {
    val colors = MaterialTheme.colorScheme
    val scale = LocalContext.current.scaleRatio
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.secondaryContainer),
    ) {
        Row(
            modifier = Modifier.padding(16.dp * scale),
            horizontalArrangement = Arrangement.spacedBy(12.dp * scale),
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = colors.onSecondaryContainer)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSecondaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp * scale))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSecondaryContainer,
                )
            }
        }
    }
}
