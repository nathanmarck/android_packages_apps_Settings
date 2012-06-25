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

import java.io.IOException;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.AlertDialog;
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
import android.os.ServiceManager;
import android.os.IBinder;
import android.os.IPowerManager;

import android.provider.Settings;
import android.os.SystemProperties;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class ColdFusionX extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "CFXSettings";
    private static final String CFX_NAVBAR = "cfx_navbarenable";
    private static final String CFX_STATUSBAR_TRANSPARENCY = "cfx_statusbar_transparency";
    private static final String DISABLE_BOOTANIMATION_PREF = "cfx_disable_bootanimation";
    private static final String DISABLE_BOOTANIMATION_PERSIST_PROP = "persist.sys.nobootanimation";
    private static final String CFX_NAVBAR_HEIGHT = "cfx_navbar_height";

    private final Configuration mCurConfig = new Configuration();

    private CheckBoxPreference mCFXNavbar;
    private ListPreference mTransparency;
    private CheckBoxPreference mDisableBootanimPref;
    private ListPreference mNavigationBarHeight;
    private Context mContext;

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

        mNavigationBarHeight = (ListPreference) findPreference(CFX_NAVBAR_HEIGHT);
        if (!mCFXNavbar.isChecked()) {
            mNavigationBarHeight.setEnabled(false);
        }
        mNavigationBarHeight.setOnPreferenceChangeListener(this);

        mContext = getActivity().getBaseContext();

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
            hotRebootDialog();
        } else if (preference == mDisableBootanimPref) {
            SystemProperties.set(DISABLE_BOOTANIMATION_PERSIST_PROP, mDisableBootanimPref.isChecked() ? "1" : "0");
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public int mapChosenDpToPixels(int dp) {
        switch (dp) {
            case 48:
                return getResources().getDimensionPixelSize(R.dimen.cfx_navbar_48);
            case 42:
                return getResources().getDimensionPixelSize(R.dimen.cfx_navbar_42);
            case 36:
                return getResources().getDimensionPixelSize(R.dimen.cfx_navbar_36);
            case 30:
                return getResources().getDimensionPixelSize(R.dimen.cfx_navbar_30);
            case 24:
                return getResources().getDimensionPixelSize(R.dimen.cfx_navbar_24);
        }
        return -1;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;
        if (preference == mTransparency) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(), Settings.System.CFX_STATUSBAR_TRANSPARENCY, val);
            restartSystemUI();
            return true;
        } else if (preference == mNavigationBarHeight) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            int height = mapChosenDpToPixels(dp);
            result = Settings.System.putInt(getActivity().getContentResolver(), Settings.System.CFX_NAVBAR_HEIGHT, height);
            hotRebootDialog();
            return true;
            //restartSystemServer();
        }
        return false;
    }

    private void hotRebootDialog() {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Hot Reboot required!")
                    .setMessage("Please hot reboot to enable/disable the navigation bar properly!")
                    .setNegativeButton("I'll reboot later", null)
                    .setCancelable(false)
                    .setPositiveButton("Hot Reboot now!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                IBinder b = ServiceManager.getService(Context.POWER_SERVICE);
                                IPowerManager pm = IPowerManager.Stub.asInterface(b);
                                pm.crash("Restarting UI");
                            } catch (android.os.RemoteException e) {
                                //
                            }
                        }
                    })
                    .create()
                    .show();
    }

    private void restartSystemUI() {
        try {
            Runtime.getRuntime().exec("pkill -TERM -f com.android.systemui");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
