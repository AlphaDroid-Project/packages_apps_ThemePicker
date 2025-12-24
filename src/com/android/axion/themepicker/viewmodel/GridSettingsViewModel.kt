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
package com.android.axion.themepicker.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private const val TAG = "GridSettingsVM"

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

class GridSettingsViewModel(application: Application) : AndroidViewModel(application) {

    val context = application
    
    val appContext: Application
        get() = context

    private val packageManager: PackageManager = context.packageManager
    private val contentResolver: ContentResolver = context.contentResolver

    private var launcherAuthority: String? = null
    
    private var gridUri: Uri? = null
    
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
                    gridUri -> loadGridOptions()
                }
            }
        }
    }

    init {
        discoverLauncherProvider()
        observeProviderChanges()
    }

    private fun discoverLauncherProvider() {
        val launcherPackages = listOf(
            "com.android.launcher3",
            "com.google.android.apps.nexuslauncher",
            "app.flavor.lawnchair"
        )

        for (pkg in launcherPackages) {
            val authority = "$pkg.grid_control"
            val testUri = Uri.parse("content://$authority/get_grid_name")

            try {
                contentResolver.query(testUri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        launcherAuthority = authority
                        Log.d(TAG, "Found launcher provider: $authority")
                        
                        gridUri = Uri.parse("content://$authority/default_grid")
                        return
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Provider $authority not available: ${e.message}")
            }
        }
        Log.w(TAG, "No launcher settings provider found")
    }

    private fun buildUri(path: String): Uri? {
        return launcherAuthority?.let { Uri.parse("content://$it/$path") }
    }

    fun loadGridOptions() {
        viewModelScope.launch(Dispatchers.IO) {
            val listUri = buildUri("list_options") ?: return@launch

            val currentGridName = getCurrentGridName()

            val cursor = contentResolver.query(listUri, null, null, null, null)
            val options = mutableListOf<GridOption>()
            var defaultOption: GridOption? = null

            cursor?.use { c ->
                val nameIdx = c.getColumnIndex("name")
                val titleIdx = c.getColumnIndex("grid_title")
                val rowsIdx = c.getColumnIndex("rows")
                val colsIdx = c.getColumnIndex("cols")

                while (c.moveToNext()) {
                    if (nameIdx == -1 || rowsIdx == -1 || colsIdx == -1) {
                        Log.e(TAG, "Missing required columns in grid options cursor")
                        continue
                    }

                    val name = c.getString(nameIdx)
                    val title = if (titleIdx != -1) c.getString(titleIdx) else null
                    val rows = c.getInt(rowsIdx)
                    val cols = c.getInt(colsIdx)
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
        gridUri?.let { uri ->
            contentResolver.registerContentObserver(uri, true, contentObserver)
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

    override fun onCleared() {
        super.onCleared()
        contentResolver.unregisterContentObserver(contentObserver)
        Log.d(TAG, "ViewModel cleared")
    }
}
