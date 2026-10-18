package com.flowtask.app.data.local.model

import java.util.UUID

data class SemesterConfig(
    val isEnabled: Boolean = false,
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null,
    val programLayerName: String = "Програма",
    val scheduleItems: List<ScheduleItem> = emptyList()
)

data class ScheduleItem(
    val id: String = UUID.randomUUID().toString(),
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ..., 7 = Sunday
    val subject: String,
    val startMinutes: Int, // minutes from midnight, e.g. 540 = 09:00
    val endMinutes: Int? = null,
    val roomOrNote: String? = null
) {
    val title: String get() = subject
    val locationOrNote: String? get() = roomOrNote
}
