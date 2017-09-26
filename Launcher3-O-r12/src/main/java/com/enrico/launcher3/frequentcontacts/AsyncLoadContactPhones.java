package com.enrico.launcher3.frequentcontacts;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.ArrayList;

class AsyncLoadContactPhones {

    public static void execute(Activity activity, String contactId) {

        new populateContactPhonesList(activity, contactId).execute();
    }

    private static class populateContactPhonesList extends AsyncTask<Void, Void, Void> {

        Activity activity;
        ArrayList<String> phonesList;
        private String contactId;

        private populateContactPhonesList(Activity activity, String contactId) {
            this.activity = activity;
            this.contactId = contactId;
        }

        @Override
        protected Void doInBackground(Void... params) {

            phonesList = ContactsUtils.getNumbers(activity, contactId);

            if (phonesList.isEmpty()) {
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (phonesList.size() > 1) {

                Bundle bundle = new Bundle();

                FragmentManager fm = activity.getFragmentManager();

                bundle.putStringArrayList("contactPhones", phonesList);

                ContactsNumberDialog dialogFragment = new ContactsNumberDialog();

                dialogFragment.setArguments(bundle);

                dialogFragment.show(fm, "callMe");

            } else {

                CallUtil.performCall(activity, phonesList.get(0));
            }
        }
    }
}
