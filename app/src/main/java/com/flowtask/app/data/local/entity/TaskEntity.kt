package com.flowtask.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val explanation: String,
    val dueDateMillis: Long? = null,
    val dueTimeMinutes: Int? = null, // Minutes since midnight: 0 to 1439
    val startDateMillis: Long? = null, // Start of period (inclusive). If null, task is single-day on dueDateMillis
    val layer: String? = null, // Custom Category / Profile layer e.g. "House", "Work", etc.
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
    val sortOrder: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis()
)
