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

package com.android.alpha.themepicker.ui.lockscreen.widgets

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.RemoteViews

class LockscreenAppWidgetHostView(context: Context) : AppWidgetHostView(context) {

    var hasRealViews: Boolean = false
        private set

    private var cornerRadiusPx: Float = 0f

    init {
        clipToOutline = true
        val density = context.resources.displayMetrics.density
        cornerRadiusPx = CORNER_RADIUS_DP * density
        outlineProvider =
            object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadiusPx)
                }
            }
    }

    fun setCornerRadiusPx(radiusPx: Float) {
        if (cornerRadiusPx != radiusPx) {
            cornerRadiusPx = radiusPx
            invalidateOutline()
        }
    }

    override fun setAppWidget(appWidgetId: Int, info: AppWidgetProviderInfo?) {
        super.setAppWidget(appWidgetId, info)
        super.setPadding(0, 0, 0, 0)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(0, 0, 0, 0)
    }

    override fun updateAppWidget(remoteViews: RemoteViews?) {
        if (remoteViews != null) hasRealViews = true
        if (isLaidOut) {
            post {
                super.updateAppWidget(remoteViews)
                super.setPadding(0, 0, 0, 0)
            }
        } else {
            super.updateAppWidget(remoteViews)
            super.setPadding(0, 0, 0, 0)
        }
    }

    companion object {
        private const val CORNER_RADIUS_DP = 16f
    }
}
