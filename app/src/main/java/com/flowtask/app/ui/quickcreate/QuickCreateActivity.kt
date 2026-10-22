package com.flowtask.app.ui.quickcreate

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.flowtask.app.data.local.AppDatabase
import com.flowtask.app.data.local.entity.InstantFolderEntity
import com.flowtask.app.data.local.entity.InstantSnapEntity
import com.flowtask.app.data.local.entity.SubTaskEntity
import com.flowtask.app.data.local.entity.TaskEntity
import com.flowtask.app.data.repository.SettingsRepository
import com.flowtask.app.reminder.TaskReminderScheduler
import com.flowtask.app.ui.theme.DockBarColor
import com.flowtask.app.ui.theme.FlowTaskTheme
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.TextMuted
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.ImageUtils
import com.flowtask.app.util.LocalAppStrings
import com.flowtask.app.widget.TodayWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class QuickCreateTab {
    TASK, INSTANT
}

class QuickCreateActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.32f)

        val settingsRepo = SettingsRepository(applicationContext)

        setContent {
            val language by settingsRepo.language.collectAsStateWithLifecycle(initialValue = "en")
            val accentColor by settingsRepo.accentColor.collectAsStateWithLifecycle(initialValue = "#1D7868")
            val themeMode by settingsRepo.themeMode.collectAsStateWithLifecycle(initialValue = "system")

            FlowTaskTheme(
                language = language,
                accentColorHex = accentColor,
                themeMode = themeMode,
                updateWindowBackground = false // Preserves native window translucency over homescreen
            ) {
                QuickCreateFloatingCardHost(
                    onDismiss = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCreateFloatingCardHost(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isCardVisible by remember { mutableStateOf(false) }
    var isDismissing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isCardVisible = true
    }

    val animatedDismiss: () -> Unit = {
        if (!isDismissing) {
            isDismissing = true
            keyboardController?.hide()
            isCardVisible = false
            val hostActivity = generateSequence(context) { (it as? ContextWrapper)?.baseContext }
                .filterIsInstance<Activity>()
                .firstOrNull() ?: (context as? Activity)
            hostActivity?.window?.let { window ->
                try {
                    android.animation.ValueAnimator.ofFloat(0.32f, 0f).apply {
                        duration = 200
                        addUpdateListener { animator ->
                            window.setDimAmount(animator.animatedValue as Float)
                        }
                        start()
                    }
                } catch (_: Exception) {
                    // Safe guard
                }
            }
            coroutineScope.launch {
                delay(200)
                onDismiss()
            }
        }
    }

    var quickCreateBackProgress by remember { mutableFloatStateOf(0f) }
    var quickCreateBackEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

    PredictiveBackHandler { progress: Flow<BackEventCompat> ->
        try {
            progress.collect { backEvent ->
                quickCreateBackProgress = backEvent.progress
                quickCreateBackEdge = backEvent.swipeEdge
            }
            animatedDismiss()
        } catch (_: CancellationException) {
        } finally {
            quickCreateBackProgress = 0f
        }
    }

    var currentTab by remember { mutableStateOf(QuickCreateTab.TASK) }

    // Database
    val db = remember { AppDatabase.getInstance(context) }
    val semesterRepo = remember { com.flowtask.app.data.repository.SemesterRepository(context) }
    val rawLayers by db.taskDao().getDistinctLayersFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val programs by semesterRepo.layerPrograms.collectAsStateWithLifecycle(initialValue = emptyList())
    val availableLayers = remember(rawLayers, programs) {
        val set = linkedSetOf<String>()
        set.addAll(rawLayers.map { it.trim() }.filter { it.isNotBlank() })
        for (p in programs) {
            if (p.layerName.isNotBlank()) {
                set.add(p.layerName.trim())
            }
        }
        set.toList().sorted()
    }
    val availableFolders by db.instantDao().getAllFolders().collectAsStateWithLifecycle(initialValue = emptyList<InstantFolderEntity>())

    // Task Form State
    var taskText by remember { mutableStateOf("") }
    var taskDueDateMillis by remember { mutableStateOf<Long?>(DateTimeUtils.getStartOfDay()) }
    var taskDueTimeMinutes by remember { mutableStateOf<Int?>(null) }
    var taskLayer by remember { mutableStateOf<String?>(null) }
    val taskSubtasks = remember { mutableStateListOf<String>() }
    var newSubtaskInput by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showLayersExpanded by remember { mutableStateOf(false) }
    var showSubtasksExpanded by remember { mutableStateOf(false) }
    var isAddingNewLayer by remember { mutableStateOf(false) }
    var newLayerInput by remember { mutableStateOf("") }

    // Instant Photo State
    var instantNote by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf("Unsorted") }
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }
    var tempCaptureFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCaptureFile != null) {
            coroutineScope.launch(Dispatchers.IO) {
                ImageUtils.processAndCropToSquare(tempCaptureFile!!)
                withContext(Dispatchers.Main) {
                    capturedPhotoPath = tempCaptureFile!!.absolutePath
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = ImageUtils.createInstantImageFile(context)
            tempCaptureFile = file
            val uri = ImageUtils.getUriForFile(context, file)
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val file = ImageUtils.createInstantImageFile(context)
            tempCaptureFile = file
            val uri = ImageUtils.getUriForFile(context, file)
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Auto-focus requester for immediate keyboard presentation
    val taskFocusRequester = remember { FocusRequester() }

    LaunchedEffect(currentTab) {
        try {
            if (currentTab == QuickCreateTab.TASK) {
                delay(100)
                taskFocusRequester.requestFocus()
            } else {
                keyboardController?.hide()
            }
        } catch (_: Exception) {
            // Focus safety
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Dismiss backdrop: tapping anywhere on the visible home screen above the card dismisses it immediately
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = animatedDismiss
                )
        )

        // Floating Card anchored directly above the keyboard / navigation bar
        AnimatedVisibility(
            visible = isCardVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                    )
                ),
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = 380f
                )
            ) + fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 200)
            ) + fadeOut(animationSpec = tween(durationMillis = 150))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        if (quickCreateBackProgress > 0f) {
                            val scale = 1f - (quickCreateBackProgress * 0.08f)
                            scaleX = scale
                            scaleY = scale
                            translationX = if (quickCreateBackEdge == BackEventCompat.EDGE_LEFT) {
                                quickCreateBackProgress * 28.dp.toPx()
                            } else {
                                -quickCreateBackProgress * 28.dp.toPx()
                            }
                            alpha = 1f - (quickCreateBackProgress * 0.22f)
                        }
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Intercept taps on the card body so they don't fall through to the dismiss backdrop
                    ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = SurfaceContainerLowest,
                border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.5f)),
                shadowElevation = 8.dp
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Mode Selector Bar & Dismiss
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mode Pills
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceContainerLow)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        CompactModePill(
                            icon = Icons.Default.TaskAlt,
                            label = strings.navToday,
                            isSelected = currentTab == QuickCreateTab.TASK,
                            activeColor = currentAccent,
                            onClick = { currentTab = QuickCreateTab.TASK }
                        )

                        CompactModePill(
                            icon = Icons.Default.CameraAlt,
                            label = strings.navInstants,
                            isSelected = currentTab == QuickCreateTab.INSTANT,
                            activeColor = currentAccent,
                            onClick = { currentTab = QuickCreateTab.INSTANT }
                        )
                    }

                    // Close Button
                    IconButton(
                        onClick = animatedDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.closeAction,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                when (currentTab) {
                    // ========================================================
                    // 1. Task Mode (Matches Google Tasks in User Screenshot)
                    // ========================================================
                    QuickCreateTab.TASK -> {
                        // "New task" clean input field
                        BasicTextField(
                            value = taskText,
                            onValueChange = { taskText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                                .focusRequester(taskFocusRequester),
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextPrimary
                            ),
                            cursorBrush = SolidColor(currentAccent),
                            maxLines = 4,
                            decorationBox = { innerTextField ->
                                if (taskText.isEmpty()) {
                                    Text(
                                        text = strings.taskExplanationPlaceholder,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = TextMuted
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )

                        // Active chips row (shown when properties are attached)
                        val hasActiveChips = taskDueDateMillis != null || taskDueTimeMinutes != null || taskLayer != null || taskSubtasks.isNotEmpty()
                        if (hasActiveChips) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Date / Time Chip
                                if (taskDueDateMillis != null) {
                                    ActivePropChip(
                                        icon = Icons.Default.CalendarToday,
                                        text = DateTimeUtils.formatDateLabel(taskDueDateMillis!!) +
                                                (taskDueTimeMinutes?.let { ", ${DateTimeUtils.formatTime(it)}" } ?: ""),
                                        color = currentAccent,
                                        onClear = {
                                            taskDueDateMillis = null
                                            taskDueTimeMinutes = null
                                        },
                                        onClick = { showDatePicker = true }
                                    )
                                }

                                // Layer Chip
                                if (taskLayer != null) {
                                    ActivePropChip(
                                        icon = Icons.Default.Layers,
                                        text = taskLayer!!,
                                        color = currentAccent,
                                        onClear = { taskLayer = null },
                                        onClick = { showLayersExpanded = !showLayersExpanded }
                                    )
                                }

                                // Subtasks Count Chip
                                if (taskSubtasks.isNotEmpty()) {
                                    ActivePropChip(
                                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                                        text = strings.subtasksHeader(taskSubtasks.size),
                                        color = currentAccent,
                                        onClear = { taskSubtasks.clear() },
                                        onClick = { showSubtasksExpanded = !showSubtasksExpanded }
                                    )
                                }
                            }
                        }

                        // Expandable Layer Picker Panel
                        AnimatedVisibility(
                            visible = showLayersExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SurfaceContainerLow)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = strings.layerSectionTitle,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { taskLayer = null }
                                            .expressiveBounce(pressScale = 0.92f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (taskLayer == null) currentAccent.copy(alpha = 0.15f) else Color.Transparent,
                                        border = BorderStroke(1.dp, if (taskLayer == null) currentAccent else OutlineSubtle.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = strings.noLayer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (taskLayer == null) currentAccent else TextPrimary
                                            )
                                        )
                                    }

                                    availableLayers.forEach { layer ->
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { taskLayer = layer }
                                                .expressiveBounce(pressScale = 0.92f),
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (taskLayer == layer) currentAccent.copy(alpha = 0.15f) else Color.Transparent,
                                            border = BorderStroke(1.dp, if (taskLayer == layer) currentAccent else OutlineSubtle.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = layer,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (taskLayer == layer) currentAccent else TextPrimary
                                                )
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { isAddingNewLayer = !isAddingNewLayer }
                                            .expressiveBounce(pressScale = 0.92f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.Transparent,
                                        border = BorderStroke(1.dp, currentAccent.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = currentAccent, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(strings.addLayerPrompt, style = MaterialTheme.typography.labelSmall.copy(color = currentAccent, fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }

                                if (isAddingNewLayer) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = newLayerInput,
                                            onValueChange = { newLayerInput = it },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text(strings.newLayerPlaceholder, fontSize = 12.sp) },
                                            shape = RoundedCornerShape(10.dp),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = {
                                                if (newLayerInput.isNotBlank()) {
                                                    taskLayer = newLayerInput.trim()
                                                    newLayerInput = ""
                                                    isAddingNewLayer = false
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = currentAccent,
                                                contentColor = TextOnAccent
                                            )
                                        ) {
                                            Text(strings.saveAction, color = TextOnAccent, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Expandable Subtasks Panel
                        AnimatedVisibility(
                            visible = showSubtasksExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SurfaceContainerLow)
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = newSubtaskInput,
                                        onValueChange = { newSubtaskInput = it },
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text(strings.addSubtaskPlaceholder, fontSize = 12.sp) },
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            if (newSubtaskInput.isNotBlank()) {
                                                taskSubtasks.add(newSubtaskInput.trim())
                                                newSubtaskInput = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(currentAccent)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = TextOnAccent, modifier = Modifier.size(18.dp))
                                    }
                                }

                                if (taskSubtasks.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    taskSubtasks.forEachIndexed { idx, sub ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 1.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SurfaceContainerLowest)
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("• $sub", fontSize = 12.sp, color = TextPrimary)
                                            IconButton(
                                                onClick = { taskSubtasks.removeAt(idx) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Toolbar: Details, Date/Time, Layer, and Save Button (Exactly like Google Tasks)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // [ ☰ ] Details / Subtasks Button
                                IconButton(
                                    onClick = { showSubtasksExpanded = !showSubtasksExpanded },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                        contentDescription = "Subtasks",
                                        tint = if (taskSubtasks.isNotEmpty() || showSubtasksExpanded) currentAccent else TextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // [ 🕒 ] Date & Time Button
                                IconButton(
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Date and Time",
                                        tint = if (taskDueDateMillis != null || taskDueTimeMinutes != null) currentAccent else TextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // [ 🏷️ ] Layer Selector Button
                                IconButton(
                                    onClick = { showLayersExpanded = !showLayersExpanded },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = "Layers",
                                        tint = if (taskLayer != null || showLayersExpanded) currentAccent else TextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // Save Button on Right (like Google Tasks)
                            TextButton(
                                onClick = {
                                    if (taskText.isNotBlank()) {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val task = TaskEntity(
                                                explanation = taskText.trim(),
                                                dueDateMillis = taskDueDateMillis,
                                                dueTimeMinutes = taskDueTimeMinutes,
                                                layer = taskLayer?.trim()?.ifBlank { null },
                                                isCompleted = false,
                                                sortOrder = 0
                                            )
                                            val taskId = db.taskDao().insertTask(task)
                                            if (taskSubtasks.isNotEmpty()) {
                                                val subs = taskSubtasks.mapIndexed { idx, explanation ->
                                                    SubTaskEntity(
                                                        taskId = taskId,
                                                        explanation = explanation,
                                                        isCompleted = false,
                                                        sortOrder = idx
                                                    )
                                                }
                                                db.taskDao().insertSubTasks(subs)
                                            }
                                            if (task.dueTimeMinutes != null) {
                                                val taskWithSubs = db.taskDao().getTaskById(taskId)
                                                if (taskWithSubs != null) {
                                                    TaskReminderScheduler.scheduleOrShowReminder(context, taskWithSubs)
                                                }
                                            }
                                            TodayWidgetProvider.updateAllWidgets(context)
                                            withContext(Dispatchers.Main) {
                                                animatedDismiss()
                                            }
                                        }
                                    }
                                },
                                enabled = taskText.isNotBlank()
                            ) {
                                Text(
                                    text = strings.saveAction,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (taskText.isNotBlank()) currentAccent else TextMuted
                                    )
                                )
                            }
                        }
                    }

                    // ========================================================
                    // 2. Instant Photo Mode
                    // ========================================================
                    QuickCreateTab.INSTANT -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { launchCamera() }
                                        .expressiveBounce(pressScale = 0.94f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = currentAccent.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, currentAccent.copy(alpha = 0.3f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = currentAccent,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    BasicTextField(
                                        value = instantNote,
                                        onValueChange = { instantNote = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = TextPrimary),
                                        cursorBrush = SolidColor(currentAccent),
                                        singleLine = true,
                                        decorationBox = { innerTextField ->
                                            if (instantNote.isEmpty()) {
                                                Text(
                                                    text = strings.addPhotoNotePlaceholder,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = TextMuted)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = if (capturedPhotoPath != null) "Photo Ready ✓" else strings.snapPhotoAction,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (capturedPhotoPath != null) currentAccent else TextMuted,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Folder Selector & Save Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { selectedFolder = "Unsorted" }
                                            .expressiveBounce(pressScale = 0.92f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (selectedFolder == "Unsorted") currentAccent.copy(alpha = 0.15f) else Color.Transparent,
                                        border = BorderStroke(1.dp, if (selectedFolder == "Unsorted") currentAccent else OutlineSubtle.copy(alpha = 0.5f))
                                    ) {
                                        Text(strings.folderUnsorted, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = TextPrimary)
                                    }

                                    availableFolders.forEach { folder ->
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedFolder = folder.name }
                                                .expressiveBounce(pressScale = 0.92f),
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (selectedFolder == folder.name) currentAccent.copy(alpha = 0.15f) else Color.Transparent,
                                            border = BorderStroke(1.dp, if (selectedFolder == folder.name) currentAccent else OutlineSubtle.copy(alpha = 0.5f))
                                        ) {
                                            Text(folder.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = TextPrimary)
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        if (capturedPhotoPath != null) {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                val snap = InstantSnapEntity(
                                                    imagePath = capturedPhotoPath!!,
                                                    noteText = instantNote.trim().ifBlank { null },
                                                    folderName = selectedFolder,
                                                    createdAtMillis = System.currentTimeMillis()
                                                )
                                                db.instantDao().insertSnap(snap)
                                                TodayWidgetProvider.updateAllWidgets(context)
                                                withContext(Dispatchers.Main) {
                                                    animatedDismiss()
                                                }
                                            }
                                        }
                                    },
                                    enabled = capturedPhotoPath != null
                                ) {
                                    Text(
                                        text = strings.saveAction,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (capturedPhotoPath != null) currentAccent else TextMuted
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = taskDueDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskDueDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                        showTimePicker = true // Prompt time after date
                    }
                ) {
                    Text(strings.saveAction, color = currentAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        taskDueDateMillis = null
                        showDatePicker = false
                    }
                ) {
                    Text(strings.cancelAction, color = TextMuted)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val initialHour = taskDueTimeMinutes?.let { it / 60 } ?: 12
        val initialMinute = taskDueTimeMinutes?.let { it % 60 } ?: 0
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )

        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfaceContainerLowest,
                border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.timeOptional,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TimePicker(state = timePickerState)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                taskDueTimeMinutes = null
                                showTimePicker = false
                            }
                        ) {
                            Text(strings.cancelAction, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                taskDueTimeMinutes = timePickerState.hour * 60 + timePickerState.minute
                                showTimePicker = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentAccent,
                                contentColor = TextOnAccent
                            )
                        ) {
                            Text(strings.saveAction, color = TextOnAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactModePill(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .expressiveBounce(pressScale = 0.92f),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) activeColor else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) TextOnAccent else TextSecondary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp,
                    color = if (isSelected) TextOnAccent else TextSecondary
                )
            )
        }
    }
}

@Composable
fun ActivePropChip(
    icon: ImageVector,
    text: String,
    color: Color,
    onClear: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .expressiveBounce(pressScale = 0.94f),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = color.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(12.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClear
                    )
            )
        }
    }
}
