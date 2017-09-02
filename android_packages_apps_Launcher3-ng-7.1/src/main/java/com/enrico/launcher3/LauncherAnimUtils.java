package com.enrico.launcher3;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.util.Property;
import android.view.View;

import java.util.HashSet;
import java.util.WeakHashMap;

public class LauncherAnimUtils {
    private static WeakHashMap<Animator, Object> sAnimators = new WeakHashMap<Animator, Object>();
    private static Animator.AnimatorListener sEndAnimListener = new Animator.AnimatorListener() {
        public void onAnimationStart(Animator animation) {
            sAnimators.put(animation, null);
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            sAnimators.remove(animation);
        }

        public void onAnimationCancel(Animator animation) {
            sAnimators.remove(animation);
        }
    };

    static void cancelOnDestroyActivity(Animator a) {
        a.addListener(sEndAnimListener);
    }

/*    // Helper method. Assumes a draw is pending, and that if the animation's duration is 0
    // it should be cancelled
    public static void startAnimationAfterNextDraw(final Animator animator, final View view) {
        view.getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
            private boolean mStarted = false;

            public void onDraw() {
                if (mStarted) return;
                mStarted = true;
                // Use this as a signal that the animation was cancelled
                if (animator.getDuration() == 0) {
                    return;
                }
                animator.start();

                final ViewTreeObserver.OnDrawListener listener = this;
                view.post(new Runnable() {
                    public void run() {
                        view.getViewTreeObserver().removeOnDrawListener(listener);
                    }
                });
            }
        });
    }*/

    static void onDestroyActivity() {
        HashSet<Animator> animators = new HashSet<Animator>(sAnimators.keySet());
        for (Animator a : animators) {
            if (a.isRunning()) {
                a.cancel();
            }
            sAnimators.remove(a);
        }
    }

    public static AnimatorSet createAnimatorSet() {
        AnimatorSet anim = new AnimatorSet();
        cancelOnDestroyActivity(anim);
        return anim;
    }

    public static ValueAnimator ofFloat(View target, float... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setFloatValues(values);
        cancelOnDestroyActivity(anim);
        return anim;
    }

    public static ObjectAnimator ofFloat(View target, Property<View, Float> property,
                                         float... values) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(target, property, values);
        cancelOnDestroyActivity(anim);
        new FirstFrameAnimatorHelper(anim, target);
        return anim;
    }

    public static ObjectAnimator ofViewAlphaAndScale(View target,
                                                     float alpha, float scaleX, float scaleY) {
        return ofPropertyValuesHolder(target,
                PropertyValuesHolder.ofFloat(View.ALPHA, alpha),
                PropertyValuesHolder.ofFloat(View.SCALE_X, scaleX),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, scaleY));
    }

    private static ObjectAnimator ofPropertyValuesHolder(View target,
                                                         PropertyValuesHolder... values) {
        return ofPropertyValuesHolder(target, target, values);
    }

    static ObjectAnimator ofPropertyValuesHolder(Object target,
                                                 View view, PropertyValuesHolder... values) {
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(target, values);
        cancelOnDestroyActivity(anim);
        new FirstFrameAnimatorHelper(anim, view);
        return anim;
    }
}
