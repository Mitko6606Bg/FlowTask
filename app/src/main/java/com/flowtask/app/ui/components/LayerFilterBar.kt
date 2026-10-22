package com.flowtask.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.ui.theme.LocalAccentColor
import com.flowtask.app.ui.theme.LocalDockColors
import com.flowtask.app.ui.theme.OutlineSubtle
import com.flowtask.app.ui.theme.SurfaceLight
import com.flowtask.app.ui.theme.TextPrimary
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

        val allBgColor by animateColorAsState(
            targetValue = if (isAllSelected) dockColors.barBackground else SurfaceLight,
            animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
            label = "allPillBg"
        )
        val allBorderColor by animateColorAsState(
            targetValue = if (isAllSelected) dockColors.barBackground else OutlineSubtle,
            animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
            label = "allPillBorder"
        )
        val allContentColor by animateColorAsState(
            targetValue = if (isAllSelected) Color.White else TextPrimary,
            animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
            label = "allPillContent"
        )
        val allIconColor by animateColorAsState(
            targetValue = if (isAllSelected) Color.White else dockColors.barBackground,
            animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
            label = "allPillIcon"
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = allBgColor,
            border = BorderStroke(1.dp, allBorderColor),
            modifier = Modifier
                .expressiveBounce(pressScale = 0.95f)
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
                    tint = allIconColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = strings.allLayers,
                    color = allContentColor,
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

            val pillBgColor by animateColorAsState(
                targetValue = if (isSelected) dockColors.barBackground else SurfaceLight,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
                label = "layerPillBg_${layerName}"
            )
            val pillBorderColor by animateColorAsState(
                targetValue = if (isSelected) dockColors.barBackground else OutlineSubtle,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
                label = "layerPillBorder_${layerName}"
            )
            val pillContentColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else TextPrimary,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
                label = "layerPillContent_${layerName}"
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = pillBgColor,
                border = BorderStroke(1.dp, pillBorderColor),
                modifier = Modifier
                    .expressiveBounce(pressScale = 0.95f)
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
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn(animationSpec = tween(120)) + expandHorizontally(
                            animationSpec = spring(dampingRatio = 0.82f, stiffness = 550f)
                        ),
                        exit = fadeOut(animationSpec = tween(90)) + shrinkHorizontally(
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = 600f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = pillContentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = layerName,
                        color = pillContentColor,
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
