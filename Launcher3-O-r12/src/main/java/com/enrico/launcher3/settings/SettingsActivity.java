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

package com.enrico.launcher3.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.provider.Settings.System;
import android.view.Menu;
import android.view.MenuItem;

import com.enrico.launcher3.AndroidVersion;
import com.enrico.launcher3.HiddenAppUtils;
import com.enrico.launcher3.LauncherFiles;
import com.enrico.launcher3.MultiSelectRecyclerViewActivity;
import com.enrico.launcher3.PermissionUtils;
import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.graphics.IconShapeOverride;
import com.enrico.launcher3.icons.IconsManager;
import com.enrico.launcher3.icons.RandomIconsTile;

/**
 * Settings activity for Launcher. Currently implements the following setting: Allow rotation
 */
public class SettingsActivity extends Activity {

    private static final String ICON_BADGING_PREFERENCE_KEY = "pref_icon_badging";
    // TODO: use Settings.Secure.NOTIFICATION_BADGING
    private static final String NOTIFICATION_BADGING = "notification_badging";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.about_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                break;
            case R.id.about:
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsTheme.apply(this);

        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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

        private SystemDisplayRotationLockObserver mRotationLockObserver;
        private IconBadgingObserver mIconBadgingObserver;
        private SharedPreferences.OnSharedPreferenceChangeListener mListenerOptions;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
            addPreferencesFromResource(R.xml.launcher_preferences);

            ContentResolver resolver = getActivity().getContentResolver();

            // Setup allow rotation preference
            Preference rotationPref = findPreference(PreferenceKeys.ALLOW_ROTATION_PREFERENCE_KEY);

            mRotationLockObserver = new SystemDisplayRotationLockObserver(rotationPref, resolver);

            // Register a content observer to listen for system setting changes while
            // this UI is active.
            resolver.registerContentObserver(
                    Settings.System.getUriFor(System.ACCELEROMETER_ROTATION),
                    false, mRotationLockObserver);

            // Initialize the UI once
            mRotationLockObserver.onChange(true);
            rotationPref.setDefaultValue(PreferencesState.getAllowRotationDefaultValue(getActivity()));

            Preference iconBadgingPref = findPreference(ICON_BADGING_PREFERENCE_KEY);

            Preference iconShapeOverride = findPreference(IconShapeOverride.KEY_PREFERENCE);

            if (AndroidVersion.isAtLeastOreo()) {

                // Listen to system notification badge settings while this UI is active.
                mIconBadgingObserver = new IconBadgingObserver(iconBadgingPref, resolver);
                resolver.registerContentObserver(
                        Settings.Secure.getUriFor(NOTIFICATION_BADGING),
                        false, mIconBadgingObserver);
                mIconBadgingObserver.onChange(true);
            }

            if (!AndroidVersion.isAtLeastOreo()) {
                setOreoSupportSum(iconShapeOverride, iconBadgingPref);
            }

            if (iconShapeOverride != null) {

                IconShapeOverride.handlePreferenceUi((ListPreference) iconShapeOverride);
            }

            Preference hideAppPreference = findPreference(HiddenAppUtils.KEY_HIDDEN_APPS);

            hideAppPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent intent = new Intent(getActivity(), MultiSelectRecyclerViewActivity.class);
                    startActivity(intent);
                    return false;
                }
            });

            disableRoundIcons();

            mListenerOptions = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                    switch (key) {
                        case PreferenceKeys.THEME_PREFERENCE_KEY:
                            getActivity().recreate();
                            break;

                        case PreferenceKeys.CHOOSE_THEME_PREFERENCE_KEY:
                            getActivity().recreate();
                            break;

                        case PreferenceKeys.SHOW_BADGE_PREFERENCE_KEY:

                            //remove badge options if notifications are not enabled
                            if (PreferencesState.isShowBadgePrefEnabled(getActivity())) {

                                PermissionUtils.checkNotificationAccess(getActivity());
                            }
                            break;

                        case IconsManager.ICON_PACK_PREFERENCE_KEY:

                            disableRoundIcons();

                            IconsManager.switchIconPacks(Utilities.getPrefs(getActivity()).getString(IconsManager.ICON_PACK_PREFERENCE_KEY, ""), getActivity());

                            //reload settings if (wtf)user clicks icons tile from settings ... - . =?
                            if (RandomIconsTile.fromTile) {
                                getActivity().recreate();
                                RandomIconsTile.fromTile = false;
                            }
                            break;

                    }
                }
            };
        }

        @Override
        public void onDestroy() {
            if (mRotationLockObserver != null) {
                getActivity().getContentResolver().unregisterContentObserver(mRotationLockObserver);
                mRotationLockObserver = null;
            }
            if (mIconBadgingObserver != null) {
                getActivity().getContentResolver().unregisterContentObserver(mIconBadgingObserver);
                mIconBadgingObserver = null;
            }
            super.onDestroy();
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

        private void setOreoSupportSum(Preference... preferences) {
            for (Preference preference : preferences) {
                preference.setEnabled(false);
                preference.setSummary(getString(R.string.android_O_only));
            }
        }

        //disable round icons if a icon pack is selected
        private void disableRoundIcons() {

            Preference roundIconsPreference = findPreference(IconsManager.ROUND_ICONS_KEY);

            roundIconsPreference.setEnabled(Utilities.getPrefs(getActivity()).getString(IconsManager.ICON_PACK_PREFERENCE_KEY, "").isEmpty());
        }
    }

    /**
     * Content observer which listens for system auto-rotate setting changes, and enables/disables
     * the launcher rotation setting accordingly.
     */
    private static class SystemDisplayRotationLockObserver extends ContentObserver {

        private final Preference mRotationPref;
        private final ContentResolver mResolver;

        SystemDisplayRotationLockObserver(
                Preference rotationPref, ContentResolver resolver) {
            super(new Handler());
            mRotationPref = rotationPref;
            mResolver = resolver;
        }

        @Override
        public void onChange(boolean selfChange) {
            boolean enabled = Settings.System.getInt(mResolver,
                    Settings.System.ACCELEROMETER_ROTATION, 1) == 1;
            mRotationPref.setEnabled(enabled);
            mRotationPref.setSummary(enabled
                    ? R.string.allow_rotation_desc : R.string.allow_rotation_blocked_desc);
        }
    }

    /**
     * Content observer which listens for system badging setting changes,
     * and updates the launcher badging setting subtext accordingly.
     */
    private static class IconBadgingObserver extends ContentObserver {

        private final Preference mBadgingPref;
        private final ContentResolver mResolver;

        IconBadgingObserver(Preference badgingPref, ContentResolver resolver) {
            super(new Handler());
            mBadgingPref = badgingPref;
            mResolver = resolver;
        }

        @Override
        public void onChange(boolean selfChange) {
            boolean enabled = Settings.Secure.getInt(mResolver, NOTIFICATION_BADGING, 1) == 1;
            mBadgingPref.setSummary(enabled
                    ? R.string.icon_badging_desc_on
                    : R.string.icon_badging_desc_off);
        }
    }
}
