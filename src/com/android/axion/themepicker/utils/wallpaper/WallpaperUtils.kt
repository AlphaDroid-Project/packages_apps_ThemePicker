package com.android.axion.themepicker.utils.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.*
import android.view.View
import android.util.Log
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.painter.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.android.axion.themepicker.data.model.EffectConfig
import com.android.axion.themepicker.data.model.WallpaperCategory
import com.android.axion.themepicker.data.model.WallpaperInfo
import com.android.axion.themepicker.data.model.ZoomProperties
import com.android.axion.themepicker.utils.effects.applyAtmosphereEffect
import com.android.axion.themepicker.utils.effects.applyGlassEffect
import org.xmlpull.v1.XmlPullParser
import kotlin.coroutines.*
import kotlinx.coroutines.*
import kotlin.math.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

private val BACKGROUNDS_PKG_NAME = "com.android.backgrounds"

private val bitmapCache = LruCache<Int, Bitmap>((Runtime.getRuntime().maxMemory() / 1024 / 8).toInt())

private val MAIN_HANDLER by lazy(LazyThreadSafetyMode.NONE) {
    Handler(Looper.getMainLooper())
}

/**
 * A [Painter] which draws an Android [Drawable] and supports [Animatable] drawables. Instances
 * should be remembered to be able to start and stop [Animatable] animations.
 *
 * Instances are usually retrieved from [rememberDrawablePainter].
 */
class DrawablePainter(
    val drawable: Drawable
) : Painter(), RememberObserver {
    private var drawInvalidateTick by mutableStateOf(0)
    private var drawableIntrinsicSize by mutableStateOf(drawable.intrinsicSize)

    private val callback: Drawable.Callback by lazy {
        object : Drawable.Callback {
            override fun invalidateDrawable(d: Drawable) {
                // Update the tick so that we get re-drawn
                drawInvalidateTick++
                // Update our intrinsic size too
                drawableIntrinsicSize = drawable.intrinsicSize
            }

            override fun scheduleDrawable(d: Drawable, what: Runnable, time: Long) {
                MAIN_HANDLER.postAtTime(what, time)
            }

            override fun unscheduleDrawable(d: Drawable, what: Runnable) {
                MAIN_HANDLER.removeCallbacks(what)
            }
        }
    }

    init {
        if (drawable.intrinsicWidth >= 0 && drawable.intrinsicHeight >= 0) {
            // Update the drawable's bounds to match the intrinsic size
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        }
    }

    override fun onRemembered() {
        drawable.callback = callback
        drawable.setVisible(true, true)
        if (drawable is Animatable) drawable.start()
    }

    override fun onAbandoned() = onForgotten()

    override fun onForgotten() {
        if (drawable is Animatable) drawable.stop()
        drawable.setVisible(false, false)
        drawable.callback = null
    }

    override fun applyAlpha(alpha: Float): Boolean {
        drawable.alpha = (alpha * 255).roundToInt().coerceIn(0, 255)
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        drawable.colorFilter = colorFilter?.asAndroidColorFilter()
        return true
    }

    override fun applyLayoutDirection(layoutDirection: LayoutDirection): Boolean {
        return drawable.setLayoutDirection(
            when (layoutDirection) {
                LayoutDirection.Ltr -> View.LAYOUT_DIRECTION_LTR
                LayoutDirection.Rtl -> View.LAYOUT_DIRECTION_RTL
            }
        )
    }

    override val intrinsicSize: Size get() = drawableIntrinsicSize

    override fun DrawScope.onDraw() {
        drawIntoCanvas { canvas ->
            // Reading this ensures that we invalidate when invalidateDrawable() is called
            drawInvalidateTick

            // Update the Drawable's bounds
            drawable.setBounds(0, 0, size.width.roundToInt(), size.height.roundToInt())

            canvas.withSave {
                drawable.draw(canvas.nativeCanvas)
            }
        }
    }
}

@Composable
fun rememberDrawablePainter(drawable: Drawable?): Painter = remember(drawable) {
    when (drawable) {
        null -> EmptyPainter
        is BitmapDrawable -> BitmapPainter(drawable.bitmap.asImageBitmap())
        is ColorDrawable -> ColorPainter(Color(drawable.color))
        else -> DrawablePainter(drawable.mutate())
    }
}

private val Drawable.intrinsicSize: Size
    get() = when {
        intrinsicWidth >= 0 && intrinsicHeight >= 0 -> {
            Size(width = intrinsicWidth.toFloat(), height = intrinsicHeight.toFloat())
        }
        else -> Size.Unspecified
    }

internal object EmptyPainter : Painter() {
    override val intrinsicSize: Size get() = Size.Unspecified
    override fun DrawScope.onDraw() {}
}

