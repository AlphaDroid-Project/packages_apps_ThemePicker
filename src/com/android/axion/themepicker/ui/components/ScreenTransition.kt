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
package com.android.axion.themepicker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.IntOffset
import com.android.axion.themepicker.ui.theme.LocalExpressiveDesign

@Composable
fun <T> ScreenTransition(
    targetState: T,
    isNavigatingBack: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    val design = LocalExpressiveDesign.current
    
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val back = isNavigatingBack
            val direction = if (back) SlideDirection.End else SlideDirection.Start
            val oppositeDirection = if (back) SlideDirection.Start else SlideDirection.End
            
            val enterSpec = spring<IntOffset>(
                dampingRatio = design.motion.screenEnter.dampingRatio,
                stiffness = design.motion.screenEnter.stiffness
            )
            val exitSpec = tween<IntOffset>(
                durationMillis = design.motion.durationMedium,
                easing = FastOutSlowInEasing
            )
            
            (slideIntoContainer(towards = direction, animationSpec = enterSpec) + 
             fadeIn(animationSpec = tween(design.motion.durationShort))) togetherWith
            (slideOutOfContainer(towards = direction, animationSpec = exitSpec) + 
             fadeOut(animationSpec = tween(design.motion.durationShort)))
        },
        label = "screen_transition",
        modifier = modifier
    ) { state ->
        content(state)
    }
}

@Composable
fun <T> SharedElementTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedContentScope.(T) -> Unit
) {
    val design = LocalExpressiveDesign.current
    
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            fadeIn(animationSpec = tween(design.motion.durationMedium)) +
            scaleIn(
                initialScale = 0.92f,
                animationSpec = spring(
                    dampingRatio = design.motion.containerTransform.dampingRatio,
                    stiffness = design.motion.containerTransform.stiffness
                )
            ) togetherWith
            fadeOut(animationSpec = tween(design.motion.durationShort)) +
            scaleOut(
                targetScale = 1.05f,
                animationSpec = tween(design.motion.durationShort)
            )
        },
        label = "shared_element_transition",
        modifier = modifier
    ) { state ->
        content(state)
    }
}

/**
 * Vertical slide transition for bottom sheets and dialogs
 */
@Composable
fun <T> VerticalSlideTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    val design = LocalExpressiveDesign.current
    
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = design.motion.emphasisMedium.dampingRatio,
                    stiffness = design.motion.emphasisMedium.stiffness
                )
            ) + fadeIn() togetherWith
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(design.motion.durationMedium)
            ) + fadeOut()
        },
        label = "vertical_slide_transition",
        modifier = modifier
    ) { state ->
        content(state)
    }
}
