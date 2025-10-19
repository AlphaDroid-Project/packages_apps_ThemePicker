package com.android.axion.themepicker.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.android.axion.themepicker.data.model.LayoutPreferenceItem
import com.android.axion.themepicker.data.model.LayoutScreen

class LayoutScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val _appIcons = MutableStateFlow<List<Drawable>>(emptyList())
    val appIcons: StateFlow<List<Drawable>> = _appIcons

    private val _currentLayoutScreen = MutableStateFlow<LayoutScreen>(LayoutScreen.Root)
    val currentLayoutScreen: StateFlow<LayoutScreen> = _currentLayoutScreen

    private val _layoutScreenStack = mutableListOf<LayoutScreen>()

    private val _isNavigatingBack = MutableStateFlow(false)
    val isNavigatingBack: StateFlow<Boolean> = _isNavigatingBack

    val preferenceItems = listOf(
        LayoutPreferenceItem(
            title = "App Grid",
            description = "Change layout grid options",
            destination = LayoutScreen.AppGridSettings
        ),
        LayoutPreferenceItem(
            title = "System Icons",
            description = "Customize system icons",
            destination = LayoutScreen.SystemIcons
        ),
        LayoutPreferenceItem(
            title = "Font",
            description = "Choose your system font",
            destination = LayoutScreen.Font
        ),
        LayoutPreferenceItem(
            title = "Shape",
            description = "Change shape corners and icons",
            destination = LayoutScreen.Shape
        )
    )

    init {
        loadAppIcons()
    }

    private fun loadAppIcons() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pm = getApplication<Application>().packageManager
                val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                    .take(3)
                    .mapNotNull { appInfo ->
                        try {
                            pm.getApplicationIcon(appInfo.packageName)
                        } catch (e: Exception) {
                            null
                        }
                    }

                _appIcons.value = apps
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun navigateToLayoutScreen(screen: LayoutScreen) {
        _isNavigatingBack.value = false
        _layoutScreenStack.add(_currentLayoutScreen.value)
        _currentLayoutScreen.value = screen
    }

    fun goBackInLayout(mainScreenViewModel: MainScreenViewModel) {
        _isNavigatingBack.value = true

        if (_layoutScreenStack.isNotEmpty()) {
            _currentLayoutScreen.value = _layoutScreenStack.removeAt(_layoutScreenStack.lastIndex)
        } else {
            mainScreenViewModel.resetToMain()
            resetLayoutNavigation()
        }
    }

    fun resetLayoutNavigation() {
        _isNavigatingBack.value = false
        _currentLayoutScreen.value = LayoutScreen.Root
        _layoutScreenStack.clear()
    }

    fun onItemSelected(item: LayoutPreferenceItem, mainScreenViewModel: MainScreenViewModel) {
        navigateToLayoutScreen(item.destination)
    }
}
