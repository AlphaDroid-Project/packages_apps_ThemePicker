package com.android.axion.themepicker.viewmodel

import android.graphics.Bitmap
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.axion.themepicker.data.model.Screen
import com.android.axion.themepicker.data.model.Screen.EntryPoint
import com.android.axion.themepicker.data.model.WallpaperInfo
import com.android.axion.themepicker.data.model.ZoomProperties
import com.android.axion.themepicker.utils.wallpaper.loadWallpapers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.*

class MainScreenViewModel : ViewModel() {

    private val _wallpapers = MutableStateFlow<List<WallpaperInfo>>(emptyList())
    val wallpapers: StateFlow<List<WallpaperInfo>> = _wallpapers

    private val _selectedTab = MutableStateFlow(1)
    val selectedTab: StateFlow<Int> = _selectedTab

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Main)
    val currentScreen: StateFlow<Screen> = _currentScreen

    private val _screenStack = mutableListOf<Screen>()
    private var _galleryReturnScreen: Screen.WallpaperGallery? = null

    private val _isNavigatingBack = MutableStateFlow(false)
    val isNavigatingBack: StateFlow<Boolean> = _isNavigatingBack

    fun initialize(context: Context) {
        if (_wallpapers.value.isNotEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            val loaded = loadWallpapers(context)
            _wallpapers.value = loaded
            _isLoading.value = false
        }
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun navigateTo(screen: Screen) {
        _isNavigatingBack.value = false
        _screenStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun goBack() {
        _isNavigatingBack.value = true

        _galleryReturnScreen?.let {
            _currentScreen.value = it
            _galleryReturnScreen = null
            return
        }

        if (_screenStack.isNotEmpty()) {
            _currentScreen.value = _screenStack.removeAt(_screenStack.lastIndex)
        } else {
            _isNavigatingBack.value = false
            _currentScreen.value = Screen.Main
        }
    }

    fun resetToMain() {
        _isNavigatingBack.value = false
        _currentScreen.value = Screen.Main
        _screenStack.clear()
        _galleryReturnScreen = null
        Log.d("MainScreenViewModel", "reset to main!")
    }

    fun onWallpaperSelected(wallpaper: WallpaperInfo) {
        navigateTo(Screen.Preview(wallpaper))
    }

    fun onApplyConfirmed(wallpaper: WallpaperInfo, zoom: ZoomProperties = ZoomProperties(), bitmap: Bitmap? = null) {
        navigateTo(Screen.Apply(wallpaper, zoom, bitmap))
    }

    fun onEditCurrent() {
        navigateTo(Screen.EditCurrent)
    }

    fun onOpenColorsSettings() {
        navigateTo(Screen.ColorsSettings)
    }
    
    fun onOpenIconPack() {
        navigateTo(Screen.IconPack)
    }
    
    fun onOpenLayout() {
        navigateTo(Screen.Layout)
    }

    fun onWallpaperSelectedFromGallery(wallpaper: WallpaperInfo) {
        _isNavigatingBack.value = false
        _galleryReturnScreen = Screen.WallpaperGallery
        _currentScreen.value = Screen.Preview(wallpaper)
    }
    
    fun onUserUpload(wallpaper: WallpaperInfo, bitmap: Bitmap? = null) {
        _isNavigatingBack.value = false
        _galleryReturnScreen = Screen.WallpaperGallery
        _currentScreen.value = Screen.Preview(wallpaper, bitmap)
    }

    fun onOpenGallery() {
        navigateTo(Screen.WallpaperGallery)
    }
    
    fun onOpenLockscreenPreview(
        wallpaper: WallpaperInfo? = null,
        entryPoint: EntryPoint = EntryPoint.DEFAULT
    ) {
        navigateTo(Screen.Lockscreen(wallpaper, entryPoint))
        Log.d("MainScreenViewModel", "entryPoint=$entryPoint")
    }
}
