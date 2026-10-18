package com.flowtask.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowtask.app.data.local.AppDatabase
import com.flowtask.app.data.local.entity.InstantFolderEntity
import com.flowtask.app.data.local.entity.InstantSnapEntity
import com.flowtask.app.data.repository.InstantRepository
import com.flowtask.app.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class InstantFolderWithFace(
    val name: String,
    val count: Int,
    val latestPhotoPath: String?,
    val isCustomFolder: Boolean
)

class InstantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InstantRepository = InstantRepository(
        AppDatabase.getInstance(application).instantDao()
    )

    val allSnaps: StateFlow<List<InstantSnapEntity>> = repository.allSnaps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFolders: StateFlow<List<InstantFolderEntity>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently opened folder in InstantsScreen (null = viewing folder grid)
    private val _currentOpenedFolder = MutableStateFlow<String?>(null)
    val currentOpenedFolder: StateFlow<String?> = _currentOpenedFolder.asStateFlow()

    // Dialog state for creating new folder
    private val _isCreateFolderDialogVisible = MutableStateFlow(false)
    val isCreateFolderDialogVisible: StateFlow<Boolean> = _isCreateFolderDialogVisible.asStateFlow()

    // Snaps belonging to the currently opened folder
    val snapsInCurrentFolder: StateFlow<List<InstantSnapEntity>> = combine(allSnaps, _currentOpenedFolder) { snaps, folder ->
        if (folder == null) emptyList() else snaps.filter { it.folderName == folder }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Folders computed with their latest face image and photo count
    val foldersWithFaces: StateFlow<List<InstantFolderWithFace>> = combine(allFolders, allSnaps) { folders: List<InstantFolderEntity>, snaps: List<InstantSnapEntity> ->
        val folderNames = mutableListOf<String>()
        // Custom folders first
        folders.forEach { f ->
            if (!folderNames.contains(f.name)) folderNames.add(f.name)
        }
        // "Unsorted" folder
        if (!folderNames.contains("Unsorted")) {
            folderNames.add("Unsorted")
        }
        // Any folder name referenced in snaps
        snaps.forEach { s ->
            if (!folderNames.contains(s.folderName)) {
                folderNames.add(s.folderName)
            }
        }

        folderNames.map { name ->
            val inFolder = snaps.filter { it.folderName == name }
            val latest = inFolder.maxByOrNull { it.createdAtMillis }?.imagePath
            val isCustom = name != "Unsorted"
            InstantFolderWithFace(
                name = name,
                count = inFolder.size,
                latestPhotoPath = latest,
                isCustomFolder = isCustom
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isCaptureSheetVisible = MutableStateFlow(false)
    val isCaptureSheetVisible: StateFlow<Boolean> = _isCaptureSheetVisible.asStateFlow()

    private val _pendingCapturePath = MutableStateFlow<String?>(null)
    val pendingCapturePath: StateFlow<String?> = _pendingCapturePath.asStateFlow()

    private var currentTempFile: File? = null

    fun openFolder(name: String) {
        _currentOpenedFolder.value = name
    }

    fun navigateUpFolder() {
        _currentOpenedFolder.value = null
    }

    fun showCreateFolderDialog() {
        _isCreateFolderDialogVisible.value = true
    }

    fun openCreateFolderDialog() {
        showCreateFolderDialog()
    }

    fun hideCreateFolderDialog() {
        _isCreateFolderDialogVisible.value = false
    }

    /**
     * Creates a new destination file and returns the File and its FileProvider Uri.
     */
    fun prepareTempCaptureUri(context: Context): Pair<File, Uri> {
        val file = ImageUtils.createInstantImageFile(context)
        currentTempFile = file
        val uri = ImageUtils.getUriForFile(context, file)
        return Pair(file, uri)
    }

    /**
     * Called after camera successfully took a photo.
     * Enforces true 4:3 aspect ratio crop and opens the note/sort sheet.
     */
    fun onPhotoCaptured() {
        val file = currentTempFile ?: return
        viewModelScope.launch(Dispatchers.IO) {
            ImageUtils.processAndCropToSquare(file)
            withContext(Dispatchers.Main) {
                _pendingCapturePath.value = file.absolutePath
                _isCaptureSheetVisible.value = true
            }
        }
    }

    /**
     * Called when a photo is chosen from gallery.
     * Copies and crops it to 4:3, then opens the note/sort sheet.
     */
    fun onGalleryPhotoSelected(context: Context, sourceUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = ImageUtils.createInstantImageFile(context)
            val success = ImageUtils.copyAndProcessUri(context, sourceUri, file)
            if (success) {
                withContext(Dispatchers.Main) {
                    currentTempFile = file
                    _pendingCapturePath.value = file.absolutePath
                    _isCaptureSheetVisible.value = true
                }
            }
        }
    }

    fun dismissCaptureSheet() {
        val path = _pendingCapturePath.value
        _isCaptureSheetVisible.value = false
        _pendingCapturePath.value = null
        currentTempFile = null

        // If discarded without saving, clean up the file
        if (path != null) {
            viewModelScope.launch(Dispatchers.IO) {
                ImageUtils.deleteFile(path)
            }
        }
    }

    fun saveSnap(noteText: String?, folderName: String) {
        val path = _pendingCapturePath.value ?: return
        viewModelScope.launch {
            repository.saveSnap(
                imagePath = path,
                noteText = noteText?.takeIf { it.isNotBlank() },
                folderName = folderName
            )
            _isCaptureSheetVisible.value = false
            _pendingCapturePath.value = null
            currentTempFile = null
        }
    }

    fun deleteSnap(snap: InstantSnapEntity) {
        viewModelScope.launch {
            repository.deleteSnap(snap)
        }
    }

    fun updateSnapFolder(snapId: Long, newFolder: String) {
        viewModelScope.launch {
            repository.updateSnapFolder(snapId, newFolder)
        }
    }

    fun updateSnapNote(snapId: Long, newNote: String?) {
        viewModelScope.launch {
            repository.updateSnapNote(snapId, newNote)
        }
    }

    fun createFolder(name: String, onCreated: ((String) -> Unit)? = null) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            repository.createFolder(trimmed)
            hideCreateFolderDialog()
            onCreated?.invoke(trimmed)
        }
    }

    fun deleteFolderByName(name: String) {
        viewModelScope.launch {
            val folderEntity = allFolders.value.find { it.name == name }
            if (folderEntity != null) {
                repository.deleteFolder(folderEntity)
            } else {
                // If it was only referenced in snaps, reassign those snaps to Unsorted
                val snapsInFolder = allSnaps.value.filter { it.folderName == name }
                snapsInFolder.forEach { snap ->
                    repository.updateSnapFolder(snap.id, "Unsorted")
                }
            }
            if (_currentOpenedFolder.value == name) {
                _currentOpenedFolder.value = null
            }
        }
    }
}
