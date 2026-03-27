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

package com.android.axion.themepicker.providers

import android.content.Context
import android.graphics.Typeface
import android.graphics.fonts.FontFamilyUpdateRequest
import android.graphics.fonts.FontFileUpdateRequest
import android.graphics.fonts.FontFileUtil
import android.graphics.fonts.FontManager
import android.graphics.fonts.FontStyle
import android.graphics.fonts.Font as GraphicsFont
import android.graphics.fonts.FontVariationAxis
import android.net.Uri
import android.os.FileUtils
import android.os.ParcelFileDescriptor
import android.os.ServiceManager
import android.os.SystemProperties
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.android.axion.themepicker.R
import com.android.internal.statusbar.IStatusBarService
import java.io.File
import java.io.FileInputStream
import java.nio.channels.FileChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ExternalFontInstaller(private val context: Context) {

    companion object {
        private const val TAG = "ExternalFontInstaller"
        private const val CUSTOM_FONT_FILE = "cust_font.ttf"
        private const val TEMP_PREVIEW_FONT = "preview_font.ttf"
        private const val OVERLAY_CATEGORY_FONT = "android.theme.customization.font"
        const val DEFAULT_FONT_FAMILY = "ext_font"
        private const val DEFAULT_FONT_OVERLAY = "com.android.theme.font.extfont"
        private const val PROP_OVERLAY_FONTS = "persist.sys.ax_overlay_fonts"
        private val FONT_WEIGHTS = intArrayOf(100, 200, 300, 400, 500, 600, 700, 800, 900)

        fun rebootDevice() {
            runCatching {
                    val binder = ServiceManager.getService("statusbar")
                    val statusBarService = IStatusBarService.Stub.asInterface(binder)
                    statusBarService.reboot(false, "system_font_change")
                }
                .onFailure { Log.e(TAG, "Failed to reboot device via statusbar service", it) }
        }
    }

    private val fontManager: FontManager = context.getSystemService(FontManager::class.java)

    suspend fun loadTypefaceFromUri(uri: Uri): Typeface? =
        withContext(Dispatchers.IO) {
            val tempFile = copyUriToCache(uri, TEMP_PREVIEW_FONT) ?: return@withContext null
            val postScriptName =
                extractPostScriptName(tempFile)
                    ?: run {
                        tempFile.delete()
                        return@withContext null
                    }
            return@withContext Typeface.createFromFile(tempFile)
        }

    suspend fun installFontFromUri(uri: Uri): String? {
        val fontFile =
            copyUriToCache(uri, CUSTOM_FONT_FILE)
                ?: run {
                    showToast(R.string.toast_failed_apply_font)
                    return null
                }

        val postScriptName =
            extractPostScriptName(fontFile)
                ?: run {
                    showToast(R.string.toast_invalid_font_file)
                    fontFile.delete()
                    return null
                }

        if (!applyFontToSystem(fontFile, postScriptName)) {
            fontFile.delete()
            return null
        }

        val fontProp = "$DEFAULT_FONT_FAMILY:$DEFAULT_FONT_FAMILY:$DEFAULT_FONT_FAMILY:$DEFAULT_FONT_FAMILY"
        SystemProperties.set(PROP_OVERLAY_FONTS, fontProp)
        updateThemeOverlays()
        cleanupPreviewFont()
        return postScriptName
    }

    private suspend fun copyUriToCache(uri: Uri, fileName: String): File? =
        withContext(Dispatchers.IO) {
            try {
                val cacheFile = File(context.cacheDir, fileName)
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    FileInputStream(pfd.fileDescriptor).use { input ->
                        cacheFile.outputStream().use { output -> FileUtils.copy(input, output) }
                    }
                }
                cacheFile
            } catch (e: Exception) {
                Log.e(TAG, "Failed to copy URI to cache", e)
                null
            }
        }

    private fun extractPostScriptName(fontFile: File): String? =
        FileInputStream(fontFile).use { fis ->
            val buffer = fis.channel.map(FileChannel.MapMode.READ_ONLY, 0, fis.channel.size())
            FontFileUtil.getPostScriptName(buffer, 0)
        }

    private fun isVariableFont(fontFile: File): Boolean =
        runCatching {
            val font = GraphicsFont.Builder(fontFile).build()
            font.axes?.any { it.tag == "wght" } == true
        }.getOrDefault(false)

    private fun applyFontToSystem(fontFile: File, postScriptName: String): Boolean {
        return runCatching {
                fontManager.clearUpdates()
                val pfd = ParcelFileDescriptor.open(fontFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val fontFileUpdateRequest = FontFileUpdateRequest(pfd, ByteArray(0))

                val isVariable = isVariableFont(fontFile)
                val fonts = if (isVariable) {
                    FONT_WEIGHTS.flatMap { weight ->
                        val axes = listOf(FontVariationAxis("wght", weight.toFloat()))
                        listOf(
                            FontFamilyUpdateRequest.Font.Builder(
                                postScriptName,
                                FontStyle(weight, FontStyle.FONT_SLANT_UPRIGHT),
                            ).setAxes(axes).build(),
                            FontFamilyUpdateRequest.Font.Builder(
                                postScriptName,
                                FontStyle(weight, FontStyle.FONT_SLANT_ITALIC),
                            ).setAxes(axes).build(),
                        )
                    }
                } else {
                    listOf(
                        FontFamilyUpdateRequest.Font.Builder(
                            postScriptName,
                            FontStyle(FontStyle.FONT_WEIGHT_NORMAL, FontStyle.FONT_SLANT_UPRIGHT),
                        ).build(),
                        FontFamilyUpdateRequest.Font.Builder(
                            postScriptName,
                            FontStyle(FontStyle.FONT_WEIGHT_BOLD, FontStyle.FONT_SLANT_UPRIGHT),
                        ).build(),
                    )
                }

                val family =
                    FontFamilyUpdateRequest.FontFamily.Builder(DEFAULT_FONT_FAMILY, fonts).build()

                val updateRequest =
                    FontFamilyUpdateRequest.Builder()
                        .addFontFileUpdateRequest(fontFileUpdateRequest)
                        .addFontFamily(family)
                        .build()

                val result =
                    fontManager.updateFontFamily(
                        updateRequest,
                        fontManager.fontConfig.configVersion,
                    )

                if (result != FontManager.RESULT_SUCCESS) {
                    showToast(R.string.toast_failed_apply_font)
                    false
                } else true
            }
            .getOrElse {
                Log.e(TAG, "Failed to update system font", it)
                false
            }
    }

    private fun updateThemeOverlays() {
        val resolver = context.contentResolver
        val userId = UserHandle.myUserId()

        val json =
            runCatching {
                    val current =
                        Settings.Secure.getStringForUser(
                            resolver,
                            Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                            userId,
                        )
                    if (current.isNullOrEmpty()) JSONObject() else JSONObject(current)
                }
                .getOrElse { JSONObject() }

        runCatching {
                if (json.has(OVERLAY_CATEGORY_FONT)) {
                    json.remove(OVERLAY_CATEGORY_FONT)
                    Settings.Secure.putStringForUser(
                        resolver,
                        Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                        json.toString(),
                        userId,
                    )
                }
                json.put(OVERLAY_CATEGORY_FONT, DEFAULT_FONT_OVERLAY)
                Settings.Secure.putStringForUser(
                    resolver,
                    Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                    json.toString(),
                    userId,
                )
            }
            .onFailure { Log.e(TAG, "Failed to persist custom font overlay", it) }
    }

    private fun showToast(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    fun resetFontUpdates() {
        runCatching { fontManager.clearUpdates() }
            .onFailure { Log.e(TAG, "Failed to clear font updates", it) }
        SystemProperties.set(PROP_OVERLAY_FONTS, "")
        cleanupPreviewFont()
    }

    private fun cleanupPreviewFont() {
        try {
            val previewFile = File(context.cacheDir, TEMP_PREVIEW_FONT)
            if (previewFile.exists()) previewFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup preview font", e)
        }
    }
}
