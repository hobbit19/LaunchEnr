/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.enrico.launcher3.board;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.enrico.launcher3.LauncherFiles;
import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.settings.SettingsTheme;

public class BoardActivity extends Activity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsTheme.apply(this);

        ActionBar mActionBar = getActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new LauncherSettingsFragment())
                .commit();
    }

    /**
     * This fragment shows the launcher preferences.
     */
    public static class LauncherSettingsFragment extends PreferenceFragment {

        EditTextPreference mBoardTitlePreference;
        private SharedPreferences.OnSharedPreferenceChangeListener mListenerOptions;
        private Preference mCustomAppPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
            addPreferencesFromResource(R.xml.board_preferences);

            mBoardTitlePreference = (EditTextPreference) findPreference(BoardUtils.BOARD_TITLE_KEY);

            setBoardTitlePreference();

            mListenerOptions = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                    switch (key) {
                        case BoardUtils.BOARD_TITLE_KEY:
                            updateBoardTitlePreferenceSummary();
                            break;
                        case BoardUtils.CUSTOM_APP_KEY:
                            manageCustomAppPreference();
                            break;
                    }
                }
            };

            mCustomAppPreference = findPreference(BoardUtils.CUSTOM_APP_PREF_KEY);
            manageCustomAppPreference();
            mCustomAppPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), MultiSelectRecyclerViewActivity.class);
                    getActivity().startActivity(intent);
                    return false;
                }
            });
        }

        //add or remove board title preference
        private void setBoardTitlePreference() {

            String summary = BoardUtils.getBoardTitle(getActivity());

            mBoardTitlePreference.setSummary(summary);
        }

        //update board title preference
        private void updateBoardTitlePreferenceSummary() {

            mBoardTitlePreference.setSummary(mBoardTitlePreference.getText());
        }

        private void manageCustomAppPreference() {
            mCustomAppPreference.setEnabled(BoardUtils.isCustomApps(getActivity()));
        }

        @Override
        public void onResume() {
            super.onResume();

            Utilities.getPrefs(getActivity()).registerOnSharedPreferenceChangeListener(mListenerOptions);
        }

        //unregister preferences changes
        @Override
        public void onPause() {

            Utilities.getPrefs(getActivity()).unregisterOnSharedPreferenceChangeListener(mListenerOptions);

            super.onPause();
        }
    }
}
