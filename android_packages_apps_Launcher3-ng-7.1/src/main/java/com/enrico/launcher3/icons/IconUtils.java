package com.enrico.launcher3.icons;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.UserHandle;

import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.LauncherAppState;
import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.compat.LauncherActivityInfoCompat;
import com.enrico.launcher3.compat.UserHandleUtil;
import com.enrico.launcher3.graphics.ShadowGenerator;
import com.enrico.launcher3.notifications.NotificationsDotListener;

import static com.enrico.launcher3.Utilities.getPrefs;

/**
 * Created by Enrico on 10/08/2017.
 */

public class IconUtils {

    public static final String BADGE_POSITION_KEY = "pref_badgePosition";
    public static final String BADGE_SHADOW_KEY = "pref_badgeShadow";
    public static final String ICON_PACK_PREFERENCE_KEY = "pref_iconPackPackage";
    public static final String ROUND_ICONS_KEY = "pref_roundIcons";

    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }

    //are round icons enabled?
    static boolean isRoundIcon(Context context) {
        String choice = Utilities.getPrefs(context)
                .getString(IconUtils.ROUND_ICONS_KEY, String.valueOf(28));
        int value = Integer.parseInt(choice);
        return value != IconProvider.NONE && Utilities.getPrefs(context).getString(IconUtils.ICON_PACK_PREFERENCE_KEY, "").isEmpty();
    }

    public static boolean areColorfulRoundIcons(Context context) {

        String choice = Utilities.getPrefs(context)
                .getString(IconUtils.ROUND_ICONS_KEY, String.valueOf(28));
        int value = Integer.parseInt(choice);
        return value == IconProvider.ROUND_COLORFUL;
    }

    //create round icon enrico's style
    static Bitmap createRoundIcon(Context context, LauncherActivityInfoCompat info) {

        //get default icon
        Bitmap defaultIcon = IconCache.getIconsHandler(context).getDrawableIconForPackage(info.getComponentName());

        //calculate dimensions
        //-1 to take into account the shadow layer
        int w = defaultIcon.getWidth();
        int h = defaultIcon.getHeight();
        int r = w / 2 - 1;

        //create bitmap
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        //draw a circle of the same dimensions
        Canvas canvas = new Canvas(b);
        Paint paint = new Paint();
        paint.setColor(RoundIconsColorProvider.getRoundIconColor(context, info.getComponentName().getPackageName()));
        final int SHADOW_COLOR = 0x80000000;
        paint.setShadowLayer(0.5f, 1, 1, SHADOW_COLOR);
        paint.setAntiAlias(true);
        canvas.drawCircle(r, r, r, paint);

        //scale default icon and center inside the canvas
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(defaultIcon, r, r, true);
        canvas.drawBitmap(scaledBitmap, (r * 2 - scaledBitmap.getWidth()) / 2, (r * 2 - scaledBitmap.getHeight()) / 2, paint);

        return b;
    }

    //create rounded badge
    static Bitmap createBadge(Context context, Bitmap b) {

        //get icons colors
        int color = IconPalette.getDominantColor(context, b);

        Bitmap b2 = b.copy(Bitmap.Config.ARGB_8888, true);

        int iconSize = b2.getWidth();

        Canvas c = new Canvas(b2);

        Paint badgePaint = new Paint();
        badgePaint.setColor(color);
        badgePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        badgePaint.setAntiAlias(true);

        int radius = iconSize / 10;
        resolveBadgePosition(context, c, iconSize, radius, badgePaint);

        return b2;
    }

    //resolve badge position
    private static void resolveBadgePosition(Context context, Canvas canvas, int width, int radius, Paint badgePaint) {
        String gravity = getPrefs(context)
                .getString(BADGE_POSITION_KEY, String.valueOf(20));

        int cx, cy;

        final int SHADOW_COLOR = 0x80000000;

        switch (Integer.parseInt(gravity)) {

            //top right
            default:
            case 20:

                cx = width - radius*2;
                cy = radius*2;

                if (isBadgeShadowEnabled(context)) {

                    badgePaint.setShadowLayer(2.0f, -1, 1, SHADOW_COLOR);
                }

                canvas.drawCircle(cx, cy, radius, badgePaint);
                break;

            //top left
            case 21:

                cx = radius*2;
                cy = radius*2;

                if (isBadgeShadowEnabled(context)) {

                    badgePaint.setShadowLayer(2.0f, 1, 1, SHADOW_COLOR);
                }

                canvas.drawCircle(cx, cy, radius, badgePaint);
                break;

            //bottom right
            case 22:

                cx = width - radius*2;
                cy = width - radius*2;

                if (isBadgeShadowEnabled(context)) {

                    badgePaint.setShadowLayer(2.0f, -1, 1, SHADOW_COLOR);
                }

                canvas.drawCircle(cx, cy, radius, badgePaint);
                break;

            //bottom left
            case 23:

                cx = radius*2;
                cy = width - radius*2;

                if (isBadgeShadowEnabled(context)) {

                    badgePaint.setShadowLayer(2.0f, 1, 1, SHADOW_COLOR);
                }

                canvas.drawCircle(cx, cy, radius, badgePaint);
                break;

        }
    }

    //is badge shadow enabled?
    private static boolean isBadgeShadowEnabled(Context context) {
        return getPrefs(context).getBoolean(BADGE_SHADOW_KEY,
                true);
    }

    //draw notification background when an item has notifications
    public static void drawNotificationBackground(Context context, Canvas canvas, int offSetX, int offSetY, Path mClipPath, int strokeWidth, int radius, int shadowOpacity) {
        canvas.save();

        canvas.translate(offSetX, offSetY);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int notificationColor = Color.argb(50, Color.red(Color.BLACK), Color.green(Color.BLACK), Color.blue(Color.BLACK));

        paint.setColor(notificationColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setXfermode(null);

        canvas.drawCircle(radius, radius, radius, paint);
        canvas.clipPath(mClipPath);

        int themeIcon = Utilities.isDarkThemeEnabled(context) ? R.drawable.ic_message_alert_white : R.drawable.ic_message_alert_black;
        int icon = Utilities.areColoredFoldersEnabled(context) ? R.drawable.ic_message_alert_white : themeIcon;
        Bitmap b = BitmapFactory.decodeResource(context.getResources(), icon);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.TRANSPARENT);
        paint.setShadowLayer(strokeWidth, 0, strokeWidth, Color.argb(shadowOpacity, 0, 0, 0));
        canvas.drawCircle(radius, radius, radius, paint);
        canvas.drawBitmap(b, (radius * 2 - b.getWidth()) / 2, (radius * 2 - b.getHeight()) / 2, paint);

        canvas.restore();
    }

    public static Bitmap createIconBitmap(Cursor c, int iconIndex, Context context) {
        byte[] data = c.getBlob(iconIndex);
        try {
            return createIconBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), context);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a bitmap suitable for the all apps view. If the package or the resource do not
     * exist, it returns null.
     */
    public static Bitmap createIconBitmap(String packageName, String resourceName,
                                          Context context) {
        PackageManager packageManager = context.getPackageManager();
        // the resource
        try {
            Resources resources = packageManager.getResourcesForApplication(packageName);
            if (resources != null) {
                final int id = resources.getIdentifier(resourceName, null, null);

                boolean hasNotifications = NotificationsDotListener.hasNotifications(packageName);

                return createIconBitmap(resources.getDrawableForDensity(id, LauncherAppState.getInstance().getInvariantDeviceProfile().fillResIconDpi, context.getTheme()), context, hasNotifications, packageName);
            }

        } catch (Exception e) {
            // Icon not found.
            e.printStackTrace();
        }
        return null;
    }

    private static int getIconBitmapSize() {
        return LauncherAppState.getInstance().getInvariantDeviceProfile().iconBitmapSize;
    }

    /**
     * Returns a bitmap which is of the appropriate size to be displayed as an icon
     */
    public static Bitmap createIconBitmap(Bitmap icon, Context context) {
        final int iconBitmapSize = getIconBitmapSize();
        if (iconBitmapSize == icon.getWidth() && iconBitmapSize == icon.getHeight()) {
            return icon;
        }
        return createIconBitmap(new BitmapDrawable(context.getResources(), icon), context, false, null);
    }

    /**
     * Returns a bitmap suitable for the all apps view.
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context, boolean notificationBadge, String packageName) {
        return createIconBitmap(icon, context, 1.0f /* scale */, notificationBadge, packageName);
    }

    /**
     * @param scale the scale to apply before drawing {@param icon} on the canvas
     */
    private static Bitmap createIconBitmap(Drawable icon, Context context, float scale, boolean notificationBadge, String packageName) {
        synchronized (sCanvas) {
            final int iconBitmapSize = getIconBitmapSize();

            int width = iconBitmapSize;
            int height = iconBitmapSize;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap != null && bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }

            // no intrinsic size --> use default size

            final Bitmap bitmap = Bitmap.createBitmap(iconBitmapSize, iconBitmapSize,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (iconBitmapSize - width) / 2;
            final int top = (iconBitmapSize - height) / 2;

            @SuppressWarnings("all") // suppress dead code warning
            final boolean debug = false;
            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left + width, top + height);
            canvas.save();
            canvas.scale(scale, scale, iconBitmapSize / 2, iconBitmapSize / 2);
            icon.draw(canvas);
            canvas.restore();
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            if (notificationBadge) {
                return createBadge(context, bitmap);
            }

            return bitmap;
        }
    }

    /**
     * Returns a bitmap suitable for the all apps view. The icon is badged for {@param user}.
     * The bitmap is also visually normalized with other icons.
     */
    static Bitmap createBadgedIconBitmap(
            Drawable icon, UserHandle user, Context context) {
        float scale = IconNormalizer.getInstance().getScale(icon, null);
        Bitmap bitmap = createIconBitmap(icon, context, scale, false, null);
        return badgeIconForUser(bitmap, user, context);
    }

    /**
     * Badges the provided icon with the user badge if required.
     */
    public static Bitmap badgeIconForUser(Bitmap icon, UserHandle user, Context context) {
        if (user != null
                && !UserHandleUtil.myUserHandle().equals(user)) {
            BitmapDrawable drawable = new Utilities.FixedSizeBitmapDrawable(icon);
            Drawable badged = context.getPackageManager().getUserBadgedIcon(
                    drawable, user);
            if (badged instanceof BitmapDrawable) {
                return ((BitmapDrawable) badged).getBitmap();
            } else {
                return createIconBitmap(badged, context, false, null);
            }
        } else {
            return icon;
        }
    }

    /**
     * Creates a normalized bitmap suitable for the all apps view. The bitmap is also visually
     * normalized with other icons and has enough spacing to add shadow.
     */
    public static Bitmap createScaledBitmapWithoutShadow(Drawable icon, Context context) {
        RectF iconBounds = new RectF();
        float scale = IconNormalizer.getInstance().getScale(icon, iconBounds);
        scale = Math.min(scale, ShadowGenerator.getScaleForBounds(iconBounds));
        return createIconBitmap(icon, context, scale, false, null);
    }

    /**
     * Adds a shadow to the provided icon. It assumes that the icon has already been scaled using
     * {@link #createScaledBitmapWithoutShadow(Drawable, Context)}
     */
    public static Bitmap addShadowToIcon(Bitmap icon) {
        return ShadowGenerator.getInstance().recreateIcon(icon);
    }

    /**
     * Adds the {@param badge} on top of {@param srcTgt} using the badge dimensions.
     */
    public static Bitmap badgeWithBitmap(Bitmap srcTgt, Bitmap badge, Context context) {
        int badgeSize = context.getResources().getDimensionPixelSize(R.dimen.profile_badge_size);
        synchronized (sCanvas) {
            sCanvas.setBitmap(srcTgt);
            sCanvas.drawBitmap(badge, new Rect(0, 0, badge.getWidth(), badge.getHeight()),
                    new Rect(srcTgt.getWidth() - badgeSize,
                            srcTgt.getHeight() - badgeSize, srcTgt.getWidth(), srcTgt.getHeight()),
                    new Paint(Paint.FILTER_BITMAP_FLAG));
            sCanvas.setBitmap(null);
        }
        return srcTgt;
    }

    //get normalized icon for edit icon dialog and for reset purposes
    public static Bitmap getEditIconBitmap(Context context, IconsHandler mIconsHandler, ItemInfo info) {
        Bitmap defaultIcon = mIconsHandler.getDrawableIconForPackage(info.getTargetComponent());
        return createBadgedIconBitmap(new BitmapDrawable(context.getResources(), defaultIcon), info.user, context);
    }
}
