package com.enrico.launcher3.board;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.icons.IconColorExtractor;

import java.util.List;

/**
 * Created by Enrico on 25/09/2017.
 */

public class BoardUtils {

    public static final String KEY_CUSTOM_APPS_SET = "custom-apps";
    public static final String BOARD_TITLE_KEY = "pref_boardTitle";
    static final String CUSTOM_APP_KEY = "custom-apps";
    static final String CUSTOM_APP_PREF_KEY = "pref_customApp";
    private static final String FREQUENT_CONTACTS_KEY = "contacts";
    private static final String NOTES_KEY = "notes";

    public static boolean isCustomApps(Context context) {
        return Utilities.getPrefs(context).getBoolean(CUSTOM_APP_KEY,
                false);
    }

    public static boolean isFrequentContacts(Context context) {
        return Utilities.getPrefs(context).getBoolean(FREQUENT_CONTACTS_KEY,
                true);
    }

    public static boolean isNotes(Context context) {
        return Utilities.getPrefs(context).getBoolean(NOTES_KEY,
                true);
    }

    static void setupApps(final Activity activity, List<String> apps, RecyclerView customAppsRecyclerView) {

        customAppsRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        customAppsRecyclerView.setLayoutManager(linearLayoutManager);

        CustomAppRecyclerViewAdapter recyclerViewAdapter = new CustomAppRecyclerViewAdapter(activity, apps);

        customAppsRecyclerView.setAdapter(recyclerViewAdapter);
    }

    //create round icon enrico's style
    public static Bitmap createRoundIcon(Context context, Bitmap defaultIcon) {

        //calculate dimensions
        //-1 to take into account the shadow layer
        int w = defaultIcon.getWidth();
        int h = defaultIcon.getHeight();
        int r = w / 2 - 1;

        //create bitmap
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        //draw a circle of the same dimensions
        Canvas canvas = new Canvas(b);
        Paint paint = new Paint();
        paint.setColor(IconColorExtractor.get(context, defaultIcon));
        final int SHADOW_COLOR = 0x80000000;
        paint.setShadowLayer(0.5f, 1, 1, SHADOW_COLOR);
        paint.setAntiAlias(true);
        canvas.drawCircle(r, r, r, paint);

        //scale default icon and center inside the canvas
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(defaultIcon, r, r, true);
        canvas.drawBitmap(scaledBitmap, (r * 2 - scaledBitmap.getWidth()) / 2, (r * 2 - scaledBitmap.getHeight()) / 2, paint);

        return b;
    }

    //retrieve board title
    public static String getBoardTitle(final Context context) {

        return Utilities.getPrefs(context).getString(BOARD_TITLE_KEY, context.getString(R.string.board));
    }
}
