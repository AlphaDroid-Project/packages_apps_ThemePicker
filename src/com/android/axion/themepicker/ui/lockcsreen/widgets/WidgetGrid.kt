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

import android.appwidget.AppWidgetProviderInfo
import android.view.ViewGroup
import android.widget.RemoteViews
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import com.android.axion.themepicker.ui.lockscreen.Dimens
import com.android.axion.themepicker.ui.lockscreen.previewScale
import com.android.axion.themepicker.utils.math.scaleRatio
import kotlin.math.roundToInt

private fun occupancyExcluding(
    widgets: List<GridWidgetItem>,
    excludeId: Int,
): Array<BooleanArray> {
    val matrix = Array(MAX_ROWS) { BooleanArray(GRID_COLUMNS) }
    widgets.forEach { w ->
        if (w.appWidgetId == excludeId) return@forEach
        val yEnd = (w.cellY + w.spanY).coerceAtMost(MAX_ROWS)
        val xEnd = (w.cellX + w.spanX).coerceAtMost(GRID_COLUMNS)
        for (y in w.cellY until yEnd) {
            for (x in w.cellX until xEnd) {
                if (y in 0 until MAX_ROWS && x in 0 until GRID_COLUMNS) matrix[y][x] = true
            }
        }
    }
    return matrix
}

private fun hasCollision(
    occupancy: Array<BooleanArray>,
    cellX: Int,
    cellY: Int,
    spanX: Int,
    spanY: Int,
): Boolean {
    for (y in cellY until cellY + spanY) {
        for (x in cellX until cellX + spanX) {
            if (y !in 0 until MAX_ROWS || x !in 0 until GRID_COLUMNS) return true
            if (occupancy[y][x]) return true
        }
    }
    return false
}

private fun maxSpanXFor(
    occupancy: Array<BooleanArray>,
    cellX: Int,
    cellY: Int,
    spanY: Int,
    desiredX: Int,
): Int {
    val maxX = desiredX.coerceIn(1, GRID_COLUMNS - cellX)
    for (sx in 1..maxX) {
        val x = cellX + sx - 1
        for (y in cellY until (cellY + spanY).coerceAtMost(MAX_ROWS)) {
            if (y !in 0 until MAX_ROWS || x !in 0 until GRID_COLUMNS || occupancy[y][x]) {
                return (sx - 1).coerceAtLeast(1)
            }
        }
    }
    return maxX
}

private fun maxSpanYFor(
    occupancy: Array<BooleanArray>,
    cellX: Int,
    cellY: Int,
    spanX: Int,
    desiredY: Int,
): Int {
    val maxY = desiredY.coerceIn(1, MAX_ROWS - cellY)
    for (sy in 1..maxY) {
        val y = cellY + sy - 1
        for (x in cellX until (cellX + spanX).coerceAtMost(GRID_COLUMNS)) {
            if (y !in 0 until MAX_ROWS || x !in 0 until GRID_COLUMNS || occupancy[y][x]) {
                return (sy - 1).coerceAtLeast(1)
            }
        }
    }
    return maxY
}

private fun clampNoCollision(
    occupancy: Array<BooleanArray>,
    cellX: Int,
    cellY: Int,
    currentSpanX: Int,
    currentSpanY: Int,
    desiredX: Int,
    desiredY: Int,
): Pair<Int, Int> {
    val nx = maxSpanXFor(occupancy, cellX, cellY, currentSpanY, desiredX)
    val ny = maxSpanYFor(occupancy, cellX, cellY, nx, desiredY)
    return nx to ny
}

private val WidgetPlacementSpec =
    spring<Float>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioLowBouncy,
    )

