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
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.themepicker.ui.lockscreen

import androidx.compose.foundation.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.res.*
import com.android.axion.themepicker.R

@Composable
fun LockscreenPreviewTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    val colorScheme = 
        if (darkTheme) dynamicDarkColorScheme(context) 
        else dynamicLightColorScheme(context)

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}

@Composable
fun WidgetIcon(widget: String): ImageVector {
    val keys = stringArrayResource(R.array.widget_keys)
    return when (widget) {
        keys[0] -> Icons.Filled.FlashlightOff
        keys[1] -> Icons.Filled.WifiOff
        keys[2] -> Icons.Filled.SignalCellularOff
        keys[3] -> Icons.Filled.VolumeUp 
        keys[4] -> Icons.Filled.BluetoothDisabled
        keys[5] -> Icons.Filled.WifiTethering
        else -> Icons.Filled.DeviceUnknown
    }
}

@Composable
fun WidgetLabel(widget: String): String {
    val keys = stringArrayResource(R.array.widget_keys)
    val labels = stringArrayResource(R.array.widget_labels)
    val index = keys.indexOf(widget)
    return if (index != -1 && index < labels.size) labels[index] else widget
}

@Composable
fun WidgetsList(): List<String> {
    return stringArrayResource(R.array.widget_keys).toList()
}

@Composable
fun surface(): Color {
    val colorRes = if (isSystemInDarkTheme()) {
            android.R.color.system_neutral1_800 
        } else android.R.color.system_neutral1_0  
    return colorResource(id = colorRes)
}

@Composable
fun onSurface(): Color {
    val colorRes = if (isSystemInDarkTheme()) {
            android.R.color.system_neutral2_50 
        } else android.R.color.system_neutral2_900  
    return colorResource(id = colorRes)
}

@Composable
fun surfaceVariant(): Color {
    val colorRes = if (isSystemInDarkTheme()) {
            android.R.color.system_neutral1_900 
        } else android.R.color.system_neutral1_50  
    return colorResource(id = colorRes)
}

object Dimens {
    val ClockDateFont = 16.dp
    val ClockTimeFont = 90.dp
    val ClockTopPadding = 32.dp
    val ClockSidePadding = 24.dp
    val ClockSpacer = 16.dp
    val WeatherIcon = 20.dp
    val WeatherSpacerSmall = 4.dp
    val WeatherSpacerLarge = 8.dp

    val WidgetSlot = 72.dp
    val WidgetSpacing = 12.dp
    val WidgetContainerHeight = 112.dp
    val WidgetContainerPadding = 24.dp
    val WidgetBorderWidth = 1.5.dp
    val WidgetCorner = 22.dp
    val WidgetDivider = 1.dp

    val TileCorner = 24.dp
    val TileBorder = 6.dp
    val TileIcon = 24.dp
    val TileTextSpacer = 8.dp
    val TilePagerIndicator = 8.dp
}
