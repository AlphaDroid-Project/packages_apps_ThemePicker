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

package com.android.axion.themepicker.ui.lockscreen.widgets

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Bundle
import android.util.Log

class WidgetHostManager(private val context: Context) {

    private val appWidgetManager = AppWidgetManager.getInstance(context)
    private val widgetHost = LockscreenAppWidgetHost(context, PREVIEW_HOST_ID)

    fun startListening() {
        try {
            widgetHost.startListening()
        } catch (e: Exception) {
            Log.e(TAG, "startListening failed", e)
        }
    }

    fun stopListening() {
        try {
            widgetHost.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "stopListening failed", e)
        }
    }

    fun createView(appWidgetId: Int): AppWidgetHostView? {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return null
        return try {
            widgetHost.createView(context, appWidgetId, info)
        } catch (e: Exception) {
            Log.e(TAG, "createView failed for id=$appWidgetId", e)
            null
        }
    }

    fun updateSize(view: AppWidgetHostView, widthDp: Int, heightDp: Int) {
        view.updateAppWidgetSize(Bundle(), widthDp, heightDp, widthDp, heightDp)
    }

    fun setCornerRadius(view: AppWidgetHostView, radiusPx: Float) {
        (view as? LockscreenAppWidgetHostView)?.setCornerRadiusPx(radiusPx)
    }

    companion object {
        private const val TAG = "WidgetHostManager"

        const val SYSTEMUI_HOST_ID = 1027

        const val PREVIEW_HOST_ID = 25270

        fun requiresConfiguration(info: AppWidgetProviderInfo): Boolean {
            if (info.configure == null) return false
            val features = info.widgetFeatures
            val configurationOptional =
                (features and AppWidgetProviderInfo.WIDGET_FEATURE_CONFIGURATION_OPTIONAL != 0) &&
                    (features and AppWidgetProviderInfo.WIDGET_FEATURE_RECONFIGURABLE != 0)
            return !configurationOptional
        }
    }
}
