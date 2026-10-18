package com.flowtask.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flowtask.app.data.repository.SettingsRepository
import com.flowtask.app.ui.theme.DynamicColorStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val language: StateFlow<String> = repository.language
    val accentColor: StateFlow<String> = repository.accentColor
    val themeMode: StateFlow<String> = repository.themeMode
    val amoledDarkMode: StateFlow<Boolean> = repository.amoledDarkMode
    val dynamicColorStyle: StateFlow<DynamicColorStyle> = repository.dynamicColorStyle
    val screenOrder: StateFlow<List<Int>> = repository.screenOrder
    val programTasksEverywhere: StateFlow<Boolean> = repository.programTasksEverywhere

    private val _isSettingsSheetVisible = MutableStateFlow(false)
    val isSettingsSheetVisible: StateFlow<Boolean> = _isSettingsSheetVisible.asStateFlow()

    fun openSettings() {
        _isSettingsSheetVisible.value = true
    }

    fun closeSettings() {
        _isSettingsSheetVisible.value = false
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            repository.setLanguage(lang)
        }
    }

    fun setAccentColor(colorHex: String) {
        viewModelScope.launch {
            repository.setAccentColor(colorHex)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setAmoledDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAmoledDarkMode(enabled)
        }
    }

    fun setDynamicColorStyle(style: DynamicColorStyle) {
        viewModelScope.launch {
            repository.setDynamicColorStyle(style)
        }
    }

    fun setProgramTasksEverywhere(everywhere: Boolean) {
        viewModelScope.launch {
            repository.setProgramTasksEverywhere(everywhere)
        }
    }

    fun moveScreenUp(index: Int) {
        val current = screenOrder.value.toMutableList()
        if (index > 0 && index < current.size) {
            val temp = current[index]
            current[index] = current[index - 1]
            current[index - 1] = temp
            repository.setScreenOrder(current)
        }
    }

    fun moveScreenDown(index: Int) {
        val current = screenOrder.value.toMutableList()
        if (index >= 0 && index < current.size - 1) {
            val temp = current[index]
            current[index] = current[index + 1]
            current[index + 1] = temp
            repository.setScreenOrder(current)
        }
    }

    fun resetScreenOrder() {
        repository.resetScreenOrder()
    }
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
