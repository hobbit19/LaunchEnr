package com.enrico.launcher3.hiddenapps;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.icons.IconCache;

import java.util.ArrayList;

/**
 * Created by Enrico on 21/07/2017.
 */

class HiddenAppsAdapter extends RecyclerView.Adapter<HiddenAppsAdapter.SimpleViewHolder> {

    static ArrayList<String> visibleApps = new ArrayList<>();
    private Activity activity;
    private ArrayList<String> hiddenPackages;
    private ArrayList<String> hiddenComponents;

    //simple recycler view adapter with activity and string array as arguments
    HiddenAppsAdapter(Activity activity) {

        this.activity = activity;
        hiddenPackages = HiddenAppsUtils.hiddenPackages(activity);
        hiddenComponents = HiddenAppsUtils.componentsArray(activity);
    }

    //method to set icon and label for the app item
    private void setAppInfo(TextView labelView, ImageView iconView, int pos) {

        PackageManager packageManager = activity.getPackageManager();
        String packageName = hiddenPackages.get(pos);

        //get default package icon
        Bitmap packageBitmap = IconCache.getIconsHandler(activity).getDefaultAppDrawable(packageName);

        //get custom package icon
        Bitmap packageIcon = IconCache.customIcon.get(packageName) != null ? IconCache.customIcon.get(packageName) : packageBitmap;

        //set non null icon
        iconView.setImageBitmap(packageIcon);

        //get custom label for package
        String customLabel = IconCache.customLabel.get(packageName);

        try {

            ApplicationInfo app = packageManager.getApplicationInfo(packageName, 0);

            //get non null label
            String label = customLabel != null ? customLabel : packageManager.getApplicationLabel(app).toString();

            labelView.setText(label);

        } catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();
        }
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // inflate recycler view items layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hidden_item, parent, false);

        return new SimpleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {

        setAppInfo(holder.label, holder.icon, holder.getAdapterPosition());

    }

    @Override
    public int getItemCount() {

        //get array length
        return hiddenComponents.size();
    }

    //simple view holder implementing click listener and with activity and itemView as arguments
    class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //original app item colors
        private int originalBgColor;
        private int originalTextColor;

        //selected text color depends on the theme
        private int selectedTextColor = Utilities.isDarkThemeEnabled(activity) ? ContextCompat.getColor(activity, R.color.material_dark) : ContextCompat.getColor(activity, R.color.material_light_o);
        private TextView label;
        private ImageView icon;

        SimpleViewHolder(View itemView) {
            super(itemView);

            //get the views here
            label = itemView.findViewById(R.id.hidden_app);
            icon = itemView.findViewById(R.id.icon);

            //enable click and on long click
            itemView.setOnClickListener(this);

            //original app item colors
            this.originalBgColor = itemView.getSolidColor();
            this.originalTextColor = label.getCurrentTextColor();
        }

        //add click
        @Override
        public void onClick(View v) {

            //make item selected if it is not selected and add it to visible apps list
            if (!itemView.isSelected()) {

                int accent = Utilities.getColorAccent(activity);

                visibleApps.add(hiddenComponents.get(getAdapterPosition()));

                v.setSelected(true);

                v.setBackgroundColor(accent);

                label.setTextColor(selectedTextColor);

            } else {

                visibleApps.remove(hiddenComponents.get(getAdapterPosition()));

                v.setSelected(false);

                v.setBackgroundColor(originalBgColor);

                label.setTextColor(originalTextColor);
            }
        }
    }
}