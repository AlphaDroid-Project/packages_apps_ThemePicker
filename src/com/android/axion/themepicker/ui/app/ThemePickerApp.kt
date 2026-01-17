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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.axion.themepicker.R
import com.android.axion.themepicker.data.model.WallpaperInfo
import com.android.axion.themepicker.data.model.NavigationDestination
import com.android.axion.themepicker.data.model.Screen
import com.android.axion.themepicker.data.model.Screen.EntryPoint
import com.android.axion.themepicker.data.model.WallpaperSettings
import com.android.axion.themepicker.data.model.ZoomProperties
import com.android.axion.themepicker.ui.components.ExpressiveScaffold
import com.android.axion.themepicker.ui.components.ScreenTransition
import com.android.axion.themepicker.ui.colors.ColorsSettingsScreen
import com.android.axion.themepicker.ui.themes.AppGridSettingsScreen
import com.android.axion.themepicker.ui.gallery.WallpaperGalleryScreen
import com.android.axion.themepicker.ui.lockscreen.LockscreenPreview
import com.android.axion.themepicker.ui.mainscreen.rememberPhotoPicker
import com.android.axion.themepicker.ui.mainscreen.WallpaperApplyScreen
import com.android.axion.themepicker.ui.preview.EditCurrentWallpaperScreen
import com.android.axion.themepicker.ui.preview.WallpaperPreviewScreen
import com.android.axion.themepicker.ui.sections.WallpaperSection
import com.android.axion.themepicker.ui.sections.StyleSection
import com.android.axion.themepicker.ui.sections.LockscreenSection
import com.android.axion.themepicker.ui.theme.*
import com.android.axion.themepicker.ui.themes.FontScreen

import com.android.axion.themepicker.viewmodel.MainScreenViewModel
import com.android.axion.themepicker.viewmodel.WallpaperGalleryViewModel
import com.android.axion.themepicker.viewmodel.WallpaperViewModel
import kotlin.coroutines.*
import kotlinx.coroutines.*
import kotlin.math.*

private const val TAG = "ThemePickerApp"

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
    
    var currentDestinationIndex by rememberSaveable { mutableStateOf(0) }
    
    val currentDestination = NavigationDestination.destinations.getOrElse(currentDestinationIndex) {
        NavigationDestination.Wallpaper
    }

    LaunchedEffect(Unit) {
        mainScreenViewModel.initialize(context)
        galleryViewModel.initialize()
    }
    
    LaunchedEffect(currentScreen) {
        if (currentScreen is Screen.Main) {
            wallpaperViewModel.resetSettings()
        }
    }
    
    val isDetailScreen = currentScreen !is Screen.Main
    
    if (isDetailScreen) {
        val isBackPressed = isNavigatingBack || currentScreen is Screen.Main
        
        ScreenTransition(
            targetState = currentScreen,
            isNavigatingBack = isBackPressed
        ) { screen ->
            DetailScreenContent(
                screen = screen,
                settings = settings,
                wallpaperViewModel = wallpaperViewModel,
                mainScreenViewModel = mainScreenViewModel,
                galleryViewModel = galleryViewModel,
                wallpapers = wallpapers
            )
        }
    } else {
        MainNavigationScaffold(
            currentDestination = currentDestination,
            onDestinationSelected = { dest -> 
                val index = NavigationDestination.destinations.indexOf(dest)
                if (index >= 0) currentDestinationIndex = index
            },
            wallpapers = wallpapers,
            mainScreenViewModel = mainScreenViewModel,
            wallpaperViewModel = wallpaperViewModel
        )
    }
}

@Composable
private fun MainNavigationScaffold(
    currentDestination: NavigationDestination,
    onDestinationSelected: (NavigationDestination) -> Unit,
    wallpapers: List<WallpaperInfo>,
    mainScreenViewModel: MainScreenViewModel,
    wallpaperViewModel: WallpaperViewModel
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val wallpaperPhotoTitle = stringResource(R.string.wallpaper_photo)
    
    val photoPickerLauncher = rememberPhotoPicker(context, wallpaperViewModel) { bitmap ->
        bitmap?.let {
            val customWallpaper = WallpaperInfo(
                id = "user_photo_${System.currentTimeMillis()}",
                title = wallpaperPhotoTitle,
                drawableRes = -1
            )
            mainScreenViewModel.onUserUpload(customWallpaper, it)
        }
    }
    
    BackHandler {
        activity?.finish()
    }
    
    ExpressiveScaffold(
        currentDestination = currentDestination,
        onDestinationSelected = onDestinationSelected
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + 
                    slideInHorizontally(
                        initialOffsetX = { if (initialState.route < targetState.route) it / 4 else -it / 4 },
                        animationSpec = tween(300)
                    ) togetherWith
                    fadeOut(animationSpec = tween(200)) +
                    slideOutHorizontally(
                        targetOffsetX = { if (initialState.route < targetState.route) -it / 4 else it / 4 },
                        animationSpec = tween(200)
                    )
                },
                label = "section_transition"
            ) { destination ->
                when (destination) {
                    NavigationDestination.Wallpaper -> {
                        WallpaperSection(
                            wallpapers = wallpapers,
                            onWallpaperSelected = mainScreenViewModel::onWallpaperSelected,
                            onEditCurrent = mainScreenViewModel::onEditCurrent,
                            onOpenGallery = mainScreenViewModel::onOpenGallery,
                            onSelectPhoto = { photoPickerLauncher.launch("image/*") }
                        )
                    }
                    NavigationDestination.Style -> {
                        StyleSection(
                            onOpenColors = mainScreenViewModel::onOpenColorsSettings,
                            onOpenAppGrid = mainScreenViewModel::onOpenAppGrid,
                            onOpenFonts = mainScreenViewModel::onOpenFonts
                        )
                    }
                    NavigationDestination.Lockscreen -> {
                        LockscreenSection(
                            onOpenFullPreview = { entryPoint ->
                                mainScreenViewModel.onOpenLockscreenPreview(entryPoint = entryPoint)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailScreenContent(
    screen: Screen,
    settings: WallpaperSettings,
    wallpaperViewModel: WallpaperViewModel,
    mainScreenViewModel: MainScreenViewModel,
    galleryViewModel: WallpaperGalleryViewModel,
    wallpapers: List<WallpaperInfo>
) {
    val context = LocalContext.current
    
    when (screen) {
        is Screen.Main -> {
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
            val wallpaperPhotoTitle = stringResource(R.string.wallpaper_photo)
            val photoPickerLauncher = rememberPhotoPicker(context, wallpaperViewModel) { bitmap ->
                bitmap?.let {
                    val customWallpaper = WallpaperInfo(
                        id = "user_photo_${System.currentTimeMillis()}",
                        title = wallpaperPhotoTitle,
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
        
        is Screen.AppGrid -> {
            BackHandler { mainScreenViewModel.goBack() }
            AppGridSettingsScreen(mainScreenViewModel = mainScreenViewModel)
        }
        
        is Screen.Fonts -> {
            BackHandler { mainScreenViewModel.goBack() }
            FontScreen(mainScreenViewModel = mainScreenViewModel)
        }
        
        is Screen.Lockscreen -> {
            BackHandler { mainScreenViewModel.goBack() }
            LockscreenPreview(
                isPreview = false,
                wallpaperBitmap = com.android.axion.themepicker.utils.wallpaper.getCurrentWallpaperBitmap(context, false),
                modifier = Modifier.fillMaxSize(),
                entryPoint = screen.entryPoint
            )
        }
    }
}
