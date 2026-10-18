package com.flowtask.app.data.repository

import com.flowtask.app.data.local.dao.InstantDao
import com.flowtask.app.data.local.entity.InstantFolderEntity
import com.flowtask.app.data.local.entity.InstantSnapEntity
import com.flowtask.app.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class InstantRepository(
    private val instantDao: InstantDao
) {
    val allSnaps: Flow<List<InstantSnapEntity>> = instantDao.getAllSnaps()

    val allFolders: Flow<List<InstantFolderEntity>> = instantDao.getAllFolders()

    fun getSnapsByFolder(folderName: String): Flow<List<InstantSnapEntity>> {
        return instantDao.getSnapsByFolder(folderName)
    }

    suspend fun saveSnap(
        imagePath: String,
        noteText: String?,
        folderName: String = "Unsorted"
    ): Long = withContext(Dispatchers.IO) {
        val snap = InstantSnapEntity(
            imagePath = imagePath,
            noteText = noteText?.trim()?.ifBlank { null },
            folderName = folderName.trim().ifBlank { "Unsorted" }
        )
        instantDao.insertSnap(snap)
    }

    suspend fun deleteSnap(snap: InstantSnapEntity) = withContext(Dispatchers.IO) {
        instantDao.deleteSnap(snap.id)
        ImageUtils.deleteFile(snap.imagePath)
    }

    suspend fun updateSnapFolder(snapId: Long, newFolder: String) = withContext(Dispatchers.IO) {
        instantDao.updateSnapFolder(snapId, newFolder.trim().ifBlank { "Unsorted" })
    }

    suspend fun updateSnapNote(snapId: Long, newNote: String?) = withContext(Dispatchers.IO) {
        instantDao.updateSnapNote(snapId, newNote?.trim()?.ifBlank { null })
    }

    suspend fun createFolder(name: String): Long = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            instantDao.insertFolder(InstantFolderEntity(name = trimmed))
        } else -1L
    }

    suspend fun deleteFolder(folder: InstantFolderEntity) = withContext(Dispatchers.IO) {
        instantDao.reassignSnapsFolder(oldFolder = folder.name, fallbackFolder = "Unsorted")
        instantDao.deleteFolder(folder.id)
    }
}
