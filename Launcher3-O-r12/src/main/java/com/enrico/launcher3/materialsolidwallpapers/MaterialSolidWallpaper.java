package com.enrico.launcher3.materialsolidwallpapers;

import android.app.Activity;
import android.app.WallpaperManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;

import com.enrico.launcher3.ImmersiveUtils;
import com.enrico.launcher3.R;
import com.enrico.launcher3.settings.SettingsTheme;

/**
 * Created by Enrico on 21/07/2017.
 */

public class MaterialSolidWallpaper extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveUtils.toggleHideyBar(this, false);

        //apply theme
        SettingsTheme.apply(this);

        setContentView(R.layout.material_wallpapers);

        ImageButton setWallpaper = findViewById(R.id.set_wallpaper);
        ImageButton close = findViewById(R.id.close);
        final View material_view = findViewById(R.id.material_wallpaper);

        setWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                WallpaperManager wm = WallpaperManager.getInstance(getApplicationContext());

                int color = SolidWallpaperUtils.getColorViewColor(material_view);

                SolidWallpaperUtils.setWallpaper(MaterialSolidWallpaper.this, wm, color);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        initRecyclerView();
    }

    //method to init recycler view
    private void initRecyclerView() {

        RecyclerView recyclerView = findViewById(R.id.material_rv);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        //set the recycler view adapter and pass arguments to the adapter to it
        recyclerView.setAdapter(new RecyclerViewAdapter(this, SolidWallpaperUtils.material_colors));
    }

    //fix Immersive mode navigation becomes sticky after minimise-restore
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ImmersiveUtils.toggleHideyBar(this, true);
        }
    }
}
