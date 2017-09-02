package com.enrico.launcher3.allapps;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * One dimensional scroll gesture detector for all apps container pull up interaction.
 * Client (e.g., AllAppsTransitionController) of this class can register a listener.
 * <p/>
 * Features that this gesture detector can support.
 */
class VerticalPullDetector {

    static final int DIRECTION_UP = 1 << 0;
    static final int DIRECTION_DOWN = 1 << 1;
    static final int DIRECTION_BOTH = DIRECTION_DOWN | DIRECTION_UP;
    /**
     * The minimum release velocity in pixels per millisecond that triggers fling..
     */
    static final float RELEASE_VELOCITY_PX_MS = 1.0f;
    /**
     * The time constant used to calculate dampening in the low-pass filter of scroll velocity.
     * Cutoff frequency is set at 10 Hz.
     */
    private static final float SCROLL_VELOCITY_DAMPENING_RC = 1000f / (2f * (float) Math.PI * 10);
    private float mTouchSlop;
    private int mScrollConditions;
    /* Scroll state, this is set to true during dragging and animation. */
    private ScrollState mState = ScrollState.IDLE;
    private float mDownX;

    //------------------- ScrollState transition diagram -----------------------------------
    //
    // IDLE ->      (mDisplacement > mTouchSlop) -> DRAGGING
    // DRAGGING -> (MotionEvent#ACTION_UP, MotionEvent#ACTION_CANCEL) -> SETTLING
    // SETTLING -> (MotionEvent#ACTION_DOWN) -> DRAGGING
    // SETTLING -> (View settled) -> IDLE
    private float mDownY;
    private float mLastY;
    private long mCurrentMillis;
    private float mVelocity;
    private float mLastDisplacement;
    private float mDisplacementY;
    private float mDisplacementX;
    private float mSubtractDisplacement;
    private boolean mIgnoreSlopWhenSettling;
    /* Client of this gesture detector can register a callback. */
    private Listener mListener;
    VerticalPullDetector(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * Returns a time-dependent dampening factor using delta time.
     */
    private static float computeDampeningFactor(float deltaTime) {
        return deltaTime / (SCROLL_VELOCITY_DAMPENING_RC + deltaTime);
    }

    /**
     * Returns the linear interpolation between two values
     */
    private static float interpolate(float from, float to, float alpha) {
        return (1.0f - alpha) * from + alpha * to;
    }

    private void setState(ScrollState newState) {

        // onDragStart and onDragEnd is reported ONLY on state transition
        if (newState == ScrollState.DRAGGING) {
            initializeDragging();
            if (mState == ScrollState.IDLE) {
                reportDragStart(false /* recatch */);
            } else if (mState == ScrollState.SETTLING) {
                reportDragStart(true /* recatch */);
            }
        }
        if (newState == ScrollState.SETTLING) {
            reportDragEnd();
        }

        mState = newState;
    }

    boolean isDraggingOrSettling() {
        return mState == ScrollState.DRAGGING || mState == ScrollState.SETTLING;
    }

    /**
     * There's no touch and there's no animation.
     */
    boolean isIdleState() {
        return mState == ScrollState.IDLE;
    }

    boolean isSettlingState() {
        return mState == ScrollState.SETTLING;
    }

    boolean isDraggingState() {
        return mState == ScrollState.DRAGGING;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    void setDetectableScrollConditions(int scrollDirectionFlags, boolean ignoreSlop) {
        mScrollConditions = scrollDirectionFlags;
        mIgnoreSlopWhenSettling = ignoreSlop;
    }

    private boolean shouldScrollStart() {
        // reject cases where the slop condition is not met.
        if (Math.abs(mDisplacementY) < mTouchSlop) {
            return false;
        }

        // reject cases where the angle condition is not met.
        float deltaY = Math.abs(mDisplacementY);
        float deltaX = Math.max(Math.abs(mDisplacementX), 1);
        if (deltaX > deltaY) {
            return false;
        }
        // Check if the client is interested in scroll in current direction.
        if (((mScrollConditions & DIRECTION_DOWN) > 0 && mDisplacementY > 0) ||
                ((mScrollConditions & DIRECTION_UP) > 0 && mDisplacementY < 0)) {
            return true;
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                mLastDisplacement = 0;
                mDisplacementY = 0;
                mVelocity = 0;

                if (mState == ScrollState.SETTLING && mIgnoreSlopWhenSettling) {
                    setState(ScrollState.DRAGGING);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mDisplacementX = ev.getX() - mDownX;
                mDisplacementY = ev.getY() - mDownY;
                computeVelocity(ev);

                // handle state and listener calls.
                if (mState != ScrollState.DRAGGING && shouldScrollStart()) {
                    setState(ScrollState.DRAGGING);
                }
                if (mState == ScrollState.DRAGGING) {
                    reportDragging();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // These are synthetic events and there is no need to update internal values.
                if (mState == ScrollState.DRAGGING) {
                    setState(ScrollState.SETTLING);
                }
                break;
            default:
                //TODO: add multi finger tracking by tracking active pointer.
                break;
        }
        // Do house keeping.
        mLastDisplacement = mDisplacementY;
        mLastY = ev.getY();
        return true;
    }

    void finishedScrolling() {
        setState(ScrollState.IDLE);
    }

    private boolean reportDragStart(boolean recatch) {
        mListener.onDragStart(!recatch);

        return true;
    }

    private void initializeDragging() {
        if (mState == ScrollState.SETTLING && mIgnoreSlopWhenSettling) {
            mSubtractDisplacement = 0;
        }
        if (mDisplacementY > 0) {
            mSubtractDisplacement = mTouchSlop;
        } else {
            mSubtractDisplacement = -mTouchSlop;
        }
    }

    private boolean reportDragging() {
        float delta = mDisplacementY - mLastDisplacement;
        if (delta != 0) {

            return mListener.onDrag(mDisplacementY - mSubtractDisplacement, mVelocity);
        }
        return true;
    }

    private void reportDragEnd() {

        mListener.onDragEnd(mVelocity, Math.abs(mVelocity) > RELEASE_VELOCITY_PX_MS);

    }

    /**
     * Computes the damped velocity using the two motion events and the previous velocity.
     */
    private float computeVelocity(MotionEvent to) {
        return computeVelocity(to.getY() - mLastY, to.getEventTime());
    }

    float computeVelocity(float delta, long currentMillis) {
        long previousMillis = mCurrentMillis;
        mCurrentMillis = currentMillis;

        float deltaTimeMillis = mCurrentMillis - previousMillis;
        float velocity = (deltaTimeMillis > 0) ? (delta / deltaTimeMillis) : 0;
        if (Math.abs(mVelocity) < 0.001f) {
            mVelocity = velocity;
        } else {
            float alpha = computeDampeningFactor(deltaTimeMillis);
            mVelocity = interpolate(mVelocity, velocity, alpha);
        }
        return mVelocity;
    }

    private enum ScrollState {
        IDLE,
        DRAGGING,      // onDragStart, onDrag
        SETTLING       // onDragEnd
    }

    interface Listener {
        void onDragStart(boolean start);

        boolean onDrag(float displacement, float velocity);

        void onDragEnd(float velocity, boolean fling);
    }
}
