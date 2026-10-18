package com.flowtask.app.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.flowtask.app.data.local.entity.SubTaskEntity
import com.flowtask.app.data.local.entity.TaskEntity

data class TaskWithSubTasks(
    @Embedded
    val task: TaskEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subTasks: List<SubTaskEntity> = emptyList()
)
