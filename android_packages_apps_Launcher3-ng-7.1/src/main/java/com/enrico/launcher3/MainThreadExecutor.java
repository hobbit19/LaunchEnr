package com.enrico.launcher3;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An executor service that executes its tasks on the main thread.
 * <p>
 * Shutting down this executor is not supported.
 */
public class MainThreadExecutor extends AbstractExecutorService {

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(@NonNull Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
    }

    /**
     * Not supported and throws an exception when used.
     */
    @Override
    @Deprecated
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported and throws an exception when used.
     */
    @Override
    @Deprecated
    @NonNull
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    /**
     * Not supported and throws an exception when used.
     */
    @Override
    @Deprecated
    public boolean awaitTermination(long l, @NonNull TimeUnit timeUnit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }
}
