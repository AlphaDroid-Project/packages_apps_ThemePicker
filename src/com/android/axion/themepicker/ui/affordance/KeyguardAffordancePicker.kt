/*
 * Copyright (C) 2025 AxionOS
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
package com.android.axion.themepicker.ui.lockscreen

import android.util.Log
import android.content.ContentResolver
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.ripple.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.android.axion.themepicker.ui.components.CommonBottomSheet
import com.android.axion.themepicker.ui.components.PagedTilePicker
import androidx.compose.material3.MaterialTheme
import com.android.axion.themepicker.R

data class AffordanceOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

object AffordancesList {
    val options = listOf(
        AffordanceOption("none", "None", Icons.Default.Close),
        AffordanceOption("mute", "Mute", Icons.Default.VolumeOff),
        AffordanceOption("camera", "Camera", Icons.Default.CameraAlt),
        AffordanceOption("home", "Device controls", Icons.Default.Home),
        AffordanceOption("video_camera", "Video camera", Icons.Default.Videocam),
        AffordanceOption("flashlight", "Flashlight", Icons.Default.FlashlightOff),
        AffordanceOption("do_not_disturb", "Do Not Disturb", Icons.Default.DoNotDisturb),
        AffordanceOption("wallet", "Wallet", Icons.Default.AccountBalanceWallet),
        AffordanceOption("qr_code_scanner", "QR code scanner", Icons.Default.QrCodeScanner)
    )
}

enum class AffordanceSlot(val settingsKey: String) {
    BOTTOM_START("slot_bottom_start"),
    BOTTOM_END("slot_bottom_end")
}

fun readAffordance(resolver: ContentResolver, slot: AffordanceSlot): String {
    return Settings.Secure.getString(resolver, slot.settingsKey) ?: "none"
}

fun writeAffordance(resolver: ContentResolver, slot: AffordanceSlot, value: String) {
    Settings.Secure.putString(resolver, slot.settingsKey, value)
}

@Composable
fun BoxScope.AffordanceOverlay(
    isPreview: Boolean,
    scale: Float = 1f,
    showPickerOnLaunch: AffordanceSlot? = null
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val colors = MaterialTheme.colorScheme
    val bottomPadding = 24.dp

    var startAffordance by remember { 
        mutableStateOf(readAffordance(resolver, AffordanceSlot.BOTTOM_START)) 
    }
    var endAffordance by remember { 
        mutableStateOf(readAffordance(resolver, AffordanceSlot.BOTTOM_END)) 
    }
    var showPicker by remember { mutableStateOf<AffordanceSlot?>(null) }

    var sheetOffset by remember { mutableStateOf(0f) }
    var sheetMaxOffset by remember { mutableStateOf(0f) }

    val density = LocalDensity.current
    val transY by animateDpAsState(
        targetValue = with(density) {
            val offsetPx = (sheetMaxOffset - sheetOffset).coerceAtLeast(0f)
            val offsetDp = offsetPx.toDp()
            (offsetDp - bottomPadding).coerceAtLeast(bottomPadding)
        },
        animationSpec = tween(durationMillis = 100, easing = LinearOutSlowInEasing),
        label = "BottomSheetAnim"
    )

    LaunchedEffect(showPickerOnLaunch) {
        if (!isPreview && showPickerOnLaunch != null) {
            showPicker = showPickerOnLaunch
            Log.d("AffordanceOverlay", "showPickerOnLaunch=$showPickerOnLaunch")
        }
    }
    
    val bottomOffset = bottomPadding * scale + transY

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(
                bottom = if (isPreview) bottomPadding / 2 else bottomOffset,
                start = 16.dp * scale,
                end = 16.dp * scale
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AffordanceButton(
            isPreview = isPreview,
            affordanceKey = startAffordance,
            scale = scale,
            selected = showPicker == AffordanceSlot.BOTTOM_START || isPreview,
            onClick = { showPicker = AffordanceSlot.BOTTOM_START }
        )

        AffordanceButton(
            isPreview = isPreview,
            affordanceKey = endAffordance,
            scale = scale,
            selected = showPicker == AffordanceSlot.BOTTOM_END || isPreview,
            onClick = { showPicker = AffordanceSlot.BOTTOM_END }
        )
    }
    
    if (isPreview) return

    showPicker?.let { slot ->
        AffordancePickerSheet(
            visible = true,
            currentSlot = slot,
            currentValue = if (slot == AffordanceSlot.BOTTOM_START) startAffordance else endAffordance,
            onDismiss = { showPicker = null },
            onSelect = { selected ->
                writeAffordance(resolver, slot, selected.key)
                if (slot == AffordanceSlot.BOTTOM_START) {
                    startAffordance = selected.key
                } else {
                    endAffordance = selected.key
                }
            },
            onOffsetChanged = { offset, maxOffset ->
                sheetOffset = offset
                sheetMaxOffset = maxOffset
            }
        )
    }
}

@Composable
private fun AffordanceButton(
    affordanceKey: String,
    scale: Float,
    onClick: () -> Unit,
    isPreview: Boolean,
    selected: Boolean = false 
) {
    val colors = MaterialTheme.colorScheme
    val affordance = AffordancesList.options.find { it.key == affordanceKey }
    val showIcon = affordance != null && affordanceKey != "none"
    val buttonSize = if (isPreview) 48.dp else 64.dp
    val iconSize = if (isPreview) 18.dp else 28.dp
    
    val hide = !showIcon && isPreview
    val unselected = !selected && !hide
    
    Box(
        modifier = Modifier
            .size(buttonSize * scale)
            .clip(CircleShape)
            .background(
                color = if (selected && showIcon) colors.surface else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = if (unselected) 1.dp else 0.dp,
                color = if (unselected) Color.White else Color.Transparent,
                shape = CircleShape
            )
            .clickable(
                enabled = !isPreview,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (showIcon) {
            Icon(
                imageVector = affordance.icon,
                contentDescription = affordance.label,
                tint = if (selected) colors.onSurface else Color.White,
                modifier = Modifier.size(iconSize * scale)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_affordance),
                tint = if (hide) Color.Transparent else Color.White,
                modifier = Modifier.size(iconSize * scale)
            )
        }
    }
}

@Composable
fun AffordancePickerSheet(
    visible: Boolean,
    currentSlot: AffordanceSlot,
    currentValue: String,
    onDismiss: () -> Unit,
    onSelect: (AffordanceOption) -> Unit,
    onOffsetChanged: ((Float, Float) -> Unit)? = null
) {
    val titleText = if (currentSlot == AffordanceSlot.BOTTOM_START) 
        stringResource(R.string.left_affordance) else stringResource(R.string.right_affordance)

    CommonBottomSheet(
        visible = visible,
        title = titleText,
        onDismiss = onDismiss,
        onOffsetChanged = onOffsetChanged
    ) {
        PagedTilePicker(
            items = AffordancesList.options,
            icon = { it.icon },
            label = { it.label },
            selected = { it.key == currentValue },
            onSelect = onSelect
        )
    }
}
