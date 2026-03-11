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

package com.android.axion.themepicker.ui.theme

import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

enum class DeviceType {
    PHONE,
    TABLET,
    FOLDABLE_UNFOLDED,
}

enum class NavigationMode {
    BOTTOM_BAR,
    NAVIGATION_RAIL,
    NAVIGATION_DRAWER,
}

enum class LayoutMode {
    SINGLE_PANE,
    DUAL_PANE,
    TRIPLE_PANE,
}

@Immutable
data class AdaptiveLayoutInfo(
    val windowWidthSizeClass: WindowWidthSizeClass,
    val windowHeightSizeClass: WindowHeightSizeClass,
    val deviceType: DeviceType,
    val navigationMode: NavigationMode,
    val layoutMode: LayoutMode,
    val isLandscape: Boolean,
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
) {
    val isTablet: Boolean
        get() = deviceType == DeviceType.TABLET || deviceType == DeviceType.FOLDABLE_UNFOLDED

    val isPhone: Boolean
        get() = deviceType == DeviceType.PHONE

    val showNavigationRail: Boolean
        get() =
            navigationMode == NavigationMode.NAVIGATION_RAIL ||
                navigationMode == NavigationMode.NAVIGATION_DRAWER

    val showBottomBar: Boolean
        get() = navigationMode == NavigationMode.BOTTOM_BAR

    val isDualPane: Boolean
        get() = layoutMode == LayoutMode.DUAL_PANE || layoutMode == LayoutMode.TRIPLE_PANE
}

val LocalAdaptiveLayoutInfo =
    staticCompositionLocalOf<AdaptiveLayoutInfo> {
        throw IllegalStateException(
            "No AdaptiveLayoutInfo configured. Make sure to provide it via AxTheme {}."
        )
    }

@Composable
fun calculateAdaptiveLayoutInfo(): AdaptiveLayoutInfo {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val windowWidthSizeClass =
        when {
            screenWidthDp < 600.dp -> WindowWidthSizeClass.COMPACT
            screenWidthDp < 840.dp -> WindowWidthSizeClass.MEDIUM
            else -> WindowWidthSizeClass.EXPANDED
        }

    val windowHeightSizeClass =
        when {
            screenHeightDp < 480.dp -> WindowHeightSizeClass.COMPACT
            screenHeightDp < 900.dp -> WindowHeightSizeClass.MEDIUM
            else -> WindowHeightSizeClass.EXPANDED
        }

    val hasHinge =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE)

    val deviceType =
        when {
            hasHinge && screenWidthDp >= 600.dp -> DeviceType.FOLDABLE_UNFOLDED
            screenWidthDp >= 600.dp -> DeviceType.TABLET
            else -> DeviceType.PHONE
        }

    val navigationMode =
        when {
            screenWidthDp >= 840.dp && isLandscape -> NavigationMode.NAVIGATION_DRAWER
            screenWidthDp >= 600.dp -> NavigationMode.NAVIGATION_RAIL
            else -> NavigationMode.BOTTOM_BAR
        }

    val layoutMode =
        when {
            screenWidthDp >= 600.dp -> LayoutMode.DUAL_PANE
            else -> LayoutMode.SINGLE_PANE
        }

    return AdaptiveLayoutInfo(
        windowWidthSizeClass = windowWidthSizeClass,
        windowHeightSizeClass = windowHeightSizeClass,
        deviceType = deviceType,
        navigationMode = navigationMode,
        layoutMode = layoutMode,
        isLandscape = isLandscape,
        screenWidthDp = screenWidthDp,
        screenHeightDp = screenHeightDp,
    )
}

@Composable
fun adaptiveGridColumns(minItemWidth: Dp = 160.dp, maxColumns: Int = 6): Int {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val calculatedColumns = (screenWidthDp / minItemWidth).toInt().coerceAtLeast(2)
    return calculatedColumns.coerceAtMost(maxColumns)
}

@Composable
fun adaptiveContentPadding(): Dp {
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    return when {
        layoutInfo.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED -> 32.dp
        layoutInfo.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM -> 24.dp
        else -> 16.dp
    }
}

@Composable
fun adaptiveSpacing(): Dp {
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    return when {
        layoutInfo.isTablet -> 24.dp
        else -> 16.dp
    }
}

@Composable
fun calculatePreviewSize(
    maxWidth: Dp,
    maxHeight: Dp,
    aspectRatio: Float =
        LocalAdaptiveLayoutInfo.current.let { it.screenWidthDp.value / it.screenHeightDp.value },
): Pair<Dp, Dp> {
    val layoutInfo = LocalAdaptiveLayoutInfo.current

    val baseWidth =
        when {
            layoutInfo.isTablet -> minOf(maxWidth * 0.4f, 280.dp)
            else -> minOf(maxWidth * 0.6f, 200.dp)
        }
    val height = baseWidth / aspectRatio

    return if (height > maxHeight) {
        val adjustedHeight = maxHeight
        val adjustedWidth = adjustedHeight * aspectRatio
        Pair(adjustedWidth, adjustedHeight)
    } else {
        Pair(baseWidth, height)
    }
}

@Composable
fun Float.ofScreenWidth(): Dp {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    return screenWidthDp * this
}

@Composable
fun Float.ofScreenHeight(): Dp {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    return screenHeightDp * this
}
