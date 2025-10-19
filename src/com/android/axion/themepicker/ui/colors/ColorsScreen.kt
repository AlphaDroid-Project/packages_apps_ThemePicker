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
package com.android.axion.themepicker.ui.colors

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.axion.themepicker.ui.colors.AdvancedColorsSettings
import com.android.axion.themepicker.ui.colors.BasicColorsSettings
import com.android.axion.themepicker.ui.expressive.ExpressiveHeader
import com.android.axion.themepicker.ui.preview.CalculatorPreview
import com.android.axion.themepicker.ui.preview.QuickSettingsPreview
import com.android.axion.themepicker.ui.preview.WorkspacePreview
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.viewmodel.MainScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorsSettingsScreen(
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = 0, 
        pageCount = { 3 } 
    )
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(LocalAxColorScheme.current.surfaceContainerLow)) {
        ExpressiveHeader(
            title = "Colors",
            onBackClick = { mainScreenViewModel.resetToMain() },
        )

        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(204.dp),
            modifier = Modifier.height(420.dp).padding(top = 16.dp)
        ) { page ->
            when (page) {
                0 -> WorkspacePreview()
                1 -> CalculatorPreview()
                2 -> QuickSettingsPreview()
            }
        }

        ColorsSettings(
            tabIndex = selectedTab,
            onColorModeChanged = { selectedTab = it },
            modifier = Modifier.wrapContentSize().padding(top = 16.dp, bottom = 24.dp)
        )
    }
}

@Composable
fun ColorsSettings(
    tabIndex: Int,
    onColorModeChanged: (Int) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
    ) {
        ColorsTabs(tabIndex = tabIndex, onColorModeChanged = onColorModeChanged)
        Column(
            modifier = Modifier
                .padding(top = 16.dp)
        ) {
            when (tabIndex) {
                0 -> BasicColorsSettings()
                1 -> AdvancedColorsSettings()
            }
        }
    }
}

@Composable
fun ColorsTabs(
    tabIndex: Int,
    onColorModeChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAxColorScheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Color Customization", "Advanced").forEachIndexed { index, title ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (tabIndex == index) colors.primary else colors.surfaceContainerLowest,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onColorModeChanged(index) }
                    .clip(RoundedCornerShape(12.dp))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = if (tabIndex == index) 
                        colors.textPrimaryInverse
                    else 
                        colors.textPrimary
                )
            }
        }
    }
}
