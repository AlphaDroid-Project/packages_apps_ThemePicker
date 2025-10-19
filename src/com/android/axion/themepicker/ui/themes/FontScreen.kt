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

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.axion.themepicker.ui.components.FastScrollBar
import com.android.axion.themepicker.ui.expressive.ExpressiveDialog
import com.android.axion.themepicker.ui.expressive.ExpressiveHeader
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.utils.math.lerp
import com.android.axion.themepicker.viewmodel.LayoutScreenViewModel
import com.android.axion.themepicker.viewmodel.MainScreenViewModel
import com.android.customization.model.ResourceConstants
import com.android.customization.model.theme.OverlayManagerCompat
import com.android.axion.themepicker.providers.CommonOverlayProvider
import com.android.axion.themepicker.data.model.FontOverlayOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun FontScreen(
    layoutScreenViewModel: LayoutScreenViewModel,
    mainScreenViewModel: MainScreenViewModel
) {
    val colors = LocalAxColorScheme.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    
    var fontOptions by remember { mutableStateOf<List<FontOverlayOption>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedIndex by remember { mutableStateOf(0) }
    var isApplying by remember { mutableStateOf(false) }
    var showFastScroller by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var hasScrolledToActive by remember { mutableStateOf(false) }
    val currentSubtitle by remember(selectedIndex, fontOptions) {
        mutableStateOf(fontOptions.getOrNull(selectedIndex)?.label ?: "")
    }
    
    val overlayProvider = remember {
        CommonOverlayProvider(
            context,
            OverlayManagerCompat(context),
            ResourceConstants.OVERLAY_CATEGORY_FONT
        )
    }
    
    LaunchedEffect(Unit) {
        val loadedOptions = overlayProvider.loadFontOptions()
        fontOptions = listOf(loadedOptions.first()) + loadedOptions.drop(1).sortedBy { it.label }
        
        val activeIndex = fontOptions.indexOfFirst { it.isActive }
        if (activeIndex >= 0) {
            selectedIndex = activeIndex
        }
        isLoading = false
    }
    
    val pagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { fontOptions.size }
    )
    
    LaunchedEffect(pagerState.currentPage) {
        selectedIndex = pagerState.currentPage
    }
    
    LaunchedEffect(fontOptions, hasScrolledToActive) {
        if (fontOptions.isNotEmpty() && !hasScrolledToActive) {
            delay(400)
            val activeIndex = fontOptions.indexOfFirst { it.isActive }
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
        title = "Reset to Default Font?",
        message = "This will restore the system default font and remove any custom font overlay.",
        confirmText = "Reset",
        onConfirm = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            scope.launch {
                isApplying = true
                showResetDialog = false
                val defaultOption = fontOptions.firstOrNull()
                if (defaultOption != null) {
                    val success = overlayProvider.applyOverlay(defaultOption)
                    if (success) {
                        fontOptions = fontOptions.mapIndexed { index, option ->
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
                title = "Font",
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
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 8.dp),
                        color = colors.surfaceContainerLowest,
                        shape = RoundedCornerShape(40.dp),
                        border = BorderStroke(
                            2.dp,
                            colors.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 36.dp, vertical = 44.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (fontOptions.isNotEmpty() && selectedIndex < fontOptions.size) {
                                FontPreview(
                                    option = fontOptions[selectedIndex],
                                    key = selectedIndex
                                )
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
                            contentPadding = PaddingValues(horizontal = 64.dp),
                            pageSpacing = 16.dp
                        ) { page ->
                            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            val scale = lerp(0.82f, 1f, 1f - abs(pageOffset).coerceIn(0f, 1f))
                            val alpha = lerp(0.4f, 1f, 1f - abs(pageOffset).coerceIn(0f, 1f))
                            
                            Box(
                                modifier = Modifier
                                    .scale(scale)
                                    .alpha(alpha)
                            ) {
                                FontOptionCard(
                                    option = fontOptions[page],
                                    isSelected = page == selectedIndex,
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
                            Text(
                                text = if (fontOptions.isNotEmpty() && selectedIndex < fontOptions.size) {
                                    fontOptions[selectedIndex].label.firstOrNull()?.uppercase() ?: "A"
                                } else "A",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(20.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.height(10.dp)
                        ) {
                            repeat(minOf(fontOptions.size, 5)) { index ->
                                val indicatorIndex = when {
                                    fontOptions.size <= 5 -> index
                                    selectedIndex < 2 -> index
                                    selectedIndex > fontOptions.size - 3 -> fontOptions.size - 5 + index
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
                                text = "${selectedIndex + 1}/${fontOptions.size}",
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
                                val success = overlayProvider.applyOverlay(fontOptions[selectedIndex])
                                if (success) {
                                    fontOptions = fontOptions.mapIndexed { index, option ->
                                        option.copy(isActive = index == selectedIndex)
                                    }
                                }
                                isApplying = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp),
                        enabled = !isApplying && selectedIndex < fontOptions.size && !fontOptions[selectedIndex].isActive,
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
                                if (fontOptions.getOrNull(selectedIndex)?.isActive == true) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                Text(
                                    text = if (fontOptions.getOrNull(selectedIndex)?.isActive == true) 
                                        "Applied" 
                                    else 
                                        "Apply Font",
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
            visible = showFastScroller && fontOptions.isNotEmpty(),
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
                items = fontOptions,
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
private fun FontPreview(option: FontOverlayOption, key: Int) {
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Fear is the biggest enemy of innovation.",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily(option.bodyFont),
                        lineHeight = 40.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 36.dp)
                )
                
                HorizontalDivider(
                    modifier = Modifier
                        .width(120.dp)
                        .padding(bottom = 36.dp),
                    color = colors.onSurfaceVariant.copy(alpha = 0.3f),
                    thickness = 3.dp
                )
                
                Text(
                    text = "1234567890!@#%&*()_+-=",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily(option.bodyFont)
                    ),
                    textAlign = TextAlign.Center,
                    color = colors.onSurfaceVariant,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FontOptionCard(
    option: FontOverlayOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAxColorScheme.current
    
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxSize(),
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
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Aa",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily(option.bodyFont),
                        fontSize = 52.sp
                    ),
                    fontWeight = FontWeight.Medium,
                    color = if (option.isActive) {
                        colors.onPrimaryContainer
                    } else {
                        colors.onSurface
                    }
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (option.isActive) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (option.isActive) {
                        colors.onPrimaryContainer
                    } else {
                        colors.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                )
                
                if (option.isActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.primary.copy(alpha = 0.8f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                modifier = Modifier.size(15.dp),
                                tint = colors.textPrimaryInverse
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimaryInverse
                            )
                        }
                    }
                }
            }
        }
    }
}
