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

package com.android.axion.themepicker.viewmodel

import android.app.Application
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.customization.model.grid.DefaultShapeGridManager
import com.android.customization.model.grid.GridOptionModel
import com.android.customization.model.grid.ShapeOptionModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private const val TAG = "GridSettingsVM"

class GridSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val bgScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val manager =
        DefaultShapeGridManager(application.applicationContext, Dispatchers.IO, bgScope)

    private val _gridOptions = MutableStateFlow<List<GridOptionModel>>(emptyList())
    val gridOptions: StateFlow<List<GridOptionModel>> = _gridOptions.asStateFlow()

    private val _selectedGridKey = MutableStateFlow<String?>(null)
    val selectedGridKey: StateFlow<String?> = _selectedGridKey.asStateFlow()

    private val _shapeOptions = MutableStateFlow<List<ShapeOptionModel>>(emptyList())
    val shapeOptions: StateFlow<List<ShapeOptionModel>> = _shapeOptions.asStateFlow()

    private val _selectedShapeKey = MutableStateFlow<String?>(null)
    val selectedShapeKey: StateFlow<String?> = _selectedShapeKey.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _isApplying = MutableStateFlow(false)
    val isApplying: StateFlow<Boolean> = _isApplying.asStateFlow()

    init {
        collectOptions()
    }

    private fun collectOptions() {
        viewModelScope.launch {
            manager.isCustomizationAvailable.collect { available -> _isAvailable.value = available }
        }
        viewModelScope.launch {
            manager.gridOptions.collect { options ->
                _gridOptions.value = options
                _selectedGridKey.value = options.firstOrNull { it.isCurrent }?.key
            }
        }
        viewModelScope.launch {
            manager.shapeOptions.collect { options ->
                _shapeOptions.value = options
                _selectedShapeKey.value = options.firstOrNull { it.isCurrent }?.key
            }
        }
    }

    fun selectGrid(option: GridOptionModel) {
        viewModelScope.launch {
            _isApplying.value = true
            _selectedGridKey.value = option.key
            withContext(Dispatchers.IO) { manager.applyGridOption(option.key) }
            delay(500)
            _isApplying.value = false
        }
    }

    fun selectShape(option: ShapeOptionModel) {
        viewModelScope.launch {
            _isApplying.value = true
            _selectedShapeKey.value = option.key
            withContext(Dispatchers.IO) { manager.applyShapeOption(option.key) }
            delay(500)
            _isApplying.value = false
        }
    }

    fun getGridDrawable(iconId: Int): Drawable? {
        return manager.getGridOptionDrawable(iconId)
    }

    override fun onCleared() {
        super.onCleared()
        bgScope.cancel()
        Log.d(TAG, "ViewModel cleared")
    }
}
