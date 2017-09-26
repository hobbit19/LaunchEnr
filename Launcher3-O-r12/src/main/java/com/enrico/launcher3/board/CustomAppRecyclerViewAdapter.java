package com.enrico.launcher3.board;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.enrico.launcher3.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Enrico on 21/07/2017.
 */

class CustomAppRecyclerViewAdapter extends RecyclerView.Adapter<CustomAppRecyclerViewAdapter.SimpleViewHolder> {

    private List<String> mApps = new ArrayList<>();
    private PackageManager mPackageManager;
    private Activity mActivity;

    //simple recycler view adapter with activity and array list contact as arguments
    CustomAppRecyclerViewAdapter(Activity activity, List<String> apps) {

        mApps = apps;
        mActivity = activity;
        mPackageManager = activity.getPackageManager();
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // inflate recycler view items layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_item, parent, false);

        return new SimpleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {

        ApplicationInfo applicationInfo;

        try {
            applicationInfo = mPackageManager.getApplicationInfo(mApps.get(holder.getAdapterPosition()), 0);

            holder.name.setText(mPackageManager.getApplicationLabel(applicationInfo));

            Drawable d = mPackageManager.getApplicationIcon(applicationInfo);

            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();

            Bitmap roundPackageIcon = BoardUtils.createRoundIcon(mActivity, bitmap);

            holder.thumbnail.setImageBitmap(roundPackageIcon);

        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {

        //get array length
        return mApps.size();
    }

    private void startActivity(String packageName) {
        Intent intent = mPackageManager.getLaunchIntentForPackage(packageName);

        if (intent != null) {
            // We found the activity now start the activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mActivity.startActivity(intent);

        } else {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=" + packageName));
            mActivity.startActivity(intent);
        }
    }

    //simple view holder implementing click and long click listeners and with activity and itemView as arguments
    class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;

        ImageView thumbnail;

        SimpleViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);

            thumbnail = itemView.findViewById(R.id.thumbnail);

            //enable click and on long click
            itemView.setOnClickListener(this);
        }

        //add click
        @Override
        public void onClick(View v) {
            startActivity(mApps.get(getAdapterPosition()));
        }
    }
}
