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
package com.android.axion.themepicker.ui.app

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.*
import android.widget.Toast
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.carousel.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.painter.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.axion.themepicker.data.model.WallpaperInfo
import com.android.axion.themepicker.data.model.Screen
import com.android.axion.themepicker.data.model.Screen.EntryPoint
import com.android.axion.themepicker.data.model.WallpaperSettings
import com.android.axion.themepicker.data.model.ZoomProperties
import com.android.axion.themepicker.ui.carousel.WallpaperCarouselCard
import com.android.axion.themepicker.ui.components.FooterIndicator
import com.android.axion.themepicker.ui.components.ScreenTransition
import com.android.axion.themepicker.ui.colors.ColorsSettingsScreen
import com.android.axion.themepicker.ui.expressive.ExpressiveHeader
import com.android.axion.themepicker.ui.gallery.WallpaperGalleryScreen
import com.android.axion.themepicker.ui.iconpack.IconPackScreen
import com.android.axion.themepicker.ui.lockscreen.LockscreenPreview
import com.android.axion.themepicker.ui.mainscreen.rememberPhotoPicker
import com.android.axion.themepicker.ui.mainscreen.ScreenOptions
import com.android.axion.themepicker.ui.mainscreen.WallpaperApplyScreen
import com.android.axion.themepicker.ui.preview.EditCurrentWallpaperScreen
import com.android.axion.themepicker.ui.preview.HomescreenPreview
import com.android.axion.themepicker.ui.preview.WallpaperPreviewScreen
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.ui.themes.LayoutMainScreen
import com.android.axion.themepicker.utils.math.scaleRatio
import com.android.axion.themepicker.utils.wallpaper.applyZoomToBitmap
import com.android.axion.themepicker.utils.wallpaper.getWallpaperDrawable
import com.android.axion.themepicker.utils.wallpaper.getCurrentWallpaperBitmap
import com.android.axion.themepicker.utils.wallpaper.BitmapProcessor
import com.android.axion.themepicker.viewmodel.MainScreenViewModel
import com.android.axion.themepicker.viewmodel.WallpaperGalleryViewModel
import com.android.axion.themepicker.viewmodel.WallpaperViewModel
import kotlin.coroutines.*
import kotlinx.coroutines.*
import kotlin.math.*

