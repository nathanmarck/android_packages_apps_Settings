/*
 * Copyright (C) 2012 The CyanogenMod Project
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

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class Buttons extends SettingsPreferenceFragment {

    private static final String BUTTONS_SHOW_ACTION_OVERFLOW = "buttons_show_action_overflow";
    private static final String BUTTONS_APP_SWITCH_MENU_SWAP = "buttons_app_switch_menu_swap";

    private CheckBoxPreference mShowActionOverflow;
    private CheckBoxPreference mAppSwitchMenuSwap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.buttons);

        mShowActionOverflow = (CheckBoxPreference) getPreferenceScreen().findPreference(
                BUTTONS_SHOW_ACTION_OVERFLOW);
        mAppSwitchMenuSwap = (CheckBoxPreference) getPreferenceScreen().findPreference(
                BUTTONS_APP_SWITCH_MENU_SWAP);

        mShowActionOverflow.setChecked((Settings.System.getInt(getActivity().
                getApplicationContext().getContentResolver(),
                Settings.System.UI_FORCE_OVERFLOW_BUTTON, 0) == 1));
        mAppSwitchMenuSwap.setChecked((Settings.System.getInt(getActivity().
                getApplicationContext().getContentResolver(),
                Settings.System.APP_SWITCH_MENU_KEY_SWAP, 0) == 1));
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mShowActionOverflow) {
            if (mShowActionOverflow.isChecked()) {
                Settings.System.putInt(getActivity().getApplicationContext().
                        getContentResolver(), Settings.System.UI_FORCE_OVERFLOW_BUTTON, 1);
            } else {
                Settings.System.putInt(getActivity().getApplicationContext().
                        getContentResolver(), Settings.System.UI_FORCE_OVERFLOW_BUTTON, 0);
                Settings.System.putInt(getActivity().getApplicationContext().
                        getContentResolver(), Settings.System.APP_SWITCH_MENU_KEY_SWAP, 0);
                mAppSwitchMenuSwap.setChecked(false);
            }
            return true;
        } else if (preference == mAppSwitchMenuSwap) {
            Settings.System.putInt(getActivity().getApplicationContext().
                    getContentResolver(), Settings.System.APP_SWITCH_MENU_KEY_SWAP,
                    mAppSwitchMenuSwap.isChecked() ? 1 : 0);
            return true;
        }
        return false;
    }
}
