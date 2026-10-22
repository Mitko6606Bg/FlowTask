package com.flowtask.app.ui.screens

import android.graphics.Bitmap
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.flowtask.app.data.local.entity.InstantSnapEntity
import com.flowtask.app.ui.components.AppDropdownMenu
import com.flowtask.app.ui.components.AppDropdownMenuItem
import com.flowtask.app.ui.components.FullScreenImageViewer
import com.flowtask.app.ui.components.NewInstantFolderDialog
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.DockBarColor
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLight
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.SurfaceLight
import com.flowtask.app.ui.theme.TextMuted
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.ui.viewmodel.InstantFolderWithFace
import com.flowtask.app.ui.viewmodel.InstantViewModel
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.ImageUtils
import com.flowtask.app.util.LocalAppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Screen state navigation hierarchy for smooth animated page transitions.
 */
sealed interface InstantScreenView {
    data object Albums : InstantScreenView
    data class Folder(val folderName: String) : InstantScreenView
    data class PhotoDetail(val snapId: Long, val folderName: String) : InstantScreenView
}

private fun viewLevel(view: InstantScreenView): Int = when (view) {
    is InstantScreenView.Albums -> 0
    is InstantScreenView.Folder -> 1
    is InstantScreenView.PhotoDetail -> 2
}

@Composable
fun InstantsScreen(
    viewModel: InstantViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current

    val allSnaps by viewModel.allSnaps.collectAsStateWithLifecycle()
    val foldersWithFaces by viewModel.foldersWithFaces.collectAsStateWithLifecycle()
    val isCreateFolderDialogVisible by viewModel.isCreateFolderDialogVisible.collectAsStateWithLifecycle()

    var currentView by remember { mutableStateOf<InstantScreenView>(InstantScreenView.Albums) }
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    var predictiveBackEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

    // Intercept back button for smooth native predictive navigation
    PredictiveBackHandler(enabled = currentView != InstantScreenView.Albums) { progress: Flow<BackEventCompat> ->
        try {
            progress.collect { backEvent ->
                predictiveBackProgress = backEvent.progress
                predictiveBackEdge = backEvent.swipeEdge
            }
            currentView = when (val state = currentView) {
                is InstantScreenView.PhotoDetail -> InstantScreenView.Folder(state.folderName)
                is InstantScreenView.Folder -> {
                    viewModel.navigateUpFolder()
                    InstantScreenView.Albums
                }
                is InstantScreenView.Albums -> InstantScreenView.Albums
            }
        } catch (_: CancellationException) {
        } finally {
            predictiveBackProgress = 0f
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        AnimatedContent(
            targetState = currentView,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val scale = 1f - (predictiveBackProgress * 0.08f)
                    scaleX = scale
                    scaleY = scale
                    translationX = if (predictiveBackEdge == BackEventCompat.EDGE_LEFT) {
                        predictiveBackProgress * 28.dp.toPx()
                    } else {
                        -predictiveBackProgress * 28.dp.toPx()
                    }
                    alpha = 1f - (predictiveBackProgress * 0.18f)
                },
            transitionSpec = {
                val goingForward = viewLevel(targetState) > viewLevel(initialState)
                if (goingForward) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width / 3 } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width / 3 } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "InstantsScreenTransition"
        ) { targetView ->
            when (targetView) {
                is InstantScreenView.Albums -> {
                    // -------------------------------------------------------------
                    // 1. ALBUMS ROOT PAGE (No enclosing container, pure clean fitment)
                    // -------------------------------------------------------------
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = strings.navInstants,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 26.sp,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = strings.totalSnapsCount(allSnaps.size),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                )
                            }

                            // "+ New Folder" Action Button
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.showCreateFolderDialog() },
                                shape = RoundedCornerShape(14.dp),
                                color = currentAccent.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, currentAccent.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreateNewFolder,
                                        contentDescription = strings.newPhotoFolderTitle,
                                        tint = currentAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.newFolderPrompt,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = currentAccent
                                        )
                                    )
                                }
                            }
                        }

                        if (allSnaps.isEmpty() && foldersWithFaces.all { it.count == 0 }) {
                            // Empty State
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(currentAccent.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            tint = currentAccent,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = strings.noSnapsTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = TextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = strings.noSnapsSubtitle,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        } else {
                            // 2-Column Square 1:1 Folder Albums Grid
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(22.dp),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    foldersWithFaces,
                                    key = { it.name },
                                    contentType = { "folder" }
                                ) { folder ->
                                    GooglePhotosFolderCard(
                                        folder = folder,
                                        onClick = {
                                            viewModel.openFolder(folder.name)
                                            currentView = InstantScreenView.Folder(folder.name)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                is InstantScreenView.Folder -> {
                    // -------------------------------------------------------------
                    // 2. FOLDER PHOTOS PAGE (Clean edge-to-edge 2-column square grid)
                    // -------------------------------------------------------------
                    val folderName = targetView.folderName
                    val displayFolderName = if (folderName == "Unsorted") strings.folderUnsorted else folderName
                    val snapsInFolder = allSnaps.filter { it.folderName == folderName }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        // Folder Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.navigateUpFolder()
                                    currentView = InstantScreenView.Albums
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = strings.closeAction,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = displayFolderName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = TextPrimary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = strings.folderPhotosCount(snapsInFolder.size),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                )
                            }
                        }

                        if (snapsInFolder.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(SurfaceContainerLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = strings.folderEmptyStateTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                }
                            }
                        } else {
                            // 2-Column Square 1:1 Photo Cards Grid
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    snapsInFolder,
                                    key = { it.id },
                                    contentType = { "snap" }
                                ) { snap ->
                                    InstantPhotoCard(
                                        snap = snap,
                                        onClick = {
                                            currentView = InstantScreenView.PhotoDetail(snap.id, folderName)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                is InstantScreenView.PhotoDetail -> {
                    // -------------------------------------------------------------
                    // 3. PHOTO VIEW PAGE (Dedicated page, image at top, card for note)
                    // -------------------------------------------------------------
                    val snap = allSnaps.find { it.id == targetView.snapId }
                    if (snap != null) {
                        PhotoDetailViewPage(
                            snap = snap,
                            folderName = targetView.folderName,
                            viewModel = viewModel,
                            onBack = {
                                currentView = InstantScreenView.Folder(targetView.folderName)
                            },
                            onDeleted = {
                                currentView = InstantScreenView.Folder(targetView.folderName)
                            }
                        )
                    } else {
                        // Fallback if snap was deleted
                        LaunchedEffect(Unit) {
                            currentView = InstantScreenView.Folder(targetView.folderName)
                        }
                    }
                }
            }
        }
    }

    // New Folder Card Pop-up Dialog
    if (isCreateFolderDialogVisible) {
        NewInstantFolderDialog(
            onDismiss = { viewModel.hideCreateFolderDialog() },
            onConfirm = { name ->
                viewModel.createFolder(name)
            }
        )
    }
}

