package com.flowtask.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLight
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.SurfaceLight
import com.flowtask.app.ui.theme.TextMuted
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.ui.viewmodel.InstantFolderWithFace
import com.flowtask.app.ui.viewmodel.InstantViewModel
import com.flowtask.app.util.ImageUtils
import com.flowtask.app.util.LocalAppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private sealed interface FolderItemType {
    object NewFolder : FolderItemType
    data class Folder(val folderWithFace: InstantFolderWithFace) : FolderItemType
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstantCaptureBottomSheet(
    viewModel: InstantViewModel,
    imagePath: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val foldersWithFaces by viewModel.foldersWithFaces.collectAsState()

    var noteText by remember { mutableStateOf("") }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imagePath) {
        withContext(Dispatchers.IO) {
            val bmp = ImageUtils.loadSampledBitmap(imagePath, reqWidth = 600, reqHeight = 800)
            withContext(Dispatchers.Main) {
                previewBitmap = bmp
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
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
                            .background(currentAccent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = currentAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.instantsTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.expressiveBounce(pressScale = 0.88f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = strings.closeAction, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1:1 Square Photo Preview Card
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.68f)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(26.dp),
                    color = SurfaceContainerLow,
                    border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f)),
                    shadowElevation = 2.dp
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Optional Note / Thoughts Input
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = {
                    Text(
                        text = strings.addPhotoNotePlaceholder,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted, fontSize = 14.sp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceContainerLow,
                    unfocusedContainerColor = SurfaceContainerLow,
                    focusedBorderColor = currentAccent,
                    unfocusedBorderColor = OutlineSubtle.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Section Header: Select Folder To Save
            Text(
                text = strings.tapFolderToSave,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = strings.tapFolderToSaveSubtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2-Column Vertical Grid of Square Rounded Folders (Google Photos Style)
            val allFolderItems = remember(foldersWithFaces) {
                listOf(FolderItemType.NewFolder) + foldersWithFaces.map { FolderItemType.Folder(it) }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                allFolderItems.chunked(2).forEach { rowPair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowPair.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                when (item) {
                                    is FolderItemType.NewFolder -> {
                                        NewFolderCardItem(
                                            onClick = { showNewFolderDialog = true }
                                        )
                                    }
                                    is FolderItemType.Folder -> {
                                        SaveToFolderCardItem(
                                            folder = item.folderWithFace,
                                            onClick = {
                                                viewModel.saveSnap(noteText, item.folderWithFace.name)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        if (rowPair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showNewFolderDialog) {
        NewInstantFolderDialog(
            onDismiss = { showNewFolderDialog = false },
            onConfirm = { newFolderName ->
                showNewFolderDialog = false
                viewModel.createFolder(newFolderName) { createdName ->
                    viewModel.saveSnap(noteText, createdName)
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Google Photos Style Square Rounded Folder Card Item for Selection
// ---------------------------------------------------------------------------
@Composable
private fun SaveToFolderCardItem(
    folder: InstantFolderWithFace,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    var faceBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(folder.latestPhotoPath) {
        if (folder.latestPhotoPath != null) {
            withContext(Dispatchers.IO) {
                val bmp = ImageUtils.loadSampledBitmap(folder.latestPhotoPath, reqWidth = 360, reqHeight = 360)
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
        // Square rounded container
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
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title & items count directly below the square
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
// "+ New Folder" Square Rounded Card Item
// ---------------------------------------------------------------------------
@Composable
private fun NewFolderCardItem(
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current

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
            color = currentAccent.copy(alpha = 0.08f),
            border = BorderStroke(1.5.dp, currentAccent.copy(alpha = 0.35f)),
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = strings.newFolderPrompt,
                    tint = currentAccent,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = strings.newFolderPrompt,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = currentAccent
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = strings.createFolderAction,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                color = TextSecondary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
