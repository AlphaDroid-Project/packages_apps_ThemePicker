package com.android.axion.themepicker.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.axion.themepicker.data.model.EffectConfig
import com.android.axion.themepicker.data.model.WallpaperPrefs
import com.android.axion.themepicker.data.model.WallpaperSettings
import com.android.axion.themepicker.utils.wallpaper.BitmapProcessor
import com.android.axion.themepicker.utils.wallpaper.applyWallpaper
import kotlin.coroutines.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    val wallpaperSettings: StateFlow<WallpaperSettings> =
        WallpaperPrefs.getSettings(context)
            .stateIn(viewModelScope, SharingStarted.Eagerly, WallpaperSettings())

    fun updateSettings(newSettings: WallpaperSettings) {
        viewModelScope.launch {
            WallpaperPrefs.saveSettings(context, newSettings)
        }
    }

    fun resetSettings() {
        updateSettings(WallpaperSettings())
    }
    
    fun handlePickedUri(context: Context, uri: Uri, onImageDecoded: (Bitmap?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                withContext(Dispatchers.Main) { onImageDecoded(bmp) }
            } catch (e: Exception) {
                Log.e("WallpaperViewModel", "Error decoding image", e)
            }
        }
    }
    
    fun applyNewWallpaper(
        context: Context,
        lockscreenBitmap: Bitmap?,
        homescreenBitmap: Bitmap?,
        lockscreenSelected: Boolean,
        homescreenSelected: Boolean,
        onFinished: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                applyWallpaper(
                    context = context,
                    lockscreenBitmap = lockscreenBitmap,
                    homescreenBitmap = homescreenBitmap,
                    lockscreenSelected = lockscreenSelected,
                    homescreenSelected = homescreenSelected
                )
            } catch (e: Exception) {
                Log.e("WallpaperViewModel", "Failed to apply wallpaper", e)
            } finally {
                withContext(Dispatchers.Main) {
                    onFinished()
                }
            }
        }
    }
}
