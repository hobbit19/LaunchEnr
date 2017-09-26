package com.enrico.launcher3.board;

import android.app.Activity;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AsyncLoadApps {

    public static void execute(Activity activity, RecyclerView contactsRecyclerView) {

        new populateContactsList(activity, contactsRecyclerView).execute();
    }

    private static class populateContactsList extends AsyncTask<Void, Void, Void> {

        //contacts
        private Activity activity;
        private List<String> mApps = new ArrayList<>();
        private RecyclerView customAppsRecyclerView;

        private populateContactsList(Activity activity, RecyclerView customAppsRecyclerView) {
            this.activity = activity;
            this.customAppsRecyclerView = customAppsRecyclerView;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Set<String> set = PreferenceManager.getDefaultSharedPreferences(activity).getStringSet(BoardUtils.KEY_CUSTOM_APPS_SET, null);
            if (set != null) {
                mApps.addAll(set);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            //for contacts
            BoardUtils.setupApps(activity, mApps, customAppsRecyclerView);
        }
    }
}
