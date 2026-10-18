package com.flowtask.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#2A8575",
    val iconName: String = "folder",
    val sortOrder: Int = 0,
    val isPinned: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "project_folders",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProjectFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentFolderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId"), Index("parentFolderId")]
)
data class ProjectFolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val parentFolderId: Long? = null, // null if root folder within project
    val name: String,
    val colorHex: String = "#6366F1",
    val sortOrder: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "project_tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProjectFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId"), Index("folderId")]
)
data class ProjectTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val folderId: Long? = null, // null if placed directly in project root
    val explanation: String,
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
    val sortOrder: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "project_subtasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectTaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectTaskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectTaskId")]
)
data class ProjectSubTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectTaskId: Long,
    val explanation: String,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "project_notes",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProjectFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId"), Index("folderId")]
)
data class ProjectNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val folderId: Long? = null, // null if at project root
    val title: String = "",
    val content: String,
    val sortOrder: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)
