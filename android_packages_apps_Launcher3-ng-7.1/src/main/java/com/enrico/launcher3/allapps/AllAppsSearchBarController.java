package com.enrico.launcher3.allapps;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.enrico.launcher3.ExtendedEditText;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.util.ComponentKey;

import java.util.ArrayList;

/**
 * An interface to a search box that AllApps can command.
 */
public abstract class AllAppsSearchBarController
        implements TextWatcher, OnEditorActionListener, ExtendedEditText.OnBackKeyListener {

    protected Launcher mLauncher;
    AlphabeticalAppsList mApps;
    private Callbacks mCb;
    private ExtendedEditText mInput;
    private String mQuery;

    private DefaultAppSearchAlgorithm mSearchAlgorithm;
    private InputMethodManager mInputMethodManager;

    public void setVisibility(int visibility) {
        mInput.setVisibility(visibility);
    }

    /**
     * Sets the references to the apps model and the search result callback.
     */
    public final void initialize(
            AlphabeticalAppsList apps, ExtendedEditText input,
            Launcher launcher, Callbacks cb) {
        mApps = apps;
        mCb = cb;
        mLauncher = launcher;

        mInput = input;
        mInput.addTextChangedListener(this);
        mInput.setOnEditorActionListener(this);
        mInput.setOnBackKeyListener(this);

        mInputMethodManager = (InputMethodManager)
                mInput.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        mSearchAlgorithm = onInitializeSearch();
    }

    /**
     * To be implemented by subclasses. This method will get called when the controller is set.
     */
    protected abstract DefaultAppSearchAlgorithm onInitializeSearch();

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Do nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Do nothing
    }

    @Override
    public void afterTextChanged(final Editable s) {
        mQuery = s.toString();
        if (mQuery.isEmpty()) {
            mSearchAlgorithm.cancel(true);
            mCb.clearSearchResult();
        } else {
            mSearchAlgorithm.cancel(false);
            mSearchAlgorithm.doSearch(mQuery, mCb);
        }
    }

    void refreshSearchResult() {
        if (TextUtils.isEmpty(mQuery)) {
            return;
        }
        // If play store continues auto updating an app, we want to show partial result.
        mSearchAlgorithm.cancel(false);
        mSearchAlgorithm.doSearch(mQuery, mCb);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // Skip if it's not the right action
        if (actionId != EditorInfo.IME_ACTION_SEARCH) {
            return false;
        }
        // Skip if the query is empty
        String query = v.getText().toString();

        return !query.isEmpty() && mLauncher.startActivitySafely(v, createMarketSearchIntent(query), null);
    }

    @Override
    public boolean onBackKey() {
        // Only hide the search field if there is no query
        String query = Utilities.trim(mInput.getEditableText().toString());
        if (query.isEmpty()) {
            reset();
            return true;
        }
        return false;
    }

    /**
     * Resets the search bar state.
     */
    public void reset() {
        unfocusSearchField();
        mCb.clearSearchResult();
        mInput.setText("");
        mQuery = null;
        hideKeyboard();
    }

    private void hideKeyboard() {
        mInputMethodManager.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
    }

    private void unfocusSearchField() {
        View nextFocus = mInput.focusSearch(View.FOCUS_DOWN);
        if (nextFocus != null) {
            nextFocus.requestFocus();
        }
    }

    /**
     * Focuses the search field to handle key events.
     */
    void focusSearchField() {
        mInput.showKeyboard();
    }

    /**
     * Returns whether the search field is focused.
     */
    boolean isSearchFieldFocused() {
        return mInput.isFocused();
    }

    /**
     * Creates a new market search intent.
     */
    Intent createMarketSearchIntent(String query) {
        Uri marketSearchUri = Uri.parse("market://search")
                .buildUpon()
                .appendQueryParameter("c", "apps")
                .appendQueryParameter("q", query)
                .build();
        return new Intent(Intent.ACTION_VIEW).setData(marketSearchUri);
    }

    /**
     * Callback for getting search results.
     */
    interface Callbacks {

        /**
         * Called when the search is complete.
         *
         * @param apps sorted list of matching components or null if in case of failure.
         */
        void onSearchResult(String query, ArrayList<ComponentKey> apps);

        /**
         * Called when the search results should be cleared.
         */
        void clearSearchResult();
    }
}