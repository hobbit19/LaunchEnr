package com.enrico.launcher3.allapps;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Keep;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.enrico.launcher3.CellLayout;
import com.enrico.launcher3.Hotseat;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.LauncherAnimUtils;
import com.enrico.launcher3.LauncherAppWidgetHostView;
import com.enrico.launcher3.R;
import com.enrico.launcher3.ShortcutAndWidgetContainer;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.Workspace;
import com.enrico.launcher3.util.TouchController;

/**
 * Handles AllApps view transition.
 * 1) Slides all apps view using direct manipulation
 * 2) When finger is released, animate to either top or bottom accordingly.
 * <p/>
 * Algorithm:
 * If release velocity > THRES1, snap according to the direction of movement.
 * If release velocity < THRES1, snap according to either top or bottom depending on whether it's
 * closer to top or closer to the page indicator.
 */
public class AllAppsTransitionController implements TouchController, VerticalPullDetector.Listener,
        View.OnLayoutChangeListener {

    private static final float ANIMATION_DURATION = 1200;
    private static final float PARALLAX_COEFFICIENT = .125f;
    private static final float FAST_FLING_PX_MS = 10;
    private static final int SINGLE_FRAME_MS = 16;
    private static final float DEFAULT_SHIFT_RANGE = 10;
    private static final float RECATCH_REJECTION_FRACTION = .0875f;
    private final Interpolator mAccelInterpolator = new AccelerateInterpolator(2f);
    private final Interpolator mDecelInterpolator = new DecelerateInterpolator(3f);
    private final Interpolator mFastOutSlowInInterpolator = new FastOutSlowInInterpolator();
    private final ScrollInterpolator mScrollInterpolator = new ScrollInterpolator();
    private final Launcher mLauncher;
    private final VerticalPullDetector mDetector;
    private final ArgbEvaluator mEvaluator;
    private AllAppsContainerView mAppsView;
    private int mAllAppsBackgroundColor;
    private Workspace mWorkspace;
    private Hotseat mHotseat;
    private int mHotseatBackgroundColor;

    // Animation in this class is controlled by a single variable {@link mProgress}.
    // Visually, it represents top y coordinate of the all apps container if multiplied with
    // {@link mShiftRange}.
    private AllAppsCaretController mCaretController;
    private float mStatusBarHeight;
    // When {@link mProgress} is 0, all apps container is pulled up.
    // When {@link mProgress} is 1, all apps container is pulled down.
    private float mShiftStart;      // [0, mShiftRange]
    private float mShiftRange;      // changes depending on the orientation
    private float mProgress;        // [0, 1], mShiftRange * mProgress = shiftCurrent
    // Velocity of the container. Unit is in px/ms.
    private float mContainerVelocity;
    private long mAnimationDuration;

    private AnimatorSet mCurrentAnimation;
    private boolean mNoIntercept;

    // Used in discovery bounce animation to provide the transition without workspace changing.
    private boolean mIsTranslateWithoutWorkspace = false;
    private AnimatorSet mDiscoBounceAnimation;

    public AllAppsTransitionController(Launcher l) {
        mLauncher = l;
        mDetector = new VerticalPullDetector(l);
        mDetector.setListener(this);
        mShiftRange = DEFAULT_SHIFT_RANGE;
        mProgress = 1f;

        mEvaluator = new ArgbEvaluator();

        mAllAppsBackgroundColor = Utilities.isDarkThemeEnabled(l) ? ContextCompat.getColor(l, R.color.material_dark) : ContextCompat.getColor(l, R.color.all_apps_container_color);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mNoIntercept = false;
            if (!mLauncher.isAllAppsVisible() && mLauncher.getWorkspace().workspaceInModalState()) {
                mNoIntercept = true;
            } else if (mLauncher.isAllAppsVisible() &&
                    !mAppsView.shouldContainerScroll(ev)) {
                mNoIntercept = true;
            } else if (!mLauncher.isAllAppsVisible() && !shouldPossiblyIntercept(ev)) {
                mNoIntercept = true;
            } else {
                // Now figure out which direction scroll events the controller will start
                // calling the callbacks.
                int directionsToDetectScroll = 0;
                boolean ignoreSlopWhenSettling = false;

                if (mDetector.isIdleState()) {
                    if (mLauncher.isAllAppsVisible()) {
                        directionsToDetectScroll |= VerticalPullDetector.DIRECTION_DOWN;
                    } else {
                        directionsToDetectScroll |= VerticalPullDetector.DIRECTION_UP;
                    }
                } else {
                    if (isInDisallowRecatchBottomZone()) {
                        directionsToDetectScroll |= VerticalPullDetector.DIRECTION_UP;
                    } else if (isInDisallowRecatchTopZone()) {
                        directionsToDetectScroll |= VerticalPullDetector.DIRECTION_DOWN;
                    } else {
                        directionsToDetectScroll |= VerticalPullDetector.DIRECTION_BOTH;
                        ignoreSlopWhenSettling = true;
                    }
                }
                mDetector.setDetectableScrollConditions(directionsToDetectScroll,
                        ignoreSlopWhenSettling);
            }
        }
        if (mNoIntercept) {
            return false;
        }
        mDetector.onTouchEvent(ev);
        if (mDetector.isSettlingState() && (isInDisallowRecatchBottomZone() || isInDisallowRecatchTopZone())) {
            return false;
        }
        return mDetector.isDraggingOrSettling();
    }

    private boolean shouldPossiblyIntercept(MotionEvent ev) {
        if (mDetector.isIdleState()) {
            CellLayout cl = mLauncher.getWorkspace().getCurrentDropLayout();
            if (cl != null) {
                ShortcutAndWidgetContainer c = cl.getShortcutsAndWidgets();
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                Rect outRect = new Rect();
                int count = c.getChildCount();
                for (int i = 0; i < count; i++) {
                    View v = c.getChildAt(i);
                    if (v instanceof LauncherAppWidgetHostView) {
                        v.getGlobalVisibleRect(outRect);
                        if (outRect.contains(x, y)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDetector.onTouchEvent(ev);
    }

    private boolean isInDisallowRecatchTopZone() {
        return mProgress < RECATCH_REJECTION_FRACTION;
    }

    private boolean isInDisallowRecatchBottomZone() {
        return mProgress > 1 - RECATCH_REJECTION_FRACTION;
    }

    @Override
    public void onDragStart(boolean start) {
        mCaretController.onDragStart();
        cancelAnimation();
        mCurrentAnimation = LauncherAnimUtils.createAnimatorSet();
        mShiftStart = mAppsView.getTranslationY();
        preparePull(start);
    }

    @Override
    public boolean onDrag(float displacement, float velocity) {
        if (mAppsView == null) {
            return false;   // early termination.
        }

        mContainerVelocity = velocity;

        float shift = Math.min(Math.max(0, mShiftStart + displacement), mShiftRange);
        setProgress(shift / mShiftRange);

        return true;
    }

    @Override
    public void onDragEnd(float velocity, boolean fling) {
        if (mAppsView == null) {
            return; // early termination.
        }

        if (fling) {
            if (velocity < 0) {
                calculateDuration(velocity, mAppsView.getTranslationY());

                mLauncher.showAppsView(true /* animated */,
                        false /* updatePredictedApps */,
                        false /* focusSearchBar */);
            } else {
                calculateDuration(velocity, Math.abs(mShiftRange - mAppsView.getTranslationY()));
                mLauncher.showWorkspace(true);
            }
            // snap to top or bottom using the release velocity
        } else {
            if (mAppsView.getTranslationY() > mShiftRange / 2) {
                calculateDuration(velocity, Math.abs(mShiftRange - mAppsView.getTranslationY()));
                mLauncher.showWorkspace(true);
            } else {
                calculateDuration(velocity, Math.abs(mAppsView.getTranslationY()));

                mLauncher.showAppsView(true, /* animated */
                        false /* updatePredictedApps */,
                        false /* focusSearchBar */);
            }
        }
    }

    public boolean isTransitioning() {
        return mDetector.isDraggingOrSettling();
    }

    /**
     * @param start {@code true} if start of new drag.
     */
    private void preparePull(boolean start) {
        if (start) {
            // Initialize values that should not change until #onDragEnd
            mStatusBarHeight = mLauncher.getDragLayer().getInsets().top;
            mHotseat.setVisibility(View.VISIBLE);
            mHotseatBackgroundColor = mHotseat.getBackgroundDrawableColor();
            mHotseat.setBackgroundTransparent(true /* transparent */);
            if (!mLauncher.isAllAppsVisible()) {
                mLauncher.tryAndUpdatePredictedApps();
                mAppsView.setVisibility(View.VISIBLE);
                mAppsView.setRevealDrawableColor(mHotseatBackgroundColor);
            }
        }
    }

    private void updateLightStatusBar(float shift) {
        // Do not modify status bar on landscape as all apps is not full bleed.
        if (mLauncher.getDeviceProfile().isVerticalBarLayout()) {
            return;
        }

        // Use a light status bar (dark icons) if all apps is behind at least half of the status
        // bar. If the status bar is already light due to wallpaper extraction, keep it that way.
        // activate light status bar if light theme is enabled
        boolean forceLight = !Utilities.isDarkThemeEnabled(mLauncher.getBaseContext()) && shift <= mStatusBarHeight / 2;
        mLauncher.activateLightStatusBar(forceLight);

    }

    public float getProgress() {
        return mProgress;
    }

    /**
     * @param progress value between 0 and 1, 0 shows all apps and 1 shows workspace
     */
    @Keep
    public void setProgress(float progress) {
        float shiftPrevious = mProgress * mShiftRange;
        mProgress = progress;
        float shiftCurrent = progress * mShiftRange;

        float workspaceHotseatAlpha = Utilities.boundToRange(progress, 0f, 1f);
        float alpha = 1 - workspaceHotseatAlpha;
        float interpolation = mAccelInterpolator.getInterpolation(workspaceHotseatAlpha);

        int color = (Integer) mEvaluator.evaluate(mDecelInterpolator.getInterpolation(alpha),
                mHotseatBackgroundColor, mAllAppsBackgroundColor);
        int bgAlpha = Color.alpha((int) mEvaluator.evaluate(alpha,
                mHotseatBackgroundColor, mAllAppsBackgroundColor));

        mAppsView.setRevealDrawableColor(ColorUtils.setAlphaComponent(color, bgAlpha));
        mAppsView.getContentView().setAlpha(alpha);
        mAppsView.setTranslationY(shiftCurrent);

        if (!mLauncher.getDeviceProfile().isVerticalBarLayout()) {
            mWorkspace.setHotseatTranslationAndAlpha(Workspace.Direction.Y, -mShiftRange + shiftCurrent,
                    interpolation);
        } else {
            mWorkspace.setHotseatTranslationAndAlpha(Workspace.Direction.Y,
                    PARALLAX_COEFFICIENT * (-mShiftRange + shiftCurrent),
                    interpolation);
        }

        if (mIsTranslateWithoutWorkspace) {
            return;
        }
        mWorkspace.setWorkspaceYTranslationAndAlpha(
                PARALLAX_COEFFICIENT * (-mShiftRange + shiftCurrent), interpolation);

        if (!mDetector.isDraggingState()) {
            mContainerVelocity = mDetector.computeVelocity(shiftCurrent - shiftPrevious,
                    System.currentTimeMillis());
        }

        mCaretController.updateCaret(progress, mContainerVelocity, mDetector.isDraggingState());
        updateLightStatusBar(shiftCurrent);
    }

    private void calculateDuration(float velocity, float disp) {
        // TODO: make these values constants after tuning.
        float velocityDivisor = Math.max(2f, Math.abs(0.5f * velocity));
        float travelDistance = Math.max(0.2f, disp / mShiftRange);
        mAnimationDuration = (long) Math.max(100, ANIMATION_DURATION / velocityDivisor * travelDistance);
    }

    public boolean animateToAllApps(AnimatorSet animationOut, long duration) {
        boolean shouldPost = true;
        if (animationOut == null) {
            return shouldPost;
        }
        Interpolator interpolator;
        if (mDetector.isIdleState()) {
            preparePull(true);
            mAnimationDuration = duration;
            mShiftStart = mAppsView.getTranslationY();
            interpolator = mFastOutSlowInInterpolator;
        } else {
            mScrollInterpolator.setVelocityAtZero(Math.abs(mContainerVelocity));
            interpolator = mScrollInterpolator;
            float nextFrameProgress = mProgress + mContainerVelocity * SINGLE_FRAME_MS / mShiftRange;
            if (nextFrameProgress >= 0f) {
                mProgress = nextFrameProgress;
            }
            shouldPost = false;
        }

        ObjectAnimator driftAndAlpha = ObjectAnimator.ofFloat(this, "progress",
                mProgress, 0f);
        driftAndAlpha.setDuration(mAnimationDuration);
        driftAndAlpha.setInterpolator(interpolator);
        animationOut.play(driftAndAlpha);

        animationOut.addListener(new AnimatorListenerAdapter() {
            boolean canceled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                canceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!canceled) {

                    finishPullUp();
                    cleanUpAnimation();
                    mDetector.finishedScrolling();

                    //disable light status bar if dark theme is enabled
                    if (Utilities.isDarkThemeEnabled(mLauncher.getBaseContext()) && Utilities.areLightBarsEnabled(mLauncher.getBaseContext())) {
                        mLauncher.disableLightStatusBar();
                        mLauncher.disableLightNavigationBar(true);
                    }

                    colorNavigationBar(mLauncher, true);
                    colorStatusBarPreMarshmallow(mLauncher, true);
                }
            }
        });
        mCurrentAnimation = animationOut;
        return shouldPost;
    }

    public void showDiscoveryBounce() {
        // cancel existing animation in case user locked and unlocked at a super human speed.
        cancelDiscoveryAnimation();

        // assumption is that this variable is always null
        mDiscoBounceAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(mLauncher,
                R.animator.discovery_bounce);
        mDiscoBounceAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                mIsTranslateWithoutWorkspace = true;
                preparePull(true);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                finishPullDown();
                mDiscoBounceAnimation = null;
                mIsTranslateWithoutWorkspace = false;
            }
        });
        mDiscoBounceAnimation.setTarget(this);
        mAppsView.post(new Runnable() {
            @Override
            public void run() {
                if (mDiscoBounceAnimation == null) {
                    return;
                }
                mDiscoBounceAnimation.start();
            }
        });
    }

    public boolean animateToWorkspace(AnimatorSet animationOut, long duration) {
        boolean shouldPost = true;
        if (animationOut == null) {
            return shouldPost;
        }
        Interpolator interpolator;
        if (mDetector.isIdleState()) {
            preparePull(true);
            mAnimationDuration = duration;
            mShiftStart = mAppsView.getTranslationY();
            interpolator = mFastOutSlowInInterpolator;
        } else {
            mScrollInterpolator.setVelocityAtZero(Math.abs(mContainerVelocity));
            interpolator = mScrollInterpolator;
            float nextFrameProgress = mProgress + mContainerVelocity * SINGLE_FRAME_MS / mShiftRange;
            if (nextFrameProgress <= 1f) {
                mProgress = nextFrameProgress;
            }
            shouldPost = false;
        }

        ObjectAnimator driftAndAlpha = ObjectAnimator.ofFloat(this, "progress",
                mProgress, 1f);
        driftAndAlpha.setDuration(mAnimationDuration);
        driftAndAlpha.setInterpolator(interpolator);
        animationOut.play(driftAndAlpha);

        animationOut.addListener(new AnimatorListenerAdapter() {
            boolean canceled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                canceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!canceled) {
                    colorNavigationBar(mLauncher, false);
                    colorStatusBarPreMarshmallow(mLauncher, false);
                    finishPullDown();
                    cleanUpAnimation();
                    mDetector.finishedScrolling();
                }
            }
        });
        mCurrentAnimation = animationOut;
        return shouldPost;
    }

    public void finishPullUp() {
        mHotseat.setVisibility(View.INVISIBLE);
        setProgress(0f);
    }

    //method to fix low visibility on light theme for pre marshmallow device that don't have light status bar
    public static void colorStatusBarPreMarshmallow(Activity activity, boolean isColored) {

        boolean isDarkTheme = Utilities.isDarkThemeEnabled(activity);

        if (android.os.Build.VERSION.SDK_INT < 23 && !isDarkTheme) {
            if (isColored) {
                activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.transBlack));
            } else {
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
        }
    }

    //color navigation bar on light theme to make it more visible
    public static void colorNavigationBar(final Launcher mLauncher, final boolean isColored) {

        final boolean isDarkTheme = Utilities.isDarkThemeEnabled(mLauncher);

        mLauncher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isColored) {

                    int navigationBarColor;

                    if (Utilities.ATLEAST_OREO) {

                        if (!isDarkTheme) {
                            mLauncher.activateLightNavigationBar(true);
                        } else {
                            navigationBarColor = ContextCompat.getColor(mLauncher, R.color.transWhite);
                            mLauncher.getWindow().setNavigationBarColor(navigationBarColor);
                        }
                    } else {
                        navigationBarColor = Utilities.isDarkThemeEnabled(mLauncher) ? ContextCompat.getColor(mLauncher, R.color.transWhite) : ContextCompat.getColor(mLauncher, R.color.transBlack);
                        mLauncher.getWindow().setNavigationBarColor(navigationBarColor);
                    }

                } else {

                    mLauncher.disableLightNavigationBar(true);

                    if (mLauncher.getWindow().getNavigationBarColor() != Color.TRANSPARENT) {
                        mLauncher.getWindow().setNavigationBarColor(Color.TRANSPARENT);
                    }
                }
            }
        });
    }

    public void finishPullDown() {
        mAppsView.setVisibility(View.INVISIBLE);
        mHotseat.setBackgroundTransparent(false /* transparent */);
        mHotseat.setVisibility(View.VISIBLE);
        mAppsView.reset();
        setProgress(1f);
    }

    private void cancelAnimation() {
        if (mCurrentAnimation != null) {
            mCurrentAnimation.cancel();
            mCurrentAnimation = null;
        }
        cancelDiscoveryAnimation();
    }

    public void cancelDiscoveryAnimation() {
        if (mDiscoBounceAnimation == null) {
            return;
        }
        mDiscoBounceAnimation.cancel();
        mDiscoBounceAnimation = null;
    }

    private void cleanUpAnimation() {
        mCurrentAnimation = null;
    }

    public void setupViews(AllAppsContainerView appsView, Hotseat hotseat, Workspace workspace) {
        mAppsView = appsView;
        mHotseat = hotseat;
        mWorkspace = workspace;
        mHotseat.addOnLayoutChangeListener(this);
        mHotseat.bringToFront();
        mCaretController = new AllAppsCaretController(
                mWorkspace.getPageIndicator().getCaretDrawable(), mLauncher);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (!mLauncher.getDeviceProfile().isVerticalBarLayout()) {
            mShiftRange = top;
        } else {
            mShiftRange = bottom;
        }
        setProgress(mProgress);
    }

    private static class ScrollInterpolator implements Interpolator {

        boolean mSteeper;

        void setVelocityAtZero(float velocity) {
            mSteeper = velocity > FAST_FLING_PX_MS;
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            float output = t * t * t;
            if (mSteeper) {
                output *= t * t; // Make interpolation initial slope steeper
            }
            return output + 1;
        }
    }
}
