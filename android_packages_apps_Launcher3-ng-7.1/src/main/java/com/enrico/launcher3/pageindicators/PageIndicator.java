package com.enrico.launcher3.pageindicators;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.enrico.launcher3.dynamicui.ExtractedColors;

/**
 * Base class for a page indicator.
 */
public abstract class PageIndicator extends FrameLayout {
    protected int mNumPages = 1;
    private CaretDrawable mCaretDrawable;

    public PageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    public void setScroll(int currentScroll, int totalScroll) {
    }

    public void setActiveMarker(int activePage) {
    }

    public void addMarker() {
        mNumPages++;
        onPageCountChanged();
    }

    public void removeMarker() {
        mNumPages--;
        onPageCountChanged();
    }

    public void setMarkersCount(int numMarkers) {
        mNumPages = numMarkers;
        onPageCountChanged();
    }

    public CaretDrawable getCaretDrawable() {
        return mCaretDrawable;
    }

    public void setCaretDrawable(CaretDrawable caretDrawable) {
        if (mCaretDrawable != null) {
            mCaretDrawable.setCallback(null);
        }

        mCaretDrawable = caretDrawable;

        if (mCaretDrawable != null) {
            mCaretDrawable.setCallback(this);
        }
    }

    protected void onPageCountChanged() {
    }

    public void setShouldAutoHide(boolean shouldAutoHide) {
    }

    public void updateColor(ExtractedColors extractedColors) {
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || who == getCaretDrawable();
    }
}
