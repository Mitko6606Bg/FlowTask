package com.flowtask.app.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.flowtask.app.data.local.entity.ProjectEntity
import com.flowtask.app.data.local.entity.ProjectFolderEntity
import com.flowtask.app.data.local.entity.ProjectNoteEntity
import com.flowtask.app.data.local.entity.ProjectSubTaskEntity
import com.flowtask.app.data.local.entity.ProjectTaskEntity

data class ProjectTaskWithSubTasks(
    @Embedded
    val task: ProjectTaskEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "projectTaskId"
    )
    val subTasks: List<ProjectSubTaskEntity> = emptyList()
)

data class ProjectOverview(
    val project: ProjectEntity,
    val taskCount: Int,
    val completedTaskCount: Int,
    val noteCount: Int,
    val folderCount: Int
)

data class FolderWithCounts(
    val folder: ProjectFolderEntity,
    val taskCount: Int,
    val noteCount: Int,
    val subFolderCount: Int
)
