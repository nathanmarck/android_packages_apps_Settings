/*
 * Copyright (C) 2012 CyanogenMod
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

package com.android.settings.cyanogenmod;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.IWindowManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class SystemSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "SystemSettings";

    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_NOTIFICATION_DRAWER = "notification_drawer";
    private static final String KEY_NOTIFICATION_DRAWER_TABLET = "notification_drawer_tablet";
    private static final String KEY_NAVIGATION_BAR = "navigation_bar";
    private static final String CFX_NAVBAR = "cfx_navbarenable";
    private static final String KEY_BUTTONS = "buttons";

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    private static final int MASK_KEY_HOME = 0x01;
    private static final int MASK_KEY_BACK = 0x02;
    private static final int MASK_KEY_MENU = 0x04;
    private static final int MASK_KEY_SEARCH = 0x08;
    private static final int MASK_KEY_APP_SWITCH = 0x10;

    private static final String KEY_NAV_BAR = "navigation_bar";
    private static final String KEY_NAV_BAR_EDIT = "nav_bar_edit";
    private static final String KEY_NAV_BAR_GLOW = "nav_bar_glow";

    private ListPreference mFontSizePref;
    private ListPreference mGlowTimes;

    private final Configuration mCurConfig = new Configuration();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.system_settings);

        mFontSizePref = (ListPreference) findPreference(KEY_FONT_SIZE);
        mFontSizePref.setOnPreferenceChangeListener(this);
        mGlowTimes = (ListPreference) findPreference(KEY_NAV_BAR_GLOW);
        mGlowTimes.setOnPreferenceChangeListener(this);
        if (Utils.isScreenLarge()) {
            getPreferenceScreen().removePreference(findPreference(KEY_NOTIFICATION_DRAWER));
        } else {
            getPreferenceScreen().removePreference(findPreference(KEY_NOTIFICATION_DRAWER_TABLET));
        }
        IWindowManager windowManager = IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));
        try {
            if (!windowManager.hasNavigationBar()) {
                getPreferenceScreen().removePreference(findPreference(KEY_NAVIGATION_BAR));
            } else {
//                getPreferenceScreen().removePreference(findPreference(KEY_BUTTONS));
            }
        } catch (RemoteException e) {
        }
        updateGlowTimesSummary();
        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
//        if (((deviceKeys & MASK_KEY_MENU) == 0) ||
//                ((deviceKeys & MASK_KEY_APP_SWITCH) != 0)) {
//            getPreferenceScreen().removePreference(findPreference(KEY_BUTTONS));
//        }
    }

    int floatToIndex(float val) {
        String[] indices = getResources().getStringArray(R.array.entryvalues_font_size);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }

    public void readFontSizePreference(ListPreference pref) {
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }

        // mark the appropriate item in the preferences list
        int index = floatToIndex(mCurConfig.fontScale);
        pref.setValueIndex(index);

        // report the current size in the summary text
        final Resources res = getResources();
        String[] fontSizeNames = res.getStringArray(R.array.entries_font_size);
        pref.setSummary(String.format(res.getString(R.string.summary_font_size),
                fontSizeNames[index]));
    }

    private void updateGlowTimesSummary() {
        int resId;
        String combinedTime = Settings.System.getString(getContentResolver(),
                Settings.System.NAV_GLOW_DURATION_ON) + "|" +
                Settings.System.getString(getContentResolver(),
                Settings.System.NAV_GLOW_DURATION_OFF);

        String[] glowArray = getResources().getStringArray(R.array.values_nav_bar_glow);

        if (glowArray[0].equals(combinedTime)) {
            resId = R.string.navigation_bar_glow_off;
            mGlowTimes.setValueIndex(0);
        } else if (glowArray[1].equals(combinedTime)) {
            resId = R.string.navigation_bar_glow_fast;
            mGlowTimes.setValueIndex(1);
        } else {
            resId = R.string.navigation_bar_glow_default;
            mGlowTimes.setValueIndex(2);
        }
        mGlowTimes.setSummary(getResources().getString(resId));
    }

    @Override
    public void onResume() {
        super.onResume();

        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateState() {
        readFontSizePreference(mFontSizePref);
    }

    public void writeFontSizePreference(Object objValue) {
        try {
            mCurConfig.fontScale = Float.parseFloat(objValue.toString());
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to save font size");
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mFontSizePref) {
            final String key = preference.getKey();
            if (KEY_FONT_SIZE.equals(key)) {
                writeFontSizePreference(objValue);
            }
            return true;
        } else if (preference == mGlowTimes) {
            String value = (String) objValue;
            String[] breakIndex = value.split("\\|");

            int onTime = Integer.valueOf(breakIndex[0]);
            int offTime = Integer.valueOf(breakIndex[1]);

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAV_GLOW_DURATION_ON, onTime);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAV_GLOW_DURATION_OFF, offTime);
            updateGlowTimesSummary();
            return true;
        }
        return false;
    }
}
