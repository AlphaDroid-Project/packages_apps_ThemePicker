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
package com.android.axion.themepicker.draganddrop

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import kotlin.math.*
import kotlin.system.*

data class ItemInfo(
    val span: Int = 1,
    val width: Float = 0f,
    val height: Float = 0f,
    val spacing: Float = 0f,
    val index: Int = 0
)

class DragState<T>(
    itemsList: List<T>
) {
    val infoMap = mutableStateMapOf<T, ItemInfo>()

    var dragItem: T? by mutableStateOf(null)
    var dragDistance: Float by mutableStateOf(0f)
    var items: List<T> by mutableStateOf(itemsList)
    
    var displayOrder: List<T> by mutableStateOf(itemsList)
    
    private var startDragPosition: Float by mutableStateOf(0f)
    private var itemPositions = mutableMapOf<T, Float>()

    fun info(item: T, info: ItemInfo) {
        infoMap[item] = info
    }
    
    fun info(item: T): ItemInfo = infoMap[item] ?: ItemInfo()

    fun startDrag(i: T) {
        dragItem = i
        dragDistance = 0f
        displayOrder = items.toList()
        
        var pos = 0f
        items.forEach { item ->
            itemPositions[item] = pos
            val itemInfo = info(item)
            pos += itemInfo.width + itemInfo.spacing
        }
        
        startDragPosition = itemPositions[i] ?: 0f
    }

    fun onDrag(distance: Float) {
        dragDistance = distance
        updateDisplayOrder()
    }

    fun endDrag(onReorder: ((List<T>) -> Unit)? = null) {
        if (dragItem != null) {
            items = displayOrder.toList()
            onReorder?.invoke(items)
        }
        
        dragItem = null
        dragDistance = 0f
        displayOrder = items
        itemPositions.clear()
    }

    private fun updateDisplayOrder() {
        val dragged = dragItem ?: return
        val draggedInfo = info(dragged)
        
        val currentDraggedCenter = startDragPosition + dragDistance + (draggedInfo.width / 2f)
        
        val newOrder = mutableListOf<T>()
        var cumulativePos = 0f
        var draggedInserted = false
        
        items.forEach { item ->
            if (item == dragged) return@forEach
            
            val itemInfo = info(item)
            val itemEnd = cumulativePos + itemInfo.width
            
            if (!draggedInserted && currentDraggedCenter <= itemEnd) {
                newOrder.add(dragged)
                draggedInserted = true
            }
            
            newOrder.add(item)
            cumulativePos += itemInfo.width + itemInfo.spacing
        }
        
        if (!draggedInserted) {
            newOrder.add(dragged)
        }
        
        displayOrder = newOrder
    }

    fun getTargetPosition(item: T): Float {
        if (dragItem == null) return 0f
        
        var targetPos = 0f
        for (displayItem in displayOrder) {
            if (displayItem == item) break
            val itemInfo = info(displayItem)
            targetPos += itemInfo.width + itemInfo.spacing
        }
        
        return targetPos
    }

    fun getItemOffset(item: T): Float {
        if (dragItem == null) return 0f
        
        val originalPos = itemPositions[item] ?: 0f
        val targetPos = getTargetPosition(item)
        
        return if (item == dragItem) {
            dragDistance
        } else {
            targetPos - originalPos
        }
    }
}

@Composable
fun <T> DragAndDropGrid(
    items: List<T>,
    span: (T) -> Int,
    width: (T) -> Dp,
    height: (T) -> Dp,
    onReorder: (List<T>) -> Unit,
    spacing: Dp = 0.dp,
    enabled: Boolean = true,
    modifier: Modifier = Modifier.fillMaxWidth(),
    maxCount: Int = 4,
    content: @Composable (item: T) -> Unit
) {
    val ds = rememberDragState(items)
    val density = LocalDensity.current
    var containerWidthPx by remember { mutableStateOf(0f) }

    LaunchedEffect(items) {
        if (ds.dragItem == null) {
            ds.items = items
            ds.displayOrder = items
        }
    }

    Box(
        modifier = modifier.onGloballyPositioned { coords ->
            containerWidthPx = coords.size.width.toFloat()
        },
        contentAlignment = Alignment.CenterStart
    ) {
        if (containerWidthPx == 0f || items.isEmpty()) return@Box

        val baseUnitWidthPx = with(density) {
            (items.map { width(it).toPx() / span(it).coerceAtLeast(1) })
                .maxOrNull()
                ?: (width(items.first()).toPx() / span(items.first()).coerceAtLeast(1))
        }

        val totalSpanUnits = items.sumOf { span(it) }
        
        val columnGapPx = ((containerWidthPx - (baseUnitWidthPx * totalSpanUnits)) / (items.size + 1))
            .coerceAtLeast(with(density) { spacing.toPx() })
            
        items.forEach { item ->
            key(item) {
                val itemSpan = span(item)
                val itemInfo = ItemInfo(
                    span = itemSpan,
                    width = baseUnitWidthPx * itemSpan,
                    height = with(density) { height(item).toPx() },
                    spacing = columnGapPx,
                    index = items.indexOf(item)
                )
                LaunchedEffect(itemInfo) {
                    ds.info(item, itemInfo)
                }
            }
        }

        items.forEachIndexed { myIndex, item ->
            key(item) {
                val itemSpan = span(item)
                val itemWidthDp = with(density) { (baseUnitWidthPx * itemSpan).toDp() }
                val itemHeightDp = height(item)

                val basePositionPx = columnGapPx + items.take(myIndex).sumOf { prevItem ->
                    val prevItemSpan = span(prevItem)
                    (baseUnitWidthPx * prevItemSpan + columnGapPx).toDouble()
                }.toFloat()
                
                val animateOffsetX by animateFloatAsState(
                    targetValue = ds.getItemOffset(item),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "itemOffset"
                )

                val offsetX = if (ds.dragItem != null) animateOffsetX else 0f

                Box(
                    modifier = Modifier
                        .offset { IntOffset((basePositionPx + offsetX).roundToInt(), 0) }
                        .zIndex(if (ds.dragItem == item) 1f else 0f)
                        .then(
                            if (enabled) {
                                Modifier.pointerInput(item) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { ds.startDrag(item) },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            ds.onDrag(ds.dragDistance + dragAmount.x)
                                        },
                                        onDragEnd = { ds.endDrag(onReorder) },
                                        onDragCancel = { ds.endDrag(onReorder) }
                                    )
                                }
                            } else Modifier
                        )
                        .graphicsLayer {
                            scaleX = if (ds.dragItem == item) 1.05f else 1f
                            scaleY = if (ds.dragItem == item) 1.05f else 1f
                        }
                        .width(itemWidthDp)
                        .height(itemHeightDp)
                ) {
                    content(item)
                }
            }
        }
    }
}

@Composable
fun <T> rememberDragState(items: List<T>): DragState<T> =
    remember { DragState(items) }
