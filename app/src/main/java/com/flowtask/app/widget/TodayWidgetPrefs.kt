package com.flowtask.app.widget

import android.content.Context
import android.content.SharedPreferences

object TodayWidgetPrefs {
    private const val PREFS_NAME = "flowtask_widget_today_prefs"
    private const val PREF_LAYER_PREFIX = "today_widget_layer_"
    private const val PREF_EXPANDED_PREFIX = "today_widget_expanded_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSelectedLayer(context: Context, appWidgetId: Int): String? {
        val prefs = getPrefs(context)
        val value = prefs.getString(PREF_LAYER_PREFIX + appWidgetId, null)
        return value?.takeIf { it.isNotBlank() }
    }

    fun setSelectedLayer(context: Context, appWidgetId: Int, layerName: String?) {
        val prefs = getPrefs(context)
        if (layerName.isNullOrBlank()) {
            prefs.edit().remove(PREF_LAYER_PREFIX + appWidgetId).apply()
        } else {
            prefs.edit().putString(PREF_LAYER_PREFIX + appWidgetId, layerName).apply()
        }
    }

    fun getExpandedTaskIds(context: Context, appWidgetId: Int): Set<Long> {
        val prefs = getPrefs(context)
        val raw = prefs.getString(PREF_EXPANDED_PREFIX + appWidgetId, null) ?: return emptySet()
        return raw.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .toSet()
    }

    fun setExpandedTaskIds(context: Context, appWidgetId: Int, taskIds: Set<Long>) {
        val prefs = getPrefs(context)
        if (taskIds.isEmpty()) {
            prefs.edit().remove(PREF_EXPANDED_PREFIX + appWidgetId).apply()
        } else {
            prefs.edit().putString(PREF_EXPANDED_PREFIX + appWidgetId, taskIds.joinToString(",")).apply()
        }
    }

    fun isTaskExpanded(context: Context, appWidgetId: Int, taskId: Long): Boolean {
        return getExpandedTaskIds(context, appWidgetId).contains(taskId)
    }

    fun toggleTaskExpanded(context: Context, appWidgetId: Int, taskId: Long): Boolean {
        val current = getExpandedTaskIds(context, appWidgetId).toMutableSet()
        val nowExpanded = if (current.contains(taskId)) {
            current.remove(taskId)
            false
        } else {
            current.add(taskId)
            true
        }
        setExpandedTaskIds(context, appWidgetId, current)
        return nowExpanded
    }

    fun deleteWidgetPrefs(context: Context, appWidgetId: Int) {
        val prefs = getPrefs(context)
        prefs.edit()
            .remove(PREF_LAYER_PREFIX + appWidgetId)
            .remove(PREF_EXPANDED_PREFIX + appWidgetId)
            .apply()
    }
}
