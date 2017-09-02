package com.enrico.launcher3.frequentcontacts;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.enrico.launcher3.R;

import java.util.ArrayList;

/**
 * Created by Enrico on 21/07/2017.
 */

class ContactsRecyclerViewAdapter extends RecyclerView.Adapter<ContactsRecyclerViewAdapter.SimpleViewHolder> {

    private ArrayList<Contact> contacts;
    private Activity activity;

    //simple recycler view adapter with activity and array list contact as arguments
    ContactsRecyclerViewAdapter(Activity activity, ArrayList<Contact> contacts) {
        this.contacts = contacts;
        this.activity = activity;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // inflate recycler view items layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, parent, false);

        return new SimpleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {

        holder.name.setText(contacts.get(position).getContactName());

        if (contacts.get(holder.getAdapterPosition()).getContactThumbnail() != null) {
            holder.thumbnail.setImageDrawable(RoundedContact.get(activity, Uri.parse(contacts.get(holder.getAdapterPosition()).getContactThumbnail())));
        } else {
            holder.thumbnail.setImageResource(R.drawable.ic_account_circle_grey600);
        }

    }

    @Override
    public int getItemCount() {

        //get array length
        return contacts.size();
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

            AsyncLoadContactPhones.execute(activity, contacts.get(getAdapterPosition()).getContactId());
        }
    }
}
