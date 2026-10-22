package com.flowtask.app.ui.components.programs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.flowtask.app.ui.components.dialogs.M3CardDatePickerDialog
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.TextMuted
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.LocalAppStrings
import java.util.UUID

private val PROGRAM_PALETTE = listOf(
    "#2A8575", // Teal
    "#2B6CB0", // Blue
    "#4F46E5", // Indigo
    "#7C3AED", // Violet
    "#DB2777", // Rose
    "#D97706", // Amber
    "#059669", // Emerald
    "#EA580C"  // Coral
)

@Composable
fun ProgramCreateCard(
    onDismiss: () -> Unit,
    onProgramCreated: (LayerProgram) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current

    var programName by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf(PROGRAM_PALETTE[0]) }
    var hasSpecificPeriod by remember { mutableStateOf(false) }
    var showTasksEverywhere by remember { mutableStateOf(false) }
    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val activeColor = remember(selectedColorHex) {
        try {
            Color(android.graphics.Color.parseColor(selectedColorHex))
        } catch (_: Exception) {
            currentAccent
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .animateContentSize(animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f))
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header: Back icon, Title, Close icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDismiss,
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

                Text(
                    text = strings.newLayerProgramTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = TextPrimary
                    )
                )
            }

            IconButton(
                onClick = onDismiss,
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

        Spacer(modifier = Modifier.height(18.dp))

        // 1. Program Name Input
        Text(
            text = strings.programLayerPrompt,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = programName,
            onValueChange = { programName = it },
            placeholder = { Text("напр. Университет, Фитнес, Работа...", fontSize = 14.sp, color = TextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceContainerLow,
                unfocusedContainerColor = SurfaceContainerLow,
                focusedBorderColor = activeColor,
                unfocusedBorderColor = OutlineSubtle.copy(alpha = 0.7f)
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Program Color Palette Dots
        Text(
            text = strings.programColorLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(
                PROGRAM_PALETTE,
                key = { it },
                contentType = { "palette_color" }
            ) { hex ->
                val isSelected = hex.equals(selectedColorHex, ignoreCase = true)
                val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { currentAccent }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) TextPrimary else Color.Transparent,
                            shape = CircleShape
                        )
                        .expressiveBounce(pressScale = 0.90f)
                        .clickable { selectedColorHex = hex },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalDivider(color = OutlineSubtle.copy(alpha = 0.4f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        // 3. Program Routine Visibility Toggle (Per Program, Default OFF = Calendar only)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.programTasksEverywhereSwitchLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.programTasksEverywhereSwitchDesc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = showTasksEverywhere,
                onCheckedChange = { showTasksEverywhere = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = activeColor,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = BgLight
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = OutlineSubtle.copy(alpha = 0.4f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        // 4. Specific Period Toggle (Default OFF = All Year Round)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.hasDateRangeLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (hasSpecificPeriod) strings.semesterEnableDesc else strings.ongoingRoutineDesc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = hasSpecificPeriod,
                onCheckedChange = { hasSpecificPeriod = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = activeColor,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = BgLight
                )
            )
        }

        AnimatedVisibility(visible = hasSpecificPeriod) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start Date Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceContainerLow,
                        border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .expressiveBounce(pressScale = 0.95f)
                            .clickable { showStartDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = activeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = strings.startDateLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                )
                                Text(
                                    text = startDateMillis?.let { DateTimeUtils.formatDate(it) } ?: strings.semesterNotSet,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (startDateMillis != null) TextPrimary else TextMuted
                                    )
                                )
                            }
                        }
                    }

                    // End Date Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceContainerLow,
                        border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .expressiveBounce(pressScale = 0.95f)
                            .clickable { showEndDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = activeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = strings.endDateLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                )
                                Text(
                                    text = endDateMillis?.let { DateTimeUtils.formatDate(it) } ?: strings.semesterNotSet,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (endDateMillis != null) TextPrimary else TextMuted
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer Action Button: Create Program
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (programName.isNotBlank()) activeColor else activeColor.copy(alpha = 0.35f),
            shadowElevation = if (programName.isNotBlank()) 2.dp else 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .expressiveBounce(pressScale = 0.96f)
        ) {
            TextButton(
                onClick = {
                    if (programName.isNotBlank()) {
                        val newProgram = LayerProgram(
                            id = UUID.randomUUID().toString(),
                            layerName = programName.trim(),
                            isEnabled = true,
                            colorHex = selectedColorHex,
                            startDateMillis = if (hasSpecificPeriod) startDateMillis else null,
                            endDateMillis = if (hasSpecificPeriod) endDateMillis else null,
                            trackWeeks = if (hasSpecificPeriod) true else false,
                            scheduleItems = emptyList(),
                            showTasksEverywhere = showTasksEverywhere
                        )
                        onProgramCreated(newProgram)
                    }
                },
                enabled = programName.isNotBlank(),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = strings.createProgramAction,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextOnAccent
                    )
                )
            }
        }
    }

    if (showStartDatePicker) {
        M3CardDatePickerDialog(
            initialDateMillis = startDateMillis,
            title = strings.startDateLabel,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { millis ->
                startDateMillis = millis
                showStartDatePicker = false
            }
        )
    }

    if (showEndDatePicker) {
        M3CardDatePickerDialog(
            initialDateMillis = endDateMillis ?: startDateMillis,
            title = strings.endDateLabel,
            onDismiss = { showEndDatePicker = false },
            onDateSelected = { millis ->
                endDateMillis = millis
                showEndDatePicker = false
            }
        )
    }
}
