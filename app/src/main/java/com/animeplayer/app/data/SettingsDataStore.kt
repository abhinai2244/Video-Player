package com.animeplayer.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.animeplayer.app.AppThemeMode
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val USE_DYNAMIC_COLOR_KEY = booleanPreferencesKey("use_dynamic_color")
        val REMEMBER_PLAYBACK_POSITION_KEY = booleanPreferencesKey("remember_playback_position")
    }

    val themeMode = dataStore.data.map {
        AppThemeMode.valueOf(it[THEME_MODE_KEY] ?: AppThemeMode.SYSTEM.name)
    }

    val useDynamicColor = dataStore.data.map {
        it[USE_DYNAMIC_COLOR_KEY] ?: true
    }

    val rememberPlaybackPosition = dataStore.data.map {
        it[REMEMBER_PLAYBACK_POSITION_KEY] ?: true
    }

    suspend fun setThemeMode(themeMode: AppThemeMode) {
        dataStore.edit {
            it[THEME_MODE_KEY] = themeMode.name
        }
    }

    suspend fun setUseDynamicColor(useDynamicColor: Boolean) {
        dataStore.edit {
            it[USE_DYNAMIC_COLOR_KEY] = useDynamicColor
        }
    }

    suspend fun setRememberPlaybackPosition(remember: Boolean) {
        dataStore.edit {
            it[REMEMBER_PLAYBACK_POSITION_KEY] = remember
        }
    }
}
