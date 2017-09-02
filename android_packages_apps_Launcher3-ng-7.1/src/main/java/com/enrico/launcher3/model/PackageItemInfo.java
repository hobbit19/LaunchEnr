package com.enrico.launcher3.model;

import android.graphics.Bitmap;

import com.enrico.launcher3.ItemInfo;

/**
 * Represents a {@link Package} in the widget tray section.
 */
public class PackageItemInfo extends ItemInfo {

    /**
     * A bitmap version of the application icon.
     */
    public Bitmap iconBitmap;

    /**
     * Indicates whether we're using a low res icon.
     */
    public boolean usingLowResIcon;

    /**
     * Package name of the {@link ItemInfo}.
     */
    public String packageName;

    /**
     * Character that is used as a section name for the {@link ItemInfo#title}.
     * (e.g., "G" will be stored if title is "Google")
     */
    public String titleSectionName;

    PackageItemInfo(String packageName) {
        this.packageName = packageName;
    }
}