fun getCurrentWallpaperBitmap(context: Context, isHome: Boolean = true): Bitmap? {
    val flag = if (isHome) WallpaperManager.FLAG_SYSTEM else WallpaperManager.FLAG_LOCK
    val wallpaperManager = WallpaperManager.getInstance(context)
    val wallBitmap = wallpaperManager.getDrawable(flag)?.toBitmap() ?: wallpaperManager.drawable!!.toBitmap()
    return wallBitmap
}

fun getCurrentWallpaperDrawable(context: Context, isHome: Boolean = true): Drawable? {
    return BitmapDrawable(context.resources, getCurrentWallpaperBitmap(context, isHome))
}

fun applyWallpaper(
    context: Context,
    lockscreenBitmap: Bitmap?,
    homescreenBitmap: Bitmap?,
    lockscreenSelected: Boolean,
    homescreenSelected: Boolean
) {
    WallpaperManager.getInstance(context).apply {
        try {
            homescreenBitmap?.takeIf { homescreenSelected }?.let { bitmap ->
                val flags = if (lockscreenSelected)
                    WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                else
                    WallpaperManager.FLAG_SYSTEM
                setBitmap(bitmap, null, false, flags)
            }

            lockscreenBitmap?.takeIf { lockscreenSelected }?.let { bitmap ->
                setBitmap(bitmap, null, false, WallpaperManager.FLAG_LOCK)
            }
        } catch (e: Exception) {
            Log.e("WallpaperApply", "Error applying wallpaper", e)
        }
    }
}

fun loadWallpapers(context: Context): List<WallpaperInfo> {
    val result = mutableListOf<WallpaperInfo>()

    try {
        val pm = context.packageManager
        val res = pm.getResourcesForApplication(BACKGROUNDS_PKG_NAME)
        val xmlId = res.getIdentifier("wallpapers", "xml", BACKGROUNDS_PKG_NAME)
        Log.d("ThemePicker", "XML id = $xmlId")

        if (xmlId == 0) return emptyList()

        val parser = res.getXml(xmlId)
        val parsedItems = mutableListOf<Triple<String, Int, Int>>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "static-wallpaper") {
                val id = parser.getAttributeValue(null, "id") ?: ""
                val drawableRes = parser.getAttributeResourceValue(null, "src", 0)
                val titleRes = parser.getAttributeResourceValue(null, "title", 0)
                parsedItems.add(Triple(id, drawableRes, titleRes))
            }
            eventType = parser.next()
        }

        for ((id, drawableRes, titleRes) in parsedItems) {
            try {
                val title = if (titleRes != 0) res.getString(titleRes) else null
                if (drawableRes != 0) {
                    result.add(WallpaperInfo(id, title, drawableRes))
                }
            } catch (e: Exception) {
                Log.w("ThemePicker", "Failed to load wallpaper for id=$id", e)
            }
        }

        Log.d("ThemePicker", "Parsed ${result.size} wallpapers")
        return result.asReversed().take(8)

    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("ThemePicker", "Backgrounds package not found", e)
    } catch (e: Exception) {
        Log.e("ThemePicker", "Error parsing wallpapers", e)
    }

    return emptyList()
}

fun getWallpaperDrawable(context: Context, resId: Int): Drawable? {
    if (resId == -1) return null
    val pm = context.packageManager
    val res = runCatching { pm.getResourcesForApplication(BACKGROUNDS_PKG_NAME) }.getOrNull()
    val drawable = runCatching { res?.getDrawable(resId, null) }.getOrNull()
    return drawable
}

class BitmapProcessor(private val context: Context) {
    private val cache = mutableMapOf<String, Bitmap>()
    fun processBitmap(
        source: Bitmap,
        config: EffectConfig,
        cacheKey: String? = null
    ): Bitmap {
        val key = cacheKey ?: "${source.hashCode()}_${config.hashCode()}"
        
        cache[key]?.let { return it }

        var result = source.copy(Bitmap.Config.ARGB_8888, true)
        if (config.atmosphere) {
            result = applyAtmosphereEffect(context, result)
        }
        
        if (config.glass) {
            result = applyGlassEffect(context, result)
        }
        
        cache[key] = result
        return result
    }
    
    fun clearCache() {
        cache.clear()
    }
}

fun applyZoomToBitmap(
    source: Bitmap,
    zoom: ZoomProperties,
    targetWidth: Int,
    targetHeight: Int
): Bitmap {
    if (zoom.scale <= 1f) return source

    val scaledWidth = (source.width * zoom.scale).toInt()
    val scaledHeight = (source.height * zoom.scale).toInt()

    val scaledBitmap = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)

    if (scaledWidth <= targetWidth || scaledHeight <= targetHeight) {
        val safeWidth = targetWidth.coerceAtMost(scaledBitmap.width)
        val safeHeight = targetHeight.coerceAtMost(scaledBitmap.height)
        return Bitmap.createBitmap(scaledBitmap, 0, 0, safeWidth, safeHeight)
    }

    val centerX = scaledWidth / 2
    val centerY = scaledHeight / 2

    val maxCropX = (scaledWidth - targetWidth).coerceAtLeast(0)
    val maxCropY = (scaledHeight - targetHeight).coerceAtLeast(0)

    val cropX = (centerX - targetWidth / 2 - zoom.offsetX)
        .toInt()
        .coerceIn(0, maxCropX)

    val cropY = (centerY - targetHeight / 2 - zoom.offsetY)
        .toInt()
        .coerceIn(0, maxCropY)

    return Bitmap.createBitmap(
        scaledBitmap,
        cropX,
        cropY,
        targetWidth.coerceAtMost(scaledBitmap.width),
        targetHeight.coerceAtMost(scaledBitmap.height)
    )
}

