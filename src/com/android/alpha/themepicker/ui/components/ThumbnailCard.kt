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

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.alpha.themepicker.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.*
import com.android.alpha.themepicker.utils.wallpaper.rememberBitmap

@Composable
fun ThumbnailCard(
    drawableRes: Int?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val bitmap = drawableRes?.let { rememberBitmap(it, size, size) }

    Card(
        modifier = modifier.size(size).aspectRatio(1f).clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceBright),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(colors.surfaceBright),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            } else {
                LoadingIndicator(modifier = Modifier.size(32.dp), color = colors.primary)
            }
        }
    }
}