val WallpaperMiniPreviewsHeight = 320.dp
val WallpaperMiniPreviewsWidth = 162.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ThemePickerApp(
    wallpaperViewModel: WallpaperViewModel = viewModel(),
    mainScreenViewModel: MainScreenViewModel = viewModel(),
    galleryViewModel: WallpaperGalleryViewModel = viewModel()
) {
    val context = LocalContext.current
    val settings by wallpaperViewModel.wallpaperSettings.collectAsState()
    val currentScreen by mainScreenViewModel.currentScreen.collectAsState()
    val wallpapers by mainScreenViewModel.wallpapers.collectAsState()
    val isNavigatingBack by mainScreenViewModel.isNavigatingBack.collectAsState()

    LaunchedEffect(Unit) {
        mainScreenViewModel.initialize(context)
        galleryViewModel.initialize()
    }
    
    LaunchedEffect(currentScreen) {
        if (currentScreen is Screen.Main) wallpaperViewModel.resetSettings()
    }

    val isBackPressed = isNavigatingBack || currentScreen is Screen.Main

    ScreenTransition(
        targetState = currentScreen,
        isNavigatingBack = isBackPressed
    ) { screen ->
        when (screen) {
            is Screen.Main -> {
                val activity = LocalContext.current as? Activity
                BackHandler {
                    activity?.finish()
                }
                MainScreen(wallpapers = wallpapers)
            }

            is Screen.Preview -> {
                BackHandler { mainScreenViewModel.goBack() }
                WallpaperPreviewScreen(
                    wallpaper = screen.wallpaper,
                    settings = WallpaperSettings(),
                    bitmap = screen.bitmap
                )
            }

            is Screen.Apply -> {
                BackHandler { mainScreenViewModel.goBack() }
                WallpaperApplyScreen(
                    wallpaper = screen.wallpaper,
                    settings = settings.copy(zoomProperties = screen.zoomProperties),
                    bitmap = screen.bitmap
                )
            }

            is Screen.EditCurrent -> {
                BackHandler { mainScreenViewModel.goBack() }
                EditCurrentWallpaperScreen(
                    settings = settings
                )
            }

            is Screen.ColorsSettings -> {
                BackHandler { mainScreenViewModel.resetToMain() }
                ColorsSettingsScreen()
            }

            is Screen.WallpaperGallery -> {
                val photoPickerLauncher = rememberPhotoPicker(context, wallpaperViewModel) { bitmap ->
                    bitmap?.let {
                        val customWallpaper = WallpaperInfo(
                            id = "user_photo_${System.currentTimeMillis()}",
                            title = "Wallpaper Photo",
                            drawableRes = -1
                        )
                        mainScreenViewModel.onUserUpload(customWallpaper, it)
                    }
                }

                WallpaperGalleryScreen(
                    galleryViewModel = galleryViewModel,
                    onSelectPhoto = { photoPickerLauncher.launch("image/*") }
                )
            }
            
            is Screen.IconPack -> {
                BackHandler { mainScreenViewModel.goBack() }
                IconPackScreen()
            }
            
            is Screen.Layout -> {
                BackHandler { mainScreenViewModel.goBack() }
                LayoutMainScreen()
            }
            
            is Screen.Lockscreen -> {
                BackHandler { mainScreenViewModel.goBack() }
                LockscreenPreview(
                    isPreview = false,
                    wallpaperBitmap = getCurrentWallpaperBitmap(context, false),
                    modifier = Modifier.fillMaxSize(),
                    entryPoint = screen.entryPoint
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    wallpapers: List<WallpaperInfo>,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val onTabChange = mainScreenViewModel::onTabSelected
    val selectedTab = mainScreenViewModel.selectedTab.collectAsState().value
    val context = LocalContext.current
    val activity = context as? Activity

    val tabs = listOf("Lockscreen", "Home Screen")
    val previewPagerState = rememberPagerState(initialPage = selectedTab, pageCount = { tabs.size })
    val optionsPagerState = rememberPagerState(initialPage = selectedTab, pageCount = { tabs.size })
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val scale = context.scaleRatio
    val previewHeight = WallpaperMiniPreviewsHeight * scale
    val previewWidth = WallpaperMiniPreviewsWidth * scale

    LaunchedEffect(previewPagerState.currentPage, previewPagerState.currentPageOffsetFraction) {
        if (!optionsPagerState.isScrollInProgress && previewPagerState.isScrollInProgress) {
            optionsPagerState.scrollToPage(previewPagerState.currentPage, previewPagerState.currentPageOffsetFraction)
        }
    }

    LaunchedEffect(optionsPagerState.currentPage, optionsPagerState.currentPageOffsetFraction) {
        if (!previewPagerState.isScrollInProgress && optionsPagerState.isScrollInProgress) {
            previewPagerState.scrollToPage(optionsPagerState.currentPage, optionsPagerState.currentPageOffsetFraction)
        }
    }

    LaunchedEffect(previewPagerState.currentPage) {
        onTabChange(previewPagerState.currentPage)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAxColorScheme.current.surfaceContainer),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExpressiveHeader(
            title = tabs[previewPagerState.currentPage],
            onBackClick = { activity?.finish() }
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f),
            contentAlignment = Alignment.Center
        ) {
            val maxHeight = max(previewHeight, maxHeight)
            Box(
                modifier = Modifier
                    .height(maxHeight)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = previewPagerState,
                    pageSize = PageSize.Fixed(previewWidth + 12.dp),
                    contentPadding = PaddingValues(horizontal = (screenWidth.dp - previewWidth) / 2),
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val pageOffset = (previewPagerState.currentPage - page) + previewPagerState.currentPageOffsetFraction
                    val offsetAlpha = 1f - (abs(pageOffset) * 0.5f).coerceIn(0f, 0.5f)

                    Box(contentAlignment = Alignment.Center) {
                        PreviewsPage(
                            isHome = page == 1,
                            modifier = Modifier
                                .size(previewWidth, previewHeight)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        if (page != previewPagerState.currentPage) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = optionsPagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageOffset = (previewPagerState.currentPage - page) + previewPagerState.currentPageOffsetFraction
                val offsetAlpha = 1f - (abs(pageOffset) * 0.5f).coerceIn(0f, 0.5f)

                ScreenOptions(
                    isHome = page == 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = offsetAlpha
                            translationX = pageOffset * -50.dp.toPx()
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FooterIndicator(
            tabCount = tabs.size,
            currentPage = previewPagerState.currentPage
        )

        WallpaperCarouselCard(
            wallpapers = wallpapers
        )
    }
}

@Composable
fun PreviewsPage(
    isHome: Boolean,
    refreshKey: Any? = null,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        if (isHome) {
            HomescreenPreview(
                modifier = modifier.clickable { mainScreenViewModel.onEditCurrent() },
                refreshKey = refreshKey
            )
        } else {
            LockscreenPreview(
                isPreview = true,
                modifier = modifier.clickable { mainScreenViewModel.onOpenLockscreenPreview() },
            )
        }
    }
}
