package com.enrico.launcher3.icons;

import android.content.ComponentName;
import android.content.Context;
import android.util.AttributeSet;

import com.enrico.launcher3.AppInfo;
import com.enrico.launcher3.ButtonDropTarget;
import com.enrico.launcher3.DragSource;
import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.R;
import com.enrico.launcher3.ShortcutInfo;

public class EditIconDropTarget extends ButtonDropTarget {

    private Launcher mLauncher;

    public EditIconDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditIconDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLauncher = Launcher.getLauncher(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setDrawable(R.drawable.ic_format_paint_white);
    }

    @Override
    protected boolean supportsDrop(DragSource source, ItemInfo info) {
        return info instanceof AppInfo || info instanceof ShortcutInfo;
    }

    @Override
    public void completeDrop(DragObject d) {

        ComponentName componentName = null;
        ItemInfo info = d.dragInfo;
        if (info instanceof AppInfo) {
            componentName = ((AppInfo) info).componentName;

        } else if (info instanceof ShortcutInfo) {
            componentName = ((ShortcutInfo) info).intent.getComponent();

        }

        if (componentName != null) {
            mLauncher.startEdit(info, componentName);
        }
    }
}
