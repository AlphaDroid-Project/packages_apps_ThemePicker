/*
 * Copyright (C) 2025 AxionOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.axion.themepicker.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.*

@Immutable
data class ExpressiveMotion(
    
    val emphasisHigh: MotionSpec = MotionSpec(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    ),
    val emphasisMedium: MotionSpec = MotionSpec(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    val emphasisLow: MotionSpec = MotionSpec(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    ),
    
    val buttonPress: MotionSpec = MotionSpec(
        dampingRatio = 0.6f,
        stiffness = 400f
    ),
    val cardPress: MotionSpec = MotionSpec(
        dampingRatio = 0.65f,
        stiffness = 350f
    ),
    val containerTransform: MotionSpec = MotionSpec(
        dampingRatio = 0.8f,
        stiffness = 380f
    ),
    
    val screenEnter: MotionSpec = MotionSpec(
        dampingRatio = 0.85f,
        stiffness = 400f
    ),
    val screenExit: MotionSpec = MotionSpec(
        dampingRatio = 1f,
        stiffness = 500f
    ),
    
    val durationShort: Int = 150,
    val durationMedium: Int = 300,
    val durationLong: Int = 500,
    val durationExtraLong: Int = 700
)

@Immutable
data class MotionSpec(
    val dampingRatio: Float,
    val stiffness: Float
) {
    fun <T> asSpring() = spring<T>(
        dampingRatio = dampingRatio,
        stiffness = stiffness
    )
}

@Immutable
data class ExpressiveShapes(
    val cornerExtraSmall: Dp = 4.dp,
    val cornerSmall: Dp = 8.dp,
    val cornerMedium: Dp = 12.dp,
    val cornerLarge: Dp = 16.dp,
    val cornerExtraLarge: Dp = 28.dp,
    val cornerFull: Dp = 1000.dp, 
    
    val cardCorner: Dp = 24.dp,
    val buttonCorner: Dp = 16.dp,
    val chipCorner: Dp = 12.dp,
    val dialogCorner: Dp = 28.dp,
    val sheetCorner: Dp = 28.dp,
    val previewCorner: Dp = 20.dp
)

@Immutable
data class ExpressiveElevation(
    val level0: Dp = 0.dp,
    val level1: Dp = 1.dp,
    val level2: Dp = 3.dp,
    val level3: Dp = 6.dp,
    val level4: Dp = 8.dp,
    val level5: Dp = 12.dp,
    
    
    val card: Dp = 0.dp, 
    val cardHovered: Dp = 1.dp,
    val cardPressed: Dp = 0.dp,
    val fab: Dp = 6.dp,
    val fabHovered: Dp = 8.dp,
    val dialog: Dp = 6.dp,
    val sheet: Dp = 1.dp
)

@Immutable
data class ExpressiveSpacing(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 48.dp,
    
    val cardPadding: Dp = 16.dp,
    val listItemPadding: Dp = 16.dp,
    val sectionSpacing: Dp = 24.dp,
    val screenPadding: Dp = 16.dp,
    val screenPaddingTablet: Dp = 24.dp
)

@Immutable
data class ExpressiveDesign(
    val motion: ExpressiveMotion = ExpressiveMotion(),
    val shapes: ExpressiveShapes = ExpressiveShapes(),
    val elevation: ExpressiveElevation = ExpressiveElevation(),
    val spacing: ExpressiveSpacing = ExpressiveSpacing()
)

val LocalExpressiveDesign = staticCompositionLocalOf { ExpressiveDesign() }
val DefaultExpressiveDesign = ExpressiveDesign()

object ExpressiveScale {
    const val pressed = 0.96f
    const val hovered = 1.02f
    const val normal = 1f
    const val selected = 1.05f
}

object ExpressiveAlpha {
    const val high = 1f
    const val medium = 0.74f
    const val disabled = 0.38f
    const val overlay = 0.12f
    const val scrim = 0.32f
}
