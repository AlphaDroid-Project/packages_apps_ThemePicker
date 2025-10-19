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
package com.android.axion.themepicker.ui.themes

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.axion.themepicker.ui.app.PreviewsPage
import com.android.axion.themepicker.ui.expressive.ExpressiveHeader
import com.android.axion.themepicker.ui.iconpack.GridOption
import com.android.axion.themepicker.ui.iconpack.LauncherSettingsViewModel
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.viewmodel.LayoutScreenViewModel
import com.android.axion.themepicker.viewmodel.MainScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppGridSettingsScreen(
    layoutScreenViewModel: LayoutScreenViewModel,
    mainScreenViewModel: MainScreenViewModel,
    launcherViewModel: LauncherSettingsViewModel = viewModel()
) {
    val colors = LocalAxColorScheme.current
    val availableGrids by launcherViewModel.availableGridOptions.collectAsState()
    val selectedGrid by launcherViewModel.selectedGrid.collectAsState()
    val isLoadingSelection by launcherViewModel.isLoadingSelection.collectAsState()

    LaunchedEffect(Unit) {
        launcherViewModel.loadGridOptions()
    }

    val isLoadingGrids = availableGrids.isEmpty()

    BackHandler {
        layoutScreenViewModel.goBackInLayout(mainScreenViewModel)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceContainerLow)
    ) {
        ExpressiveHeader(
            title = "App Grid",
            subtitle = null,
            onBackClick = { layoutScreenViewModel.goBackInLayout(mainScreenViewModel) },
            onActionClick = null
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            PreviewsPage(
                isHome = true,
                refreshKey = selectedGrid?.name ?: "default",
                modifier = Modifier
                    .size(204.dp, 420.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceContainerLowest
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Home screen Layout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isLoadingGrids) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    }
                } else {
                    if (isLoadingGrids || isLoadingSelection) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    } else {
                        val sortedGrids = availableGrids.sortedWith(GridOption.DESCENDING_COMPARATOR)
                        
                        Log.d("AppGridSettingsScreen", "sortedGrids=${sortedGrids}")

                        sortedGrids.chunked(4).forEach { chunk ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chunk.forEach { grid ->
                                    GridChip(
                                        grid = grid,
                                        isSelected = selectedGrid?.name == grid.name,
                                        isLoading = isLoadingSelection,
                                        onClick = {
                                            if (!isLoadingSelection) {
                                                launcherViewModel.selectGrid(grid)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(4 - chunk.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridChip(
    grid: GridOption,
    isSelected: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current
    val backgroundColor = if (isSelected) colors.primary else colors.surfaceContainerHigh
    val borderColor = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.3f)
    val textColor = if (isSelected) colors.onPrimary else colors.onSurface

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = grid.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
    }
}
