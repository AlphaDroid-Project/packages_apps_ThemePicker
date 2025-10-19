package com.android.axion.themepicker.data.model

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.android.axion.themepicker.data.model.WallpaperSettings
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
                glass = prefs[KEY_GLASS] ?: false
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
