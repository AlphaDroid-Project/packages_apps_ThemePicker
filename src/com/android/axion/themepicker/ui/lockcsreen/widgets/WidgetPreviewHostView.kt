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
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams
import kotlin.math.min
import kotlin.math.roundToInt

class WidgetPreviewHostView(context: Context) : AppWidgetHostView(context) {

    private var containerWidthPx: Int = 0
    private var containerHeightPx: Int = 0

    init {
        clipToPadding = false
        clipChildren = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        isFocusable = false
        descendantFocusability = FOCUS_BLOCK_DESCENDANTS
    }

    fun setContainerSizePx(widthPx: Int, heightPx: Int) {
        containerWidthPx = widthPx
        containerHeightPx = heightPx
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = true

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount == 0 || containerWidthPx <= 0 || containerHeightPx <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val child = getChildAt(0)
        measureChild(child)

        val childWidth = child.measuredWidth.coerceAtLeast(1)
        val childHeight = child.measuredHeight.coerceAtLeast(1)

        val widthScale = containerWidthPx.toFloat() / childWidth
        val heightScale = containerHeightPx.toFloat() / childHeight
        val scale = min(widthScale, heightScale)

        child.scaleX = scale
        child.scaleY = scale
        child.pivotX = 0f
        child.pivotY = 0f

        val scaledWidth = (childWidth * scale).roundToInt()
        val scaledHeight = (childHeight * scale).roundToInt()
        setMeasuredDimension(scaledWidth, scaledHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (childCount > 0) {
            val child = getChildAt(0)
            child.layout(0, 0, child.measuredWidth, child.measuredHeight)
        }
    }

    private fun measureChild(child: View) {
        val lp =
            child.layoutParams ?: LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        val childWidthSpec =
            when (lp.width) {
                LayoutParams.MATCH_PARENT ->
                    MeasureSpec.makeMeasureSpec(containerWidthPx, MeasureSpec.EXACTLY)
                LayoutParams.WRAP_CONTENT ->
                    MeasureSpec.makeMeasureSpec(containerWidthPx, MeasureSpec.AT_MOST)
                else -> MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY)
            }
        val childHeightSpec =
            when (lp.height) {
                LayoutParams.MATCH_PARENT ->
                    MeasureSpec.makeMeasureSpec(containerHeightPx, MeasureSpec.EXACTLY)
                LayoutParams.WRAP_CONTENT ->
                    MeasureSpec.makeMeasureSpec(containerHeightPx, MeasureSpec.AT_MOST)
                else -> MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY)
            }

        child.measure(childWidthSpec, childHeightSpec)
    }
}
