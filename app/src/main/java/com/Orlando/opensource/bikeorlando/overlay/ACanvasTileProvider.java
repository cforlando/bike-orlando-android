package com.Orlando.opensource.bikeorlando.overlay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;

/**
 * Created by tencent on 9/23/2014.
 * <p/>
 * <a href="http://stackoverflow.com/questions/20382823/google-maps-api-v2-draw-part-of-circle-on-mapfragment
 * /20408460#20408460">Implementation concept source</a>
 */
public abstract class ACanvasTileProvider implements TileProvider {

    static int TILE_SIZE = 256;

    int tileSize = TILE_SIZE;

    BitmapThreadLocal bitmapThreadLocal;

    public ACanvasTileProvider() {
        super();
        bitmapThreadLocal = new BitmapThreadLocal();
    }

    private static byte[] bitmapToByteArray(Bitmap bm) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] data = bos.toByteArray();
        return data;
    }

    abstract void onDraw(Canvas canvas, TileProjection projection);

    @Override
    public Tile getTile(int x, int y, int zoom) {
        TileProjection projection = new TileProjection(tileSize, x, y, zoom);

        byte[] data;
        Bitmap image = getBitmap();
        Canvas canvas = new Canvas(image);
        onDraw(canvas, projection);
        data = bitmapToByteArray(image);
        Tile tile = new Tile(TILE_SIZE, TILE_SIZE, data);
        return tile;
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    Bitmap getBitmap() {
        Bitmap bitmap = bitmapThreadLocal.get();
        bitmap.eraseColor(Color.TRANSPARENT);
        return bitmap;
    }

    class BitmapThreadLocal extends ThreadLocal<Bitmap> {

        @Override
        protected Bitmap initialValue() {
            Bitmap image = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE,
                    Bitmap.Config.ARGB_8888);
            return image;
        }
    }
}
