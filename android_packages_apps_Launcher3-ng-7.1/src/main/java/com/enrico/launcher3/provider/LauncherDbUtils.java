package com.enrico.launcher3.provider;

import android.database.Cursor;

import com.enrico.launcher3.LauncherSettings.WorkspaceScreens;

import java.util.ArrayList;

/**
 * A set of utility methods for Launcher DB used for DB updates and migration.
 */
public class LauncherDbUtils {

    /**
     * Makes the first screen as screen 0 (if screen 0 already exists,
     * renames it to some other number).
     * If the first row of screen 0 is non empty, runs a 'lossy' GridMigrationTask to clear
     * the first row. The items in the first screen are moved and resized but the carry-forward
     * items are simply deleted.
     */

    /**
     * Parses the cursor containing workspace screens table and returns the list of screen IDs
     */
    public static ArrayList<Long> getScreenIdsFromCursor(Cursor sc) {
        ArrayList<Long> screenIds = new ArrayList<>();
        try {
            final int idIndex = sc.getColumnIndexOrThrow(WorkspaceScreens._ID);
            while (sc.moveToNext()) {
                try {
                    screenIds.add(sc.getLong(idIndex));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            sc.close();
        }
        return screenIds;
    }
}
