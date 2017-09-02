package com.enrico.launcher3.materialsolidwallpapers;

import android.app.WallpaperManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;

import com.enrico.launcher3.ImmersiveUtils;
import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;

/**
 * Created by Enrico on 21/07/2017.
 */

public class MaterialSolidWallpaper extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveUtils.toggleHideyBar(this, false);

        //apply theme
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getBaseContext(), this.getTheme());
        Utilities.applyTheme(contextThemeWrapper, this);

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

        //create an array of colors that will populate the recycler view
        Integer[] material_colors = new Integer[]{

                R.color.material_red_400,
                R.color.material_pink_400,
                R.color.material_purple_400,
                R.color.material_deepPurple_400,
                R.color.material_indigo_400,
                R.color.material_blue_400,
                R.color.material_lightBlue_400,
                R.color.material_cyan_400,
                R.color.material_teal_400,
                R.color.material_green_400,
                R.color.material_amber_400,
                R.color.material_orange_400,
                R.color.material_deepOrange_400,
                R.color.material_brown_400,
                R.color.material_blueGrey_400,
                R.color.black,
                R.color.white
        };

        //set the recycler view adapter and pass arguments to the adapter to it
        recyclerView.setAdapter(new RecyclerViewAdapter(this, material_colors));
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
