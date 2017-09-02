package com.enrico.launcher3.icons;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.LauncherAppState;
import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;

import java.util.ArrayList;
import java.util.List;

public class ChooseIconActivity extends AppCompatActivity {

    private static ItemInfo sItemInfo;
    final ViewGroup nullParent = null;
    private String mCurrentPackageLabel;
    private String mCurrentPackageName;
    private String mIconPackPackageName;
    private GridLayoutManager mGridLayout;
    private RecyclerView mIconsGrid;
    private IconCache mIconCache;
    private IconsHandler mIconsHandler;
    private GridAdapter mGridAdapter;

    private EditText editTextSearch;
    private List<String> allIcons, matchingIcons;
    private boolean mNoMatchingDrawables;

    public static void setItemInfo(ItemInfo info) {
        sItemInfo = info;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //apply activity's theme if dark theme is enabled
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getBaseContext(), this.getTheme());

        Utilities.applyTheme(contextThemeWrapper, this);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.material_dark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.material_dark));

        setContentView(R.layout.all_icons_view);

        //set the toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //provide back navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //go back
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        editTextSearch = findViewById(R.id.edtSearch);

        mCurrentPackageName = getIntent().getStringExtra("app_package");
        mCurrentPackageLabel = getIntent().getStringExtra("app_label");
        mIconPackPackageName = getIntent().getStringExtra("icon_pack_package");

        mIconCache = LauncherAppState.getInstance().getIconCache();
        mIconsHandler = IconCache.getIconsHandler(this);

        mIconsGrid = findViewById(R.id.icons_grid);
        mIconsGrid.setHasFixedSize(true);
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.grid_item_spacing);
        mIconsGrid.addItemDecoration(new GridItemSpacer(itemSpacing));

        mGridLayout = new GridLayoutManager(this, 4);
        mIconsGrid.setLayoutManager(mGridLayout);
        mIconsGrid.setAlpha(0.0f);
        toolbar.setAlpha(0.0f);
        editTextSearch.setAlpha(0.0f);
        final View appIcon = findViewById(R.id.app_icon);

        final Animator anim = AnimatorInflater
                .loadAnimator(this, R.animator.flip);
        anim.setTarget(appIcon);
        anim.start();

        new Thread(new Runnable() {
            @Override
            public void run() {

                allIcons =
                        mIconsHandler.getAllDrawables(mIconPackPackageName);
                matchingIcons =
                        mIconsHandler.getMatchingDrawables(mCurrentPackageName);
                ChooseIconActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mGridAdapter = new GridAdapter(allIcons, matchingIcons);
                        mIconsGrid.setAdapter(mGridAdapter);
                        anim.cancel();
                        appIcon.setVisibility(View.GONE);

                        mIconsGrid.animate().alpha(1.0f);
                        toolbar.animate().alpha(1.0f);
                        editTextSearch.animate().alpha(1.0f);

                        editTextSearch.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                filter(charSequence.toString());
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void filter(String text) {

        String query = text.toLowerCase().trim();

        //new array list that will hold the filtered data
        ArrayList<String> filterdNames = new ArrayList<>();
        ArrayList<String> filterdshit = new ArrayList<>();

        mNoMatchingDrawables = matchingIcons.isEmpty();

        if (query.isEmpty()) {

            filterdNames.clear();
            filterdNames.add(null);
            filterdNames.addAll(allIcons);

            filterdshit.clear();

            if (!mNoMatchingDrawables) {
                filterdshit.add(null);
                filterdshit.addAll(matchingIcons);
            }

            mGridAdapter.filterList(filterdNames, filterdshit);

        } else {

            filterdNames.clear();
            filterdshit.clear();

            if (mNoMatchingDrawables) {
                //looping through existing elements
                for (String s : allIcons) {
                    //if the existing elements contains the search input
                    if (s.toLowerCase().trim().contains(query)) {
                        //adding the element to filtered list
                        filterdNames.add(s);
                    }
                }
            } else {

                filterdNames.clear();
                filterdshit.clear();

                //looping through existing elements
                for (String s : allIcons) {
                    //if the existing elements contains the search input
                    if (s.toLowerCase().trim().contains(query)) {
                        //adding the element to filtered list
                        filterdNames.add(s);
                    }
                }

                for (String s : matchingIcons) {
                    //if the existing elements contains the search input
                    if (s.toLowerCase().trim().contains(query)) {
                        //adding the element to filtered list
                        filterdshit.add(s);
                    }
                }
            }
            //calling a method of the adapter class and passing the filtered list
            mGridAdapter.filterList(filterdNames, filterdshit);
        }
    }

    private class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

        private static final int TYPE_MATCHING_HEADER = 0;
        private static final int TYPE_MATCHING_ICONS = 1;
        private static final int TYPE_ALL_HEADER = 2;
        private static final int TYPE_ALL_ICONS = 3;

        List<String> mAllDrawables = new ArrayList<>();
        List<String> mMatchingDrawables = new ArrayList<>();
        private final SpanSizeLookup mSpanSizeLookup = new SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return getItemViewType(position) == TYPE_MATCHING_HEADER
                        || getItemViewType(position) == TYPE_ALL_HEADER ?
                        4 : 1;
            }
        };

        private GridAdapter(final List<String> allDrawables, final List<String> matchingDrawables) {

            mAllDrawables.add(null);
            mAllDrawables.addAll(allDrawables);
            mMatchingDrawables.add(null);
            mMatchingDrawables.addAll(matchingDrawables);

            mGridLayout.setSpanSizeLookup(mSpanSizeLookup);
            mNoMatchingDrawables = matchingDrawables.isEmpty();

            if (mNoMatchingDrawables) {
                mMatchingDrawables.clear();
            }
        }

        private void filterList(List<String> filteredAllDrawables, List<String> filteredMatchingDrawables) {

            mAllDrawables = filteredAllDrawables;
            mMatchingDrawables = filteredMatchingDrawables;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (!mNoMatchingDrawables && position < mMatchingDrawables.size() &&
                    mMatchingDrawables.get(position) == null) {

                return TYPE_MATCHING_HEADER;
            }

            if (!mNoMatchingDrawables && position > TYPE_MATCHING_HEADER &&
                    position < mMatchingDrawables.size()) {
                return TYPE_MATCHING_ICONS;
            }

            if (position == mMatchingDrawables.size()) {
                return TYPE_ALL_HEADER;
            }

            return TYPE_ALL_ICONS;
        }

        @Override
        public int getItemCount() {
            return mAllDrawables.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Activity activity = ChooseIconActivity.this;
            if (viewType == TYPE_MATCHING_HEADER) {
                TextView text = (TextView) activity.getLayoutInflater().inflate(
                        R.layout.all_icons_view_header, nullParent);
                text.setText(R.string.similar_icons);
                return new ViewHolder(text);
            }
            if (viewType == TYPE_ALL_HEADER) {
                TextView text = (TextView) activity.getLayoutInflater().inflate(
                        R.layout.all_icons_view_header, nullParent);
                text.setText(R.string.all_icons);
                return new ViewHolder(text);
            }

            return new ViewHolder(activity.getLayoutInflater().inflate(R.layout.icon_item, nullParent));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if (holder.getItemViewType() != TYPE_MATCHING_HEADER
                    && holder.getItemViewType() != TYPE_ALL_HEADER) {
                boolean drawablesMatching = holder.getItemViewType() == TYPE_MATCHING_ICONS;
                final List<String> drawables = drawablesMatching ?
                        mMatchingDrawables : mAllDrawables;

                if (position >= drawables.size()) {
                    return;
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Drawable icon = IconsHandler.loadDrawable(
                                mIconPackPackageName, drawables.get(holder.getAdapterPosition()), true);
                        if (icon != null) {
                            mIconCache.addCustomInfoToDataBase(icon, sItemInfo, mCurrentPackageLabel);
                        }
                        ChooseIconActivity.this.finish();
                    }
                });
                Drawable icon = null;
                String drawable = drawables.get(position);
                try {
                    icon = IconsHandler.loadDrawable(mIconPackPackageName, drawable, true);
                } catch (OutOfMemoryError e) {
                    // time for a new device?
                    e.printStackTrace();
                }
                if (icon != null) {

                    holder.icon.setImageDrawable(icon);
                    holder.label.setText(drawable);
                }
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView label;
            private ImageView icon;

            private ViewHolder(View v) {
                super(v);

                icon = itemView.findViewById(R.id.icon);
                label = itemView.findViewById(R.id.name);
            }
        }
    }

    private class GridItemSpacer extends RecyclerView.ItemDecoration {
        private int spacing;

        private GridItemSpacer(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.top = spacing;
        }
    }
}
