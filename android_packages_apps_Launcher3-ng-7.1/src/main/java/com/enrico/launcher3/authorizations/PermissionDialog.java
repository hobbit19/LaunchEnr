package com.enrico.launcher3.authorizations;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enrico.launcher3.R;

/**
 * Created by Enrico on 25/07/2017.
 */

public class PermissionDialog extends DialogFragment {

    private int requestCode;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        requestCode = getArguments().getInt("requestCode");

        final ViewGroup nullParent = null;

        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        View dialogView = layoutInflater.inflate(R.layout.permission_layout, nullParent, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View titleView = layoutInflater.inflate(R.layout.dialogs_custom_title, nullParent, false);
        TextView dialogTitle = titleView.findViewById(R.id.dialogTitle);

        dialogTitle.setText(PermissionUtils.getPermissionRequested(getActivity(), requestCode)[1]);

        TextView message = dialogView.findViewById(R.id.rationale);

        message.setText(PermissionUtils.getPermissionRequested(getActivity(), requestCode)[2]);

        builder.setCustomTitle(titleView);

        builder.setView(dialogView);

        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);

        if (requestCode == PermissionUtils.NOTIFICATION_CODE || requestCode == PermissionUtils.ADMIN_CODE) {

            switch (requestCode) {

                case PermissionUtils.NOTIFICATION_CODE:
                    PermissionUtils.askNotificationAccess(getActivity());
                    break;

                case PermissionUtils.ADMIN_CODE:
                    PermissionUtils.askForAdmin(getActivity());
                    break;
            }

        } else {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]
                            {PermissionUtils.getPermissionRequested(getActivity(), requestCode)[0]}
                    , requestCode);
        }
    }
}