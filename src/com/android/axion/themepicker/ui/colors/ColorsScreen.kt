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

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.android.axion.themepicker.R
import com.android.axion.themepicker.ui.colors.BasicColorsSettings
import com.android.axion.themepicker.ui.expressive.ExpressiveHeader
import com.android.axion.themepicker.ui.preview.CalculatorPreview
import com.android.axion.themepicker.ui.preview.QuickSettingsPreview
import com.android.axion.themepicker.ui.preview.WorkspacePreview
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme
import com.android.axion.themepicker.utils.math.sdp
import com.android.axion.themepicker.viewmodel.MainScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorsSettingsScreen(
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 3 }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAxColorScheme.current.surfaceContainerLow)
    ) {
        ExpressiveHeader(
            title = stringResource(id = R.string.colors_title),
            onBackClick = { mainScreenViewModel.resetToMain() },
        )

        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(162.sdp),
            modifier = Modifier
                .height(320.sdp)
                .padding(top = 16.sdp)
        ) { page ->
            when (page) {
                0 -> WorkspacePreview()
                1 -> CalculatorPreview()
                2 -> QuickSettingsPreview()
            }
        }

        ColorsSectionHeader(
            modifier = Modifier.padding(
                horizontal = 24.sdp,
                vertical = 24.sdp
            )
        )

        BasicColorsSettings()
    }
}

@Composable
fun ColorsSectionHeader(
    modifier: Modifier = Modifier
) {
    val colors = LocalAxColorScheme.current

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.customize_palette_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            letterSpacing = 0.sp
        )

        Spacer(modifier = Modifier.height(4.sdp))

        Text(
            text = stringResource(id = R.string.customize_palette_description),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = colors.textSecondary,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        )
    }
}
