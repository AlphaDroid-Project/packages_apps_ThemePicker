/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.android.customization.model.uistyle;

import static com.android.customization.model.ResourceConstants.ANDROID_PACKAGE;
import static com.android.customization.model.ResourceConstants.SETTINGS_PACKAGE;
import static com.android.customization.model.ResourceConstants.SYSUI_PACKAGE;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_UI_STYLE_ANDROID;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_UI_STYLE_SETTINGS;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_UI_STYLE_SYSUI;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.PorterDuff.Mode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import com.android.wallpaper.R;
import com.android.wallpaper.util.ResourceUtils;

import com.android.customization.model.CustomizationManager;
import com.android.customization.model.CustomizationOption;
import com.android.customization.model.ResourceConstants;
import com.android.customization.model.theme.OverlayManagerCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UIStyleOption implements CustomizationOption<UIStyleOption> {

    private static int[] COLOR_BUTTON_IDS = {
            R.id.preview_check_selected, R.id.preview_radio_selected,
            R.id.preview_toggle_selected
    };

    @ColorInt private int mBackgroundColorLight;
    @ColorInt private int mBackgroundColorDark;
    private String mTitle;
    private boolean mIsDefault;

    // Mapping from category to overlay package name
    private final Map<String, String> mOverlayPackageNames = new HashMap<>();

    public UIStyleOption(String title, boolean isDefault) {
        mTitle = title;
        mIsDefault = isDefault;
    }

    public UIStyleOption(String title) {
        this(title, false);
    }

    @Override
    public void bindThumbnailTile(View view) {
        /*OG CODE*/
        Resources res = view.getContext().getResources();
        @ColorInt int color = resolveColor(view.getResources());
        LayerDrawable selectedOption = (LayerDrawable) view.getResources().getDrawable(
                R.drawable.color_chip_hollow, view.getContext().getTheme());
        Drawable unselectedOption = view.getResources().getDrawable(
                R.drawable.color_chip_filled, view.getContext().getTheme());
        selectedOption.findDrawableByLayerId(R.id.center_fill).setTintList(
                ColorStateList.valueOf(color));
        unselectedOption.setTintList(ColorStateList.valueOf(color));
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[] {android.R.attr.textColorPrimary}, selectedOption);
        stateListDrawable.addState(
                new int[] {-android.R.attr.textColorTertiary}, unselectedOption);
        int resId = R.id.ui_style_section_tile;
        if (view.findViewById(R.id.option_icon) != null) {
            resId = R.id.option_icon;
        }


        ((ImageView) view.findViewById(resId)).setImageDrawable(stateListDrawable);
        view.setContentDescription(mTitle); 
    }

    @ColorInt
    private int resolveColor(Resources res) {
        Configuration configuration = res.getConfiguration();
        return (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES ? mBackgroundColorDark : mBackgroundColorLight;
    }

    @Override
    public boolean isActive(CustomizationManager<UIStyleOption> manager) {
        UIStyleManager uiStyleManager = (UIStyleManager) manager;
        OverlayManagerCompat overlayManager = uiStyleManager.getOverlayManager();
        if (mIsDefault) {
            return overlayManager.getEnabledPackageName(SYSUI_PACKAGE, OVERLAY_CATEGORY_UI_STYLE_SYSUI) == null &&
                    overlayManager.getEnabledPackageName(SETTINGS_PACKAGE, OVERLAY_CATEGORY_UI_STYLE_SETTINGS) == null &&
                    overlayManager.getEnabledPackageName(ANDROID_PACKAGE, OVERLAY_CATEGORY_UI_STYLE_ANDROID) == null;
        }
        for (Map.Entry<String, String> overlayEntry : getOverlayPackages().entrySet()) {
            if (overlayEntry.getValue() == null || !overlayEntry.getValue().equals(overlayManager.getEnabledPackageName(determinePackage(overlayEntry.getKey()), overlayEntry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.theme_ui_style_option;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public void bindPreview(ViewGroup container) {
        ViewGroup cardBody = container.findViewById(R.id.theme_preview_card_body_container);
        if (cardBody.getChildCount() == 0) {
            LayoutInflater.from(container.getContext()).inflate(
                    R.layout.preview_card_ui_style_content, cardBody, true);
        }

        Resources res = container.getResources();
        View v = container.findViewById(R.id.preview_styles);
        @ColorInt int backgroundColor = resolveColor(res);
        v.setBackgroundColor(backgroundColor);

        @ColorInt int controlGreyColor = ResourceUtils.getColorAttr(
                    container.getContext(),
                    android.R.attr.textColorTertiary);
        @ColorInt int accentColor = ResourceUtils.getColorAttr(
                    container.getContext(),
                    android.R.attr.colorAccent);
        ColorStateList tintList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_selected},
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_enabled}
                },
                new int[] {
                        accentColor,
                        accentColor,
                        controlGreyColor
                }
        );

        for (int i = 0; i < COLOR_BUTTON_IDS.length; i++) {
            CompoundButton button = container.findViewById(COLOR_BUTTON_IDS[i]);
            button.setButtonTintList(tintList);
        }

        Switch enabledSwitch = container.findViewById(R.id.preview_toggle_selected);
        enabledSwitch.setThumbTintList(tintList);
        enabledSwitch.setTrackTintList(tintList);

        ColorStateList seekbarTintList = ColorStateList.valueOf(accentColor);
        SeekBar seekbar = container.findViewById(R.id.preview_seekbar);
        seekbar.setThumbTintList(seekbarTintList);
        seekbar.setProgressTintList(seekbarTintList);
        seekbar.setProgressBackgroundTintList(seekbarTintList);
        // Disable seekbar
        seekbar.setOnTouchListener((view, motionEvent) -> true);
    }

    private String determinePackage(String category) {
       switch(category) {
           case OVERLAY_CATEGORY_UI_STYLE_SYSUI:
               return SYSUI_PACKAGE;
           case OVERLAY_CATEGORY_UI_STYLE_SETTINGS:
               return SETTINGS_PACKAGE;
           case OVERLAY_CATEGORY_UI_STYLE_ANDROID:
               return ANDROID_PACKAGE;
           default:
               return null;
       }
    }

    public void addStyleInfo(@ColorInt int backgroundColorLight, @ColorInt int backgroundColorDark) {
        mBackgroundColorLight = backgroundColorLight;
        mBackgroundColorDark = backgroundColorDark;
    }

    public void addOverlayPackage(String category, String overlayPackage) {
        mOverlayPackageNames.put(category, overlayPackage);
    }

    public Map<String, String> getOverlayPackages() {
        return mOverlayPackageNames;
    }

    /**
     * @return whether this ui style option has overlays and previews for all the required packages
     */
    public boolean isValid(Context context) {
        return mOverlayPackageNames.keySet().size() ==
                ResourceConstants.getPackagesToOverlay(context).length;
    }

    public boolean isDefault() {
        return mIsDefault;
    }
}
