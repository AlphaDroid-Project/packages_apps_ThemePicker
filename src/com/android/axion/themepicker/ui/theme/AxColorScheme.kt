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

import android.content.Context
import androidx.annotation.ColorRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.android.internal.R
import com.android.axion.themepicker.utils.colors.colorAttr

val LocalAxColorScheme = staticCompositionLocalOf<AxColorScheme> {
    throw IllegalStateException(
        "No AxColorScheme configured. Make sure to provide it via AxTheme {}."
    )
}

@Immutable
class AxColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,

    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,

    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,

    val surface: Color,
    val surfaceContainer: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerLowest: Color,
    
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,

    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,

    val background: Color,
    val onBackground: Color,

    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,

    val axSurface: Color,
    val axBackground: Color,
    
    val textPrimary: Color,
    val textPrimaryInverse: Color,
    val textSecondary: Color,
    
    val outlineVariant: Color,
    val outline: Color,
) {
    companion object {
        private fun color(context: Context, @ColorRes id: Int): Color {
            return Color(context.resources.getColor(id, context.theme))
        }

        operator fun invoke(context: Context): AxColorScheme {
            return AxColorScheme(
                primary = color(context, R.color.materialColorPrimary),
                onPrimary = color(context, R.color.materialColorOnPrimary),
                primaryContainer = color(context, R.color.materialColorPrimaryContainer),
                onPrimaryContainer = color(context, R.color.materialColorOnPrimaryContainer),

                secondary = color(context, R.color.materialColorSecondary),
                onSecondary = color(context, R.color.materialColorOnSecondary),
                secondaryContainer = color(context, R.color.materialColorSecondaryContainer),
                onSecondaryContainer = color(context, R.color.materialColorOnSecondaryContainer),

                tertiary = color(context, R.color.materialColorTertiary),
                onTertiary = color(context, R.color.materialColorOnTertiary),
                tertiaryContainer = color(context, R.color.materialColorTertiaryContainer),
                onTertiaryContainer = color(context, R.color.materialColorOnTertiaryContainer),

                surface = color(context, R.color.materialColorSurface),
                surfaceContainer = color(context, R.color.materialColorSurfaceContainer),
                surfaceContainerLow = color(context, R.color.materialColorSurfaceContainerLow),
                surfaceContainerLowest = color(context, R.color.materialColorSurfaceContainerLowest),

                surfaceContainerHigh = color(context, R.color.materialColorSurfaceContainerHigh),
                surfaceContainerHighest = color(context, R.color.materialColorSurfaceContainerHighest),

                surfaceVariant = color(context, R.color.materialColorSurfaceVariant),
                onSurface = color(context, R.color.materialColorOnSurface),
                onSurfaceVariant = color(context, R.color.materialColorOnSurfaceVariant),

                background = color(context, R.color.materialColorBackground),
                onBackground = color(context, R.color.materialColorOnBackground),

                error = color(context, R.color.materialColorError),
                onError = color(context, R.color.materialColorOnError),
                errorContainer = color(context, R.color.materialColorErrorContainer),
                onErrorContainer = color(context, R.color.materialColorOnErrorContainer),

                axSurface = color(context, R.color.materialColorSurface),
                axBackground = color(context, R.color.materialColorBackground),
                
                textPrimary = color(context, R.color.materialColorOnSurface),
                textPrimaryInverse = color(context, R.color.materialColorOnPrimary), 
                textSecondary = color(context, R.color.materialColorOnSecondary),
                
                outlineVariant = color(context, android.R.color.system_outline_variant_light),
                outline = color(context, android.R.color.system_outline_light),
            )
        }
    }
}

@Composable
fun rememberAxColorScheme(context: Context = LocalContext.current): AxColorScheme {
    val base = AxColorScheme(context)

    val axSurface = if (isSystemInDarkTheme()) {
        colorResource(android.R.color.system_neutral1_800)
    } else {
        colorResource(android.R.color.system_neutral1_0)
    }

    val axBackground = if (isSystemInDarkTheme()) {
        colorResource(android.R.color.system_neutral1_1000)
    } else {
        colorResource(android.R.color.system_neutral1_100)
    }

    val textPrimary = colorAttr(android.R.attr.textColorPrimary)
    val textPrimaryInverse = colorAttr(android.R.attr.textColorPrimaryInverse)
    val textSecondary = colorAttr(android.R.attr.textColorSecondary)
    
    return AxColorScheme(
        primary = base.primary,
        onPrimary = base.onPrimary,
        primaryContainer = base.primaryContainer,
        onPrimaryContainer = base.onPrimaryContainer,

        secondary = base.secondary,
        onSecondary = base.onSecondary,
        secondaryContainer = base.secondaryContainer,
        onSecondaryContainer = base.onSecondaryContainer,

        tertiary = base.tertiary,
        onTertiary = base.onTertiary,
        tertiaryContainer = base.tertiaryContainer,
        onTertiaryContainer = base.onTertiaryContainer,

        surface = base.surface,
        surfaceContainer = base.surfaceContainer,
        surfaceContainerLow = base.surfaceContainerLow,
        surfaceContainerLowest = base.surfaceContainerLowest,

        surfaceContainerHigh = base.surfaceContainerHigh,
        surfaceContainerHighest = base.surfaceContainerHighest,

        surfaceVariant = base.surfaceVariant,
        onSurface = base.onSurface,
        onSurfaceVariant = base.onSurfaceVariant,

        background = base.background,
        onBackground = base.onBackground,

        error = base.error,
        onError = base.onError,
        errorContainer = base.errorContainer,
        onErrorContainer = base.onErrorContainer,

        axSurface = axSurface,
        axBackground = axBackground,
        
        textPrimary = textPrimary,
        textPrimaryInverse = textPrimaryInverse,
        textSecondary = textSecondary,
        
        outlineVariant = base.outlineVariant,
        outline = base.outline,
    )
}
