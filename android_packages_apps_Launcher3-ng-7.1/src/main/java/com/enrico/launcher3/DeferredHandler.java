package com.enrico.launcher3;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;

import com.enrico.launcher3.util.Thunk;

import java.util.LinkedList;

/**
 * Queue of things to run on a looper thread.  Items posted with {@link #post} will not
 * be actually enqued on the handler until after the last one has run, to keep from
 * starving the thread.
 * <p>
 * This class is fifo.
 */
public class DeferredHandler {
    @Thunk
    private static final LinkedList<Runnable> mQueue = new LinkedList<>();
    private static MessageQueue mMessageQueue = Looper.myQueue();
    private static Impl mHandler = new Impl();

    DeferredHandler() {
    }

    private static void scheduleNextLocked() {
        if (mQueue.size() > 0) {
            Runnable peek = mQueue.getFirst();
            if (peek instanceof IdleRunnable) {
                mMessageQueue.addIdleHandler(mHandler);
            } else {
                mHandler.sendEmptyMessage(1);
            }
        }
    }

    /**
     * Schedule runnable to run after everything that's on the queue right now.
     */
    public void post(Runnable runnable) {
        synchronized (mQueue) {
            mQueue.add(runnable);
            if (mQueue.size() == 1) {
                scheduleNextLocked();
            }
        }
    }

    /**
     * Schedule runnable to run when the queue goes idle.
     */
    void postIdle(final Runnable runnable) {
        post(new IdleRunnable(runnable));
    }

    void cancelAll() {
        synchronized (mQueue) {
            mQueue.clear();
        }
    }

    /**
     * Runs all queued Runnables from the calling thread.
     */
    void flush() {
        LinkedList<Runnable> queue = new LinkedList<>();
        synchronized (mQueue) {
            queue.addAll(mQueue);
            mQueue.clear();
        }
        for (Runnable r : queue) {
            r.run();
        }
    }

    @Thunk
    private static class Impl extends Handler implements MessageQueue.IdleHandler {
        public void handleMessage(Message msg) {
            Runnable r;
            synchronized (mQueue) {
                if (mQueue.size() == 0) {
                    return;
                }
                r = mQueue.removeFirst();
            }
            r.run();
            synchronized (mQueue) {
                scheduleNextLocked();
            }
        }

        public boolean queueIdle() {
            handleMessage(null);
            return false;
        }
    }

    private class IdleRunnable implements Runnable {
        Runnable mRunnable;

        IdleRunnable(Runnable r) {
            mRunnable = r;
        }

        public void run() {
            mRunnable.run();
        }
    }
}

