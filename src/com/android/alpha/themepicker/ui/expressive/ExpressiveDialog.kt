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

package com.android.alpha.themepicker.ui.expressive

import androidx.compose.foundation.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.android.alpha.themepicker.R

@Composable
fun ExpressiveDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.confirm),
    dismissText: String = stringResource(R.string.cancel),
    confirmButtonColors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    onConfirm: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {},
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                )
            },
            containerColor = colors.surfaceContainerHigh,
            confirmButton = {
                FilledTonalButton(
                    onClick = onConfirm,
                    shape = MaterialTheme.shapes.large,
                    colors = confirmButtonColors,
                ) {
                    Text(confirmText, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, shape = MaterialTheme.shapes.large) {
                    Text(dismissText, fontWeight = FontWeight.Medium)
                }
            },
            shape = MaterialTheme.shapes.extraLargeIncreased,
        )
    }
}
