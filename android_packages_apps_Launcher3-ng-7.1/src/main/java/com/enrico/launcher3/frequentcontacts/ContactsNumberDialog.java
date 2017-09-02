package com.enrico.launcher3.frequentcontacts;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enrico.launcher3.R;

import java.util.ArrayList;

/**
 * Created by Enrico on 25/07/2017.
 */

public class ContactsNumberDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ArrayList<String> contactPhones = getArguments().getStringArrayList("contactPhones");

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        final ViewGroup nullParent = null;

        View dialogView = layoutInflater.inflate(R.layout.contact_phones_layout, nullParent, false);

        View titleView = layoutInflater.inflate(R.layout.dialogs_custom_title, nullParent, false);
        TextView dialogTitle = titleView.findViewById(R.id.dialogTitle);

        dialogTitle.setText(R.string.chooseNumber);

        RecyclerView phones_rv = dialogView.findViewById(R.id.phones_rv);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity());

        phones_rv.setHasFixedSize(true);
        phones_rv.setLayoutManager(layoutManager);

        //set the recycler view adapter and pass arguments to the adapter to it
        phones_rv.setAdapter(new PhonesRecyclerViewAdapter(getActivity(), contactPhones));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(dialogView);
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