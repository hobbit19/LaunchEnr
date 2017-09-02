package com.enrico.launcher3;

/**
 * An interface to get callbacks during a launcher transition.
 */
public interface LauncherTransitionable {
    void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean multiplePagesVisible);

    void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace);

    void onLauncherTransitionStep(Launcher l, float t);

    void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace);
}
