package com.enrico.launcher3.notes;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Enrico on 04/08/2017.
 */

class NotesUtils {

    static ArrayList<String> initNoteElement(Activity activity, String[] who) {

        //create the notes db
        SQLiteDatabase notesDB = activity.openOrCreateDatabase("notesDB", MODE_PRIVATE, null);

        //create a table called ? with a column called ? where notes titles will be stored
        notesDB.execSQL("CREATE TABLE IF NOT EXISTS " + who[0] + "(id INTEGER PRIMARY KEY AUTOINCREMENT," + who[1] + " varchar);");

        ArrayList<String> selected = new ArrayList<>();

        Cursor notesDBcursor;

        notesDBcursor = notesDB.rawQuery("SELECT * FROM " + who[0], null);

        if (notesDBcursor != null && notesDBcursor.moveToFirst()) {

            while (!notesDBcursor.isAfterLast()) {

                //add items to selected field
                selected.add(notesDBcursor.getString(notesDBcursor.getColumnIndex(who[1])));
                notesDBcursor.moveToNext();
            }
            notesDBcursor.close();
        }

        notesDB.close();

        return selected;
    }

    static void setupNotes(Activity activity, ArrayList<String> titles, ArrayList<String> notes, ArrayList<String> priorities, ArrayList<String> dates, RecyclerView notesRecyclerView) {

        notesRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);

        notesRecyclerView.setLayoutManager(layoutManager);

        NotesRecyclerViewAdapter notesRecyclerViewAdapter = new NotesRecyclerViewAdapter(activity, titles, notes, priorities, dates);

        //set the recycler view adapter and pass arguments to the adapter to it
        notesRecyclerView.setAdapter(notesRecyclerViewAdapter);
    }

    static String selectedItem(Activity activity, String[] who, int where, ArrayList<String> column) {

        //create the notes db
        SQLiteDatabase notesDB = activity.openOrCreateDatabase("notesDB", MODE_PRIVATE, null);

        Cursor notesCursor = notesDB.rawQuery("SELECT * FROM " + who[0], null);

        String selected = "";

        if (notesCursor != null && notesCursor.moveToFirst()) {

            while (!notesCursor.isAfterLast()) {

                //get items at selected position
                selected = column.get(where);
                notesCursor.moveToNext();
            }
            notesCursor.close();
        }

        notesDB.close();

        return selected;
    }
}
