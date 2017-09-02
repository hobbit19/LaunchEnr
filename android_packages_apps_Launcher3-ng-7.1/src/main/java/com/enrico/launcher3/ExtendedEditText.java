package com.enrico.launcher3;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;


/**
 * The edit text that reports back when the back key has been pressed.
 */
public class ExtendedEditText extends AppCompatEditText {

    private boolean mShowImeAfterFirstLayout;
    private OnBackKeyListener mBackKeyListener;

    public ExtendedEditText(Context context) {
        // ctor chaining breaks the touch handling
        super(context);
    }

    public ExtendedEditText(Context context, AttributeSet attrs) {
        // ctor chaining breaks the touch handling
        super(context, attrs);
    }

    public ExtendedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnBackKeyListener(OnBackKeyListener listener) {
        mBackKeyListener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        // If this is a back key, propagate the key back to the listener
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mBackKeyListener != null) {
                return mBackKeyListener.onBackKey();
            }
            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        // We don't want this view to interfere with Launcher own drag and drop.
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mShowImeAfterFirstLayout) {
            // soft input only shows one frame after the layout of the EditText happens,
            post(new Runnable() {
                @Override
                public void run() {
                    showSoftInput();
                    mShowImeAfterFirstLayout = false;
                }
            });
        }
    }

    public void showKeyboard() {
        mShowImeAfterFirstLayout = !showSoftInput();
    }

    private boolean showSoftInput() {
        return requestFocus() &&
                ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Implemented by listeners of the back key.
     */
    public interface OnBackKeyListener {
        boolean onBackKey();
    }
}
