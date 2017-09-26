package com.enrico.launcher3.materialsolidwallpapers;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.enrico.launcher3.R;

/**
 * Created by Enrico on 21/07/2017.
 */

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.SimpleViewHolder> {

    private int[] material_colors;
    private Activity activity;
    private View material;

    //simple recycler view adapter with activity and int array as arguments
    RecyclerViewAdapter(Activity activity, int[] material_colors) {
        this.material_colors = material_colors;
        this.activity = activity;
        material = activity.findViewById(R.id.material_wallpaper);
        material.setBackgroundColor(ContextCompat.getColor(activity, material_colors[0]));
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // inflate recycler view items layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.material_color, parent, false);
        return new SimpleViewHolder(activity, itemView);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {

        //set item color according to the position
        holder.material_color.setBackgroundColor(ContextCompat.getColor(activity, material_colors[position]));
    }

    @Override
    public int getItemCount() {

        //get array length
        return material_colors.length;
    }

    //simple view holder implementing click and long click listeners and with activity and itemView as arguments
    class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FrameLayout material_color;
        private Activity activity;

        SimpleViewHolder(Activity activity, View itemView) {
            super(itemView);

            //get activity
            this.activity = activity;

            //get the views here
            material_color = itemView.findViewById(R.id.material_color);

            //enable click and on long click
            itemView.setOnClickListener(this);
        }

        //add click
        @Override
        public void onClick(View v) {

            material.setBackgroundColor(ContextCompat.getColor(activity, material_colors[getAdapterPosition()]));

        }
    }
}