package com.flowtask.app.ui.components.programs

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import kotlinx.coroutines.CancellationException
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.text.style.TextAlign
import java.util.Calendar
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.data.local.model.ScheduleItem
import com.flowtask.app.ui.components.dialogs.M3CardTimePickerDialog
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLight
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.TextMuted
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.LocalAppStrings
import java.util.UUID

private data class DayCellData(
    val dayNumber: Int,
    val dayOfWeek: Int // 1..7 (1 = Mon .. 7 = Sun)
)

@Composable
fun ProgramWeekCalendarView(
    program: LayerProgram,
    onBack: () -> Unit,
    onEditProgram: () -> Unit,
    onDeleteProgram: () -> Unit,
    onSaveProgram: (LayerProgram) -> Unit,
    onSyncToCalendar: () -> Unit,
    syncSuccessMessage: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val programColor = program.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { currentAccent }
    } ?: currentAccent

    val daysShort = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
    var selectedDayOfWeek by remember { mutableIntStateOf(1) } // 1 = Monday .. 7 = Sunday
    val selectedDayName = strings.daysOfWeek.getOrElse(selectedDayOfWeek - 1) { "" }

    val initCal = remember(program.startDateMillis) {
        val cal = Calendar.getInstance()
        if (program.startDateMillis != null) {
            cal.timeInMillis = program.startDateMillis!!
        }
        cal
    }
    var displayedYear by remember { mutableIntStateOf(initCal.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableIntStateOf(initCal.get(Calendar.MONTH)) }

    val monthTitle = remember(displayedYear, displayedMonth) {
        DateTimeUtils.getMonthDisplayName(displayedYear, displayedMonth)
    }

    val (topWeekDays, remainingRows) = remember(displayedYear, displayedMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, displayedYear)
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0

        val prevCal = (cal.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        val daysInPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val topWeek = (0..6).map { col ->
            val dayOfWeek = col + 1
            val d = if (col < firstDayOfWeek) {
                daysInPrevMonth - (firstDayOfWeek - 1 - col)
            } else {
                col - firstDayOfWeek + 1
            }
            DayCellData(dayNumber = d, dayOfWeek = dayOfWeek)
        }

        val rows = mutableListOf<List<Int?>>()
        var currentDay = 7 - firstDayOfWeek + 1
        while (currentDay <= daysInMonth) {
            val row = (0..6).map {
                if (currentDay <= daysInMonth) {
                    currentDay++
                } else {
                    null
                }
            }
            rows.add(row)
        }
        Pair(topWeek, rows)
    }

    val hasDates = program.startDateMillis != null && program.endDateMillis != null
    val periodLabel = if (hasDates) {
        "${DateTimeUtils.formatDate(program.startDateMillis!!)} – ${DateTimeUtils.formatDate(program.endDateMillis!!)}"
    } else {
        strings.allYearRound
    }

    val dayItems = program.scheduleItems
        .filter { it.dayOfWeek == selectedDayOfWeek }
        .sortedBy { it.startMinutes }

    // State for Add Routine Card (inline on the selected day)
    var isAddingRoutine by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ScheduleItem?>(null) }
    var routineSubject by remember { mutableStateOf("") }
    var routineRoomOrNote by remember { mutableStateOf("") }
    var routineMinutes by remember { mutableIntStateOf(540) } // 09:00 default
    var showTimePicker by remember { mutableStateOf(false) }

    val formattedRoutineTime = String.format("%02d:%02d", routineMinutes / 60, routineMinutes % 60)

    // Back gesture inside routine creation/editing dismisses the inline input
    PredictiveBackHandler(enabled = isAddingRoutine || editingItem != null) { progress ->
        try {
            progress.collect {}
            isAddingRoutine = false
            editingItem = null
        } catch (_: CancellationException) {
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .animateContentSize(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f))
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .expressiveBounce(pressScale = 0.90f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.closeAction,
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(programColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = program.layerName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = TextPrimary
                            ),
                            maxLines = 1
                        )
                    }
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEditProgram,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .expressiveBounce(pressScale = 0.90f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = strings.editProgramTitle,
                        tint = TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDeleteProgram,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .expressiveBounce(pressScale = 0.90f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = strings.deleteLayerProgram,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Month Calendar Card with Top Week Highlighted
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceContainerLight,
            border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header: Month Title & Navigation Chevrons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = strings.calendarWeekViewTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, displayedYear)
                                    set(Calendar.MONTH, displayedMonth)
                                    add(Calendar.MONTH, -1)
                                }
                                displayedYear = cal.get(Calendar.YEAR)
                                displayedMonth = cal.get(Calendar.MONTH)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceContainerLow)
                                .expressiveBounce(pressScale = 0.90f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, displayedYear)
                                    set(Calendar.MONTH, displayedMonth)
                                    add(Calendar.MONTH, 1)
                                }
                                displayedYear = cal.get(Calendar.YEAR)
                                displayedMonth = cal.get(Calendar.MONTH)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceContainerLow)
                                .expressiveBounce(pressScale = 0.90f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Weekday Labels: Пн Вт Ср Чт Пт Сб Нд
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    daysShort.forEach { dayLabel ->
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // TOP WEEK (Row 0): Highlighted, Active, Interactive
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = programColor.copy(alpha = 0.08f),
                    border = BorderStroke(1.2.dp, programColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        topWeekDays.forEach { dayCell ->
                            val isSelected = selectedDayOfWeek == dayCell.dayOfWeek
                            val routinesCount = program.scheduleItems.count { it.dayOfWeek == dayCell.dayOfWeek }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) programColor else Color.Transparent)
                                    .expressiveBounce(pressScale = 0.92f)
                                    .clickable {
                                        selectedDayOfWeek = dayCell.dayOfWeek
                                        isAddingRoutine = false
                                        editingItem = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${dayCell.dayNumber}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected) TextOnAccent else TextPrimary,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    if (routinesCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) TextOnAccent else programColor)
                                        )
                                    } else {
                                        Box(modifier = Modifier.size(5.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // REMAINING ROWS (Row 1..N): Grayed Out, Disabled, Unclickable
                remainingRows.forEach { rowDays ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .padding(vertical = 1.dp, horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowDays.forEach { dayNum ->
                            if (dayNum != null) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary.copy(alpha = 0.22f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Day Routines Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.routineForDayHeader(selectedDayName),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
            )

            if (!isAddingRoutine && editingItem == null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = programColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, programColor.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .expressiveBounce(pressScale = 0.92f)
                        .clickable {
                            editingItem = null
                            routineSubject = ""
                            routineRoomOrNote = ""
                            routineMinutes = 540
                            isAddingRoutine = true
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = programColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.addAction,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = programColor,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Existing Routines for the Selected Day
        if (dayItems.isEmpty() && !isAddingRoutine && editingItem == null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        editingItem = null
                        routineSubject = ""
                        routineRoomOrNote = ""
                        routineMinutes = 540
                        isAddingRoutine = true
                    },
                shape = RoundedCornerShape(18.dp),
                color = SurfaceContainerLow,
                border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.noClassesForDay,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+ " + strings.addRoutineForDayButton(selectedDayName),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = programColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                dayItems.forEach { item ->
                    val hour = item.startMinutes / 60
                    val minute = item.startMinutes % 60
                    val formatted = String.format("%02d:%02d", hour, minute)

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceContainerLow,
                        border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Time pill
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = programColor.copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = programColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = formatted,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = programColor,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 14.sp
                                        )
                                    )
                                    if (!item.locationOrNote.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Room,
                                                contentDescription = null,
                                                tint = TextMuted,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = item.locationOrNote!!,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        editingItem = item
                                        routineSubject = item.title
                                        routineRoomOrNote = item.locationOrNote ?: ""
                                        routineMinutes = item.startMinutes
                                        isAddingRoutine = false
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .expressiveBounce(pressScale = 0.90f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = strings.editAction,
                                        tint = programColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(2.dp))
                                IconButton(
                                    onClick = {
                                        val updated = program.scheduleItems.filter { it.id != item.id }
                                        onSaveProgram(program.copy(scheduleItems = updated))
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .expressiveBounce(pressScale = 0.90f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = strings.deleteAction,
                                        tint = TextMuted,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Inline Card to Add or Edit Routine (like task create card)
        AnimatedVisibility(visible = isAddingRoutine || editingItem != null) {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = SurfaceContainerLowest,
                    border = BorderStroke(1.2.dp, programColor.copy(alpha = 0.45f)),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (editingItem != null) strings.editClassTitle else strings.newClassTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            )
                            IconButton(
                                onClick = {
                                    isAddingRoutine = false
                                    editingItem = null
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = strings.cancelAction,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Subject Input
                        OutlinedTextField(
                            value = routineSubject,
                            onValueChange = { routineSubject = it },
                            placeholder = { Text(strings.subjectPlaceholder, fontSize = 13.sp, color = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerLow,
                                unfocusedContainerColor = SurfaceContainerLow,
                                focusedBorderColor = programColor,
                                unfocusedBorderColor = OutlineSubtle.copy(alpha = 0.7f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Time Selector & Room/Note Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Time button
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SurfaceContainerLow,
                                border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .expressiveBounce(pressScale = 0.94f)
                                    .clickable { showTimePicker = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = programColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = formattedRoutineTime,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                    )
                                }
                            }

                            // Location / Note Input
                            OutlinedTextField(
                                value = routineRoomOrNote,
                                onValueChange = { routineRoomOrNote = it },
                                placeholder = { Text(strings.roomOrNotePlaceholder, fontSize = 13.sp, color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceContainerLow,
                                    unfocusedContainerColor = SurfaceContainerLow,
                                    focusedBorderColor = programColor,
                                    unfocusedBorderColor = OutlineSubtle.copy(alpha = 0.7f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Save Button
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (routineSubject.isNotBlank()) programColor else programColor.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .expressiveBounce(pressScale = 0.95f)
                        ) {
                            TextButton(
                                onClick = {
                                    if (routineSubject.isNotBlank()) {
                                        val updatedItems = if (editingItem != null) {
                                            program.scheduleItems.map {
                                                if (it.id == editingItem!!.id) it.copy(
                                                    subject = routineSubject.trim(),
                                                    startMinutes = routineMinutes,
                                                    roomOrNote = routineRoomOrNote.trim().ifBlank { null }
                                                ) else it
                                            }
                                        } else {
                                            val newItem = ScheduleItem(
                                                id = UUID.randomUUID().toString(),
                                                dayOfWeek = selectedDayOfWeek,
                                                subject = routineSubject.trim(),
                                                startMinutes = routineMinutes,
                                                roomOrNote = routineRoomOrNote.trim().ifBlank { null }
                                            )
                                            program.scheduleItems + newItem
                                        }
                                        onSaveProgram(program.copy(scheduleItems = updatedItems))
                                        isAddingRoutine = false
                                        editingItem = null
                                        routineSubject = ""
                                        routineRoomOrNote = ""
                                    }
                                },
                                enabled = routineSubject.isNotBlank()
                            ) {
                                Text(
                                    text = if (editingItem != null) strings.saveAction else strings.addClassButton,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextOnAccent
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Calendar Sync Action Pill
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .expressiveBounce(pressScale = 0.96f)
                .clickable { onSyncToCalendar() },
            shape = RoundedCornerShape(18.dp),
            color = SurfaceContainerLight,
            border = BorderStroke(1.dp, programColor.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (syncSuccessMessage) Icons.Default.Check else Icons.Default.Sync,
                    contentDescription = null,
                    tint = programColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (syncSuccessMessage) strings.updateAction else strings.syncProgramToCalendar,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = programColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showTimePicker) {
        M3CardTimePickerDialog(
            initialMinutes = routineMinutes,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { minutes ->
                routineMinutes = minutes
                showTimePicker = false
            }
        )
    }
}
