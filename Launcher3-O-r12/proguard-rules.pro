-keep,allowshrinking,allowoptimization class com.enrico.launcher3.** {
  *;
}

-keep class com.enrico.launcher3.allapps.AllAppsBackgroundDrawable {
  public void setAlpha(int);
  public int getAlpha();
}

-keep class com.enrico.launcher3.BaseRecyclerViewFastScrollBar {
  public void setThumbWidth(int);
  public int getThumbWidth();
  public void setTrackWidth(int);
  public int getTrackWidth();
}

-keep class com.enrico.launcher3.BaseRecyclerViewFastScrollPopup {
  public void setAlpha(float);
  public float getAlpha();
}

-keep class com.enrico.launcher3.ButtonDropTarget {
  public int getTextColor();
}

-keep class com.enrico.launcher3.CellLayout {
  public float getBackgroundAlpha();
  public void setBackgroundAlpha(float);
}

-keep class com.enrico.launcher3.CellLayout$LayoutParams {
  public void setWidth(int);
  public int getWidth();
  public void setHeight(int);
  public int getHeight();
  public void setX(int);
  public int getX();
  public void setY(int);
  public int getY();
}

-keep class com.enrico.launcher3.dragndrop.DragLayer$LayoutParams {
  public void setWidth(int);
  public int getWidth();
  public void setHeight(int);
  public int getHeight();
  public void setX(int);
  public int getX();
  public void setY(int);
  public int getY();
}

-keep class com.enrico.launcher3.FastBitmapDrawable {
  public void setDesaturation(float);
  public float getDesaturation();
  public void setBrightness(float);
  public float getBrightness();
}

-keep class com.enrico.launcher3.PreloadIconDrawable {
  public float getAnimationProgress();
  public void setAnimationProgress(float);
}

-keep class com.enrico.launcher3.pageindicators.CaretDrawable {
  public float getCaretProgress();
  public void setCaretProgress(float);
}

-keep class com.enrico.launcher3.Workspace {
  public float getBackgroundAlpha();
  public void setBackgroundAlpha(float);
}

# Proguard will strip new callbacks in LauncherApps.Callback from
# WrappedCallback if compiled against an older SDK. Don't let this happen.
-keep class com.enrico.launcher3.compat.** {
  *;
}

-keep class !android.support.v7.view.menu.*MenuBuilder*, android.support.v7.** { *; }
-keep interface android.support.v7.* { *; }