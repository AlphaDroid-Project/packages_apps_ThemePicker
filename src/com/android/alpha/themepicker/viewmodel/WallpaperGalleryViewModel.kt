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

package com.android.alpha.themepicker.viewmodel

import androidx.lifecycle.ViewModel
import com.android.alpha.themepicker.data.model.GalleryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WallpaperGalleryViewModel : ViewModel() {

    private val _currentGalleryState = MutableStateFlow<GalleryState>(GalleryState.Overview)
    val currentGalleryState: StateFlow<GalleryState> = _currentGalleryState

    private val _screenStack = mutableListOf<GalleryState>()

    private val _isNavigatingBack = MutableStateFlow(false)
    val isNavigatingBack: StateFlow<Boolean> = _isNavigatingBack

    fun initialize(initialState: GalleryState = GalleryState.Overview) {
        if (_currentGalleryState.value == initialState) return
        _currentGalleryState.value = initialState
    }

    fun navigateTo(state: GalleryState) {
        _isNavigatingBack.value = false
        _screenStack.add(_currentGalleryState.value)
        _currentGalleryState.value = state
    }

    fun goBack(onBackFromRoot: () -> Unit) {
        _isNavigatingBack.value = true

        if (_screenStack.isNotEmpty()) {
            _currentGalleryState.value = _screenStack.removeAt(_screenStack.lastIndex)
        } else {
            onBackFromRoot()
            _isNavigatingBack.value = false
        }
    }

    fun resetToOverview() {
        _isNavigatingBack.value = false
        _currentGalleryState.value = GalleryState.Overview
        _screenStack.clear()
    }
}
