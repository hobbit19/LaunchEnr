package com.enrico.launcher3.notes;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class AsyncLoadNotes {

    public static void execute(Activity activity, RecyclerView notesRecyclerView) {

        new populateNotesList(activity, notesRecyclerView).execute();
    }

    private static class populateNotesList extends AsyncTask<Void, Void, Void> {

        //contacts
        private Activity activity;
        private ArrayList<String> titles, notes, dates, priorities;
        private RecyclerView notesRecyclerView;


        private populateNotesList(Activity activity, RecyclerView notesRecyclerView) {
            this.activity = activity;
            this.notesRecyclerView = notesRecyclerView;
        }

        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {

            //for notes
            titles = NotesUtils.initNoteElement(activity, NoteField.TITLES);

            //for notes
            notes = NotesUtils.initNoteElement(activity, NoteField.NOTES);

            //for priorities
            priorities = NotesUtils.initNoteElement(activity, NoteField.PRIORITIES);

            //for dates
            dates = NotesUtils.initNoteElement(activity, NoteField.DATES);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            //for notes
            NotesUtils.setupNotes(activity, titles, notes, priorities, dates, notesRecyclerView);
        }
    }
}
