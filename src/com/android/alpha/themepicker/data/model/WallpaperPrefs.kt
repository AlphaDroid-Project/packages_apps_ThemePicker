/*
 * Copyright (C) 2025-2026 AxionOS
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

package com.android.alpha.themepicker.data.model

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.wallpaperDataStore by preferencesDataStore("wallpaper_settings")

object WallpaperPrefs {
    private val KEY_WALLPAPER_ID = intPreferencesKey("wallpaper_id")
    private val KEY_LOCKSCREEN = booleanPreferencesKey("lockscreen")
    private val KEY_HOMESCREEN = booleanPreferencesKey("homescreen")
    private val KEY_ATMOSPHERE = booleanPreferencesKey("atmosphere")
    private val KEY_GLASS = booleanPreferencesKey("glass")

    fun getSettings(context: Context): Flow<WallpaperSettings> =
        context.wallpaperDataStore.data.map { prefs ->
            WallpaperSettings(
                wallpaperId = prefs[KEY_WALLPAPER_ID] ?: -1,
                lockscreen = prefs[KEY_LOCKSCREEN] ?: true,
                homescreen = prefs[KEY_HOMESCREEN] ?: true,
                atmosphere = prefs[KEY_ATMOSPHERE] ?: false,
                glass = prefs[KEY_GLASS] ?: false,
            )
        }

    suspend fun saveSettings(context: Context, settings: WallpaperSettings) {
        context.wallpaperDataStore.edit { prefs ->
            prefs[KEY_WALLPAPER_ID] = settings.wallpaperId
            prefs[KEY_LOCKSCREEN] = settings.lockscreen
            prefs[KEY_HOMESCREEN] = settings.homescreen
            prefs[KEY_ATMOSPHERE] = settings.atmosphere
            prefs[KEY_GLASS] = settings.glass
        }
    }
}
