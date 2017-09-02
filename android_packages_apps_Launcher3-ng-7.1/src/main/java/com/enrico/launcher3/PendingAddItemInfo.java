package com.enrico.launcher3;

import android.content.ComponentName;

/**
 * Meta data that is used for deferred binding.
 * e.g., this object is used to pass information on dragable targets when they are dropped onto
 * the workspace from another container.
 */
public class PendingAddItemInfo extends ItemInfo {

    /**
     * The component that will be created.
     */
    public ComponentName componentName;
}
