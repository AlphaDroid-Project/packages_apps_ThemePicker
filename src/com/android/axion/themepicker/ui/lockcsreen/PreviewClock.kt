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
package com.android.axion.themepicker.ui.lockscreen

import android.content.Context
import android.database.ContentObserver
import android.icu.util.TimeZone as IcuTimeZone
import android.os.Vibrator
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import com.android.axion.themepicker.utils.math.scaleRatio
import com.android.systemui.plugins.clocks.*
import com.android.systemui.shared.clocks.NTClockProvider
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.math.cbrt
import kotlin.math.roundToInt
import kotlin.math.sqrt

val Context.previewScale: Float
    get() {
        val displayMetrics = resources.displayMetrics
        val sw = minOf(displayMetrics.widthPixels, displayMetrics.heightPixels) / displayMetrics.density
        
        val isTablet = sw >= 600f
        val baseMultiplier = if (isTablet) 0.24f else 0.42f
        val baseDp = if (isTablet) 600f else 420f
        
        val dpRatio = sw / baseDp
        val adjustedMultiplier = if (isTablet) {
            baseMultiplier * cbrt(dpRatio.toDouble()).toFloat()
        } else {
            baseMultiplier * sqrt(dpRatio)
        }
        
        val maxScale = if (isTablet) 0.32f else 0.55f
        return adjustedMultiplier.coerceIn(0.22f, maxScale)
    }

fun Modifier.scaledLayout(scale: Float, overrideWidth: Dp = Dp.Unspecified): Modifier = this.layout { measurable, constraints ->
    val widthPx = if (overrideWidth != Dp.Unspecified) overrideWidth.roundToPx() else -1
    val childConstraints = if (widthPx != -1) {
        constraints.copy(minWidth = widthPx, maxWidth = widthPx)
    } else {
        constraints
    }
    
    val placeable = measurable.measure(childConstraints)
    val scaledWidth = (placeable.width * scale).roundToInt()
    val scaledHeight = (placeable.height * scale).roundToInt()
    layout(scaledWidth, scaledHeight) {
        placeable.placeWithLayer(
            (scaledWidth - placeable.width) / 2,
            (scaledHeight - placeable.height) / 2
        ) {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin.Center
        }
    }
}

