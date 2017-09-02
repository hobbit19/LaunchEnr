package com.enrico.launcher3.dragndrop;

import com.enrico.launcher3.Alarm;
import com.enrico.launcher3.CellLayout;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.OnAlarmListener;
import com.enrico.launcher3.Workspace;

public class SpringLoadedDragController implements OnAlarmListener {
    // how long the user must hover over a mini-screen before it unshrinks
    private final long ENTER_SPRING_LOAD_HOVER_TIME = 500;
    private final long ENTER_SPRING_LOAD_CANCEL_HOVER_TIME = 950;

    private Alarm mAlarm;

    // the screen the user is currently hovering over, if any
    private CellLayout mScreen;
    private Launcher mLauncher;

    public SpringLoadedDragController(Launcher launcher) {
        mLauncher = launcher;
        mAlarm = new Alarm();
        mAlarm.setOnAlarmListener(this);
    }

    public void cancel() {
        mAlarm.cancelAlarm();
    }

    // Set a new alarm to expire for the screen that we are hovering over now
    public void setAlarm(CellLayout cl) {
        mAlarm.cancelAlarm();
        mAlarm.setAlarm((cl == null) ? ENTER_SPRING_LOAD_CANCEL_HOVER_TIME :
                ENTER_SPRING_LOAD_HOVER_TIME);
        mScreen = cl;
    }

    // this is called when our timer runs out
    public void onAlarm(Alarm alarm) {
        if (mScreen != null) {
            // Snap to the screen that we are hovering over now
            Workspace w = mLauncher.getWorkspace();
            int page = w.indexOfChild(mScreen);
            if (page != w.getCurrentPage()) {
                w.snapToPage(page);
            }
        } else {
            mLauncher.getDragController().cancelDrag();
        }
    }
}
