package com.enrico.launcher3.model;

import android.content.Context;

import com.enrico.launcher3.AppInfo;
import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.util.Thunk;

import java.text.Collator;
import java.util.Comparator;

/**
 * Class to manage access to an app name comparator.
 * <p>
 * Used to sort application name in all apps view and widget tray view.
 */
public class AppNameComparator {
    private final Collator mCollator;
    private final AbstractUserComparator<ItemInfo> mAppInfoComparator;
    private final Comparator<String> mSectionNameComparator;

    public AppNameComparator(Context context) {
        mCollator = Collator.getInstance();
        mAppInfoComparator = new AbstractUserComparator<ItemInfo>(context) {

            @Override
            public final int compare(ItemInfo a, ItemInfo b) {
                // Order by the title in the current locale
                int result = compareTitles(a.title.toString(), b.title.toString());
                if (result == 0 && a instanceof AppInfo && b instanceof AppInfo) {
                    AppInfo aAppInfo = (AppInfo) a;
                    AppInfo bAppInfo = (AppInfo) b;
                    // If two apps have the same title, then order by the component name
                    result = aAppInfo.componentName.compareTo(bAppInfo.componentName);
                    if (result == 0) {
                        // If the two apps are the same component, then prioritize by the order that
                        // the app user was created (prioritizing the main user's apps)
                        return super.compare(a, b);
                    }
                }
                return result;
            }
        };
        mSectionNameComparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return compareTitles(o1, o2);
            }
        };
    }

    /**
     * Returns a locale-aware comparator that will alphabetically order a list of applications.
     */
    public Comparator<ItemInfo> getAppInfoComparator() {
        return mAppInfoComparator;
    }

    /**
     * Returns a locale-aware comparator that will alphabetically order a list of section names.
     */
    public Comparator<String> getSectionNameComparator() {
        return mSectionNameComparator;
    }

    /**
     * Compares two titles with the same return value semantics as Comparator.
     */
    @Thunk
    private int compareTitles(String titleA, String titleB) {
        // Ensure that we de-prioritize any titles that don't start with a linguistic letter or digit
        boolean aStartsWithLetter = (titleA.length() > 0) &&
                Character.isLetterOrDigit(titleA.codePointAt(0));
        boolean bStartsWithLetter = (titleB.length() > 0) &&
                Character.isLetterOrDigit(titleB.codePointAt(0));
        if (aStartsWithLetter && !bStartsWithLetter) {
            return -1;
        } else if (!aStartsWithLetter && bStartsWithLetter) {
            return 1;
        }

        // Order by the title in the current locale
        return mCollator.compare(titleA, titleB);
    }
}
