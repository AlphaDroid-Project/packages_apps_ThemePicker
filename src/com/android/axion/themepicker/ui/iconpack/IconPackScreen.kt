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
package com.android.axion.themepicker.ui.iconpack

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.axion.themepicker.ui.app.PreviewsPage
import com.android.axion.themepicker.ui.expressive.ExpressiveHeader
import com.android.axion.themepicker.ui.preferences.IconButtonCircle
import com.android.axion.themepicker.ui.preferences.PreferenceGroupCard
import com.android.axion.themepicker.ui.preferences.SliderCard
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.utils.math.scaleRatio
import com.android.axion.themepicker.viewmodel.MainScreenViewModel

@Composable
fun IconPackScreen(
    mainScreenViewModel: MainScreenViewModel = viewModel(),
    launcherSettingsViewModel: LauncherSettingsViewModel = viewModel()
) {
    val colors = LocalAxColorScheme.current
    val context = LocalContext.current
    val scale = context.scaleRatio
    val installedIconPacks by launcherSettingsViewModel.installedIconPacks.collectAsState()
    val selectedPack by launcherSettingsViewModel.selectedIconPack
    val themedIconsEnabled by launcherSettingsViewModel.themedIconsEnabled
    val iconSize by launcherSettingsViewModel.iconSize.collectAsState()
    val fontSize by launcherSettingsViewModel.fontSize.collectAsState()
    
    val sidePadding = 16.dp * scale
    val verticalPadding = 16.dp * scale
    val previewHeight = 320.dp * scale
    val previewWidth = 162.dp * scale

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceContainerLow)
    ) {
        ExpressiveHeader(
            title = "Icon Pack",
            onBackClick = { mainScreenViewModel.resetToMain() },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            PreviewsPage(
                isHome = true,
                refreshKey = "$selectedPack-$themedIconsEnabled-$fontSize-$iconSize",
                modifier = Modifier
                    .size(previewWidth, previewHeight)
                    .clip(RoundedCornerShape(16.dp * scale))
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp * scale),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = sidePadding, vertical = verticalPadding)
        ) {
            item {
                IconButtonCircle(
                    icon = Icons.Default.Add,
                    label = "Add",
                    selected = false,
                    onClick = {
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("market://search?q=icon+pack")
                                setPackage("com.android.vending")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                )
            }

            item {
                IconButtonCircle(
                    icon = Icons.Default.Android,
                    label = "Default",
                    selected = !themedIconsEnabled && selectedPack.isEmpty(),
                    onClick = {
                        launcherSettingsViewModel.resetIconPack()
                        launcherSettingsViewModel.setThemedIcons(false)
                    }
                )
            }

            item {
                IconButtonCircle(
                    icon = Icons.Default.Brush,
                    label = "Themed",
                    selected = themedIconsEnabled,
                    onClick = {
                        launcherSettingsViewModel.resetIconPack()
                        launcherSettingsViewModel.setThemedIcons(true)
                    }
                )
            }

            items(installedIconPacks) { item ->
                val isSelected = selectedPack == item.packageName
                IconButtonCircle(
                    bitmap = item.icon,
                    label = item.label,
                    selected = isSelected,
                    onClick = {
                        launcherSettingsViewModel.setIconPack(item.packageName)
                        if (themedIconsEnabled) {
                            launcherSettingsViewModel.setThemedIcons(false)
                        }
                    }
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = sidePadding, vertical = verticalPadding)
        ) {
            PreferenceGroupCard {
                val iconSize by launcherSettingsViewModel.iconSize.collectAsState()
                val fontSize by launcherSettingsViewModel.fontSize.collectAsState()

                var iconSizeValue by remember { mutableStateOf(iconSize.toFloat()) }
                var fontSizeValue by remember { mutableStateOf(fontSize.toFloat()) }

                LaunchedEffect(iconSize) {
                    iconSizeValue = iconSize.toFloat()
                }
                LaunchedEffect(fontSize) {
                    fontSizeValue = fontSize.toFloat()
                }

                SliderCard(
                    title = "Icon Size",
                    value = iconSizeValue,
                    defaultValue = 100f,
                    onValueChangeFinished = {
                        launcherSettingsViewModel.setIconSize(it.toInt())
                    },
                    valueRange = 50f..150f
                )

                Divider()

                SliderCard(
                    title = "Font Size",
                    value = fontSizeValue,
                    defaultValue = 100f,
                    onValueChangeFinished = {
                        launcherSettingsViewModel.setFontSize(it.toInt())
                    },
                    valueRange = 50f..150f
                )
            }
        }
    }
}