// ---------------------------------------------------------------------------
// 1:1 Square Google Photos Folder Card (Title & count directly underneath)
// ---------------------------------------------------------------------------
@Composable
private fun GooglePhotosFolderCard(
    folder: InstantFolderWithFace,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    var faceBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(folder.latestPhotoPath) {
        if (folder.latestPhotoPath != null) {
            withContext(Dispatchers.IO) {
                val bmp = ImageUtils.loadSampledBitmap(folder.latestPhotoPath, reqWidth = 400, reqHeight = 400)
                withContext(Dispatchers.Main) {
                    faceBitmap = bmp
                }
            }
        } else {
            faceBitmap = null
        }
    }

    val displayTitle = if (folder.name == "Unsorted") strings.folderUnsorted else folder.name

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .expressiveBounce(pressScale = 0.96f)
            .clickable(onClick = onClick)
    ) {
        // 1:1 Square squircle container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            color = SurfaceContainerLow,
            border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f)),
            shadowElevation = 1.dp
        ) {
            if (faceBitmap != null) {
                Image(
                    bitmap = faceBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title and item count directly below the square
        Text(
            text = displayTitle,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TextPrimary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = strings.folderPhotosCount(folder.count),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                color = TextSecondary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------------------------------------------------------------------------
// 1:1 Square Photo Card inside opened folder
// ---------------------------------------------------------------------------
@Composable
private fun InstantPhotoCard(
    snap: InstantSnapEntity,
    onClick: () -> Unit
) {
    var thumbnailBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(snap.imagePath) {
        withContext(Dispatchers.IO) {
            val bmp = ImageUtils.loadSampledBitmap(snap.imagePath, reqWidth = 400, reqHeight = 400)
            withContext(Dispatchers.Main) {
                thumbnailBitmap = bmp
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .expressiveBounce(pressScale = 0.96f)
            .clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            color = SurfaceContainerLow,
            border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f)),
            shadowElevation = 1.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (thumbnailBitmap != null) {
                    Image(
                        bitmap = thumbnailBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Note badge indicator if snap has a text note
                if (!snap.noteText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        if (!snap.noteText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = snap.noteText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = TextSecondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Proper Photo Detail View Page (Image at top, Card container for note, clean app theme)
// ---------------------------------------------------------------------------
@Composable
private fun PhotoDetailViewPage(
    snap: InstantSnapEntity,
    folderName: String,
    viewModel: InstantViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val allFolders by viewModel.allFolders.collectAsStateWithLifecycle()

    var fullBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isEditingNote by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf(snap.noteText ?: "") }
    var showMoveMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isFullScreenViewerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(snap.imagePath) {
        withContext(Dispatchers.IO) {
            val bmp = ImageUtils.loadSampledBitmap(snap.imagePath, reqWidth = 1000, reqHeight = 1000)
            withContext(Dispatchers.Main) {
                fullBitmap = bmp
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp)
    ) {
        // Navigation Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .expressiveBounce(pressScale = 0.90f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.closeAction,
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Folder Badge Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = currentAccent.copy(alpha = 0.12f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = if (snap.folderName == "Unsorted") strings.folderUnsorted else snap.folderName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = currentAccent,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = DateTimeUtils.formatDate(snap.createdAtMillis),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )
            }

            // Quick Actions: Move to folder & Delete photo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    IconButton(
                        onClick = { showMoveMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .expressiveBounce(pressScale = 0.90f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                            contentDescription = strings.moveFolderAction,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    AppDropdownMenu(
                        expanded = showMoveMenu,
                        onDismissRequest = { showMoveMenu = false }
                    ) {
                        AppDropdownMenuItem(
                            text = { Text(strings.folderUnsorted) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                viewModel.updateSnapFolder(snap.id, "Unsorted")
                                showMoveMenu = false
                            }
                        )
                        allFolders.forEach { folder ->
                            AppDropdownMenuItem(
                                text = { Text(folder.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    viewModel.updateSnapFolder(snap.id, folder.name)
                                    showMoveMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .expressiveBounce(pressScale = 0.90f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = strings.deleteSnapAction,
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 1:1 Square Image Container at the Top (zero black bars, clickable for full screen zoom)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .expressiveBounce(pressScale = 0.985f)
                .clip(RoundedCornerShape(28.dp))
                .clickable { isFullScreenViewerOpen = true },
            shape = RoundedCornerShape(28.dp),
            color = SurfaceContainerLowest,
            border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f)),
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (fullBitmap != null) {
                    Image(
                        bitmap = fullBitmap!!.asImageBitmap(),
                        contentDescription = strings.fullScreenPhotoTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Zoom / Full screen pill indicator in bottom-right corner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = strings.zoomHint,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.zoomHint,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = currentAccent, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Card Style Container for Note Part (matching the rest of the app)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceContainerLowest,
            border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f)),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Card Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(currentAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NoteAlt,
                                contentDescription = null,
                                tint = currentAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.editNoteAction,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                        )
                    }

                    if (!isEditingNote) {
                        IconButton(
                            onClick = { isEditingNote = true },
                            modifier = Modifier
                                .size(32.dp)
                                .expressiveBounce(pressScale = 0.88f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = strings.editAction,
                                tint = currentAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card Body: Note content / editor
                if (isEditingNote) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = {
                            Text(
                                text = strings.addPhotoNotePlaceholder,
                                fontSize = 14.sp,
                                color = TextMuted
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedContainerColor = SurfaceContainerLow,
                            focusedBorderColor = currentAccent,
                            unfocusedBorderColor = OutlineSubtle.copy(alpha = 0.7f)
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                noteText = snap.noteText ?: ""
                                isEditingNote = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f)),
                            modifier = Modifier.expressiveBounce(pressScale = 0.94f)
                        ) {
                            Text(strings.cancelAction, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                viewModel.updateSnapNote(snap.id, noteText.trim().ifEmpty { null })
                                isEditingNote = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DockBarColor,
                                contentColor = TextOnAccent
                            ),
                            modifier = Modifier.expressiveBounce(pressScale = 0.94f)
                        ) {
                            Text(strings.saveAction, color = TextOnAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .expressiveBounce(pressScale = 0.98f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { isEditingNote = true },
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceContainerLow,
                        border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
                    ) {
                        val hasNote = !snap.noteText.isNullOrBlank()
                        Text(
                            text = if (hasNote) snap.noteText!! else strings.addPhotoNotePlaceholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                color = if (hasNote) TextPrimary else TextMuted
                            ),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }

        // Room at bottom for floating navigation hub
        Spacer(modifier = Modifier.height(100.dp))
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        Dialog(onDismissRequest = { showDeleteConfirmDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = SurfaceContainerLowest,
                border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f)),
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = strings.deleteSnapAction,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Сигурни ли сте, че искате да изтриете тази снимка?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteConfirmDialog = false },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f)),
                            modifier = Modifier.expressiveBounce(pressScale = 0.94f)
                        ) {
                            Text(strings.cancelAction, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                viewModel.deleteSnap(snap)
                                onDeleted()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            modifier = Modifier.expressiveBounce(pressScale = 0.94f)
                        ) {
                            Text(strings.deleteAction, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Full Screen Zoomable Image Viewer
    if (isFullScreenViewerOpen) {
        FullScreenImageViewer(
            snap = snap,
            initialBitmap = fullBitmap,
            onDismiss = { isFullScreenViewerOpen = false }
        )
    }
}
