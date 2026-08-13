package com.notflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DarkThemeConfig {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK
}

class SettingsViewModel : ViewModel() {

    private val _darkThemeConfig = MutableStateFlow(DarkThemeConfig.FOLLOW_SYSTEM)
    val darkThemeConfig: StateFlow<DarkThemeConfig> = _darkThemeConfig.asStateFlow()

    private val _defaultGroceryCategory = MutableStateFlow("Produce")
    val defaultGroceryCategory: StateFlow<String> = _defaultGroceryCategory.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setDarkThemeConfig(config: DarkThemeConfig) {
        _darkThemeConfig.value = config
    }

    fun setDefaultGroceryCategory(category: String) {
        _defaultGroceryCategory.value = category
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel() as T
        }
    }
}
