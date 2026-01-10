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
package com.android.axion.themepicker.ui.lockscreen

import android.util.Log
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.core.graphics.drawable.toBitmap
import com.android.axion.themepicker.R
import com.android.axion.themepicker.data.model.Screen.EntryPoint
import com.android.axion.themepicker.utils.math.scaleRatio
import com.android.axion.themepicker.utils.wallpaper.getCurrentWallpaperBitmap
import kotlinx.coroutines.*
import kotlin.math.*
import java.util.*

@Composable
fun LockscreenPreview(
    isPreview: Boolean = false,
    wallpaperBitmap: Bitmap? = null,
    modifier: Modifier = Modifier,
    entryPoint: EntryPoint = EntryPoint.DEFAULT
) {
    val context = LocalContext.current
    val wallpaper = wallpaperBitmap ?: getCurrentWallpaperBitmap(context, false)!!
    var showPicker by remember { mutableStateOf(false) }
    var showAffordancePicker by remember { mutableStateOf<AffordanceSlot?>(null) }
    var widgetItems by remember { mutableStateOf(load(context, isPreview)) }
    val scale = if (isPreview) context.previewScale else context.scaleRatio

    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(entryPoint) {
        when (entryPoint) {
            EntryPoint.WIDGETS -> showPicker = true
            EntryPoint.SHORTCUTS -> showAffordancePicker = AffordanceSlot.BOTTOM_START
            else -> {}
        }
        Log.d("AffordanceOverlay", "entryPoint=$entryPoint showAffordancePicker=$showAffordancePicker showPicker=$showPicker")
    }

    fun updateWidgets(newItems: List<WidgetItem>) {
        widgetItems = newItems
        if (!isPreview) save(context, newItems)
    }

    Box(modifier = modifier) {
        wallpaper?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = Dimens.ClockTopPadding * scale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PreviewClock(isPreview)
            if (!isPreview) Spacer(modifier = Modifier.height(Dimens.ClockSpacer * scale))
            WidgetGrid(
                isPreview = isPreview,
                widgetItems = widgetItems,
                onUpdate = { removedItem ->
                    if (!isPreview) updateWidgets(widgetItems - removedItem)
                },
                onReorder = { if (!isPreview) updateWidgets(it) },
                onPickWidget = { if (!isPreview) showPicker = true }
            )
        }

        AffordanceOverlay(
            isPreview = isPreview,
            scale = scale,
            showPickerOnLaunch = showAffordancePicker
        )

        if (!isPreview) {
            WidgetPickerBottomSheet(
                visible = showPicker,
                current = widgetItems,
                onDismiss = { showPicker = false },
                onSelect = { selected ->
                    val newItems = widgetItems + selected
                    updateWidgets(newItems)
                }
            )
        }
    }
}
