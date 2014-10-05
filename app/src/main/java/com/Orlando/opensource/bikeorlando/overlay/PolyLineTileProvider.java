package com.Orlando.opensource.bikeorlando.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by tencent on 9/23/2014.
 */
public class PolyLineTileProvider extends ACanvasTileProvider {

    Paint paint;

    public PolyLineTileProvider() {
        super();

        paint = new Paint();
        paint.setColor(0xff00ff00);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
    }

    @Override
    void onDraw(Canvas canvas, TileProjection projection) {
        canvas.drawCircle(0,0,10, paint);
    }

}
