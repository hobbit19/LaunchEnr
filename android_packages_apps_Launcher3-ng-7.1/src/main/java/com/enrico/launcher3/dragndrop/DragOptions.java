package com.enrico.launcher3.dragndrop;

import android.graphics.Point;

/**
 * Set of options to control the drag and drop behavior.
 */
public class DragOptions {

    /**
     * Whether or not an accessible drag operation is in progress.
     */
    public boolean isAccessibleDrag = false;
    /**
     * Determines when a deferred drag should start. By default, drags aren't deferred at all.
     */
    public DeferDragCondition deferDragCondition = new DeferDragCondition();
    /**
     * Specifies the start location for the system DnD, null when using internal DnD
     */
    Point systemDndStartPoint = null;

    /**
     * Specifies a condition that must be met before DragListener#onDragStart() is called.
     * By default, there is no condition and onDragStart() is called immediately following
     * DragController#startDrag().
     * <p>
     * This condition can be overridden, and callbacks are provided for the following cases:
     * - The drag starts, but onDragStart() is deferred (onDeferredDragStart()).
     * - The drag ends before the condition is met (onDropBeforeDeferredDrag()).
     * - The condition is met (onDragStart()).
     */
    public static class DeferDragCondition {
        public boolean shouldStartDeferredDrag(double distanceDragged) {
            return true;
        }

        /**
         * The drag has started, but onDragStart() is deferred.
         * This happens when shouldStartDeferredDrag() returns true.
         */
        public void onDeferredDragStart() {
            // Do nothing.
        }

        /**
         * User dropped before the deferred condition was met,
         * i.e. before shouldStartDeferredDrag() returned true.
         */
        public void onDropBeforeDeferredDrag() {
            // Do nothing
        }

        /**
         * onDragStart() has been called, now we are in a normal drag.
         */
        public void onDragStart() {
            // Do nothing
        }
    }
}
