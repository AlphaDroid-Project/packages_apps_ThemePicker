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

@Composable
fun <T> ScreenTransition(
    targetState: T,
    isNavigatingBack: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val back = isNavigatingBack
            val direction = if (back)
                AnimatedContentTransitionScope.SlideDirection.Right
            else
                AnimatedContentTransitionScope.SlideDirection.Left
            val slideSpec = tween<IntOffset>(200, easing = LinearOutSlowInEasing)
            slideIntoContainer(towards = direction, animationSpec = slideSpec) togetherWith
            slideOutOfContainer(towards = direction, animationSpec = slideSpec)
        },
        label = "screen_transition",
        modifier = modifier
    ) { state ->
        content(state)
    }
}
