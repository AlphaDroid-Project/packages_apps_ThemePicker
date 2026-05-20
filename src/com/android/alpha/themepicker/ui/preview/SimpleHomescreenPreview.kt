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

package com.android.alpha.themepicker.ui.preview

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.alpha.themepicker.utils.wallpaper.getCurrentWallpaperBitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SimpleHomescreenPreview(wallpaperBitmap: Bitmap? = null, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by
        produceState(wallpaperBitmap, wallpaperBitmap) {
            value =
                wallpaperBitmap
                    ?: withContext(Dispatchers.IO) { getCurrentWallpaperBitmap(context, true) }
        }
    val imageBitmap = bitmap?.let { remember(it) { it.asImageBitmap() } }

    Box(modifier = modifier) {
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            StatusBarHint(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
            )

            val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            Text(
                text = dateFormat.format(Date()),
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 12.dp, top = 2.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            AppGridPlaceholder(
                rows = 3,
                columns = 4,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            )

            Spacer(modifier = Modifier.height(6.dp))

            SearchBarPlaceholder(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(4.dp))

            DockPlaceholder(
                count = 5,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun StatusBarHint(modifier: Modifier = Modifier) {
    val dateFormat = SimpleDateFormat("h:mm", Locale.getDefault())

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = dateFormat.format(Date()),
            color = Color.White,
            fontSize = 6.sp,
            fontWeight = FontWeight.Medium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(3) {
                Box(
                    modifier =
                        Modifier.size(3.dp).background(Color.White.copy(alpha = 0.7f), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun AppGridPlaceholder(rows: Int, columns: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                repeat(columns) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier.size(20.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.25f),
                                        RoundedCornerShape(5.dp),
                                    )
                        )
                        Box(
                            modifier =
                                Modifier.width(16.dp)
                                    .height(2.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.3f),
                                        RoundedCornerShape(1.dp),
                                    )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBarPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier =
                    Modifier.size(8.dp).background(Color.White.copy(alpha = 0.4f), CircleShape)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier =
                        Modifier.size(6.dp).background(Color.White.copy(alpha = 0.3f), CircleShape)
                )
                Box(
                    modifier =
                        Modifier.size(6.dp).background(Color.White.copy(alpha = 0.3f), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun DockPlaceholder(count: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceEvenly) {
        repeat(count) {
            Box(
                modifier =
                    Modifier.size(20.dp).background(Color.White.copy(alpha = 0.25f), CircleShape)
            )
        }
    }
}
