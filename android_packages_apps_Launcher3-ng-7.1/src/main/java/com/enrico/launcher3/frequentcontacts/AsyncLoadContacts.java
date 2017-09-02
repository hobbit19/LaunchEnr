package com.enrico.launcher3.frequentcontacts;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.simplegestures.GesturesUtils;

import java.util.ArrayList;

public class AsyncLoadContacts {

    public static void execute(Activity activity, RecyclerView contactsRecyclerView) {

        new populateContactsList(activity, contactsRecyclerView).execute();
    }

    private static class populateContactsList extends AsyncTask<Void, Void, Void> {

        //contacts
        private Activity activity;
        private ArrayList<Contact> contactsList;
        private RecyclerView contactsRecyclerView;

        private populateContactsList(Activity activity, RecyclerView contactsRecyclerView) {
            this.activity = activity;
            this.contactsRecyclerView = contactsRecyclerView;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(activity, activity.getString(R.string.problem), Toast.LENGTH_LONG)
                        .show();

                Utilities.getPrefs(activity).edit().putString(GesturesUtils.FLING_DOWN_KEY, String.valueOf(GesturesUtils.FLING_DOWN_DISABLED)).apply();

                this.cancel(true);

            } else {
                //for contacts
                contactsList = ContactsUtils.getAllContacts(activity);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            //for contacts
            ContactsUtils.setupContacts(activity, contactsList, contactsRecyclerView);
        }
    }
}
