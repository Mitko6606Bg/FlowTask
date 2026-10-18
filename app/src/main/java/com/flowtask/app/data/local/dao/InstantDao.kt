package com.flowtask.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flowtask.app.data.local.entity.InstantFolderEntity
import com.flowtask.app.data.local.entity.InstantSnapEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface InstantDao {

    @Query("SELECT * FROM instant_snaps ORDER BY createdAtMillis DESC")
    fun getAllSnaps(): Flow<List<InstantSnapEntity>>

    @Query("SELECT * FROM instant_snaps WHERE folderName = :folderName ORDER BY createdAtMillis DESC")
    fun getSnapsByFolder(folderName: String): Flow<List<InstantSnapEntity>>

    @Query("SELECT * FROM instant_snaps WHERE id = :id LIMIT 1")
    suspend fun getSnapById(id: Long): InstantSnapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnap(snap: InstantSnapEntity): Long

    @Query("DELETE FROM instant_snaps WHERE id = :id")
    suspend fun deleteSnap(id: Long): Int

    @Query("UPDATE instant_snaps SET folderName = :newFolder WHERE id = :id")
    suspend fun updateSnapFolder(id: Long, newFolder: String): Int

    @Query("UPDATE instant_snaps SET noteText = :newNote WHERE id = :id")
    suspend fun updateSnapNote(id: Long, newNote: String?): Int

    @Query("SELECT * FROM instant_folders ORDER BY createdAtMillis ASC")
    fun getAllFolders(): Flow<List<InstantFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder: InstantFolderEntity): Long

    @Query("DELETE FROM instant_folders WHERE id = :id")
    suspend fun deleteFolder(id: Long): Int

    @Query("DELETE FROM instant_folders WHERE name = :name")
    suspend fun deleteFolderByName(name: String): Int

    @Query("UPDATE instant_snaps SET folderName = :fallbackFolder WHERE folderName = :oldFolder")
    suspend fun reassignSnapsFolder(oldFolder: String, fallbackFolder: String): Int
}
