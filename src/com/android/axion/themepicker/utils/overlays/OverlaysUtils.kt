package com.android.axion.themepicker.utils.overlays

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.android.axion.themepicker.data.model.IconPackOption
import com.android.axion.themepicker.providers.CommonOverlayProvider
import com.android.customization.model.ResourceConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun loadIconPackOptions(
    context: Context,
    androidProvider: CommonOverlayProvider,
    sysUiProvider: CommonOverlayProvider
): List<IconPackOption> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    val androidOptions = androidProvider.loadOptions()
    val sysUiOptions = sysUiProvider.loadOptions()
    
    val sysUiMap = sysUiOptions.filter { it.packageName != null }
        .associateBy { 
            val pkg = it.packageName!!
            pkg.substring(0, pkg.lastIndexOf("."))
        }
    
    androidOptions.map { androidOption ->
        val androidPkg = androidOption.packageName
        val prefix = androidPkg?.let { pkg ->
            pkg.substring(0, pkg.lastIndexOf("."))
        }
        val sysUiPackage = prefix?.let { sysUiMap[it]?.packageName }
        
        val previewIcons = if (androidPkg != null) {
            loadIconPreviews(context, pm, androidPkg)
        } else {
            loadIconPreviews(context, pm, ResourceConstants.ANDROID_PACKAGE)
        }
        
        IconPackOption(
            packageName = androidPkg,
            label = androidOption.label,
            previewIcons = previewIcons,
            sysUiPackageName = sysUiPackage,
            isActive = androidOption.isActive
        )
    }
}

private fun loadIconPreviews(
    context: Context,
    pm: PackageManager,
    packageName: String
): List<Drawable> {
    return try {
        val resources = if (packageName == ResourceConstants.ANDROID_PACKAGE) {
            Resources.getSystem()
        } else {
            pm.getResourcesForApplication(packageName)
        }
        
        ResourceConstants.ICONS_FOR_PREVIEW.mapNotNull { iconName ->
            try {
                val resId = resources.getIdentifier(iconName, "drawable", packageName)
                if (resId != 0) resources.getDrawable(resId, null) else null
            } catch (e: Exception) {
                null
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}
