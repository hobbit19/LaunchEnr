package com.enrico.launcher3.accessibility;

import com.enrico.launcher3.CellLayout;
import com.enrico.launcher3.R;
import com.enrico.launcher3.folder.FolderPagedView;

/**
 * Implementation of {@link DragAndDropAccessibilityDelegate} to support DnD in a folder.
 */
public class FolderAccessibilityHelper extends DragAndDropAccessibilityDelegate {

    /**
     * 0-index position for the first cell in {@link #mView} in {@link #mParent}.
     */
    private final int mStartPosition;

    private final FolderPagedView mParent;

    public FolderAccessibilityHelper(CellLayout layout) {
        super(layout);
        mParent = (FolderPagedView) layout.getParent();

        int index = mParent.indexOfChild(layout);
        mStartPosition = index * layout.getCountX() * layout.getCountY();
    }
    @Override
    protected int intersectsValidDropTarget(int id) {
        return Math.min(id, mParent.getAllocatedContentSize() - mStartPosition - 1);
    }

    @Override
    protected String getLocationDescriptionForIconDrop(int id) {
        return mContext.getString(R.string.move_to_position, id + mStartPosition + 1);
    }

    @Override
    protected String getConfirmationForIconDrop(int id) {
        return mContext.getString(R.string.item_moved);
    }
}
