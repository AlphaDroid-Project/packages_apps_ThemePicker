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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
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
import com.android.axion.themepicker.ui.theme.*
import com.android.axion.themepicker.utils.math.sdp
import com.android.axion.themepicker.viewmodel.MainScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorsSettingsScreen(
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val colors = MaterialTheme.colorScheme
    val design = LocalExpressiveDesign.current
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 3 }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        ExpressiveHeader(
            title = stringResource(id = R.string.colors_title),
            onBackClick = { mainScreenViewModel.resetToMain() },
        )

        if (layoutInfo.isDualPane) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = design.spacing.screenPaddingTablet),
                horizontalArrangement = Arrangement.spacedBy(design.spacing.large)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PreviewCarousel(pagerState = pagerState)
                }
                
                Column(
                    modifier = Modifier
                        .weight(0.55f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    ColorsSectionHeader(
                        modifier = Modifier.padding(vertical = design.spacing.medium)
                    )
                    BasicColorsSettings()
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                PreviewCarousel(
                    pagerState = pagerState,
                    modifier = Modifier.padding(top = design.spacing.medium)
                )

                ColorsSectionHeader(
                    modifier = Modifier.padding(
                        horizontal = design.spacing.screenPadding,
                        vertical = design.spacing.medium
                    )
                )

                BasicColorsSettings()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PreviewCarousel(
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    val layoutInfo = LocalAdaptiveLayoutInfo.current
    val design = LocalExpressiveDesign.current
    
    val previewWidth = if (layoutInfo.isTablet) 200.sdp else 162.sdp
    val previewHeight = if (layoutInfo.isTablet) 400.sdp else 320.sdp

    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fixed(previewWidth),
        contentPadding = PaddingValues(horizontal = design.spacing.medium),
        pageSpacing = design.spacing.small,
        modifier = modifier.height(previewHeight)
    ) { page ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(design.shapes.previewCorner))
        ) {
            when (page) {
                0 -> WorkspacePreview()
                1 -> CalculatorPreview()
                2 -> QuickSettingsPreview()
            }
        }
    }
}

@Composable
fun ColorsSectionHeader(
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val design = LocalExpressiveDesign.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(design.spacing.extraSmall)
    ) {
        Text(
            text = stringResource(id = R.string.customize_palette_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )

        Text(
            text = stringResource(id = R.string.customize_palette_description),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )
    }
}
