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
package com.android.axion.themepicker.ui.gallery

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.axion.themepicker.data.model.GalleryState
import com.android.axion.themepicker.data.model.WallpaperCategory
import com.android.axion.themepicker.data.model.WallpaperInfo
import com.android.axion.themepicker.ui.components.ScreenTransition
import com.android.axion.themepicker.ui.components.ThumbnailCard
import com.android.axion.themepicker.ui.expressive.ExpressiveHeader
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.utils.wallpaper.loadAllCategories
import com.android.axion.themepicker.utils.wallpaper.rememberBitmap
import com.android.axion.themepicker.viewmodel.MainScreenViewModel
import com.android.axion.themepicker.viewmodel.WallpaperGalleryViewModel

private val ThumbnailBitmapSize = 90.dp
private val ThumbnailSize = 120.dp
private val ThumbnailPadding = 4.dp
private val ThumbnailPaddingVertical = 8.dp

@Composable
fun WallpaperGalleryScreen(
    galleryViewModel: WallpaperGalleryViewModel = viewModel(),
    mainScreenViewModel: MainScreenViewModel = viewModel(),
    onSelectPhoto: () -> Unit
) {
    val onWallpaperSelected = mainScreenViewModel::onWallpaperSelectedFromGallery

    val context = LocalContext.current
    val colors = LocalAxColorScheme.current

    val galleryState by galleryViewModel.currentGalleryState.collectAsState()
    val isNavigatingBack by galleryViewModel.isNavigatingBack.collectAsState()

    var categories by remember { mutableStateOf<List<WallpaperCategory>>(emptyList()) }
    var latestWallpapers by remember { mutableStateOf<List<WallpaperInfo>>(emptyList()) }

    val currentState = galleryState
    val headerTitle by remember(currentState) {
        derivedStateOf {
            when (currentState) {
                is GalleryState.Overview -> "Wallpaper Gallery"
                is GalleryState.CategoryList -> "Wallpapers"
                is GalleryState.CategoryDetail -> currentState.category.title
            }
        }
    }

    LaunchedEffect(Unit) {
        val loadedCategories = loadAllCategories(context)
        categories = loadedCategories
        latestWallpapers = loadedCategories
            .flatMap { it.wallpapers }
            .asReversed()
            .take(16)
    }

    BackHandler(enabled = true) {
        galleryViewModel.goBack {
            mainScreenViewModel.resetToMain()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp)
        ) {
            ExpressiveHeader(
                title = headerTitle,
                onBackClick = { 
                    galleryViewModel.goBack {
                        mainScreenViewModel.resetToMain()
                    }
                }
            )
            
            val back = isNavigatingBack || galleryState is GalleryState.Overview

            ScreenTransition(
                targetState = galleryState,
                isNavigatingBack = back
            ) { state ->
                when (state) {
                    is GalleryState.Overview -> OverviewContent(
                        latestWallpapers = latestWallpapers,
                        onEditCurrent = mainScreenViewModel::onEditCurrent,
                        onSelectPhoto = onSelectPhoto,
                        onMoreClick = { galleryViewModel.navigateTo(GalleryState.CategoryList(categories)) },
                        onWallpaperSelected = onWallpaperSelected
                    )
                    is GalleryState.CategoryList -> CategoryListContent(
                        categories = state.categories,
                        onCategoryClick = { category ->
                            galleryViewModel.navigateTo(GalleryState.CategoryDetail(category))
                        }
                    )
                    is GalleryState.CategoryDetail -> CategoryDetailContent(
                        category = state.category,
                        onWallpaperSelected = onWallpaperSelected
                    )
                }
            }
        }
    }
}

private fun GalleryStateSaver() = Saver<GalleryState, Any>(
    save = { state ->
        when (state) {
            is GalleryState.Overview -> "overview"
            is GalleryState.CategoryList -> "categoryList"
            is GalleryState.CategoryDetail -> "categoryDetail:${state.category.id}"
        }
    },
    restore = { value ->
        when {
            value == "overview" -> GalleryState.Overview
            value == "categoryList" -> GalleryState.CategoryList(emptyList())
            value.toString().startsWith("categoryDetail:") -> {
                val id = value.toString().substringAfter("categoryDetail:")
                GalleryState.CategoryDetail(WallpaperCategory(id, id, emptyList()))
            }
            else -> GalleryState.Overview
        }
    }
)

@Composable
private fun OverviewContent(
    latestWallpapers: List<WallpaperInfo>,
    onEditCurrent: () -> Unit,
    onSelectPhoto: () -> Unit,
    onMoreClick: () -> Unit,
    onWallpaperSelected: (WallpaperInfo) -> Unit
) {
    val colors = LocalAxColorScheme.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        text = "Edit Current",
                        icon = Icons.Default.Edit,
                        onClick = onEditCurrent
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionCard(
                        text = "My Photos",
                        icon = Icons.Default.Photo,
                        onClick = onSelectPhoto
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Latest Wallpapers",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )

                Row(
                    modifier = Modifier.wrapContentSize().clickable { onMoreClick() },
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "See More",
                        fontSize = 18.sp,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
            }
        }

        item {
            GalleryGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 1000.dp),
                items = latestWallpapers.take(16),
                itemContent = { wallpaper -> 
                    WallpaperThumbnail(
                        wallpaper = wallpaper as WallpaperInfo, 
                        onClick = { onWallpaperSelected(wallpaper) }
                    )
                }
            )
        }
    }
}

@Composable
private fun CategoryListContent(
    categories: List<WallpaperCategory>,
    onCategoryClick: (WallpaperCategory) -> Unit
) {
    GalleryGrid(
        items = categories,
        itemContent = { category -> 
            CategoryCard(
                category = category as WallpaperCategory,
                onClick = { onCategoryClick(category) }
            )
        }
    )
}

@Composable
private fun CategoryDetailContent(
    category: WallpaperCategory,
    onWallpaperSelected: (WallpaperInfo) -> Unit
) {
    GalleryGrid(
        items = category.wallpapers,
        itemContent = { wallpaper -> 
            WallpaperThumbnail(
                wallpaper = wallpaper as WallpaperInfo, 
                onClick = { onWallpaperSelected(wallpaper) }
            )
        }
    )
}

@Composable
private fun GalleryGrid(
    items: List<Any>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: Any) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = ThumbnailSize),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ) {
        itemsIndexed(items) { index, item ->
            itemContent(item)
        }
    }
}

@Composable
private fun QuickActionCard(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = LocalAxColorScheme.current
    val shape = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(ThumbnailSize)
            .clickable { onClick() },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceContainerLowest
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = text,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(category: WallpaperCategory, onClick: () -> Unit) {
    val firstWallpaper = category.wallpapers.firstOrNull()
    ThumbnailCard(
        drawableRes = firstWallpaper?.drawableRes,
        contentDescription = category.title,
        size = ThumbnailSize,
        modifier = Modifier.padding(horizontal = ThumbnailPadding, vertical = ThumbnailPaddingVertical),
        onClick = onClick
    )
}

@Composable
private fun WallpaperThumbnail(wallpaper: WallpaperInfo, onClick: () -> Unit) {
    ThumbnailCard(
        drawableRes = wallpaper.drawableRes,
        contentDescription = wallpaper.title,
        size = ThumbnailSize,
        modifier = Modifier.padding(horizontal = ThumbnailPadding, vertical = ThumbnailPaddingVertical),
        onClick = onClick
    )
}
