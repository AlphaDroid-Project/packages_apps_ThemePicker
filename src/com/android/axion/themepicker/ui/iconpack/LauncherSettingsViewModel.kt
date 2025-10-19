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
package com.android.axion.themepicker.ui.iconpack

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private const val TAG = "LauncherSettingsVM"

data class IconPackItem(
    val packageName: String,
    val label: String, 
    val icon: androidx.compose.ui.graphics.ImageBitmap? = null
)

data class GridOption(
    val name: String,
    val title: String,
    val rows: Int,
    val cols: Int,
    val isDefault: Boolean
) {
    companion object {
        val DESCENDING_COMPARATOR = compareByDescending<GridOption> { it.cols }
            .thenByDescending { it.rows }
    }
}

class LauncherSettingsViewModel(application: Application) : AndroidViewModel(application) {

    val context = application
    
    val appContext: Application
        get() = context

    private val packageManager: PackageManager = context.packageManager
    private val contentResolver: ContentResolver = context.contentResolver

    private var launcherAuthority: String? = null
    
    private var themedIconUri: Uri? = null
    private var iconPackUri: Uri? = null
    private var gridUri: Uri? = null

    private val _installedIconPacks = MutableStateFlow<List<IconPackItem>>(emptyList())
    val installedIconPacks: StateFlow<List<IconPackItem>> = _installedIconPacks.asStateFlow()
    val selectedIconPack = mutableStateOf("")
    val themedIconsEnabled = mutableStateOf(false)
    
    private val _availableGridOptions = MutableStateFlow<List<GridOption>>(emptyList())
    val availableGridOptions: StateFlow<List<GridOption>> = _availableGridOptions.asStateFlow()
    
    private val _selectedGrid = MutableStateFlow<GridOption?>(null)
    val selectedGrid: StateFlow<GridOption?> = _selectedGrid.asStateFlow()
    
