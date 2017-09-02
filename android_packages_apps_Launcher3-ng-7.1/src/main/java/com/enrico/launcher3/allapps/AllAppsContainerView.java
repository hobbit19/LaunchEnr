package com.enrico.launcher3.allapps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.enrico.launcher3.AppInfo;
import com.enrico.launcher3.BaseContainerView;
import com.enrico.launcher3.BubbleTextView;
import com.enrico.launcher3.CellLayout;
import com.enrico.launcher3.DeleteDropTarget;
import com.enrico.launcher3.DeviceProfile;
import com.enrico.launcher3.DragSource;
import com.enrico.launcher3.DropTarget;
import com.enrico.launcher3.ExtendedEditText;
import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.LauncherTransitionable;
import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.Workspace;
import com.enrico.launcher3.dragndrop.DragOptions;
import com.enrico.launcher3.folder.Folder;
import com.enrico.launcher3.graphics.TintedDrawableSpan;
import com.enrico.launcher3.keyboard.FocusedItemDecorator;
import com.enrico.launcher3.shortcuts.DeepShortcutsContainer;
import com.enrico.launcher3.util.ComponentKey;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

final class FullMergeAlgorithm implements AlphabeticalAppsList.MergeAlgorithm {

    @Override
    public boolean continueMerging(AlphabeticalAppsList.SectionInfo section,
                                   AlphabeticalAppsList.SectionInfo withSection,
                                   int sectionAppCount, int numAppsPerRow, int mergeCount) {
        // Don't merge the predicted apps
        if (section.firstAppItem.viewType != AllAppsGridAdapter.VIEW_TYPE_ICON) {
            return false;
        }
        // Otherwise, merge every other section
        return true;
    }
}

final class SimpleSectionMergeAlgorithm implements AlphabeticalAppsList.MergeAlgorithm {

    private int mMinAppsPerRow;
    private int mMinRowsInMergedSection;
    private int mMaxAllowableMerges;
    private CharsetEncoder mAsciiEncoder;

    SimpleSectionMergeAlgorithm(int minAppsPerRow, int minRowsInMergedSection, int maxNumMerges) {
        mMinAppsPerRow = minAppsPerRow;
        mMinRowsInMergedSection = minRowsInMergedSection;
        mMaxAllowableMerges = maxNumMerges;
        mAsciiEncoder = Charset.forName("US-ASCII").newEncoder();
    }

    @Override
    public boolean continueMerging(AlphabeticalAppsList.SectionInfo section,
                                   AlphabeticalAppsList.SectionInfo withSection,
                                   int sectionAppCount, int numAppsPerRow, int mergeCount) {
        // Don't merge the predicted apps
        if (section.firstAppItem.viewType != AllAppsGridAdapter.VIEW_TYPE_ICON) {
            return false;
        }

        // Continue merging if the number of hanging apps on the final row is less than some
        // fixed number (ragged), the merged rows has yet to exceed some minimum row count,
        // and while the number of merged sections is less than some fixed number of merges
        int rows = sectionAppCount / numAppsPerRow;
        int cols = sectionAppCount % numAppsPerRow;

        // Ensure that we do not merge across scripts, currently we only allow for english and
        // native scripts so we can test if both can just be ascii encoded
        boolean isCrossScript = false;
        if (withSection.firstAppItem != null) {
            isCrossScript = mAsciiEncoder.canEncode(section.firstAppItem.sectionName) !=
                    mAsciiEncoder.canEncode(withSection.firstAppItem.sectionName);
        }
        return (0 < cols && cols < mMinAppsPerRow) &&
                rows < mMinRowsInMergedSection &&
                mergeCount < mMaxAllowableMerges &&
                !isCrossScript;
    }
}

