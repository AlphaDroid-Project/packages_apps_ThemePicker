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
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.axion.themepicker.data.model.LayoutScreen
import com.android.axion.themepicker.data.model.LayoutPreferenceItem
import com.android.axion.themepicker.ui.components.ScreenTransition
import com.android.axion.themepicker.ui.expressive.ExpressiveHeader
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.ui.themes.FontScreen
import com.android.axion.themepicker.utils.math.scaleRatio
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
            is LayoutScreen.Font -> {
                BackHandler {
                    layoutScreenViewModel.goBackInLayout(mainScreenViewModel)
                }
                FontScreen(layoutScreenViewModel, mainScreenViewModel)
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
    val scale = LocalContext.current.scaleRatio
    val appIcons by layoutScreenViewModel.appIcons.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceContainerHigh)
    ) {
        ExpressiveHeader(
            title = "Appearance",
            subtitle = null,
            onBackClick = { mainScreenViewModel.resetToMain() },
            onActionClick = null
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp * scale, vertical = 16.dp * scale),
            verticalArrangement = Arrangement.spacedBy(12.dp * scale),
            modifier = Modifier.fillMaxSize()
        ) {
            items(layoutScreenViewModel.preferenceItems.size) { index ->
                val item = layoutScreenViewModel.preferenceItems[index]
                
                when (item.title) {
                    "App Grid" -> AppGridCard(item, appIcons, scale, layoutScreenViewModel, mainScreenViewModel)
                    "Font" -> FontCard(item, scale, layoutScreenViewModel, mainScreenViewModel)
                }
            }
        }
    }
}

@Composable
private fun AppGridCard(
    item: LayoutPreferenceItem,
    appIcons: List<android.graphics.drawable.Drawable>,
    scale: Float,
    layoutScreenViewModel: LayoutScreenViewModel,
    mainScreenViewModel: MainScreenViewModel
) {
    val colors = LocalAxColorScheme.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp * scale))
            .clickable {
                layoutScreenViewModel.onItemSelected(item, mainScreenViewModel)
            },
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp * scale),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp * scale))
            
            Box(
                modifier = Modifier
                    .size(48.dp * scale)
                    .clip(RoundedCornerShape(12.dp * scale))
                    .background(colors.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp * scale),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp * scale)) {
                        repeat(2) { index ->
                            if (appIcons.size > index) {
                                androidx.compose.foundation.Image(
                                    painter = rememberDrawablePainter(drawable = appIcons[index]),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(16.dp * scale)
                                        .clip(RoundedCornerShape(4.dp * scale))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp * scale)
                                        .clip(RoundedCornerShape(4.dp * scale))
                                        .background(colors.primary.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp * scale)) {
                        repeat(2) { index ->
                            val iconIndex = index + 2
                            if (appIcons.size > iconIndex) {
                                androidx.compose.foundation.Image(
                                    painter = rememberDrawablePainter(drawable = appIcons[iconIndex]),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(16.dp * scale)
                                        .clip(RoundedCornerShape(4.dp * scale))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp * scale)
                                        .clip(RoundedCornerShape(4.dp * scale))
                                        .background(colors.primary.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FontCard(
    item: LayoutPreferenceItem,
    scale: Float,
    layoutScreenViewModel: LayoutScreenViewModel,
    mainScreenViewModel: MainScreenViewModel
) {
    val colors = LocalAxColorScheme.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp * scale)
            .clip(RoundedCornerShape(28.dp * scale))
            .clickable {
                layoutScreenViewModel.onItemSelected(item, mainScreenViewModel)
            },
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp * scale)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp * scale),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp * scale)
            ) {
                Text(
                    text = "Aa",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Text(
                    text = "A is for Axion :)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}
