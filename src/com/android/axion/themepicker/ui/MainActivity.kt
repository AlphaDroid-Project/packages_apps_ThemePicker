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
package com.android.axion.themepicker.ui

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.android.axion.themepicker.data.model.WallpaperInfo
import com.android.axion.themepicker.ui.app.ThemePickerApp
import com.android.axion.themepicker.ui.theme.AxTheme
import com.android.axion.themepicker.viewmodel.MainScreenViewModel
import java.io.InputStream

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainScreenViewModel by viewModels()
    private var wallpaperShareIntent = false
    private var isLaunchExtra = false
    private var wallpaperChangedReceiver: BroadcastReceiver? = null
    private var listening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        isLaunchExtra = intent?.getBooleanExtra("is_launch_extra", false) == true

        handleWallpaperShareIntent(intent)

        if (wallpaperShareIntent && !isLaunchExtra) {
            val am = getSystemService(ActivityManager::class.java)
            am?.appTasks?.find { it.taskInfo.id == taskId }?.setExcludeFromRecents(true)
            startListening()
        }

        setContent {
            val context = LocalContext.current
            val isDarkTheme = isSystemInDarkTheme()
            val materialColors = if (isDarkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)

            MaterialTheme(colorScheme = materialColors) {
                AxTheme {
                    Surface(
                        modifier = Modifier,
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ThemePickerApp()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        isLaunchExtra = intent.getBooleanExtra("is_launch_extra", false)
        handleWallpaperShareIntent(intent)
    }

    private fun handleWallpaperShareIntent(intent: Intent?) {
        if (intent == null || isLaunchExtra) return
        val action = intent.action
        val data: Uri? = intent.data
        if (data == null) return
        wallpaperShareIntent = true
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(data!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            bitmap?.let {
                val customWallpaper = WallpaperInfo(
                    id = "shared_wallpaper_${System.currentTimeMillis()}",
                    title = "Shared Wallpaper",
                    drawableRes = -1
                )
                mainViewModel.onUserUpload(customWallpaper, it)
            }
        } catch (e: Exception) {
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (wallpaperShareIntent && !hasFocus) {
            stopListening()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopListening()
    }
    
    private fun startListening() {
        if (listening) return
        wallpaperChangedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (Intent.ACTION_WALLPAPER_CHANGED == intent?.action) {
                    stopListening()
                }
            }
        }
        registerReceiver(wallpaperChangedReceiver, IntentFilter(Intent.ACTION_WALLPAPER_CHANGED))
        listening = true
    }

    private fun stopListening() {
        if (isLaunchExtra) return
        if (!listening) {
            wallpaperChangedReceiver?.let {
                unregisterReceiver(it)
            }
            wallpaperChangedReceiver = null
            listening = false
        }
        Process.killProcess(Process.myPid())
    }
}
