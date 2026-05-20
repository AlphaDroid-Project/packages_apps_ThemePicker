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

package com.android.alpha.themepicker.utils.effects

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.app.wallpaper.WallpaperDescription
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.service.wallpaper.IWallpaperConnection
import android.service.wallpaper.IWallpaperEngine
import android.service.wallpaper.IWallpaperService
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceControl
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager

private const val TAG = "WallpaperEffectConnection"

class WallpaperEffectConnection(
    private val context: Context,
    private val component: ComponentName,
    private val description: WallpaperDescription,
    private val destinationFlag: Int = WallpaperManager.FLAG_SYSTEM,
) : IWallpaperConnection.Stub(), ServiceConnection {

    private var service: IWallpaperService? = null
    private var engine: IWallpaperEngine? = null
    private var containerView: SurfaceView? = null
    private var connected = false
    private var engineReady = false
    private val mirrorSurfaceControls = mutableListOf<SurfaceControl>()
    private var listener: Listener? = null

    interface Listener {
        fun onEngineShown() {}

        fun onDisconnected() {}
    }

    fun setListener(l: Listener?) {
        listener = l
    }

    fun connect(surfaceView: SurfaceView): Boolean {
        containerView = surfaceView

        val intent = Intent(WallpaperService.SERVICE_INTERFACE)
        intent.component = this.component
        Log.d(TAG, "connect: binding to ${this.component.flattenToShortString()}")
        return try {
            context
                .bindService(intent, this, Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT)
                .also {
                    connected = it
                    Log.d(TAG, "connect: bindService=$it")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind wallpaper service", e)
            false
        }
    }

    fun disconnect() {
        connected = false
        destroyEngine()
        try {
            context.unbindService(this)
        } catch (_: Exception) {}
        service = null
        containerView = null
        listener?.onDisconnected()
        listener = null
    }

    private fun destroyEngine() {
        engine?.let { eng ->
            try {
                eng.destroy()
            } catch (_: RemoteException) {}
            for (sc in mirrorSurfaceControls) {
                sc.release()
            }
            mirrorSurfaceControls.clear()
        }
        engine = null
        engineReady = false
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        Log.d(TAG, "onServiceConnected: $name")
        service = IWallpaperService.Stub.asInterface(binder)
        val sv = containerView ?: return
        if (sv.display != null) {
            attachConnection(sv.display.displayId)
        } else {
            sv.addOnAttachStateChangeListener(
                object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        attachConnection(v.display.displayId)
                        sv.removeOnAttachStateChangeListener(this)
                    }

                    override fun onViewDetachedFromWindow(v: View) {}
                }
            )
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        service = null
        engine = null
        Log.w(TAG, "Wallpaper service gone: $name")
    }

    private fun attachConnection(displayId: Int) {
        val sv = containerView ?: return
        val svc = service ?: return
        val token =
            sv.windowToken
                ?: run {
                    Log.w(TAG, "attachConnection: windowToken is null")
                    return
                }
        Log.d(TAG, "attachConnection: display=$displayId, size=${sv.width}x${sv.height}")
        try {
            svc.attach(
                this,
                token,
                WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA,
                true,
                sv.width,
                sv.height,
                Rect(0, 0, 0, 0),
                displayId,
                destinationFlag,
                null,
                description,
            )
        } catch (e: RemoteException) {
            Log.w(TAG, "Failed attaching wallpaper", e)
        }
    }

    override fun attachEngine(engine: IWallpaperEngine, displayId: Int) {
        Log.d(TAG, "attachEngine: displayId=$displayId, connected=$connected")
        if (!connected) {
            try {
                engine.destroy()
            } catch (_: RemoteException) {}
            return
        }
        this.engine = engine
        try {
            val sv = containerView ?: return
            val display = sv.display ?: return
            val size = Point()
            display.getRealSize(size)
            engine.setVisibility(true)
            engine.resizePreview(Rect(0, 0, size.x, size.y))
        } catch (e: RemoteException) {
            Log.w(TAG, "Error in attachEngine", e)
        }
    }

    override fun engineShown(engine: IWallpaperEngine) {
        Log.d(TAG, "engineShown")
        engineReady = true
        containerView?.post {
            reparentWallpaperSurface()
            listener?.onEngineShown()
        }
    }

    override fun setWallpaper(name: String?): ParcelFileDescriptor? = null

    override fun onWallpaperColorsChanged(colors: WallpaperColors?, displayId: Int) {}

    override fun onLocalWallpaperColorsChanged(
        area: RectF?,
        colors: WallpaperColors?,
        displayId: Int,
    ) {}

    private fun reparentWallpaperSurface() {
        val sv = containerView ?: return
        val eng = engine ?: return

        if (sv.surfaceControl != null) {
            mirrorAndReparent(sv, eng)
        } else {
            sv.holder.addCallback(
                object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        mirrorAndReparent(sv, eng)
                        sv.holder.removeCallback(this)
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int,
                    ) {}

                    override fun surfaceDestroyed(holder: SurfaceHolder) {}
                }
            )
        }
    }

    private fun mirrorAndReparent(parentSurface: SurfaceView, eng: IWallpaperEngine) {
        try {
            val parentSC = parentSurface.surfaceControl ?: return
            val mirrorSC = eng.mirrorSurfaceControl() ?: return

            val surfaceFrame = parentSurface.holder.surfaceFrame
            val display = parentSurface.display
            val displaySize = Point()
            display.getRealSize(displaySize)

            val scaleX = surfaceFrame.width().toFloat() / displaySize.x
            val scaleY = surfaceFrame.height().toFloat() / displaySize.y

            val m = Matrix()
            m.postScale(scaleX, scaleY)
            val values = FloatArray(9)
            m.getValues(values)

            SurfaceControl.Transaction().use { t ->
                t.setMatrix(
                    mirrorSC,
                    values[Matrix.MSCALE_X],
                    values[Matrix.MSKEW_Y],
                    values[Matrix.MSKEW_X],
                    values[Matrix.MSCALE_Y],
                )
                t.reparent(mirrorSC, parentSC)
                t.show(mirrorSC)
                t.apply()
            }
            mirrorSurfaceControls.add(mirrorSC)
        } catch (e: Exception) {
            Log.e(TAG, "Couldn't reparent wallpaper surface", e)
        }
    }
}
