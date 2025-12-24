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
package com.android.axion.themepicker.ui.app

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.axion.themepicker.ui.lockscreen.LockscreenPreview
import com.android.axion.themepicker.ui.preview.HomescreenPreview
import com.android.axion.themepicker.utils.wallpaper.centerCrop
import com.android.axion.themepicker.utils.wallpaper.getCurrentWallpaperBitmap

val WallpaperMiniPreviewsWidth = 140.dp
val WallpaperMiniPreviewsHeight = 300.dp

val WallpaperPreviewsWidth = 160.dp
val WallpaperPreviewsHeight = 340.dp

@Composable
fun PreviewsPage(
    isHome: Boolean,
    refreshKey: Any? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wallpaperBitmap = remember(refreshKey) {
        getCurrentWallpaperBitmap(context, isHome)
    }
    
    Box(modifier = modifier) {
        if (isHome) {
            wallpaperBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Home screen preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                HomescreenPreview(
                    modifier = Modifier.fillMaxSize(),
                    wallpaperDrawable = BitmapDrawable(
                        context.resources,
                        centerCrop(context, bitmap)
                    ),
                    refreshKey = refreshKey
                )
            }
        } else {
            LockscreenPreview(
                isPreview = true,
                wallpaperBitmap = wallpaperBitmap,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
