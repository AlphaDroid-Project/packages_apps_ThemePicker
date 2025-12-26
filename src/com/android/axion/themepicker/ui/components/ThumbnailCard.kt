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
import androidx.compose.ui.unit.*
import androidx.compose.material3.MaterialTheme
import com.android.axion.themepicker.utils.wallpaper.rememberBitmap

@Composable
fun ThumbnailCard(
    drawableRes: Int?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val bitmap = drawableRes?.let { rememberBitmap(it, size, size) }

    Card(
        modifier = modifier
            .size(size)
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.surface),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    color = colors.primary
                )
            }
        }
    }
}