public class AllAppsContainerView extends BaseContainerView implements DragSource,
        LauncherTransitionable, View.OnLongClickListener, AllAppsSearchBarController.Callbacks {

    private static final int MIN_ROWS_IN_MERGED_SECTION_PHONE = 3;
    private static final int MAX_NUM_MERGES_PHONE = 2;

    private final Launcher mLauncher;
    private final AlphabeticalAppsList mApps;
    private final AllAppsGridAdapter mAdapter;
    private final RecyclerView.LayoutManager mLayoutManager;
    private final RecyclerView.ItemDecoration mItemDecoration;

    // The computed bounds of the container
    private final Rect mContentBounds = new Rect();
    // This coordinate is relative to this container view
    private final Point mBoundsCheckLastTouchDownPos = new Point(-1, -1);
    private AllAppsRecyclerView mAppsRecyclerView;
    private AllAppsSearchBarController mSearchBarController;
    private View mSearchContainer;
    private ExtendedEditText mSearchInput;
    private HeaderElevationController mElevationController;
    private int mSearchContainerOffsetTop;
    private SpannableStringBuilder mSearchQueryBuilder = null;
    private int mSectionNamesMargin;
    private int mNumAppsPerRow;
    private int mNumPredictedAppsPerRow;
    private int mRecyclerViewBottomPadding;

    public AllAppsContainerView(Context context) {
        this(context, null);
    }

    public AllAppsContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources res = context.getResources();

        mLauncher = Launcher.getLauncher(context);
        mSectionNamesMargin = res.getDimensionPixelSize(R.dimen.all_apps_grid_view_start_margin);
        mApps = new AlphabeticalAppsList(context);
        mAdapter = new AllAppsGridAdapter(mLauncher, mApps, mLauncher, this);
        mApps.setAdapter(mAdapter);
        mLayoutManager = mAdapter.getLayoutManager();
        mItemDecoration = mAdapter.getItemDecoration();
        DeviceProfile grid = mLauncher.getDeviceProfile();
        if (!grid.isVerticalBarLayout()) {
            mRecyclerViewBottomPadding = 0;
            setPadding(0, 0, 0, 0);
        } else {
            mRecyclerViewBottomPadding =
                    res.getDimensionPixelSize(R.dimen.all_apps_list_bottom_padding);
        }
        mSearchQueryBuilder = new SpannableStringBuilder();
        Selection.setSelection(mSearchQueryBuilder, 0);
    }

    public void setPredictedApps(List<ComponentKey> apps) {
        mApps.setPredictedApps(apps);
    }

    public void setApps(List<AppInfo> apps) {
        mApps.setApps(apps);
    }

    public void addApps(List<AppInfo> apps) {
        mApps.addApps(apps);
        mSearchBarController.refreshSearchResult();
    }

    public void updateApps(List<AppInfo> apps) {
        mApps.updateApps(apps);
        mSearchBarController.refreshSearchResult();
    }

    public void removeApps(List<AppInfo> apps) {
        mApps.removeApps(apps);
        mSearchBarController.refreshSearchResult();
    }

    public void setSearchBarController(AllAppsSearchBarController searchController) {
        if (mSearchBarController != null) {
            throw new RuntimeException("Expected search bar controller to only be set once");
        }
        mSearchBarController = searchController;
        mSearchBarController.initialize(mApps, mSearchInput, mLauncher, this);
        mAdapter.setSearchController(mSearchBarController);
    }

    public void scrollToTop() {
        mAppsRecyclerView.scrollToTop();
    }

    public boolean shouldContainerScroll(MotionEvent ev) {
        int[] point = new int[2];
        point[0] = (int) ev.getX();
        point[1] = (int) ev.getY();
        Utilities.mapCoordInSelfToDescendent(mAppsRecyclerView, this, point);

        // IF the MotionEvent is inside the search box, and the container keeps on receiving
        // touch input, container should move down.
        if (mLauncher.getDragLayer().isEventOverView(mSearchContainer, ev)) {
            return true;
        }

        // IF the MotionEvent is inside the thumb, container should not be pulled down.
        if (mAppsRecyclerView.getScrollBar().isNearThumb(point[0], point[1])) {
            return false;
        }

        // IF a shortcuts container is open, container should not be pulled down.
        if (mLauncher.getOpenShortcutsContainer() != null) {
            return false;
        }

        // IF scroller is at the very top OR there is no scroll bar because there is probably not
        // enough items to scroll, THEN it's okay for the container to be pulled down.
        if (mAppsRecyclerView.getScrollBar().getThumbOffset().y <= 0) {
            return mAppsRecyclerView.getScrollBar().getThumbOffset().y <= 0;
        }
        return false;
    }

    public void startAppsSearch() {
        if (mSearchBarController != null) {
            mSearchBarController.focusSearchField();
        }
    }

    public void reset() {
        // Reset the search bar and base recycler view after transitioning home
        scrollToTop();
        mSearchBarController.reset();
        mAppsRecyclerView.reset();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // This is a focus listener that proxies focus from a view into the list view.  This is to
        // work around the search box from getting first focus and showing the cursor.
        getContentView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mAppsRecyclerView.requestFocus();
                }
            }
        });

        mSearchContainer = findViewById(R.id.search_container);
        mSearchInput = findViewById(R.id.search_box_input);

        // Update the hint to contain the icon.
        // Prefix the original hint with two spaces. The first space gets replaced by the icon
        // using span. The second space is used for a singe space character between the hint
        // and the icon.
        SpannableString spanned = new SpannableString("  " + mSearchInput.getHint());
        spanned.setSpan(new TintedDrawableSpan(getContext(), R.drawable.ic_allapps_search),
                0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        mSearchInput.setHint(spanned);

        mSearchContainerOffsetTop = getResources().getDimensionPixelSize(
                R.dimen.all_apps_search_bar_margin_top);

        mElevationController = new HeaderElevationController.ControllerVL(mSearchContainer);

        // Load the all apps recycler view
        mAppsRecyclerView = findViewById(R.id.apps_list_view);
        mAppsRecyclerView.setApps(mApps);
        mAppsRecyclerView.setLayoutManager(mLayoutManager);
        mAppsRecyclerView.setAdapter(mAdapter);
        mAppsRecyclerView.setHasFixedSize(true);
        mAppsRecyclerView.addOnScrollListener(mElevationController);
        mAppsRecyclerView.setElevationController(mElevationController);

        if (mItemDecoration != null) {
            mAppsRecyclerView.addItemDecoration(mItemDecoration);
        }

        FocusedItemDecorator focusedItemDecorator = new FocusedItemDecorator(mAppsRecyclerView);
        mAppsRecyclerView.addItemDecoration(focusedItemDecorator);
        mAppsRecyclerView.preMeasureViews(mAdapter);
        mAdapter.setIconFocusListener(focusedItemDecorator.getFocusListener());

        getRevealView().setVisibility(View.VISIBLE);
        getContentView().setVisibility(View.VISIBLE);
        getContentView().setBackground(null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthPx = MeasureSpec.getSize(widthMeasureSpec);
        int heightPx = MeasureSpec.getSize(heightMeasureSpec);
        updatePaddingsAndMargins(widthPx, heightPx);
        mContentBounds.set(mContainerPaddingLeft, 0, widthPx - mContainerPaddingRight, heightPx);

        DeviceProfile grid = mLauncher.getDeviceProfile();
        grid.updateAppsViewNumCols();
            if (mNumAppsPerRow != grid.inv.numColumns ||
                    mNumPredictedAppsPerRow != grid.inv.numColumns) {
                mNumAppsPerRow = grid.inv.numColumns;
                mNumPredictedAppsPerRow = grid.inv.numColumns;

                mAppsRecyclerView.setNumAppsPerRow(grid, mNumAppsPerRow);
                mAdapter.setNumAppsPerRow(mNumAppsPerRow);
                mApps.setNumAppsPerRow(mNumAppsPerRow, mNumPredictedAppsPerRow, new FullMergeAlgorithm());
                if (mNumAppsPerRow > 0) {
                    int rvPadding = mAppsRecyclerView.getPaddingStart(); // Assumes symmetry
                    final int thumbMaxWidth =
                            getResources().getDimensionPixelSize(
                                    R.dimen.container_fastscroll_thumb_max_width);
                    mSearchContainer.setPadding(
                            rvPadding - mContainerPaddingLeft + thumbMaxWidth,
                            mSearchContainer.getPaddingTop(),
                            rvPadding - mContainerPaddingRight + thumbMaxWidth,
                            mSearchContainer.getPaddingBottom());
                }
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Update the number of items in the grid before we measure the view
        // TODO: mSectionNamesMargin is currently 0, but also account for it,
        // if it's enabled in the future.
        grid.updateAppsViewNumCols();
        if (mNumAppsPerRow != grid.allAppsNumCols ||
                mNumPredictedAppsPerRow != grid.allAppsNumPredictiveCols) {
            mNumAppsPerRow = grid.allAppsNumCols;
            mNumPredictedAppsPerRow = grid.allAppsNumPredictiveCols;

            // If there is a start margin to draw section names, determine how we are going to merge
            // app sections
            boolean mergeSectionsFully = mSectionNamesMargin == 0 || !grid.isPhone;
            AlphabeticalAppsList.MergeAlgorithm mergeAlgorithm = mergeSectionsFully ?
                    new FullMergeAlgorithm() :
                    new SimpleSectionMergeAlgorithm((int) Math.ceil(mNumAppsPerRow / 2f),
                            MIN_ROWS_IN_MERGED_SECTION_PHONE, MAX_NUM_MERGES_PHONE);

            mAppsRecyclerView.setNumAppsPerRow(grid, mNumAppsPerRow);
            mAdapter.setNumAppsPerRow(mNumAppsPerRow);
            mApps.setNumAppsPerRow(mNumAppsPerRow, mNumPredictedAppsPerRow, mergeAlgorithm);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void updatePaddingsAndMargins(int widthPx, int heightPx) {
        Rect bgPadding = new Rect();
        getRevealView().getBackground().getPadding(bgPadding);

        mAppsRecyclerView.updateBackgroundPadding(bgPadding);
        mAdapter.updateBackgroundPadding(bgPadding);
        mElevationController.updateBackgroundPadding(bgPadding);

        // Pad the recycler view by the background padding plus the start margin (for the section
        // names)
        int maxScrollBarWidth = mAppsRecyclerView.getMaxScrollbarWidth();
        int startInset = Math.max(mSectionNamesMargin, maxScrollBarWidth);
        if (Utilities.isRtl(getResources())) {
            mAppsRecyclerView.setPadding(bgPadding.left + maxScrollBarWidth, 0, bgPadding.right
                    + startInset, mRecyclerViewBottomPadding);
        } else {
            mAppsRecyclerView.setPadding(bgPadding.left + startInset, 0, bgPadding.right +
                    maxScrollBarWidth, mRecyclerViewBottomPadding);
        }

        MarginLayoutParams lp = (MarginLayoutParams) mSearchContainer.getLayoutParams();
        lp.leftMargin = bgPadding.left;
        lp.rightMargin = bgPadding.right;

        // Clip the view to the left and right edge of the background to
        // to prevent shadows from rendering beyond the edges
        final Rect newClipBounds = new Rect(
                bgPadding.left, 0, widthPx - bgPadding.right, heightPx);
        setClipBounds(newClipBounds);

        // Allow the overscroll effect to reach the edges of the view
        mAppsRecyclerView.setClipToPadding(false);

        DeviceProfile grid = mLauncher.getDeviceProfile();
            if (!grid.isVerticalBarLayout()) {
                MarginLayoutParams mlp = (MarginLayoutParams) mAppsRecyclerView.getLayoutParams();

                Rect insets = mLauncher.getDragLayer().getInsets();
                getContentView().setPadding(0, 0, 0, 0);
                int height = insets.top + grid.hotseatCellHeightPx;

                mlp.topMargin = height;
                mAppsRecyclerView.setLayoutParams(mlp);

                mSearchContainer.setPadding(
                        mSearchContainer.getPaddingLeft(),
                        insets.top + mSearchContainerOffsetTop,
                        mSearchContainer.getPaddingRight(),
                        mSearchContainer.getPaddingBottom());
                lp.height = height;
            }

        mSearchContainer.setLayoutParams(lp);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Determine if the key event was actual text, if so, focus the search bar and then dispatch
        // the key normally so that it can process this key event
        if (!mSearchBarController.isSearchFieldFocused() &&
                event.getAction() == KeyEvent.ACTION_DOWN) {
            final int unicodeChar = event.getUnicodeChar();
            final boolean isKeyNotWhitespace = unicodeChar > 0 &&
                    !Character.isWhitespace(unicodeChar) && !Character.isSpaceChar(unicodeChar);
            if (isKeyNotWhitespace) {
                boolean gotKey = TextKeyListener.getInstance().onKeyDown(this, mSearchQueryBuilder,
                        event.getKeyCode(), event);
                if (gotKey && mSearchQueryBuilder.length() > 0) {
                    mSearchBarController.focusSearchField();
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return handleTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return handleTouchEvent(ev);
    }

    @Override
    public boolean onLongClick(View v) {
        // Return early if this is not initiated from a touch
        if (!v.isInTouchMode()) return false;
        // When we have exited all apps or are in transition, disregard long clicks

        if (!mLauncher.isAppsViewVisible() ||
                mLauncher.getWorkspace().isSwitchingState()) return false;
        // Return if global dragging is not enabled or we are already dragging
        if (!mLauncher.isDraggingEnabled()) return false;
        if (mLauncher.getDragController().isDragging()) return false;

        // Start the drag
        DragOptions dragOptions = new DragOptions();
        if (v instanceof BubbleTextView) {
            final BubbleTextView icon = (BubbleTextView) v;
            if (icon.hasDeepShortcuts()) {
                DeepShortcutsContainer dsc = DeepShortcutsContainer.showForIcon(icon);
                if (dsc != null) {
                    dragOptions.deferDragCondition = dsc.createDeferDragCondition(new Runnable() {
                        @Override
                        public void run() {
                            icon.setVisibility(VISIBLE);
                        }
                    });
                }
            }
        }
        mLauncher.getWorkspace().beginDragShared(v, this, dragOptions);

        return false;
    }

    @Override
    public boolean supportsFlingToDelete() {
        return true;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return true;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        DeviceProfile grid = mLauncher.getDeviceProfile();
        return (float) grid.allAppsIconSizePx / grid.iconSizePx;
    }

    @Override
    public void onFlingToDeleteCompleted() {
        // We just dismiss the drag when we fling, so cleanup here
        mLauncher.exitSpringLoadedDragModeDelayed(true,
                Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
    }

    @Override
    public void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete,
                                boolean success) {
        if (isFlingToDelete || !success || (target != mLauncher.getWorkspace() &&
                !(target instanceof DeleteDropTarget) && !(target instanceof Folder))) {
            // Exit spring loaded mode if we have not successfully dropped or have not handled the
            // drop in Workspace
            mLauncher.exitSpringLoadedDragModeDelayed(true,
                    Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
        }

        // Display an error message if the drag failed due to there not being enough space on the
        // target layout we were dropping on.
        if (!success) {
            boolean showOutOfSpaceMessage = false;
            if (target instanceof Workspace && !mLauncher.getDragController().isDeferringDrag()) {
                int currentScreen = mLauncher.getCurrentWorkspaceScreen();
                Workspace workspace = (Workspace) target;
                CellLayout layout = (CellLayout) workspace.getChildAt(currentScreen);
                ItemInfo itemInfo = d.dragInfo;
                if (layout != null) {
                    showOutOfSpaceMessage =
                            !layout.findCellForSpan(null, itemInfo.spanX, itemInfo.spanY);
                }
            }
            if (showOutOfSpaceMessage) {
                mLauncher.showOutOfSpaceMessage(false);
            }

            d.deferDragViewCleanupPostAnimation = false;
        }
    }

    @Override
    public void onLauncherTransitionPrepare(Launcher l, boolean animated,
                                            boolean multiplePagesVisible) {
        // Do nothing
    }

    @Override
    public void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace) {
        // Do nothing
    }

    @Override
    public void onLauncherTransitionStep(Launcher l, float t) {
        // Do nothing
    }

    @Override
    public void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace) {
        if (toWorkspace) {
            reset();
        }
    }

    private boolean handleTouchEvent(MotionEvent ev) {
        DeviceProfile grid = mLauncher.getDeviceProfile();
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mContentBounds.isEmpty()) {
                    // Outset the fixed bounds and check if the touch is outside all apps
                    Rect tmpRect = new Rect(mContentBounds);
                    tmpRect.inset(-grid.allAppsIconSizePx / 2, 0);
                    if (ev.getX() < tmpRect.left || ev.getX() > tmpRect.right) {
                        mBoundsCheckLastTouchDownPos.set(x, y);
                        return true;
                    }
                } else {
                    // Check if the touch is outside all apps
                    if (ev.getX() < getPaddingLeft() ||
                            ev.getX() > (getWidth() - getPaddingRight())) {
                        mBoundsCheckLastTouchDownPos.set(x, y);
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mBoundsCheckLastTouchDownPos.x > -1) {
                    ViewConfiguration viewConfig = ViewConfiguration.get(getContext());
                    float dx = ev.getX() - mBoundsCheckLastTouchDownPos.x;
                    float dy = ev.getY() - mBoundsCheckLastTouchDownPos.y;
                    float distance = (float) Math.hypot(dx, dy);
                    if (distance < viewConfig.getScaledTouchSlop()) {
                        // The background was clicked, so just go home
                        Launcher launcher = Launcher.getLauncher(getContext());
                        launcher.showWorkspace(true);
                        return true;
                    }
                }
                // Fall through
            case MotionEvent.ACTION_CANCEL:
                mBoundsCheckLastTouchDownPos.set(-1, -1);
                break;
        }
        return false;
    }

    @Override
    public void onSearchResult(String query, ArrayList<ComponentKey> apps) {
        if (apps != null) {
            if (mApps.setOrderedFilter(apps)) {
                mAppsRecyclerView.onSearchResultsChanged();
            }
            mAdapter.setLastSearchQuery(query);
        }
    }

    @Override
    public void clearSearchResult() {
        if (mApps.setOrderedFilter(null)) {
            mAppsRecyclerView.onSearchResultsChanged();
        }

        // Clear the search query
        mSearchQueryBuilder.clear();
        mSearchQueryBuilder.clearSpans();
        Selection.setSelection(mSearchQueryBuilder, 0);
    }

    public boolean shouldRestoreImeState() {
        return !TextUtils.isEmpty(mSearchInput.getText());
    }
}
