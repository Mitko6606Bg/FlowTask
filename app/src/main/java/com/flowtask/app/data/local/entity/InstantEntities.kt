package com.flowtask.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "instant_snaps",
    indices = [Index(value = ["folderName"])]
)
data class InstantSnapEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,
    val noteText: String? = null,
    val folderName: String = "Unsorted",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "instant_folders",
    indices = [Index(value = ["name"], unique = true)]
)
data class InstantFolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)
