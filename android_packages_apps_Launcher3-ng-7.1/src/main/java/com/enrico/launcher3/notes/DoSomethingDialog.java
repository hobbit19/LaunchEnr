package com.enrico.launcher3.notes;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.enrico.launcher3.R;

/**
 * Created by Enrico on 25/07/2017.
 */

public class DoSomethingDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final int pos = getArguments().getInt("pos");

        final ViewGroup nullParent = null;
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        View dialogView = layoutInflater.inflate(R.layout.note_actions_layout, nullParent, false);
        View titleView = layoutInflater.inflate(R.layout.dialogs_custom_title, nullParent, false);
        final TextView dialogTitle = titleView.findViewById(R.id.dialogTitle);

        ImageButton share = dialogView.findViewById(R.id.button_share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                NotesRecyclerViewAdapter.youSureToShare(getActivity(), pos);
            }
        });

        ImageButton delete = dialogView.findViewById(R.id.button_delete);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                NotesRecyclerViewAdapter.youSureToDelete(getActivity(), pos);

            }
        });

        ImageButton copy = dialogView.findViewById(R.id.button_copy);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                NotesRecyclerViewAdapter.copyToClipboard(getActivity(), pos);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(dialogView);
        dialogTitle.setText(R.string.doSomething);
        builder.setCustomTitle(titleView);

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }
}