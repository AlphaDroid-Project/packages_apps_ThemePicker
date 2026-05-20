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

package com.android.alpha.themepicker.ui.lockscreen.widgets

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlin.math.roundToInt

data class DropTarget(val cellX: Int, val cellY: Int, val isValid: Boolean)

class WidgetDragDropState {

    private var originalWidgets: List<GridWidgetItem> = emptyList()
    private var startCellX: Int = 0
    private var startCellY: Int = 0
    private var totalDelta: Offset = Offset.Zero

    var previewWidgets by mutableStateOf<List<GridWidgetItem>>(emptyList())
        private set

    var draggingWidgetId by mutableStateOf<Int?>(null)
        private set

    var draggingItemOffset by mutableStateOf(Offset.Zero)
        private set

    var dropTarget by mutableStateOf<DropTarget?>(null)
        private set

    val dragInProgress: Boolean
        get() = draggingWidgetId != null

    fun isDragging(widget: GridWidgetItem): Boolean =
        draggingWidgetId != null && draggingWidgetId == widget.appWidgetId

    fun onDragStart(widget: GridWidgetItem, allWidgets: List<GridWidgetItem>) {
        originalWidgets = allWidgets.toList()
        previewWidgets = allWidgets.toList()
        draggingWidgetId = widget.appWidgetId
        draggingItemOffset = Offset.Zero
        totalDelta = Offset.Zero
        startCellX = widget.cellX
        startCellY = widget.cellY
        dropTarget = null
    }

    fun onDrag(delta: Offset, cellSizePx: Float, gapPx: Float) {
        val dragId = draggingWidgetId ?: return
        val dragged = originalWidgets.firstOrNull { it.appWidgetId == dragId } ?: return

        totalDelta += delta

        val cellStep = cellSizePx + gapPx
        val dxCells = (totalDelta.x / cellStep).roundToInt()
        val dyCells = (totalDelta.y / cellStep).roundToInt()
        val col = (startCellX + dxCells).coerceIn(0, GRID_COLUMNS - 1)
        val row = (startCellY + dyCells).coerceIn(0, MAX_ROWS - 1)

        val current = dropTarget
        if (current != null && current.cellX == col && current.cellY == row) {
            val draggedNow = previewWidgets.firstOrNull { it.appWidgetId == dragId } ?: dragged
            draggingItemOffset = Offset(
                totalDelta.x - (draggedNow.cellX - startCellX) * cellStep,
                totalDelta.y - (draggedNow.cellY - startCellY) * cellStep,
            )
            return
        }

        val overlapping =
            originalWidgets.filter { w ->
                w.appWidgetId != dragId && wouldOverlap(w, col, row, dragged.spanX, dragged.spanY)
            }

        val preview = originalWidgets.toMutableList()
        val dragIdx = preview.indexOfFirst { it.appWidgetId == dragId }

        val involvedIds = buildSet {
            add(dragId)
            overlapping.forEach { add(it.appWidgetId) }
        }
        val baseGrid =
            buildOccupiedGrid(originalWidgets.filter { it.appWidgetId !in involvedIds })

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
            for ((ownIndex, ow) in overlapping.withIndex()) {
                val preferX = if (ownIndex == 0) dragged.cellX else -1
                val preferY = if (ownIndex == 0) dragged.cellY else -1
                val pos = findPositionOnGrid(workingGrid, ow.spanX, ow.spanY, preferX, preferY)
                if (pos != null) {
                    val owIdx = preview.indexOfFirst { it.appWidgetId == ow.appWidgetId }
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

        val draggedNow = preview.firstOrNull { it.appWidgetId == dragId } ?: dragged
        draggingItemOffset = Offset(
            totalDelta.x - (draggedNow.cellX - startCellX) * cellStep,
            totalDelta.y - (draggedNow.cellY - startCellY) * cellStep,
        )
    }

    fun onDragEnd(): List<GridWidgetItem>? {
        val result = if (dropTarget?.isValid == true) previewWidgets.toList() else null
        reset()
        return result
    }

    fun onCancelled() {
        reset()
    }

    private fun reset() {
        draggingWidgetId = null
        draggingItemOffset = Offset.Zero
        totalDelta = Offset.Zero
        dropTarget = null
        previewWidgets = emptyList()
        originalWidgets = emptyList()
        startCellX = 0
        startCellY = 0
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

private fun findPositionOnGrid(
    grid: Array<BooleanArray>,
    spanX: Int,
    spanY: Int,
    preferredX: Int = -1,
    preferredY: Int = -1,
): Pair<Int, Int>? {
    if (preferredX in 0 until GRID_COLUMNS &&
            preferredY in 0 until MAX_ROWS &&
            canPlaceOn(grid, preferredX, preferredY, spanX, spanY)) {
        return preferredX to preferredY
    }
    for (r in 0 until MAX_ROWS) {
        for (c in 0 until GRID_COLUMNS) {
            if (canPlaceOn(grid, c, r, spanX, spanY)) return c to r
        }
    }
    return null
}
