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

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.themepicker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import com.android.axion.themepicker.ui.lockscreen.Dimens

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> PagedTilePicker(
    items: List<T>,
    icon: @Composable (T) -> ImageVector,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    sheetHeightFraction: Float = 0.4f,
    selected: (T) -> Boolean = { false },
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val itemsPerPage = 2
    val pageCount = (items.size + itemsPerPage - 1) / itemsPerPage
    val pagerState = rememberPagerState { pageCount }

    val screenHeight =
        LocalDensity.current.run { context.resources.displayMetrics.heightPixels.toDp() }
    val sheetHeight = screenHeight * sheetHeightFraction

    Column(
        modifier = modifier.fillMaxWidth().height(sheetHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) { page ->
            val pageItems = items.drop(page * itemsPerPage).take(itemsPerPage)
            Row(
                modifier =
                    Modifier.fillMaxWidth().padding(horizontal = SheetDimens.SheetPagerPadding),
                horizontalArrangement = Arrangement.spacedBy(SheetDimens.SheetPagerSpacing),
            ) {
                pageItems.forEach { item ->
                    PagedTile(
                        icon = icon(item),
                        label = label(item),
                        isSelected = selected(item),
                        onClick = { onSelect(item) },
                        modifier = Modifier.weight(1f),
                    )
                }

                if (pageItems.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(SheetDimens.SheetSpacerMedium))
        PageIndicator(pageCount, pagerState.currentPage)
        Spacer(Modifier.height(SheetDimens.SheetPagerSpacingNav))
    }
}

@Composable
private fun PagedTile(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            BoxWithConstraints(
                modifier =
                    Modifier.matchParentSize()
                        .clip(RoundedCornerShape(Dimens.TileCorner))
                        .background(if (isSelected) colors.primaryContainer else colors.surface)
                        .border(
                            Dimens.TileBorder,
                            if (isSelected) colors.primary else colors.surfaceVariant,
                            RoundedCornerShape(Dimens.TileCorner),
                        )
                        .clickable { onClick() }
            ) {
                val cardSize = maxWidth
                val circleSize = cardSize * 1.4f

                Box(
                    modifier =
                        Modifier.size(circleSize)
                            .offset(x = circleSize * 0.16f, y = circleSize * 0.16f)
                            .clip(CircleShape)
                            .background(if (isSelected) colors.primary else colors.surfaceVariant)
                            .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) colors.onPrimary else colors.onSurface,
                        modifier = Modifier.size(cardSize * 0.22f),
                    )
                }
            }

            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) colors.onPrimaryContainer else colors.onSurface,
                modifier =
                    Modifier.size(Dimens.TileIcon)
                        .align(Alignment.TopStart)
                        .aspectRatio(1f)
                        .offset(
                            x = SheetDimens.SheetPagerPadding,
                            y = SheetDimens.SheetPagerPadding,
                        ),
            )
        }

        Spacer(Modifier.height(Dimens.TileTextSpacer))
        Text(text = label, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    val colors = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val animProgress by
                animateFloatAsState(
                    targetValue = if (index == currentPage) 1f else 0f,
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                )

            val indicatorWidth = Dimens.TilePagerIndicator + (16.dp * animProgress)
            Box(
                modifier =
                    Modifier.padding(horizontal = 4.dp)
                        .height(Dimens.TilePagerIndicator)
                        .width(indicatorWidth * 2)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) colors.primary else colors.surfaceVariant
                        )
            )
        }
    }
}
