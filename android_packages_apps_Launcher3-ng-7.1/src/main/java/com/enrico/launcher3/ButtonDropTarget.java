package com.enrico.launcher3;

import android.animation.AnimatorSet;
import android.animation.FloatArrayEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.enrico.launcher3.dragndrop.DragController;
import com.enrico.launcher3.dragndrop.DragLayer;
import com.enrico.launcher3.dragndrop.DragOptions;
import com.enrico.launcher3.dragndrop.DragView;
import com.enrico.launcher3.util.Thunk;

/**
 * Implements a DropTarget.
 */
public abstract class ButtonDropTarget extends AppCompatImageButton
        implements DropTarget, DragController.DragListener, OnClickListener {

    private static final int DRAG_VIEW_DROP_DURATION = 285;
    protected final Launcher mLauncher;
    private final boolean mHideParentOnDisable;
    /**
     * An item must be dragged at least this many pixels before this drop target is enabled.
     */
    private final int mDragDistanceThreshold;
    protected DropTargetBar mDropTargetBar;

    /**
     * Whether this drop target is active for the current drag
     */
    protected boolean mActive;
    /**
     * The paint applied to the drag view on hover
     */
    protected int mHoverColor;
    protected int mOriginalButtonColor;
    protected Drawable mDrawable;
    @Thunk
    ColorMatrix mSrcFilter, mDstFilter, mCurrentFilter;
    private int mBottomDragPadding;
    /**
     * Whether an accessible drag is in progress
     */
    private boolean mAccessibleDrag;
    private AnimatorSet mCurrentColorAnim;
    private Context context;

    public ButtonDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLauncher = Launcher.getLauncher(context);
        mOriginalButtonColor = Utilities.getComplementaryColor(Utilities.getColorAccent(context));

        mHoverColor = Utilities.isDarkThemeEnabled(context)? ContextCompat.getColor(mLauncher, R.color.trans_material_dark_default) : ContextCompat.getColor(mLauncher, R.color.trans_material_light_o);

        Resources resources = getResources();
        mBottomDragPadding = resources.getDimensionPixelSize(R.dimen.drop_target_drag_padding);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ButtonDropTarget, defStyle, 0);
        mHideParentOnDisable = a.getBoolean(R.styleable.ButtonDropTarget_hideParentOnDisable, false);
        a.recycle();
        mDragDistanceThreshold = resources.getDimensionPixelSize(R.dimen.drag_distanceThreshold);
        this.context = context;


    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    protected void setDrawable(int resId) {
        // We do not set the drawable in the xml as that inflates two drawables corresponding to
        // drawableLeft and drawableStart.
        mDrawable = getResources().getDrawable(resId, context.getTheme());

        this.setImageDrawable(mDrawable);

    }

    public void setDropTargetBar(DropTargetBar dropTargetBar) {
        mDropTargetBar = dropTargetBar;
    }

    @Override
    public void onFlingToDelete(DragObject d, PointF vec) {
    }

    @Override
    public final void onDragEnter(DragObject d) {

        d.dragView.setColor(mHoverColor);
        animateButtonColor(mHoverColor);

        if (d.stateAnnouncer != null) {
            d.stateAnnouncer.cancel();
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
    }

    @Override
    public void onDragOver(DragObject d) {
        // Do nothing
    }

    protected void resetHoverColor() {

        animateButtonColor(mOriginalButtonColor);
    }

    private void animateButtonColor(int targetColor) {
        if (mCurrentColorAnim != null) {
            mCurrentColorAnim.cancel();
        }

        mCurrentColorAnim = new AnimatorSet();
        mCurrentColorAnim.setDuration(DragView.COLOR_CHANGE_DURATION);

        if (mSrcFilter == null) {
            mSrcFilter = new ColorMatrix();
            mDstFilter = new ColorMatrix();
            mCurrentFilter = new ColorMatrix();
        }

        DragView.setColorScale(targetColor, mSrcFilter);
        DragView.setColorScale(targetColor, mDstFilter);
        ValueAnimator anim1 = ValueAnimator.ofObject(
                new FloatArrayEvaluator(mCurrentFilter.getArray()),
                mSrcFilter.getArray(), mDstFilter.getArray());
        anim1.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDrawable.setColorFilter(new ColorMatrixColorFilter(mCurrentFilter));
                invalidate();
            }
        });

        mCurrentColorAnim.play(anim1);
        mCurrentColorAnim.start();
    }

    @Override
    public final void onDragExit(DragObject d) {
        if (!d.dragComplete) {
            d.dragView.setColor(0);
            resetHoverColor();

        } else {
            // Restore the hover color
            d.dragView.setColor(mHoverColor);

        }
    }

    @Override
    public void onDragStart(DropTarget.DragObject dragObject, DragOptions options) {
        mActive = supportsDrop(dragObject.dragSource, dragObject.dragInfo);
        mDrawable.setColorFilter(null);
        mDrawable.setTint(mOriginalButtonColor);
        if (mCurrentColorAnim != null) {
            mCurrentColorAnim.cancel();
            mCurrentColorAnim = null;
        }

        (mHideParentOnDisable ? ((ViewGroup) getParent()) : this)
                .setVisibility(mActive ? View.VISIBLE : View.GONE);

        mAccessibleDrag = options.isAccessibleDrag;
        setOnClickListener(mAccessibleDrag ? this : null);
    }

    @Override
    public final boolean acceptDrop(DragObject dragObject) {
        return supportsDrop(dragObject.dragSource, dragObject.dragInfo);
    }

    protected abstract boolean supportsDrop(DragSource source, ItemInfo info);

    @Override
    public boolean isDropEnabled() {
        return mActive && (mAccessibleDrag ||
                mLauncher.getDragController().getDistanceDragged() >= mDragDistanceThreshold);
    }

    @Override
    public void onDragEnd() {
        mActive = false;
        setOnClickListener(null);
    }

    /**
     * On drop animate the dropView to the icon.
     */
    @Override
    public void onDrop(final DragObject d) {
        final DragLayer dragLayer = mLauncher.getDragLayer();
        final Rect from = new Rect();

        dragLayer.getViewRectRelativeToSelf(d.dragView, from);

        int width = mDrawable.getIntrinsicWidth();
        int height = mDrawable.getIntrinsicHeight();
        final Rect to = getIconRect(d.dragView.getMeasuredWidth(), d.dragView.getMeasuredHeight(),
                width, height);
        final float scale = (float) to.width() / from.width();
        mDropTargetBar.deferOnDragEnd();

        Runnable onAnimationEndRunnable = new Runnable() {
            @Override
            public void run() {
                completeDrop(d);
                mDropTargetBar.onDragEnd();
                mLauncher.exitSpringLoadedDragModeDelayed(true, 0, null);
            }
        };
        dragLayer.animateView(d.dragView, from, to, scale, 1f, 1f, 0.1f, 0.1f,
                mLauncher.getDragController().isExternalDrag() ? 1 : DRAG_VIEW_DROP_DURATION,
                new DecelerateInterpolator(2),
                new LinearInterpolator(), onAnimationEndRunnable,
                DragLayer.ANIMATION_END_DISAPPEAR, null);
    }

    @Override
    public void prepareAccessibilityDrop() {
    }

    @Thunk
    public abstract void completeDrop(DragObject d);

    @Override
    public void getHitRectRelativeToDragLayer(android.graphics.Rect outRect) {
        super.getHitRect(outRect);
        outRect.bottom += mBottomDragPadding;

        int[] coords = new int[2];
        mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, coords);
        outRect.offsetTo(coords[0], coords[1]);
    }

    protected Rect getIconRect(int viewWidth, int viewHeight, int drawableWidth, int drawableHeight) {
        DragLayer dragLayer = mLauncher.getDragLayer();

        // Find the rect to animate to (the view is center aligned)
        Rect to = new Rect();
        dragLayer.getViewRectRelativeToSelf(this, to);

        final int left;
        final int right;

        if (Utilities.isRtl(getResources())) {
            right = to.right - getPaddingRight();
            left = right - drawableWidth;
        } else {
            left = to.left + getPaddingLeft();
            right = left + drawableWidth;
        }

        final int top = to.top + (getMeasuredHeight() - drawableHeight) / 2;
        final int bottom = top + drawableHeight;

        to.set(left, top, right, bottom);

        // Center the destination rect about the trash icon
        final int xOffset = -(viewWidth - drawableWidth) / 2;
        final int yOffset = -(viewHeight - drawableHeight) / 2;
        to.offset(xOffset, yOffset);

        return to;
    }

    @Override
    public void onClick(View v) {
        mLauncher.getAccessibilityDelegate().handleAccessibleDrop(this, null, null);
    }
}
