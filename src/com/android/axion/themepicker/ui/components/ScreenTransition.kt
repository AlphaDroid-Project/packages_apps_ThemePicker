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

package com.android.axion.themepicker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset

private const val SCALE_INITIAL = 0.92f
private const val SCALE_TARGET = 1.05f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> ScreenTransition(
    targetState: T,
    isNavigatingBack: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val effectsSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val direction = if (isNavigatingBack) SlideDirection.End else SlideDirection.Start

            (slideIntoContainer(towards = direction, animationSpec = spatialSpec) +
                fadeIn(animationSpec = effectsSpec)) togetherWith
                (slideOutOfContainer(towards = direction, animationSpec = spatialSpec) +
                    fadeOut(animationSpec = effectsSpec)) using
                SizeTransform(clip = false)
        },
        label = "screen_transition",
        modifier = modifier,
    ) { state ->
        content(state)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> SharedElementTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedContentScope.(T) -> Unit,
) {
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val effectsSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            (fadeIn(animationSpec = effectsSpec) +
                scaleIn(initialScale = SCALE_INITIAL, animationSpec = spatialSpec)) togetherWith
                (fadeOut(animationSpec = effectsSpec) +
                    scaleOut(targetScale = SCALE_TARGET, animationSpec = spatialSpec)) using
                SizeTransform(clip = false)
        },
        label = "shared_element_transition",
        modifier = modifier,
    ) { state ->
        content(state)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> VerticalSlideTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val effectsSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            (slideInVertically(initialOffsetY = { it }, animationSpec = spatialSpec) +
                fadeIn(animationSpec = effectsSpec)) togetherWith
                (slideOutVertically(targetOffsetY = { it }, animationSpec = spatialSpec) +
                    fadeOut(animationSpec = effectsSpec)) using
                SizeTransform(clip = false)
        },
        label = "vertical_slide_transition",
        modifier = modifier,
    ) { state ->
        content(state)
    }
}
