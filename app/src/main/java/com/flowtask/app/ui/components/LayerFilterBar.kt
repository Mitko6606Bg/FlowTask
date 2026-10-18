package com.flowtask.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.ui.theme.BgLight
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.LocalDockColors
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceLight
import com.flowtask.app.ui.theme.TextOnAccent
import com.flowtask.app.ui.theme.TextPrimary
import com.flowtask.app.ui.theme.TextSecondary
import com.flowtask.app.ui.theme.expressiveBounce
import com.flowtask.app.util.LocalAppStrings

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LayerFilterBar(
    availableLayers: List<String>,
    selectedLayer: String?,
    onSelectLayer: (String?) -> Unit,
    modifier: Modifier = Modifier,
    onLongClickLayer: ((String) -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val currentAccent = LocalAccentColor.current
    val dockColors = LocalDockColors.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    // If there are no layers defined by the user yet, don't clutter the top bar
    if (availableLayers.isEmpty() && selectedLayer == null) {
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All Layers" (Всички) Chip
        val isAllSelected = selectedLayer == null
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isAllSelected) dockColors.barBackground else SurfaceLight,
            border = BorderStroke(1.dp, if (isAllSelected) dockColors.barBackground else OutlineSubtle),
            modifier = Modifier
                .expressiveBounce(pressScale = 0.94f)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onSelectLayer(null) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isAllSelected) Icons.Default.Check else Icons.Default.Layers,
                    contentDescription = null,
                    tint = if (isAllSelected) Color.White else dockColors.barBackground,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = strings.allLayers,
                    color = if (isAllSelected) Color.White else TextPrimary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                    )
                )
            }
        }

        // Each individual Layer Chip
        availableLayers.forEach { layerName ->
            val isSelected = selectedLayer.equals(layerName, ignoreCase = true)

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) dockColors.barBackground else SurfaceLight,
                border = BorderStroke(1.dp, if (isSelected) dockColors.barBackground else OutlineSubtle),
                modifier = Modifier
                    .expressiveBounce(pressScale = 0.94f)
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (onLongClickLayer != null) {
                            Modifier.combinedClickable(
                                onClick = {
                                    if (isSelected) {
                                        onSelectLayer(null)
                                    } else {
                                        onSelectLayer(layerName)
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLongClickLayer(layerName)
                                }
                            )
                        } else {
                            Modifier.clickable {
                                if (isSelected) {
                                    onSelectLayer(null)
                                } else {
                                    onSelectLayer(layerName)
                                }
                            }
                        }
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = layerName,
                        color = if (isSelected) Color.White else TextPrimary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}
