package com.flowtask.app.ui.components.programs

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.data.local.model.LayerProgram
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLight
import com.flowtask.app.ui.theme.SurfaceContainerLow
import com.flowtask.app.ui.theme.TextMuted
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.DateTimeUtils
import com.flowtask.app.util.LocalAppStrings

@Composable
fun ProgramFolderCard(
    program: LayerProgram,
    onOpenProgram: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val programColor = program.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { currentAccent }
    } ?: currentAccent

    val hasDates = program.startDateMillis != null && program.endDateMillis != null
    val periodLabel = if (hasDates) {
        "${DateTimeUtils.formatDate(program.startDateMillis!!)} – ${DateTimeUtils.formatDate(program.endDateMillis!!)}"
    } else {
        strings.allYearRound
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .expressiveBounce(pressScale = 0.96f)
            .clickable { onOpenProgram() },
        shape = RoundedCornerShape(22.dp),
        color = SurfaceContainerLight,
        border = BorderStroke(1.dp, OutlineSubtle.copy(alpha = 0.7f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Program Squircle Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(programColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EventRepeat,
                    contentDescription = null,
                    tint = programColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Name and Period/Routine Badges
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = program.layerName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Routines count badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceContainerLow
                    ) {
                        Text(
                            text = strings.programRoutinesCount(program.scheduleItems.size),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = if (program.scheduleItems.isNotEmpty()) programColor else TextSecondary
                            ),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    // Period badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceContainerLow
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasDates) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = periodLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Calendar Active Switch
            Switch(
                checked = program.isEnabled,
                onCheckedChange = onToggleActive,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = programColor,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = BgLight
                )
            )
        }
    }
}
