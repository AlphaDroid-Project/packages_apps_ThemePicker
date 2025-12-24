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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.axion.themepicker.data.model.LayoutScreen
import com.android.axion.themepicker.data.model.LayoutPreferenceItem
import com.android.axion.themepicker.ui.components.ScreenTransition
import com.android.axion.themepicker.ui.expressive.ExpressiveHeader
import com.android.axion.themepicker.ui.theme.*
import com.android.axion.themepicker.ui.themes.FontScreen
import com.android.axion.themepicker.utils.math.scaleRatio
import com.android.axion.themepicker.utils.wallpaper.rememberDrawablePainter
import com.android.axion.themepicker.viewmodel.LayoutScreenViewModel
import com.android.axion.themepicker.viewmodel.MainScreenViewModel

/**
 * Layout Main Screen - Material 3 Expressive
 * 
 * Adaptive layout for appearance customization options
 */
@Composable
fun LayoutMainScreen(
    mainScreenViewModel: MainScreenViewModel = viewModel(),
    layoutScreenViewModel: LayoutScreenViewModel = viewModel(),
    startWithAppGrid: Boolean = false,
    startWithFonts: Boolean = false
) {
    val currentLayoutScreen by layoutScreenViewModel.currentLayoutScreen.collectAsState()
    val isNavigatingBack by layoutScreenViewModel.isNavigatingBack.collectAsState()
    
    // Navigate to correct screen on startup if deep linking
    LaunchedEffect(startWithAppGrid, startWithFonts) {
        when {
            startWithAppGrid -> layoutScreenViewModel.navigateToAppGrid()
            startWithFonts -> layoutScreenViewModel.navigateToFonts()
        }
    }

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
    val design = LocalExpressiveDesign.current
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    val appIcons by layoutScreenViewModel.appIcons.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceContainerHigh)
    ) {
        ExpressiveHeader(
            title = "Appearance",
            subtitle = if (layoutInfo.isTablet) "Customize fonts, icons, and grid" else null,
            onBackClick = { mainScreenViewModel.resetToMain() },
            onActionClick = null
        )

        if (layoutInfo.isDualPane) {
            // Tablet: Grid layout
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(design.spacing.screenPaddingTablet),
                horizontalArrangement = Arrangement.spacedBy(design.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(design.spacing.medium),
                modifier = Modifier.fillMaxSize()
            ) {
                items(layoutScreenViewModel.preferenceItems.size) { index ->
                    val item = layoutScreenViewModel.preferenceItems[index]
                    
                    when (item.title) {
                        "App Grid" -> AppGridCard(
                            item = item,
                            appIcons = appIcons,
                            layoutScreenViewModel = layoutScreenViewModel,
                            mainScreenViewModel = mainScreenViewModel,
                            modifier = Modifier.fillMaxWidth()
                        )
                        "Font" -> FontCard(
                            item = item,
                            layoutScreenViewModel = layoutScreenViewModel,
                            mainScreenViewModel = mainScreenViewModel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // Phone: Vertical list
            LazyColumn(
                contentPadding = PaddingValues(design.spacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(design.spacing.medium),
                modifier = Modifier.fillMaxSize()
            ) {
                items(layoutScreenViewModel.preferenceItems.size) { index ->
                    val item = layoutScreenViewModel.preferenceItems[index]
                    
                    when (item.title) {
                        "App Grid" -> AppGridCard(
                            item = item,
                            appIcons = appIcons,
                            layoutScreenViewModel = layoutScreenViewModel,
                            mainScreenViewModel = mainScreenViewModel
                        )
                        "Font" -> FontCard(
                            item = item,
                            layoutScreenViewModel = layoutScreenViewModel,
                            mainScreenViewModel = mainScreenViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppGridCard(
    item: LayoutPreferenceItem,
    appIcons: List<android.graphics.drawable.Drawable>,
    layoutScreenViewModel: LayoutScreenViewModel,
    mainScreenViewModel: MainScreenViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "app_grid_card_scale"
    )
    
    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                layoutScreenViewModel.onItemSelected(item, mainScreenViewModel)
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF10B981),
                            Color(0xFF06B6D4),
                            Color(0xFF3B82F6)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                
                // App grid preview
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(2) { index ->
                                if (appIcons.size > index) {
                                    Image(
                                        painter = rememberDrawablePainter(drawable = appIcons[index]),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                } else {
                                    AppGridPlaceholder()
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(2) { index ->
                                val iconIndex = index + 2
                                if (appIcons.size > iconIndex) {
                                    Image(
                                        painter = rememberDrawablePainter(drawable = appIcons[iconIndex]),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                } else {
                                    AppGridPlaceholder()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppGridPlaceholder() {
    val colors = LocalAxColorScheme.current
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(colors.primary.copy(alpha = 0.3f))
    )
}

@Composable
private fun FontCard(
    item: LayoutPreferenceItem,
    layoutScreenViewModel: LayoutScreenViewModel,
    mainScreenViewModel: MainScreenViewModel,
    modifier: Modifier = Modifier
) {
    val design = LocalExpressiveDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "font_card_scale"
    )
    
    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                layoutScreenViewModel.onItemSelected(item, mainScreenViewModel)
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6),
                            Color(0xFFA855F7),
                            Color(0xFFD946EF)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                
                // Typography preview
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
