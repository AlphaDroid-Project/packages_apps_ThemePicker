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
package com.android.axion.themepicker.ui.preview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
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
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.utils.wallpaper.getCurrentWallpaperBitmap

val ColorPreviewsHeight = 420.dp
val ColorPreviewsWidth = 204.dp
val ColorPreviewsCornerSize = RoundedCornerShape(32.dp)

@Composable
fun WorkspacePreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current
        val wallpaperBitmap: Bitmap? = remember {
            getCurrentWallpaperBitmap(context, true)
        }

        val iconColor = LocalAxColorScheme.current.surfaceContainerLowest

        Box(
            modifier = Modifier
                .width(ColorPreviewsWidth)
                .height(ColorPreviewsHeight)
                .clip(ColorPreviewsCornerSize)
                .background(LocalAxColorScheme.current.primary)
                .padding(1.5.dp)
        ) {
            wallpaperBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Wallpaper background",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(ColorPreviewsCornerSize),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(iconColor)
                        )
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(iconColor)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .background(iconColor)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(iconColor)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(iconColor)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(iconColor)
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(iconColor)
                        )
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(iconColor)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(iconColor)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(iconColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorPreview() {
    val colors = LocalAxColorScheme.current
    val bgColor = colors.axBackground
    val accentColor = colors.primary
    val secColor = colors.tertiary
    val surface = colors.axSurface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(ColorPreviewsWidth)
                .height(ColorPreviewsHeight)
                .background(
                    bgColor,
                    shape = RoundedCornerShape(32.dp)
                )
                .border(
                    width = 3.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {}
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(accentColor, shape = CircleShape)
                        )
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(secColor, shape = CircleShape)
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(surface.copy(alpha = 0.9f), shape = CircleShape)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(secColor, shape = CircleShape)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(surface.copy(alpha = 0.9f), shape = CircleShape)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(secColor, shape = CircleShape)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(surface.copy(alpha = 0.9f), shape = CircleShape)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(secColor, shape = CircleShape)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(surface.copy(alpha = 0.9f), shape = CircleShape)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(accentColor, shape = CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickSettingsPreview() {
    val colors = LocalAxColorScheme.current
    val accentColor = colors.primary
    val qsSurface = colors.axSurface
    val bgColor = colors.axBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(ColorPreviewsWidth)
                .height(ColorPreviewsHeight)
                .background(bgColor, shape = RoundedCornerShape(32.dp))
                .border(
                    width = 3.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(IntrinsicSize.Min)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(accentColor, shape = CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(accentColor, shape = CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxHeight()
                                .background(qsSurface, shape = CircleShape)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(IntrinsicSize.Min)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxHeight()
                                .background(qsSurface, shape = CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(qsSurface, shape = CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(qsSurface, shape = CircleShape)
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(qsSurface, shape = CircleShape)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(accentColor, shape = CircleShape)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(qsSurface, shape = CircleShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .fillMaxWidth()
                            .background(accentColor, shape = CircleShape)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(qsSurface, shape = CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(qsSurface, shape = CircleShape)
                    )
                }
            }
        }
    }
}
