package com.animeplayer.app.ui.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animeplayer.app.AppThemeMode
import com.animeplayer.app.data.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application
) : ViewModel() {

    private val settingsDataStore = SettingsDataStore(application)

    val themeMode = settingsDataStore.themeMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        AppThemeMode.SYSTEM
    )

    val useDynamicColor = settingsDataStore.useDynamicColor.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        true
    )

    val rememberPlaybackPosition = settingsDataStore.rememberPlaybackPosition.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        true
    )

    fun setThemeMode(themeMode: AppThemeMode) {
        viewModelScope.launch {
            settingsDataStore.setThemeMode(themeMode)
        }
    }

    fun setUseDynamicColor(useDynamicColor: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setUseDynamicColor(useDynamicColor)
        }
    }

    fun setRememberPlaybackPosition(remember: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setRememberPlaybackPosition(remember)
        }
    }
}
