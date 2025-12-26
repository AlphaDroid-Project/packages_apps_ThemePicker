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
package com.android.axion.themepicker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource

@Composable
fun AxTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    
    val baseColorScheme = if (isDark) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicLightColorScheme(context)
    }
    
    val customBackground = if (isDark) {
        colorResource(android.R.color.system_neutral1_1000)
    } else {
        colorResource(android.R.color.system_neutral1_50)
    }
    
    val colorScheme = baseColorScheme.copy(
        background = customBackground
    )
    
    val expressiveDesign = DefaultExpressiveDesign
    val adaptiveLayoutInfo = calculateAdaptiveLayoutInfo()

    CompositionLocalProvider(
        LocalExpressiveDesign provides expressiveDesign,
        LocalAdaptiveLayoutInfo provides adaptiveLayoutInfo
    ) {
        MaterialTheme(
            colorScheme = colorScheme
        ) {
            content()
        }
    }
}
