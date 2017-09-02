package com.enrico.launcher3.allapps;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.enrico.launcher3.BubbleTextView;
import com.enrico.launcher3.BubbleTextView.BubbleTextShadowHandler;
import com.enrico.launcher3.ClickShadowView;
import com.enrico.launcher3.DeviceProfile;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.R;

/**
 * A container for RecyclerView to allow for the click shadow view to be shown behind an icon that
 * is launching.
 */
public class AllAppsRecyclerViewContainerView extends FrameLayout
        implements BubbleTextShadowHandler {

    private final ClickShadowView mTouchFeedbackView;

    public AllAppsRecyclerViewContainerView(Context context) {
        this(context, null);
    }

    public AllAppsRecyclerViewContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsRecyclerViewContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Launcher launcher = Launcher.getLauncher(context);
        DeviceProfile grid = launcher.getDeviceProfile();

        mTouchFeedbackView = new ClickShadowView(context);

        // Make the feedback view large enough to hold the blur bitmap.
        int size = grid.allAppsIconSizePx + mTouchFeedbackView.getExtraSize();
        addView(mTouchFeedbackView, size, size);
    }

    @Override
    public void setPressedIcon(BubbleTextView icon, Bitmap background) {
        if (icon == null || background == null) {
            mTouchFeedbackView.setBitmap(null);
            mTouchFeedbackView.animate().cancel();
        } else if (mTouchFeedbackView.setBitmap(background)) {
            View rv = findViewById(R.id.apps_list_view);
            mTouchFeedbackView.alignWithIconView(icon, (ViewGroup) icon.getParent(), rv);
            mTouchFeedbackView.animateShadow();
        }
    }
}
