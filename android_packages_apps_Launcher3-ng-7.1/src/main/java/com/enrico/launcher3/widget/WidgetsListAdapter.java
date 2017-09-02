package com.enrico.launcher3.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.enrico.launcher3.LauncherAppState;
import com.enrico.launcher3.R;
import com.enrico.launcher3.WidgetPreviewLoader;
import com.enrico.launcher3.model.WidgetItem;
import com.enrico.launcher3.model.WidgetsModel;

import java.util.List;

/**
 * Recycler view adapter for the widget tray.
 *
 * <p>Memory vs. Performance:
 * The less number of types of views are inserted into a {@link RecyclerView}, the more recycling
 * happens and less memory is consumed. {@link #getItemViewType} was not overridden as there is
 * only a single type of view.
 */
class WidgetsListAdapter extends Adapter<WidgetsRowViewHolder> {

    private final WidgetPreviewLoader mWidgetPreviewLoader;
    private final LayoutInflater mLayoutInflater;

    private final View.OnClickListener mIconClickListener;
    private final View.OnLongClickListener mIconLongClickListener;

    private WidgetsModel mWidgetsModel;

    private final int mIndent;

    WidgetsListAdapter(View.OnClickListener iconClickListener,
            View.OnLongClickListener iconLongClickListener,
            Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        mWidgetPreviewLoader = LauncherAppState.getInstance().getWidgetCache();

        mIconClickListener = iconClickListener;
        mIconLongClickListener = iconLongClickListener;
        mIndent = context.getResources().getDimensionPixelSize(R.dimen.widget_section_indent);
    }

    void setWidgetsModel(WidgetsModel w) {
        mWidgetsModel = w;
    }

    @Override
    public int getItemCount() {
        if (mWidgetsModel == null) {
            return 0;
        }
        return mWidgetsModel.getPackageSize();
    }

    @Override
    public void onBindViewHolder(WidgetsRowViewHolder holder, int pos) {
        List<WidgetItem> infoList = mWidgetsModel.getSortedWidgets(pos);

        ViewGroup row = holder.cellContainer;

        // Add more views.
        // if there are too many, hide them.
        int diff = infoList.size() - row.getChildCount();

        if (diff > 0) {
            for (int i = 0; i < diff; i++) {
                WidgetCell widget = (WidgetCell) mLayoutInflater.inflate(
                        R.layout.widget_cell, row, false);

                // set up touch.
                widget.setOnClickListener(mIconClickListener);
                widget.setOnLongClickListener(mIconLongClickListener);
                LayoutParams lp = widget.getLayoutParams();
                lp.height = widget.cellSize;
                lp.width = widget.cellSize;
                widget.setLayoutParams(lp);

                row.addView(widget);
            }
        } else if (diff < 0) {
            for (int i=infoList.size() ; i < row.getChildCount(); i++) {
                row.getChildAt(i).setVisibility(View.GONE);
            }
        }

        // Bind the views in the application info section.
        holder.title.applyFromPackageItemInfo(mWidgetsModel.getPackageItemInfo(pos));

        // Bind the view in the widget horizontal tray region.
        for (int i=0; i < infoList.size(); i++) {
            WidgetCell widget = (WidgetCell) row.getChildAt(i);
            widget.applyFromCellItem(infoList.get(i), mWidgetPreviewLoader);
            widget.ensurePreview();
            widget.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public WidgetsRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ViewGroup container = (ViewGroup) mLayoutInflater.inflate(
                R.layout.widgets_list_row_view, parent, false);
        LinearLayout cellList = container.findViewById(R.id.widgets_cell_list);

        // if the end padding is 0, then container view (horizontal scroll view) doesn't respect
        // the end of the linear layout width + the start padding and doesn't allow scrolling.

        cellList.setPaddingRelative(mIndent, 0, 1, 0);


        return new WidgetsRowViewHolder(container);
    }

    @Override
    public void onViewRecycled(WidgetsRowViewHolder holder) {
        int total = holder.cellContainer.getChildCount();
        for (int i = 0; i < total; i++) {
            WidgetCell widget = (WidgetCell) holder.cellContainer.getChildAt(i);
            widget.clear();
        }
    }

    public boolean onFailedToRecycleView(WidgetsRowViewHolder holder) {
        // If child views are animating, then the RecyclerView may choose not to recycle the view,
        // causing extraneous onCreateViewHolder() calls.  It is safe in this case to continue
        // recycling this view, and take care in onViewRecycled() to cancel any existing
        // animations.
        return true;
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }
}
