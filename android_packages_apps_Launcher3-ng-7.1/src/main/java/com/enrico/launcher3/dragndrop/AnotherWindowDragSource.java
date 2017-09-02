package com.enrico.launcher3.dragndrop;

import android.content.Context;
import android.view.View;

import com.enrico.launcher3.DragSource;
import com.enrico.launcher3.DropTarget.DragObject;
import com.enrico.launcher3.Launcher;

/**
 * DragSource used when the drag started at another window.
 */
class AnotherWindowDragSource implements DragSource {

    private final Context mContext;

    AnotherWindowDragSource(Context context) {
        mContext = context;
    }

    @Override
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 1;
    }

    @Override
    public void onFlingToDeleteCompleted() {
    }

    @Override
    public void onDropCompleted(View target, DragObject d,
                                boolean isFlingToDelete, boolean success) {
        if (!success) {
            Launcher.getLauncher(mContext).exitSpringLoadedDragModeDelayed(false, 0, null);
        }

    }
}
