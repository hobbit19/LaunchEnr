package com.enrico.launcher3.customsettings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.enrico.launcher3.BoardTitleUtils;
import com.enrico.launcher3.LauncherFiles;
import com.enrico.launcher3.MultiSelectRecyclerViewActivity;
import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.about.AboutDialog;
import com.enrico.launcher3.authorizations.PermissionUtils;
import com.enrico.launcher3.icons.IconUtils;
import com.enrico.launcher3.icons.IconsHandler;
import com.enrico.launcher3.icons.RandomIconsTile;
import com.enrico.launcher3.notifications.NotificationUtils;
import com.enrico.launcher3.notifications.NotificationsDotListener;
import com.enrico.launcher3.simplegestures.GesturesUtils;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.about_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.about:

                //open about dialog
                AboutDialog.show(this, true);
                break;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //apply activity's theme if dark theme is enabled
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getBaseContext(), this.getTheme());

        Utilities.applyTheme(contextThemeWrapper, this);

        //set the view
        setContentView(R.layout.settings_activity);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.material_dark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.material_dark));

        //set the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //provide back navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //go back
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new LauncherSettingsFragment())
                .commit();
    }

    /**
     * This fragment shows the launcher preferences.
     */
    public static class LauncherSettingsFragment extends PreferenceFragment {

        private static PreferenceScreen launcherSettings;
        Intent notificationService;
        private SharedPreferences.OnSharedPreferenceChangeListener mListenerOptions;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            notificationService = new Intent(getActivity(), NotificationsDotListener.class);

            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
            addPreferencesFromResource(R.xml.launcher_preferences);

            //Setup Light status bar
            Preference lightStatusBar = findPreference(Utilities.LIGHT_BARS_KEY);
            launcherSettings = getPreferenceScreen();

            //Remove light status bar preference if on lollipop
            if (android.os.Build.VERSION.SDK_INT < 23) {
                launcherSettings.removePreference(lightStatusBar);
            }

            //set different title if on android o
            if (Utilities.ATLEAST_OREO) {
                lightStatusBar.setTitle(getActivity().getString(R.string.light_bars));
                lightStatusBar.setSummary(getActivity().getString(R.string.light_bars_sum));
            }

            Preference hideAppPreference = findPreference(Utilities.KEY_HIDDEN_APPS);

            //open hidden app activity
            hideAppPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent intent = new Intent(getActivity(), MultiSelectRecyclerViewActivity.class);
                    startActivity(intent);
                    return false;
                }
            });

            final Preference badgePosition = findPreference(IconUtils.BADGE_POSITION_KEY);
            final Preference badgeShadow = findPreference(IconUtils.BADGE_SHADOW_KEY);

            //remove badge options if notifications are not enabled
            moreBadgePreferences(badgePosition, badgeShadow);

            final EditTextPreference boardTitlePreference = (EditTextPreference) findPreference(BoardTitleUtils.BOARD_TITLE_KEY);

            addBoardTitlePreference(boardTitlePreference);

            final ListPreference roundIconsPreference = (ListPreference) findPreference(IconUtils.ROUND_ICONS_KEY);

            disableRoundIcons(roundIconsPreference);

            mListenerOptions = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                    switch (key) {

                        case Utilities.THEME_KEY:
                            getActivity().recreate();
                            break;

                        case Utilities.CHOOSE_THEME_KEY:
                            getActivity().recreate();
                            break;

                        case NotificationUtils.NOTIFICATIONS_KEY:

                            //remove badge options if notifications are not enabled
                            moreBadgePreferences(badgePosition, badgeShadow);
                            break;

                        case IconUtils.ICON_PACK_PREFERENCE_KEY:

                            disableRoundIcons(roundIconsPreference);

                            IconsHandler.switchIconPacks(Utilities.getPrefs(getActivity()).getString(IconUtils.ICON_PACK_PREFERENCE_KEY, ""), getActivity());

                            //reload settings if (wtf)user clicks icons tile from settings ... - . =?
                            if (RandomIconsTile.fromTile) {
                                getActivity().recreate();
                                RandomIconsTile.fromTile = false;
                            }
                            break;

                        case BoardTitleUtils.BOARD_TITLE_KEY:
                            updateBoardTitlePreferenceSummary(boardTitlePreference);
                            break;

                        case GesturesUtils.FLING_DOWN_KEY:

                            addBoardTitlePreference(boardTitlePreference);
                            break;
                    }
                }
            };
        }

        @Override
        public void onDestroy() {

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

            AboutDialog.show(getActivity(), false);
            super.onPause();
        }

        //disable round icons if a icon pack is selected
        private void disableRoundIcons(ListPreference roundIconsPreference) {
            if (!Utilities.getPrefs(getActivity()).getString(IconUtils.ICON_PACK_PREFERENCE_KEY, "").isEmpty()) {
                launcherSettings.removePreference(roundIconsPreference);
            } else {
                launcherSettings.addPreference(roundIconsPreference);
            }
        }

        //enabled badge options if the notifications badges are enabled
        private void moreBadgePreferences(Preference badgePosition, Preference badgeShadow) {

            if (NotificationUtils.isNotificationServiceEnabled(getActivity())) {

                launcherSettings.addPreference(badgePosition);
                launcherSettings.addPreference(badgeShadow);

                toggleNotificationListenerService(true);

            } else {

                launcherSettings.removePreference(badgePosition);
                launcherSettings.removePreference(badgeShadow);
            }
        }

        //check if we read to read status bar notifications status
        private void toggleNotificationListenerService(boolean isBadge) {

            if (isBadge) {

                PermissionUtils.askForNotificationAccess(getActivity());
            }
        }

        //update board title preference
        private void updateBoardTitlePreferenceSummary(EditTextPreference boardTitlePreference) {

            boardTitlePreference.setSummary(boardTitlePreference.getText());

        }

        //add or remove board title preference
        private void addBoardTitlePreference(EditTextPreference boardTitlePreference) {

            if (GesturesUtils.isBoardEnabled(getActivity())) {

                launcherSettings.addPreference(boardTitlePreference);

                String summary = BoardTitleUtils.getBoardTitle(getActivity());

                boardTitlePreference.setSummary(summary);

            } else {
                launcherSettings.removePreference(boardTitlePreference);
            }
        }
    }
}
