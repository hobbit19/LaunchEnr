package com.enrico.launcher3;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;

import com.enrico.launcher3.AutoInstallsLayout.LayoutParserCallback;
import com.enrico.launcher3.LauncherSettings.Favorites;
import com.enrico.launcher3.util.Thunk;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * A class that parses content values corresponding to some common app types.
 */
class CommonAppTypeParser implements LayoutParserCallback {

    // Including TARGET_NONE
    public static final int SUPPORTED_TYPE_COUNT = 7;

    private static final int RESTORE_FLAG_BIT_SHIFT = 4;

    private static final int TARGET_PHONE = 1;
    private static final int TARGET_MESSENGER = 2;
    private static final int TARGET_EMAIL = 3;
    private static final int TARGET_BROWSER = 4;
    private static final int TARGET_GALLERY = 5;
    private static final int TARGET_CAMERA = 6;
    @Thunk
    private final Context mContext;
    private final long mItemId;
    @Thunk
    private final int mResId;
    ContentValues parsedValues;
    Intent parsedIntent;

    private String parsedTitle;

    CommonAppTypeParser(long itemId, int itemType, Context context) {
        mItemId = itemId;
        mContext = context;
        mResId = getResourceForItemType(itemType);
    }

    private static int getResourceForItemType(int type) {
        switch (type) {
            case TARGET_PHONE:
                return R.xml.app_target_phone;

            case TARGET_MESSENGER:
                return R.xml.app_target_messenger;

            case TARGET_EMAIL:
                return R.xml.app_target_email;

            case TARGET_BROWSER:
                return R.xml.app_target_browser;

            case TARGET_GALLERY:
                return R.xml.app_target_gallery;

            case TARGET_CAMERA:
                return R.xml.app_target_camera;

            default:
                return 0;
        }
    }

    public static int encodeItemTypeToFlag(int itemType) {
        return itemType << RESTORE_FLAG_BIT_SHIFT;
    }

    static int decodeItemTypeFromFlag(int flag) {
        return (flag & ShortcutInfo.FLAG_RESTORED_APP_TYPE) >> RESTORE_FLAG_BIT_SHIFT;
    }

    @Override
    public long generateNewItemId() {
        return mItemId;
    }

    @Override
    public long insertAndCheck(SQLiteDatabase db, ContentValues values) {
        parsedValues = values;

        // Remove unwanted values
        values.put(Favorites.ICON_PACKAGE, (String) null);
        values.put(Favorites.ICON_RESOURCE, (String) null);
        values.put(Favorites.ICON, (byte[]) null);
        return 1;
    }

    /**
     * Tries to find a suitable app to the provided app type.
     */
    boolean findDefaultApp() {
        if (mResId == 0) {
            return false;
        }

        parsedIntent = null;
        parsedValues = null;
        new MyLayoutParser().parseValues();
        return (parsedValues != null) && (parsedIntent != null);
    }

    private class MyLayoutParser extends DefaultLayoutParser {

        MyLayoutParser() {
            super(CommonAppTypeParser.this.mContext, null, CommonAppTypeParser.this,
                    CommonAppTypeParser.this.mContext.getResources(), mResId, TAG_RESOLVE);
        }

        @Override
        protected long addShortcut(String title, Intent intent, int type) {
            if (type == Favorites.ITEM_TYPE_APPLICATION) {
                parsedIntent = intent;
                parsedTitle = title;
            }
            return super.addShortcut(title, intent, type);
        }

        void parseValues() {
            XmlResourceParser parser = mSourceRes.getXml(mLayoutId);
            try {
                beginDocument(parser, mRootTag);
                new ResolveParser().parseAndAdd(parser);
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
            parser.close();
        }
    }

}
