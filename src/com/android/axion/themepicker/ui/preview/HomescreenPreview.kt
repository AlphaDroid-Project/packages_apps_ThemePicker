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
package com.android.axion.themepicker.ui.preview

import android.app.WallpaperManager
import android.app.WallpaperColors
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.SurfaceView
import android.view.SurfaceHolder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.wallpaper.util.PreviewUtils
import com.android.wallpaper.util.SurfaceViewUtils
import com.android.axion.themepicker.utils.wallpaper.getCurrentWallpaperDrawable
import kotlinx.coroutines.*

private const val TAG = "HomescreenPreview"

@Composable
fun HomescreenPreview(
    modifier: Modifier = Modifier,
    wallpaperDrawable: Drawable? = null,
    refreshKey: Any? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewUtils = remember { PreviewUtils(context = context, authorityMetadataKey = "com.android.launcher3.grid.control") }
    val wallpaperManager = remember { WallpaperManager.getInstance(context) }
    val drawable = wallpaperDrawable ?: remember { getCurrentWallpaperDrawable(context, true) }

    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        if (refreshKey != null) refreshTrigger++
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                SurfaceView(ctx).apply {
                    holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            val canvas = holder.lockCanvas()
                            drawable?.setBounds(0, 0, canvas.width, canvas.height)
                            drawable?.draw(canvas)
                            holder.unlockCanvasAndPost(canvas)
                        }

                        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                        override fun surfaceDestroyed(holder: SurfaceHolder) {}
                    })
                }
            },
            update = { surfaceView ->
                surfaceView.holder.surface?.takeIf { it.isValid }?.let {
                    try {
                        val canvas = surfaceView.holder.lockCanvas()
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                        drawable?.setBounds(0, 0, canvas.width, canvas.height)
                        drawable?.draw(canvas)
                        surfaceView.holder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating wallpaper", e)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        key(refreshTrigger) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    SurfaceView(ctx).apply {
                        setZOrderMediaOverlay(true)
                        bindWorkspacePreview(
                            surface = this,
                            previewUtils = previewUtils,
                            wallpaperManager = wallpaperManager,
                            lifecycleOwner = lifecycleOwner,
                            context = ctx
                        )
                    }
                },
                update = { surfaceView ->
                    surfaceView.holder.surface?.takeIf { it.isValid }?.let {
                        refreshWorkspacePreview(
                            surface = surfaceView,
                            previewUtils = previewUtils,
                            wallpaperManager = wallpaperManager,
                            context = context
                        )
                    }
                }
            )
        }
    }
}

private fun refreshWorkspacePreview(
    surface: SurfaceView,
    previewUtils: PreviewUtils,
    wallpaperManager: WallpaperManager,
    context: Context
) {
    val scope = CoroutineScope(Dispatchers.Main)
    scope.launch {
        try {
            val wallpaperColors = try {
                wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get wallpaper colors", e)
                null
            }

            renderWorkspacePreview(
                surface = surface,
                previewUtils = previewUtils,
                wallpaperColors = wallpaperColors,
                context = context
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh workspace preview", e)
        }
    }
}

private fun bindWorkspacePreview(
    surface: SurfaceView,
    previewUtils: PreviewUtils,
    wallpaperManager: WallpaperManager,
    lifecycleOwner: LifecycleOwner,
    context: Context
) {
    val scope = lifecycleOwner.lifecycleScope

    var surfaceCallback: SurfaceViewUtils.SurfaceCallback? = null
    surfaceCallback?.let { surface.holder.removeCallback(it) }

    surfaceCallback = object : SurfaceViewUtils.SurfaceCallback {
        var job: Job? = null
        var previewDisposableHandle: DisposableHandle? = null

        override fun surfaceCreated(holder: SurfaceHolder) {
            job?.cancel()
            job = scope.launch {
                try {
                    delay(300)

                    val wallpaperColors = try {
                        wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to get wallpaper colors", e)
                        null
                    }

                    val workspaceCallback = renderWorkspacePreview(
                        surface = surface,
                        previewUtils = previewUtils,
                        wallpaperColors = wallpaperColors,
                        context = context
                    )

                    previewDisposableHandle?.dispose()
                    previewDisposableHandle = DisposableHandle {
                        workspaceCallback?.let { previewUtils.cleanUp(it) }
                    }
                } catch (e: CancellationException) {
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to render workspace preview", e)
                }
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            job?.cancel()
            job = null
            previewDisposableHandle?.dispose()
            previewDisposableHandle = null
        }
    }

    surface.holder.addCallback(surfaceCallback)
}

private suspend fun renderWorkspacePreview(
    surface: SurfaceView,
    previewUtils: PreviewUtils,
    wallpaperColors: WallpaperColors?,
    context: Context
): Message? {
    if (!previewUtils.supportsPreview()) {
        Log.w(TAG, "Preview not supported")
        return null
    }

    val surfacePosition = surface.holder.surfaceFrame
    val width = surfacePosition.width()
    val height = surfacePosition.height()

    if (width == 0 || height == 0) {
        Log.w(TAG, "Skipping workspace preview render: surface is 0x0")
        return null
    }

    return suspendCancellableCoroutine { continuation ->
        try {
            val displayId = context.display?.displayId ?: 0

            val extras = bundleOf(
                SurfaceViewUtils.KEY_DISPLAY_ID to displayId,
                SurfaceViewUtils.KEY_VIEW_WIDTH to width,
                SurfaceViewUtils.KEY_VIEW_HEIGHT to height,
            )

            wallpaperColors?.let {
                extras.putParcelable(SurfaceViewUtils.KEY_WALLPAPER_COLORS, it)
            }

            Log.d(TAG, "Rendering preview with dimensions: ${width}x${height}")
            val request = SurfaceViewUtils.createSurfaceViewRequest(surface, extras)

            previewUtils.renderPreview(
                request,
                object : PreviewUtils.WorkspacePreviewCallback {
                    override fun onPreviewRendered(resultBundle: Bundle?) {
                        if (resultBundle != null) {
                            val surfacePackage = SurfaceViewUtils.getSurfacePackage(resultBundle)
                            if (surfacePackage != null) {
                                surface.setChildSurfacePackage(surfacePackage)
                                Log.d(TAG, "Preview rendered successfully")
                            } else {
                                Log.w(TAG, "Surface package is null")
                            }
                            continuation.resume(
                                SurfaceViewUtils.getCallback(resultBundle),
                                onCancellation = null
                            )
                        } else {
                            Log.w(TAG, "Result bundle is null")
                            continuation.resume(null, onCancellation = null)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering preview", e)
            continuation.resume(null, onCancellation = null)
        }
    }
}
