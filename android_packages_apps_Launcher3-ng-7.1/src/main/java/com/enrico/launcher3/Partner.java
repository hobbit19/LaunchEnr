package com.enrico.launcher3;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Pair;

/**
 * Utilities to discover and interact with partner customizations. There can
 * only be one set of customizations on a device, and it must be bundled with
 * the system.
 */
class Partner {

    public static final String RES_WALLPAPERS = "partner_wallpapers";
    static final String RES_FOLDER = "partner_folder";
    static final String RES_DEFAULT_LAYOUT = "partner_default_layout";
    /**
     * Marker action used to discover partner
     */
    private static final String
            ACTION_PARTNER_CUSTOMIZATION = "com.enrico.launcher3.action.PARTNER_CUSTOMIZATION";
    private static final String RES_DEFAULT_WALLPAPER_HIDDEN = "default_wallpapper_hidden";
    private static final String RES_SYSTEM_WALLPAPER_DIR = "system_wallpaper_directory";

    private static final String RES_REQUIRE_FIRST_RUN_FLOW = "requires_first_run_flow";

    /**
     * These resources are used to override the device profile
     */
    private static final String RES_GRID_NUM_ROWS = "grid_num_rows";
    private static final String RES_GRID_NUM_COLUMNS = "grid_num_columns";
    private static final String RES_GRID_ICON_SIZE_DP = "grid_icon_size_dp";

    private static boolean sSearched = false;
    private static Partner sPartner;
    private final String mPackageName;
    private final Resources mResources;
    private Partner(String packageName, Resources res) {
        mPackageName = packageName;
        mResources = res;
    }

    /**
     * Find and return partner details, or {@code null} if none exists.
     */
    public static synchronized Partner get(PackageManager pm) {
        if (!sSearched) {
            Pair<String, Resources> apkInfo = Utilities.findSystemApk(ACTION_PARTNER_CUSTOMIZATION, pm);
            if (apkInfo != null) {
                sPartner = new Partner(apkInfo.first, apkInfo.second);
            }
            sSearched = true;
        }
        return sPartner;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public Resources getResources() {
        return mResources;
    }

    boolean hasDefaultLayout() {
        int defaultLayout = getResources().getIdentifier(Partner.RES_DEFAULT_LAYOUT,
                "xml", getPackageName());
        return defaultLayout != 0;
    }

/*    public boolean hasFolder() {
        int folder = getResources().getIdentifier(Partner.RES_FOLDER,
                "xml", getPackageName());
        return folder != 0;
    }

    public boolean hideDefaultWallpaper() {
        int resId = getResources().getIdentifier(RES_DEFAULT_WALLPAPER_HIDDEN, "bool",
                getPackageName());
        return resId != 0 && getResources().getBoolean(resId);
    }

    public File getWallpaperDirectory() {
        int resId = getResources().getIdentifier(RES_SYSTEM_WALLPAPER_DIR, "string",
                getPackageName());
        return (resId != 0) ? new File(getResources().getString(resId)) : null;
    }

    public boolean requiresFirstRunFlow() {
        int resId = getResources().getIdentifier(RES_REQUIRE_FIRST_RUN_FLOW, "bool",
                getPackageName());
        return resId != 0 && getResources().getBoolean(resId);
    }*/

    void applyInvariantDeviceProfileOverrides(InvariantDeviceProfile inv, DisplayMetrics dm) {
        int numRows = -1;
        int numColumns = -1;
        float iconSize = -1;

        try {
            int resId = getResources().getIdentifier(RES_GRID_NUM_ROWS,
                    "integer", getPackageName());
            if (resId > 0) {
                numRows = getResources().getInteger(resId);
            }

            resId = getResources().getIdentifier(RES_GRID_NUM_COLUMNS,
                    "integer", getPackageName());
            if (resId > 0) {
                numColumns = getResources().getInteger(resId);
            }

            resId = getResources().getIdentifier(RES_GRID_ICON_SIZE_DP,
                    "dimen", getPackageName());
            if (resId > 0) {
                int px = getResources().getDimensionPixelSize(resId);
                iconSize = Utilities.dpiFromPx(px, dm);
            }
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
            return;
        }

        if (numRows > 0 && numColumns > 0) {
            inv.numRows = numRows;
            inv.numColumns = numColumns;
        }

        if (iconSize > 0) {
            inv.iconSize = iconSize;
        }
    }
}
