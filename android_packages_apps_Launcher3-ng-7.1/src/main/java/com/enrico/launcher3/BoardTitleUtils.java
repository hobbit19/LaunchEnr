package com.enrico.launcher3;

import android.content.Context;

/**
 * Created by Enrico on 12/08/2017.
 */

public class BoardTitleUtils {

    public static final String BOARD_TITLE_KEY = "pref_boardTitle";

    //retrieve board title
    public static String getBoardTitle(final Context context) {

        return Utilities.getPrefs(context).getString(BOARD_TITLE_KEY, context.getString(R.string.board));
    }
}
