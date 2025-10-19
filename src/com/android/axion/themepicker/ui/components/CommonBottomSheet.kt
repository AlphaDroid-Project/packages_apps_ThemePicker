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
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import kotlin.coroutines.*
import kotlinx.coroutines.*

object SheetDimens {
    val SheetCorner = 28.dp
    val SheetTopPadding = 24.dp
    val SheetPagerTop = 40.dp
    val SheetPagerPadding = 24.dp
    val SheetPagerSpacing = 16.dp
    val SheetPagerSpacingNav = 28.dp
    val SheetCloseSize = 40.dp
    val SheetTitleFont = 22.sp
    val SheetSpacerSmall = 12.dp
    val SheetSpacerMedium = 16.dp
}

@Composable
fun CommonBottomSheet(
    visible: Boolean,
    title: String,
    heightFraction: Float = 0.4f,
    surfaceColor: Color? = null,
    onDismiss: () -> Unit,
    onOffsetChanged: ((currentOffset: Float, maxOffset: Float) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = LocalAxColorScheme.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val sheetHeightPx = screenHeightPx * heightFraction

        val offsetY = remember { Animatable(sheetHeightPx + 50f) }

        LaunchedEffect(offsetY.value) {
            onOffsetChanged?.invoke(offsetY.value, sheetHeightPx + 50f)
        }

        LaunchedEffect(visible) {
            if (visible) {
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            } else {
                offsetY.animateTo(
                    targetValue = sheetHeightPx + 50f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            }
        }

        val backdropAlpha by animateFloatAsState(
            targetValue = if (visible) 0.5f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "BackdropAlpha"
        )

        if (visible || offsetY.value < sheetHeightPx) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = backdropAlpha))
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            coroutineScope.launch {
                                offsetY.animateTo(
                                    targetValue = sheetHeightPx + 50f,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                )
                                onOffsetChanged?.invoke(offsetY.value, sheetHeightPx + 50f)
                                onDismiss()
                            }
                        })
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { sheetHeightPx.toDp() })
                        .offset(y = with(density) { offsetY.value.toDp() })
                        .background(
                            surfaceColor ?: colors.surfaceContainerLowest,
                            RoundedCornerShape(
                                topStart = SheetDimens.SheetCorner,
                                topEnd = SheetDimens.SheetCorner
                            )
                        )
                        .clip(
                            RoundedCornerShape(
                                topStart = SheetDimens.SheetCorner,
                                topEnd = SheetDimens.SheetCorner
                            )
                        )
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { _, dragAmount ->
                                    val newOffset = (offsetY.value + dragAmount).coerceAtLeast(0f)
                                    coroutineScope.launch {
                                        offsetY.snapTo(newOffset)
                                    }
                                },
                                onDragEnd = {
                                    coroutineScope.launch {
                                        if (offsetY.value > sheetHeightPx * 0.25f) {
                                            offsetY.animateTo(
                                                targetValue = sheetHeightPx + 50f,
                                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                                            )
                                            onOffsetChanged?.invoke(offsetY.value, sheetHeightPx + 50f)
                                            onDismiss()
                                        } else {
                                            offsetY.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                                            )
                                            onOffsetChanged?.invoke(offsetY.value, sheetHeightPx + 50f)
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = SheetDimens.SheetTopPadding)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(colors.outlineVariant)
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = title,
                            fontSize = SheetDimens.SheetTitleFont,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = SheetDimens.SheetPagerPadding)
                                .padding(bottom = 20.dp),
                            textAlign = TextAlign.Start
                        )

                        content()
                    }
                }
            }
        }
    }
}
