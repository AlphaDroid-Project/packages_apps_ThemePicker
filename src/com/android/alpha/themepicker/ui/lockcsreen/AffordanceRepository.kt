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

package com.android.alpha.themepicker.ui.lockscreen

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext

private const val TAG = "AffordanceRepo"
private const val SYSUI_PACKAGE = "com.android.systemui"
private const val MAX_RETRIES = 10
private const val RETRY_DELAY_MS = 500L

data class AffordanceInfo(
    val id: String,
    val name: String,
    val iconResourceId: Int,
    val isEnabled: Boolean,
    val enablementExplanation: String? = null,
)

data class AffordanceSelection(
    val slotId: String,
    val affordanceId: String,
    val affordanceName: String,
)

object AffordanceRepository {

    private val AFFORDANCES_URI: Uri =
        Uri.parse(
            "content://com.android.systemui.customization/lockscreen_quickaffordance/affordances"
        )

    private val SELECTIONS_URI: Uri =
        Uri.parse(
            "content://com.android.systemui.customization/lockscreen_quickaffordance/selections"
        )

    suspend fun queryAffordances(context: Context): List<AffordanceInfo> =
        queryWithRetry(retryOnEmpty = true) {
            context.contentResolver.query(AFFORDANCES_URI, null, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndex("id")
                val nameCol = cursor.getColumnIndex("name")
                val iconCol = cursor.getColumnIndex("icon")
                val enabledCol = cursor.getColumnIndex("is_enabled")
                val explanationCol = cursor.getColumnIndex("enablement_explanation")

                if (idCol == -1 || nameCol == -1 || iconCol == -1) return@use emptyList()

                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            AffordanceInfo(
                                id = cursor.getString(idCol),
                                name = cursor.getString(nameCol),
                                iconResourceId = cursor.getInt(iconCol),
                                isEnabled = enabledCol != -1 && cursor.getInt(enabledCol) == 1,
                                enablementExplanation =
                                    if (explanationCol != -1) cursor.getString(explanationCol)
                                    else null,
                            )
                        )
                    }
                }
            }
        }

    fun observeAffordances(context: Context): Flow<List<AffordanceInfo>> =
        observeUri(context, AFFORDANCES_URI)
            .map { queryAffordances(context) }
            .transformLatest { list ->
                emit(list)
                if (list.isEmpty()) {
                    var attempt = 0
                    while (attempt < MAX_RETRIES) {
                        delay(RETRY_DELAY_MS * (attempt + 1))
                        val retry = queryAffordances(context)
                        emit(retry)
                        if (retry.isNotEmpty()) return@transformLatest
                        attempt++
                    }
                }
            }

    suspend fun querySelections(context: Context): List<AffordanceSelection> = queryWithRetry {
        context.contentResolver.query(SELECTIONS_URI, null, null, null, null)?.use { cursor ->
            val slotCol = cursor.getColumnIndex("slot_id")
            val affordanceCol = cursor.getColumnIndex("affordance_id")
            val nameCol = cursor.getColumnIndex("affordance_name")

            if (slotCol == -1 || affordanceCol == -1) return@use emptyList()

            buildList {
                while (cursor.moveToNext()) {
                    add(
                        AffordanceSelection(
                            slotId = cursor.getString(slotCol),
                            affordanceId = cursor.getString(affordanceCol),
                            affordanceName = if (nameCol != -1) cursor.getString(nameCol) else "",
                        )
                    )
                }
            }
        }
    }

    fun observeSelections(context: Context): Flow<List<AffordanceSelection>> =
        observeUri(context, SELECTIONS_URI).map { querySelections(context) }

    suspend fun selectAffordance(context: Context, slotId: String, affordanceId: String) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.insert(
                    SELECTIONS_URI,
                    ContentValues().apply {
                        put("slot_id", slotId)
                        put("affordance_id", affordanceId)
                    },
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to select affordance $affordanceId for slot $slotId", e)
            }
        }
    }

    suspend fun unselectAll(context: Context, slotId: String) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.delete(SELECTIONS_URI, "slot_id", arrayOf(slotId))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unselect all from slot $slotId", e)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun loadAffordanceIcon(context: Context, iconResourceId: Int): Drawable? {
        if (iconResourceId == 0) return null
        return try {
            context.packageManager
                .getResourcesForApplication(SYSUI_PACKAGE)
                .getDrawable(iconResourceId, context.theme)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load affordance icon $iconResourceId", e)
            null
        }
    }

    private suspend fun <T> queryWithRetry(
        retryOnEmpty: Boolean = false,
        block: () -> List<T>?,
    ): List<T> {
        return withContext(Dispatchers.IO) {
            repeat(MAX_RETRIES) { attempt ->
                try {
                    val result = block()
                    if (result != null && (!retryOnEmpty || result.isNotEmpty())) {
                        return@withContext result
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Query attempt ${attempt + 1}/$MAX_RETRIES failed", e)
                }
                if (attempt < MAX_RETRIES - 1) delay(RETRY_DELAY_MS * (attempt + 1))
            }
            emptyList()
        }
    }

    private fun observeUri(context: Context, uri: Uri): Flow<Unit> =
        callbackFlow {
                val observer =
                    object : ContentObserver(null) {
                        override fun onChange(selfChange: Boolean) {
                            trySend(Unit)
                        }
                    }
                context.contentResolver.registerContentObserver(uri, true, observer)
                awaitClose { context.contentResolver.unregisterContentObserver(observer) }
            }
            .onStart { emit(Unit) }
            .flowOn(Dispatchers.IO)
}
