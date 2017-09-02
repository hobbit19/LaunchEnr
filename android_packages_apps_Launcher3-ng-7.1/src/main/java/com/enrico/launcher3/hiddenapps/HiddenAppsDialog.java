package com.enrico.launcher3.hiddenapps;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
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
import com.enrico.launcher3.Utilities;

import java.util.Set;

/**
 * Created by Enrico on 25/07/2017.
 */

public class HiddenAppsDialog extends DialogFragment {

    private HiddenAppsListener hiddenAppsListener;

    public static void show(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        HiddenAppsDialog dialogFragment = new HiddenAppsDialog();

        dialogFragment.show(fm, "showMe");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        hiddenAppsListener = (HiddenAppsListener) getActivity();

        final ViewGroup nullParent = null;
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        final View dialogView = layoutInflater.inflate(R.layout.hidden_apps_layout, nullParent, false);
        View titleView = layoutInflater.inflate(R.layout.dialogs_custom_title, nullParent, false);
        TextView dialogTitle = titleView.findViewById(R.id.dialogTitle);

        RecyclerView hidden_apps_rv = dialogView.findViewById(R.id.hiddenAppsRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        hidden_apps_rv.setHasFixedSize(true);
        hidden_apps_rv.setLayoutManager(layoutManager);

        //set the recycler view adapter and pass arguments to the adapter to it
        hidden_apps_rv.setAdapter(new HiddenAppsAdapter(getActivity()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setCancelable(false);
        builder.setView(dialogView);
        dialogTitle.setText(R.string.hidden_title);
        builder.setCustomTitle(titleView);

        final Set<String> hiddenApps = HiddenAppsUtils.hiddenComponents(getActivity());

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (HiddenAppsAdapter.visibleApps.size() > 0 && hiddenApps != null) {

                    //get visible apps list and remove objects from hidden apps hash set
                    for (String cn : HiddenAppsAdapter.visibleApps) {
                        hiddenApps.remove(cn);
                    }

                    //update the preference
                    Utilities.getPrefs(getActivity()).edit().putStringSet(Utilities.HIDDEN_APPS_SET_KEY, hiddenApps).apply();

                    //clear visible apps
                    HiddenAppsAdapter.visibleApps.clear();

                    //call this method to manage the related preference in settings fragment
                    hiddenAppsListener.onHiddenAppsEdit();
                }
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                manageDismiss();
            }
        });

        return builder.create();
    }

    //on dismiss be sure to clear the visible apps or next time if we do not select anything
    //the hidden apps hash set will be updated anyway
    private void manageDismiss() {
        if (HiddenAppsAdapter.visibleApps.size() > 0) {
            HiddenAppsAdapter.visibleApps.clear();
        }
        dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        manageDismiss();
    }

    public interface HiddenAppsListener {
        void onHiddenAppsEdit();
    }
}