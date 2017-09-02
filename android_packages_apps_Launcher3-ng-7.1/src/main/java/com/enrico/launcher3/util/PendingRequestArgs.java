package com.enrico.launcher3.util;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.LauncherAppWidgetProviderInfo;

/**
 * Utility class to store information regarding a pending request made by launcher. This information
 * can be saved across launcher instances.
 */
public class PendingRequestArgs extends ItemInfo implements Parcelable {

    public static final Parcelable.Creator<PendingRequestArgs> CREATOR =
            new Parcelable.Creator<PendingRequestArgs>() {
                public PendingRequestArgs createFromParcel(Parcel source) {
                    return new PendingRequestArgs(source);
                }

                public PendingRequestArgs[] newArray(int size) {
                    return new PendingRequestArgs[size];
                }
            };
    private static final int TYPE_NONE = 0;
    private static final int TYPE_INTENT = 1;
    private static final int TYPE_APP_WIDGET = 2;
    private final int mArg1;
    private final int mObjectType;
    private final Parcelable mObject;

    public PendingRequestArgs(ItemInfo info) {
        mArg1 = 0;
        mObjectType = TYPE_NONE;
        mObject = null;

        copyFrom(info);
    }

    private PendingRequestArgs(int arg1, int objectType, Parcelable object) {
        mArg1 = arg1;
        mObjectType = objectType;
        mObject = object;
    }

    private PendingRequestArgs(Parcel parcel) {
        readFromValues(ContentValues.CREATOR.createFromParcel(parcel));

        mArg1 = parcel.readInt();
        mObjectType = parcel.readInt();
        if (parcel.readInt() != 0) {
            mObject = mObjectType == TYPE_INTENT
                    ? Intent.CREATOR.createFromParcel(parcel)
                    : new LauncherAppWidgetProviderInfo(parcel);
        } else {
            mObject = null;
        }
    }

    public static PendingRequestArgs forWidgetInfo(
            int appWidgetId, LauncherAppWidgetProviderInfo widgetInfo, ItemInfo info) {
        PendingRequestArgs args = new PendingRequestArgs(appWidgetId, TYPE_APP_WIDGET, widgetInfo);
        args.copyFrom(info);
        return args;
    }

    public static PendingRequestArgs forIntent(int requestCode, Intent intent, ItemInfo info) {
        PendingRequestArgs args = new PendingRequestArgs(requestCode, TYPE_INTENT, intent);
        args.copyFrom(info);
        return args;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ContentValues itemValues = new ContentValues();
        writeToValues(itemValues);
        itemValues.writeToParcel(dest, flags);

        dest.writeInt(mArg1);
        dest.writeInt(mObjectType);
        if (mObject != null) {
            dest.writeInt(1);
            mObject.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public LauncherAppWidgetProviderInfo getWidgetProvider() {
        return mObjectType == TYPE_APP_WIDGET ? (LauncherAppWidgetProviderInfo) mObject : null;
    }

    public int getWidgetId() {
        return mObjectType == TYPE_APP_WIDGET ? mArg1 : 0;
    }

    public Intent getPendingIntent() {
        return mObjectType == TYPE_INTENT ? (Intent) mObject : null;
    }

    public int getRequestCode() {
        return mObjectType == TYPE_INTENT ? mArg1 : 0;
    }
}
