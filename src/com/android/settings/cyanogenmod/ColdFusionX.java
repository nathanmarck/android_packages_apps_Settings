/*
 * Copyright (C) 2012 tilal6991
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

import android.content.ContentResolver;
import android.content.Intent;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.os.Handler;
import android.util.Log;
import android.net.Uri;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.IWindowManager;

import android.provider.Settings;
import android.os.SystemProperties;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class ColdFusionX extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "CFXSettings";
    private static final String CFX_NAVBAR = "cfx_navbarenable";
    private static final String DISABLE_BOOTANIMATION_PREF = "cfx_disable_bootanimation";
    private static final String DISABLE_BOOTANIMATION_PERSIST_PROP = "persist.sys.nobootanimation";
    private static final String PREF_TRANSPARENCY = "cfx_statusbar_transparency";

    private final Configuration mCurConfig = new Configuration();

    private CheckBoxPreference mCFXNavbar;
    private ListPreference mTransparency;
    private CheckBoxPreference mDisableBootanimPref;
    private ListPreference mNavigationBarHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.cfx_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mCFXNavbar = (CheckBoxPreference) findPreference(CFX_NAVBAR);
        mDisableBootanimPref = (CheckBoxPreference) prefSet.findPreference(DISABLE_BOOTANIMATION_PREF);

        mTransparency = (ListPreference) findPreference(CFX_STATUSBAR_TRANSPARENCY);
        mTransparency.setOnPreferenceChangeListener(this);
        mTransparency.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.CFX_STATUSBAR_TRANSPARENCY,
                0)));

        String disableBootanimation = SystemProperties.get(DISABLE_BOOTANIMATION_PERSIST_PROP, "0");

        mCFXNavbar.setChecked(Settings.System.getInt(getActivity().getContentResolver(), CFX_NAVBAR, 0) == 1);
        mDisableBootanimPref.setChecked("1".equals(disableBootanimation));

        mNavigationBarHeight = (ListPreference) findPreference("cfx_navbar_height");
        mNavigationBarHeight.setOnPreferenceChangeListener(this);

        findPreference("cfx_about").setOnPreferenceClickListener(
            new OnPreferenceClickListener() {
                 public boolean onPreferenceClick(Preference preference) {
                     Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.cfx_about_thread)));
                     startActivity(browserIntent);
                     return true;
            }
        });
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
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mCFXNavbar) {
            Settings.System.putInt(getContentResolver(), CFX_NAVBAR, mCFXNavbar.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mDisableBootanimPref) {
            SystemProperties.set(DISABLE_BOOTANIMATION_PERSIST_PROP, mDisableBootanimPref.isChecked() ? "1" : "0");
        } else if (preference == mNavigationBarHeight) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            int height = mapChosenDpToPixels(dp);
            Settings.System.putInt(getContentResolver(), Settings.System.CFX_NAVBAR_HEIGHT, height);
            toggleBar();
        } else if (preference == mTransparency) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_TRANSPARENCY, val);
            restartSystemUI();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void toggleBar() {
        boolean isBarOn = Settings.System.getInt(getContentResolver(),
                Settings.System.CFX_NAVBAR, 0) == 1;
        Handler h = new Handler();
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.CFX_NAVBAR, isBarOn ? 0 : 1);
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.CFX_NAVBAR, isBarOn ? 1 : 0);
    }

    public int mapChosenDpToPixels(int dp) {
        switch (dp) {
            case 48:
                return getResources().getDimensionPixelSize(R.dimen.cfx_navbar_48);
            case 42:
                return getResources().getDimensionPixelSize(R.dimen.cfx_navbar_42);
            case 36:
                return getResources().getDimensionPixelSize(R.dimen.cfx_navbar_36);
            case 24:
                return getResources().getDimensionPixelSize(R.dimen.cfx_navbar_24);
        }
        return -1;
    }


    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();

        return true;
    }
}
