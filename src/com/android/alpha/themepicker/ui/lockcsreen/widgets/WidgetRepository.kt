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

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.provider.Settings
import androidx.core.graphics.drawable.toBitmap
import org.json.JSONArray
import org.json.JSONObject

const val GRID_COLUMNS = 4
const val MAX_ROWS = 2

private const val KEY_CONFIG = "lockscreen_widgets_config"
private const val KEY_ENABLED = "lockscreen_widgets_enabled"

data class GridWidgetItem(
    val appWidgetId: Int = -1,
    val provider: String = "",
    val cellX: Int = 0,
    val cellY: Int = 0,
    val spanX: Int = 1,
    val spanY: Int = 1,
)

fun GridWidgetItem.providerInfo(context: Context): AppWidgetProviderInfo? {
    val cn = ComponentName.unflattenFromString(provider) ?: return null
    val manager = AppWidgetManager.getInstance(context)
    return manager.installedProviders.firstOrNull { it.provider == cn }
}

fun GridWidgetItem.label(context: Context): String {
    val info = providerInfo(context)
    return info?.loadLabel(context.packageManager)
        ?: ComponentName.unflattenFromString(provider)?.shortClassName
        ?: provider
}

fun GridWidgetItem.icon(context: Context): Drawable? {
    return providerInfo(context)?.loadIcon(context, context.resources.displayMetrics.densityDpi)
}

fun GridWidgetItem.preview(context: Context): Drawable? {
    return providerInfo(context)
        ?.loadPreviewImage(context, context.resources.displayMetrics.densityDpi)
}

fun loadWidgets(context: Context): List<GridWidgetItem> {
    val json =
        Settings.System.getStringForUser(
            context.contentResolver,
            KEY_CONFIG,
            UserHandle.USER_CURRENT,
        ) ?: return emptyList()

    return try {
        val arr = JSONArray(json)
        (0 until arr.length())
            .mapNotNull { i ->
                val obj = arr.getJSONObject(i)
                GridWidgetItem(
                    appWidgetId = obj.optInt("appWidgetId", -1),
                    provider = obj.optString("provider", ""),
                    cellX = obj.optInt("cellX", 0),
                    cellY = obj.optInt("cellY", 0),
                    spanX = obj.optInt("spanX", 1).coerceIn(1, GRID_COLUMNS),
                    spanY = obj.optInt("spanY", 1).coerceIn(1, MAX_ROWS),
                )
            }
            .filter { it.provider.isNotEmpty() }
    } catch (e: Exception) {
        emptyList()
    }
}

fun saveWidgets(context: Context, widgets: List<GridWidgetItem>) {
    val arr = JSONArray()
    widgets.forEach { entry ->
        arr.put(
            JSONObject().apply {
                put("appWidgetId", entry.appWidgetId)
                put("provider", entry.provider)
                put("cellX", entry.cellX)
                put("cellY", entry.cellY)
                put("spanX", entry.spanX)
                put("spanY", entry.spanY)
            }
        )
    }
    Settings.System.putStringForUser(
        context.contentResolver,
        KEY_CONFIG,
        arr.toString(),
        UserHandle.USER_CURRENT,
    )
    Settings.System.putIntForUser(
        context.contentResolver,
        KEY_ENABLED,
        if (widgets.isEmpty()) 0 else 1,
        UserHandle.USER_CURRENT,
    )
}

fun buildOccupiedGrid(widgets: List<GridWidgetItem>): Array<BooleanArray> {
    val grid = Array(MAX_ROWS) { BooleanArray(GRID_COLUMNS) }
    widgets.forEach { w ->
        for (r in w.cellY until (w.cellY + w.spanY).coerceAtMost(MAX_ROWS)) {
            for (c in w.cellX until (w.cellX + w.spanX).coerceAtMost(GRID_COLUMNS)) {
                grid[r][c] = true
            }
        }
    }
    return grid
}

fun findAvailablePosition(widgets: List<GridWidgetItem>, spanX: Int, spanY: Int): Pair<Int, Int>? {
    val grid = buildOccupiedGrid(widgets)
    for (r in 0 until MAX_ROWS) {
        for (c in 0 until GRID_COLUMNS) {
            if (canPlace(grid, c, r, spanX, spanY)) return c to r
        }
    }
    return null
}

fun findAvailablePositionExcluding(
    widgets: List<GridWidgetItem>,
    excludeProvider: String,
    spanX: Int,
    spanY: Int,
    preferredX: Int,
    preferredY: Int,
): Pair<Int, Int>? {
    val grid = buildOccupiedGrid(widgets.filter { it.provider != excludeProvider })
    if (canPlace(grid, preferredX, preferredY, spanX, spanY)) {
        return preferredX to preferredY
    }
    for (r in 0 until MAX_ROWS) {
        for (c in 0 until GRID_COLUMNS) {
            if (canPlace(grid, c, r, spanX, spanY)) return c to r
        }
    }
    return null
}

