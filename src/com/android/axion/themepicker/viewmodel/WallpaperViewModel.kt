package com.android.axion.themepicker.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.axion.themepicker.data.model.WallpaperPrefs
import com.android.axion.themepicker.data.model.WallpaperSettings
import com.android.axion.themepicker.utils.wallpaper.applyWallpaper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*
import java.io.ByteArrayOutputStream

private val TAG = "WallpaperViewModel"

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
                val displayMetrics = context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val screenHeight = displayMetrics.heightPixels

                val bmp = decodeSampledBitmapFromUri(
                    context = context,
                    uri = uri,
                    reqWidth = screenWidth,
                    reqHeight = screenHeight
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

    private fun decodeSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            val (width, height) = options.outWidth to options.outHeight
            if (width <= 0 || height <= 0) return null

            options.inSampleSize = calculateSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            val decoded = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            val baos = ByteArrayOutputStream()
            decoded.compress(Bitmap.CompressFormat.PNG, 100, baos)
            decoded.recycle()

            val bytes = baos.toByteArray()
            baos.close()

            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory decoding bitmap", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode bitmap", e)
            null
        }
    }

    private fun calculateSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (srcWidth, srcHeight) = options.outWidth to options.outHeight
        var inSampleSize = 1

        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            var halfHeight = srcHeight / 2
            var halfWidth = srcWidth / 2

            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        val bytesPerPixel = 4
        val maxHeap = Runtime.getRuntime().maxMemory() / 4
        while ((srcWidth * srcHeight * bytesPerPixel / inSampleSize.toDouble().pow(2)) > maxHeap) {
            inSampleSize *= 2
        }

        return inSampleSize
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
                Log.e(TAG, "Failed to apply wallpaper", e)
            } finally {
                withContext(Dispatchers.Main) {
                    onFinished()
                }
            }
        }
    }
}
