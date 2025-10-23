package com.android.axion.themepicker.utils.effects

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.android.renderscript.Toolkit
import kotlin.math.*

fun applyAtmosphereEffect(context: Context, bitmap: Bitmap): Bitmap {
    return try {
        val source = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val downscaled = Bitmap.createScaledBitmap(
            source,
            (source.width * 0.5f).toInt(),
            (source.height * 0.5f).toInt(),
            true
        )

        val radius = 100
        val blur = 25

        val iterations = radius / blur
        val remainingRadius = radius % blur

        var result = if (remainingRadius > 0) {
            Toolkit.blur(downscaled, remainingRadius)
        } else {
            downscaled
        }

        repeat(iterations) {
            result = Toolkit.blur(result, blur)
        }

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