fun canResizeAt(
    widgets: List<GridWidgetItem>,
    resizingWidget: GridWidgetItem,
    newSpanX: Int,
    newSpanY: Int,
): Boolean {
    if (resizingWidget.cellX + newSpanX > GRID_COLUMNS) return false
    if (resizingWidget.cellY + newSpanY > MAX_ROWS) return false
    val grid = buildOccupiedGrid(widgets.filter { it.provider != resizingWidget.provider })
    return canPlace(grid, resizingWidget.cellX, resizingWidget.cellY, newSpanX, newSpanY)
}

private fun canPlace(
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

fun getAvailableWidgetProviders(context: Context): List<AppWidgetProviderInfo> {
    val manager = AppWidgetManager.getInstance(context)
    return manager.installedProviders.sortedBy { it.loadLabel(context.packageManager) }
}

data class WidgetProviderMeta(
    val info: AppWidgetProviderInfo,
    val label: String,
    val description: String?,
)

data class WidgetAppGroup(
    val packageName: String,
    val appLabel: String,
    val appIcon: Bitmap?,
    val widgets: List<WidgetProviderMeta>,
)

fun getGroupedWidgetProviders(context: Context): List<WidgetAppGroup> {
    val manager = AppWidgetManager.getInstance(context)
    val pm = context.packageManager
    val density = context.resources.displayMetrics.density

    return manager.installedProviders
        .groupBy { it.provider.packageName }
        .map { (pkg, providers) ->
            val (appLabel, appIcon) =
                try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    val label = pm.getApplicationLabel(appInfo).toString()
                    val iconSize = (32 * density).toInt()
                    val icon =
                        try {
                            pm.getApplicationIcon(appInfo).toBitmap(iconSize, iconSize)
                        } catch (_: Exception) {
                            null
                        }
                    label to icon
                } catch (_: Exception) {
                    pkg to null
                }

            val widgets =
                providers
                    .map { info ->
                        WidgetProviderMeta(
                            info = info,
                            label = info.loadLabel(pm),
                            description =
                                try {
                                    info.loadDescription(context)?.toString()
                                } catch (_: Exception) {
                                    null
                                },
                        )
                    }
                    .sortedBy { it.label.lowercase() }

            WidgetAppGroup(
                packageName = pkg,
                appLabel = appLabel,
                appIcon = appIcon,
                widgets = widgets,
            )
        }
        .sortedBy { it.appLabel.lowercase() }
}

fun searchWidgetProviders(groups: List<WidgetAppGroup>, query: String): List<WidgetAppGroup> {
    val queryWords = query.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (queryWords.isEmpty()) return groups

    data class ScoredGroup(val group: WidgetAppGroup, val score: Int)

    return groups
        .mapNotNull { group ->
            val appTitleScore =
                queryWords.sumOf { qw -> scoreWord(group.appLabel, qw) * APP_TITLE_WEIGHT }

            val widgetScores =
                group.widgets.map { widget ->
                    val labelScore =
                        queryWords.sumOf { qw -> scoreWord(widget.label, qw) * WIDGET_LABEL_WEIGHT }
                    val descScore =
                        if (widget.description != null) {
                            queryWords.sumOf { qw ->
                                scoreWord(widget.description, qw) * WIDGET_DESC_WEIGHT
                            }
                        } else 0
                    widget to (labelScore + descScore)
                }

            if (appTitleScore > 0) {

                val totalScore = appTitleScore + widgetScores.sumOf { it.second }
                ScoredGroup(group, totalScore)
            } else {

                val matchedWidgets = widgetScores.filter { it.second > 0 }
                if (matchedWidgets.isEmpty()) return@mapNotNull null
                val filteredGroup = group.copy(widgets = matchedWidgets.map { it.first })
                val totalScore = matchedWidgets.sumOf { it.second }
                ScoredGroup(filteredGroup, totalScore)
            }
        }
        .sortedByDescending { it.score }
        .map { it.group }
}

private const val APP_TITLE_WEIGHT = 12
private const val WIDGET_LABEL_WEIGHT = 3
private const val WIDGET_DESC_WEIGHT = 1

private fun scoreWord(target: String, queryWord: String): Int {
    val targetWords = target.trim().lowercase().split("\\s+".toRegex())
    var maxScore = 0
    for (tw in targetWords) {
        val score =
            when {
                tw == queryWord -> 2
                tw.startsWith(queryWord) -> 1
                else -> 0
            }
        if (score > maxScore) maxScore = score
    }
    return maxScore
}

fun loadWidgetPreviewBitmap(context: Context, info: AppWidgetProviderInfo): Bitmap? {
    val dpi = context.resources.displayMetrics.densityDpi
    val density = context.resources.displayMetrics.density

    val previewDrawable = info.loadPreviewImage(context, dpi)
    if (previewDrawable != null) {
        val w = previewDrawable.intrinsicWidth.takeIf { it > 0 } ?: (200 * density).toInt()
        val h = previewDrawable.intrinsicHeight.takeIf { it > 0 } ?: (100 * density).toInt()
        return try {
            previewDrawable.toBitmap(w, h)
        } catch (_: Exception) {
            null
        }
    }

    val iconDrawable = info.loadIcon(context, dpi)
    if (iconDrawable != null) {
        val size = (48 * density).toInt()
        return try {
            iconDrawable.toBitmap(size, size)
        } catch (_: Exception) {
            null
        }
    }

    return null
}
