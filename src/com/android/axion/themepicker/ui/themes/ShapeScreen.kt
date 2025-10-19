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

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.axion.themepicker.data.model.IconShapeOption
import com.android.axion.themepicker.data.model.OverlayOption
import com.android.axion.themepicker.providers.CommonOverlayProvider
import com.android.axion.themepicker.ui.components.FastScrollBar
import com.android.axion.themepicker.ui.expressive.ExpressiveDialog
import com.android.axion.themepicker.ui.expressive.ExpressiveHeader
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.utils.math.lerp
import com.android.axion.themepicker.utils.wallpaper.getCurrentWallpaperBitmap
import com.android.axion.themepicker.viewmodel.LayoutScreenViewModel
import com.android.axion.themepicker.viewmodel.MainScreenViewModel
import com.android.customization.model.ResourceConstants
import com.android.customization.model.theme.OverlayManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Composable
fun ShapeScreen(
    layoutScreenViewModel: LayoutScreenViewModel,
    mainScreenViewModel: MainScreenViewModel
) {
    val colors = LocalAxColorScheme.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    
    var shapeOptions by remember { mutableStateOf<List<IconShapeOption>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedIndex by remember { mutableStateOf(0) }
    var isApplying by remember { mutableStateOf(false) }
    var showFastScroller by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var hasScrolledToActive by remember { mutableStateOf(false) }
    
    val currentSubtitle by remember(selectedIndex, shapeOptions) {
        mutableStateOf(shapeOptions.getOrNull(selectedIndex)?.label ?: "")
    }
    
    val overlayProvider = remember {
        CommonOverlayProvider(
            context,
            OverlayManagerCompat(context),
            ResourceConstants.OVERLAY_CATEGORY_SHAPE
        )
    }
    
    LaunchedEffect(Unit) {
        val loadedOptions = overlayProvider.loadIconShapeOptions()
        shapeOptions = listOf(loadedOptions.first()) + loadedOptions.drop(1).sortedBy { it.label }
        val activeIndex = shapeOptions.indexOfFirst { it.isActive }
        if (activeIndex >= 0) {
            selectedIndex = activeIndex
        }
        isLoading = false
    }
    
    val pagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { shapeOptions.size }
    )
    
    LaunchedEffect(pagerState.currentPage) {
        selectedIndex = pagerState.currentPage
    }
    
    LaunchedEffect(shapeOptions, hasScrolledToActive) {
        if (shapeOptions.isNotEmpty() && !hasScrolledToActive) {
            delay(400)
            val activeIndex = shapeOptions.indexOfFirst { it.isActive }
            if (activeIndex >= 0) {
                pagerState.scrollToPage(activeIndex)
            }
            hasScrolledToActive = true
        }
    }
    
    LaunchedEffect(showFastScroller) {
        if (showFastScroller) {
            delay(2000)
            showFastScroller = false
        }
    }
    
    ExpressiveDialog(
        showDialog = showResetDialog,
        onDismiss = { showResetDialog = false },
        title = "Reset to Default Icon Shapes?",
        message = "This will restore the system default icon shapes and remove any custom shapes overlay.",
        confirmText = "Reset",
        onConfirm = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            scope.launch {
                isApplying = true
                showResetDialog = false
                val defaultOption = shapeOptions.firstOrNull()
                if (defaultOption != null) {
                    val androidSuccess = overlayProvider.applyOverlay(defaultOption)
                    if (androidSuccess) {
                        shapeOptions = shapeOptions.mapIndexed { index, option ->
                            option.copy(isActive = index == 0)
                        }
                        pagerState.animateScrollToPage(0)
                    }
                }
                isApplying = false
            }
        }
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ExpressiveHeader(
                title = "Icons Shapes",
                subtitle = currentSubtitle,
                onBackClick = { layoutScreenViewModel.goBackInLayout(mainScreenViewModel) },
                onActionClick = { showResetDialog = true }
            )
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        strokeWidth = 5.dp,
                        strokeCap = StrokeCap.Round
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val shape = RoundedCornerShape(40.dp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 8.dp),
                        color = colors.surfaceContainerLowest, 
                        shape = shape,
                        border = BorderStroke(
                            2.dp,
                            colors.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val wallpaper = getCurrentWallpaperBitmap(context, true)
                            if (wallpaper != null) {
                                Image(
                                    bitmap = wallpaper.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(shape), 
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 36.dp, vertical = 44.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (shapeOptions.isNotEmpty() && selectedIndex < shapeOptions.size) {
                                    ShapesPreview(
                                        option = shapeOptions[selectedIndex],
                                        key = selectedIndex
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp),
                            contentPadding = PaddingValues(horizontal = 80.dp),
                            pageSpacing = 20.dp
                        ) { page ->
                            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            val scale = lerp(0.85f, 1f, 1f - abs(pageOffset).coerceIn(0f, 1f))
                            
                            Box(
                                modifier = Modifier.scale(scale)
                            ) {
                                IconShapesCard(
                                    option = shapeOptions[page],
                                    isSelected = shapeOptions[page].isActive,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        scope.launch {
                                            pagerState.animateScrollToPage(page)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = {
                                showFastScroller = !showFastScroller
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(48.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            if (shapeOptions.isNotEmpty() && selectedIndex < shapeOptions.size) {
                                val option = shapeOptions[selectedIndex]
                                if (option.shapedAppIcons.isNotEmpty()) {
                                    Image(
                                        bitmap = option.shapedAppIcons.first().toBitmap(32, 32).asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        colorFilter = ColorFilter.tint(colors.onSurface)
                                    )
                                } else {
                                    Text(
                                        text = option.label.firstOrNull()?.uppercase() ?: "A",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(20.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.height(10.dp)
                        ) {
                            repeat(minOf(shapeOptions.size, 5)) { index ->
                                val indicatorIndex = when {
                                    shapeOptions.size <= 5 -> index
                                    selectedIndex < 2 -> index
                                    selectedIndex > shapeOptions.size - 3 -> shapeOptions.size - 5 + index
                                    else -> selectedIndex - 2 + index
                                }
                                
                                val isSelected = indicatorIndex == selectedIndex
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(
                                            width = if (isSelected) 24.dp else 10.dp,
                                            height = 10.dp
                                        )
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(
                                            if (isSelected)
                                                colors.primary
                                            else
                                                colors.surfaceContainerHighest
                                        )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(20.dp))
                        
                        FilledTonalButton(
                            onClick = {
                                showFastScroller = !showFastScroller
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "${selectedIndex + 1}/${shapeOptions.size}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                isApplying = true
                                val selectedOption = shapeOptions[selectedIndex]
                                val androidSuccess = overlayProvider.applyOverlay(selectedOption)
                                if (androidSuccess) {
                                    shapeOptions = shapeOptions.mapIndexed { index, option ->
                                        option.copy(isActive = index == selectedIndex)
                                    }
                                }
                                isApplying = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp),
                        enabled = !isApplying && selectedIndex < shapeOptions.size && !shapeOptions[selectedIndex].isActive,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                            disabledContainerColor = colors.surfaceContainerHighest,
                            disabledContentColor = colors.onSurfaceVariant
                        )
                    ) {
                        if (isApplying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = colors.onPrimary,
                                strokeWidth = 4.dp,
                                strokeCap = StrokeCap.Round
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (shapeOptions.getOrNull(selectedIndex)?.isActive == true) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                Text(
                                    text = if (shapeOptions.getOrNull(selectedIndex)?.isActive == true) 
                                        "Applied" 
                                    else 
                                        "Apply Icon Shapes",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
        
        AnimatedVisibility(
            visible = showFastScroller && shapeOptions.isNotEmpty(),
            enter = fadeIn(tween(200)) + slideInHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                initialOffsetX = { it }
            ),
            exit = fadeOut(tween(150)) + slideOutHorizontally(
                animationSpec = tween(250, easing = FastOutSlowInEasing),
                targetOffsetX = { it }
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            FastScrollBar(
                items = shapeOptions,
                currentIndex = selectedIndex,
                onItemSelected = { index -> 
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                labelExtractor = { it.label }
            )
        }
    }
}

@Composable
private fun ShapesPreview(
    option: IconShapeOption,
    key: Int
) {
    val colors = LocalAxColorScheme.current

    key(key) {
        var visible by remember { mutableStateOf(false) }
        
        LaunchedEffect(Unit) {
            delay(50)
            visible = true
        }
    
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                animationSpec = tween(400, easing = FastOutSlowInEasing),
                initialOffsetY = { -30 }
            ),
            exit = slideOutVertically(
                animationSpec = tween(200),
                targetOffsetY = { 30 }
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (option.shapedAppIcons.isNotEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            option.shapedAppIcons.take(3).forEach { drawable ->
                                Image(
                                    bitmap = drawable.toBitmap(120, 120).asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(72.dp)
                                )
                            }
                        }
                        
                        if (option.shapedAppIcons.size >= 6) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                option.shapedAppIcons.drop(3).take(3).forEach { drawable ->
                                    Image(
                                        bitmap = drawable.toBitmap(120, 120).asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.size(72.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun IconShapesCard(
    option: IconShapeOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAxColorScheme.current
    val contentColor = if (isSelected) {
            colors.textPrimaryInverse 
        } else colors.onSurface
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        color = if (option.isActive) {
            colors.primary
        } else {
            colors.surfaceContainerLowest
        },
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(
            width = if (option.isActive) 0.dp else 2.dp,
            color = if (option.isActive) {
                Color.Transparent
            } else {
                colors.outlineVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (option.shapedAppIcons.isNotEmpty()) {
                    Image(
                        bitmap = option.shapedAppIcons.first().toBitmap(120, 120).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        colorFilter = ColorFilter.tint(contentColor)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (option.isActive) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        modifier = Modifier.size(20.dp),
                        tint = colors.textPrimaryInverse
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
            }
        }
    }
}
