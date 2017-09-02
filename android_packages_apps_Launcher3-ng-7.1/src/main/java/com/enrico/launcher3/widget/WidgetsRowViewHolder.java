package com.enrico.launcher3.widget;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;

import com.enrico.launcher3.BubbleTextView;
import com.enrico.launcher3.R;

class WidgetsRowViewHolder extends ViewHolder {

    final ViewGroup cellContainer;
    public final BubbleTextView title;

    WidgetsRowViewHolder(ViewGroup v) {
        super(v);

        cellContainer = v.findViewById(R.id.widgets_cell_list);
        title = v.findViewById(R.id.section);
    }
}
