package com.flowtask.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.flowtask.app.ui.theme.DynamicColorStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(
    private val prefs: SharedPreferences,
    private val context: Context? = null
) {
    constructor(context: Context) : this(
        context.getSharedPreferences("flowtask_settings", Context.MODE_PRIVATE),
        context.applicationContext
    )

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "bg") ?: "bg")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _accentColor = MutableStateFlow(prefs.getString(KEY_ACCENT_COLOR, "#2A8575") ?: "#2A8575")
    val accentColor: StateFlow<String> = _accentColor.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _amoledDarkMode = MutableStateFlow(prefs.getBoolean(KEY_AMOLED_DARK_MODE, false))
    val amoledDarkMode: StateFlow<Boolean> = _amoledDarkMode.asStateFlow()

    private val _dynamicColorStyle = MutableStateFlow(DynamicColorStyle.fromKey(prefs.getString(KEY_DYNAMIC_COLOR_STYLE, "balanced")))
    val dynamicColorStyle: StateFlow<DynamicColorStyle> = _dynamicColorStyle.asStateFlow()

    private val _screenOrder = MutableStateFlow(loadScreenOrder())
    val screenOrder: StateFlow<List<Int>> = _screenOrder.asStateFlow()

    private val _allTasksViewMode = MutableStateFlow(prefs.getString(KEY_ALL_TASKS_VIEW_MODE, "LIST") ?: "LIST")
    val allTasksViewMode: StateFlow<String> = _allTasksViewMode.asStateFlow()

    private val _programTasksEverywhere = MutableStateFlow(
        prefs.getBoolean(KEY_PROGRAM_TASKS_EVERYWHERE, true)
    )
    val programTasksEverywhere: StateFlow<Boolean> = _programTasksEverywhere.asStateFlow()

    private val _calendarFiltersExpanded = MutableStateFlow(
        prefs.getBoolean(KEY_CALENDAR_FILTERS_EXPANDED, false)
    )
    val calendarFiltersExpanded: StateFlow<Boolean> = _calendarFiltersExpanded.asStateFlow()

    private val _reminderAdvanceMinutes = MutableStateFlow(
        prefs.getInt(KEY_REMINDER_ADVANCE_MINUTES, 0)
    )
    val reminderAdvanceMinutes: StateFlow<Int> = _reminderAdvanceMinutes.asStateFlow()

    fun setLanguage(lang: String) {
        _language.value = lang
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    fun setAccentColor(colorHex: String) {
        _accentColor.value = colorHex
        prefs.edit().putString(KEY_ACCENT_COLOR, colorHex).apply()
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    fun setAmoledDarkMode(enabled: Boolean) {
        _amoledDarkMode.value = enabled
        prefs.edit().putBoolean(KEY_AMOLED_DARK_MODE, enabled).apply()
    }

    fun setDynamicColorStyle(style: DynamicColorStyle) {
        _dynamicColorStyle.value = style
        prefs.edit().putString(KEY_DYNAMIC_COLOR_STYLE, style.key).apply()
    }

    fun setScreenOrder(order: List<Int>) {
        _screenOrder.value = order
        val serialized = order.joinToString(",")
        prefs.edit().putString(KEY_SCREEN_ORDER, serialized).apply()
    }

    fun setAllTasksViewMode(mode: String) {
        _allTasksViewMode.value = mode
        prefs.edit().putString(KEY_ALL_TASKS_VIEW_MODE, mode).apply()
    }

    fun setProgramTasksEverywhere(everywhere: Boolean) {
        _programTasksEverywhere.value = everywhere
        prefs.edit().putBoolean(KEY_PROGRAM_TASKS_EVERYWHERE, everywhere).apply()
    }

    fun setCalendarFiltersExpanded(expanded: Boolean) {
        _calendarFiltersExpanded.value = expanded
        prefs.edit().putBoolean(KEY_CALENDAR_FILTERS_EXPANDED, expanded).apply()
    }

    fun setReminderAdvanceMinutes(minutes: Int) {
        _reminderAdvanceMinutes.value = minutes
        prefs.edit().putInt(KEY_REMINDER_ADVANCE_MINUTES, minutes).apply()
    }

    fun resetScreenOrder() {
        setScreenOrder(listOf(0, 1, 2, 3))
    }

    private fun loadScreenOrder(): List<Int> {
        val raw = prefs.getString(KEY_SCREEN_ORDER, "0,1,2,3") ?: "0,1,2,3"
        return try {
            val parsed = raw.split(",").map { it.trim().toInt() }
            if (parsed.size == 4 && parsed.toSet() == setOf(0, 1, 2, 3)) {
                parsed
            } else if (parsed.size == 3 && parsed.toSet() == setOf(0, 1, 2)) {
                parsed + 3
            } else {
                listOf(0, 1, 2, 3)
            }
        } catch (_: Exception) {
            listOf(0, 1, 2, 3)
        }
    }

    companion object {
        const val KEY_PROGRAM_TASKS_EVERYWHERE = "key_program_tasks_everywhere"
        const val KEY_CALENDAR_FILTERS_EXPANDED = "key_calendar_filters_expanded"
        const val KEY_REMINDER_ADVANCE_MINUTES = "key_reminder_advance_minutes"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_ACCENT_COLOR = "key_accent_color"
        private const val KEY_THEME_MODE = "key_theme_mode"
        const val KEY_AMOLED_DARK_MODE = "key_amoled_dark_mode"
        const val KEY_DYNAMIC_COLOR_STYLE = "key_dynamic_color_style"
        private const val KEY_SCREEN_ORDER = "key_screen_order"
        private const val KEY_ALL_TASKS_VIEW_MODE = "key_all_tasks_view_mode"
    }
}
