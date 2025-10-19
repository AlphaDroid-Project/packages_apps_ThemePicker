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
package com.android.axion.themepicker.ui.expressive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.axion.themepicker.ui.theme.LocalAxColorScheme

@Composable
fun ExpressiveHeader(
    title: String,
    subtitle: String? = null,
    onBackClick: () -> Unit,
    onActionClick: (() -> Unit)? = null,
    actionIcon: ImageVector = Icons.Default.Refresh,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = LocalAxColorScheme.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 48.dp, end = 8.dp, bottom = 16.dp)
        ) {
            FilledTonalIconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            if (onActionClick != null) {
                FilledTonalIconButton(
                    onClick = onActionClick,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    shape = RoundedCornerShape(16.dp),
                    enabled = enabled
                ) {
                    Icon(actionIcon, contentDescription = "Action")
                }
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}
