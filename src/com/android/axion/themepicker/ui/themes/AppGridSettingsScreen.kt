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
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.axion.themepicker.ui.app.PreviewsPage
import com.android.axion.themepicker.ui.expressive.ExpressiveHeader
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.utils.math.scaleRatio
import com.android.axion.themepicker.viewmodel.GridOption
import com.android.axion.themepicker.viewmodel.GridSettingsViewModel
import com.android.axion.themepicker.viewmodel.LayoutScreenViewModel
import com.android.axion.themepicker.viewmodel.MainScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppGridSettingsScreen(
    layoutScreenViewModel: LayoutScreenViewModel,
    mainScreenViewModel: MainScreenViewModel,
    gridViewModel: GridSettingsViewModel = viewModel()
) {
    val colors = LocalAxColorScheme.current
    val scale = LocalContext.current.scaleRatio
    val availableGrids by gridViewModel.availableGridOptions.collectAsState()
    val selectedGrid by gridViewModel.selectedGrid.collectAsState()
    val isLoadingSelection by gridViewModel.isLoadingSelection.collectAsState()

    LaunchedEffect(Unit) {
        gridViewModel.loadGridOptions()
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
                .padding(vertical = 16.dp * scale),
            contentAlignment = Alignment.Center
        ) {
            PreviewsPage(
                isHome = true,
                refreshKey = selectedGrid?.name ?: "default",
                modifier = Modifier
                    .size(162.dp * scale, 360.dp * scale)
                    .clip(RoundedCornerShape(16.dp * scale))
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp * scale, end = 16.dp * scale, top = 24.dp * scale),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceContainerLowest
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp * scale),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp * scale)
            ) {
                Text(
                    text = "Home screen Layout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp * scale)
                )

                if (isLoadingGrids) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp * scale),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp * scale),
                            strokeWidth = 3.dp * scale
                        )
                    }
                } else {
                    if (isLoadingGrids || isLoadingSelection) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp * scale),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp * scale),
                                strokeWidth = 3.dp * scale
                            )
                        }
                    } else {
                        val sortedGrids = availableGrids.sortedWith(GridOption.DESCENDING_COMPARATOR)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp * scale),
                            verticalArrangement = Arrangement.spacedBy(8.dp * scale),
                            horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                        ) {
                            items(sortedGrids) { grid ->
                                GridChip(
                                    grid = grid,
                                    isSelected = selectedGrid?.name == grid.name,
                                    isLoading = isLoadingSelection,
                                    onClick = {
                                        if (!isLoadingSelection) {
                                            gridViewModel.selectGrid(grid)
                                        }
                                    }
                                )
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
    val scale = LocalContext.current.scaleRatio
    val backgroundColor = if (isSelected) colors.primary else colors.surfaceContainerHigh
    val borderColor = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.3f)
    val textColor = if (isSelected) colors.onPrimary else colors.onSurface

    Box(
        modifier = modifier
            .height(40.dp * scale)
            .clip(RoundedCornerShape(20.dp * scale))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp * scale else 1.dp * scale,
                color = borderColor,
                shape = RoundedCornerShape(20.dp * scale)
            )
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(horizontal = 12.dp * scale, vertical = 8.dp * scale),
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
