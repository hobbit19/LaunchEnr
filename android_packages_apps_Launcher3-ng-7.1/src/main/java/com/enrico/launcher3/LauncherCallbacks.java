package com.enrico.launcher3;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import com.enrico.launcher3.allapps.AllAppsSearchBarController;
import com.enrico.launcher3.util.ComponentKey;

import java.util.ArrayList;
import java.util.List;

/**
 * LauncherCallbacks is an interface used to extend the Launcher activity. It includes many hooks
 * in order to add additional functionality. Some of these are very general, and give extending
 * classes the ability to react to Activity life-cycle or specific user interactions. Others
 * are more specific and relate to replacing parts of the application, for example, the search
 * interface or the wallpaper picker.
 */
interface LauncherCallbacks {

    /*
     * Activity life-cycle methods. These methods are triggered after
     * the code in the corresponding Launcher method is executed.
     */
    void preOnCreate();

    void onCreate(Bundle savedInstanceState);

    void preOnResume();

    void onResume();

    void onStart();

    void onStop();

    void onPause();

    void onDestroy();

    void onSaveInstanceState(Bundle outState);

    void onPostCreate(Bundle savedInstanceState);

    void onNewIntent(Intent intent);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    int[] grantResults);

    void onWindowFocusChanged(boolean hasFocus);

    void onAttachedToWindow();

    void onDetachedFromWindow();

    boolean onPrepareOptionsMenu(Menu menu);

    void onHomeIntent();

    boolean handleBackPressed();

    void onTrimMemory(int level);

    /*
     * Extension points for providing custom behavior on certain user interactions.
     */
    void onLauncherProviderChange();

    void finishBindingItems(final boolean upgradePath);

    void bindAllApplications(ArrayList<AppInfo> apps);

    void onInteractionBegin();

    void onInteractionEnd();

    @Deprecated
    void onWorkspaceLockedChanged();

    /**
     * Starts a search with {@param initialQuery}. Return false if search was not started.
     */
    boolean startSearch(
            String initialQuery, boolean selectInitialQuery, Bundle appSearchData);

    boolean hasCustomContentToLeft();

    void populateCustomContentContainer();

    /*
     * Extensions points for adding / replacing some other aspects of the Launcher experience.
     */

    boolean shouldMoveToDefaultScreenOnHomeIntent();

    boolean hasSettings();

    AllAppsSearchBarController getAllAppsSearchBarController();

    List<ComponentKey> getPredictedApps();

    boolean shouldShowDiscoveryBounce();
}
