package com.flowtask.app.data.local.model

import java.util.UUID

/**
 * Represents a weekly routine / recurring schedule and optional date period
 * associated with a specific Task Layer (e.g. "Работа", "Дом", "Спорт", "Програма").
 */
data class LayerProgram(
    val id: String = UUID.randomUUID().toString(),
    val layerName: String,
    val isEnabled: Boolean = true,
    val colorHex: String? = null,
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null,
    val trackWeeks: Boolean = true,
    val scheduleItems: List<ScheduleItem> = emptyList(),
    val showTasksEverywhere: Boolean = false
)
