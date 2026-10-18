package com.flowtask.app.ui.screens

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import com.flowtask.app.data.local.model.SemesterConfig
import com.flowtask.app.ui.components.dialogs.M3CardDatePickerDialog
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.SemesterUtils
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.ui.theme.AccentPrimary
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.DynamicColorStyle
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.LocalDarkTheme
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
import com.flowtask.app.ui.theme.transformDynamicScheme
import com.flowtask.app.ui.viewmodel.SettingsViewModel
import com.flowtask.app.ui.viewmodel.TaskViewModel
import com.flowtask.app.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    viewModel: SettingsViewModel,
    taskViewModel: TaskViewModel? = null,
    onOpenSemesterSettings: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current

    val currentLanguage by viewModel.language.collectAsState()
    val currentAccentHex by viewModel.accentColor.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val isAmoled by viewModel.amoledDarkMode.collectAsState()
    val dynamicColorStyle by viewModel.dynamicColorStyle.collectAsState()
    val isDark = LocalDarkTheme.current
    val screenOrder by viewModel.screenOrder.collectAsState()
    val currentProgramTasksEverywhere by viewModel.programTasksEverywhere.collectAsState()
    val semesterConfig by (taskViewModel?.semesterConfig ?: remember { MutableStateFlow(SemesterConfig()) }).collectAsState()
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val colors = listOf(
        Pair("Sage Teal", "#2A8575"),
        Pair("Indigo Focus", "#6366F1"),
        Pair("Ocean Blue", "#2563EB"),
        Pair("Coral Rose", "#E11D48"),
        Pair("Sunset Amber", "#D97706"),
        Pair("Modern Purple", "#9333EA"),
        Pair("Slate Minimal", "#475569")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        // Fixed Header pinned at top: directly draggable, always visible, zero scroll interference
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.settingsTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextPrimary
                )
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = strings.closeAction, tint = TextSecondary)
            }
        }

        // Disable overscroll so drag gestures at boundary hand off seamlessly to sheet dismiss
        @Suppress("DEPRECATION")
        @OptIn(ExperimentalFoundationApi::class)
        CompositionLocalProvider(
            LocalOverscrollConfiguration provides null,
            LocalOverscrollFactory provides null
        ) {
            val lazyListState = rememberLazyListState()
            var wasScrolledWhenFlingStarted by remember { mutableStateOf(false) }

            val nestedScrollConnection = remember(sheetState, lazyListState) {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        if (lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0) {
                            wasScrolledWhenFlingStarted = false
                        }
                        return Offset.Zero
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        wasScrolledWhenFlingStarted = lazyListState.firstVisibleItemIndex > 0 ||
                                lazyListState.firstVisibleItemScrollOffset > 0
                        return Velocity.Zero
                    }

                    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                        // Consume upward fling velocity when sheet is expanded to prevent the bounce loop
                        if (available.y < 0) {
                            return available
                        }
                        // Consume downward fling velocity if it originated from scrolling within the list,
                        // so flinging to the top doesn't slam the sheet into dismissal or jitter.
                        if (wasScrolledWhenFlingStarted) {
                            wasScrolledWhenFlingStarted = false
                            return available
                        }
                        return Velocity.Zero
                    }
                }
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .nestedScroll(nestedScrollConnection),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item(key = "section_language") {
                    // SECTION 1: Language (Bilingual BG / EN)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = SurfaceContainerLow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = currentAccent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.languageSection,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Bulgarian Option
                                val isBg = currentLanguage == "bg"
                                SegmentedButton(
                                    selected = isBg,
                                    onClick = { viewModel.setLanguage("bg") },
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = currentAccent.copy(alpha = 0.15f),
                                        activeContentColor = currentAccent,
                                        activeBorderColor = currentAccent,
                                        inactiveContainerColor = SurfaceContainerLowest,
                                        inactiveContentColor = TextSecondary,
                                        inactiveBorderColor = OutlineSubtle.copy(alpha = 0.7f)
                                    ),
                                    border = SegmentedButtonDefaults.borderStroke(
                                        color = if (isBg) currentAccent else OutlineSubtle.copy(alpha = 0.7f),
                                        width = 1.dp
                                    ),
                                    icon = {
                                        SegmentedButtonDefaults.Icon(active = isBg)
                                    },
                                    label = {
                                        Text(
                                            text = strings.languageBulgarian,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isBg) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isBg) TextPrimary else TextSecondary,
                                                fontSize = 14.sp
                                            )
                                        )
                                    },
                                    modifier = Modifier.expressiveBounce(pressScale = 0.95f)
                                )

                                // English Option
                                val isEn = currentLanguage == "en"
                                SegmentedButton(
                                    selected = isEn,
                                    onClick = { viewModel.setLanguage("en") },
                                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = currentAccent.copy(alpha = 0.15f),
                                        activeContentColor = currentAccent,
                                        activeBorderColor = currentAccent,
                                        inactiveContainerColor = SurfaceContainerLowest,
                                        inactiveContentColor = TextSecondary,
                                        inactiveBorderColor = OutlineSubtle.copy(alpha = 0.7f)
                                    ),
                                    border = SegmentedButtonDefaults.borderStroke(
                                        color = if (isEn) currentAccent else OutlineSubtle.copy(alpha = 0.7f),
                                        width = 1.dp
                                    ),
                                    icon = {
                                        SegmentedButtonDefaults.Icon(active = isEn)
                                    },
                                    label = {
                                        Text(
                                            text = strings.languageEnglish,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isEn) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isEn) TextPrimary else TextSecondary,
                                                fontSize = 14.sp
                                            )
                                        )
                                    },
                                    modifier = Modifier.expressiveBounce(pressScale = 0.95f)
                                )
                            }
                        }
                    }
                }

        item(key = "section_appearance") {
            // SECTION 2: Accent Color & Material You
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceContainerLow,
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = currentAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.appearanceSection,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Theme Mode Selector (System / Light / Dark)
                    Text(
                        text = strings.darkModeTitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.darkModeDesc,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val themeModes = listOf(
                        Triple("system", strings.darkModeSystem, Icons.Default.BrightnessAuto),
                        Triple("light", strings.darkModeLight, Icons.Default.LightMode),
                        Triple("dark", strings.darkModeDark, Icons.Default.DarkMode)
                    )

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        themeModes.forEachIndexed { index, (modeKey, label, icon) ->
                            val isSelected = currentThemeMode == modeKey
                            SegmentedButton(
                                selected = isSelected,
                                onClick = { viewModel.setThemeMode(modeKey) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = themeModes.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = currentAccent.copy(alpha = 0.15f),
                                    activeContentColor = currentAccent,
                                    activeBorderColor = currentAccent,
                                    inactiveContainerColor = SurfaceContainerLowest,
                                    inactiveContentColor = TextSecondary,
                                    inactiveBorderColor = OutlineSubtle.copy(alpha = 0.7f)
                                ),
                                border = SegmentedButtonDefaults.borderStroke(
                                    color = if (isSelected) currentAccent else OutlineSubtle.copy(alpha = 0.7f),
                                    width = 1.dp
                                ),
                                icon = {
                                    SegmentedButtonDefaults.Icon(active = isSelected) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) TextPrimary else TextSecondary,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.expressiveBounce(pressScale = 0.95f)
                            )
                        }
                    }

                    // Material You Dynamic Wallpaper Colors (Android 12+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Spacer(modifier = Modifier.height(18.dp))
                        val isMaterialYou = currentAccentHex == "DYNAMIC"
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .expressiveBounce(pressScale = 0.96f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.setAccentColor(if (isMaterialYou) "#2A8575" else "DYNAMIC")
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isMaterialYou) currentAccent.copy(alpha = 0.15f) else SurfaceContainerLowest,
                            border = androidx.compose.foundation.BorderStroke(
                                1.2.dp,
                                if (isMaterialYou) currentAccent else OutlineSubtle.copy(alpha = 0.7f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = currentAccent, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = strings.materialYouToggle,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        )
                                        Text(
                                            text = strings.materialYouDesc,
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                                        )
                                    }
                                }

                                Switch(
                                    checked = isMaterialYou,
                                    onCheckedChange = { checked ->
                                        viewModel.setAccentColor(if (checked) "DYNAMIC" else "#2A8575")
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = currentAccent)
                                )
                            }
                        }

                        if (isMaterialYou) {
                            Spacer(modifier = Modifier.height(16.dp))
                            WallpaperDynamicStylePicker(
                                currentStyle = dynamicColorStyle,
                                onStyleSelected = { viewModel.setDynamicColorStyle(it) },
                                isDark = isDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AMOLED Dark Mode Toggle (Pure #000000 OLED background)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .expressiveBounce(pressScale = 0.96f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.setAmoledDarkMode(!isAmoled)
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAmoled) currentAccent.copy(alpha = 0.15f) else SurfaceContainerLowest,
                        border = androidx.compose.foundation.BorderStroke(
                            1.2.dp,
                            if (isAmoled) currentAccent else OutlineSubtle.copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = currentAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = strings.amoledToggle,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        text = strings.amoledDesc,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }

                            Switch(
                                checked = isAmoled,
                                onCheckedChange = { checked ->
                                    viewModel.setAmoledDarkMode(checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = currentAccent
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Custom Accent Palette Swatches
                    Text(
                        text = strings.customAccentColors,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colors.forEach { (name, hex) ->
                            val swatchColor = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = currentAccentHex == hex

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .expressiveBounce(pressScale = 0.90f)
                                    .clip(CircleShape)
                                    .background(swatchColor)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) AccentPrimary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setAccentColor(hex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = name, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        item(key = "section_semester") {
            // SECTION: Programs & Routines & Task Visibility
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceContainerLow,
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header row: launcher for Programs & Routines sheet
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .expressiveBounce(pressScale = 0.96f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onDismiss()
                                onOpenSemesterSettings()
                            }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(currentAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventRepeat,
                                    contentDescription = null,
                                    tint = currentAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.semesterSettingsTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strings.semesterSettingsDesc,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(OutlineSubtle.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Semester Weeks in Calendar Header & Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.semesterWeeksSettingsTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.semesterWeeksSettingsDesc,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = semesterConfig.isEnabled,
                            onCheckedChange = { isChecked ->
                                taskViewModel?.setSemesterEnabled(isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = currentAccent,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = BgLight
                            )
                        )
                    }

                    if (semesterConfig.isEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Start Date Card
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .expressiveBounce(pressScale = 0.95f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showStartDatePicker = true },
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceContainerLowest,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = currentAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = strings.semesterStartDateLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        )
                                        Text(
                                            text = semesterConfig.startDateMillis?.let { DateTimeUtils.formatDate(it) } ?: strings.semesterNotSet,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (semesterConfig.startDateMillis != null) TextPrimary else TextMuted
                                            )
                                        )
                                    }
                                }
                            }

                            // End Date Card
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .expressiveBounce(pressScale = 0.95f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showEndDatePicker = true },
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceContainerLowest,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = currentAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = strings.semesterEndDateLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        )
                                        Text(
                                            text = semesterConfig.endDateMillis?.let { DateTimeUtils.formatDate(it) } ?: strings.semesterNotSet,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (semesterConfig.endDateMillis != null) TextPrimary else TextMuted
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        if (semesterConfig.startDateMillis != null && semesterConfig.endDateMillis != null) {
                            val totalWeeks = SemesterUtils.getTotalWeeks(semesterConfig.startDateMillis, semesterConfig.endDateMillis)
                            val currentWeek = SemesterUtils.getCurrentSemesterWeek(semesterConfig.startDateMillis, semesterConfig.endDateMillis)
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = currentAccent.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, currentAccent.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.semesterTotalWeeksCount(totalWeeks),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = currentAccent
                                        )
                                    )
                                    if (currentWeek != null) {
                                        Text(
                                            text = strings.semesterCurrentWeekLabel(currentWeek),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = TextSecondary
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

        item(key = "section_screen_order") {
            // SECTION 3: Screen Order Arrangement
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceContainerLow,
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Reorder, contentDescription = null, tint = currentAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = strings.screenOrderSection,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                )
                                Text(
                                    text = strings.screenOrderDesc,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.resetScreenOrder() },
                            modifier = Modifier
                                .size(32.dp)
                                .expressiveBounce(pressScale = 0.88f)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = strings.resetOrder, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        screenOrder.forEachIndexed { index, pageId ->
                            val (title, icon) = when (pageId) {
                                0 -> Pair(strings.navProjects, Icons.Default.Folder)
                                1 -> Pair(strings.navToday, Icons.Default.Schedule)
                                2 -> Pair(strings.navAllTasks, Icons.Default.CalendarMonth)
                                3 -> Pair(strings.navInstants, Icons.Default.PhotoLibrary)
                                else -> Pair(strings.navAllTasks, Icons.Default.CalendarMonth)
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceContainerLowest,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(currentAccent.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = currentAccent,
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Icon(icon, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { viewModel.moveScreenUp(index) },
                                            enabled = index > 0,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .expressiveBounce(pressScale = 0.88f)
                                        ) {
                                            Icon(
                                                Icons.Default.KeyboardArrowUp,
                                                contentDescription = strings.moveUp,
                                                tint = if (index > 0) TextPrimary else TextMuted
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.moveScreenDown(index) },
                                            enabled = index < screenOrder.size - 1,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .expressiveBounce(pressScale = 0.88f)
                                        ) {
                                            Icon(
                                                Icons.Default.KeyboardArrowDown,
                                                contentDescription = strings.moveDown,
                                                tint = if (index < screenOrder.size - 1) TextPrimary else TextMuted
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

        // Footer
        item(key = "section_footer") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.versionInfo,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 12.sp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
}

    if (showStartDatePicker) {
        M3CardDatePickerDialog(
            initialDateMillis = semesterConfig.startDateMillis,
            title = strings.semesterStartDateLabel,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { millis ->
                taskViewModel?.updateSemesterDates(millis, semesterConfig.endDateMillis)
                showStartDatePicker = false
            }
        )
    }

    if (showEndDatePicker) {
        M3CardDatePickerDialog(
            initialDateMillis = semesterConfig.endDateMillis ?: semesterConfig.startDateMillis,
            title = strings.semesterEndDateLabel,
            onDismiss = { showEndDatePicker = false },
            onDateSelected = { millis ->
                taskViewModel?.updateSemesterDates(semesterConfig.startDateMillis, millis)
                showEndDatePicker = false
            }
        )
    }
}

@Composable
fun WallpaperDynamicStylePicker(
    currentStyle: DynamicColorStyle,
    onStyleSelected: (DynamicColorStyle) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val baseScheme = remember(context, isDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else null
    }

    if (baseScheme == null) return

    val styles = listOf(
        DynamicColorStyle.BALANCED to strings.dynamicStyleBalanced,
        DynamicColorStyle.VIBRANT to strings.dynamicStyleVibrant,
        DynamicColorStyle.MUTED to strings.dynamicStyleMuted,
        DynamicColorStyle.HIGH_CONTRAST to strings.dynamicStyleHighContrast
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = strings.dynamicStylesTitle,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 13.sp
            )
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = strings.dynamicStylesDesc,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styles.forEach { (style, label) ->
                val isSelected = currentStyle == style
                val styledScheme = remember(baseScheme, style, isDark) {
                    transformDynamicScheme(baseScheme, style, isDark)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .expressiveBounce(pressScale = 0.94f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onStyleSelected(style) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) styledScheme.primary.copy(alpha = 0.14f) else SurfaceContainerLowest,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) styledScheme.primary else OutlineSubtle.copy(alpha = 0.65f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            WallpaperColorCircle(
                                primary = styledScheme.primary,
                                primaryContainer = styledScheme.primaryContainer,
                                secondaryContainer = styledScheme.secondaryContainer,
                                onPrimary = styledScheme.onPrimary,
                                isSelected = isSelected,
                                modifier = Modifier.fillMaxSize(0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun WallpaperColorCircle(
    primary: Color,
    primaryContainer: Color,
    secondaryContainer: Color,
    onPrimary: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val d = size.minDimension
            val topLeft = Offset((size.width - d) / 2f, (size.height - d) / 2f)
            val arcSize = Size(d, d)

            // Top semi-circle: 180° to 360°
            drawArc(
                color = primaryContainer,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize
            )

            // Bottom-left quadrant: 90° to 180°
            drawArc(
                color = secondaryContainer,
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize
            )

            // Bottom-right quadrant: 0° to 90°
            drawArc(
                color = primary,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.44f)
                    .clip(CircleShape)
                    .background(primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = onPrimary,
                    modifier = Modifier.fillMaxSize(0.68f)
                )
            }
        }
    }
}
