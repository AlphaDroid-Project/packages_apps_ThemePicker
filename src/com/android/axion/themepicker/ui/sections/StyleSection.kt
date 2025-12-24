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

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.axion.themepicker.ui.theme.*

@Composable
fun StyleSection(
    onOpenColors: () -> Unit,
    onOpenAppGrid: () -> Unit,
    onOpenFonts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                if (layoutInfo.isTablet) design.spacing.screenPaddingTablet
                else design.spacing.screenPadding
            ),
        verticalArrangement = Arrangement.spacedBy(design.spacing.medium)
    ) {
        StyleHeader()
        
        if (layoutInfo.isDualPane) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(design.spacing.medium)
            ) {
                StyleCard(
                    title = "Colors",
                    subtitle = "Wallpaper colors & themes",
                    description = "Create a cohesive look",
                    icon = Icons.Filled.Palette,
                    gradientColors = listOf(
                        Color(0xFF8B5CF6),
                        Color(0xFFA855F7),
                        Color(0xFFD946EF)
                    ),
                    onClick = onOpenColors,
                    modifier = Modifier.weight(1f)
                )
                
                StyleCard(
                    title = "App Grid",
                    subtitle = "Home screen layout",
                    description = "Customize grid size",
                    icon = Icons.Filled.GridView,
                    gradientColors = listOf(
                        Color(0xFF10B981),
                        Color(0xFF06B6D4),
                        Color(0xFF3B82F6)
                    ),
                    onClick = onOpenAppGrid,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(design.spacing.medium)
            ) {
                StyleCard(
                    title = "Fonts",
                    subtitle = "System typography",
                    description = "Choose your style",
                    icon = Icons.Filled.TextFormat,
                    gradientColors = listOf(
                        Color(0xFFF59E0B),
                        Color(0xFFF472B6),
                        Color(0xFFEF4444)
                    ),
                    onClick = onOpenFonts,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.weight(1f))
            }
        } else {
            StyleCard(
                title = "Colors",
                subtitle = "Wallpaper colors & themes",
                description = "Create a cohesive look",
                icon = Icons.Filled.Palette,
                gradientColors = listOf(
                    Color(0xFF8B5CF6),
                    Color(0xFFA855F7),
                    Color(0xFFD946EF)
                ),
                onClick = onOpenColors,
                modifier = Modifier.fillMaxWidth()
            )
            
            StyleCard(
                title = "App Grid",
                subtitle = "Home screen layout",
                description = "Customize grid size",
                icon = Icons.Filled.GridView,
                gradientColors = listOf(
                    Color(0xFF10B981),
                    Color(0xFF06B6D4),
                    Color(0xFF3B82F6)
                ),
                onClick = onOpenAppGrid,
                modifier = Modifier.fillMaxWidth()
            )
            
            StyleCard(
                title = "Fonts",
                subtitle = "System typography",
                description = "Choose your style",
                icon = Icons.Filled.TextFormat,
                gradientColors = listOf(
                    Color(0xFFF59E0B),
                    Color(0xFFF472B6),
                    Color(0xFFEF4444)
                ),
                onClick = onOpenFonts,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        ProTip(
            text = "Colors are automatically extracted from your wallpaper. Enable wallpaper colors for a cohesive look."
        )
    }
}

@Composable
private fun StyleHeader(
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "header_gradient")
    
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            colors.primary,
                            colors.tertiary,
                            colors.primary
                        ),
                        startX = gradientOffset
                    )
                )
        )
        
        Text(
            text = "Personalize",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        
        Text(
            text = "Make your device uniquely yours",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun StyleCard(
    title: String,
    subtitle: String,
    description: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val design = LocalExpressiveDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "style_card_scale"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isPressed) -0.5f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "style_card_rotation"
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
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
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
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureHighlights(
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(design.spacing.small)
    ) {
        Text(
            text = "Highlights",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(design.spacing.small)
        ) {
            FeatureHighlightChip(
                text = "Dynamic Color",
                icon = Icons.Outlined.AutoAwesome,
                modifier = Modifier.weight(1f)
            )
            FeatureHighlightChip(
                text = "Material You",
                icon = Icons.Outlined.Palette,
                modifier = Modifier.weight(1f)
            )
            FeatureHighlightChip(
                text = "Adaptive",
                icon = Icons.Outlined.Tune,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FeatureHighlightChip(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(design.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primaryContainer
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(20.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
        }
    }
}

@Composable
private fun ProTip(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val design = LocalExpressiveDesign.current
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.primaryContainer.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(design.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primary
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(16.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pro Tip",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface
                )
            }
        }
    }
}
