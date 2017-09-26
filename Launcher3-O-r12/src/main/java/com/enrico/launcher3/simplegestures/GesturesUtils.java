package com.enrico.launcher3.simplegestures;

import android.animation.Animator;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Toast;

import com.enrico.launcher3.AdminReceiver;
import com.enrico.launcher3.ImmersiveUtils;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.PermissionUtils;
import com.enrico.launcher3.R;
import com.enrico.launcher3.SearchWidgetProvider;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.board.BoardActivity;
import com.enrico.launcher3.board.BoardUtils;
import com.enrico.launcher3.settings.PreferenceKeys;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by Enrico on 09/08/2017.
 */

public class GesturesUtils {

    public static final int FLING_DOWN_DISABLED = 16;
    public static final int FLING_DOWN_BOARD = 19;
    public static final String FLING_DOWN_KEY = "pref_flingDown";
    private static final int FLING_DOWN_SEARCH = 17;
    private static final int FLING_DOWN_STATUS_BAR = 18;

    public static void openOrCloseBoard(final Launcher mLauncher, boolean show, final boolean openEditMode) {

        final View board = mLauncher.getBoardPanel();

        final View appsCard = board.findViewById(R.id.appsCard);
        final View contactsCard = board.findViewById(R.id.contactsCard);
        final View notesCard = board.findViewById(R.id.notesCard);

        final Set<String> customApps = PreferenceManager.getDefaultSharedPreferences(mLauncher).getStringSet(BoardUtils.KEY_CUSTOM_APPS_SET, null);
        if (show) {

            ImmersiveUtils.toggleHideyBar(mLauncher, false);

            int x = board.getRight();
            int y = board.getTop();

            int startRadius = 0;
            int endRadius = (int) Math.hypot(board.getWidth(), board.getHeight());

            Animator anim = ViewAnimationUtils.createCircularReveal(board, x, y, startRadius, endRadius);

            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                    boolean isCustomAppPopulated = customApps != null && !customApps.isEmpty();
                    int appsVisibility = BoardUtils.isCustomApps(mLauncher) ? View.VISIBLE : isCustomAppPopulated ? View.VISIBLE : View.GONE;

                    appsCard.setVisibility(appsVisibility);
                    int contactsVisibility = BoardUtils.isFrequentContacts(mLauncher) ? View.VISIBLE : View.GONE;
                    contactsCard.setVisibility(contactsVisibility);
                    int notesVisibility = BoardUtils.isNotes(mLauncher) ? View.VISIBLE : View.GONE;
                    notesCard.setVisibility(notesVisibility);

                    board.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mLauncher.getHotseat().setVisibility(View.GONE);
                    mLauncher.getWorkspace().setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            anim.start();

        } else {

            ImmersiveUtils.toggleHideyBar(mLauncher, false);

            int x = board.getRight();
            int y = board.getBottom();

            int startRadius = Math.max(board.getWidth(), board.getHeight());
            int endRadius = 0;

            Animator anim = ViewAnimationUtils.createCircularReveal(board, x, y, startRadius, endRadius);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mLauncher.getHotseat().setVisibility(View.VISIBLE);
                    mLauncher.getWorkspace().setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {

                    board.setVisibility(View.INVISIBLE);
                    if (openEditMode) {
                        Intent intent = new Intent(mLauncher, BoardActivity.class);
                        mLauncher.startActivity(intent);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            anim.start();
        }

    }

    private static void expandSB(Launcher mLauncher) {
        //https://gist.github.com/tom-dignan/2468681
        //properly modified since "expand" method does not exist on android versions > 4.2.2
        try {
            Object service = mLauncher.getSystemService("statusbar");
            Class<?> statusBarManager = Class
                    .forName("android.app.StatusBarManager");
            Method expand = statusBarManager.getMethod("expandNotificationsPanel");
            expand.invoke(service);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //method to lock the device
    //https://stackoverflow.com/questions/19745890/how-do-i-lock-phone-programmatically-android
    static void lockDevice(Launcher mLauncher) {

        DevicePolicyManager mDevicePolicyManager;

        mDevicePolicyManager = (DevicePolicyManager) mLauncher.getBaseContext().getSystemService(
                Context.DEVICE_POLICY_SERVICE);

        ComponentName mComponentName = new ComponentName(mLauncher, AdminReceiver.class);

        boolean isAdmin = mDevicePolicyManager.isAdminActive(mComponentName);
        if (isAdmin) {
            mDevicePolicyManager.lockNow();
        } else {
            PermissionUtils.askForAdmin(mLauncher);
        }
    }

    private static void pullToSearch(Launcher mLauncher) {

        if (SearchWidgetProvider.get(mLauncher) == null) {
            Toast.makeText(mLauncher, R.string.no_search_provider, Toast.LENGTH_SHORT)
                    .show();
        } else {
            mLauncher.startSearch("", false, null, false);
        }
    }

    //resolve what to do with pull down
    static void resolveFlingDownTo(Launcher mLauncher) {

        String choice = Utilities.getPrefs(mLauncher)
                .getString(FLING_DOWN_KEY, String.valueOf(16));

        switch (Integer.parseInt(choice)) {

            case FLING_DOWN_SEARCH:

                pullToSearch(mLauncher);
                break;

            case FLING_DOWN_STATUS_BAR:
                expandSB(mLauncher);
                break;

            case FLING_DOWN_BOARD:
                openOrCloseBoard(mLauncher, true, false);
                break;

        }
    }

    //determine if board is enabled
    public static boolean isBoardEnabled(Context context) {

        String choice = Utilities.getPrefs(context)
                .getString(FLING_DOWN_KEY, String.valueOf(16));

        int value = Integer.parseInt(choice);

        return value == FLING_DOWN_BOARD;
    }

    //determine is fling down to somewhere is enabled
    private static boolean isFlingDownToSomething(Context context) {

        String choice = Utilities.getPrefs(context)
                .getString(FLING_DOWN_KEY, String.valueOf(16));

        int value = Integer.parseInt(choice);

        return value != FLING_DOWN_DISABLED;
    }

    //is double tap to lock enabled?
    static boolean isDoubleTapToLockEnabled(Context context) {
        return Utilities.getPrefs(context).getBoolean(PreferenceKeys.DT_LOCK_PREFERENCE_KEY,
                false);
    }

    //determine if gestures (fling or dt) are enabled
    public static boolean areSimpleGesturesEnabled(Context context) {
        return isFlingDownToSomething(context) || isDoubleTapToLockEnabled(context);
    }
}
