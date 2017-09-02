package com.enrico.launcher3.about;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enrico.launcher3.BuildConfig;
import com.enrico.launcher3.R;

/**
 * Created by Enrico on 25/07/2017.
 */

public class AboutDialog extends DialogFragment {

    Animator anim;

    public static void show(Activity activity, boolean show) {

        String tag = "about";
        FragmentManager fm = activity.getFragmentManager();
        DialogFragment dialogFragment;

        if (show) {
            dialogFragment = new AboutDialog();
            dialogFragment.show(fm, tag);

        } else {

            dialogFragment = (DialogFragment) fm.findFragmentByTag(tag);

            if (dialogFragment != null) {
                dialogFragment.dismiss();
            }

        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final ViewGroup nullParent = null;
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        View dialogView = layoutInflater.inflate(R.layout.about_layout, nullParent, false);
        View titleView = layoutInflater.inflate(R.layout.dialogs_custom_title, nullParent, false);
        TextView dialogTitle = titleView.findViewById(R.id.dialogTitle);

        final View appIcon = dialogView.findViewById(R.id.app_icon);

        anim = AnimatorInflater
                .loadAnimator(getActivity(), R.animator.flip);
        anim.setTarget(appIcon);
        anim.start();

        String appVersion = BuildConfig.VERSION_NAME;
        dialogTitle.setText(getActivity().getString(R.string.about) + appVersion);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setCustomTitle(titleView);
        builder.setView(dialogView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();

            }
        });

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // do something
        anim.cancel();
    }
}