package com.flowtask.app.widget

import android.content.Context
import android.content.SharedPreferences

object ProgramWidgetPrefs {
    private const val PREFS_NAME = "flowtask_widget_program_prefs"
    private const val PREF_PREFIX_KEY = "appwidget_program_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSelectedProgramId(context: Context, appWidgetId: Int): String? {
        val prefs = getPrefs(context)
        return prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
    }

    fun setSelectedProgramId(context: Context, appWidgetId: Int, programId: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString(PREF_PREFIX_KEY + appWidgetId, programId).apply()
    }

    fun deleteWidgetPref(context: Context, appWidgetId: Int) {
        val prefs = getPrefs(context)
        prefs.edit().remove(PREF_PREFIX_KEY + appWidgetId).apply()
    }
}
