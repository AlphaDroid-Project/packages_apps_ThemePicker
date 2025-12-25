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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.axion.themepicker.data.model.WallpaperInfo
import com.android.axion.themepicker.ui.theme.*
import com.android.axion.themepicker.utils.math.sdp
import com.android.axion.themepicker.utils.wallpaper.getCurrentWallpaperBitmap
import com.android.axion.themepicker.utils.wallpaper.rememberBitmap

@Composable
fun WallpaperSection(
    wallpapers: List<WallpaperInfo>,
    onWallpaperSelected: (WallpaperInfo) -> Unit,
    onEditCurrent: () -> Unit,
    onOpenGallery: () -> Unit,
    onSelectPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    
    val currentWallpaper = remember { getCurrentWallpaperBitmap(context, true) }
    
    if (layoutInfo.isDualPane) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(design.spacing.screenPaddingTablet),
            horizontalArrangement = Arrangement.spacedBy(design.spacing.large)
        ) {
            HeroCard(
                currentWallpaper = currentWallpaper,
                onEditCurrent = onEditCurrent,
                onOpenGallery = onOpenGallery,
                onSelectPhoto = onSelectPhoto,
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
            )
            
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(design.spacing.medium)
            ) {
                SectionHeader(
                    title = "Featured",
                    subtitle = "Curated wallpapers for you"
                )
                
                WallpaperGrid(
                    wallpapers = wallpapers,
                    onWallpaperSelected = onWallpaperSelected,
                    columns = 2
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(design.spacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(design.spacing.large)
        ) {
            item {
                HeroCard(
                    currentWallpaper = currentWallpaper,
                    onEditCurrent = onEditCurrent,
                    onOpenGallery = onOpenGallery,
                    onSelectPhoto = onSelectPhoto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.85f)
                )
            }
            
            item {
                SectionHeader(
                    title = "Explore",
                    subtitle = "Discover new wallpapers"
                )
            }
            
            item {
                WallpaperGrid(
                    wallpapers = wallpapers.take(9),
                    onWallpaperSelected = onWallpaperSelected,
                    columns = 3
                )
            }
        }
    }
}

@Composable
private fun HeroCard(
    currentWallpaper: Bitmap?,
    onEditCurrent: () -> Unit,
    onOpenGallery: () -> Unit,
    onSelectPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(design.shapes.cardCorner),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            currentWallpaper?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = 20.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(design.spacing.large),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colors.primary.copy(alpha = 0.9f),
                        contentColor = colors.onPrimary
                    ) {
                        Text(
                            text = "Your Wallpaper",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = "Make it yours",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        text = "Customize to match your style",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    currentWallpaper?.let { bmp ->
                        Card(
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(0.55f),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Current wallpaper",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                
                val configuration = LocalContext.current.resources.configuration
                val screenWidthDp = configuration.screenWidthDp
                val showLabels = screenWidthDp >= 360
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (showLabels) Arrangement.spacedBy(8.dp) else Arrangement.SpaceEvenly
                ) {
                    WallpaperActionChip(
                        title = "Walls",
                        icon = Icons.Outlined.Wallpaper,
                        onClick = onOpenGallery,
                        showLabel = showLabels,
                        modifier = if (showLabels) Modifier.weight(1f) else Modifier
                    )
                    WallpaperActionChip(
                        title = "Photos",
                        icon = Icons.Outlined.Photo,
                        onClick = onSelectPhoto,
                        showLabel = showLabels,
                        modifier = if (showLabels) Modifier.weight(1f) else Modifier
                    )
                    WallpaperActionChip(
                        title = "Edit",
                        icon = Icons.Outlined.Tune,
                        onClick = onEditCurrent,
                        showLabel = showLabels,
                        modifier = if (showLabels) Modifier.weight(1f) else Modifier
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperActionChip(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    showLabel: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "action_chip_scale"
    )
    
    Surface(
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
        shape = if (showLabel) RoundedCornerShape(20.dp) else CircleShape,
        color = Color.White.copy(alpha = 0.2f)
    ) {
        if (showLabel) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee()
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
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
            style = MaterialTheme.typography.titleLarge,
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
private fun WallpaperGrid(
    wallpapers: List<WallpaperInfo>,
    onWallpaperSelected: (WallpaperInfo) -> Unit,
    columns: Int,
    modifier: Modifier = Modifier
) {
    val design = LocalExpressiveDesign.current
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(design.spacing.small)
    ) {
        wallpapers.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(design.spacing.small)
            ) {
                row.forEach { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onWallpaperSelected(wallpaper) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WallpaperCard(
    wallpaper: WallpaperInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 400f
        ),
        label = "wallpaper_card_scale"
    )
    
    val bitmap = rememberBitmap(wallpaper.drawableRes, 150.sdp, 220.sdp)
    
    Card(
        modifier = modifier
            .aspectRatio(0.65f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = wallpaper.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f)
                            )
                        )
                    )
            )
        }
    }
}
