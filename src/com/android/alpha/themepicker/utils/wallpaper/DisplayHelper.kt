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

package com.android.alpha.themepicker.utils.wallpaper

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.DisplayInfo

object DisplayHelper {

    private const val TABLET_MIN_DPS = 600f

    enum class DeviceDisplayType {

        SINGLE,
        FOLDED,
        UNFOLDED,
    }

    fun getInternalDisplays(context: Context): List<Display> {
        val dm = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val allDisplays = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_ALL_INCLUDING_DISABLED)
        if (allDisplays.isEmpty()) {
            return listOfNotNull(context.display)
        }
        val internals = allDisplays.filter { it.type == Display.TYPE_INTERNAL }
        return internals.ifEmpty { listOfNotNull(context.display) }
    }

    fun hasMultiInternalDisplays(context: Context): Boolean {
        return getInternalDisplays(context).size > 1
    }

    fun getWallpaperDisplay(context: Context): Display {
        val displays = getInternalDisplays(context)
        return displays.maxByOrNull { display ->
            val info = DisplayInfo()
            display.getDisplayInfo(info)
            info.logicalWidth * info.logicalHeight
        } ?: displays[0]
    }

    fun getSmallerDisplay(context: Context): Display {
        val displays = getInternalDisplays(context)
        val largest = getWallpaperDisplay(context)
        return displays.firstOrNull { it.uniqueId != largest.uniqueId } ?: largest
    }

    fun getRealSize(display: Display): Point {
        val info = DisplayInfo()
        display.getDisplayInfo(info)
        return Point(info.logicalWidth, info.logicalHeight)
    }

    fun isLargeScreenDevice(context: Context): Boolean {
        val maxDisplaySize = getRealSize(getWallpaperDisplay(context))
        val smallestWidth = minOf(maxDisplaySize.x, maxDisplaySize.y).toFloat()
        val densityDpi = context.resources.configuration.densityDpi
        val smallestWidthDp = smallestWidth * 160f / densityDpi
        return smallestWidthDp >= TABLET_MIN_DPS
    }

    fun isOnWallpaperDisplay(context: Context): Boolean {
        val contextDisplay = context.display ?: return true
        return contextDisplay.uniqueId == getWallpaperDisplay(context).uniqueId
    }

    fun getCurrentDisplayType(context: Context): DeviceDisplayType {
        if (!hasMultiInternalDisplays(context)) {
            return DeviceDisplayType.SINGLE
        }
        return if (isOnWallpaperDisplay(context)) {
            DeviceDisplayType.UNFOLDED
        } else {
            DeviceDisplayType.FOLDED
        }
    }

    fun getInternalDisplaySizes(context: Context, allDimensions: Boolean = false): List<Point> {
        val sizes = getInternalDisplays(context).map { getRealSize(it) }
        return if (allDimensions) {
            sizes + sizes.map { Point(it.y, it.x) }
        } else {
            sizes
        }
    }

    fun getWallpaperDisplaySize(context: Context): Point {
        return getRealSize(getWallpaperDisplay(context))
    }

    fun getSystemWallpaperMaxScale(context: Context): Float {
        return try {
            context.resources.getFloat(
                Resources.getSystem().getIdentifier("config_wallpaperMaxScale", "dimen", "android")
            )
        } catch (_: Exception) {
            1.1f
        }
    }
}
