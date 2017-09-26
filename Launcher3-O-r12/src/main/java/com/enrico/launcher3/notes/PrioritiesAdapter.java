package com.enrico.launcher3.notes;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.enrico.launcher3.R;
import com.enrico.launcher3.theme.ThemePreference;

/**
 * Created by Enrico on 21/07/2017.
 */

class PrioritiesAdapter extends RecyclerView.Adapter<PrioritiesAdapter.SimpleViewHolder> {

    private static int selectedPriority;
    private Integer[] priorities;
    private Activity activity;
    private TextView priority;

    //simple recycler view adapter with activity and integer array as arguments
    PrioritiesAdapter(Activity activity, TextView priority, Integer[] priorities) {
        this.priorities = priorities;
        this.activity = activity;
        this.priority = priority;
    }

    static String priority() {

        return String.valueOf(selectedPriority);
    }

    private static void setDefaultPriority(int color) {

        selectedPriority = color;

    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // inflate recycler view items layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.priority_item, parent, false);

        return new SimpleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {

        ThemePreference.createCircularPreferenceBitmap(true, null, holder.priorityItem, activity, ContextCompat.getColor(activity, priorities[holder.getAdapterPosition()]));

        //set default priority to neutral
        setDefaultPriority(priorities[0]);
        priority.setTextColor(ContextCompat.getColor(activity, selectedPriority));

    }

    @Override
    public int getItemCount() {

        //get array length
        return priorities.length;
    }

    //simple view holder implementing click listener and with activity and itemView as arguments
    class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView priorityItem;

        SimpleViewHolder(View itemView) {
            super(itemView);

            //get the views here
            priorityItem = itemView.findViewById(R.id.color);

            //enable click and on long click
            itemView.setOnClickListener(this);
        }

        //add click
        @Override
        public void onClick(View v) {

            selectedPriority = priorities[getAdapterPosition()];

            priority.setTextColor(ContextCompat.getColor(activity, selectedPriority));

        }
    }
}