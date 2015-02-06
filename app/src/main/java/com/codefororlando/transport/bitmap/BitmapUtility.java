package com.codefororlando.transport.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.util.TypedValue;

public class BitmapUtility {

    /**
     * Create a square bitmap with a colored circle, transparent background, and an overlay bitmap
     * centered inside the circle.
     *
     * @param context              application context
     * @param colorBackground      color of circle
     * @param bitmap               overlay bitmap
     * @param widthAndHeightPixels width and height of the bitmap in pixels.
     * @return colored circle with centered overlay and transparent background
     */
    public static Bitmap createBitmapWithCircleAndOverlay(Context context, @ColorRes int colorBackground, @DrawableRes int bitmap, int widthAndHeightPixels) {

        // Background paint for drawing the circle
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(context.getResources().getColor(colorBackground));

        // Gather resources for drawing and calculations
        final int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthAndHeightPixels, context.getResources().getDisplayMetrics());
        final Bitmap icon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        final Bitmap overlay = BitmapFactory.decodeResource(context.getResources(), bitmap);
        final Canvas canvas = new Canvas(icon);

        // Determine scale
        final float scale;
        if (overlay.getWidth() == overlay.getHeight()) {
            scale = 0.65f;
        } else if (overlay.getWidth() > overlay.getHeight()) {
            scale = overlay.getWidth() / icon.getWidth() * 0.65f;
        } else {
            scale = overlay.getHeight() / icon.getHeight() * 0.65f;
        }

        // Calculate applied scale and centering
        final float scaledWidth = icon.getWidth() * scale;
        final float scaledHeight = icon.getHeight() * scale;
        final float offsetWidth = (icon.getWidth() - scaledWidth) / 2f;
        final float offsetHeight = (icon.getHeight() - scaledHeight) / 2f;

        // Generate the drawing rectangles for scaling and alignment to center
        final Rect rectOverlaySrc = new Rect(0, 0, overlay.getWidth(), overlay.getHeight());
        final RectF rectOverlayDst = new RectF(offsetWidth, offsetHeight, scaledWidth + offsetWidth, scaledHeight + offsetHeight);
        final float half = size / 2f;

        // Draw the circle and bitmap
        canvas.drawCircle(half, half, half, paint);
        canvas.drawBitmap(overlay, rectOverlaySrc, rectOverlayDst, null);

        return icon;
    }

}
