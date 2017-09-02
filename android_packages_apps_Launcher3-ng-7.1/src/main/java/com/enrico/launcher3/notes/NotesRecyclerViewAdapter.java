package com.enrico.launcher3.notes;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.enrico.launcher3.R;
import com.enrico.launcher3.customsettings.ThemePreference;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Enrico on 21/07/2017.
 */

class NotesRecyclerViewAdapter extends RecyclerView.Adapter<NotesRecyclerViewAdapter.SimpleViewHolder> {

    private static ArrayList<String> notesTitles, notesBodies, notesDates, notesPriorities;

    private Activity activity;

    //simple recycler view adapter with activity and string array as arguments
    NotesRecyclerViewAdapter(Activity activity, ArrayList<String> titles, ArrayList<String> notes, ArrayList<String> priorities, ArrayList<String> dates) {
        this.activity = activity;
        notesTitles = titles;
        notesBodies = notes;
        notesDates = dates;
        notesPriorities = priorities;
    }

    static void youSureToShare(final Activity activity, final int pos) {

        final RecyclerView notesRecyclerView = activity.findViewById(R.id.notesRecyclerView);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity);

        alertDialogBuilder.setTitle(activity.getString(R.string.uSure));

        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.dismiss();
                                notesRecyclerView.getAdapter().notifyDataSetChanged();

                            }
                        }
                )
                .setPositiveButton(activity.getString(R.string.share), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        notesRecyclerView.getAdapter().notifyDataSetChanged();
                        shareNote(activity, pos);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

    }

    static void copyToClipboard(Activity activity, int pos) {

        String body = notesBodies.get(pos);
        String date = notesDates.get(pos);
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("clipboard data ", body.substring(0, body.length() - date.length()));
        clipboard.setPrimaryClip(clip);

        Toast.makeText(activity, activity.getString(R.string.copied), Toast.LENGTH_SHORT)
                .show();

    }

    private static void shareNote(Activity activity, int pos) {

        String body = notesBodies.get(pos);
        String date = notesDates.get(pos);

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, body.substring(0, body.length() - date.length()));
        activity.startActivity(Intent.createChooser(sharingIntent, activity.getString(R.string.shareWith)));
    }

    static void youSureToDelete(final Activity activity, final int pos) {

        final RecyclerView notesRecyclerView = activity.findViewById(R.id.notesRecyclerView);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity);

        alertDialogBuilder.setTitle(activity.getString(R.string.uSure));

        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.dismiss();
                                notesRecyclerView.getAdapter().notifyDataSetChanged();

                            }
                        }
                )
                .setPositiveButton(activity.getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        deleteNote(activity, pos);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

    }

    private static void deleteNote(Activity activity, int pos) {

        SQLiteDatabase notesDB = activity.openOrCreateDatabase("notesDB", MODE_PRIVATE, null);

        String title = NotesUtils.selectedItem(activity, NoteField.TITLES, pos, notesTitles);
        String note = NotesUtils.selectedItem(activity, NoteField.NOTES, pos, notesBodies);
        String date = NotesUtils.selectedItem(activity, NoteField.DATES, pos, notesDates);
        String priority = NotesUtils.selectedItem(activity, NoteField.PRIORITIES, pos, notesPriorities);


        String titlesTable, notesTable, datesTable, prioritiesTable,
                whereClause_title, whereClause_note, whereClause_date, whereClause_priority;

        String[] whereArgs_title, whereArgs_note, whereArgs_date, whereArgs_priority;

        //set the names of the tables
        titlesTable = "titles";
        notesTable = "notes";
        datesTable = "dates";
        prioritiesTable = "priorities";

        //set where clause
        whereClause_title = "title" + "=?";
        whereClause_note = "note" + "=?";
        whereClause_date = "date" + "=?";
        whereClause_priority = "priority" + "=?";

        //set the where arguments
        whereArgs_title = new String[]{title};
        whereArgs_note = new String[]{note};
        whereArgs_date = new String[]{date};
        whereArgs_priority = new String[]{priority};

        //delete 'em all
        notesDB.delete(titlesTable, whereClause_title, whereArgs_title);
        notesDB.delete(notesTable, whereClause_note, whereArgs_note);
        notesDB.delete(datesTable, whereClause_date, whereArgs_date);
        notesDB.delete(prioritiesTable, whereClause_priority, whereArgs_priority);

        //update data arrays and update the recycler view

        //remove all the adapter data
        notesTitles.remove(pos);
        notesBodies.remove(pos);
        notesDates.remove(pos);
        notesPriorities.remove(pos);

        NotesRecyclerViewAdapter notesRecyclerViewAdapter = new NotesRecyclerViewAdapter(activity, notesTitles, notesBodies, notesPriorities, notesDates);

        RecyclerView notesRecyclerView = activity.findViewById(R.id.notesRecyclerView);

        //and update the dynamic list
        //don't move this method above the db deletion method or you'll get javalangindexoutofboundsexception-invalid-index error
        notesRecyclerView.getAdapter().notifyDataSetChanged();

        notesRecyclerView.setAdapter(notesRecyclerViewAdapter);

    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // inflate recycler view items layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new SimpleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {

        String title = notesTitles.get(holder.getAdapterPosition());
        String note = notesBodies.get(holder.getAdapterPosition());
        String date = notesDates.get(holder.getAdapterPosition());
        String priority = notesPriorities.get(holder.getAdapterPosition());

        holder.title.setText(title.substring(0, title.length() - date.length()));
        holder.note.setText(note.substring(0, note.length() - date.length()));
        holder.date.setText(date);

        int priorityColor;

        if (notesPriorities.size() != 0) {

            priorityColor = ContextCompat.getColor(activity, Integer.valueOf(priority.substring(0, priority.length() - date.length())));
            ThemePreference.createCircularPreferenceBitmap(true, null, holder.priority, activity, priorityColor);
        }

        //show divider only if there are > 1 notes
        int visibility = notesPriorities.size() > 1 ? View.VISIBLE : View.GONE;

        holder.divider.setVisibility(visibility);

        holder.note.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                //if we have a link, the long click on item will not open our beloved dialog
                openActionsDialog(holder.getAdapterPosition());
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {

        //get array length
        return notesBodies.size();
    }

    private void openActionsDialog(int pos) {

        Bundle bundle = new Bundle();

        bundle.putInt("pos", pos);

        FragmentManager fm = activity.getFragmentManager();
        DoSomethingDialog dialogFragment = new DoSomethingDialog();

        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "dosomethingwithme");
    }

    //simple view holder implementing click and long click listeners and with activity and itemView as arguments
    class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        private ImageView priority;
        private TextView title, note, date;
        private View divider;

        SimpleViewHolder(View itemView) {
            super(itemView);

            this.divider = itemView.findViewById(R.id.divider);
            this.title = itemView.findViewById(R.id.title);
            this.note = itemView.findViewById(R.id.note);
            this.date = itemView.findViewById(R.id.date);
            this.priority = itemView.findViewById(R.id.priority);

            itemView.setOnLongClickListener(this);

        }

        @Override
        public boolean onLongClick(View v) {

            openActionsDialog(getAdapterPosition());
            return false;
        }
    }
}