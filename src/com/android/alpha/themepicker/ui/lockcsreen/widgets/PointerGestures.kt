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

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull

fun Modifier.allowGestures(allowed: Boolean): Modifier =
    if (allowed) this else this.then(Modifier.pointerInput(Unit) { consumeAllGestures() })

suspend fun PointerInputScope.consumeAllGestures() = coroutineScope {
    awaitEachGesture {
        awaitPointerEvent(pass = PointerEventPass.Initial)
            .changes
            .fastForEach(PointerInputChange::consume)
    }
}

suspend fun PointerInputScope.observeTaps(
    pass: PointerEventPass = PointerEventPass.Initial,
    shouldConsume: Boolean = false,
    onTap: (Offset) -> Unit,
) = coroutineScope {
    awaitEachGesture {
        val down = awaitFirstDown(pass = pass)
        if (shouldConsume) down.consume()
        val tapTimeout = viewConfiguration.longPressTimeoutMillis
        val up = withTimeoutOrNull(tapTimeout) { waitForUpOrCancellation(pass = pass) }
        if (up != null) onTap(up.position)
    }
}
