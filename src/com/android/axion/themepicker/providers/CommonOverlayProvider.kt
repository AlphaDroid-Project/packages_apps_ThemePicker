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
package com.android.axion.themepicker.providers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.PathShape
import android.os.UserHandle
import android.util.Log
import android.util.PathParser
import com.android.customization.model.ResourceConstants
import com.android.customization.model.theme.OverlayManagerCompat
import com.android.customization.widget.DynamicAdaptiveIconDrawable
import com.android.themepicker.R
import com.android.axion.themepicker.data.model.FontOverlayOption
import com.android.axion.themepicker.data.model.IconPackOption
import com.android.axion.themepicker.data.model.IconShapeOption
import com.android.axion.themepicker.data.model.OverlayOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommonOverlayProvider(
    private val context: Context,
    private val overlayManager: OverlayManagerCompat,
    private val category: String
) {
    private val packageManager: PackageManager = context.packageManager
    private val overlayPackages: List<String>
    private var activeOverlay: String?

    init {
        val packagesToOverlay = ResourceConstants.getPackagesToOverlay(context)
        overlayPackages = overlayManager.getOverlayPackagesForCategory(
            category,
            UserHandle.myUserId(),
            *packagesToOverlay
        )
        activeOverlay = overlayManager.getEnabledPackageName(
            ResourceConstants.ANDROID_PACKAGE,
            category
        )
    }

    suspend fun loadOptions(): List<OverlayOption> = withContext(Dispatchers.IO) {
        val options = mutableListOf<OverlayOption>()
        
        options.add(createDefaultOption())
        
        val customOptions = overlayPackages.mapNotNull { overlayPackage ->
            try {
                val label = packageManager.getApplicationInfo(overlayPackage, 0)
                    .loadLabel(packageManager).toString()
                
                OverlayOption(
                    packageName = overlayPackage,
                    label = label,
                    isActive = overlayPackage == activeOverlay
                )
            } catch (e: Exception) {
                Log.w(TAG, "Couldn't load overlay $overlayPackage, will skip it", e)
                null
            }
        }
        
        options.addAll(customOptions.sortedBy { it.label })
        options
    }

    suspend fun loadFontOptions(): List<FontOverlayOption> = withContext(Dispatchers.IO) {
        val options = mutableListOf<FontOverlayOption>()
        
        options.add(createDefaultFontOption())
        
        val customOptions = overlayPackages.mapNotNull { overlayPackage ->
            try {
                val overlayRes = packageManager.getResourcesForApplication(overlayPackage)
                val headlineFont = Typeface.create(
                    getFontFamily(overlayPackage, overlayRes, ResourceConstants.CONFIG_HEADLINE_FONT_FAMILY),
                    Typeface.NORMAL
                )
                val bodyFont = Typeface.create(
                    getFontFamily(overlayPackage, overlayRes, ResourceConstants.CONFIG_BODY_FONT_FAMILY),
                    Typeface.NORMAL
                )
                val label = packageManager.getApplicationInfo(overlayPackage, 0)
                    .loadLabel(packageManager).toString()
                
                FontOverlayOption(
                    packageName = overlayPackage,
                    label = label,
                    headlineFont = headlineFont,
                    bodyFont = bodyFont,
                    isActive = overlayPackage == activeOverlay
                )
            } catch (e: Exception) {
                Log.w(TAG, "Couldn't load font overlay $overlayPackage, will skip it", e)
                null
            }
        }
        
        options.addAll(customOptions.sortedBy { it.label })
        options
    }

    suspend fun loadIconShapeOptions(): List<IconShapeOption> = withContext(Dispatchers.IO) {
        val options = mutableListOf<IconShapeOption>()
        val thumbSize = context.resources.getDimensionPixelSize(R.dimen.component_shape_thumb_size)
        
        options.add(createDefaultIconShapeOption(thumbSize))
        
        val customOptions = overlayPackages.mapNotNull { overlayPackage ->
            try {
                val overlayRes = packageManager.getResourcesForApplication(overlayPackage)
                val path = loadShapePath(overlayRes, overlayPackage)
                val label = packageManager.getApplicationInfo(overlayPackage, 0)
                    .loadLabel(packageManager).toString()
                
                if (path != null) {
                    IconShapeOption(
                        packageName = overlayPackage,
                        label = label,
                        shapePath = path,
                        shapeDrawable = createShapeDrawable(path, thumbSize),
                        shapedAppIcons = getShapedAppIcons(path),
                        isActive = overlayPackage == activeOverlay
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.w(TAG, "Couldn't load shape overlay $overlayPackage, will skip it", e)
                null
            }
        }
        
        options.addAll(customOptions.sortedBy { it.label })
        options
    }

    fun applyOverlay(option: OverlayOption): Boolean {
        return try {
            if (option.packageName == null) {
                disableAllOverlays()
            } else {
                overlayManager.setEnabledExclusiveInCategory(
                    option.packageName,
                    UserHandle.myUserId()
                )
            }
            activeOverlay = option.packageName
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error applying overlay: ${option.packageName}", e)
            false
        }
    }

    fun applyOverlay(option: FontOverlayOption): Boolean {
        return applyOverlay(option as OverlayOption)
    }

    private fun disableAllOverlays() {
        overlayPackages.forEach { overlay ->
            try {
                overlayManager.disableOverlay(overlay, UserHandle.myUserId())
            } catch (e: Exception) {
                Log.w(TAG, "Error disabling overlay: $overlay", e)
            }
        }
    }

    fun getActiveOverlay(): String? = activeOverlay

    private fun createDefaultOption(): OverlayOption {
        return OverlayOption(
            packageName = null,
            label = context.getString(R.string.default_theme_title),
            isActive = activeOverlay == null
        )
    }

    private fun createDefaultFontOption(): FontOverlayOption {
        val system = Resources.getSystem()
        val headlineFont = Typeface.create(
            system.getString(
                system.getIdentifier(
                    ResourceConstants.CONFIG_HEADLINE_FONT_FAMILY,
                    "string",
                    ResourceConstants.ANDROID_PACKAGE
                )
            ),
            Typeface.NORMAL
        )
        val bodyFont = Typeface.create(
            system.getString(
                system.getIdentifier(
                    ResourceConstants.CONFIG_BODY_FONT_FAMILY,
                    "string",
                    ResourceConstants.ANDROID_PACKAGE
                )
            ),
            Typeface.NORMAL
        )
        
        return FontOverlayOption(
            packageName = null,
            label = context.getString(R.string.default_theme_title),
            headlineFont = headlineFont,
            bodyFont = bodyFont,
            isActive = activeOverlay == null
        )
    }

    private fun createDefaultIconShapeOption(thumbSize: Int): IconShapeOption {
        val system = Resources.getSystem()
        val path = loadShapePath(system, ResourceConstants.ANDROID_PACKAGE)
        
        return IconShapeOption(
            packageName = null,
            label = context.getString(R.string.default_theme_title),
            shapePath = path,
            shapeDrawable = createShapeDrawable(path, thumbSize),
            shapedAppIcons = getShapedAppIcons(path),
            isActive = activeOverlay == null
        )
    }

    private fun loadShapePath(resources: Resources, packageName: String): Path? {
        return try {
            val shapeString = resources.getString(
                resources.getIdentifier(
                    ResourceConstants.CONFIG_ICON_MASK,
                    "string",
                    packageName
                )
            )
            if (!shapeString.isNullOrEmpty()) {
                PathParser.createPathFromPathData(shapeString)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error loading shape path for $packageName", e)
            null
        }
    }

    private fun createShapeDrawable(path: Path?, thumbSize: Int): ShapeDrawable {
        val shapePath = path ?: Path()
        val pathShape = PathShape(shapePath, ResourceConstants.PATH_SIZE, ResourceConstants.PATH_SIZE)
        return ShapeDrawable(pathShape).apply {
            intrinsicHeight = thumbSize
            intrinsicWidth = thumbSize
        }
    }

    private fun getShapedAppIcons(path: Path?): List<Drawable> {
        if (path == null) return emptyList()
        
        val shapedIcons = mutableListOf<Drawable>()
        val userApps = getUserApps()
        
        for (packageName in userApps) {
            var icon: Drawable? = null
            var name: CharSequence? = null
            
            try {
                val appIcon = packageManager.getApplicationIcon(packageName)
                if (appIcon is AdaptiveIconDrawable) {
                    icon = DynamicAdaptiveIconDrawable(
                        appIcon.background,
                        appIcon.foreground,
                        path
                    )
                    
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    name = packageManager.getApplicationLabel(appInfo)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.d(TAG, "Couldn't find app $packageName, won't use it for icon shape preview")
            } finally {
                if (icon != null && !name.isNullOrEmpty()) {
                    shapedIcons.add(icon)
                }
            }
            
            if (shapedIcons.size >= 6) break
        }
        
        return shapedIcons
    }

    private fun getUserApps(): List<String> {
        val launchableApps = mutableListOf<String>()
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        for (appInfo in apps) {
            if (packageManager.getLaunchIntentForPackage(appInfo.packageName) != null) {
                launchableApps.add(appInfo.packageName)
            }
            if (launchableApps.size >= 6) break
        }
        
        return launchableApps
    }

    private fun getFontFamily(overlayPackage: String, overlayRes: Resources, configName: String): String {
        return overlayRes.getString(overlayRes.getIdentifier(configName, "string", overlayPackage))
    }

    companion object {
        private const val TAG = "CommonOverlayProvider"
    }
}
