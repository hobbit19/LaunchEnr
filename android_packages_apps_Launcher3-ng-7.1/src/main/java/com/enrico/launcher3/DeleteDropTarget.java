package com.enrico.launcher3;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.enrico.launcher3.dragndrop.DragLayer;
import com.enrico.launcher3.dragndrop.DragOptions;
import com.enrico.launcher3.folder.Folder;
import com.enrico.launcher3.util.FlingAnimation;
import com.enrico.launcher3.util.Thunk;

public class DeleteDropTarget extends ButtonDropTarget {

    public DeleteDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeleteDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @return true for items that should have a "Remove" action in accessibility.
     */
    public static boolean supportsAccessibleDrop(ItemInfo info) {
        return (info instanceof ShortcutInfo)
                || (info instanceof LauncherAppWidgetInfo)
                || (info instanceof FolderInfo);
    }

    /**
     * Removes the item from the workspace. If the view is not null, it also removes the view.
     */
    public static void removeWorkspaceOrFolderItem(Launcher launcher, ItemInfo item, View view) {
        // Remove the item from launcher and the db, we can ignore the containerInfo in this call
        // because we already remove the drag view from the folder (if the drag originated from
        // a folder) in Folder.beginDrag()
        launcher.removeItem(view, item, true /* deleteFromDb */);
        launcher.getWorkspace().stripEmptyScreens();
        launcher.getDragLayer().announceForAccessibility(launcher.getString(R.string.item_removed));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setDrawable(R.drawable.ic_close_white);
    }

    @Override
    public void onDragStart(DropTarget.DragObject dragObject, DragOptions options) {
        super.onDragStart(dragObject, options);
    }

    @Override
    protected boolean supportsDrop(DragSource source, ItemInfo info) {
        return true;
    }

    @Override
    @Thunk
    public void completeDrop(DragObject d) {
        ItemInfo item = d.dragInfo;
        if ((d.dragSource instanceof Workspace) || (d.dragSource instanceof Folder)) {
            removeWorkspaceOrFolderItem(mLauncher, item, null);
        }
    }

    @Override
    public void onFlingToDelete(final DragObject d, PointF vel) {
        // Don't highlight the icon as it's animating
        d.dragView.setColor(0);

        final DragLayer dragLayer = mLauncher.getDragLayer();
        FlingAnimation fling = new FlingAnimation(d, vel,
                getIconRect(d.dragView.getMeasuredWidth(), d.dragView.getMeasuredHeight(),
                        mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight()),
                dragLayer);

        final int duration = fling.getDuration();
        final long startTime = AnimationUtils.currentAnimationTimeMillis();

        // NOTE: Because it takes time for the first frame of animation to actually be
        // called and we expect the animation to be a continuation of the fling, we have
        // to account for the time that has elapsed since the fling finished.  And since
        // we don't have a startDelay, we will always get call to update when we call
        // start() (which we want to ignore).
        final TimeInterpolator tInterpolator = new TimeInterpolator() {
            private int mCount = -1;
            private float mOffset = 0f;

            @Override
            public float getInterpolation(float t) {
                if (mCount < 0) {
                    mCount++;
                } else if (mCount == 0) {
                    mOffset = Math.min(0.5f, (float) (AnimationUtils.currentAnimationTimeMillis() -
                            startTime) / duration);
                    mCount++;
                }
                return Math.min(1f, mOffset + t);
            }
        };

        Runnable onAnimationEndRunnable = new Runnable() {
            @Override
            public void run() {
                mLauncher.exitSpringLoadedDragMode();
                completeDrop(d);
                mLauncher.getDragController().onDeferredEndFling(d);
            }
        };

        dragLayer.animateView(d.dragView, fling, duration, tInterpolator, onAnimationEndRunnable,
                DragLayer.ANIMATION_END_DISAPPEAR, null);
    }
}
