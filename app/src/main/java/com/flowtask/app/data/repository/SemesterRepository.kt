package com.flowtask.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.data.local.model.ScheduleItem
import com.flowtask.app.data.local.model.SemesterConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class SemesterRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("flowtask_semester", Context.MODE_PRIVATE)

    private val _layerPrograms = MutableStateFlow(loadLayerPrograms())
    val layerPrograms: StateFlow<List<LayerProgram>> = _layerPrograms.asStateFlow()

    private val _semesterConfig = MutableStateFlow(loadLegacyConfig())
    val semesterConfig: StateFlow<SemesterConfig> = _semesterConfig.asStateFlow()

    fun updateConfig(config: SemesterConfig) {
        _semesterConfig.value = config
        saveLegacyToPrefs(config)

        // Sync into layer programs list
        val current = _layerPrograms.value.toMutableList()
        val index = current.indexOfFirst { it.layerName.equals(config.programLayerName, ignoreCase = true) }
        val updatedProgram = LayerProgram(
            id = if (index >= 0) current[index].id else java.util.UUID.randomUUID().toString(),
            layerName = config.programLayerName,
            isEnabled = config.isEnabled,
            startDateMillis = config.startDateMillis,
            endDateMillis = config.endDateMillis,
            trackWeeks = true,
            scheduleItems = config.scheduleItems
        )
        if (index >= 0) {
            current[index] = updatedProgram
        } else {
            current.add(updatedProgram)
        }
        _layerPrograms.value = current
        saveLayerProgramsToPrefs(current)
    }

    fun setEnabled(enabled: Boolean) {
        val updated = _semesterConfig.value.copy(isEnabled = enabled)
        updateConfig(updated)
    }

    fun updateDates(startDateMillis: Long?, endDateMillis: Long?) {
        val updated = _semesterConfig.value.copy(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis
        )
        updateConfig(updated)
    }

    fun setProgramLayerName(layerName: String) {
        val updated = _semesterConfig.value.copy(programLayerName = layerName)
        updateConfig(updated)
    }

    fun addScheduleItem(item: ScheduleItem) {
        val current = _semesterConfig.value.scheduleItems.toMutableList()
        current.add(item)
        val updated = _semesterConfig.value.copy(scheduleItems = current)
        updateConfig(updated)
    }

    fun removeScheduleItem(itemId: String) {
        val current = _semesterConfig.value.scheduleItems.filter { it.id != itemId }
        val updated = _semesterConfig.value.copy(scheduleItems = current)
        updateConfig(updated)
    }

    fun updateScheduleItems(items: List<ScheduleItem>) {
        val updated = _semesterConfig.value.copy(scheduleItems = items)
        updateConfig(updated)
    }

    // =========================================================================
    // Multi-Layer Programs API
    // =========================================================================

    fun saveLayerProgram(program: LayerProgram) {
        val current = _layerPrograms.value.toMutableList()
        val index = current.indexOfFirst { it.id == program.id || it.layerName.equals(program.layerName, ignoreCase = true) }
        if (index >= 0) {
            current[index] = program
        } else {
            current.add(program)
        }
        _layerPrograms.value = current
        saveLayerProgramsToPrefs(current)

        // If this program corresponds to current semesterConfig layer, update it too
        if (program.layerName.equals(_semesterConfig.value.programLayerName, ignoreCase = true)) {
            val updatedConfig = SemesterConfig(
                isEnabled = program.isEnabled,
                startDateMillis = program.startDateMillis,
                endDateMillis = program.endDateMillis,
                programLayerName = program.layerName,
                scheduleItems = program.scheduleItems
            )
            _semesterConfig.value = updatedConfig
            saveLegacyToPrefs(updatedConfig)
        }
    }

    fun deleteLayerProgram(programId: String) {
        val current = _layerPrograms.value.filter { it.id != programId }
        _layerPrograms.value = current
        saveLayerProgramsToPrefs(current)
    }

    fun toggleProgramEnabled(programId: String, isEnabled: Boolean) {
        val current = _layerPrograms.value.map {
            if (it.id == programId) it.copy(isEnabled = isEnabled) else it
        }
        _layerPrograms.value = current
        saveLayerProgramsToPrefs(current)

        // Sync legacy if needed
        val target = current.find { it.id == programId }
        if (target != null && target.layerName.equals(_semesterConfig.value.programLayerName, ignoreCase = true)) {
            val updatedConfig = _semesterConfig.value.copy(isEnabled = isEnabled)
            _semesterConfig.value = updatedConfig
            saveLegacyToPrefs(updatedConfig)
        }
    }

    fun getProgramForLayer(layerName: String?): LayerProgram? {
        if (layerName.isNullOrBlank()) return null
        return _layerPrograms.value.firstOrNull { it.layerName.trim().equals(layerName.trim(), ignoreCase = true) }
    }

    fun renameLayerProgram(oldLayerName: String, newLayerName: String) {
        val trimmedOld = oldLayerName.trim()
        val trimmedNew = newLayerName.trim()
        val current = _layerPrograms.value.map {
            if (it.layerName.trim().equals(trimmedOld, ignoreCase = true)) {
                it.copy(layerName = trimmedNew)
            } else {
                it
            }
        }
        _layerPrograms.value = current
        saveLayerProgramsToPrefs(current)

        if (_semesterConfig.value.programLayerName.trim().equals(trimmedOld, ignoreCase = true)) {
            val updatedConfig = _semesterConfig.value.copy(programLayerName = trimmedNew)
            _semesterConfig.value = updatedConfig
            saveLegacyToPrefs(updatedConfig)
        }
    }

    fun deleteLayerProgramByLayerName(layerName: String) {
        val trimmed = layerName.trim()
        val current = _layerPrograms.value.filter {
            !it.layerName.trim().equals(trimmed, ignoreCase = true)
        }
        _layerPrograms.value = current
        saveLayerProgramsToPrefs(current)

        if (_semesterConfig.value.programLayerName.trim().equals(trimmed, ignoreCase = true)) {
            val updatedConfig = _semesterConfig.value.copy(programLayerName = "")
            _semesterConfig.value = updatedConfig
            saveLegacyToPrefs(updatedConfig)
        }
    }

    // =========================================================================
    // Persistence Serialization
    // =========================================================================

    private fun loadLayerPrograms(): List<LayerProgram> {
        val raw = prefs.getString(KEY_LAYER_PROGRAMS, null)
        if (!raw.isNullOrBlank()) {
            try {
                val jsonArray = JSONArray(raw)
                val list = mutableListOf<LayerProgram>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val scheduleItems = parseScheduleItems(obj.optString("scheduleItems", "[]"))
                    list.add(
                        LayerProgram(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            layerName = obj.optString("layerName", "Програма"),
                            isEnabled = obj.optBoolean("isEnabled", true),
                            colorHex = if (obj.has("colorHex") && !obj.isNull("colorHex")) obj.getString("colorHex") else null,
                            startDateMillis = if (obj.has("startDateMillis") && !obj.isNull("startDateMillis")) obj.getLong("startDateMillis") else null,
                            endDateMillis = if (obj.has("endDateMillis") && !obj.isNull("endDateMillis")) obj.getLong("endDateMillis") else null,
                            trackWeeks = obj.optBoolean("trackWeeks", true),
                            scheduleItems = scheduleItems,
                            showTasksEverywhere = obj.optBoolean("showTasksEverywhere", false)
                        )
                    )
                }
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {
            }
        }

        // Migrate from legacy single semester config if present
        val legacy = loadLegacyConfig()
        val defaultProgram = LayerProgram(
            id = "default_program_layer",
            layerName = legacy.programLayerName,
            isEnabled = legacy.isEnabled,
            startDateMillis = legacy.startDateMillis,
            endDateMillis = legacy.endDateMillis,
            trackWeeks = true,
            scheduleItems = legacy.scheduleItems,
            showTasksEverywhere = false
        )
        return listOf(defaultProgram)
    }

    private fun saveLayerProgramsToPrefs(programs: List<LayerProgram>) {
        try {
            val jsonArray = JSONArray()
            for (p in programs) {
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("layerName", p.layerName)
                    put("isEnabled", p.isEnabled)
                    if (p.colorHex != null) put("colorHex", p.colorHex)
                    if (p.startDateMillis != null) put("startDateMillis", p.startDateMillis)
                    if (p.endDateMillis != null) put("endDateMillis", p.endDateMillis)
                    put("trackWeeks", p.trackWeeks)
                    put("showTasksEverywhere", p.showTasksEverywhere)

                    val itemsArray = JSONArray()
                    for (item in p.scheduleItems) {
                        itemsArray.put(serializeScheduleItem(item))
                    }
                    put("scheduleItems", itemsArray.toString())
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_LAYER_PROGRAMS, jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }

    private fun parseScheduleItems(raw: String?): List<ScheduleItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(raw)
            val items = mutableListOf<ScheduleItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                items.add(
                    ScheduleItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        dayOfWeek = obj.optInt("dayOfWeek", 1),
                        subject = obj.optString("subject", ""),
                        startMinutes = obj.optInt("startMinutes", 540),
                        endMinutes = if (obj.has("endMinutes") && !obj.isNull("endMinutes")) obj.getInt("endMinutes") else null,
                        roomOrNote = if (obj.has("roomOrNote") && !obj.isNull("roomOrNote")) obj.getString("roomOrNote") else null
                    )
                )
            }
            items
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializeScheduleItem(item: ScheduleItem): JSONObject {
        return JSONObject().apply {
            put("id", item.id)
            put("dayOfWeek", item.dayOfWeek)
            put("subject", item.subject)
            put("startMinutes", item.startMinutes)
            if (item.endMinutes != null) put("endMinutes", item.endMinutes)
            if (item.roomOrNote != null) put("roomOrNote", item.roomOrNote)
        }
    }

    private fun loadLegacyConfig(): SemesterConfig {
        return try {
            val isEnabled = prefs.getBoolean(KEY_IS_ENABLED, false)
            val startMillis = if (prefs.contains(KEY_START_DATE)) prefs.getLong(KEY_START_DATE, 0L).takeIf { it > 0 } else null
            val endMillis = if (prefs.contains(KEY_END_DATE)) prefs.getLong(KEY_END_DATE, 0L).takeIf { it > 0 } else null
            val layerName = prefs.getString(KEY_LAYER_NAME, "Програма") ?: "Програма"
            val rawItems = prefs.getString(KEY_SCHEDULE_ITEMS, null)
            val scheduleItems = parseScheduleItems(rawItems)

            SemesterConfig(
                isEnabled = isEnabled,
                startDateMillis = startMillis,
                endDateMillis = endMillis,
                programLayerName = layerName,
                scheduleItems = scheduleItems
            )
        } catch (_: Exception) {
            SemesterConfig()
        }
    }

    private fun saveLegacyToPrefs(config: SemesterConfig) {
        try {
            val editor = prefs.edit()
            editor.putBoolean(KEY_IS_ENABLED, config.isEnabled)
            if (config.startDateMillis != null) {
                editor.putLong(KEY_START_DATE, config.startDateMillis)
            } else {
                editor.remove(KEY_START_DATE)
            }
            if (config.endDateMillis != null) {
                editor.putLong(KEY_END_DATE, config.endDateMillis)
            } else {
                editor.remove(KEY_END_DATE)
            }
            editor.putString(KEY_LAYER_NAME, config.programLayerName)

            val jsonArray = JSONArray()
            for (item in config.scheduleItems) {
                jsonArray.put(serializeScheduleItem(item))
            }
            editor.putString(KEY_SCHEDULE_ITEMS, jsonArray.toString())
            editor.apply()
        } catch (_: Exception) {
        }
    }

    companion object {
        private const val KEY_IS_ENABLED = "key_is_enabled"
        private const val KEY_START_DATE = "key_start_date"
        private const val KEY_END_DATE = "key_end_date"
        private const val KEY_LAYER_NAME = "key_layer_name"
        private const val KEY_SCHEDULE_ITEMS = "key_schedule_items"
        private const val KEY_LAYER_PROGRAMS = "key_layer_programs"
    }
}
