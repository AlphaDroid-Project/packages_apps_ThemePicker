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

package com.android.axion.themepicker.ui.lockscreen.widgets

import android.appwidget.AppWidgetHostView
import android.view.ViewGroup
import android.widget.RemoteViews
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import com.android.axion.themepicker.ui.lockscreen.Dimens
import com.android.axion.themepicker.ui.lockscreen.previewScale
import com.android.axion.themepicker.utils.math.scaleRatio

private val WidgetPlacementSpec =
    spring<Float>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioLowBouncy,
    )

@Composable
fun WidgetGrid(
    isPreview: Boolean = false,
    widgets: List<GridWidgetItem>,
    hostViews: Map<Int, AppWidgetHostView> = emptyMap(),
    onRemove: (GridWidgetItem) -> Unit,
    onPickWidget: () -> Unit,
    onResizeWidget: ((GridWidgetItem) -> Unit)? = null,
    dragDropState: WidgetDragDropState? = null,
    onWidgetsMoved: ((List<GridWidgetItem>) -> Unit)? = null,
) {
    val context = LocalContext.current
    val scale = if (isPreview) context.previewScale else context.scaleRatio

    val cellSize = Dimens.WidgetCellSize * scale
    val gap = Dimens.WidgetCellGap * scale
    val cornerRadius = Dimens.WidgetCellCorner * scale
    val gridWidth = GRID_COLUMNS * cellSize + (GRID_COLUMNS - 1) * gap
    val gridHeight = MAX_ROWS * cellSize + (MAX_ROWS - 1) * gap

    val displayWidgets =
        if (dragDropState?.dragInProgress == true) {
            dragDropState.previewWidgets
        } else {
            widgets
        }

    val occupied = remember(displayWidgets) { buildOccupiedGrid(displayWidgets) }

    val density = LocalDensity.current
    var gridOrigin by remember { mutableStateOf(Offset.Zero) }
    val cellSizePx = with(density) { cellSize.toPx() }
    val gapPx = with(density) { gap.toPx() }

    Box(
        modifier =
            Modifier.width(gridWidth)
                .height(gridHeight)
                .onGloballyPositioned { coords -> gridOrigin = coords.positionInRoot() }
                .then(
                    if (dragDropState != null && !isPreview) {
                        Modifier.widgetDropTarget(
                            gridOrigin = { gridOrigin },
                            cellSizePx = cellSizePx,
                            gapPx = gapPx,
                            state = dragDropState,
                            onDrop = { newWidgets -> onWidgetsMoved?.invoke(newWidgets) },
                        )
                    } else Modifier
                ),
        contentAlignment = Alignment.TopStart,
    ) {
        for (row in 0 until MAX_ROWS) {
            for (col in 0 until GRID_COLUMNS) {
                if (!occupied[row][col]) {
                    val x = col * (cellSize + gap)
                    val y = row * (cellSize + gap)
                    Box(
                        modifier =
                            Modifier.offset(x = x, y = y)
                                .size(cellSize)
                                .clip(RoundedCornerShape(cornerRadius))
                                .background(Color.White.copy(alpha = 0.25f))
                                .border(
                                    width = 1.dp * scale,
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(cornerRadius),
                                )
                                .then(
                                    if (!isPreview)
                                        Modifier.clickable(
                                            indication = null,
                                            interactionSource =
                                                remember { MutableInteractionSource() },
                                        ) {
                                            onPickWidget()
                                        }
                                    else Modifier
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!isPreview) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp * scale),
                            )
                        }
                    }
                }
            }
        }

        if (dragDropState != null && dragDropState.dragInProgress) {
            val target = dragDropState.dropTarget
            if (target != null && !target.isValid) {
                val dragProv = dragDropState.draggedProvider
                val dragged = widgets.firstOrNull { it.provider == dragProv }
                if (dragged != null) {
                    val tx = target.cellX * (cellSize + gap)
                    val ty = target.cellY * (cellSize + gap)
                    val tw = dragged.spanX * cellSize + (dragged.spanX - 1).coerceAtLeast(0) * gap
                    val th = dragged.spanY * cellSize + (dragged.spanY - 1).coerceAtLeast(0) * gap

                    Box(
                        modifier =
                            Modifier.offset(x = tx, y = ty)
                                .size(width = tw, height = th)
                                .clip(RoundedCornerShape(cornerRadius))
                                .background(Color.Red.copy(alpha = 0.20f))
                                .border(
                                    width = 1.5.dp * scale,
                                    color = Color.Red.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(cornerRadius),
                                )
                    )
                }
            }
        }

        displayWidgets.forEach { widget ->
            val targetX = widget.cellX * (cellSizePx + gapPx)
            val targetY = widget.cellY * (cellSizePx + gapPx)
            val w = widget.spanX * cellSize + (widget.spanX - 1).coerceAtLeast(0) * gap
            val h = widget.spanY * cellSize + (widget.spanY - 1).coerceAtLeast(0) * gap
            val isBeingDragged = dragDropState?.isDragging(widget) == true

            key(widget.appWidgetId, widget.provider, widget.spanX, widget.spanY) {
                val animatedX by
                    animateFloatAsState(
                        targetValue = targetX,
                        animationSpec = WidgetPlacementSpec,
                        label = "widgetX",
                    )
                val animatedY by
                    animateFloatAsState(
                        targetValue = targetY,
                        animationSpec = WidgetPlacementSpec,
                        label = "widgetY",
                    )

                val widgetView = hostViews[widget.appWidgetId]

                val info =
                    remember(widget.provider) {
                        if (widgetView != null) null else widget.providerInfo(context)
                    }
                val previewBitmap =
                    remember(widget.provider) {
                        if (widgetView != null) null
                        else
                            info
                                ?.loadPreviewImage(
                                    context,
                                    context.resources.displayMetrics.densityDpi,
                                )
                                ?.let { drawable ->
                                    val iw =
                                        drawable.intrinsicWidth.takeIf { it > 0 }
                                            ?: (200 * context.resources.displayMetrics.density)
                                                .toInt()
                                    val ih =
                                        drawable.intrinsicHeight.takeIf { it > 0 }
                                            ?: (100 * context.resources.displayMetrics.density)
                                                .toInt()
                                    drawable.toBitmap(iw, ih)
                                }
                    }
                val hasPreviewLayout = info != null && info.previewLayout != 0
                val iconDrawable =
                    remember(widget.provider) {
                        if (widgetView != null) null else widget.icon(context)
                    }
                val labelText = remember(widget.provider) { widget.label(context) }

                Box(
                    modifier =
                        Modifier.offset { IntOffset(animatedX.toInt(), animatedY.toInt()) }
                            .size(width = w, height = h)
                            .graphicsLayer { alpha = if (isBeingDragged) 0.3f else 1f }
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(
                                Color.White.copy(alpha = if (widgetView != null) 0f else 0.22f)
                            )
                            .then(
                                if (!isPreview && dragDropState != null) {
                                    Modifier.widgetDragSource(widget, widgets, dragDropState)
                                } else if (!isPreview) {
                                    Modifier.clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        onRemove(widget)
                                    }
                                } else Modifier
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (widgetView != null && !isPreview) {

                        AndroidView(
                            factory = { _ ->
                                (widgetView.parent as? ViewGroup)?.removeView(widgetView)
                                widgetView
                            },
                            modifier = Modifier.fillMaxSize(),
                            update = { view -> view.requestLayout() },
                        )
                    } else if (previewBitmap != null) {

                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = labelText,
                            modifier = Modifier.fillMaxSize().padding(2.dp * scale),
                            contentScale = ContentScale.Fit,
                        )
                    } else if (hasPreviewLayout && info != null) {

                        var layoutFailed by remember { mutableStateOf(false) }
                        if (!layoutFailed) {
                            AndroidView(
                                factory = { ctx ->
                                    WidgetPreviewHostView(ctx).apply {
                                        try {
                                            setAppWidget(-1, info)
                                            val rv =
                                                RemoteViews(
                                                    info.provider.packageName,
                                                    info.previewLayout,
                                                )
                                            updateAppWidget(rv)
                                        } catch (_: Exception) {
                                            layoutFailed = true
                                        }
                                        post { if (childCount == 0) layoutFailed = true }
                                    }
                                },
                                modifier = Modifier.fillMaxSize().padding(2.dp * scale),
                                update = { view ->
                                    view.setContainerSizePx(
                                        (w.value * context.resources.displayMetrics.density)
                                            .toInt(),
                                        (h.value * context.resources.displayMetrics.density).toInt(),
                                    )
                                    view.requestLayout()
                                },
                            )
                        }
                    } else {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(4.dp * scale),
                        ) {
                            iconDrawable?.let { drawable ->
                                Image(
                                    bitmap =
                                        drawable
                                            .toBitmap(
                                                width =
                                                    (24 * context.resources.displayMetrics.density)
                                                        .toInt(),
                                                height =
                                                    (24 * context.resources.displayMetrics.density)
                                                        .toInt(),
                                            )
                                            .asImageBitmap(),
                                    contentDescription = labelText,
                                    modifier = Modifier.size(20.dp * scale),
                                )
                            }
                                ?: Icon(
                                    Icons.Default.Widgets,
                                    contentDescription = labelText,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp * scale),
                                )

                            if (widget.spanX > 1 || widget.spanY > 1) {
                                Spacer(Modifier.height(2.dp * scale))
                                Text(
                                    text = labelText,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp * scale,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    if (!isPreview) {

                        Box(
                            modifier =
                                Modifier.align(Alignment.TopEnd)
                                    .padding(3.dp * scale)
                                    .size(14.dp * scale)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = 0.85f))
                                    .clickable { onRemove(widget) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(9.dp * scale),
                            )
                        }

                        if (onResizeWidget != null) {
                            Box(
                                modifier =
                                    Modifier.align(Alignment.BottomEnd)
                                        .padding(3.dp * scale)
                                        .size(14.dp * scale)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.85f))
                                        .clickable { onResizeWidget(widget) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.OpenInFull,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(9.dp * scale),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
