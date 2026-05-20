/*
 * Copyright (C) 2025-2026 AxionOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.alpha.themepicker.ui.lockscreen

import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.core.graphics.drawable.toBitmap
import com.android.alpha.themepicker.R
import com.android.alpha.themepicker.ui.components.CommonBottomSheet

enum class AffordanceSlot(val slotId: String) {
    BOTTOM_START("bottom_start"),
    BOTTOM_END("bottom_end"),
}

@Composable
fun AffordanceOverlay(
    isPreview: Boolean,
    scale: Float = 1f,
    selections: List<AffordanceSelection> = emptyList(),
    affordances: List<AffordanceInfo> = emptyList(),
    activeSlot: AffordanceSlot? = null,
    onSlotClicked: (AffordanceSlot) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val startAffordanceId =
        selections.firstOrNull { it.slotId == AffordanceSlot.BOTTOM_START.slotId }?.affordanceId
    val endAffordanceId =
        selections.firstOrNull { it.slotId == AffordanceSlot.BOTTOM_END.slotId }?.affordanceId

    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp * scale, end = 16.dp * scale),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AffordanceButton(
            isPreview = isPreview,
            affordanceId = startAffordanceId,
            affordances = affordances,
            scale = scale,
            selected = activeSlot == AffordanceSlot.BOTTOM_START || isPreview,
            onClick = { onSlotClicked(AffordanceSlot.BOTTOM_START) },
        )

        AffordanceButton(
            isPreview = isPreview,
            affordanceId = endAffordanceId,
            affordances = affordances,
            scale = scale,
            selected = activeSlot == AffordanceSlot.BOTTOM_END || isPreview,
            onClick = { onSlotClicked(AffordanceSlot.BOTTOM_END) },
        )
    }
}

@Composable
private fun AffordanceButton(
    affordanceId: String?,
    affordances: List<AffordanceInfo>,
    scale: Float,
    onClick: () -> Unit,
    isPreview: Boolean,
    selected: Boolean = false,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val affordance = affordances.firstOrNull { it.id == affordanceId }
    val hasAffordance = affordance != null
    val buttonSize = if (isPreview) 48.dp else 64.dp
    val iconSize = if (isPreview) 18.dp else 28.dp
    val density = context.resources.displayMetrics.density

    val hide = !hasAffordance && isPreview
    val unselected = !selected && !hide

    val iconBitmap =
        remember(affordance?.iconResourceId) {
            affordance?.iconResourceId?.let { resId ->
                AffordanceRepository.loadAffordanceIcon(context, resId)?.let { drawable ->
                    val px = (28 * density).toInt()
                    drawable.toBitmap(width = px, height = px)
                }
            }
        }

    Box(
        modifier =
            Modifier.size(buttonSize * scale)
                .clip(CircleShape)
                .background(
                    color = if (selected && hasAffordance) colors.surface else Color.Transparent,
                    shape = CircleShape,
                )
                .border(
                    width = if (unselected) 1.dp else 0.dp,
                    color = if (unselected) Color.White else Color.Transparent,
                    shape = CircleShape,
                )
                .clickable(
                    enabled = !isPreview,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    onClick()
                },
        contentAlignment = Alignment.Center,
    ) {
        if (hasAffordance && iconBitmap != null) {
            Image(
                bitmap = iconBitmap.asImageBitmap(),
                contentDescription = affordance?.name,
                modifier = Modifier.size(iconSize * scale),
                colorFilter = ColorFilter.tint(if (selected) colors.onSurface else Color.White),
            )
        } else {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_affordance),
                tint = if (hide) Color.Transparent else Color.White,
                modifier = Modifier.size(iconSize * scale),
            )
        }
    }
}

@Composable
internal fun AffordancePickerSheet(
    visible: Boolean,
    currentSlot: AffordanceSlot,
    currentAffordanceId: String?,
    affordances: List<AffordanceInfo>,
    onDismiss: () -> Unit,
    onSelect: (AffordanceInfo) -> Unit,
    onRemove: () -> Unit,
) {
    val titleText =
        if (currentSlot == AffordanceSlot.BOTTOM_START) stringResource(R.string.left_affordance)
        else stringResource(R.string.right_affordance)

    CommonBottomSheet(
        visible = visible,
        title = titleText,
        heightFraction = 0.5f,
        onDismiss = onDismiss,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                AffordancePickerItem(
                    name = stringResource(R.string.affordance_none),
                    iconBitmap = null,
                    fallbackIcon = Icons.Default.Close,
                    isSelected = currentAffordanceId == null,
                    isEnabled = true,
                    onClick = {
                        onRemove()
                        onDismiss()
                    },
                )
            }

            items(count = affordances.size, key = { affordances[it].id }) { index ->
                val affordance = affordances[index]
                AffordancePickerItem(
                    name = affordance.name,
                    iconResourceId = affordance.iconResourceId,
                    isSelected = affordance.id == currentAffordanceId,
                    isEnabled = affordance.isEnabled,
                    enablementExplanation = affordance.enablementExplanation,
                    onClick = {
                        if (affordance.isEnabled) {
                            onSelect(affordance)
                            onDismiss()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun AffordancePickerItem(
    name: String,
    iconResourceId: Int? = null,
    iconBitmap: Bitmap? = null,
    fallbackIcon: ImageVector? = null,
    isSelected: Boolean,
    isEnabled: Boolean,
    enablementExplanation: String? = null,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val density = context.resources.displayMetrics.density

    val resolvedBitmap =
        iconBitmap
            ?: remember(iconResourceId) {
                iconResourceId?.let { resId ->
                    AffordanceRepository.loadAffordanceIcon(context, resId)?.let { drawable ->
                        val px = (24 * density).toInt()
                        drawable.toBitmap(width = px, height = px)
                    }
                }
            }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier.clip(MaterialTheme.shapes.medium)
                .clickable(enabled = isEnabled) { onClick() }
                .padding(4.dp)
                .alpha(if (isEnabled) 1f else 0.4f),
    ) {
        Box(
            modifier =
                Modifier.size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) colors.primaryContainer else colors.surfaceContainerHigh
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (resolvedBitmap != null) {
                Image(
                    bitmap = resolvedBitmap.asImageBitmap(),
                    contentDescription = name,
                    modifier = Modifier.size(24.dp),
                    colorFilter =
                        ColorFilter.tint(
                            if (isSelected) colors.onPrimaryContainer else colors.onSurface
                        ),
                )
            } else if (fallbackIcon != null) {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = name,
                    tint = if (isSelected) colors.onPrimaryContainer else colors.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 64.dp),
        )
    }
}
