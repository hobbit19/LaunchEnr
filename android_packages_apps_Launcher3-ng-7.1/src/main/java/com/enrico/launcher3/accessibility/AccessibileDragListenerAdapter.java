package com.enrico.launcher3.accessibility;

import android.view.ViewGroup;

import com.enrico.launcher3.CellLayout;
import com.enrico.launcher3.DropTarget.DragObject;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.dragndrop.DragController.DragListener;
import com.enrico.launcher3.dragndrop.DragOptions;

/**
 * Utility listener to enable/disable accessibility drag flags for a ViewGroup
 * containing CellLayouts
 */
public class AccessibileDragListenerAdapter implements DragListener {

    private final ViewGroup mViewGroup;
    private final int mDragType;

    /**
     * @param parent
     * @param dragType either {@link CellLayout#WORKSPACE_ACCESSIBILITY_DRAG} or
     *                 {@link CellLayout#FOLDER_ACCESSIBILITY_DRAG}
     */
    protected AccessibileDragListenerAdapter(ViewGroup parent, int dragType) {
        mViewGroup = parent;
        mDragType = dragType;
    }

    @Override
    public void onDragStart(DragObject dragObject, DragOptions options) {
        enableAccessibleDrag(true);
    }

    @Override
    public void onDragEnd() {
        enableAccessibleDrag(false);
        Launcher.getLauncher(mViewGroup.getContext()).getDragController().removeDragListener(this);
    }

    protected void enableAccessibleDrag(boolean enable) {
        for (int i = 0; i < mViewGroup.getChildCount(); i++) {
            setEnableForLayout((CellLayout) mViewGroup.getChildAt(i), enable);
        }
    }

    protected final void setEnableForLayout(CellLayout layout, boolean enable) {
        layout.enableAccessibleDrag(enable, mDragType);
    }
}
