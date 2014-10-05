package com.Orlando.opensource.bikeorlando.overlay;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by tencent on 9/23/2014.
 * <p/>
 * <a href="http://stackoverflow.com/questions/20382823/google-maps-api-v2-draw-part-of-circle-on-mapfragment
 * /20408460#20408460">Implementation concept source</a>
 */
public class TileProjection {

    private int tileSize;
    private int x;
    private int y;
    private int zoom;

    private DoublePoint pixelOrigin;
    private double pixelsPerLonDegree;
    private double pixelsPerLonRadian;

    public TileProjection(int tileSize, int x, int y, int zoom) {
        this.tileSize = tileSize;
        this.x = x;
        this.y = y;
        this.zoom = zoom;

        pixelOrigin = new DoublePoint(tileSize / 2, tileSize / 2);
        pixelsPerLonDegree = tileSize / 360d;
        pixelsPerLonRadian = tileSize / (2 * Math.PI);
    }

    /**
     * Return value reduced to min and max if outside one of these bounds.
     *
     * @param value
     * @param min
     * @param max
     * @return
     */
    private static double bound(double value, double min, double max) {
        value = Math.max(value, min);
        value = Math.min(value, max);
        return value;
    }

    /**
     * Get the dimensions of the Tile in LatLng coordinates
     *
     * @return
     */
    public LatLngBounds getTileBounds() {
        DoublePoint tileSW = new DoublePoint(x * tileSize, (y + 1) * tileSize);
        DoublePoint worldSW = getPixelToWorldCoordinate(tileSW);
        LatLng SW = worldCoordinateToLatLng(worldSW);
        DoublePoint tileNE = new DoublePoint((x + 1) * tileSize, y * tileSize);
        DoublePoint worldNE = getPixelToWorldCoordinate(tileNE);
        LatLng NE = worldCoordinateToLatLng(worldNE);
        return new LatLngBounds(SW, NE);
    }

    /**
     * Calculate the pixel coordinates inside a tile, relative to the left upper corner (origin) of the tile.
     *
     * @param latLng
     * @param point
     */
    public void latLngToPoint(LatLng latLng, DoublePoint point) {
        latLngToWorldCoordinate(latLng, point);
        worldToPixelCoordinate(point, point);
        point.x -= x * tileSize;
        point.y -= y * tileSize;
    }

    private DoublePoint getPixelToWorldCoordinate(DoublePoint pixelCoordinate) {
        int numTiles = 1 << zoom;
        return new DoublePoint(pixelCoordinate.x / numTiles, pixelCoordinate.y / numTiles);
    }

    /**
     * Transform the world coordinates into pixel-coordinates relative to the whole tile-area. (i.e. the coordinate
     * system that spans all tiles.)
     * <p/>
     * Takes the resulting point as parameter, to avoid creation of new objects.
     *
     * @param worldCoordinate
     * @param pixelCoordinate
     */
    private void worldToPixelCoordinate(DoublePoint worldCoordinate, DoublePoint pixelCoordinate) {
        int numTiles = 1 << zoom;
        pixelCoordinate.x = worldCoordinate.x * numTiles;
        pixelCoordinate.y = worldCoordinate.y * numTiles;
    }

    /**
     * Get the coordinates in a system describing the whole globe in a coordinate range from 0 to TILE_SIZE (type
     * double).
     * <p/>
     * Takes the resulting point as parameter, to avoid creation of new objects.
     *
     * @param worldCoordinate
     * @return
     */
    private LatLng worldCoordinateToLatLng(DoublePoint worldCoordinate) {
        DoublePoint origin = pixelOrigin;
        double lng = (worldCoordinate.x - origin.x) / pixelsPerLonDegree;
        double latRadians = (worldCoordinate.y - origin.y) / -pixelsPerLonRadian;
        double lat = Math.toDegrees(2 * Math.atan(Math.exp(latRadians)) - Math.PI / 2);
        return new LatLng(lat, lng);
    }

    private void latLngToWorldCoordinate(LatLng latLng, DoublePoint worldCoordinate) {
        DoublePoint origin = pixelOrigin;

        worldCoordinate.x = origin.x + latLng.longitude * pixelsPerLonDegree;

        /**
         * Truncating to 0.9999 effectively limits latitude to 89.189. This is
         * about a third of a tile past the edge of the world tile.
         */
        double sinY = bound(Math.sin(Math.toRadians(latLng.latitude)), -0.9999, 0.9999);
        worldCoordinate.y = origin.y + 0.5 * Math.log((1 + sinY) / (1 - sinY)) * -pixelsPerLonRadian;
    }

    /**
     * A Point in an x/y coordinate system with coordinates of type double.
     */
    public static class DoublePoint {

        double x;
        double y;

        public DoublePoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

}
