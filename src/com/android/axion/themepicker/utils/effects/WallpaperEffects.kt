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

package com.android.axion.themepicker.utils.effects

import android.content.Context
import android.graphics.*
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.graphics.shapes.toPath
import com.google.android.renderscript.Toolkit
import kotlin.math.*

fun applyAtmosphereEffect(context: Context, bitmap: Bitmap): Bitmap {
    return try {
        val source = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val downscaled =
            Bitmap.createScaledBitmap(
                source,
                (source.width * 0.5f).toInt(),
                (source.height * 0.5f).toInt(),
                true,
            )

        val radius = 100
        val blur = 25

        val iterations = radius / blur
        val remainingRadius = radius % blur

        var result =
            if (remainingRadius > 0) {
                Toolkit.blur(downscaled, remainingRadius)
            } else {
                downscaled
            }

        repeat(iterations) { result = Toolkit.blur(result, blur) }

        val output = Bitmap.createScaledBitmap(result, source.width, source.height, true)

        if (result != downscaled) result.recycle()
        downscaled.recycle()
        source.recycle()

        output
    } catch (e: Exception) {
        Log.e("WallpaperEffect", "Error applying atmosphere effect", e)
        bitmap
    }
}

fun applyWeatherPreview(context: Context, bitmap: Bitmap): Bitmap {
    return try {
        val source = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = (pixel shr 24) and 0xFF
            var r = (pixel shr 16) and 0xFF
            var g = (pixel shr 8) and 0xFF
            var b = pixel and 0xFF

            val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            r = ((r * 0.75 + gray * 0.25).toInt()).coerceIn(0, 255)
            g = ((g * 0.75 + gray * 0.25).toInt()).coerceIn(0, 255)
            b = ((b * 0.75 + gray * 0.25).toInt()).coerceIn(0, 255)

            r = (r * 0.92).toInt().coerceIn(0, 255)
            b = min(255, (b * 1.08).toInt())

            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        source.setPixels(pixels, 0, width, 0, 0, width, height)
        val blurred = Toolkit.blur(source, 3)
        source.recycle()
        blurred
    } catch (e: Exception) {
        Log.e("WallpaperEffect", "Error applying weather preview", e)
        bitmap
    }
}

fun applyMagicPortraitPreview(
    context: Context,
    bitmap: Bitmap,
    shapeType: Int,
    color: Int,
): Bitmap {
    return try {
        val w = bitmap.width
        val h = bitmap.height
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        canvas.drawColor(color)

        val shapePath = createPortraitShapePath(shapeType, w, h)
        canvas.save()
        canvas.clipPath(shapePath)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.restore()

        result
    } catch (e: Exception) {
        Log.e("WallpaperEffect", "Error applying magic portrait preview", e)
        bitmap
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun createPortraitShapePath(type: Int, width: Int, height: Int): Path {
    val cx = width / 2f
    val cy = height * 0.42f
    val size = min(width, height) * 0.50f

    val polygon =
        when (type) {
            0 -> MaterialShapes.Pill
            1 -> MaterialShapes.Square
            2 -> MaterialShapes.Arch
            3 -> MaterialShapes.Cookie4Sided
            4 -> MaterialShapes.Cookie6Sided
            else -> MaterialShapes.Pill
        }

    val matrix = Matrix()
    matrix.postTranslate(-0.5f, -0.5f)
    matrix.postScale(size * 2f, size * 2f)
    matrix.postTranslate(cx, cy)

    val path = polygon.toPath()
    path.transform(matrix)
    return path
}

fun applyCinematicPreview(context: Context, bitmap: Bitmap): Bitmap {
    return try {
        val source = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val w = source.width
        val h = source.height

        val blurred = Toolkit.blur(source, 12)
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(blurred, 0f, 0f, null)

        val cx = w / 2f
        val cy = h * 0.45f
        val radius = min(w, h) * 0.30f
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader =
                    RadialGradient(
                        cx,
                        cy,
                        radius,
                        intArrayOf(0xFFFFFFFF.toInt(), 0x00FFFFFF),
                        floatArrayOf(0.4f, 1.0f),
                        Shader.TileMode.CLAMP,
                    )
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            }

        val sharpLayer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val sharpCanvas = Canvas(sharpLayer)
        sharpCanvas.drawBitmap(source, 0f, 0f, null)
        sharpCanvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        canvas.drawBitmap(sharpLayer, 0f, 0f, null)

        val vigPaint =
            Paint().apply {
                shader =
                    RadialGradient(
                        w / 2f,
                        h / 2f,
                        max(w, h) * 0.65f,
                        intArrayOf(0x00000000, 0x55000000),
                        floatArrayOf(0.5f, 1.0f),
                        Shader.TileMode.CLAMP,
                    )
            }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), vigPaint)

        sharpLayer.recycle()
        blurred.recycle()
        source.recycle()
        result
    } catch (e: Exception) {
        Log.e("WallpaperEffect", "Error applying cinematic preview", e)
        bitmap
    }
}

fun applyGlassEffect(context: Context, bitmap: Bitmap): Bitmap {
    return try {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val resultPixels = IntArray(width * height)

        val numBands = 28
        val bandWidth = width / numBands
        val baseAmplitude = 15.0
        val verticalRipple = 2.0
        val brightnessFactor = 0.05

        for (y in 0 until height) {
            val normalizedY = y.toDouble() / height

            for (x in 0 until width) {
                val bandIndex = x / bandWidth
                val bandCenter = bandIndex * bandWidth + bandWidth / 2.0
                val distFromCenter = (x - bandCenter) / bandWidth

                val symmetricWave =
                    sin(distFromCenter * Math.PI * 2) * 0.8 +
                        0.25 * sin(distFromCenter * Math.PI * 4)

                val verticalOffset = sin(normalizedY * Math.PI * 4) * verticalRipple

                val displacementX = (symmetricWave * baseAmplitude).toInt()
                val displacementY = verticalOffset.toInt()

                val srcX = (x + displacementX).coerceIn(0, width - 1)
                val srcY = (y + displacementY).coerceIn(0, height - 1)
                val pixel = pixels[srcY * width + srcX]

                val a = (pixel shr 24) and 0xFF
                var r = (pixel shr 16) and 0xFF
                var g = (pixel shr 8) and 0xFF
                var b = pixel and 0xFF

                val light = 1.0 + brightnessFactor * symmetricWave
                r = (r * light).toInt().coerceIn(0, 255)
                g = (g * light).toInt().coerceIn(0, 255)
                b = (b * light).toInt().coerceIn(0, 255)

                resultPixels[y * width + x] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        result.setPixels(resultPixels, 0, width, 0, 0, width, height)
        result
    } catch (e: Exception) {
        Log.e("WallpaperEffect", "Error applying glass effect", e)
        bitmap
    }
}
