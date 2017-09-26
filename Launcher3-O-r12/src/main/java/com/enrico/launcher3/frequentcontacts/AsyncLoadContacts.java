package com.enrico.launcher3.frequentcontacts;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

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

            //for contacts
            contactsList = ContactsUtils.getAllContacts(activity);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            //for contacts
            ContactsUtils.setupContacts(activity, contactsList, contactsRecyclerView);
        }
    }
}
