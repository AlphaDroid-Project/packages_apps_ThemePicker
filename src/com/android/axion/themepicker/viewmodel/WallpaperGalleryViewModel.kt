package com.android.axion.themepicker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.axion.themepicker.data.model.GalleryState
import com.android.axion.themepicker.data.model.WallpaperInfo
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
