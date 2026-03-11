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

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.geometry.Offset

data class DropTarget(val cellX: Int, val cellY: Int, val isValid: Boolean)

class WidgetDragDropState {

    private var originalWidgets: List<GridWidgetItem> = emptyList()

    var previewWidgets by mutableStateOf<List<GridWidgetItem>>(emptyList())
        private set

    var draggedProvider by mutableStateOf<String?>(null)
        private set

    var dropTarget by mutableStateOf<DropTarget?>(null)
        private set

    val dragInProgress: Boolean
        get() = draggedProvider != null

    fun isDragging(widget: GridWidgetItem): Boolean =
        draggedProvider != null && draggedProvider == widget.provider

    fun onStarted(widget: GridWidgetItem, allWidgets: List<GridWidgetItem>) {
        originalWidgets = allWidgets.toList()
        previewWidgets = allWidgets.toList()
        draggedProvider = widget.provider
        dropTarget = null
    }

    fun onMoved(offset: Offset, gridOrigin: Offset, cellSizePx: Float, gapPx: Float) {
        val dragProv = draggedProvider ?: return
        val dragged = originalWidgets.firstOrNull { it.provider == dragProv } ?: return

        val relX = offset.x - gridOrigin.x
        val relY = offset.y - gridOrigin.y
        val cellStep = cellSizePx + gapPx
        val col = (relX / cellStep).toInt().coerceIn(0, GRID_COLUMNS - 1)
        val row = (relY / cellStep).toInt().coerceIn(0, MAX_ROWS - 1)

        val current = dropTarget
        if (current != null && current.cellX == col && current.cellY == row) return

        val overlapping =
            originalWidgets.filter { w ->
                w.provider != dragProv && wouldOverlap(w, col, row, dragged.spanX, dragged.spanY)
            }

        val preview = originalWidgets.toMutableList()
        val dragIdx = preview.indexOfFirst { it.provider == dragProv }

        val involvedProviders = buildSet {
            add(dragProv)
            overlapping.forEach { add(it.provider) }
        }
        val baseGrid =
            buildOccupiedGrid(originalWidgets.filter { it.provider !in involvedProviders })

        if (!canPlaceOn(baseGrid, col, row, dragged.spanX, dragged.spanY)) {
            dropTarget = DropTarget(col, row, false)
        } else if (overlapping.isEmpty()) {
            preview[dragIdx] = dragged.copy(cellX = col, cellY = row)
            dropTarget = DropTarget(col, row, true)
        } else {

            val workingGrid = baseGrid.map { it.copyOf() }.toTypedArray()
            markOccupied(workingGrid, col, row, dragged.spanX, dragged.spanY)

            val placements = mutableListOf<Pair<Int, Pair<Int, Int>>>()
            var allFit = true
            for (ow in overlapping) {
                val pos = findPositionOnGrid(workingGrid, ow.spanX, ow.spanY)
                if (pos != null) {
                    val owIdx = preview.indexOfFirst { it.provider == ow.provider }
                    placements.add(owIdx to pos)
                    markOccupied(workingGrid, pos.first, pos.second, ow.spanX, ow.spanY)
                } else {
                    allFit = false
                    break
                }
            }

            if (allFit) {
                preview[dragIdx] = dragged.copy(cellX = col, cellY = row)
                placements.forEach { (idx, pos) ->
                    preview[idx] = preview[idx].copy(cellX = pos.first, cellY = pos.second)
                }
            }
            dropTarget = DropTarget(col, row, allFit)
        }

        previewWidgets = preview
    }

    fun onExited() {
        dropTarget = null
        previewWidgets = originalWidgets.toList()
    }

    fun onDrop(): List<GridWidgetItem>? {
        if (dropTarget?.isValid != true) {
            reset()
            return null
        }
        val result = previewWidgets.toList()
        reset()
        return result
    }

    fun onCancelled() {
        reset()
    }

    private fun reset() {
        draggedProvider = null
        dropTarget = null
        previewWidgets = emptyList()
        originalWidgets = emptyList()
    }
}

private fun wouldOverlap(
    existing: GridWidgetItem,
    cellX: Int,
    cellY: Int,
    spanX: Int,
    spanY: Int,
): Boolean {
    return existing.cellX < cellX + spanX &&
        existing.cellX + existing.spanX > cellX &&
        existing.cellY < cellY + spanY &&
        existing.cellY + existing.spanY > cellY
}

private fun canPlaceOn(
    grid: Array<BooleanArray>,
    cellX: Int,
    cellY: Int,
    spanX: Int,
    spanY: Int,
): Boolean {
    if (cellX + spanX > GRID_COLUMNS) return false
    if (cellY + spanY > MAX_ROWS) return false
    for (r in cellY until cellY + spanY) {
        for (c in cellX until cellX + spanX) {
            if (grid[r][c]) return false
        }
    }
    return true
}

private fun markOccupied(
    grid: Array<BooleanArray>,
    cellX: Int,
    cellY: Int,
    spanX: Int,
    spanY: Int,
) {
    for (r in cellY until (cellY + spanY).coerceAtMost(MAX_ROWS)) {
        for (c in cellX until (cellX + spanX).coerceAtMost(GRID_COLUMNS)) {
            grid[r][c] = true
        }
    }
}

private fun findPositionOnGrid(grid: Array<BooleanArray>, spanX: Int, spanY: Int): Pair<Int, Int>? {
    for (r in 0 until MAX_ROWS) {
        for (c in 0 until GRID_COLUMNS) {
            if (canPlaceOn(grid, c, r, spanX, spanY)) return c to r
        }
    }
    return null
}

private fun DragAndDropEvent.toOffset(): Offset {
    return toAndroidDragEvent().run { Offset(x, y) }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.widgetDragSource(
    widget: GridWidgetItem,
    allWidgets: List<GridWidgetItem>,
    state: WidgetDragDropState,
): Modifier {
    val currentState by rememberUpdatedState(state)
    val currentWidgets by rememberUpdatedState(allWidgets)

    return dragAndDropSource(
        block = {
            detectDragGesturesAfterLongPress(
                onDrag = { _, _ -> },
                onDragStart = {
                    currentState.onStarted(widget, currentWidgets)
                    startTransfer(
                        DragAndDropTransferData(ClipData.newPlainText("widget", widget.provider))
                    )
                },
            )
        }
    )
}

@Composable
fun Modifier.widgetDropTarget(
    gridOrigin: () -> Offset,
    cellSizePx: Float,
    gapPx: Float,
    state: WidgetDragDropState,
    onDrop: (List<GridWidgetItem>) -> Unit,
): Modifier {
    val currentState by rememberUpdatedState(state)
    val currentOnDrop by rememberUpdatedState(onDrop)

    val target = remember {
        object : DragAndDropTarget {
            override fun onEntered(event: DragAndDropEvent) {}

            override fun onMoved(event: DragAndDropEvent) {
                currentState.onMoved(
                    offset = event.toOffset(),
                    gridOrigin = gridOrigin(),
                    cellSizePx = cellSizePx,
                    gapPx = gapPx,
                )
            }

            override fun onExited(event: DragAndDropEvent) {
                currentState.onExited()
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val result = currentState.onDrop() ?: return false
                currentOnDrop(result)
                return true
            }

            override fun onEnded(event: DragAndDropEvent) {
                if (currentState.dragInProgress) {
                    currentState.onCancelled()
                }
            }
        }
    }

    return dragAndDropTarget(
        shouldStartDragAndDrop = { event -> event.mimeTypes().contains("text/plain") },
        target = target,
    )
}
