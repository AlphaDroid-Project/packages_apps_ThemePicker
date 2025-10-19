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
package com.android.axion.themepicker.ui.themes

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.axion.themepicker.data.model.LayoutScreen
import com.android.axion.themepicker.ui.components.ScreenTransition
import com.android.axion.themepicker.ui.expressive.ExpressiveHeader
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.ui.themes.FontScreen
import com.android.axion.themepicker.ui.themes.SystemIconsScreen
import com.android.axion.themepicker.utils.wallpaper.rememberDrawablePainter
import com.android.axion.themepicker.viewmodel.LayoutScreenViewModel
import com.android.axion.themepicker.viewmodel.MainScreenViewModel

@Composable
fun LayoutMainScreen(
    mainScreenViewModel: MainScreenViewModel = viewModel(),
    layoutScreenViewModel: LayoutScreenViewModel = viewModel()
) {
    val currentLayoutScreen by layoutScreenViewModel.currentLayoutScreen.collectAsState()
    val isNavigatingBack by layoutScreenViewModel.isNavigatingBack.collectAsState()

    ScreenTransition(
        targetState = currentLayoutScreen,
        isNavigatingBack = isNavigatingBack
    ) { screen ->
        when (screen) {
            is LayoutScreen.Root -> {
                BackHandler {
                    layoutScreenViewModel.goBackInLayout(mainScreenViewModel)
                }
                LayoutRootScreen(
                    layoutScreenViewModel = layoutScreenViewModel,
                    mainScreenViewModel = mainScreenViewModel
                )
            }
            is LayoutScreen.AppGridSettings -> {
                AppGridSettingsScreen(layoutScreenViewModel, mainScreenViewModel)
            }
            is LayoutScreen.SystemIcons -> {
                BackHandler {
                    layoutScreenViewModel.goBackInLayout(mainScreenViewModel)
                }
                SystemIconsScreen(layoutScreenViewModel, mainScreenViewModel)
            }
            is LayoutScreen.Font -> {
                BackHandler {
                    layoutScreenViewModel.goBackInLayout(mainScreenViewModel)
                }
                FontScreen(layoutScreenViewModel, mainScreenViewModel)
            }
            is LayoutScreen.Shape -> {
                BackHandler {
                    layoutScreenViewModel.goBackInLayout(mainScreenViewModel)
                }
                ShapeScreen(layoutScreenViewModel, mainScreenViewModel)
            }
        }
    }
}

@Composable
private fun LayoutRootScreen(
    layoutScreenViewModel: LayoutScreenViewModel,
    mainScreenViewModel: MainScreenViewModel
) {
    val colors = LocalAxColorScheme.current
    val appIcons by layoutScreenViewModel.appIcons.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceContainerLow)
    ) {
        ExpressiveHeader(
            title = "Appearance",
            subtitle = null,
            onBackClick = { mainScreenViewModel.resetToMain() },
            onActionClick = null
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(layoutScreenViewModel.preferenceItems) { item ->
                Card(
                    modifier = Modifier
                        .aspectRatio(1.1f)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable {
                            layoutScreenViewModel.onItemSelected(item, mainScreenViewModel)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            when (item.title) {
                                "App Grid" -> AppGridIllustration(appIcons)
                                "System Icons" -> SystemIconsIllustration()
                                "Font" -> FontIllustration()
                                "Shape" -> ShapeIllustration()
                            }
                        }

                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppGridIllustration(appIcons: List<android.graphics.drawable.Drawable>) {
    val colors = LocalAxColorScheme.current
    val icons = appIcons.take(3)
    val placeholderTint = colors.primary.copy(alpha = 0.3f)

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (icons.isEmpty()) {
            repeat(3) {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = null,
                    tint = placeholderTint,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else {
            icons.forEach { drawable ->
                androidx.compose.foundation.Image(
                    painter = rememberDrawablePainter(drawable = drawable),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable
private fun SystemIconsIllustration() {
    val colors = LocalAxColorScheme.current
    val icons = listOf(
        Icons.Default.Wifi,
        Icons.Default.BatteryFull,
        Icons.Default.Notifications,
        Icons.Default.Settings
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icons.forEach {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun FontIllustration() {
    val colors = LocalAxColorScheme.current
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = "Aa",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.primary
        )
        Text(
            text = "A is for Axion :)",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun ShapeIllustration() {
    val colors = LocalAxColorScheme.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.primary.copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.6f))
        )
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(50))
                .background(colors.primary.copy(alpha = 0.3f))
        )
    }
}
