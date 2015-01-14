/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Source: https://github.com/stewjacks/complex-overlay-tiles
 */

package com.iogistics.complexoverlaytiles;

import android.content.*;
import android.graphics.*;
import android.graphics.Paint.*;

import com.codefororlando.transport.bikeorlando.*;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.*;

import java.io.*;
import java.util.*;

/**
 * @author Stewart Jackson
 * @author Ian Thomas <toxicbakery@gmail.com>
 */
public class CustomTileProvider implements TileProvider {

    private static final int DEFAULT_TILE_SIZE = 256;

    private final SphericalMercatorProjection mProjection;
    private final int scale;
    private final int dimension;
    private final ArrayList<ArrayList<LatLng>> routes;
    private final Paint paint;

    public CustomTileProvider(Context context, ArrayList<ArrayList<LatLng>> routes) {
        scale = 1;
        dimension = scale * DEFAULT_TILE_SIZE;

        this.routes = routes;
        mProjection = new SphericalMercatorProjection(DEFAULT_TILE_SIZE);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(context.getResources().getColor(R.color.colorRoute));
        paint.setStrokeCap(Cap.ROUND);
        paint.setStrokeJoin(Join.ROUND);
        paint.setShadowLayer(0, 0, 0, 0);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        Matrix matrix = new Matrix();
        
        /*
         * The scale factor in the transformation matrix is 1/10 here because I scale up the tiles for drawing.
         * Why? Well, the spherical mercator projection doesn't seem to quite provide the resolution I need for
         * scaling up at high zoom levels. This bypasses it without needing a higher tile resolution.
         */
        float scale = ((float) Math.pow(2, zoom) * this.scale / 10);
        matrix.postScale(scale, scale);
        matrix.postTranslate(-x * dimension, -y * dimension);

        Bitmap bitmap = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888); //save memory on old
        // phones
        Canvas c = new Canvas(bitmap);
        c.setMatrix(matrix);
        drawCanvasForZoom(c, zoom);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        return new Tile(dimension, dimension, byteArrayOutputStream.toByteArray());
    }

    /**
     * Draw points inside the current projection determined by a Spherical Mercator Projection.
     *
     * @param canvas to draw the current tile on
     * @param zoom   of the current map projection
     */
    private void drawCanvasForZoom(Canvas canvas, int zoom) {

        paint.setStrokeWidth(getLineWidth(zoom));
        paint.setAlpha(getAlpha(zoom));

        Path path = new Path();

        if (routes != null) {
            for (ArrayList<LatLng> route : routes) {
                if (route != null && route.size() > 1) {

                    Point screenPt1 = mProjection.toPoint(route.get(0)); //first point
                    MarkerOptions m = new MarkerOptions();
                    m.position(route.get(0));
                    path.moveTo((float) screenPt1.x * 10, (float) screenPt1.y * 10);
                    for (int j = 1; j < route.size(); j++) {
                        Point screenPt2 = mProjection.toPoint(route.get(j));
                        path.lineTo((float) screenPt2.x * 10, (float) screenPt2.y * 10);
                    }
                }
            }
        }

        canvas.drawPath(path, paint);
    }

    /**
     * Calculate line width relative to zoom.
     *
     * @param zoom of the current map projection
     * @return relative line width
     */
    private float getLineWidth(int zoom) {
        switch (zoom) {
            case 21:
            case 20:
                return 0.0001f;
            case 19:
                return 0.00025f;
            case 18:
                return 0.0005f;
            case 17:
                return 0.0005f;
            case 16:
                return 0.001f;
            case 15:
                return 0.001f;
            case 14:
                return 0.001f;
            case 13:
                return 0.002f;
            case 12:
                return 0.003f;
            default:
                return 0f;
        }
    }

    /**
     * Calculate alpha relative to zoom.
     *
     * @param zoom of the current map projection
     * @return relative alpha
     */
    private int getAlpha(int zoom) {

        switch (zoom) {
            case 20:
                return 140;
            case 19:
                return 140;
            case 18:
                return 140;
            case 17:
                return 140;
            case 16:
                return 180;
            case 15:
                return 180;
            case 14:
                return 180;
            default:
                return 255;
        }
    }

}