    private val _isLoadingSelection = MutableStateFlow(false)
    val isLoadingSelection: StateFlow<Boolean> = _isLoadingSelection
    
    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            uri?.let {
                when (it) {
                    themedIconUri -> updateThemedIconState(it)
                    iconPackUri -> updateIconPackState(it)
                    gridUri -> loadGridOptions()
                }
            }
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            setupContentUris()
            themedIconUri?.let { updateThemedIconState(it) }
            iconPackUri?.let { updateIconPackState(it) }
            loadGridOptions()
            observeProviderChanges()
        }

        loadInstalledIconPacks()
    }

    private fun setupContentUris() {
        val homeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN)
            .addCategory(android.content.Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(
            homeIntent,
            PackageManager.MATCH_DEFAULT_ONLY or PackageManager.GET_META_DATA
        )
        launcherAuthority = resolveInfo?.activityInfo?.metaData
            ?.getString("com.android.launcher3.grid.control")
        if (launcherAuthority != null) {
            Log.d(TAG, "Found launcher authority: $launcherAuthority")
            themedIconUri = buildUri("icon_themed")
            iconPackUri = buildUri("icon_pack")
            gridUri = buildUri("default_grid")
        } else {
            Log.w(TAG, "Launcher authority not found - customizations unavailable")
        }
    }
    
    private fun buildUri(path: String): Uri? {
        return launcherAuthority?.let {
            Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(it)
                .appendPath(path)
                .build()
        }
    }

    private fun loadInstalledIconPacks() {
        viewModelScope.launch(Dispatchers.IO) {
            val packs = Settings.Secure.getString(
                context.contentResolver,
                "icon_pack_providers"
            )?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

            val items = packs.mapNotNull { pkg ->
                runCatching {
                    val appInfo = packageManager.getApplicationInfo(pkg, 0)
                    val label = packageManager.getApplicationLabel(appInfo)?.toString() ?: pkg
                    val drawable = packageManager.getApplicationIcon(appInfo)
                    if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                        drawable.setBounds(0, 0, 1, 1)
                    } else {
                        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                    }
                    IconPackItem(pkg, label, drawable.toBitmap().asImageBitmap())
                }.getOrNull()
            }

            _installedIconPacks.value = items
        }
    }

    fun loadGridOptions() {
        viewModelScope.launch(Dispatchers.IO) {
            val listUri = buildUri("list_options") ?: return@launch

            val currentGridName = getCurrentGridName()

            val cursor = contentResolver.query(listUri, null, null, null, null)
            val options = mutableListOf<GridOption>()
            var defaultOption: GridOption? = null

            cursor?.use { c ->
                while (c.moveToNext()) {
                    val name = c.getString(c.getColumnIndex("name"))
                    val title = c.getString(c.getColumnIndex("grid_title"))
                    val rows = c.getInt(c.getColumnIndex("rows"))
                    val cols = c.getInt(c.getColumnIndex("cols"))
                    val isDefault = name == currentGridName

                    val option = GridOption(name, title ?: "$cols × $rows", rows, cols, isDefault)
                    options.add(option)
                    if (isDefault) defaultOption = option
                }
            }
            val sortedOptions = options.sortedWith(GridOption.DESCENDING_COMPARATOR)

            Log.d(TAG, "sortedOptions=${sortedOptions}")

            _availableGridOptions.value = sortedOptions
            _selectedGrid.value = sortedOptions.find { it.isDefault }
        }
    }

    private fun getCurrentGridName(): String? {
        val uri = buildUri("get_grid_name") ?: return null
        var gridName: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToNext()) {
                val nameIdx = it.getColumnIndex("grid_name")
                if (nameIdx != -1) {
                    gridName = it.getString(nameIdx)
                }
            }
        }
        Log.d(TAG, "Fetched current grid name: $gridName")
        return gridName
    }

    private fun observeProviderChanges() {
        themedIconUri?.let { uri ->
            contentResolver.registerContentObserver(uri, true, contentObserver)
        }
        iconPackUri?.let { uri ->
            contentResolver.registerContentObserver(uri, true, contentObserver)
        }
        gridUri?.let { uri ->
            contentResolver.registerContentObserver(uri, true, contentObserver)
        }
    }

    private fun updateThemedIconState(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            var enabled = false
            cursor?.use {
                if (it.moveToNext()) {
                    val idx = it.getColumnIndex("boolean_value")
                    if (idx != -1) enabled = it.getInt(idx) == 1
                }
            }
            themedIconsEnabled.value = enabled
            Log.d(TAG, "Themed icons state: $enabled")
        }
    }

    private fun updateIconPackState(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            var pkg = ""
            cursor?.use {
                if (it.moveToNext()) {
                    val idx = it.getColumnIndex("string_value")
                    if (idx != -1) pkg = it.getString(idx) ?: ""
                }
            }
            selectedIconPack.value = pkg
            Log.d(TAG, "Icon pack state: $pkg")
        }
    }

    fun setThemedIcons(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            themedIconUri?.let { uri ->
                val values = ContentValues().apply { 
                    put("boolean_value", if (enabled) 1 else 0) 
                }
                contentResolver.update(uri, values, null, null)
                themedIconsEnabled.value = enabled
                Log.d(TAG, "Set themed icons: $enabled")
            }
        }
    }

    fun setIconPack(packageName: String) {
        selectedIconPack.value = packageName
        viewModelScope.launch(Dispatchers.IO) {
            iconPackUri?.let { uri ->
                val values = ContentValues().apply { 
                    put("string_value", packageName) 
                }
                contentResolver.update(uri, values, null, null)
                Log.d(TAG, "Set icon pack: $packageName")
            }
        }
    }

    fun setGrid(gridName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            gridUri?.let { uri ->
                val values = ContentValues().apply { 
                    put("name", gridName) 
                }
                val result = contentResolver.update(uri, values, null, null)
                if (result > 0) {
                    _selectedGrid.value = _availableGridOptions.value.find { it.name == gridName }
                    Log.d(TAG, "Set grid: $gridName")
                } else {
                    Log.w(TAG, "Failed to set grid: $gridName")
                }
            }
        }
    }

    fun selectGrid(grid: GridOption) {
        viewModelScope.launch {
            _isLoadingSelection.value = true
            setGrid(grid)
            delay(1000)
            _isLoadingSelection.value = false
        }
    }

    fun setGrid(grid: GridOption) {
        setGrid(grid.name)
    }

    fun resetIconPack() {
        selectedIconPack.value = ""
        setIconPack("")
    }
    
    fun resetToDefaults() {
        viewModelScope.launch(Dispatchers.IO) {
            setIconPack("")
            setThemedIcons(false)
            _availableGridOptions.value.find { it.isDefault }?.let { 
                setGrid(it.name)
            }
            Log.d(TAG, "Reset all settings to defaults")
        }
    }

    override fun onCleared() {
        super.onCleared()
        contentResolver.unregisterContentObserver(contentObserver)
        Log.d(TAG, "ViewModel cleared")
    }
}