@Composable
fun PreviewClock(isPreview: Boolean, isRegionDark: Boolean = true) {
    val context = LocalContext.current
    val scale = if (isPreview) context.previewScale else context.scaleRatio
    var isLoadedFromSettings by remember { mutableStateOf(false) }

    val clockProvider = remember {
        val vibrator = context.getSystemService(Vibrator::class.java)
        NTClockProvider(
            layoutInflater = LayoutInflater.from(context),
            resources = context.resources,
            isClockReactiveVariantsEnabled = true,
            vibrator = vibrator
        )
    }

    val metadatas = remember { clockProvider.getClocks() }
    val pagerState = rememberPagerState { metadatas.size }
    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }
    var clockTypeObserverTrigger by remember { mutableStateOf(0) }

    if (isPreview) {
        DisposableEffect(Unit) {
            val uri = Settings.Secure.getUriFor("lock_screen_custom_clock_face")
            val resolver = context.contentResolver
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    clockTypeObserverTrigger++
                }
            }
            resolver.registerContentObserver(uri, false, observer)
            onDispose {
                resolver.unregisterContentObserver(observer)
            }
        }
    }

    LaunchedEffect(clockTypeObserverTrigger) {
        if (isPreview) {
            withContext(Dispatchers.IO) {
                try {
                    val json = Settings.Secure.getString(
                        context.contentResolver,
                        "lock_screen_custom_clock_face"
                    )
                    if (!json.isNullOrEmpty()) {
                        val clockId = JSONObject(json).optString("clockId")
                        val index = metadatas.indexOfFirst { it.clockId == clockId }
                        if (index >= 0) {
                            withContext(Dispatchers.Main) {
                                pagerState.scrollToPage(index)
                            }
                        }
                    }
                } catch (_: Exception) { }
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance().time
            delay(1000L)
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val json = Settings.Secure.getString(
                    context.contentResolver,
                    "lock_screen_custom_clock_face"
                )
                if (!json.isNullOrEmpty()) {
                    val clockId = JSONObject(json).optString("clockId")
                    val index = metadatas.indexOfFirst { it.clockId == clockId }
                    if (index >= 0) {
                        withContext(Dispatchers.Main) {
                            pagerState.scrollToPage(index)
                        }
                    }
                }
            } catch (e: Exception) {
            } finally {
                isLoadedFromSettings = true
            }
        }
    }

    LaunchedEffect(pagerState.currentPage, isLoadedFromSettings) {
        if (isLoadedFromSettings) {
            val selectedClock = metadatas[pagerState.currentPage]
            val timestamp = System.currentTimeMillis()
            val json = JSONObject().apply {
                put("clockId", selectedClock.clockId)
                put("metadata", JSONObject().apply {
                    put("appliedTimestamp", timestamp)
                })
                put("axes", JSONArray())
            }.toString()

            withContext(Dispatchers.IO) {
                try {
                    Settings.Secure.putString(
                        context.contentResolver,
                        "lock_screen_custom_clock_face",
                        json
                    )
                } catch (e: Exception) {
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            userScrollEnabled = !isPreview,
            verticalAlignment = Alignment.Top
        ) { page ->
            val metadata = metadatas[page]
            val controller = remember(metadata.clockId) {
                clockProvider.createClock(context, ClockSettings(clockId = metadata.clockId)).apply {
                    initialize(isDarkTheme = true, dozeFraction = 0f, foldFraction = 0f)
                    
                    smallClock.events.onRegionDarknessChanged(isRegionDark)
                    largeClock.events.onRegionDarknessChanged(isRegionDark)
                    
                    events.onLocaleChanged(Locale.getDefault())
                    events.onTimeZoneChanged(IcuTimeZone.getDefault())
                    events.onTimeFormatChanged(TimeFormatKind.getFromContext(context))
                    events.onDateChanged()
                    
                    events.onNTWeatherDataChanged(NTWeatherData("24", 0x1))
                    events.onCalendarDataChanged(CalendarSimpleData(1L, "Meeting", System.currentTimeMillis() + 300000L, System.currentTimeMillis() + 3600000L, null))
                    
                    smallClock.events.onTimeTick()
                    largeClock.events.onTimeTick()
                }
            }

            LaunchedEffect(isRegionDark) {
                controller.smallClock.events.onRegionDarknessChanged(isRegionDark)
                controller.largeClock.events.onRegionDarknessChanged(isRegionDark)
            }

            LaunchedEffect(currentTime) {
                controller.smallClock.events.onTimeTick()
                controller.largeClock.events.onTimeTick()
            }

            val configuration = LocalConfiguration.current
            val fullWidth = configuration.screenWidthDp.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 12.dp * scale),
                contentAlignment = Alignment.Center
            ) {
                SystemUIClockView(
                    controller = controller,
                    modifier = Modifier.scaledLayout(scale, if (isPreview) fullWidth else Dp.Unspecified)
                )
            }
        }

        if (!isPreview) {
            Spacer(modifier = Modifier.height(16.dp * scale))
            ClockIndicator(pageCount = metadatas.size, currentPage = pagerState.currentPage)
            Spacer(modifier = Modifier.height(16.dp * scale))
        }
    }
}

@Composable
fun SystemUIClockView(
    controller: ClockController,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            val clockView = controller.smallClock.view
            (clockView.parent as? ViewGroup)?.removeView(clockView)
            
            clockView.layoutParams = FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )

            FrameLayout(context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )
                
                addView(clockView)
            }
        },
        modifier = modifier.fillMaxWidth().wrapContentHeight(),
        update = {
            controller.smallClock.events.onTimeTick()
        }
    )
}

@Composable
private fun ClockIndicator(pageCount: Int, currentPage: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        animateColorAsState(
                            targetValue = if (isActive) Color.White else Color.White.copy(alpha = 0.3f),
                            animationSpec = tween(durationMillis = 300)
                        ).value
                    )
            )
        }
    }
}
