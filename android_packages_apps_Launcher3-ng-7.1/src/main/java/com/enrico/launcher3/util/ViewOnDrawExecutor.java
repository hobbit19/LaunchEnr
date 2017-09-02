package com.enrico.launcher3.util;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewTreeObserver.OnDrawListener;

import com.enrico.launcher3.DeferredHandler;
import com.enrico.launcher3.Launcher;

import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * An executor which runs all the tasks after the first onDraw is called on the target view.
 */
public class ViewOnDrawExecutor implements Executor, OnDrawListener, Runnable,
        OnAttachStateChangeListener {

    private final ArrayList<Runnable> mTasks = new ArrayList<>();
    private final DeferredHandler mHandler;

    private Launcher mLauncher;
    private View mAttachedView;
    private boolean mCompleted;

    private boolean mLoadAnimationCompleted;
    private boolean mFirstDrawCompleted;

    public ViewOnDrawExecutor(DeferredHandler handler) {
        mHandler = handler;
    }

    public void attachTo(Launcher launcher) {
        mLauncher = launcher;
        mAttachedView = launcher.getWorkspace();
        mAttachedView.addOnAttachStateChangeListener(this);

        attachObserver();
    }

    private void attachObserver() {
        if (!mCompleted) {
            mAttachedView.getViewTreeObserver().addOnDrawListener(this);
        }
    }

    @Override
    public void execute(@NonNull Runnable command) {
        mTasks.add(command);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        attachObserver();
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
    }

    @Override
    public void onDraw() {
        mFirstDrawCompleted = true;
        mAttachedView.post(this);
    }

    public void onLoadAnimationCompleted() {
        mLoadAnimationCompleted = true;
        if (mAttachedView != null) {
            mAttachedView.post(this);
        }
    }

    @Override
    public void run() {
        // Post the pending tasks after both onDraw and onLoadAnimationCompleted have been called.
        if (mLoadAnimationCompleted && mFirstDrawCompleted && !mCompleted) {
            for (final Runnable r : mTasks) {
                mHandler.post(r);
            }
            markCompleted();
        }
    }

    public void markCompleted() {
        mTasks.clear();
        mCompleted = true;
        if (mAttachedView != null) {
            mAttachedView.getViewTreeObserver().removeOnDrawListener(this);
            mAttachedView.removeOnAttachStateChangeListener(this);
        }
        if (mLauncher != null) {
            mLauncher.clearPendingExecutor(this);
        }
    }
}
