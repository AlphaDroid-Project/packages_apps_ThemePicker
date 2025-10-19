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

import android.widget.Toast
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
import com.android.axion.themepicker.R
import com.android.axion.themepicker.ui.components.CommonBottomSheet
import com.android.axion.themepicker.ui.components.PagedTilePicker
import com.android.axion.themepicker.ui.components.SheetDimens

@Composable
fun WidgetPickerBottomSheet(
    visible: Boolean,
    current: List<WidgetItem>,
    onDismiss: () -> Unit,
    onSelect: (WidgetItem) -> Unit
) {
    var showSizeOptions by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val available = WidgetsList().filter { it !in current.map { it.name } }

    val titleText = showSizeOptions?.replaceFirstChar { it.uppercase() }
        ?: stringResource(R.string.widgets_title)

    CommonBottomSheet(
        visible = visible,
        title = titleText,
        surfaceColor = if (showSizeOptions != null) surface() else null,
        onDismiss = {
            showSizeOptions = null
            onDismiss()
        }
    ) {
        if (showSizeOptions != null) {
            SizePicker(
                widgetName = showSizeOptions!!,
                current = current,
                onSelect = onSelect,
                onDismiss = {
                    showSizeOptions = null
                    onDismiss()
                },
                onBack = { showSizeOptions = null }
            )
        } else {
            PagedTilePicker(
                items = available,
                icon = { WidgetIcon(it) },
                label = { it.replaceFirstChar(Char::uppercase) },
                onSelect = { name -> showSizeOptions = name }
            )
        }
    }
}

@Composable
private fun SizePicker(
    widgetName: String,
    current: List<WidgetItem>,
    onSelect: (WidgetItem) -> Unit,
    onDismiss: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val warning = stringResource(R.string.no_space_available)

    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = SheetDimens.SheetTopPadding),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(1, 2).forEach { span ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(Color.Transparent, RoundedCornerShape(Dimens.WidgetCorner))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            val totalSpan = current.sumOf { it.span } + span
                            if (totalSpan > 4) {
                                Toast.makeText(context, warning, Toast.LENGTH_SHORT).show()
                            } else {
                                onSelect(WidgetItem(widgetName, span))
                                onDismiss()
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .width(if (span == 1) Dimens.WidgetSlot else Dimens.WidgetSlot * 2)
                            .height(Dimens.WidgetSlot),
                        contentAlignment = Alignment.Center
                    ) {
                        WidgetItem(widgetName, span).Render(onRemove = {}, showRemove = false)
                    }
                    Spacer(Modifier.height(SheetDimens.SheetSpacerSmall))
                    Text("${span}x1", color = onSurface())
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(SheetDimens.SheetTopPadding)
                .size(SheetDimens.SheetCloseSize)
                .clip(CircleShape)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.back),
                tint = onSurface()
            )
        }
    }
}
