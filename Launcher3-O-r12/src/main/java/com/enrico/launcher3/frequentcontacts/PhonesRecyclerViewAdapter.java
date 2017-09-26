package com.enrico.launcher3.frequentcontacts;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enrico.launcher3.R;

import java.util.ArrayList;

/**
 * Created by Enrico on 21/07/2017.
 */

class PhonesRecyclerViewAdapter extends RecyclerView.Adapter<PhonesRecyclerViewAdapter.SimpleViewHolder> {

    private ArrayList<String> phones;
    private Activity activity;

    //simple recycler view adapter with activity and array list contact as arguments
    PhonesRecyclerViewAdapter(Activity activity, ArrayList<String> phones) {
        this.phones = phones;
        this.activity = activity;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // inflate recycler view items layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_phone_item, parent, false);

        return new SimpleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {

        holder.phone.setText(phones.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {

        //get array length
        return phones.size();
    }

    //simple view holder implementing click and long click listeners and with activity and itemView as arguments
    class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView phone;

        SimpleViewHolder(View itemView) {
            super(itemView);

            phone = itemView.findViewById(R.id.phone);

            //enable click and on long click
            itemView.setOnClickListener(this);
        }

        //add click
        @Override
        public void onClick(View v) {

            CallUtil.performCall(activity, phones.get(getAdapterPosition()));
        }
    }
}