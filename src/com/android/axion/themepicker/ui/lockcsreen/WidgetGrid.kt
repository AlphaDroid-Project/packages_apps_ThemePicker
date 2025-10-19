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

import com.android.axion.themepicker.ui.lockscreen.Dimens.WidgetBorderWidth
import com.android.axion.themepicker.ui.lockscreen.Dimens.WidgetContainerHeight
import com.android.axion.themepicker.ui.lockscreen.Dimens.WidgetContainerPadding
import com.android.axion.themepicker.ui.lockscreen.Dimens.WidgetCorner
import com.android.axion.themepicker.ui.lockscreen.Dimens.WidgetSlot
import com.android.axion.themepicker.ui.lockscreen.Dimens.WidgetSpacing

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
import com.android.axion.themepicker.draganddrop.DragAndDropGrid
import kotlinx.coroutines.*
import kotlin.math.*
import java.util.*

@Composable
fun WidgetGrid(
    isPreview: Boolean = false,
    widgetItems: List<WidgetItem>,
    onUpdate: (WidgetItem) -> Unit,
    onReorder: (List<WidgetItem>) -> Unit,
    onPickWidget: () -> Unit
) {
    val context = LocalContext.current
    val scale = if (isPreview) context.previewScale - 0.2f else context.scaleRatio
    val widgetSlot = scale * WidgetSlot
    val spacing = scale * WidgetSpacing

    val previewItems = if (isPreview) {
        remember(widgetItems) { widgetItems.toList() }
    } else {
        widgetItems
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.WidgetContainerHeight * scale),
            contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    Dimens.WidgetBorderWidth,
                    if (isPreview) Color.Transparent else Color.White.copy(alpha = 0.5f),
                    RoundedCornerShape(Dimens.WidgetCorner)
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (!isPreview) onPickWidget()
                }
        )

        DragAndDropGrid(
            items = previewItems,
            span = { it.span },
            width = { widgetSlot * it.span },
            height = { widgetSlot },
            spacing = spacing,
            onReorder = onReorder,
            enabled = !isPreview,
        ) { item ->
            item.Render(onRemove = { onUpdate(item) })
        }
    }
}
