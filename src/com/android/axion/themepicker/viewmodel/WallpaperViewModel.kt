package com.android.axion.themepicker.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.android.axion.themepicker.data.model.WallpaperPrefs
import com.android.axion.themepicker.data.model.WallpaperSettings
import com.android.axion.themepicker.data.model.ZoomProperties
import com.android.axion.themepicker.utils.wallpaper.applyWallpaper
import com.android.axion.themepicker.utils.wallpaper.decodeSampledBitmapFromUri
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*
import java.io.ByteArrayOutputStream

private val TAG = "WallpaperViewModel"

class WallpaperViewModel(
    application: Application
) : AndroidViewModel(application) {

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
                val bmp = decodeSampledBitmapFromUri(
                    context = context,
                    uri = uri
                )

                withContext(Dispatchers.Main) {
                    onImageDecoded(bmp)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error decoding image", e)
                withContext(Dispatchers.Main) { onImageDecoded(null) }
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
            } finally {
                withContext(Dispatchers.Main) { onFinished() }
            }
        }
    }
}