@Composable
fun rememberBitmap(
    drawableRes: Int,
    targetSizeWidth: Dp,
    targetSizeHeight: Dp
): Bitmap? {
    val context = LocalContext.current
    val density = LocalDensity.current
    val targetWidthPx = with(density) { targetSizeWidth.roundToPx() }
    val targetHeightPx = with(density) { targetSizeHeight.roundToPx() }

    return produceState<Bitmap?>(initialValue = null, drawableRes) {
        bitmapCache.get(drawableRes)?.let {
            value = it
            return@produceState
        }

        val bmp = withContext(Dispatchers.IO) {
            getWallpaperDrawable(context, drawableRes)?.toBitmap()?.let { original ->
                val targetAspectRatio = targetWidthPx.toFloat() / targetHeightPx
                val bmpAspectRatio = original.width.toFloat() / original.height

                val (cropWidth, cropHeight, cropLeft, cropTop) = if (bmpAspectRatio > targetAspectRatio) {
                    val h = original.height
                    val w = (h * targetAspectRatio).toInt()
                    Quad(w, h, (original.width - w) / 2, 0)
                } else {
                    val w = original.width
                    val h = (w / targetAspectRatio).toInt()
                    Quad(w, h, 0, (original.height - h) / 2)
                }

                val cropped = Bitmap.createBitmap(
                    original,
                    cropLeft.coerceAtLeast(0),
                    cropTop.coerceAtLeast(0),
                    cropWidth.coerceAtLeast(1),
                    cropHeight.coerceAtLeast(1)
                )

                val scaled = Bitmap.createScaledBitmap(cropped, targetWidthPx, targetHeightPx, true)

                val outputStream = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                val compressedBytes = outputStream.toByteArray()
                BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
            }
        }

        bmp?.let { bitmapCache.put(drawableRes, it) }
        value = bmp
    }.value
}

fun loadAllCategories(context: Context): List<WallpaperCategory> {
    val categories = mutableListOf<WallpaperCategory>()

    try {
        val pm = context.packageManager
        val res = pm.getResourcesForApplication(BACKGROUNDS_PKG_NAME)
        val xmlId = res.getIdentifier("wallpapers", "xml", BACKGROUNDS_PKG_NAME)
        
        if (xmlId == 0) return emptyList()

        val parser = res.getXml(xmlId)
        var currentCategory: String? = null
        var currentTitle: String? = null
        val currentWallpapers = mutableListOf<WallpaperInfo>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "category" -> {
                            if (currentCategory != null && currentWallpapers.isNotEmpty()) {
                                categories.add(
                                    WallpaperCategory(
                                        id = currentCategory,
                                        title = currentTitle ?: currentCategory,
                                        wallpapers = currentWallpapers.toList()
                                    )
                                )
                                currentWallpapers.clear()
                            }
                            
                            currentCategory = parser.getAttributeValue(null, "id")
                            val titleRes = parser.getAttributeResourceValue(null, "title", 0)
                            currentTitle = if (titleRes != 0) {
                                try { res.getString(titleRes) } catch (e: Exception) { currentCategory }
                            } else {
                                currentCategory
                            }
                        }
                        "static-wallpaper" -> {
                            val id = parser.getAttributeValue(null, "id") ?: ""
                            val drawableRes = parser.getAttributeResourceValue(null, "src", 0)
                            val titleRes = parser.getAttributeResourceValue(null, "title", 0)
                            
                            if (drawableRes != 0) {
                                val title = if (titleRes != 0) {
                                    try { res.getString(titleRes) } catch (e: Exception) { null }
                                } else null
                                
                                currentWallpapers.add(
                                    WallpaperInfo(id, title, drawableRes)
                                )
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        if (currentCategory != null && currentWallpapers.isNotEmpty()) {
            categories.add(
                WallpaperCategory(
                    id = currentCategory,
                    title = currentTitle ?: currentCategory,
                    wallpapers = currentWallpapers.toList()
                )
            )
        }

        Log.d("WallpaperGallery", "Loaded ${categories.size} categories")
        
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("WallpaperGallery", "Backgrounds package not found", e)
    } catch (e: Exception) {
        Log.e("WallpaperGallery", "Error loading categories", e)
    }

    return categories
}

private data class Quad(val width: Int, val height: Int, val left: Int, val top: Int)
