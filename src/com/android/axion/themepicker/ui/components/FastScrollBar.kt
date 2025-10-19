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
package com.android.axion.themepicker.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme

@Composable
fun <T> FastScrollBar(
    items: List<T>,
    currentIndex: Int,
    onItemSelected: (Int) -> Unit,
    labelExtractor: (T) -> String
) {
    val colors = LocalAxColorScheme.current
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toList()

    val groupedItems = items.drop(1).groupBy { 
        labelExtractor(it).firstOrNull()?.uppercaseChar() ?: 'A'
    }

    val currentLetter = if (currentIndex in items.indices) {
        labelExtractor(items[currentIndex]).firstOrNull()?.uppercaseChar() ?: 'A'
    } else null

    Surface(
        modifier = Modifier
            .padding(end = 12.dp)
            .width(52.dp)
            .heightIn(max = 600.dp),
        shape = RoundedCornerShape(26.dp),
        color = colors.surfaceContainerLowest.copy(alpha = 0.98f),
        border = BorderStroke(2.dp, colors.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            alphabet.forEach { letter ->
                val hasItems = groupedItems.containsKey(letter)
                val isCurrentLetter = letter == currentLetter

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCurrentLetter) colors.primaryContainer else Color.Transparent
                        )
                        .clickable(enabled = hasItems) {
                            groupedItems[letter]?.firstOrNull()?.let { item ->
                                val index = items.indexOf(item)
                                if (index >= 0) onItemSelected(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = when {
                            isCurrentLetter -> FontWeight.ExtraBold
                            hasItems -> FontWeight.Bold
                            else -> FontWeight.Normal
                        },
                        color = when {
                            isCurrentLetter -> colors.textPrimaryInverse
                            hasItems -> colors.onSurface
                            else -> colors.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                    )
                }
            }
        }
    }
}
