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
package com.android.axion.themepicker.ui.sections

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.axion.themepicker.data.model.Screen.EntryPoint
import com.android.axion.themepicker.ui.lockscreen.LockscreenPreview
import com.android.axion.themepicker.ui.theme.*
import com.android.axion.themepicker.utils.wallpaper.getCurrentWallpaperBitmap

@Composable
fun LockscreenSection(
    onOpenFullPreview: (EntryPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    
    val lockWallpaper = remember { getCurrentWallpaperBitmap(context, false) }
    
    if (layoutInfo.isDualPane) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(design.spacing.screenPaddingTablet),
            horizontalArrangement = Arrangement.spacedBy(design.spacing.large)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(design.spacing.medium)
            ) {
                SectionHeader(
                    title = "Lock Screen",
                    subtitle = "Personalize your experience"
                )
                
                FeatureCard(
                    title = "Widgets",
                    subtitle = "Add information at a glance",
                    icon = Icons.Filled.Widgets,
                    accentColor = colors.primary,
                    onClick = { onOpenFullPreview(EntryPoint.WIDGETS) }
                )
                
                FeatureCard(
                    title = "Shortcuts",
                    subtitle = "Quick access to your favorites",
                    icon = Icons.Filled.TouchApp,
                    accentColor = colors.tertiary,
                    onClick = { onOpenFullPreview(EntryPoint.SHORTCUTS) }
                )
                
                FeatureCard(
                    title = "Clock Style",
                    subtitle = "Choose your time format",
                    icon = Icons.Filled.Schedule,
                    accentColor = colors.secondary,
                    onClick = { onOpenFullPreview(EntryPoint.DEFAULT) }
                )
            }
            
            LockPreview(
                wallpaperBitmap = lockWallpaper,
                onClick = { onOpenFullPreview(EntryPoint.DEFAULT) },
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(design.spacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(design.spacing.large)
        ) {
            item {
                LockPreview(
                    wallpaperBitmap = lockWallpaper,
                    onClick = { onOpenFullPreview(EntryPoint.DEFAULT) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.75f)
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(design.spacing.small)
                ) {
                    FeatureChip(
                        title = "Widgets",
                        icon = Icons.Outlined.Widgets,
                        accentColor = colors.primary,
                        onClick = { onOpenFullPreview(EntryPoint.WIDGETS) },
                        modifier = Modifier.weight(1f)
                    )
                    FeatureChip(
                        title = "Shortcuts",
                        icon = Icons.Outlined.TouchApp,
                        accentColor = colors.tertiary,
                        onClick = { onOpenFullPreview(EntryPoint.SHORTCUTS) },
                        modifier = Modifier.weight(1f)
                    )
                    FeatureChip(
                        title = "Clock",
                        icon = Icons.Outlined.Schedule,
                        accentColor = colors.secondary,
                        onClick = { onOpenFullPreview(EntryPoint.DEFAULT) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                TipCard(
                    text = "Tap the preview to see all customization options"
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun LockPreview(
    wallpaperBitmap: Bitmap?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "lock_preview_scale"
    )
    
    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(design.shapes.cardCorner),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            wallpaperBitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = 25.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight(0.85f)
                    .aspectRatio(0.5f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    LockscreenPreview(
                        isPreview = true,
                        wallpaperBitmap = wallpaperBitmap,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = design.spacing.medium),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.TouchApp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tap to customize",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val design = LocalExpressiveDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "feature_card_scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = LocalAxColorScheme.current.surfaceContainerLowest)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(design.spacing.large)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.05f))
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LocalAxColorScheme.current.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalAxColorScheme.current.onSurfaceVariant
                    )
                }
                
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureChip(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val colors = LocalAxColorScheme.current
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "feature_chip_scale"
    )
    
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 0.dp,
        color = colors.surfaceContainerLowest
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(24.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
            }
        }
    }
}

@Composable
private fun TipCard(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(design.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primaryContainer
            ) {
                Icon(
                    Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(18.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