@Composable
fun WidgetGrid(
    isPreview: Boolean = false,
    widgets: List<GridWidgetItem>,
    onRemove: (GridWidgetItem) -> Unit,
    onPickWidget: () -> Unit,
    onConfigure: ((GridWidgetItem) -> Unit)? = null,
    onResizeWidget: ((GridWidgetItem) -> Unit)? = null,
    dragDropState: WidgetDragDropState? = null,
    onWidgetsMoved: ((List<GridWidgetItem>) -> Unit)? = null,
) {
    val context = LocalContext.current
    val scale = if (isPreview) context.previewScale else context.scaleRatio

    val cellSize = Dimens.WidgetCellSize * scale
    val gap = Dimens.WidgetCellGap * scale
    val cornerRadius = Dimens.WidgetCellCorner * scale
    val gridWidth = cellSize * GRID_COLUMNS + gap * (GRID_COLUMNS - 1)
    val gridHeight = cellSize * MAX_ROWS + gap * (MAX_ROWS - 1)

    val displayWidgets =
        if (dragDropState?.dragInProgress == true) {
            dragDropState.previewWidgets
        } else {
            widgets
        }

    val occupied = remember(displayWidgets) { buildOccupiedGrid(displayWidgets) }

    val density = LocalDensity.current
    val cellSizePx = with(density) { cellSize.toPx() }
    val gapPx = with(density) { gap.toPx() }
    var resizingId by remember { mutableStateOf(-1) }
    var resizingSpanX by remember { mutableIntStateOf(1) }
    var resizingSpanY by remember { mutableIntStateOf(1) }
    var resizingCellX by remember { mutableIntStateOf(0) }
    var resizingCellY by remember { mutableIntStateOf(0) }
    var selectedId by remember { mutableIntStateOf(-1) }

    Box(
        modifier =
            Modifier.width(gridWidth)
                .height(gridHeight)
                .then(
                    if (!isPreview)
                        Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { if (selectedId != -1) selectedId = -1 },
                        )
                    else Modifier
                ),
        contentAlignment = Alignment.TopStart,
    ) {
        for (row in 0 until MAX_ROWS) {
            for (col in 0 until GRID_COLUMNS) {
                if (!occupied[row][col]) {
                    val x = (cellSize + gap) * col
                    val y = (cellSize + gap) * row
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
                val dragId = dragDropState.draggingWidgetId
                val dragged = widgets.firstOrNull { it.appWidgetId == dragId }
                if (dragged != null) {
                    val tx = (cellSize + gap) * target.cellX
                    val ty = (cellSize + gap) * target.cellY
                    val tw = cellSize * dragged.spanX + gap * (dragged.spanX - 1).coerceAtLeast(0)
                    val th = cellSize * dragged.spanY + gap * (dragged.spanY - 1).coerceAtLeast(0)

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
            val isResizing = resizingId == widget.appWidgetId
            val effSpanX = if (isResizing) resizingSpanX else widget.spanX
            val effSpanY = if (isResizing) resizingSpanY else widget.spanY
            val effCellX = if (isResizing) resizingCellX else widget.cellX
            val effCellY = if (isResizing) resizingCellY else widget.cellY
            val targetX = effCellX * (cellSizePx + gapPx)
            val targetY = effCellY * (cellSizePx + gapPx)
            val w = cellSize * effSpanX + gap * (effSpanX - 1).coerceAtLeast(0)
            val h = cellSize * effSpanY + gap * (effSpanY - 1).coerceAtLeast(0)
            val isBeingDragged = dragDropState?.isDragging(widget) == true

            key(widget.appWidgetId, widget.provider) {
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

                val providerInfo =
                    remember(widget.provider) { widget.providerInfo(context) }

                val previewBitmap =
                    remember(widget.provider) {
                        providerInfo
                            ?.loadPreviewImage(
                                context,
                                context.resources.displayMetrics.densityDpi,
                            )
                            ?.let { drawable ->
                                val iw =
                                    drawable.intrinsicWidth.takeIf { it > 0 }
                                        ?: (200 * context.resources.displayMetrics.density).toInt()
                                val ih =
                                    drawable.intrinsicHeight.takeIf { it > 0 }
                                        ?: (100 * context.resources.displayMetrics.density).toInt()
                                drawable.toBitmap(iw, ih)
                            }
                    }
                val hasPreviewLayout = providerInfo != null && providerInfo.previewLayout != 0
                val iconDrawable = remember(widget.provider) { widget.icon(context) }
                val labelText = remember(widget.provider) { widget.label(context) }

                val isSelected = selectedId == widget.appWidgetId
                val selectionColor = MaterialTheme.colorScheme.primary
                val interactionSrc = remember(widget.appWidgetId) { MutableInteractionSource() }
                val currentWidgetsState by rememberUpdatedState(widgets)
                val currentWidgetForDrag by rememberUpdatedState(widget)
                val selectableModifier =
                    if (isPreview) Modifier
                    else
                        Modifier.selectable(
                            selected = isSelected,
                            interactionSource = interactionSrc,
                            indication = null,
                            onClick = { selectedId = if (isSelected) -1 else widget.appWidgetId },
                        )
                val dragModifier =
                    if (isPreview || dragDropState == null) Modifier
                    else {
                        val dds = dragDropState
                        Modifier.pointerInput(widget.appWidgetId, dds, cellSizePx, gapPx) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    val wItem = currentWidgetForDrag
                                    selectedId = wItem.appWidgetId
                                    dds.onDragStart(wItem, currentWidgetsState)
                                },
                                onDrag = { change, delta ->
                                    change.consume()
                                    dds.onDrag(delta, cellSizePx, gapPx)
                                },
                                onDragEnd = {
                                    dds.onDragEnd()?.also { onWidgetsMoved?.invoke(it) }
                                },
                                onDragCancel = { dds.onCancelled() },
                            )
                        }
                    }
                val draggingOffsetModifier =
                    if (isBeingDragged && dragDropState != null) {
                        val dds = dragDropState
                        Modifier.graphicsLayer {
                            translationX = dds.draggingItemOffset.x
                            translationY = dds.draggingItemOffset.y
                        }.zIndex(4f)
                    } else Modifier
                val borderModifier =
                    if (isSelected)
                        Modifier.border(
                            width = 2.dp * scale,
                            color = selectionColor,
                            shape = RoundedCornerShape(cornerRadius),
                        )
                    else Modifier
                val offsetX = if (isBeingDragged) targetX else animatedX
                val offsetY = if (isBeingDragged) targetY else animatedY
                val cellBgModifier =
                    Modifier.clip(RoundedCornerShape(cornerRadius))
                        .background(Color.White.copy(alpha = 0.22f))
                Box(
                    modifier =
                        Modifier.offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                            .size(width = w, height = h)
                            .then(draggingOffsetModifier)
                            .then(cellBgModifier)
                            .then(borderModifier)
                            .then(selectableModifier)
                            .then(dragModifier),
                    contentAlignment = Alignment.Center,
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = labelText,
                            modifier = Modifier.fillMaxSize().padding(2.dp * scale),
                            contentScale = ContentScale.Fit,
                        )
                    } else if (hasPreviewLayout && providerInfo != null) {
                        var layoutFailed by remember { mutableStateOf(false) }
                        if (!layoutFailed) {
                            AndroidView(
                                factory = { ctx ->
                                    WidgetPreviewHostView(ctx).apply {
                                        try {
                                            setAppWidget(-1, providerInfo)
                                            val rv =
                                                RemoteViews(
                                                    providerInfo.provider.packageName,
                                                    providerInfo.previewLayout,
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
                                        (h.value * context.resources.displayMetrics.density)
                                            .toInt(),
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

                    if (!isPreview && isSelected) {
                        val badgeSize = 24.dp * scale
                        val iconSize = 16.dp * scale
                        val badgeOffset = 4.dp * scale
                        val badgeBg = MaterialTheme.colorScheme.primary
                        val badgeTint = MaterialTheme.colorScheme.onPrimary
                        val showConfigure =
                            onConfigure != null &&
                                providerInfo != null &&
                                providerInfo.configure != null
                        Row(
                            modifier =
                                Modifier.align(Alignment.TopEnd)
                                    .zIndex(4f)
                                    .padding(badgeOffset),
                            horizontalArrangement = Arrangement.spacedBy(4.dp * scale),
                        ) {
                            if (showConfigure) {
                                Box(
                                    modifier =
                                        Modifier.size(badgeSize)
                                            .clip(CircleShape)
                                            .background(badgeBg)
                                            .clickable { onConfigure!!(widget) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = badgeTint,
                                        modifier = Modifier.size(iconSize),
                                    )
                                }
                            }
                            Box(
                                modifier =
                                    Modifier.size(badgeSize)
                                        .clip(CircleShape)
                                        .background(badgeBg)
                                        .clickable { onRemove(widget) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = null,
                                    tint = badgeTint,
                                    modifier = Modifier.size(iconSize),
                                )
                            }
                        }

                        if (onResizeWidget != null) {
                            val cellFullPx = cellSizePx + gapPx
                            val siblings by rememberUpdatedState(widgets)
                            val currentWidget by rememberUpdatedState(widget)
                            val resizeMode =
                                providerInfo?.resizeMode ?: AppWidgetProviderInfo.RESIZE_BOTH
                            val occForCheck =
                                remember(widgets, widget.appWidgetId) {
                                    occupancyExcluding(widgets, widget.appWidgetId)
                                }
                            val maxGrowX =
                                remember(occForCheck, widget.cellX, widget.cellY, widget.spanY) {
                                    maxSpanXFor(
                                        occForCheck,
                                        widget.cellX,
                                        widget.cellY,
                                        widget.spanY,
                                        GRID_COLUMNS - widget.cellX,
                                    )
                                }
                            val maxGrowY =
                                remember(occForCheck, widget.cellX, widget.cellY, widget.spanX) {
                                    maxSpanYFor(
                                        occForCheck,
                                        widget.cellX,
                                        widget.cellY,
                                        widget.spanX,
                                        MAX_ROWS - widget.cellY,
                                    )
                                }
                            val hCapable =
                                (resizeMode and AppWidgetProviderInfo.RESIZE_HORIZONTAL) != 0
                            val vCapable =
                                (resizeMode and AppWidgetProviderInfo.RESIZE_VERTICAL) != 0
                            val canGrowLeft =
                                remember(occForCheck, widget.cellX, widget.cellY, widget.spanY) {
                                    widget.cellX > 0 &&
                                        !hasCollision(
                                            occForCheck,
                                            widget.cellX - 1,
                                            widget.cellY,
                                            1,
                                            widget.spanY,
                                        )
                                }
                            val canGrowUp =
                                remember(occForCheck, widget.cellX, widget.cellY, widget.spanX) {
                                    widget.cellY > 0 &&
                                        !hasCollision(
                                            occForCheck,
                                            widget.cellX,
                                            widget.cellY - 1,
                                            widget.spanX,
                                            1,
                                        )
                                }
                            val canResizeH =
                                hCapable && (maxGrowX > widget.spanX || widget.spanX > 1)
                            val canResizeV =
                                vCapable && (maxGrowY > widget.spanY || widget.spanY > 1)
                            val canResizeW = hCapable && (canGrowLeft || widget.spanX > 1)
                            val canResizeU = vCapable && (canGrowUp || widget.spanY > 1)
                            val pillColor = badgeBg
                            val pillLong = 24.dp * scale
                            val pillShort = 8.dp * scale
                            val pillHit = 48.dp * scale

                            if (canResizeH) {
                                Box(
                                    modifier =
                                        Modifier.align(Alignment.CenterEnd)
                                            .zIndex(3f)
                                            .offset(x = pillHit / 2)
                                            .size(pillHit)
                                            .pointerInput(widget.appWidgetId) {
                                                awaitEachGesture {
                                                    val down = awaitFirstDown(
                                                        requireUnconsumed = false,
                                                    )
                                                    down.consume()
                                                    val wItem = currentWidget
                                                    val startX = wItem.spanX
                                                    val startY = wItem.spanY
                                                    val baseCellX = wItem.cellX
                                                    val baseCellY = wItem.cellY
                                                    var acc = 0f
                                                    val occupancy = occupancyExcluding(
                                                        siblings,
                                                        wItem.appWidgetId,
                                                    )
                                                    resizingId = wItem.appWidgetId
                                                    resizingSpanX = startX
                                                    resizingSpanY = startY
                                                    resizingCellX = baseCellX
                                                    resizingCellY = baseCellY
                                                    while (true) {
                                                        val event = awaitPointerEvent()
                                                        val change = event.changes.firstOrNull {
                                                            it.id == down.id
                                                        } ?: break
                                                        if (!change.pressed) {
                                                            val nx = resizingSpanX
                                                            val ny = resizingSpanY
                                                            change.consume()
                                                            resizingId = -1
                                                            val latest = currentWidget
                                                            if (nx != latest.spanX ||
                                                                    ny != latest.spanY) {
                                                                onResizeWidget(
                                                                    latest.copy(
                                                                        spanX = nx,
                                                                        spanY = ny,
                                                                    )
                                                                )
                                                            }
                                                            break
                                                        }
                                                        val d = change.positionChange()
                                                        change.consume()
                                                        acc += d.x
                                                        val dx = (acc / cellFullPx).roundToInt()
                                                        val desiredX = (startX + dx).coerceIn(
                                                            1,
                                                            GRID_COLUMNS - baseCellX,
                                                        )
                                                        val (cx, _) = clampNoCollision(
                                                            occupancy,
                                                            baseCellX,
                                                            baseCellY,
                                                            resizingSpanX,
                                                            resizingSpanY,
                                                            desiredX,
                                                            resizingSpanY,
                                                        )
                                                        resizingSpanX = cx
                                                    }
                                                }
                                            },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        modifier =
                                            Modifier.size(
                                                    width = pillShort,
                                                    height = pillLong,
                                                )
                                                .clip(RoundedCornerShape(pillShort / 2))
                                                .background(pillColor)
                                    )
                                }
                            }

                            if (canResizeV) {
                                Box(
                                    modifier =
                                        Modifier.align(Alignment.BottomCenter)
                                            .zIndex(3f)
                                            .offset(y = pillHit / 2)
                                            .size(pillHit)
                                            .pointerInput(widget.appWidgetId) {
                                                awaitEachGesture {
                                                    val down = awaitFirstDown(
                                                        requireUnconsumed = false,
                                                    )
                                                    down.consume()
                                                    val wItem = currentWidget
                                                    val startX = wItem.spanX
                                                    val startY = wItem.spanY
                                                    val baseCellX = wItem.cellX
                                                    val baseCellY = wItem.cellY
                                                    var acc = 0f
                                                    val occupancy = occupancyExcluding(
                                                        siblings,
                                                        wItem.appWidgetId,
                                                    )
                                                    resizingId = wItem.appWidgetId
                                                    resizingSpanX = startX
                                                    resizingSpanY = startY
                                                    resizingCellX = baseCellX
                                                    resizingCellY = baseCellY
                                                    while (true) {
                                                        val event = awaitPointerEvent()
                                                        val change = event.changes.firstOrNull {
                                                            it.id == down.id
                                                        } ?: break
                                                        if (!change.pressed) {
                                                            val nx = resizingSpanX
                                                            val ny = resizingSpanY
                                                            change.consume()
                                                            resizingId = -1
                                                            val latest = currentWidget
                                                            if (nx != latest.spanX ||
                                                                    ny != latest.spanY) {
                                                                onResizeWidget(
                                                                    latest.copy(
                                                                        spanX = nx,
                                                                        spanY = ny,
                                                                    )
                                                                )
                                                            }
                                                            break
                                                        }
                                                        val d = change.positionChange()
                                                        change.consume()
                                                        acc += d.y
                                                        val dy = (acc / cellFullPx).roundToInt()
                                                        val desiredY = (startY + dy).coerceIn(
                                                            1,
                                                            MAX_ROWS - baseCellY,
                                                        )
                                                        val (_, cy) = clampNoCollision(
                                                            occupancy,
                                                            baseCellX,
                                                            baseCellY,
                                                            resizingSpanX,
                                                            resizingSpanY,
                                                            resizingSpanX,
                                                            desiredY,
                                                        )
                                                        resizingSpanY = cy
                                                    }
                                                }
                                            },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        modifier =
                                            Modifier.size(
                                                    width = pillLong,
                                                    height = pillShort,
                                                )
                                                .clip(RoundedCornerShape(pillShort / 2))
                                                .background(pillColor)
                                    )
                                }
                            }

                            if (canResizeW) {
                                Box(
                                    modifier =
                                        Modifier.align(Alignment.CenterStart)
                                            .zIndex(3f)
                                            .offset(x = -pillHit / 2)
                                            .size(pillHit)
                                            .pointerInput(widget.appWidgetId) {
                                                awaitEachGesture {
                                                    val down = awaitFirstDown(
                                                        requireUnconsumed = false,
                                                    )
                                                    down.consume()
                                                    val wItem = currentWidget
                                                    val origCellX = wItem.cellX
                                                    val origCellY = wItem.cellY
                                                    val origSpanX = wItem.spanX
                                                    val origSpanY = wItem.spanY
                                                    val rightEdge = origCellX + origSpanX
                                                    var acc = 0f
                                                    val occupancy = occupancyExcluding(
                                                        siblings,
                                                        wItem.appWidgetId,
                                                    )
                                                    resizingId = wItem.appWidgetId
                                                    resizingSpanX = origSpanX
                                                    resizingSpanY = origSpanY
                                                    resizingCellX = origCellX
                                                    resizingCellY = origCellY
                                                    while (true) {
                                                        val event = awaitPointerEvent()
                                                        val change = event.changes.firstOrNull {
                                                            it.id == down.id
                                                        } ?: break
                                                        if (!change.pressed) {
                                                            val nx = resizingCellX
                                                            val sx = resizingSpanX
                                                            change.consume()
                                                            resizingId = -1
                                                            val latest = currentWidget
                                                            if (nx != latest.cellX ||
                                                                    sx != latest.spanX) {
                                                                onResizeWidget(
                                                                    latest.copy(
                                                                        cellX = nx,
                                                                        spanX = sx,
                                                                    )
                                                                )
                                                            }
                                                            break
                                                        }
                                                        val d = change.positionChange()
                                                        change.consume()
                                                        acc += d.x
                                                        val shift =
                                                            (acc / cellFullPx).roundToInt()
                                                        var newCellX =
                                                            (origCellX + shift)
                                                                .coerceIn(0, rightEdge - 1)
                                                        while (newCellX < origCellX &&
                                                                hasCollision(
                                                                    occupancy,
                                                                    newCellX,
                                                                    origCellY,
                                                                    rightEdge - newCellX,
                                                                    origSpanY,
                                                                )) {
                                                            newCellX++
                                                        }
                                                        resizingCellX = newCellX
                                                        resizingSpanX = rightEdge - newCellX
                                                    }
                                                }
                                            },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        modifier =
                                            Modifier.size(
                                                    width = pillShort,
                                                    height = pillLong,
                                                )
                                                .clip(RoundedCornerShape(pillShort / 2))
                                                .background(pillColor)
                                    )
                                }
                            }

                            if (canResizeU) {
                                Box(
                                    modifier =
                                        Modifier.align(Alignment.TopCenter)
                                            .zIndex(3f)
                                            .offset(y = -pillHit / 2)
                                            .size(pillHit)
                                            .pointerInput(widget.appWidgetId) {
                                                awaitEachGesture {
                                                    val down = awaitFirstDown(
                                                        requireUnconsumed = false,
                                                    )
                                                    down.consume()
                                                    val wItem = currentWidget
                                                    val origCellX = wItem.cellX
                                                    val origCellY = wItem.cellY
                                                    val origSpanX = wItem.spanX
                                                    val origSpanY = wItem.spanY
                                                    val bottomEdge = origCellY + origSpanY
                                                    var acc = 0f
                                                    val occupancy = occupancyExcluding(
                                                        siblings,
                                                        wItem.appWidgetId,
                                                    )
                                                    resizingId = wItem.appWidgetId
                                                    resizingSpanX = origSpanX
                                                    resizingSpanY = origSpanY
                                                    resizingCellX = origCellX
                                                    resizingCellY = origCellY
                                                    while (true) {
                                                        val event = awaitPointerEvent()
                                                        val change = event.changes.firstOrNull {
                                                            it.id == down.id
                                                        } ?: break
                                                        if (!change.pressed) {
                                                            val ny = resizingCellY
                                                            val sy = resizingSpanY
                                                            change.consume()
                                                            resizingId = -1
                                                            val latest = currentWidget
                                                            if (ny != latest.cellY ||
                                                                    sy != latest.spanY) {
                                                                onResizeWidget(
                                                                    latest.copy(
                                                                        cellY = ny,
                                                                        spanY = sy,
                                                                    )
                                                                )
                                                            }
                                                            break
                                                        }
                                                        val d = change.positionChange()
                                                        change.consume()
                                                        acc += d.y
                                                        val shift =
                                                            (acc / cellFullPx).roundToInt()
                                                        var newCellY =
                                                            (origCellY + shift)
                                                                .coerceIn(0, bottomEdge - 1)
                                                        while (newCellY < origCellY &&
                                                                hasCollision(
                                                                    occupancy,
                                                                    origCellX,
                                                                    newCellY,
                                                                    origSpanX,
                                                                    bottomEdge - newCellY,
                                                                )) {
                                                            newCellY++
                                                        }
                                                        resizingCellY = newCellY
                                                        resizingSpanY = bottomEdge - newCellY
                                                    }
                                                }
                                            },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        modifier =
                                            Modifier.size(
                                                    width = pillLong,
                                                    height = pillShort,
                                                )
                                                .clip(RoundedCornerShape(pillShort / 2))
                                                .background(pillColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
