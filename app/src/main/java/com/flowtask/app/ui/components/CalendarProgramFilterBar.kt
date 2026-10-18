package com.flowtask.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.flowtask.app.ui.theme.LocalDockColors
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceContainerLowest
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarProgramFilterBar(
    layerPrograms: List<LayerProgram>,
    selectedProgramId: String?,
    onSelectProgram: (String?) -> Unit,
    modifier: Modifier = Modifier,
    lastSelectedProgramId: String? = null
) {
    val currentAccent = LocalAccentColor.current
    val dockColors = LocalDockColors.current
    val scrollState = rememberScrollState()

    // If there are no layer programs defined yet, keep UI clean
    if (layerPrograms.isEmpty()) {
        return
    }

    val isProgramActive = selectedProgramId != "NONE"
    val effectiveSelectedId = when (selectedProgramId) {
        "NONE" -> null
        null -> (lastSelectedProgramId?.takeIf { id -> layerPrograms.any { it.id == id } })
            ?: layerPrograms.firstOrNull { it.isEnabled }?.id
            ?: layerPrograms.firstOrNull()?.id
        else -> selectedProgramId
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Horizontally scrolling program pills
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (program in layerPrograms) {
                val isSelected = isProgramActive && program.id == effectiveSelectedId
                val programColor = remember(program.colorHex, currentAccent) {
                    program.colorHex?.let {
                        try {
                            Color(android.graphics.Color.parseColor(it))
                        } catch (_: Exception) {
                            currentAccent
                        }
                    } ?: currentAccent
                }

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            onSelectProgram("NONE")
                        } else {
                            onSelectProgram(program.id)
                        }
                    },
                    label = {
                        Text(
                            text = program.layerName,
                            color = if (isSelected) Color.White else TextPrimary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                    },
                    leadingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(programColor)
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SurfaceContainerLowest,
                        labelColor = TextPrimary,
                        selectedContainerColor = dockColors.barBackground,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = OutlineSubtle.copy(alpha = 0.7f),
                        selectedBorderColor = dockColors.barBackground
                    ),
                    modifier = Modifier.expressiveBounce(pressScale = 0.94f)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Subtle divider separating scrolling pills and the right switch
        Box(
            modifier = Modifier
                .height(24.dp)
                .width(1.dp)
                .background(OutlineSubtle.copy(alpha = 0.6f))
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Switch to switch on and off the program on the calendar (with M3 spring physics)
        Switch(
            checked = isProgramActive,
            onCheckedChange = { isChecked ->
                if (isChecked) {
                    val targetId = (lastSelectedProgramId?.takeIf { id -> layerPrograms.any { it.id == id } })
                        ?: layerPrograms.firstOrNull { it.isEnabled }?.id
                        ?: layerPrograms.firstOrNull()?.id
                    onSelectProgram(targetId)
                } else {
                    onSelectProgram("NONE")
                }
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = dockColors.barBackground,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = BgLight,
                uncheckedBorderColor = OutlineSubtle.copy(alpha = 0.7f)
            ),
            modifier = Modifier.expressiveBounce(pressScale = 0.92f)
        )
    }
}
