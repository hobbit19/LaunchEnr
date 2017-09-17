/*
 * Copyright (C) 2017 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enrico.launcher3;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MultiSelectRecyclerViewActivity extends AppCompatActivity implements MultiSelectRecyclerViewAdapter.ItemClickListener {

    private List<ResolveInfo> mInstalledPackages;
    private Toolbar mToolbar;
    private MultiSelectRecyclerViewAdapter mAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.hide_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        updateHiddenApps();
        return super.onOptionsItemSelected(item);
    }

    private void updateHiddenApps() {

        mAdapter.addSelectionsToHideList(MultiSelectRecyclerViewActivity.this);
        LauncherAppState appState = LauncherAppState.getInstance();
        if (appState != null) {
            appState.getModel().forceReload();
        }

        navigateUpTo(new Intent(MultiSelectRecyclerViewActivity.this, Launcher.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //apply activity's theme if dark theme is enabled
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getBaseContext(), this.getTheme());

        Utilities.applyTheme(contextThemeWrapper, this);

        setContentView(R.layout.activity_multiselect);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.material_dark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.material_dark));

        //set the toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        //provide back navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //go back
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Set<String> mSelectedApps = PreferenceManager.getDefaultSharedPreferences(MultiSelectRecyclerViewActivity.this).getStringSet(Utilities.KEY_HIDDEN_APPS_SET, null);
        if (mSelectedApps != null) {
            if (mSelectedApps.size() != 0) {
                getSupportActionBar().setTitle(String.valueOf(mSelectedApps.size()) + getString(R.string.hide_app_selected));
            } else {
                getSupportActionBar().setTitle(getString(R.string.hidden_app));
            }
        }

        mInstalledPackages = getInstalledApps();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MultiSelectRecyclerViewActivity.this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new MultiSelectRecyclerViewAdapter(MultiSelectRecyclerViewActivity.this, mInstalledPackages, this);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClicked(int position) {

        mAdapter.toggleSelection(mToolbar, position, mInstalledPackages.get(position).activityInfo.packageName);
    }

    private List<ResolveInfo> getInstalledApps() {
        //get a list of installed apps.
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> installedApps = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        Collections.sort(installedApps, new ResolveInfo.DisplayNameComparator(packageManager));
        return installedApps;
    }
}